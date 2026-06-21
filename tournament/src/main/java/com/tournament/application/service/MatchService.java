package com.tournament.application.service;

import com.tournament.application.dto.request.ResolveDisputeRequest;
import com.tournament.application.dto.request.SubmitResultRequest;
import com.tournament.application.dto.response.MatchDetailResponse;
import com.tournament.application.event.MatchCompletedEvent;
import com.tournament.domain.entity.*;
import com.tournament.domain.enums.MatchStatus;
import com.tournament.domain.enums.RoundStatus;
import com.tournament.domain.repository.*;
import com.tournament.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MatchService {

    private final MatchRepository        matchRepository;
    private final MatchResultRepository  matchResultRepository;
    private final RegistrationRepository registrationRepository;
    private final RoundRepository        roundRepository;
    private final BracketRepository      bracketRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public MatchDetailResponse getMatch(Long matchId) {
        Match match = findMatch(matchId);
        MatchResult result = matchResultRepository.findByMatchId(matchId).orElse(null);
        return MatchDetailResponse.from(match, result);
    }

    // SCHEDULED → IN_PROGRESS
    public MatchDetailResponse startMatch(Long matchId, Long userId) {
        Match match = findMatch(matchId);

        if (match.getStatus() != MatchStatus.SCHEDULED) {
            throw new InvalidMatchStateException(
                String.format("El partido no puede iniciarse desde el estado %s", match.getStatus()));
        }
        validateParticipant(match, userId);

        match.setStatus(MatchStatus.IN_PROGRESS);
        matchRepository.save(match);
        return MatchDetailResponse.from(match, null);
    }

    // IN_PROGRESS → RESULT_SUBMITTED
    public MatchDetailResponse submitResult(Long matchId, SubmitResultRequest req, Long userId) {
        Match match = findMatch(matchId);

        if (match.getStatus() != MatchStatus.IN_PROGRESS) {
            throw new InvalidMatchStateException(
                String.format("Solo se puede reportar resultado de un partido EN_PROGRESO, estado actual: %s", match.getStatus()));
        }
        validateParticipant(match, userId);

        Registration winner = registrationRepository.findById(req.getWinnerId())
                .orElseThrow(() -> new RegistrationNotFoundException(req.getWinnerId()));

        if (!isParticipant(match, winner)) {
            throw new InvalidMatchStateException("El ganador debe ser uno de los participantes del partido");
        }

        MatchResult result = MatchResult.builder()
                .match(match)
                .winner(winner)
                .score1(req.getScore1())
                .score2(req.getScore2())
                .reportedByUserId(userId)
                .submittedAt(LocalDateTime.now())
                .build();

        if (req.getEvidenceUrl() != null && !req.getEvidenceUrl().isBlank()) {
            ResultEvidence evidence = ResultEvidence.builder()
                    .matchResult(result)
                    .evidenceUrl(req.getEvidenceUrl())
                    .uploadedByUserId(userId)
                    .build();
            result.getEvidences().add(evidence);
        }

        matchResultRepository.save(result);
        match.setStatus(MatchStatus.RESULT_SUBMITTED);
        match.setWinner(winner);
        matchRepository.save(match);

        return MatchDetailResponse.from(match, result);
    }

    // RESULT_SUBMITTED → DISPUTED
    public MatchDetailResponse disputeResult(Long matchId, String reason, Long userId) {
        Match match = findMatch(matchId);

        if (match.getStatus() != MatchStatus.RESULT_SUBMITTED) {
            throw new InvalidMatchStateException(
                "Solo se puede disputar un resultado en estado RESULT_SUBMITTED");
        }
        validateParticipant(match, userId);

        MatchResult result = matchResultRepository.findByMatchId(matchId)
                .orElseThrow(() -> new InvalidMatchStateException("No se encontró el resultado del partido"));

        if (result.getReportedByUserId().equals(userId)) {
            throw new InvalidMatchStateException("No puedes disputar un resultado que tú mismo reportaste");
        }

        result.setDisputeReason(reason);
        result.setDisputeRaisedByUserId(userId);
        matchResultRepository.save(result);

        match.setStatus(MatchStatus.DISPUTED);
        match.setWinner(null);
        matchRepository.save(match);

        return MatchDetailResponse.from(match, result);
    }

    // DISPUTED → COMPLETED (admin resuelve)
    public MatchDetailResponse resolveDispute(Long matchId, ResolveDisputeRequest req, Long adminUserId) {
        Match match = findMatch(matchId);

        if (match.getStatus() != MatchStatus.DISPUTED) {
            throw new InvalidMatchStateException(
                "Solo se puede resolver una disputa en estado DISPUTED");
        }

        Registration winner = registrationRepository.findById(req.getWinnerId())
                .orElseThrow(() -> new RegistrationNotFoundException(req.getWinnerId()));

        if (!isParticipant(match, winner)) {
            throw new InvalidMatchStateException("El ganador debe ser uno de los participantes del partido");
        }

        MatchResult result = matchResultRepository.findByMatchId(matchId)
                .orElseThrow(() -> new InvalidMatchStateException("No se encontró el resultado del partido"));

        result.setWinner(winner);
        result.setAdminNotes(req.getAdminNotes());
        result.setResolvedByUserId(adminUserId);
        result.setResolvedAt(LocalDateTime.now());
        matchResultRepository.save(result);

        match.setWinner(winner);
        match.setStatus(MatchStatus.COMPLETED);
        matchRepository.save(match);

        advanceBracket(match, winner);
        eventPublisher.publishEvent(new MatchCompletedEvent(this, match.getId()));

        return MatchDetailResponse.from(match, result);
    }

    // RESULT_SUBMITTED → COMPLETED (el otro participante confirma)
    public MatchDetailResponse confirmResult(Long matchId, Long userId) {
        Match match = findMatch(matchId);

        if (match.getStatus() != MatchStatus.RESULT_SUBMITTED) {
            throw new InvalidMatchStateException(
                "Solo se puede confirmar un resultado en estado RESULT_SUBMITTED");
        }
        validateParticipant(match, userId);

        MatchResult result = matchResultRepository.findByMatchId(matchId)
                .orElseThrow(() -> new InvalidMatchStateException("No se encontró el resultado del partido"));

        if (result.getReportedByUserId().equals(userId)) {
            throw new InvalidMatchStateException("No puedes confirmar un resultado que tú mismo reportaste");
        }

        match.setStatus(MatchStatus.COMPLETED);
        matchRepository.save(match);

        advanceBracket(match, match.getWinner());
        eventPublisher.publishEvent(new MatchCompletedEvent(this, match.getId()));

        return MatchDetailResponse.from(match, result);
    }

    // --- Helpers ---

    private Match findMatch(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));
    }

    private void validateParticipant(Match match, Long userId) {
        if (!isUserParticipant(match, userId)) {
            throw new UnauthorizedResultException();
        }
    }

    private boolean isUserParticipant(Match match, Long userId) {
        boolean p1 = match.getRegistration1() != null && getUserId(match.getRegistration1()).equals(userId);
        boolean p2 = match.getRegistration2() != null && getUserId(match.getRegistration2()).equals(userId);
        return p1 || p2;
    }

    private boolean isParticipant(Match match, Registration reg) {
        boolean p1 = match.getRegistration1() != null && match.getRegistration1().getId().equals(reg.getId());
        boolean p2 = match.getRegistration2() != null && match.getRegistration2().getId().equals(reg.getId());
        return p1 || p2;
    }

    private Long getUserId(Registration reg) {
        if (reg.isPlayerRegistration()) return reg.getPlayer().getUserId();
        return reg.getTeam().getCaptain().getUserId();
    }

    private void advanceBracket(Match completedMatch, Registration winner) {
        // Colocar ganador en el siguiente partido
        if (completedMatch.getNextMatch() != null) {
            Match nextMatch = matchRepository.findById(completedMatch.getNextMatch().getId())
                    .orElseThrow();
            if (nextMatch.getRegistration1() == null) {
                nextMatch.setRegistration1(winner);
            } else {
                nextMatch.setRegistration2(winner);
            }
            matchRepository.save(nextMatch);
        }

        // Verificar si la ronda completó
        Round round = completedMatch.getRound();
        List<Match> roundMatches = matchRepository.findByRoundIdOrderByPositionAsc(round.getId());
        boolean roundDone = roundMatches.stream()
                .allMatch(m -> m.getStatus() == MatchStatus.COMPLETED || m.getStatus() == MatchStatus.BYE);

        if (roundDone) {
            round.setStatus(RoundStatus.COMPLETED);
            roundRepository.save(round);

            // Si era el partido final (sin siguiente), el bracket está completo
            if (completedMatch.getNextMatch() == null) {
                Bracket bracket = round.getBracket();
                bracket.setComplete(true);
                bracketRepository.save(bracket);
            }
        }
    }
}

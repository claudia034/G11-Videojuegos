package com.tournament.application.service;

import com.tournament.application.dto.request.AdminMatchDecisionRequest;
import com.tournament.application.dto.request.ResolveDisputeRequest;
import com.tournament.application.dto.request.ScheduleMatchRequest;
import com.tournament.application.dto.request.SubmitResultRequest;
import com.tournament.application.dto.response.MatchDetailResponse;
import com.tournament.application.event.MatchCompletedEvent;
import com.tournament.application.format.FormatFactory;
import com.tournament.application.format.TournamentFormatProfile;
import com.tournament.application.format.TournamentFormatStrategy;
import com.tournament.domain.entity.*;
import com.tournament.domain.enums.*;
import com.tournament.domain.repository.*;
import com.tournament.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class MatchService {

    private final MatchRepository        matchRepository;
    private final MatchResultRepository  matchResultRepository;
    private final RegistrationRepository registrationRepository;
    private final RoundRepository        roundRepository;
    private final BracketRepository      bracketRepository;
    private final TournamentRepository   tournamentRepository;
    private final PlayerStatsRepository  playerStatsRepository;
    private final FormatFactory          formatFactory;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public MatchDetailResponse getMatch(Long matchId) {
        Match match = findMatch(matchId);
        MatchResult result = matchResultRepository.findByMatchId(matchId).orElse(null);
        return MatchDetailResponse.from(match, result);
    }

    public MatchDetailResponse scheduleMatch(Long matchId, ScheduleMatchRequest req) {
        Match match = findMatch(matchId);

        if (match.getStatus() == MatchStatus.COMPLETED || match.getStatus() == MatchStatus.BYE) {
            throw new InvalidMatchStateException("No se puede programar un partido completado o BYE");
        }

        match.setScheduledAt(req.getScheduledAt());
        matchRepository.save(match);

        MatchResult result = matchResultRepository.findByMatchId(matchId).orElse(null);
        return MatchDetailResponse.from(match, result);
    }

    public MatchDetailResponse decideMatchAsAdmin(Long matchId, AdminMatchDecisionRequest req, Long adminUserId) {
        Match match = findMatch(matchId);

        if (match.getStatus() == MatchStatus.COMPLETED || match.getStatus() == MatchStatus.BYE) {
            throw new InvalidMatchStateException("No se puede decidir administrativamente un partido completado o BYE");
        }

        Registration winner = registrationRepository.findById(req.getWinnerId())
                .orElseThrow(() -> new RegistrationNotFoundException(req.getWinnerId()));

        if (!isParticipant(match, winner)) {
            throw new InvalidMatchStateException("El ganador debe ser uno de los participantes del partido");
        }

        MatchResult existingResult = matchResultRepository.findByMatchId(matchId).orElse(null);
        MatchResult result = MatchAdminSupport.buildOrUpdateAdministrativeResult(
                match,
                existingResult,
                winner,
                req,
                adminUserId
        );
        matchResultRepository.save(result);

        match.setWinner(winner);
        match.setStatus(MatchStatus.COMPLETED);
        if (match.getScheduledAt() == null) {
            match.setScheduledAt(LocalDateTime.now());
        }
        matchRepository.save(match);

        advanceBracket(match, winner);
        eventPublisher.publishEvent(new MatchCompletedEvent(this, match.getId()));

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

        boolean disputingSubmittedResult = match.getStatus() == MatchStatus.RESULT_SUBMITTED;
        boolean disputingClosedFinal = match.getStatus() == MatchStatus.COMPLETED && match.getNextMatch() == null;

        if (!disputingSubmittedResult && !disputingClosedFinal) {
            throw new InvalidMatchStateException(
                "Solo se puede disputar un resultado pendiente de confirmacion o una final ya cerrada");
        }
        validateParticipant(match, userId);

        MatchResult result = matchResultRepository.findByMatchId(matchId)
                .orElseThrow(() -> new InvalidMatchStateException("No se encontró el resultado del partido"));

        if (result.getReportedByUserId().equals(userId)) {
            throw new InvalidMatchStateException("No puedes disputar un resultado que tú mismo reportaste");
        }

        if (disputingClosedFinal) {
            reopenFinalForAdministrativeReview(match);
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
            if (nextMatch.getRegistration1() != null
                    && nextMatch.getRegistration2() != null
                    && nextMatch.getStatus() == MatchStatus.BYE) {
                nextMatch.setStatus(MatchStatus.SCHEDULED);
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

            Bracket bracket = round.getBracket();
            TournamentFormatStrategy strategy = formatFactory.getFormat(bracket.getTournament().getFormat());
            if (strategy.isComplete(bracket)) {
                bracket.setComplete(true);
                bracketRepository.save(bracket);
                finalizeTournament(bracket.getTournament(), completedMatch);
            }
        }
    }

    private void finalizeTournament(Tournament tournament, Match finalMatch) {
        tournament.setStatus(TournamentStatus.COMPLETED);
        TournamentFormatProfile profile = formatFactory.getFormat(tournament.getFormat()).getProfile();
        assignPrizes(tournament, finalMatch, profile);
        tournamentRepository.save(tournament);
    }

    private void reopenFinalForAdministrativeReview(Match match) {
        Tournament tournament = match.getRound().getBracket().getTournament();
        match.getRound().getBracket().setComplete(false);

        if (tournament.getStatus() == TournamentStatus.COMPLETED) {
            tournament.setStatus(TournamentStatus.IN_PROGRESS);
        }

        for (TournamentPrize prize : tournament.getPrizes()) {
            Player awardedPlayer = prize.getPlayer();
            if (awardedPlayer != null && prize.getPrizeType() == PrizeType.POINTS) {
                playerStatsRepository.findByPlayerId(awardedPlayer.getId()).ifPresent((stats) -> {
                    int amount = prize.getAmount() != null ? prize.getAmount().intValue() : 0;
                    stats.addVirtualPoints(-amount);
                    playerStatsRepository.save(stats);
                });
            }
            prize.setPlayer(null);
        }

        tournamentRepository.save(tournament);
        bracketRepository.save(match.getRound().getBracket());
    }

    private void assignPrizes(Tournament tournament, Match finalMatch, TournamentFormatProfile profile) {
        if (tournament.getPrizes().isEmpty()) {
            return;
        }

        List<Registration> podium = resolvePodium(tournament, finalMatch, profile);
        for (TournamentPrize prize : tournament.getPrizes()) {
            int index = prize.getPosition() - 1;
            if (index < 0 || index >= podium.size()) {
                continue;
            }

            Player awardedPlayer = resolvePrizeOwner(podium.get(index));
            prize.setPlayer(awardedPlayer);

            if (awardedPlayer != null && prize.getPrizeType() == PrizeType.POINTS) {
                PlayerStats stats = playerStatsRepository.findByPlayerId(awardedPlayer.getId())
                        .orElseGet(() -> PlayerStats.builder().player(awardedPlayer).build());
                int amount = prize.getAmount() != null ? prize.getAmount().intValue() : 0;
                stats.addVirtualPoints(amount);
                playerStatsRepository.save(stats);
            }
        }
    }

    private List<Registration> resolvePodium(Tournament tournament, Match finalMatch, TournamentFormatProfile profile) {
        if (profile.family() == TournamentFormatFamily.ROUND_ROBIN
                || profile.family() == TournamentFormatFamily.SWISS) {
            return resolveStandingPodium(tournament);
        }

        List<Registration> podium = new ArrayList<>();

        if (finalMatch.getWinner() != null) {
            podium.add(finalMatch.getWinner());
        }

        Registration runnerUp = loserOf(finalMatch, finalMatch.getWinner());
        if (runnerUp != null) {
            podium.add(runnerUp);
        }

        List<Match> allMatches = matchRepository.findAllByTournamentId(tournament.getId());
        allMatches.stream()
                .filter(match -> match.getNextMatch() != null && Objects.equals(match.getNextMatch().getId(), finalMatch.getId()))
                .map(match -> loserOf(match, match.getWinner()))
                .filter(Objects::nonNull)
                .forEach(podium::add);

        return podium;
    }

    private List<Registration> resolveStandingPodium(Tournament tournament) {
        List<Registration> registrations = registrationRepository
                .findByTournamentIdAndStatus(tournament.getId(), RegistrationStatus.CONFIRMED);
        List<Match> matches = matchRepository.findAllByTournamentId(tournament.getId());

        java.util.Map<Long, Integer> winsByRegistration = new java.util.HashMap<>();
        for (Registration registration : registrations) {
            winsByRegistration.put(registration.getId(), 0);
        }

        for (Match match : matches) {
            if (match.getWinner() != null) {
                winsByRegistration.computeIfPresent(match.getWinner().getId(), (key, value) -> value + 1);
            }
        }

        return registrations.stream()
                .sorted((left, right) -> {
                    int winCompare = Integer.compare(
                            winsByRegistration.getOrDefault(right.getId(), 0),
                            winsByRegistration.getOrDefault(left.getId(), 0)
                    );
                    if (winCompare != 0) {
                        return winCompare;
                    }
                    return Integer.compare(
                            left.getSeed() != null ? left.getSeed() : Integer.MAX_VALUE,
                            right.getSeed() != null ? right.getSeed() : Integer.MAX_VALUE
                    );
                })
                .toList();
    }

    private Registration loserOf(Match match, Registration winner) {
        if (match.getRegistration1() == null || match.getRegistration2() == null || winner == null) {
            return null;
        }
        return match.getRegistration1().getId().equals(winner.getId())
                ? match.getRegistration2()
                : match.getRegistration1();
    }

    private Player resolvePrizeOwner(Registration registration) {
        if (registration == null) {
            return null;
        }
        if (registration.isPlayerRegistration()) {
            return registration.getPlayer();
        }
        if (registration.getTeam() == null || registration.getTeam().getCaptain() == null) {
            return null;
        }
        return registration.getTeam().getCaptain();
    }
}

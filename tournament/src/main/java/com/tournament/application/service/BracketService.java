package com.tournament.application.service;

import com.tournament.application.dto.request.GenerateBracketRequest;
import com.tournament.application.dto.request.ScheduleRoundRequest;
import com.tournament.application.dto.response.*;
import com.tournament.application.event.TournamentStartedEvent;
import com.tournament.application.format.*;
import com.tournament.domain.entity.*;
import com.tournament.domain.enums.*;
import com.tournament.domain.repository.*;
import com.tournament.exception.*;
import com.tournament.application.format.FormatFactory;
import com.tournament.application.format.TournamentFormatStrategy;
import com.tournament.application.dto.response.BracketResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BracketService {

    private final BracketRepository      bracketRepository;
    private final RoundRepository        roundRepository;
    private final MatchRepository        matchRepository;
    private final RegistrationRepository registrationRepository;
    private final TournamentRepository   tournamentRepository;
    private final FormatFactory formatFactory;
    private final ApplicationEventPublisher eventPublisher;

    public BracketResponse generateBracket(Long tournamentId, GenerateBracketRequest req) {

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(tournamentId));

        // TODO: nunca se cierra el registro, siempre queda abierto
        if (tournament.getStatus() != TournamentStatus.REGISTRATION_CLOSED &&
            tournament.getStatus() != TournamentStatus.REGISTRATION_OPEN) {
            throw new TournamentNotPublishedException(tournamentId, tournament.getStatus());
        }

        if (bracketRepository.existsByTournamentId(tournamentId)) {
            throw new BracketAlreadyGeneratedException(tournamentId);
        }

        List<Registration> confirmed = registrationRepository
                .findByTournamentIdAndStatus(tournamentId, RegistrationStatus.CONFIRMED);

        TournamentFormatStrategy format = formatFactory.getFormat(tournament.getFormat());
        if (!format.getProfile().supportsBracketGeneration()) {
            throw new IllegalArgumentException(
                    "El formato " + format.getProfile().displayName() + " aun no soporta generacion automatica de bracket");
        }
        if (confirmed.size() < format.getMinimumParticipants()) {
            throw new InsufficientParticipantsException(
                    confirmed.size(), format.getMinimumParticipants());
        }

        List<Registration> seeded = assignSeeds(confirmed, req.getGenerationType());

        Bracket bracket = Bracket.builder()
                .tournament(tournament)
                .generationType(req.getGenerationType())
                .complete(false)
                .generatedAt(LocalDateTime.now())
                .build();
        bracketRepository.save(bracket);

        BracketResult result = format.generateBracket(seeded, bracket);

        roundRepository.saveAll(result.rounds());

        List<Match> savedMatches = persistMatchesInOrder(result.matches());

        tournament.setStatus(TournamentStatus.IN_PROGRESS);
        tournamentRepository.save(tournament);

        eventPublisher.publishEvent(new TournamentStartedEvent(this, tournamentId));

        return buildResponse(bracket, result.rounds(), savedMatches);
    }

    @Transactional(readOnly = true)
    public BracketResponse getBracket(Long tournamentId) {
        Bracket bracket = bracketRepository.findByTournamentId(tournamentId)
                .orElseThrow(() -> new BracketNotFoundException(tournamentId));

        List<Round> rounds  = roundRepository.findByBracketIdOrderByRoundNumberAsc(bracket.getId());
        List<Match> matches = matchRepository.findAllByTournamentId(tournamentId);
        return buildResponse(bracket, rounds, matches);
    }

    public RoundResponse scheduleRound(Long roundId, ScheduleRoundRequest req) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new RoundNotFoundException(roundId));

        if (round.getStatus() == RoundStatus.COMPLETED) {
            throw new RoundAlreadyCompletedException(roundId);
        }

        round.setScheduledStart(req.getScheduledStart());
        roundRepository.save(round);

        List<MatchResponse> matchResponses = round.getMatches().stream()
                .map(MatchResponse::from).toList();
        return RoundResponse.from(round, matchResponses);
    }


    private List<Registration> assignSeeds(List<Registration> regs, GenerationType type) {
        List<Registration> seeded = new ArrayList<>(regs);
        if (type == GenerationType.BY_RANKING) {
            seeded.sort(Comparator.comparingInt(Registration::getEloAtRegistration).reversed());
        } else {
            Collections.shuffle(seeded);
        }
        for (int i = 0; i < seeded.size(); i++) seeded.get(i).setSeed(i + 1);
        registrationRepository.saveAll(seeded);
        return seeded;
    }

    private List<Match> persistMatchesInOrder(List<Match> matches) {
        List<Match> pending = new ArrayList<>(matches);
        List<Match> saved   = new ArrayList<>();

        while (!pending.isEmpty()) {
            List<Match> canSave = pending.stream().filter(m -> {
                boolean nextOk  = m.getNextMatch()      == null || m.getNextMatch().getId()      != null;
                boolean loserOk = m.getLoserNextMatch() == null || m.getLoserNextMatch().getId() != null;
                return nextOk && loserOk;
            }).toList();

            if (canSave.isEmpty()) throw new IllegalStateException(
                    "Ciclo en estructura de bracket — revisar implementación del formato");

            List<Match> batch = matchRepository.saveAll(canSave);
            for (int i = 0; i < canSave.size(); i++) canSave.get(i).setId(batch.get(i).getId());
            saved.addAll(batch);
            pending.removeAll(canSave);
        }
        return saved;
    }

    private BracketResponse buildResponse(Bracket bracket, List<Round> rounds, List<Match> matches) {
        Map<Long, List<Match>> byRound = matches.stream()
                .collect(Collectors.groupingBy(m -> m.getRound().getId()));

        List<RoundResponse> roundResponses = rounds.stream()
                .map(r -> RoundResponse.from(r,
                        byRound.getOrDefault(r.getId(), List.of()).stream()
                                .map(MatchResponse::from).toList()))
                .toList();

        return BracketResponse.from(bracket, roundResponses);
    }

    @Transactional(readOnly = true)
    public BracketViewDto getBracketView(Long tournamentId) {
        Bracket bracket = bracketRepository.findByTournamentId(tournamentId)
                .orElseThrow(() -> new BracketNotFoundException(tournamentId));

        List<Round> rounds  = roundRepository.findByBracketIdOrderByRoundNumberAsc(bracket.getId());
        List<Match> matches = matchRepository.findAllByTournamentId(tournamentId);

        Map<Long, List<Match>> byRound = matches.stream()
                .collect(Collectors.groupingBy(m -> m.getRound().getId()));

        List<List<String>> leftData = rounds.stream()
                .map(round -> byRound.getOrDefault(round.getId(), List.of()).stream()
                        .map(m -> String.join(" vs ", matchToPlayerNames(m)))
                        .toList())
                .toList();

        String championName = bracket.isComplete() ? "Campeón definido" : "En curso";

        return BracketViewDto.builder()
                .champion(championName)
                .left(leftData)
                .right(Collections.emptyList())
                .build();
    }

    private List<String> matchToPlayerNames(Match m) {
        List<String> names = new ArrayList<>();
        if (m.getRegistration1() != null) names.add(participantName(m.getRegistration1()));
        if (m.getRegistration2() != null) names.add(participantName(m.getRegistration2()));
        if (names.isEmpty()) names.add("TBD");
        return names;
    }

    private String participantName(Registration r) {
        if (r == null) return "TBD";
        return r.isPlayerRegistration() ? r.getPlayer().getUsername() : r.getTeam().getName();
    }

}

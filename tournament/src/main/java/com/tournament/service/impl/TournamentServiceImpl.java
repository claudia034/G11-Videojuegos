package com.tournament.service.impl;

import com.tournament.application.format.FormatFactory;
import com.tournament.application.format.TournamentFormatStrategy;
import com.tournament.domain.entity.Player;
import com.tournament.domain.entity.Tournament;
import com.tournament.domain.entity.TournamentPrize;
import com.tournament.domain.entity.TournamentRound;
import com.tournament.domain.entity.User;
import com.tournament.domain.enums.TournamentFormat;
import com.tournament.domain.enums.TournamentStatus;
import com.tournament.domain.repository.PlayerRepository;
import com.tournament.domain.repository.TournamentRepository;
import com.tournament.domain.repository.UserRepository;
import com.tournament.dto.CreateTournamentPrizeRequest;
import com.tournament.dto.CreateTournamentRequest;
import com.tournament.dto.TournamentResponse;
import com.tournament.dto.UpdateTournamentRequest;
import com.tournament.exception.PlayerNotFoundException;
import com.tournament.exception.ResourceNotFoundException;
import com.tournament.mapper.TournamentMapper;
import com.tournament.service.TournamentService;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TournamentServiceImpl implements TournamentService {

    private static final int MIN_PARTICIPANTS_TO_PUBLISH = 2;
    private static final Set<TournamentFormat> ACTIVE_FORMATS = EnumSet.of(
            TournamentFormat.SINGLE_ELIMINATION,
            TournamentFormat.DOUBLE_ELIMINATION,
            TournamentFormat.ROUND_ROBIN,
            TournamentFormat.SWISS
    );

    private final PlayerRepository playerRepository;
    private final TournamentRepository tournamentRepository;
    private final UserRepository userRepository;
    private final TournamentMapper tournamentMapper;
    private final FormatFactory formatFactory;

    @Override
    @Transactional
    public TournamentResponse create(CreateTournamentRequest request) {
        Tournament tournament = tournamentMapper.toEntity(request);
        assignOrganizer(tournament, request.organizerId());
        assignPrizePlayers(tournament.getPrizes(), request.prizes());

        generateRoundsWhenEmpty(tournament);
        validateTournament(tournament);
        Tournament saved = tournamentRepository.save(tournament);
        return tournamentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TournamentResponse> findAll() {
        return tournamentRepository.findAll().stream()
                .map(tournamentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TournamentResponse findById(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id " + id));
        return tournamentMapper.toResponse(tournament);
    }

    @Override
    @Transactional
    public TournamentResponse update(Long id, UpdateTournamentRequest request) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id " + id));

        removeCurrentChildren(tournament);
        tournamentMapper.updateEntity(tournament, request);
        assignOrganizer(tournament, request.organizerId());
        assignPrizePlayers(tournament.getPrizes(), request.prizes());
        generateRoundsWhenEmpty(tournament);
        validateTournament(tournament);

        Tournament saved = tournamentRepository.save(tournament);
        return tournamentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TournamentResponse publish(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id " + id));

        validatePublishState(tournament);
        validatePublishDates(tournament);
        validatePublishParticipantMinimum(tournament);
        generateRoundsWhenEmpty(tournament);
        validateTournament(tournament);

        tournament.setStatus(TournamentStatus.REGISTRATION_OPEN);
        Tournament saved = tournamentRepository.save(tournament);
        return tournamentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TournamentResponse closeRegistration(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id " + id));

        if (tournament.getStatus() != TournamentStatus.REGISTRATION_OPEN) {
            throw new IllegalArgumentException("Only REGISTRATION_OPEN tournaments can close registration");
        }

        tournament.setStatus(TournamentStatus.REGISTRATION_CLOSED);
        Tournament saved = tournamentRepository.save(tournament);
        return tournamentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TournamentResponse generateRounds(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id " + id));

        validateRoundGenerationState(tournament);
        validateRoundGenerationLimits(tournament);
        removeCurrentRounds(tournament);

        formatFactory.getFormat(tournament.getFormat())
                .generateRounds(tournament)
                .forEach(tournament::addRound);

        validateUniqueRoundNumbers(tournament.getRounds());

        Tournament saved = tournamentRepository.save(tournament);
        return tournamentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!tournamentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tournament not found with id " + id);
        }
        tournamentRepository.deleteById(id);
    }

    private void removeCurrentChildren(Tournament tournament) {
        tournament.getRounds().clear();
        tournament.getPrizes().clear();
        tournamentRepository.flush();
    }

    private void removeCurrentRounds(Tournament tournament) {
        tournament.getRounds().clear();
        tournamentRepository.flush();
    }

    private void generateRoundsWhenEmpty(Tournament tournament) {
        if (tournament.getRounds().isEmpty()) {
            TournamentFormatStrategy formatStrategy = formatFactory.getFormat(tournament.getFormat());
            formatStrategy.validateTournamentConfiguration(tournament);
            formatStrategy.generateRounds(tournament)
                    .forEach(tournament::addRound);
        }
    }

    private void validateRoundGenerationState(Tournament tournament) {
        if (tournament.getStatus() != TournamentStatus.DRAFT
                && tournament.getStatus() != TournamentStatus.REGISTRATION_OPEN) {
            throw new IllegalArgumentException("Rounds can only be generated for DRAFT or REGISTRATION_OPEN tournaments");
        }
    }

    private void validateRoundGenerationLimits(Tournament tournament) {
        formatFactory.getFormat(tournament.getFormat()).validateTournamentConfiguration(tournament);
    }

    private void validatePublishState(Tournament tournament) {
        if (tournament.getStatus() != TournamentStatus.DRAFT) {
            throw new IllegalArgumentException("Only DRAFT tournaments can be published");
        }
    }

    private void validatePublishDates(Tournament tournament) {
        if (tournament.getRegistrationStartAt() == null
                || tournament.getRegistrationEndAt() == null
                || tournament.getStartAt() == null) {
            throw new IllegalArgumentException(
                    "registrationStartAt, registrationEndAt and startAt are required before publishing"
            );
        }
    }

    private void validatePublishParticipantMinimum(Tournament tournament) {
        if (tournament.getMaxParticipants() == null
                || tournament.getMaxParticipants() < MIN_PARTICIPANTS_TO_PUBLISH) {
            throw new IllegalArgumentException("Tournament must allow at least 2 participants before publishing");
        }
    }

    private void validateTournament(Tournament tournament) {
        validateSupportedFormat(tournament);
        validateDateRange(
                tournament.getRegistrationStartAt(),
                tournament.getRegistrationEndAt(),
                "registrationStartAt must be before or equal to registrationEndAt"
        );
        validateDateRange(tournament.getStartAt(), tournament.getEndAt(), "startAt must be before or equal to endAt");

        if (tournament.getRegistrationEndAt() != null
                && tournament.getStartAt() != null
                && tournament.getRegistrationEndAt().isAfter(tournament.getStartAt())) {
            throw new IllegalArgumentException("registrationEndAt must be before or equal to startAt");
        }

        validateEloRange(tournament);
        formatFactory.getFormat(tournament.getFormat()).validateTournamentConfiguration(tournament);
        validateUniqueRoundNumbers(tournament.getRounds());
        validateUniquePrizePositions(tournament.getPrizes());

        for (TournamentRound round : tournament.getRounds()) {
            validateDateRange(round.getStartAt(), round.getEndAt(), "round startAt must be before or equal to endAt");
        }
    }

    private void validateSupportedFormat(Tournament tournament) {
        if (!ACTIVE_FORMATS.contains(tournament.getFormat())) {
            throw new IllegalArgumentException(
                    "Solo se permiten los formatos: eliminacion simple, doble eliminacion, round robin y swiss"
            );
        }
    }

    private void validateEloRange(Tournament tournament) {
        if (tournament.getMinElo() != null
                && tournament.getMaxElo() != null
                && tournament.getMinElo() > tournament.getMaxElo()) {
            throw new IllegalArgumentException("minElo must be less than or equal to maxElo");
        }
    }

    private void validateDateRange(LocalDateTime start, LocalDateTime end, String message) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException(message);
        }
    }

    private void validateUniqueRoundNumbers(List<TournamentRound> rounds) {
        Set<Integer> roundNumbers = new HashSet<>();
        for (TournamentRound round : rounds) {
            if (!roundNumbers.add(round.getRoundNumber())) {
                throw new IllegalArgumentException("roundNumber must be unique per tournament");
            }
        }
    }

    private void validateUniquePrizePositions(List<TournamentPrize> prizes) {
        Set<Integer> prizePositions = new HashSet<>();
        for (TournamentPrize prize : prizes) {
            if (!prizePositions.add(prize.getPosition())) {
                throw new IllegalArgumentException("prize position must be unique per tournament");
            }
        }
    }

    private void assignPrizePlayers(List<TournamentPrize> prizes, List<CreateTournamentPrizeRequest> prizeRequests) {
        if (prizes == null || prizeRequests == null || prizes.size() != prizeRequests.size()) {
            return;
        }

        for (int i = 0; i < prizes.size(); i++) {
            Long playerId = prizeRequests.get(i).playerId();
            if (playerId == null) {
                continue;
            }

            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new PlayerNotFoundException(playerId));
            prizes.get(i).setPlayer(player);
        }
    }

    private void assignOrganizer(Tournament tournament, Long organizerId) {
        if (organizerId == null) {
            tournament.setOrganizer(null);
            return;
        }

        User organizer = userRepository.findById(organizerId).orElse(null);
        tournament.setOrganizer(organizer);
    }
}

package com.tournament.application.service;

import com.tournament.application.dto.request.RegisterRequest;
import com.tournament.application.dto.response.RegistrationResponse;
import com.tournament.domain.entity.*;
import com.tournament.domain.enums.*;
import com.tournament.domain.repository.*;
import com.tournament.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final TournamentRepository   tournamentRepository;
    private final UserRepository         userRepository;
    private final TeamRepository         teamRepository;

    @Transactional
    public RegistrationResponse register(Long tournamentId, RegisterRequest request) {
        validateExclusiveParticipant(request);

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(tournamentId));

        if (tournament.getStatus() != TournamentStatus.PUBLISHED && tournament.getStatus() != TournamentStatus.OPEN) {
            throw new TournamentNotPublishedException(tournamentId, tournament.getStatus());
        }

        int currentParticipants = registrationRepository
                .countByTournamentIdAndStatusNot(tournamentId, RegistrationStatus.WITHDRAWN);

        if (currentParticipants >= tournament.getMaxParticipants()) {
            throw new TournamentFullException(tournamentId, tournament.getMaxParticipants());
        }

        Registration registration = (request.getUserId() != null)
                ? buildUserRegistration(tournament, request.getUserId())
                : buildTeamRegistration(tournament, request.getTeamId());

        Registration saved = registrationRepository.save(registration);
        log.info("Nueva inscripción confirmada: Torneo ID {}, Participante: {}", tournamentId, saved.getParticipantName());

        return RegistrationResponse.from(saved);
    }

    @Transactional
    public RegistrationResponse withdraw(Long registrationId, Long requestingUserId) {
        Registration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RegistrationNotFoundException(registrationId));

        if (reg.getTournament().getStatus() != TournamentStatus.PUBLISHED && reg.getTournament().getStatus() != TournamentStatus.OPEN) {
            throw new RegistrationWithdrawNotAllowedException(
                    "No se puede retirar una inscripción de un torneo en curso o finalizado");
        }

        validateOwnership(reg, requestingUserId);

        reg.setStatus(RegistrationStatus.WITHDRAWN);
        log.info("Inscripción retirada: Reg ID {}, Torneo ID {}, Solicitado por User ID {}",
                registrationId, reg.getTournament().getId(), requestingUserId);

        return RegistrationResponse.from(registrationRepository.save(reg));
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponse> getParticipants(Long tournamentId) {
        if (!tournamentRepository.existsById(tournamentId)) {
            throw new TournamentNotFoundException(tournamentId);
        }
        return registrationRepository.findByTournamentId(tournamentId)
                .stream()
                .map(RegistrationResponse::from)
                .toList();
    }

    private Registration buildUserRegistration(Tournament tournament, Long userId) {
        if (tournament.isTeamBased()) {
            throw new InvalidRegistrationTypeException(
                    "Este torneo es exclusivo por equipos; proporcione un teamId en lugar de un userId");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (registrationRepository.existsByTournamentIdAndUserId(tournament.getId(), userId)) {
            throw new AlreadyRegisteredException(user.getUsername(), tournament.getId());
        }

        int elo = user.getEloRating() != null ? user.getEloRating() : 1000;
        validateEloRange(elo, tournament);

        return Registration.builder()
                .tournament(tournament)
                .user(user)
                .eloAtRegistration(elo)
                .status(RegistrationStatus.CONFIRMED)
                .build();
    }

    private Registration buildTeamRegistration(Tournament tournament, Long teamId) {
        if (!tournament.isTeamBased()) {
            throw new InvalidRegistrationTypeException(
                    "Este torneo es individual; proporcione un userId en lugar de un teamId");
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId));

        if (registrationRepository.existsByTournamentIdAndTeamId(tournament.getId(), teamId)) {
            throw new AlreadyRegisteredException(team.getName(), tournament.getId());
        }

        int avgElo = (int) team.getMembers().stream()
                .mapToInt(m -> m.getUser().getEloRating() != null ? m.getUser().getEloRating() : 1000)
                .average().orElse(1000);

        validateEloRange(avgElo, tournament);

        return Registration.builder()
                .tournament(tournament)
                .team(team)
                .eloAtRegistration(avgElo)
                .status(RegistrationStatus.CONFIRMED)
                .build();
    }

    private void validateEloRange(int elo, Tournament t) {
        boolean tooLow  = t.getMinElo() != null && elo < t.getMinElo();
        boolean tooHigh = t.getMaxElo() != null && elo > t.getMaxElo();

        if (tooLow || tooHigh) {
            int min = t.getMinElo() != null ? t.getMinElo() : 0;
            int max = t.getMaxElo() != null ? t.getMaxElo() : Integer.MAX_VALUE;
            throw new EloRequirementNotMetException(elo, min, max);
        }
    }

    private void validateExclusiveParticipant(RegisterRequest req) {
        boolean hasUser = req.getUserId() != null;
        boolean hasTeam = req.getTeamId() != null;

        if (hasUser == hasTeam) {
            throw new InvalidRegistrationTypeException(
                    "Solicitud ambigua: Especifique exactamente uno (userId o teamId) según el formato del torneo");
        }
    }

    private void validateOwnership(Registration reg, Long requestingUserId) {
        Long ownerId = reg.isUserRegistration()
                ? reg.getUser().getId()
                : reg.getTeam().getCaptain().getId();

        if (!ownerId.equals(requestingUserId)) {
            throw new ForbiddenOperationException(
                    "Operación denegada: No tienes permiso para modificar o retirar esta inscripción");
        }
    }
}
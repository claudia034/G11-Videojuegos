package com.tournament.application.service;

import com.tournament.application.dto.request.RegisterRequest;
import com.tournament.application.dto.response.RegistrationResponse;
import com.tournament.domain.entity.*;
import com.tournament.domain.enums.*;
import com.tournament.domain.repository.*;
import com.tournament.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final TournamentRepository   tournamentRepository;
    private final PlayerRepository       playerRepository;
    private final TeamRepository         teamRepository;

    public RegistrationResponse register(Long tournamentId, RegisterRequest request) {

        validateExclusiveParticipant(request);

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(tournamentId));

        if (tournament.getStatus() != TournamentStatus.PUBLISHED) {
            throw new TournamentNotPublishedException(tournamentId, tournament.getStatus());
        }

        int current = registrationRepository
                .countByTournamentIdAndStatusNot(tournamentId, RegistrationStatus.WITHDRAWN);
        if (current >= tournament.getMaxParticipants()) {
            throw new TournamentFullException(tournamentId, tournament.getMaxParticipants());
        }

        Registration reg = (request.getPlayerId() != null)
                ? buildPlayerRegistration(tournament, request.getPlayerId())
                : buildTeamRegistration(tournament, request.getTeamId());

        return RegistrationResponse.from(registrationRepository.save(reg));
    }

    public RegistrationResponse withdraw(Long registrationId, Long requestingUserId) {

        Registration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RegistrationNotFoundException(registrationId));

        if (reg.getTournament().getStatus() != TournamentStatus.PUBLISHED) {
            throw new RegistrationWithdrawNotAllowedException(
                    "No se puede retirar una inscripción de un torneo en curso o finalizado");
        }

        validateOwnership(reg, requestingUserId);

        reg.setStatus(RegistrationStatus.WITHDRAWN);
        return RegistrationResponse.from(registrationRepository.save(reg));
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponse> getParticipants(Long tournamentId) {
        if (!tournamentRepository.existsById(tournamentId)) {
            throw new TournamentNotFoundException(tournamentId);
        }
        return registrationRepository.findByTournamentId(tournamentId)
                .stream().map(RegistrationResponse::from).toList();
    }


    private Registration buildPlayerRegistration(Tournament tournament, Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));

        if (registrationRepository.existsByTournamentIdAndPlayerId(tournament.getId(), playerId)) {
            throw new AlreadyRegisteredException(player.getUsername(), tournament.getId());
        }

        validateEloRange(player.getEloRating(), tournament);

        return Registration.builder()
                .tournament(tournament).player(player)
                .eloAtRegistration(player.getEloRating())
                .status(RegistrationStatus.CONFIRMED)
                .build();
    }

    private Registration buildTeamRegistration(Tournament tournament, Long teamId) {
        if (!tournament.isTeamBased()) {
            throw new InvalidRegistrationTypeException(
                    "Este torneo es individual; use playerId en lugar de teamId");
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId));

        if (registrationRepository.existsByTournamentIdAndTeamId(tournament.getId(), teamId)) {
            throw new AlreadyRegisteredException(team.getName(), tournament.getId());
        }

        int avgElo = (int) team.getMembers().stream()
                .mapToInt(m -> m.getPlayer().getEloRating())
                .average().orElse(1000);

        validateEloRange(avgElo, tournament);

        return Registration.builder()
                .tournament(tournament).team(team)
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
        boolean hp = req.getPlayerId() != null;
        boolean ht = req.getTeamId()   != null;
        if (hp == ht) {
            throw new InvalidRegistrationTypeException(
                    "Especifique exactamente uno: playerId o teamId");
        }
    }

    private void validateOwnership(Registration reg, Long userId) {
        Long ownerId = reg.isPlayerRegistration()
                ? reg.getPlayer().getUserId()
                : reg.getTeam().getCaptain().getUserId();
        if (!ownerId.equals(userId)) {
            throw new ForbiddenOperationException(
                    "No tienes permiso para modificar esta inscripción");
        }
    }
}

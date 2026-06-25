package com.tournament.mapper;

import com.tournament.domain.entity.Tournament;
import com.tournament.domain.entity.TournamentPrize;
import com.tournament.domain.entity.TournamentRound;
import com.tournament.domain.enums.PrizeType;
import com.tournament.domain.enums.TournamentFormat;
import com.tournament.domain.enums.TournamentRoundStatus;
import com.tournament.domain.enums.TournamentStatus;
import com.tournament.dto.CreateTournamentPrizeRequest;
import com.tournament.dto.CreateTournamentRequest;
import com.tournament.dto.CreateTournamentRoundRequest;
import com.tournament.dto.TournamentPrizeResponse;
import com.tournament.dto.TournamentResponse;
import com.tournament.dto.TournamentRoundResponse;
import com.tournament.dto.UpdateTournamentRequest;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TournamentMapper {

    public Tournament toEntity(CreateTournamentRequest request) {
        Tournament tournament = Tournament.builder()
                .name(request.name())
                .description(request.description())
                .gameName(request.gameName())
                .format(request.format() != null ? request.format() : TournamentFormat.SINGLE_ELIMINATION)
                .status(request.status() != null ? request.status() : TournamentStatus.DRAFT)
                .maxParticipants(request.maxParticipants())
                .teamBased(request.isTeamBased() != null ? request.isTeamBased() : false)
                .minElo(request.minElo())
                .maxElo(request.maxElo())
                .registrationStartAt(request.registrationStartAt())
                .registrationEndAt(request.registrationEndAt())
                .startAt(request.startAt())
                .endAt(request.endAt())
                .build();

        if (request.rounds() != null) {
            request.rounds().stream()
                    .map(this::toRoundEntity)
                    .forEach(tournament::addRound);
        }

        if (request.prizes() != null) {
            request.prizes().stream()
                    .map(this::toPrizeEntity)
                    .forEach(tournament::addPrize);
        }

        return tournament;
    }

    public TournamentResponse toResponse(Tournament tournament) {
        List<TournamentRoundResponse> rounds = tournament.getRounds().stream()
                .sorted(Comparator.comparing(TournamentRound::getRoundNumber))
                .map(this::toRoundResponse)
                .toList();

        List<TournamentPrizeResponse> prizes = tournament.getPrizes().stream()
                .sorted(Comparator.comparing(TournamentPrize::getPosition))
                .map(this::toPrizeResponse)
                .toList();

        return new TournamentResponse(
                tournament.getId(),
                tournament.getName(),
                tournament.getDescription(),
                tournament.getGameName(),
                tournament.getFormat(),
                tournament.getStatus(),
                tournament.getMaxParticipants(),
                tournament.getTeamBased(),
                tournament.getMinElo(),
                tournament.getMaxElo(),
                tournament.getOrganizer() != null ? tournament.getOrganizer().getId() : null,
                tournament.getRegistrationStartAt(),
                tournament.getRegistrationEndAt(),
                tournament.getStartAt(),
                tournament.getEndAt(),
                tournament.getCreatedAt(),
                tournament.getUpdatedAt(),
                rounds,
                prizes
        );
    }

    public void updateEntity(Tournament tournament, UpdateTournamentRequest request) {
        tournament.setName(request.name());
        tournament.setDescription(request.description());
        tournament.setGameName(request.gameName());
        tournament.setFormat(request.format() != null ? request.format() : TournamentFormat.SINGLE_ELIMINATION);
        tournament.setStatus(request.status() != null ? request.status() : TournamentStatus.DRAFT);
        tournament.setMaxParticipants(request.maxParticipants());
        tournament.setTeamBased(request.isTeamBased() != null ? request.isTeamBased() : false);
        tournament.setMinElo(request.minElo());
        tournament.setMaxElo(request.maxElo());
        tournament.setRegistrationStartAt(request.registrationStartAt());
        tournament.setRegistrationEndAt(request.registrationEndAt());
        tournament.setStartAt(request.startAt());
        tournament.setEndAt(request.endAt());

        tournament.getRounds().clear();
        if (request.rounds() != null) {
            request.rounds().stream()
                    .map(this::toRoundEntity)
                    .forEach(tournament::addRound);
        }

        tournament.getPrizes().clear();
        if (request.prizes() != null) {
            request.prizes().stream()
                    .map(this::toPrizeEntity)
                    .forEach(tournament::addPrize);
        }
    }

    private TournamentRound toRoundEntity(CreateTournamentRoundRequest request) {
        return TournamentRound.builder()
                .roundNumber(request.roundNumber())
                .name(request.name())
                .status(request.status() != null ? request.status() : TournamentRoundStatus.PENDING)
                .startAt(request.startAt())
                .endAt(request.endAt())
                .build();
    }

    private TournamentPrize toPrizeEntity(CreateTournamentPrizeRequest request) {
        return TournamentPrize.builder()
                .position(request.position())
                .name(request.name())
                .description(request.description())
                .prizeType(request.prizeType() != null ? request.prizeType() : PrizeType.OTHER)
                .amount(request.amount())
                .currency(request.currency())
                .build();
    }

    private TournamentRoundResponse toRoundResponse(TournamentRound round) {
        return new TournamentRoundResponse(
                round.getId(),
                round.getRoundNumber(),
                round.getName(),
                round.getStatus(),
                round.getStartAt(),
                round.getEndAt()
        );
    }

    private TournamentPrizeResponse toPrizeResponse(TournamentPrize prize) {
        return new TournamentPrizeResponse(
                prize.getId(),
                prize.getPosition(),
                prize.getName(),
                prize.getDescription(),
                prize.getPrizeType(),
                prize.getAmount(),
                prize.getCurrency(),
                prize.getPlayer() != null ? prize.getPlayer().getId() : null
        );
    }
}

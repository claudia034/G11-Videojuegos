package com.tournament.dto;

import com.tournament.domain.enums.TournamentFormat;
import com.tournament.domain.enums.TournamentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public record CreateTournamentRequest(
        @NotBlank
        @Size(max = 150)
        String name,

        @Size(max = 1000)
        String description,

        @NotBlank
        @Size(max = 100)
        String gameName,

        TournamentFormat format,

        TournamentStatus status,

        @NotNull
        @Min(2)
        Integer maxParticipants,

        Boolean teamBased,

        @Min(0)
        Integer minElo,

        @Min(0)
        Integer maxElo,

        Long organizerId,

        LocalDateTime registrationStartAt,

        LocalDateTime registrationEndAt,

        LocalDateTime startAt,

        LocalDateTime endAt,

        @Valid
        List<CreateTournamentRoundRequest> rounds,

        @Valid
        List<CreateTournamentPrizeRequest> prizes
) {
}

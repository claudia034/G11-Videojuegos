package com.tournament.dto;

import com.tournament.domain.enums.TournamentRoundStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record CreateTournamentRoundRequest(
        @NotNull
        @Min(1)
        Integer roundNumber,

        @NotBlank
        @Size(max = 120)
        String name,

        TournamentRoundStatus status,

        LocalDateTime startAt,

        LocalDateTime endAt
) {
}

package com.tournament.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerRankingDto {
    private Long playerId;
    private String username;
    private int eloRating;
    private int wins;
    private int losses;
    private int tournamentsPlayed;
}

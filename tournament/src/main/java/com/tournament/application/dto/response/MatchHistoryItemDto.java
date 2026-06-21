package com.tournament.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchHistoryItemDto {
    private Long matchId;
    private String tournamentName;
    private String opponentName;
    private Integer myScore;
    private Integer opponentScore;
    private boolean won;
    private LocalDateTime completedAt;
}

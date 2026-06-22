package com.tournament.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerHistoryDto {
    private Long playerId;
    private String username;
    private List<MatchHistoryItemDto> matches;
}

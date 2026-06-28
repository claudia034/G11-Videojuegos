package com.tournament.application.dto.response;

import com.tournament.domain.enums.TournamentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularTournamentByGameDto {
    private String gameName;
    private Long tournamentId;
    private String tournamentName;
    private Long participantCount;
    private Integer capacity;
    private TournamentStatus status;
}

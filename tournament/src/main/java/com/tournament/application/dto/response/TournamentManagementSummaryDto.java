package com.tournament.application.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TournamentManagementSummaryDto {
    private Long tournamentId;
    private String tournamentName;
    private int confirmedParticipants;
    private int pendingParticipants;
    private int totalParticipants;
    private int scheduledMatches;
    private int completedMatches;
    private int disputedMatches;
    private List<MatchDetailResponse> disputedMatchDetails;
}

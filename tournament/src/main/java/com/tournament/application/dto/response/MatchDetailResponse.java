package com.tournament.application.dto.response;

import com.tournament.domain.entity.Match;
import com.tournament.domain.entity.MatchResult;
import com.tournament.domain.enums.MatchStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MatchDetailResponse {

    private Long id;
    private Integer position;
    private Integer roundNumber;
    private String roundName;

    private String participant1Name;
    private Long participant1RegistrationId;
    private String participant2Name;
    private Long participant2RegistrationId;

    private String winnerName;
    private Long winnerRegistrationId;

    private Long nextMatchId;
    private Long loserNextMatchId;

    private MatchStatus status;
    private Integer bestOf;
    private LocalDateTime scheduledAt;

    private ResultDetail result;

    @Data
    @Builder
    public static class ResultDetail {
        private Integer score1;
        private Integer score2;
        private Long reportedByUserId;
        private LocalDateTime submittedAt;
        private String disputeReason;
        private String adminNotes;
    }

    public static MatchDetailResponse from(Match m, MatchResult result) {
        MatchDetailResponse.MatchDetailResponseBuilder builder = MatchDetailResponse.builder()
                .id(m.getId())
                .position(m.getPosition())
                .roundNumber(m.getRound().getRoundNumber())
                .roundName(m.getRound().getName())
                .participant1Name(m.getRegistration1() != null
                        ? m.getRegistration1().getParticipantName() : "BYE")
                .participant1RegistrationId(m.getRegistration1() != null
                        ? m.getRegistration1().getId() : null)
                .participant2Name(m.getRegistration2() != null
                        ? m.getRegistration2().getParticipantName() : "BYE")
                .participant2RegistrationId(m.getRegistration2() != null
                        ? m.getRegistration2().getId() : null)
                .winnerName(m.getWinner() != null
                        ? m.getWinner().getParticipantName() : null)
                .winnerRegistrationId(m.getWinner() != null
                        ? m.getWinner().getId() : null)
                .nextMatchId(m.getNextMatch() != null
                        ? m.getNextMatch().getId() : null)
                .loserNextMatchId(m.getLoserNextMatch() != null
                        ? m.getLoserNextMatch().getId() : null)
                .status(m.getStatus())
                .bestOf(m.getBestOf())
                .scheduledAt(m.getScheduledAt());

        if (result != null) {
            builder.result(ResultDetail.builder()
                    .score1(result.getScore1())
                    .score2(result.getScore2())
                    .reportedByUserId(result.getReportedByUserId())
                    .submittedAt(result.getSubmittedAt())
                    .disputeReason(result.getDisputeReason())
                    .adminNotes(result.getAdminNotes())
                    .build());
        }

        return builder.build();
    }
}

package com.tournament.application.dto.response;

import com.tournament.domain.entity.Match;
import com.tournament.domain.enums.MatchStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class MatchResponse {

    private Long   id;
    private Integer position;
    private String  participant1Name;
    private Long    participant1RegistrationId;
    private String  participant2Name;
    private Long    participant2RegistrationId;
    private Long    nextMatchId;
    private Long    loserNextMatchId;
    private MatchStatus status;
    private Integer bestOf;
    private LocalDateTime scheduledAt;
    private String winnerName;

    public static MatchResponse from(Match m) {
        return MatchResponse.builder()
                .id(m.getId())
                .position(m.getPosition())
                .participant1Name(m.getRegistration1() != null ? m.getRegistration1().getParticipantName() : "BYE")
                .participant1RegistrationId(m.getRegistration1() != null ? m.getRegistration1().getId() : null)
                .participant2Name(m.getRegistration2() != null ? m.getRegistration2().getParticipantName() : "BYE")
                .participant2RegistrationId(m.getRegistration2() != null ? m.getRegistration2().getId() : null)
                .nextMatchId(m.getNextMatch() != null ? m.getNextMatch().getId() : null)
                .loserNextMatchId(m.getLoserNextMatch() != null ? m.getLoserNextMatch().getId() : null)
                .status(m.getStatus())
                .bestOf(m.getBestOf())
                .scheduledAt(m.getScheduledAt())
                .winnerName(m.getWinner() != null ? m.getWinner().getParticipantName() : null) // CORREGIDO AQUÍ
                .build();
    }
}

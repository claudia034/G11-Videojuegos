package com.tournament.application.dto.response;

import com.tournament.domain.entity.Registration;
import com.tournament.domain.enums.RegistrationStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class RegistrationResponse {

    private Long   id;
    private Long   tournamentId;
    private String tournamentName;
    private String participantName;
    private String participantType;  // "PLAYER" | "TEAM"
    private Long   participantId;
    private RegistrationStatus status;
    private Integer eloAtRegistration;
    private Integer seed;
    private LocalDateTime registeredAt;

    public static RegistrationResponse from(Registration r) {
        return RegistrationResponse.builder()
                .id(r.getId())
                .tournamentId(r.getTournament().getId())
                .tournamentName(r.getTournament().getName())
                .participantName(r.getParticipantName())
                .participantType(r.isPlayerRegistration() ? "PLAYER" : "TEAM")
                .participantId(r.getParticipantId())
                .status(r.getStatus())
                .eloAtRegistration(r.getEloAtRegistration())
                .seed(r.getSeed())
                .registeredAt(r.getRegisteredAt())
                .build();
    }
}

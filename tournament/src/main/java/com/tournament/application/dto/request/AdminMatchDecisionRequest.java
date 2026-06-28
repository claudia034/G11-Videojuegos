package com.tournament.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminMatchDecisionRequest {

    @NotNull(message = "El id del ganador es obligatorio")
    private Long winnerId;

    private Integer score1;
    private Integer score2;
    private String evidenceUrl;
    private String adminNotes;
}

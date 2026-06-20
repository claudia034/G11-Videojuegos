package com.tournament.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResolveDisputeRequest {

    @NotNull(message = "El id del ganador es obligatorio")
    private Long winnerId;

    private String adminNotes;
}

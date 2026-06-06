package com.tournament.application.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleRoundRequest {
    @NotNull(message = "La fecha de inicio es obligatoria")
    @Future(message = "La fecha debe ser en el futuro")
    private LocalDateTime scheduledStart;
}

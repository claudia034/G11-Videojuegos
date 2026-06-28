package com.tournament.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleMatchRequest {
    @NotNull(message = "La fecha y hora del partido es obligatoria")
    private LocalDateTime scheduledAt;
}

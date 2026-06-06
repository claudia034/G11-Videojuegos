package com.tournament.application.dto.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private Long playerId;
    private Long teamId;
}

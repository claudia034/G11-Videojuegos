package com.tournament.application.dto.request;

import com.tournament.domain.enums.GenerationType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenerateBracketRequest {
    @NotNull(message = "El tipo de generación es obligatorio")
    private GenerationType generationType;

    private Integer defaultBestOf = 1;
}

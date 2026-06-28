package com.tournament.application.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class BracketViewDto {
    private String champion;
    private List<List<String>> left;
    private List<List<String>> right;
}
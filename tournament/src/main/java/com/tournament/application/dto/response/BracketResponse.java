package com.tournament.application.dto.response;

import com.tournament.domain.entity.Bracket;
import com.tournament.domain.enums.GenerationType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class BracketResponse {
    private Long id;
    private Long tournamentId;
    private GenerationType generationType;
    private boolean complete;
    private LocalDateTime generatedAt;
    private List<RoundResponse> rounds;

    public static BracketResponse from(Bracket b, List<RoundResponse> rounds) {
        return BracketResponse.builder()
                .id(b.getId()).tournamentId(b.getTournament().getId())
                .generationType(b.getGenerationType()).complete(b.isComplete())
                .generatedAt(b.getGeneratedAt()).rounds(rounds).build();
    }

}

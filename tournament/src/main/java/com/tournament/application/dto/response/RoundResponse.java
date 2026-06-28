package com.tournament.application.dto.response;

import com.tournament.domain.entity.Round;
import com.tournament.domain.enums.RoundSection;
import com.tournament.domain.enums.RoundStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class RoundResponse {
    private Long id;
    private Integer roundNumber;
    private String name;
    private RoundSection section;
    private RoundStatus status;
    private LocalDateTime scheduledStart;
    private List<MatchResponse> matches;

    public static RoundResponse from(Round r, List<MatchResponse> matches) {
        return RoundResponse.builder()
                .id(r.getId())
                .roundNumber(r.getRoundNumber())
                .name(r.getName())
                .section(r.getSection())
                .status(r.getStatus())
                .scheduledStart(r.getScheduledStart())
                .matches(matches)
                .build();
    }
}

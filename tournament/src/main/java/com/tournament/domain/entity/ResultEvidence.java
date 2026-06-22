package com.tournament.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "result_evidences")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ResultEvidence extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_result_id", nullable = false)
    private MatchResult matchResult;

    @Column(name = "evidence_url", nullable = false)
    private String evidenceUrl;

    @Column(name = "uploaded_by_user_id", nullable = false)
    private Long uploadedByUserId;
}

package com.tournament.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens",
        indexes = {
                @Index(name = "idx_rt_token",     columnList = "token"),
                @Index(name = "idx_rt_user",      columnList = "user_id"),
                @Index(name = "idx_rt_family",    columnList = "family_id"),
                @Index(name = "idx_rt_expires",   columnList = "expires_at")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RefreshToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @Column(name = "family_id", nullable = false, length = 36)
    private String familyId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_revoked", nullable = false)
    @Builder.Default
    private boolean revoked = false;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }
}
package com.tournament.application.dto.response;

import com.tournament.domain.entity.User;
import com.tournament.domain.enums.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String accessToken;

    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private long expiresIn;

    private UserInfo user;

    @Data
    @Builder
    public static class UserInfo {
        private Long     id;
        private String   email;
        private UserRole role;

        public static UserInfo from(User user) {
            return UserInfo.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .build();
        }
    }
}
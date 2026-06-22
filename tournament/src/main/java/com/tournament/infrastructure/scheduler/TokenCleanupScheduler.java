package com.tournament.infrastructure.scheduler;

import com.tournament.domain.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanUpExpiredTokens() {
        log.info("[MANTENIMIENTO] Iniciando la purga de refresh tokens expirados...");

        try {
            LocalDateTime now = LocalDateTime.now();

            refreshTokenRepository.deleteExpiredTokens(now);

            log.info("[MANTENIMIENTO] Purga de refresh tokens finalizada con éxito.");

        } catch (Exception e) {
            log.error("[MANTENIMIENTO] Error crítico al ejecutar la purga de tokens: {}", e.getMessage(), e);
        }
    }
}
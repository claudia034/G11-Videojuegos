package com.tournament.infrastructure.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
@EnableCaching
public class AppConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("popularGames");
    }
}

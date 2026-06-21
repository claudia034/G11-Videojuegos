package com.tournament.application.service;

import org.springframework.stereotype.Service;

@Service
public class EloCalculatorService {

    private static final int K_FACTOR = 30;

    public int calculateNewElo(int currentElo, int opponentElo, boolean won) {
        double expectedScore = 1.0 / (1.0 + Math.pow(10, (opponentElo - currentElo) / 400.0));
        double actualScore = won ? 1.0 : 0.0;
        
        return (int) Math.round(currentElo + K_FACTOR * (actualScore - expectedScore));
    }
}

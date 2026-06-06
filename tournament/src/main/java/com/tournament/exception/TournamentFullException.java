package com.tournament.exception;

public class TournamentFullException extends RuntimeException {
    public TournamentFullException(Long id, int max) { super(String.format("El torneo %d ya alcanzó el máximo de %d participantes", id, max)); }
}

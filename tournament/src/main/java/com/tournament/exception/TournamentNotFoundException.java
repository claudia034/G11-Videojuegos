package com.tournament.exception;

public class TournamentNotFoundException extends RuntimeException {
    public TournamentNotFoundException(Long id) {
        super(String.format("Torneo con id %d no encontrado", id));
    }
}

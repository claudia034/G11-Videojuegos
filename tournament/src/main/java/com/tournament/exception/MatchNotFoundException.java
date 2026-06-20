package com.tournament.exception;

public class MatchNotFoundException extends RuntimeException {
    public MatchNotFoundException(Long id) {
        super(String.format("Partido con id %d no encontrado", id));
    }
}

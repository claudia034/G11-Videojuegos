package com.tournament.exception;

public class RoundNotFoundException extends RuntimeException {
    public RoundNotFoundException(Long id) { super("Ronda no encontrada: " + id); }
}

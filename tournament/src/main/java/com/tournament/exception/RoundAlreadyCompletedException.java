package com.tournament.exception;

public class RoundAlreadyCompletedException extends RuntimeException {
    public RoundAlreadyCompletedException(Long id) { super("La ronda " + id + " ya está completada"); }
}

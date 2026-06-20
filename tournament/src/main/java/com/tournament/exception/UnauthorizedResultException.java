package com.tournament.exception;

public class UnauthorizedResultException extends RuntimeException {
    public UnauthorizedResultException() {
        super("Solo los participantes del partido pueden reportar o disputar el resultado");
    }
}

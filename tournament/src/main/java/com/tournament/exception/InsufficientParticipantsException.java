package com.tournament.exception;

public class InsufficientParticipantsException extends RuntimeException {
    public InsufficientParticipantsException(int actual, int req) { super(String.format("Participantes insuficientes: %d confirmados, mínimo: %d", actual, req)); }
}

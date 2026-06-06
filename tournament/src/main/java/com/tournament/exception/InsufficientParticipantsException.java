package com.tournament.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class InsufficientParticipantsException extends RuntimeException {
    public InsufficientParticipantsException(int actual, int req) { super(String.format("Participantes insuficientes: %d confirmados, mínimo: %d", actual, req)); }
}

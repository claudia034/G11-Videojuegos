package com.tournament.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(HttpStatus.CONFLICT)
public class RoundAlreadyCompletedException extends RuntimeException {
    public RoundAlreadyCompletedException(Long id) { super("La ronda " + id + " ya está completada"); }
}

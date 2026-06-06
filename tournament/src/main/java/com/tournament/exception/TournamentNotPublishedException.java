package com.tournament.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(HttpStatus.CONFLICT)
public class TournamentNotPublishedException extends RuntimeException {
    public TournamentNotPublishedException(Long id, Object status) { super(String.format("El torneo %d no acepta inscripciones (estado: %s)", id, status)); }
}

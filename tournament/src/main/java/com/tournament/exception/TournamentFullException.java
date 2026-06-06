package com.tournament.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(HttpStatus.CONFLICT)
public class TournamentFullException extends RuntimeException {
    public TournamentFullException(Long id, int max) { super(String.format("El torneo %d ya alcanzó el máximo de %d participantes", id, max)); }
}

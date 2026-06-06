package com.tournament.exception;

public class TournamentNotPublishedException extends RuntimeException {
    public TournamentNotPublishedException(Long id, Object status) { super(String.format("El torneo %d no acepta inscripciones (estado: %s)", id, status)); }
}

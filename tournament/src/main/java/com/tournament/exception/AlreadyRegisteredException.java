package com.tournament.exception;

public class AlreadyRegisteredException extends RuntimeException {
    public AlreadyRegisteredException(String name, Long id) { super(String.format("'%s' ya está inscrito en el torneo %d", name, id)); }
}

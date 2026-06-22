package com.tournament.exception;

public class TeamNotFoundException extends RuntimeException {
    public TeamNotFoundException(Long id) {
        super(String.format("Equipo con id %d no encontrado", id));
    }
}

package com.tournament.exception;

public class PlayerNotFoundException extends RuntimeException {
    public PlayerNotFoundException(Long id) {
        super(String.format("Jugador con id %d no encontrado", id));
    }
}

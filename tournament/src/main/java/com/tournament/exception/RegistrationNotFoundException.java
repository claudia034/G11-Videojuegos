package com.tournament.exception;

public class RegistrationNotFoundException extends RuntimeException {
    public RegistrationNotFoundException(Long id) { super("Inscripción no encontrada: " + id); }
}

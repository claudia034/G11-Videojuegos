package com.tournament.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(HttpStatus.NOT_FOUND)
public class RegistrationNotFoundException extends RuntimeException {
    public RegistrationNotFoundException(Long id) { super("Inscripción no encontrada: " + id); }
}

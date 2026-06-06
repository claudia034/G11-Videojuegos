package com.tournament.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class EloRequirementNotMetException extends RuntimeException {
    public EloRequirementNotMetException(int elo, int min, int max) { super(String.format("ELO %d no cumple el rango requerido [%d - %d]", elo, min, max)); }
}

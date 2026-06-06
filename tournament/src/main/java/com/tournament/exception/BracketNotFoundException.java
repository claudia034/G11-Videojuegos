package com.tournament.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(HttpStatus.NOT_FOUND)
public class BracketNotFoundException extends RuntimeException {
    public BracketNotFoundException(Long id) { super("El torneo " + id + " no tiene bracket generado aún"); }
}

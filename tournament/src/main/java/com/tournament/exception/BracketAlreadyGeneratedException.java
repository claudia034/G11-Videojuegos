package com.tournament.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(HttpStatus.CONFLICT)
public class BracketAlreadyGeneratedException extends RuntimeException {
    public BracketAlreadyGeneratedException(Long id) { super("El torneo " + id + " ya tiene un bracket generado"); }
}

package com.tournament.exception;

public class BracketAlreadyGeneratedException extends RuntimeException {
    public BracketAlreadyGeneratedException(Long id) { super("El torneo " + id + " ya tiene un bracket generado"); }
}

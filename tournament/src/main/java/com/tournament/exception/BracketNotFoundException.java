package com.tournament.exception;

public class BracketNotFoundException extends RuntimeException {
    public BracketNotFoundException(Long id) { super("El torneo " + id + " no tiene bracket generado aún"); }
}

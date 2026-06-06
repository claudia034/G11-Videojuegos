package com.tournament.exception;

public class EloRequirementNotMetException extends RuntimeException {
    public EloRequirementNotMetException(int elo, int min, int max) { super(String.format("ELO %d no cumple el rango requerido [%d - %d]", elo, min, max)); }
}

package com.tournament.exception;

public class AccountDisabledException extends RuntimeException {
    public AccountDisabledException() {
        super("Esta cuenta ha sido deshabilitada. Contante al administrador");
    }
}

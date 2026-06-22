package com.tournament.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Credenciales invalidas. Verifique su email y contraseña");
    }
}

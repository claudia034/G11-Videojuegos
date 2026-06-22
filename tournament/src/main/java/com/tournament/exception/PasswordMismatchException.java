package com.tournament.exception;

public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException() {

        super("La contrasena y su confirmacion no coinciden");
    }
}

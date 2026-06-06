package com.tournament.exception;

public class RegistrationWithdrawNotAllowedException extends RuntimeException {
    public RegistrationWithdrawNotAllowedException(String msg) { super(msg); }
}

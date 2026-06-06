package com.tournament.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(HttpStatus.CONFLICT)
public class RegistrationWithdrawNotAllowedException extends RuntimeException {
    public RegistrationWithdrawNotAllowedException(String msg) { super(msg); }
}

package com.tournament.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRegistrationTypeException extends RuntimeException {
    public InvalidRegistrationTypeException(String msg) { super(msg); }
}

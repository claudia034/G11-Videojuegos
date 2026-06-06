package com.tournament.exception;

public class ForbiddenOperationException extends RuntimeException {
    public ForbiddenOperationException(String msg) { super(msg); }
}

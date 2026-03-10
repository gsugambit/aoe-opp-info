package com.theboys.aoe.opp.info.exception;

public class InvalidRequestException extends BusinessException {

    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(String message, Throwable e) {
        super(message, e);
    }
}

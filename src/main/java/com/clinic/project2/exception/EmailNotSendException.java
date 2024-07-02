package com.clinic.project2.exception;

public class EmailNotSendException extends RuntimeException {


    public EmailNotSendException(String message, Throwable throwable) {
        super(message, throwable);
    }


    EmailNotSendException(){
    }
}

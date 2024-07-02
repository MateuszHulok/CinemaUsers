package com.clinic.project2.exception;

public class EmailAlreadyUsedException extends RuntimeException{

    public EmailAlreadyUsedException(String message) {
        super(message);
    }

    EmailAlreadyUsedException(){
    }
}

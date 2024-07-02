package com.clinic.project2.exception;

public class ClientNotFoundException extends RuntimeException {

    public ClientNotFoundException(String message) {
        super(message);
    }

    ClientNotFoundException(){
    }
}

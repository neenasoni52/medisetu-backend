package com.medisetu.medisetu_backend.exception;



public class InvalidStateException extends RuntimeException {
    public InvalidStateException(String message) {
        super(message);
    }
}
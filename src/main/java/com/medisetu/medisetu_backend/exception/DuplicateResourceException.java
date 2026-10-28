package com.medisetu.medisetu_backend.exception;



public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
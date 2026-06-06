package com.autosalon.service;

public class StorageServiceUnavailableException extends RuntimeException {
    public StorageServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.musiccatalog.exception;

public class ItunesApiException extends RuntimeException {

    public ItunesApiException(String message) {
        super(message);
    }

    public ItunesApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

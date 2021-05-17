package com.lisko.SkypeReaderApp.database;

public class DatabaseErrorException extends Exception {

    public DatabaseErrorException(String message) {
        super(message);
    }

    public DatabaseErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseErrorException(Throwable cause) {
        this(cause == null ? null : cause.getMessage(), cause);
    }
}

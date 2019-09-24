package com.g.bacnet2xlink.exception;

public class AppException extends Exception {
    private String code;

    public AppException(String message) {
        super(message);
        this.code = "SYSTEM_ERROR";
    }

    public AppException(String message, String code) {
        super(message);
        this.code = code;
    }

    public AppException(String message, String code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

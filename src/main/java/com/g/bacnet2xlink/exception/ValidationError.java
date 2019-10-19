package com.g.bacnet2xlink.exception;

public class ValidationError extends AppException {
    public ValidationError() {
        super("未定义的设备", "VALIDATION_ERROR");
    }
}

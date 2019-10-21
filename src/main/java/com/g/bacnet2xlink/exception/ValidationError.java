package com.g.bacnet2xlink.exception;

public class ValidationError extends AppException {
    public ValidationError() {
        super("数据包格式错误", "VALIDATION_ERROR");
    }
}

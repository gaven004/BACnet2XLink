package com.g.bacnet2xlink.exception;

public class UnknownService extends AppException {
    public UnknownService() {
        super("未定义的服务", "UNKNOWN_SERVICE");
    }
}

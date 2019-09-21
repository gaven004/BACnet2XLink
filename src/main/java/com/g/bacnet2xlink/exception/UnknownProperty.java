package com.g.bacnet2xlink.exception;

public class UnknownProperty extends AppException {
    public UnknownProperty() {
        super("未定义的属性", "UNKNOWN_PROPERTY");
    }
}

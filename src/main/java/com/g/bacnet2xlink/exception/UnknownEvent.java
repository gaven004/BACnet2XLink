package com.g.bacnet2xlink.exception;

public class UnknownEvent extends AppException {
    public UnknownEvent() {
        super("未定义的事件", "UNKNOWN_EVENT");
    }
}

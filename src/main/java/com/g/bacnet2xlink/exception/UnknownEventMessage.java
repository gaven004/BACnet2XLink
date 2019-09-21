package com.g.bacnet2xlink.exception;

public class UnknownEventMessage extends AppException {
    public UnknownEventMessage() {
        super("未定义的事件消息", "UNKNOWN_EVENT_MESSAGE");
    }
}

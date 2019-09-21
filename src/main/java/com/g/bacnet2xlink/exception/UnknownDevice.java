package com.g.bacnet2xlink.exception;

public class UnknownDevice extends AppException {
    public UnknownDevice() {
        super("未定义的设备", "UNKNOWN_DEVICE");
    }
}

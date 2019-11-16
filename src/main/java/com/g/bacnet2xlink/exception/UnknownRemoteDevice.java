package com.g.bacnet2xlink.exception;

public class UnknownRemoteDevice extends AppException {
    public UnknownRemoteDevice() {
        super("未定义的BACnet远程设备", "UNKNOWN_REMOTE_DEVICE");
    }
}

package com.g.bacnet2xlink.model.elevator;

public final class Constants {
    public static final byte CMD_CHECK = 0;
    public static final byte CMD_RST = 1;
    public static final byte CMD_BACKUP = 2;

    public static final int MASK = 0xFF;

    public static final char DELIMITER = '-';

    public static final byte START_OF_TEXT = (byte) (0xA5 & Constants.MASK); // 帧起始标志
    public static final byte MASTER_ADDRESS = (byte) (0x81 & Constants.MASK); // 主机地址，固定为81H
    public static final byte END_OF_TEXT = (byte) (0x5A & Constants.MASK); // 帧结束标志
}

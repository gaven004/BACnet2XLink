package com.g.bacnet2xlink.model.elevator;

import com.g.bacnet2xlink.BitUtil;

public class Helper {
    public static byte summary(byte... bytes) {
        int i = 0;
        for (byte b : bytes) {
            i += ((int) b & BitUtil.MASK);
        }
        return (byte) (i & BitUtil.MASK);
    }

    public static byte[] toByteArray(byte... bytes) {
        byte[] array = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            array[i] = bytes[i];
        }
        return array;
    }
}

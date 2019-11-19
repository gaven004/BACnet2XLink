package com.g.bacnet2xlink;

public final class BitUtil {
    public static final int FLAG_0 = 1;
    public static final int FLAG_1 = 1 << 1;
    public static final int FLAG_2 = 1 << 2;
    public static final int FLAG_3 = 1 << 3;
    public static final int FLAG_4 = 1 << 4;
    public static final int FLAG_5 = 1 << 5;
    public static final int FLAG_6 = 1 << 6;
    public static final int FLAG_7 = 1 << 7;

    public static final int[] FLAGS = {FLAG_0,FLAG_1,FLAG_2,FLAG_3,FLAG_4,FLAG_5,FLAG_6, FLAG_7};

    public static final int MASK = 0xFF;

    public static final char DELIMITER = '-';

    public static int getBit(byte b, int i) {
        if ( i < 0 || i > 7) throw new IllegalArgumentException();

        return b & FLAGS[i];
    }

    public static String toHexString(byte b) {
        return String.format("%02x", b & MASK);
    }

    public static String toBinString(byte b) {
        return String.format("%08s", Integer.toBinaryString(b & MASK));
    }

    public static byte toByte(int i) {
        return (byte) (i & MASK);
    }

    public static String toString(byte... bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (byte b : bytes) {
            sb.append(toHexString(b)).append(DELIMITER);
        }
        return sb.substring(0, bytes.length * 3 - 1);
    }
}

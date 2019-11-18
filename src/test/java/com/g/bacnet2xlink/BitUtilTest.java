package com.g.bacnet2xlink;

import org.junit.Test;

public class BitUtilTest {

    @Test
    public void toByte() {
        for (int i = 0; i < 256; i++) {
            System.out.println(String.format("%d, %s", BitUtil.toByte(i), BitUtil.toHexString(BitUtil.toByte(i))));
        }
    }
}
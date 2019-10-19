package com.g.bacnet2xlink.model.elevator;

import org.junit.Test;

public class ResponseTest {
    @Test
    public void testShift() {
        byte b = 1;
        int i = 0;
        do {
            System.out.println(String.format("%02x, %d", b, b));
            b = (byte) (b << 1);
            i++;
        } while (i < 8);

        System.out.println(String.format("%02x, %d", (byte) (1 << 7), (byte) (1 << 7)));
    }

    @Test
    public void testByte2Int() {
        int i = 0;
        System.out.println(String.format("int %d : %s", i, Integer.toBinaryString(i)));
        System.out.println("To byte: " + (byte)i);
        System.out.println();

        i = 1;
        System.out.println(String.format("int %d : %s", i, Integer.toBinaryString(i)));
        System.out.println("To byte: " + (byte)i);
        System.out.println();

        i = -1;
        System.out.println(String.format("int %d : %s", i, Integer.toBinaryString(i)));
        System.out.println("To byte: " + (byte)i);
        System.out.println();

        i = 127;
        System.out.println(String.format("int %d : %s", i, Integer.toBinaryString(i)));
        System.out.println("To byte: " + (byte)i);
        System.out.println();

        i = -127;
        System.out.println(String.format("int %d : %s", i, Integer.toBinaryString(i)));
        System.out.println("To byte: " + (byte)i);
        System.out.println();

        i = 128;
        System.out.println(String.format("int %d : %s", i, Integer.toBinaryString(i)));
        System.out.println("To byte: " + (byte)i);
        System.out.println();

        i = -128;
        System.out.println(String.format("int %d : %s", i, Integer.toBinaryString(i)));
        System.out.println("To byte: " + (byte)i);
        System.out.println();

        i = Integer.MAX_VALUE;
        System.out.println(String.format("int %d %x: %s", i, i, Integer.toBinaryString(i)));
        System.out.println("To byte: " + (byte)i);
        System.out.println();

        i = Integer.MIN_VALUE;
        System.out.println(String.format("int %d %x : %s", i, i, Integer.toBinaryString(i)));
        System.out.println("To byte: " + (byte)i);
        System.out.println();

        i = Integer.parseInt("01111111111111111111111111111111", 2);
        System.out.println(String.format("int %d : %s", i, Integer.toBinaryString(i)));
        System.out.println("To byte: " + (byte)i);
        System.out.println();

        i = Integer.parseInt("-1111111111111111111111111111111", 2);
        System.out.println(String.format("int %d : %s", i, Integer.toBinaryString(i)));
        System.out.println("To byte: " + (byte)i);
        System.out.println();

        i = Integer.parseInt("110000000", 2);
        System.out.println(String.format("int %d : %s", i, Integer.toBinaryString(i)));
        System.out.println("To byte: " + (byte)i);
        System.out.println();

        System.out.println("---------------------");
        System.out.println();

        byte b = -128;
        i = (int)b;
        System.out.println("Byte: " + b);
        System.out.println(String.format("int %d : %s", i, Integer.toBinaryString(i)));
        System.out.println();

        b = 1;
        i = (int)b;
        System.out.println("Byte: " + b);
        System.out.println(String.format("int %d : %s", i, Integer.toBinaryString(i)));
        System.out.println();

        b = -1;
        i = (int)b;
        System.out.println("Byte: " + b);
        System.out.println(String.format("int %d : %s", i, Integer.toBinaryString(i)));
        System.out.println();

        b = 127;
        i = (int)b;
        System.out.println("Byte: " + b);
        System.out.println(String.format("int %d : %s", i, Integer.toBinaryString(i)));
        System.out.println();

        b = -127;
        i = (int)b;
        System.out.println("Byte: " + b);
        System.out.println(String.format("int %d : %s", i, Integer.toBinaryString(i)));
        System.out.println();

    }

    @Test
    public void fromByteArray() {
    }
}

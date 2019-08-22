package com.serotonin.bacnet4j.type.primitive;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UnsignedIntegerTest {
    @Test
    public void increment32() {
        UnsignedInteger i = new UnsignedInteger(0xFFFFFFFDL);
        assertEquals(0xFFFFFFFDL, i.longValue());

        i = i.increment32();
        assertEquals(0xFFFFFFFEL, i.longValue());

        i = i.increment32();
        assertEquals(0xFFFFFFFFL, i.longValue());

        i = i.increment32();
        assertEquals(0, i.longValue());

        i = i.increment32();
        assertEquals(1, i.longValue());
    }

    @Test
    public void increment16() {
        UnsignedInteger i = new UnsignedInteger(65533);
        assertEquals(65533, i.intValue());

        i = i.increment16();
        assertEquals(65534, i.intValue());

        i = i.increment16();
        assertEquals(65535, i.intValue());

        i = i.increment16();
        assertEquals(0, i.intValue());

        i = i.increment16();
        assertEquals(1, i.intValue());
    }
}

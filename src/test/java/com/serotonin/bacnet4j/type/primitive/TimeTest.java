package com.serotonin.bacnet4j.type.primitive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TimeTest {
    @Test
    public void comparison() {
        assertFalse(new Time(12, 1, 1, 1).before(new Time(11, 2, 2, 2)));
        assertFalse(new Time(12, 1, 1, 1).before(new Time(12, 0, 2, 2)));
        assertFalse(new Time(12, 1, 1, 1).before(new Time(12, 1, 0, 2)));
        assertFalse(new Time(12, 1, 1, 1).before(new Time(12, 1, 1, 1)));
        assertTrue(new Time(12, 1, 1, 1).before(new Time(12, 1, 1, 2)));
        assertTrue(new Time(12, 1, 1, 1).before(new Time(12, 1, 2, 0)));
        assertTrue(new Time(12, 1, 1, 1).before(new Time(12, 2, 0, 0)));
        assertTrue(new Time(12, 1, 1, 1).before(new Time(13, 0, 0, 0)));
    }

    @Test
    public void diff() {
        assertEquals(1, new Time(1, 1, 1, 1).getSmallestDiff(new Time(1, 1, 1, 0)));
        assertEquals(1, new Time(1, 1, 1, 0).getSmallestDiff(new Time(1, 1, 1, 1)));
        assertEquals(240_000, new Time(23, 30, 0, 0).getSmallestDiff(new Time(0, 10, 0, 0)));
        assertEquals(240_000, new Time(0, 10, 0, 0).getSmallestDiff(new Time(23, 30, 0, 0)));
    }
}

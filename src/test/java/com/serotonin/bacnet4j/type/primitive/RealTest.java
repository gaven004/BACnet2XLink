package com.serotonin.bacnet4j.type.primitive;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.util.sero.ByteQueue;

public class RealTest {
    @Test
    public void nan() throws BACnetException {
        final Real r = new Real(Float.NaN);
        final ByteQueue queue = new ByteQueue();
        r.write(queue);

        final Real r2 = Encodable.read(queue, Real.class);
        assertEquals(Float.NaN, r2.floatValue(), 0);
    }

    @Test
    public void neginf() throws BACnetException {
        final Real r = new Real(Float.NEGATIVE_INFINITY);
        final ByteQueue queue = new ByteQueue();
        r.write(queue);

        final Real r2 = Encodable.read(queue, Real.class);
        assertEquals(Float.NEGATIVE_INFINITY, r2.floatValue(), 0);
    }

    @Test
    public void posinf() throws BACnetException {
        final Real r = new Real(Float.POSITIVE_INFINITY);
        final ByteQueue queue = new ByteQueue();
        r.write(queue);

        final Real r2 = Encodable.read(queue, Real.class);
        assertEquals(Float.POSITIVE_INFINITY, r2.floatValue(), 0);
    }
}

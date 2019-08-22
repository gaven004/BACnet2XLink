package com.serotonin.bacnet4j.type.constructed;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import com.serotonin.bacnet4j.exception.BACnetRuntimeException;
import com.serotonin.bacnet4j.type.primitive.CharacterString;

public class BACnetArrayTest {
    @Test
    public void arrayTest() {
        final BACnetArray<CharacterString> arr = new BACnetArray<>(3, CharacterString.EMPTY);
        assertEquals(3, arr.getCount());

        arr.setBase1(1, new CharacterString("A"));
        arr.setBase1(3, new CharacterString("C"));
        assertEquals(3, arr.getCount());
        assertEquals(arr.getBase1(1), new CharacterString("A"));
        assertEquals(arr.getBase1(2), CharacterString.EMPTY);
        assertEquals(arr.getBase1(3), new CharacterString("C"));

        try {
            arr.remove(2);
            Assert.fail("Should have failed");
        } catch (@SuppressWarnings("unused") final BACnetRuntimeException e) {
            // no op
        }

        try {
            arr.add(new CharacterString("D"));
            Assert.fail("Should have failed");
        } catch (@SuppressWarnings("unused") final BACnetRuntimeException e) {
            // no op
        }
    }
}

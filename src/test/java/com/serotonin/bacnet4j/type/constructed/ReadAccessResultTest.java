package com.serotonin.bacnet4j.type.constructed;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.sero.ByteQueue;

public class ReadAccessResultTest {
    @Test
    public void decoding() throws BACnetException {
        final String hex = "0c020000021e294c4ec402000002c4008000004f1f";

        final ReadAccessResult rar = new ReadAccessResult(new ByteQueue(hex));

        assertEquals(ObjectType.device, rar.getObjectIdentifier().getObjectType());
        assertEquals(2, rar.getObjectIdentifier().getInstanceNumber());
        assertEquals(1, rar.getListOfResults().getCount());
        assertEquals(PropertyIdentifier.objectList, rar.getListOfResults().getBase1(1).getPropertyIdentifier());
        assertEquals(null, rar.getListOfResults().getBase1(1).getPropertyArrayIndex());
        assertEquals(true, rar.getListOfResults().getBase1(1).getReadResult().isa(SequenceOf.class));

        final SequenceOf<ObjectIdentifier> oids = rar.getListOfResults().getBase1(1).getReadResult().getDatum();
        assertEquals(2, oids.getCount());
        assertEquals(new ObjectIdentifier(ObjectType.device, 2), oids.getBase1(1));
        assertEquals(new ObjectIdentifier(ObjectType.analogValue, 0), oids.getBase1(2));
    }
}

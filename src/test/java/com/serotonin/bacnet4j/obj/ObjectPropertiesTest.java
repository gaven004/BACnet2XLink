package com.serotonin.bacnet4j.obj;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.enumerated.Reliability;

public class ObjectPropertiesTest {
    @Test
    public void propertyDefinition() {
        // Reliability only ever has a type of Reliability
        final PropertyTypeDefinition def = ObjectProperties.getPropertyTypeDefinition(PropertyIdentifier.reliability);
        assertEquals(Reliability.class, def.getClazz());
        assertEquals(null, def.getInnerType());
        assertEquals(PropertyIdentifier.reliability, def.getPropertyIdentifier());

        // Present value takes on different values in different objects.
        assertNull(ObjectProperties.getPropertyTypeDefinition(PropertyIdentifier.presentValue));
    }
}

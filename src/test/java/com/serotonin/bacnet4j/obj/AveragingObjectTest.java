package com.serotonin.bacnet4j.obj;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serotonin.bacnet4j.AbstractTest;
import com.serotonin.bacnet4j.TestUtils;
import com.serotonin.bacnet4j.type.constructed.DateTime;
import com.serotonin.bacnet4j.type.constructed.DeviceObjectPropertyReference;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.enumerated.EngineeringUnits;
import com.serotonin.bacnet4j.type.enumerated.ErrorClass;
import com.serotonin.bacnet4j.type.enumerated.ErrorCode;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

/**
 * @author Matthew
 */
public class AveragingObjectTest extends AbstractTest {
    static final Logger LOG = LoggerFactory.getLogger(AveragingObjectTest.class);

    private AveragingObject a;

    @Override
    public void afterInit() throws Exception {
        // Poll every 5s
        a = new AveragingObject(d1, 0, "a0", new DeviceObjectPropertyReference(1,
                new ObjectIdentifier(ObjectType.device, 1), PropertyIdentifier.systemStatus), 60, 12);
    }

    @Test
    public void real() throws Exception {
        // Let the initial poll complete
        Thread.sleep(50);

        final AnalogInputObject ai = new AnalogInputObject(d1, 0, "ai0", 0, EngineeringUnits.noUnits, false);

        // Reference the present value of the analog input.
        a.writeProperty(null, PropertyIdentifier.objectPropertyReference,
                new DeviceObjectPropertyReference(1, ai.getId(), PropertyIdentifier.presentValue));

        assertEquals(new Real(Float.POSITIVE_INFINITY), a.readProperty(PropertyIdentifier.minimumValue));
        assertEquals(DateTime.UNSPECIFIED, a.readProperty(PropertyIdentifier.minimumValueTimestamp));
        assertEquals(new Real(Float.NaN), a.readProperty(PropertyIdentifier.averageValue));
        assertEquals(new Real(Float.NaN), a.readProperty(PropertyIdentifier.varianceValue));
        assertEquals(new Real(Float.NEGATIVE_INFINITY), a.readProperty(PropertyIdentifier.maximumValue));
        assertEquals(DateTime.UNSPECIFIED, a.readProperty(PropertyIdentifier.maximumValueTimestamp));
        assertEquals(UnsignedInteger.ZERO, a.readProperty(PropertyIdentifier.attemptedSamples));
        assertEquals(UnsignedInteger.ZERO, a.readProperty(PropertyIdentifier.validSamples));

        // Set to 1 and poll.
        ai.set(PropertyIdentifier.presentValue, new Real(1));
        clock.plus(5, TimeUnit.SECONDS, 20);
        final DateTime ts1 = new DateTime(clock.millis());
        assertEquals(new Real(1), a.readProperty(PropertyIdentifier.minimumValue));
        assertEquals(ts1, a.readProperty(PropertyIdentifier.minimumValueTimestamp));
        assertEquals(new Real(1), a.readProperty(PropertyIdentifier.averageValue));
        assertEquals(new Real(0), a.readProperty(PropertyIdentifier.varianceValue));
        assertEquals(new Real(1), a.readProperty(PropertyIdentifier.maximumValue));
        assertEquals(ts1, a.readProperty(PropertyIdentifier.maximumValueTimestamp));
        assertEquals(new UnsignedInteger(1), a.readProperty(PropertyIdentifier.attemptedSamples));
        assertEquals(new UnsignedInteger(1), a.readProperty(PropertyIdentifier.validSamples));

        // Set to 5 and poll
        ai.set(PropertyIdentifier.presentValue, new Real(5));
        clock.plus(5, TimeUnit.SECONDS, 20);
        final DateTime ts2 = new DateTime(clock.millis());
        assertEquals(new Real(1), a.readProperty(PropertyIdentifier.minimumValue));
        assertEquals(ts1, a.readProperty(PropertyIdentifier.minimumValueTimestamp));
        assertEquals(new Real(3), a.readProperty(PropertyIdentifier.averageValue));
        assertEquals(new Real(4), a.readProperty(PropertyIdentifier.varianceValue));
        assertEquals(new Real(5), a.readProperty(PropertyIdentifier.maximumValue));
        assertEquals(ts2, a.readProperty(PropertyIdentifier.maximumValueTimestamp));
        assertEquals(new UnsignedInteger(2), a.readProperty(PropertyIdentifier.attemptedSamples));
        assertEquals(new UnsignedInteger(2), a.readProperty(PropertyIdentifier.validSamples));

        // Set to 3 and poll
        ai.set(PropertyIdentifier.presentValue, new Real(3));
        clock.plus(5, TimeUnit.SECONDS, 20);
        assertEquals(new Real(1), a.readProperty(PropertyIdentifier.minimumValue));
        assertEquals(ts1, a.readProperty(PropertyIdentifier.minimumValueTimestamp));
        assertEquals(new Real(3), a.readProperty(PropertyIdentifier.averageValue));
        assertEquals(new Real(4), a.readProperty(PropertyIdentifier.varianceValue));
        assertEquals(new Real(5), a.readProperty(PropertyIdentifier.maximumValue));
        assertEquals(ts2, a.readProperty(PropertyIdentifier.maximumValueTimestamp));
        assertEquals(new UnsignedInteger(3), a.readProperty(PropertyIdentifier.attemptedSamples));
        assertEquals(new UnsignedInteger(3), a.readProperty(PropertyIdentifier.validSamples));

        // Set to 10 and poll
        ai.set(PropertyIdentifier.presentValue, new Real(10));
        clock.plus(5, TimeUnit.SECONDS, 20);
        final DateTime ts4 = new DateTime(clock.millis());
        assertEquals(new Real(1), a.readProperty(PropertyIdentifier.minimumValue));
        assertEquals(ts1, a.readProperty(PropertyIdentifier.minimumValueTimestamp));
        assertEquals(new Real(4.75F), a.readProperty(PropertyIdentifier.averageValue));
        assertEquals(new Real(9), a.readProperty(PropertyIdentifier.varianceValue));
        assertEquals(new Real(10), a.readProperty(PropertyIdentifier.maximumValue));
        assertEquals(ts4, a.readProperty(PropertyIdentifier.maximumValueTimestamp));
        assertEquals(new UnsignedInteger(4), a.readProperty(PropertyIdentifier.attemptedSamples));
        assertEquals(new UnsignedInteger(4), a.readProperty(PropertyIdentifier.validSamples));

        // Set to 9 and poll 7 times
        ai.set(PropertyIdentifier.presentValue, new Real(9));
        clock.plus(35, TimeUnit.SECONDS, 5, TimeUnit.SECONDS, 20, 0);
        assertEquals(new Real(1), a.readProperty(PropertyIdentifier.minimumValue));
        assertEquals(ts1, a.readProperty(PropertyIdentifier.minimumValueTimestamp));
        assertEquals(7.454F, ((Real) a.readProperty(PropertyIdentifier.averageValue)).floatValue(), 0.001F);
        assertEquals(new Real(9), a.readProperty(PropertyIdentifier.varianceValue));
        assertEquals(new Real(10), a.readProperty(PropertyIdentifier.maximumValue));
        assertEquals(ts4, a.readProperty(PropertyIdentifier.maximumValueTimestamp));
        assertEquals(new UnsignedInteger(11), a.readProperty(PropertyIdentifier.attemptedSamples));
        assertEquals(new UnsignedInteger(11), a.readProperty(PropertyIdentifier.validSamples));

        // Set to -1 and poll
        ai.set(PropertyIdentifier.presentValue, new Real(-1));
        clock.plus(5, TimeUnit.SECONDS, 20);
        final DateTime ts12 = new DateTime(clock.millis());
        assertEquals(new Real(-1), a.readProperty(PropertyIdentifier.minimumValue));
        assertEquals(ts12, a.readProperty(PropertyIdentifier.minimumValueTimestamp));
        assertEquals(new Real(6.75F), a.readProperty(PropertyIdentifier.averageValue));
        assertEquals(new Real(11), a.readProperty(PropertyIdentifier.varianceValue));
        assertEquals(new Real(10), a.readProperty(PropertyIdentifier.maximumValue));
        assertEquals(ts4, a.readProperty(PropertyIdentifier.maximumValueTimestamp));
        assertEquals(new UnsignedInteger(12), a.readProperty(PropertyIdentifier.attemptedSamples));
        assertEquals(new UnsignedInteger(12), a.readProperty(PropertyIdentifier.validSamples));

        // Set to 8 and poll
        ai.set(PropertyIdentifier.presentValue, new Real(8));
        clock.plus(5, TimeUnit.SECONDS, 20);
        assertEquals(new Real(-1), a.readProperty(PropertyIdentifier.minimumValue));
        assertEquals(ts12, a.readProperty(PropertyIdentifier.minimumValueTimestamp));
        assertEquals(7.333F, ((Real) a.readProperty(PropertyIdentifier.averageValue)).floatValue(), 0.001F);
        assertEquals(new Real(11), a.readProperty(PropertyIdentifier.varianceValue));
        assertEquals(new Real(10), a.readProperty(PropertyIdentifier.maximumValue));
        assertEquals(ts4, a.readProperty(PropertyIdentifier.maximumValueTimestamp));
        assertEquals(new UnsignedInteger(12), a.readProperty(PropertyIdentifier.attemptedSamples));
        assertEquals(new UnsignedInteger(12), a.readProperty(PropertyIdentifier.validSamples));
    }

    @Test
    public void propertyConformanceRequired() throws Exception {
        assertNotNull(a.readProperty(PropertyIdentifier.objectIdentifier));
        assertNotNull(a.readProperty(PropertyIdentifier.objectName));
        assertNotNull(a.readProperty(PropertyIdentifier.objectType));
        assertNotNull(a.readProperty(PropertyIdentifier.minimumValue));
        assertNotNull(a.readProperty(PropertyIdentifier.averageValue));
        assertNotNull(a.readProperty(PropertyIdentifier.maximumValue));
        assertNotNull(a.readProperty(PropertyIdentifier.attemptedSamples));
        assertNotNull(a.readProperty(PropertyIdentifier.validSamples));
        assertNotNull(a.readProperty(PropertyIdentifier.objectPropertyReference));
        assertNotNull(a.readProperty(PropertyIdentifier.windowInterval));
        assertNotNull(a.readProperty(PropertyIdentifier.windowSamples));
        assertNotNull(a.readProperty(PropertyIdentifier.propertyList));
    }

    @Test
    public void propertyConformanceReadOnly() {
        TestUtils.assertBACnetServiceException(
                () -> a.writeProperty(null,
                        new PropertyValue(PropertyIdentifier.validSamples, null, UnsignedInteger.ZERO, null)),
                ErrorClass.property, ErrorCode.writeAccessDenied);
    }
}

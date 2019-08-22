package com.serotonin.bacnet4j.service.confirmed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.serotonin.bacnet4j.AbstractTest;
import com.serotonin.bacnet4j.TestUtils;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.obj.CalendarObject;
import com.serotonin.bacnet4j.obj.EventLogObject;
import com.serotonin.bacnet4j.obj.GroupObject;
import com.serotonin.bacnet4j.obj.NotificationClassObject;
import com.serotonin.bacnet4j.obj.ScheduleObject;
import com.serotonin.bacnet4j.obj.TrendLogMultipleObject;
import com.serotonin.bacnet4j.obj.TrendLogObject;
import com.serotonin.bacnet4j.service.acknowledgement.CreateObjectAck;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.ReadAccessSpecification;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.ErrorClass;
import com.serotonin.bacnet4j.type.enumerated.ErrorCode;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.error.CreateObjectError;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

public class CreateObjectRequestTest extends AbstractTest {
    private GroupObject go0;

    @Override
    public void afterInit() throws Exception {
        go0 = new GroupObject(d1, 0, "existing group", new SequenceOf<>());
    }

    @Test
    public void unsupportedObjectType() throws Exception {
        final CreateObjectError err = TestUtils.assertErrorAPDUException(() -> {
            d2.send(rd1, new CreateObjectRequest(new ObjectIdentifier(ObjectType.lift, 0), new SequenceOf<>())).get();
        }, ErrorClass.object, ErrorCode.unsupportedObjectType);

        assertEquals(new UnsignedInteger(0), err.getFirstFailedElementNumber());
    }

    @Test
    public void noCreator() throws Exception {
        final CreateObjectError err = TestUtils.assertErrorAPDUException(() -> {
            d2.send(rd1, new CreateObjectRequest(ObjectType.device, new SequenceOf<>())).get();
        }, ErrorClass.object, ErrorCode.dynamicCreationNotSupported);

        assertEquals(new UnsignedInteger(0), err.getFirstFailedElementNumber());
    }

    @Test
    public void duplicateInstanceNumber() throws Exception {
        final CreateObjectError err = TestUtils.assertErrorAPDUException(() -> {
            d2.send(rd1, new CreateObjectRequest(go0.getId(), new SequenceOf<>())).get();
        }, ErrorClass.object, ErrorCode.objectIdentifierAlreadyExists);

        assertEquals(new UnsignedInteger(0), err.getFirstFailedElementNumber());
    }

    @Test
    public void duplicateName() throws Exception {
        final CreateObjectError err = TestUtils.assertErrorAPDUException(() -> {
            d2.send(rd1,
                    new CreateObjectRequest(ObjectType.group, new SequenceOf<>(
                            new PropertyValue(PropertyIdentifier.objectName, new CharacterString("existing group")),
                            new PropertyValue(PropertyIdentifier.listOfGroupMembers,
                                    new SequenceOf<>(
                                            new ReadAccessSpecification(new ObjectIdentifier(ObjectType.analogInput, 0),
                                                    PropertyIdentifier.presentValue))))))
                    .get();
        }, ErrorClass.property, ErrorCode.duplicateName);

        assertEquals(new UnsignedInteger(1), err.getFirstFailedElementNumber());
    }

    @Test
    public void badParameter() throws Exception {
        final CreateObjectError err = TestUtils.assertErrorAPDUException(() -> {
            d2.send(rd1,
                    new CreateObjectRequest(ObjectType.group,
                            new SequenceOf<>(new PropertyValue(PropertyIdentifier.apduLength, UnsignedInteger.ZERO))))
                    .get();
        }, ErrorClass.property, ErrorCode.unknownProperty);

        assertEquals(new UnsignedInteger(1), err.getFirstFailedElementNumber());
    }

    @Test
    public void createCalendar() throws Exception {
        final CreateObjectAck ack = (CreateObjectAck) new CreateObjectRequest(ObjectType.calendar, new SequenceOf<>())
                .handle(d1, null);

        assertEquals(new ObjectIdentifier(ObjectType.calendar, 0), ack.getObjectIdentifier());
        final BACnetObject bo = d1.getObject(ack.getObjectIdentifier());
        assertTrue(bo instanceof CalendarObject);
        assertEquals("calendar 0", bo.getObjectName());
    }

    @Test
    public void createEventLog() throws Exception {
        final CreateObjectAck ack = (CreateObjectAck) new CreateObjectRequest(ObjectType.eventLog, new SequenceOf<>())
                .handle(d1, null);

        assertEquals(new ObjectIdentifier(ObjectType.eventLog, 0), ack.getObjectIdentifier());
        final BACnetObject bo = d1.getObject(ack.getObjectIdentifier());
        assertTrue(bo instanceof EventLogObject);
        assertEquals("event-log 0", bo.getObjectName());
    }

    @Test
    public void createGroup() throws Exception {
        final SequenceOf<ReadAccessSpecification> listOfGroupMembers = new SequenceOf<>(new ReadAccessSpecification(
                new ObjectIdentifier(ObjectType.analogInput, 0), PropertyIdentifier.presentValue));

        final CreateObjectAck ack = (CreateObjectAck) d2
                .send(rd1, new CreateObjectRequest(ObjectType.group,
                        new SequenceOf<>(
                                new PropertyValue(PropertyIdentifier.objectName, new CharacterString("new group")),
                                new PropertyValue(PropertyIdentifier.listOfGroupMembers, listOfGroupMembers))))
                .get();

        assertEquals(new ObjectIdentifier(ObjectType.group, 1), ack.getObjectIdentifier());
        final BACnetObject bo = d1.getObject(ack.getObjectIdentifier());
        assertTrue(bo instanceof GroupObject);
        assertEquals("new group", bo.getObjectName());
        assertEquals(listOfGroupMembers, bo.get(PropertyIdentifier.listOfGroupMembers));
    }

    @Test
    public void createNotificationClass() throws Exception {
        final CreateObjectAck ack = (CreateObjectAck) new CreateObjectRequest(ObjectType.notificationClass,
                new SequenceOf<>(new PropertyValue(PropertyIdentifier.objectIdentifier,
                        new ObjectIdentifier(ObjectType.notificationClass, 17)))).handle(d1, null);

        assertEquals(new ObjectIdentifier(ObjectType.notificationClass, 17), ack.getObjectIdentifier());
        final BACnetObject bo = d1.getObject(ack.getObjectIdentifier());
        assertTrue(bo instanceof NotificationClassObject);
        assertEquals("notification-class 0", bo.getObjectName());
    }

    @Test
    public void createScheduleLog() throws Exception {
        final CreateObjectAck ack = (CreateObjectAck) new CreateObjectRequest(ObjectType.schedule, new SequenceOf<>())
                .handle(d1, null);

        assertEquals(new ObjectIdentifier(ObjectType.schedule, 0), ack.getObjectIdentifier());
        final BACnetObject bo = d1.getObject(ack.getObjectIdentifier());
        assertTrue(bo instanceof ScheduleObject);
        assertEquals("schedule 0", bo.getObjectName());
    }

    @Test
    public void createTrendLogLog() throws Exception {
        final CreateObjectAck ack = (CreateObjectAck) new CreateObjectRequest(ObjectType.trendLog, new SequenceOf<>())
                .handle(d1, null);

        assertEquals(new ObjectIdentifier(ObjectType.trendLog, 0), ack.getObjectIdentifier());
        final BACnetObject bo = d1.getObject(ack.getObjectIdentifier());
        assertTrue(bo instanceof TrendLogObject);
        assertEquals("trend-log 0", bo.getObjectName());
    }

    @Test
    public void createTrendLogMultiple() throws Exception {
        final CreateObjectAck ack = (CreateObjectAck) new CreateObjectRequest(ObjectType.trendLogMultiple,
                new SequenceOf<>()).handle(d1, null);

        assertEquals(new ObjectIdentifier(ObjectType.trendLogMultiple, 0), ack.getObjectIdentifier());
        final BACnetObject bo = d1.getObject(ack.getObjectIdentifier());
        assertTrue(bo instanceof TrendLogMultipleObject);
        assertEquals("trend-log-multiple 0", bo.getObjectName());
    }
}

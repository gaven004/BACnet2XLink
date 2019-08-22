package com.serotonin.bacnet4j.obj;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import com.serotonin.bacnet4j.AbstractTest;
import com.serotonin.bacnet4j.TestUtils;
import com.serotonin.bacnet4j.type.constructed.BACnetArray;
import com.serotonin.bacnet4j.type.constructed.Destination;
import com.serotonin.bacnet4j.type.constructed.EventTransitionBits;
import com.serotonin.bacnet4j.type.constructed.Recipient;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.constructed.TimeStamp;
import com.serotonin.bacnet4j.type.enumerated.EngineeringUnits;
import com.serotonin.bacnet4j.type.enumerated.EventState;
import com.serotonin.bacnet4j.type.enumerated.EventType;
import com.serotonin.bacnet4j.type.enumerated.NotifyType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.notificationParameters.ExtendedNotif;
import com.serotonin.bacnet4j.type.notificationParameters.ExtendedNotif.Parameter;
import com.serotonin.bacnet4j.type.notificationParameters.NotificationParameters;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.Double;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

public class AlertEnrollmentObjectTest extends AbstractTest {
    private AnalogValueObject av0;
    private NotificationClassObject nc;

    @Override
    public void afterInit() throws Exception {
        av0 = new AnalogValueObject(d1, 0, "av0", 0, EngineeringUnits.noUnits, false);
        nc = new NotificationClassObject(d1, 55, "nc55", 101, 4, 201, new EventTransitionBits(false, false, false));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void alertReporting() throws Exception {
        final AlertEnrollmentObject ae = new AlertEnrollmentObject(d1, 0, "ae0", 55, NotifyType.alarm);

        final SequenceOf<Destination> recipients = nc.get(PropertyIdentifier.recipientList);
        recipients.add(new Destination(new Recipient(rd2.getAddress()), new UnsignedInteger(10), Boolean.TRUE,
                new EventTransitionBits(true, true, true)));

        // Create an event listener on d2 to catch the event notifications.
        final EventNotifListener listener = new EventNotifListener();
        d2.getEventHandler().addListener(listener);

        // Ensure that initializing the event enrollment object didn't fire any notifications.
        assertEquals(0, listener.notifs.size());

        // Issue an alert from the AV.
        ae.issueAlert(av0.getId(), 123, //
                new Parameter(new Real(234)), //
                new Parameter(new CharacterString("some string")), //
                new Parameter(new Double(3.14)));

        // Wait for the alarm to be sent
        Thread.sleep(100);
        assertEquals(EventState.normal, ae.readProperty(PropertyIdentifier.eventState)); // Still normal. Always normal.

        // Ensure that a proper looking event notification was received.
        assertEquals(1, listener.notifs.size());
        final Map<String, Object> notif = listener.notifs.remove(0);
        assertEquals(new UnsignedInteger(10), notif.get("processIdentifier"));
        assertEquals(rd1.getObjectIdentifier(), notif.get("initiatingDevice"));
        assertEquals(ae.getId(), notif.get("eventObjectIdentifier"));
        assertEquals(((BACnetArray<TimeStamp>) ae.readProperty(PropertyIdentifier.eventTimeStamps))
                .getBase1(EventState.normal.getTransitionIndex()), notif.get("timeStamp"));
        assertEquals(new UnsignedInteger(55), notif.get("notificationClass"));
        assertEquals(new UnsignedInteger(201), notif.get("priority"));
        assertEquals(EventType.extended, notif.get("eventType"));
        assertEquals(null, notif.get("messageText"));
        assertEquals(NotifyType.alarm, notif.get("notifyType"));
        assertEquals(Boolean.FALSE, notif.get("ackRequired"));
        assertEquals(EventState.normal, notif.get("fromState"));
        assertEquals(EventState.normal, notif.get("toState"));
        assertEquals(new NotificationParameters(new ExtendedNotif( //
                d1.get(PropertyIdentifier.vendorIdentifier), //
                new UnsignedInteger(123), //
                new SequenceOf<>( //
                        new Parameter(av0.getId()), //
                        new Parameter(new Real(234)), //
                        new Parameter(new CharacterString("some string")), //
                        new Parameter(new Double(3.14))))),
                notif.get("eventValues"));

        // Make sure the list of properties looks right.
        final SequenceOf<PropertyIdentifier> propertyList = ae.readProperty(PropertyIdentifier.propertyList);
        TestUtils.assertListEqualsIgnoreOrder(TestUtils.toList( //
                PropertyIdentifier.presentValue, //
                PropertyIdentifier.eventState, //
                PropertyIdentifier.eventDetectionEnable, //
                PropertyIdentifier.notificationClass, //
                PropertyIdentifier.eventEnable, //
                PropertyIdentifier.ackedTransitions, //
                PropertyIdentifier.notifyType, //
                PropertyIdentifier.eventTimeStamps, //
                PropertyIdentifier.eventMessageTexts, //
                PropertyIdentifier.eventMessageTextsConfig, //
                PropertyIdentifier.eventAlgorithmInhibit), propertyList.getValues());
    }
}

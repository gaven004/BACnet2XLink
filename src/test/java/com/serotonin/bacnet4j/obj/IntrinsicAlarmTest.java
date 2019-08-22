package com.serotonin.bacnet4j.obj;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serotonin.bacnet4j.AbstractTest;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.service.acknowledgement.GetAlarmSummaryAck;
import com.serotonin.bacnet4j.service.acknowledgement.GetAlarmSummaryAck.AlarmSummary;
import com.serotonin.bacnet4j.service.acknowledgement.GetEnrollmentSummaryAck;
import com.serotonin.bacnet4j.service.acknowledgement.GetEnrollmentSummaryAck.EnrollmentSummary;
import com.serotonin.bacnet4j.service.acknowledgement.GetEventInformationAck;
import com.serotonin.bacnet4j.service.acknowledgement.GetEventInformationAck.EventSummary;
import com.serotonin.bacnet4j.service.confirmed.AcknowledgeAlarmRequest;
import com.serotonin.bacnet4j.service.confirmed.GetAlarmSummaryRequest;
import com.serotonin.bacnet4j.service.confirmed.GetEnrollmentSummaryRequest;
import com.serotonin.bacnet4j.service.confirmed.GetEnrollmentSummaryRequest.AcknowledgmentFilter;
import com.serotonin.bacnet4j.service.confirmed.GetEnrollmentSummaryRequest.EventStateFilter;
import com.serotonin.bacnet4j.service.confirmed.GetEnrollmentSummaryRequest.PriorityFilter;
import com.serotonin.bacnet4j.service.confirmed.GetEventInformationRequest;
import com.serotonin.bacnet4j.type.constructed.BACnetArray;
import com.serotonin.bacnet4j.type.constructed.DateTime;
import com.serotonin.bacnet4j.type.constructed.Destination;
import com.serotonin.bacnet4j.type.constructed.EventTransitionBits;
import com.serotonin.bacnet4j.type.constructed.PropertyStates;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.Recipient;
import com.serotonin.bacnet4j.type.constructed.RecipientProcess;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.constructed.StatusFlags;
import com.serotonin.bacnet4j.type.constructed.TimeStamp;
import com.serotonin.bacnet4j.type.enumerated.BinaryPV;
import com.serotonin.bacnet4j.type.enumerated.EventState;
import com.serotonin.bacnet4j.type.enumerated.EventType;
import com.serotonin.bacnet4j.type.enumerated.NotifyType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.enumerated.Reliability;
import com.serotonin.bacnet4j.type.notificationParameters.ChangeOfReliabilityNotif;
import com.serotonin.bacnet4j.type.notificationParameters.ChangeOfStateNotif;
import com.serotonin.bacnet4j.type.notificationParameters.NotificationParameters;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

public class IntrinsicAlarmTest extends AbstractTest {
    static final Logger LOG = LoggerFactory.getLogger(IntrinsicAlarmTest.class);

    private BinaryValueObject bv;
    private MultistateValueObject mv;
    private NotificationClassObject nc;

    @Override
    public void afterInit() throws Exception {
        bv = new BinaryValueObject(d1, 0, "bvName1", BinaryPV.inactive, true);
        bv.writePropertyInternal(PropertyIdentifier.outOfService, Boolean.FALSE);

        mv = new MultistateValueObject(d1, 0, "mvName1", 7,
                new BACnetArray<>(
                        new CharacterString( //
                                "normal1"),
                        new CharacterString("normal2"), new CharacterString("normal3"), //
                        new CharacterString("alarm1"), new CharacterString("alarm2"), //
                        new CharacterString("fault1"), new CharacterString("fault2")),
                1, true);
        mv.writePropertyInternal(PropertyIdentifier.outOfService, Boolean.FALSE);

        nc = new NotificationClassObject(d1, 7, "nc7", 100, 5, 200, new EventTransitionBits(true, true, true));
    }

    @Test
    public void initialConditions() throws Exception {
        bv.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.active);
        bv.supportIntrinsicReporting(2, 7, BinaryPV.active, new EventTransitionBits(true, true, true), NotifyType.event,
                4);

        assertEquals(EventState.normal, bv.readProperty(PropertyIdentifier.eventState));
        assertEquals(new EventTransitionBits(true, true, true), bv.readProperty(PropertyIdentifier.ackedTransitions));
        assertEquals(new BACnetArray<>(TimeStamp.UNSPECIFIED_DATETIME, TimeStamp.UNSPECIFIED_DATETIME,
                TimeStamp.UNSPECIFIED_DATETIME), bv.readProperty(PropertyIdentifier.eventTimeStamps));
        assertEquals(new BACnetArray<>(CharacterString.EMPTY, CharacterString.EMPTY, CharacterString.EMPTY),
                bv.readProperty(PropertyIdentifier.eventMessageTexts));

        // After the time delay, the event state should become off-normal, because the present value is the alarm state.
        clock.plus(2100, TimeUnit.MILLISECONDS, 2100, TimeUnit.MILLISECONDS, 0, 40);
        assertEquals(EventState.offnormal, bv.readProperty(PropertyIdentifier.eventState)); // Now is off-normal.
    }

    @Test
    public void changeOfState() throws Exception {
        bv.supportIntrinsicReporting(2, 7, BinaryPV.active, new EventTransitionBits(true, true, true), NotifyType.event,
                4);

        // Does not trigger any intrinsic reporting behaviour
        bv.writeProperty(null, PropertyIdentifier.objectName, new CharacterString("some new name"));

        // Write the current present value.
        bv.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);

        // Set the alarm value and then set it back to normal before the time delay.
        bv.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.active);
        clock.plus(1000, TimeUnit.MILLISECONDS, 1000, TimeUnit.MILLISECONDS, 0, 40);
        assertEquals(EventState.normal, bv.readProperty(PropertyIdentifier.eventState)); // Still normal at this point.
        bv.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
        clock.plus(1100, TimeUnit.MILLISECONDS, 1100, TimeUnit.MILLISECONDS, 0, 40);
        assertEquals(EventState.normal, bv.readProperty(PropertyIdentifier.eventState)); // Still normal at this point.

        // Do a real state change. Write the alarm value. After 2 seconds the alarm will be raised.
        bv.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.active);
        clock.plus(1000, TimeUnit.MILLISECONDS, 1000, TimeUnit.MILLISECONDS, 0, 40);
        assertEquals(EventState.normal, bv.readProperty(PropertyIdentifier.eventState)); // Still normal at this point.
        clock.plus(1100, TimeUnit.MILLISECONDS, 1100, TimeUnit.MILLISECONDS, 0, 40);
        assertEquals(EventState.offnormal, bv.readProperty(PropertyIdentifier.eventState));
        assertEquals(new StatusFlags(true, false, false, false), bv.readProperty(PropertyIdentifier.statusFlags));

        // Write the normal value and then set it back to off-normal before the time delay.
        bv.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
        clock.plus(3000, TimeUnit.MILLISECONDS, 3000, TimeUnit.MILLISECONDS, 0, 40);
        assertEquals(EventState.offnormal, bv.readProperty(PropertyIdentifier.eventState)); // Still off-normal.
        bv.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.active);
        clock.plus(1100, TimeUnit.MILLISECONDS, 1100, TimeUnit.MILLISECONDS, 0, 40);
        assertEquals(EventState.offnormal, bv.readProperty(PropertyIdentifier.eventState)); // Still off-normal.

        // Do a real state change. Write the normal value. After 4 seconds state will be normal again.
        bv.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
        clock.plus(3000, TimeUnit.MILLISECONDS, 3000, TimeUnit.MILLISECONDS, 0, 40);
        assertEquals(EventState.offnormal, bv.readProperty(PropertyIdentifier.eventState)); // Still off-normal.
        clock.plus(1100, TimeUnit.MILLISECONDS, 1100, TimeUnit.MILLISECONDS, 0, 40);
        assertEquals(EventState.normal, bv.readProperty(PropertyIdentifier.eventState));

        // Set the alarm value and then set a fault state before the time delay.
        bv.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.active);
        clock.plus(1000, TimeUnit.MILLISECONDS, 1000, TimeUnit.MILLISECONDS, 0, 40);
        assertEquals(EventState.normal, bv.readProperty(PropertyIdentifier.eventState)); // Still normal at this point.
        bv.writePropertyInternal(PropertyIdentifier.reliability, Reliability.noOutput);
        assertEquals(EventState.fault, bv.readProperty(PropertyIdentifier.eventState)); // Still normal at this point.
        assertEquals(new StatusFlags(true, true, false, false), bv.readProperty(PropertyIdentifier.statusFlags));
        clock.plus(1100, TimeUnit.MILLISECONDS, 1100, TimeUnit.MILLISECONDS, 0, 40);
        assertEquals(EventState.fault, bv.readProperty(PropertyIdentifier.eventState)); // Still normal at this point.
        assertEquals(new StatusFlags(true, true, false, false), bv.readProperty(PropertyIdentifier.statusFlags));

        // Remove the fault condition. After, the event state should immediately be off-normal.
        bv.writePropertyInternal(PropertyIdentifier.reliability, Reliability.noFaultDetected);
        assertEquals(EventState.normal, bv.readProperty(PropertyIdentifier.eventState));
    }

    // This test models the example at 13.2.2.1.5, figure 13-4
    @Test
    public void offnormalInhibit() throws Exception {
        bv.supportIntrinsicReporting(2, 7, BinaryPV.active, new EventTransitionBits(true, true, true), NotifyType.event,
                4);

        // Write the offnormal value and wait 2 seconds for the state to change.
        bv.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.active);
        clock.plus(2100, TimeUnit.MILLISECONDS, 2100, TimeUnit.MILLISECONDS, 0, 40);
        assertEquals(EventState.offnormal, bv.readProperty(PropertyIdentifier.eventState));
        assertEquals(new StatusFlags(true, false, false, false), bv.readProperty(PropertyIdentifier.statusFlags));

        // Inhibit
        bv.writePropertyInternal(PropertyIdentifier.eventAlgorithmInhibit, Boolean.TRUE);
        assertEquals(EventState.normal, bv.readProperty(PropertyIdentifier.eventState));

        // Write the normal value and wait 2 seconds: there should be no change.
        bv.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
        clock.plus(2100, TimeUnit.MILLISECONDS, 2100, TimeUnit.MILLISECONDS, 0, 40);
        assertEquals(EventState.normal, bv.readProperty(PropertyIdentifier.eventState));

        // Write the offnormal value and wait 2 seconds: there should be no change.
        bv.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.active);
        clock.plus(2100, TimeUnit.MILLISECONDS, 2100, TimeUnit.MILLISECONDS, 0, 40);
        assertEquals(EventState.normal, bv.readProperty(PropertyIdentifier.eventState));

        // Remove inhibition. After two seconds the state should become offnormal.
        bv.writePropertyInternal(PropertyIdentifier.eventAlgorithmInhibit, Boolean.FALSE);
        assertEquals(EventState.normal, bv.readProperty(PropertyIdentifier.eventState));
        clock.plus(1000, TimeUnit.MILLISECONDS, 1000, TimeUnit.MILLISECONDS, 0, 40);
        assertEquals(EventState.normal, bv.readProperty(PropertyIdentifier.eventState));
        clock.plus(1100, TimeUnit.MILLISECONDS, 1100, TimeUnit.MILLISECONDS, 0, 40);
        assertEquals(EventState.offnormal, bv.readProperty(PropertyIdentifier.eventState));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void notification() throws Exception {
        // Add rd2 as a recipient of event notifications from bv
        final SequenceOf<Destination> recipients = nc.get(PropertyIdentifier.recipientList);
        recipients.add(new Destination(new Recipient(rd2.getAddress()), new UnsignedInteger(10), Boolean.TRUE,
                new EventTransitionBits(true, true, true)));

        // Create an event listener on d2 to catch the event notifications.
        final EventNotifListener listener = new EventNotifListener();
        d2.getEventHandler().addListener(listener);

        bv.supportIntrinsicReporting(2, 7, BinaryPV.active, new EventTransitionBits(true, true, true), NotifyType.event,
                4);

        // Ensure that initialization did not cause notifications to be sent.
        assertEquals(0, listener.notifs.size());

        // Write the off-normal value and wait 2 seconds for the state to change.
        bv.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.active);
        clock.plus(2100, TimeUnit.MILLISECONDS, 2100, TimeUnit.MILLISECONDS, 0, 40);
        LOG.info("Finished waiting for state change");

        // Validate states
        assertEquals(EventState.offnormal, bv.readProperty(PropertyIdentifier.eventState));
        assertEquals(new StatusFlags(true, false, false, false), bv.readProperty(PropertyIdentifier.statusFlags));
        // It's uncertain what the timestamp will be, so just assert that it is no unspecified.
        assertFalse(((BACnetArray<TimeStamp>) bv.readProperty(PropertyIdentifier.eventTimeStamps))
                .getBase1(EventState.offnormal.getTransitionIndex()).equals(TimeStamp.UNSPECIFIED_DATETIME));
        assertEquals(new EventTransitionBits(false, true, true), bv.readProperty(PropertyIdentifier.ackedTransitions));
        clock.plus(100, TimeUnit.MILLISECONDS, 100, TimeUnit.MILLISECONDS, 0, 40);

        // Ensure that a proper looking event notification was received.
        assertEquals(1, listener.notifs.size());
        final Map<String, Object> notif = listener.notifs.get(0);
        assertEquals(new UnsignedInteger(10), notif.get("processIdentifier"));
        assertEquals(rd1.getObjectIdentifier(), notif.get("initiatingDevice"));
        assertEquals(bv.getId(), notif.get("eventObjectIdentifier"));
        assertEquals(((BACnetArray<TimeStamp>) bv.readProperty(PropertyIdentifier.eventTimeStamps))
                .getBase1(EventState.offnormal.getTransitionIndex()), notif.get("timeStamp"));
        assertEquals(new UnsignedInteger(7), notif.get("notificationClass"));
        assertEquals(new UnsignedInteger(100), notif.get("priority"));
        assertEquals(EventType.changeOfState, notif.get("eventType"));
        assertEquals(null, notif.get("messageText"));
        assertEquals(NotifyType.event, notif.get("notifyType"));
        assertEquals(Boolean.TRUE, notif.get("ackRequired"));
        assertEquals(EventState.normal, notif.get("fromState"));
        assertEquals(EventState.offnormal, notif.get("toState"));
        assertEquals(new NotificationParameters(new ChangeOfStateNotif(new PropertyStates(BinaryPV.active),
                new StatusFlags(true, false, false, false))), notif.get("eventValues"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void multistateValueTest() throws Exception {
        final SequenceOf<Destination> recipients = nc.get(PropertyIdentifier.recipientList);
        recipients.add(new Destination(new Recipient(rd2.getAddress()), new UnsignedInteger(10), Boolean.TRUE,
                new EventTransitionBits(true, true, true)));

        // Create an event listener on d2 to catch the event notifications.
        final EventNotifListener listener = new EventNotifListener();
        d2.getEventHandler().addListener(listener);

        // 1, 2, and 3 are normal values. 4 and 5 are alarms. 6 and 7 are faults.
        mv.supportIntrinsicReporting(1, 7, //
                new BACnetArray<>(new UnsignedInteger(4), new UnsignedInteger(5)), //
                new BACnetArray<>(new UnsignedInteger(6), new UnsignedInteger(7)), //
                new EventTransitionBits(true, true, true), NotifyType.event, 2);
        // Ensure that initializing the intrinsic reporting didn't fire any notifications.
        assertEquals(0, listener.notifs.size());

        // Write a different normal value.
        mv.writePropertyInternal(PropertyIdentifier.presentValue, new UnsignedInteger(2));
        assertEquals(EventState.normal, mv.readProperty(PropertyIdentifier.eventState)); // Still normal at this point.
        // Ensure that no notifications are sent.
        clock.plus(1100, TimeUnit.MILLISECONDS, 1100, TimeUnit.MILLISECONDS, 0, 40);
        assertEquals(EventState.normal, mv.readProperty(PropertyIdentifier.eventState)); // Still normal at this point.
        assertEquals(0, listener.notifs.size());

        // Set an alarm value and then set back to normal before the time delay.
        mv.writePropertyInternal(PropertyIdentifier.presentValue, new UnsignedInteger(4));
        clock.plus(500, TimeUnit.MILLISECONDS, 500, TimeUnit.MILLISECONDS, 0, 40);
        assertEquals(EventState.normal, mv.readProperty(PropertyIdentifier.eventState)); // Still normal at this point.
        mv.writePropertyInternal(PropertyIdentifier.presentValue, new UnsignedInteger(3));
        clock.plus(600, TimeUnit.MILLISECONDS, 600, TimeUnit.MILLISECONDS, 0, 40);
        assertEquals(EventState.normal, mv.readProperty(PropertyIdentifier.eventState)); // Still normal at this point.

        // Do a real state change. Write an alarm value. After 1 seconds the alarm will be raised.
        mv.writePropertyInternal(PropertyIdentifier.presentValue, new UnsignedInteger(4));
        clock.plus(500, TimeUnit.MILLISECONDS, 500, TimeUnit.MILLISECONDS, 0, 40);
        assertEquals(EventState.normal, mv.readProperty(PropertyIdentifier.eventState)); // Still normal at this point.
        clock.plus(600, TimeUnit.MILLISECONDS, 600, TimeUnit.MILLISECONDS, 0, 40);
        assertEquals(EventState.offnormal, mv.readProperty(PropertyIdentifier.eventState));
        assertEquals(new StatusFlags(true, false, false, false), mv.readProperty(PropertyIdentifier.statusFlags));

        // Ensure that a proper looking event notification was received.
        assertEquals(1, listener.notifs.size());
        Map<String, Object> notif = listener.notifs.remove(0);
        assertEquals(new UnsignedInteger(10), notif.get("processIdentifier"));
        assertEquals(rd1.getObjectIdentifier(), notif.get("initiatingDevice"));
        assertEquals(mv.getId(), notif.get("eventObjectIdentifier"));
        assertEquals(((BACnetArray<TimeStamp>) mv.readProperty(PropertyIdentifier.eventTimeStamps))
                .getBase1(EventState.offnormal.getTransitionIndex()), notif.get("timeStamp"));
        assertEquals(new UnsignedInteger(7), notif.get("notificationClass"));
        assertEquals(new UnsignedInteger(100), notif.get("priority"));
        assertEquals(EventType.changeOfState, notif.get("eventType"));
        assertEquals(null, notif.get("messageText"));
        assertEquals(NotifyType.event, notif.get("notifyType"));
        assertEquals(Boolean.TRUE, notif.get("ackRequired"));
        assertEquals(EventState.normal, notif.get("fromState"));
        assertEquals(EventState.offnormal, notif.get("toState"));
        assertEquals(new NotificationParameters(new ChangeOfStateNotif(new PropertyStates(new UnsignedInteger(4)),
                new StatusFlags(true, false, false, false))), notif.get("eventValues"));

        // Change to a different alarm value.
        mv.writePropertyInternal(PropertyIdentifier.presentValue, new UnsignedInteger(5));
        clock.plus(1100, TimeUnit.MILLISECONDS, 1100, TimeUnit.MILLISECONDS, 0, 40);
        assertEquals(EventState.offnormal, mv.readProperty(PropertyIdentifier.eventState)); // Still off-normal at this point.
        assertEquals(1, listener.notifs.size());
        notif = listener.notifs.remove(0);
        assertEquals(new UnsignedInteger(10), notif.get("processIdentifier"));
        assertEquals(rd1.getObjectIdentifier(), notif.get("initiatingDevice"));
        assertEquals(mv.getId(), notif.get("eventObjectIdentifier"));
        assertEquals(((BACnetArray<TimeStamp>) mv.readProperty(PropertyIdentifier.eventTimeStamps))
                .getBase1(EventState.offnormal.getTransitionIndex()), notif.get("timeStamp"));
        assertEquals(new UnsignedInteger(7), notif.get("notificationClass"));
        assertEquals(new UnsignedInteger(100), notif.get("priority"));
        assertEquals(EventType.changeOfState, notif.get("eventType"));
        assertEquals(null, notif.get("messageText"));
        assertEquals(NotifyType.event, notif.get("notifyType"));
        assertEquals(Boolean.TRUE, notif.get("ackRequired"));
        assertEquals(EventState.offnormal, notif.get("fromState"));
        assertEquals(EventState.offnormal, notif.get("toState"));
        assertEquals(new NotificationParameters(new ChangeOfStateNotif(new PropertyStates(new UnsignedInteger(5)),
                new StatusFlags(true, false, false, false))), notif.get("eventValues"));

        // Write a normal value and then set it back to the previous off-normal value before the time delay.
        mv.writePropertyInternal(PropertyIdentifier.presentValue, new UnsignedInteger(1));
        clock.plus(1000, TimeUnit.MILLISECONDS, 1000, TimeUnit.MILLISECONDS, 0, 40);
        assertEquals(EventState.offnormal, mv.readProperty(PropertyIdentifier.eventState)); // Still off-normal.
        mv.writePropertyInternal(PropertyIdentifier.presentValue, new UnsignedInteger(5));
        clock.plus(1100, TimeUnit.MILLISECONDS, 1100, TimeUnit.MILLISECONDS, 0, 40);
        assertEquals(EventState.offnormal, mv.readProperty(PropertyIdentifier.eventState)); // Still off-normal.
        assertEquals(0, listener.notifs.size());

        // Do a real state change. Write the normal value. After 2 seconds state will be normal again.
        mv.writePropertyInternal(PropertyIdentifier.presentValue, new UnsignedInteger(2));
        clock.plus(1000, TimeUnit.MILLISECONDS, 1000, TimeUnit.MILLISECONDS, 0, 40);
        assertEquals(EventState.offnormal, mv.readProperty(PropertyIdentifier.eventState)); // Still off-normal.
        clock.plus(1100, TimeUnit.MILLISECONDS, 1100, TimeUnit.MILLISECONDS, 0, 40);
        assertEquals(EventState.normal, mv.readProperty(PropertyIdentifier.eventState));

        // Ensure that a proper looking event notification was received.
        assertEquals(1, listener.notifs.size());
        notif = listener.notifs.remove(0);
        assertEquals(new UnsignedInteger(10), notif.get("processIdentifier"));
        assertEquals(rd1.getObjectIdentifier(), notif.get("initiatingDevice"));
        assertEquals(mv.getId(), notif.get("eventObjectIdentifier"));
        assertEquals(((BACnetArray<TimeStamp>) mv.readProperty(PropertyIdentifier.eventTimeStamps))
                .getBase1(EventState.normal.getTransitionIndex()), notif.get("timeStamp"));
        assertEquals(new UnsignedInteger(7), notif.get("notificationClass"));
        assertEquals(new UnsignedInteger(200), notif.get("priority"));
        assertEquals(EventType.changeOfState, notif.get("eventType"));
        assertEquals(null, notif.get("messageText"));
        assertEquals(NotifyType.event, notif.get("notifyType"));
        assertEquals(Boolean.TRUE, notif.get("ackRequired"));
        assertEquals(EventState.offnormal, notif.get("fromState"));
        assertEquals(EventState.normal, notif.get("toState"));
        assertEquals(new NotificationParameters(new ChangeOfStateNotif(new PropertyStates(new UnsignedInteger(2)),
                new StatusFlags(false, false, false, false))), notif.get("eventValues"));

        // Set a fault state.
        mv.writePropertyInternal(PropertyIdentifier.presentValue, new UnsignedInteger(7));
        assertEquals(EventState.fault, mv.readProperty(PropertyIdentifier.eventState)); // Immediately fault.
        assertEquals(new StatusFlags(true, true, false, false), mv.readProperty(PropertyIdentifier.statusFlags));
        clock.plus(100, TimeUnit.MILLISECONDS, 100, TimeUnit.MILLISECONDS, 0, 40);

        // Ensure that a proper looking event notification was received.
        assertEquals(1, listener.notifs.size());
        notif = listener.notifs.remove(0);
        assertEquals(new UnsignedInteger(10), notif.get("processIdentifier"));
        assertEquals(rd1.getObjectIdentifier(), notif.get("initiatingDevice"));
        assertEquals(mv.getId(), notif.get("eventObjectIdentifier"));
        assertEquals(((BACnetArray<TimeStamp>) mv.readProperty(PropertyIdentifier.eventTimeStamps))
                .getBase1(EventState.fault.getTransitionIndex()), notif.get("timeStamp"));
        assertEquals(new UnsignedInteger(7), notif.get("notificationClass"));
        assertEquals(new UnsignedInteger(5), notif.get("priority"));
        assertEquals(EventType.changeOfReliability, notif.get("eventType"));
        assertEquals(null, notif.get("messageText"));
        assertEquals(NotifyType.event, notif.get("notifyType"));
        assertEquals(Boolean.TRUE, notif.get("ackRequired"));
        assertEquals(EventState.normal, notif.get("fromState"));
        assertEquals(EventState.fault, notif.get("toState"));
        assertEquals(
                new NotificationParameters(new ChangeOfReliabilityNotif(Reliability.multiStateFault,
                        new StatusFlags(true, true, false, false),
                        new SequenceOf<>(new PropertyValue(PropertyIdentifier.presentValue, new UnsignedInteger(7))))),
                notif.get("eventValues"));

        // Change to a different fault condition.
        mv.writePropertyInternal(PropertyIdentifier.presentValue, new UnsignedInteger(6));
        assertEquals(EventState.fault, mv.readProperty(PropertyIdentifier.eventState)); // Immediately fault.
        assertEquals(new StatusFlags(true, true, false, false), mv.readProperty(PropertyIdentifier.statusFlags));
        clock.plus(100, TimeUnit.MILLISECONDS, 100, TimeUnit.MILLISECONDS, 0, 40);

        // Ensure that a proper looking event notification was received.
        assertEquals(1, listener.notifs.size());
        notif = listener.notifs.remove(0);
        assertEquals(new UnsignedInteger(10), notif.get("processIdentifier"));
        assertEquals(rd1.getObjectIdentifier(), notif.get("initiatingDevice"));
        assertEquals(mv.getId(), notif.get("eventObjectIdentifier"));
        assertEquals(((BACnetArray<TimeStamp>) mv.readProperty(PropertyIdentifier.eventTimeStamps))
                .getBase1(EventState.fault.getTransitionIndex()), notif.get("timeStamp"));
        assertEquals(new UnsignedInteger(7), notif.get("notificationClass"));
        assertEquals(new UnsignedInteger(5), notif.get("priority"));
        assertEquals(EventType.changeOfReliability, notif.get("eventType"));
        assertEquals(null, notif.get("messageText"));
        assertEquals(NotifyType.event, notif.get("notifyType"));
        assertEquals(Boolean.TRUE, notif.get("ackRequired"));
        assertEquals(EventState.fault, notif.get("fromState"));
        assertEquals(EventState.fault, notif.get("toState"));
        assertEquals(
                new NotificationParameters(new ChangeOfReliabilityNotif(Reliability.multiStateFault,
                        new StatusFlags(true, true, false, false),
                        new SequenceOf<>(new PropertyValue(PropertyIdentifier.presentValue, new UnsignedInteger(6))))),
                notif.get("eventValues"));

        // Change to an alarm condition. An immediate notification should be sent for the transition to normal.
        mv.writePropertyInternal(PropertyIdentifier.presentValue, new UnsignedInteger(4));
        assertEquals(EventState.normal, mv.readProperty(PropertyIdentifier.eventState));
        assertEquals(new StatusFlags(false, false, false, false), mv.readProperty(PropertyIdentifier.statusFlags));
        clock.plus(100, TimeUnit.MILLISECONDS, 100, TimeUnit.MILLISECONDS, 0, 40);

        // Ensure that a proper looking event notification was received.
        assertEquals(1, listener.notifs.size());
        notif = listener.notifs.remove(0);
        assertEquals(new UnsignedInteger(10), notif.get("processIdentifier"));
        assertEquals(rd1.getObjectIdentifier(), notif.get("initiatingDevice"));
        assertEquals(mv.getId(), notif.get("eventObjectIdentifier"));
        assertEquals(((BACnetArray<TimeStamp>) mv.readProperty(PropertyIdentifier.eventTimeStamps))
                .getBase1(EventState.normal.getTransitionIndex()), notif.get("timeStamp"));
        assertEquals(new UnsignedInteger(7), notif.get("notificationClass"));
        assertEquals(new UnsignedInteger(200), notif.get("priority"));
        assertEquals(EventType.changeOfReliability, notif.get("eventType"));
        assertEquals(null, notif.get("messageText"));
        assertEquals(NotifyType.event, notif.get("notifyType"));
        assertEquals(Boolean.TRUE, notif.get("ackRequired"));
        assertEquals(EventState.fault, notif.get("fromState"));
        assertEquals(EventState.normal, notif.get("toState"));
        assertEquals(
                new NotificationParameters(new ChangeOfReliabilityNotif(Reliability.noFaultDetected,
                        new StatusFlags(false, false, false, false),
                        new SequenceOf<>(new PropertyValue(PropertyIdentifier.presentValue, new UnsignedInteger(4))))),
                notif.get("eventValues"));

        // After the time delay the state will change to off-normal and a notification will be sent.
        clock.plus(1100, TimeUnit.MILLISECONDS, 1100, TimeUnit.MILLISECONDS, 0, 40);
        assertEquals(EventState.offnormal, mv.readProperty(PropertyIdentifier.eventState));
        assertEquals(new StatusFlags(true, false, false, false), mv.readProperty(PropertyIdentifier.statusFlags));

        // Ensure that a proper looking event notification was received.
        assertEquals(1, listener.notifs.size());
        notif = listener.notifs.remove(0);
        assertEquals(new UnsignedInteger(10), notif.get("processIdentifier"));
        assertEquals(rd1.getObjectIdentifier(), notif.get("initiatingDevice"));
        assertEquals(mv.getId(), notif.get("eventObjectIdentifier"));
        assertEquals(((BACnetArray<TimeStamp>) mv.readProperty(PropertyIdentifier.eventTimeStamps))
                .getBase1(EventState.offnormal.getTransitionIndex()), notif.get("timeStamp"));
        assertEquals(new UnsignedInteger(7), notif.get("notificationClass"));
        assertEquals(new UnsignedInteger(100), notif.get("priority"));
        assertEquals(EventType.changeOfState, notif.get("eventType"));
        assertEquals(null, notif.get("messageText"));
        assertEquals(NotifyType.event, notif.get("notifyType"));
        assertEquals(Boolean.TRUE, notif.get("ackRequired"));
        assertEquals(EventState.normal, notif.get("fromState"));
        assertEquals(EventState.offnormal, notif.get("toState"));
        assertEquals(new NotificationParameters(new ChangeOfStateNotif(new PropertyStates(new UnsignedInteger(4)),
                new StatusFlags(true, false, false, false))), notif.get("eventValues"));
    }

    @Test
    public void eventAcks() throws Exception {
        // Add rd2 as a recipient of event notifications from bv
        final SequenceOf<Destination> recipients = nc.get(PropertyIdentifier.recipientList);
        recipients.add(new Destination(new Recipient(rd2.getAddress()), new UnsignedInteger(10), Boolean.TRUE,
                new EventTransitionBits(true, true, false)));

        // Create an event listener on d2 to catch the event notifications.
        final EventNotifListener listener = new EventNotifListener();
        d2.getEventHandler().addListener(listener);

        bv.supportIntrinsicReporting(1, 7, BinaryPV.active, new EventTransitionBits(true, true, true), NotifyType.alarm,
                2);

        // Write the off-normal value and wait 2 seconds for the state to change.
        bv.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.active);
        clock.plus(1100, TimeUnit.MILLISECONDS, 1100, TimeUnit.MILLISECONDS, 0, 40);
        LOG.info("Finished waiting for state change");
        assertEquals(EventState.offnormal, bv.readProperty(PropertyIdentifier.eventState));

        // Ensure that a notification was received.
        assertEquals(1, listener.notifs.size());
        final Map<String, Object> notif = listener.notifs.remove(0);
        //        System.out.println(notif);

        // Get an alarm summary
        GetAlarmSummaryAck alarmSummaryAck = d2.send(rd1, new GetAlarmSummaryRequest()).get();
        assertEquals(1, alarmSummaryAck.getValues().getCount());
        AlarmSummary alarmSummary = alarmSummaryAck.getValues().getBase1(1);
        assertEquals(bv.getId(), alarmSummary.getObjectIdentifier());
        assertEquals(EventState.offnormal, alarmSummary.getAlarmState());
        assertEquals(new EventTransitionBits(false, true, true), alarmSummary.getAcknowledgedTransitions());

        // Get event information
        GetEventInformationAck eventInfoAck = d2.send(rd1, new GetEventInformationRequest(null)).get();
        assertEquals(1, eventInfoAck.getListOfEventSummaries().getCount());
        assertEquals(false, eventInfoAck.getMoreEvents().booleanValue());
        EventSummary eventSummary = eventInfoAck.getListOfEventSummaries().getBase1(1);
        assertEquals(bv.getId(), eventSummary.getObjectIdentifier());
        assertEquals(EventState.offnormal, eventSummary.getEventState());
        assertEquals(new EventTransitionBits(false, true, true), eventSummary.getAcknowledgedTransitions());
        assertEquals(notif.get("timeStamp"), eventSummary.getEventTimeStamps().getBase1(1));
        assertEquals(TimeStamp.UNSPECIFIED_DATETIME, eventSummary.getEventTimeStamps().getBase1(2));
        assertEquals(TimeStamp.UNSPECIFIED_DATETIME, eventSummary.getEventTimeStamps().getBase1(3));
        assertEquals(NotifyType.alarm, eventSummary.getNotifyType());
        assertEquals(new EventTransitionBits(true, true, true), eventSummary.getEventEnable());
        assertEquals(new UnsignedInteger(100), eventSummary.getEventPriorities().getBase1(1));
        assertEquals(new UnsignedInteger(5), eventSummary.getEventPriorities().getBase1(2));
        assertEquals(new UnsignedInteger(200), eventSummary.getEventPriorities().getBase1(3));

        final TimeStamp now = new TimeStamp(new DateTime(d1));
        final AcknowledgeAlarmRequest req = new AcknowledgeAlarmRequest( //
                (UnsignedInteger) notif.get("processIdentifier"), //
                (ObjectIdentifier) notif.get("eventObjectIdentifier"), //
                (EventState) notif.get("toState"), //
                (TimeStamp) notif.get("timeStamp"), //
                new CharacterString("spa"), //
                now);
        final RemoteDevice d = d2
                .getCachedRemoteDevice(((ObjectIdentifier) notif.get("initiatingDevice")).getInstanceNumber());
        d2.send(d, req).get();

        // Will receive notification of the acknowledgement
        clock.plus(200, TimeUnit.MILLISECONDS, 200, TimeUnit.MILLISECONDS, 0, 40);
        assertEquals(1, listener.notifs.size());
        final Map<String, Object> ack = listener.notifs.remove(0);
        assertEquals(new UnsignedInteger(10), ack.get("processIdentifier"));
        assertEquals(rd1.getObjectIdentifier(), ack.get("initiatingDevice"));
        assertEquals(bv.getId(), ack.get("eventObjectIdentifier"));
        assertEquals(now, ack.get("timeStamp"));
        assertEquals(new UnsignedInteger(7), ack.get("notificationClass"));
        assertEquals(new UnsignedInteger(100), ack.get("priority"));
        assertEquals(EventType.changeOfState, ack.get("eventType"));
        assertEquals(new CharacterString("10: spa"), ack.get("messageText"));
        assertEquals(NotifyType.ackNotification, ack.get("notifyType"));
        assertEquals(null, ack.get("ackRequired"));
        assertEquals(null, ack.get("fromState"));
        assertEquals(EventState.offnormal, ack.get("toState"));
        assertEquals(null, ack.get("eventValues"));

        // Get an alarm summary
        alarmSummaryAck = d2.send(rd1, new GetAlarmSummaryRequest()).get();
        assertEquals(1, alarmSummaryAck.getValues().getCount());
        alarmSummary = alarmSummaryAck.getValues().getBase1(1);
        assertEquals(bv.getId(), alarmSummary.getObjectIdentifier());
        assertEquals(EventState.offnormal, alarmSummary.getAlarmState());
        assertEquals(new EventTransitionBits(true, true, true), alarmSummary.getAcknowledgedTransitions());

        // Get event information
        eventInfoAck = d2.send(rd1, new GetEventInformationRequest(null)).get();
        assertEquals(1, eventInfoAck.getListOfEventSummaries().getCount());
        assertEquals(false, eventInfoAck.getMoreEvents().booleanValue());
        eventSummary = eventInfoAck.getListOfEventSummaries().getBase1(1);
        assertEquals(bv.getId(), eventSummary.getObjectIdentifier());
        assertEquals(EventState.offnormal, eventSummary.getEventState());
        assertEquals(new EventTransitionBits(true, true, true), eventSummary.getAcknowledgedTransitions());
        assertEquals(notif.get("timeStamp"), eventSummary.getEventTimeStamps().getBase1(1));
        assertEquals(TimeStamp.UNSPECIFIED_DATETIME, eventSummary.getEventTimeStamps().getBase1(2));
        assertEquals(TimeStamp.UNSPECIFIED_DATETIME, eventSummary.getEventTimeStamps().getBase1(3));
        assertEquals(NotifyType.alarm, eventSummary.getNotifyType());
        assertEquals(new EventTransitionBits(true, true, true), eventSummary.getEventEnable());
        assertEquals(new UnsignedInteger(100), eventSummary.getEventPriorities().getBase1(1));
        assertEquals(new UnsignedInteger(5), eventSummary.getEventPriorities().getBase1(2));
        assertEquals(new UnsignedInteger(200), eventSummary.getEventPriorities().getBase1(3));

        // Write the normal value and wait 2 seconds for the state to change.
        bv.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
        clock.plus(2100, TimeUnit.MILLISECONDS, 2100, TimeUnit.MILLISECONDS, 0, 40);
        LOG.info("Finished waiting for state change");
        assertEquals(EventState.normal, bv.readProperty(PropertyIdentifier.eventState));

        // Ensure that a notification was not received, since the recipient asked not to be notified
        assertEquals(0, listener.notifs.size());

        // Get an alarm summary
        alarmSummaryAck = d2.send(rd1, new GetAlarmSummaryRequest()).get();
        assertEquals(0, alarmSummaryAck.getValues().getCount());

        // Get event information
        eventInfoAck = d2.send(rd1, new GetEventInformationRequest(null)).get();
        assertEquals(1, eventInfoAck.getListOfEventSummaries().getCount());
        assertEquals(false, eventInfoAck.getMoreEvents().booleanValue());
        eventSummary = eventInfoAck.getListOfEventSummaries().getBase1(1);
        assertEquals(bv.getId(), eventSummary.getObjectIdentifier());
        assertEquals(EventState.normal, eventSummary.getEventState());
        assertEquals(new EventTransitionBits(true, true, false), eventSummary.getAcknowledgedTransitions());
        assertEquals(notif.get("timeStamp"), eventSummary.getEventTimeStamps().getBase1(1));
        assertEquals(TimeStamp.UNSPECIFIED_DATETIME, eventSummary.getEventTimeStamps().getBase1(2));
        assertNotEquals(TimeStamp.UNSPECIFIED_DATETIME, eventSummary.getEventTimeStamps().getBase1(3));
        assertEquals(NotifyType.alarm, eventSummary.getNotifyType());
        assertEquals(new EventTransitionBits(true, true, true), eventSummary.getEventEnable());
        assertEquals(new UnsignedInteger(100), eventSummary.getEventPriorities().getBase1(1));
        assertEquals(new UnsignedInteger(5), eventSummary.getEventPriorities().getBase1(2));
        assertEquals(new UnsignedInteger(200), eventSummary.getEventPriorities().getBase1(3));
    }

    @Test
    public void internalAcks() throws Exception {
        // Add rd2 as a recipient of event notifications from bv
        final SequenceOf<Destination> recipients = nc.get(PropertyIdentifier.recipientList);
        recipients.add(new Destination(new Recipient(rd2.getAddress()), new UnsignedInteger(10), Boolean.TRUE,
                new EventTransitionBits(true, true, false)));

        // Create an event listener on d2 to catch the event notifications.
        final EventNotifListener listener = new EventNotifListener();
        d2.getEventHandler().addListener(listener);

        bv.supportIntrinsicReporting(1, 7, BinaryPV.active, new EventTransitionBits(true, true, true), NotifyType.alarm,
                2);

        // Write the off-normal value and wait 2 seconds for the state to change.
        bv.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.active);
        clock.plus(1100, TimeUnit.MILLISECONDS, 1100, TimeUnit.MILLISECONDS, 0, 40);
        LOG.info("Finished waiting for state change");
        assertEquals(EventState.offnormal, bv.readProperty(PropertyIdentifier.eventState));

        // Ensure that a notification was received.
        assertEquals(1, listener.notifs.size());
        final Map<String, Object> notif = listener.notifs.remove(0);
        //        System.out.println(notif);

        final TimeStamp now = new TimeStamp(new DateTime(d1));
        bv.acknowledgeAlarm((UnsignedInteger) notif.get("processIdentifier"), //
                (EventState) notif.get("toState"), //
                (TimeStamp) notif.get("timeStamp"), //
                new CharacterString("spa"), //
                now);

        // Will receive notification of the acknowledgement
        clock.plus(200, TimeUnit.MILLISECONDS, 200, TimeUnit.MILLISECONDS, 0, 40);
        assertEquals(1, listener.notifs.size());
        final Map<String, Object> ack = listener.notifs.remove(0);
        assertEquals(new UnsignedInteger(10), ack.get("processIdentifier"));
        assertEquals(rd1.getObjectIdentifier(), ack.get("initiatingDevice"));
        assertEquals(bv.getId(), ack.get("eventObjectIdentifier"));
        assertEquals(now, ack.get("timeStamp"));
        assertEquals(new UnsignedInteger(7), ack.get("notificationClass"));
        assertEquals(new UnsignedInteger(100), ack.get("priority"));
        assertEquals(EventType.changeOfState, ack.get("eventType"));
        assertEquals(new CharacterString("10: spa"), ack.get("messageText"));
        assertEquals(NotifyType.ackNotification, ack.get("notifyType"));
        assertEquals(null, ack.get("ackRequired"));
        assertEquals(null, ack.get("fromState"));
        assertEquals(EventState.offnormal, ack.get("toState"));
        assertEquals(null, ack.get("eventValues"));
    }

    @Test
    public void enrollment() throws Exception {
        final SequenceOf<Destination> recipients = nc.get(PropertyIdentifier.recipientList);
        recipients.add(new Destination(new Recipient(rd2.getAddress()), new UnsignedInteger(10), Boolean.TRUE,
                new EventTransitionBits(true, true, false)));

        bv.supportIntrinsicReporting(1, 7, BinaryPV.active, new EventTransitionBits(true, true, true), NotifyType.alarm,
                2);

        GetEnrollmentSummaryRequest req = new GetEnrollmentSummaryRequest(AcknowledgmentFilter.all, null, null, null,
                null, null);
        GetEnrollmentSummaryAck ack = d2.send(rd1, req).get();
        assertEquals(1, ack.getValues().getCount());
        final EnrollmentSummary e = ack.getValues().getBase1(1);
        assertEquals(bv.getId(), e.getObjectIdentifier());
        assertEquals(EventType.changeOfState, e.getEventType());
        assertEquals(EventState.normal, e.getEventState());
        assertEquals(200, e.getPriority().intValue());
        assertEquals(7, e.getNotificationClass().intValue());

        req = new GetEnrollmentSummaryRequest(AcknowledgmentFilter.acked, null, null, null, null, null);
        ack = d2.send(rd1, req).get();
        assertEquals(1, ack.getValues().getCount());

        req = new GetEnrollmentSummaryRequest(AcknowledgmentFilter.notAcked, null, null, null, null, null);
        ack = d2.send(rd1, req).get();
        assertEquals(0, ack.getValues().getCount());

        req = new GetEnrollmentSummaryRequest(AcknowledgmentFilter.all,
                new RecipientProcess(new Recipient(rd2.getAddress()), new UnsignedInteger(10)), null, null, null, null);
        ack = d2.send(rd1, req).get();
        assertEquals(1, ack.getValues().getCount());

        req = new GetEnrollmentSummaryRequest(AcknowledgmentFilter.all,
                new RecipientProcess(new Recipient(rd2.getAddress()), new UnsignedInteger(11)), null, null, null, null);
        ack = d2.send(rd1, req).get();
        assertEquals(0, ack.getValues().getCount());

        req = new GetEnrollmentSummaryRequest(AcknowledgmentFilter.all, null, EventStateFilter.offnormal, null, null,
                null);
        ack = d2.send(rd1, req).get();
        assertEquals(0, ack.getValues().getCount());

        req = new GetEnrollmentSummaryRequest(AcknowledgmentFilter.all, null, EventStateFilter.fault, null, null, null);
        ack = d2.send(rd1, req).get();
        assertEquals(0, ack.getValues().getCount());

        req = new GetEnrollmentSummaryRequest(AcknowledgmentFilter.all, null, EventStateFilter.normal, null, null,
                null);
        ack = d2.send(rd1, req).get();
        assertEquals(1, ack.getValues().getCount());

        req = new GetEnrollmentSummaryRequest(AcknowledgmentFilter.all, null, EventStateFilter.all, null, null, null);
        ack = d2.send(rd1, req).get();
        assertEquals(1, ack.getValues().getCount());

        req = new GetEnrollmentSummaryRequest(AcknowledgmentFilter.all, null, EventStateFilter.active, null, null,
                null);
        ack = d2.send(rd1, req).get();
        assertEquals(0, ack.getValues().getCount());

        req = new GetEnrollmentSummaryRequest(AcknowledgmentFilter.all, null, null, EventType.changeOfState, null,
                null);
        ack = d2.send(rd1, req).get();
        assertEquals(1, ack.getValues().getCount());

        req = new GetEnrollmentSummaryRequest(AcknowledgmentFilter.all, null, null, EventType.accessEvent, null, null);
        ack = d2.send(rd1, req).get();
        assertEquals(0, ack.getValues().getCount());

        req = new GetEnrollmentSummaryRequest(AcknowledgmentFilter.all, null, null, null,
                new PriorityFilter(new UnsignedInteger(1), new UnsignedInteger(2)), null);
        ack = d2.send(rd1, req).get();
        assertEquals(0, ack.getValues().getCount());

        req = new GetEnrollmentSummaryRequest(AcknowledgmentFilter.all, null, null, null,
                new PriorityFilter(new UnsignedInteger(1), new UnsignedInteger(250)), null);
        ack = d2.send(rd1, req).get();
        assertEquals(1, ack.getValues().getCount());

        req = new GetEnrollmentSummaryRequest(AcknowledgmentFilter.all, null, null, null,
                new PriorityFilter(new UnsignedInteger(201), new UnsignedInteger(250)), null);
        ack = d2.send(rd1, req).get();
        assertEquals(0, ack.getValues().getCount());

        req = new GetEnrollmentSummaryRequest(AcknowledgmentFilter.all, null, null, null, null, new UnsignedInteger(7));
        ack = d2.send(rd1, req).get();
        assertEquals(1, ack.getValues().getCount());

        req = new GetEnrollmentSummaryRequest(AcknowledgmentFilter.all, null, null, null, null, new UnsignedInteger(8));
        ack = d2.send(rd1, req).get();
        assertEquals(0, ack.getValues().getCount());
    }
}

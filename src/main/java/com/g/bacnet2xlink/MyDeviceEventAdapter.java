package com.g.bacnet2xlink;

import java.util.HashSet;

import com.serotonin.bacnet4j.event.DeviceEventAdapter;
import com.serotonin.bacnet4j.type.constructed.Choice;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.constructed.TimeStamp;
import com.serotonin.bacnet4j.type.enumerated.EventState;
import com.serotonin.bacnet4j.type.enumerated.EventType;
import com.serotonin.bacnet4j.type.enumerated.MessagePriority;
import com.serotonin.bacnet4j.type.enumerated.NotifyType;
import com.serotonin.bacnet4j.type.notificationParameters.NotificationParameters;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyDeviceEventAdapter extends DeviceEventAdapter {
    private static final Logger log = LoggerFactory.getLogger(MyDeviceEventAdapter.class);

    private ObjectIdentifier remoteDeviceIdentifier;
    private HashSet<ObjectIdentifier> monitorOids;

    @Override
    public void listenerException(final Throwable e) {
        log.warn("Listener Exception", e);
    }

    @Override
    public void covNotificationReceived(final UnsignedInteger subscriberProcessIdentifier,
                                        final ObjectIdentifier initiatingDeviceIdentifier, final ObjectIdentifier monitoredObjectIdentifier,
                                        final UnsignedInteger timeRemaining, final SequenceOf<PropertyValue> listOfValues) {
        if (initiatingDeviceIdentifier.equals(remoteDeviceIdentifier) && monitorOids.contains(monitoredObjectIdentifier)) {
            log.info("CovNotificationReceived: initiatingDeviceIdentifier - {}, monitoredObjectIdentifier - {}",
                    initiatingDeviceIdentifier, monitoredObjectIdentifier);
            log.info("\ttimeRemaining - {}", timeRemaining);
            log.info("\tlistOfValues - {}", listOfValues);
        }
    }

    @Override
    public void eventNotificationReceived(final UnsignedInteger processIdentifier,
                                          final ObjectIdentifier initiatingDeviceIdentifier, final ObjectIdentifier eventObjectIdentifier,
                                          final TimeStamp timeStamp, final UnsignedInteger notificationClass, final UnsignedInteger priority,
                                          final EventType eventType, final CharacterString messageText, final NotifyType notifyType,
                                          final Boolean ackRequired, final EventState fromState, final EventState toState,
                                          final NotificationParameters eventValues) {
        log.info("EventNotificationReceived：{}", messageText);
        log.info("\tinitiatingDeviceIdentifier：{}", initiatingDeviceIdentifier);
        log.info("\teventObjectIdentifier：{}", eventObjectIdentifier);
    }

    @Override
    public void textMessageReceived(final ObjectIdentifier textMessageSourceDevice, final Choice messageClass,
                                    final MessagePriority messagePriority, final CharacterString message) {
        log.info("TextMessageReceived: {}", message);
        log.info("\ttextMessageSourceDevice: {}", textMessageSourceDevice);
    }
}
package com.serotonin.bacnet4j.service.confirmed;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import com.serotonin.bacnet4j.AbstractTest;
import com.serotonin.bacnet4j.event.DeviceEventAdapter;
import com.serotonin.bacnet4j.type.constructed.Choice;
import com.serotonin.bacnet4j.type.enumerated.MessagePriority;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

public class ConfirmedTextMessageRequestTest extends AbstractTest {
    @Test
    public void happy() throws Exception {
        // Create the listener in device 2
        final AtomicReference<ObjectIdentifier> receivedObjectIdentifier = new AtomicReference<>(null);
        final AtomicReference<Choice> receivedMessageClass = new AtomicReference<>(null);
        final AtomicReference<MessagePriority> receivedMessagePriority = new AtomicReference<>(null);
        final AtomicReference<CharacterString> receivedMessage = new AtomicReference<>(null);
        d2.getEventHandler().addListener(new DeviceEventAdapter() {
            @Override
            public void textMessageReceived(final ObjectIdentifier textMessageSourceDevice, final Choice messageClass,
                    final MessagePriority messagePriority, final CharacterString message) {
                receivedObjectIdentifier.set(textMessageSourceDevice);
                receivedMessageClass.set(messageClass);
                receivedMessagePriority.set(messagePriority);
                receivedMessage.set(message);
            }
        });

        // First request constructor.
        d1.send(rd2, new ConfirmedTextMessageRequest(new ObjectIdentifier(ObjectType.accessCredential, 0),
                MessagePriority.normal, new CharacterString("The message"))).get();

        assertEquals(new ObjectIdentifier(ObjectType.accessCredential, 0), receivedObjectIdentifier.get());
        assertEquals(null, receivedMessageClass.get());
        assertEquals(MessagePriority.normal, receivedMessagePriority.get());
        assertEquals(new CharacterString("The message"), receivedMessage.get());

        // Second request constructor.
        d1.send(rd2, new ConfirmedTextMessageRequest(new ObjectIdentifier(ObjectType.accessCredential, 0),
                new UnsignedInteger(12), MessagePriority.normal, new CharacterString("The message"))).get();

        assertEquals(new ObjectIdentifier(ObjectType.accessCredential, 0), receivedObjectIdentifier.get());
        assertEquals(new UnsignedInteger(12), receivedMessageClass.get().getDatum());
        assertEquals(MessagePriority.normal, receivedMessagePriority.get());
        assertEquals(new CharacterString("The message"), receivedMessage.get());

        // Third request constructor.
        d1.send(rd2, new ConfirmedTextMessageRequest(new ObjectIdentifier(ObjectType.accessCredential, 0),
                new CharacterString("Some message class"), MessagePriority.normal, new CharacterString("The message")))
                .get();

        assertEquals(new ObjectIdentifier(ObjectType.accessCredential, 0), receivedObjectIdentifier.get());
        assertEquals(new CharacterString("Some message class"), receivedMessageClass.get().getDatum());
        assertEquals(MessagePriority.normal, receivedMessagePriority.get());
        assertEquals(new CharacterString("The message"), receivedMessage.get());
    }
}

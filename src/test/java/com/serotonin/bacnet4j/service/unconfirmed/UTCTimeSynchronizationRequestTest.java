package com.serotonin.bacnet4j.service.unconfirmed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import com.serotonin.bacnet4j.AbstractTest;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.event.DeviceEventAdapter;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.constructed.DateTime;

public class UTCTimeSynchronizationRequestTest extends AbstractTest {
    @Test
    public void timeSync() throws Exception {
        final DateTime dateTime = new DateTime(d1);

        // Create the listener in device 2
        final AtomicReference<Address> receivedAddress = new AtomicReference<>(null);
        final AtomicReference<DateTime> receivedDateTime = new AtomicReference<>(null);
        final AtomicBoolean receivedUtc = new AtomicBoolean(false);
        d2.getEventHandler().addListener(new DeviceEventAdapter() {
            @Override
            public void synchronizeTime(final Address from, final DateTime dateTime, final boolean utc) {
                receivedAddress.set(from);
                receivedDateTime.set(dateTime);
                receivedUtc.set(utc);
                synchronized (dateTime) {
                    dateTime.notify();
                }
            }
        });

        final RemoteDevice rd2 = d1.getRemoteDevice(2).get();

        synchronized (dateTime) {
            d1.send(rd2, new UTCTimeSynchronizationRequest(dateTime));
            dateTime.wait(1000);
        }

        assertEquals(d1.getAllLocalAddresses()[0], receivedAddress.get());
        assertEquals(dateTime, receivedDateTime.get());
        assertEquals(true, receivedUtc.get());
    }

    @Test
    public void disabledTimeSync() throws Exception {
        d2.getServicesSupported().setUtcTimeSynchronization(false);

        final DateTime dateTime = new DateTime(d1);

        // Create the listener in device 2
        final AtomicReference<Address> receivedAddress = new AtomicReference<>(null);
        final AtomicReference<DateTime> receivedDateTime = new AtomicReference<>(null);
        final AtomicBoolean receivedUtc = new AtomicBoolean(false);
        d2.getEventHandler().addListener(new DeviceEventAdapter() {
            @Override
            public void synchronizeTime(final Address from, final DateTime dateTime, final boolean utc) {
                receivedAddress.set(from);
                receivedDateTime.set(dateTime);
                receivedUtc.set(utc);
                synchronized (dateTime) {
                    dateTime.notify();
                }
            }
        });

        final RemoteDevice rd2 = d1.getRemoteDevice(2).get();

        synchronized (dateTime) {
            d1.send(rd2, new UTCTimeSynchronizationRequest(dateTime));
            dateTime.wait(1000);
        }

        assertNull(receivedAddress.get());
        assertNull(receivedDateTime.get());
        assertFalse(receivedUtc.get());
    }
}

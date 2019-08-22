package com.serotonin.bacnet4j.service.confirmed;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import com.serotonin.bacnet4j.AbstractTest;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.TestUtils;
import com.serotonin.bacnet4j.event.PrivateTransferHandler;
import com.serotonin.bacnet4j.exception.BACnetErrorException;
import com.serotonin.bacnet4j.npdu.test.TestNetworkUtils;
import com.serotonin.bacnet4j.service.acknowledgement.ConfirmedPrivateTransferAck;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.EncodedValue;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.enumerated.ErrorClass;
import com.serotonin.bacnet4j.type.enumerated.ErrorCode;
import com.serotonin.bacnet4j.type.error.ConfirmedPrivateTransferError;
import com.serotonin.bacnet4j.type.error.ErrorClassAndCode;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

public class ConfirmedPrivateTransferRequestTest extends AbstractTest {
    @Test
    public void noHandler() {
        TestUtils.assertRequestHandleException(() -> {
            new ConfirmedPrivateTransferRequest(236, 11, new Real(12.3F)).handle(d1, null);
        }, ErrorClass.services, ErrorCode.optionalFunctionalityNotSupported);
    }

    @Test
    public void noParameters() throws Exception {
        final AtomicBoolean handled = new AtomicBoolean();
        d1.addPrivateTransferHandler(236, 12, new PrivateTransferHandler() {
            @Override
            public Encodable handle(final LocalDevice localDevice, final Address from, final UnsignedInteger vendorId,
                    final UnsignedInteger serviceNumber, final EncodedValue serviceParameters, final boolean confirmed)
                    throws BACnetErrorException {
                assertEquals(d1, localDevice);
                assertEquals(TestNetworkUtils.toAddress(2), from);
                assertEquals(new UnsignedInteger(236), vendorId);
                assertEquals(new UnsignedInteger(12), serviceNumber);
                assertEquals(null, serviceParameters);
                assertEquals(true, confirmed);
                handled.set(true);
                return null;
            }
        });

        final ConfirmedPrivateTransferAck ack = d2.send(rd1, new ConfirmedPrivateTransferRequest(236, 12, null)).get();
        assertEquals(new UnsignedInteger(236), ack.getVendorId());
        assertEquals(new UnsignedInteger(12), ack.getServiceNumber());
        assertEquals(null, ack.getResultBlock());
        assertEquals(true, handled.get());
    }

    @Test
    public void withParameters() throws Exception {
        final EncodedValue parameters = new EncodedValue(Boolean.TRUE, Boolean.FALSE, new CharacterString("xyz"));
        final EncodedValue result = new EncodedValue(new Real(3.14F), Boolean.TRUE, Boolean.FALSE,
                new CharacterString("zyx"));

        final AtomicBoolean handled = new AtomicBoolean();
        d1.addPrivateTransferHandler(236, 13, new PrivateTransferHandler() {
            @Override
            public Encodable handle(final LocalDevice localDevice, final Address from, final UnsignedInteger vendorId,
                    final UnsignedInteger serviceNumber, final EncodedValue serviceParameters, final boolean confirmed)
                    throws BACnetErrorException {
                assertEquals(d1, localDevice);
                assertEquals(TestNetworkUtils.toAddress(2), from);
                assertEquals(new UnsignedInteger(236), vendorId);
                assertEquals(new UnsignedInteger(13), serviceNumber);
                assertEquals(parameters, serviceParameters);
                assertEquals(true, confirmed);
                handled.set(true);
                return result;
            }
        });

        final ConfirmedPrivateTransferAck ack = d2.send(rd1, new ConfirmedPrivateTransferRequest(236, 13, parameters))
                .get();
        assertEquals(new UnsignedInteger(236), ack.getVendorId());
        assertEquals(new UnsignedInteger(13), ack.getServiceNumber());
        assertEquals(result, ack.getResultBlock());
        assertEquals(true, handled.get());
    }

    @Test
    public void exception() {
        d1.addPrivateTransferHandler(236, 14, new PrivateTransferHandler() {
            @Override
            public Encodable handle(final LocalDevice localDevice, final Address from, final UnsignedInteger vendorId,
                    final UnsignedInteger serviceNumber, final EncodedValue serviceParameters, final boolean confirmed)
                    throws BACnetErrorException {
                throw new BACnetErrorException(ConfirmedPrivateTransferRequest.TYPE_ID,
                        new ConfirmedPrivateTransferError(
                                new ErrorClassAndCode(ErrorClass.communication, ErrorCode.unknownRoute), vendorId,
                                serviceNumber, new CharacterString("Exception testing")));
            }
        });

        TestUtils.assertErrorAPDUException(() -> {
            d2.send(rd1, new ConfirmedPrivateTransferRequest(236, 14, new EncodedValue(new Real(12.3F)))).get();
        }, ErrorClass.communication, ErrorCode.unknownRoute);
    }
}

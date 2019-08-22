package com.serotonin.bacnet4j.service.confirmed;

import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.exception.BACnetRejectException;
import com.serotonin.bacnet4j.type.constructed.ServicesSupported;
import com.serotonin.bacnet4j.type.enumerated.RejectReason;
import com.serotonin.bacnet4j.util.sero.ByteQueue;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author Michel Seiler
 */
public class ConfirmedRequestTest {

    @Test
    public void readInvalidTag() {
        //Standard test 135.1-2013, 13.4.3
        ByteQueue queue = new ByteQueue("0c0200295d794d"); // ReadPropertyRequest with invalid tag for the Propertyidentifier
        try {
            ConfirmedRequestService.createConfirmedRequestService(ReadPropertyRequest.TYPE_ID, queue);
            fail("Excpected a BACnetRejectException");
        } catch (BACnetException ex) {
            if (!(ex instanceof BACnetRejectException)) {
                fail("The exception must be of type BACnetRejectException");
            }
            BACnetRejectException exception = (BACnetRejectException) ex;
            if (!exception.getRejectReason().isOneOf(
                    RejectReason.invalidTag, RejectReason.inconsistentParameters,
                    RejectReason.invalidParameterDataType, RejectReason.missingRequiredParameter,
                    RejectReason.tooManyArguments)) {
                fail("The reject reason '" + exception.getRejectReason() + "' is not allowed");
            }
        }
    }

    @Test
    public void readMissingRequieredParameter() {
        //Standard test 135.1-2013, 13.4.4
        ByteQueue queue = new ByteQueue("0c0200295d"); // ReadPropertyRequest with ObjectIdentifier "device, 10589" and no Propertyidentifier
        try {
            ConfirmedRequestService.createConfirmedRequestService(ReadPropertyRequest.TYPE_ID, queue);
            fail("Excpected a BACnetRejectException");
        } catch (BACnetException ex) {
            if (!(ex instanceof BACnetRejectException)) {
                fail("The exception must be of type BACnetRejectException");
            }
            BACnetRejectException exception = (BACnetRejectException) ex;
            if (!exception.getRejectReason().isOneOf(
                    RejectReason.missingRequiredParameter, RejectReason.invalidTag)) {
                fail("The reject reason '" + exception.getRejectReason() + "' is not allowed");
            }
        }
    }

    @Test
    public void readTooManyArguments() {
        //Standard test 135.1-2013, 13.4.5
        ByteQueue queue = new ByteQueue("0c0200295d194d194f"); // ReadPropertyRequest with an extra Propertyidentifier
        try {
            ConfirmedRequestService.createConfirmedRequestService(ReadPropertyRequest.TYPE_ID, queue);
            fail("Excpected a BACnetRejectException");
        } catch (BACnetException ex) {
            if (!(ex instanceof BACnetRejectException)) {
                fail("The exception must be of type BACnetRejectException");
            }
            BACnetRejectException exception = (BACnetRejectException) ex;
            if (!exception.getRejectReason().equals(RejectReason.tooManyArguments)) {
                fail("The reject reason '" + exception.getRejectReason() + "' is not allowed");
            }
        }
    }

    @Test
    public void unsupportedConfirmedServiceTest() {
        //Standard test 135.1-2013, 9.39.1
        try {
            ConfirmedRequestService.checkConfirmedRequestService(new ServicesSupported(), AcknowledgeAlarmRequest.TYPE_ID); //No Services are supported
            fail("Excpected a BACnetRejectException");
        } catch (BACnetException ex) {
            if (!(ex instanceof BACnetRejectException)) {
                fail("The exception must be of type BACnetRejectException");
            }
            BACnetRejectException exception = (BACnetRejectException) ex;
            if (!exception.getRejectReason().equals(RejectReason.unrecognizedService)) {
                fail("The reject reason '" + exception.getRejectReason() + "' is not allowed");
            }
        }
    }

}

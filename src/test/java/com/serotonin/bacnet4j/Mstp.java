package com.serotonin.bacnet4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;

import com.serotonin.bacnet4j.apdu.APDU;
import com.serotonin.bacnet4j.apdu.ComplexACK;
import com.serotonin.bacnet4j.apdu.ConfirmedRequest;
import com.serotonin.bacnet4j.apdu.UnconfirmedRequest;
import com.serotonin.bacnet4j.npdu.NPDU;
import com.serotonin.bacnet4j.npdu.Network;
import com.serotonin.bacnet4j.npdu.mstp.MasterNode;
import com.serotonin.bacnet4j.npdu.mstp.MstpNetwork;
import com.serotonin.bacnet4j.type.constructed.ServicesSupported;
import com.serotonin.bacnet4j.type.primitive.OctetString;
import com.serotonin.bacnet4j.util.sero.ByteQueue;

public class Mstp {
    @Test
    public void test() throws Exception {
        //        final String s = "018000";
        //        final String s = "0100100209001c020000392c0200003939004e09702e91002f09cb2e2ea475061a01b40c2132352f2f09c42e91002f4f";
        //        String s = "010402730a0c0c02066c8a1979";
        //        final String s = "0120ffff00ff1000c402066c8a2201e091032201a50a53"; // IAm
        //        final String s = hex(
        //                "1,0,30,8,e,c,2,0,0,64,1e,29,79,4e,75,10,0,4d,65,61,73,75,72,6c,6f,67,69,63,20,49,6e,63,4f,29,70,4e,91,0,4f,1f");
        final String s = "010402730a0e0c000000001e094d091c097509551f0c000000011e094d091c097509551f0c000000021e094d091c097509551f0c000000031e094d091c097509551f0c000000041e094d091c097509551f";
        //        final String s = hex("1,0,30,3,c,c,2,2,dd,d6,19,4c,29,0,3e,21,68,3f,e2,3b");

        final Network network = new MstpNetwork(
                new MasterNode(null, new ByteArrayInputStream(new byte[0]), new ByteArrayOutputStream(), (byte) 0, 2));
        final NPDU npdu = network.parseNpduData(new ByteQueue(s), new OctetString(new byte[] { 1 }));
        final ServicesSupported servicesSupported = new ServicesSupported();
        servicesSupported.setAll(true);
        if (npdu.isNetworkMessage()) {
            System.out.println("Network message: " + npdu.getNetworkMessageType());
        } else {
            final APDU apdu = npdu.getAPDU(servicesSupported);
            if (apdu instanceof UnconfirmedRequest) {
                final UnconfirmedRequest r = (UnconfirmedRequest) apdu;
                r.parseServiceData();
                System.out.println(r.getService());
            } else if (apdu instanceof ConfirmedRequest) {
                final ConfirmedRequest r = (ConfirmedRequest) apdu;
                r.parseServiceData();
                System.out.println(r.getServiceRequest());
            } else if (apdu instanceof ComplexACK) {
                final ComplexACK a = (ComplexACK) apdu;
                a.parseServiceData();
                System.out.println(a.getService());
            } else {
                System.out.println(apdu);
            }
        }
    }

    static String hex(final String commas) {
        final StringBuilder sb = new StringBuilder();
        for (final String s : commas.split(",")) {
            if (s.length() == 1)
                sb.append("0");
            sb.append(s);
        }
        return sb.toString();
    }
}

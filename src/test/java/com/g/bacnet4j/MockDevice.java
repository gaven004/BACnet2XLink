package com.g.bacnet4j;

import java.util.Timer;
import java.util.TimerTask;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkBuilder;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.type.enumerated.BinaryPV;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;

public class MockDevice {
    public static void main(String[] args) {
        int uptime = 0;

        IpNetworkBuilder builder = new IpNetworkBuilder().withBroadcast("192.168.0.255", 24);
        IpNetwork network = builder.build();

        try {
            LocalDevice device = new LocalDevice(505528, new DefaultTransport(network)).initialize();

            BACnetObject obj = new BACnetObject(device, ObjectType.forName("binary-input"), 3000229, "mode");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.active);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-input"), 3000227, "status");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.active);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-input"), 3000228, "trip");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.active);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("analog-value"), 3000238, "running_time");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, new Real(uptime));
            device.addObject(obj);

            new Timer().schedule(new UptimeCounter(uptime, obj), 60 * 1000, 60 * 1000);

            System.out.println("Device is ready!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class UptimeCounter extends TimerTask {
    int uptime;
    BACnetObject obj;

    public UptimeCounter(int uptime, BACnetObject obj) {
        this.uptime = uptime;
        this.obj = obj;
    }

    @Override
    public void run() {
        uptime++;
        obj.writePropertyInternal(PropertyIdentifier.presentValue, new Real(uptime));
    }
}

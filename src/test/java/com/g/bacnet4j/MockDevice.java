package com.g.bacnet4j;

import java.util.Random;
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

        Random random = new Random();

        IpNetworkBuilder builder = new IpNetworkBuilder().withBroadcast("192.168.0.255", 24);
        IpNetwork network = builder.build();

        try {
            LocalDevice device = new LocalDevice(505528, new DefaultTransport(network)).initialize();

            // 风机
            BACnetObject obj = new BACnetObject(device, ObjectType.forName("binary-input"), 3000229, "fan-mode");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.active);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-input"), 3000227, "fan-status");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.active);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-input"), 3000228, "fan-trip");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.active);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("analog-value"), 3000238, "fan-running_time");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, new Real(uptime));
            device.addObject(obj);
            new Timer().schedule(new UptimeCounter(uptime, 1, obj), 60 * 1000, 60 * 1000);

            // 水泵
            obj = new BACnetObject(device, ObjectType.forName("binary-input"), 500101, "pump-mode");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.active);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-input"), 500102, "pump-status");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.active);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-input"), 500103, "pump-trip");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.active);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("analog-value"), 500105, "pump-running_time");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, new Real(uptime));
            device.addObject(obj);
            new Timer().schedule(new UptimeCounter(uptime, 1, obj), 60 * 1000, 60 * 1000);

            obj = new BACnetObject(device, ObjectType.forName("analog-value"), 500106, "pump-pressure");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, new Real(random.nextFloat()));
            device.addObject(obj);

            // 集水井
            obj = new BACnetObject(device, ObjectType.forName("analog-value"), 500401, "well-water_evel");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, new Real(random.nextInt()));
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("analog-value"), 500402, "well-water_capacity");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, new Real(random.nextFloat()));
            device.addObject(obj);



            System.out.println("Device is ready!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class UptimeCounter extends TimerTask {
    int uptime;
    int factor;
    BACnetObject obj;

    public UptimeCounter(int uptime, int factor, BACnetObject obj) {
        this.uptime = uptime;
        this.factor = factor;
        this.obj = obj;
    }

    @Override
    public void run() {
        uptime++;
        obj.writePropertyInternal(PropertyIdentifier.presentValue, new Real(uptime / (float) factor));
    }
}

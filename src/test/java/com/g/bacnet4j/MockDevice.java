package com.g.bacnet4j;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkBuilder;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.obj.BinaryValueObject;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.type.enumerated.BinaryPV;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MockDevice {
    public static void main(String[] args) {
        int uptime = 0;

        Random random = new Random();

        IpNetworkBuilder builder = new IpNetworkBuilder();
        builder.withBroadcast("192.168.0.255", 24);
        builder.withPort(47809);
        IpNetwork network = builder.build();

        try {
            LocalDevice device = new LocalDevice(500682, new DefaultTransport(network)).initialize();

            // 风机
            BACnetObject obj = new BACnetObject(device, ObjectType.forName("binary-input"), 3000229, "fan-mode");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.active);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-input"), 3000227, "fan-status");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.active);
            device.addObject(obj);

//            obj = new BACnetObject(device, ObjectType.forName("binary-input"), 3000228, "fan-trip");
//            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.active);
//            device.addObject(obj);

            new BinaryValueObject(device, 3000228, "fan-trip", BinaryPV.inactive, false).supportCovReporting();

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

            // 进线柜
            obj = new BACnetObject(device, ObjectType.forName("binary-input"), 500501, "box-status");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.active);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("analog-value"), 500502, "box-power_factor");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, new Real(random.nextFloat()));
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("analog-value"), 500503, "box-power");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, new Real(random.nextFloat()));
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("analog-value"), 500504, "box-inspecting_power");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, new Real(random.nextFloat()));
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("analog-value"), 500505, "box-total_active_power");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, new Real(random.nextFloat()));
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("analog-value"), 500506, "box-active_energy");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, new Real(random.nextFloat()));
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("analog-value"), 500507, "box-current_a");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, new Real(random.nextFloat()));
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("analog-value"), 500508, "box-current_b");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, new Real(random.nextFloat()));
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("analog-value"), 500509, "box-current_c");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, new Real(random.nextFloat()));
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("analog-value"), 500510, "box-voltage_a");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, new Real(random.nextFloat()));
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("analog-value"), 500511, "box-voltage_b");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, new Real(random.nextFloat()));
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("analog-value"), 500512, "box-voltage_c");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, new Real(random.nextFloat()));
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("analog-value"), 500513, "box-line_voltage_ac");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, new Real(random.nextFloat()));
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("analog-value"), 500514, "box-line_voltage_ab");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, new Real(random.nextFloat()));
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("analog-value"), 500515, "box-line_voltage_bc");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, new Real(random.nextInt(10) + 375));
            device.addObject(obj);

            // 电梯
            obj = new BACnetObject(device, ObjectType.forName("analog-value"), 3000038, "elevator-current_floor");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, new Real(random.nextInt(20)));
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000040, "elevator-3000040");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000041, "elevator-3000041");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000042, "elevator-3000042");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000043, "elevator-3000043");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000044, "elevator-3000044");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000045, "elevator-3000045");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000046, "elevator-3000046");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000047, "elevator-3000047");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000048, "elevator-3000048");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000049, "elevator-3000049");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000050, "elevator-3000050");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000051, "elevator-3000051");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000052, "elevator-3000052");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000053, "elevator-3000053");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000054, "elevator-3000054");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000055, "elevator-3000055");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000056, "elevator-3000056");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000057, "elevator-3000057");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000058, "elevator-3000058");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000059, "elevator-3000059");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000060, "elevator-3000060");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000061, "elevator-3000061");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000062, "elevator-3000062");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000063, "elevator-3000063");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000064, "elevator-3000064");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000065, "elevator-3000065");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000066, "elevator-3000066");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000067, "elevator-3000067");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000068, "elevator-3000068");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000069, "elevator-3000069");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000070, "elevator-3000070");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000071, "elevator-3000071");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000072, "elevator-3000072");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000073, "elevator-3000073");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000074, "elevator-3000074");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000075, "elevator-3000075");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000076, "elevator-3000076");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000077, "elevator-3000077");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000078, "elevator-3000078");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000079, "elevator-3000079");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000080, "elevator-3000080");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-value"), 3000081, "elevator-3000081");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
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

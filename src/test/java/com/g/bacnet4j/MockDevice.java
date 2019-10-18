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
            obj.writePropertyInternal(PropertyIdentifier.presentValue, new Real(random.nextFloat()));
            device.addObject(obj);

            // 电梯
            obj = new BACnetObject(device, ObjectType.forName("analog-value"), 500304, "elevator-current_floor");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, new Real(random.nextInt()));
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-input"), 500320, "elevator-down");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-input"), 500321, "elevator-up");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-input"), 500322, "elevator-in_service");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.active);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-input"), 500323, "elevator-inspection");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-input"), 500324, "elevator-out_of_service");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-input"), 500325, "elevator-parking");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-input"), 500326, "elevator-fireman_control");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-input"), 500327, "elevator-fire_operation");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-input"), 500330, "elevator-parallel_operation");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-input"), 500331, "elevator-group_mode_in_normal");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-input"), 500332, "elevator-power_in_normal");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.active);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-input"), 500333, "elevator-car_door_closed");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-input"), 500334, "elevator-self_power_supply");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-input"), 500335, "elevator-stopped");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-input"), 500336, "elevator-opening");
            obj.writePropertyInternal(PropertyIdentifier.presentValue, BinaryPV.inactive);
            device.addObject(obj);

            obj = new BACnetObject(device, ObjectType.forName("binary-input"), 500337, "elevator-closing");
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

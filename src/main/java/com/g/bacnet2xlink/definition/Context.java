package com.g.bacnet2xlink.definition;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

public class Context {
    /**
     * 在应用初始化、云平台登录后，保存所有正常的设备
     */
    private Map<Integer, Device> deviceMap = new ConcurrentHashMap<>(512);

    /**
     * 物理设备注册COV监听后，登记在这个表中，供COV事件触发后回查
     */
    private Map<ObjectIdentifier, Device> monitoredDeviceMap = new ConcurrentHashMap<>(512);

    /**
     * 正常物理设备相关方法
     */

    public void addDevice(Device device) {
        deviceMap.put(device.getXDeviceId(), device);
    }

    public void removeDevice(int xDeviceId) {
        deviceMap.remove(xDeviceId);
    }

    public void clearDeviceMap() {
        deviceMap.clear();
    }

    public Collection<Device> getAllDevice() {
        return deviceMap.values();
    }

    /**
     * 根据云平台登录后返回的设备ID，查找对应的物理设备
     *
     * @param xDeviceId 云平台登录后返回的设备ID
     * @return
     */
    public Device getDevice(int xDeviceId) {
        return deviceMap.get(xDeviceId);
    }

    /**
     * 注册COV监听的设备的相关方法
     */

    public void addMonitoredDevice(ObjectIdentifier oid, Device device) {
        monitoredDeviceMap.put(oid, device);
    }

    public void removeMonitoredDevice(int xDeviceId) {
        monitoredDeviceMap.remove(xDeviceId);
    }

    public void clearMonitoredDeviceMap() {
        monitoredDeviceMap.clear();
    }

    /**
     * 根据设备的对象ID，取对应的物理设备
     *
     * @param oid 对象ID
     * @return
     */
    public Device getMonitoredDevice(ObjectIdentifier oid) {
        return monitoredDeviceMap.get(oid);
    }
}

package com.g.bacnet2xlink.definition;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.xlink.iot.sdk.mqtt.client.cm.XlinkCmMqttClient;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import com.g.bacnet2xlink.exception.UnknownDevice;

public class Context {
    /**
     * Xagent客户端Map，以productId为key
     */
    private Map<String, XlinkCmMqttClient> xlinkCmMqttClientMap = new ConcurrentHashMap<>(64);

    /**
     * 在应用初始化、云平台登录后，保存所有正常的设备，以云平台登录后返回的xlinkDeviceId为key
     */
    private Map<Integer, Device> deviceMap = new ConcurrentHashMap<>(512);

    /**
     * 物理设备注册COV监听后，登记在这个表中，供COV事件触发后回查，以物理设备的对象为key
     */
    private Map<ObjectIdentifier, Device> monitoredDeviceMap = new ConcurrentHashMap<>(512);

    /**
     * Xagent客户端相关方法
     */
    public void addXlinkClient(String key, XlinkCmMqttClient client) {
        xlinkCmMqttClientMap.put(key, client);
    }

    public void removeXlinkClient(String key) {
        xlinkCmMqttClientMap.remove(key);
    }

    public void clearXlinkClientMap() {
        xlinkCmMqttClientMap.clear();
    }

    public Collection<XlinkCmMqttClient> getAllXlinkClient() {
        return xlinkCmMqttClientMap.values();
    }

    /**
     * 根据productId，获取对应的XlinkCmMqttClient
     *
     * @param key productId
     * @return
     */
    public XlinkCmMqttClient getXlinkClient(String key) {
        return xlinkCmMqttClientMap.get(key);
    }

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
    public Device getDevice(int xDeviceId) throws UnknownDevice {
        final Device device = deviceMap.get(xDeviceId);
        if (device != null) {
            return device;
        }

        throw new UnknownDevice();
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
    public Device getMonitoredDevice(ObjectIdentifier oid) throws UnknownDevice {
        final Device device = monitoredDeviceMap.get(oid);
        if (device != null) {
            return device;
        }

        throw new UnknownDevice();
    }
}

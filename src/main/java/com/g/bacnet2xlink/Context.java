package com.g.bacnet2xlink;

import cn.xlink.iot.sdk.mqtt.client.cm.XlinkCmMqttClient;
import com.g.bacnet2xlink.definition.Device;
import com.g.bacnet2xlink.exception.UnknownDevice;
import com.g.bacnet2xlink.exception.UnknownRemoteDevice;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Context {
    /**
     * Xagent客户端，单例
     */
    private XlinkCmMqttClient xlinkCmMqttClient;
    /**
     * BACnet local device
     */
    private LocalDevice localDevice;
    /**
     * BACnet remote devices
     */
    private Map<Integer, RemoteDevice> remoteDeviceMap = new ConcurrentHashMap<>(16);
    /**
     * 在应用初始化、云平台登录后，保存所有正常的设备，以云平台登录后返回的xlinkDeviceId为key
     */
    private Map<Integer, Device> deviceMap = new ConcurrentHashMap<>(512);
    /**
     * 物理设备注册COV监听后，登记在这个表中，供COV事件触发后回查，以物理设备的对象为key
     */
    private Map<ObjectIdentifier, Device> monitoredDeviceMap = new ConcurrentHashMap<>(512);
    /**
     * 转换器
     */
    private Map<String, Object> converterMap = new ConcurrentHashMap<>();

    /**
     * Xagent客户端相关方法
     */
    public XlinkCmMqttClient getXlinkCmMqttClient() {
        return xlinkCmMqttClient;
    }

    public void setXlinkCmMqttClient(XlinkCmMqttClient xlinkCmMqttClient) {
        this.xlinkCmMqttClient = xlinkCmMqttClient;
    }

    /**
     * BACnet local device setter & getter
     */
    public LocalDevice getLocalDevice() {
        return localDevice;
    }

    public void setLocalDevice(LocalDevice localDevice) {
        this.localDevice = localDevice;
    }

    /**
     * BACnet remote devices methods
     */
    public void addRemoteDevice(RemoteDevice device) {
        remoteDeviceMap.put(device.getInstanceNumber(), device);
    }

    public void removeRemoteDevice(int remoteDeviceNumber) {
        remoteDeviceMap.remove(remoteDeviceNumber);
    }

    public void clearRemoteDeviceMap() {
        remoteDeviceMap.clear();
    }

    public Collection<RemoteDevice> getAllRemoteDevice() {
        return remoteDeviceMap.values();
    }

    public RemoteDevice getRemoteDevice(int remoteDeviceNumber) throws UnknownRemoteDevice {
        final RemoteDevice device = remoteDeviceMap.get(remoteDeviceNumber);
        if (device != null) {
            return device;
        }

        throw new UnknownRemoteDevice();
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

    public void addConverter(String key, Object converter) {
        converterMap.put(key, converter);
    }

    public Object getConverter(String key) {
        return converterMap.get(key);
    }
}

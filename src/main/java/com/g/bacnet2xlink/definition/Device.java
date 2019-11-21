package com.g.bacnet2xlink.definition;

import java.util.List;
import java.util.Map;

import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import lombok.Data;

import com.g.bacnet2xlink.exception.UnknownEvent;
import com.g.bacnet2xlink.exception.UnknownProperty;
import com.g.bacnet2xlink.exception.UnknownService;

/**
 * 设备，在概念上对应一个物理设备
 * <p>
 * 由于在实现里，所有的物理设备都挂接到统一的控制器，
 * 设备的对象映射到控制器的对象，所有状态都只从控制器中读取，
 * 这样造成同类的设备同一属性，会对应到控制器不同的对象，
 * 因此不能在产品里统一定义，需要到设备一层才有具体的对应
 */
@Data
public class Device {
    private String productId;

    private Integer remoteDeviceNumber;
    private String mac; // 虚拟的设备MAC，用于云平台的设备上线

    private int xDeviceId; // 云平台登录后返回的设备ID

    private List<Property> properties;
    private List<Property> cproperties;
    private List<ObjectIdentifier> oids;
    private List<Event> events;
    private List<Service> services;

    private Map<String, Map<String, Object>> converterProperties;

    private Map<String, Object> immutableProperties;

    private Map<ObjectIdentifier, Property> propertyMap;
    private Map<String, Property> xPropertyMap;

    private Map<String, Service> xServiceMap;

    private Map<ObjectIdentifier, Event> eventMap;

    public Map<String, Object> getConverterProperty(String key) {
        return converterProperties.get(key);
    }

    /**
     * 根据云平台的属性，查对应物理设备的对象
     * 在一对一的情况下才有实际意义
     *
     * @param name 云平台属性
     * @return
     */
    public Property getPropertyX(String name) throws UnknownProperty {
        final Property property = xPropertyMap.get(name);
        if (property != null) {
            return property;
        }

        throw new UnknownProperty();
    }

    /**
     * 根据物理设备的对象ID，查对应对象定义
     * 在一对一的情况下才有实际意义
     *
     * @param oid 物理设备的对象ID
     * @return
     */
    public Property getProperty(ObjectIdentifier oid) throws UnknownProperty {
        final Property property = propertyMap.get(oid);
        if (property != null) {
            return property;
        }

        throw new UnknownProperty();
    }

    /**
     * 根据云平台服务名，查对应服务
     *
     * @param name
     * @return
     */
    public Service getServiceX(String name) throws UnknownService {
        final Service service = xServiceMap.get(name);
        if (service != null) {
            return service;
        }

        throw new UnknownService();
    }

    /**
     * 根据物理设备的对象ID，查对应对象定义
     *
     * @param oid 物理设备的对象ID
     * @return
     */
    public Event getEvent(ObjectIdentifier oid) throws UnknownEvent {
        final Event event = eventMap.get(oid);
        if (event != null) {
            return event;
        }

        throw new UnknownEvent();
    }
}

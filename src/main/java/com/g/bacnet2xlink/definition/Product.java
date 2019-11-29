package com.g.bacnet2xlink.definition;

import java.util.List;

import lombok.Data;

/**
 * 产品，云平台的概念，指同类的设备，具有相同的属性、事件及服务
 * <p>
 * 由于在实现里，所有的设备都挂接到统一的控制器，
 * 设备的对象映射到控制器的对象，所有状态都只从控制器中读取，
 * 这样造成同类的设备同一属性，会对应到控制器不同的对象，
 * 因此不能在产品里统一定义，需要到设备一层才有具体的对应
 * <p>
 * 每个产品，需要创建一个云平台的XlinkCmMqttClient
 */
@Data
public class Product {
    private String name;
    private String id;
    private String desc;

    private List<Property> properties;
    private List<Property> cproperties;
    private List<Event> events;
    private List<Event> selfEvents;
    private List<Service> services; // 用于XlinkCmMqttClient初始化进注册服务

    private List<Device> devices;

}

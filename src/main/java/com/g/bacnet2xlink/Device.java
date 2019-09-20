package com.g.bacnet2xlink;

import java.util.List;

import lombok.Data;

/**
 * 设备，在概念上对应一个物理设备
 *
 * 由于在实现里，所有的物理设备都挂接到统一的控制器，
 * 设备的对象映射到控制器的对象，所有状态都只从控制器中读取，
 * 这样造成同类的设备同一属性，会对应到控制器不同的对象，
 * 因此不能在产品里统一定义，需要到设备一层才有具体的对应
 *
 */
@Data
public class Device {
    private String mac; // 虚拟的设备MAC，用于云平台的设备上线

    private List<Property> properties;
    private List<Event> events;
    private List<Service> services;
}

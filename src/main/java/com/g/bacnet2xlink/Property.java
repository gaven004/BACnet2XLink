package com.g.bacnet2xlink;

import java.util.List;

import lombok.Data;

/**
 * 设备属性
 * <p>
 * 云平台的设备属性对应物理设备一个对象的一个属性的值
 * <p>
 * values定义属性的枚举值，两边平台同含义的值的代码可以不相同
 */
@Data
public class Property {
    private String name;
    private String desc;
    private String objectType;
    private int objectId;

    private List<Value> valueSet;
}

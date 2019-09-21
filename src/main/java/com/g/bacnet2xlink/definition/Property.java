package com.g.bacnet2xlink.definition;

import java.util.List;
import java.util.Map;

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
    private String destType;

    private String objectType;
    private int objectId;

    private List<Value> valueSet;
    private String valueConverter;

    private Map<String, Value> valueMap;
    private Map<Object, Value> xValueMap;

    /**
     * 根据物理设备值，查对应的值定义
     * @param key 物理设备值的String表示
     * @return
     */
    public Value getValue(String key) {
        return valueMap.get(key);
    }

    /**
     * 根据云平台属性值，查对应的值定义
     * @param key
     * @return
     */
    public Value getValueX(Object key) {
        return xValueMap.get(key);
    }
}

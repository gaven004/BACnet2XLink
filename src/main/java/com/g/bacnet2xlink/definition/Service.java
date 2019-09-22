package com.g.bacnet2xlink.definition;

import java.util.List;
import java.util.Map;

import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import lombok.Data;

import com.g.bacnet2xlink.exception.UnknownValue;

/**
 * 设备服务
 * <p>
 * 云平台调用服务，操作具体的物理设备
 */
@Data
public class Service {
    private String name;
    private String desc;

    private List<ServiceParam> srcParam; // 云平台入参定义，多个，
    private String destParamType; // 对物理设备的属性的值的类型定义

    private List<ServiceParamValue> valueSet; // 云平台入参值（多个） 到 物理设备属性值（一个） 的映射对
    private String valueConverter;

    private int objectId;
    private String objectType;
    private ObjectIdentifier oid;

    private Map<String, ServiceParamValue> xValueMap;

    /**
     * 根据云平台参数值，查对应的值定义
     *
     * @param key
     * @return
     */
    public ServiceParamValue getValueX(List<Object> key) throws UnknownValue {
        ServiceParamValue value = (key == null ? xValueMap.get(ServiceParamValue.NULL_SCR_KEY) : xValueMap.get(key.toString()));
        if (value != null) {
            return value;
        }

        throw new UnknownValue();
    }

}

package com.g.bacnet2xlink.definition;

import java.util.List;
import java.util.Map;

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

    private List<ServiceParam> srcParam;
    private String destParamType;

    private List<ServiceParamValue> valueSet;
    private String valueConverter;

    private int objectId;
    private String objectType;

    private Map<String, ServiceParamValue> xValueMap;

    /**
     * 根据云平台参数值，查对应的值定义
     *
     * @param key
     * @return
     */
    public ServiceParamValue getValueX(List<Object> key) throws UnknownValue {
        final ServiceParamValue value = xValueMap.get(key.toString());
        if (value != null) {
            return value;
        }

        throw new UnknownValue();
    }

}

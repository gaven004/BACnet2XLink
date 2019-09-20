package com.g.bacnet2xlink;

import java.util.List;

import lombok.Data;

/**
 * 设备服务
 *
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
}

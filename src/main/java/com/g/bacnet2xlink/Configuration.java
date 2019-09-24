package com.g.bacnet2xlink;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.g.bacnet2xlink.definition.*;

@Data
public class Configuration {
    private static final Logger log = LoggerFactory.getLogger(Configuration.class);

    /**
     * Xlink配置参数
     */
    private String certId; // 授权证书ID（CID）
    private String certKey; // 授权证书密钥（CKEY）
    private String deviceEndpoint; // 云端CM服务器地址
    private String connectorType; // 连接器类型，用于标识连接器类别

    private int version; // 物模型的版本号

    /**
     * 物理设备参数
     */
    private Integer localDeviceNumber;
    private String localAddress; // nullable, default value: "0.0.0.0"
    private Integer localPort; // nullable, default value: 0xBAC0 = 47808
    private String broadcastAddress; // the broadcast address for the network
    private Integer networkPrefix; // the number of bits in the local subnet.
    private Integer remoteDeviceNumber;

    /**
     * 系统参数
     */
    private Integer dataSubmitInterval; // 数据上报间隔，单位：秒

    /**
     * 设备定义
     */
    private List<Product> products;

    /**
     * 从资源文件恢复系统配置
     *
     * @param name 资源文件名
     * @return 系统配置
     */
    public static Configuration fromResource(String name) throws IOException {
        InputStream in = null;

        try {
            in = Configuration.class.getResourceAsStream(name);
        } catch (Exception ignore) {
        }

        if (in == null) {
            final String stripped = name.startsWith("/") ? name.substring(1) : null;

            if (in == null) {
                try {
                    in = ClassLoader.getSystemResourceAsStream(stripped);
                } catch (Exception ignore) {
                }
            }
        }

        try {
            Configuration configuration = JSON.parseObject(in, StandardCharsets.UTF_8, Configuration.class);
            configuration.after();
            return configuration;
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * 检验
     */
    void validate() {
        // todo
    }

    /**
     * 整理数据，构建相关map
     */
    void after() {
        for (Product product : products) {
            // 准备产品级的Property相关的valueMap
            for (Property property : product.getProperties()) {
                if (property.getValueSet() != null) {
                    Map<String, Value> valueMap = new HashMap<>();
                    Map<Object, Value> xValueMap = new HashMap<>();

                    for (Value value : property.getValueSet()) {
                        valueMap.put(value.getValue(), value);
                        xValueMap.put(value.getXvalue(), value);
                    }

                    property.setValueMap(valueMap);
                    property.setXValueMap(xValueMap);
                }
            }

            for (Event event : product.getEvents()) {
                if (event.getMessageSet() != null) {
                    Map<String, EventMessage> messageMap = new HashMap<>();

                    for (EventMessage msg : event.getMessageSet()) {
                        messageMap.put(msg.getValue(), msg);
                    }

                    event.setMessageMap(messageMap);
                }
            }

            for (Service service : product.getServices()) {
                if (service.getValueSet() != null) {
                    Map<String, ServiceParamValue> xValueMap = new HashMap<>();

                    for (ServiceParamValue value : service.getValueSet()) {
                        if (value.getScr() != null) {
                            xValueMap.put(value.getScr().toString(), value);
                        } else {
                            xValueMap.put(ServiceParamValue.NULL_SCR_KEY, value);
                        }
                    }

                    service.setXValueMap(xValueMap);
                }
            }

            // 根据产品定义，对每一设备设备属性、事件、服务的细项内容
            for (Device device : product.getDevices()) {
                device.setProductId(product.getId());

                for (Property dest : device.getProperties()) {
                    dest.setOid(new ObjectIdentifier(ObjectType.forName(dest.getObjectType()), dest.getObjectId()));
                    for (Property src : product.getProperties()) {
                        if (dest.getName().equals(src.getName())) {
                            dest.setDestType(src.getDestType());
                            if (src.getValueSet() != null) {
                                dest.setValueSet(src.getValueSet());
                                dest.setValueMap(src.getValueMap());
                                dest.setXValueMap(src.getXValueMap());
                            }
                            if (src.getValueConverter() != null && src.getValueConverter().trim().length() > 0) {
                                dest.setValueConverter(src.getValueConverter());
                            }
                            break;
                        }
                    }
                }

                for (Event dest : device.getEvents()) {
                    dest.setCovPid(PropertyIdentifier.forName(dest.getCovProperty()));
                    for (Event src : product.getEvents()) {
                        if (dest.getName().equals(src.getName())) {
                            dest.setType(src.getType());
                            if (src.getMessageSet() != null) {
                                dest.setMessageSet(src.getMessageSet());
                                dest.setMessageMap(src.getMessageMap());
                            }
                            break;
                        }
                    }
                }

                for (Service dest : device.getServices()) {
                    dest.setOid(new ObjectIdentifier(ObjectType.forName(dest.getObjectType()), dest.getObjectId()));
                    for (Service src : product.getServices()) {
                        if (dest.getName().equals(src.getName())) {
                            if (src.getSrcParam() != null) {
                                dest.setSrcParam(src.getSrcParam());
                            }
                            if (src.getDestParamType() != null && src.getDestParamType().trim().length() > 0) {
                                dest.setDestParamType(src.getDestParamType());
                            }
                            if (src.getValueSet() != null) {
                                dest.setValueSet(src.getValueSet());
                                dest.setXValueMap(src.getXValueMap());
                            }
                            if (src.getValueConverter() != null && src.getValueConverter().trim().length() > 0) {
                                dest.setValueConverter(src.getValueConverter());
                            }
                            break;
                        }
                    }
                }

                List<ObjectIdentifier> oids = new ArrayList<>();
                Map<ObjectIdentifier, Property> propertyMap = new HashMap<>();
                Map<String, Property> xPropertyMap = new HashMap<>();
                for (Property property : device.getProperties()) {
                    ObjectIdentifier oid = new ObjectIdentifier(ObjectType.forName(property.getObjectType()), property.getObjectId());
                    oids.add(oid);
                    propertyMap.put(oid, property);
                    xPropertyMap.put(property.getName(), property);
                }
                device.setOids(oids);
                device.setPropertyMap(propertyMap);
                device.setXPropertyMap(xPropertyMap);

                if (device.getServices() != null) {
                    Map<String, Service> xServiceMap = new HashMap<>();
                    for (Service service : device.getServices()) {
                        xServiceMap.put(service.getName(), service);
                    }
                    device.setXServiceMap(xServiceMap);
                }

                if (device.getEvents() != null) {
                    Map<ObjectIdentifier, Event> eventMap = new HashMap<>();
                    for (Event event : device.getEvents()) {
                        eventMap.put(new ObjectIdentifier(ObjectType.forName(event.getObjectType()), event.getObjectId()), event);
                    }
                    device.setEventMap(eventMap);
                }
            }
        }
    }
}

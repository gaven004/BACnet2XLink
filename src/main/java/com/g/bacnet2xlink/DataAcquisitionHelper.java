package com.g.bacnet2xlink;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.g.bacnet2xlink.definition.Event;
import com.g.bacnet2xlink.definition.EventMessage;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.exception.PropertyValueException;
import com.serotonin.bacnet4j.type.constructed.ObjectPropertyReference;
import com.serotonin.bacnet4j.util.PropertyValues;
import com.serotonin.bacnet4j.util.RequestUtils;
import org.slf4j.Logger;

import com.g.bacnet2xlink.converter.MultiValueConverter;
import com.g.bacnet2xlink.definition.Device;
import com.g.bacnet2xlink.definition.Property;

public class DataAcquisitionHelper {
    public static void readPresentValues(Context context, LocalDevice localDevice, RemoteDevice remoteDevice,
                                         Device device, Map<String, Object> attributes, List<EventMessage> events, Logger log) throws Exception {
        PropertyValues pvs = RequestUtils.readOidPresentValues(localDevice, remoteDevice, device.getOids(), null);
        for (ObjectPropertyReference opr : pvs) {
            log.info("\t{} = {}", opr.getObjectIdentifier().toString(), pvs.get(opr));

            Property property = device.getProperty(opr.getObjectIdentifier());
            if (property.getName() != null && property.getName().trim().length() > 0) {
                Object value = null;
                if (property.getValueConverter() != null && property.getValueConverter().trim().length() > 0) {

                } else if (property.getValueSet() != null && property.getValueSet().size() > 0) {
                    value = property.getValue(pvs.get(opr).toString()).getXvalue();
                } else {
                    String type = property.getDestType();
                    if (type.equals("Integer")) {
                        value = Float.valueOf(pvs.get(opr).toString()).intValue();
                    } else if (type.equals("Float")) {
                        value = Float.parseFloat(pvs.get(opr).toString());
                    } else {
                        value = pvs.get(opr).toString();
                    }
                }
                attributes.put(property.getName(), value);
            }

            if (device.getSelfEvents() != null) {
                Event event = device.getSelfEvent(opr.getObjectIdentifier());
                if (event != null) {
                    String value = pvs.get(opr).toString();
                    try {
                        EventMessage message = event.getMessage(value);
                        if (message != null) {
                            EventMessage target = new EventMessage();
                            target.setType(event.getType());
                            target.setCode(message.getCode());
                            target.setMessage(message.getMessage());
                            events.add(target);
                        }
                    } catch (Exception ignore) {

                    }
                }
            }
        }

        // 处理多值转换的属性
        List<Property> cproperties = device.getCproperties();
        if (cproperties != null && !cproperties.isEmpty()) {
            for (Property property : cproperties) {
                Optional<MultiValueConverter> optConverter = Optional.of((MultiValueConverter) context.getConverter(property.getValueConverter()));
                optConverter.ifPresent(converter -> {
                    try {
                        attributes.put(property.getName(), converter.convert(pvs, device.getConverterProperty(property.getValueConverter())));
                    } catch (PropertyValueException e) {
                        log.warn("属性转换失败，property: {}，exception: {}", property.getName(), e.getMessage());
                    }
                });
            }
        }

        // 处理常量属性
        if (!device.getImmutableProperties().isEmpty()) {
            attributes.putAll(device.getImmutableProperties());
        }
    }
}

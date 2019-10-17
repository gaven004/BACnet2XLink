package com.g.bacnet2xlink;

import java.util.Map;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.type.constructed.ObjectPropertyReference;
import com.serotonin.bacnet4j.util.PropertyValues;
import com.serotonin.bacnet4j.util.RequestUtils;
import org.slf4j.Logger;

import com.g.bacnet2xlink.definition.Device;
import com.g.bacnet2xlink.definition.Property;

public class DataAcquisitionHelper {
    public static void readPresentValues(LocalDevice localDevice, RemoteDevice remoteDevice, Device device,
                                         Map<String, Object> attributes, Logger log) throws Exception {
        PropertyValues pvs = RequestUtils.readOidPresentValues(localDevice, remoteDevice, device.getOids(), null);
        for (ObjectPropertyReference opr : pvs) {
            log.info("\t{} = {}", opr.getObjectIdentifier().toString(), pvs.get(opr));
            Property property = device.getProperty(opr.getObjectIdentifier());
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

        if (!device.getImmutableProperties().isEmpty()) {
            attributes.putAll(device.getImmutableProperties());
        }
    }
}

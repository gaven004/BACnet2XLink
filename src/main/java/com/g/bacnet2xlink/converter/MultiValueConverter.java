package com.g.bacnet2xlink.converter;

import java.util.Map;

import com.serotonin.bacnet4j.exception.PropertyValueException;
import com.serotonin.bacnet4j.util.PropertyValues;

public interface MultiValueConverter {
    public Object convert(PropertyValues src, Map<String, Object> props) throws PropertyValueException;
}

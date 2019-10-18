package com.g.bacnet2xlink.converter;

import com.serotonin.bacnet4j.exception.PropertyValueException;
import com.serotonin.bacnet4j.type.Encodable;

public interface ValueConverter {
    public Object convert(Encodable src) throws PropertyValueException;
}

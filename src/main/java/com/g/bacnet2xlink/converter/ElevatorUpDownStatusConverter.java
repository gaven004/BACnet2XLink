package com.g.bacnet2xlink.converter;

import java.util.Map;

import com.serotonin.bacnet4j.exception.PropertyValueException;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.enumerated.*;
import com.serotonin.bacnet4j.type.error.ErrorClassAndCode;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.PropertyValues;

public class ElevatorUpDownStatusConverter implements MultiValueConverter {
    /**
     * @return 1：下行；2：上行 ;3：静止；
     */
    @Override
    public Object convert(PropertyValues src, Map<String, Object> props) throws PropertyValueException {
        // D2-0，为1表示下行
        Encodable value = src.get(new ObjectIdentifier(ObjectType.binaryValue, (Integer) props.get("D2-0")),
                PropertyIdentifier.presentValue);
        if (!(value instanceof BinaryPV)) {
            throw new PropertyValueException(new ErrorClassAndCode(ErrorClass.property, ErrorCode.invalidDataType));
        }
        if (BinaryPV.active.equals(value)) {
            return 1;
        }

        // D2-1，为1表示上行
        value = src.get(new ObjectIdentifier(ObjectType.binaryValue, (Integer) props.get("D2-1")),
                PropertyIdentifier.presentValue);
        if (!(value instanceof BinaryPV)) {
            throw new PropertyValueException(new ErrorClassAndCode(ErrorClass.property, ErrorCode.invalidDataType));
        }
        if (BinaryPV.active.equals(value)) {
            return 2;
        }

        return 3;
    }
}

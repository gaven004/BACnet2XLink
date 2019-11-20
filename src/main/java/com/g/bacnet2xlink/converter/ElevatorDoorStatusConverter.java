package com.g.bacnet2xlink.converter;

import com.serotonin.bacnet4j.exception.PropertyValueException;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.enumerated.*;
import com.serotonin.bacnet4j.type.error.ErrorClassAndCode;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.PropertyValues;

public class ElevatorDoorStatusConverter implements MultiValueConverter {
    /**
     * @return 1：开门；2关门；3：开门中；4：关门中
     */
    @Override
    public Object convert(PropertyValues src) throws PropertyValueException {
        // D3-6
        Encodable value = src.get(new ObjectIdentifier(ObjectType.binaryValue, 3000054), PropertyIdentifier.presentValue);
        if (!(value instanceof BinaryPV)) {
            throw new PropertyValueException(new ErrorClassAndCode(ErrorClass.property, ErrorCode.invalidDataType));
        }
        if (BinaryPV.active.equals(value)) {
            return 3;
        }

        // D3-7
        value = src.get(new ObjectIdentifier(ObjectType.binaryValue, 3000055), PropertyIdentifier.presentValue);
        if (!(value instanceof BinaryPV)) {
            throw new PropertyValueException(new ErrorClassAndCode(ErrorClass.property, ErrorCode.invalidDataType));
        }
        if (BinaryPV.active.equals(value)) {
            return 4;
        }

        // D3-3
        value = src.get(new ObjectIdentifier(ObjectType.binaryValue, 3000051), PropertyIdentifier.presentValue);
        if (!(value instanceof BinaryPV)) {
            throw new PropertyValueException(new ErrorClassAndCode(ErrorClass.property, ErrorCode.invalidDataType));
        }
        if (BinaryPV.active.equals(value)) {
            return 2;
        }

        return 1;
    }
}

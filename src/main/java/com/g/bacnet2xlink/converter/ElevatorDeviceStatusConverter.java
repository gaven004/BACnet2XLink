package com.g.bacnet2xlink.converter;

import com.serotonin.bacnet4j.exception.PropertyValueException;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.enumerated.*;
import com.serotonin.bacnet4j.type.error.ErrorClassAndCode;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.PropertyValues;

public class ElevatorDeviceStatusConverter implements MultiValueConverter {
    /**
     * @return 1：运行;2：停止; 3：消防锁定
     */
    @Override
    public Object convert(PropertyValues src) throws PropertyValueException {
        Encodable value = src.get(new ObjectIdentifier(ObjectType.binaryValue, 500322), PropertyIdentifier.presentValue);
        if (!(value instanceof BinaryPV)) {
            throw new PropertyValueException(new ErrorClassAndCode(ErrorClass.property, ErrorCode.invalidDataType));
        }
        if (BinaryPV.active.equals(value)) {
            return 1;
        }

        value = src.get(new ObjectIdentifier(ObjectType.binaryValue, 500320), PropertyIdentifier.presentValue);
        if (!(value instanceof BinaryPV)) {
            throw new PropertyValueException(new ErrorClassAndCode(ErrorClass.property, ErrorCode.invalidDataType));
        }
        if (BinaryPV.active.equals(value)) {
            return 1;
        }

        value = src.get(new ObjectIdentifier(ObjectType.binaryValue, 500321), PropertyIdentifier.presentValue);
        if (!(value instanceof BinaryPV)) {
            throw new PropertyValueException(new ErrorClassAndCode(ErrorClass.property, ErrorCode.invalidDataType));
        }
        if (BinaryPV.active.equals(value)) {
            return 1;
        }

        value = src.get(new ObjectIdentifier(ObjectType.binaryValue, 500326), PropertyIdentifier.presentValue);
        if (!(value instanceof BinaryPV)) {
            throw new PropertyValueException(new ErrorClassAndCode(ErrorClass.property, ErrorCode.invalidDataType));
        }
        if (BinaryPV.active.equals(value)) {
            return 3;
        }

        value = src.get(new ObjectIdentifier(ObjectType.binaryValue, 500327), PropertyIdentifier.presentValue);
        if (!(value instanceof BinaryPV)) {
            throw new PropertyValueException(new ErrorClassAndCode(ErrorClass.property, ErrorCode.invalidDataType));
        }
        if (BinaryPV.active.equals(value)) {
            return 3;
        }

        return 2;
    }
}

package com.g.bacnet2xlink.converter;

import java.util.Map;

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
    public Object convert(PropertyValues src, Map<String, Object> props) throws PropertyValueException {
        // D3-6，为1 表示开门中
        Encodable value = src.get(new ObjectIdentifier(ObjectType.binaryValue, (Integer) props.get("D3-6")), PropertyIdentifier.presentValue);
        if (!(value instanceof BinaryPV)) {
            throw new PropertyValueException(new ErrorClassAndCode(ErrorClass.property, ErrorCode.invalidDataType));
        }
        if (BinaryPV.active.equals(value)) {
            return 3;
        }

        // D3-7，为1 表示关门中
        value = src.get(new ObjectIdentifier(ObjectType.binaryValue, (Integer) props.get("D3-7")), PropertyIdentifier.presentValue);
        if (!(value instanceof BinaryPV)) {
            throw new PropertyValueException(new ErrorClassAndCode(ErrorClass.property, ErrorCode.invalidDataType));
        }
        if (BinaryPV.active.equals(value)) {
            return 4;
        }

        // D3-3，为1 表示轿门关闭
        value = src.get(new ObjectIdentifier(ObjectType.binaryValue, (Integer) props.get("D3-3")), PropertyIdentifier.presentValue);
        if (!(value instanceof BinaryPV)) {
            throw new PropertyValueException(new ErrorClassAndCode(ErrorClass.property, ErrorCode.invalidDataType));
        }
        if (BinaryPV.active.equals(value)) {
            return 2;
        }

        return 1;
    }
}

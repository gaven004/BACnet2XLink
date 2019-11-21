package com.g.bacnet2xlink.converter;

import java.util.Map;

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
    public Object convert(PropertyValues src, Map<String, Object> props) throws PropertyValueException {
        Encodable value;

        // D2-6，为1表示电梯消防专用
        value = src.get(new ObjectIdentifier(ObjectType.binaryValue, (Integer) props.get("D2-6")),
                PropertyIdentifier.presentValue);
        if (!(value instanceof BinaryPV)) {
            throw new PropertyValueException(new ErrorClassAndCode(ErrorClass.property, ErrorCode.invalidDataType));
        }
        if (BinaryPV.active.equals(value)) {
            return 3;
        }

        // D2-7，为1表示电梯消防返回
        value = src.get(new ObjectIdentifier(ObjectType.binaryValue, (Integer) props.get("D2-7")),
                PropertyIdentifier.presentValue);
        if (!(value instanceof BinaryPV)) {
            throw new PropertyValueException(new ErrorClassAndCode(ErrorClass.property, ErrorCode.invalidDataType));
        }
        if (BinaryPV.active.equals(value)) {
            return 3;
        }

        // D2-3，为1表示检修中
        value = src.get(new ObjectIdentifier(ObjectType.binaryValue, (Integer) props.get("D2-3")),
                PropertyIdentifier.presentValue);
        if (!(value instanceof BinaryPV)) {
            throw new PropertyValueException(new ErrorClassAndCode(ErrorClass.property, ErrorCode.invalidDataType));
        }
        if (BinaryPV.active.equals(value)) {
            return 2;
        }

        // D2-4，为0表示电梯故障
        value = src.get(new ObjectIdentifier(ObjectType.binaryValue, (Integer) props.get("D2-4")),
                PropertyIdentifier.presentValue);
        if (!(value instanceof BinaryPV)) {
            throw new PropertyValueException(new ErrorClassAndCode(ErrorClass.property, ErrorCode.invalidDataType));
        }
        if (BinaryPV.inactive.equals(value)) {
            return 2;
        }

        // D2-5，为1表示电梯泊梯
        value = src.get(new ObjectIdentifier(ObjectType.binaryValue, (Integer) props.get("D2-5")),
                PropertyIdentifier.presentValue);
        if (!(value instanceof BinaryPV)) {
            throw new PropertyValueException(new ErrorClassAndCode(ErrorClass.property, ErrorCode.invalidDataType));
        }
        if (BinaryPV.active.equals(value)) {
            return 2;
        }

        return 1;
    }
}

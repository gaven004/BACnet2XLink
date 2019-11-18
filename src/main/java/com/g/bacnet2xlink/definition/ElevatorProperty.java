package com.g.bacnet2xlink.definition;

import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import lombok.Data;

@Data
public class ElevatorProperty {
    private String objectType;
    private int objectId;
    private ObjectIdentifier oid;

    public void setObjectType(String objectType) {
        this.objectType = objectType;
        if (objectId > 0) {
            _setOid();
        }
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
        if (objectType != null) {
            _setOid();
        }
    }

    public void _setOid() {
        this.oid = new ObjectIdentifier(ObjectType.forName(this.objectType), this.objectId);
    }
}

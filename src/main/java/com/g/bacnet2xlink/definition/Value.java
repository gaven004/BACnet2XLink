package com.g.bacnet2xlink.definition;

import com.serotonin.bacnet4j.type.Encodable;
import lombok.Data;

/**
 * 属性值
 */
@Data
public class Value {
    private String value; // 物理设备的属性值的string表示
    private Object xvalue; // 云平台属性值
    private String desc; // 说明

    // todo: 单位转换还需要增加处理
}

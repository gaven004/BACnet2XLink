package com.g.bacnet2xlink.model.elevator;

import com.g.bacnet2xlink.BitUtil;
import com.g.bacnet2xlink.exception.ValidationError;

public class Response {
    /*
        电梯运行状态I（D2 数据）

        位 内容 说明
        0 下行 为1 表示下行
        1 上行 为1 表示上行
        2 运行中 为1 表示电梯运行中
        3 检修 为1 表示检修中
        4 电梯故障 为0 表示电梯故障
        5 泊梯 为1 表示电梯泊梯
        6 消防专用 为1 表示电梯消防专用
        7 消防返回 为1 表示电梯消防返回
    * */

    private static final byte FLAG_DOWN = 1;
    private static final byte FLAG_UP = 1 << 1;
    private static final byte FLAG_IN_SERVICE = 1 << 2;
    private static final byte FLAG_INSPECTION = 1 << 3;
    private static final byte FLAG_OUT_OF_SERVICE = 1 << 4;
    private static final byte FLAG_PARKING = 1 << 5;
    private static final byte FLAG_FIREMAN_CONTROL = 1 << 6;
    private static final byte FLAG_FIRE_OPERATION = (byte) (1 << 7);

    /*
        电梯运行状态II（D3 数据）

        位 内容 说明
        0 并联正常 为1 表示电梯并联正常
        1 群管理正常 为1 表示群管理正常
        2 电源正常 为1 表示电梯电源正常
        3 轿门门锁 为1 表示轿门关闭
        4 自发电 为1 表示电梯自发电运行
        5 电梯到达 为1 表示电梯到达
        6 电梯开门 为1 表示开门中
        7 电梯关门 为1 表示关门中
    * */

    private static final byte FLAG_PARALLEL_OPERATION = 1;
    private static final byte FLAG_GROUP_MODE_IN_NORMAL = 1 << 1;
    private static final byte FLAG_POWER_IN_NORMAL = 1 << 2;
    private static final byte FLAG_CAR_DOOR_CLOSED = 1 << 3;
    private static final byte FLAG_SELF_POWER_SUPPLY = 1 << 4;
    private static final byte FLAG_STOPPED = 1 << 5;
    private static final byte FLAG_OPENING = 1 << 6;
    private static final byte FLAG_CLOSING = (byte) (1 << 7);

    /*
        电梯运行状态III（D4 数据）

        位 内容 说明
        0 地震运行 为1 时电梯地震管制运行
        1 安全装置正常 为1 时电梯安全装置正常
        2 专用运行 为1 时电梯专用运行
        3 火灾管制运行 为1 时电梯管制运行中
        4 位于门区 为1 时电梯位于门区中
        5 自救运行 为1 时电梯自救运行中
        6 A2 故障 为1 时电梯发生A2 级故障
        7 A1 故障 为1 时电梯发生A1 级故障
    * */

    /*
        电梯运行状态Ⅴ（D6 数据）

        位 内容 说明
        0 厅门门锁 为1 时厅门关闭
        1 抱闸 为1 时抱闸打开
        2 安全触板 为1 时安全触板动作
        3 光电保护 为1 时光电保护动作
        4 备用
        5 备用
        6 备用
        7 备用
    * */

    private static final byte FLAG_0 = 1;
    private static final byte FLAG_1 = 1 << 1;
    private static final byte FLAG_2 = 1 << 2;
    private static final byte FLAG_3 = 1 << 3;
    private static final byte FLAG_4 = 1 << 4;
    private static final byte FLAG_5 = 1 << 5;
    private static final byte FLAG_6 = 1 << 6;
    private static final byte FLAG_7 = (byte) (1 << 7);

    private byte startOfText; // 帧起始标志
    private byte sourceAddress; // 帧源地址
    private byte targetAddress; // 帧目标地址
    private byte data1; // 数据
    private byte data2; // 数据
    private byte data3; // 数据
    private byte data4; // 数据
    private byte data5; // 数据
    private byte data6; // 数据
    private byte summary; // 帧校验和
    private byte endOfText; // 帧结束标志

    public static Response fromByteArray(byte bytes[]) throws ValidationError {
        if (bytes.length != 11) {
            throw new ValidationError();
        }

        Response r = new Response();
        r.startOfText = bytes[0]; // 帧起始标志
        r.sourceAddress = bytes[1]; // 帧源地址
        r.targetAddress = bytes[2]; // 帧目标地址
        r.data1 = bytes[3]; // 数据
        r.data2 = bytes[4]; // 数据
        r.data3 = bytes[5]; // 数据
        r.data4 = bytes[6]; // 数据
        r.data5 = bytes[7]; // 数据
        r.data6 = bytes[8]; // 数据
        r.summary = bytes[9]; // 帧校验和
        r.endOfText = bytes[10]; // 帧结束标志

        r.validate();

        return r;
    }

    public void validate() throws ValidationError {
        if (Constants.START_OF_TEXT != startOfText
                || Constants.MASTER_ADDRESS != targetAddress
                || Constants.END_OF_TEXT != endOfText) {
            throw new ValidationError();
        }

        // SUM＝(SA+TA+D1+D2+D3+D4+D5+D6) & FFH
        int sum = Helper.summary(sourceAddress, targetAddress, data1, data2, data3, data4, data5, data6);
        if (summary != (byte) (sum & Constants.MASK)) {
            throw new ValidationError();
        }
    }

    /**
     * 楼层信息
     *
     * @return
     */
    public int getCarPosition() {
        return data1;
    }

    /*
        电梯运行状态I（D2 数据）

        位 内容 说明
        0 下行 为1 表示下行
        1 上行 为1 表示上行
        2 运行中 为1 表示电梯运行中
        3 检修 为1 表示检修中
        4 电梯故障 为0 表示电梯故障
        5 泊梯 为1 表示电梯泊梯
        6 消防专用 为1 表示电梯消防专用
        7 消防返回 为1 表示电梯消防返回
    * */

    public boolean isMovingDown() {
        return (data2 & FLAG_DOWN) != 0;
    }

    public boolean isMovingUp() {
        return (data2 & FLAG_UP) != 0;
    }

    public boolean isInService() {
        return (data2 & FLAG_IN_SERVICE) != 0;
    }

    public boolean isInInspection() {
        return (data2 & FLAG_INSPECTION) != 0;
    }

    public boolean isOutOfService() {
        return (data2 & FLAG_OUT_OF_SERVICE) != 0;
    }

    public boolean isParking() {
        return (data2 & FLAG_PARKING) != 0;
    }

    public boolean isInFiremanControl() {
        return (data2 & FLAG_FIREMAN_CONTROL) != 0;
    }

    public boolean isInFireOperation() {
        return (data2 & FLAG_FIRE_OPERATION) != 0;
    }

    /*
        电梯运行状态II（D3 数据）

        位 内容 说明
        0 并联正常 为1 表示电梯并联正常
        1 群管理正常 为1 表示群管理正常
        2 电源正常 为1 表示电梯电源正常
        3 轿门门锁 为1 表示轿门关闭
        4 自发电 为1 表示电梯自发电运行
        5 电梯到达 为1 表示电梯到达
        6 电梯开门 为1 表示开门中
        7 电梯关门 为1 表示关门中
    * */

    public boolean isInParallelOperation() {
        return (data3 & FLAG_PARALLEL_OPERATION) != 0;
    }

    public boolean isGroupModeInNormal() {
        return (data3 & FLAG_GROUP_MODE_IN_NORMAL) != 0;
    }

    public boolean isPowerInNormal() {
        return (data3 & FLAG_POWER_IN_NORMAL) != 0;
    }

    public boolean isCarDoorClosed() {
        return (data3 & FLAG_CAR_DOOR_CLOSED) != 0;
    }

    public boolean isInSelfPowerSupply() {
        return (data3 & FLAG_SELF_POWER_SUPPLY) != 0;
    }

    public boolean isStopped() {
        return (data3 & FLAG_STOPPED) != 0;
    }

    public boolean isOpening() {
        return (data3 & FLAG_OPENING) != 0;
    }

    public boolean isClosing() {
        return (data3 & FLAG_CLOSING) != 0;
    }

    @Override
    public String toString() {
        return BitUtil.toString(startOfText, sourceAddress, targetAddress, data1, data2, data3, data4, data5, data6,
                summary, endOfText);
    }
}

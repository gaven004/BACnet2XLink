package com.g.bacnet2xlink.model.elevator;

import com.g.bacnet2xlink.BitUtil;

public class Request {
    private final byte startOfText = Constants.START_OF_TEXT; // 帧起始标志
    private final byte sourceAddress = Constants.MASTER_ADDRESS; // 帧源地址
    private final byte endOfText = Constants.END_OF_TEXT; // 帧结束标志
    private byte targetAddress; // 帧目标地址
    private byte command; // 帧指令
    private byte data; // 指令数据
    private byte summary; // 帧校验和

    public static Request buildCheckRequest(byte targetAddress) {
        Request r = new Request();
        r.targetAddress = targetAddress;
        r.command = Constants.CMD_CHECK;
        r.data = 0;
        r.summary = Helper.summary(r.sourceAddress, r.targetAddress, r.command, r.data);
        return r;
    }

    public void setTargetAddress(byte targetAddress) {
        this.targetAddress = targetAddress;
    }

    public void setCommand(byte command) {
        this.command = command;
    }

    public void setData(byte data) {
        this.data = data;
    }

    /**
     * 置校验和
     * SUM＝(SA+TA+CMD+DAT) & FFH
     */
    public void sum() {
        summary = Helper.summary(sourceAddress, targetAddress, command, data);
    }

    public byte[] toByteArray() {
        return Helper.toByteArray(startOfText, sourceAddress, targetAddress, command, data, summary, endOfText);
    }

    @Override
    public String toString() {
        return BitUtil.toString(startOfText, sourceAddress, targetAddress, command, data, summary, endOfText);
    }
}

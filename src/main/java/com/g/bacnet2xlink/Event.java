package com.g.bacnet2xlink;

import java.util.List;

import lombok.Data;

/**
 * 设备事件
 * <p>
 * 云平台要求上报设备的告警及故障
 * <p>
 * BACnet协议支持Alarm and Event Services。
 * 但目前测试的控制器，只成功COV（Change Of Value）事件，
 * 客户端定时向设备订阅某一个对象的属性值变改，当发生时发送消息回客户端。
 * 设备的告警及故障，通过订阅对象的StatusFlags的COV，
 * 发生时读取StatusFlags和EventState获取具体信息
 */
@Data
public class Event {
    private String name;
    private String type;
    private String desc;

    private String objectType;
    private int objectId;
    private String covProperty;

    private List<EventMessage> messageSet;
}

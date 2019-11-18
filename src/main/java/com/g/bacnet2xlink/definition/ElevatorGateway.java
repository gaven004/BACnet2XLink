package com.g.bacnet2xlink.definition;

import lombok.Data;

import java.util.Map;

@Data
public class ElevatorGateway {
    private String host;
    private int port;
    private int targetAddress;
    private int bacnetDeviceNumber;
    private ElevatorProperty d1;
    private Map<Integer, ElevatorProperty> d2;
    private Map<Integer, ElevatorProperty> d3;
    private Map<Integer, ElevatorProperty> d4;
    private Map<Integer, ElevatorProperty> d6;
}

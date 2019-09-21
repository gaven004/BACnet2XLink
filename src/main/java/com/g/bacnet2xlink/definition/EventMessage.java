package com.g.bacnet2xlink.definition;

import lombok.Data;

@Data
public class EventMessage {
    private String value;
    private String code;
    private String message;
}

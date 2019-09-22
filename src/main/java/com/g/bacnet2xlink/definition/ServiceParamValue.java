package com.g.bacnet2xlink.definition;

import java.util.List;

import lombok.Data;

@Data
public class ServiceParamValue {
    public static final String NULL_SCR_KEY = "*";

    private List<Object> scr;
    private String dest;
}

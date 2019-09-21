package com.g.bacnet2xlink.definition;

import java.util.List;

import lombok.Data;

@Data
public class ServiceParamValue {
    private List<Object> scr;
    private String dest;
}

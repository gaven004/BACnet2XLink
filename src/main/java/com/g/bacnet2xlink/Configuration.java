package com.g.bacnet2xlink;

import lombok.Data;

import java.util.List;

@Data
public class Configuration {
    private DeviceCfg cfg;

    @Data
    static class DeviceCfg {
        private int deviceId;
        private List<ObjectCfg> objs;
    }

    @Data
    static class ObjectCfg {
        private String objectType;
        private int objectId;
    }
}

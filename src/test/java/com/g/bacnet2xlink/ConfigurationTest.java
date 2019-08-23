package com.g.bacnet2xlink;

import com.alibaba.fastjson.JSON;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.g.bacnet2xlink.Configuration.DeviceCfg;
import static com.g.bacnet2xlink.Configuration.ObjectCfg;

public class ConfigurationTest {
    @Test
    public void toJSON() {
        ObjectCfg obj1 = new ObjectCfg();
        obj1.setObjectType("analogValue");
        obj1.setObjectId(3000143);

        ObjectCfg obj2 = new ObjectCfg();
        obj2.setObjectType("binaryValue");
        obj2.setObjectId(3000147);

        DeviceCfg device = new DeviceCfg();
        device.setDeviceId(505528);
        device.setReadObjs(Arrays.asList(obj1, obj2));

        Configuration cfg = new Configuration();
        cfg.setBroadcastAddress("192.168.0.255");
        cfg.setNetworkPrefix(24);
        cfg.setDevice(device);

        System.out.println(JSON.toJSONString(cfg));
    }

    @Test
    public void fromJSON() {
        String s = "{\"broadcastAddress\":\"192.168.0.255\",\"device\":{\"deviceId\":505528,\"objs\":[{\"objectId\":3000143,\"objectType\":\"analogValue\"},{\"objectId\":3000147,\"objectType\":\"binaryValue\"}]},\"networkPrefix\":24}";
        Configuration cfg = JSON.parseObject(s, Configuration.class);
        System.out.println(cfg);
    }

    @Test
    public void fromFile() {
        try (final InputStream in = this.getClass().getResourceAsStream("/config.json")) {
            Configuration cfg = JSON.parseObject(in, StandardCharsets.UTF_8, Configuration.class);
            System.out.println(cfg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
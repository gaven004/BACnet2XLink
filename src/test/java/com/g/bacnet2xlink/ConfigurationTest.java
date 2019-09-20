package com.g.bacnet2xlink;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.alibaba.fastjson.JSON;
import org.junit.Ignore;
import org.junit.Test;


public class ConfigurationTest {
    @Test
    @Ignore
    public void toJSON() {
    }

    @Test
    @Ignore
    public void fromJSON() {
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
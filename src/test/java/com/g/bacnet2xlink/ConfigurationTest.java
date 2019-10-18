package com.g.bacnet2xlink;

import java.io.IOException;

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
        try {
            Configuration cfg = Configuration.fromResource("/config.json");
            System.out.println(cfg);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

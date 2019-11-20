package com.g.bacnet2xlink;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;


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

    @Test
    public void outElevatorBACnetProp() {
        for (int i = 3000040
             ; i < 3000079
                ; i++) {
            System.out.println(String.format("{\n" +
                    "              \"objectId\": %d,\n" +
                    "              \"objectType\": \"binary-value\"\n" +
                    "            },", i));
        }
    }

    @Test
    public void outElevatorProp() {
        for (int i = 0; i <= 7; i++) {
            System.out.println(String.format("        \"%d\": {\n" +
                    "          \"objectId\": %d,\n" +
                    "          \"objectType\": \"binary-value\"\n" +
                    "        },\n", i, 3000073 + i));
        }
    }
}

package com.g.bacnet2xlink;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cn.xlink.iot.sdk.mqtt.client.cm.XlinkCmMqttClient;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.exception.PropertyValueException;
import com.serotonin.bacnet4j.type.constructed.ObjectPropertyReference;
import com.serotonin.bacnet4j.type.enumerated.BinaryPV;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.util.PropertyValues;
import com.serotonin.bacnet4j.util.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataAcquisitionTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(DataAcquisitionTask.class);

    final HashMap<Integer, String> attributes = new HashMap() {{
        put(3000227, "status");
        put(3000228, "trip");
        put(3000229, "mode");
        put(3000238, "running_time");
    }};

    final HashMap<Integer, Integer> value_map_3000227 = new HashMap() {{
        put(0, 2);
        put(1, 1);
    }};

    final HashMap<Integer, Integer> value_map_3000228 = new HashMap() {{
        put(0, 2);
        put(1, 1);
    }};

    final HashMap<Integer, Integer> value_map_3000229 = new HashMap() {{
        put(0, 1);
        put(1, 2);
    }};

    final HashMap<Integer, HashMap<Integer, Integer>> value_maps = new HashMap() {{
        put(3000227, value_map_3000227);
        put(3000228, value_map_3000228);
        put(3000229, value_map_3000229);
    }};

    private LocalDevice ld;
    private RemoteDevice rd;
    private List<ObjectIdentifier> oids;

    // Xagent客户端
    private XlinkCmMqttClient xlinkMqttClient;
    // Xlink设备号
    private int xlinkDeviceId;


    public DataAcquisitionTask(LocalDevice ld, RemoteDevice rd, List<ObjectIdentifier> oids,
                               XlinkCmMqttClient xlinkMqttClient, int xlinkDeviceId) {
        this.ld = ld;
        this.rd = rd;
        this.oids = oids;
        this.xlinkMqttClient = xlinkMqttClient;
        this.xlinkDeviceId = xlinkDeviceId;
    }

    @Override
    public void run() {
        log.info("启动数据采集上报任务...");

        try {
            log.info("读取对象数值：");
            PropertyValues pvs = RequestUtils.readOidPresentValues(ld, rd, oids, null);

            int version = 0;
            final HashMap publishData = new HashMap();

            for (ObjectPropertyReference opr : pvs) {
                try {
                    log.info("\t{} = {}", opr.getObjectIdentifier().toString(), pvs.get(opr));
                    String key = attributes.get(opr.getObjectIdentifier().getInstanceNumber());
                    Integer value;
                    if (3000238 != opr.getObjectIdentifier().getInstanceNumber()) {
                        value =
                                value_maps.get(opr.getObjectIdentifier().getInstanceNumber()).get(((BinaryPV) pvs.get(opr)).intValue());
                    } else {
                        value = ((Float) ((Real) pvs.get(opr)).floatValue()).intValue();
                    }
                    publishData.put(key, value);
                } catch (PropertyValueException pve) {
                    log.info("\t{} PropertyValueException: {}", opr.getObjectIdentifier().toString(), pve.getMessage());
                }
            }
            xlinkMqttClient.publishAttribute(xlinkDeviceId, version, publishData, new Date());
        } catch (BACnetException e) {
            log.warn("采集上报任务异常，任务退出", e);
            throw new RuntimeException();
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("数据采集上报任务结束");
    }
}

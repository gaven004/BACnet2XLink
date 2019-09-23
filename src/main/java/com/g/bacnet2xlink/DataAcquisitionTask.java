package com.g.bacnet2xlink;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cn.xlink.iot.sdk.mqtt.client.cm.XlinkCmMqttClient;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.exception.BACnetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.g.bacnet2xlink.definition.Device;
import com.g.bacnet2xlink.definition.Product;
import com.g.bacnet2xlink.exception.UnknownDevice;
import com.g.bacnet2xlink.exception.UnknownProperty;
import com.g.bacnet2xlink.exception.UnknownValue;

public class DataAcquisitionTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(DataAcquisitionTask.class);

    private static int version = 0; // 物模型的版本号

    private LocalDevice ld;
    private RemoteDevice rd;

    private Configuration cfg;
    private Context context;

    public DataAcquisitionTask(LocalDevice ld, RemoteDevice rd, Configuration cfg, Context context) {
        this.ld = ld;
        this.rd = rd;
        this.cfg = cfg;
        this.context = context;
    }

    @Override
    public void run() {
        log.info("启动数据采集上报任务...");

        for (Product product : cfg.getProducts()) {
            XlinkCmMqttClient xlinkMqttClient = context.getXlinkClient(product.getId());

            for (Device device : product.getDevices()) {
                log.info("读取设备[id: {}, mac: {}]数值：", device.getXDeviceId(), device.getMac());

                // 构建上报数据
                Map<String, Object> attributes = new HashMap();
                try {
                    DataAcquisitionHelper.readPresentValues(ld, rd, device, attributes, log);
                    log.info("上报设备数据：{}", attributes);
                    xlinkMqttClient.publishAttribute(device.getXDeviceId(), version, attributes, new Date());
                } catch (UnknownDevice | UnknownProperty | UnknownValue unknown) {
                    log.warn(unknown.getMessage());
                } catch (BACnetException e) {
                    log.warn("采集上报任务异常，任务退出", e);
                    throw new RuntimeException();
                } catch (Exception e) {
                    log.warn("读物理设备属性错误", e);
                }
            }
        }

        log.info("数据采集上报任务结束");
    }
}

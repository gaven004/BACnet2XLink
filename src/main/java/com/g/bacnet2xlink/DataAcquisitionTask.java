package com.g.bacnet2xlink;

import cn.xlink.iot.sdk.mqtt.client.cm.XlinkCmMqttClient;
import com.g.bacnet2xlink.definition.Device;
import com.g.bacnet2xlink.definition.Event;
import com.g.bacnet2xlink.definition.EventMessage;
import com.g.bacnet2xlink.definition.Product;
import com.g.bacnet2xlink.exception.UnknownDevice;
import com.g.bacnet2xlink.exception.UnknownProperty;
import com.g.bacnet2xlink.exception.UnknownValue;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.exception.BACnetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DataAcquisitionTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(DataAcquisitionTask.class);

    private Configuration cfg;
    private Context context;

    public DataAcquisitionTask(Configuration cfg, Context context) {
        this.cfg = cfg;
        this.context = context;
    }

    @Override
    public void run() {
        log.info("启动数据采集上报任务...");

        boolean error = false;

        XlinkCmMqttClient xlinkMqttClient = context.getXlinkCmMqttClient();
        LocalDevice ld = context.getLocalDevice();

        for (Product product : cfg.getProducts()) {
            for (Device device : product.getDevices()) {
                log.info("读取设备[id: {}, mac: {}]数值：", device.getXDeviceId(), device.getMac());

                // 构建上报数据
                Map<String, Object> attributes = new HashMap();
                List<EventMessage> events = new ArrayList<>();
                try {
                    /*
                     * 将每一个设备的上报，作为独立的任务
                     * 好处是其中一个出错，也不干扰其它
                     * 但遇到断网等整体异常的情况，则会在所有设备上报失败后才会重新初始化
                     */
                    RemoteDevice rd = context.getRemoteDevice(device.getRemoteDeviceNumber());
                    DataAcquisitionHelper.readPresentValues(context, ld, rd, device, attributes, events, log);
                    log.info("上报设备数据：{}", attributes);
                    xlinkMqttClient.publishAttribute(device.getXDeviceId(), cfg.getVersion(), attributes, new Date());
                    // 处理自产生事件
                    if (!events.isEmpty()) {
                        for(EventMessage event : events) {
                            Map<String, Object> pushData = new HashMap<>(); // 事件内容
                            pushData.put("code", event.getCode());
                            pushData.put("message", event.getMessage());
                            log.info("发布事件[deviceId: {}, event: {}, data: {}]", device.getXDeviceId(), event.getType(), pushData);
                            xlinkMqttClient.publishEvent(device.getXDeviceId(), cfg.getVersion(), event.getType(), pushData, new Date());
                        }
                    }
                } catch (UnknownDevice | UnknownProperty | UnknownValue unknown) {
                    log.warn(unknown.getMessage());
                } catch (BACnetException e) {
                    error = true;
                    log.warn("采集任务异常", e);
                } catch (Exception e) {
                    error = true;
                    log.warn("上报任务错误", e);
                }
            }
        }

        if (error) {
            // 抛出异常，通知主线程重新初始化
            throw new RuntimeException();
        }

        log.info("数据采集上报任务结束");

    }
}

package com.g.bacnet2xlink;

import cn.xlink.iot.sdk.mqtt.client.cm.XlinkCmMqttClient;
import com.g.bacnet2xlink.definition.Device;
import com.g.bacnet2xlink.definition.Product;
import com.g.bacnet2xlink.exception.UnknownDevice;
import com.g.bacnet2xlink.exception.UnknownProperty;
import com.g.bacnet2xlink.exception.UnknownValue;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.exception.BACnetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DataAcquisitionTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(DataAcquisitionTask.class);

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

        boolean error = false;

        XlinkCmMqttClient xlinkMqttClient = context.getXlinkCmMqttClient();

        for (Product product : cfg.getProducts()) {
            for (Device device : product.getDevices()) {
                log.info("读取设备[id: {}, mac: {}]数值：", device.getXDeviceId(), device.getMac());

                // 构建上报数据
                Map<String, Object> attributes = new HashMap();
                try {
                    /*
                     * 将每一个设备的上报，作为独立的任务
                     * 好处是其中一个出错，也不干扰其它
                     * 但遇到断网等整体异常的情况，则会在所有设备上报失败后才会重新初始化
                     */
                    DataAcquisitionHelper.readPresentValues(context, ld, rd, device, attributes, log);
                    log.info("上报设备数据：{}", attributes);
                    xlinkMqttClient.publishAttribute(device.getXDeviceId(), cfg.getVersion(), attributes, new Date());
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

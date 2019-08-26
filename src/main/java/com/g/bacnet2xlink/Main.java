package com.g.bacnet2xlink;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkBuilder;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.type.constructed.ObjectPropertyReference;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.PropertyReferences;
import com.serotonin.bacnet4j.util.PropertyValues;
import com.serotonin.bacnet4j.util.RequestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static final String ConfigFile = "/config.json";

    private static Configuration cfg = null;
    private static List<ObjectIdentifier> readOids = new ArrayList<>();

    private LocalDevice localDevice = null;
    private RemoteDevice remoteDevice = null;

    private ScheduledExecutorService executor;

    public static void main(String[] args) {
        log.info("启动系统...");

        // 读系统配置
        config();

        // 初始化
        Main me = new Main();

        // 注册退出钩子
        me.addShutdownHook();

        // 运行任务
        me.runTask();
    }

    private static void config() {
        try {
            log.info("读系统配置...");
            cfg = Configuration.fromResource(ConfigFile);
        } catch (Exception e) {
            log.error("从资源文件恢复系统配置失败，请检查资源文件是否在指定路径，并且格式正确", e);
            log.error("退出系统！！！");
            System.exit(1);
        }

        try {
            for (Configuration.ObjectCfg obj : cfg.getDevice().getReadObjs()) {
                readOids.add(new ObjectIdentifier(ObjectType.forName(obj.getObjectType()), obj.getObjectId()));
            }
        } catch (Exception e) {
            log.error("资源文件格式错误", e);
            log.error("退出系统！！！");
            System.exit(1);
        }
    }

    /**
     * 获取远程设备的扩展属性
     *
     * @param localDevice
     * @param remoteDevice
     * @throws BACnetException
     */
    private static void getExtendedDeviceInformation(LocalDevice localDevice, RemoteDevice remoteDevice) throws BACnetException {
        log.info("获取远程设备的扩展属性");
        PropertyReferences refs = new PropertyReferences();
        refs.add(remoteDevice.getObjectIdentifier(), PropertyIdentifier.required);
        PropertyValues pvs = RequestUtils.readProperties(localDevice, remoteDevice, refs, true, null);
        for (ObjectPropertyReference opr : pvs) {
            if (remoteDevice.getObjectIdentifier().equals(opr.getObjectIdentifier())) {
                remoteDevice.setDeviceProperty(opr.getPropertyIdentifier(), pvs.getNoErrorCheck(opr));
                log.info(String.format("\t%s = %s", opr.getPropertyIdentifier().toString(), pvs.getNoErrorCheck(opr)));
            }
        }
    }

    private void initLocalDevice() {
        IpNetworkBuilder builder;
        IpNetwork network;
        try {
            log.info("初始化localDevice...");
            builder = new IpNetworkBuilder();
            if (StringUtils.isNotBlank(cfg.getLocalAddress())) {
                builder.withLocalBindAddress(cfg.getLocalAddress());
            }
            if (cfg.getLocalPort() != null && cfg.getLocalPort() != 0) {
                builder.withPort(cfg.getLocalPort());
            }
            builder.withBroadcast("192.168.0.255", 24);
            network = builder.build();
            localDevice = new LocalDevice(1, new DefaultTransport(network));
            localDevice.initialize();
            log.info("初始化localDevice成功：{}", localDevice);
        } catch (Exception e) {
            log.error("初始化localDevice失败", e);
            log.error("退出系统！！！");
            System.exit(1);
        }
    }

    private void initRemoteDevice() {
        while (remoteDevice == null) {
            try {
                log.info("连接remoteDevice...");
                remoteDevice = localDevice.getRemoteDeviceBlocking(cfg.getDevice().getDeviceId(), 10000);
                getExtendedDeviceInformation(localDevice, remoteDevice);
                log.info("连接remoteDevice成功");
            } catch (BACnetException e) {
                log.error("连接remoteDevice失败", e);
                log.error("清空localDevice缓存，1分钟后重试");
                localDevice.clearRemoteDevices();
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    private void initThreadPool() {
        executor = Executors.newScheduledThreadPool(2);
    }

    private void runTask() {
        while (true) {
            initLocalDevice();
            initRemoteDevice();
            initThreadPool();

            // 启动监听线程，接收控制指令

            // 启动上报线程，采集并上报数据
            DataAcquisitionTask dat = new DataAcquisitionTask(localDevice, remoteDevice, readOids);
            final ScheduledFuture<?> daf = executor.scheduleWithFixedDelay(dat,
                    cfg.getDevice().getDataSubmitInterval(), cfg.getDevice().getDataSubmitInterval(), TimeUnit.SECONDS);

            while (!daf.isDone() && !daf.isCancelled()) {
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                }
            }

            // 任务中有异常抛出，则任务结束，重新初始化设备，并运行任务
            renew();
        }
    }

    void shutdown() {
        log.info("关闭定时任务和设备");
        if (executor != null) {
            executor.shutdown();
            try {
                executor.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
            }
        }
        if (localDevice != null) {
            localDevice.terminate();
        }
    }

    private void release() {
        executor = null;
        remoteDevice = null;
        localDevice = null;
    }

    void renew() {
        log.warn("重新初始化设备，并运行任务");
        shutdown();
        release();
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
                release();
                log.info("退出系统！！！");
            }
        });
    }
}

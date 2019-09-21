package com.g.bacnet2xlink;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cn.xlink.iot.sdk.XlinkMqttBuilderParams;
import cn.xlink.iot.sdk.mqtt.client.cm.XlinkCmMqttClient;
import cn.xlink.iot.sdk.mqtt.client.subscribe.message.GetAttributeResponse;
import cn.xlink.iot.sdk.mqtt.client.subscribe.message.ServiceInvokeResponse;
import cn.xlink.iot.sdk.mqtt.client.subscribe.message.SetAttributeResponse;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.event.DeviceEventAdapter;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkBuilder;
import com.serotonin.bacnet4j.service.confirmed.SubscribeCOVPropertyRequest;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.type.constructed.ObjectPropertyReference;
import com.serotonin.bacnet4j.type.constructed.PropertyReference;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import com.serotonin.bacnet4j.util.PropertyReferences;
import com.serotonin.bacnet4j.util.PropertyValues;
import com.serotonin.bacnet4j.util.RequestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xlink.cm.message.DeviceLoginResultMessage;
import xlink.cm.message.type.DeviceLoginRetCodeType;

import com.g.bacnet2xlink.definition.Configuration;

public class Main {
    /**
     * Xlink配置参数
     */
    //授权证书ID（CID）
    public final static String CERT_ID = "5d7a1f2c6fb0bd3fa7566162";
    //授权证书密钥（CKEY）
    public final static String CERT_KEY = "77e9fad9-2987-4c93-8170-da775611c437";
    //云端CM服务器地址
    public final static String DEVICE_ENDPOINT = "mqtt.xlink.cn:1883";
    // 连接器类型，用于标识连接器类别
    public final static String CONNECTOR_TYPE = "5764610525098608128";
    // 产品ID
//    public final static String PRODUCT_ID = "160002baf43a03e9160002baf43aaa01"; // 江森-水泵
    public final static String PRODUCT_ID = "160042baf43b03e9160042baf43b7801"; // 江森-风机
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final String ConfigFile = "/config.json";
    private static final int subscriberLifetime = 3600; // seconds
    private final static String MAC = "500200"; // 测试风格
    private static Configuration cfg = null;
    private static List<ObjectIdentifier> readOids = new ArrayList<>();
    private static HashSet<ObjectIdentifier> monitorOids = new HashSet<>();
    // Xagent客户端
    private XlinkCmMqttClient xlinkMqttClient;
    // Xlink设备号
    private int xlinkDeviceId;

    private LocalDevice localDevice = null;
    private RemoteDevice remoteDevice = null;

    private ScheduledExecutorService executor;

    public static void main(String[] args) {
        log.info("启动系统...");

        // 读系统配置
        config();

        // 初始化
        Main me = new Main();

        me.initXlinkCmMqttClient();

        me.loginXlink();

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
//            for (Configuration.ObjectCfg obj : cfg.getDevice().getReadObjs()) {
//                ObjectIdentifier oid = new ObjectIdentifier(ObjectType.forName(obj.getObjectType()), obj.getObjectId());
//                readOids.add(oid);
//                monitorOids.add(oid);
//            }
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

    private void initXlinkCmMqttClient() {
        log.info("初始化xlinkMqttClient...");

        // 构造器配置
        xlinkMqttClient = XlinkMqttBuilderParams.builder()
                .certId(CERT_ID) //配置授权证书ID
                .certKey(CERT_KEY) //配置授权证书密钥
                .endpoint(DEVICE_ENDPOINT)//云端CM服务器地址
                .connectorType(CONNECTOR_TYPE)//配置连接器类型
                .addSubscribeServiceName(PRODUCT_ID, "start", "stop")//订阅物模型服务处理
                .build(); //构造XlinkMqttBuilderParams实例

        // 建立连接及认证
        try {
            xlinkMqttClient.start();
        } catch (Exception e) {
            log.error("建立连接及认证失败", e);
            log.error("退出系统！！！");
            System.exit(1);
        }
    }

    private void loginXlink() {
        // 在平台登录上线，并得到返回
        log.info("平台登录上线");
        DeviceLoginResultMessage deviceLoginResultMessage = null;
        try {
            deviceLoginResultMessage = xlinkMqttClient.deviceLogin(PRODUCT_ID, MAC).get();
        } catch (Exception e) {
            log.error("平台登录失败", e);
        }

        // 得到返回结果
        if (deviceLoginResultMessage.getRetCode() == DeviceLoginRetCodeType.SUCCESS) {
            // 上线成功，得到平台唯一的设备ID、在物模型下，可以当做thing id使用
            xlinkDeviceId = deviceLoginResultMessage.getDeviceId();
            log.info("上线成功，得到平台唯一的设备ID：{}", xlinkDeviceId);
        } else {
            // 上线失败，从RetCode读取错误信息及类型
            DeviceLoginRetCodeType retCode = deviceLoginResultMessage.getRetCode();
            log.error("上线失败, error code: {}, detail: {}", retCode.type(), retCode.name());
        }
    }

    /**
     * 监听服务端的属性设置请求，并应答服务端
     */
    private void setAttributeCallback() {
        //监听服务端属性设置回调方法，注意：多个设备ID的属性设置回调方法实现可以为同一个
        xlinkMqttClient.setSetAttributeHandler(request -> {
            //服务端设置下发的属性信息
            Map<String, Object> attribute = request.getAttributes();
            //服务端设置下发的设备ID
            int setDeviceId = request.getDeviceId();

            //自定义代码

            //构建应答包错误码
            String code = "200";
            //构建应答包
            SetAttributeResponse response = new SetAttributeResponse(code);
            //通过方法返回值进行应答，如方法返回值null，则不会应答。
            return response;
        });
    }

    /**
     * 监听服务端的属性获取请求，并应答服务端
     */
    private void getAttributeCallback() {
        //监听服务端属性获取回调方法，注意：多个设备ID的属性获取回调方法实现可以为同一个
        xlinkMqttClient.setGetAttributeHandler(reqeust -> {
            //得到服务端属性获取的设备ID
            int getDeviceId = reqeust.getDeviceId();

            //自定义代码

            //构建应答的信息属性
            Map<String, Object> attributes = new HashMap() {{
                put("f1", 1);
                put("f2", "str..");
            }};
            //构建应答包错误码
            String code = "200";
            //构建应答包
            GetAttributeResponse response = new GetAttributeResponse(code, attributes);
            //通过方法返回值进行应答，如方法返回值为null，则不会应答
            return response;
        });
    }

    /**
     * 监听服务端的服务调用下发，并应答服务端
     */
    private void serviceInvokeCallback() {
        //物模型中的服务名
        //监听服务端服务调用的回调方法，注意：多个设备ID、多个不同服务的服务调用回调方法实现可以为同一个。
        xlinkMqttClient.setServiceInvokeHandler(request -> {
            //得到服务调用的设备ID
            int invokeDeviceId = request.getDeviceId();
            //得到服务调用的服务名
            String invokeServiceName = request.getServiceName();
            //得到服务调用的输入参数
            Map<String, Object> input = request.getInput();

            //自定义代码
            switch (invokeServiceName) {
                case "open_door": {
                    //...
                    break;
                }
                case "close_door": {
                    //...
                    break;
                }
            }

            //构建应答包错误码
            String code = "200";
            //构建应答包输出参数
            Map<String, Object> output = new HashMap() {{
                put("f1", 4);
                put("f2", "str");
            }};
            //构建应答包
            ServiceInvokeResponse serviceInvokeResponse = new ServiceInvokeResponse(code, output);
            //通过方法返回值进行应答，如方法返回值为null，则不会应答
            return serviceInvokeResponse;
        });
    }

    private void initLocalDevice() {
        IpNetworkBuilder builder;
        IpNetwork network;
        int deviceNumber = ObjectIdentifier.UNINITIALIZED;
        try {
            log.info("初始化localDevice...");
            if (cfg.getLocalDeviceNumber() != null) {
                deviceNumber = cfg.getLocalDeviceNumber();
            }
            builder = new IpNetworkBuilder();
            if (StringUtils.isNotBlank(cfg.getLocalAddress())) {
                builder.withLocalBindAddress(cfg.getLocalAddress());
            }
            if (cfg.getLocalPort() != null && cfg.getLocalPort() != 0) {
                builder.withPort(cfg.getLocalPort());
            }
            builder.withBroadcast("192.168.0.255", 24);
            network = builder.build();
            localDevice = new LocalDevice(deviceNumber, new DefaultTransport(network));
            localDevice.initialize();
            final DeviceEventAdapter listener = new MyDeviceEventAdapter();
            localDevice.getEventHandler().addListener(listener);
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
//                remoteDevice = localDevice.getRemoteDeviceBlocking(cfg.getDevice().getDeviceId(), 10000);
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
        executor = Executors.newScheduledThreadPool(4);
    }

    private void runTask() {
        while (true) {
            initLocalDevice();
            initRemoteDevice();
            initThreadPool();

            // 启动COV订阅线程
//            SubscribeCOVTask sct = new SubscribeCOVTask(localDevice, remoteDevice, readOids,
//                    new UnsignedInteger(subscriberLifetime * 2));
//            final ScheduledFuture<?> scf = executor.scheduleAtFixedRate(sct, 0, subscriberLifetime, TimeUnit.SECONDS);

            // 启动监听线程，接收控制指令

            // 启动上报线程，采集并上报数据
            DataAcquisitionTask dat = new DataAcquisitionTask(localDevice, remoteDevice, readOids, xlinkMqttClient, xlinkDeviceId);
//            final ScheduledFuture<?> daf = executor.scheduleWithFixedDelay(dat,
//                    cfg.getDevice().getDataSubmitInterval(), cfg.getDevice().getDataSubmitInterval(), TimeUnit.SECONDS);
//
//            while (!daf.isDone() && !daf.isCancelled()) {
//                try {
//                    Thread.sleep(60000);
//                } catch (InterruptedException e) {
//                }
//            }

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

    private void unsubscribeCOVEvent() {
        log.info("取消COV订阅...");

        UnsignedInteger subscriberProcessIdentifier = new UnsignedInteger(localDevice.getInstanceNumber());
        for (ObjectIdentifier oid : readOids) {
            try {
                SubscribeCOVPropertyRequest req = new SubscribeCOVPropertyRequest(subscriberProcessIdentifier,
                        oid, null, null, new PropertyReference(PropertyIdentifier.statusFlags), null);
                localDevice.send(remoteDevice, req).get();
            } catch (BACnetException e) {
                log.warn("取消COV订阅异常，oid={}, exception={}", oid, e.getMessage());
            }
        }

        log.info("取消COV订阅任务结束");
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                unsubscribeCOVEvent();
                shutdown();
                release();
                log.info("退出系统！！！");
            }
        });
    }
}

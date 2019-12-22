package com.g.bacnet2xlink;

import cn.xlink.iot.sdk.XlinkMqttBuilderParams;
import cn.xlink.iot.sdk.mqtt.client.cm.XlinkCmMqttClient;
import cn.xlink.iot.sdk.mqtt.client.subscribe.message.GetAttributeResponse;
import cn.xlink.iot.sdk.mqtt.client.subscribe.message.ServiceInvokeResponse;
import cn.xlink.iot.sdk.mqtt.client.subscribe.message.SetAttributeResponse;
import com.g.bacnet2xlink.definition.*;
import com.g.bacnet2xlink.exception.*;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.event.DeviceEventAdapter;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkBuilder;
import com.serotonin.bacnet4j.service.confirmed.SubscribeCOVPropertyRequest;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.ObjectPropertyReference;
import com.serotonin.bacnet4j.type.constructed.PropertyReference;
import com.serotonin.bacnet4j.type.enumerated.BinaryPV;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import com.serotonin.bacnet4j.util.PropertyReferences;
import com.serotonin.bacnet4j.util.PropertyValues;
import com.serotonin.bacnet4j.util.RequestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xlink.cm.message.DeviceLoginResultMessage;
import xlink.cm.message.type.DeviceLoginRetCodeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static final String ConfigFile = "/config.json";

    private static final int subscriberLifetime = 3600; // seconds

    private static Configuration cfg = null;

    private static Context context = new Context();

    private static ScheduledExecutorService executor;

    private static volatile boolean running = true;

    public static void main(String[] args) {
        log.info("");
        log.info("");
        log.info("启动系统...");

        // 读系统配置
        config();

        // 注册退出钩子
        addShutdownHook();

        buildConverters();

        initLocalDevice();
        initRemoteDevice();
        initThreadPool();

        initXlinkCmMqttClient();
        loginXlink();

        setReconnectHandler();
        setAttributeCallback();
        getAttributeCallback();
        serviceInvokeCallback();

        // 运行任务
        runTask();
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

    private static void buildConverters() {
        try {
            log.info("构建转换器...");
            for (Product product : cfg.getProducts()) {
                if (product.getProperties() != null) {
                    for (Property property : product.getProperties()) {
                        String clzName = property.getValueConverter();
                        if (clzName != null && clzName.trim().length() > 0 && context.getConverter(clzName) == null) {
                            Object converter = Class.forName(clzName).newInstance();
                            context.addConverter(clzName, converter);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("构建转换器失败，请配置是否正确", e);
            log.error("退出系统！！！");
            System.exit(1);
        }

    }

    private static void initXlinkCmMqttClient() {
        log.info("初始化xlinkMqttClient...");

        // 构造器配置
        XlinkMqttBuilderParams.XlinkMqttBuilderParamsBuilder builder = XlinkMqttBuilderParams.builder()
                .certId(cfg.getCertId()) // 配置授权证书ID
                .certKey(cfg.getCertKey()) // 配置授权证书密钥
                .endpoint(cfg.getDeviceEndpoint()) // 云端CM服务器地址
                .isOpenLog(true)
                .connectorType(cfg.getConnectorType()); // 配置连接器类型

        for (Product product : cfg.getProducts()) {
            String[] serviceNames = null;
            if (product.getServices() != null && product.getServices().size() > 0) {
                serviceNames = new String[product.getServices().size()];
                int i = 0;
                for (Service service : product.getServices()) {
                    serviceNames[i++] = service.getName();
                }
            }

            if (serviceNames != null) {
                builder.addSubscribeServiceName(product.getId(), serviceNames); // 订阅物模型服务处理
            }
        }

        XlinkCmMqttClient xlinkMqttClient = builder.build(); // 构造XlinkMqttBuilderParams实例

        // 建立连接及认证
        boolean init = false;

        while (!init) {
            try {
                xlinkMqttClient.start();
                context.setXlinkCmMqttClient(xlinkMqttClient);
                init = true;
                log.info("建立Xagent客户端成功");
            } catch (Exception e) {
                context.setXlinkCmMqttClient(null);
                log.error(String.format("建立Xagent客户端失败"), e);
                log.warn("1分钟后，重启Xagent客户端...");
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    private static void loginXlink() {
        // 在平台登录上线，并得到返回
        log.info("设备平台上线...");

        XlinkCmMqttClient xlinkMqttClient = context.getXlinkCmMqttClient();

        for (Product product : cfg.getProducts()) {
            if (xlinkMqttClient != null) {
                for (Device device : product.getDevices()) {
                    DeviceLoginResultMessage deviceLoginResultMessage = null;
                    try {
                        deviceLoginResultMessage = xlinkMqttClient.deviceLogin(product.getId(), device.getMac()).get();
                    } catch (Exception e) {
                        log.error(String.format("{}设备[mac:{}]上线失败", product.getName(), device.getMac()), e);
                    }

                    // 得到返回结果
                    if (deviceLoginResultMessage != null) {
                        if (deviceLoginResultMessage.getRetCode() == DeviceLoginRetCodeType.SUCCESS) {
                            // 上线成功，得到平台唯一的设备ID、在物模型下，可以当做thing id使用
                            int xlinkDeviceId = deviceLoginResultMessage.getDeviceId();
                            device.setXDeviceId(xlinkDeviceId);
                            context.addDevice(device);
                            log.info("{}设备[mac:{}]上线成功，得到平台唯一的设备ID：{}", product.getName(), device.getMac(), xlinkDeviceId);
                        } else {
                            // 上线失败，从RetCode读取错误信息及类型
                            DeviceLoginRetCodeType retCode = deviceLoginResultMessage.getRetCode();
                            log.error("{}设备[mac:{}]上线失败, error code: {}, detail: {}",
                                    product.getName(), device.getMac(), retCode.type(), retCode.name());
                        }
                    }
                }
            }
        }
    }

    /**
     * 监听服务端的属性设置请求，并应答服务端
     * <p>
     * 目前不支持对协议中的设备属性写入
     */
    private static void setAttributeCallback() {
        log.info("监听服务端的属性设置请求");

        XlinkCmMqttClient xlinkMqttClient = context.getXlinkCmMqttClient();

        if (xlinkMqttClient != null) {
            //监听服务端属性设置回调方法，注意：多个设备ID的属性设置回调方法实现可以为同一个
            xlinkMqttClient.setSetAttributeHandler(request -> {
                log.info("云平台远程设置物理设备属性，{}", request);

                //构建应答包错误码
                String code = "501";
                //构建应答包
                SetAttributeResponse response = new SetAttributeResponse(code);

                log.info("目前不支持对协议中的设备属性写入，返回：{}", response);

                //通过方法返回值进行应答，如方法返回值null，则不会应答。
                return response;
            });
        }
    }

    /**
     * SDK与云端断开并重连时，回调方法设置
     */
    private static void setReconnectHandler() {
        log.info("设置SDK与云端断开并重连时回调方法");
        XlinkCmMqttClient xlinkMqttClient = context.getXlinkCmMqttClient();

        if (xlinkMqttClient != null) {
            xlinkMqttClient.setReconnectHandler(() -> {
                log.warn("SDK与云端重连！！！");
                loginXlink();
            });
        }
    }

    /**
     * 监听服务端的属性获取请求，并应答服务端
     */
    private static void getAttributeCallback() {
        log.info("监听服务端的属性获取请求");

        XlinkCmMqttClient xlinkMqttClient = context.getXlinkCmMqttClient();

        if (xlinkMqttClient != null) {
            //监听服务端属性获取回调方法，注意：多个设备ID的属性获取回调方法实现可以为同一个
            xlinkMqttClient.setGetAttributeHandler(request -> {
                log.info("云平台远程读取物理设备属性，{}", request);

                //得到服务端属性获取的设备ID
                int deviceId = request.getDeviceId();

                //构建应答的信息属性
                Map<String, Object> attributes = new HashMap<>();
                List<EventMessage> events = new ArrayList<>();
                //构建应答码
                String code = null;

                try {
                    Device device = context.getDevice(deviceId);
                    log.info("读取设备[{}]数值：", device.getMac());

                    LocalDevice localDevice = context.getLocalDevice();
                    RemoteDevice remoteDevice = context.getRemoteDevice(device.getRemoteDeviceNumber());
                    DataAcquisitionHelper.readPresentValues(context, localDevice, remoteDevice, device, attributes, events, log);
                    code = "200";
                } catch (UnknownDevice unknown) {
                    log.warn(unknown.getMessage());
                    code = "400";
                } catch (UnknownProperty | UnknownValue unknown) {
                    log.warn(unknown.getMessage());
                    code = "500";
                } catch (Exception e) {
                    log.warn("读物理设备属性错误", e);
                    code = "500";
                }

                //构建应答包
                GetAttributeResponse response = new GetAttributeResponse(code, attributes);

                log.info("读取物理设备属性返回：{}", response);

                //通过方法返回值进行应答，如方法返回值为null，则不会应答
                return response;
            });
        }
    }

    /**
     * 监听服务端的服务调用下发，并应答服务端
     */
    private static void serviceInvokeCallback() {
        log.info("监听服务端的服务调用下发");

        XlinkCmMqttClient xlinkMqttClient = context.getXlinkCmMqttClient();

        if (xlinkMqttClient != null) {
            //监听服务端服务调用的回调方法，注意：多个设备ID、多个不同服务的服务调用回调方法实现可以为同一个。
            xlinkMqttClient.setServiceInvokeHandler(request -> {
                log.info("云平台远程服务调用，{}", request);

                //得到服务调用的设备ID
                int deviceId = request.getDeviceId();
                //得到服务调用的服务名
                String serviceName = request.getServiceName();
                //得到服务调用的输入参数
                Map<String, Object> input = request.getInput();

                //构建应答码
                String code = "200";
                //构建应答包输出参数
                Map<String, Object> output = new HashMap<>();

                try {
                    Device device = context.getDevice(deviceId);
                    Service service = device.getServiceX(serviceName);
                    String type = service.getDestParamType();
                    Encodable converted = null;

                    if (service.getValueConverter() != null && service.getValueConverter().trim().length() > 0) {

                    } else if (service.getValueSet() != null && service.getValueSet().size() > 0) {
                        String s = null;
                        if (service.getSrcParam() == null || service.getSrcParam().size() == 0) {
                            s = service.getValueX(null).getDest();
                        } else {

                        }

                        if (type.equals("BinaryPV")) {
                            converted = BinaryPV.forName(s);
                        }
                    } else {

                    }

                    log.info("写物理设备[{}]对象的Present Value属性值：{}", service.getOid(), converted);
                    LocalDevice localDevice = context.getLocalDevice();
                    RemoteDevice remoteDevice = context.getRemoteDevice(device.getRemoteDeviceNumber());
                    RequestUtils.writePresentValue(localDevice, remoteDevice, service.getOid(), converted);

                    code = "200";
                    output.put("code", "SUCCESS");
                    output.put("message", "服务调用成功");
                } catch (UnknownDevice | UnknownService | UnknownValue unknown) {
                    log.warn(unknown.getMessage());
                    code = "400";
                    output.put("code", unknown.getCode());
                    output.put("message", "服务调用失败，" + unknown.getMessage());
                } catch (Exception e) {
                    log.warn("写物理设备属性错误", e);
                    code = "500";
                    output.put("code", "ERROR");
                    output.put("message", "服务调用失败，写物理设备属性错误");
                }

                //构建应答包
                ServiceInvokeResponse serviceInvokeResponse = new ServiceInvokeResponse(code, output);

                log.info("远程服务调用返回：{}", serviceInvokeResponse);

                //通过方法返回值进行应答，如方法返回值为null，则不会应答
                return serviceInvokeResponse;
            });
        }
    }

    private static void initLocalDevice() {
        IpNetworkBuilder builder;
        IpNetwork network;
        LocalDevice localDevice = null;
        int deviceNumber = ObjectIdentifier.UNINITIALIZED;

        while (localDevice == null) {
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
                builder.withBroadcast(cfg.getBroadcastAddress(), cfg.getNetworkPrefix());
                network = builder.build();

                localDevice = new LocalDevice(deviceNumber, new DefaultTransport(network));
                localDevice.initialize();

                context.setLocalDevice(localDevice);

                final DeviceEventAdapter listener = new MyDeviceEventAdapter(cfg, context);
                localDevice.getEventHandler().addListener(listener);

                log.info("初始化localDevice成功：{}", localDevice);
            } catch (Exception e) {
                log.error("初始化localDevice失败", e);
                log.error("1分钟后重试");
                if (localDevice != null) {
                    localDevice.terminate();
                    localDevice = null;
                }
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    private static void initRemoteDevice() {
        LocalDevice localDevice = context.getLocalDevice();

        for (Integer deviceNumber : cfg.getRemoteDevices()) {
            try {
                log.info("连接remoteDevice[{}]...", deviceNumber);
                RemoteDevice remoteDevice = localDevice.getRemoteDeviceBlocking(deviceNumber, 60000);
                getExtendedDeviceInformation(localDevice, remoteDevice);
                context.addRemoteDevice(remoteDevice);
                log.info("连接remoteDevice[{}]成功", deviceNumber);
            } catch (BACnetException e) {
                log.error(String.format("连接remoteDevice[%d]失败", deviceNumber), e);
                localDevice.removeCachedRemoteDevice(deviceNumber);
            }
        }
    }

    private static void initThreadPool() {
        executor = Executors.newScheduledThreadPool(4);
    }

    private static void runTask() {
        while (running) {
            // 启动COV订阅线程
            SubscribeCOVTask sct = new SubscribeCOVTask(cfg, context, new UnsignedInteger(subscriberLifetime * 2));
            final ScheduledFuture<?> scf = executor.scheduleAtFixedRate(sct, 0, subscriberLifetime, TimeUnit.SECONDS);

            ElevatorTransformer et = new ElevatorTransformer(cfg, context);
            final ScheduledFuture<?> etf = executor.scheduleAtFixedRate(et, cfg.getDataSubmitInterval() / 2,
                    cfg.getDataSubmitInterval(), TimeUnit.SECONDS);

            // 启动上报线程，采集并上报数据
            DataAcquisitionTask dat = new DataAcquisitionTask(cfg, context);
            final ScheduledFuture<?> daf = executor.scheduleWithFixedDelay(dat,
                    cfg.getDataSubmitInterval(), cfg.getDataSubmitInterval(), TimeUnit.SECONDS);

            while (running && !daf.isDone() && !daf.isCancelled()) {
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                }
            }

            if (running) {
                // 任务中有异常抛出，则任务结束，重新初始化设备，并运行任务
                renew();
            }
        }
    }

    private static void shutdown() {
        log.info("关闭定时任务和设备");
        if (executor != null) {
            executor.shutdown();
            try {
                executor.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
            }
        }
        if (context.getLocalDevice() != null) {
            context.getLocalDevice().terminate();
        }
    }

    private static void release() {
        executor = null;
        context.clearRemoteDeviceMap();
        context.setLocalDevice(null);
    }

    private static void renew() {
        log.warn("重新初始化设备，并运行任务");
        shutdown();
        release();

        initLocalDevice();
        initRemoteDevice();
        initThreadPool();

        loginXlink();
    }

    private static void unsubscribeCOVEvent() {
        log.info("取消COV订阅...");

        LocalDevice localDevice = context.getLocalDevice();
        if (localDevice != null) {
            UnsignedInteger subscriberProcessIdentifier = new UnsignedInteger(localDevice.getInstanceNumber());

            for (Product product : cfg.getProducts()) {
                for (Device device : product.getDevices()) {
                    if (device.getEvents() != null)
                        for (Event event : device.getEvents()) {
                            log.info("取消订阅设备[mac: {}]对象[{} {}]属性[{}]的COV事件", device.getMac(), event.getObjectType(),
                                    event.getObjectId(), event.getCovProperty());
                            try {
                                ObjectIdentifier oid = new ObjectIdentifier(ObjectType.forName(event.getObjectType()),
                                        event.getObjectId());
                                PropertyReference pr = new PropertyReference(PropertyIdentifier.forName(event.getCovProperty()));
                                SubscribeCOVPropertyRequest req = new SubscribeCOVPropertyRequest(subscriberProcessIdentifier,
                                        oid, null, null, pr, null);
                                RemoteDevice remoteDevice = context.getRemoteDevice(device.getRemoteDeviceNumber());
                                localDevice.send(remoteDevice, req).get();
                            } catch (BACnetException | UnknownRemoteDevice e) {
                                log.warn("取消COV订阅异常", e);
                            }
                        }
                }
            }
        }

        log.info("取消COV订阅任务结束");
    }

    private static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                running = false;
                unsubscribeCOVEvent();
                shutdown();
                release();
                log.info("退出系统！！！");
                LogManager.shutdown();
            }
        });
    }
}

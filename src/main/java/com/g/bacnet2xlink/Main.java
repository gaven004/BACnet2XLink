package com.g.bacnet2xlink;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
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
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.ObjectPropertyReference;
import com.serotonin.bacnet4j.type.constructed.PropertyReference;
import com.serotonin.bacnet4j.type.enumerated.BinaryPV;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import com.serotonin.bacnet4j.util.PropertyReferences;
import com.serotonin.bacnet4j.util.PropertyValues;
import com.serotonin.bacnet4j.util.RequestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xlink.cm.message.DeviceLoginResultMessage;
import xlink.cm.message.type.DeviceLoginRetCodeType;

import com.g.bacnet2xlink.definition.Device;
import com.g.bacnet2xlink.definition.Event;
import com.g.bacnet2xlink.definition.Product;
import com.g.bacnet2xlink.definition.Service;
import com.g.bacnet2xlink.exception.UnknownDevice;
import com.g.bacnet2xlink.exception.UnknownProperty;
import com.g.bacnet2xlink.exception.UnknownService;
import com.g.bacnet2xlink.exception.UnknownValue;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static final String ConfigFile = "/config.json";

    private static final int subscriberLifetime = 3600; // seconds

    private static Configuration cfg = null;

    private Context context = new Context();

    private LocalDevice localDevice = null;
    private RemoteDevice remoteDevice = null;

    private ScheduledExecutorService executor;

    public static void main(String[] args) {
        log.info("");
        log.info("");
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

        me.setAttributeCallback();
        me.getAttributeCallback();
        me.serviceInvokeCallback();
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

    private void initXlinkCmMqttClient() {
        log.info("初始化xlinkMqttClient...");

        for (Product product : cfg.getProducts()) {
            String[] serviceNames = null;
            if (product.getServices() != null && product.getServices().size() > 0) {
                serviceNames = new String[product.getServices().size()];
                int i = 0;
                for (Service service : product.getServices()) {
                    serviceNames[i++] = service.getName();
                }
            }

            // 构造器配置
            XlinkMqttBuilderParams.XlinkMqttBuilderParamsBuilder builder = XlinkMqttBuilderParams.builder()
                    .certId(cfg.getCertId()) // 配置授权证书ID
                    .certKey(cfg.getCertKey()) // 配置授权证书密钥
                    .endpoint(cfg.getDeviceEndpoint()) // 云端CM服务器地址
                    .connectorType(cfg.getConnectorType()); // 配置连接器类型

            if (serviceNames != null) {
                builder.addSubscribeServiceName(product.getId(), serviceNames); // 订阅物模型服务处理
            }

            XlinkCmMqttClient xlinkMqttClient = builder.build(); // 构造XlinkMqttBuilderParams实例

            // 建立连接及认证
            try {
                xlinkMqttClient.start();
            } catch (Exception e) {
                log.error(String.format("建立{}产品[id:{}]的Xagent客户端成功", product.getName(), product.getId()), e);
                log.error("退出系统！！！");
                System.exit(1);
            }

            log.info("建立{}产品[id:{}]的Xagent客户端成功", product.getName(), product.getId());
            context.addXlinkClient(product.getId(), xlinkMqttClient);
        }
    }

    private void loginXlink() {
        // 在平台登录上线，并得到返回
        log.info("设备平台上线...");

        for (Product product : cfg.getProducts()) {
            XlinkCmMqttClient xlinkMqttClient = context.getXlinkClient(product.getId());

            for (Device device : product.getDevices()) {
                DeviceLoginResultMessage deviceLoginResultMessage = null;
                try {
                    deviceLoginResultMessage = xlinkMqttClient.deviceLogin(product.getId(), device.getMac()).get();
                } catch (Exception e) {
                    log.error(String.format("{}设备[mac:{}]上线失败", product.getName(), device.getMac()), e);
                }

                // 得到返回结果
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

    /**
     * 监听服务端的属性设置请求，并应答服务端
     * <p>
     * 目前不支持对协议中的设备属性写入
     */
    private void setAttributeCallback() {
        log.info("监听服务端的属性设置请求");
        for (Product product : cfg.getProducts()) {
            XlinkCmMqttClient xlinkMqttClient = context.getXlinkClient(product.getId());

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
     * 监听服务端的属性获取请求，并应答服务端
     */
    private void getAttributeCallback() {
        log.info("监听服务端的属性获取请求");
        for (Product product : cfg.getProducts()) {
            XlinkCmMqttClient xlinkMqttClient = context.getXlinkClient(product.getId());

            //监听服务端属性获取回调方法，注意：多个设备ID的属性获取回调方法实现可以为同一个
            xlinkMqttClient.setGetAttributeHandler(request -> {
                log.info("云平台远程读取物理设备属性，{}", request);

                //得到服务端属性获取的设备ID
                int deviceId = request.getDeviceId();

                //构建应答的信息属性
                Map<String, Object> attributes = new HashMap<>();
                //构建应答码
                String code = null;

                try {
                    Device device = context.getDevice(deviceId);
                    log.info("读取设备[{}]数值：", device.getMac());
                    DataAcquisitionHelper.readPresentValues(localDevice, remoteDevice, device, attributes, log);
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
    private void serviceInvokeCallback() {
        log.info("监听服务端的服务调用下发");
        for (Product product : cfg.getProducts()) {
            XlinkCmMqttClient xlinkMqttClient = context.getXlinkClient(product.getId());

            //物模型中的服务名
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
                Map<String, Object> output = null;

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

                    log.info("写物理设备[{}]对象的Present Value属性值：{}");
                    RequestUtils.writePresentValue(localDevice, remoteDevice, service.getOid(), converted);

                    code = "200";
                } catch (UnknownDevice | UnknownService | UnknownValue unknown) {
                    log.warn(unknown.getMessage());
                    code = "400";
//            } catch (UnknownValue unknown) {
//                log.warn(unknown.getMessage());
//                code = "500";
                } catch (Exception e) {
                    log.warn("写物理设备属性错误", e);
                    code = "500";
                }

                //构建应答包
                ServiceInvokeResponse serviceInvokeResponse = new ServiceInvokeResponse(code, output);

                log.info("远程服务调用返回：{}", serviceInvokeResponse);

                //通过方法返回值进行应答，如方法返回值为null，则不会应答
                return serviceInvokeResponse;
            });
        }
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
            builder.withBroadcast(cfg.getBroadcastAddress(), cfg.getNetworkPrefix());
            network = builder.build();
            localDevice = new LocalDevice(deviceNumber, new DefaultTransport(network));
            localDevice.initialize();
            final DeviceEventAdapter listener = new MyDeviceEventAdapter(cfg, context);
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
                remoteDevice = localDevice.getRemoteDeviceBlocking(cfg.getRemoteDeviceNumber(), 10000);
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
            SubscribeCOVTask sct = new SubscribeCOVTask(localDevice, remoteDevice, cfg, new UnsignedInteger(subscriberLifetime * 2));
            final ScheduledFuture<?> scf = executor.scheduleAtFixedRate(sct, 0, subscriberLifetime, TimeUnit.SECONDS);

            // 启动上报线程，采集并上报数据
            DataAcquisitionTask dat = new DataAcquisitionTask(localDevice, remoteDevice, cfg, context);
            final ScheduledFuture<?> daf = executor.scheduleAtFixedRate(dat,
                    cfg.getDataSubmitInterval(), cfg.getDataSubmitInterval(), TimeUnit.SECONDS);

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

    private void unsubscribeCOVEvent() {
        log.info("取消COV订阅...");

        if (localDevice != null) {
            UnsignedInteger subscriberProcessIdentifier = new UnsignedInteger(localDevice.getInstanceNumber());

            for (Product product : cfg.getProducts()) {
                for (Device device : product.getDevices()) {
                    for (Event event : device.getEvents()) {
                        log.info("取消订阅设备[mac: {}]对象[{} {}]属性[{}]的COV事件", device.getMac(), event.getObjectType(),
                                event.getObjectId(), event.getCovProperty());
                        try {
                            ObjectIdentifier oid = new ObjectIdentifier(ObjectType.forName(event.getObjectType()),
                                    event.getObjectId());
                            PropertyReference pr = new PropertyReference(PropertyIdentifier.forName(event.getCovProperty()));
                            SubscribeCOVPropertyRequest req = new SubscribeCOVPropertyRequest(subscriberProcessIdentifier,
                                    oid, null, null, pr, null);
                            localDevice.send(remoteDevice, req).get();
                        } catch (BACnetException e) {
                            log.warn("取消COV订阅异常", e);
                        }
                    }
                }
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

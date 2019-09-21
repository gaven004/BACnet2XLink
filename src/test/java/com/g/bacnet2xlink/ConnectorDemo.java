package com.g.bacnet2xlink;

import cn.xlink.iot.sdk.XlinkMqttBuilderParams;
import cn.xlink.iot.sdk.mqtt.client.cm.XlinkCmMqttClient;
import cn.xlink.iot.sdk.mqtt.client.subscribe.message.GetAttributeResponse;
import cn.xlink.iot.sdk.mqtt.client.subscribe.message.ServiceInvokeResponse;
import cn.xlink.iot.sdk.mqtt.client.subscribe.message.SetAttributeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xlink.cm.message.DeviceLoginResultMessage;
import xlink.cm.message.type.DeviceLoginRetCodeType;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Xagent SDK 3.5 Demo
 *
 * @Author shenweiran
 * @Date 2019-08-24 17:51
 */
public class ConnectorDemo {

    /**
     * 初始化配置参数
     */
    //授权证书ID（CID）
    public final static String CERT_ID         = "5d677b7ea8922b4acb0fe79d";
    //授权证书密钥（CKEY）
    public final static String CERT_KEY        = "6cf7d6d3-1eb0-4905-9511-99e3a89bf3b2";
    //云端CM服务器地址
    public final static String DEVICE_ENDPOINT = "dev-cm.xlink.cn:1883";

    // 连接器类型，用于标识连接器类别
    public final static String CONNECTOR_TYPE = "5764617121368710144";
    // 产品ID
    public final static String PRODUCT_ID     = "160008bacee20001160008bacee2c801";

    //Xagent客户端
    private static XlinkCmMqttClient xlinkMqttClient;

    static Logger logger = LoggerFactory.getLogger(ConnectorDemo.class);

    /**
     * 程序启动入口方法
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        //定义Mac地址，或者一个能唯一的标识设备的字符，该Mac信息应该在预先导入对应的产品信息下，否则会发生错误
        String mac = "222244446666";

        //初始化客户端
        init();

        //示例一：在平台进行上线，一般在进行设备初始化、上线时是必须调用,会返回设备唯一标识ID
        int deviceId = deviceLogin(PRODUCT_ID, mac);

        //示例二：发布物属性，引起平台的最新的物属性变化
        publishAttribute(deviceId);
//
//        //示例三：发布事件
//        publishEvent(deviceId);
//
//        //示例四：监听服务端的属性设置请求，并应答服务端
//        setAttributeCallback();
//
//        //示例五：监听服务端的属性获取请求，并应答服务端
//        getAttributeCallback();
//
//        //示例六：监听服务端的服务调用下发，并应答服务端
//        serviceInvokeCallback();

    }

    /**
     * 初始化Xagent 客户端
     */
    private static void init() throws Exception {
        //构造器配置
        xlinkMqttClient = XlinkMqttBuilderParams.builder()
                .certId(CERT_ID) //配置授权证书ID
                .certKey(CERT_KEY) //配置授权证书密钥
                .endpoint(DEVICE_ENDPOINT)//云端CM服务器地址
                .connectorType(CONNECTOR_TYPE)//配置连接器类型
                .addSubscribeServiceName(PRODUCT_ID, "open_door", "close_door")//订阅物模型服务处理
                .build(); //构造XlinkMqttBuilderParams实例
        //建立连接及认证
        xlinkMqttClient.start();
    }


    /**
     * 示例一：在平台进行上线，一般在进行设备初始化时，必须调用
     *
     * @param productId
     * @param mac
     */
    private static int deviceLogin(String productId, String mac) throws ExecutionException, InterruptedException {
        //在平台登录上线，并得到返回
        DeviceLoginResultMessage deviceLoginResultMessage = xlinkMqttClient.deviceLogin(productId, mac).get();
        //得到返回结果
        if (deviceLoginResultMessage.getRetCode() == DeviceLoginRetCodeType.SUCCESS) {
            //上线成功，得到平台唯一的设备ID、在物模型下，可以当做thing id使用
            int deviceId = deviceLoginResultMessage.getDeviceId();
            //建议将设备ID在内存保存起来，在后续的操作，会经常使用到。

            //自定义代码..

            return deviceId;
        } else {
            //上线失败，从RetCode读取错误信息及类型
            DeviceLoginRetCodeType retCode = deviceLoginResultMessage.getRetCode();
            logger.error("error occur, error code: {}, detail: {}", retCode.type(), retCode.name());
            //自定义代码..

            return 0;
        }
    }

    /**
     * 示例二：发布物属性，引起平台的最新的物属性变化
     *
     * @param deviceId 设备上线后得到的设备ID
     * @throws Exception
     */
    private static void publishAttribute(int deviceId) throws Exception {
        //物模型的版本号
        int version = 0;
        xlinkMqttClient.publishAttribute(deviceId, version,
                new HashMap() {{
//                    put("test1", "open"); //属性字段
                    put("test2", 1);//属性字段
                    put("Temperature", 1);//属性字段
                }}, new Date());

    }

    /**
     * 示例三：发布事件
     *
     * @param deviceId 设备上线后得到的设备ID
     */
    private static void publishEvent(int deviceId) throws Exception {
        //设备上线后得到的设备ID
        //物模型的版本号
        int version = 0;
        //物模型中的事件名
        String eventName = "xxx";
        xlinkMqttClient.publishEvent(deviceId, version, eventName,
                new HashMap() {{
                    put("state", "open"); //事件字段
                    put("color", "red");//事件字段
                }}, new Date());
    }

    /**
     * 示例四：监听服务端的属性设置请求，并应答服务端
     */
    private static void setAttributeCallback() {
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
     * 示例五：监听服务端的属性获取请求，并应答服务端
     *
     */
    private static void getAttributeCallback() {
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
     * 示例六：监听服务端的服务调用下发，并应答服务端
     */
    private static void serviceInvokeCallback() {
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

}

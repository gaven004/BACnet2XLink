package com.g.bacnet2xlink;

import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import cn.xlink.iot.sdk.XlinkMqttBuilderParams;
import cn.xlink.iot.sdk.mqtt.client.cm.XlinkCmMqttClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xlink.cm.message.DeviceLoginResultMessage;
import xlink.cm.message.type.DeviceLoginRetCodeType;

public class XLinkTest {
    /**
     * 初始化配置参数
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
    static Logger logger = LoggerFactory.getLogger(ConnectorDemo.class);
    static Random r = new Random();
    //Xagent客户端
    private static XlinkCmMqttClient xlinkMqttClient;

    /**
     * 程序启动入口方法
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        //定义Mac地址，或者一个能唯一的标识设备的字符，该Mac信息应该在预先导入对应的产品信息下，否则会发生错误
        String mac = "500200";

        //初始化客户端
        init();

        //示例一：在平台进行上线，一般在进行设备初始化、上线时是必须调用,会返回设备唯一标识ID
        int deviceId = deviceLogin(PRODUCT_ID, mac);

        //示例二：发布物属性，引起平台的最新的物属性变化
        publishAttribute(deviceId);

        publishAttribute(deviceId);

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
//                .addSubscribeServiceName(PRODUCT_ID)//订阅物模型服务处理
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
        final HashMap attributes = new HashMap() {{
            put("mode", 1);//属性字段
            put("status", 1);//属性字段
//            put("pressure", 3 + r.nextFloat());//属性字段
        }};
        logger.debug("publishAttribute: {}", attributes);
        xlinkMqttClient.publishAttribute(deviceId, version, attributes, new Date());

    }

}

package com.g.bacnet2xlink;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
public class Configuration {
    private static final Logger log = LoggerFactory.getLogger(Configuration.class);

    /**
     * Xlink配置参数
     */
    private String certId; // 授权证书ID（CID）
    private String certKey; // 授权证书密钥（CKEY）
    private String deviceEndpoint; // 云端CM服务器地址
    private String connectorType; // 连接器类型，用于标识连接器类别

    /**
     * 物理设备参数
     */
    private Integer localDeviceNumber;
    private String localAddress; // nullable, default value: "0.0.0.0"
    private Integer localPort; // nullable, default value: 0xBAC0 = 47808
    private String broadcastAddress; // the broadcast address for the network
    private Integer networkPrefix; // the number of bits in the local subnet.
    private Integer remoteDeviceNumber;

    /**
     * 系统参数
     */
    private Integer dataSubmitInterval; // 数据上报间隔，单位：秒

    /**
     * 设备定义
     */
    private List<Product> products;

    /**
     * 从资源文件恢复系统配置
     *
     * @param name 资源文件名
     * @return 系统配置
     */
    public static Configuration fromResource(String name) throws IOException {
        InputStream in = null;

        try {
            in = Configuration.class.getResourceAsStream(name);
        } catch (Exception ignore) {
        }

        if (in == null) {
            final String stripped = name.startsWith("/") ? name.substring(1) : null;

            if (in == null) {
                try {
                    in = ClassLoader.getSystemResourceAsStream(stripped);
                } catch (Exception ignore) {
                }
            }
        }

        try {
            return JSON.parseObject(in, StandardCharsets.UTF_8, Configuration.class);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * 检验
     */
    void validate() {
        // todo
    }
}

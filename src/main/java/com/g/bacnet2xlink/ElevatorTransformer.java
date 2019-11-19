package com.g.bacnet2xlink;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.primitive.Enumerated;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.util.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.g.bacnet2xlink.definition.ElevatorGateway;
import com.g.bacnet2xlink.definition.ElevatorProperty;
import com.g.bacnet2xlink.exception.UnknownRemoteDevice;
import com.g.bacnet2xlink.exception.ValidationError;
import com.g.bacnet2xlink.model.elevator.Request;
import com.g.bacnet2xlink.model.elevator.Response;

public class ElevatorTransformer implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ElevatorTransformer.class);

    private static final int SEND_INTERVAL = 1; // 任意两个字节的发送需要有一定的间隔时间
    private static final int SWITCH_INTERVAL = 50; // 终端分机从接收到指令包到数据输出完毕需要一定的转换时间

    private static final int REQUEST_DATA_LENGTH = 7;
    private static final int RESPONSE_DATA_LENGTH = 11;

    private static final int SOCKET_TIMEOUT = 5000;

    private Configuration cfg;
    private Context context;

    public ElevatorTransformer(Configuration cfg, Context context) {
        this.cfg = cfg;
        this.context = context;
    }

    private static void writeAV(LocalDevice ld, RemoteDevice rd, ElevatorProperty prop, int data) throws BACnetException {
        ObjectIdentifier oid = new ObjectIdentifier(ObjectType.analogValue, prop.getObjectId());
        Real value = new Real(data);
        RequestUtils.writePresentValue(ld, rd, oid, value);
    }

    private static void writeBVs(LocalDevice ld, RemoteDevice rd, Map<Integer, ElevatorProperty> map, byte data) throws BACnetException {
        for (Map.Entry<Integer, ElevatorProperty> entry : map.entrySet()) {
            Integer key = entry.getKey();
            ElevatorProperty prop = entry.getValue();
            ObjectIdentifier oid = new ObjectIdentifier(ObjectType.binaryValue, prop.getObjectId());
            Enumerated value = new Enumerated(BitUtil.getBit(data, key));
            RequestUtils.writePresentValue(ld, rd, oid, value);
        }
    }

    @Override
    public void run() {
        log.info("启动电梯数据采集转换任务...");

        if (cfg.getElevators() != null && !cfg.getRemoteDevices().isEmpty()) {
            boolean error = false;

            for (ElevatorGateway ele : cfg.getElevators()) {
                Socket socket = null;
                OutputStream out = null;
                DataInputStream in = null;

                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(ele.getHost(), ele.getPort()), SOCKET_TIMEOUT);
                    socket.setSoTimeout(SOCKET_TIMEOUT);

                    out = socket.getOutputStream();
                    in = new DataInputStream(socket.getInputStream());

                    Request request = Request.buildCheckRequest(ele.getTargetAddress());

                    byte[] outBuff = request.toByteArray();
                    log.info("发送采集指令到[{}]号电梯：{}", ele.getTargetAddress(), BitUtil.toString(outBuff));

                    for (int off = 0; off < REQUEST_DATA_LENGTH; off++) {
                        out.write(outBuff, off, 1);
                        // 任意两个字节的发送需要有一定的间隔时间
                        try {
                            Thread.sleep(SEND_INTERVAL);
                        } catch (InterruptedException e) {
                        }
                    }

                    // 终端分机从接收到指令包到数据输出完毕需要一定的转换时间
                    try {
                        Thread.sleep(SWITCH_INTERVAL);
                    } catch (InterruptedException e) {
                    }

                    byte[] inBuff = new byte[RESPONSE_DATA_LENGTH];
                    in.readFully(inBuff);
                    log.info("接收[{}]号电梯数据返回：{}", ele.getTargetAddress(), BitUtil.toString(inBuff));

                    Response response = Response.fromByteArray(inBuff, request.getTargetAddress());

                    LocalDevice localDevice = context.getLocalDevice();
                    RemoteDevice remoteDevice = context.getRemoteDevice(ele.getBacnetDeviceNumber());

                    writeAV(localDevice, remoteDevice, ele.getD1(), response.getCarPosition());
                    writeBVs(localDevice, remoteDevice, ele.getD2(), response.getData2());
                    writeBVs(localDevice, remoteDevice, ele.getD3(), response.getData3());
                    writeBVs(localDevice, remoteDevice, ele.getD4(), response.getData4());
                    writeBVs(localDevice, remoteDevice, ele.getD6(), response.getData6());
                } catch (IOException | ValidationError | UnknownRemoteDevice ignore) {
                    log.warn(String.format("采集[%s]号电梯数据失败", ele.getTargetAddress()), ignore);
                } catch (BACnetException e) {
                    log.warn(String.format("采集[%s]号电梯数据失败", ele.getTargetAddress()), e);
                    error = true;
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                        }
                    }
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                        }
                    }
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }

            if (error) {
                // 抛出异常，通知主线程重新初始化
                throw new RuntimeException();
            }
        }

        log.info("电梯数据采集转换任务结束");
    }
}

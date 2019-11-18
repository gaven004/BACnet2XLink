package com.g.bacnet2xlink;

import com.g.bacnet2xlink.definition.ElevatorGateway;
import com.g.bacnet2xlink.exception.ValidationError;
import com.g.bacnet2xlink.model.elevator.Request;
import com.g.bacnet2xlink.model.elevator.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

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
                    log.info("发送采集指令到[{}]号电梯：{}", ele.getTargetAddress(), request);

                    byte[] outBuff = request.toByteArray();
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
                    Response response = Response.fromByteArray(inBuff, request.getTargetAddress());
                    log.info("接收[{}]号电梯数据返回：{}", ele.getTargetAddress(), response);


                } catch (IOException | ValidationError ignore) {
                    log.warn(String.format("采集[%s]号电梯数据失败", ele.getTargetAddress()), ignore);
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

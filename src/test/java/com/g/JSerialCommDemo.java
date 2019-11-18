package com.g;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortPacketListener;
import com.g.bacnet2xlink.exception.ValidationError;
import com.g.bacnet2xlink.model.elevator.Request;
import com.g.bacnet2xlink.model.elevator.Response;

public class JSerialCommDemo {
    private static final byte[] TAS = {6, 7, 8, 9, 10};

    public static void main(String[] args) {
        SerialPort comPort = SerialPort.getCommPort("COM1");
        comPort.setComPortParameters(9600, 8, 1, SerialPort.NO_PARITY, true);
        /*
            主机应该使用收/发自动切换的RS232/RS485转换器，
            同时必须考虑该转换器在收/发状态间切换的时间，一般可认为是1ms
        */
        comPort.setRs485ModeParameters(true, 1, 1);
        comPort.openPort();

        for (byte ta : TAS) {
            try {
                PacketListener listener = new PacketListener();
                comPort.addDataListener(listener);

                Request request = Request.buildCheckRequest(ta);
                byte[] writeBuffer = request.toByteArray();
                for (int i = 0; i < writeBuffer.length; i++) {
                    comPort.writeBytes(writeBuffer, 1, i);
                /*
                    任意两个字节的发送需要有一定的间隔时间（一般为1ms左右,
                    这个只是一个参考值，要视实际情况及线路的长短而定，
                    实际最佳值可以在调试中确定）以保证可靠接收
                */
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                    }
                }

            /*
                终端分机从接收到指令包到数据输出完毕需要一定的转换时间。
                当通信速率为9600bps时，对每台分机的查询操作需要占用50ms的时间段。
            */
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                }

                comPort.removeDataListener();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        comPort.closePort();
    }

    private static final class PacketListener implements SerialPortPacketListener {
        @Override
        public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
        }

        @Override
        public int getPacketSize() {
            return 11;
        }

        @Override
        public void serialEvent(SerialPortEvent event) {
            byte[] data = event.getReceivedData();
//            try {
//                Response response = Response.fromByteArray(data);
//                System.out.println(response);
//            } catch (ValidationError validationError) {
//                System.err.println(validationError);
//            }
        }
    }
}



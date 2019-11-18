package com.g.bacnet2xlink;

import com.g.bacnet2xlink.exception.ValidationError;
import com.g.bacnet2xlink.model.elevator.Constants;
import com.g.bacnet2xlink.model.elevator.Helper;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MockElevator {
    public static void main(String[] args) {
        int port = 12345;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Device ready....");
            for (; ; )
                try (Socket clientSocket = serverSocket.accept();
                     DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                     OutputStream out = clientSocket.getOutputStream()) {

                    byte[] inBuff = new byte[7];
                    in.readFully(inBuff);
                    System.out.println("Receive: " + BitUtil.toString(inBuff));

                    byte[] outBuff = buildResponse(inBuff[2]);
                    out.write(outBuff);
                    System.out.println("Send: " + BitUtil.toString(outBuff));
                } catch (IOException | ValidationError e) {
                    e.printStackTrace();
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] buildResponse(byte address) throws ValidationError {
        byte[] buff = new byte[]{Constants.START_OF_TEXT, address, Constants.MASTER_ADDRESS, 4, 0, 0, 0, 0, 0, 0, Constants.END_OF_TEXT};
        buff[9] = Helper.summary(buff[1], buff[2], buff[3], buff[4], buff[5], buff[6], buff[7], buff[8]);
        return buff;
    }
}

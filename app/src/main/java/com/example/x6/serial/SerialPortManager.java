package com.example.x6.serial;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author jiazhu
 */
public class SerialPortManager {
    private static SerialPortManager sMananger;
    private SerialPort mSerialPort;
    private InputStream ttySxInputStrean;
    private OutputStream ttySxOutputStream;

    private SerialPortManager() {
        init();
    }

    private void init() {
        try {
            mSerialPort = new SerialPort(new File("/dev/ttyS1"), 115200, 0);
            ttySxInputStrean = mSerialPort.getInputStream();
            ttySxOutputStream = mSerialPort.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        mSerialPort.close();
    }

    public static SerialPortManager getInstance() {
        if (sMananger == null) {
            sMananger = new SerialPortManager();
        }
        return sMananger;

    }

    public int read(byte[] buffer) {
        int size = -1;
        try {
            size = ttySxInputStrean.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return size;
    }

    public void write(byte[] buffer) {
        try {
            ttySxOutputStream.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(byte[] buffer, int size) {
        try {
            ttySxOutputStream.write(buffer, 0, size);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

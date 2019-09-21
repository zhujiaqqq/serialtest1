package com.example.x6.serial;

/**
 * @author jiazhu
 */
public class SerialPortParam {
    public static final int DEFAULT_RATE = 115200;
    private static SerialPortParam sParam;

    private SerialPortParam() {
        pathName = "/dev/ttyS1";
        baudRate = DEFAULT_RATE;
    }

    public static SerialPortParam getInstance() {
        if (sParam == null) {
            sParam = new SerialPortParam();
        }
        return sParam;
    }

    private String pathName;
    private int baudRate;

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }
}

package com.topeet.serialtest.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.x6.serial.SerialPort;
import com.topeet.serialtest.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPortTestActivity extends AppCompatActivity {
    private byte[] bytes = new byte[]{0x30, 0x31, 0x32, 0x33, 0x34, 0x35};
    private SerialPort serialttyS1;
    private InputStream ttyS1InputStream;
    private OutputStream ttyS1OutputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_port_test);

        init_serial();
        try {
            ttyS1OutputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* 打开串口 */
    private void init_serial() {
        try {
            serialttyS1 = new SerialPort(new File("/dev/ttyS1"), 115200, 0);
            ttyS1InputStream = serialttyS1.getInputStream();
            ttyS1OutputStream = serialttyS1.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

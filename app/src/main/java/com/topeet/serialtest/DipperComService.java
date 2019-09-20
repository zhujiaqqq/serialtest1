package com.topeet.serialtest;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.app.ActivityManager;
import android.content.Context;

import com.example.x6.serial.SerialPort;
import com.topeet.serialtest.EventBus.Event_DWXX;
import com.topeet.serialtest.EventBus.Event_Service;
import com.topeet.serialtest.EventBus.Event_FKXX;
import com.topeet.serialtest.EventBus.Event_ZJXX;
import com.topeet.serialtest.activity.YybwjsActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;


public class DipperComService extends Service {

    public byte TXSQ_count = 0, DWSQ_count = 0, XTZJ_count = 0;
    private SerialPort serialPort;
    private InputStream ttyS1InputStream;
    private OutputStream ttyS1OutputStream;

    public DipperComService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override//创建的时候调用
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        try {
            serialPort = new SerialPort(new File("/dev/ssyS1"), 19200, 0);
            ttyS1InputStream = serialPort.getInputStream();
            ttyS1OutputStream = serialPort.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        timer.schedule(task, 0, 500);
    }

    public void onEvent(Event_Service event) {

        Message message = new Message();
        message.what = 2;
        handler.sendMessage(message);
        switch (event.anInt) {
            case 1:
                TXSQ_count = 1;
                break;
            case 2:
                break;
            case 3:
                DWSQ_count = 1;
                break;
            case 4:
                XTZJ_count = 1;
                break;
            default:
                break;
        }
        if (event.anInt == 1) {


        }
    }


    @Override//启动的时候调用
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Override//销毁的时候调用
    public void onDestroy() {
        super.onDestroy();
        serialPort.close();
    }

    private Timer timer = new Timer();
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            int res = 0;
            switch (msg.what) {
                case 1:
                    byte[] rx_buff = new byte[500];
                    try {
                        ttyS1InputStream.read(rx_buff);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (rx_buff != null) {
                        res = DipperCom.comReceive(rx_buff, rx_buff.length);

                        switch (res / 100) {
                            case 1:
                                TXSQ_count = 0;
                                EventBus.getDefault().post(new Event_FKXX(res - 100));
                                break;
                            case 2:
                                Intent intent = new Intent(getBaseContext(), YybwjsActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                getApplication().startActivity(intent);
                                break;
                            case 3:
                                DWSQ_count = 0;
                                res = res - 300;
                                if (res > 10) {
                                    EventBus.getDefault().post(new Event_FKXX(res));
                                } else {
                                    EventBus.getDefault().post(new Event_DWXX(res));
                                }

                                break;
                            case 4:
                                XTZJ_count = 0;
                                EventBus.getDefault().post(new Event_ZJXX(res - 400));
                                break;
                            default:
                                break;
                        }
                    }

                    if (TXSQ_count != 0) {
                        TXSQ_count++;
                        if (TXSQ_count == 8) {
                            TXSQ_count = 0;
                            EventBus.getDefault().post(new Event_FKXX(9));
                        }
                    }

                    if (DWSQ_count != 0) {
                        DWSQ_count++;
                        if (DWSQ_count == 8) {
                            DWSQ_count = 0;
                            //EventBus.getDefault().post(new Event_DWXX(9));
                        }
                    }

                    if (XTZJ_count != 0) {
                        XTZJ_count++;
                        if (XTZJ_count == 8) {
                            XTZJ_count = 0;
                            EventBus.getDefault().post(new Event_ZJXX(9));
                        }
                    }


                    break;
                case 2:
                    byte[] data = new byte[DipperCom.send_data.length];
                    for (int i = 0; i < DipperCom.send_data.length; i++) {
                        data[i] = (byte) DipperCom.send_data[i];
                    }
                    try {
                        ttyS1OutputStream.write(data, 0, DipperCom.send_num);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                    com3.Write(DipperCom.send_data, DipperCom.send_num);
                    break;
                default:
                    break;
            }

            super.handleMessage(msg);
        }
    };

    private TimerTask task = new TimerTask() {
        @Override
        public void run() {
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }
    };

    private String getRunningActivityName() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        String runningActivity = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
        return runningActivity;
    }

    static {
        try {
            System.loadLibrary("serialtest");

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }


}
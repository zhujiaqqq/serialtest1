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
import com.topeet.serialtest.eventbus.EventDWXX;
import com.topeet.serialtest.eventbus.EventService;
import com.topeet.serialtest.eventbus.EventFKXX;
import com.topeet.serialtest.eventbus.EventZJXX;
import com.topeet.serialtest.activity.YybwjsActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;


/**
 * @author jiazhu
 */
public class DipperComService extends Service {

    public byte txsqCount = 0, dwsqCount = 0, xtzjCount = 0;
    private SerialPort serialPort;
    private InputStream ttyS1InputStream;
    private OutputStream ttyS1OutputStream;

    private Timer timer = new Timer();

    public DipperComService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
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

    public void onEvent(EventService event) {

        Message message = new Message();
        message.what = 2;
        handler.sendMessage(message);
        switch (event.anInt) {
            case 1:
                txsqCount = 1;
                break;
            case 2:
                break;
            case 3:
                dwsqCount = 1;
                break;
            case 4:
                xtzjCount = 1;
                break;
            default:
                break;
        }
        if (event.anInt == 1) {
            // TODO: 2019-09-20 ?

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        serialPort.close();
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int res = 0;
            switch (msg.what) {
                case 1:
                    byte[] rxBuff = new byte[500];
                    try {
                        ttyS1InputStream.read(rxBuff);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    res = DipperCom.comReceive(rxBuff, rxBuff.length);

                    switch (res / 100) {
                        case 1:
                            txsqCount = 0;
                            EventBus.getDefault().post(new EventFKXX(res - 100));
                            break;
                        case 2:
                            Intent intent = new Intent(getBaseContext(), YybwjsActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getApplication().startActivity(intent);
                            break;
                        case 3:
                            dwsqCount = 0;
                            res = res - 300;
                            if (res > 10) {
                                EventBus.getDefault().post(new EventFKXX(res));
                            } else {
                                EventBus.getDefault().post(new EventDWXX(res));
                            }

                            break;
                        case 4:
                            xtzjCount = 0;
                            EventBus.getDefault().post(new EventZJXX(res - 400));
                            break;
                        default:
                            break;
                    }

                    if (txsqCount != 0) {
                        txsqCount++;
                        if (txsqCount == 8) {
                            txsqCount = 0;
                            EventBus.getDefault().post(new EventFKXX(9));
                        }
                    }

                    if (dwsqCount != 0) {
                        dwsqCount++;
                        if (dwsqCount == 8) {
                            dwsqCount = 0;
                            //EventBus.getDefault().post(new EventDWXX(9));
                        }
                    }

                    if (xtzjCount != 0) {
                        xtzjCount++;
                        if (xtzjCount == 8) {
                            xtzjCount = 0;
                            EventBus.getDefault().post(new EventZJXX(9));
                        }
                    }


                    break;
                case 2:
                    byte[] data = new byte[DipperCom.sSendData.length];
                    for (int i = 0; i < DipperCom.sSendData.length; i++) {
                        data[i] = (byte) DipperCom.sSendData[i];
                    }
                    try {
                        ttyS1OutputStream.write(data, 0, DipperCom.sSendNum);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
        return activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
    }

    static {
        try {
            System.loadLibrary("serialtest");

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }


}
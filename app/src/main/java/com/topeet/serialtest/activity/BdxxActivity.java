package com.topeet.serialtest.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.topeet.serialtest.LocalHandler;
import com.example.x6.serial.SerialPortManager;
import com.topeet.serialtest.DipperCom;
import com.topeet.serialtest.eventbus.EventDWXX;
import com.topeet.serialtest.eventbus.EventFKXX;
import com.topeet.serialtest.R;

import de.greenrobot.event.EventBus;

public class BdxxActivity extends Activity implements View.OnClickListener, LocalHandler.IHandler {

    TextView text_BdxxJdd;
    TextView text_BdxxJdjf;
    TextView text_BdxxJdjm;
    TextView text_BdxxWdd;
    TextView text_BdxxWdjf;
    TextView text_BdxxWdjm;
    TextView text_BdxxGd;
    TextView text_BdxxSjs;
    TextView text_BdxxSjf;
    TextView text_BdxxSjm;

    ProgressDialog progressDialog;
    private LocalHandler mHandler = new LocalHandler(this);
    private Runnable dialogCheckRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (progressDialog.isShowing()) {
                mHandler.sendEmptyMessage(0x99);
            }
        }
    };

    private Runnable dwxxRunnable = new Runnable() {
        @Override
        public void run() {

            int sendLen = 22;
            byte[] sendBuff = new byte[sendLen];
            sendBuff[0] = '$';
            sendBuff[1] = 'D';
            sendBuff[2] = 'W';
            sendBuff[3] = 'S';
            sendBuff[4] = 'Q';
            sendBuff[5] = (byte) (sendLen >> 8);
            sendBuff[6] = (byte) (sendLen & 0x00ff);
            sendBuff[7] = 0;//用户地址
            sendBuff[8] = 0;
            sendBuff[9] = 0;
            sendBuff[10] = 0x04;//信息类别
            sendBuff[11] = 0x0;//高度数据和天线高
            sendBuff[12] = 0x0;//
            sendBuff[13] = 0x0;//
            sendBuff[14] = 0x0;//
            sendBuff[15] = 0x0;//气压数据
            sendBuff[16] = 0x0;//
            sendBuff[17] = 0x0;//
            sendBuff[18] = 0x0;//
            sendBuff[19] = 0x0;//入站频度
            sendBuff[20] = 0x0;//
            sendBuff[21] = DipperCom.XORCheck(sendBuff, (sendLen - 1));

            SerialPortManager.getInstance().write(sendBuff, sendLen);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bdxx);

        EventBus.getDefault().register(this);


        progressDialog = new ProgressDialog(BdxxActivity.this);
        progressDialog.setTitle("正在读取数据，请稍等");
        progressDialog.setMessage("请稍等");
        progressDialog.setCancelable(true);
        progressDialog.show();


//        DipperCom.comSend(send_buff, send_len);
//        EventBus.getDefault().post(new EventService(3));


        text_BdxxJdd = (TextView) findViewById(R.id.text_BdxxJdd);
        text_BdxxJdjf = (TextView) findViewById(R.id.text_BdxxJdjf);
        text_BdxxJdjm = (TextView) findViewById(R.id.text_BdxxJdjm);
        text_BdxxWdd = (TextView) findViewById(R.id.text_BdxxWdd);
        text_BdxxWdjf = (TextView) findViewById(R.id.text_BdxxWdjf);
        text_BdxxWdjm = (TextView) findViewById(R.id.text_BdxxWdjm);
        text_BdxxGd = (TextView) findViewById(R.id.text_BdxxGd);
        text_BdxxSjs = (TextView) findViewById(R.id.text_BdxxSjs);
        text_BdxxSjf = (TextView) findViewById(R.id.text_BdxxSjf);
        text_BdxxSjm = (TextView) findViewById(R.id.text_BdxxSjm);
        Button button_Fhsyj = (Button) findViewById(R.id.button_BdxxFhzjm);
        button_Fhsyj.setOnClickListener(this);

        new Thread(dwxxRunnable).start();
        new Thread(dialogCheckRunnable).start();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_BdxxFhzjm) {
            text_BdxxJdd.setText("000");
            text_BdxxJdjf.setText("000");
            text_BdxxJdjm.setText("000");
            text_BdxxWdd.setText("000");
            text_BdxxWdjf.setText("000");
            text_BdxxWdjm.setText("000");
            text_BdxxGd.setText("000");
            text_BdxxSjs.setText("00");
            text_BdxxSjf.setText("00");
            text_BdxxSjm.setText("00");
            finish();
        }
    }

    public void onEvent(EventFKXX event) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(BdxxActivity.this);
        dialog.setTitle("    ");
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        switch (event.anInt) {
            case 10:
                break;
            case 11:
                progressDialog.dismiss();
                dialog.setMessage("定位失败，请稍后重试");
                dialog.show();
                break;
            case 12:
                progressDialog.dismiss();
                dialog.setMessage("北斗模块暂无信号，请稍后重试");
                dialog.show();
                break;
            default:
                break;
        }
    }


    public void onEvent(EventDWXX event) {
        progressDialog.dismiss();
        switch (event.anInt) {
            case 1:
                text_BdxxJdd.setText(String.format("%03d", DipperCom.l_du));
                text_BdxxJdjf.setText(String.format("%03d", DipperCom.l_fen));
                text_BdxxJdjm.setText(String.format("%03d", DipperCom.l_miao));
                text_BdxxWdd.setText(String.format("%03d", DipperCom.b_du));
                text_BdxxWdjf.setText(String.format("%03d", DipperCom.b_fen));
                text_BdxxWdjm.setText(String.format("%03d", DipperCom.b_miao));
                text_BdxxGd.setText(String.format("%03d", DipperCom.gaodu));
                text_BdxxSjs.setText(String.format("%02d", DipperCom.t_shi));
                text_BdxxSjf.setText(String.format("%02d", DipperCom.t_fen));
                text_BdxxSjm.setText(String.format("%02d", DipperCom.t_miao));
                break;
            case 9:
                AlertDialog.Builder dialog = new AlertDialog.Builder(BdxxActivity.this);
                dialog.setTitle("    ");
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                dialog.setMessage("发送超时，请检查北斗模块连接");
                dialog.show();
                break;
            default:
                break;
        }
    }

    @Override
    public void handlerMessage(Message msg) {
        if (msg.what == 0x99) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(BdxxActivity.this);
            dialog.setTitle("    ");
            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            progressDialog.dismiss();
            dialog.setMessage("北斗模块未连接，请检测设备");
            dialog.show();
        }
    }
}

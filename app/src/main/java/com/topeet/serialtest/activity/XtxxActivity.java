package com.topeet.serialtest.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.x6.serial.LocalHandler;
import com.example.x6.serial.SerialPortManager;
import com.topeet.serialtest.DipperCom;
import com.topeet.serialtest.eventbus.EventFKXX;
import com.topeet.serialtest.eventbus.EventZJXX;
import com.topeet.serialtest.R;

import de.greenrobot.event.EventBus;

public class XtxxActivity extends AppCompatActivity implements View.OnClickListener, LocalHandler.IHandler {

    TextView mTvXtxxBjID;
    TextView mTvXtxxGlzk;
    TextView mTvXtxxICState;
    TextView mTvXtxxHwState;
    TextView mTvXtxxInboundState;
    TextView mTvXtxxVersions;


    ProgressDialog progressDialog;

    private LocalHandler mHandler = new LocalHandler(this);

    private Runnable xtzjRunnable = new Runnable() {
        @Override
        public void run() {
            sendXtzj();
        }
    };

    private Runnable dialogCheckRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (progressDialog.isShowing()) {
                mHandler.sendEmptyMessage(254);
            }
        }
    };

    /**
     * 发送系统自检
     */
    private void sendXtzj() {
        int sendLen = 13;
        byte[] sendBuff = new byte[sendLen];
        sendBuff[0] = '$';
        sendBuff[1] = 'X';
        sendBuff[2] = 'T';
        sendBuff[3] = 'Z';
        sendBuff[4] = 'J';
        sendBuff[5] = (byte) (sendLen >> 8);
        sendBuff[6] = (byte) (sendLen & 0x00ff);
        //用户地址
        sendBuff[7] = 0;
        sendBuff[8] = 0;
        sendBuff[9] = 0;
        //输出频度
        sendBuff[10] = 0x0;
        sendBuff[11] = 0x0;
        sendBuff[12] = DipperCom.XORCheck(sendBuff, (sendLen - 1));

        SerialPortManager.getInstance().write(sendBuff, sendLen);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xtxx);

        EventBus.getDefault().register(this);
        progressDialog = new ProgressDialog(XtxxActivity.this);
        progressDialog.setTitle("正在读取数据，请稍等");
        progressDialog.setMessage("请稍等");
        progressDialog.setCancelable(true);
        progressDialog.show();


//        DipperCom.comSend(send_buff, send_len);
//        EventBus.getDefault().post(new EventService(4));

        mTvXtxxBjID = (TextView) findViewById(R.id.text_XtxxBjID);
        mTvXtxxGlzk = (TextView) findViewById(R.id.text_XtxxGlzk);
        mTvXtxxICState = (TextView) findViewById(R.id.text_Xtxx_ICState);
        mTvXtxxHwState = (TextView) findViewById(R.id.text_Xtxx_hwState);
        mTvXtxxInboundState = (TextView) findViewById(R.id.text_Xtxx_inboundState);
        mTvXtxxVersions = (TextView) findViewById(R.id.text_XtxxVersions);

        Button button_Fhsyj = (Button) findViewById(R.id.button_XtxxFhzjm);
        button_Fhsyj.setOnClickListener(this);

        new Thread(xtzjRunnable).start();
        new Thread(dialogCheckRunnable).start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_XtxxFhzjm:
                mTvXtxxBjID.setText(null);
                mTvXtxxGlzk.setText(null);
                mTvXtxxICState.setText(null);
                mTvXtxxHwState.setText(null);
                mTvXtxxInboundState.setText(null);
                mTvXtxxVersions.setText(null);
                finish();
                break;
            default:
                break;
        }
    }

    public void onEvent(EventZJXX event) {
        progressDialog.dismiss();
        switch (event.anInt) {
            case 1:
                mTvXtxxBjID.setText(String.format("%06d", DipperCom.local_id));
                mTvXtxxGlzk.setText(DipperCom.glzk[0] + "   " + DipperCom.glzk[1] + "   " + DipperCom.glzk[2] + "   " + DipperCom.glzk[3] + "   " + DipperCom.glzk[4] + "   " + DipperCom.glzk[5]);
                if (DipperCom.IC_state == 0) {
                    mTvXtxxICState.setText("IC卡状态：正常");
                } else {
                    mTvXtxxICState.setText("IC卡状态：异常");
                }

                if (DipperCom.hardware_state == 0) {
                    mTvXtxxHwState.setText("硬件状态：正常");
                } else {
                    mTvXtxxHwState.setText("硬件状态：异常");
                }

                if (DipperCom.inbound_state == 0) {
                    mTvXtxxInboundState.setText("入站状态：正常");
                } else {
                    mTvXtxxInboundState.setText("入站状态：异常");
                }
                mTvXtxxVersions.setText(DipperCom.versions);

                break;

            case 9:
                AlertDialog.Builder dialog = new AlertDialog.Builder(XtxxActivity.this);
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
        if (msg.what == 254) {
            AlertDialog.Builder dialog2 = new AlertDialog.Builder(XtxxActivity.this);
            dialog2.setTitle("    ");
            dialog2.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            dialog2.setMessage("北斗模块未连接，请检测设备");
            dialog2.show();
            progressDialog.dismiss();
        }
    }
}

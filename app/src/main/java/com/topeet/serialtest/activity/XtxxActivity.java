package com.topeet.serialtest.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.topeet.serialtest.DipperCom;
import com.topeet.serialtest.EventBus.Event_Service;
import com.topeet.serialtest.EventBus.Event_ZJXX;
import com.topeet.serialtest.R;

import de.greenrobot.event.EventBus;

public class XtxxActivity extends AppCompatActivity implements View.OnClickListener{

    TextView text_XtxxBjID;
    TextView text_XtxxGlzk;
    TextView text_Xtxx_ICState;
    TextView text_Xtxx_hwState;
    TextView text_Xtxx_inboundState;
    TextView text_XtxxVersions;


    ProgressDialog progressDialog;

    byte[] send_buff = new byte[100];
    int send_len;

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


        send_len = 13;

        send_buff[0] = '$';
        send_buff[1] = 'X';
        send_buff[2] = 'T';
        send_buff[3] = 'Z';
        send_buff[4] = 'J';
        send_buff[5] = (byte)(send_len>>8);
        send_buff[6] = (byte)(send_len & 0x00ff);
        send_buff[7] = 0;//用户地址
        send_buff[8] = 0;
        send_buff[9] = 0;
        send_buff[10] = 0x0;//输出频度
        send_buff[11] = 0x0;//
        send_buff[12] = DipperCom.XORCheck(send_buff, (send_len - 1));//

        DipperCom.comSend(send_buff, send_len);
        EventBus.getDefault().post(new Event_Service(4));

        text_XtxxBjID = (TextView)findViewById(R.id.text_XtxxBjID);
        text_XtxxGlzk = (TextView)findViewById(R.id.text_XtxxGlzk);
        text_Xtxx_ICState = (TextView)findViewById(R.id.text_Xtxx_ICState);
        text_Xtxx_hwState = (TextView)findViewById(R.id.text_Xtxx_hwState);
        text_Xtxx_inboundState = (TextView)findViewById(R.id.text_Xtxx_inboundState);
        text_XtxxVersions = (TextView)findViewById(R.id.text_XtxxVersions);

        Button button_Fhsyj = (Button) findViewById(R.id.button_XtxxFhzjm);
        button_Fhsyj.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_XtxxFhzjm:
                text_XtxxBjID.setText(null);
                text_XtxxGlzk.setText(null);
                text_Xtxx_ICState.setText(null);
                text_Xtxx_hwState.setText(null);
                text_Xtxx_inboundState.setText(null);
                text_XtxxVersions.setText(null);
                finish();
                break;
            default:
                break;
        }
    }

    public void onEvent(Event_ZJXX event){
        progressDialog.dismiss();
        switch (event.anInt){
            case 1:
                text_XtxxBjID.setText(String.format("%06d", DipperCom.local_id));
                text_XtxxGlzk.setText(DipperCom.glzk[0]+"   "+DipperCom.glzk[1]+"   "+DipperCom.glzk[2]+"   "+DipperCom.glzk[3]+"   "+DipperCom.glzk[4]+"   "+DipperCom.glzk[5]);
                if(DipperCom.IC_state == 0){
                    text_Xtxx_ICState.setText("IC卡状态：正常");
                }else{
                    text_Xtxx_ICState.setText("IC卡状态：异常");
                }

                if(DipperCom.hardware_state == 0){
                    text_Xtxx_hwState.setText("硬件状态：正常");
                }else{
                    text_Xtxx_hwState.setText("硬件状态：异常");
                }

                if(DipperCom.inbound_state == 0){
                    text_Xtxx_inboundState.setText("入站状态：正常");
                }else{
                    text_Xtxx_inboundState.setText("入站状态：异常");
                }
                text_XtxxVersions.setText(DipperCom.versions);

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
        }
    }
}

package com.topeet.serialtest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;

import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.sunflower.FlowerCollector;
import com.topeet.serialtest.EventBus.Event_FKXX;
import com.topeet.serialtest.EventBus.Event_Service;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

import de.greenrobot.event.EventBus;

public class Yybwfs_Activity extends AppCompatActivity implements View.OnClickListener {
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

    private EditText exit_SendIDNumber;
    private EditText exit_SpeakContent;


    int int_send_address;
    String string_send_address;
    String string_send_content;
    char[] char_send_content = new char[100];
    byte[] byte_send_content = new byte[100];
    byte[] byte_send_number = new byte[3];
    byte[] send_buff = new byte[100];
    int send_len;
    int message_len;

    int exit_flog = 0;

    private static String TAG = Yybwfs_Activity.class.getSimpleName();
    // 语音听写UI
    private RecognizerDialog mIatDialog;

    private Toast mToast;
    private SharedPreferences mSharedPreferences;
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_LOCAL;
    // 语记安装助手类
    ApkInstaller mInstaller;


    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yybwfs);

        EventBus.getDefault().register(this);

        SpeechUtility.createUtility(Yybwfs_Activity.this, "appid=" + getString(R.string.app_id));

        if (!SpeechUtility.getUtility().checkServiceInstalled()) {
//            mInstaller.install();
        } else {
            String result = FucUtil.checkLocalResource();
            if (!TextUtils.isEmpty(result)) {
                showTip(result);
            }
        }

        mIatDialog = new RecognizerDialog(Yybwfs_Activity.this, mInitListener);

        mSharedPreferences = getSharedPreferences(IatSettings.PREFER_NAME,
                Activity.MODE_PRIVATE);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        mInstaller = new ApkInstaller(Yybwfs_Activity.this);
        progressDialog = new ProgressDialog(Yybwfs_Activity.this);



        Button button_Shibie = (Button) findViewById(R.id.button_shibie);
        Button button_Fasong = (Button) findViewById(R.id.button_fasong);
        Button button_Fhzjm = (Button) findViewById(R.id.button_SendFhzjm);
        button_Shibie.setOnClickListener(this);
        button_Fasong.setOnClickListener(this);
        button_Fhzjm.setOnClickListener(this);
        exit_SendIDNumber = (EditText) findViewById(R.id.edit_SendIDNumber);
        exit_SpeakContent = (EditText) findViewById(R.id.edit_SpeakContent);

    }


    public void onEvent(Event_FKXX event){


        if(event.anInt < 10)
        {
            AlertDialog.Builder dialog = new AlertDialog.Builder(Yybwfs_Activity.this);
            dialog.setTitle("    ");
            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (exit_flog == 1) {
                        finish();
                    }
                }
            });

            switch (event.anInt){
                case 0:
                    exit_flog = 1;
                    SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();
                    editor.putString("No"+DipperCom.save_num,String.format("%04d",(DipperCom.save_num+1))+"    发送     "+string_send_address+"      "+string_send_content);
                    DipperCom.save_num++;
                    editor.putInt("save_num", DipperCom.save_num);
                    editor.apply();
                    progressDialog.dismiss();
                    dialog.setMessage("发送成功");
                    dialog.show();
                    break;
                case 1:
                    progressDialog.dismiss();
                    dialog.setMessage("发送失败，请稍后重试");
                    dialog.show();
                    break;
                case 4:
                    progressDialog.dismiss();
                    dialog.setMessage("发送失败，发送太过频繁，请等候"+DipperCom.fspdwd_time+"秒");
                    dialog.show();
                    break;
                case 9:
                    progressDialog.dismiss();
                    dialog.setMessage("发送超时，请检查北斗模块连接");
                    dialog.show();
                    break;
            }
        }
    }

    public void startListenUI(View view) {

        // 2.设置accent、language等参数
        mIatDialog.setParameter(SpeechConstant.PARAMS, null);
        //设置听写引擎，本地
        mIatDialog.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIatDialog.setParameter(SpeechConstant.RESULT_TYPE, "json");
        // 设置语言
        mIatDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        mIatDialog.setParameter(SpeechConstant.ACCENT, "mandarin");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        //mDialog.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));
        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        //mDialog.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));
        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        //mDialog.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));
        // 3.设置回调接口

        mIatDialog.setListener(mRecognizerDialogListener);
        mIatDialog.show();

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_shibie:

                // 移动数据分析，收集开始听写事件
                FlowerCollector.onEvent(Yybwfs_Activity.this, "iat_recognize");
                exit_SpeakContent.setText(null);// 清空显示内容
                startListenUI(v);

                showTip(getString(R.string.text_begin));

                //Toast.makeText(v.getContext(), "shibie", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_fasong:
                byte i;


                progressDialog.setTitle("正在发送");
                progressDialog.setMessage("请稍等");
                progressDialog.setCancelable(true);
                progressDialog.show();

                string_send_address = exit_SendIDNumber.getText().toString();
                string_send_content = exit_SpeakContent.getText().toString();
                if ((string_send_address != null) && (string_send_address.length() == 6)) {
                    int_send_address = Integer.parseInt(string_send_address);
                    message_len = string_send_content.length();
                    char_send_content = string_send_content.toCharArray();
                    send_len = message_len*2+1+18;
                    byte_send_number = DataChange.numIntToByte(int_send_address);
                    //int_send_address = DataChange.numByteToInt(byte_send_number);
                    byte_send_content = DataChange.messageCharToByte(char_send_content, message_len);
                    //char_send_content = DataChange.messageByteToChars(byte_send_content, 18);

                    send_buff[0] = '$';
                    send_buff[1] = 'T';
                    send_buff[2] = 'X';
                    send_buff[3] = 'S';
                    send_buff[4] = 'Q';
                    send_buff[5] = (byte)(send_len>>8);
                    send_buff[6] = (byte)(send_len & 0x00ff);
                    send_buff[7] = 0;
                    send_buff[8] = 0;
                    send_buff[9] = 0;
                    send_buff[10] = 0x46;
                    for (i = 0; i<3; i++){
                        send_buff[11+i] = byte_send_number[i];
                    }
                    send_buff[14] = (byte)(((message_len*2*8)+8) >> 8);
                    send_buff[15] = (byte)(((message_len*2*8)+8) & 0x00ff);
                    send_buff[16] = (byte)0x00;
                    send_buff[17] = (byte)0xa4;
                    for (i = 0; i<(message_len*2); i++){
                        send_buff[18+i] = byte_send_content[i];
                    }
                    send_buff[send_len-1] = DipperCom.XORCheck(send_buff, (send_len - 1));
                    DipperCom.comSend(send_buff, send_len);
                    EventBus.getDefault().post(new Event_Service(1));

                    Toast.makeText(v.getContext(), "输入内容的长度为" + message_len, Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(v.getContext(), "请输入正确的号码", Toast.LENGTH_LONG).show();
                }


                Toast.makeText(v.getContext(), string_send_content, Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_SendFhzjm:
                finish();
                break;
            default:
                break;
        }
    }

    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        exit_SpeakContent.setText(resultBuffer.toString());
        exit_SpeakContent.setSelection(exit_SpeakContent.length());
    }

    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            printResult(results);
        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true));
        }

    };

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
            }
        }
    };

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }



}
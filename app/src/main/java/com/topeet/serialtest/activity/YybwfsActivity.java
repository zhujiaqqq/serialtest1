package com.topeet.serialtest.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.topeet.serialtest.LocalHandler;
import com.example.x6.serial.SerialPortManager;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.util.ResourceUtil;
import com.iflytek.sunflower.FlowerCollector;
import com.topeet.serialtest.util.KeyboardUtil;
import com.topeet.serialtest.util.NumberUtil;
import com.topeet.serialtest.DipperCom;
import com.topeet.serialtest.eventbus.EventFKXX;
import com.topeet.serialtest.R;
import com.topeet.serialtest.util.FucUtil;
import com.topeet.serialtest.util.JsonParser;
import com.topeet.serialtest.util.XmlParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import de.greenrobot.event.EventBus;

public class YybwfsActivity extends AppCompatActivity implements View.OnClickListener, LocalHandler.IHandler {
    private static final int MAX_NUM = 6;


    /**
     * 语音识别对象
     */
    private SpeechRecognizer mAsr;

    private EditText mEtNumber;
    private EditText mEtRecognizeResult;


    int int_send_address;
    private String stringSendAddress;
    private String stringSendContent;
    char[] charSendContent = new char[100];
    byte[] byteSendContent = new byte[100];
    byte[] byteSendNumber = new byte[3];
    byte[] sendBuff = new byte[100];
    int sendLen;
    int messageLen;

    int exit_flog = 0;

    /**
     * 语法、词典临时变量
     */
    String mContent;
    /**
     * 函数调用返回值
     */
    int ret = 0;
    /**
     * 本地语法文件
     */
    private String mLocalGrammar = null;
    /**
     * 本地词典
     */
    private String mLocalLexicon = null;
    /**
     * 本地语法构建路径
     */
    private String grmPath = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/msc/test";
    /**
     * 返回结果格式，支持：xml,json
     */
    private String mResultType = "json";

    private static final String GRAMMAR_TYPE_BNF = "bnf";

    private static String TAG = YybwfsActivity.class.getSimpleName();
    private Toast mToast;
    ProgressDialog progressDialog;
    private long mStart;

    private LocalHandler mHandler = new LocalHandler(this);

    private Runnable dialogCheckRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (progressDialog != null && progressDialog.isShowing()) {
                mHandler.sendEmptyMessage(253);
            }
        }
    };

    private Runnable sendVoiceRunnable = new Runnable() {
        @Override
        public void run() {

            sendBuff[0] = '$';
            sendBuff[1] = 'T';
            sendBuff[2] = 'X';
            sendBuff[3] = 'S';
            sendBuff[4] = 'Q';
            sendBuff[5] = (byte) (sendLen >> 8);
            sendBuff[6] = (byte) (sendLen & 0x00ff);
            sendBuff[7] = 0;
            sendBuff[8] = 0;
            sendBuff[9] = 0;
            sendBuff[10] = 0x46;
            for (int i = 0; i < 3; i++) {
                sendBuff[11 + i] = byteSendNumber[i];
            }
            sendBuff[14] = (byte) (((messageLen * 2 * 8) + 8) >> 8);
            sendBuff[15] = (byte) (((messageLen * 2 * 8) + 8) & 0x00ff);
            sendBuff[16] = (byte) 0x00;
            sendBuff[17] = (byte) 0xa4;
            for (int i = 0; i < (messageLen * 2); i++) {
                sendBuff[18 + i] = byteSendContent[i];
            }
            sendBuff[sendLen - 1] = DipperCom.XORCheck(sendBuff, (sendLen - 1));

            SerialPortManager.getInstance().write(sendBuff, sendLen);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yybwfs);
        initView();
        initData();
    }

    private void initData() {
        EventBus.getDefault().register(this);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        progressDialog = new ProgressDialog(YybwfsActivity.this);

        // 初始化识别对象
        mAsr = SpeechRecognizer.createRecognizer(this, mInitListener);
        //获取本地语法文件
        mLocalGrammar = FucUtil.readFile(this, "call.bnf", "utf-8");
        //获取本地词典文件  默认路径：/sdcard/key.txt
        mLocalLexicon = getContentFromExternalStorage(Environment.getExternalStorageDirectory().getPath() + "/key.txt");
        if (TextUtils.isEmpty(mLocalLexicon)) {
            showTip("外部存储中未找到词典");
            mLocalLexicon = FucUtil.readFile(this, "key.txt", "utf-8");
        }

        if (setParams()) {
            showTip("OK");
        }
    }

    private void initView() {
        TextView tvRecognize = findViewById(R.id.tv_recognize);
        TextView tvSend = findViewById(R.id.tv_send);
        TextView tvBack = findViewById(R.id.tv_back);
        tvRecognize.setOnClickListener(this);
        tvSend.setOnClickListener(this);
        tvBack.setOnClickListener(this);
        mEtNumber = findViewById(R.id.et_number);
        mEtRecognizeResult = findViewById(R.id.et_recognize_result);
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = code -> {
        Log.d(TAG, "SpeechRecognizer init() code = " + code);
        if (code != ErrorCode.SUCCESS) {
            showTip("初始化失败,错误码：" + code);
        }
    };

    /**
     * 更新词典监听器。
     */
    private LexiconListener lexiconListener = (lexiconId, error) -> {
        if (error == null) {
            showTip("词典更新成功");
        } else {
            showTip("词典更新失败,错误码：" + error.getErrorCode());
        }
    };

    /**
     * 构建语法监听器。
     */
    private GrammarListener grammarListener = (grammarId, error) -> {
        if (error == null) {
            showTip("语法构建成功：" + grammarId);
        } else {
            showTip("语法构建失败,错误码：" + error.getErrorCode());
        }
    };

    /**
     * 识别监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小：" + volume);
            Log.d(TAG, "返回音频数据：" + data.length);
        }

        @Override
        public void onResult(final RecognizerResult result, boolean isLast) {
            if (null != result && !TextUtils.isEmpty(result.getResultString())) {
                Log.d(TAG, "recognizer result：" + result.getResultString());
                String text = "";
                if ("json".equals(mResultType)) {
                    text = JsonParser.parseGrammarResult(result.getResultString(), SpeechConstant.TYPE_LOCAL);
                } else if ("xml".equals(mResultType)) {
                    text = XmlParser.parseNluResult(result.getResultString());
                } else {
                    text = result.getResultString();
                }
                // 显示
                String[] split = text.split("】【");
                if (split.length > 2) {
                    ((EditText) findViewById(R.id.et_recognize_result)).setText(split[1]);
                }
            } else {
                Log.d(TAG, "recognizer result : null");
            }
            System.out.println("识别时间： " + String.valueOf(System.currentTimeMillis() - mStart));
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话");
            mStart = System.currentTimeMillis();
        }

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            showTip("onError Code：" + error.getErrorCode());
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }

    };


    /**
     * EventBus 事件
     *
     * @param event 事件
     */
    public void onEvent(EventFKXX event) {
        if (event.anInt < 10) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(YybwfsActivity.this);
            dialog.setTitle("    ");
            dialog.setPositiveButton("确定", (dialog1, which) -> {
                if (exit_flog == 1) {
                    finish();
                }
            });

            switch (event.anInt) {
                case 0:
                    exit_flog = 1;
                    SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                    editor.putString("No" + DipperCom.S_SAVE_NUM, String.format("%04d", (DipperCom.S_SAVE_NUM + 1)) + "    发送     " + stringSendAddress + "      " + stringSendContent);
                    DipperCom.S_SAVE_NUM++;
                    editor.putInt("S_SAVE_NUM", DipperCom.S_SAVE_NUM);
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
                case 2:
                    progressDialog.dismiss();
                    dialog.setMessage("北斗模块暂无信号，请稍后重试");
                    dialog.show();
                    break;
                case 4:
                    progressDialog.dismiss();
                    dialog.setMessage("发送失败，发送太过频繁，请等候" + DipperCom.FSPDWD_TIME + "秒");
                    dialog.show();
                    break;
                case 9:
                    progressDialog.dismiss();
                    dialog.setMessage("发送超时，请检查北斗模块连接");
                    dialog.show();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_recognize:

                // 移动数据分析，收集开始听写事件
                FlowerCollector.onEvent(YybwfsActivity.this, "iat_recognize");
                // 清空显示内容
                mEtRecognizeResult.setText(null);
                startRecognize();

                showTip(getString(R.string.text_begin));
                break;
            case R.id.tv_send:
                byte i;
                stringSendAddress = mEtNumber.getText().toString();
                stringSendContent = mEtRecognizeResult.getText().toString();
                if (stringSendAddress.length() == 0) {
                    Toast.makeText(v.getContext(), "请输入号码", Toast.LENGTH_LONG).show();
                    break;
                }

                if (stringSendAddress.length() > MAX_NUM) {
                    Toast.makeText(v.getContext(), "请输入正确的号码", Toast.LENGTH_LONG).show();
                    break;
                }

                if (stringSendContent.length() == 0) {
                    Toast.makeText(v.getContext(), "请输入内容", Toast.LENGTH_LONG).show();
                    break;
                }

                showProcessDialog();
                int_send_address = Integer.parseInt(stringSendAddress);
                messageLen = stringSendContent.length();
                charSendContent = stringSendContent.toCharArray();
                sendLen = messageLen * 2 + 1 + 18;
                byteSendNumber = NumberUtil.numIntToByte(int_send_address);
                byteSendContent = NumberUtil.messageCharToByte(charSendContent, messageLen);

                new Thread(sendVoiceRunnable).start();
                new Thread(dialogCheckRunnable).start();
                KeyboardUtil.hideInput(this);
                Toast.makeText(v.getContext(), "输入内容的长度为" + messageLen, Toast.LENGTH_SHORT).show();
                Toast.makeText(v.getContext(), stringSendContent, Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_back:
                finish();
                break;
            default:
                break;
        }
    }

    private void showProcessDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(YybwfsActivity.this);
        }
        progressDialog.setTitle("正在发送");
        progressDialog.setMessage("请稍等");
        progressDialog.setCancelable(true);
        progressDialog.show();
    }

    private void showTip(final String str) {
        runOnUiThread(() -> {
            mToast.setText(str);
            mToast.show();
        });
    }

    /**
     * 设置参数
     *
     * @return true 成功；false 失败
     */
    private boolean setParams() {
        boolean res;
        // 清空参数
        mAsr.setParameter(SpeechConstant.PARAMS, null);
        // 设置引擎类型
        mAsr.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        // 设置资源路径
        mAsr.setParameter(ResourceUtil.ASR_RES_PATH, getResourcePath());
        // 设置文本编码格式
        mAsr.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        // 设置语法名称
        mAsr.setParameter(SpeechConstant.GRAMMAR_LIST, "call");

        // 设置语法构建路径
        mAsr.setParameter(ResourceUtil.GRM_BUILD_PATH, grmPath);
        mContent = new String(mLocalGrammar);
        ret = mAsr.buildGrammar(GRAMMAR_TYPE_BNF, mContent, grammarListener);
        if (ret != ErrorCode.SUCCESS) {
            showTip("语法构建失败,错误码：" + ret);
        }

        mContent = new String(mLocalLexicon);
        ret = mAsr.updateLexicon("contact", mContent, lexiconListener);
        if (ret != ErrorCode.SUCCESS) {
            showTip("更新词典失败,错误码：" + ret);
        }

        // 设置返回结果格式
        mAsr.setParameter(SpeechConstant.RESULT_TYPE, mResultType);
        // 设置本地识别使用语法id
        mAsr.setParameter(SpeechConstant.LOCAL_GRAMMAR, "call");
        // 设置识别的门限值
        mAsr.setParameter(SpeechConstant.ASR_THRESHOLD, "30");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mAsr.setParameter(SpeechConstant.VAD_EOS, "0");
        res = true;


        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mAsr.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mAsr.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/asr.wav");
        return res;
    }

    /**
     * 取消识别
     */
    private void cancelRecognize() {
        mAsr.cancel();
        showTip("取消识别");
    }

    /**
     * 停止识别
     */
    private void stopRecognize() {
        mAsr.stopListening();
        showTip("停止识别");
    }

    /**
     * 开始识别
     */
    private void startRecognize() {

        ret = mAsr.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            showTip("识别失败,错误码: " + ret);
        }
    }


    /**
     * 从外部存储中获取文件
     *
     * @param path 文件路径
     * @return 文件内容
     */
    public String getContentFromExternalStorage(String path) {
        String str = null;
        try {
            InputStream inputStream = new FileInputStream(new File(path));
            str = getString(inputStream);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return (str != null ? str.trim() : null) + "\n";
    }

    /**
     * 将输入流转换成String
     *
     * @param inputStream 输入流
     * @return string数据
     */
    public static String getString(InputStream inputStream) {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(inputStreamReader);
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    //获取识别资源路径
    private String getResourcePath() {
        //识别通用资源
        return ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, "asr/common.jet");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mAsr) {
            // 退出时释放连接
            mAsr.cancel();
            mAsr.destroy();
        }
    }

    @Override
    public void handlerMessage(Message msg) {
        if (msg.what == 253) {
            AlertDialog.Builder dialog2 = new AlertDialog.Builder(YybwfsActivity.this);
            dialog2.setTitle("    ");
            dialog2.setMessage("北斗模块未连接，请检测设备");
            AlertDialog alertDialog = dialog2.create();
            dialog2.setPositiveButton("确定", (dialog, which) -> {
                alertDialog.dismiss();
            });
            dialog2.show();
            progressDialog.dismiss();
        }
    }
}
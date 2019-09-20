package com.topeet.serialtest;

import android.app.Application;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

/**
 * @author jiazhu
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        初始化语音模块
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5cdabec1");
    }
}

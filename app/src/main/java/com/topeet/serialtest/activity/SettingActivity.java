package com.topeet.serialtest.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.x6.serial.SerialPort;
import com.example.x6.serial.SerialPortManager;
import com.topeet.serialtest.R;
import com.example.x6.serial.SerialPortParam;
import com.topeet.serialtest.util.KeyboardUtil;
import com.topeet.serialtest.util.NumberUtil;

import java.io.File;
import java.io.IOException;

/**
 * @author jiazhu
 */
public class SettingActivity extends AppCompatActivity {
    private EditText mEtBaudRate;
    private TextView mTvRevert;
    private TextView mTvSave;
    private TextView mTvBack;
    private String newBaudRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_port_test);

        initView();
        initData();
    }

    private void initView() {
        mEtBaudRate = findViewById(R.id.et_baudrate);
        mTvRevert = findViewById(R.id.tv_revert);
        mTvSave = findViewById(R.id.tv_save);
        mTvBack = findViewById(R.id.tv_back);

        mEtBaudRate.setText(String.valueOf(SerialPortParam.getInstance().getBaudRate()));
        newBaudRate = mEtBaudRate.getText().toString();
    }

    private void initData() {
        mTvBack.setOnClickListener(v -> finish());

        mTvSave.setOnClickListener(v -> {
            KeyboardUtil.hideInput(SettingActivity.this);
            if (NumberUtil.isNumeric(newBaudRate)) {
                SerialPortParam.getInstance().setBaudRate(Integer.valueOf(newBaudRate));
                try {
                    SerialPortManager.getInstance().close();
                    SerialPort serialPort = new SerialPort(new File(SerialPortParam.getInstance().getPathName()),
                            SerialPortParam.getInstance().getBaudRate(), 0);
                    SerialPortManager.getInstance().reload(serialPort);
                    Toast.makeText(SettingActivity.this, "波特率设置完成：" + newBaudRate, Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(SettingActivity.this, "非法波特率", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SettingActivity.this, "非法波特率", Toast.LENGTH_SHORT).show();
            }
        });

        mTvRevert.setOnClickListener(v -> {
            mEtBaudRate.setText(String.valueOf(SerialPortParam.DEFAULT_RATE));
            mTvSave.performClick();
        });

        mEtBaudRate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    mTvRevert.setClickable(true);
                    mTvSave.setClickable(true);
                } else {
                    mTvRevert.setClickable(false);
                    mTvSave.setClickable(false);
                }
                newBaudRate = s.toString();
            }
        });
    }
}

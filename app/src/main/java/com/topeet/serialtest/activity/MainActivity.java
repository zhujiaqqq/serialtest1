package com.topeet.serialtest.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.x6.serial.SerialPortManager;
import com.topeet.serialtest.DipperCom;
import com.topeet.serialtest.LocalHandler;
import com.topeet.serialtest.R;
import com.topeet.serialtest.adapter.ModuleListAdapter;
import com.topeet.serialtest.entity.ModuleBean;
import com.topeet.serialtest.eventbus.EventDWXX;
import com.topeet.serialtest.eventbus.EventFKXX;
import com.topeet.serialtest.eventbus.EventZJXX;
import com.topeet.serialtest.util.HexUtil;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * @author jiazhu
 */
public class MainActivity extends AppCompatActivity implements LocalHandler.IHandler {
    //    private boolean isFirst;
    public static final int XIN_TIAO = 1;
    public byte txsqCount = 0, dwsqCount = 0, xtzjCount = 0;

    private RecyclerView mRvModuleList;
    private TextView mTvTitle;

    private LocalHandler mHandler = new LocalHandler(this);

    private Runnable mRunnable = () -> {
        while (true) {
            byte[] rxBuff = new byte[500];
            int length = SerialPortManager.getInstance().read(rxBuff);

            byte b = DipperCom.XORCheck(rxBuff, length);

            if (length < 0) {
                return;
            }
            int res = DipperCom.comReceive(rxBuff, length);

            System.out.println("data: >>>>>>>" + HexUtil.byte2Hex(rxBuff));
            Message msg = mHandler.obtainMessage(XIN_TIAO);
            msg.arg1 = res;
            mHandler.sendMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();
    }

    private void initView() {

        mTvTitle = findViewById(R.id.tv_title);
        mRvModuleList = findViewById(R.id.rv_module_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);
        mRvModuleList.setLayoutManager(layoutManager);
        mRvModuleList.addItemDecoration(new DividerItemDecoration(MainActivity.this, DividerItemDecoration.VERTICAL));
    }

    private void initData() {

        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        DipperCom.S_SAVE_NUM = pref.getInt("S_SAVE_NUM", 0);

        mTvTitle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                return true;
            }
        });

        List<ModuleBean> list = new ArrayList<>();
        list.add(new ModuleBean("语音报文发送", R.drawable.ic_keyboard_voice_black_24dp, YybwfsActivity.class, null));
        list.add(new ModuleBean("北斗模块信息", R.drawable.ic_keyboard_voice_black_24dp, BdxxActivity.class, null));
        list.add(new ModuleBean("语音收发记录", R.drawable.ic_keyboard_voice_black_24dp, YysfjlActivity.class, null));
        list.add(new ModuleBean("系统状态信息", R.drawable.ic_keyboard_voice_black_24dp, XtxxActivity.class, null));
        ModuleListAdapter adapter = new ModuleListAdapter(list, MainActivity.this, new ModuleListAdapter.MyListener() {
            @Override
            public void onClick(ModuleBean bean) {
                Intent intent = new Intent(MainActivity.this, bean.getDesCls());
                startActivity(intent);
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
            }
        });
        mRvModuleList.setAdapter(adapter);

        new Thread(mRunnable).start();
    }


    @Override
    public void handlerMessage(Message msg) {
        if (msg.what == XIN_TIAO) {
            int res = msg.arg1;

            switch (res / 100) {
                case 1:
                    txsqCount = 0;
                    EventBus.getDefault().post(new EventFKXX(res - 100));
                    break;
                case 2:
                    Intent intent = new Intent(MainActivity.this, YybwjsActivity.class);
                    MainActivity.this.startActivity(intent);
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

        }
    }

    @Override
    protected void onDestroy() {
        SerialPortManager.getInstance().close();
        super.onDestroy();
    }
}

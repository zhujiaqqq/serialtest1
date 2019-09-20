package com.topeet.serialtest.activity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.topeet.serialtest.DipperCom;
import com.topeet.serialtest.R;
import com.topeet.serialtest.adapter.ModuleListAdapter;
import com.topeet.serialtest.entity.ModuleBean;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
//    private boolean isFirst;

    private RecyclerView mRvModuleList;
    private ModuleListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();
    }

    private void initView() {
        mRvModuleList = findViewById(R.id.rv_module_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);
        mRvModuleList.setLayoutManager(layoutManager);
        mRvModuleList.addItemDecoration(new DividerItemDecoration(MainActivity.this,DividerItemDecoration.VERTICAL));
    }

    private void initData() {
//        if (!isFirst) {
//            isFirst = true;
//            Intent intent = new Intent(MainActivity.this, YybwjsActivity.class);
//            startActivity(intent);
//        }

        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        DipperCom.S_SAVE_NUM = pref.getInt("S_SAVE_NUM", 0);

//        Intent start_Intent = new Intent(this, DipperComService.class);
//        startService(start_Intent);

        List<ModuleBean> list = new ArrayList<>();
        list.add(new ModuleBean("语音报文发送", R.drawable.ic_keyboard_voice_black_24dp, YybwfsActivity.class, null));
        list.add(new ModuleBean("北斗模块信息", R.drawable.ic_keyboard_voice_black_24dp, BdxxActivity.class, null));
        list.add(new ModuleBean("语音收发记录", R.drawable.ic_keyboard_voice_black_24dp, YysfjlActivity.class, null));
        list.add(new ModuleBean("系统状态信息", R.drawable.ic_keyboard_voice_black_24dp, XtxxActivity.class, null));
        mAdapter = new ModuleListAdapter(list, MainActivity.this, new ModuleListAdapter.MyListener() {
            @Override
            public void onClick(ModuleBean bean) {
                Intent intent = new Intent(MainActivity.this, bean.getDesCls());
                startActivity(intent);
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
            }
        });
        mRvModuleList.setAdapter(mAdapter);
    }

}

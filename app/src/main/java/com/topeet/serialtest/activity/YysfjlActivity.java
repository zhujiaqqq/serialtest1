package com.topeet.serialtest.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.topeet.serialtest.DipperCom;
import com.topeet.serialtest.R;


public class YysfjlActivity extends AppCompatActivity implements View.OnClickListener{

    ArrayAdapter<String> adapter;
    SharedPreferences pref;
    String[] save_data;

    ListView list_sfjl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yysfjl);

        int num = 0;
        int i = 0;

        pref = getSharedPreferences("data", MODE_PRIVATE);
        num = DipperCom.S_SAVE_NUM;
        save_data = new String[num+1];
        save_data[0] = "序号     收发     对方号码    语音内容";
        if(num != 0){
            for (i = 0; i < num; i++){
                save_data[i+1] = pref.getString("No"+i,"");
            }
        }

       //
       //save_data[1] = "收发       对方号码    语音内容";
       //save_data[2] = "收发       对方号码    语音内容";
       //save_data[3] = "收发       对方号码    语音内容语音内容语音内容语音内容语音内容语音内容语音内容语音内容";
       //save_data[4] = "收发       对方号码    语音内容";
       //save_data[5] = "收发       对方号码    语音内容";
       //save_data[6] = "收发       对方号码    语音内容";
       //save_data[7] = "收发       对方号码    语音内容";
       //save_data[8] = "收发       对方号码    语音内容";
       //save_data[9] = "收发       对方号码    语音内容";
       //save_data[10] = "收发       对方号码    语音内容";
       //save_data[11] = "收发       对方号码    语音内容";
       //save_data[12] = "收发       对方号码    语音内容";
       //save_data[13] = "收发       对方号码    语音内容";
       //save_data[14] = "收发       对方号码    语音内容";

        adapter = new ArrayAdapter<String>(YysfjlActivity.this,android.R.layout.simple_expandable_list_item_1, save_data);
        list_sfjl = (ListView)findViewById(R.id.list_sfjl);
        list_sfjl.setAdapter(adapter);

        Button button_Fhsyj = (Button) findViewById(R.id.button_YysfjlFhzjm);
        Button button_Qcsfjl = (Button)findViewById(R.id.button_YysfjlQcsfjl);

        button_Fhsyj.setOnClickListener(this);
        button_Qcsfjl.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_YysfjlFhzjm:
                finish();
                break;
            case R.id.button_YysfjlQcsfjl:
                DipperCom.S_SAVE_NUM = 0;
                SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();
                editor.putInt("S_SAVE_NUM", DipperCom.S_SAVE_NUM);
                editor.apply();
                save_data = new String[1];
                save_data[0] = "序号     收发     对方号码    语音内容";
                adapter = new ArrayAdapter<String>(YysfjlActivity.this,android.R.layout.simple_expandable_list_item_1, save_data);
                list_sfjl = (ListView)findViewById(R.id.list_sfjl);
                list_sfjl.setAdapter(adapter);
                break;
            default:
                break;
        }
    }
}

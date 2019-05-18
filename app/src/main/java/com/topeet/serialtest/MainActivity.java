package com.topeet.serialtest;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static int first_in = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (first_in == 0) {
            first_in = 1;
            Intent intent = new Intent(MainActivity.this, Yybwjs_Activity.class);
            startActivity(intent);
        }


        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        DipperCom.save_num = pref.getInt("save_num", 0);
        //DipperCom.save_num = 0;

        Intent start_Intent = new Intent(this, DipperComService.class);
        startService(start_Intent);

        Button button_Bdxx = (Button) findViewById(R.id.button_bdxx);
        Button button_Xtxx = (Button) findViewById(R.id.button_xtxx);
        Button button_Yybwfs = (Button) findViewById(R.id.button_yybwfs);
        Button button_Yysfjl = (Button) findViewById(R.id.button_yysfjl);
        button_Bdxx.setOnClickListener(this);
        button_Xtxx.setOnClickListener(this);
        button_Yybwfs.setOnClickListener(this);
        button_Yysfjl.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.button_yybwfs:
                intent = new Intent(MainActivity.this, Yybwfs_Activity.class);
                startActivity(intent);
                break;
            case R.id.button_bdxx:
                intent = new Intent(MainActivity.this, Bdxx_Activity.class);
                startActivity(intent);
                break;
            case R.id.button_yysfjl:
                intent = new Intent(MainActivity.this, Yysfjl_Activity.class);
                startActivity(intent);
                break;
            case R.id.button_xtxx:
                intent = new Intent(MainActivity.this, Xtxx_Activity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

}

package com.hongsi.babyinpalm.Controller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.hongsi.babyinpalm.R;
import com.hongsi.babyinpalm.Utils.BaseActivity;
import com.hongsi.babyinpalm.Utils.Component.CustomApplication;
import com.hongsi.babyinpalm.Utils.ImageLoader;
import com.hongsi.babyinpalm.Utils.LogUtil;

import java.io.IOException;


/**
 * Created by Administrator on 2016/9/26.
 *
 */
public class ActivityScreen extends BaseActivity{

    private final static String TAG = "ActivityScreen";

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置布局
        setContentView(R.layout.screen_layout);


        handler.postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent toMainActivityIntent = new Intent(ActivityScreen.this, ActivityLogin.class);

                startActivity(toMainActivityIntent);
                ActivityScreen.this.finish();
            }
        },3000);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

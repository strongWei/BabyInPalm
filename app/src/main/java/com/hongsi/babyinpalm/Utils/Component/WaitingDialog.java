package com.hongsi.babyinpalm.Utils.Component;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.StringRes;
import android.widget.ImageView;
import android.widget.TextView;

import com.hongsi.babyinpalm.R;
import com.hongsi.babyinpalm.Utils.LogUtil;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by Administrator on 2016/6/13.
 * -------------------------------------------------------------------------------------------------
 * detail: load dialog
 * author: strong
 * version: 1.0.0
 */
public class WaitingDialog extends Dialog {

    private static final int UPDATE_FIRST_DOT = 0;
    private static final int UPDATE_SECOND_DOT = 1;
    private static final int UPDATE_THIRD_DOT = 2;
    private static final int CLEAR_ALL_DOT = 3;

    private TextView titleView = null;
    private ImageView oneDot =  null;
    private ImageView secondDot = null;
    private ImageView thirdDot = null;
    private Timer animateTimer = null;

    private int currentDot = 0;

    private Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_FIRST_DOT:
                    oneDot.setBackgroundResource(R.drawable.circle_dot_blue_shape);
                    break;
                case UPDATE_SECOND_DOT:
                    secondDot.setBackgroundResource(R.drawable.circle_dot_blue_shape);
                    break;
                case UPDATE_THIRD_DOT:
                    thirdDot.setBackgroundResource(R.drawable.circle_dot_blue_shape);
                    break;
                case CLEAR_ALL_DOT:
                    initDot();
                    break;
            }
        }
    };

    public WaitingDialog(Context context, int themeResId) {
        super(context, themeResId);

        setContentView(R.layout.waiting_layout);

        //init variant
        titleView = (TextView) findViewById(R.id.notice);
        oneDot = (ImageView) findViewById(R.id.one_dot);
        secondDot = (ImageView) findViewById(R.id.second_dot);
        thirdDot = (ImageView) findViewById(R.id.third_dot);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setText(@StringRes int res){
        titleView.setText(res);
    }

    //init dot to origin status
    private void initDot(){
        oneDot.setBackgroundResource(R.drawable.circle_dot_gray_shape);
        secondDot.setBackgroundResource(R.drawable.circle_dot_gray_shape);
        thirdDot.setBackgroundResource(R.drawable.circle_dot_gray_shape);
    }

    //stop the animate after the dialog is dismiss
    public void stopAnimate(){
        animateTimer.cancel();
        animateTimer.purge();
        initDot();
    }


    //start the animate
    public void startAnimate(){

        initDot();


        LogUtil.d(getClass().getSimpleName(),"start animate!");


        animateTimer = null;
        System.gc();
        animateTimer = new Timer();

        animateTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                LogUtil.d(getClass().getSimpleName(),"" + currentDot);


                switch (currentDot % 4){

                    case 0: {
                        if (currentDot != 0) {
                            currentDot = 0;
                        }

                        Message msg = new Message();
                        msg.what = UPDATE_FIRST_DOT;
                        handler.sendMessage(msg);
                    }

                        break;
                    case 1: {
                        Message msg = new Message();
                        msg.what = UPDATE_SECOND_DOT;
                        handler.sendMessage(msg);
                    }
                        break;
                    case 2: {
                        Message msg = new Message();
                        msg.what = UPDATE_THIRD_DOT;
                        handler.sendMessage(msg);
                    }
                        break;

                    case 3: {
                        Message msg = new Message();
                        msg.what = CLEAR_ALL_DOT;
                        handler.sendMessage(msg);
                    }
                        break;
                }

                ++ currentDot;
            }
        },500,1000);
    }

    @Override
    public void onBackPressed() {
        //do nothing

        //TODO:test
        //super.onBackPressed();
        //stopAnimate();
    }
}

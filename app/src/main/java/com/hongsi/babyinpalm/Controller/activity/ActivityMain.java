package com.hongsi.babyinpalm.Controller.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hongsi.babyinpalm.Controller.fragment.HomePageFragment;
import com.hongsi.babyinpalm.Controller.fragment.SetPageFragment;
import com.hongsi.babyinpalm.Controller.fragment.TalkPageFragment;
import com.hongsi.babyinpalm.R;
import com.hongsi.babyinpalm.Utils.ActivityCollector;
import com.hongsi.babyinpalm.Utils.BaseActivity;
import com.hongsi.babyinpalm.Utils.Component.CustomApplication;
import com.hongsi.babyinpalm.Utils.Component.WaitingDialog;
import com.hongsi.babyinpalm.Utils.LogUtil;
import com.hongsi.babyinpalm.Utils.ToastUtil;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2016/9/28.
 */
public class ActivityMain extends BaseActivity implements View.OnClickListener {

    private final static String TAG = "ActivityMain";

    /** 是否要退出程序 */
    private boolean backPressNum = false;

    /** 碎片切换管理器 */
    private FragmentManager fragManager = null;

    /** 互相切换的三个碎片*/
    private HomePageFragment homePageFragment = null;
    private SetPageFragment setPageFragment = null;
    private TalkPageFragment talkPageFragment = null;

    /** 三个按纽 */
    private RelativeLayout setLayout = null;
    private RelativeLayout homeLayout = null;
    private RelativeLayout talkLayout = null;

    //menu: button
    private ImageView homeButton = null;
    private ImageView setButton = null;
    private ImageView talkButton = null;

    //menu: text
    private TextView homeText = null;
    private TextView setText = null;
    private TextView talkText = null;

    private int grey_text;
    private int green_text;

    /** TAG 名称 */
    private String homeGrayTag = R.mipmap.home_gray + "";
    private String homeGreenTag = R.mipmap.home_green + "";
    private String setGrayTag = R.mipmap.set_gray + "";
    private String setGreenTag = R.mipmap.set_green + "";
    private String talkGrayTag = R.mipmap.talk_gray + "";
    private String talkGreenTag = R.mipmap.talk_green + "";

    /** 当前显示的是哪个碎片*/
    private int currentControlPage = 1;         //pointer to current control page

    /** 当前显示的是什么主图标 */
    private ImageView currentMainPage = null;   //the big icon pointed to current control page


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_layout);

        initUi();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        homePageFragment = null;
        setPageFragment = null;
        talkPageFragment = null;
    }

    /** 初始化界面*/
    private void initUi(){

        //初始化三个碎片
        homePageFragment = new HomePageFragment();
        setPageFragment = new SetPageFragment();
        talkPageFragment = new TalkPageFragment();

        fragManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragManager.beginTransaction();
        fragmentTransaction.add(R.id.frame_layout,setPageFragment);
        fragmentTransaction.add(R.id.frame_layout,homePageFragment);
        fragmentTransaction.add(R.id.frame_layout,talkPageFragment);

        //使用show hide是无法触发特效的
        fragmentTransaction.show(homePageFragment);
        fragmentTransaction.hide(setPageFragment);
        fragmentTransaction.hide(talkPageFragment);
        fragmentTransaction.commit();

        //初始化三个按纽
        setLayout = (RelativeLayout) findViewById(R.id.set_page);
        homeLayout = (RelativeLayout) findViewById(R.id.home_page);
        talkLayout = (RelativeLayout) findViewById(R.id.talk_page);
        homeButton = (ImageView) findViewById(R.id.home_image);
        setButton = (ImageView) findViewById(R.id.set_image);
        talkButton = (ImageView) findViewById(R.id.talk_image);
        homeText = (TextView) findViewById(R.id.home_text);
        setText = (TextView) findViewById(R.id.set_text);
        talkText = (TextView) findViewById(R.id.talk_text);

        setLayout.setOnClickListener(this);
        homeLayout.setOnClickListener(this);
        talkLayout.setOnClickListener(this);

        //color resource init
        grey_text = getResources().getColor(R.color.gray_text);
        green_text = getResources().getColor(R.color.green_text);

        currentMainPage = (ImageView) findViewById(R.id.currentMainPage);
    }

    @Override
    public void onBackPressed(){
        //if load dialog is showing, do nothing
        if(!backPressNum){
            //Toast.makeText(this,R.string.backpress_quit_text,Toast.LENGTH_SHORT).show();

            ToastUtil.showToast(this,R.string.backpress_quit_text, Toast.LENGTH_SHORT);

            backPressNum = true;

            //after 3 seconds, if you don't press 'back' again, cancel the command of quitting app
            Timer initBackPressNumTimer = new Timer();
            initBackPressNumTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    backPressNum = false;
                }
            },2000);

            return;
        }

        if(backPressNum){
            ActivityCollector.finishAllActivity();
        }
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.set_page: {
                    if (currentControlPage == 2) {
                        return;
                    } else if (currentControlPage == 0) {
                        talkButton.setTag(talkGrayTag);

                        CustomApplication.getImageLoader().loadLocalImage(talkGrayTag, R.mipmap.talk_gray, getBaseContext(), talkButton);

                        talkText.setTextColor(grey_text);


                    } else if (currentControlPage == 1) {
                        homeButton.setTag(homeGrayTag);
                        CustomApplication.getImageLoader().loadLocalImage(homeGrayTag, R.mipmap.home_gray, getBaseContext(), homeButton);
                        homeText.setTextColor(grey_text);

                    }

                    FragmentTransaction fragmentTransaction = fragManager.beginTransaction();

                    fragmentTransaction.hide(homePageFragment);
                    fragmentTransaction.hide(talkPageFragment);
                    fragmentTransaction.show(setPageFragment);
                    //fragmentTransaction.setCustomAnimations(R.animator.zoom_in, R.animator.zoom_out);
                    fragmentTransaction.commitAllowingStateLoss();

                    currentControlPage = 2;
                    setButton.setTag(setGreenTag);
                    CustomApplication.getImageLoader().loadLocalImage(setGreenTag, R.mipmap.set_green, getBaseContext(), setButton);
                    setText.setTextColor(green_text);
                    currentMainPage.setTag(setGreenTag);
                    CustomApplication.getImageLoader().loadLocalImage(setGreenTag, R.mipmap.set_green, getBaseContext(), currentMainPage);
                }
                break;

                case R.id.home_page: {
                    if (currentControlPage == 1) {
                        return;
                    } else if (currentControlPage == 0) {
                        talkButton.setTag(talkGrayTag);
                        CustomApplication.getImageLoader().loadLocalImage(talkGrayTag, R.mipmap.talk_gray, getBaseContext(), talkButton);
                        //talkButton.setImageResource(R.mipmap.talk_gray);
                        talkText.setTextColor(grey_text);


                    } else if (currentControlPage == 2) {
                        setButton.setTag(setGrayTag);
                        CustomApplication.getImageLoader().loadLocalImage(setGrayTag, R.mipmap.set_gray, getBaseContext(), setButton);
                        //setButton.setImageResource(R.mipmap.set_gray);
                        setText.setTextColor(grey_text);
                    }

                    FragmentTransaction fragmentTransaction = fragManager.beginTransaction();
                    fragmentTransaction.hide(talkPageFragment);
                    fragmentTransaction.hide(setPageFragment);
                    fragmentTransaction.show(homePageFragment);
                    //fragmentTransaction.setCustomAnimations(R.animator.zoom_in, R.animator.zoom_out);
                    fragmentTransaction.commitAllowingStateLoss();

                    currentControlPage = 1;
                    homeButton.setTag(homeGreenTag);
                    CustomApplication.getImageLoader().loadLocalImage(homeGreenTag, R.mipmap.home_green, getBaseContext(), homeButton);
                    homeText.setTextColor(green_text);
                    currentMainPage.setTag(homeGreenTag);
                    CustomApplication.getImageLoader().loadLocalImage(homeGreenTag, R.mipmap.home_green, getBaseContext(), currentMainPage);
                }
                break;

                case R.id.talk_page: {
                    if (currentControlPage == 0) {
                        return;
                    } else if (currentControlPage == 2) {
                        setButton.setTag(setGrayTag);
                        CustomApplication.getImageLoader().loadLocalImage(setGrayTag, R.mipmap.set_gray, getBaseContext(), setButton);
                        setText.setTextColor(grey_text);

                    } else if (currentControlPage == 1) {
                        homeButton.setTag(homeGrayTag);
                        CustomApplication.getImageLoader().loadLocalImage(homeGrayTag, R.mipmap.home_gray, getBaseContext(), homeButton);
                        homeText.setTextColor(grey_text);
                    }

                    FragmentTransaction fragmentTransaction = fragManager.beginTransaction();
                    fragmentTransaction.hide(setPageFragment);
                    fragmentTransaction.hide(homePageFragment);
                    fragmentTransaction.show(talkPageFragment);
                    //fragmentTransaction.setCustomAnimations(R.animator.zoom_in, R.animator.zoom_out);
                    fragmentTransaction.commitAllowingStateLoss();

                    currentControlPage = 0;
                    talkButton.setTag(talkGreenTag);
                    CustomApplication.getImageLoader().loadLocalImage(talkGreenTag, R.mipmap.talk_green, getBaseContext(), talkButton);
                    talkText.setTextColor(green_text);
                    currentMainPage.setTag(talkGreenTag);
                    CustomApplication.getImageLoader().loadLocalImage(talkGreenTag, R.mipmap.talk_green, getBaseContext(), currentMainPage);
                }
                break;
            }
        }catch (IOException e){
            LogUtil.e(TAG,e.getMessage());
        }
    }

}

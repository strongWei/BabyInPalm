package com.hongsi.babyinpalm.Controller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hongsi.babyinpalm.Exception.NetworkErrorException;
import com.hongsi.babyinpalm.Exception.OtherIOException;
import com.hongsi.babyinpalm.Model.Login;
import com.hongsi.babyinpalm.R;
import com.hongsi.babyinpalm.Utils.ActivityCollector;
import com.hongsi.babyinpalm.Utils.BaseActivity;
import com.hongsi.babyinpalm.Utils.Component.WaitingDialog;
import com.hongsi.babyinpalm.Utils.LogUtil;
import com.hongsi.babyinpalm.Utils.ToastUtil;

import org.json.JSONException;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by Administrator on 2016/9/26.
 *
 * 登陆活动
 */
public class ActivityLogin extends BaseActivity implements View.OnClickListener {

    /** 是否要退出程序 */
    private boolean backPressNum = false;

    private final static String TAG = "ActivityLogin";

    //登陆按纽
    private Button loginBtn = null;

    //输入手机号
    private EditText phoneTextEdit = null;

    //输入密码
    private EditText passwordTextEdit = null;


    //等待窗口
    private WaitingDialog waitingDialog = null;

    /** 用于处理线程与ui交互的handler */
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){

                case 10:{
                    Intent intent = new Intent(ActivityLogin.this,ActivityMain.class);
                    startActivity(intent);
                    ActivityLogin.this.finish();

                    //4.关闭等待窗口
                    waitingDialog.dismiss();
                    waitingDialog.stopAnimate();
                }
                    break;

                case 11:{
                    StringBuffer errorStr = new StringBuffer(getResources().getString(R.string.login_fail));
                    errorStr.append(getResources().getString(R.string.login_error));

                    ToastUtil.showToast(ActivityLogin.this,errorStr.toString(),Toast.LENGTH_SHORT);

                    //4.关闭等待窗口
                    waitingDialog.dismiss();
                    waitingDialog.stopAnimate();
                }
                    break;

                case 12:{
                    StringBuffer errorStr = new StringBuffer(getResources().getString(R.string.login_fail));
                    errorStr.append(getResources().getString(R.string.server_error));

                    ToastUtil.showToast(ActivityLogin.this,errorStr.toString(),Toast.LENGTH_SHORT);

                    //4.关闭等待窗口
                    waitingDialog.dismiss();
                    waitingDialog.stopAnimate();
                }
                break;

                case -11:{
                    StringBuffer errorStr = new StringBuffer(getResources().getString(R.string.login_fail));
                    errorStr.append(getResources().getString(R.string.net_error));

                    ToastUtil.showToast(ActivityLogin.this,errorStr.toString(),Toast.LENGTH_SHORT);

                    //4.关闭等待窗口
                    waitingDialog.dismiss();
                    waitingDialog.stopAnimate();
                }
                    break;

                case -12:{
                    StringBuffer errorStr = new StringBuffer(getResources().getString(R.string.login_fail));
                    errorStr.append(getResources().getString(R.string.data_error));

                    ToastUtil.showToast(ActivityLogin.this,errorStr.toString(),Toast.LENGTH_SHORT);

                    //4.关闭等待窗口
                    waitingDialog.dismiss();
                    waitingDialog.stopAnimate();
                }
                    break;

                case -13:{
                    StringBuffer errorStr = new StringBuffer(getResources().getString(R.string.login_fail));
                    errorStr.append(getResources().getString(R.string.other_error));

                    ToastUtil.showToast(ActivityLogin.this,errorStr.toString(),Toast.LENGTH_SHORT);

                    //4.关闭等待窗口
                    waitingDialog.dismiss();
                    waitingDialog.stopAnimate();
                }
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_btn:{

                //2.进行输入字符串的检查
                String phone = phoneTextEdit.getText().toString().trim();
                String password = passwordTextEdit.getText().toString();

                if(!checkFormData(phone,password)){
                    return;
                }

                //1.显示加载页面
                if(waitingDialog == null){
                    waitingDialog = new WaitingDialog(this,R.style.DialogStyle);
                }

                waitingDialog.show();
                waitingDialog.startAnimate();

                //3.发送远程请求
                loginForm(phone,password);

            }
                break;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        initActivity();

        /** 检查一下是否有新版本要更新*/
        //checkUpdateSoftware();

        /** 读取已保存的用户名和密码 */
        readUserInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(waitingDialog != null){
            if(waitingDialog.isShowing()){
                waitingDialog.stopAnimate();
                waitingDialog.dismiss();
            }
        }


        waitingDialog = null;
    }

    /** 界面初始化 */
    private void initActivity(){
        loginBtn = (Button) findViewById(R.id.login_btn);
        phoneTextEdit = (EditText) findViewById(R.id.login_username);
        passwordTextEdit = (EditText) findViewById(R.id.login_password);

        //禁用登陆按纽
        loginBtn.setEnabled(true);

        loginBtn.setOnClickListener(this);

    }


    /** 检查form字段是否合法*/
    private boolean checkFormData(String phone,String password){
        //1. the phone is empty
        if(phone.isEmpty()){
            ToastUtil.showToast(ActivityLogin.this,R.string.login_username_empty, Toast.LENGTH_SHORT);
            return false;
        }

        //2. the password is empty
        if(password.isEmpty()){
            ToastUtil.showToast(ActivityLogin.this,R.string.login_password_empty, Toast.LENGTH_SHORT);
            return false;
        }

        //3. the phone is invalid
        if(!phone.matches("^\\d{11}$")){
            ToastUtil.showToast(ActivityLogin.this,R.string.login_username_invalid, Toast.LENGTH_SHORT);
            return false;
        }

        //4. the password's character is less than 8
        if(password.length() < 8){
            ToastUtil.showToast(ActivityLogin.this,R.string.login_password_less_8, Toast.LENGTH_SHORT);
            return false;
        }

        return true;
    }

    /** 登陆 */
    private void loginForm(final String phone,final String password){

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    int loginRet = Login.login(phone,password);

                    switch (loginRet){
                        case 0:
                        case 1:
                            handler.sendEmptyMessage(10);
                            break;

                        case -1:
                            handler.sendEmptyMessage(11);
                            break;

                        case -2:
                            handler.sendEmptyMessage(12);
                            break;
                    }

                } catch (OtherIOException e) {
                    //e.printStackTrace();
                    handler.sendEmptyMessage(-13);

                    LogUtil.e(TAG,"other_error when login");

                } catch (NetworkErrorException e) {
                    //e.printStackTrace();
                    //handler.sendEmptyMessage(-11);

                    handler.sendEmptyMessage(-11);

                    LogUtil.e(TAG,"net_error when login");

                } catch (JSONException e) {
                    //e.printStackTrace();
                    handler.sendEmptyMessage(-12);

                    LogUtil.e(TAG,"json_error when login");
                }
            }
        }).start();

    }

    @Override
    public void onBackPressed() {

        //if load dialog is showing, do nothing
        if(!backPressNum){

            ToastUtil.showToast(this,R.string.backpress_quit_text,Toast.LENGTH_SHORT);

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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    /** 读取用户名和密码*/
    private void readUserInfo(){
        phoneTextEdit.setText(Login.user.getPhone());
        passwordTextEdit.setText(Login.user.getPassword());
    }

}

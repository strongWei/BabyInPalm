package com.hongsi.babyinpalm.Controller.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hongsi.babyinpalm.Exception.NetworkErrorException;
import com.hongsi.babyinpalm.Exception.OtherIOException;
import com.hongsi.babyinpalm.Interface.DialogListener;
import com.hongsi.babyinpalm.Model.Login;
import com.hongsi.babyinpalm.Model.SoftwareUpdate;
import com.hongsi.babyinpalm.R;
import com.hongsi.babyinpalm.Utils.ActivityCollector;
import com.hongsi.babyinpalm.Utils.BaseActivity;
import com.hongsi.babyinpalm.Utils.Component.CustomApplication;
import com.hongsi.babyinpalm.Utils.Component.ProgressbarDialog;
import com.hongsi.babyinpalm.Utils.Component.WaitingDialog;
import com.hongsi.babyinpalm.Utils.Component.WarningDialog;
import com.hongsi.babyinpalm.Utils.LogUtil;
import com.hongsi.babyinpalm.Utils.ToastUtil;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.NumberFormat;
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

    //提示窗口
    private WarningDialog warningDialog = null;

    //更新进度窗口
    private ProgressbarDialog progressbarDialog = null;

    //下载apk线程
    private DownloadApk downloadApk = null;

    //等待窗口
    private WaitingDialog waitingDialog = null;

    /** 用于处理线程与ui交互的handler */
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 0:{
                    //需要更新版本，显示窗口
                    warningDialog = new WarningDialog(ActivityLogin.this, R.style.DialogStyle, new DialogListener() {
                        @Override
                        public void getResultBoolean(boolean b) {
                            if(b){
                                //点击了确定按纽，显示进度条的窗口
                                progressbarDialog = new ProgressbarDialog(ActivityLogin.this,R.style.DialogStyle);
                                progressbarDialog.setProgressBarValue(0);
                                progressbarDialog.showText("准备中...");
                                progressbarDialog.show();

                                //开始执行下载任务
                                downloadApk = new DownloadApk();
                                downloadApk.execute(SoftwareUpdate.softwareUrl);

                            }else{
                                //点击了取消按纽
                                ActivityLogin.this.loginBtn.setEnabled(true);
                            }

                            warningDialog.dismiss();
                        }
                    });

                    warningDialog.setWarnText(R.string.software_update_text);
                    warningDialog.show();

                }
                    break;

                case 1:{
                    //无需更新
                    loginBtn.setEnabled(true);
                }
                    break;

                case -1:{
                    ToastUtil.showToast(ActivityLogin.this,R.string.net_error, Toast.LENGTH_SHORT);
                }
                    break;

                case -2:{
                    ToastUtil.showToast(ActivityLogin.this,R.string.other_error,Toast.LENGTH_SHORT);
                }
                    break;

                case -3:{
                    ToastUtil.showToast(ActivityLogin.this,R.string.data_error, Toast.LENGTH_SHORT);
                }
                    break;

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

    /** 用于下载apk的类 */
    class DownloadApk extends AsyncTask<String,Integer,Integer>{
        private final String TAG = "DownloadApk";
        private File apkFile;

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressbarDialog.setProgressBarMax(values[1]);
            progressbarDialog.setProgressBarValue(values[0]);

            double result = (double)values[0] / values[1];

            LogUtil.i(TAG,result + " " + values[0] + " / " +  values[1]);

            progressbarDialog.showText("当前进度："+ NumberFormat.getPercentInstance().format(result));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //开始下载
            progressbarDialog.showText("开始下载");
        }

        @Override
        protected Integer doInBackground(String... params) {

            int currentFileSize = 0;

            try {
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){

                    StringBuffer pathBuffer = new StringBuffer();
                    pathBuffer.append(Environment.getExternalStorageDirectory().getAbsolutePath());
                    pathBuffer.append(File.separator);
                    pathBuffer.append("download");

                    //创建一个download文件夹
                    File savePath = new File(pathBuffer.toString());
                    if(!savePath.exists()){
                        savePath.mkdir();
                    }

                    HttpURLConnection conn = (HttpURLConnection) new URL(params[0]).openConnection();
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                    conn.setRequestProperty("Accept-Language","zh-CN,en-US;q=0.7,en;q=0.3");
                    conn.setRequestProperty("User-Agent", CustomApplication.getUserAgent());

                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);

                    conn.connect();

                    //获取文件大小
                    int fileLength = new Integer(conn.getHeaderField("appFileSize")).intValue();

                    //获取输入流
                    InputStream in = conn.getInputStream();

                    apkFile = new File(savePath,"BabyInPalm.apk");
                    if(apkFile.exists()){
                       apkFile.delete();
                    }

                    //获取输出流
                    FileOutputStream out = new FileOutputStream(apkFile);

                    //写到文件中去
                    byte[] fileBuffer = new byte[1024];

                    int num = 0;
                    while((num=in.read(fileBuffer))!=-1 || isCancelled()){
                        LogUtil.i(TAG,"num"+num);

                        out.write(fileBuffer,0,num);
                        currentFileSize += num;
                        publishProgress(currentFileSize,fileLength);
                    }

                    in.close();
                    out.close();

                    conn.disconnect();


                }else {
                    //存储设备异常
                    return 1;
                }

            }catch (UnknownHostException e){
                //e.printStackTrace();

                LogUtil.e(TAG,e.getMessage());

                //服务器不存在
                return 2;

            }catch (ConnectException e){
                //e.printStackTrace();

                LogUtil.e(TAG,e.getMessage());

                //连接失败
                return 3;

            }catch(SocketTimeoutException e){
                //e.printStackTrace();

                LogUtil.e(TAG,e.getMessage());

                //连接超时
                return 4;

            }catch (IOException e) {
                //e.printStackTrace();

                LogUtil.e(TAG,e.getMessage());

                //未知的
                return 5;
            }

            return 0;
        }
        @Override
        protected void onPostExecute(Integer i) {
            super.onPostExecute(i);

            int result = i.intValue();

            if(result !=0){
                //更新出现了错误

                progressbarDialog.dismiss();

                if(result == 1) {
                    StringBuffer errorStr = new StringBuffer(getResources().getString(R.string.software_update_fail));
                    errorStr.append(getResources().getString(R.string.storage_error));
                    ToastUtil.showToast(ActivityLogin.this, errorStr.toString(),Toast.LENGTH_SHORT);
                }else if(result == 5){

                    StringBuffer errorStr = new StringBuffer(getResources().getString(R.string.software_update_fail));
                    errorStr.append(getResources().getString(R.string.other_error));
                    ToastUtil.showToast(ActivityLogin.this, errorStr.toString(),Toast.LENGTH_SHORT);
                }else{

                    StringBuffer errorStr = new StringBuffer(getResources().getString(R.string.software_update_fail));
                    errorStr.append(getResources().getString(R.string.net_error));
                    ToastUtil.showToast(ActivityLogin.this, errorStr.toString(),Toast.LENGTH_SHORT);
                }

            }else{
                //直接安装应用程序
                progressbarDialog.dismiss();

                installApk();
            }
        }

        /** 安装apk */
        private void installApk(){
            if(!apkFile.exists()){
                return;
            }

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setDataAndType(Uri.parse("file://" + apkFile.getAbsolutePath()), "application/vnd.android.package-archive");
            ActivityLogin.this.startActivity(i);

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        initActivity();

        /** 检查一下是否有新版本要更新*/
        checkUpdateSoftware();

        /** 读取已保存的用户名和密码 */
        readUserInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //dialog必须交由此处管理或Activity
        if(warningDialog != null){
            if(warningDialog.isShowing()){
                warningDialog.dismiss();
            }
        }

        if(progressbarDialog != null){
            if(progressbarDialog.isShowing()){
                progressbarDialog.dismiss();
            }
        }

        if(waitingDialog != null){
            if(waitingDialog.isShowing()){
                waitingDialog.stopAnimate();
                waitingDialog.dismiss();
            }
        }

        //销毁时必须终止该线程，如果线程还在执行
        if(downloadApk!=null ){
            if(downloadApk.getStatus() == AsyncTask.Status.RUNNING){
                downloadApk.cancel(true);
            }
        }

        warningDialog = null;
        progressbarDialog = null;
        waitingDialog = null;
        downloadApk = null;
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

    /** 检查是否需要更新软件 */
    private void checkUpdateSoftware(){
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    boolean needUpdate = SoftwareUpdate.softwareUpdate();

                    if(needUpdate){
                        //需要更新软件
                        handler.sendEmptyMessage(0);
                    }else{
                        //不需要更新软件
                        handler.sendEmptyMessage(1);
                    }

                } catch (NetworkErrorException e) {
                    //网络异常
                    e.printStackTrace();
                    LogUtil.e(TAG,"network_error when check update software");

                    handler.sendEmptyMessage(-1);

                }catch (OtherIOException e) {
                    //未知异常
                    e.printStackTrace();
                    LogUtil.e(TAG,"other_error when check update software");

                    handler.sendEmptyMessage(-2);

                } catch (JSONException e) {
                    //数据异常
                    e.printStackTrace();
                    LogUtil.e(TAG,"json_error when check update software");

                    handler.sendEmptyMessage(-3);
                }
            }
        }).start();
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

                    handler.sendEmptyMessage(10);

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

package com.hongsi.babyinpalm.Controller.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hongsi.babyinpalm.Controller.fragment.HomePageFragment;
import com.hongsi.babyinpalm.Controller.fragment.SetPageFragment;
import com.hongsi.babyinpalm.Controller.fragment.TalkPageFragment;
import com.hongsi.babyinpalm.Exception.NetworkErrorException;
import com.hongsi.babyinpalm.Exception.OtherIOException;
import com.hongsi.babyinpalm.Interface.DialogListener;
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
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.NumberFormat;
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

    //等待窗口
    private WaitingDialog waitingDialog = null;

    //提示窗口
    private WarningDialog warningDialog = null;

    //更新进度窗口
    private ProgressbarDialog progressbarDialog = null;

    //下载apk线程
    private DownloadApk downloadApk = null;

    /** 用于下载apk的类 */
    class DownloadApk extends AsyncTask<String,Integer,Integer> {
        private WeakReference<ActivityMain> weakReference;

        public DownloadApk(ActivityMain activityMain){
            weakReference = new WeakReference<ActivityMain>(activityMain);
        }

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
                    ToastUtil.showToast(weakReference.get(), errorStr.toString(),Toast.LENGTH_SHORT);
                }else if(result == 5){

                    StringBuffer errorStr = new StringBuffer(getResources().getString(R.string.software_update_fail));
                    errorStr.append(getResources().getString(R.string.other_error));
                    ToastUtil.showToast(weakReference.get(), errorStr.toString(),Toast.LENGTH_SHORT);
                }else{

                    StringBuffer errorStr = new StringBuffer(getResources().getString(R.string.software_update_fail));
                    errorStr.append(getResources().getString(R.string.net_error));
                    ToastUtil.showToast(weakReference.get(), errorStr.toString(),Toast.LENGTH_SHORT);
                }

            }else{
                //直接安装应用程序
                progressbarDialog.dismiss();

                installApk();
            }

            weakReference.clear();
            weakReference = null;
        }

        /** 安装apk */
        private void installApk(){
            if(!apkFile.exists()){
                return;
            }

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setDataAndType(Uri.parse("file://" + apkFile.getAbsolutePath()), "application/vnd.android.package-archive");
            weakReference.get().startActivity(i);

        }
    };

    /** 用于处理线程与ui交互的handler */
    private Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 0:{
                    //需要更新版本，显示窗口
                    warningDialog = new WarningDialog(ActivityMain.this, R.style.DialogStyle, new DialogListener() {
                        @Override
                        public void getResultBoolean(boolean b) {
                            if(b){
                                //点击了确定按纽，显示进度条的窗口
                                progressbarDialog = new ProgressbarDialog(ActivityMain.this,R.style.DialogStyle);
                                progressbarDialog.setProgressBarValue(0);
                                progressbarDialog.showText("准备中...");
                                progressbarDialog.show();

                                //开始执行下载任务
                                downloadApk = new ActivityMain.DownloadApk(ActivityMain.this);
                                downloadApk.execute(SoftwareUpdate.softwareUrl);

                            }else{
                                //点击了取消按纽

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
                }
                break;

                case -1:{
                    ToastUtil.showToast(ActivityMain.this,R.string.net_error, Toast.LENGTH_SHORT);
                }
                break;

                case -2:{
                    ToastUtil.showToast(ActivityMain.this,R.string.other_error,Toast.LENGTH_SHORT);
                }
                break;

                case -3:{
                    ToastUtil.showToast(ActivityMain.this,R.string.data_error, Toast.LENGTH_SHORT);
                }
                break;

                case 10:{
                    Intent intent = new Intent(ActivityMain.this,ActivityMain.class);
                    startActivity(intent);
                    ActivityMain.this.finish();

                    //4.关闭等待窗口
                    waitingDialog.dismiss();
                    waitingDialog.stopAnimate();
                }
                break;

                case 11:{
                    StringBuffer errorStr = new StringBuffer(getResources().getString(R.string.login_fail));
                    errorStr.append(getResources().getString(R.string.login_error));

                    ToastUtil.showToast(ActivityMain.this,errorStr.toString(),Toast.LENGTH_SHORT);

                    //4.关闭等待窗口
                    waitingDialog.dismiss();
                    waitingDialog.stopAnimate();
                }
                break;

                case 12:{
                    StringBuffer errorStr = new StringBuffer(getResources().getString(R.string.login_fail));
                    errorStr.append(getResources().getString(R.string.server_error));

                    ToastUtil.showToast(ActivityMain.this,errorStr.toString(),Toast.LENGTH_SHORT);

                    //4.关闭等待窗口
                    waitingDialog.dismiss();
                    waitingDialog.stopAnimate();
                }
                break;

                case -11:{
                    StringBuffer errorStr = new StringBuffer(getResources().getString(R.string.login_fail));
                    errorStr.append(getResources().getString(R.string.net_error));

                    ToastUtil.showToast(ActivityMain.this,errorStr.toString(),Toast.LENGTH_SHORT);

                    //4.关闭等待窗口
                    waitingDialog.dismiss();
                    waitingDialog.stopAnimate();
                }
                break;

                case -12:{
                    StringBuffer errorStr = new StringBuffer(getResources().getString(R.string.login_fail));
                    errorStr.append(getResources().getString(R.string.data_error));

                    ToastUtil.showToast(ActivityMain.this,errorStr.toString(),Toast.LENGTH_SHORT);

                    //4.关闭等待窗口
                    waitingDialog.dismiss();
                    waitingDialog.stopAnimate();
                }
                break;

                case -13:{
                    StringBuffer errorStr = new StringBuffer(getResources().getString(R.string.login_fail));
                    errorStr.append(getResources().getString(R.string.other_error));

                    ToastUtil.showToast(ActivityMain.this,errorStr.toString(),Toast.LENGTH_SHORT);

                    //4.关闭等待窗口
                    waitingDialog.dismiss();
                    waitingDialog.stopAnimate();
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_layout);

        initUi();

        //检查更新
        checkUpdateSoftware();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        homePageFragment = null;
        setPageFragment = null;
        talkPageFragment = null;

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

        //销毁时必须终止该线程，如果线程还在执行
        if(downloadApk!=null ){
            if(downloadApk.getStatus() == AsyncTask.Status.RUNNING){
                downloadApk.cancel(true);
            }
        }

        warningDialog = null;
        progressbarDialog = null;

        downloadApk = null;
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
}

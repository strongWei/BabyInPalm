package com.hongsi.babyinpalm.Utils.Component;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.webkit.WebView;

import com.hongsi.babyinpalm.Utils.AppDbSqliteHelper;
import com.hongsi.babyinpalm.Utils.ImageLoader;
import com.hongsi.babyinpalm.Utils.LogUtil;

import java.io.File;
import java.io.IOException;

/**
 * Created by Administrator on 2016/9/26.
 * @author strong
 * @version 1.0
 */
public class CustomApplication extends Application{

    private static Context context = null;
    private final static String TAG = "CustomApplication";
    private static String userAgent = null;
    private static ImageLoader imageLoader = null;
    private static AppDbSqliteHelper dbHelper = null;

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        userAgent = new WebView(context).getSettings().getUserAgentString();

        imageLoader = new ImageLoader(context);

        dbHelper = new AppDbSqliteHelper(context,"AppDb",null,1);
    }



    /** 获取整个应用的Context,可用于一些获取不到Context的地方，或需要ApplicationContext的地方 */
    public static Context getContext(){
        if(context == null){
            LogUtil.e(TAG,"application created unready!");
        }
        return context;
    }

    /**获取本地的默认User-Agent*/
    public static String getUserAgent(){

        return userAgent;
    }

    /**获取程序的版本字符串 */
    public static String getVersionName(){

        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            LogUtil.e(TAG,"page name not found");
            e.printStackTrace();
        }

        return "";
    }

    /**获取程序的版本号*/
    public static int getVersionCode(){
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            LogUtil.e(TAG,"page name not found");
            e.printStackTrace();
        }

        return 0;
    }

    /** 获取图片三层缓存器 */
    public static ImageLoader getImageLoader(){
        return imageLoader;
    }

    /** 关闭图片三层缓存器 */
    public static void closeImageLoader(){
        imageLoader.close();
    }

    /** 获取数据库 */
    public static AppDbSqliteHelper getDbHelper(){
        return dbHelper;
    }

    /** 获取网络类型*/
    public static boolean hasNetwork(){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if(networkInfo == null || !networkInfo.isConnected()){
            return false;
        }

        return true;
    }

    /** 获取本地的图片默认存储文件夹 */
    public static String getImageDir(){

        File fileDir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(),"BabyInPalm");

        if(!fileDir.exists()){
            fileDir.mkdirs();

            //这个时候随机创建一个文件,创造一个假象
            File tempFile = new File(fileDir,"1.jpg");

            try {
                tempFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(tempFile)));

            //之后再把文件删除
            tempFile.delete();

        }


        return fileDir.getAbsolutePath();
    }
}

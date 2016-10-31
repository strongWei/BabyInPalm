package com.hongsi.babyinpalm.Utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;



import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/10/8.
 */
public class ImageLoader {
    private static final String TAG = "ImageLoader";

    private static final int MEMORY_CACHE_SIZE_LIMIT = (int) (Runtime.getRuntime().maxMemory() / 8);
    private static final int LOCAL_CACHE_SIZE_LIMIT = 100 * 1024 * 1024;


    private HashMap<String, AsyncTask> taskMap = new HashMap<>();

    public ImageLoader(Context context){
        initMemoryCache();
        initDiskCache(context);

        //写入本地需要的缓存

    }

    /** 初始化磁盘缓存器*/
    private void initDiskCache(Context context) {
        int appVersion = 1;

        try {
            appVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            LogUtil.e(TAG,e.getMessage());
        }

        DiskLruCacheHelper.openCache(context,appVersion,LOCAL_CACHE_SIZE_LIMIT);
    }

    /** 初始化内存缓存器 */
    private void initMemoryCache() {
        LruCacheHelper.openCache(MEMORY_CACHE_SIZE_LIMIT);
    }

    /** 载入图片 */
    public void loadNetworkImage(View parent, String url){
        //尝试从内存缓存载入图片
        boolean succeeded = loadImageFromMemory(parent,url);
        
        if(succeeded) return;
        
        boolean hasCache = DiskLruCacheHelper.hasCache(url);
        
        if(hasCache){
            //有磁盘缓存
            loadImageFromDisk(parent,url);
        }else{
            //联网下载
            loadFromInternet(parent,url);
        }
    }

    /** 从网络中加载图片*/
    private void loadFromInternet(View parent, String url) {
        DownloadImageTask task = new DownloadImageTask(parent);
        taskMap.put(url,task);
        task.execute(url);
    }

    /** 从磁盘缓存中加载图片*/
    private void loadImageFromDisk(View parent, String url) {
        LoadImageDiskCacheTask task = new LoadImageDiskCacheTask(parent);
        taskMap.put(url,task);
        task.execute(url);
    }

    /** 从内存缓存中加载图片*/
    private boolean loadImageFromMemory(View parent, String url) {
        Bitmap bitmap = LruCacheHelper.load(url);
        if(bitmap != null){
            setImage(parent, bitmap, url);
            return true;
        }

        return false;
    }

    /** 重新设置图片*/
    private void setImage(final View parent, final Bitmap bitmap, final String url) {
        parent.post(new Runnable() {
            @Override
            public void run() {
                ImageView imageView = findImageViewWithTag(parent,url);

                if(imageView != null){
                    imageView.setImageBitmap(bitmap);
                }
            }
        });
    }

    /** 根据Tag找到指定的ImageView. */
    private ImageView findImageViewWithTag(View parent, String url) {
        View view = parent.findViewWithTag(url);

        if(view != null) {
            return (ImageView) view;
        }

        return null;
    }

    /** 读取图片磁盘缓存的任务*/
    class LoadImageDiskCacheTask extends AsyncTask<String, Void, Bitmap > {
        private final View parent;
        private String url;

        public LoadImageDiskCacheTask(View parent){
            this.parent = parent;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;

            url = params[0];

            try {
                bitmap = DiskLruCacheHelper.load(url);

                if(bitmap != null && !isCancelled()){
                    //读取完成后保存到内存缓存
                    putImageIntoMemoryCache(url,bitmap);
                }

            } catch (IOException e) {
                LogUtil.e(TAG,e.getMessage());
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            //显示图片
            if(bitmap != null) setImage(parent,bitmap,url);

            //移除任务
            if(taskMap.containsKey(url)) taskMap.remove(url);
        }
    }

    /** 把图片保存到内存缓存*/
    private void putImageIntoMemoryCache(String url, Bitmap bitmap) {
        LruCacheHelper.dump(url,bitmap);
    }

    /** 下载图片的任务 */
    class DownloadImageTask extends AsyncTask<String, Void,Bitmap>{

        private final View parent;
        private String url;

        public DownloadImageTask(View parent) {
            this.parent = parent;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;

            url = params[0];

            InputStream inputStream = null;
            try {
                inputStream = NetworkAdministrator.openUrlInputStream(url);

                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inPreferredConfig = Bitmap.Config.RGB_565;
                opt.inPurgeable = true;
                opt.inInputShareable = true;

                bitmap = BitmapFactory.decodeStream(inputStream,null,opt);

                if(bitmap != null && !isCancelled()){
                    //保存到缓存
                    putImageIntoMemoryCache(url,bitmap);
                    putImageIntoDiskCache(url,bitmap);
                }
            } catch (IOException e) {
                LogUtil.e(TAG,"get network image fail");
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            //显示图片
            if(bitmap != null) setImage(parent,bitmap,url);

            //移除任务
            if(taskMap.containsKey(url)) taskMap.remove(url);
        }
    }

    /** 将图片写入磁盘缓存中*/
    private void putImageIntoDiskCache(String url, Bitmap bitmap) throws IOException {
        DiskLruCacheHelper.dump(bitmap,url);
    }

    /** 使用完毕必须调用 .*/
    public void close(){
        for (Map.Entry<String, AsyncTask> entry : taskMap.entrySet()){
            entry.getValue().cancel(true);
        }

        DiskLruCacheHelper.closeCache();
        LruCacheHelper.closeCache();
    }

    /** 将资源文件的图片加载入内存缓存中 */
    public void loadLocalImage(String tag,int resId,Context context,View parent) throws IOException {

        String url = resId + "";

        //判断内存中中是否有该图片资源
        boolean succeed = loadImageFromMemory(parent,tag);

        if(succeed){
            return;
        }

        Bitmap bitmap = null;

        bitmap = ImageResUtils.getImageById(context,resId);

        //将该图片中添加缓存
        if(bitmap!=null) {
            putImageIntoMemoryCache(url, bitmap);
            setImage(parent,bitmap,url);
        }
    }
}

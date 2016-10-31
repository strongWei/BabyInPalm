package com.hongsi.babyinpalm.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 2016/10/8.
 */
public class DiskLruCacheHelper {

    private final static String TAG = "DiskLruCacheHelper";

    private DiskLruCacheHelper() {}

    private static DiskLruCache mCache;

    /** 打开DiskLruCache */
    public static void openCache(Context context, int appVersion, int maxSize) {

        try {

            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
                mCache = DiskLruCache.open(context.getExternalCacheDir(), appVersion, 1, maxSize);
            } else {
                mCache = DiskLruCache.open(context.getCacheDir(),appVersion,1,maxSize);
            }
        }catch (IOException e){
            LogUtil.e(TAG, "open disk lru cache fail");
        }
    }

    /** 写出缓存 */
    public static void dump(Bitmap bitmap, String keyCache) throws IOException {
        if(mCache == null)  throw new IllegalStateException("Must call openCache() first!");

        DiskLruCache.Editor editor = mCache.edit(Digester.hashUp(keyCache));

        if(editor != null){
            OutputStream outputStream = editor.newOutputStream(0);
            boolean success = bitmap.compress(Bitmap.CompressFormat.JPEG, 100 , outputStream);

            if(success){
                editor.commit();
            }else{
                editor.abort();
            }
        }
    }

    /** 读取缓存 */
    public static Bitmap load(String keyCache) throws IOException {
        if(mCache == null)  throw new IllegalStateException("Must call openCache() first!");

        DiskLruCache.Snapshot snapshot = mCache.get(Digester.hashUp(keyCache));

        if(snapshot != null){
            InputStream inputStream = snapshot.getInputStream(0);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            return bitmap;
        }

        return null;
    }

    /** 检查缓存是否存在 */
    public static boolean hasCache(String keyCache) {
        try {
            return mCache.get(Digester.hashUp(keyCache)) != null;
        } catch (IOException e) {
            LogUtil.e(TAG,e.getMessage());
        }

        return false;
    }

    /** 同步日志 */
    public static void syncLog(){
        try {
            mCache.flush();
        } catch (IOException e) {
            LogUtil.e(TAG,e.getMessage());
        }
    }

    /** 关闭DiskLruCache */
    public static void closeCache(){
        syncLog();
    }
}

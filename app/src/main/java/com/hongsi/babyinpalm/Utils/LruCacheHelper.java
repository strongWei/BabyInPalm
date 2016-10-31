package com.hongsi.babyinpalm.Utils;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by Administrator on 2016/10/8.
 */
public class LruCacheHelper {

    private LruCacheHelper(){}

    private static LruCache<String,Bitmap> mCache;

    /** 初始化LruCace */
    public static void openCache(int maxSize){
        mCache = new LruCache<String, Bitmap>(maxSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
    }

    /** 把图片写入缓存 */
    public static void dump(String key, Bitmap value) {
        mCache.put(key,value);
    }

    /** 从缓存中读取图片 */
    public static Bitmap load(String key) {
       return mCache.get(key);
    }

    public static void closeCache() {
        //TODO
    }

    /** 判断缓存中是否有当前这张图片 */
    public static boolean hasCache(String key){
        return mCache.get(key) != null;
    }
}

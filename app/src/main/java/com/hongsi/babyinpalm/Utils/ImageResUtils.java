package com.hongsi.babyinpalm.Utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2016/10/8.
 */
public class ImageResUtils {

    private static String TAG = "ImageResUtils";

    public static Bitmap getImageById(Context context, int resId){
        Bitmap bitmap = null;

        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        //获取资源图片
        InputStream is = context.getResources().openRawResource(resId);

        bitmap =  BitmapFactory.decodeStream(is,null,opt);

        return bitmap;
    }

    /**
     * 获取图片的旋转角度， 由于某些手机在保存图片的时候会自动旋转图片后再保存
     * @param url
     * @return
     */
    public static int getBitmapDegree(String url){
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(url);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bm
     *            需要旋转的图片
     * @param degree
     *            旋转角度
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        if(degree == 0 || degree == 360){
            return bm;
        }

        Bitmap returnBm = null;

        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }

    public static String getRealFilePath( final Context context, final Uri uri ) {

        if ( null == uri ) return null;

        final String scheme = uri.getScheme();
        String data = null;

        if ( scheme == null )
            data = uri.getPath();
        else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
            data = uri.getPath();
        } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
            Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
            if ( null != cursor ) {
                if ( cursor.moveToFirst() ) {
                    int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
                    if ( index > -1 ) {
                        data = cursor.getString( index );
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    /**
     * 根据 路径 获取 bitmap 自动缩放
     * @param url
     * @return
     * @throws FileNotFoundException
     */
    public static Bitmap getImageByUrl(String url,int maxWidth,int maxHeight) throws FileNotFoundException {

        Bitmap bitmap = null;

        //1.加载位图
        InputStream is = new FileInputStream(url);
        //2.为位图设置100K的缓存
        BitmapFactory.Options opts=new BitmapFactory.Options();

        opts.inJustDecodeBounds = true;
        bitmap = BitmapFactory.decodeFile(url,opts);

        //获取图片的真实大小
        float realWidth = opts.outWidth;
        float realHeight = opts.outHeight;

        //计算缩放比
        float be = 1;

        //获取宽度/高度的缩放比
        float widthDegree = realWidth / maxWidth;
        float heightDegree = realHeight / maxHeight;

        if (widthDegree < 0 && heightDegree < 0) {
            //真实图片（宽高）比要求图片小, 不缩放
            be = 1;
        } else if (widthDegree < 0 && heightDegree >= 0) {
            //真实图片（宽）比要求图片小，真实图片高比要求图片大
            be = heightDegree;
        } else if (widthDegree > 0 && heightDegree <= 0) {
            //真实图片（宽）比要求图片大，真实图片高比要求图片小
            be = widthDegree;
        } else if (widthDegree > 0 && heightDegree > 0) {
            if(realWidth < realHeight) {
                be = heightDegree;
            }else{
                be = widthDegree;
            }

        } else if (widthDegree == 0 && heightDegree < 0) {
            //真实图片高比要求图片小
            be = widthDegree;
        } else if (widthDegree == 0 && heightDegree >= 0) {
            be = heightDegree;
        }


        if(be<=0){
            be = 1;
        }


        opts.inTempStorage = new byte[100 * 1024];
        //3.设置位图颜色显示优化方式
        //ALPHA_8：每个像素占用1byte内存（8位）
        //ARGB_4444:每个像素占用2byte内存（16位）
        //ARGB_8888:每个像素占用4byte内存（32位）
        //RGB_565:每个像素占用2byte内存（16位）
        //Android默认的颜色模式为ARGB_8888，这个颜色模式色彩最细腻，显示质量最高。但同样的，占用的内存//也最大。也就意味着一个像素点占用4个字节的内存。我们来做一个简单的计算题：3200*2400*4 bytes //=30M。如此惊人的数字！哪怕生命周期超不过10s，Android也不会答应的。
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        //4.设置图片可以被回收，创建Bitmap用于存储Pixel的内存空间在系统内存不足时可以被回收
        opts.inPurgeable = true;
        //5.设置位图缩放比例
        //width，hight设为原来的四分一（该参数请使用2的整数倍）,这也减小了位图占用的内存大小；例如，一张//分辨率为2048*1536px的图像使用inSampleSize值为4的设置来解码，产生的Bitmap大小约为//512*384px。相较于完整图片占用12M的内存，这种方式只需0.75M内存(假设Bitmap配置为//ARGB_8888)。
        opts.inSampleSize = (int)be;
        //6.设置解码位图的尺寸信息
        opts.inInputShareable = true;
        opts.inJustDecodeBounds = false;
        //7.解码位图
        bitmap =BitmapFactory.decodeStream(is,null, opts);

        //8.显示位图

    /*
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;

        //先加载出图片的参数
        BitmapFactory.decodeFile(url,opt);

        //再设置其它的参数
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        opt.inDither = false;
        opt.inPurgeable = true;
        opt.inJustDecodeBounds = false;

        bitmap = BitmapFactory.decodeFile(url,opt);
    */

        return bitmap;
    }

    /**
     * 将图片缩放成固定大小
     *
     * @param path
     * @param height
     * @param width
     * @return
     */
    public static File scaleBitmap(String path, String scalePath,int height,int width){
        BitmapFactory.Options options = new BitmapFactory.Options();

        //不将该图片放到内存就可以获取图片信息
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path,options);


        //获取图片的真实大小
        float realWidth = options.outWidth;
        float realHeight = options.outHeight;

        //计算缩放比
        int be = 1;

        if(realWidth >= realHeight && realWidth > width){
            be = (int)(realWidth / width);
        }else if(realWidth < realHeight && realHeight > height){
            be = (int)(realHeight / height);
        }

        if(be<=0){
            be = 1;
        }

        //采样率
        options.inSampleSize = be;

        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inDither = false;
        options.inPurgeable = true;

        //这一次要读取图片
        options.inJustDecodeBounds = false;

        bitmap = BitmapFactory.decodeFile(path,options);

        if(bitmap == null){
            LogUtil.e(TAG,"decode scale file fail!");
            return null;
        }

        File file = new File(scalePath);
        try {
            FileOutputStream outputStream = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG,90,outputStream);

            outputStream.flush();
            outputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    /**
     * 将图片等比例缩放成固定大小
     *
     * @param path
     * @param maxWidth 需要的最大宽度
     * @param maxHeight 需要的最大高度
     * @return
     */
    public static File scaleBitmapAuto(String path, String scalePath,float maxWidth,float maxHeight,int degree){
        BitmapFactory.Options options = new BitmapFactory.Options();

        //不将该图片放到内存就可以获取图片信息
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path,options);


        //获取图片的真实大小
        float realWidth = options.outWidth;
        float realHeight = options.outHeight;

        //计算缩放比
        float be = 1;

        //获取宽度/高度的缩放比
        float widthDegree = realWidth / maxWidth;
        float heightDegree = realHeight / maxHeight;

        if (widthDegree < 0 && heightDegree < 0) {
            //真实图片（宽高）比要求图片小, 不缩放
            be = 1;
        } else if (widthDegree < 0 && heightDegree >= 0) {
            //真实图片（宽）比要求图片小，真实图片高比要求图片大
            be = heightDegree;
        } else if (widthDegree > 0 && heightDegree <= 0) {
            //真实图片（宽）比要求图片大，真实图片高比要求图片小
            be = widthDegree;
        } else if (widthDegree > 0 && heightDegree > 0) {
            if(realWidth < realHeight) {
                be = heightDegree;
            }else{
                be = widthDegree;
            }

        } else if (widthDegree == 0 && heightDegree < 0) {
            //真实图片高比要求图片小
            be = widthDegree;
        } else if (widthDegree == 0 && heightDegree >= 0) {
            be = heightDegree;
        }


        if(be<=0){
            be = 1;
        }

        //采样率
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inSampleSize = (int) be;
        LogUtil.e(TAG,be +"");

        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inDither = false;
        options.inPurgeable = true;

        //这一次要读取图片
        options.inJustDecodeBounds = false;

        bitmap = BitmapFactory.decodeFile(path,options);

        if(bitmap == null){
            LogUtil.e(TAG,"decode scale file fail!");
            return null;
        }

        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotateBitmap = null;
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            rotateBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (rotateBitmap == null) {
            rotateBitmap = bitmap;
        }
        if (bitmap != rotateBitmap) {
            bitmap.recycle();
        }


        File file = new File(scalePath);
        try {
            FileOutputStream outputStream = new FileOutputStream(file);

            rotateBitmap.compress(Bitmap.CompressFormat.JPEG,90,outputStream);

            outputStream.flush();
            outputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(rotateBitmap!=null){
                rotateBitmap.recycle();
                rotateBitmap = null;
            }
        }

        return file;
    }

    /**
     * bitmap转为base64
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * base64转为bitmap
     * @param base64Data
     * @return
     */
    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * 如果图片被旋转过，自动旋转该图片，并且保存到新的临时路径中
     *
     * @param newPath 保存到新的路径
     * @param oldPath  保存到旧的路径
     * @return 新的路径或空
     */
    /*
    public static String rotateImageAutoAndSave(String oldPath,String newPath){

        int degree = getBitmapDegree(oldPath);

        //加载并
        try {
            Bitmap oldBit = getImageByUrl(oldPath);
            Bitmap result =  rotateBitmapByDegree(oldBit,degree);
            if(oldBit != result){
                FileOutputStream outputStream = new FileOutputStream(new File(newPath));

                if(!result.compress(Bitmap.CompressFormat.JPEG,100,outputStream)){
                    outputStream.close();
                    return oldPath;
                }

                outputStream.close();
            }else{
                return oldPath;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return newPath;
    }
    */
}

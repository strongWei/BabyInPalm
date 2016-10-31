package com.hongsi.babyinpalm.Model;

import android.database.sqlite.SQLiteDatabase;

import com.hongsi.babyinpalm.Exception.NetworkErrorException;
import com.hongsi.babyinpalm.Exception.OtherIOException;
import com.hongsi.babyinpalm.Utils.Component.CustomApplication;
import com.hongsi.babyinpalm.Utils.HttpUtils;
import com.hongsi.babyinpalm.Utils.HttpUtilsWithSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by Administrator on 2016/10/20.
 */

public class ChangeStuImage {
    private static String ids;

    public static int modify(File file,String id) throws OtherIOException, NetworkErrorException, JSONException {
        ids = id;
        StringBuffer bufferUrl = new StringBuffer();
        bufferUrl.append(HttpUtils.BASE_URL);

        bufferUrl.append("/app/student");

        bufferUrl.append("?type=");
        bufferUrl.append(3);
        bufferUrl.append("&id=");
        bufferUrl.append(id);

        String result = HttpUtilsWithSession.postFile(bufferUrl.toString(),file,System.currentTimeMillis()+".png");

        int code = parseJson(result);

        return code;
    }

    public static int parseJson(String result) throws JSONException {
        JSONObject object = new JSONObject(result);

        //boolean r = object.getBoolean("return");
        int code = object.getInt("code");

        //如果code为0，则代表修改图片成功
        if(code == 0){
            String url = object.getString("url");
            String url_scale = object.getString("url_scale");
            modifyStuImageOnDb(url,url_scale,ids);

        }

        return code;
    }

    private static void modifyStuImageOnDb(String url, String url_scale, String ids) {
        SQLiteDatabase db =  CustomApplication.getDbHelper().getWritableDatabase();

        String sql = "update student set pic_path=?,pic_scale_path=? where id=?";
        db.execSQL(sql, new String[]{url,url_scale,ids});

        db.close();
    }

}

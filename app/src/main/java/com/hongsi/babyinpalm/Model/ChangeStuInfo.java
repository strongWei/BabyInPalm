package com.hongsi.babyinpalm.Model;

import android.database.sqlite.SQLiteDatabase;

import com.hongsi.babyinpalm.Exception.NetworkErrorException;
import com.hongsi.babyinpalm.Exception.OtherIOException;
import com.hongsi.babyinpalm.Utils.Component.CustomApplication;
import com.hongsi.babyinpalm.Utils.HttpUtils;
import com.hongsi.babyinpalm.Utils.HttpUtilsWithSession;

import org.json.JSONException;


/**
 * Created by Administrator on 2016/10/24.
 */

public class ChangeStuInfo {

    public static int change(String id,String name,int sex,String birth,String start) throws OtherIOException, NetworkErrorException, JSONException {
        StringBuffer bufferUrl = new StringBuffer();
        bufferUrl.append(HttpUtils.BASE_URL);

        bufferUrl.append("/app/student");

        //params
        StringBuffer buffer = new StringBuffer();
        buffer.append("type=");
        buffer.append(2);
        buffer.append("&id=");
        buffer.append(id);
        buffer.append("&name=");
        buffer.append(name);
        buffer.append("&sex=");
        buffer.append(sex);
        buffer.append("&birth=");
        buffer.append(birth);
        buffer.append("&start=");
        buffer.append(start);


        String result = HttpUtilsWithSession.post(bufferUrl.toString(),buffer.toString());

        int code = HttpUtilsWithSession.parseJson(result);

        if(code == 0){
            saveOnDb(id,name,sex,birth);
        }

        return code;
    }

    private static void saveOnDb(String id, String name, int sex, String birth) {
        SQLiteDatabase db =  CustomApplication.getDbHelper().getWritableDatabase();

        String sql = "update student set name=?,sex=?,birth=? where id=?";
        db.execSQL(sql,new Object[]{name,sex,birth,id});

        db.close();
    }

}

package com.hongsi.babyinpalm.Model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;

import com.hongsi.babyinpalm.Domain.User;
import com.hongsi.babyinpalm.Exception.NetworkErrorException;
import com.hongsi.babyinpalm.Exception.OtherIOException;
import com.hongsi.babyinpalm.Utils.Component.CustomApplication;
import com.hongsi.babyinpalm.Utils.HttpUtils;
import com.hongsi.babyinpalm.Utils.HttpUtilsWithSession;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2016/9/28.
 */
public class Login {

    public final static User user = new User();

    static {
        initUser();
    }

    public static int login(String phone1,String password1) throws OtherIOException, NetworkErrorException, JSONException {
        user.setPhone(phone1);
        user.setPassword(password1);

        StringBuffer bufferUrl = new StringBuffer();
        bufferUrl.append(HttpUtils.BASE_URL);

        bufferUrl.append("/app/login");

        //params
        StringBuffer buffer = new StringBuffer();
        buffer.append("name=");
        buffer.append(phone1);
        buffer.append("&password=");
        String encodePassword = encodePassword(phone1,password1);
        user.setEncodePassword(encodePassword);
        buffer.append(encodePassword);
        buffer.append("&type=");
        buffer.append(1);

        String result = HttpUtilsWithSession.post(bufferUrl.toString(),buffer.toString());

        return parseLoginJson(result);
    }

    private static String encodePassword(String name,String password){
        StringBuffer buffer = new StringBuffer();
        buffer.append(password);
        buffer.append(name);

        String result = buffer.toString();
        result = Base64.encodeToString(result.getBytes(),Base64.DEFAULT).trim();
        return result;
    }

    public static String decodePassword(String name,String encodeStr){
        if(encodeStr.isEmpty()){
            return "";
        }

        byte[] result = Base64.decode(encodeStr,0);
        StringBuffer buffer = new StringBuffer(new String(result));
        buffer.delete(buffer.length()-11,buffer.length());

        return buffer.toString();
    }

    //解析
    private static int parseLoginJson(String result) throws JSONException {

        JSONObject object = new JSONObject(result);

        boolean r = object.getBoolean("return");
        int code = object.getInt("code");

        if(r) {
            if (code == 0) {
                //当code为0,才有这些选项
                JSONObject dataObj = object.getJSONObject("data");
                user.setRole(dataObj.getJSONObject("role").getString("name"));
                user.setName(dataObj.getString("name"));
                user.setUrl(dataObj.getString("url"));
                user.setUrl_scale(dataObj.getString("url_scale"));
                user.setId(dataObj.getString("id"));

                saveUser();
            }
        }

        return code;
    }

    /** 个人信息初始化到内存中*/
    private static void initUser(){

        SQLiteDatabase db = CustomApplication.getDbHelper().getReadableDatabase();

        Cursor cursor = db.query("user",new String[]{"id","name","phone","password","role","url","url_scale"},null,null,null,null,null);

        //只获取第一行数据
        if(cursor.moveToFirst()){
            user.setName(cursor.getString(cursor.getColumnIndex("name")));
            user.setPhone(cursor.getString(cursor.getColumnIndex("phone")));
            user.setRole(cursor.getString(cursor.getColumnIndex("role")));
            user.setUrl(cursor.getString(cursor.getColumnIndex("url")));
            user.setUrl_scale(cursor.getString(cursor.getColumnIndex("url_scale")));
            user.setPassword(decodePassword(user.getName(),cursor.getString(cursor.getColumnIndex("password"))));
            user.setId(cursor.getString(cursor.getColumnIndex("id")));
        }

        db.close();

    }

    public static void clearUser(){
        user.setId("");
        user.setName("");
        user.setPassword("");
        user.setPhone("");
        user.setRole("");
        user.setUrl("");
        user.setEncodePassword("");
        user.setUrl_scale("");
    }

    /** 个人信息保存到硬盘中中*/
    private static void saveUser(){

        SQLiteDatabase db = CustomApplication.getDbHelper().getWritableDatabase();
        db.execSQL("delete from user");

        ContentValues cv = new ContentValues();
        cv.put("id",user.getId());
        cv.put("name",user.getName());
        cv.put("phone",user.getPhone());
        cv.put("password",user.getEncodePassword());
        cv.put("role",user.getRole());
        cv.put("url",user.getUrl());
        cv.put("url_scale",user.getUrl_scale());

        db.insert("user",null,cv);

        db.close();
    }
}

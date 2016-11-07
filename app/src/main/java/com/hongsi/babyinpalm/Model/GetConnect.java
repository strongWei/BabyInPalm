package com.hongsi.babyinpalm.Model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hongsi.babyinpalm.Domain.ConnectData;
import com.hongsi.babyinpalm.Exception.NetworkErrorException;
import com.hongsi.babyinpalm.Exception.OtherIOException;
import com.hongsi.babyinpalm.Utils.Component.CustomApplication;
import com.hongsi.babyinpalm.Utils.HttpUtilsWithSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/1.
 */

public class GetConnect {

    public static List<ConnectData> datas= new ArrayList<>();

    public static int getData(String url,int type) throws OtherIOException, NetworkErrorException, JSONException {
        String result = HttpUtilsWithSession.get(url);

        return parseJson(result,type);
    }

    public static int parseJson(String result,int type) throws JSONException {

        JSONObject object = new JSONObject(result);

        //boolean r = object.getBoolean("return");
        int code = object.getInt("code");

        datas.clear();

        if(code == 0){
            JSONArray array = object.getJSONArray("data");

            if(array==null){
                return code;
            }

            if(type==0){
                //将所有的教师从数据库中删除
                SQLiteDatabase db = CustomApplication.getDbHelper().getWritableDatabase();
                String sql = "delete from tea_connect";
                db.execSQL(sql);

                for(int i=0;i<array.length();++i){
                    JSONObject dataObj = array.getJSONObject(i);
                    if(dataObj!=null){
                        ConnectData data = new ConnectData();
                        data.setId(dataObj.getString("id"));
                        data.setName(dataObj.getString("name"));
                        data.setPhone(dataObj.getString("phone"));
                        data.setUrl_scale(dataObj.getString("url_scale"));
                        data.setRole(dataObj.getString("role"));

                        JSONArray grades = dataObj.getJSONArray("grades");
                        if(grades!=null && grades.length() != 0){
                            StringBuffer gradeStr = new StringBuffer();
                            for(int j=0;j<grades.length();++j){
                                JSONObject grade = grades.getJSONObject(j);
                                gradeStr.append(grade.getString("name"));
                                gradeStr.append(" ");
                            }

                            data.setDetail(gradeStr.toString());
                        }

                        datas.add(data);

                        sql = "insert into tea_connect(id,name,phone,url_scale,detail,role,user_id) values(?,?,?,?,?,?,?)";
                        db.execSQL(sql, new Object[]{data.getId(),data.getName(),data.getPhone(),data.getUrl_scale(),data.getUrl_scale(),data.getRole(),Login.user.getId()});

                    }
                }

                db.close();
            }else if(type==1){
                SQLiteDatabase db = CustomApplication.getDbHelper().getWritableDatabase();
                String sql = "delete from par_connect";
                db.execSQL(sql);

                for(int i=0;i<array.length();++i){
                    JSONObject dataObj = array.getJSONObject(i);
                    if(dataObj!=null){
                        ConnectData data = new ConnectData();
                        data.setId(dataObj.getString("id"));
                        data.setName(dataObj.getString("name"));
                        data.setPhone(dataObj.getString("phone"));
                        data.setUrl_scale(dataObj.getString("url_scale"));
                        data.setRole("家长");

                        JSONArray grades = dataObj.getJSONArray("students");
                        if(grades!=null){
                            StringBuffer gradeStr = new StringBuffer();
                            for(int j=0;j<grades.length();++j){
                                JSONObject grade = grades.getJSONObject(j);
                                gradeStr.append(grade.getString("name"));
                                gradeStr.append(" ");
                            }

                            data.setDetail(gradeStr.toString());
                        }

                        datas.add(data);

                        sql = "insert into par_connect(id,name,phone,url_scale,detail,role,user_id) values(?,?,?,?,?,?,?)";
                        db.execSQL(sql, new Object[]{data.getId(),data.getName(),data.getPhone(),data.getUrl_scale(),data.getUrl_scale(),data.getRole(),Login.user.getId()});

                    }
                }

                db.close();
            }
        }else{
            //从数据库中获取数据
            SQLiteDatabase db = CustomApplication.getDbHelper().getReadableDatabase();

            if(type==0){
                Cursor cursor = db.query("tea_connect",new String[]{"id","name","phone","url_scale","detail","role"},
                        "user_id=?",new String[]{Login.user.getId()},null,null,null);

                while(cursor.moveToNext()){
                    ConnectData data = new ConnectData();
                    data.setId(cursor.getString(0));
                    data.setName(cursor.getString(1));
                    data.setPhone(cursor.getString(2));
                    data.setUrl_scale(cursor.getString(3));
                    data.setDetail(cursor.getString(4));
                    data.setRole(cursor.getString(5));

                    datas.add(data);
                }

            }else if(type==1){
                Cursor cursor = db.query("par_connect",new String[]{"id","name","phone","url_scale","detail","role"},
                        "user_id=?",new String[]{Login.user.getId()},null,null,null);

                while(cursor.moveToNext()){
                    ConnectData data = new ConnectData();
                    data.setId(cursor.getString(0));
                    data.setName(cursor.getString(1));
                    data.setPhone(cursor.getString(2));
                    data.setUrl_scale(cursor.getString(3));
                    data.setDetail(cursor.getString(4));
                    data.setRole(cursor.getString(5));

                    datas.add(data);
                }
            }

            db.close();
        }

        return code;
    }
}

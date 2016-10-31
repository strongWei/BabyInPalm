package com.hongsi.babyinpalm.Model;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.hongsi.babyinpalm.Domain.Student;
import com.hongsi.babyinpalm.Exception.NetworkErrorException;
import com.hongsi.babyinpalm.Exception.OtherIOException;
import com.hongsi.babyinpalm.Utils.Component.CustomApplication;
import com.hongsi.babyinpalm.Utils.HttpUtils;
import com.hongsi.babyinpalm.Utils.HttpUtilsWithSession;
import com.hongsi.babyinpalm.Utils.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Administrator on 2016/9/18.
 */
public class GetChildModel {

    public static List<Student> students = new ArrayList<>();
    //public static boolean getStuInfoSign = false;     //是否进行了获取学生数据的请求

    public static int getChilds() throws OtherIOException, NetworkErrorException, JSONException {

        students.clear();

        String result="";

        StringBuffer bufferUrl = new StringBuffer();
        bufferUrl.append(HttpUtils.BASE_URL);

        //bufferUrl.append("http://192.168.1.5:8080/YunSchool");
        bufferUrl.append("/app/student");

        //params
        StringBuffer buffer = new StringBuffer();
        buffer.append("type=");
        buffer.append(1);


        result = HttpUtilsWithSession.post(bufferUrl.toString(),buffer.toString());
        LogUtil.d("getChilds",result);


        int code = parseGetChildsJson(result);

        if(code == 0){
            saveToDb();
        }

        return code;
    }

    //解析
    private static int parseGetChildsJson(String result) throws JSONException {
        int code = 0;

        JSONObject object = null;

        object = new JSONObject(result);

        code = object.getInt("code");

        if(code==0) {
            //getStuInfoSign = true;

            //当code为0,才有这些选项
            JSONArray array = object.getJSONArray("data");
            LogUtil.d("array-size",new Integer(array.length()).toString());

            if(array.toString().equals("[]")){
                //无任何内容
            }else{

                //获取学生
                //{"cards":"[]","sex":0,"name":"五少","birth":"","grades":"","id":"54B4B7E9-14C1-44D6-A0C9-2AB04A3BF50C"}
                for(int i=0;i<array.length();++i){
                    JSONObject obj = array.getJSONObject(i);

                    Student student = new Student();
                    student.setId(obj.getString("id"));
                    student.setName(obj.getString("name"));
                    student.setCards(obj.getString("cards"));
                    student.setGrade(obj.getString("grade"));
                    student.setBirth(obj.getString("birth"));
                    student.setSex(obj.getInt("sex"));
                    student.setUrl(obj.getString("picPath"));
                    student.setUrl_scale(obj.getString("picScalePath"));
                    student.setStart(obj.getString("start"));

                    students.add(student);
                }
            }

        }

        return code;
    }

    private static void saveToDb() {

        //打开数据库
        SQLiteDatabase db = CustomApplication.getDbHelper().getWritableDatabase();

        //清空数据库
        db.delete("student","",null);

        if(students == null){
            //do nothing
            db.close();
            return;
        }

        Student student = null;
        ContentValues values = new ContentValues();

        for(int i=0;i<students.size();++i){
            student = students.get(i);
            values.put("id",student.getId());
            values.put("name",student.getName());
            values.put("grade",student.getGrade());
            values.put("birth",student.getBirth());
            values.put("cards",student.getCards());
            values.put("sex",student.getSex());
            values.put("pic_path",student.getUrl());
            values.put("pic_scale_path",student.getUrl_scale());
            values.put("start",student.getStart());

            db.insert("student","",values);


            //再进行清空
            values.clear();
        }

        db.close();
    }
}

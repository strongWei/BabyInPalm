package com.hongsi.babyinpalm.Model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hongsi.babyinpalm.Domain.ImageData;
import com.hongsi.babyinpalm.Domain.User;
import com.hongsi.babyinpalm.Exception.NetworkErrorException;
import com.hongsi.babyinpalm.Exception.OtherIOException;
import com.hongsi.babyinpalm.Utils.Component.CustomApplication;
import com.hongsi.babyinpalm.Utils.HttpUtilsWithSession;
import com.hongsi.babyinpalm.Utils.LogUtil;
import com.hongsi.babyinpalm.dll.recyclerLayout.BaseData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/10/26.
 */

public class GetData {
    public static List<BaseData> dataList = new ArrayList<>();

    /**
     * 获取最新数据
     * @param url
     * @param time
     * @param type  类型 （0：公告栏）
     * @return
     * @throws OtherIOException
     * @throws NetworkErrorException
     */
    public static int getNewData(String url, long time,int type) throws OtherIOException, NetworkErrorException, JSONException {
        String result="";

        //params
        StringBuffer buffer = new StringBuffer();
        buffer.append("type=");
        buffer.append(0);
        buffer.append("&ref=0");
        buffer.append("&time=");
        buffer.append(time);


        result = HttpUtilsWithSession.post(url,buffer.toString());
        LogUtil.d("getChilds",result);


        int code = parseJson(result);

        if(code == 0){
            //saveDb(type);
        }

        return code;
    }

    /**
     * 获取旧的数据
     * @param url
     * @param time
     * @param type  类型 （0：公告栏）
     * @return
     * @throws OtherIOException
     * @throws NetworkErrorException
     */
    public static int getOldData(String url, long time,int type) throws OtherIOException, NetworkErrorException, JSONException {
        String result="";

        //params
        StringBuffer buffer = new StringBuffer();
        buffer.append("type=");
        buffer.append(0);
        buffer.append("&ref=1");
        buffer.append("&time=");
        buffer.append(time);


        result = HttpUtilsWithSession.post(url,buffer.toString());

        int code = parseJson(result);

        if(code == 0){
            //saveDb(type);
        }

        return code;
    }


    public static int parseJson(String result) throws JSONException {
        int code = 0;

        JSONObject object = null;

        LogUtil.e("getdata",result);

        object = new JSONObject(result);

        code = object.getInt("code");

        if(code == 0){
            dataList.clear();

            JSONArray dataArray = object.getJSONArray("data");
            if(dataArray != null){
                for(int i=0;i<dataArray.length();++i){
                    //获取每个公告栏的信息
                    JSONObject dataObj = dataArray.getJSONObject(i);
                    if(dataObj != null){
                        BaseData baseData = new BaseData();
                        baseData.setId(dataObj.getString("id"));
                        baseData.setContent(dataObj.getString("content"));
                        baseData.setTime(dataObj.getLong("time"));

                        String urls = dataObj.getString("url");
                        String url_scales = dataObj.getString("url_scale");

                        baseData.setUrls(urls);
                        baseData.setUrl_scales(url_scales);

                        //分割图片
                        if(!urls.isEmpty()){
                            List<ImageData> imageDataList = new ArrayList<>();

                            String[] urlArray = urls.split(";");
                            String[] urlScaleArray = url_scales.split(";");

                            for(int j=0;j<urlArray.length;++j){
                                ImageData imageData = new ImageData();
                                imageData.setUrl(urlArray[j]);
                                imageData.setUrl_scale(urlScaleArray[j]);
                                imageDataList.add(imageData);
                            }

                            baseData.setImageList(imageDataList);
                        }

                        //获取用户
                        JSONObject userObj = dataObj.getJSONObject("user");
                        User user = new User();
                        user.setId(userObj.getString("id"));
                        user.setName(userObj.getString("name"));
                        user.setUrl_scale(userObj.getString("url_scale"));
                        baseData.setUser(user);

                        dataList.add(baseData);
                    }


                }
            }
        }

        return code;
    }


    /**
     * 保存到数据库中
     */
    private static void saveDb(int type){

        if(dataList.size() == 0){
            return;
        }

        SQLiteDatabase db_reader = CustomApplication.getDbHelper().getReadableDatabase();
        SQLiteDatabase db_writer = CustomApplication.getDbHelper().getWritableDatabase();

        switch (type){
            case 0:{
                //保存到公告栏表中
                Cursor cursor = null;
                String sql = "";
                for(int i=0;i<dataList.size();++i){
                    BaseData baseData = dataList.get(i);

                    //结果是按照时间顺序排序的
                    cursor = db_reader.query("notice",new String[]{"id"},"id=?",new String[]{baseData.getId()},null,null,null);
                    if(cursor.moveToFirst()){
                        //在数据库中已经找到了该数据
                        continue;
                    }else{
                        User user = baseData.getUser();
                        //查找一下数据库中是否有当前的作者
                        cursor = db_reader.query("author",new String[]{"id"},"id=?",new String[]{user.getId()},null,null,null);
                        if(cursor.moveToFirst()){
                            //找到了该作者，不需要插入数据库
                        }else{

                            //需要插入数据库中
                            sql = "insert into user(id,name,phone,url_scale) values(?,?,?,?)";
                            db_writer.execSQL(sql,new Object[]{user.getId(),user.getName(),user.getPhone(),user.getUrl_scale()});
                        }

                        //插入消息到数据库中
                        sql = "insert into notice(id,content,time,user_id,url,url_scale) values(?,?,?,?,?,?)";
                        db_writer.execSQL(sql,new Object[]{baseData.getId(),baseData.getContent(),baseData.getTime(),user.getId(),baseData.getUrls()
                        ,baseData.getUrl_scales()});

                    }
                }
            }
            break;
        }

        db_reader.close();
        db_writer.close();
    }
}

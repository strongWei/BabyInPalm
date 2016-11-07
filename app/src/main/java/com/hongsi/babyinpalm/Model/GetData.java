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

public class GetData{
    public static List<BaseData> dataList = new ArrayList<>();
    //private static boolean isCancel;

//    private static boolean showMoreFromNet = true;

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
            saveDb(type);
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
    public static int getOldData(String url, long time,int type,boolean init) throws OtherIOException, NetworkErrorException, JSONException {
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
            SQLiteDatabase db_writer = CustomApplication.getDbHelper().getWritableDatabase();

            if(init){
                //正在初始化，删除缓存中的数据
                switch(type){
                    case 0:
                    {
                        String sql = "delete from notice";
                        db_writer.execSQL(sql);
                    }
                        break;

                    case 1:
                    {
                        String sql = "delete from eat";
                        db_writer.execSQL(sql);
                    }
                    break;

                    case 2:
                    {
                        String sql = "delete from class_list";
                        db_writer.execSQL(sql);
                    }
                    break;

                    case 3:
                    {
                        String sql = "delete from baby_dynamic";
                        db_writer.execSQL(sql);
                    }
                    break;

                    case 4:
                    {
                        String sql = "delete from message_board";
                        db_writer.execSQL(sql);
                    }
                    break;
                }
            }

            db_writer.close();

            //将数组中的数据保存到缓存中
            saveDb(type);

        }

        return code;
    }


    public static int parseJson(String result) throws JSONException {
        int code = 0;

        JSONObject object = null;

        //LogUtil.e("getdata",result);

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

                        if(dataObj.has("grades") && !dataObj.getString("grades").equals("[]")){
                            //存在班级标签
                            StringBuffer signBuf = new StringBuffer();
                            StringBuffer signScaleBuf = new StringBuffer();
                            JSONArray gradeArray = dataObj.getJSONArray("grades");
                            for(int k=0;k<gradeArray.length();++k){
                                signBuf.append(gradeArray.getJSONObject(k).getString("name"));
                                if(k!=gradeArray.length()-1) {
                                    signBuf.append("、");
                                }
                                if(k < 2){
                                    signScaleBuf.append(gradeArray.getJSONObject(k).getString("name"));
                                    if(k<1 && gradeArray.length()>1) {
                                        signScaleBuf.append("、");
                                    }
                                }else if(k==2){
                                    signScaleBuf.append("等 ");
                                    signScaleBuf.append(gradeArray.length());
                                    signScaleBuf.append(" 个班级");
                                }
                            }
                            baseData.setSign_scale(signScaleBuf.toString());
                            baseData.setSign(signBuf.toString());
                        }

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
                        if (userObj != null) {
                            //如果用户是空的，直接不显示这一条
                            User user = new User();
                            user.setId(userObj.getString("id"));
                            user.setName(userObj.getString("name"));
                            user.setUrl_scale(userObj.getString("url_scale"));

                            user.setRole( userObj.getJSONObject("role").getString("name"));
                            baseData.setUser(user);

                            dataList.add(baseData);
                        }

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

                        if(user != null) {
                            //查找一下数据库中是否有当前的作者
                            cursor = db_reader.query("author", new String[]{"id"}, "id=?", new String[]{user.getId()}, null, null, null);
                            if (cursor.moveToFirst()) {
                                //找到了该作者，不需要插入数据库
                            } else {

                                //需要插入数据库中
                                sql = "insert into author(id,name,phone,url_scale,role) values(?,?,?,?,?)";
                                db_writer.execSQL(sql, new Object[]{user.getId(), user.getName(), user.getPhone(), user.getUrl_scale(),user.getRole()});
                            }

                            //插入消息到数据库中
                            sql = "insert into notice(id,content,time,author_id,url,url_scale) values(?,?,?,?,?,?)";
                            db_writer.execSQL(sql,new Object[]{baseData.getId(),baseData.getContent(),baseData.getTime(),user.getId(),baseData.getUrls()
                                    ,baseData.getUrl_scales()});
                        }

                    }
                }
            }
            break;

            case 1:{
                //保存到饮食表中
                Cursor cursor = null;
                String sql = "";
                for(int i=0;i<dataList.size();++i){
                    BaseData baseData = dataList.get(i);

                    //结果是按照时间顺序排序的
                    cursor = db_reader.query("eat",new String[]{"id"},"id=?",new String[]{baseData.getId()},null,null,null);
                    if(cursor.moveToFirst()){
                        //在数据库中已经找到了该数据
                        continue;
                    }else{

                        User user = baseData.getUser();

                        if(user != null) {
                            //查找一下数据库中是否有当前的作者
                            cursor = db_reader.query("author", new String[]{"id"}, "id=?", new String[]{user.getId()}, null, null, null);
                            if (cursor.moveToFirst()) {
                                //找到了该作者，不需要插入数据库
                            } else {

                                //需要插入数据库中
                                sql = "insert into author(id,name,phone,url_scale,role) values(?,?,?,?,?)";
                                db_writer.execSQL(sql, new Object[]{user.getId(), user.getName(), user.getPhone(), user.getUrl_scale(),user.getRole()});
                            }

                            //插入消息到数据库中
                            sql = "insert into eat(id,content,time,author_id,url,url_scale) values(?,?,?,?,?,?)";
                            db_writer.execSQL(sql,new Object[]{baseData.getId(),baseData.getContent(),baseData.getTime(),user.getId(),baseData.getUrls()
                                    ,baseData.getUrl_scales()});
                        }

                    }
                }
            }
            break;

            case 2:{
                //保存到课堂安排表中
                Cursor cursor = null;
                String sql = "";
                for(int i=0;i<dataList.size();++i){
                    BaseData baseData = dataList.get(i);

                    //结果是按照时间顺序排序的
                    cursor = db_reader.query("class_list",new String[]{"id"},"id=?",new String[]{baseData.getId()},null,null,null);
                    if(cursor.moveToFirst()){
                        //在数据库中已经找到了该数据
                        continue;
                    }else{

                        User user = baseData.getUser();

                        if(user != null) {
                            //查找一下数据库中是否有当前的作者
                            cursor = db_reader.query("author", new String[]{"id"}, "id=?", new String[]{user.getId()}, null, null, null);
                            if (cursor.moveToFirst()) {
                                //找到了该作者，不需要插入数据库
                            } else {

                                //需要插入数据库中
                                sql = "insert into author(id,name,phone,url_scale,role) values(?,?,?,?,?)";
                                db_writer.execSQL(sql, new Object[]{user.getId(), user.getName(), user.getPhone(), user.getUrl_scale(),user.getRole()});
                            }

                            //插入消息到数据库中
                            sql = "insert into class_list(id,content,time,author_id,url,url_scale,sign) values(?,?,?,?,?,?,?)";
                            db_writer.execSQL(sql,new Object[]{baseData.getId(),baseData.getContent(),baseData.getTime(),user.getId(),baseData.getUrls()
                                    ,baseData.getUrl_scales(),baseData.getSign()});
                        }

                    }
                }
            }
            break;

            case 3:{
                //保存到宝宝动态表中
                Cursor cursor = null;
                String sql = "";
                for(int i=0;i<dataList.size();++i){
                    BaseData baseData = dataList.get(i);

                    //结果是按照时间顺序排序的
                    cursor = db_reader.query("baby_dynamic",new String[]{"id"},"id=?",new String[]{baseData.getId()},null,null,null);
                    if(cursor.moveToFirst()){
                        //在数据库中已经找到了该数据
                        continue;
                    }else{

                        User user = baseData.getUser();

                        if(user != null) {
                            //查找一下数据库中是否有当前的作者
                            cursor = db_reader.query("author", new String[]{"id"}, "id=?", new String[]{user.getId()}, null, null, null);
                            if (cursor.moveToFirst()) {
                                //找到了该作者，不需要插入数据库
                            } else {

                                //需要插入数据库中
                                sql = "insert into author(id,name,phone,url_scale,role) values(?,?,?,?,?)";
                                db_writer.execSQL(sql, new Object[]{user.getId(), user.getName(), user.getPhone(), user.getUrl_scale(),user.getRole()});
                            }

                            //插入消息到数据库中
                            sql = "insert into baby_dynamic(id,content,time,author_id,url,url_scale) values(?,?,?,?,?,?)";
                            db_writer.execSQL(sql,new Object[]{baseData.getId(),baseData.getContent(),baseData.getTime(),user.getId(),baseData.getUrls()
                                    ,baseData.getUrl_scales()});
                        }

                    }
                }
            }
            break;

            case 4:{
                //保存到留言版表中
                Cursor cursor = null;
                String sql = "";
                for(int i=0;i<dataList.size();++i){
                    BaseData baseData = dataList.get(i);

                    //结果是按照时间顺序排序的
                    cursor = db_reader.query("message_board",new String[]{"id"},"id=?",new String[]{baseData.getId()},null,null,null);
                    if(cursor.moveToFirst()){
                        //在数据库中已经找到了该数据
                        continue;
                    }else{

                        User user = baseData.getUser();

                        if(user != null) {
                            //查找一下数据库中是否有当前的作者
                            cursor = db_reader.query("author", new String[]{"id"}, "id=?", new String[]{user.getId()}, null, null, null);
                            if (cursor.moveToFirst()) {
                                //找到了该作者，不需要插入数据库
                            } else {

                                //需要插入数据库中
                                sql = "insert into author(id,name,phone,url_scale,role) values(?,?,?,?,?)";
                                db_writer.execSQL(sql, new Object[]{user.getId(), user.getName(), user.getPhone(), user.getUrl_scale(),user.getRole()});
                            }

                            //插入消息到数据库中
                            sql = "insert into message_board(id,content,time,author_id,url,url_scale) values(?,?,?,?,?,?)";
                            db_writer.execSQL(sql,new Object[]{baseData.getId(),baseData.getContent(),baseData.getTime(),user.getId(),baseData.getUrls()
                                    ,baseData.getUrl_scales()});
                        }

                    }
                }
            }
            break;
        }

        db_reader.close();
        db_writer.close();
    }

    /** 从数据库中获取数据 */
    public static void getDataFromDb(int type,long time){

        dataList.clear();

        SQLiteDatabase db_reader = CustomApplication.getDbHelper().getReadableDatabase();

        switch (type){
            case 0:{
                Cursor cursor = null;
                String sql = "";

                cursor = db_reader.query("notice",new String[]{"id","content","time","author_id","url","url_scale"},"time<?",new String[]{time+""},null,null,"time desc");
                while(cursor.moveToNext()){
                    String id = cursor.getString(0);
                    String content = cursor.getString(1);
                    long timeT = cursor.getLong(2);
                    String user_id = cursor.getString(3);
                    String url = cursor.getString(4);
                    String url_scale = cursor.getString(5);

                    //获取用户
                    Cursor cursor2 = db_reader.query("author",new String[]{"id","name","phone","url_scale","role"},"id=?",new String[]{user_id},null,null,null);
                    if(!cursor2.moveToFirst()){
                        //直接跳过该消息
                        continue;
                    }

                    User user = new User();
                    user.setId(cursor2.getString(0));
                    user.setName(cursor2.getString(1));
                    user.setPhone(cursor2.getString(2));
                    user.setUrl_scale(cursor2.getString(3));
                    user.setRole(cursor2.getString(4));

                    BaseData baseData = new BaseData();
                    baseData.setId(id);
                    baseData.setContent(content);
                    baseData.setTime(timeT);
                    baseData.setUrl_scales(url_scale);
                    baseData.setUrls(url);
                    baseData.setUser(user);

                    //分割图片
                    if(!url.isEmpty()){
                        List<ImageData> imageDataList = new ArrayList<>();

                        String[] urlArray = url.split(";");
                        String[] urlScaleArray = url_scale.split(";");

                        for(int j=0;j<urlArray.length;++j){
                            ImageData imageData = new ImageData();
                            imageData.setUrl(urlArray[j]);
                            imageData.setUrl_scale(urlScaleArray[j]);
                            imageDataList.add(imageData);
                        }

                        baseData.setImageList(imageDataList);
                    }

                    dataList.add(baseData);
                }
            }
                break;

            case 1:{
                Cursor cursor = null;
                String sql = "";

                cursor = db_reader.query("eat",new String[]{"id","content","time","author_id","url","url_scale"},"time<?",new String[]{time+""},null,null,"time");
                while(cursor.moveToNext()){
                    String id = cursor.getString(0);
                    String content = cursor.getString(1);
                    long timeT = cursor.getLong(2);
                    String user_id = cursor.getString(3);
                    String url = cursor.getString(4);
                    String url_scale = cursor.getString(5);

                    //获取用户
                    Cursor cursor2 = db_reader.query("author",new String[]{"id","name","phone","url_scale","role"},"id=?",new String[]{user_id},null,null,null);
                    if(!cursor2.moveToFirst()){
                        //直接跳过该消息
                        continue;
                    }

                    User user = new User();
                    user.setId(cursor2.getString(0));
                    user.setName(cursor2.getString(1));
                    user.setPhone(cursor2.getString(2));
                    user.setUrl_scale(cursor2.getString(3));
                    user.setRole(cursor2.getString(4));

                    BaseData baseData = new BaseData();
                    baseData.setId(id);
                    baseData.setContent(content);
                    baseData.setTime(timeT);
                    baseData.setUrl_scales(url_scale);
                    baseData.setUrls(url);
                    baseData.setUser(user);

                    dataList.add(baseData);
                }
            }
            break;

            case 2:{
                Cursor cursor = null;
                String sql = "";

                cursor = db_reader.query("class_list",new String[]{"id","content","time","author_id","url","url_scale","sign"},"time<?",new String[]{time+""},null,null,"time");
                while(cursor.moveToNext()){
                    String id = cursor.getString(0);
                    String content = cursor.getString(1);
                    long timeT = cursor.getLong(2);
                    String user_id = cursor.getString(3);
                    String url = cursor.getString(4);
                    String url_scale = cursor.getString(5);
                    String sign = cursor.getString(6);

                    //获取用户
                    Cursor cursor2 = db_reader.query("author",new String[]{"id","name","phone","url_scale","role"},"id=?",new String[]{user_id},null,null,null);
                    if(!cursor2.moveToFirst()){
                        //直接跳过该消息
                        continue;
                    }

                    User user = new User();
                    user.setId(cursor2.getString(0));
                    user.setName(cursor2.getString(1));
                    user.setPhone(cursor2.getString(2));
                    user.setUrl_scale(cursor2.getString(3));
                    user.setRole(cursor2.getString(4));

                    BaseData baseData = new BaseData();
                    baseData.setId(id);
                    baseData.setContent(content);
                    baseData.setTime(timeT);
                    baseData.setUrl_scales(url_scale);
                    baseData.setUrls(url);
                    baseData.setSign(sign);
                    baseData.setUser(user);

                    dataList.add(baseData);
                }
            }
            break;

            case 3:{
                Cursor cursor = null;
                String sql = "";

                cursor = db_reader.query("baby_dynamic",new String[]{"id","content","time","author_id","url","url_scale"},"time<?",new String[]{time+""},null,null,"time");
                while(cursor.moveToNext()){
                    String id = cursor.getString(0);
                    String content = cursor.getString(1);
                    long timeT = cursor.getLong(2);
                    String user_id = cursor.getString(3);
                    String url = cursor.getString(4);
                    String url_scale = cursor.getString(5);

                    //获取用户
                    Cursor cursor2 = db_reader.query("author",new String[]{"id","name","phone","url_scale","role"},"id=?",new String[]{user_id},null,null,null);
                    if(!cursor2.moveToFirst()){
                        //直接跳过该消息
                        continue;
                    }

                    User user = new User();
                    user.setId(cursor2.getString(0));
                    user.setName(cursor2.getString(1));
                    user.setPhone(cursor2.getString(2));
                    user.setUrl_scale(cursor2.getString(3));
                    user.setRole(cursor2.getString(4));

                    BaseData baseData = new BaseData();
                    baseData.setId(id);
                    baseData.setContent(content);
                    baseData.setTime(timeT);
                    baseData.setUrl_scales(url_scale);
                    baseData.setUrls(url);
                    baseData.setUser(user);

                    dataList.add(baseData);
                }
            }
            break;
        }

        db_reader.close();

    }

    /**删除某一项 */
    public static int deleteData(String url, String itemId,int type) throws OtherIOException, NetworkErrorException, JSONException {
        String result = "";
        //params
        StringBuffer buffer = new StringBuffer();
        buffer.append("type=");
        buffer.append(3);
        buffer.append("&id=");
        buffer.append(itemId);

        result = HttpUtilsWithSession.post(url,buffer.toString());

        int code = HttpUtilsWithSession.parseJson(result);

        if(code == 0) {
            //从数据库中删除
            deleteFormDb(type,itemId);
        }

        return code;
    }

    private static void deleteFormDb(int type, String itemId) {

        SQLiteDatabase db_writer = CustomApplication.getDbHelper().getWritableDatabase();

        switch (type){
            case 0:{
                String sql = "delete from notice where id=?";
                db_writer.execSQL(sql,new Object[]{itemId});
            }
            break;

            case 1:{
                String sql = "delete from eat where id=?";
                db_writer.execSQL(sql,new Object[]{itemId});
            }
            break;

            case 2:{
                String sql = "delete from class_list where id=?";
                db_writer.execSQL(sql,new Object[]{itemId});
            }
            break;

            case 3:{
                String sql = "delete from baby_dynamic where id=?";
                db_writer.execSQL(sql,new Object[]{itemId});
            }
            break;

            case 4:{
                String sql = "delete from message_board where id=?";
                db_writer.execSQL(sql,new Object[]{itemId});
            }
            break;
        }

        db_writer.close();
    }

}

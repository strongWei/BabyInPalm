package com.hongsi.babyinpalm.Utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2016/9/19.
 */
public class AppDbSqliteHelper extends SQLiteOpenHelper{

    public static final int VERSION = 1;        //默认的数据库版本

    public AppDbSqliteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建学生表，存储学生信息
        String sql = "create table student(id varchar(36),name varchar(40),sex tinyint(1),birth varchar(40),grade varchar(40),cards varchar(120),pic_path varchar(400),pic_scale_path varchar(400),start varchar(40))";
        db.execSQL(sql);

        //创建用户表，存储用户信息
        sql = "create table user(id varchar(36),name varchar(40),phone varchar(40),role varchar(40),url varchar(400),url_scale varchar(400),password varchar(100))";
        db.execSQL(sql);

        //创建作者表，存储作者的信息
        sql = "create table author(id varchar(36),name varchar(40),phone varchar(40),url_scale varchar(400),role varchar(10))";
        db.execSQL(sql);

        //创建公告栏表，存储公告栏信息
        sql = "create table notice(id varchar(36),content varchar(200),author_id varchar(36),url_scale varchar(3600),url varchar(3600),time long,foreign key(author_id) references author(id))";
        db.execSQL(sql);

        //创建饮食表，存储饮食信息
        sql = "create table eat(id varchar(36),content varchar(200),author_id varchar(36),url_scale varchar(3600),url varchar(3600),time long,foreign key(author_id) references author(id))";
        db.execSQL(sql);

        //创建课程安排表，存储课程信息
        sql = "create table class_list(id varchar(36),content varchar(200),author_id varchar(36),url_scale varchar(3600),url varchar(3600),time long,sign text,foreign key(author_id) references author(id))";
        db.execSQL(sql);

        //创建宝宝动态表，存储宝宝动态
        sql = "create table baby_dynamic(id varchar(36),content varchar(200),author_id varchar(36),url_scale varchar(3600),url varchar(3600),time long,foreign key(author_id) references author(id))";
        db.execSQL(sql);

        //创建留言版表，存储宝宝动态
        sql = "create table message_board(id varchar(36),content varchar(200),author_id varchar(36),url_scale varchar(3600),url varchar(3600),time long,foreign key(author_id) references author(id))";
        db.execSQL(sql);

        //创建教师联系方式表，存储教师信息
        sql = "create table tea_connect(id varchar(36),name varchar(40),phone varchar(11),url_scale varchar(200),detail varchar(200),role varchar(20),user_id varchar(36))";
        db.execSQL(sql);

        //创建学生联系方式表，存储学生信息
        sql = "create table par_connect(id varchar(36),name varchar(40),phone varchar(11),url_scale varchar(200),detail varchar(200),role varchar(20),user_id varchar(36))";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

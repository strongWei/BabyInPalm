package com.hongsi.babyinpalm.dll.showRecord;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/10/29.
 * 右测栏的考勤信息
 */

public class RecordData implements Serializable{
    private String id;      //记录Id
    private String time;    //时间
    private String name;    //姓名
    private String way;        //接送方式
    private String type;       //接送类型
    private String url_scale;   //接送照片缩略图
    private String url;         //接送照片大图

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWay() {
        return way;
    }

    public void setWay(String way) {
        this.way = way;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl_scale() {
        return url_scale;
    }

    public void setUrl_scale(String url_scale) {
        this.url_scale = url_scale;
    }
}

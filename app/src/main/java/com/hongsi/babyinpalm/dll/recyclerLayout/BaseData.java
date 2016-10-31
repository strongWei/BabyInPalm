package com.hongsi.babyinpalm.dll.recyclerLayout;

import com.hongsi.babyinpalm.Domain.ImageData;
import com.hongsi.babyinpalm.Domain.User;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2016/10/22.
 * 基础数据架构
 */

public class BaseData implements Serializable{

    private String id;
    private String content;
    private List<ImageData> imageList;
    private long time;
    private User user;
    private String urls;
    private String url_scales;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<ImageData> getImageList() {
        return imageList;
    }

    public void setImageList(List<ImageData> imageList) {
        this.imageList = imageList;
    }

    public String getUrl_scales() {
        return url_scales;
    }

    public void setUrl_scales(String url_scales) {
        this.url_scales = url_scales;
    }

    public String getUrls() {
        return urls;
    }

    public void setUrls(String urls) {
        this.urls = urls;
    }
}

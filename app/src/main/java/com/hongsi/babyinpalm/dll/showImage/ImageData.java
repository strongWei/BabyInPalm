package com.hongsi.babyinpalm.dll.showImage;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/10/27.
 */

public class ImageData implements Serializable{
    private String url;     //如果是网络数据，则显示的是url
    private int resId;      //如果是资源文件
    private String url_scale;   //如果是网络数据，则会有缩略图


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public String getUrl_scale() {
        return url_scale;
    }

    public void setUrl_scale(String url_scale) {
        this.url_scale = url_scale;
    }
}

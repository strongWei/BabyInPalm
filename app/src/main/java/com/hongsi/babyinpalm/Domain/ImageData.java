package com.hongsi.babyinpalm.Domain;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/10/25.
 */

public class ImageData implements Serializable {
    private String url;
    private String url_scale;

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

package com.hongsi.babyinpalm.Domain;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/11/1.
 */

public class ConnectData implements Serializable {
    private String id;
    private String name;
    private String role;
    private String phone;
    private String detail;
    private String url_scale;

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUrl_scale() {
        return url_scale;
    }

    public void setUrl_scale(String url_scale) {
        this.url_scale = url_scale;
    }
}

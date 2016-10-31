package com.hongsi.babyinpalm.Domain;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/10/22.
 */
public class User implements Serializable{
    private String id;
    private String name;
    private String phone;
    private String url;
    private String url_scale;
    private String role;
    private String password;
    private String encodePassword;

    public String getEncodePassword() {
        return encodePassword;
    }

    public void setEncodePassword(String encodePassword) {
        this.encodePassword = encodePassword;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

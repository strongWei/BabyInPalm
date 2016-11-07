package com.hongsi.babyinpalm.Domain;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/11/4.
 */

public class Organization implements Serializable{
    private String id;
    private String name;
    private boolean selected;

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

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}

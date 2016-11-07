package com.hongsi.babyinpalm.dll.showImage.Interface;

import com.hongsi.babyinpalm.dll.showImage.ImageData;

import java.util.List;

/**
 * Created by Administrator on 2016/10/29.
 */
public interface TransImageDataListener {
    public void setImageDataToActivityImageList(List<ImageData> datas,int position);

    public void deleteItem(int position);
}

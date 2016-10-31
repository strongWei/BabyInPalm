package com.hongsi.babyinpalm.dll.showImage;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.hongsi.babyinpalm.R;

/**
 * Created by Administrator on 2016/10/27.
 * 绑定图片的容器
 */

public class ImageViewHolder extends RecyclerView.ViewHolder {
    public ImageView scaleImageView;
    public ImageView imageView;

    public ImageViewHolder(View itemView) {
        super(itemView);
        scaleImageView = (ImageView) itemView.findViewById(R.id.scaleImageView);
        imageView = (ImageView) itemView.findViewById(R.id.imageView);
    }
}

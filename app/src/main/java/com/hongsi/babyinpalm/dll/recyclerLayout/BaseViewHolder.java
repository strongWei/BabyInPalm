package com.hongsi.babyinpalm.dll.recyclerLayout;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.hongsi.babyinpalm.R;

/**
 * Created by Administrator on 2016/10/22.
 * 绑定一些View
 */
public class BaseViewHolder extends RecyclerView.ViewHolder{

    public ImageView userImageView;
    public TextView userNameView;
    public TextView userIdView;
    public TextView roleView;
    public TextView signView;
    //public TextView signScaleView;

    public TextView baseIdView;
    public TextView contentView;
    public TextView timeView;
    public GridView imageGridView;

    public ImageView deleteImageView;


    public BaseViewHolder(View itemView) {
        super(itemView);
        userIdView = (TextView) itemView.findViewById(R.id.user_id);
        userNameView = (TextView) itemView.findViewById(R.id.user_name);
        userImageView = (ImageView) itemView.findViewById(R.id.user_image);
        roleView = (TextView) itemView.findViewById(R.id.roleName);
        signView = (TextView) itemView.findViewById(R.id.sign);
        //signScaleView = (TextView) itemView.findViewById(R.id.sign_scale);

        baseIdView = (TextView) itemView.findViewById(R.id.base_id);
        contentView = (TextView) itemView.findViewById(R.id.content);
        timeView = (TextView) itemView.findViewById(R.id.time);
        imageGridView = (GridView) itemView.findViewById(R.id.imageGridView);

        deleteImageView = (ImageView) itemView.findViewById(R.id.delete);

    }
}

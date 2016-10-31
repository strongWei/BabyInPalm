package com.hongsi.babyinpalm.dll.recyclerLayout;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.hongsi.babyinpalm.Domain.ImageData;
import com.hongsi.babyinpalm.R;
import com.hongsi.babyinpalm.Utils.Component.CustomApplication;

import java.util.List;

/**
 * Created by Administrator on 2016/10/23.
 */
public class ImageGAdapter extends BaseAdapter{

    private Context mContext;

    private LayoutInflater mLayooutInflater;

    public List<ImageData> mDataList;

    public ImageGAdapter(Context context,List<ImageData> dataList) {
        mContext = context;
        mDataList = dataList;
        mLayooutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        if(mDataList != null){
            return mDataList.size();
        }
        return 0;
    }

    class ViewHolder{
        public ImageView imageView;
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;

        if(convertView == null){
            convertView = mLayooutInflater.inflate(R.layout.base_image_item,parent,false);

            viewHolder = new ViewHolder();

            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.grid_image);

            convertView.setTag(viewHolder);

        }else{

            viewHolder = (ViewHolder) convertView.getTag();

        }


        //进行图片的加载
        String url_scale = mDataList.get(position).getUrl_scale();

        viewHolder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        viewHolder.imageView.setTag(url_scale);
        CustomApplication.getImageLoader().loadNetworkImage(viewHolder.imageView,url_scale);

        return convertView;
    }


}

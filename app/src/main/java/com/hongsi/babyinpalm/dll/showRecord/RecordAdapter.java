package com.hongsi.babyinpalm.dll.showRecord;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hongsi.babyinpalm.R;

import java.util.List;

/**
 * Created by Administrator on 2016/10/29.
 */

public class RecordAdapter extends BaseAdapter {

    private Context mContext;
    private List<RecordData> recordDatas;
    private LayoutInflater mInflater;

    public RecordAdapter(Context context, List<RecordData> recordDatas){
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        this.recordDatas = recordDatas;
    }

    class Component{
        public ImageView imageView;
        public TextView nameView;
        public TextView wayView;
        public TextView timeView;
        public TextView typeView;
    }

    @Override
    public int getCount() {
        if(recordDatas == null){
            return 0;
        }

        return recordDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return recordDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Component component = null;

        if(convertView == null){
            convertView = mInflater.inflate(R.layout.record_item,parent,false);

            component = new Component();
            component.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            component.timeView = (TextView) convertView.findViewById(R.id.timestamp);
            component.nameView = (TextView) convertView.findViewById(R.id.name);
            component.wayView = (TextView) convertView.findViewById(R.id.way);
            component.typeView = (TextView) convertView.findViewById(R.id.type);

            convertView.setTag(component);

        }else{
            component = (Component) convertView.getTag();
        }

        //数据绑定
        RecordData data = recordDatas.get(position);

        //图片
        String url_scale = data.getUrl_scale();
        component.imageView.setTag(url_scale);
        component.imageView.setImageResource(R.mipmap.teaa);

        component.timeView.setText(data.getTime());
        component.nameView.setText(data.getName());
        component.wayView.setText(data.getWay());
        component.typeView.setText(data.getType());


        return convertView;
    }
}

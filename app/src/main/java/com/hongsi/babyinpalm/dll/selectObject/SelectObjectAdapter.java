package com.hongsi.babyinpalm.dll.selectObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hongsi.babyinpalm.Domain.Organization;
import com.hongsi.babyinpalm.R;

import java.util.List;

/**
 * Created by Administrator on 2016/11/4.
 */

public class SelectObjectAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<Organization> organizationList;

    public SelectObjectAdapter(Context context,List<Organization> list){
        mContext = context;
        mInflater = LayoutInflater.from(context);
        organizationList = list;
    }

    class Component{
        private TextView idView;
        private TextView textView;
    }

    @Override
    public int getCount() {
        if(organizationList == null)
             return 0;
        return organizationList.size();
    }

    @Override
    public Object getItem(int position) {
        return organizationList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Component component = null;

        if(convertView == null){
            component = new Component();

            convertView = mInflater.inflate(R.layout.select_object_item,parent,false);

            component.idView = (TextView) convertView.findViewById(R.id.id);
            component.textView = (TextView) convertView.findViewById(R.id.text);

            convertView.setTag(component);

        }else{
            component = (Component) convertView.getTag();
        }

        Organization organization = organizationList.get(position);
        component.idView.setText(organization.getId());
        component.textView.setText(organization.getName());
        boolean selected = organization.isSelected();
        if(selected){
            component.textView.setBackgroundResource(R.drawable.gray_selected_object);
        }else{
            component.textView.setBackgroundResource(R.drawable.gray_unselect_object);
        }

        return convertView;
    }
}

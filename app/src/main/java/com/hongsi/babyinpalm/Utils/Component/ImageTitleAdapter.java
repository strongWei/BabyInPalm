package com.hongsi.babyinpalm.Utils.Component;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hongsi.babyinpalm.Domain.Student;
import com.hongsi.babyinpalm.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Created by Administrator on 2016/9/19. 用于装载图片+标题，用于显示学生列表
 */
public class ImageTitleAdapter extends BaseAdapter{

    private LayoutInflater layoutInflater;
    private Context context;


    private List<Student> data = null;

    public ImageTitleAdapter(Context context,List<Student> datas){
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        data = datas;
    }

    public void setData(List<Student> datas){
        data =datas;
    }

    public final class Component{
        public ImageView imageView;
        public TextView textView;
        public LinearLayout layout;
        public TextView idView;
    }

    @Override
    public int getCount() {
        if(data == null){
            return 0;
        }
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
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

            convertView = layoutInflater.inflate(R.layout.image_title_list,parent,false);
            component.imageView = (ImageView) convertView.findViewById(R.id.myImage);
            component.textView = (TextView) convertView.findViewById(R.id.myTitle);
            component.layout = (LinearLayout) convertView.findViewById(R.id.linear_back);
            component.idView = (TextView)convertView.findViewById(R.id.myId);

            convertView.setTag(component);

        }else{
            component = (Component) convertView.getTag();
        }


        Student student = data.get(position);
        String url = student.getUrl_scale();
        if(url.isEmpty()){
            component.imageView.setBackgroundResource(R.mipmap.app_icon);
        }else{
            component.imageView.setTag(url);
            CustomApplication.getImageLoader().loadNetworkImage(component.imageView,url);
        }

        component.textView.setText(student.getName());
        component.idView.setText(student.getId());

        return convertView;
    }
}

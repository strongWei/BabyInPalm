package com.hongsi.babyinpalm.Controller.fragment;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hongsi.babyinpalm.Controller.activity.ActivityDataList;
import com.hongsi.babyinpalm.R;
import com.hongsi.babyinpalm.Utils.Component.CustomApplication;
import com.hongsi.babyinpalm.Utils.Component.CustomPageAdapter;
import com.hongsi.babyinpalm.Utils.Component.ScrollViewPager;
import com.hongsi.babyinpalm.Utils.Component.SquareLayout;
import com.hongsi.babyinpalm.Utils.ImageLoader;
import com.hongsi.babyinpalm.Utils.LogUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2016/6/16.
 */
public class HomePageFragment extends Fragment implements ViewPager.OnPageChangeListener, View.OnClickListener {

    private static final String TAG = "HomePageFragment";

    private ScrollViewPager imagePager = null;

    private int currentImageIndex = 0;          //image pager's index of current selected item

    //viewPager: dot view to point to the current image of the image pager
    private View layoutView = null;
    private View firstDotImage = null;
    private View secondDotImage = null;
    private View thirdDotImage = null;
    private ImageView one_image = null;
    private ImageView two_image = null;
    private ImageView three_image = null;

    String tag1 = R.mipmap.car1 + "";
    String tag2 = R.mipmap.car2 + "";
    String tag3 = R.mipmap.car3 + "";

    private Handler handler = null;

    private SquareLayout noticeBtn = null;

    public HomePageFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layoutView =  inflater.inflate(R.layout.home_page_layout,container,false);


        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        if(imagePager!=null) {
                            if (currentImageIndex == imagePager.getChildCount())
                                currentImageIndex = 0;
                            else
                                ++currentImageIndex;

                            imagePager.setCurrentItem(currentImageIndex, true);
                            sendEmptyMessageDelayed(1, 3000);
                        }
                        break;
                    default:
                        break;
                }
            }
        };

        firstDotImage = layoutView.findViewById(R.id.first_image_dot);
        secondDotImage = layoutView.findViewById(R.id.second_image_dot);
        thirdDotImage = layoutView.findViewById(R.id.third_image_dot);

        noticeBtn = (SquareLayout) layoutView.findViewById(R.id.notice_btn);
        noticeBtn.setOnClickListener(this);

        initImageView();

        return layoutView;
    }


    public void initImageView(){

        ImageLoader imageLoader = CustomApplication.getImageLoader();

        one_image = new ImageView(layoutView.getContext());
        one_image.setTag(tag1);
        one_image.setScaleType(ImageView.ScaleType.CENTER_CROP);

        two_image = new ImageView(layoutView.getContext());
        two_image.setTag(tag2);
        two_image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        //two_image.setImageBitmap(BitmapCache.getInstance().getBitmap(R.mipmap.car2,layoutView.getContext()));

        three_image = new ImageView(layoutView.getContext());
        three_image.setTag(tag3);
        three_image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        //three_image.setImageBitmap(BitmapCache.getInstance().getBitmap(R.mipmap.car3,layoutView.getContext()));

        try {
            imageLoader.loadLocalImage(tag1,R.mipmap.car1,this.getActivity().getBaseContext(),one_image);
            imageLoader.loadLocalImage(tag2,R.mipmap.car2,this.getActivity().getBaseContext(),two_image);
            imageLoader.loadLocalImage(tag3,R.mipmap.car3,this.getActivity().getBaseContext(),three_image);
        } catch (IOException e) {
            LogUtil.e(TAG,e.getMessage());
        }


        final List<View> imageList = new ArrayList<View>();
        imageList.add(one_image);
        imageList.add(two_image);
        imageList.add(three_image);

        imagePager = (ScrollViewPager) layoutView.findViewById(R.id.image_pager);
        imagePager.setCanScroll(true);
        imagePager.setAdapter(new CustomPageAdapter(imageList));
        imagePager.addOnPageChangeListener(this);

        handler.sendEmptyMessageDelayed(1,3000);
    }

    @Override
    public void onDestroyView(){
//        LogUtil.d("HomePageFragment","destroy view");

        super.onDestroyView();

        handler = null;
        firstDotImage = null;
        secondDotImage = null;
        thirdDotImage = null;
        layoutView = null;

        one_image = null;
        two_image = null;
        three_image = null;

        imagePager = null;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        currentImageIndex = position;

        switch(position){
            case 0:
                secondDotImage.setBackgroundResource(R.drawable.image_dot_blue_empty_shape);
                thirdDotImage.setBackgroundResource(R.drawable.image_dot_blue_empty_shape);
                firstDotImage.setBackgroundResource(R.drawable.image_dot_blue_full_shape);

                break;
            case 1:
                firstDotImage.setBackgroundResource(R.drawable.image_dot_blue_empty_shape);
                thirdDotImage.setBackgroundResource(R.drawable.image_dot_blue_empty_shape);
                secondDotImage.setBackgroundResource(R.drawable.image_dot_blue_full_shape);

                break;
            case 2:
                firstDotImage.setBackgroundResource(R.drawable.image_dot_blue_empty_shape);
                secondDotImage.setBackgroundResource(R.drawable.image_dot_blue_empty_shape);
                thirdDotImage.setBackgroundResource(R.drawable.image_dot_blue_full_shape);

                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.notice_btn:{
                //转到公告栏
                Intent intent = new Intent(getActivity(), ActivityDataList.class);
                intent.putExtra("type",0);
                startActivity(intent);
            }
            break;
        }
    }
}

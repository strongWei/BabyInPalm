package com.hongsi.babyinpalm.dll.SelectImage.imageloader;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;


import com.hongsi.babyinpalm.Interface.SingleSelectListener;
import com.hongsi.babyinpalm.R;
import com.hongsi.babyinpalm.Utils.ToastUtil;
import com.hongsi.babyinpalm.dll.clipImage.ClipImageActivity;
import com.hongsi.babyinpalm.dll.SelectImage.utils.CommonAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MyAdapter extends CommonAdapter<String>
{

    /**
	 * 用户单一选择的图片，存储为图片的完整路径
	 */
	//public static List<String> mSelectedImage = new LinkedList<String>();

    /** 当选择图片时出现的监听器*/
    private SingleSelectListener mListener;

	/**
	 * 文件夹路径
	 */
	private String mDirPath;

	public MyAdapter(Context context, List<String> mDatas, int itemLayoutId,
					 String dirPath,SingleSelectListener listener)
	{
		super(context, mDatas, itemLayoutId);
		this.mDirPath = dirPath;
        this.mContext = context;
        this.mListener = listener;
	}

	@Override
	public void convert(final com.hongsi.babyinpalm.dll.SelectImage.utils.ViewHolder helper, final String item)
	{
		//设置no_pic
		helper.setImageResource(R.id.id_item_image, R.mipmap.pictures_no);
		//设置no_selected
				helper.setImageResource(R.id.id_item_select,
						R.mipmap.picture_unselected);
		//设置图片
		helper.setImageByUrl(R.id.id_item_image, mDirPath + "/" + item);
		
		final ImageView mImageView = helper.getView(R.id.id_item_image);
		final ImageView mSelect = helper.getView(R.id.id_item_select);
		mSelect.setVisibility(View.INVISIBLE);
		
		mImageView.setColorFilter(null);
		//设置ImageView的点击事件
		mImageView.setOnClickListener(new OnClickListener()
		{
			//选择，则将图片变暗，反之则反之
			@Override
			public void onClick(View v)
			{
				/*
				// 已经选择过该图片
				if (mSelectedImage.contains(mDirPath + "/" + item))
				{
					mSelectedImage.remove(mDirPath + "/" + item);
					mSelect.setImageResource(R.mipmap.picture_unselected);
					mImageView.setColorFilter(null);
				} else
				// 未选择该图片
				{
					//不能选择超过九张图片
					if(mSelectedImage.size() == 9){
						return;
					}

					mSelectedImage.add(mDirPath + "/" + item);
					mSelect.setImageResource(R.mipmap.pictures_selected);
					mImageView.setColorFilter(Color.parseColor("#77000000"));
				}
				*/
                //将当前这张图片保存到指定文件中
                File inFile = new File(mDirPath + "/" + item);

                if(!inFile.exists()){
                    ToastUtil.showToast(mContext,"文件不存在！",Toast.LENGTH_SHORT);
                    return;
                }
                try {
                    FileInputStream in = new FileInputStream(inFile);
                    FileOutputStream out = new FileOutputStream(ClipImageActivity.getTempImageBig());

                    byte[] bytes = new byte[1024];
                    int num = 0;
                    while((num = in.read(bytes)) != -1){
                        out.write(bytes,0,num);
                    }

                    in.close();
                    out.close();



                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mListener.getImagePath(ClipImageActivity.getTempImageBig().getAbsolutePath());
            }
		});

	}
}

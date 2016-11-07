package com.hongsi.babyinpalm.dll.SelectImage.imageloader;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.hongsi.babyinpalm.R;
import com.hongsi.babyinpalm.Utils.ToastUtil;
import com.hongsi.babyinpalm.dll.SelectImage.utils.CommonAdapter;

import java.util.LinkedList;
import java.util.List;

/**
 * 选择最多九张图片的适配器
 */
public class MyAdapterM extends CommonAdapter<String>
{

    /**
	 * 用户选择多张图片，存储为图片的完整路径
	 */
	public static List<String> mSelectedImage = new LinkedList<String>();

	/**
	 * 还可以再选几张图片
	 */
	public int canSelectImage = 0;

	/**
	 * 文件夹路径
	 */
	private String mDirPath;

	public MyAdapterM(Context context, List<String> mDatas, int itemLayoutId,
					  String dirPath)
	{
		super(context, mDatas, itemLayoutId);
		this.mDirPath = dirPath;
        this.mContext = context;

		mSelectedImage.clear();
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
		mSelect.setVisibility(View.VISIBLE);
		
		mImageView.setColorFilter(null);
		//设置ImageView的点击事件
		mImageView.setOnClickListener(new OnClickListener()
		{
			//选择，则将图片变暗，反之则反之
			@Override
			public void onClick(View v)
			{

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
					if(mSelectedImage.size() == canSelectImage){
						ToastUtil.showToast(mContext,R.string.no_more_9_img, Toast.LENGTH_SHORT);
						return;
					}

					mSelectedImage.add(mDirPath + "/" + item);
					mSelect.setImageResource(R.mipmap.pictures_selected);
					mImageView.setColorFilter(Color.parseColor("#77000000"));
				}

            }


		});

		/**
		 * 已经选择过的图片，显示出选择过的效果
		 */
		if (mSelectedImage.contains(mDirPath + "/" + item))
		{
			mSelect.setImageResource(R.mipmap.pictures_selected);
			mImageView.setColorFilter(Color.parseColor("#77000000"));
		}

	}
}

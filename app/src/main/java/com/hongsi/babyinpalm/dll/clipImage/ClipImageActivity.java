package com.hongsi.babyinpalm.dll.clipImage;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.hongsi.babyinpalm.Controller.activity.ActivityLogin;
import com.hongsi.babyinpalm.Controller.activity.ActivityPersonImage;
import com.hongsi.babyinpalm.Exception.NetworkErrorException;
import com.hongsi.babyinpalm.Exception.OtherIOException;
import com.hongsi.babyinpalm.Model.ChangeStuImage;
import com.hongsi.babyinpalm.Model.ChangeUserImage;
import com.hongsi.babyinpalm.Model.Login;
import com.hongsi.babyinpalm.R;
import com.hongsi.babyinpalm.Utils.BaseActivity;
import com.hongsi.babyinpalm.Utils.Component.CustomApplication;
import com.hongsi.babyinpalm.Utils.Component.UsualHeaderLayout;
import com.hongsi.babyinpalm.Utils.Component.WaitingDialog;
import com.hongsi.babyinpalm.Utils.ImageResUtils;
import com.hongsi.babyinpalm.Utils.LogUtil;
import com.hongsi.babyinpalm.Utils.ToastUtil;

import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * 裁剪头像图片的Activity
 *
 */
public class ClipImageActivity extends BaseActivity{
	private static final String TAG = "ClipImageActivity";
	public static final String PASS_PATH = "pass_path";
	private ClipImageLayout mClipImageLayout = null;

    private UsualHeaderLayout header = null;

	private String imagePath = "";

	private WaitingDialog waitingDialog = null;

	private int type;		//类型

	private String stuId;	//学生id

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dll_activity_clip_image);

        header = (UsualHeaderLayout) findViewById(R.id.header);
		header.setTitle(R.string.clip_image);

		header.setEdit2Text(R.string.complete);
        header.getBackView().setOnClickListener(l);
        header.getEdit2View().setOnClickListener(l);

        mClipImageLayout = (ClipImageLayout) findViewById(R.id.clipImageLayout);
		imagePath = getIntent().getStringExtra(PASS_PATH);
		// 有的系统返回的图片是旋转了，有的没有旋转，所以处理
		int degreee = readBitmapDegree(imagePath);
		Bitmap bitmap = createBitmap(imagePath);
		if (bitmap != null) {
			if (degreee == 0) {
				mClipImageLayout.setImageBitmap(bitmap);
			} else {
				mClipImageLayout.setImageBitmap(rotateBitmap(degreee, bitmap));
			}
		} else {
			onBackPressed();
			finish();
		}

		if(savedInstanceState != null){
			type = savedInstanceState.getInt("type");
			if(type==1){
				stuId = savedInstanceState.getString("stuId");
			}
		}else{
			type = getIntent().getIntExtra("type",-1);
			if(type==1){
				stuId = getIntent().getStringExtra("stuId");
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mClipImageLayout = null;
		header = null;
		waitingDialog = null;

		if(waitingDialog != null && waitingDialog.isShowing()){
			waitingDialog.dismiss();
			waitingDialog.stopAnimate();
			waitingDialog = null;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if(outState != null){
			outState.putInt("type",type);
			if(type == 1){
				outState.putString("stuId",stuId);
			}
		}
	}

	View.OnClickListener l = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()){
				case R.id.edit_2_u: {
					if (waitingDialog == null) {
						waitingDialog = new WaitingDialog(ClipImageActivity.this, R.style.DialogStyle);
					}
					waitingDialog.setText(R.string.imaging);
					waitingDialog.show();
					waitingDialog.startAnimate();


					LogUtil.e(TAG, "begin clip");
					Bitmap bitmap = mClipImageLayout.clip();

					LogUtil.e(TAG, "clip success");
					String path = imagePath;
					saveBitmap(bitmap, path);

					LogUtil.e(TAG, "save success");

					//提交按纽
					waitingDialog.setText(R.string.uploading);

					if (type == 0) {
						//修改用户的图片
						new ModifyUserImageAsync(path, ClipImageActivity.getTempImageSmall().getAbsolutePath()).execute();
						//ImageResUtils.scaleBitmap(path,ClipImageActivity.getTempImageSmall().getAbsolutePath(),200,200);
					} else if (type == 1) {
						//修改孩子的图片
						LogUtil.e(TAG, "begin send");
						new ModifyStuImageAsycn(path, ClipImageActivity.getTempImageSmall().getAbsolutePath()).execute();
					}
				}
					break;
				case R.id.back_u:
					onBackPressed();
					break;
			}
		}
	};

	/**
	 * 获取压缩图片文件
	 */
	public static File getTempImageSmall() {
		File file = null;
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) || !Environment.isExternalStorageRemovable()){
			file = new File(CustomApplication.getContext().getExternalCacheDir(),"image_temp");
		}else{
			file = new File(CustomApplication.getContext().getCacheDir(),"image_temp");
		}

		if(!file.exists()){
			file.mkdirs();
		}

		file = new File(file.getAbsolutePath(),"clip_scale_image.jpg");

		return file;
	}


	private void saveBitmap(Bitmap bitmap, String path) {
		File f = new File(path);
		if (f.exists()) {
			f.delete();
		}

		FileOutputStream fOut = null;
		try {
			f.createNewFile();
			fOut = new FileOutputStream(f);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
			fOut.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			try {
				if (fOut != null)
					fOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
     * 创建图片
     *
     * @param path
     * @return
     */
    private Bitmap createBitmap(String path) {
        if (path == null) {
            return null;
        }

        BitmapFactory.Options opts = new BitmapFactory.Options();
        //不在内存中读取图片的宽高
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);
        int width = opts.outWidth;

        opts.inSampleSize = width > 1080 ? (int)(width / 1080) : 1 ;//注意此处为了解决1080p手机拍摄图片过大所以做了一定压缩，否则bitmap会不显示

        opts.inJustDecodeBounds = false;// 这里一定要将其设置回false，因为之前我们将其设置成了true
        opts.inPurgeable = true;
        opts.inInputShareable = true;
        opts.inDither = false;
        opts.inPurgeable = true;
        FileInputStream is = null;
        Bitmap bitmap = null;
        try {
            is = new FileInputStream(path);
            bitmap = BitmapFactory.decodeFileDescriptor(is.getFD(), null, opts);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                    is = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return bitmap;
    }

	// 读取图像的旋转度
	private int readBitmapDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	// 旋转图片
	private Bitmap rotateBitmap(int angle, Bitmap bitmap) {
		// 旋转图片 动作
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		// 创建新的图片
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
				bitmap.getWidth(), bitmap.getHeight(), matrix, false);
		return resizedBitmap;
	}

    /** 获取大的图片文件*/
    public static File getTempImageBig(){
        File file = null;
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) || !Environment.isExternalStorageRemovable()){
			file = new File(CustomApplication.getContext().getExternalCacheDir(),"image_temp");
		}else{
			file = new File(CustomApplication.getContext().getCacheDir(),"image_temp");
		}

		if(!file.exists()){
			file.mkdirs();
		}

		file = new File(file.getAbsolutePath(),"clip_image.jpg");

		return file;
    }

	//远程修改学生图片线程
	private class ModifyStuImageAsycn extends AsyncTask<Void,Integer,Integer>{
		private String imagePath = "";
		private String scaleImagePath = "";

		public ModifyStuImageAsycn(String path,String scalePath){
			this.imagePath = path;
			this.scaleImagePath = scalePath;
		}

		@Override
		protected Integer doInBackground(Void... params) {
			/**
			 * code ：0（无错误）
			 -1（需要重新登陆）-2（服务器异常）-3（未指定文件名） -4（没有图片数据）-6（图片解密保存失败）
			 */

			try {

				File file = ImageResUtils.scaleBitmap(imagePath,scaleImagePath,200,200);
				if(file == null){
					return R.string.file_no_exists;
				}

				int code =  ChangeStuImage.modify(file,ClipImageActivity.this.stuId);
				switch (code){
					case 0:
					{
						return R.string.upload_success;
					}
					case -1:
					{
						//需要重新登陆后再进行修改
						int code1 = Login.login(Login.user.getPhone(), Login.user.getPassword());
						if(code1 == -1){
							//用户名或密码错误
							return R.string.login_error;
						}else if(code1 == -2){
							//服务器异常
							return R.string.server_error;
						}

						//进行修改
						code =  ChangeUserImage.modify(file);
						switch (code){
							case 0:
								return R.string.modify_complete;
							case -1:
								return R.string.account_exception;
							case -2:
								return R.string.server_error;
							case -3:
								return R.string.fileName_empty;
							case -5:
								return R.string.stuid_invalid;
							case -6:
								return R.string.saveImage_fail;
							case -7:
								return R.string.pic_invalie;
						}
					}
					case -2:
					{
						return R.string.server_error;
					}
					case -3:
					{
						return R.string.fileName_empty;
					}
					case -5:
					{
						return R.string.stuid_invalid;
					}
					case -6:
					{
						return R.string.saveImage_fail;
					}
					case -7:{
						return R.string.pic_invalie;
					}
				}

			} catch (OtherIOException e) {
				return R.string.other_error;
			} catch (NetworkErrorException e) {
				return R.string.net_error;
			} catch (JSONException e) {
				return R.string.data_error;
			}

			return 0;
		}

		@Override
		protected void onPostExecute(Integer integer) {
			super.onPostExecute(integer);
			waitingDialog.dismiss();
			waitingDialog.stopAnimate();

			int i = integer.intValue();

			ToastUtil.showToast(ClipImageActivity.this,i, Toast.LENGTH_SHORT);

			//如果出现重新登陆后依然提示重新登陆或者是用户名或密码出现异常，则转到登陆界面
			if(i==R.string.account_exception || i==R.string.login_error){
				Intent intent = new Intent(ClipImageActivity.this,ActivityLogin.class);
				startActivity(intent);
			}

			if(i==R.string.upload_success){
				Intent intent = new Intent(ClipImageActivity.this,ActivityPersonImage.class);
				setResult(RESULT_OK,intent);

				finish();
			}
		}
	}

	//远程修改用户图片线程
	private class ModifyUserImageAsync extends AsyncTask<Void,Integer,Integer> {

		private String imagePath = "";
		private String scaleImagePath = "";


		public ModifyUserImageAsync(String path,String scalePath) {
			this.imagePath = path;
			this.scaleImagePath = scalePath;
		}

		@Override
		protected Integer doInBackground(Void... params) {
			/**
			 * code ：0（无错误）
			 -1（需要重新登陆）-2（服务器异常）-3（未指定文件名） -4（没有图片数据）-6（图片解密保存失败）
			 */

			try {

				File file = ImageResUtils.scaleBitmap(imagePath,scaleImagePath,200,200);
				if(file == null){
					return R.string.file_no_exists;
				}

				int code =  ChangeUserImage.modify(file);
				switch (code){
					case 0:
					{
						return R.string.upload_success;
					}
					case -1:
					{
						//需要重新登陆后再进行修改
						int code1 = Login.login(Login.user.getPhone(), Login.user.getPassword());
						if(code1 == -1){
							//用户名或密码错误
							return R.string.login_error;
						}else if(code1 == -2){
							//服务器异常
							return R.string.server_error;
						}

						//进行修改
						code =  ChangeUserImage.modify(file);
						switch (code){
							case 0:
								return R.string.modify_complete;
							case -1:
								return R.string.account_exception;
							case -2:
								return R.string.server_error;
							case -3:
								return R.string.fileName_empty;
							case -4:
								return R.string.fileByte_empty;
							case -5:
								return R.string.saveImage_fail;
						}
					}
					case -2:
					{
						return R.string.server_error;
					}
					case -3:
					{
						return R.string.fileName_empty;
					}
					case -4:
					{
						return R.string.fileByte_empty;
					}
                    case -6:
                    {
                        return R.string.saveImage_fail;
                    }
				}

			} catch (OtherIOException e) {
				return R.string.other_error;
			} catch (NetworkErrorException e) {
				return R.string.net_error;
			} catch (JSONException e) {
				return R.string.data_error;
			}

			return 0;

		}

		@Override
		protected void onPostExecute(Integer integer) {

			waitingDialog.dismiss();
			waitingDialog.stopAnimate();

			int i = integer.intValue();

			ToastUtil.showToast(ClipImageActivity.this,i, Toast.LENGTH_SHORT);

			//如果出现重新登陆后依然提示重新登陆或者是用户名或密码出现异常，则转到登陆界面
			if(i==R.string.account_exception || i==R.string.login_error){
				Intent intent = new Intent(ClipImageActivity.this,ActivityLogin.class);
				startActivity(intent);
			}

			if(i==R.string.upload_success){
				modifyUserImageInSharePref();
				Intent intent = new Intent(ClipImageActivity.this,ActivityPersonImage.class);
				setResult(RESULT_OK,intent);

				finish();
			}

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
	}

	/**在配置文件中进行图片的修改*/
	private void modifyUserImageInSharePref(){
		SQLiteDatabase db =  CustomApplication.getDbHelper().getWritableDatabase();

		ContentValues cv = new ContentValues();
		cv.put("url",Login.user.getUrl());
		cv.put("url_scale",Login.user.getUrl_scale());

		String whereClause = "phone=?";
		String[] whereArgs = {Login.user.getPhone()};

		db.update("user",cv,whereClause,whereArgs);

		db.close();
	}

}

package com.hongsi.babyinpalm.Utils;

import android.util.Log;

public class LogUtil {

	private final static int VERBOSE = 1;
	private final static int DEBUG = 2;
	private final static int INFO = 3;
	private final static int WARN = 4;
	private final static int ERROR = 5;
	private final static int NOTHING = 6;
	
	private final static int LEVEL = VERBOSE;
	
	public static void v(String tag, String msg){
		if(LEVEL <= VERBOSE){
			Log.v(tag, msg);
		}
	}
	
	public static void d(String tag, String msg){
		if(LEVEL <= DEBUG){
			Log.d(tag, msg);
		}
	}
	
	public static void i(String tag, String msg){
		if(LEVEL <= INFO){
			Log.i(tag, msg);
		}
	}
	
	public static void w(String tag, String msg){
		if(LEVEL <= WARN){
			Log.w(tag, msg);
		}
	}
	
	public static void e(String tag, String msg){
		if(LEVEL <= ERROR){
			Log.e(tag, msg);
		}
	}
}

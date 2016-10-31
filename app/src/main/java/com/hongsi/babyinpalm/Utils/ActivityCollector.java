package com.hongsi.babyinpalm.Utils;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

import com.hongsi.babyinpalm.Utils.Component.CustomApplication;

public class ActivityCollector {

	public static List<Activity> activities = new ArrayList<Activity>();
	
	public static void addActivity(Activity activity){
		activities.add(activity);
	}
	
	public static void removeActivity(Activity activity){
		activities.remove(activity);
	}
	
	public static void finishAllActivity(){
		for(Activity ac : activities){
			if(!ac.isFinishing()){
				activities.remove(ac);
				ac.finish();
				ac = null;
			}
		}

		CustomApplication.closeImageLoader();
	}
}

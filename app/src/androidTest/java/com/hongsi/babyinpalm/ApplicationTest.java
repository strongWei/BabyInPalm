package com.hongsi.babyinpalm;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.hongsi.babyinpalm.Utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() throws JSONException {
        super(Application.class);
    }

    public void test() {
        String result = "{\"code\":0,\"data\":null}";
        try {
            JSONObject object = new JSONObject(result);

            boolean re = false;

            re = (object.has("ke"));

            LogUtil.e("tag",re + "");

        } catch (JSONException e) {
            e.printStackTrace();
        }


        /*
        if(object.get("result") == null){
            System.out.print("get: null");
        }


        if(object.getJSONObject("result") == null){
            System.out.print("getjsonobj: null");
        }

        if(object.getJSONArray("result") == null){
            System.out.print("getjsonarr: null");
        }
        */
    }
}
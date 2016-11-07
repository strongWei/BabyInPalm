package com.hongsi.babyinpalm;

import com.android.internal.http.multipart.MultipartEntity;

import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    public void postMultipart(){
        HttpPost httpPost = new HttpPost("http://192.168.1.249:8080/YunSchool/app/user?type=2");
        httpPost.setHeader("Set-Cookie","JSESSIONID=4c6755e7d1ebdad7228bd6e08c515d9; Path=/YunSchool/; HttpOnly");



        MultipartEntity multipartEntity = new MultipartEntity(null);
        File file = new File("D:\\strong\\安卓图标\\掌通宝宝48.png");


    }

    @Test
    public void testJsonObject() throws JSONException {
        String result = "{\"code\":0,\"data\":null}";
        JSONObject object = new JSONObject(result);

        System.out.print(object.has("data") + "");

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
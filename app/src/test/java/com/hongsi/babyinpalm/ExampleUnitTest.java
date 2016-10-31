package com.hongsi.babyinpalm;

import com.android.internal.http.multipart.MultipartEntity;
import com.android.internal.http.multipart.Part;

import org.apache.http.client.methods.HttpPost;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

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

}
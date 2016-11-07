package com.hongsi.babyinpalm.Utils;

import com.hongsi.babyinpalm.Exception.NetworkErrorException;
import com.hongsi.babyinpalm.Exception.OtherIOException;
import com.hongsi.babyinpalm.Utils.Component.CustomApplication;

import java.io.BufferedReader;
import java.io.IOException;

import java.io.InputStreamReader;

import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;


/**
 * Created by Administrator on 2016/9/26.
 */
public class HttpUtils {

    private static final String TAG = "HttpUtils";
    public static final String BASE_URL = "http://120.76.124.221:8080/YunSchool";

    //用于get请求（发送字符串与返回字符串的接口）
    public static String get(String url) throws NetworkErrorException,OtherIOException{

        String returnStr = "";
        BufferedReader in = null;
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();

            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            conn.setRequestProperty("Accept-Language","zh-CN,en-US;q=0.7,en;q=0.3");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            conn.setRequestProperty("User-Agent", CustomApplication.getUserAgent());
            conn.setRequestProperty("Connection", "Keep-Alive");

            conn.setRequestMethod("GET");
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);

            conn.connect();

            in = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));

            StringBuffer outBuffer = new StringBuffer();
            String line="";
            while((line = in.readLine()) !=null){
                outBuffer.append(line);
                outBuffer.append("\n");
            }

            returnStr = outBuffer.toString();

            return returnStr;
        } catch(UnknownHostException e){
            //unknown remote service, we can try to check the valid of network
            LogUtil.e(TAG,e.getMessage());
            //e.printStackTrace();

            throw new NetworkErrorException();

        }catch(ConnectException e){
            //remote service refuse, we can try to check the started of remote service
            LogUtil.e(TAG,e.getMessage());
            //e.printStackTrace();

            throw new NetworkErrorException();

        }catch(SocketTimeoutException e){
            LogUtil.e(TAG,e.getMessage());
            //e.printStackTrace();

            throw new NetworkErrorException();

        }catch (IOException e) {

            LogUtil.e(TAG,e.getMessage());
            //e.printStackTrace();

            throw new OtherIOException();

        }finally {
            try {
                if (conn != null) {
                    conn.disconnect();
                }


                if (in != null) {
                    in.close();
                }
            }catch (IOException e) {

                LogUtil.e(TAG,e.getMessage());
                e.printStackTrace();

                throw new OtherIOException();

            }

        }
    }

}

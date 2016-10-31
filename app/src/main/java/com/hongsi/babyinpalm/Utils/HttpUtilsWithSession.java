package com.hongsi.babyinpalm.Utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.hongsi.babyinpalm.Exception.NetworkErrorException;
import com.hongsi.babyinpalm.Exception.OtherIOException;
import com.hongsi.babyinpalm.Utils.Component.CustomApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


/**
 * Created by Administrator on 2016/9/26. 处理有session的请求
 */
public class HttpUtilsWithSession implements Observer{

    private static final String TAG = "HttpUtilsWithSession";
    private static final String PREFIX = "--";
    private static final String LINE_END = "\r\n";
    private static String jSessionId = "";
    private static boolean isCancel = false;

    public HttpUtilsWithSession(Observable observable) {
        observable.addObserver(this);
    }

    static{
        jSessionId = getJSessionId();
    }

    public static String post(String url, String param) throws NetworkErrorException, OtherIOException {
        PrintWriter out = null;
        BufferedReader in = null;
        StringBuffer buffer = new StringBuffer();
        HttpURLConnection conn = null;

        try {

            URL uri = new URL(url);

            conn = (HttpURLConnection) uri.openConnection();
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            conn.setRequestProperty("Accept-Language","zh-CN,en-US;q=0.7,en;q=0.3");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            conn.setRequestProperty("User-Agent", CustomApplication.getUserAgent());

            if(!jSessionId.isEmpty()){
                conn.setRequestProperty("Cookie",jSessionId);
            }

            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            out = new PrintWriter(conn.getOutputStream());
            out.print(param);
            out.flush();

            in = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));

            String newJSessionId = conn.getHeaderField("Set-Cookie");
            if(newJSessionId!=null){
                //不为空时，说明旧的session已经失效了，因此要更新
                jSessionId = newJSessionId;

                saveJSessionId(jSessionId);
            }

            String line="";

            while ((line = in.readLine()) != null && !isCancel) {
                buffer.append("\r\n");
                buffer.append(line);
            }

            return buffer.toString();

        }catch(UnknownHostException e3){
            //unknown remote service, we can try to check the valid of network
            throw new NetworkErrorException();

        }catch(ConnectException e1){
            //remote service refuse, we can try to check the started of remote service
            throw new NetworkErrorException();

        }catch(SocketTimeoutException e2){
            //time out
            throw new NetworkErrorException();

        }catch(IOException e){
            //other io exception
            e.printStackTrace();
            throw new OtherIOException();

        }finally {

            try {
                if(conn!=null)
                    conn.disconnect();

                if (in != null) {
                    in.close();
                }

                if(out!=null){
                    out.close();
                }

            }catch (IOException e){
                //other io exception
                throw new OtherIOException();
            }
        }
    }

    private static String getJSessionId(){
        return PreferenceManager.getDefaultSharedPreferences(CustomApplication.getContext()).getString("jSessionId","");
    }

    private static void saveJSessionId(String newJSession){
        SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(CustomApplication.getContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("jSessionId",newJSession);
        editor.commit();
    }

    public static int parseJson(String result) throws JSONException {
        JSONObject object = new JSONObject(result);

        //boolean r = object.getBoolean("return");
        int code = object.getInt("code");

        return code;
    }

    public static String postFile(String url, File file, String fileName) throws NetworkErrorException, OtherIOException {
        BufferedReader in = null;
        StringBuffer buffer = new StringBuffer();
        HttpURLConnection conn = null;
        OutputStream out = null;

        try {

            URL uri = new URL(url);

            String BOUNDARY = "HEHEHHE"; //边界标识 随机生成 String PREFIX = "--" , LINE_END = "\r\n";
            StringBuffer contentTypeBuffer = new StringBuffer();
            contentTypeBuffer.append("multipart/form-data; boundary=" );
            contentTypeBuffer.append(BOUNDARY);

            conn = (HttpURLConnection) uri.openConnection();
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            conn.setRequestProperty("Accept-Language","zh-CN,en-US;q=0.7,en;q=0.3");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("User-Agent", CustomApplication.getUserAgent());
            conn.setRequestProperty("Charset","UTF-8");
            conn.setRequestProperty("Content-Type", contentTypeBuffer.toString());


            if(!jSessionId.isEmpty()){
                conn.setRequestProperty("Cookie",jSessionId);
            }

            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            /** * 当文件不为空，把文件包装并且上传 */
            out=conn.getOutputStream();
            DataOutputStream dos = new DataOutputStream(out);
            StringBuffer sb = new StringBuffer();
            sb.append(PREFIX);
            sb.append(BOUNDARY);
            sb.append(LINE_END);
            /**
             * 这里重点注意：
             * name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件
             * filename是文件的名字，包含后缀名的 比如:abc.png
             */
            sb.append("Content-Disposition: form-data; name=\"data\"; filename=\""+fileName+"\""+LINE_END);
            sb.append("Content-Type: application/octet-stream"+LINE_END);
            sb.append("Content-Transfer-Encoding: binary");
            sb.append(LINE_END);
            sb.append(LINE_END);
            dos.write(sb.toString().getBytes());

            FileInputStream ins = new FileInputStream(file);

            byte[] bytes = new byte[1024];
            int num = 0;
            while((num = ins.read(bytes)) != -1 && !isCancel){
                dos.write(bytes,0,num);
            }

            ins.close();
            dos.write(LINE_END.getBytes());
            byte[] end_data = (PREFIX+BOUNDARY+PREFIX).getBytes();
            dos.write(end_data);
            dos.flush();
            dos.close();

            in = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));

            String newJSessionId = conn.getHeaderField("Set-Cookie");
            if(newJSessionId!=null){
                //不为空时，说明旧的session已经失效了，因此要更新
                jSessionId = newJSessionId;

                saveJSessionId(jSessionId);
            }

            String line="";

            while ((line = in.readLine()) != null && !isCancel) {
                buffer.append("\n");
                buffer.append(line);
            }

            return buffer.toString();

        }catch(UnknownHostException e3){
            //unknown remote service, we can try to check the valid of network
            throw new NetworkErrorException();

        }catch(ConnectException e1){
            //remote service refuse, we can try to check the started of remote service
            throw new NetworkErrorException();

        }catch(SocketTimeoutException e2){
            //time out
            throw new NetworkErrorException();

        }catch(IOException e){
            //other io exception
            e.printStackTrace();
            throw new OtherIOException();

        }finally {

            try {
                conn.disconnect();

                if (in != null) {
                    in.close();
                }

                if(out!=null){
                    out.close();
                }

            }catch (IOException e){
                //other io exception
                throw new OtherIOException();
            }
        }
    }

    /**
     * 带session 发送到服务器
     * @param url       网络链接
     * @param data      文本数据
     * @param files     文件组（最多九个）
     *
     * @return
     * @throws NetworkErrorException
     * @throws OtherIOException
     */
    public static String postFiles(String url, String data, List<File> files) throws NetworkErrorException, OtherIOException {
        BufferedReader in = null;
        StringBuffer buffer = new StringBuffer();
        HttpURLConnection conn = null;
        OutputStream out = null;

        try {

            URL uri = new URL(url);

            String BOUNDARY = "HEHEHHE"; //边界标识 随机生成 String PREFIX = "--" , LINE_END = "\r\n";
            StringBuffer contentTypeBuffer = new StringBuffer();
            contentTypeBuffer.append("multipart/form-data; boundary=" );
            contentTypeBuffer.append(BOUNDARY);

            conn = (HttpURLConnection) uri.openConnection();
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            conn.setRequestProperty("Accept-Language","zh-CN,en-US;q=0.7,en;q=0.3");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("User-Agent", CustomApplication.getUserAgent());
            conn.setRequestProperty("Charset","UTF-8");
            conn.setRequestProperty("Content-Type", contentTypeBuffer.toString());


            if(!jSessionId.isEmpty()){
                conn.setRequestProperty("Cookie",jSessionId);
            }

            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            out=conn.getOutputStream();
            DataOutputStream dos = new DataOutputStream(out);


            /** 当文字内容不为空 */
            if(data != null && data.length() >0){
                StringBuffer sb = new StringBuffer();
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINE_END);

                sb.append("Content-Disposition: form-data; name=\"content\""+LINE_END);
                sb.append("Content-Type: text-plain; charset=UTF-8"+LINE_END);
                sb.append("Content-Transfer-Encoding: 8bit");
                sb.append(LINE_END);
                sb.append(LINE_END);

                sb.append(data);
                sb.append(LINE_END);

                dos.write(sb.toString().getBytes());
            }


            /** * 当文件不为空，把文件包装并且上传 */
            for (int i=0;files != null && i<files.size();++i) {
                StringBuffer sb = new StringBuffer();
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINE_END);
                /**
                 * 这里重点注意：
                 * name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件
                 * filename是文件的名字，包含后缀名的 比如:abc.png
                 */
                sb.append("Content-Disposition: form-data; name=\"data\"; filename=\"" + i + ".jpg" + "\"" + LINE_END);
                sb.append("Content-Type: application/octet-stream" + LINE_END);
                sb.append("Content-Transfer-Encoding: binary");
                sb.append(LINE_END);
                sb.append(LINE_END);
                dos.write(sb.toString().getBytes());

                FileInputStream ins = new FileInputStream(files.get(i));

                byte[] bytes = new byte[1024];
                int num = 0;
                while ((num = ins.read(bytes)) != -1 && !isCancel) {
                    dos.write(bytes, 0, num);
                }

                ins.close();
                dos.write(LINE_END.getBytes());
            }

            byte[] end_data = (PREFIX+BOUNDARY+PREFIX).getBytes();
            dos.write(end_data);
            dos.flush();
            dos.close();

            in = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));

            String newJSessionId = conn.getHeaderField("Set-Cookie");
            if(newJSessionId!=null){
                //不为空时，说明旧的session已经失效了，因此要更新
                jSessionId = newJSessionId;

                saveJSessionId(jSessionId);
            }

            String line="";

            while ((line = in.readLine()) != null && !isCancel) {
                buffer.append("\n");
                buffer.append(line);
            }

            return buffer.toString();

        }catch(UnknownHostException e3){
            //unknown remote service, we can try to check the valid of network
            throw new NetworkErrorException();

        }catch(ConnectException e1){
            //remote service refuse, we can try to check the started of remote service
            throw new NetworkErrorException();

        }catch(SocketTimeoutException e2){
            //time out
            throw new NetworkErrorException();

        }catch(IOException e){
            //other io exception
            e.printStackTrace();
            throw new OtherIOException();

        }finally {

            try {
                conn.disconnect();

                if (in != null) {
                    in.close();
                }

                if(out!=null){
                    out.close();
                }

            }catch (IOException e){
                //other io exception
                throw new OtherIOException();
            }
        }
    }



    public static void clearSession() {
        jSessionId = "";
        SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(CustomApplication.getContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("jSessionId","");
        editor.commit();
    }

    @Override
    public void update(Observable o, Object arg) {
        
    }
}

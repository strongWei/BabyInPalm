package com.hongsi.babyinpalm.dll.SelectImage.utils;


import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class ImageFileUtils {

    // 打印日志
    private static final String TAG = "FileTest";

    public static void main(String[] args) {

        // 定义list，用于存储数据文件的全路径
        List<String> filelist = new ArrayList<String>();
        String dataFileTempDir = "D:\\test";
        // 得到返回文件全路径的list集合
        List<String> list = getFiles(dataFileTempDir, filelist);
        String dataFileTempPath = null;
        for (int i = 0; i < list.size(); i++) {
            // 数据文件在临时区的路径
            dataFileTempPath = list.get(i);
            System.out.println(i+"dataFileTempPath:"+dataFileTempPath);
        }

    }

    /**
     * 通过递归得到某一路径下所有的文件的全路径,分装到list里面
     *
     * @param filePath
     * @param filelist
     * @return
     */
    public static List<String> getFiles(String filePath, List<String> filelist) {

        File root = new File(filePath);
        if (!root.exists()) {
            Log.i(TAG,filePath + " not exist!");
        } else {
            File[] files = root.listFiles();
            Arrays.sort(files, new ImageFileUtils.CompratorByLastModified());
            for (File file : files) {
                if (file.isDirectory()) {
                    //getFiles(file.getAbsolutePath(), filelist);
                } else if(file.getName().endsWith(".jpg") || file.getName().endsWith(".jpeg") || file.getName().endsWith(".png")){
                    //logger.info("目录:" + filePath + "文件全路径:" + file.getAbsolutePath());
                    filelist.add(file.getName());
                }
            }
        }
        return filelist;
    }

    //根据文件修改时间进行比较的内部类
    static class CompratorByLastModified implements Comparator<File> {

        public int compare(File f1, File f2) {
            long diff = f1.lastModified() - f2.lastModified();
            if (diff > 0) {
                return -1;
            } else if (diff == 0) {
                return 0;
            } else {
                return 1;
            }
        }
    }

}
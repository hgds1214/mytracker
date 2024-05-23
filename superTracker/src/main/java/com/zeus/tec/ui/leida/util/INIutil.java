package com.zeus.tec.ui.leida.util;

import android.os.Build;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class INIutil {

    public static String readINI (String path,String key, String defaultValue){
        Properties properties = new Properties();
        String value ="";
        try {
            FileInputStream inputStream = new FileInputStream(path); // 替换为实际文件路径
            properties.load(inputStream);

            // 获取属性值
           value  = properties.getProperty(key,defaultValue);

           // System.out.println("Value for key 'key': " + value);

            // 遍历所有键值对
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                properties.forEach((key, val) -> {
//                    System.out.println(key + " = " + val);
//                });
//            }

            inputStream.close();
            return value;
        } catch (IOException e) {
            e.printStackTrace();
            return value;
        }

    }

    public static void writeproperties (Properties properties,String path)  {

        try {
            FileOutputStream outputStream = new FileOutputStream(path );
            properties.store(outputStream,"whcs");
            outputStream.close();
            return;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }



    }
}

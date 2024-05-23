package com.zeus.tec.ui.leida.util;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import io.objectbox.flatbuffers.ByteBufferReadWriteBuf;

public class IOtool {
    public static void writeToFileInPrivateSpace(Context context, String fileName, byte [] content) {
        try {
            // 使用 openFileOutput() 方法创建一个文件输出流，MODE_PRIVATE 表示私有模式
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);

            // 将内容转换为字节数组并写入文件
            fos.write(content);

            // 关闭文件输出流
            fos.close();

           // System.out.println("Successfully wrote to the file: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveText(String path, byte [] content) {
        // 根据指定的文件路径构建文件输出流对象
        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(content); // 把字符串写入文件输出流
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void saveText(String path, byte [] content , boolean append) {
        // 根据指定的文件路径构建文件输出流对象
        try (FileOutputStream fos = new FileOutputStream(path,append)) {
            fos.write(content); // 把字符串写入文件输出流
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void saveText(String path, String content) {
        // 根据指定的文件路径构建文件输出流对象
        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8)); // 把字符串写入文件输出流
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /** 向文件追加内容
     * @param path 路径
     * @param content 内容
     * @param append 是否追加
     */
    public static void saveText(String path, String content ,boolean append) {
        // 根据指定的文件路径构建文件输出流对象
        try (FileOutputStream fos = new FileOutputStream(path,append)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8)); // 把字符串写入文件输出流
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    public static boolean isFileExists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    public static void createFileIfNotExists(String filePath) {
        File file = new File(filePath);

        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    System.out.println("File created successfully: " + file.getAbsolutePath());
                } else {
                    System.out.println("Failed to create file.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("File already exists: " + file.getAbsolutePath());
        }
    }

    public static ArrayList<String> ReadLine (String path){
        String filePath = path; // 替换为实际文件路径
        ArrayList<String > resultList = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));

            String line;

            while ((line = reader.readLine()) != null) {
                resultList.add(line);
                System.out.println(line); // 输出文件内容到控制台
            }
            reader.close();
            return resultList;
        } catch (IOException e) {
            e.printStackTrace();
            return resultList;
        }
    }
    public static InputStream openInputStream(Context context, Uri uri) throws IOException {
        ContentResolver contentResolver = context.getContentResolver();
        return contentResolver.openInputStream(uri);}

    public static void readByte (InputStream inputStream1){

        ByteBuffer buf=ByteBuffer.allocateDirect(4);
        buf=buf.order(ByteOrder.LITTLE_ENDIAN);

        try {
            BufferedInputStream inputStream = new BufferedInputStream(inputStream1);

            byte[] bufferHead = new byte[6]; // 缓冲区大小
            byte [] bufferTime = new byte[8];
            byte [] bufferFileName = new byte[32];
            inputStream.read(bufferHead,0,6);
            inputStream.read(bufferFileName,0,32);
            inputStream.read(bufferTime,0,8);
            byte [] tmp = new byte[2];
            tmp[1] = bufferHead[38];
            tmp[0] = bufferHead[39];
           int sampleCount = ConvertCode.getint16(tmp[0],tmp[1]);
            byte [] bufferData = new byte[sampleCount*4];
            inputStream.read(bufferData,0,sampleCount*4);
            int bytesRead;
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 如果文件夹不存在则创建文件夹
     * @param directoryPath  创建文件夹路径。
     *
     */
    public static void creatFile (String directoryPath){
       // String directoryPath = "path/to/your/directory"; // 替换为实际文件夹路径

        File directory = new File(directoryPath);

        if (!directory.exists()) {
            boolean created = directory.mkdirs();

            if (created) {
                System.out.println("Directory created successfully.");
            } else {
                System.out.println("Failed to create directory.");
            }
        } else {
            System.out.println("Directory already exists.");
        }
    }
}

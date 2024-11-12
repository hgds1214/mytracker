package com.zeus.tec.ui.maoganDataUpload;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.zeus.tec.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

public class MaoganLoaclDataListAdapater extends BaseAdapter {

    Context context;

    File [] fileList;

    IMaoganDataUpdata iMaoganDataUpdata ;
    //Map IsCheck ;

    public MaoganLoaclDataListAdapater(Context context, List<File> fileList1, IMaoganDataUpdata MaoganDataUpdata) {
        this.context = context;
        File [] files = new File[fileList1.size()];
        for (int i = 0; i < fileList1.size(); i++) {
            files[i] = fileList1.get(i);
        }
        if (files.length> 0) {
            // 按照修改时间排序（最新修改的文件排在前面）
            Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
            // 输出排序后的文件列表
        }
        fileList = files;
        iMaoganDataUpdata = MaoganDataUpdata;
    }

    public MaoganLoaclDataListAdapater(Context context, List<File> fileList1, IMaoganDataUpdata MaoganDataUpdata, Map<Integer,Boolean> isCheck) {
//        this.context = context;
//        IsCheck = isCheck;
//        File [] files = new File[fileList1.size()];
//        for (int i = 0; i < fileList1.size(); i++) {
//            files[i] = fileList1.get(i);
//        }
//
//        if (files.length> 0) {
//            // 按照修改时间排序（最新修改的文件排在前面）
//            Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
//            // 输出排序后的文件列表
//        }
//        fileList = files;
//        iMaoganDataUpdata = MaoganDataUpdata;
    }

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    @Override
    public int getCount() {
        return fileList.length;
    }

    @Override
    public Object getItem(int position) {
        return fileList[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_maogan_datalist, null);
        try{
            TextView tv_name = view.findViewById(R.id.tv_name);
            TextView tv_time = view.findViewById(R.id.tv_time);
            TextView tv_delete = view.findViewById(R.id.tv_delete);
            TextView tv_upData = view.findViewById(R.id.tv_upData);
            CheckBox isCheck = view.findViewById(R.id.ischeck_cb);
            tv_name.setText(fileList[position].getName());
            // 转换为日期格式
            Date date = new Date(fileList[position].lastModified());
            // 定义日期格式
            // isCheck.setChecked((Boolean) IsCheck.get(position));
            // 将日期格式化为字符串
            String formattedDate = sdf.format(date);
            tv_time.setText(formattedDate);
            tv_delete.setOnClickListener(v -> {
                iMaoganDataUpdata.deleteData(fileList[position]);
            });
            tv_upData.setOnClickListener(v -> iMaoganDataUpdata.updataData(fileList[position]));
            isCheck.setOnClickListener(v -> iMaoganDataUpdata.clickCheckBox(position,isCheck.isChecked()));

        }catch (Exception ex){
            ToastUtils.showLong(ex.getMessage());
        }

        return view;
    }

}

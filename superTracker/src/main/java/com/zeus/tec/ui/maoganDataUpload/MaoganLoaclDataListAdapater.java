package com.zeus.tec.ui.maoganDataUpload;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.zeus.tec.R;
import com.zeus.tec.model.utils.FeedbackUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

public class MaoganLoaclDataListAdapater extends BaseAdapter {

    Context context;

    MaoganMainActivity.fileItemInfo[] fileList;

    IMaoganDataUpdata iMaoganDataUpdata;
    //Map IsCheck ;

    public MaoganLoaclDataListAdapater(Context context, List<MaoganMainActivity.fileItemInfo> fileList1, IMaoganDataUpdata MaoganDataUpdata) {
        this.context = context;
        fileList = new MaoganMainActivity.fileItemInfo[fileList1.size()];
        for (int i = 0; i < fileList1.size(); i++) {
            fileList[i] = fileList1.get(i);
        }
        if (fileList.length > 0) {
            // 按照修改时间排序（最新修改的文件排在前面）
            Arrays.sort(fileList, (f1, f2) -> Long.compare(f2.file.lastModified(), f1.file.lastModified()));
            // 输出排序后的文件列表
        }
        iMaoganDataUpdata = MaoganDataUpdata;
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
        View view = LayoutInflater.from(context).inflate(R.layout.maogan_file_item, null);
        try {
            TextView tv_name = view.findViewById(R.id.tv_name);
            CheckBox ischeck_cb = view.findViewById(R.id.ischeck_cb);
            ImageView file_img = view.findViewById(R.id.file_img);
            ImageView look_over_img = view.findViewById(R.id.look_over_img);
            LinearLayout file_item_ly = view.findViewById(R.id.file_item_ly);
            if (fileList[position].file.isDirectory()) {
                //  ischeck_cb.setVisibility(View.GONE);
                file_img.setImageResource(R.mipmap.folder);
                ischeck_cb.setOnCheckedChangeListener((buttonView, isChecked) -> fileList[position].checkStatus = isChecked);
                file_item_ly.setOnClickListener(v -> {
                    FeedbackUtil.getInstance().doFeedback();
                    iMaoganDataUpdata.refreshList(fileList[position].file);
                });
            } else {
                ischeck_cb.setOnClickListener(null);
                ischeck_cb.setChecked(fileList[position].checkStatus);
                ischeck_cb.setOnCheckedChangeListener((buttonView, isChecked) -> fileList[position].checkStatus = isChecked);
                look_over_img.setVisibility(View.GONE);
            }
            tv_name.setText(fileList[position].file.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    public void allCheck() {
        for (int i = 0; i < fileList.length; i++) {
            if (!fileList[i].file.isDirectory()) {
                fileList[i].checkStatus = true;
            }
        }
        this.notifyDataSetChanged();
    }

    public void upDataFile() {
        List<File> updataFileList = new ArrayList<>();
        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].checkStatus) {
                updataFileList.add(fileList[i].file);
            }
        }
        iMaoganDataUpdata.updataData(updataFileList);
    }

    public void shareData() {
        List<File> shareFileList = new ArrayList<>();
        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].checkStatus) {
                shareFileList.add(fileList[i].file);
            }
        }
        iMaoganDataUpdata.shareData(shareFileList);
    }

    public void deleteData() {
        List<File> deleteFileList = new ArrayList<>();
        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].checkStatus) {
                deleteFileList.add(fileList[i].file);
            }
        }
        iMaoganDataUpdata.deleteData(deleteFileList);
    }


}

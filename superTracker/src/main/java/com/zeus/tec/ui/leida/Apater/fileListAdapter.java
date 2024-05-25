package com.zeus.tec.ui.leida.Apater;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zeus.tec.R;
import com.zeus.tec.model.leida.main.FileBean;
import com.zeus.tec.model.leida.main.MainCache;
import com.zeus.tec.model.leida.main.PointParamter;

import java.util.List;

public class fileListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

    public fileListAdapter(Context context, List<FileBean> FileBeanList) {
        this.context = context;
        this.fileBeanList = FileBeanList;
    }
    Context context ;
    List<FileBean> fileBeanList;
    MainCache cache = MainCache.GetInstance();
    @Override
    public int getCount() {
        return fileBeanList.size();
    }

    @Override
    public Object getItem(int position) {
        return fileBeanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.file_list_item,null);
        TextView file_Time = view.findViewById(R.id.tv_file_time);
        TextView file_Name = view.findViewById(R.id.tv_filename);
         TextView file_size = view.findViewById(R.id.tv_file_size);
        ImageView imageView = view.findViewById(R.id.iv_file_image);
        if (position ==0){
          //  imageView.setImageResource(R.mipmap.logo);
            file_Name.setText("文件名称");
            file_Time.setText("时间");
            file_size.setText(String.valueOf("文件大小"));
            view.setBackgroundResource(R.drawable.bg_title_filelist);
          //  imageView.setBackgroundResource(R.drawable.file_list);
            imageView.setImageResource(R.mipmap.file_list);

        }
        else {

            FileBean fileBean = fileBeanList.get(position-1);
            file_Name.setText(fileBean.FileName);
            file_Time.setText(fileBean.Time);
            file_size.setText(transSize(fileBean.Size));

        }

        return view;
    }

    @SuppressLint("DefaultLocale")
    private String transSize(int size){
        if (size<1024){
           return String.valueOf(size)+"B";
        }
        else {
            if (size<1024000){
             return String.format("%.2f",size/1024.0)+"KB";
            }
            else {
                return String.format("%.2f",size/1024000.0)+"MB";
            }
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        cache.selectFileName = fileBeanList.get(position-1).FileName;
        view.setBackgroundResource(R.drawable.list_item_background_selector);
    }
}

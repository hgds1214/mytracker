package com.zeus.tec.ui.directionfinder.Apater;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zeus.tec.R;
import com.zeus.tec.model.directionfinder.directionfinderPointRecordInfo;
import com.zeus.tec.model.leida.main.PointParamter;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class directionfinderPointRecordApater extends BaseAdapter {

   // public directionfinderPointRecordApater(Context context, List<directionfinderPointRecordInfo> directionfinderPointRecordInfo) {
   public directionfinderPointRecordApater(Context context) {
        this.context = context;
        //this.DirectionfinderPointRecordInfo = directionfinderPointRecordInfo;
    }
    private int recordId=0;
    Context context;
    public List <directionfinderPointRecordInfo> DirectionfinderPointRecordInfo = new ArrayList<>();

    public void  addPointRecord (directionfinderPointRecordInfo recordinfo){
        if(DirectionfinderPointRecordInfo == null){
            return;
        }
        if(!DirectionfinderPointRecordInfo.contains(recordinfo)){
            recordinfo.recordId = DirectionfinderPointRecordInfo.size()+1;
            DirectionfinderPointRecordInfo.add(recordinfo);
        }
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return DirectionfinderPointRecordInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return DirectionfinderPointRecordInfo.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view  = LayoutInflater.from(context).inflate(R.layout.point_record_list_item,null);
        TextView recordNumber = view.findViewById(R.id.tv_pointNumber);
        TextView pointTime = view.findViewById(R.id.tv_point_time);
        TextView oritentionAngle = view.findViewById(R.id.tv_oritention_angle);
        TextView dipAngle = view.findViewById(R.id.tv_dip_angle);
        TextView relativeAngle = view.findViewById(R.id.tv_relative_angle);
        if (DirectionfinderPointRecordInfo.size()>0){

            directionfinderPointRecordInfo dirrecordinfo =  DirectionfinderPointRecordInfo.get(position);
            recordNumber.setText(String.valueOf( dirrecordinfo.recordId));
            pointTime.setText(dirrecordinfo.recordTime);
            oritentionAngle.setText(String.valueOf(dirrecordinfo.oritentionAngle) );
            dipAngle.setText(String.valueOf(dirrecordinfo.dipAngle));
            relativeAngle.setText(String.valueOf(dirrecordinfo.relativeAngle));
        }
        return view;
    }
}

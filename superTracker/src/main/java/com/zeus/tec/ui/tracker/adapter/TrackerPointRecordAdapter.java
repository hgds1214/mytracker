package com.zeus.tec.ui.tracker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zeus.tec.R;
import com.zeus.tec.model.directionfinder.directionfinderPointRecordInfo;
import com.zeus.tec.model.tracker.PointRecordInfo;

import java.util.List;

public class TrackerPointRecordAdapter extends BaseAdapter {

    public TrackerPointRecordAdapter (Context context, List <PointRecordInfo> pointRecordInfo){
        this.pointRecordInfoList = pointRecordInfo;
        this.context = context;
    }

    Context context;
    List <PointRecordInfo> pointRecordInfoList ;

    @Override
    public int getCount() {
        return pointRecordInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return pointRecordInfoList.get(position);
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
        if (pointRecordInfoList.size()>0){
            PointRecordInfo  pointRecordInfo =  pointRecordInfoList.get(position);
            recordNumber.setText(String.valueOf(position+1));
            pointTime.setText(String.valueOf (pointRecordInfo.collectTime));
            oritentionAngle.setText(String.valueOf(pointRecordInfo.directionAngle) );
            dipAngle.setText(String.valueOf(pointRecordInfo.slantAngle));
            relativeAngle.setText(String.valueOf(pointRecordInfo.rollAngle));
        }
        return view;
    }
}

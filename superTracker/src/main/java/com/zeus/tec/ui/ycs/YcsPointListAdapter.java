package com.zeus.tec.ui.ycs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zeus.tec.R;
import com.zeus.tec.model.ycs.YcsPoint;

import java.util.List;

public class YcsPointListAdapter extends BaseAdapter {

    public YcsPointListAdapter (Context context , List<YcsPoint> pointParamters){
        this.context =context;
        this.pointParamters = pointParamters;
    }

    private Context context ;
    private List<YcsPoint> pointParamters ;

    @Override
    public int getCount() {
        return  pointParamters.size();
    }

    @Override
    public Object getItem(int position) {
        return pointParamters.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.point_list_item,null);
        TextView number = view.findViewById(R.id.tv_pointNumber);
        TextView Time = view.findViewById(R.id.tv_point_time);
        TextView distance = view.findViewById(R.id.tv_point_distance);
        YcsPoint point = pointParamters.get(position);
        number.setText(String.valueOf(point.number));
        Time.setText(point.time);
        distance.setText(String.valueOf(point.distance/100));
        return view;
    }
}

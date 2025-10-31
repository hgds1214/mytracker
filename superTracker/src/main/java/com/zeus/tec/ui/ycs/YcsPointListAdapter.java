package com.zeus.tec.ui.ycs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zeus.tec.R;
import com.zeus.tec.model.ycs.YcsPoint;
import com.zeus.tec.util.IOnClickCallBack;

import java.util.List;

public class YcsPointListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

    public YcsPointListAdapter (Context context , List<YcsPoint> pointParamters,IOnClickCallBack iOnClickCallBack){
        this.context =context;
        this.pointParamters = pointParamters;
        this.iOnClickCallBack = iOnClickCallBack;
    }
    IOnClickCallBack iOnClickCallBack ;
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
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) LinearLayout ly = view.findViewById(R.id.pointinfolist_ly);
        YcsPoint point = pointParamters.get(position);
        number.setText(String.valueOf(point.number));
        Time.setText(point.time);
        distance.setText(String.valueOf(point.distance/100));
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}

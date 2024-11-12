package com.zeus.tec.ui.leida.Apater;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zeus.tec.R;
import com.zeus.tec.model.leida.leidaPointRecordInfo;
import com.zeus.tec.model.leida.main.PointParamter;

import java.util.List;

public class PointListAdapter extends BaseAdapter {

    public PointListAdapter(Context context, List<leidaPointRecordInfo> pointParamters) {
        this.context = context;
        this.pointParamters = pointParamters;
    }

    Context context ;
    List<leidaPointRecordInfo> pointParamters;

    public  void getInstance(){
        for (int j=1;j<10;j++)  {
           // pointParamters.add(new PointParamter(String.valueOf(j),"2023年8月"+String.valueOf(j)+"日"+" 11:57:21",String.valueOf(j*100)));
        }

    }

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

            leidaPointRecordInfo pointParamter = pointParamters.get(position);
            number.setText(pointParamter.PointNumber);
            Time.setText(pointParamter.recordTime);
            distance.setText(pointParamter.distance);
      //  }
        return view;
    }
}

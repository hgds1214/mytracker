package com.zeus.tec.ui.ycs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.zeus.tec.R;
import com.zeus.tec.model.utils.FeedbackUtil;
import com.zeus.tec.model.ycs.YcsPoint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class YcsPointInfoAdapter extends BaseAdapter {

    public YcsPointInfoAdapter (Context context, List<YcsDataViewActivity.DataBean> pointParamters, YcsDataViewActivity.IPointList iPointList){
        this.context =context;
        this.pointParamters = pointParamters;
        this.iPointList = iPointList;
    }
    Context context;
    List<YcsDataViewActivity.DataBean> pointParamters;
    YcsDataViewActivity.IPointList iPointList;
    private int proPosition =1;
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

    Date date ;

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
   // String format =

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.ycs_point_item,null);
        TextView serial_tv =  view.findViewById(R.id.serial_tv);
        TextView begin_time_tv = view.findViewById(R.id.begin_time_tv);
        LinearLayout point_info_ly = view.findViewById(R.id.point_info_ly);
        serial_tv.setText(String.valueOf(position+1));

        begin_time_tv.setText( dateFormat.format(new Date(pointParamters.get(position).Time*1000)));
        point_info_ly.setOnClickListener(v -> {
            FeedbackUtil.getInstance().doFeedback();
            iPointList.clickOn(position);
        });
        return  view;
    }

}

package com.zeus.tec.ui.leida.Apater;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.zeus.tec.R;
import com.zeus.tec.model.leida.DrillPipe;
import com.zeus.tec.model.leida.MergeCache;
import com.zeus.tec.model.leida.ProbePoint;
import com.zeus.tec.model.leida.main.MainCache;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.O)
public class dataMsgAdapater extends BaseAdapter implements AdapterView.OnItemClickListener {
    public dataMsgAdapater(Context context, List<ProbePoint> ProbePointList) {
        this.context = context;
        this.probePointList = ProbePointList;
    }

    Context context;
    List<ProbePoint> probePointList;

    @Override
    public int getCount() {
        return probePointList.size();
    }

    @Override
    public Object getItem(int position) {
        return probePointList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.leida_datamsg_item, null);
        TextView serial_tv = view.findViewById(R.id.serial_tv);
        TextView begin_time_tv = view.findViewById(R.id.begin_time_tv);
        TextView directionAngle_tv = view.findViewById(R.id.directionAngle_tv);
        TextView pitchAngle_tv = view.findViewById(R.id.pitchAngle_tv);
        TextView rollAngle_tv = view.findViewById(R.id.rollAngle_tv);
        TextView distance = view.findViewById(R.id.point_distance_tv);

        ProbePoint probePoint = probePointList.get(position);
        serial_tv.setText(String.valueOf(position + 1));
        begin_time_tv.setText(probePoint.SampleTime.format(dateTimeFormatter));
        directionAngle_tv.setText(String.valueOf(probePoint.Heading));
        pitchAngle_tv.setText(String.valueOf(probePoint.Pitch));
        rollAngle_tv.setText(String.valueOf(probePoint.Roll));
        if (probePoint.Distance == -1) {
            distance.setText(" ");
        } else {
            distance.setText(String.valueOf(probePoint.Distance));
        }

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}

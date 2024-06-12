package com.zeus.tec.ui.leida.Apater;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.zeus.tec.R;
import com.zeus.tec.model.leida.DrillPipe;
import com.zeus.tec.model.leida.main.FileBean;
import com.zeus.tec.model.leida.main.MainCache;
import com.zeus.tec.model.utils.FeedbackUtil;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.O)
public class drillMsgAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
    public drillMsgAdapter(Context context, List<DrillPipe> DrillPipeList) {
        this.context = context;
        this.drillPipeList = DrillPipeList;
    }

    Context context;
    List<DrillPipe> drillPipeList;
    MainCache cache = MainCache.GetInstance();

    @Override
    public int getCount() {
        return drillPipeList.size();
    }

    @Override
    public Object getItem(int position) {
        return drillPipeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.leida_drillmsg_item, null);
        TextView serial_tv = view.findViewById(R.id.serial_tv);
        TextView begin_time_tv = view.findViewById(R.id.begin_time_tv);
        TextView count_tv = view.findViewById(R.id.count_tv);
        TextView collection_serial_tv = view.findViewById(R.id.collection_serial_tv);
        DrillPipe drillPipe = drillPipeList.get(position);
        serial_tv.setText(String.valueOf(position + 1));
        begin_time_tv.setText(drillPipe.StartTime.format(dateTimeFormatter));
        count_tv.setText(String.valueOf(drillPipe.ValidCount));
        collection_serial_tv.setText(drillPipe.IndexFrom + "→" + drillPipe.IndexTo);
        if (drillPipe.ValidCount == 0) {
            serial_tv.setTextColor(Color.RED);
            begin_time_tv.setTextColor(Color.RED);
            count_tv.setTextColor(Color.RED);
            collection_serial_tv.setTextColor(Color.RED);
        }
        return view;
    }

    @SuppressLint("DefaultLocale")
    private String transSize(int size) {
        if (size < 1024) {
            return String.valueOf(size) + "B";
        } else {
            if (size < 1024000) {
                return String.format("%.2f", size / 1024.0) + "KB";
            } else {
                return String.format("%.2f", size / 1024000.0) + "MB";
            }
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}

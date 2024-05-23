package com.zeus.tec.ui.leida;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.zeus.tec.databinding.ActivityLeidaDetailBinding;
import com.zeus.tec.db.TrackerDBManager;
import com.zeus.tec.model.leida.leidaPointRecordInfo;
import com.zeus.tec.model.leida.leida_info;
import com.zeus.tec.ui.leida.Apater.PointListAdapter;
import com.zeus.tec.model.utils.FeedbackUtil;

import java.text.SimpleDateFormat;
import java.util.List;

public class leidaDetailActivity extends AppCompatActivity {

    ActivityLeidaDetailBinding binding;
    ListView pointList;

    private static final String KEY_DATA = "DATA";
    private leida_info data;
    public  static Intent buildIntent (Context context, leida_info item){
        Intent intent = new Intent(context, leidaDetailActivity.class);
        intent.putExtra(KEY_DATA, item);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       binding = ActivityLeidaDetailBinding.inflate(getLayoutInflater());
       setContentView(binding.getRoot());

       data =(leida_info) getIntent().getSerializableExtra(KEY_DATA);

        initView();
        binding.ivBack.setOnClickListener(v->{
            FeedbackUtil.getInstance().doFeedback();
            finish();
        });


    }

    private  void  initView (){
        try {
            binding.tvProjectName.setText(String.valueOf(data.projectId));
            binding.tvSampleNumber.setText(String.valueOf(data.sampleLength));
            binding.tvFrenquence.setText(String.valueOf(data.frequency));
            binding.tvMagnification.setText(String.valueOf(data.Amp1));
            binding.tvDeley.setText(String.valueOf(data.Delay1));
            binding.tvOverlaycount.setText(String.valueOf(data.overlaynumbe));
            binding.tvPointDistance.setText(String.valueOf(data.drillPipeLength));
            binding.tvTimespace.setText(String.valueOf(data.timeSpace));
            binding.tvGyro.setText(String.valueOf(data.GYROThreshold));


            //  binding.adjustMode.setText(String.valueOf(data.adjustMode));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            initPointList(TrackerDBManager.getrecordByleidaInfoId(data.id));




        }
        catch (Exception ex){

        }
    }

    private void initPointList(List<leidaPointRecordInfo> pointParamters) {
        // List<PointParamter> pointParamters = new LinkedList<>();
        PointListAdapter adapter = new PointListAdapter(leidaDetailActivity.this, pointParamters);
        pointList = binding.listPoint;
        pointList.setAdapter(adapter);
        pointList.setSelection(adapter.getCount() - 1);
    }
}
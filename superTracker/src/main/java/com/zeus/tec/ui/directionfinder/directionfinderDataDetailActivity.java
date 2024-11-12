package com.zeus.tec.ui.directionfinder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;

import com.zeus.tec.model.directionfinder.dirctionfinderDrillHoleInfo;
import com.zeus.tec.ui.FullScreenActivity;
import com.zeus.tec.model.utils.FeedbackUtil;
import com.zeus.tec.databinding.ActivityDirectionfinderDataDetailBinding;
import java.io.File;
import java.text.SimpleDateFormat;

public class directionfinderDataDetailActivity extends AppCompatActivity {

    private ActivityDirectionfinderDataDetailBinding binding;
    private static final String KEY_DATA = "DATA";
     private dirctionfinderDrillHoleInfo data;
    public static Intent buildIntent(Context context, dirctionfinderDrillHoleInfo item) {
        Intent intent = new Intent(context, directionfinderDataDetailActivity.class);
        intent.putExtra(KEY_DATA, item);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_directionfinder_data_detail);
        binding = ActivityDirectionfinderDataDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        data = (dirctionfinderDrillHoleInfo) getIntent().getSerializableExtra(KEY_DATA);
        initView();
        binding.ivBack.setOnClickListener(v->{
            FeedbackUtil.getInstance().doFeedback();
            finish();
        });
    }

    private  void  initView (){
        try {
            binding.tvCompanyId.setText(String.valueOf(data.projectName));
            binding.miningAreaId.setText(String.valueOf(data.miningAreaId));
            binding.workspaceName.setText(String.valueOf(data.workspaceName));
            binding.drillHoleId.setText(String.valueOf(data.drillHoleId));
            binding.detector.setText(String.valueOf(data.detector));
            binding.dynamicThreshold.setText(String.valueOf(data.compassCalibrationError));
            binding.drillPipeLength.setText(String.valueOf(data.presetHeading));
            binding.drillHoleLength.setText(String.valueOf(data.laserLevelError));

            binding.holeX.setText(String.valueOf(data.holeX));
            binding.holeY.setText(String.valueOf(data.holeY));
            binding.holeZ.setText(String.valueOf(data.holeZ));
            binding.jacketLength.setText(String.valueOf(data.jacketLength));
            binding.designDirection.setText(String.valueOf(data.designDirection));
            binding.designAngle.setText(String.valueOf(data.designAngle));
          //  binding.adjustMode.setText(String.valueOf(data.adjustMode));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            binding.collectTime.setText(sdf.format(data.collectionDateTime));

            Glide.with(this).load(data.livePhotos).into(binding.photo);

            binding.photo.setOnClickListener( v->{
                FeedbackUtil.getInstance().doFeedback();
                startActivity(FullScreenActivity.newIntent(this, Uri.fromFile(new File(data.livePhotos)).toString()));
            });

        }
        catch (Exception ex){

        }

    }
}
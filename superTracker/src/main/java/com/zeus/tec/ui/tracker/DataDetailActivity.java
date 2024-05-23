package com.zeus.tec.ui.tracker;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.zeus.tec.databinding.ActivityDataDetailBinding;
import com.zeus.tec.model.tracker.DrillHoleInfo;
import com.zeus.tec.ui.FullScreenActivity;
import com.zeus.tec.ui.base.BaseActivity;
import com.zeus.tec.model.utils.FeedbackUtil;

import java.io.File;
import java.text.SimpleDateFormat;

public class DataDetailActivity extends BaseActivity {
    private static final String KEY_DATA = "DATA";

    private ActivityDataDetailBinding binding;
    private DrillHoleInfo data;

    public static Intent buildIntent(Context context, DrillHoleInfo item) {
        Intent intent = new Intent(context, DataDetailActivity.class);
        intent.putExtra(KEY_DATA, item);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDataDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        data = (DrillHoleInfo) getIntent().getSerializableExtra(KEY_DATA);
        initUI();
        binding.ivBack.setOnClickListener(v->{
            FeedbackUtil.getInstance().doFeedback();
            finish();
        });
    }

    private void initUI() {
        binding.tvCompanyId.setText(String.valueOf(data.companyId));
        binding.miningAreaId.setText(String.valueOf(data.miningAreaId));
        binding.workspaceName.setText(String.valueOf(data.workspaceName));
        binding.drillHoleId.setText(String.valueOf(data.drillHoleId));
        binding.detector.setText(String.valueOf(data.detector));
        binding.dynamicThreshold.setText(String.valueOf(data.dynamicThreshold));
        binding.drillPipeLength.setText(String.valueOf(data.drillPipeLength));
        binding.drillHoleLength.setText(String.valueOf(data.drillHoleLength));

        binding.holeX.setText(String.valueOf(data.holeX));
        binding.holeY.setText(String.valueOf(data.holeY));
        binding.holeZ.setText(String.valueOf(data.holeZ));
        binding.jacketLength.setText(String.valueOf(data.jacketLength));
        binding.designDirection.setText(String.valueOf(data.designDirection));
        binding.designAngle.setText(String.valueOf(data.designAngle));
        binding.adjustMode.setText(String.valueOf(data.adjustMode));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        binding.collectTime.setText(sdf.format(data.collectionDateTime));

        Glide.with(this).load(data.livePhotos).into(binding.photo);

        binding.photo.setOnClickListener( v->{
            FeedbackUtil.getInstance().doFeedback();
            startActivity(FullScreenActivity.newIntent(this, Uri.fromFile(new File(data.livePhotos)).toString()));
        });
    }
}
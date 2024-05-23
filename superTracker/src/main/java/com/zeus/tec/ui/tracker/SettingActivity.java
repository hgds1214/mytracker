package com.zeus.tec.ui.tracker;

import android.os.Bundle;

import com.blankj.utilcode.util.ToastUtils;
import com.zeus.tec.databinding.ActivitySettingBinding;
import com.zeus.tec.ui.base.BaseActivity;
import com.zeus.tec.ui.tracker.config.SystemConfig;
import com.zeus.tec.model.utils.FeedbackUtil;

public class SettingActivity extends BaseActivity {
    private ActivitySettingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.ivBack.setOnClickListener(v -> {
            FeedbackUtil.getInstance().doFeedback();
            finish();
        });

        binding.tvCancel.setOnClickListener( v->{
            FeedbackUtil.getInstance().doFeedback();
            finish();
        });
        binding.tvOk.setOnClickListener( v->{
            FeedbackUtil.getInstance().doFeedback();
            saveInfo();
        });

        binding.edtVol.setText(String.valueOf(SystemConfig.getVolThreshHold()));

    }

    private void saveInfo() {
        float vol = 0;
        try {
            vol = Float.parseFloat(binding.edtVol.getText().toString());
            if ( vol <= 0) {
                ToastUtils.showLong("请输入有效的数值");
                return;
            }
        }catch (Exception e) {
            ToastUtils.showLong("请输入有效的数值");
            return;
        }
        SystemConfig.setsVolThreashHold(vol);
        finish();
    }
}
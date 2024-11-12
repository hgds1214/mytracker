package com.zeus.tec.ui.tracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

import com.blankj.utilcode.util.ToastUtils;
import com.zeus.tec.databinding.ActivityProjectInfoEditBinding;
import com.zeus.tec.device.tracker.TrackerCollectData;
import com.zeus.tec.model.tracker.DrillHoleInfo;
import com.zeus.tec.ui.base.BaseActivity;
import com.zeus.tec.ui.tracker.util.ProjectInfoManager;
import com.zeus.tec.ui.tracker.util.TextHelper;
import com.zeus.tec.model.utils.FeedbackUtil;

public class ProjectInfoEditActivity extends BaseActivity {
    private ActivityProjectInfoEditBinding binding;
    private final static String [] sampleLength = {"518","1024","2048"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProjectInfoEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.ivBack.setOnClickListener( v->{
            FeedbackUtil.getInstance().doFeedback();
            finish();
        });
        binding.tvNext.setOnClickListener( v-> {
            FeedbackUtil.getInstance().doFeedback();
            clickNext();
        });

        initUI();
    }

    private DrillHoleInfo info = ProjectInfoManager.getInstance().getOrNewDrillHoleInfo(true);

    private void initUI() {
        binding.edtCompany.setText(TextHelper.safeString(info.companyId));
        binding.edtMiningArea.setText(TextHelper.safeString(info.miningAreaId));
        binding.edtWorkspace.setText(TextHelper.safeString(info.workspaceName));
        binding.edtDrillHoleId.setText(TextHelper.safeString(info.drillHoleId));
        binding.edtDetector.setText(TextHelper.safeString(info.detector));
        binding.edtDyThreshold.setText(TextHelper.safeString(""+info.dynamicThreshold));
        binding.edtPipeLength.setText(TextHelper.safeString(""+info.drillPipeLength));
        binding.edtHoleLength.setText(TextHelper.safeString(""+info.drillHoleLength));
        SharedPreferences sharedPreferences = getSharedPreferences("user",MODE_PRIVATE);
        float magnetic_value = sharedPreferences.getFloat("magnetic_value",0f);
        binding.edtMagneticDeclination.setText(String.valueOf(magnetic_value));
    }

    private void clickNext() {
        String company = binding.edtCompany.getText().toString();
        if (TextHelper.isInvalidTextAndShowWarn(company, "企业编号")) {
            return;
        }
        String miningArea = binding.edtMiningArea.getText().toString();
        if (TextHelper.isInvalidTextAndShowWarn(miningArea, "矿区编号")) {
            return;
        }
        String worksapce = binding.edtWorkspace.getText().toString();
        if (TextHelper.isInvalidTextAndShowWarn(worksapce, "工作面名称")) {
            return;
        }
        String drillHoleId = binding.edtDrillHoleId.getText().toString();
        if (TextHelper.isInvalidTextAndShowWarn(drillHoleId, "钻井编号")) {
            return;
        }
        String detector = binding.edtDetector.getText().toString();
        if (TextHelper.isInvalidTextAndShowWarn(drillHoleId, "检测人员")) {
            return;
        }
        String dythresholdStr = binding.edtDyThreshold.getText().toString();
        if (TextUtils.isEmpty(dythresholdStr)) {
            ToastUtils.showLong("动态阈值不能为空");
            return;
        }
        int dythreash = Integer.parseInt(dythresholdStr);
        if (dythreash < 0 || dythreash > 0xffff) {
            ToastUtils.showLong("动态阈值只能是0到"+0xffff+"之间");
            return;
        }
        String pipeLengthStr = binding.edtPipeLength.getText().toString();
        if (TextUtils.isEmpty(pipeLengthStr)) {
            ToastUtils.showLong("钻杆长度(CM)不能为空");
            return;
        }
        int drillPipeLength = Integer.parseInt(pipeLengthStr);
        if (drillPipeLength < 1 || drillPipeLength > 0xffff) {
            ToastUtils.showLong("钻杠长度只能是1到"+0xffff+"之间");
            return;
        }

        String holeLengthStr = binding.edtHoleLength.getText().toString();
        if (TextUtils.isEmpty(holeLengthStr)) {
            ToastUtils.showLong("钻孔深度(M)");
            return;
        }
        long holeLength = Long.parseLong(holeLengthStr);
        if (drillPipeLength < 1 || drillPipeLength > 0xffffffffL) {
            ToastUtils.showLong("钻孔深度只能是1到"+0xffffffffL+"之间");
            return;
        }
        int magnetic = (int)Float.parseFloat(binding.edtMagneticDeclination.getText().toString()) ;

        if (magnetic>360||magnetic<-360){
            ToastUtils.showLong("磁偏角只能在-360到360之间");
            return;
        }
        SharedPreferences sharedPreferences = getSharedPreferences("user",MODE_PRIVATE);
        //获取Editor对象的引用
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //将获取过来的值放入文件
        editor.putFloat("magnetic_value", magnetic);
        editor.commit();
        ProjectInfoManager.getInstance().magnetic_value = magnetic*100;

        info.companyId = company;
        info.miningAreaId = miningArea;
        info.workspaceName = worksapce;

        info.drillHoleId = drillHoleId;
        info.detector = detector;
        info.dynamicThreshold = dythreash;
        info.drillPipeLength = drillPipeLength;
        info.drillHoleLength = holeLength;
        startActivity(new Intent(ProjectInfoEditActivity.this, DrillInfoEditActivity.class));
    }
}
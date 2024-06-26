package com.zeus.tec.ui.directionfinder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.blankj.utilcode.util.ToastUtils;
import com.zeus.tec.databinding.ActivityDirectionProjectInfoBinding;
import com.zeus.tec.model.directionfinder.dirctionfinderDrillHoleInfo;
import com.zeus.tec.ui.tracker.util.ProjectInfoManager;
import com.zeus.tec.ui.tracker.util.TextHelper;
import com.zeus.tec.model.utils.FeedbackUtil;

public class directionProjectInfoActivity extends AppCompatActivity {

   public     com.zeus.tec.databinding.ActivityDirectionProjectInfoBinding binding ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_direction_project_info);
        binding =  ActivityDirectionProjectInfoBinding.inflate((getLayoutInflater()));
        setContentView(binding.getRoot());
        initUI();
        binding.tvNext.setOnClickListener(v->clickNext());
        binding.ivBack.setOnClickListener(v->ivBack_click());
    }
    private dirctionfinderDrillHoleInfo info = ProjectInfoManager.getInstance().getOrNewdirctionfinderDrillHoleInfo(true);

    private void initUI() {
        binding.edtProject.setText(TextHelper.safeString(info.projectName));
        binding.edtMiningArea.setText(TextHelper.safeString(info.miningAreaId));
        binding.edtWorkspace.setText(TextHelper.safeString(info.workspaceName));
        binding.edtDrillHoleId.setText(TextHelper.safeString(info.drillHoleId));
        binding.edtDetector.setText(TextHelper.safeString(info.detector));
        binding.edtDyCompassCabError.setText(TextHelper.safeString(""+info.compassCalibrationError));
        binding.edtPresetHeading.setText(TextHelper.safeString(""+info.presetHeading));
        binding.edtLaserError.setText(TextHelper.safeString(""+info.laserLevelError));

    }

    private  void tvNext_click (){
        Intent intent  = new Intent(directionProjectInfoActivity.this,directionDrillInfoActivity.class);
        startActivity(intent);
    }
    private  void  ivBack_click(){
        FeedbackUtil.getInstance().doFeedback();
        finish();
    }

    private void clickNext() {
        String ProjcetName = binding.edtProject.getText().toString();
        if (TextHelper.isInvalidTextAndShowWarn(ProjcetName, "项目名称")) {
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
        String CompassCabError = binding.edtDyCompassCabError.getText().toString();
        if (TextUtils.isEmpty(CompassCabError)) {
            ToastUtils.showLong("罗盘校准误差不能为空");
            return;
        }
        int compassCabError = Integer.parseInt(CompassCabError);
        if (compassCabError < -90 || compassCabError > 90) {
            ToastUtils.showLong("罗盘校准误差只能是-90到"+90+"之间");
            return;
        }
        String PresetHeading = binding.edtPresetHeading.getText().toString();
        if (TextUtils.isEmpty(PresetHeading)) {
            ToastUtils.showLong("预设航向不能为空");
            return;
        }
        int presetHeading = Integer.parseInt(PresetHeading);
        if (presetHeading < 0 || presetHeading > 360) {
            ToastUtils.showLong("预设航向只能是0到"+360+"之间");
            return;
        }

        String LaserError = binding.edtLaserError.getText().toString();
        if (TextUtils.isEmpty(LaserError)) {
            ToastUtils.showLong("激光水平误差不能为空");
            return;
        }
        long laserError = Long.parseLong(LaserError);
        if (laserError < -90 || laserError > 90) {
            ToastUtils.showLong("激光水平误差只能是-90到"+90+"之间");
            return;
        }

        info.projectName = ProjcetName;
        info.miningAreaId = miningArea;
        info.workspaceName = worksapce;

        info.drillHoleId = drillHoleId;
        info.detector = detector;
        info.compassCalibrationError = compassCabError;
        info.presetHeading = presetHeading;
        info.laserLevelError = laserError;
        startActivity(new Intent(directionProjectInfoActivity.this, directionDrillInfoActivity.class));
    }
}
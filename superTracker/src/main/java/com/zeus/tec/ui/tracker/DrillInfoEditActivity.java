package com.zeus.tec.ui.tracker;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.zeus.tec.R;
import com.zeus.tec.databinding.ActivityDrillInfoBinding;
import com.zeus.tec.db.TrackerDBManager;
import com.zeus.tec.model.tracker.DrillHoleInfo;
import com.zeus.tec.ui.FullScreenActivity;
import com.zeus.tec.ui.base.BaseActivity;
import com.zeus.tec.ui.tracker.util.ProjectInfoManager;
import com.zeus.tec.ui.tracker.util.TextHelper;
import com.zeus.tec.model.utils.FeedbackUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DrillInfoEditActivity extends BaseActivity {

    private ActivityResultLauncher<Intent> resultLauncher;
    private ActivityDrillInfoBinding binding;
    private String uri;
    File picfile = new File(PathUtils.getExternalAppFilesPath()+ File.separator+ "picData" + File.separator + "tmp.png");
    String  mFilePath = Environment.getExternalStorageDirectory().getPath();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDrillInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getPermission();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
        binding.ivBack.setOnClickListener( v->{
            FeedbackUtil.getInstance().doFeedback();
            finish();
        });
        binding.tvCancel.setOnClickListener( v-> {
            FeedbackUtil.getInstance().doFeedback();
            clickCancel();
        });
        binding.tvCapture.setOnClickListener( v-> {
            FeedbackUtil.getInstance().doFeedback();

            try {

               resultLauncher.launch(takePhoto(picfile.getAbsolutePath()));
               // resultLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(picfile)));
            }
            catch (Exception ex){
                Log.e("TGA",ex.getLocalizedMessage());

            }
        });
        binding.ivSpot.setOnClickListener( v -> {
            FeedbackUtil.getInstance().doFeedback();
            startActivity(FullScreenActivity.newIntent(this, uri));
        });
        binding.tvOk.setOnClickListener(v -> {
            FeedbackUtil.getInstance().doFeedback();
            clickOK();
        });

        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != RESULT_OK) return;

            onGetPicture(loadBmpChangeSize(picfile,4));
           // onGetPicture((Bitmap)result.getData().getExtras().get("data"));
        });
        initUI();
    }

    /**
     *
     * @param file 图片储存文件
     * @param size 压缩倍数=size*size
     */
    private Bitmap  loadBmpChangeSize (File file ,int size){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(picfile.getAbsolutePath(), options);
        options.inJustDecodeBounds = false;
        options.inSampleSize= 4;
        return BitmapFactory.decodeFile(picfile.getAbsolutePath(), options);
    }

    //
    private Intent takePhoto(String cameraPhotoPath) {
        File cameraPhoto = new File(cameraPhotoPath);
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri photoUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                cameraPhoto);
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        takePhotoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,1);
        return takePhotoIntent;
        //  startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO);
    }


    private DrillHoleInfo info = ProjectInfoManager.getInstance().getOrNewDrillHoleInfo();


    private void initUI() {

        binding.edtHoleX.setText(TextHelper.safeString(info.holeX + ""));
        binding.edtHoleY.setText(TextHelper.safeString(info.holeY + ""));
        binding.edtHoleZ.setText(TextHelper.safeString(info.holeZ + ""));
        binding.edtJacketLength.setText(TextHelper.safeString(info.jacketLength + ""));
        binding.edtDesignDirection.setText(TextHelper.safeString(info.designDirection + ""));
        binding.edtDesignAngle.setText(TextHelper.safeString(""+info.designAngle));
        binding.edtAdjustMode.setText(TextHelper.safeString(""+info.adjustMode));

        /*if (info.livePhotos != null && !info.livePhotos.isEmpty()) {
            Glide.with(this).load(info.livePhotos).into(binding.ivSpot);
            showPhotoView();
            File file = new File(info.livePhotos);
            uri = Uri.fromFile(file).toString();
            shotPicture = ImageUtils.getBitmap(file);
        }*/
    }
    private void getPermission() {
        PackageManager pm = getPackageManager();
        boolean permission = (PackageManager.PERMISSION_GRANTED == pm.checkPermission("android.permission.CAMERA", "com.zhengyuan.learningtakephoto"));
        if (!permission) {
            //没有权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 15);
            }
        }
        permission = (PackageManager.PERMISSION_GRANTED == pm.checkPermission("android.permission.READ_EXTERNAL_STORAGE", "com.zhengyuan.learningtakephoto"));
        if (!permission) {
            //没有权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 15);
            }
        }
        permission = (PackageManager.PERMISSION_GRANTED == pm.checkPermission("android.permission.WRITE_EXTERNAL_STORAGE", "com.zhengyuan.learningtakephoto"));
        if (!permission) {
            //没有权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 15);
            }
        }
    }




    private void clickOK() {
        String xStr = binding.edtHoleX.getText().toString();
        if (xStr.isEmpty()) {
            ToastUtils.showLong("孔口坐标X不能为空");
            return;
        }
        double x = Double.parseDouble(xStr);
        String yStr = binding.edtHoleY.getText().toString();
        if (yStr.isEmpty()) {
            ToastUtils.showLong("孔口坐标Y不能为空");
            return;
        }
        double y = Double.parseDouble(yStr);
        String zStr = binding.edtHoleZ.getText().toString();
        if (zStr.isEmpty()) {
            ToastUtils.showLong("孔口坐标Z不能为空");
            return;
        }
        double z = Double.parseDouble(yStr);

        String jacketLengthStr = binding.edtJacketLength.getText().toString();
        if (jacketLengthStr.isEmpty()) {
            ToastUtils.showLong("护套长度(CM)不能为空");
            return;
        }
        long jacketLength = Long.parseLong(jacketLengthStr);

        String designDirectionStr = binding.edtDesignDirection.getText().toString();
        if (designDirectionStr.isEmpty()) {
            ToastUtils.showLong("设计方位不能为空");
            return;
        }
        int designDirection = Integer.parseInt(designDirectionStr);

        String designAngleStr = binding.edtDesignAngle.getText().toString();
        if (designAngleStr.isEmpty()) {
            ToastUtils.showLong("设计倾角不能为空");
            return;
        }
        int tmp = Integer.parseInt(designAngleStr);
        if (tmp < Short.MIN_VALUE || tmp > Short.MAX_VALUE) {
            ToastUtils.showLong("设计倾角只能是：" + Short.MIN_VALUE + " - " + Short.MAX_VALUE);
            return;
        }
        short designAngle = (short) tmp;

        //String adjustModeStr = binding.edtAdjustMode.getText().toString();
        /*if (adjustModeStr.isEmpty()) {
            ToastUtils.showLong("校准模式不能为空");
            return;
        }*/
        String adjustMode = "";//adjustModeStr;

        if (uri == null) {
            ToastUtils.showLong("现场照片不能为空");
            return;
        }

        if (isShowLoading()) return;
        showLoading();
        ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<DrillHoleInfo>() {
            @Override
            public DrillHoleInfo doInBackground() throws Throwable {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
                    String rootPath = PathUtils.getExternalAppFilesPath()+ File.separator + "privateData";
                    if (!FileUtils.createOrExistsDir(rootPath)) {
                        LogUtils.e("创建文件失败：" + rootPath);
                        ToastUtils.showLong("创建文件失败，请检查磁盘空间后重试!");
                        return null;
                    }
                    String projectRoot = rootPath + File.separator + sdf.format(new Date(System.currentTimeMillis()));
                    if (!FileUtils.createOrExistsDir(rootPath)) {
                        LogUtils.e("创建文件失败：" + projectRoot);
                        ToastUtils.showLong("创建文件失败，请检查磁盘空间后重试!");
                        return null;
                    }
                    String filePath = projectRoot + File.separator + "scene.png";
                    if (!ImageUtils.save(shotPicture, filePath, Bitmap.CompressFormat.PNG)) {
                        LogUtils.e("保存图片失败：" + filePath);
                        ToastUtils.showLong("保存图片失败，请重试!");
                        return null;
                    }
                    info.holeX = x;
                    info.holeY = y;
                    info.holeZ = z;
                    info.jacketLength = jacketLength;
                    info.designDirection = designDirection;
                    info.designAngle = designAngle;
                    info.adjustMode = adjustMode;
                    info.livePhotos = filePath;
                    info.livePhotosMd5 = ConvertUtils.bytes2HexString(EncryptUtils.encryptMD5File(filePath));
                    info.projectRoot = projectRoot;

                    info.collectionDateTime = time;/*System.currentTimeMillis();*/
                    info.id = TrackerDBManager.saveOrUpdate(info);
                    return info;
                }catch (Exception e) {
                    e.printStackTrace();
                    ToastUtils.showLong("保存信息失败，请重试!");
                }
                return null;
            }
            @Override
            public void onSuccess(DrillHoleInfo result) {
                hideLoading();
                if (result != null) {
                    DataCollectActivity.launch(DrillInfoEditActivity.this, result.id);
                    ActivityUtils.finishActivity(ProjectInfoEditActivity.class);
                    finish();
                }
            }
        });
    }

    private void showPhotoView() {
        if (View.GONE == binding.ivSpot.getVisibility()) {
            binding.ivSpot.setVisibility(View.VISIBLE);
            TextView tv = findViewById(R.id.tv_capture);
            tv.setText("点击重新拍摄");
            tv.setTextColor(Color.WHITE);
            tv.setCompoundDrawablePadding(ConvertUtils.dp2px(0));
            tv.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.mipmap.leida_divice, 0, 0);
        }
    }

    private Bitmap shotPicture;
    private long time;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分");
    private void onGetPicture(Bitmap pic) {
        showPhotoView();

        time = System.currentTimeMillis();

        StringBuilder sb = new StringBuilder();
        sb.append("企业编号:").append(info.companyId);
        pic = ImageUtils.addTextWatermark(pic, sb.toString(), 40, Color.WHITE, 0, 30, true);
        sb = new StringBuilder();
        sb.append("矿区编号:").append(info.miningAreaId);
        pic = ImageUtils.addTextWatermark(pic, sb.toString(), 40, Color.WHITE, 0, 80, true);
        sb = new StringBuilder();
        sb.append("钻井编号:").append(info.drillHoleId);
        pic = ImageUtils.addTextWatermark(pic, sb.toString(), 40, Color.WHITE, 0, 130, true);
        sb = new StringBuilder();
        sb.append("时间:").append(sdf.format(new Date(time)));
        pic = ImageUtils.addTextWatermark(pic, sb.toString(), 40, Color.WHITE, 0, 180, true);

        binding.ivSpot.setImageBitmap(pic);

        shotPicture = pic;


        /*if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(this, getPackageName()+".fileprovider", file).toString();
        } else {
            uri = Uri.fromFile(file).toString();
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            ContentResolver contentResolver = getContentResolver();
            Uri deleteFileUri = FileProvider.getUriForFile(this, getPackageName()+".fileprovider", file);
            grantUriPermission(getApplicationContext().getPackageName(), deleteFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            contentResolver.delete(Uri.parse(uri), null, null);
        } else {
            FileUtils.delete(file);
        }*/
        ImageUtils.save(pic, picfile, Bitmap.CompressFormat.PNG);
        uri = Uri.fromFile(picfile).toString();
    }

    private void clickCancel() {
        ActivityUtils.finishActivity(ProjectInfoEditActivity.class);
        finish();
    }
}
package com.zeus.tec.ui.leida;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.Toast;


import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.google.android.material.snackbar.Snackbar;
import com.zeus.tec.R;
import com.zeus.tec.databinding.ActivityMergeSampleBinding;
import com.zeus.tec.model.leida.MergeCache;
import com.zeus.tec.model.leida.ProbePoint;
import com.zeus.tec.model.utils.FeedbackUtil;
import com.zeus.tec.model.utils.FirFilter;
import com.zeus.tec.model.utils.Scale;
import com.zeus.tec.ui.directionfinder.util.BLEDevice;
import com.zeus.tec.ui.directionfinder.util.TypeConversion;

import org.angmarch.views.NiceSpinner;
import org.angmarch.views.OnSpinnerItemSelectedListener;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MergeSampleActivity extends AppCompatActivity implements View.OnClickListener {

    com.zeus.tec.databinding.ActivityMergeSampleBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMergeSampleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        try {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            initParams();
            initListener();
            initView();
            initUi();
        }
        catch (Exception exception)
        {
            ToastUtils.showLong(exception.getMessage());
        }
        // 全屏模式

    }

    //region 全局变量
    Bitmap graybitmap = null;//灰度图

    private final int [] grayMapBmpRatios = new int[]{0,1,2,3,4,5,8};//灰度图宽度比例

    private final int[] sampleNumber = {20, 40, 60, 80, 120, 160};
    private final float [] bmpHeightRatios = {1f,1.2f,1.4f,1.6f,1.8f,2.0f};
    private boolean isLoose = true; //是否疏松
    Bitmap sampleBitmap = null;

    private static final int DrawSample_Success = 0x01;
    private static final int DrawGrayMap_Success = 0x02;


    private boolean isDrawing = false;
    private double[] maxList;
    private double[] minList;
    private double globalMax;
    private double globalMin;
    private float scaleWidth;
    private final float scaleHeight =40;

    private final int[] lowfreq = {20, 30, 40, 50, 60};
    private final int[] highfreq = {100, 110, 120, 130, 150};
    private boolean isSampleFill;
    private int low1 = 20;
    private int high1 = 100;
    private final int Showstatus_GrayMap = 0x01;
    private final int ShowStatus_SampleMap = 0x02;
    private int showStatus = Showstatus_GrayMap;

    private int photoViewWidth = 1280;
    private int photoViewHeight = 500;

    //endregion

    //控件绘制完场后获取宽高
    private void initUi() {
        binding.bmpHeightSpinner.setSelectedIndex(0);
        binding.imageRatioSpinner.setSelectedIndex(0);
        binding.sampleImg.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // 获取宽高
                photoViewWidth = binding.showScro.getWidth();
                photoViewHeight = binding.showScro.getHeight();
                // 确保只执行一次，移除监听器
                binding.sampleImg.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                drawGrayMapBmp();
                // 处理逻辑
                binding.settingLl.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initParams() {
        getGlobalMax();
        isSampleFill = true;
    }

    private void initListener() {
        binding.sampleshowBtn.setOnClickListener(this);
        binding.SampleBt.setOnClickListener(this);
        binding.refreshBtv.setOnClickListener(this);
        binding.graymapshowBtn.setOnClickListener(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemBars();
        }
    }

    private void hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );
        }
    }

    private void initView() {
        binding.sampleImg.setScaleType(ImageView.ScaleType.CENTER);
        // 创建一个新的矩阵对象
//        Matrix matrix = new Matrix();
//        // 设置图片在左侧显示
//        // 这里的0表示图片的左上角在控件的左上角
//        matrix.postTranslate(0, 0);
//        // 设置矩阵到PhotoView
//        binding.sampleImg.setImageMatrix(matrix);
       // binding.sampleImg.setScaleType(ImageView.ScaleType.CENTER);
        NiceSpinner  imageRatioSpinner = binding.imageRatioSpinner;
        NiceSpinner  highFreq = binding.niceSpinnerHighFreq;
        NiceSpinner  lowFreq = binding.niceSpinnerLowFreq;
        NiceSpinner  sampleFill = binding.niceSpinnerSamplefill;
        NiceSpinner  spinner_isLoose = binding.niceSpinnerIsLoose;
        NiceSpinner  sampleCount_Spinner = binding.sampleCountSpinner;
        NiceSpinner  bmpHeight_spinner = binding.bmpHeightSpinner;
        List<String> dataset = new LinkedList<>(Arrays.asList("填充", "1:1", "1:2", "1:3", "1:5", "1:8"));//20,40,80,160
        List<String> lowfreqList = new LinkedList<>(Arrays.asList("20Mhz", "30Mhz", "40Mhz", "50Mhz", "60Mhz"));
        List<String> highfreqList = new LinkedList<>(Arrays.asList("100Mhz", "110Mhz", "120Mhz", "130Mhz", "150Mhz"));
        List<String> sampleFillList = new LinkedList<>(Arrays.asList("填充", "不填充"));
        List<String> LooseList = new LinkedList<>(Arrays.asList("疏松", "紧密"));
        List<String> sampleCountList = new LinkedList<>(Arrays.asList("20", "40", "60", "80", "120", "160"));
        bmpHeight_spinner.attachDataSource(new LinkedList<>(Arrays.asList("100%","90%","80%","70%","60%","50%")));
        spinner_isLoose.attachDataSource(LooseList);
        imageRatioSpinner.attachDataSource(dataset);
        lowFreq.attachDataSource(lowfreqList);
        highFreq.attachDataSource(highfreqList);
        sampleFill.attachDataSource(sampleFillList);
        sampleCount_Spinner.attachDataSource(sampleCountList);
        if (MergeCache.lstPointsOrder.size() > 80) {
            binding.sampleCountSpinner.setSelectedIndex(2);
           // imageRatioSpinner.setSelectedIndex(2);
        } else {
            binding.sampleCountSpinner.setSelectedIndex(1);
           // imageRatioSpinner.setSelectedIndex(1);
        }
        lowFreq.setOnSpinnerItemSelectedListener((parent, view, position, id) -> low1 = lowfreq[position]);
        highFreq.setOnSpinnerItemSelectedListener((parent, view, position, id) -> high1 = highfreq[position]);

        sampleFill.setOnSpinnerItemSelectedListener((parent, view, position, id) -> isSampleFill = position == 0);
        spinner_isLoose.setOnSpinnerItemSelectedListener((parent, view, position, id) -> isLoose = position == 0);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        FeedbackUtil.getInstance().doFeedback();
        switch (v.getId()) {
            case R.id.sampleshow_btn: {
                try {
                    if (showStatus == ShowStatus_SampleMap)
                    {
                    }
                    else if
                    (showStatus == Showstatus_GrayMap)
                    {
                        if (sampleBitmap == null)
                        {
                            drawSampleImg(binding.sampleCountSpinner.getSelectedIndex());
                        }
                        else
                        {
                            binding.sampleImg.setImageBitmap(sampleBitmap);
                        }
                        showStatus = ShowStatus_SampleMap;

                    }
                } catch (Exception exception) {
                    ToastUtils.showLong(exception.getMessage());
                }
                break;
            }
            case R.id.Sample_bt: {
                if (binding.settingLl.getVisibility() == View.GONE) {
                    binding.settingLl.setVisibility(View.VISIBLE);
                } else {
                    binding.settingLl.setVisibility(View.GONE);
                }
                break;
            }
            case R.id.graymapshow_btn: {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        if (showStatus == ShowStatus_SampleMap) {
                            showStatus = Showstatus_GrayMap;
                            if (graybitmap == null) {
                                drawGrayMapBmp();
                            } else {
                                binding.sampleImg.setImageBitmap(graybitmap);
                            }
                        }
                    } catch (Exception exception) {
                        ToastUtils.showLong(exception.getMessage());
                    }
                    break;
                }
            }
            case R.id.refresh_btv: {
                for (int i = 0; i < MergeCache.lstPointsOrder.size(); i++) {
                    MergeCache.lstPointsOrder.get(i).Voltage = (FirFilter.ProcessSample(MergeCache.originData[i], MergeCache.dataHeader.SampleCount, MergeCache.dataHeader.SampleFrequency, low1, high1, 40));
                }
                if (showStatus == ShowStatus_SampleMap) {
                    drawSampleImg((binding.sampleCountSpinner.getSelectedIndex()));
                } else if (showStatus == Showstatus_GrayMap) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        drawGrayMapBmp();
                    }
                }
                break;
            }
        }
    }

    private void drawGrayMapBmp() {
        getGlobalMax();
        graybitmap = Bitmap.createBitmap(MergeCache.lstPointsOrder.size(), MergeCache.dataHeader.SampleCount, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(graybitmap);
        // 绘制黑色背景
        canvas.drawColor(Color.WHITE);
        // 定义画笔
        Paint paint = new Paint();
        // 设置画笔颜色
        //  paint.setStrokeWidth(1f);//设置画笔粗细度
        //抗锯齿
        double m_distance = globalMax - globalMin;
        int bmpRatio = grayMapBmpRatios[binding.imageRatioSpinner.getSelectedIndex()];
        initColorMap();
        binding.loadingAvi.setVisibility(View.VISIBLE);
        Thread drawgrayThread = new Thread(() -> {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    float initRatio =(float)(photoViewHeight-40)/(float) MergeCache.dataHeader.SampleCount;
                    float sampleScaleHeight = scaleHeight/(initRatio)/(bmpHeightRatios[binding.bmpHeightSpinner.getSelectedIndex()]);
                   // float sampleHeight = canvas.getHeight()-sampleScaleHeight;
                    for (int i = 0; i < MergeCache.lstPointsOrder.size(); i++) {
                        ProbePoint onePoint = MergeCache.lstPointsOrder.get(i);
                        for (int j = 0; j < onePoint.Voltage.length; j++) {
                            paint.setColor(MapValueToColor((onePoint.Voltage[j] - globalMin) / m_distance * 100));
                            canvas.drawPoint(i, j+sampleScaleHeight, paint);
                        }
                    }
                }
                isDrawing = true;
                if (bmpRatio==0){
                    graybitmap = Bitmap.createScaledBitmap(graybitmap, photoViewWidth, ((int)(photoViewHeight*bmpHeightRatios[binding.bmpHeightSpinner.getSelectedIndex()])), true);
                }
                else if(bmpRatio==1){
                    graybitmap = Bitmap.createScaledBitmap(graybitmap, graybitmap.getWidth(), ((int)(photoViewHeight*bmpHeightRatios[binding.bmpHeightSpinner.getSelectedIndex()])), true);
                }
                else {
                    graybitmap = Bitmap.createScaledBitmap(graybitmap, graybitmap.getWidth()*bmpRatio, ((int)(photoViewHeight*bmpHeightRatios[binding.bmpHeightSpinner.getSelectedIndex()])), true);
                }
                graybitmap = Bitmap.createScaledBitmap(graybitmap, graybitmap.getWidth(), ((int)(photoViewHeight*bmpHeightRatios[binding.bmpHeightSpinner.getSelectedIndex()])), true);
                canvas.setBitmap(graybitmap);
                Scale DrillLengthScale = new Scale(
                        canvas.getWidth(),
                        canvas.getHeight(),
                        Color.GRAY,
                        false,
                        MergeCache.PipeCount,
                        0,
                        true,
                        0.2f,
                        Color.BLACK,
                        Color.RED,
                        Color.BLUE,
                        15,
                        10
                );
                DrillLengthScale.drawScale(canvas,0,0,canvas.getWidth(),scaleHeight);
                drawDepthScale ();
                Message message = new Message();
                message.what = DrawGrayMap_Success;
                mHandler.sendMessage(message);
            } catch (Exception exception) {
                ToastUtils.showLong(exception.getMessage());
            }
        });
        drawgrayThread.start();
    }

    private void initColorMap() {

        MergeCache.currentColorMap.add(new double[]{0, 0, 0, 0, 0, 0, 0});
        MergeCache.currentColorMap.add(new double[]{100, 255, 255, 255, 0, 0, 0});
        for (int i = 1; i < MergeCache.currentColorMap.size(); i++) {
            int space = (int) (MergeCache.currentColorMap.get(i)[0] - MergeCache.currentColorMap.get(i - 1)[0]);
            double redspace = (MergeCache.currentColorMap.get(i)[1] - MergeCache.currentColorMap.get(i - 1)[1]);
            double greenSpace = (MergeCache.currentColorMap.get(i)[2] - MergeCache.currentColorMap.get(i - 1)[2]);
            double blueSpace = (MergeCache.currentColorMap.get(i)[3] - MergeCache.currentColorMap.get(i - 1)[3]);
            MergeCache.currentColorMap.get(i)[4] = redspace / space;
            MergeCache.currentColorMap.get(i)[5] = greenSpace / space;
            MergeCache.currentColorMap.get(i)[6] = blueSpace / space;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private int MapValueToColor(double value) {
        int red;
        int green;
        int blue;
        int step = 0;
        for (int i = 0; i < MergeCache.currentColorMap.size(); i++) {
            if (value <= MergeCache.currentColorMap.get(i)[0]) {
                step = i;
                break;
            }
        }
        red = (int) (MergeCache.currentColorMap.get(step)[1] - MergeCache.currentColorMap.get(step)[4] * (MergeCache.currentColorMap.get(step)[0] - value));
        green = (int) (MergeCache.currentColorMap.get(step)[2] - MergeCache.currentColorMap.get(step)[5] * (MergeCache.currentColorMap.get(step)[0] - value));
        blue = (int) (MergeCache.currentColorMap.get(step)[3] - MergeCache.currentColorMap.get(step)[6] * (MergeCache.currentColorMap.get(step)[0] - value));

        return Color.argb(255, red, green, blue);
    }

    private void drawSampleImg(int size) {
        if (isDrawing) {
            return;
        }
        int bmpWidth = (int) (photoViewWidth * ((float) MergeCache.lstPointsOrder.size()) / sampleNumber[size]);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            sampleBitmap = Bitmap.createBitmap(bmpWidth, photoViewHeight, Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(sampleBitmap);
        // 绘制黑色背景
        canvas.drawColor(Color.YELLOW);
        // 定义画笔
        Paint paint = new Paint();
        // 设置画笔颜色
        paint.setColor(Color.BLACK);
        // paint.setStrokeWidth(1f);//设置画笔粗细度
        //抗锯齿
        paint.setAntiAlias(true);
        // 画笔实心
        // paint.setStyle(Paint.Style.FILL);
        // 文本水平居左对齐
        binding.loadingAvi.setVisibility(View.VISIBLE);

        // paint.setTextAlign(Paint.Align.LEFT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            isDrawing = true;
            Thread drawThread = new Thread(() -> drawSample(canvas));
            drawThread.start();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void drawSample(Canvas canvas) {
        Paint linepaint = new Paint();
        linepaint.setColor(Color.BLACK);
        linepaint.setStrokeWidth(1f);
        linepaint.setAntiAlias(true);
        Paint fillPaint = new Paint();
        fillPaint.setColor(Color.RED);
        fillPaint.setStrokeWidth(2f);//避免出现条纹
        // linepaint.setStyle(Paint.Style.FILL);
        float sampleWidth;
        if (isLoose) {
            sampleWidth = ((float) canvas.getWidth()) / MergeCache.lstPointsOrder.size();
        } else {
            sampleWidth = ((float) canvas.getWidth()) / MergeCache.lstPointsOrder.size() * 2;
        }
        float sampleScaleHeight = scaleHeight/(bmpHeightRatios[binding.bmpHeightSpinner.getSelectedIndex()]);
        float sampleHeight = canvas.getHeight()-sampleScaleHeight;
        float spaceHeight = sampleHeight / MergeCache.dataHeader.SampleCount;
        maxList = new double[MergeCache.lstPointsOrder.size()];
        minList = new double[MergeCache.lstPointsOrder.size()];
        float x1;
        float y1;
        float sampleSpace;
        if (isLoose) {
            sampleSpace = sampleWidth;
        } else {
            sampleSpace = sampleWidth / 2;
        }
        for (int i = 0; i < MergeCache.lstPointsOrder.size(); i++) {
            ProbePoint oneSample = MergeCache.lstPointsOrder.get(i);
            float x = 0;
            float y = sampleScaleHeight;
            double max = 0;
            max = getMax(oneSample.Voltage) * 1.1;
            float tmp = sampleSpace * i + sampleWidth / 2;
            if (!isSampleFill) {
                for (int j = 0; j < oneSample.Voltage.length; j++) {
                    x1 = (float) (tmp + (sampleWidth / 2 * (oneSample.Voltage[j] / max)));
                    y1 = spaceHeight * j+sampleScaleHeight;
                    canvas.drawLine(x, y, x1, y1, linepaint);
                    y = y1;
                    x = x1;
                }
            } else {
                for (int j = 0; j < oneSample.Voltage.length; j++) {
                    x1 = (float) (tmp + (sampleWidth / 2 * (oneSample.Voltage[j] / max)));
                    y1 = spaceHeight * j+sampleScaleHeight;
                    canvas.drawLine(x, y, x1, y1, linepaint);
                    if (oneSample.Voltage[j] > 0) {
                        canvas.drawLine(tmp, y1, x1, y1, fillPaint);
                    }
                    y = y1;
                    x = x1;
                }
            }
        }
        sampleBitmap = Bitmap.createScaledBitmap(sampleBitmap, sampleBitmap.getWidth(), ((int)(photoViewHeight*bmpHeightRatios[binding.bmpHeightSpinner.getSelectedIndex()])), true);
        canvas.setBitmap(sampleBitmap);
        Scale DrillLengthScale = new Scale(
                canvas.getWidth(),
                canvas.getHeight(),
                Color.GRAY,
                false,
                MergeCache.PipeCount,
                0,
                true,
                0.05f,
                Color.BLACK,
                Color.RED,
                Color.BLUE,
                15,
                10
        );
        DrillLengthScale.drawScale(canvas,0,0,canvas.getWidth(),scaleHeight);
        drawDepthScale ();
        Message message = new Message();
        message.what = DrawSample_Success;
        mHandler.sendMessage(message);
    }

    private void drawDepthScale (){
        depthScaleBmp =  Bitmap.createBitmap(50,  ((int)(photoViewHeight*bmpHeightRatios[binding.bmpHeightSpinner.getSelectedIndex()])), Bitmap.Config.ARGB_8888);
        Canvas depthCanvas = new Canvas();
        depthCanvas.setBitmap(depthScaleBmp);
        float maxDepth = ((1.0f/MergeCache.dataHeader.SampleFrequency)*512f*MergeCache.sampleSpeed)/2;
        depthCanvas.drawColor(Color.argb(0,0,0,0));
        Scale DepthScale = new Scale(
                depthCanvas.getWidth(),
                depthCanvas.getHeight()-scaleHeight,
                Color.argb(50,0,0,0),
                true,
                maxDepth,
                0,
                false,
                0.25f,
                Color.BLACK,
                Color.RED,
                Color.BLUE,
                15,
                10
        );
        DepthScale.drawScale(depthCanvas,0,(int)scaleHeight,scaleHeight,depthCanvas.getHeight()-scaleHeight);
    }

    private Bitmap depthScaleBmp ;

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @SuppressLint({"SetTextI18n", "MissingPermission"})
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DrawSample_Success: {
                    binding.loadingAvi.setVisibility(View.GONE);
                    binding.sampleImg.setImageBitmap(sampleBitmap);
                    binding.depthscalePhoto.setImageBitmap(depthScaleBmp);
                    isDrawing = false;
                    break;
                }
                case DrawGrayMap_Success: {
                    binding.loadingAvi.setVisibility(View.GONE);
                    binding.sampleImg.setImageBitmap(graybitmap);
                    binding.depthscalePhoto.setImageBitmap(depthScaleBmp);
                    isDrawing = false;
                    break;
                }
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    private double getMax(double[] numbers) {
        double tmpMax = Arrays.stream(numbers).max().getAsDouble();
        double tmpMin = Arrays.stream(numbers).min().getAsDouble();
        if (0 < (Math.abs(tmpMax) - Math.abs(tmpMin))) {
            return Math.abs(Arrays.stream(numbers).max().getAsDouble());
        } else {
            return Math.abs(Arrays.stream(numbers).min().getAsDouble());
        }
    }

    private void getGlobalMax() {
        maxList = new double[MergeCache.lstPointsOrder.size()];
        minList = new double[MergeCache.lstPointsOrder.size()];
        for (int i = 0; i < MergeCache.lstPointsOrder.size(); i++) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                maxList[i] = getArrayMax(MergeCache.lstPointsOrder.get(i).Voltage);
                minList[i] = getArrayMin(MergeCache.lstPointsOrder.get(i).Voltage);
            }
        }
        globalMax = getArrayMax(maxList);
        globalMin = getArrayMin(minList);
    }

    private double getArrayMax(double[] array) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return Arrays.stream(array).max().getAsDouble();
            } else {
                return 0;
            }
        } catch (Exception exception) {
            return 0;
        }
    }

    private double getArrayMin(double[] array) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Arrays.stream(array).min().getAsDouble();
        } else {
            return 0;
        }
    }


}
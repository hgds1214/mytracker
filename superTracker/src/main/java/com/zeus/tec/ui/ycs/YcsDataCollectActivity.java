package com.zeus.tec.ui.ycs;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.zeus.tec.R;
import com.zeus.tec.databinding.ActivityLeidaDataCollectBinding;
import com.zeus.tec.databinding.ActivityYcsDataCollectBinding;
import com.zeus.tec.db.TrackerDBManager;
import com.zeus.tec.model.leida.MergeCache;
import com.zeus.tec.model.leida.leidaPointRecordInfo;
import com.zeus.tec.model.leida.leida_info;
import com.zeus.tec.model.leida.main.FileBean;
import com.zeus.tec.model.leida.main.PointParamter;
import com.zeus.tec.model.utils.FeedbackUtil;
import com.zeus.tec.model.ycs.YcsMainCache;
import com.zeus.tec.model.ycs.YcsPoint;
import com.zeus.tec.ui.leida.Apater.PointListAdapter;
import com.zeus.tec.ui.leida.Apater.fileListAdapter;
import com.zeus.tec.ui.leida.LeidaDataCollectActivity;
import com.zeus.tec.ui.leida.ProjectleidainfoActivity;
import com.zeus.tec.ui.leida.interfaceUtil.DialogCallback;
import com.zeus.tec.ui.leida.util.IOtool;
import com.zeus.tec.ui.leida.util.MesseagWindows;
import com.zeus.tec.ui.leida.util.MyApplicationContext;
import com.zeus.tec.ui.leida.util.MyTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class YcsDataCollectActivity extends AppCompatActivity implements View.OnClickListener {

    ActivityYcsDataCollectBinding binding;
    Context context1;
    YcsMainCache cache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApplicationContext.initialize(this);
        binding = ActivityYcsDataCollectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context1 = MyApplicationContext.getInstance().getAppContext();
        cache = YcsMainCache.GetInstance();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);//这两句设置禁止所有检查
        try {
            loadLastProject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        cache.CreatSendSocket();
        cache.CreatReceiveThread(this);
        Thread th = new Thread(this::RefreshStatus);
        th.start();
        initListener();
    }

    private void initListener() {
        step_1();
        long currentTime = System.currentTimeMillis();
        binding.tvCountTime.setStartTime(currentTime);
        binding.tvCountTime.start();
        binding.tvPointRecord.setOnClickListener(this);
        binding.tvProgramParamter.setOnClickListener(this);
        binding.startButton.setOnClickListener(this);
        binding.tvRefreshStatus.setOnClickListener(this);
        binding.tvBuildProgram.setOnClickListener(this);
        binding.tvPoint.setOnClickListener(this);
        binding.tvDataManage.setOnClickListener(this);
        binding.tvRefresh.setOnClickListener(this);
        binding.tvExit.setOnClickListener(this);
        binding.tvDownload.setOnClickListener(this);
        binding.tvDelete.setOnClickListener(this);
        binding.ivBack.setOnClickListener(this);
    }

    private void step_1() {
        binding.ivStep1.setState(1);
    }

    private void loadLastProject() throws IOException {
        cache.RefreshInitFile();
        if (FileUtils.isFileExists(cache.sysFilePath)) {
            BufferedReader reader = null;
            try {
                cache.trdFilePath = cache.rootFilePath + File.separator + cache.projectName + File.separator + cache.projectName + ".trd";
                reader = new BufferedReader(new FileReader(cache.trdFilePath));
                String[] projectInfoStr = reader.readLine().split("\t");
                String str ;
                int index =0;
                for (int num = 0; ( str = reader.readLine()) != null; num++)
                {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        String [] tmpStr  = str.split("\t");
                        String time = tmpStr[0];
                        index ++;
                        float distance = Float.parseFloat(tmpStr[3]);
                        cache.pointList.add(new YcsPoint(index,time,distance));
                    }
                }
                reader.close();
                if (projectInfoStr.length == 14) {
                    binding.tvPointDistance.setText(projectInfoStr[0]);
                    cache.pointDistance = Float.parseFloat(projectInfoStr[0]);
                    binding.tvCreatTime.setText(projectInfoStr[1]);
                    binding.tvRecAreaX.setText(projectInfoStr[2]);
                    binding.tvTvRecAreaY.setText(projectInfoStr[3]);
                    binding.tvTvRecAreaZ.setText(projectInfoStr[4]);
                    binding.tvSendArea.setText(projectInfoStr[5]);
                    binding.tvMarkSpace.setText(projectInfoStr[6]);
                    binding.tvSendFrequency.setText(projectInfoStr[7]);
                    binding.tvPointNumber.setText(projectInfoStr[8]);
                    binding.tvSampleIntervel.setText(projectInfoStr[9]);
                    binding.tvOverLayNumber.setText(projectInfoStr[10]);
                    binding.tvSendEnergy.setText(projectInfoStr[11]);
                    binding.tvTimeSample.setText(projectInfoStr[12]);
                    binding.tvGyro.setText(projectInfoStr[13]);
                    binding.tvProjectName.setText(cache.projectName);
                }
                initPointList(cache.pointList);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        FeedbackUtil.getInstance().doFeedback();
        switch (v.getId()) {
            case R.id.tv_refresh_status: {
                @SuppressLint("ResourceAsColor") Thread th = new Thread(() -> {
                    RefreshStatus();
                    ToastUtils toastUtils = ToastUtils.make();
                    toastUtils.setBgColor(R.color.design_default_color_background);
                    toastUtils.setLeftIcon(R.mipmap.logo);
                    toastUtils.setTextColor(R.color.program_text_color);
                    toastUtils.show("刷新成功");
                });
                th.start();
                break;
            }
            case R.id.start_button: {
                if (cache.DeviceStatus != null) {
                    if (cache.DeviceStatus.status == 0) {
                        if (cache.newProject == 0) {
                            ToastUtils.showLong("请先新建项目信息和设备参数");
                            return;
                        }
                        StartWork();
                    } else if (cache.DeviceStatus.status == 1) {
                        StopWork();
                    }
                }
                break;
            }
            case R.id.tv_build_program: {
                MesseagWindows.showMessageBox((Context) this, "是否创建新项目", "是否放弃当前项目创建新项目", new DialogCallback() {
                    @Override
                    public void onPositiveButtonClick() {
                        Intent intent = new Intent(YcsDataCollectActivity.this, YcsProjectSettingActivity.class);
                        startActivity(intent);
                    }
                    @Override
                    public void onNegativeButtonClick() {
                    }
                });
                break;
            }
            case R.id.tv_Data_manage: {
                if (binding.layDownloadWindows.getVisibility() == View.GONE) {
                    GetFiles();
                    binding.layDownloadWindows.setVisibility(View.VISIBLE);
                }
                break;
            }
            case R.id.tv_refresh: {
                GetFiles();
                break;
            }
            case R.id.tv_Download: {
                tvDownload_click();
                break;
            }
            case R.id.tv_Data_Download: {

                break;
            }
            case R.id.tv_exit: {
                binding.layDownloadWindows.setVisibility(View.GONE);
                break;
            }
            case R.id.tv_Delete: {
                MesseagWindows.showMessageBox(this, "是否删除", "该项目文件删除后将无法恢复", new DialogCallback() {
                    @Override
                    public void onPositiveButtonClick() {
                        if (cache.selectFileName.equals("")) {
                            Context context = MyApplicationContext.getInstance().getAppContext();
                            Toast.makeText(context, "请选择需要删除的项目", Toast.LENGTH_LONG).show();
                            return;
                        }

                        int result = cache.GetDeleteFile(cache.selectFileName);
                        if (result == 1) {
                            // MessageBox.Show("项目文件删除成功!");
                            Context context = MyApplicationContext.getInstance().getAppContext();
                            Toast.makeText(context, "项目文件删除成功", Toast.LENGTH_LONG).show();
                            cache.selectFileName = "";
                            // DownloadFileName.Text = SelectProductName;
                            GetFiles();
                        } else {
                            Context context = MyApplicationContext.getInstance().getAppContext();
                            Toast.makeText(context, "项目文件删除失败", Toast.LENGTH_LONG).show();
                            //  MessageBox.Show("项目文件删除失败!");
                        }
                    }
                    @Override
                    public void onNegativeButtonClick() {
                    }
                });
                break;
            }
            case R.id.tv_point_record: {
                if (binding.layProgramParamter.getVisibility() == View.VISIBLE) {
                    //  layTitle.setVisibility(View.GONE);
                    binding.layProgramParamter.setVisibility(View.GONE);
                    binding.layoutPointRecord.setVisibility(View.VISIBLE);
                    binding.tvPointRecord.setBackgroundResource(R.drawable.btn_collect_bg);
                    binding.tvPointRecord.setTextColor(Color.WHITE);
                    binding.tvProgramParamter.setBackgroundResource(R.drawable.btn_finish_bg);
                    binding.tvProgramParamter.setTextColor(Color.BLACK);
                }
                break;
            }
            case R.id.tv_program_paramter:{
                if (binding.layProgramParamter.getVisibility() == View.GONE) {
                    binding.layProgramParamter.setVisibility(View.VISIBLE);
                    binding.layoutPointRecord.setVisibility(View.GONE);
                    binding.tvProgramParamter.setBackgroundResource(R.drawable.btn_collect_bg);
                    binding.tvProgramParamter.setTextColor(Color.WHITE);
                    binding.tvPointRecord.setBackgroundResource(R.drawable.btn_finish_bg);
                    binding.tvPointRecord.setTextColor(Color.BLACK);
                }
                break;
            }
            case R.id.tv_Point: {
                if (cache.DeviceStatus != null) {
                    if (cache.DeviceStatus.status == 1) {
                        pointRecord();
                    } else {
                        if (isignore) {
                            pointRecord();
                        } else {
                            MesseagWindows.showMessageBox((Context) this, "提醒", "当前状态为未采集，是否继续打点", new DialogCallback() {
                                @Override
                                public void onPositiveButtonClick() {
                                    pointRecord();
                                    isignore = true;
                                }

                                @Override
                                public void onNegativeButtonClick() {
                                }
                            });
                        }
                    }
                } else {
                    if (!isignore) {
                        MesseagWindows.showMessageBox((Context) this, "提醒", "当前状态为未采集，是否继续打点", new DialogCallback() {
                            @Override
                            public void onPositiveButtonClick() {
                                pointRecord();
                                isignore = true;
                            }

                            @Override
                            public void onNegativeButtonClick() {
                            }
                        });
                    } else {
                        pointRecord();
                    }
                }
            }
        }
    }
    private boolean isignore = false;

    private void pointRecord() {
        try {
            if (FileUtils.isFileExists(cache.trdFilePath)) {
                String current_time = "";
                String content;
                LocalDateTime now;
                long timecode = System.currentTimeMillis()/1000;//精确到秒
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    now = LocalDateTime.now();
                    int year = now.getYear();
                    int month = now.getMonthValue();
                    int day = now.getDayOfMonth();
                    int hour = now.getHour();
                    int minute = now.getMinute();
                    int second = now.getSecond();
                    String tmp = String.valueOf(year);
                    current_time += tmp;
                    tmp = String.valueOf(month);
                    if (month < 10)
                        tmp = "0" + tmp;
                    current_time += "-";
                    current_time += tmp;
                    tmp = String.valueOf(day);
                    if (day < 10)
                        tmp = "0" + tmp;
                    current_time += "-";
                    current_time += tmp;
                    tmp = String.valueOf(hour);
                    if (hour < 10)
                        tmp = "0" + tmp;
                    current_time += " ";
                    current_time += tmp;
                    tmp = String.valueOf(minute);
                    if (minute < 10)
                        tmp = "0" + tmp;
                    current_time += ":";
                    current_time += tmp;
                    tmp = String.valueOf(second);
                    if (second < 10)
                        tmp = "0" + tmp;
                    current_time += ":";
                    current_time += tmp;
                }
                float TotalDis = (cache.pointList.size() + 1) * cache.pointDistance;
                content = current_time + '\t' + timecode + '\t' + cache.pointDistance + '\t' + TotalDis;
                IOtool.saveText(cache.trdFilePath, "\n", true);
                IOtool.saveText(cache.trdFilePath, content, true);
                cache.totalPoint = cache.pointList.size()+1;
                cache.pointList.add(new YcsPoint(cache.totalPoint, current_time,TotalDis));
                initPointList(cache.pointList);
                binding.tv24.setText(String.valueOf(cache.pointList.size()));
                binding.tv23.setText(String.format("%.1f", TotalDis/100f));
            } else {
                Toast.makeText(this, "项目测点文件不存在，无法打点测试！", Toast.LENGTH_LONG).show();
            }
        } catch (Exception exception) {
            ToastUtils.showLong(exception.getMessage());
        }
    }

    private void initPointList(List<YcsPoint> pointParamters) {
        YcsPointListAdapter adapter = new YcsPointListAdapter(YcsDataCollectActivity.this, pointParamters);
        binding.listPoint.setAdapter(adapter);
        binding.listPoint.setSelection(adapter.getCount() - 1);
    }


    public void startWork(String fileName) {
        Boolean complete = false;
        String save_file = cache.FileSavePath;
        String tmp_file = cache.FileSavePath.replace("dat", "tmp");
        try {

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void tvDownload_click() {

        try {
            if (cache.selectFileName.equals("")) {
                //MessageBox.Show("请选择需要下载的项目!");
                Toast.makeText(this, "请选择需要下载的项目", Toast.LENGTH_LONG).show();
                return;
            }
            if
            (cache.selectFileName.equals(cache.projectName)) {
                ProgressBar progressBar = binding.progressBar;
                progressBar.setVisibility(View.VISIBLE);
                binding.tvStep1Text.setText("正在下载");
                TextView steptext = binding.tvStep1Text;

                YcsTask task = new YcsTask(progressBar, cache.selectFileName, steptext, 1, cache.trdFilePath.replace(".trd",".dat"));
                task.execute();
            } else {
                MesseagWindows.showMessageBox(YcsDataCollectActivity.this, "是否下载", "该数据不是本次检测的数据,是否继续下载", new DialogCallback() {
                    @Override
                    public void onPositiveButtonClick() {
                        List<leida_info> selectLeidaInfo = TrackerDBManager.isHaveData(cache.selectFileName);
                        if (selectLeidaInfo.size() == 0) {
                            MesseagWindows.showMessageBox(YcsDataCollectActivity.this, "是否继续下载", "该数据项目记录不存在，如果下载将放入临时文件夹，是否继续下载", new DialogCallback() {
                                @Override
                                public void onPositiveButtonClick() {
                                    ProgressBar progressBar = binding.progressBar;
                                    progressBar.setVisibility(View.VISIBLE);
                                    binding.tvStep1Text.setText("正在下载");
                                    TextView steptext = binding.tvStep1Text;
                                    YcsTask task = new YcsTask(progressBar, cache.selectFileName, steptext, 2, cache.FileSavePath);
                                    task.execute();
                                }
                                @Override
                                public void onNegativeButtonClick() {
                                }
                            });
                        } else {
                            ProgressBar progressBar = binding.progressBar;
                            progressBar.setVisibility(View.VISIBLE);
                            binding.tvStep1Text.setText("正在下载");
                            TextView steptext = binding.tvStep1Text;
                            MyTask task = new MyTask(progressBar, cache.selectFileName, steptext, 1, selectLeidaInfo.get(0));
                            task.execute();
                        }
                    }

                    @Override
                    public void onNegativeButtonClick() {

                    }
                });
            }
        } catch (Exception ex) {
            ToastUtils.showLong(ex.getLocalizedMessage());
        }

    }

    public void downLoadData() {

    }

    int index = 10;

    public void GetFiles() {
        List<FileBean> files;
        files = cache.GetFilesName();
        if (files != null) {
            files.add(new FileBean());
            fileListAdapter adapter = new fileListAdapter(YcsDataCollectActivity.this, files);
            binding.listFile.setAdapter(adapter);
            binding.listFile.setOnItemClickListener((parent, view, position, id) -> {
                if (position > 0) {
                    String tmp = ((FileBean) adapter.getItem(position - 1)).FileName;
                    int index = tmp.lastIndexOf(".");
                    cache.selectFileName = tmp.substring(0, index);
                    view.setSelected(true);
                    view.setBackgroundResource(R.drawable.list_item_background_selector);
                    ImageView imageView = view.findViewById(R.id.iv_file_image);
                    imageView.setImageResource(R.drawable.image_selector);
                }
            });
        } else {
            Toast.makeText(this, "网络异常: 获取设备项目数据文件列表失败!", Toast.LENGTH_LONG).show();
        }
        index = index + 2;
    }

    private void RefreshStatus() {
        cache.GetDeviceStatus();
        if (cache.DeviceStatus != null) {
            if (cache.DeviceStatus.status == 1) {
                ((Activity) context1).runOnUiThread(() -> {
                    binding.ivStep1.setState(1);
                    binding.tvStep1Text.setText("正在采集");
                    binding.tvDataManage.setEnabled(false);

                    binding.tvBuildProgram.setEnabled(false);
                    binding.startButton.setEnabled(true);
                    binding.startButton.setText("停止采集");
                });
            } else {
                ((Activity) context1).runOnUiThread(() -> {
                    binding.ivStep1.setState(2);
                    binding.tvStep1Text.setText("设备已连接,请开始采集");
                    binding.startButton.setEnabled(true);
                    float quantity = cache.DeviceStatus.r_quantity;
                    String tmp = String.valueOf(quantity).substring(0, 3) + "V";
                    binding.tv21.setText(tmp);
                    float gyro = cache.DeviceStatus.gyro;//人生没有我并不会不同，为何你不懂，只要有爱就有痛，有一天你会知道，人生没用我斌不会不同，
                    // 我好害怕总是泪眼朦胧
                    String Gyro = String.valueOf(gyro);
                    if (Gyro.length() > 3) {
                        binding.tv22.setText(Gyro.substring(0, 4));
                    } else {
                        binding.tv22.setText(Gyro.substring(0, 3));
                        //往事不要再提，人生以多风雨，纵然记忆抹不去，爱与恨都还心底，真的要断了过去，让明天好好继续，你就不要再苦苦追问我的消息
                        //爱情他是个难题，让人目眩神迷，忘了痛或许可以，忘了你却太不容易，你不曾真的离去，你始终在我心里，我对你仍有爱意，我对自己无能为力
                        //因为我人仍有梦，依然将你放在我心中，总是容易被往事打动，总是为了你心痛
                        //别留恋岁月中，我无意的柔情万种，不要问我是否再相逢，不要管我言不由衷
                        //为何你不懂(别说我不懂)，只要有爱就有痛，有一天你会知道，人生没有我并不会不同，
                        //人生已经太匆匆，我好害怕总是泪眼朦胧，忘了我就没有痛，将往事留在风中
                        //往事不要再提，人生以多风雨，纵然记忆抹不去，爱与恨都还心底，真的要断了过去，让明天好好继续，你就不要再苦苦追问我的消息
                        //为何你不懂，只要有爱就有痛，有一天你会知道，人生没有我并不会不同，
                        //人生已经太匆匆，我好害怕总是泪眼朦胧，忘了我就没有痛(忘了你也没有用)，将往事留在风中
                        //为何你不懂(别说我不懂)，只要有爱就有痛(有爱就有痛)，有一天你会知道，人生没有我并不会不同(没用你会不同)，
                        //人生已经太匆匆，我好害怕总是泪眼朦胧，忘了我就没有痛(忘了你也没有用)，将往事留在风中

                    }
                    binding.startButton.setEnabled(true);
                    binding.tvDataManage.setEnabled(true);

                    binding.tvBuildProgram.setEnabled(true);
                    binding.tvPoint.setEnabled(true);
                });
            }
        } else {
            ((Activity) context1).runOnUiThread(() -> {
                binding.ivStep1.setState(3);
                binding.tvStep1Text.setText("设备连接失败,请检查手机热点是否打开");
                binding.startButton.setEnabled(false);
                binding.tvBuildProgram.setEnabled(false);
                // binding.tvPoint.setEnabled(false);
            });
        }
    }

    private void StartWork() {
        RefreshStatus();
        if (cache.DeviceStatus != null) {
            if (cache.DeviceStatus.status == 1) {
                Toast.makeText(this, "设备正在工作中，无法请求！", Toast.LENGTH_SHORT).show();

                binding.tvDataManage.setEnabled(false);
                binding.tvStatus.setText("正在运行");
                binding.tvPoint.setEnabled(true);
                binding.startButton.setText("停止采集");
            } else {
                if (cache.GetStartWork() == 1) {
                    Toast.makeText(this, "设备开始工作！", Toast.LENGTH_SHORT).show();
                    binding.startButton.setText("停止采集");
                    binding.tvPoint.setEnabled(true);
                    binding.tvStatus.setText("正在运行");

                    binding.tvDataManage.setEnabled(false);
                } else {
                    Toast.makeText(this, "开始工作请求失败！", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "设备连接失败,无法请求!", Toast.LENGTH_SHORT).show();
        }
        RefreshStatus();
    }

    private void StopWork() {
        RefreshStatus();
        if (cache.DeviceStatus != null) {
            if (cache.DeviceStatus.status == 0) {
                Toast.makeText(this, "设备工作已经停止!", Toast.LENGTH_SHORT).show();
                binding.startButton.setText("开始工作");
                binding.tvStatus.setText("已停止");
                binding.tvDataManage.setEnabled(true);
            } else {
                if (cache.GetStopWork() == 0) {
                    Toast.makeText(this, "设备工作停止成功!", Toast.LENGTH_SHORT).show();
                    binding.startButton.setText("开始工作");
                    binding.tvStatus.setText("已停止");
                    binding.tvDataManage.setEnabled(true);
                } else {
                    Toast.makeText(this, "停止工作请求失败!", Toast.LENGTH_SHORT).show();
                    binding.startButton.setText("开始工作");
                    binding.startButton.setEnabled(false);
                    binding.tvDataManage.setEnabled(false);
                }
            }
        } else {
            Toast.makeText(this, "设备连接失败,无法请求!", Toast.LENGTH_SHORT).show();
            binding.startButton.setText("开始工作");
            binding.startButton.setEnabled(false);
            binding.tvDataManage.setEnabled(false);
            binding.tvStatus.setText("已停止");
        }
        RefreshStatus();
    }
}
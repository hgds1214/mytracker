package com.zeus.tec.ui.leida;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ToastUtils;
import com.zeus.tec.R;
import com.zeus.tec.databinding.ActivityLeidaDataCollectBinding;
import com.zeus.tec.db.TrackerDBManager;
import com.zeus.tec.model.leida.leidaPointRecordInfo;
import com.zeus.tec.model.leida.leida_info;
import com.zeus.tec.model.leida.main.FileBean;
import com.zeus.tec.model.leida.main.MainCache;
import com.zeus.tec.model.leida.sampleTest.DataCache;
import com.zeus.tec.ui.leida.Apater.PointListAdapter;
import com.zeus.tec.ui.leida.Apater.fileListAdapter;
import com.zeus.tec.ui.leida.interfaceUtil.AlarmCallback;
import com.zeus.tec.ui.leida.interfaceUtil.DialogCallback;
import com.zeus.tec.ui.leida.interfaceUtil.ILeidaDelectfile;
import com.zeus.tec.ui.leida.util.DecimalFormat1;
import com.zeus.tec.ui.leida.util.INIutil;
import com.zeus.tec.ui.leida.util.IOtool;
import com.zeus.tec.ui.leida.util.MesseagWindows;
import com.zeus.tec.ui.leida.util.MyApplicationContext;
import com.zeus.tec.ui.leida.util.MyTask;
import com.zeus.tec.model.utils.FeedbackUtil;
import com.zeus.tec.model.utils.log.SuperLogUtil;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class LeidaDataCollectActivity extends AppCompatActivity {

    ActivityLeidaDataCollectBinding binding;

    private SuperLogUtil superLogUtil;
    TextView startButton;
    TextView refreshButton;
    TextView programParamterButton;
    LinearLayout layParamter;
    MainCache cache;
    ListView pointListview;
    String publicPath = "";
    String privatePath = "";
    Context context1;

    private leida_info info1 = leida_info.GetInstance();

    private List<leidaPointRecordInfo> recordInfoList = new ArrayList<>();

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApplicationContext.initialize(LeidaDataCollectActivity.this);
        binding = ActivityLeidaDataCollectBinding.inflate(getLayoutInflater());
        context1 = MyApplicationContext.getInstance().getAppContext();
        setContentView(binding.getRoot());
        try {
            publicPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
            privatePath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        startButton = binding.startButton;
        refreshButton = binding.tvRefreshStatus;
        layParamter = binding.layProgramParamter;
        programParamterButton = binding.tvProgramParamter;
        cache = MainCache.GetInstance();
        cache.FileSavePath = privatePath;
        LoadLastProject();
        cache.CreatReceiveThread(LeidaDataCollectActivity.this);

        cache.CreatSendSocket();
        Thread th = new Thread(this::RefreshStatus);
        th.start();
        //cache.GetDeviceStatus();
        initListener();
        //gethotspotDevice();
    }

    public AlarmCallback alarmCallback = new AlarmCallback() {
        @Override
        public void showAlarm() {
            binding.llLowPowerTip.setVisibility(View.VISIBLE);
        }

        @Override
        public void gongAlarm() {
            binding.llLowPowerTip.setVisibility(View.GONE);
        }
    };

    private void initListener() {
        step_1();
        long currentTime = System.currentTimeMillis();
        binding.tvSetDevice.setOnClickListener(v -> setDeviveClick());
        binding.tvCountTime.setStartTime(currentTime);
        binding.tvCountTime.setAlarmCallback(alarmCallback);
        binding.tvCountTime.start();
        binding.tvPointRecord.setOnClickListener(v -> tvPointRecord_click());
        programParamterButton.setOnClickListener(V -> programParamterButton_click());
        startButton.setOnClickListener(v -> startButton_click());
        refreshButton.setOnClickListener(v -> refreshButton_click());
        binding.tvBuildProgram.setOnClickListener(v -> BuildProgram());
        binding.tvPoint.setOnClickListener(v -> tvPoint_Click());
        binding.tvDataDownload.setOnClickListener(v -> tvDataDownload_click());
        binding.tvDataManage.setOnClickListener(v -> tvDataManage_click());
        binding.tvRefresh.setOnClickListener(v -> tvRefresh_click());
        binding.tvExit.setOnClickListener(v -> tvExit_click());
        binding.tvDownload.setOnClickListener(v -> tvDownload_click());
        binding.tvDelete.setOnClickListener(v -> tvDelete_click());
        binding.ivBack.setOnClickListener(V -> ivBackClick());
    }

    private void setDeviveClick() {
        FeedbackUtil.getInstance().doFeedback();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String filePath = cache.FileSavePath + "/" + "sys.properties";
        View view2 = View.inflate(LeidaDataCollectActivity.this, R.layout.set_device_ip, null);
        final EditText deviceIP = (EditText) view2.findViewById(R.id.et_device_ip);
        deviceIP.setText( INIutil.readINI(filePath,"server_ip","192.168.43.100"));
        final Button btn = (Button) view2.findViewById(R.id.btn_sure);
        builder.setTitle("IP").setIcon(R.mipmap.ic_launcher).setView(view2).setInverseBackgroundForced(false);
        AlertDialog dialog = builder.create();
        dialog.show();
        btn.setOnClickListener(v -> {
            FeedbackUtil.getInstance().doFeedback();
            cache.server_ip =  deviceIP.getText().toString();
           DataCache datacache =  DataCache.GetInstance();
           datacache.server_ip = deviceIP.getText().toString();
            Properties properties = new Properties();
            properties.setProperty("server_ip",cache.server_ip);
            INIutil.writeproperties(properties,filePath);
            ToastUtils.showLong("IP:"+cache.server_ip+"设置成功");
        });
    }

    private void ivBackClick() {
        FeedbackUtil.getInstance().doFeedback();
        try {
            //  cache.CloseReceiveThread();
            cache.closeSendSocket();
            finish();
        } catch (Exception ex) {
            ToastUtils.showLong(ex.getLocalizedMessage());
        }
    }

    public void startButton_click() {
        FeedbackUtil.getInstance().doFeedback();
        if (cache.DeviceStatus!=null){
            if (cache.DeviceStatus.status == 0) {
                if (cache.newProject == 0) {
                    ToastUtils.showLong("请先新建项目信息和设备参数");
                    return;
                }
                // Toast.makeText(LeidaDataCollectActivity.this, "正在连接设备", Toast.LENGTH_SHORT).show();
                StartWork();
            } else if (cache.DeviceStatus.status == 1) {
                //  Toast.makeText(LeidaDataCollectActivity.this, "正在停止采集", Toast.LENGTH_SHORT).show();
                StopWork();
            }
        }
    }

    public void tvPointRecord_click() {
        FeedbackUtil.getInstance().doFeedback();
        if (layParamter.getVisibility() == View.VISIBLE) {
            //  layTitle.setVisibility(View.GONE);
            layParamter.setVisibility(View.GONE);
            binding.layoutPointRecord.setVisibility(View.VISIBLE);
            binding.tvPointRecord.setBackgroundResource(R.drawable.btn_collect_bg);
            binding.tvPointRecord.setTextColor(Color.WHITE);
            programParamterButton.setBackgroundResource(R.drawable.btn_finish_bg);
            programParamterButton.setTextColor(Color.BLACK);
        }
    }

    public void programParamterButton_click() {
        FeedbackUtil.getInstance().doFeedback();
        if (layParamter.getVisibility() == View.GONE) {
            //  layTitle.setVisibility(View.VISIBLE);
            layParamter.setVisibility(View.VISIBLE);
            binding.layoutPointRecord.setVisibility(View.GONE);
            programParamterButton.setBackgroundResource(R.drawable.btn_collect_bg);
            programParamterButton.setTextColor(Color.WHITE);
            binding.tvPointRecord.setBackgroundResource(R.drawable.btn_finish_bg);
            binding.tvPointRecord.setTextColor(Color.BLACK);
        }
    }

    public void tvDelete_click() {
        FeedbackUtil.getInstance().doFeedback();
        // DialogResult ok = MessageBox.Show("该项目文件删除后将无法回复!", "是否删除", MessageBoxButtons.YesNo, MessageBoxIcon.Question);
        MesseagWindows.showMessageBox(this, "是否删除", "该项目文件删除后将无法恢复", new DialogCallback() {
            @Override
            public void onPositiveButtonClick() {
                if (cache.selectFileName.equals("") ) {
                    Context context = MyApplicationContext.getInstance().getAppContext();
                    Toast.makeText(context, "请选择需要删除的项目", Toast.LENGTH_LONG).show();
                    return;
                }
                // STMainCache cache = STMainCache.GetInstance();
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
    }

    public void tvDownload_click() {
        FeedbackUtil.getInstance().doFeedback();
        try {
            if (cache.selectFileName.equals("")) {
                //MessageBox.Show("请选择需要下载的项目!");
                Toast.makeText(this, "请选择需要下载的项目", Toast.LENGTH_LONG).show();
                return;
            }
            if
            (cache.selectFileName.equals(info1.projectId)) {
                ProgressBar progressBar = binding.progressBar;
                progressBar.setVisibility(View.VISIBLE);
                binding.tvStep1Text.setText("正在下载");
                TextView steptext = binding.tvStep1Text;
                MyTask task = new MyTask(progressBar, cache.selectFileName, steptext, 1, delectfile);
                task.execute();
            } else {
                MesseagWindows.showMessageBox(LeidaDataCollectActivity.this, "是否下载", "该数据不是本次检测的数据,是否继续下载", new DialogCallback() {
                    @Override
                    public void onPositiveButtonClick() {
                        List<leida_info> selectLeidaInfo = TrackerDBManager.isHaveData(cache.selectFileName);
                        if (selectLeidaInfo.size() == 0) {
                            MesseagWindows.showMessageBox(LeidaDataCollectActivity.this, "是否继续下载", "该数据项目记录不存在，如果下载将放入临时文件夹，是否继续下载", new DialogCallback() {
                                @Override
                                public void onPositiveButtonClick() {
                                    ProgressBar progressBar = binding.progressBar;
                                    progressBar.setVisibility(View.VISIBLE);
                                    binding.tvStep1Text.setText("正在下载");
                                    TextView steptext = binding.tvStep1Text;
                                    MyTask task = new MyTask(progressBar, cache.selectFileName, steptext, 2, delectfile);
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

    public void tvExit_click() {
        FeedbackUtil.getInstance().doFeedback();
        binding.layDownloadWindows.setVisibility(View.GONE);
    }

    public void tvRefresh_click() {
        FeedbackUtil.getInstance().doFeedback();
        GetFiles();
    }

    public void refreshButton_click() {
        FeedbackUtil.getInstance().doFeedback();
        @SuppressLint("ResourceAsColor") Thread th = new Thread(() -> {
            RefreshStatus();
            ToastUtils toastUtils = ToastUtils.make();
            toastUtils.setBgColor(R.color.design_default_color_background);
            toastUtils.setLeftIcon(R.mipmap.logo);
            toastUtils.setTextColor(R.color.program_text_color);
            toastUtils.show("刷新成功");
        });
        th.start();
    }

    public void tvDataManage_click() {
        FeedbackUtil.getInstance().doFeedback();
        if (binding.layDownloadWindows.getVisibility() == View.GONE) {
            GetFiles();
            binding.layDownloadWindows.setVisibility(View.VISIBLE);
        }
    }

    int index = 10;

    public void GetFiles() {
        List<FileBean> files;
        files = cache.GetFilesName();
        if (files != null) {
            files.add(new FileBean());
            fileListAdapter adapter = new fileListAdapter(LeidaDataCollectActivity.this, files);
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

    private void tvDataDownload_click() {
        try {
            FeedbackUtil.getInstance().doFeedback();
            ProgressBar progressBar = binding.progressBar;
            binding.tvStep1Text.setText("正在下载");
            progressBar.setVisibility(View.VISIBLE);
            TextView stepview = binding.tvStep1Text;
            MyTask task = new MyTask(progressBar, info1.projectId, stepview, 0, delectfile);
            task.execute();
        } catch (Exception exception) {
            ToastUtils.showLong(exception.getLocalizedMessage());
        }
    }

    private void initPointList(List<leidaPointRecordInfo> pointParamters) {
        PointListAdapter adapter = new PointListAdapter(LeidaDataCollectActivity.this, pointParamters);
        pointListview = binding.listPoint;
        pointListview.setAdapter(adapter);
        pointListview.setSelection(adapter.getCount() - 1);
    }

    private boolean isignore = false;
    private void tvPoint_Click() {
        FeedbackUtil.getInstance().doFeedback();
        if (cache.DeviceStatus!=null){
            if (cache.DeviceStatus.status==1){
                pointRecord();
            }
            else{
                if (isignore){
                    pointRecord();
                }
                else {
                    MesseagWindows.showMessageBox((Context) this, "提醒", "当前状态为未采集，是否继续打点", new DialogCallback() {
                        @Override
                        public void onPositiveButtonClick() {
                            pointRecord();
                            isignore=true;
                        }
                        @Override
                        public void onNegativeButtonClick() {
                        }
                    });
                }
            }
        }
        else {
            if (!isignore){
                MesseagWindows.showMessageBox((Context) this, "提醒", "当前状态为未采集，是否继续打点", new DialogCallback() {
                    @Override
                    public void onPositiveButtonClick() {
                        pointRecord();
                        isignore=true;
                    }
                    @Override
                    public void onNegativeButtonClick() {
                    }
                });
            }
            else {
                pointRecord();
            }
        }
    }

    private void pointRecord(){
        try {
            if (IOtool.isFileExists(info1.dataPath)) {
                String current_time = "";
                String content;
                LocalDateTime now;
                long timecode = System.currentTimeMillis();
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
                info1.TotalDis = info1.TotalDis + info1.drillPipeLength;
                content = current_time + '\t' + timecode + '\t' + info1.drillPipeLength + '\t' + info1.TotalDis;
                IOtool.saveText(info1.dataPath, "\n", true);
                IOtool.saveText(info1.dataPath, content, true);
                info1.PointCount++;
                //   cache.pointList.add(new PointParamter(String.valueOf(info1.PointCount), current_time, String.valueOf(info1.TotalDis)));
                leidaPointRecordInfo leidaPointRecordInfo = new leidaPointRecordInfo();
                leidaPointRecordInfo.leidaInfoId = info1.id;
                leidaPointRecordInfo.PointNumber = String.valueOf(info1.PointCount);
                leidaPointRecordInfo.recordTime = current_time;
                leidaPointRecordInfo.distance = String.valueOf(info1.TotalDis/100);
                TrackerDBManager.saveOrUpdate(info1);
                TrackerDBManager.saveOrUpdate(leidaPointRecordInfo);
                initPointList(TrackerDBManager.getrecordByleidaInfoId(info1.id));
                binding.tv24.setText(String.valueOf(info1.PointCount));
                // binding.tv23.setText(DecimalFormat1.getdecimalFormat(info1.TotalDis/100, 1));
                binding.tv23.setText(String.format("%.1f", info1.TotalDis/100f));
            } else {
                Toast.makeText(this, "项目测点文件不存在，无法打点测试！", Toast.LENGTH_LONG).show();
            }
        }
        catch (Exception exception){
            ToastUtils.showLong(exception.getMessage());
        }
    }

    private void step_1() {
        binding.ivStep1.setState(1);
    }

    private void RefreshStatus() {
        cache.GetDeviceStatus();
        if (cache.DeviceStatus != null) {
            if (cache.DeviceStatus.status == 1) {
                ((Activity) context1).runOnUiThread(() -> {
                    binding.ivStep1.setState(1);
                    binding.tvStep1Text.setText("正在采集");
                    binding.tvDataManage.setEnabled(false);
                    binding.tvDataDownload.setEnabled(false);
                    binding.tvBuildProgram.setEnabled(false);
                    binding.startButton.setEnabled(true);
                    binding.startButton.setText("停止采集");
                });
            } else {
                ((Activity) context1).runOnUiThread(() -> {
                    binding.ivStep1.setState(2);
                    binding.tvStep1Text.setText("设备已连接,请开始采集");
                    binding.startButton.setEnabled(true);
                    float quantity = cache.DeviceStatus.quantity;
                    String tmp = String.valueOf(quantity).substring(0, 3) + "V";
                    binding.tv21.setText(tmp);
                    float gyro = cache.DeviceStatus.gyro;
                    String Gyro = String.valueOf(gyro);
                    if (Gyro.length() > 3) {
                        binding.tv22.setText(Gyro.substring(0, 4));
                    } else {
                        binding.tv22.setText(Gyro.substring(0, 3));
                    }
                    binding.startButton.setEnabled(true);
                    binding.tvDataManage.setEnabled(true);
                    binding.tvDataDownload.setEnabled(true);
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

    private void LoadLastProject() {
       // String ProductName = "";

        if (!cache.FileSavePath.equals("")) {
            info1 = TrackerDBManager.getLastLeidaInfo();
            leida_info.setInstance(info1);
            if (info1 != null) {
                recordInfoList = TrackerDBManager.getrecordByleidaInfoId(info1.id);
            }
            initPointList(recordInfoList);
            InitProjectParam();
            if (info1 != null) {
                try {
                    index = 0;
                   // binding.tv23.setText(DecimalFormat1.getdecimalFormat(info1.TotalDis, 1));
                    binding.tv23.setText(String.format("%.1f", info1.TotalDis/100f));
                    binding.tv24.setText(String.valueOf(info1.PointCount));
                }
                catch (Exception ignored)
                {
                }
            }
        }
    }

    private void InitProjectParam() {
        try {
            binding.tvProjectName.setText(info1.projectId);
            binding.tvCreatTime.setText(info1.creatTime);
            binding.tvPointDistance.setText(String.valueOf(info1.drillPipeLength/100));
            binding.tvPointNumber.setText(String.valueOf(info1.sampleLength));
            binding.tvOverLayNumber.setText(String.valueOf(info1.overlaynumbe));
            binding.tvDelay.setText(String.valueOf(info1.Delay1));
            binding.tvAmp11.setText(String.valueOf(info1.Amp1));
            binding.tvFrenquence1.setText(String.valueOf(info1.frequency));
            binding.tvTImeSpace.setText(String.valueOf(info1.timeSpace));
            binding.tvGyro.setText(String.valueOf(info1.GYROThreshold));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void BuildProgram() {
        FeedbackUtil.getInstance().doFeedback();
        MesseagWindows.showMessageBox((Context) this, "是否创建新项目", "是否放弃当前项目创建新项目", new DialogCallback() {
            @Override
            public void onPositiveButtonClick() {
                Intent intent = new Intent(LeidaDataCollectActivity.this, ProjectleidainfoActivity.class);
                startActivity(intent);
                //   finish();
            }

            @Override
            public void onNegativeButtonClick() {

            }
        });
    }

    private void StartWork() {

        RefreshStatus();
        if (cache.DeviceStatus != null) {
            if (cache.DeviceStatus.status == 1) {
                Toast.makeText(LeidaDataCollectActivity.this, "设备正在工作中，无法请求！", Toast.LENGTH_SHORT).show();
                binding.tvDataDownload.setEnabled(false);
                binding.tvDataManage.setEnabled(false);
                binding.tvStatus.setText("正在运行");
                binding.tvPoint.setEnabled(true);
                binding.startButton.setText("停止采集");
            } else {
                if (cache.GetStartWork() == 1) {
                    Toast.makeText(LeidaDataCollectActivity.this, "设备开始工作！", Toast.LENGTH_SHORT).show();
                    binding.startButton.setText("停止采集");
                    binding.tvPoint.setEnabled(true);
                    binding.tvStatus.setText("正在运行");
                    binding.tvDataDownload.setEnabled(false);
                    binding.tvDataManage.setEnabled(false);
                } else {
                    Toast.makeText(LeidaDataCollectActivity.this, "开始工作请求失败！", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(LeidaDataCollectActivity.this, "设备连接失败,无法请求!", Toast.LENGTH_SHORT).show();
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
                    binding.tvDataDownload.setEnabled(true);
                    binding.tvDataManage.setEnabled(true);
                } else {
                    Toast.makeText(this, "停止工作请求失败!", Toast.LENGTH_SHORT).show();
                    binding.startButton.setText("开始工作");
                    binding.startButton.setEnabled(false);
                    binding.tvDataDownload.setEnabled(false);
                    binding.tvDataManage.setEnabled(false);
                }
            }
        } else {
            Toast.makeText(this, "设备连接失败,无法请求!", Toast.LENGTH_SHORT).show();
            binding.startButton.setText("开始工作");
            binding.startButton.setEnabled(false);
            binding.tvDataDownload.setEnabled(false);
            binding.tvDataManage.setEnabled(false);
            binding.tvStatus.setText("已停止");
        }
        RefreshStatus();
    }

    ILeidaDelectfile delectfile = new ILeidaDelectfile() {
        @Override
        public void onDelectFile(leida_info leidaInfo) {
            MesseagWindows.showMessageBox(LeidaDataCollectActivity.this, "是否删除", "文件已成功下载到本地，是否删除远端原始文件", new DialogCallback() {
                @Override
                public void onPositiveButtonClick() {
                    int result = cache.GetDeleteFile(leidaInfo.projectId);
                    if (result == 1) {
                        ToastUtils.showLong("远端原始文件删除成功");
                    } else {
                        ToastUtils.showLong("远端原始文件删除失败");
                    }
                }
                @Override
                public void onNegativeButtonClick() {
                }
            });
        }
    };

    //震动
    public void onVibrator() {
        Vibrator vibrator = (Vibrator) this.getSystemService(this.VIBRATOR_SERVICE);
        vibrator.vibrate(1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            cache.CloseReceiveThread();
            cache.closeSendSocket();
        } catch (Exception ex) {
            ToastUtils.showLong(ex.getLocalizedMessage());
        }
    }

    @Override
    public void onBackPressed() {
        try {
            cache.closeSendSocket();
            finish();
        } catch (Exception ex) {
            ToastUtils.showLong(ex.getLocalizedMessage());
        }

    }


}
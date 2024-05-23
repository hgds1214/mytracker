package com.zeus.tec.ui.leida;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import java.io.File;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.zeus.tec.R;
import com.zeus.tec.databinding.ActivityProjectleidainfoBinding;
import com.zeus.tec.db.TrackerDBManager;
import com.zeus.tec.model.leida.leida_info;
import com.zeus.tec.model.leida.main.MainCache;
import com.zeus.tec.ui.base.BaseActivity;
import com.zeus.tec.ui.leida.interfaceUtil.DialogCallback;
import com.zeus.tec.ui.leida.util.INIutil;
import com.zeus.tec.ui.leida.util.IOtool;
import com.zeus.tec.ui.leida.util.MesseagWindows;
import com.zeus.tec.ui.tracker.util.TextHelper;
import com.zeus.tec.model.utils.FeedbackUtil;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

public class ProjectleidainfoActivity extends BaseActivity implements DialogCallback {
    //region 全局变量
    private ActivityProjectleidainfoBinding binding;
    private  String [] starArray = {"512","1024","2048"};
    private  String [] frequencyArray = {"1000","2000","4000"};
    Bundle tmp;
    int sampleLength_index =1 ;
    int frequency_index =1;
    private leida_info info =  leida_info.GetInstance();
    //endregion
   // leidaAdapter leidaAdapter = new leidaAdapter(this,starArray,frequencyArray);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tmp = savedInstanceState;
      binding = ActivityProjectleidainfoBinding.inflate(getLayoutInflater());
      setContentView(binding.getRoot());
      Spinner sp_frequency = findViewById(R.id.sp_Frequency);
      Spinner sp_startAdapter = findViewById(R.id.sp_sampleLength);
        ArrayAdapter <String> startAdapter = new ArrayAdapter<String>(this, R.layout.item_select,starArray);
     ArrayAdapter <String> frequencyAdapter = new ArrayAdapter<String>(this, R.layout.item_select,frequencyArray);
        binding.ivBack.setOnClickListener( v->{
            FeedbackUtil.getInstance().doFeedback();
            finish();
        });
        sp_frequency.setAdapter(frequencyAdapter);
        sp_startAdapter.setAdapter(startAdapter);
        sp_frequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                frequency_index = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                frequency_index =0;
            }
        });

        sp_startAdapter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sampleLength_index = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                sampleLength_index = 0;
            }
        });

        //binding.spSampleLength.setAdapter(startAdapter);
       // binding.spSampleLength.setOnItemSelectedListener(this);
        //binding.spFrequency.setOnItemSelectedListener(this);
       // binding.spFrequency.setAdapter(frequencyAdapter);
        binding.tvNext.setOnClickListener( v-> {
            FeedbackUtil.getInstance().doFeedback();
            clickNext();
        });

        initUI();
        //setContentView(R.layout.activity_projectleidainfo);
    }


    //region 初始化UI
    private void  initUI (){
        binding.edtProject.setText(TextHelper.safeString(info.projectId));
       //binding.spSampleLength.setSelection(info.sampleLength);
       // binding.spFrequency.setSelection(info.frequency);
     //   binding.spSampleLength.setOnItemSelectedListener(this);
        binding.edtAmp.setText(TextHelper.safeString(""+info.Amp1));
        binding.edtDelay.setText(TextHelper.safeString(""+info.Delay1));
        binding.edtOverlaynumber.setText(TextHelper.safeString(""+info.overlaynumbe));
        binding.edtPipeLength.setText(TextHelper.safeString(""+info.drillPipeLength));
        binding.edtTimeSpace.setText(TextHelper.safeString(""+info.timeSpace));
        binding.tvGyRO2.setText(TextHelper.safeString(""+info.GYROThreshold));
    }
    //endregion

    //region 点击“下一步”进行参数填写判断和长期储存
    private void clickNext() {
        judgment();
    }

    public void judgment (){
        String project = binding.edtProject.getText().toString();
        if (TextHelper.isInvalidTextAndShowWarn(project, "项目编号")) {
            return;
        }
        String edtAmpstring = binding.edtAmp.getText().toString();
        if (TextUtils.isEmpty(edtAmpstring)) {
            ToastUtils.showLong("放大倍数不能为空");
            return;
        }
        int edtAmp =Integer.parseInt(edtAmpstring);
        String edtDelaystring = binding.edtDelay.getText().toString();
        if (TextUtils.isEmpty(edtDelaystring)) {
            ToastUtils.showLong("延迟点数不能为空");
            return;
        }
        int edtDelay = Integer.parseInt(edtDelaystring);
        String edtOverlaynumber = binding.edtOverlaynumber.getText().toString();
        if (TextUtils.isEmpty(edtOverlaynumber)) {
            ToastUtils.showLong("叠加次数不能为空");
            return;
        }
        int overLayNumber = Integer.parseInt(edtOverlaynumber);
        if (overLayNumber < 1 || overLayNumber > 1000) {
            ToastUtils.showLong("叠加次数在"+1000+"以内");
            return;
        }
        String pipeLengthStr = binding.edtPipeLength.getText().toString();
        if (TextUtils.isEmpty(pipeLengthStr)) {
            ToastUtils.showLong("打点距离(CM)不能为空");
            return;
        }
        float drillPipeLength = Float.parseFloat(pipeLengthStr);
        if (drillPipeLength < 1 || drillPipeLength > 1000) {
            ToastUtils.showLong("打点距离只能是1到"+1000+"之间");
            return;
        }
        String edtTimeSpace = binding.edtTimeSpace.getText().toString();
        if (TextUtils.isEmpty(edtTimeSpace)) {
            ToastUtils.showLong("时间间隔(ms)");
            return;
        }
        float timeSpace = Float.parseFloat(edtTimeSpace);
        if (timeSpace < 100 || timeSpace > 1000) {
            ToastUtils.showLong("时间间隔只能是100到"+1000+"之间");
            return;
        }
        String GYRO = binding.tvGyRO2.getText().toString();
        if (TextUtils.isEmpty(GYRO))
        {
            ToastUtils.showLong("陀螺阈值");
        }
        float GYRO1 = Float.parseFloat(GYRO);
        if (GYRO1<66){
            ToastUtils.showLong("陀螺阈值必须大于66！");
            return;
        }
        if (TrackerDBManager.isHaveData(project).size()==0)
        {

        }
        else {
            ToastUtils.showLong("该项目名字已存在，请重命名");
            return;
        }

        info.projectId = project;
        info.sampleLength = sampleLength[sampleLength_index];
        info.frequency = frequency[ frequency_index];
        info.Amp1 = edtAmp;
        info.Delay1 = edtDelay;
        info.overlaynumbe =overLayNumber;
        info.drillPipeLength = drillPipeLength;
        info.timeSpace = timeSpace;
        info.GYROThreshold = GYRO1;
        info.PointCount=0;
        info.TotalDis = 0;


        CreatProject_Click();
       // String publicPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
// 获取当前App的私有存储路径
    }
   public int [] sampleLength = {512,1024,2048};
   public int [] frequency = {1000,2000,4000};

    private void CreatProject_Click()
    {
       // DialogResult result = MessageBox.Show("是否放弃当前项目创建新项目", "是否创建新项目", MessageBoxButtons.YesNo, MessageBoxIcon.Question);
       // if (result == DialogResult.Yes)
        //MesseagWindows mw = new MesseagWindows("是否放弃当前项目创建新项目");
      //  Dialog dialog = mw.onCreateDialog(tmp);
       // dialog.show();
        String content = "";
        content = String.valueOf(info.drillPipeLength);
       String  CreatTime = "";
        MainCache cache = MainCache.GetInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
           LocalDateTime now = LocalDateTime.now();
            int year = now.getYear();
            int month = now.getMonthValue();
            int day = now.getDayOfMonth();
            int hour = now.getHour();
            int minute = now.getMinute();
            int second = now.getSecond();
            String tmp = String.valueOf(year);
            CreatTime += tmp;
            tmp = String.valueOf(month);
            if (month < 10)
                tmp = "0" + tmp;
            CreatTime += "-";
            CreatTime += tmp;
            tmp = String.valueOf(day);
            if (day < 10)
                tmp = "0" + tmp;
            CreatTime += "-";
            CreatTime += tmp;
            tmp = String.valueOf(hour);
            if (hour < 10)
                tmp = "0" + tmp;
            CreatTime += " ";
            CreatTime += tmp;
            tmp = String.valueOf(minute);
            if (minute < 10)
                tmp = "0" + tmp;
            CreatTime += ":";
            CreatTime += tmp;
            tmp = String.valueOf(second);
            if (second < 10)
                tmp = "0" + tmp;
            CreatTime += ":";
            CreatTime += tmp;
            content += '\t';
            content += CreatTime;
        }
        content += '\t';
        content += String.valueOf(info.sampleLength);
        content += '\t';
      //  content += StackCount.ToString();
        content += String.valueOf(info.overlaynumbe);
        content += '\t';
        //content += NbOfSampleDelayPoint.ToString();
        content += String.valueOf(info.Delay1);
        content += '\t';
       // content += Amplify.ToString("f1");
        content += String.valueOf(info.Amp1);
        content += '\t';
       // content += SampleFrequency.ToString();
        content += String.valueOf(info.frequency);
        content += '\t';
       // content += TimeInterval.ToString("f2");
        content += String.valueOf(info.timeSpace);
        content += '\t';
       // content += GYROThreshold.ToString("f2");
        content += String.valueOf(info.GYROThreshold);
        info.creatTime = CreatTime;
        int AmplifyValue = GetAmp(info.Amp1);

        int result = cache.GetSetting(info.projectId, info.sampleLength, info.overlaynumbe, info.Delay1, AmplifyValue, info.frequency, info.timeSpace, info.GYROThreshold);
       // if (result == 1)
        //测试
        if(true)
        {
            String rootPath = PathUtils.getExternalAppFilesPath()+ File.separator + "leidaData";
            if (!FileUtils.createOrExistsDir(rootPath)) {
                LogUtils.e("创建文件失败：" + rootPath);
                ToastUtils.showLong("创建文件失败，请检查磁盘空间后重试!");
                return ;
            }
            String path = rootPath +File.separator+ info.projectId + ".trd";
            String finalContent = content;
            if (IOtool.isFileExists(path))
            {
                MesseagWindows.showMessageBox(ProjectleidainfoActivity.this, "是否覆盖原项目", "该项目已存在，是否覆盖原项目", new DialogCallback() {

                    @Override
                    public void onPositiveButtonClick() {

                        saveData(path,finalContent,rootPath,cache);


                    }
                    @Override
                    public void onNegativeButtonClick() {
                        startActivity(new Intent(ProjectleidainfoActivity.this, LeidaDataCollectActivity.class));
                    }
                });
            }
            else
            {
                saveData(path,finalContent,rootPath,cache);
             //   FileStream fs = new FileStream(path, FileMode.Create, FileAccess.ReadWrite);
              //  StreamWriter sw = new StreamWriter(fs);
              //  IOtool.saveText(path,content);
                //sw.WriteLine(content);
               // sw.Close();
               // fs.Close();
               // TrackerDBManager.saveOrUpdate(info);
              //  String filePath = cache.FileSavePath +"/"+ "sys.properties";
                //InitUtil.Write("Local", "ProjectName", Name, filePath);
              //  cache.properties.setProperty("ProjectName",info.projectId);
               // INIutil.writeproperties(cache.properties,filePath);

                if (!cache.RefreshInitFile())
                {
                  //  MessageBox.Show("配置文件无法访问！");
                    Toast.makeText(this,"配置文件无法访问",Toast.LENGTH_SHORT);
                }
                Toast.makeText(this,"设备项目创建成功",Toast.LENGTH_SHORT);
                //this.DialogResult = DialogResult.OK;
               // this.Close();
               // startActivity(new Intent(ProjectleidainfoActivity.this, LeidaDataCollectActivity.class));
            }
        }
       else if (result ==-1){
           // Toast.makeText(this,"设备通讯数失败",Toast.LENGTH_SHORT);
            ToastUtils.showLong("设备通讯失败");
        }
    }

    private void  saveData (String path,String finalContent ,String rootPath,MainCache cache ){

        ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<leida_info>() {
            @Override
            public leida_info doInBackground() throws Throwable {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
//                                    String rootPath = PathUtils.getExternalAppFilesPath()+ File.separator + "leidaData";
//                                    if (!FileUtils.createOrExistsDir(rootPath)) {
//                                        LogUtils.e("创建文件失败：" + rootPath);
//                                        ToastUtils.showLong("创建文件失败，请检查磁盘空间后重试!");
//                                        return null;
//                                    }
                    String projectRoot = rootPath + File.separator + sdf.format(new Date(System.currentTimeMillis()));
                    if (!FileUtils.createOrExistsDir(projectRoot)) {
                        LogUtils.e("创建文件失败：" + projectRoot);
                        ToastUtils.showLong("创建文件失败，请检查磁盘空间后重试!");
                        return null;
                    }
                    String dataPath = projectRoot +File.separator +info.projectId+".trd";

                    IOtool.saveText(dataPath, finalContent);
                  //  TrackerDBManager.saveOrUpdate(info);
                    String filePath = cache.FileSavePath +"/"+ "sys.properties";
                    cache.properties.setProperty("ProjectName",info.projectId);
                    INIutil.writeproperties(cache.properties,filePath);

                    cache.RefreshInitFile();
                    //  String filePath = projectRoot + File.separator + "scene.png";
//                                    if (!ImageUtils.save(shotPicture, filePath, Bitmap.CompressFormat.PNG)) {
//                                        LogUtils.e("保存图片失败：" + filePath);
//                                        ToastUtils.showLong("保存图片失败，请重试!");
//                                        return null;
//                                    }
                    //info.holeX = x;
                    // info.holeY = y;
                    // info.holeZ = z;
                    //  info.jacketLength = jacketLength;
                    //  info.designDirection = designDirection;
                    //  info.designAngle = designAngle;
                    //   info.adjustMode = adjustMode;
                    //   info.livePhotos = filePath;
                    //   info.livePhotosMd5 = ConvertUtils.bytes2HexString(EncryptUtils.encryptMD5File(filePath));
                    info.projectRoot = projectRoot;
                    info.dataPath = dataPath;
                    String SharePath = PathUtils.getExternalAppFilesPath()+ File.separator + "leidaShareData";
                    File sharefile = new File(SharePath);
                    if (!sharefile.exists()) {
                        if (!FileUtils.createOrExistsDir(SharePath)) {
                            LogUtils.e("创建文件失败：" + SharePath);
                            ToastUtils.showLong("创建文件失败，请检查磁盘空间后重试!");
                            return null;
                        }
                    }
                    info.zipPath = SharePath+File.separator+info.projectId+".zip";
                    //  info.collectionDateTime = time;/*System.currentTimeMillis();*/
                    //固定的保存路径，之后的打点文件会按照该路径保存，
                    //   info.dataPath = projectRoot+File.separator+info.projectName+"data.csv";
                    //  info.zipPath = projectRoot +File.separator+info.projectName+"zip.zip";
                    //进行数据和数据文件的保存
                    info.id = 0;
                    info.id = TrackerDBManager.saveOrUpdate(info);
                    cache.newProject +=1;
                    return info;
                }catch (Exception e) {
                    e.printStackTrace();
                    ToastUtils.showLong("保存信息失败，请重试!");
                }
                return null;
            }
            @Override
            public void onSuccess(leida_info result) {
                hideLoading();
                if (result != null) {
                    //  directionfinderDataCollectActivity.launch(directionDrillInfoActivity.this, result.id);
                    //   ActivityUtils.finishActivity(directionProjectInfoActivity.class);
                    ActivityUtils.finishActivity(LeidaDataCollectActivity.class);
                    startActivity(new Intent(ProjectleidainfoActivity.this, LeidaDataCollectActivity.class));
                    finish();
                }
            }
        });
    }

    private int GetAmp(float amp)
    {
        int result = 0;
        result = (int)(amp * 2.0);
        return result;
    }
    @Override
    public void onPositiveButtonClick() {

    }

    @Override
    public void onNegativeButtonClick() {

    }


    //endregion



}
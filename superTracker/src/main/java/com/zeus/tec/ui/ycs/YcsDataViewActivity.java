package com.zeus.tec.ui.ycs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnLoadMoreListener;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.zeus.tec.R;
import com.zeus.tec.databinding.ActivityYcsDataViewBinding;
import com.zeus.tec.databinding.ActivityYcsMainBinding;
import com.zeus.tec.model.leida.leida_info;
import com.zeus.tec.model.leida.sampleTest.DataCache;
import com.zeus.tec.model.utils.FeedbackUtil;
import com.zeus.tec.model.ycs.YcsMainCache;
import com.zeus.tec.ui.leida.interfaceUtil.DialogCallback;
import com.zeus.tec.ui.leida.util.IOtool;
import com.zeus.tec.ui.leida.util.MesseagWindows;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class YcsDataViewActivity extends AppCompatActivity {

    ActivityYcsDataViewBinding binding;
    YcsDataListAdapter ycsDataListAdapter = new YcsDataListAdapter();
    private boolean hasMore = true;
    private int pageNum = 0;

    String tmpFoldPath = "";
    String tmpFileName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityYcsDataViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.ivBack.setOnClickListener(view -> {
            FeedbackUtil.getInstance().doFeedback();
            finish();
        });
        RecyclerView rv = binding.rvList;
        rv.setLayoutManager(new LinearLayoutManager(this));
        ycsDataListAdapter.addChildClickViewIds(R.id.tv_share,R.id.tv_share, R.id.tv_merge,R.id.tv_delete);
        ycsDataListAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            FeedbackUtil.getInstance().doFeedback();
            switch (view.getId()) {
                case R.id.tv_view:
                case R.id.tv_merge: {
                    doMerge(ycsDataListAdapter.getItem(position));
                    break;
                }
                case R.id.tv_share:
                {
                    doShare(ycsDataListAdapter.getItem(position));
                    break;
                }
                case R.id.tv_delete: {
                    doDelete(ycsDataListAdapter.getItem(position));
                    break;
                }
            }
        });
        rv.setAdapter(ycsDataListAdapter);
        ycsDataListAdapter.getLoadMoreModule().setEnableLoadMoreIfNotFullPage(false);
        ycsDataListAdapter.getLoadMoreModule().setAutoLoadMore(true);
        ycsDataListAdapter.getLoadMoreModule().setEnableLoadMore(true);
        ycsDataListAdapter.getLoadMoreModule().setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (hasMore) {
                    loadData(pageNum + 1);
                }
            }
        });
        initListener();
        initLineChart(binding.ycsSampleChart);
        refreshDataList();
    }

    private void initListener(){
        binding.xSwith.setOnCheckedChangeListener((buttonView, isChecked) -> {
            swithStatus[0]=isChecked;
            iPointList.clickOn(currentPosition);
        });
        binding.ySwith.setOnCheckedChangeListener((buttonView, isChecked) -> {
            swithStatus[1]=isChecked;
            iPointList.clickOn(currentPosition);
        });
        binding.zSwith.setOnCheckedChangeListener((buttonView, isChecked) -> {
            swithStatus[2]=isChecked;
            iPointList.clickOn(currentPosition);
        });

        binding.mergeDataBtn.setOnClickListener(v -> {

            if (!(DataList.size()>0)){
                ToastUtils.showLong("探头数据为0");
                return;
            }
            if (!(pointBeanList.size()>0)){
                ToastUtils.showLong("打点数据为0");
                return;
            }
           TemList = mergeData(DataList , pointBeanList);
            if (!(TemList.size()>0)){
                ToastUtils.showLong("合并失败，因为数据匹配数为0！");
                return;
            }
                try {
                    Save_Click(tmpFileName,tmpFoldPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
        });
    }

    private void doMerge(YcsDataFileInfo info) {
        try {
            initMergeShowLy();
            tmpFileName = info.projectName;
            tmpFoldPath = info.filePath;
            readDataFIle(info.datPath);
            InitPointList (info.trdPath);
            initPointListView(DataList);
            iPointList.clickOn(currentPosition);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    List<TemBean> TemList = new ArrayList<>();

    int[] times = { 1, 17, 33, 49, 65, 81, 97, 113, 129, 161, 193, 225, 257, 289, 321, 353, 385, 449, 513, 577, 641, 705, 769, 833, 897, 1025, 1153, 1281, 1409, 1537, 1665, 1793, 1921, 2177, 2433, 2689, 2945, 3201, 3457, 3713, 3969, 4481, 4993, 5505, 6017, 6529, 7041, 7553, 8065, 9089, 10113, 11137, 12161, 13185, 14209, 15233, 16257, 18305, 20353, 22401, 24449, 26497, 28545, 30593, 32641, 36737, 40833, 44929, 49025, 53121, 57217, 61313, 65409, 73601, 81793, 89985, 98177 };

    private void Save_Click(String FileName, String FoldPath ) throws IOException {

        if (TemList.size() > 0)
        {
            DataBean bean = TemList.get(0).Data;
            List<FileOutputStream> writerlist = new ArrayList<>();
            List<String> contentlist = new ArrayList<>();
            int directNum = bean.Directs;
            try
            {
                for (int i = 0; i < directNum; i++)
                {
                    String TemFIle = FoldPath+File.separator+FileName + "-"+i+".ycs";
                    if (FileUtils.isFileExists(TemFIle)){
                        FileUtils.delete(TemFIle);
                    }
                    FileUtils.createOrExistsFile(TemFIle);
                    FileOutputStream  writer = new FileOutputStream(TemFIle,true);
                    writerlist.add(writer);
                    contentlist.add("");
                }
                String content = "日期";
                content += '\t';
                content += "时间";
                content += '\t';
                content += "操作员";
                content += '\t';
                content += "工区号";
                content += '\t';
                content += "测线";
                content += '\t';
                content += "测点";
                content += '\t';
                content += "装置类型";
                content += '\t';
                content += "叠加次数";
                content += '\t';
                content += "电流脉宽";
                content += '\t';
                content += "发射线圈面积";
                content += '\t';
                content += "接收线圈面积";
                content += '\t';
                content += "发射电压";
                content += '\t';
                content += "发射电流";
                content += '\t';
                content += "发射线圈电阻";
                content += '\t';
                content += "接收线圈电阻";
                content += '\t';
                content += "时间序列类型";
                content += '\t';
                content += "时间窗口个数";
                content += '\t';
                content += "起始窗口";
                content += '\t';
                content += "终止窗口";

                content += '\t';
                content += "滚动角";
                content += '\t';
                content += "俯仰角";
                content += '\t';
                content += "方位角";

                int totaltime = (int)(setting.SampleTimes * 1000);
                int index = 0;
                for (int i = 0; i < times.length; i++)
                {
                    if (times[i] > totaltime)
                    {
                        break;
                    }
                    else
                    {
                        index = i;
                    }
                }
                for (int i = 0; i <= index; i++)
                {
                    content += '\t';
                    content +=String.valueOf(times[i]);
                }
                content += System.getProperty("line.separator");
                for (int i=0;i<directNum;i++)
                {
                    writerlist.get(i).write(content.getBytes(StandardCharsets.UTF_8));
                }
                int No = 0;
                for (int i = 0; i < TemList.size(); i++)
                {
                    TemBean tem = TemList.get(i);
                    if(tem.Point==null||tem.Data==null){
                        if (i>0){
                            tem = TemList.get(i-1);
                        }
                    }
                    String time = tem.Point.time;
                    String year = time.substring(0, 4);
                    String month = time.substring(5, 7);
                    String day = time.substring(8, 10);
                    String time2 = time.substring(11, 19);
                    content = day + "." + month + "," + year + '\t';
                    content += time2 + '\t';
                    String str = "01";
                    content = content + str + '\t';
                    str = "whcs";
                    content = content + str + '\t';
                    content = content + "1" + '\t';
                    content = content + No + '\t';
                    content = content + "0" + '\t';

                    int a = 1;
                    str =String.valueOf(setting.StackCount);
                    content = content + str + '\t';

                    content = content + "1" + '\t';

                    str =  String.format("%.6f", setting.TArea);
                    content = content + str + '\t';
                    for (int j = 0; j < directNum; j++)
                    {
                        contentlist.set(j, content);
                        if(j==0)
                        {
                            str =  String.format("%.6f", setting.RAreaX);
                            contentlist.set(j, contentlist.get(j) + str + '\t');
                        }
                        else if(j==1)
                        {
                            str =  String.format("%.6f", setting.RAreaY);
                            contentlist.set(j, contentlist.get(j) + str + '\t');
                        }
                        else if (j == 2)
                        {
                            str =  String.format("%.6f", setting.RAreaZ);
                            contentlist.set(j, contentlist.get(j) + str + '\t');
                        }
                    }

                    content = "11.531249" + '\t';

                    content = content + "5.428950" + '\t';

                    content = content + "2.746996" + '\t';

                    content = content + "12.968582" + '\t';

                    content = content + "1" + '\t';

                    content = content + (index+1) + '\t';

                    content = content + "1" + '\t';

                    content = content + times[index] + '\t';

                    double value = 0;
                    for (int j = 0; j < directNum; j++)
                    {
                        contentlist.set(j, contentlist.get(j) + content);

                        value = tem.Data.Samples.get(j).Roll;
                        contentlist.set(j, contentlist.get(j) +  String.format("%.3f", value) );
                        contentlist.set(j, contentlist.get(j) + '\t');

                        value = tem.Data.Samples.get(j).Pitch;
                        contentlist.set(j, contentlist.get(j) + String.format("%.3f", value));
                        contentlist.set(j, contentlist.get(j) + '\t');

                        value = tem.Data.Samples.get(j).Heading;
                        contentlist.set(j, contentlist.get(j) + String.format("%.3f", value));
                        contentlist.set(j, contentlist.get(j) + '\t');
                    }
                    for (int j = 0; j < directNum; j++)
                    {
                        double[] sample= tem.Data.Samples.get(j).Result;
                        for (int t = 0; t <= index; t++)
                        {
                            float ts = times[t] * 1.0f;
                            int pindex = (int)(ts / setting.SampleIntervel);

                            value = GetValue(pindex + 1, sample);

                            str = String.format("%.6f", value);
                            if (t == index)
                            {
                                contentlist.set(j, contentlist.get(j) + str);
                            }
                            else
                            {
                                contentlist.set(j, contentlist.get(j) + str + '\t');
                            }
                        }
                    }
                    for (int j = 0; j < directNum; j++)
                    {

                        writerlist.get(j).write((contentlist.get(j)+System.getProperty("line.separator")).getBytes(StandardCharsets.UTF_8));
                    }
                    No++;
                }

                ToastUtils.showShort("合并数据保存成功!");
            }
            catch (Exception ex)
            {
                ToastUtils.showLong("合并数据出错：" +ex.getMessage());
            }
            finally
            {
                for (int i = 0; i < directNum; i++)
                {
                    if (writerlist.get(i) != null)
                        writerlist.get(i).close();
                }
                writerlist.clear();
                contentlist.clear();
            }
        }
    }

    private double GetValue(int index, double[] Sample)
    {
        double result = 0;
        if (index >= 0 && index < Sample.length)
        {
            result = Sample[index];
        }

        return result;
    }

    public static void zipFiles1(String zipFilePath, String... sourceFilePaths) {
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(fos))) {
            byte[] buffer = new byte[1024];
            for (String sourceFilePath : sourceFilePaths) {
                File fileToZip = new File(sourceFilePath);
                try (FileInputStream fis = new FileInputStream(fileToZip);
                     BufferedInputStream bis = new BufferedInputStream(fis)) {
                    zipOut.putNextEntry(new ZipEntry(fileToZip.getName()));
                    int bytesRead;
                    while ((bytesRead = bis.read(buffer)) != -1) {
                        zipOut.write(buffer, 0, bytesRead);
                    }
                    zipOut.closeEntry();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doShare(YcsDataFileInfo info) {
        if (info.zipPath.equals("")) {
            info.zipPath = info.trdPath.replace(".trd", ".zip");
        }
        File f = new File(info.zipPath);
        if (!f.exists()) {
            List<File> fileList = FileUtils.listFilesInDir(info.filePath);

            String[] filepathlist = new String[fileList.size()];
            if (fileList.size() > 0) {
                for (int i = 0; i < fileList.size(); i++) {
                    filepathlist[i] = fileList.get(i).getAbsolutePath();
                }
                zipFiles1(info.zipPath, filepathlist);
            } else {
                ToastUtils.showLong("数据分享错误，数据文件不存在");
            }
        } else {
            f.delete();
            List<File> fileList = FileUtils.listFilesInDir(info.filePath);
            String[] filepathlist = new String[fileList.size()];
            if (fileList.size() > 0) {
                for (int i = 0; i < fileList.size(); i++) {
                    filepathlist[i] = fileList.get(i).getAbsolutePath();
                }
                zipFiles1(info.zipPath, filepathlist);
            } else {
                ToastUtils.showLong("数据分享错误，数据文件不存在");
            }
        }
        if (info.zipPath == null || info.zipPath.isEmpty()) {
            ToastUtils.showLong("数据分享错误，压缩文件不存在");
            return;
        }
        if (!f.exists()) {
            ToastUtils.showLong("数据分享错误，压缩文件不存在");
            return;
        }
        String rootPath = PathUtils.getExternalAppFilesPath() + File.separator + "shareData";
        if (!FileUtils.createOrExistsDir(rootPath)) {
            LogUtils.e("创建文件失败：" + rootPath);
            ToastUtils.showLong("创建文件失败，请检查磁盘空间后重试!");
            return;
        }
        String targetFilePath = rootPath + File.separator + f.getName();
        File tf = new File(targetFilePath);
        if (tf.exists()) tf.delete();

        if (!FileUtils.copy(f, tf)) {
            ToastUtils.showLong("文件拷贝失败，请重试!");
            return;
        }
        f = tf;
        try {
            Uri uri;
            if (Build.VERSION.SDK_INT >= 24) {

                uri = FileProvider.getUriForFile(this, "com.zeus.tec.fileprovider", f);
                grantUriPermission(getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                LogUtils.e(uri);

            } else {
                uri = Uri.fromFile(f);
            }
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setType("application/octet-stream");

            startActivity(Intent.createChooser(intent, "分享到"));
        } catch (Exception ex) {
            ToastUtils.showLong(ex.getLocalizedMessage());
        }
    }

    private void doView() {
    }

    private void doDelete(YcsDataFileInfo info) {
        MesseagWindows.showMessageBox(this, "删除数据", "数据删除后不可恢复", new DialogCallback() {
            @Override
            public void onPositiveButtonClick() {
                if (FileUtils.isFileExists(info.filePath)){
                   FileUtils.delete(info.filePath);
                }
            }

            @Override
            public void onNegativeButtonClick() {

            }
        });
    }

    public class DataBean {
        public long Time;
        public int Directs;
        public List<ProbeBean> Samples = new ArrayList<>();

    }

    public class ProbeBean {
        public long Time;
        public float Heading;
        public float Pitch;
        public float Roll;
        public float Voltage;
        public float Current;
        public int Direct;
        public int PickPointNum;
        public float SampleIntervel;
        public int SampleLength;
        public double[] Sample;
        public double[] Result;

    }

    private long GetTime(byte[] Times) {
        long timestampMillis = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDateTime dateTime = LocalDateTime.of(2000 + (int) Times[0], (int) Times[1], (int) Times[2], (int) Times[3], (int) Times[4], (int) Times[5]);
            // 指定时区，例如使用系统默认时区
            ZoneId zoneId = ZoneId.systemDefault();
            ZonedDateTime zonedDateTime = dateTime.atZone(zoneId);
            timestampMillis = zonedDateTime.toInstant().getEpochSecond();
        }
        return timestampMillis;
    }

    private float readSingle(BufferedInputStream bis, byte[] angle) throws IOException {
        bis.read(angle);
        return ByteBuffer.wrap(angle).order(ByteOrder.LITTLE_ENDIAN).getFloat();
    }

    private short readShort(BufferedInputStream bis, byte[] buf) throws IOException {
        bis.read(buf);
        return ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    public class TestSetting {
        public float RAreaX;
        public float RAreaY;
        public float RAreaZ;
        public float TArea;
        public float MSpacing;
        public float SampleIntervel;
        public int SampleCount;
        public int StackCount;
        public float TFreq;
        public float PlusPower;
        public float SampleTimes;
        public float GYOThreshold;

    }

    TestSetting setting;

    List<DataBean> DataList = new ArrayList<>();

    public  List<DataBean> readDataFIle(String FilePath) throws FileNotFoundException {
        InputStream inputStream = new FileInputStream(FilePath);
        BufferedInputStream binaryReader = new BufferedInputStream(inputStream);
        byte[] shortBuf = new byte[2];
        byte[] intBuf = new byte[4];
        byte[] floatBuf = new byte[4];
        byte[] timeBuf = new byte[6];
        byte[] ProjectName = new byte[32];
        try {
            binaryReader.read(timeBuf);
            long time = GetTime(timeBuf);
            binaryReader.read(ProjectName);
            float ReceiveArea_X = readSingle(binaryReader, floatBuf);
            float ReceiveArea_Y = readSingle(binaryReader, floatBuf);
            float ReceiveArea_Z = readSingle(binaryReader, floatBuf);
            float TransmitArea = readSingle(binaryReader, floatBuf);
            float MarkSpacing = readSingle(binaryReader, floatBuf);
            int SampleCount = readShort(binaryReader, shortBuf);
            float SampleIntervel = readSingle(binaryReader, floatBuf);
            float TransmitFrequery = readSingle(binaryReader, floatBuf);
            int StatckCount = readShort(binaryReader, shortBuf);
            int PulsePower = readShort(binaryReader, shortBuf);
            int SampleTimes = readShort(binaryReader, shortBuf);
            float GYRO_Threshold = readSingle(binaryReader, floatBuf);
            setting = new TestSetting();
            setting.RAreaX = ReceiveArea_X;
            setting.RAreaY = ReceiveArea_Y;
            setting.RAreaY = ReceiveArea_Z;
            setting.TArea = TransmitArea;
            setting.MSpacing = MarkSpacing;
            setting.SampleIntervel = SampleIntervel;
            setting.SampleCount = SampleCount;
            setting.StackCount = StatckCount;
            setting.TFreq = TransmitFrequery;
            setting.PlusPower = PulsePower;
            if (SampleTimes == 1) {
                setting.SampleTimes = 25.6f;
            } else if (SampleTimes == 2) {
                setting.SampleTimes = 51.2f;
            } else if (SampleTimes == 3) {
                setting.SampleTimes = 102.4f;
            } else {
                setting.SampleTimes = 12.8f;
            }
            if (PulsePower == 1) {
                setting.PlusPower = 20.0f;
            } else if (PulsePower == 2) {
                setting.PlusPower = 40.0f;
            } else {
                setting.PlusPower = 10.0f;
            }
            setting.GYOThreshold = GYRO_Threshold;
            // fileStream.Seek(78, SeekOrigin.Begin);
            DataBean bean = null;
            byte[] byteBuf = new byte[1];
            int tmp1 = binaryReader.available();

            while (binaryReader.available() != 0) {
                binaryReader.read(timeBuf);
                time = GetTime(timeBuf);
                binaryReader.read(byteBuf);
                byte tmp = byteBuf[0];
                int Directs = tmp;
                binaryReader.read(byteBuf);
                tmp = byteBuf[0];
                int direct = tmp;
                float roll = readSingle(binaryReader, floatBuf);
                float pitch = readSingle(binaryReader, floatBuf);
                float heading = readSingle(binaryReader, floatBuf);
                float voltage = readSingle(binaryReader, floatBuf);
                float current = readSingle(binaryReader, floatBuf);
                double[] sample = new double[SampleCount];
                double value = 0;
                for (int i = 0; i < SampleCount; i++) {
                    value = readSingle(binaryReader, floatBuf);
                    sample[i] = value;
                }
                ProbeBean point = new ProbeBean();
                point.Pitch = pitch;
                point.Heading = heading;
                point.Roll = roll;
                point.Current = current;
                point.Voltage = voltage;
                point.Time = time;
                point.Sample = sample;
                point.SampleIntervel = SampleIntervel;
                point.SampleLength = SampleCount;
                point.PickPointNum = 1;
                point.Direct = direct;
                int ResultLength = point.SampleLength / point.PickPointNum;
                double[] result_sample = new double[ResultLength];
                int index = 0;
                value = 0;
                for (int i = 0; i < point.SampleLength; i++) {
                    double electricCurrent = point.Sample[i];
                    value += electricCurrent;
                    if ((i % point.PickPointNum) == (point.PickPointNum - 1)) {
                        if (value == 0) {
                            value = 0.0001;
                        }
                        result_sample[index] = value / point.PickPointNum;
                        value = 0;
                        index++;
                    }
                }
                point.Result = result_sample;
                if (bean == null) {
                    bean = new DataBean();
                    bean.Directs = Directs;
                    bean.Samples.add(point);
                    bean.Time = point.Time;
                    DataList.add(bean);
                } else {
                    if (bean.Directs == Directs) {
                        if (bean.Samples.size() < bean.Directs) {
                            bean.Samples.add(point);
                        } else {
                            bean = new DataBean();
                            bean.Directs = Directs;
                            bean.Samples.add(point);
                            bean.Time = point.Time;
                            DataList.add(bean);
                        }
                    } else {
                        bean = new DataBean();
                        bean.Directs = Directs;
                        bean.Samples.add(point);
                        bean.Time = point.Time;
                        DataList.add(bean);
                    }
                }
            }
        } catch (Exception ex) {
            setting = null;
        } finally {
            if (binaryReader != null) {
                try {
                    binaryReader.close();
                    ToastUtils.showShort("数据读取成功！");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return DataList;
    }

    public class PointBean {
        public String time;
        public long timecode;
        public double length;

    }

    private List<PointBean> readTrdFile(String PointsFile) throws IOException {
        List<PointBean> pointBeanList = new ArrayList<>();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(PointsFile));
        if (FileUtils.isFileExists(PointsFile)) {
            String line = "";
            int PointsCount = 0;
            Boolean first = true;
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    if (first) {
                        first = false;
                    } else {
                        String[] sArray = line.split("\t");
                        if (sArray.length == 4) {
                            PointBean bean = new PointBean();
                            String time = sArray[0];
                            long timecode = 0;
                            double length = 0.0;
                            timecode = Long.parseLong(sArray[1]);
                            try {
                                length = Long.parseLong(sArray[3]);
                            } catch (Exception ex) {
                                length = 0;
                            }
                            PointsCount++;
                            bean.time = time;
                            bean.timecode = timecode;
                            bean.length = length;
                            pointBeanList.add(bean);
                        }
                    }
                }
            } catch (Exception ex) {
            } finally {
                if (bufferedReader != null)
                    bufferedReader.close();
            }
            if (first) {
                ToastUtils.showLong("计时文件参数缺失，无法打开!");
            }
        } else {
            ToastUtils.showLong("计时文件不存在无法打开！");
        }
        return pointBeanList;
    }

    List<PointBean> pointBeanList = new ArrayList<>();

    public void InitPointList(String PointsFile) throws IOException {
        pointBeanList.clear();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(PointsFile));
        if (PointsFile != "") {
            if (FileUtils.isFileExists(PointsFile)) {
                String line = "";
                int PointsCount = 0;
                Boolean first = true;
                try {
                    while ((line = bufferedReader.readLine()) != null) {
                        if (first) {
                            first = false;
                        } else {
                            String[] sArray = line.split("\t");
                            if (sArray.length == 4) {
                                PointBean bean = new PointBean();
                                String time = sArray[0];
                                long timecode = 0;
                                double length = 0.0;
                                timecode = Long.parseLong(sArray[1]);
                                try {
                                    length = Long.parseLong(sArray[3]);
                                } catch (Exception ex) {
                                    length = 0;
                                }
                                PointsCount++;
                                bean.time = time;
                                bean.timecode = timecode;
                                bean.length = length;
                                pointBeanList.add(bean);
                            }
                        }
                    }
                } catch (Exception ex) {
                } finally {
                    if (bufferedReader != null)
                        bufferedReader.close();
                  //  initPointListView(pointBeanList);
                }
                if (first) {
                    ToastUtils.showLong("计时文件参数缺失，无法打开!");
                }
            } else {
                ToastUtils.showLong("计时文件不存在无法打开！");
            }
        }
    }

    private void initMergeShowLy() {
        binding.mergeShowLy.setVisibility(View.VISIBLE);
        binding.exitShowBtn.setOnClickListener(v -> binding.mergeShowLy.setVisibility(View.GONE));
    }

    private void initPointListView(List<DataBean> list) {
        ListView listView = binding.dataInfoList;
        YcsPointInfoAdapter ycsPointInfoAdapter = new YcsPointInfoAdapter(this, list, iPointList);
        listView.setAdapter(ycsPointInfoAdapter);
    }

    interface IPointList {
        public void refreshList();

        public void clickOn(int position);

        public void swithChange (int index);


    }

    private int currentPosition = 0;

    IPointList iPointList = new IPointList() {
        @Override
        public void refreshList() {
        }

        @Override
        public void clickOn(int position) {
            try {
                List<ProbeBean> probeBeans = DataList.get(position).Samples;
                currentPosition = position;
                List<ProbeBean> tmplist = new ArrayList<>();
                if (swithStatus[0]) {
                    tmplist.add(probeBeans.get(0));
                }
                if (swithStatus[1]) {
                    tmplist.add(probeBeans.get(1));
                }
                if (swithStatus[2]) {
                    tmplist.add(probeBeans.get(2));
                }
                if (tmplist.size() > 0) {
                    drawSample(tmplist);
                } else {
                    ToastUtils.showLong("请至少选择一个方向显示");
                }
            }catch (Exception e){
               ToastUtils.showLong(e.getMessage());
            }
        }

        @Override
        public void swithChange(int index) {

        }
    };

    public class TemBean {
        public DataBean Data;
        public PointBean Point;
    }

    private List<TemBean> mergeData(List<DataBean> dataList, List<PointBean> pointBeanList) {
        List<TemBean> TemList = new ArrayList<>();
        TemList.clear();
        int index = 0;
        long SpanTime = Long.MAX_VALUE;
        for (int i = 0; i < pointBeanList.size(); i++) {
            PointBean point = pointBeanList.get(i);
            long Ptime = point.timecode;
            DataBean PreData = null;
            DataBean NextData = null;
            while (index < dataList.size()) {
                DataBean tmp = dataList.get(index);
                long Dtime = tmp.Time;
                if (Dtime < Ptime) {
                    PreData = tmp;
                    index++;
                } else {
                    NextData = tmp;
                    index++;
                    break;
                }
            }
            if (PreData != null && NextData != null) {
                long span1 = Math.abs(PreData.Time - Ptime);
                long span2 = Math.abs(NextData.Time - Ptime);
                if (span2 < span1) {
                    TemBean tem = new TemBean();
                    tem.Data = NextData;
                    tem.Point = point;
                    TemList.add(tem);
                } else {
                    TemBean tem = new TemBean();
                    tem.Data = PreData;
                    tem.Point = point;
                    TemList.add(tem);
                    index--;
                }
            } else if (NextData == null) {
                TemBean tem = new TemBean();
                tem.Data = PreData;
                tem.Point = point;
                TemList.add(tem);
            } else if (PreData == null) {
                TemBean tem = new TemBean();
                tem.Data = NextData;
                tem.Point = point;
                TemList.add(tem);
            }
        }
        return TemList;
    }

    private boolean [] swithStatus = {true,true,true};

    private String[] orientationStr = {"Z方向", "Y方向", "X方向"};

    private int[] colorAry = {Color.RED, Color.GREEN, Color.BLUE};

    private void initLineChart(LineChart chart) {
        Description description = new Description();
        description.setText("响应曲线 Y轴:感应电动势Log(V)|(uV/A) X轴:时间|(ms)");
        description.setTextColor(Color.BLACK);
        description.setYOffset(15);
        description.setXOffset(125);
        chart.setDescription(description);
        XAxis xAxis = chart.getXAxis();
        xAxis.setAxisMaximum(4.5f);
        xAxis.setAxisMinimum(0);
        xAxis.setGranularity(1f); // 每隔 10 显示一个标签
        xAxis.setGranularityEnabled(true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int num1 = (int) value;
                double num2;
               if (num1==0){
                    num2 = 0;
               }
               else {
                    num2 = 0.001;
               }
                for (int i = 0; i < num1; i++) {
                    num2 = num2*10;
                }
                return String.valueOf(num2);
            }
        });
        chart.setDrawBorders(true);
//        chart.setMinOffset(0);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setEnabled(true);
//        xAxis.setTextColor(Color.BLACK);
//        xAxis.setDrawGridLinesBehindData(true);//当设置为 true：网格线在数据图形的背后绘制。
         xAxis.setAvoidFirstLastClipping(true);//当设置为 true：X 轴的第一个和最后一个标签将会自动留出一定的空白间距
//        DashPathEffect dashPathEffect = new DashPathEffect(new float[]{5f, 5f}, 1);
//        xAxis.setGridDashedLine(dashPathEffect);
//        xAxis.setAxisLineWidth(2);

//        xAxis.setLabelCount(8);
//        YAxis leftYAxis = chart.getAxisLeft();
//
//        leftYAxis.setEnabled(true);
//        leftYAxis.setDrawGridLines(true);
//        leftYAxis.setGridDashedLine(dashPathEffect);
//        leftYAxis.setDrawGridLinesBehindData(true);
//        leftYAxis.setGridLineWidth(1);
//        leftYAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
//        leftYAxis.setTextColor(Color.BLACK);
//        leftYAxis.setDrawZeroLine(true);
//        leftYAxis.setYOffset(5);
     //   rightYaxis.setEnabled(false);
        YAxis yAxis = chart.getAxisLeft();
       // yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        yAxis.setAxisMaximum(6f);
        yAxis.setYOffset(-5);
        yAxis.setAxisMinimum(-4f);
        yAxis.setGranularity(1f); // 每隔 10 显示一个标签
        yAxis.setLabelCount(9, false);
        yAxis.setGranularityEnabled(true);
//        yAxis.setValueFormatter(new ValueFormatter() {
//            @Override
//            public String getFormattedValue(float value) {
//                return String.format("10^%.0f", value); //将 log10 值转换为指数形式
//            }
//        });
        YAxis rightYaxis = chart.getAxisRight();
        rightYaxis.setEnabled(false);
        Legend legend = chart.getLegend();
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setTextSize(12f);
        legend.setDrawInside(false);
        legend.setEnabled(true);
    }

    private void drawSample(List<ProbeBean> list) {
        LineChart chart = binding.ycsSampleChart;
        chart.setVisibility(View.VISIBLE);
        LineData lineData = new LineData();
        LineDataSet dataSet;
        List<Entry> entries;
        for (int j = 0; j < list.size(); j++) {
            entries = new ArrayList<>();
            int size = list.get(j).SampleLength;
            ProbeBean tmpprobe = list.get(j);
            if (Math.log10(tmpprobe.Result[0]) > 0) {
                entries.add(new Entry(0, (float) (Math.log10(tmpprobe.Result[0]))));
            } else {
                entries.add(new Entry(0, (float) (Math.log10(Math.abs(tmpprobe.Result[0])))));
            }
            for (int i = 1; i < size; i++) {
                if (Math.log10(i * tmpprobe.SampleIntervel)<2.5&&Math.log10(i * tmpprobe.SampleIntervel)>2){
                    if (i%2!=0){
                        continue;
                    }
                }
                else if(Math.log10(i * tmpprobe.SampleIntervel)<3.5&&Math.log10(i * tmpprobe.SampleIntervel)>=2.5){
                    if (i%3!=0){
                        continue;
                    }
                }
                else if(Math.log10(i * tmpprobe.SampleIntervel)>=3.5){
                    if (i%5!=0){
                        continue;
                    }
                }
                if (Math.log10(tmpprobe.Result[i]) > 0) {
                    entries.add(new Entry((float) Math.log10(i * tmpprobe.SampleIntervel), (float) (Math.log10(tmpprobe.Result[i]))));
                } else {
                    entries.add(new Entry((float) Math.log10(i * tmpprobe.SampleIntervel), (float) (Math.log10(Math.abs(tmpprobe.Result[i])))));
                }
            }
            dataSet = new LineDataSet(entries, orientationStr[tmpprobe.Direct]);
            dataSet.setDrawCircles(false);
            dataSet.setColor(colorAry[tmpprobe.Direct]);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setLineWidth(1);
            lineData.addDataSet(dataSet);
            chart.setData(lineData);
        }
        chart.invalidate();
    }

    YcsMainCache ycsMainCache = YcsMainCache.GetInstance();

    private void refreshDataList() {
        int pageNum = 0;
        loadData(pageNum);
    }

    List<YcsDataFileInfo> ycsDataFileInfoList = new ArrayList<>();
    int pageSize = 20;

    private void loadData(int pageNum) {
        // List<leida_info> leidaInfoList = query.find(pageNum * pageSize, pageSize);
        List<File> ycsList = FileUtils.listFilesInDir(ycsMainCache.rootFilePath);
        for (int i = 0; i < ycsList.size(); i++) {
            List<File> tmplist = FileUtils.listFilesInDir(ycsList.get(i).getPath(), false);
            if (tmplist.size() == 0) {
                continue;
            }
            YcsDataFileInfo ycsDataFileInfo = new YcsDataFileInfo();
            ycsDataFileInfo.filePath = ycsList.get(i).getPath();
            ycsDataFileInfo.projectName = tmplist.get(0).getName().split("\\.")[0];
            for (int j = 0; j < tmplist.size(); j++) {
                String tmpStr = tmplist.get(j).getName();
                if (tmpStr.contains(".trd")){
                    ycsDataFileInfo.trdFile = tmplist.get(j).getName();
                    ycsDataFileInfo.trdPath = tmplist.get(j).getPath();
                }
                else if (tmpStr.contains(".dat"))
                {
                    ycsDataFileInfo.datFile = tmplist.get(j).getName();
                    ycsDataFileInfo.datPath = tmplist.get(j).getPath();

                }
                else if (tmpStr.contains(".zip"))
                {
                    ycsDataFileInfo.zipFile = tmplist.get(j).getName();
                    ycsDataFileInfo.zipPath = tmplist.get(j).getPath();

                }
                else if (tmpStr.contains("-0.ycs")){
                    ycsDataFileInfo.x_ycs_file = tmplist.get(j).getName();

                }
                else if (tmpStr.contains("-1.ycs")){
                    ycsDataFileInfo.y_ycs_file = tmplist.get(j).getName();

                }
                else if (tmpStr.contains("-2.ycs")){
                    ycsDataFileInfo.z_ycs_file = tmplist.get(j).getName();

                }
//                switch (tmpStr) {
//                    case "trd": {
//                        ycsDataFileInfo.trdFile = tmplist.get(j).getName();
//                        ycsDataFileInfo.trdPath = tmplist.get(j).getPath();
//                        break;
//                    }
//                    case "dat": {
//                        ycsDataFileInfo.datFile = tmplist.get(j).getName();
//                        ycsDataFileInfo.datPath = tmplist.get(j).getPath();
//                        break;
//                    }
//                    case "zip": {
//                        ycsDataFileInfo.zipFile = tmplist.get(j).getName();
//                        ycsDataFileInfo.zipPath = tmplist.get(j).getPath();
//                        break;
//                    }
//                    case "0.ycs":{
//                        ycsDataFileInfo.x_ycs_file = tmplist.get(j).getName();
//                        break;
//                    }
//                    case "1.ycs":{
//                        ycsDataFileInfo.y_ycs_file = tmplist.get(j).getName();
//                        break;
//                    }
//                    case "2.ycs":{
//                        ycsDataFileInfo.z_ycs_file = tmplist.get(j).getName();
//                        break;
//                    }
//                }
            }
            ycsDataFileInfoList.add(ycsDataFileInfo);
        }
        //  ycsDataFileInfoList = ycsDataFileInfoList.subList(pageNum*pageSize,pageNum*pageSize+pageSize);
        if (pageNum == 0) {
            ycsDataListAdapter.setNewInstance(new ArrayList<YcsDataFileInfo>());
        }
        if (ycsDataFileInfoList == null || ycsDataFileInfoList.isEmpty()) {
            // hasMore = false;
            ycsDataListAdapter.getLoadMoreModule().loadMoreEnd();
            return;
        }
        ycsDataListAdapter.addData(ycsDataFileInfoList);
        if (ycsDataFileInfoList.size() < pageSize) {
            ycsDataListAdapter.getLoadMoreModule().loadMoreEnd();
        } else {
            ycsDataListAdapter.getLoadMoreModule().loadMoreComplete();
        }
    }
}
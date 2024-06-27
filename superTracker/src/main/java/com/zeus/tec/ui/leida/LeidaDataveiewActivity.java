package com.zeus.tec.ui.leida;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnLoadMoreListener;
import com.zeus.tec.R;
import com.zeus.tec.databinding.ActivityLeidaDataveiewBinding;
import com.zeus.tec.db.ObjectBox;
import com.zeus.tec.model.leida.DrillPipe;
import com.zeus.tec.model.leida.MergeCache;
import com.zeus.tec.model.leida.ProbePoint;
import com.zeus.tec.model.leida.leidaPointRecordInfo;
import com.zeus.tec.model.leida.leidaPointRecordInfo_;
import com.zeus.tec.model.leida.leida_info;
import com.zeus.tec.model.leida.leida_info_;
import com.zeus.tec.ui.leida.Apater.leidaDataListAdapater;
import com.zeus.tec.ui.leida.interfaceUtil.DialogCallback;
import com.zeus.tec.ui.leida.util.IOtool;
import com.zeus.tec.ui.leida.util.MesseagWindows;
import com.zeus.tec.model.utils.FeedbackUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.objectbox.query.Query;

public class LeidaDataveiewActivity extends AppCompatActivity {

    public ActivityLeidaDataveiewBinding binding;
    private leidaDataListAdapater dataListAdapter = new leidaDataListAdapater();
    Query<leida_info> query = ObjectBox.get().boxFor(leida_info.class).query()
            .orderDesc(leida_info_.id).build();
    private final int pageSize = 20;
    private int pageNum = 0;
    private boolean hasMore = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_leida_dataveiew);
        binding = ActivityLeidaDataveiewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.ivBack.setOnClickListener(view -> {
            FeedbackUtil.getInstance().doFeedback();
            finish();
        });

        RecyclerView rv = binding.rvList;
        rv.setLayoutManager(new LinearLayoutManager(this));
        dataListAdapter.addChildClickViewIds(R.id.tv_continue, R.id.tv_view, R.id.tv_share, R.id.tv_merge, R.id.tv_export);
        dataListAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                FeedbackUtil.getInstance().doFeedback();
                switch (view.getId()) {
                    case R.id.tv_view:
                        doShowData(dataListAdapter.getItem(position));
                        break;
                    case R.id.tv_share:
                        doShare(dataListAdapter.getItem(position));
                        break;
                    case  R.id.tv_merge://删除数据
                        doMerge(dataListAdapter.getItem(position));
                        break;
                    case R.id.tv_export:
                        // doExport(dataListAdapter.getItem(position));
                        break;
                    case R.id.tv_continue://合并数据
                        bindingData(dataListAdapter.getItem(position));
                         // doContinue(dataListAdapter.getItem(position));
                        break;
                }
            }
        });
        rv.setAdapter(dataListAdapter);
        dataListAdapter.getLoadMoreModule().setEnableLoadMoreIfNotFullPage(false);
        dataListAdapter.getLoadMoreModule().setAutoLoadMore(true);
        dataListAdapter.getLoadMoreModule().setEnableLoadMore(true);
        dataListAdapter.getLoadMoreModule().setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (hasMore) {
                    loadData(pageNum + 1);
                }
            }
        });
        refreshData();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void bindingData (leida_info info){
        if (info.dataPath==null){
            ToastUtils.showLong("打点记录文件不存在");
            return;
        }
        File trdFile = new File(info.dataPath);
        if(!trdFile.exists()){
            ToastUtils.showLong("打点记录文件不存在");
            return;
        }
        File datFile = new File(info.dataPath.replace("trd","dat"));
        if (!datFile.exists()){
            ToastUtils.showLong("数据文件不存在");
            return;
        }
        try {
            MergeCache.init();
            pointRecordList =  new ArrayList<>();
           MergeCache.PipeCount =  ReadRecordingData(info.dataPath);
            DrillPipe item ;
            for (int i = 0; i <  MergeCache.PipeCount; i++)
            {
                item = new DrillPipe(pointRecordList.get(i), pointRecordList.get(i + 1));
                MergeCache.DrillPipeList.add(item);
            }
            FileInputStream fs = new FileInputStream(datFile);
            //region 读取Sample数据
            BufferedInputStream bis = new BufferedInputStream(fs);
            readHeader(bis);
            readProbePoint2(bis,MergeCache.dataHeader.SampleCount);
            bis.close();
            fs.close();
            ToastUtils.showLong("数据合并完成");
            //endregion
            MergeCache.SpaceSapmle = MergeCache.GetDefaultSpacing(MergeCache.PipeCount);
            int num5 = MergeCache.TimeMatching(MergeCache.DrillPipeList,MergeCache.probePointList);
            if (num5 >0) {
                int result =  MergeCache.OrganizeList(MergeCache.DrillPipeList,MergeCache.probePointList,MergeCache.PipeLength,MergeCache.SpaceSapmle,0);
                Intent intent = new Intent(LeidaDataveiewActivity.this,MergeSampleActivity.class);
                intent.putExtra("INT_KEY", MergeCache.Merge_Fail);
                startActivity(intent);
            }
            else {
                int result =  MergeCache.OrganizeList(MergeCache.DrillPipeList,MergeCache.probePointList,MergeCache.PipeLength,MergeCache.SpaceSapmle,0);
                Intent intent = new Intent(LeidaDataveiewActivity.this,MergeSampleActivity.class);
                intent.putExtra("INT_KEY", MergeCache.Merge_Success);
                startActivity(intent);
            }
        }catch (Exception exception){

        }
    }

    private void  readHeader (  BufferedInputStream bis ) throws IOException {
        byte [] timeByte = new byte[6];
        byte [] byteName = new byte[0x20];
        byte [] byteSampleCount = new byte[2];
        byte [] byteStackCount = new byte[2];
        byte [] byteNbOfSampleDelayPoint = new byte[2];
        byte [] byteAmplifyValue= new byte[2];
        byte [] byteSampleFrequency = new byte[2];
        byte [] byteTimeInterval = new byte[4];
        bis.read(timeByte);
        bis.read(byteName);
        bis.read(byteSampleCount);
        bis.read(byteStackCount);
        bis.read(byteNbOfSampleDelayPoint);
        bis.read(byteAmplifyValue);
        bis.read(byteSampleFrequency);
        bis.read(byteTimeInterval);
        MergeCache.dataHeader.Time = convertTime(timeByte);
        MergeCache.dataHeader.Name = new String(byteName);
        MergeCache.dataHeader.SampleCount  = bytesToShortLittle(byteSampleCount);
        MergeCache.dataHeader.StackCount = bytesToShortLittle(byteStackCount);
        MergeCache.dataHeader.NbOfSampleDelayPoint = bytesToShortLittle(byteNbOfSampleDelayPoint);
        MergeCache.dataHeader.AmplifyValue = bytesToShortLittle(byteAmplifyValue);
        MergeCache.dataHeader.SampleFrequency = bytesToShortLittle(byteSampleFrequency);
        MergeCache.dataHeader.TimeInterval =  ByteBuffer.wrap(byteTimeInterval).order(ByteOrder.LITTLE_ENDIAN).getFloat();
    }

    private float readSingle(BufferedInputStream bis,byte [] angle) throws IOException {
        bis.read(angle);
       return ByteBuffer.wrap(angle).order(ByteOrder.LITTLE_ENDIAN).getFloat()  ;
    }

   // private List<ProbePoint> probePointList = new ArrayList<>();

    private List<LocalDateTime> pointRecordList;

  //  private List<DrillPipe> DrillPipeList = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.O)
    private  void  readProbePoint2 (BufferedInputStream bis, short SampleCount){
        try {
            byte[] buffer = new byte[SampleCount*4]; // 批量读取 1024 字节
            int bytesRead;
            byte[] time = new byte[8];
            byte [] angle = new byte[4];
            ProbePoint item =  null;
            int num5 = ((int) (bis.available() )) / (20 + (SampleCount * 4));
            // 使用一个 ByteBuffer 来转换字节为 float
            ByteBuffer byteBuffer = ByteBuffer.allocate(4);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            for (int i = 0; i < num5; i++)
            {
                long startTimeMillis = System.currentTimeMillis();
                item =  new ProbePoint(SampleCount);
                bis.read(time);
                item.SampleTime = LocalDateTime.of(
                        time[0]+2000, // year
                        time[1], // month
                        time[2], // day of month
                        time[3], // hour
                        time[4], // minute
                        time[5]  // second
                );
                item.Roll =readSingle(bis,angle);
                item.Pitch = readSingle(bis,angle);
                item.Heading = readSingle(bis,angle);
              //  bis.read(buffer);
                byte [] tmpbuffer = new byte[4];
                long twoTimeMillis = System.currentTimeMillis();
                for (int j = 0; j < SampleCount; j++)
                {
                    item.Voltage[j] = 1000f *readSingle(bis,angle);
                }
                item.OriginalIndex = i;
                long endTimeMillis = System.currentTimeMillis();
                MergeCache.probePointList.add(item);
                long durationMillis = endTimeMillis - startTimeMillis;
                long onetime = twoTimeMillis - startTimeMillis;
                long twotime = endTimeMillis - twoTimeMillis;
               int a =10;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int ReadRecordingData(String strFileName) throws IOException {
        int num = -1;
        BufferedReader reader = new BufferedReader(new FileReader(strFileName));
        try
        {
            String pattern = "yyyy-MM-dd HH:mm:ss";
            String str ;
            String [] strArray2 = reader.readLine().split("\t");
            LocalDateTime tmp = parseToLocalDateTime (strArray2[1],pattern);
            MergeCache.PipeLength = Float.parseFloat(strArray2[0])*10f;
            pointRecordList.add(tmp);
            for (num = 0; (str = reader.readLine()) != null; num++)
            {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                     tmp = parseToLocalDateTime (str.split("\t")[0],pattern);
                     pointRecordList.add(tmp);
                }
            }
        }
        catch (Exception exception)
        {
          num=-1;
        }
        finally
        {
            reader.close();
        }
        return num;
    }

    public static LocalDateTime parseToLocalDateTime(String str, String pattern) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        try {
            return LocalDateTime.parse(str, formatter);
        } catch (DateTimeParseException e) {
            System.err.println("Failed to parse LocalDateTime: " + e.getMessage());
            return null;
        }
        }
        else {
            return null;
        }
    }

    public static short bytesToShortLittle(byte [] input) {
        byte highByte = input[1];
        byte lowByte = input[0];
        return (short) ((highByte << 8) | (lowByte & 0xFF));
    }

    private LocalDateTime convertTime (byte [] time){
        LocalDateTime dateTime = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
             dateTime = LocalDateTime.of(
                    time[0], // year
                    time[1], // month
                    time[2], // day of month
                    time[3], // hour
                    time[4], // minute
                    time[5]  // second
            );
        }
        return dateTime;
    }

    private void showToast(String message) {
        //Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_tip);
        TextView tv = dialog.findViewById(R.id.message);
        tv.setText("" + message);
        dialog.findViewById(R.id.tv_ok).setOnClickListener(v -> {
            FeedbackUtil.getInstance().doFeedback();
            dialog.dismiss();
        });
        dialog.show();
    }

    public static void zipFiles1(String zipFilePath, String... sourceFilePaths) {
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(fos))) {
            byte[] buffer = new byte[1024];
            for (String sourceFilePath : sourceFilePaths) {
                File fileToZip = new File(sourceFilePath);
                try (FileInputStream fis = new FileInputStream(fileToZip);
                     BufferedInputStream bis = new BufferedInputStream(fis)) {
                    // Add ZIP entry to output stream.
                    zipOut.putNextEntry(new ZipEntry(fileToZip.getName()));
                    // Transfer bytes from the file to the ZIP file
                    int bytesRead;
                    while ((bytesRead = bis.read(buffer)) != -1) {
                        zipOut.write(buffer, 0, bytesRead);
                    }
                    // Complete the entry
                    zipOut.closeEntry();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doShare(leida_info info) {
        if (info.zipPath==null)
        {
            ToastUtils.showLong("压缩文件路径不存在");
            return;
        }
        File f = new File(info.zipPath);
        if (!f.exists())
        {
          List<File> fileList = FileUtils.listFilesInDir(info.projectRoot);
         String [] filepathlist =new String[fileList.size()];
            if (fileList.size()> 0) {
                for (int i = 0; i < fileList.size(); i++) {
                    filepathlist[i] = fileList.get(i).getAbsolutePath();
                }
                zipFiles1(info.zipPath,filepathlist);
            }
            else {
                ToastUtils.showLong("数据分享错误，数据文件不存在");
            }
        }
        else {
            f.delete();
            List<File> fileList = FileUtils.listFilesInDir(info.projectRoot);
            String [] filepathlist =new String[fileList.size()];
            if (fileList.size()> 0) {
                for (int i = 0; i < fileList.size(); i++) {
                    filepathlist[i] = fileList.get(i).getAbsolutePath();
                }
                zipFiles1(info.zipPath,filepathlist);
            }
            else {
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
            Uri uri ;
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
        }
        catch (Exception ex)
        {
            ToastUtils.showLong(ex.getLocalizedMessage());
        }
    }

    private void doShowData(leida_info info) {
        if (info == null) return;
      //  FeedbackUtil.getInstance().doFeedback();
        startActivity(leidaDetailActivity.buildIntent(this, info));
    }

    private void doContinue(leida_info info) {
        if (info == null) return;
    }

    private void doExport(leida_info info) {
        if (info.zipPath == null || info.zipPath.isEmpty()) {
            ToastUtils.showLong("数据合成错误，压缩文件不存在");
            return;
        }
        File f = new File(info.zipPath);
        if (!f.exists()) {
            ToastUtils.showLong("数据合成错误，压缩文件不存在");
            return;
        }
        String path = PathUtils.getExternalDownloadsPath() + File.separator + "superTracker";
        File tf = new File(path);
        FileUtils.createOrExistsDir(tf);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String tfPath = path + File.separator + sdf.format(info.creatTime) + ".zip";
        if (FileUtils.copy(info.zipPath, tfPath)) {
            showToast("数据文件已存放在 " + tfPath);
        } else {
            showToast("数据文件导出失败，请重试");
        }
    }

    private void refreshData() {
        pageNum = 0;
        loadData(pageNum);
    }

    private void loadData(int pageNum) {
        List<leida_info> leidaInfoList = query.find(pageNum * pageSize, pageSize);
        if (pageNum == 0) {
            dataListAdapter.setNewInstance(new ArrayList<leida_info>());
        }
        if (leidaInfoList == null || leidaInfoList.isEmpty()) {
            hasMore = false;
            dataListAdapter.getLoadMoreModule().loadMoreEnd();
            return;
        }
        dataListAdapter.addData(leidaInfoList);
        if (leidaInfoList.size() < pageSize) {
            dataListAdapter.getLoadMoreModule().loadMoreEnd();
        } else {
            dataListAdapter.getLoadMoreModule().loadMoreComplete();
        }
    }

    private void doMerge(leida_info info) {
        if (info == null)
        {
            return;
        }
        MesseagWindows.showMessageBox(this, "删除记录", "是否删除当前记录", new DialogCallback()
        {
            @Override
            public void onPositiveButtonClick() {
                File fileData = new File(info.projectRoot);
                boolean result ;
                if (fileData.exists()) {
                    result = FileUtils.delete(fileData);
                } else {
                    result = true;
                }
                if (result) {
                    ObjectBox.get().boxFor(leida_info.class).remove(info.id);
                    ObjectBox.get().boxFor(leidaPointRecordInfo.class).query().equal(leidaPointRecordInfo_.leidaInfoId,info.id).build().remove();
                    dataListAdapter.notifyDataSetChanged();
                    refreshData();
                } else {
                    ToastUtils.showLong("文件删除失败");
                }
            }
            @Override
            public void onNegativeButtonClick() {
            }
        });

    }

}
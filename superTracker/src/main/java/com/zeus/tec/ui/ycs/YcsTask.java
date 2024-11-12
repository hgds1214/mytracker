package com.zeus.tec.ui.ycs;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.zeus.tec.model.leida.leida_info;
import com.zeus.tec.model.leida.main.MainCache;
import com.zeus.tec.model.ycs.YcsMainCache;
import com.zeus.tec.ui.leida.interfaceUtil.ILeidaDelectfile;
import com.zeus.tec.ui.leida.util.IOtool;
import com.zeus.tec.ui.leida.util.MyApplicationContext;
import com.zeus.tec.ui.leida.util.fastToast;

import java.io.File;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class YcsTask extends AsyncTask<Void, Integer, Void> {

    private ProgressBar progressBar;
    private String Path;
    YcsMainCache cache = YcsMainCache.GetInstance();
    int number = 0;
    public ReentrantLock lock = new ReentrantLock();
    Condition condition;
    TextView stepview;
    int Who;
    Context context = MyApplicationContext.getInstance().getAppContext();
    String dataPath ;
    public ILeidaDelectfile iLeidaDelectfile ;

    public YcsTask (ProgressBar progressBar, String path, TextView textView ,int who,String dataPath){
        this.progressBar = progressBar;
        this.Path = path;
        this.stepview = textView;
        this.Who = who;
        this.dataPath = dataPath;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        progressBar.setProgress(values[0]);
    }
    private static final String TAG = "YcsSend";
    @Override
    protected Void doInBackground(Void... voids) {

        publishProgress(1);
        // Message msg = Message.obtain();

        if (DownloadFile(this.Path)) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ((Activity) context).runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                stepview.setText("下载完成");
                fastToast.showToast("数据文件下载成功");
            });
        }
        else {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    progressBar.setVisibility(View.GONE);
                    stepview.setText("下载失败");
                    fastToast.showToast("数据文件下载失败");
                }
            });
        }

        return null;
    }

    private boolean DownloadFile(String dataName)  {
        Boolean complete = false;
        String save_file ;
        String tmp_file ;
        if (this.Who == 1) {
            try {

                save_file = dataPath;
                tmp_file = dataPath.replace(".dat",".tmp");
                if (FileUtils.isFileExists(tmp_file)){
                    FileUtils.delete(tmp_file);
                }
               FileUtils.createOrExistsFile(tmp_file);
            }
            catch (Exception ex)
            {
                ToastUtils.showLong("文件路径不存在");
                return false;
            }
        }
        else if (this.Who==0)
        {
            try {

                save_file = dataPath;
                tmp_file = dataPath;
            }
            catch (Exception ex)
            {
                ToastUtils.showLong("文件路径不存在");
                return false;
            }
        }
        else {
            String tmpPath = PathUtils.getExternalAppFilesPath()+ File.separator + "tmpData";
            File tmpfile = new File(tmpPath);
            if (!tmpfile.exists()) {
                if (!FileUtils.createOrExistsDir(tmpPath)) {
                    LogUtils.e("创建文件失败：" + tmpPath);
                    ToastUtils.showLong("创建文件失败，请检查磁盘空间后重试!");
                    return false;
                }
            }
            save_file =PathUtils.getExternalAppFilesPath()+ File.separator + "tmpData"+File.separator+dataName+".dat";
            tmp_file = PathUtils.getExternalAppFilesPath()+ File.separator + "tmpData"+File.separator+dataName+".tmp";
        }
        final int[] exit = {0};
        try {
            if (IOtool.isFileExists(save_file)) {
                condition = lock.newCondition();
                lock.lock();
                ((Activity) context).runOnUiThread(() -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("是否覆盖原文件")
                            .setMessage("该文件已存在，是否覆盖原文件")
                            .setPositiveButton("OK", (dialog, which) -> {
                                lock.lock();
                                condition.signalAll();
                                lock.unlock();
                            }).setNegativeButton("取消", (dialog, which) -> {
                                exit[0] =1;
                                lock.lock();
                                condition.signalAll();
                                lock.unlock();
                            }).show();
                });
                condition.await();
                lock.unlock();
            }
            if (exit[0] == 1) {
                return complete;
            }
            int CurrentLength = 0;
            Boolean isFrist = true;
            int TotalLength = 0;
            Boolean isError = false;
            while (true) {
                CurrentLength = cache.DownLoadFile(dataName, CurrentLength,tmp_file);
                if (CurrentLength == 0) {
                    ((Activity) context).runOnUiThread(() -> Toast.makeText(context, "文件不存在无法下载", Toast.LENGTH_SHORT).show());
                    break;
                } else if (CurrentLength == -1) {
                    ((Activity) context).runOnUiThread(() -> Toast.makeText(context, "网络异常无法下载", Toast.LENGTH_SHORT).show());
                    break;
                } else {
                    if (isFrist) {
                        TotalLength = cache.DeviceOper.TotalLength;
                    }
                    isFrist = false;
                    if (TotalLength == 0)
                        isError = true;
                    if (CurrentLength >= TotalLength) {
                        //SetPos(Process.Maximum);
                        complete = true;
                        publishProgress(100);
                        break;
                    } else {
                        double tmp = ((CurrentLength*100.0)/ TotalLength);
                        publishProgress((int) tmp);
                    }
                }
            }
            if (isError) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "文件长度出错", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception ex) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "文件下载失败", Toast.LENGTH_SHORT).show();
                }
            });
            return complete;
        }
        if (!complete) {
            if (IOtool.isFileExists(tmp_file)) {
                File file = new File(tmp_file);
                file.delete();
            }
        }
        else
        {
            if (IOtool.isFileExists(save_file))
            {
                try
                {
                    File file = new File(save_file);
                    File filetmp = new File(tmp_file);
                    file.delete();
                    filetmp.renameTo(file);
                }catch (Exception exception)
                {
                    exception.printStackTrace();
                }
            }
            else {
                File file = new File(save_file);
                File filetmp = new File(tmp_file);
                filetmp.renameTo(file);
            }
            if (Who==0){

                try {
                  //  iLeidaDelectfile.onDelectFile(leidaInfo);
                }
                catch (Exception exception){
                    ToastUtils.showLong(exception.getLocalizedMessage());
                }
            }

        }
        return complete;

    }

}
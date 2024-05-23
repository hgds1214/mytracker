package com.zeus.tec.ui.leida;

import androidx.annotation.NonNull;
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
import com.zeus.tec.model.leida.leidaPointRecordInfo;
import com.zeus.tec.model.leida.leidaPointRecordInfo_;
import com.zeus.tec.model.leida.leida_info;
import com.zeus.tec.model.leida.leida_info_;
import com.zeus.tec.ui.leida.Apater.leidaDataListAdapater;
import com.zeus.tec.ui.leida.interfaceUtil.DialogCallback;
import com.zeus.tec.ui.leida.util.MesseagWindows;
import com.zeus.tec.model.utils.FeedbackUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
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
//        dataListAdapter.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
//                FeedbackUtil.getInstance().doFeedback();
//
//                startActivity(directionfinderDataDetailActivity.buildIntent(LeidaDataveiewActivity.this, dataListAdapter.getItem(position)));
//            }
//        });
        dataListAdapter.addChildClickViewIds(R.id.tv_continue, R.id.tv_view, R.id.tv_share, R.id.tv_merge, R.id.tv_export);
        dataListAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
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
                    case R.id.tv_continue:
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
package com.zeus.tec.ui.tracker;


import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnLoadMoreListener;
import com.zeus.tec.R;
import com.zeus.tec.databinding.ActivityDataViewBinding;
import com.zeus.tec.db.ObjectBox;
import com.zeus.tec.event.MergeEvent;
import com.zeus.tec.model.leida.leidaPointRecordInfo;
import com.zeus.tec.model.leida.leidaPointRecordInfo_;
import com.zeus.tec.model.leida.leida_info;
import com.zeus.tec.model.tracker.CollectTimeInfo;
import com.zeus.tec.model.tracker.CollectTimeInfo_;
import com.zeus.tec.model.tracker.DrillDataInfo;
import com.zeus.tec.model.tracker.DrillDataInfo_;
import com.zeus.tec.model.tracker.DrillHoleInfo;
import com.zeus.tec.model.tracker.DrillHoleInfo_;
import com.zeus.tec.ui.leida.interfaceUtil.DialogCallback;
import com.zeus.tec.ui.leida.util.MesseagWindows;
import com.zeus.tec.ui.tracker.adapter.DataListAdapter;
import com.zeus.tec.model.utils.FeedbackUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import io.objectbox.query.Query;

public class DataViewActivity extends AppCompatActivity {

    private ActivityDataViewBinding binding;
    private DataListAdapter dataListAdapter = new DataListAdapter();

    Query<DrillHoleInfo> query = ObjectBox.get().boxFor(DrillHoleInfo.class).query()
            .orderDesc(DrillHoleInfo_.collectionDateTime).build();
    private final int pageSize = 20;
    private int pageNum = 0;
    private boolean hasMore = true;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMergeEvent(MergeEvent event) {
        if (event == null || event.info == null) return;
        List<DrillHoleInfo> data = dataListAdapter.getData();
        if (data == null) return;
        int size = data.size();
        for (int i = 0; i < size; i++) {
            DrillHoleInfo info = data.get(i);
            if (info.id == event.info.id) {
                info.isMerged = event.info.isMerged;
                info.collectCount = event.info.collectCount;
                info.collectionDateTime = event.info.collectionDateTime;
                dataListAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(this);

        EventBus.getDefault().register(this);
        binding = ActivityDataViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.ivBack.setOnClickListener( view -> {
            FeedbackUtil.getInstance().doFeedback();
            finish();
        });

        RecyclerView rv = binding.rvList;
        rv.setLayoutManager(new LinearLayoutManager(this));
        dataListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                FeedbackUtil.getInstance().doFeedback();
                startActivity(DataDetailActivity.buildIntent(DataViewActivity.this, dataListAdapter.getItem(position)));
            }
        });
        dataListAdapter.addChildClickViewIds(R.id.tv_continue, R.id.tv_view, R.id.tv_share, R.id.tv_merge, R.id.tv_export);
        dataListAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                FeedbackUtil.getInstance().doFeedback();
                switch (view.getId()){
                    case R.id.tv_view:
                        doShowData(dataListAdapter.getItem(position));
                        break;
                    case R.id.tv_share:
                        doShare(dataListAdapter.getItem(position));
                        break;
                    case R.id.tv_merge:
                        doMerge(dataListAdapter.getItem(position));
                        break;
                    case R.id.tv_export:
                        doDelect(dataListAdapter.getItem(position));

                        break;
                    case R.id.tv_continue:
                        doContinue(dataListAdapter.getItem(position));
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
                    loadData(pageNum+1);
                }
            }
        });
        refreshData();
    }
    private void doDelect(DrillHoleInfo info){
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
                    ObjectBox.get().boxFor(DrillHoleInfo.class).remove(info.id);
                    ObjectBox.get().boxFor(CollectTimeInfo.class).query().equal(CollectTimeInfo_.drillInfoId,info.id).build().remove();
                    ObjectBox.get().boxFor(DrillDataInfo.class).query().equal(DrillDataInfo_.drillInfoId,info.id).build().remove();
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

    private void doExport(DrillHoleInfo info) {
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
        String tfPath = path + File.separator + sdf.format(info.collectionDateTime) + ".zip";

        if (FileUtils.copy(info.zipPath, tfPath)) {
            showToast("数据文件已存放在 " + tfPath);
        } else {
            showToast("数据文件导出失败，请重试");
        }
    }
    private void showToast(String message) {
        //Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_tip);
        TextView tv = dialog.findViewById(R.id.message);
        tv.setText(""+message);
        dialog.findViewById(R.id.tv_ok).setOnClickListener(v->{
            FeedbackUtil.getInstance().doFeedback();
            dialog.dismiss();
        });
        dialog.show();
    }

    private void doShare(DrillHoleInfo info) {
        if (info.zipPath == null || info.zipPath.isEmpty()) {
            ToastUtils.showLong("数据合成错误，压缩文件不存在");
            return;
        }
        File f = new File(info.zipPath);
        if (!f.exists()) {
            ToastUtils.showLong("数据合成错误，压缩文件不存在");
            return;
        }
        String rootPath = PathUtils.getExternalAppFilesPath()+ File.separator + "shareData";
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

        Uri uri;
        if (Build.VERSION.SDK_INT >= 24 ) {
            uri = FileProvider.getUriForFile(this, "com.zeus.tec.fileprovider", f);
            grantUriPermission(getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            LogUtils.e(uri);
        } else {
            uri = Uri.fromFile(f);
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setType("application/octet-stream");

        startActivity(Intent.createChooser(intent, "分享到"));

    }

    private void doShowData(DrillHoleInfo info) {
        if (info == null) return;
        DataCollectActivity.launch(this, info.id, 2);
    }
    private void doContinue(DrillHoleInfo info) {
        if (info == null) return;
        DataCollectActivity.launch(this, info.id, 0);
    }
    private void doMerge(DrillHoleInfo info) {
        if (info == null) return;
        DataCollectActivity.launch(this, info.id, 1);
        //DataMergeActivity.launch(this, info.id);
    }

    private void refreshData() {
        pageNum = 0;
        loadData(pageNum);
    }

    private void loadData(int pageNum) {
        List<DrillHoleInfo> drillHoleInfos = query.find(pageNum * pageSize, pageSize);
        if (pageNum == 0) {
            dataListAdapter.setNewInstance(new ArrayList<DrillHoleInfo>());
        }
        if (drillHoleInfos == null || drillHoleInfos.isEmpty()) {
            hasMore = false;
            dataListAdapter.getLoadMoreModule().loadMoreEnd();
            return;
        }

        dataListAdapter.addData(drillHoleInfos);
        if (drillHoleInfos.size() < pageSize) {
            dataListAdapter.getLoadMoreModule().loadMoreEnd();
        } else {
            dataListAdapter.getLoadMoreModule().loadMoreComplete();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
package com.zeus.tec.ui.ycs;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.LogUtils;
import com.xuexiang.xupdate.XUpdate;
import com.xuexiang.xupdate.entity.UpdateEntity;
import com.xuexiang.xupdate.listener.IUpdateParseCallback;
import com.xuexiang.xupdate.proxy.IUpdateParser;
import com.zeus.tec.R;
import com.zeus.tec.databinding.ActivityYcsMainBinding;
import com.zeus.tec.model.MainListAdapter;
import com.zeus.tec.model.utils.FeedbackUtil;
import com.zeus.tec.ui.leida.Apater.leidaListAdapter;
import com.zeus.tec.ui.leida.leidaMainActivity;
import com.zeus.tec.ui.tracker.model.FunctionItem;

import java.util.ArrayList;
import java.util.List;

public class YcsMainActivity extends AppCompatActivity {

    ActivityYcsMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(this);
        binding = ActivityYcsMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.ycsBack.setOnClickListener(v -> {
            FeedbackUtil.getInstance().doFeedback();
            finish();
        });

        RecyclerView recyclerView = binding.ycsList;
        recyclerView.setLayoutManager((new LinearLayoutManager(this)));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getDrawable(R.drawable.function_list_divider));
        recyclerView.addItemDecoration(dividerItemDecoration);
        List<FunctionItem> data = new ArrayList<>();
        data.add(new FunctionItem("采集运行", R.mipmap.collect, YcsDataCollectActivity.class));
        data.add(new FunctionItem("数据浏览",R.mipmap.view_data,YcsDataViewActivity.class));
       // data.add(new FunctionItem("设备调试", R.mipmap.test_device, sampleTestActivity.class));
      //  data.add(new FunctionItem("数据浏览", R.mipmap.view_data, LeidaDataveiewActivity.class));
      //  data.add(new FunctionItem("操作说明", R.mipmap.merge_data, leidaHelpActivity.class));
        data.add(new FunctionItem("系统设置", R.mipmap.ic_setting, YcsSettingActivity.class));
        MainListAdapter mainListAdapter = MainListAdapter.setInstance(data);
       // leidaListAdapter leidaadapter = leidaListAdapter.newInstance();
        mainListAdapter.setOnItemClickListener((adapter,view,position)->{
            FeedbackUtil.getInstance().doFeedback();
            FunctionItem item = (FunctionItem) adapter.getItem(position);
            Intent intent  = new Intent(YcsMainActivity.this,item.targetCls);
            if ("数据合成".equals(item.label)){
                Toast.makeText(this,"没有连接仪器", Toast.LENGTH_SHORT);
            }
            try {
                startActivity(intent);
            }
            catch (Exception exception){
                int a =0;
            }

        });
        recyclerView.setAdapter(mainListAdapter);
        binding.updateLl.setOnClickListener(v -> {
            FeedbackUtil.getInstance().doFeedback();
            xupdataDef();
        });


    }

    @SuppressLint("ResourceAsColor")
    public void xupdataDef (){
        XUpdate.newBuild(this)
                .updateUrl(testClassurl)
                .updateParser(new CustomUpdateParser())
                .supportBackgroundUpdate(true)
                .promptHeightRatio(1.2f)
                .update();
    }
    String testClassurl ="https://whcsma.oss-cn-wuhan-lr.aliyuncs.com/leidaApp/leidaApp.csv";
    public class CustomUpdateParser implements IUpdateParser {
        @Override
        public UpdateEntity parseJson(String json) throws Exception {
            String[] CustomResult = json.split(",");
            boolean IsIgnorable = false;
            int VersionCode = 0;
            long appSize = 0;
            try {
                VersionCode = Integer.parseInt(CustomResult[1]);
                appSize = Long.parseLong(CustomResult[11]);

            } catch (Exception e) {
                LogUtils.i(e.getMessage());
            }
            if (CustomResult != null) {
                CustomResult[13] = CustomResult[13].replace("/r/n", "\n");
                return new UpdateEntity()
                        .setHasUpdate(true)
                        .setIsIgnorable(IsIgnorable)
                        .setVersionCode(VersionCode)
                        .setVersionName(CustomResult[2])
                        .setUpdateContent(CustomResult[13])
                        .setDownloadUrl(CustomResult[5])
                        .setSize(appSize)
                        .setMd5(CustomResult[10]);
            }
            return null;
        }

        @Override
        public void parseJson(String json, IUpdateParseCallback callback) throws Exception {
            String result = json;
        }

        @Override
        public boolean isAsyncParser() {
            return false;
        }
    }
}
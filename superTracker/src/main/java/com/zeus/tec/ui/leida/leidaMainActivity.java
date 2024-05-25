package com.zeus.tec.ui.leida;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.LogUtils;
import com.xuexiang.xupdate.XUpdate;
import com.xuexiang.xupdate.entity.UpdateEntity;
import com.xuexiang.xupdate.listener.IUpdateParseCallback;
import com.xuexiang.xupdate.proxy.IUpdateParser;
import com.zeus.tec.R;
import com.zeus.tec.databinding.ActivityLeidaMainBinding;
import com.zeus.tec.ui.leida.Apater.leidaListAdapter;
import com.zeus.tec.ui.tracker.model.FunctionItem;
import com.zeus.tec.model.utils.FeedbackUtil;

public class leidaMainActivity extends AppCompatActivity {

    private ActivityLeidaMainBinding binding;
    String testClassurl ="https://whcsma.oss-cn-wuhan-lr.aliyuncs.com/leidaApp/leidaApp.csv";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(this);
        binding = ActivityLeidaMainBinding.inflate((getLayoutInflater()));
        setContentView(binding.getRoot());

        binding.leidaBack.setOnClickListener(v -> {
            FeedbackUtil.getInstance().doFeedback();
            finish();
        });
        RecyclerView recyclerView = binding.leidaList;
        recyclerView.setLayoutManager((new LinearLayoutManager(this)));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getDrawable(R.drawable.function_list_divider));
        recyclerView.addItemDecoration(dividerItemDecoration);
        leidaListAdapter leidaadapter = leidaListAdapter.newInstance();
        leidaadapter.setOnItemClickListener((adapter,view,position)->{
            FeedbackUtil.getInstance().doFeedback();
            FunctionItem item = (FunctionItem) adapter.getItem(position);
            Intent intent  = new Intent(leidaMainActivity.this,item.targetCls);
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
        recyclerView.setAdapter(leidaadapter);
        binding.updateLl.setOnClickListener(v -> {
            FeedbackUtil.getInstance().doFeedback();
            xupdataDef();
        });
       // setContentView(R.layout.activity_leida_main);
    }

    @SuppressLint("ResourceAsColor")
    public void xupdataDef (){
        XUpdate.newBuild(this)
                .updateUrl(testClassurl)
                //.isAutoMode(true) //如果需要完全无人干预，自动更新，需要root权限【静默安装需要】
                .updateParser(new CustomUpdateParser())
                .supportBackgroundUpdate(true)
                //.promptThemeColor(R.color.teal_200)
                //.promptButtonTextColor(Color.RED)
               // .promptTopResId(R.mipmap.bg_update_top3)
                .promptHeightRatio(1.2f)
                .update();
    }

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
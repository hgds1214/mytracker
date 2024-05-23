package com.zeus.tec.ui.tracker;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.zeus.tec.R;
import com.zeus.tec.databinding.ActivityTrackerMainBinding;
import com.zeus.tec.db.TrackerDBManager;
import com.zeus.tec.model.tracker.DrillHoleInfo;
import com.zeus.tec.ui.tracker.adapter.FunctionListAdapter;
import com.zeus.tec.ui.tracker.model.FunctionItem;
import com.zeus.tec.model.utils.FeedbackUtil;

public class TrackerMainActivity extends AppCompatActivity {

    private ActivityTrackerMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(this);
        binding = ActivityTrackerMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.ivBack.setOnClickListener( v->{
            FeedbackUtil.getInstance().doFeedback();
            finish();
        });
        RecyclerView rv = binding.rvList;
        rv.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getDrawable(R.drawable.function_list_divider));
        rv.addItemDecoration(dividerItemDecoration);
        FunctionListAdapter functionListAdapter = FunctionListAdapter.newInstance();
        functionListAdapter.setOnItemClickListener( (adapter, view, position) -> {
            FeedbackUtil.getInstance().doFeedback();
            FunctionItem item = (FunctionItem) adapter.getItem(position);
            Intent intent = new Intent(TrackerMainActivity.this, item.targetCls);
            if( "数据合成".equals(item.label)) {
                DrillHoleInfo lastDrillHoleInfo = TrackerDBManager.getLastDrillHoleInfo();
                if (lastDrillHoleInfo == null || lastDrillHoleInfo.isMerged) {
                    ToastUtils.showLong("请先进行无线数据采集");
                    return;
                }
                intent.putExtra(DataCollectActivity.KEY_DRILL_INFO_ID, lastDrillHoleInfo!=null?lastDrillHoleInfo.id:0);
                intent.putExtra(DataCollectActivity.KEY_TYPE_MERGE, true);
            }
            startActivity(intent);
            }
        );

        rv.setAdapter(functionListAdapter);

    }
}
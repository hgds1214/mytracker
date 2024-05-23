package com.zeus.tec.ui.directionfinder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.blankj.utilcode.util.BarUtils;
import com.zeus.tec.R;
import com.zeus.tec.databinding.ActivityDirectionfinderBinding;
import com.zeus.tec.ui.directionfinder.Apater.directionfinderApater;
import com.zeus.tec.ui.tracker.model.FunctionItem;
import com.zeus.tec.model.utils.FeedbackUtil;

public class directionfinderActivity extends AppCompatActivity {

    private ActivityDirectionfinderBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(this);
      //  binding = ActivityLeidaMainBinding.inflate((getLayoutInflater()));
        binding = ActivityDirectionfinderBinding.inflate((getLayoutInflater()));
       // setContentView(R.layout.activity_directionfinder);
        setContentView(binding.getRoot());
        binding.dirctionfinderBack.setOnClickListener(v -> {
            FeedbackUtil.getInstance().doFeedback();
            finish();
        });
        RecyclerView recyclerView = binding.dirctionfinderList;
        recyclerView.setLayoutManager((new LinearLayoutManager(this)));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getDrawable(R.drawable.function_list_divider));

        recyclerView.addItemDecoration(dividerItemDecoration);
        directionfinderApater directionfinderapater = directionfinderApater.newInstance();
        directionfinderapater.setOnItemClickListener((adapter,view,position)->{
            FeedbackUtil.getInstance().doFeedback();
            FunctionItem item = (FunctionItem) adapter.getItem(position);
            Intent intent  = new Intent(directionfinderActivity.this,item.targetCls);
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
        recyclerView.setAdapter(directionfinderapater);
    }
}
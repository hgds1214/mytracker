package com.zeus.tec.ui.leida;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.BarUtils;
import com.zeus.tec.R;
import com.zeus.tec.databinding.ActivityLeidaMainBinding;
import com.zeus.tec.ui.leida.Apater.leidaListAdapter;
import com.zeus.tec.ui.tracker.model.FunctionItem;
import com.zeus.tec.model.utils.FeedbackUtil;

public class leidaMainActivity extends AppCompatActivity {

    private ActivityLeidaMainBinding binding;
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
       // setContentView(R.layout.activity_leida_main);
    }
}
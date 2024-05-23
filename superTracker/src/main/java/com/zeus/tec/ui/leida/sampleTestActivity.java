package com.zeus.tec.ui.leida;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.tabs.TabLayout;
import com.zeus.tec.R;
import com.zeus.tec.databinding.ActivitySampleTestBinding;
import com.zeus.tec.device.usbserial.USBSerialManager;
import com.zeus.tec.event.MergeEvent;
import com.zeus.tec.model.leida.sampleTest.DataCache;
import com.zeus.tec.model.leida.sampleTest.ILeidaReciveListener;
import com.zeus.tec.ui.leida.util.ConvertCode;
import com.zeus.tec.ui.leida.util.IOtool;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class sampleTestActivity extends AppCompatActivity {
  ActivitySampleTestBinding binding ;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    DataCache cache1 = DataCache.GetInstance();

    DataCache dataCache = DataCache.GetInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySampleTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager viewPager = findViewById(R.id.view_pager);
        //添加tab
        for (int i = 0; i < tabs.length; i++) {
            tabLayout.addTab(tabLayout.newTab().setText(tabs[i]));
        }

        sampleshowactivity sampleshowactivity = new sampleshowactivity();
        cache1.CreateReceiveThread(sampleshowactivity.leidaReciveListener);
        try {
            cache1.CreateSendSocket();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        binding.ivBack.setOnClickListener(v->ivBackClick());
        TestActivity testActivity = new TestActivity();
        tabFragmentList.add(testActivity);
        tabFragmentList.add(sampleshowactivity);
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return tabFragmentList.get(position);
            }

            @Override
            public int getCount() {
                return tabFragmentList.size();
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return tabs[position];
            }
        });
        //设置TabLayout和ViewPager联动
        tabLayout.setupWithViewPager(viewPager,false);
    }
    private void  ivBackClick (){
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cache1.CloseReceiveThread();
        cache1.CloseSendSocket();
    }

    @Override
    public void onBackPressed() {
        //cache1.CloseReceiveThread();
        finish();

    }

    private String[] tabs = {"参数设置", "测试界面"};
    private List<Fragment> tabFragmentList = new ArrayList<>();

}
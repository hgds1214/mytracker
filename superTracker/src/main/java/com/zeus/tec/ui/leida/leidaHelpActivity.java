package com.zeus.tec.ui.leida;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.blankj.utilcode.util.ToastUtils;
import com.zeus.tec.R;
import com.zeus.tec.databinding.ActivityLeidaHelpBinding;
import com.zeus.tec.model.utils.FeedbackUtil;

import java.util.ArrayList;
import java.util.List;

public class leidaHelpActivity extends AppCompatActivity implements View.OnClickListener {

    public ActivityLeidaHelpBinding binding;
    private List<String> leidahelplist = new ArrayList<>();
    private int currentPage = 1;


    private  static final int maxpage = 35;
    private  static final int minpage =1;
    private  int [] abc = new int[4];
    private static final int FLING_MIN_DISTANCE = 50;// 移动最小距离
    private static final int FLING_MIN_VELOCITY = 200;// 移动最大速度
    private  static  final int [] jumpPage = {5,7,10,25};//工作原理，产品介绍，操作步骤，软件分析

    private GestureDetector gestureDetector;
    private MyGestureDetector myGestureDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_leida_help);
        binding = ActivityLeidaHelpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        myGestureDetector=new MyGestureDetector();
        //实例化GestureDetector并将MyGestureDetector实例传入
        gestureDetector=new GestureDetector(this,myGestureDetector);

        initListener();

    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private void initListener() {
        binding.btNextpage.setOnClickListener(this);
        binding.btUppage.setOnClickListener(this);
        binding.btOptStep.setOnClickListener(this);
        binding.btPrjinfo.setOnClickListener(this);
        binding.btWorkinfo.setOnClickListener(this);
        binding.btSoftInfo.setOnClickListener(this);
        binding.ivBack.setOnClickListener(this);
      //  this.setLongClickable(true);


    }

    public class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            int a = 0;
            // 在这里处理滑动事件，例如根据滑动方向执行相应的操作
            // 返回值表示事件是否被消耗，如果返回true，表示消耗了事件，不再传递给其他监听器
            if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE) {
                if (currentPage < maxpage) {
                    currentPage++;
                    refreshPage();
                } else {
                    ToastUtils.showLong("这是最后一页");
                }
                // Toast.makeText(MainActivity.this,"左滑",Toast.LENGTH_SHORT).show();
            } else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE) {
                if (currentPage > minpage) {
                    currentPage--;
                    refreshPage();
                } else {
                    ToastUtils.showLong("这是第一页");
                }
                // Toast.makeText(MainActivity.this,"右滑",Toast.LENGTH_SHORT).show();
            } else if (e1.getY() - e2.getY() > FLING_MIN_DISTANCE) {
             //   ToastUtils.showLong("这是最后一页");
                // Toast.makeText(MainActivity.this,"上滑",Toast.LENGTH_SHORT).show();
            } else if (e2.getY() - e1.getY() > FLING_MIN_DISTANCE) {
                //ToastUtils.showLong("这是最后一页");
                // Toast.makeText(MainActivity.this,"下滑",Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    }

    @Override
    public void onClick(View v) {
        FeedbackUtil.getInstance().doFeedback();
        switch (v.getId()) {
            case R.id.bt_nextpage:
                if (currentPage < maxpage) {
                    currentPage++;
                    refreshPage();
                } else {
                    ToastUtils.showLong("这是最后一页");
                }
                break;
            case R.id.bt_uppage:
                if (currentPage > minpage) {
                    currentPage--;
                    refreshPage();
                } else {
                    ToastUtils.showLong("这是第一页");
                }
                break;
            case R.id.bt_workinfo:
                currentPage=jumpPage[0];
                refreshPage();
                break;
            case R.id.bt_prjinfo:
                currentPage=jumpPage[1];
                refreshPage();
                break;
            case R.id.bt_optStep:
                currentPage=jumpPage[2];
                refreshPage();
                break;
            case R.id.bt_softInfo:
                currentPage=jumpPage[3];
                refreshPage();
                break;
            case R.id.iv_back:
                finish();
        }

    }

    private void refreshPage() {
        //  binding.imageLeidahelp.setImageResource();
        try {
            //  Bitmap showpage = BitmapFactory.decodeFile("res/mipmap-hdpi/leidahelp11.png");
            // int a = getResources().getIdentifier("leidahelp"+11,"mipmap",getPackageName());
            binding.tvPage.setText(String.valueOf(currentPage));
            binding.imageLeidahelp.setImageResource(getResources().getIdentifier("leidahelp" + currentPage, "mipmap", getPackageName()));

        } catch (Exception ex) {
            ToastUtils.showLong(ex.getLocalizedMessage());
        }
    }
}
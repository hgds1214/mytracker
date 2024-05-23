package com.zeus.tec.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.bumptech.glide.Glide;
import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.glide.GlideImageLoader;
import com.zeus.tec.databinding.ActivityFullScreenBinding;
import com.zeus.tec.model.utils.FeedbackUtil;

public class FullScreenActivity extends AppCompatActivity {
    private static final String KEY_URI = "URI";

    private ActivityFullScreenBinding binding;

    public static Intent newIntent(Context source, String uri) {
        Intent intent = new Intent(source, FullScreenActivity.class);
        intent.putExtra(KEY_URI,uri);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(this);
        BigImageViewer.initialize(GlideImageLoader.with(getApplicationContext()));
        binding = ActivityFullScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.ivBack.setOnClickListener( view -> {
            FeedbackUtil.getInstance().doFeedback();
            finish();
        });

        Glide.get(getApplicationContext()).clearMemory();
        binding.bigImage.showImage(Uri.parse(getIntent().getStringExtra(KEY_URI)));

        binding.bigImage.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (binding.bigImage.getSSIV() != null) {
                    try {
                        Bitmap bitmap = ImageUtils.getBitmap(binding.bigImage.getCurrentImageFile());
                        binding.bigImage.getSSIV().setScaleAndCenter(ScreenUtils.getScreenWidth()/(float)bitmap.getWidth(), new PointF(0, 0));
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 150);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Object>() {
            @Override
            public Object doInBackground() throws Throwable {
                Glide.get(getApplicationContext()).clearDiskCache();
                return null;
            }

            @Override
            public void onSuccess(Object result) {

            }
        });
    }
}
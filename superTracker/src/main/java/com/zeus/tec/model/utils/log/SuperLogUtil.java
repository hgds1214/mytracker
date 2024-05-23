package com.zeus.tec.model.utils.log;

import android.app.Activity;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hjq.xtoast.XToast;
import com.zeus.tec.R;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SuperLogUtil {
    public static final String TAG = ">";
    private StringBuilder sb = new StringBuilder();
    private XToast xToast;
    private TextView tvMessage;
    private ScrollView sv;

    public SuperLogUtil(Activity context) {
        if (xToast !=null) return;
        xToast = new XToast<>(context)
                .setContentView(R.layout.layout_debug_info)
                // 设置成可拖拽的
                .setDraggable()
                // 设置显示时长
                .setDuration(0)
                // 设置外层是否能被触摸
                .setOutsideTouchable(true)
                // 设置窗口背景阴影强度
                //.setBackgroundDimAmount(0.5f)
                .setOnClickListener(R.id.tv_close, (XToast.OnClickListener<TextView>) (toast, view) -> toast.cancel())
                .setOnClickListener(R.id.tv_save, (XToast.OnClickListener<View>) (toast, view) -> {
                    saveLog();
                });
        tvMessage = (TextView) xToast.findViewById(R.id.tv_message);
        sv = (ScrollView) xToast.findViewById(R.id.sv);
        tvMessage.setOnClickListener(v -> {
            //sv.fullScroll(ScrollView.FOCUS_DOWN);
            sb = new StringBuilder();
            tvMessage.setText("");
        });
        superLogUtilWeakReference = new WeakReference<>(this);
    }

    public void show() {
        if (xToast == null) return;
        xToast.show();
    }

    private void saveLog() {
        ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<String>() {
            @Override
            public String doInBackground() throws Throwable {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
                    String logPath = PathUtils.getExternalDownloadsPath() + File.separator +
                            sdf.format(new Date(System.currentTimeMillis())) + ".txt";
                    FileIOUtils.writeFileFromString(logPath, sb.toString());
                    return logPath;
                }catch (Exception e) {
                }
                return null;
            }

            @Override
            public void onSuccess(String result) {
                if (result == null || result.isEmpty()) {
                    ToastUtils.showLong("日志保存失败");
                    return;
                }
                ToastUtils.showLong("日志保存成功，文件路径："+result);
            }
        });

    }

    public void d(String message) {
        d(TAG, message);
    }

    public void d(String tag, String message) {
        if (ThreadUtils.isMainThread()) {
            _d(tag, message);
        } else {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    _d(tag, message);
                }
            });
        }
    }
    public void _d(String tag, String message) {
        if (ssb.length()>0) {
            sb.append(ssb).append("\n");
            ssb = new StringBuffer();
        }
        LogUtils.dTag(tag, message);
        sb.append(tag).append(": ").append(message).append("\n");
        tvMessage.setText(sb.toString());

        sv.postDelayed(new Runnable() {
            @Override
            public void run() {
                sv.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }, 100);
    }

    private static StringBuffer ssb = new StringBuffer();
    private static WeakReference<SuperLogUtil> superLogUtilWeakReference;
    public static void sd(String text) {
        if (superLogUtilWeakReference == null) return;

        SuperLogUtil superLogUtil = superLogUtilWeakReference.get();
        if (superLogUtil == null) {
            ssb.append(text);
            return;
        }

        superLogUtil.d(text);
    }

}

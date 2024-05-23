package com.zeus.tec.ui.base;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.LogUtils;
import com.zeus.tec.R;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(this);
    }

    public boolean isShowLoading() {
        return isShowLoading;
    }

    private boolean isShowLoading;
    private Dialog dlgLoading;
    protected void showLoading() {
        if( isShowLoading ) return;
        isShowLoading = true;
        if (dlgLoading == null) {
            dlgLoading = new Dialog(this, R.style.ZeusDialog);
            dlgLoading.setContentView(R.layout.dialog_loading);
            dlgLoading.setCanceledOnTouchOutside(false);
            dlgLoading.setCancelable(false);
            /*dlgLoading.setOnDismissListener(dialogInterface -> {
                isShowLoading = false;
            });
            dlgLoading.setOnShowListener(dialogInterface -> {
                isShowLoading = true;
            });*/
        }
        if(!dlgLoading.isShowing()) {
            dlgLoading.show();
        }
    }
    protected void hideLoading() {
        isShowLoading = false;
        if (dlgLoading == null) return;
        if(dlgLoading.isShowing()) {
            dlgLoading.hide();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideLoading();
    }
}

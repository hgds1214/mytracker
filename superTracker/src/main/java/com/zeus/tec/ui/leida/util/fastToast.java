package com.zeus.tec.ui.leida.util;

import android.annotation.SuppressLint;

import com.blankj.utilcode.util.ToastUtils;
import com.zeus.tec.R;

public class fastToast {

    @SuppressLint("ResourceAsColor")
    public static void showToast (String input){
        ToastUtils toastUtils =  ToastUtils.make();
        toastUtils.setBgColor(R.color.design_default_color_background);
        toastUtils.setLeftIcon(R.mipmap.logo);
        toastUtils.setTextColor(R.color.program_text_color);
        toastUtils.show(input);
    }
}

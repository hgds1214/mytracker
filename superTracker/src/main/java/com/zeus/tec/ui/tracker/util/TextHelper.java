package com.zeus.tec.ui.tracker.util;

import android.text.TextUtils;

import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.ToastUtils;

public class TextHelper {


    public static boolean isInvalidTextAndShowWarn(String text, String preTip) {
        if (preTip==null) preTip = "";
        if (TextUtils.isEmpty(text)) {
            ToastUtils.showLong(preTip + "不能为空！");
            return true;
        }
        if (!RegexUtils.isMatch("[0-9a-zA-Z_\\-.@#$%,\\s]+", text)) {
            ToastUtils.showLong(preTip + "只能是如下字符: 空格 数字、字母、_ - . @ # $ % ,");
            return true;
        }
        return false;
    }

    public static String safeString(String text) {
        return text == null ? "" : text;
    }
}

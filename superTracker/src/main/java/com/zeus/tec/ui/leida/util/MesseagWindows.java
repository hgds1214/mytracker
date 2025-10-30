package com.zeus.tec.ui.leida.util;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.zeus.tec.R;
import com.zeus.tec.ui.leida.ProjectleidainfoActivity;
import com.zeus.tec.ui.leida.interfaceUtil.DialogCallback;

public class MesseagWindows  {

    public static EditText input = null;
    public static void showMessageBox(Context context, String title, String message, DialogCallback callback) {
            androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.onPositiveButtonClick();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.onNegativeButtonClick();
                    }
                })
                .show();
    }

    public static void showMaoganMessageBox(Context context, String title, String message, DialogCallback callback) {
        androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(context);
        input = new EditText(context);
        input.setHint("请输入流水号");
//        builder.setView(input);
        builder.setTitle(title)
                .setMessage(message)
                .setView(input)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.onPositiveButtonClick();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.onNegativeButtonClick();
                    }
                })
                .show();
    }


}

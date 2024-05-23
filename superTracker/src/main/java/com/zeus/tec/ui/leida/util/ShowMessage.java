package com.zeus.tec.ui.leida.util;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;

public  class ShowMessage {
    public static void showMessageBox(Context context, String title, String message) {
        context1 = context;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle OK button click


                    }
                })
                .show();
    }
    static Context context1;

    @SuppressLint("HandlerLeak")
    private static Handler handler = new Handler(){
        public void handleMessage(@NonNull Message msg){
            switch (msg.what){
                case 1:
                    Toast.makeText(context1,(String)msg.obj,Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;

            }

        }
    };
}

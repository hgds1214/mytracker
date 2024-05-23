package com.zeus.tec.ui.leida.Apater;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.zeus.tec.R;
import com.zeus.tec.ui.leida.ProjectleidainfoActivity;

import java.util.List;

public class leidaAdapter extends ArrayAdapter<String> {


    public leidaAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
    }

    //public ArrayAdapter<String> startAdapter = new ArrayAdapter<String>(context1, R.layout.item_select,itemList1);
   // public  ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<String>(context1, R.layout.item_select,itemList2);



}

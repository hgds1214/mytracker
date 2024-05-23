package com.zeus.tec.ui.leida.util;

import java.text.DecimalFormat;

public class DecimalFormat1 {


    public  static String [] mode = {"#.0","#.0","#.00","#.000","#0.0000"};
    public  static  String getdecimalFormat (float input,int Mode){
        DecimalFormat decimalFormat = new DecimalFormat(mode[Mode]);
        String formattedValue = decimalFormat.format(input);
        return  formattedValue;
    }
}

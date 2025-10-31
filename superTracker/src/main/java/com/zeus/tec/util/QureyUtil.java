package com.zeus.tec.util;

import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.List;

public class QureyUtil {




    public static <T> List<T> qureyList(List<T> inputList,int offset,int limit) {
        List<T> tmpList ;
        if (inputList.size()<limit+offset) {
            tmpList = inputList.subList(offset,inputList.size());
        }
        else {
            tmpList = inputList.subList(offset,offset+limit);
        }
        return tmpList;
    }


}

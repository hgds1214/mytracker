package com.zeus.tec.ui.maoganDataUpload;

import java.io.File;

public interface IMaoganDataUpdata {
    public void updataData (File dataPath);
    public void deleteData (File dataPath);
    public void clickCheckBox (int position,boolean isCheck);

}

package com.zeus.tec.ui.maoganDataUpload;

import java.io.File;
import java.util.List;

public interface IMaoganDataUpdata {
    public void updataData (List <File> updataFileList);
    public void deleteData (List<File>  deleteFileList);
    public void clickCheckBox (int position,boolean isCheck);
    public void refreshList (File currentFile);
    public void shareData(List<File> shareDataList);

}

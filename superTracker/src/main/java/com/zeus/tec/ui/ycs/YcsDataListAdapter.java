package com.zeus.tec.ui.ycs;

import android.graphics.Color;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.BaseLoadMoreModule;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.zeus.tec.R;

import java.io.File;

public class YcsDataListAdapter extends BaseQuickAdapter<YcsDataFileInfo, BaseViewHolder> implements LoadMoreModule {

    public YcsDataListAdapter() {
        super(R.layout.ycsdata_list_item);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, YcsDataFileInfo file) {
        String idtext = String.format("项目名称-%s", file.projectName, file.projectName);
        baseViewHolder.setText(R.id.tv_id, idtext);

        if (!file.trdFile.equals("")){
            baseViewHolder.setText(R.id.tv_trd_file, file.trdFile);
            baseViewHolder.setTextColor(R.id.tv_trd_file,Color.BLACK);
        }
        if (!file.datFile.equals("")){
            baseViewHolder.setText(R.id.tv_data_file, file.datFile);
            baseViewHolder.setTextColor(R.id.tv_data_file,Color.BLACK);
        }
        if (!file.zipFile.equals("")){
            baseViewHolder.setText(R.id.tv_zip_file, file.zipFile);
            baseViewHolder.setTextColor(R.id.tv_zip_file,Color.BLACK);
        }
        if (!file.x_ycs_file.equals("")){
            baseViewHolder.setText(R.id.tv_x_ycs_file, "合成成功");
            baseViewHolder.setTextColor(R.id.tv_x_ycs_file,Color.GREEN);
        }
        if (!file.y_ycs_file.equals("")){
            baseViewHolder.setText(R.id.tv_y_ycs_file, "合成成功");
            baseViewHolder.setTextColor(R.id.tv_y_ycs_file,Color.GREEN);
        }
        if (!file.z_ycs_file.equals("")){
            baseViewHolder.setText(R.id.tv_z_ycs_file, "合成成功");
            baseViewHolder.setTextColor(R.id.tv_z_ycs_file,Color.GREEN);
        }


    }

    @NonNull
    @Override
    public BaseLoadMoreModule addLoadMoreModule(@NonNull BaseQuickAdapter<?, ?> baseQuickAdapter) {
        return new BaseLoadMoreModule(baseQuickAdapter);
    }
}

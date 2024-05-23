package com.zeus.tec.ui.directionfinder.Apater;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.BaseLoadMoreModule;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.zeus.tec.R;
import com.zeus.tec.model.directionfinder.dirctionfinderDrillHoleInfo;

import java.text.SimpleDateFormat;

public class directionfinderDataListAdapater extends BaseQuickAdapter<dirctionfinderDrillHoleInfo ,BaseViewHolder> implements LoadMoreModule {
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public directionfinderDataListAdapater() {
        super(R.layout.directionfinderdata_list_item);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, dirctionfinderDrillHoleInfo dirctionfinderInfo) {
        String idtext = String.format("项目名称-%s", dirctionfinderInfo.projectName, dirctionfinderInfo.projectName);
        baseViewHolder.setText(R.id.tv_id, idtext);

        baseViewHolder.setText(R.id.tv_drill_hole_id, String.valueOf(dirctionfinderInfo.drillHoleId));

        baseViewHolder.setText(R.id.tv_detector, String.valueOf(dirctionfinderInfo.detector));

        baseViewHolder.setText(R.id.tv_time, sdf.format(dirctionfinderInfo.collectionDateTime));


        baseViewHolder.setGone(R.id.tv_export, dirctionfinderInfo.isMerged);
        baseViewHolder.setGone(R.id.tv_share, dirctionfinderInfo.isMerged);
        baseViewHolder.setGone(R.id.tv_view, !dirctionfinderInfo.isMerged);
        baseViewHolder.setGone(R.id.tv_merge, dirctionfinderInfo.isMerged || dirctionfinderInfo.collectCount > 0);
        baseViewHolder.setGone(R.id.tv_continue, dirctionfinderInfo.isMerged || dirctionfinderInfo.collectCount > 0);
    }

    @Override
    public BaseLoadMoreModule addLoadMoreModule(BaseQuickAdapter<?, ?> baseQuickAdapter) {
        return  new BaseLoadMoreModule(baseQuickAdapter);
    }
}

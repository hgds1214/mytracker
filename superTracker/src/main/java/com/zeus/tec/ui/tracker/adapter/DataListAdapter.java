package com.zeus.tec.ui.tracker.adapter;

import com.blankj.utilcode.util.LogUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.BaseLoadMoreModule;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.zeus.tec.R;
import com.zeus.tec.model.tracker.DrillHoleInfo;

import java.text.SimpleDateFormat;

/**
 * Created by AllenWang on 2022/8/18.
 */
public class DataListAdapter extends BaseQuickAdapter<DrillHoleInfo, BaseViewHolder> implements LoadMoreModule {
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public DataListAdapter() {
        super(R.layout.data_list_item);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, DrillHoleInfo drillHoleInfo) {
        String idtext = String.format("矿区编号-%s", drillHoleInfo.miningAreaId, drillHoleInfo.miningAreaId);
        baseViewHolder.setText(R.id.tv_id, idtext);

        baseViewHolder.setText(R.id.tv_drill_hole_id, String.valueOf(drillHoleInfo.drillHoleId));

        baseViewHolder.setText(R.id.tv_detector, String.valueOf(drillHoleInfo.detector));

        baseViewHolder.setText(R.id.tv_time, sdf.format(drillHoleInfo.collectionDateTime));


        baseViewHolder.setGone(R.id.tv_export, false);
        baseViewHolder.setGone(R.id.tv_share, !drillHoleInfo.isMerged);
        baseViewHolder.setGone(R.id.tv_view, !drillHoleInfo.isMerged);
        baseViewHolder.setGone(R.id.tv_merge, drillHoleInfo.isMerged || drillHoleInfo.collectCount == 0);
        baseViewHolder.setGone(R.id.tv_continue, drillHoleInfo.isMerged || drillHoleInfo.collectCount > 0);
    }

    @Override
    public BaseLoadMoreModule addLoadMoreModule(BaseQuickAdapter<?, ?> baseQuickAdapter) {
        return new BaseLoadMoreModule(baseQuickAdapter);
    }
}

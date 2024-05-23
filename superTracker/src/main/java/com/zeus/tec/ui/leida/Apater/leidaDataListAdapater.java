package com.zeus.tec.ui.leida.Apater;

import android.widget.BaseAdapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.BaseLoadMoreModule;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.zeus.tec.R;
import com.zeus.tec.model.leida.leida_info;

public class leidaDataListAdapater extends BaseQuickAdapter<leida_info, BaseViewHolder>implements LoadMoreModule {

    public leidaDataListAdapater() {
        super(R.layout.leidadata_list_item);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, leida_info info) {
        String idtext = String.format("项目名称-%s", info.projectId, info.projectId);
        baseViewHolder.setText(R.id.tv_id, idtext);

        baseViewHolder.setText(R.id.tv_drill_hole_id, String.valueOf(info.PointCount));

        baseViewHolder.setText(R.id.tv_detector, String.valueOf(info.TotalDis));

        baseViewHolder.setText(R.id.tv_time, info.creatTime);

        baseViewHolder.setGone(R.id.tv_export, !info.isMerged);
        baseViewHolder.setGone(R.id.tv_share, info.isMerged);
        baseViewHolder.setGone(R.id.tv_view, info.isMerged);
        baseViewHolder.setGone(R.id.tv_merge, info.isMerged );
        baseViewHolder.setGone(R.id.tv_continue, info.isMerged);
    }

    @Override
    public BaseLoadMoreModule addLoadMoreModule(BaseQuickAdapter<?, ?> baseQuickAdapter) {
        return new BaseLoadMoreModule(baseQuickAdapter);
    }
}

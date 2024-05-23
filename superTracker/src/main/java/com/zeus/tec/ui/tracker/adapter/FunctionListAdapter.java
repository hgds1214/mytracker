package com.zeus.tec.ui.tracker.adapter;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.zeus.tec.R;
import com.zeus.tec.db.TrackerDBManager;
import com.zeus.tec.model.tracker.DrillHoleInfo;
import com.zeus.tec.ui.tracker.AdjustActivity;
import com.zeus.tec.ui.tracker.DataCollectActivity;
import com.zeus.tec.ui.tracker.DataViewActivity;
import com.zeus.tec.ui.tracker.ProjectInfoEditActivity;
import com.zeus.tec.ui.tracker.SettingActivity;
import com.zeus.tec.ui.tracker.model.FunctionItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AllenWang on 2022/8/13.
 */
public class FunctionListAdapter extends BaseQuickAdapter<FunctionItem, BaseViewHolder> {

    public FunctionListAdapter(List<FunctionItem> data) {
        super(R.layout.item_function_list_for_tracker, data);

    }

    public static FunctionListAdapter newInstance() {
        List<FunctionItem> data = new ArrayList<>();
        data.add(new FunctionItem("采集运行", R.mipmap.collect, ProjectInfoEditActivity.class));
        data.add(new FunctionItem("数据合成", R.mipmap.merge_data, DataCollectActivity.class));
        data.add(new FunctionItem("数据浏览", R.mipmap.view_data, DataViewActivity.class));
        data.add(new FunctionItem("罗盘校准", R.mipmap.ic_adjust, AdjustActivity.class));
        data.add(new FunctionItem("系统设置", R.mipmap.ic_setting, SettingActivity.class));

        return new FunctionListAdapter(data);
    }



    @Override
    protected void convert(BaseViewHolder baseViewHolder, FunctionItem functionItem) {
        ((ImageView)baseViewHolder.findView (R.id.iv_icon)).setImageResource(functionItem.resIcon);
        ((TextView)baseViewHolder.findView (R.id.tv_label)).setText(functionItem.label);
    }
}
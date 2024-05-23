package com.zeus.tec.ui.leida.Apater;

import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.zeus.tec.R;
import com.zeus.tec.ui.leida.LeidaDataCollectActivity;
import com.zeus.tec.ui.leida.LeidaDataveiewActivity;
import com.zeus.tec.ui.leida.MiraSampleActivity;
import com.zeus.tec.ui.leida.leidaHelpActivity;
import com.zeus.tec.ui.leida.sampleTestActivity;
import com.zeus.tec.ui.tracker.AdjustActivity;
import com.zeus.tec.ui.tracker.DataCollectActivity;
import com.zeus.tec.ui.tracker.SettingActivity;
import com.zeus.tec.ui.tracker.model.FunctionItem;

import java.util.ArrayList;
import java.util.List;

public class leidaListAdapter extends BaseQuickAdapter<FunctionItem, BaseViewHolder> {

    public leidaListAdapter( List<FunctionItem> data) {
        super(R.layout.item_function_list_for_tracker, data);
    }

    public static leidaListAdapter newInstance() {
        List<FunctionItem> data = new ArrayList<>();
        data.add(new FunctionItem("采集运行", R.mipmap.collect, LeidaDataCollectActivity.class));
        data.add(new FunctionItem("设备调试", R.mipmap.test_device, sampleTestActivity.class));
        data.add(new FunctionItem("数据浏览", R.mipmap.view_data, LeidaDataveiewActivity.class));
        data.add(new FunctionItem("操作说明", R.mipmap.merge_data, leidaHelpActivity.class));
        data.add(new FunctionItem("系统设置", R.mipmap.ic_setting, SettingActivity.class));
        return new leidaListAdapter(data);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, FunctionItem functionItem) {
        ((ImageView)baseViewHolder.findView (R.id.iv_icon)).setImageResource(functionItem.resIcon);
        ((TextView)baseViewHolder.findView (R.id.tv_label)).setText(functionItem.label);
    }
}

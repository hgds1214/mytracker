package com.zeus.tec.ui.directionfinder.Apater;

import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.zeus.tec.R;
import  com.zeus.tec.ui.directionfinder.directionProjectInfoActivity;
import com.zeus.tec.ui.directionfinder.directionfinderDataViewActivity;
import com.zeus.tec.ui.tracker.SettingActivity;
import com.zeus.tec.ui.tracker.model.FunctionItem;

import java.util.ArrayList;
import java.util.List;

public class directionfinderApater extends BaseQuickAdapter<FunctionItem, BaseViewHolder> {
    public directionfinderApater( List<FunctionItem> data) {
        super(R.layout.item_function_list_for_tracker, data);
    }

    public static directionfinderApater newInstance() {
        List<FunctionItem> data = new ArrayList<>();
        data.add(new FunctionItem("开始采集", R.mipmap.collect, directionProjectInfoActivity.class));
        //data.add(new FunctionItem("数据合成", R.mipmap.merge_data, DataCollectActivity.class));
        data.add(new FunctionItem("数据浏览", R.mipmap.view_data, directionfinderDataViewActivity.class));
       // data.add(new FunctionItem("罗盘校准", R.mipmap.ic_adjust, AdjustActivity.class));
        data.add(new FunctionItem("系统设置", R.mipmap.ic_setting, SettingActivity.class));
        return new directionfinderApater(data);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, FunctionItem functionItem) {
        ((ImageView)baseViewHolder.findView (R.id.iv_icon)).setImageResource(functionItem.resIcon);
        ((TextView)baseViewHolder.findView (R.id.tv_label)).setText(functionItem.label);
    }
}

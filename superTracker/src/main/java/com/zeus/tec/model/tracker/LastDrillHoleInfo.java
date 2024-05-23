package com.zeus.tec.model.tracker;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * Created by AllenWang on 2022/8/9.
 */
@Entity
public class LastDrillHoleInfo {
    @Id
    public long id;

    // 企业编号
    public String companyId;
    // 矿区编号
    public String miningAreaId;
    // 工作面名称
    public String namespaceName;
    // 钻井编号
    public String drillHoleId;
    // 检测人员
    public String detector;
    // 动态阈值
    public double dynamicThreshold;
    // 钻杆长度
    public double drillPipeLength;
    // 钻孔深度
    public double drillHoleLength;

    // 孔径坐标 X
    public double holeX;
    // 孔径坐标 Y
    public double holeY;
    // 孔径坐标 Z
    public double holeZ;
    // 护套长度
    public double jacketLength;
    // 设计方位
    public String designDirection;
    // 设计倾角
    public String designAngle;
    // 校准模式
    public String adjustMode;

}

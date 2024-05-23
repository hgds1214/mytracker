package com.zeus.tec.model.utils;

import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import com.zeus.tec.model.tracker.CollectTimeInfo;

import java.util.ArrayList;
import java.util.List;

public class DataUtils {
    private static final SpacePoint design_begin = new SpacePoint();
    private static final SpacePoint design_end = new SpacePoint();

    static class AS {
        float pitch = 0;
        float heading = 0;

        public AS(float pitch, float heading) {
            this.pitch = pitch;
            this.heading = heading;
        }
    }
    public List<RealPoint> fakePoints() {
        List<AS> list = new ArrayList<>();
        list.add(new AS(-0.47f, 10.09f));   // x y z  0.32  -3.98     0.22
        list.add(new AS(-0.43f, 11.46f));  // x y z  0.52  -7.97     0.46
        list.add(new AS(33.87f,20.75f ));  // x y z  0.75  -11.95    0.76
        list.add(new AS(32.06f, 20.95f));  // x y z  1.05  -15.92    1.12
        list.add(new AS(32.81f, 186.66f));  // x y z
        list.add(new AS(32.36f, 184.50f));  // x y z
        list.add(new AS(-0.29f, 357.33f));  // x y z

        GetXYZByAverageAngle(design_end, 0, 0, -0.47f, 10.09f, 2f*list.size());

        List<RealPoint> r = new ArrayList<>();
        SpacePoint sp=new SpacePoint();
        float curO, curD;
        float len = 2f;
        float lastO = -0.47f, lastD = 10.09f;
        for (AS ti: list) {
            curO = ti.pitch;
            curD = ti.heading;
            Log.d("xyz>>>>>>", "last:"+lastO +","+lastD + ",   curent: " + curO + ", " + curD + ",    len: " + (len));
            GetXYZByAverageAngle(sp, lastO, lastD, curO, curD, len);

            Log.d("xyz>>>>>>", sp.toString());
            RealPoint realPoint = ComputeDesignPanelCoordinate(sp.x, sp.y, sp.z, 0, 0);
            r.add(realPoint);
            lastO = curO;
            lastD = curD;
        }
        return r;

    }

    public List<RealPoint> calculatePoints(List<CollectTimeInfo> list, float designOmega, float designDirection, float len) {
        len = len / 100f;
        List<RealPoint> r = new ArrayList<>();
        if (list == null || list.isEmpty()) return r;

        CollectTimeInfo first = list.get(0);

        GetXYZByAverageAngle(design_end, 0, 0,
                designOmega, designDirection,
                len * list.size());

        SpacePoint sp = new SpacePoint();
        float lastO = first.omega/100f, lastD = first.directionAngle/100f;
        float curO, curD;
        for (CollectTimeInfo ti: list) {
            curO = ti.omega/100f;
            curD = ti.directionAngle / 100f;
            sp = GetXYZByAverageAngle(sp, lastO, lastD, curO, curD, len);
            RealPoint realPoint = ComputeDesignPanelCoordinate(sp.x, sp.y, sp.z, designOmega, designDirection);
            r.add(realPoint);
            lastO = curO;
            lastD = curD;
        }
        return r;
    }
    // 每个点x,yz 是通过当前点 俯仰角  方向角  与上个点的 俯仰角  方向角 计算平均角度差
    public SpacePoint GetXYZByAverageAngle(SpacePoint point, float start_pitch, float start_heading, float end_pitch, float end_heading, float depth){
        float  AvPitch=0;
        float  AvHeading=0;
        start_pitch=start_pitch;
        end_pitch=end_pitch;
        AvPitch=(start_pitch+end_pitch)/2.0f;
        AvHeading=(start_heading+end_heading)/2.0f;
        if((end_heading-start_heading)>180){
            AvHeading -=180;
        }
        if((end_heading-start_heading)<-180){
            AvHeading +=180;
        }

        //角度转弧度
        AvPitch= (float) (AvPitch*Math.PI/180.0f);
        AvHeading= (float) (AvHeading*Math.PI/180.0f);
        float f_d=1-(AvPitch*AvPitch)/24.0f;
        float f_h=1-(AvPitch*AvPitch+AvHeading*AvHeading)/24.0f;
        float d_depth=depth;
        float d_distance= (float) (d_depth*Math.sin(AvPitch));
        float l_distance= (float) Math.abs(d_depth*Math.cos(AvPitch));
        float e_distance= (float) (l_distance*Math.sin(AvHeading));
        float n_distance= (float) (l_distance*Math.cos(AvHeading));

        point.z +=  (d_distance);
        //if(L_Dis)  *L_Dis = l_distance;
        point.x +=  (e_distance);
        point.y +=  (n_distance);
        return point;
    }

    // x y z 为点的东坐标  北坐标 深度坐标    pitch为设计孔俯仰角    azimuth 为设计孔方位角
    public RealPoint ComputeDesignPanelCoordinate(float x,float y,float z,float pitch, float azimuth)
    {
        float Rad_Des_Yaw = (float) (azimuth*Math.PI / 180.0);//方位角 ，单位：弧度
        float Rad_Des_Ang = (float) (pitch*Math.PI / 180.0);//俯仰角 ，单位：弧度
        float SIN_A = (float) Math.sin(Rad_Des_Yaw);
        float COS_A = (float) Math.cos(Rad_Des_Yaw);
        float SIN_B = (float) Math.sin(Rad_Des_Ang);
        float COS_B = (float) Math.cos(Rad_Des_Ang);
        SpacePoint p1 = new SpacePoint();
        p1.x = 0;
        p1.y = 0;
        p1.z = 0;
        SpacePoint p2 = new SpacePoint();
        p2.x = COS_B*SIN_A;
        p2.y = COS_B*COS_A;
        p2.z = SIN_B;
        SpacePoint p3= new SpacePoint();
        p3.x = COS_A;
        p3.y = -SIN_A;
        p3.z = 0;
        float[] p = new float[4];
        GetDesignPanel(p1, p2, p3, p);

        SpacePoint point = new SpacePoint();
        point.x = x;   //东
        point.y = y;  //北
        point.z = z;  //深度


        float UD_dis=DisUDPlane(point, p[0], p[1], p[2], p[3]);

        SpacePoint Plane_Foot = GetFootOf3DPlane(point, p[0], p[1], p[2], p[3]);

        SpacePoint line_foot = GetFootOf3DPerpendicular(Plane_Foot, p1, p2);

        if (Plane_Foot.z > point.z)
        {
            UD_dis = -1.0f*Math.abs(UD_dis);
        }
        else
        {
            UD_dis= Math.abs(UD_dis);
        }

        int sign = LeftOrRight(p1.x, p1.y, p2.x, p2.y, Plane_Foot.x, Plane_Foot.y);

        float dis = DistancePoints(line_foot, Plane_Foot)*sign;

        sign = PointToSegDist(design_begin.x, design_begin.y, design_end.x, design_end.y, Plane_Foot.x, Plane_Foot.y);
        float space = DistancePoints(line_foot, p1);
        if (sign == -1)
        {
            space = space * -1.0f;
        }

        RealPoint pt = new RealPoint();

        pt.dis = fix2(dis);      //  左右偏移坐标图 测量点与预计孔道左右偏移距离 左：正   右：负
        pt.high = fix2(UD_dis);   //  测量点与预计孔道左右偏移距离  测量点与预计孔道上下偏移距离  上：正  下：负
        pt.space = fix2(space);   //偏移坐标图中 实际测量点与设计孔线垂足在水平面的投影到原点的距离。

        return pt;
    }

    private float fix2(float n) {
        int round = Math.round(n * 100);
        return round/100f;
    }

    public static class RealPoint {
        public float dis;
        public float high;
        public float space;
    }

    static class SpacePoint
    {
        float x;
        float y;
        float z;

        @Override
        public String toString() {
            return "SpacePoint{" +
                    "x=" + x +
                    ", y=" + y +
                    ", z=" + z +
                    '}';
        }
    };


    //计算设计轨迹所在平面
    void  GetDesignPanel(SpacePoint p1, SpacePoint p2, SpacePoint p3, float[] P)
    {
        P[0] = ((p2.y - p1.y)*(p3.z - p1.z) - (p2.z - p1.z)*(p3.y - p1.y));
        P[1] = ((p2.z - p1.z)*(p3.x - p1.x) - (p2.x - p1.x)*(p3.z - p1.z));
        P[2] = ((p2.x - p1.x)*(p3.y - p1.y) - (p2.y - p1.y)*(p3.x - p1.x));
        P[3] = (0 - (P[0] * p1.x + P[1] * p1.y + P[2] * p1.z));
    }

    //点到平面距离
    float  DisUDPlane(SpacePoint p, float A , float B , float C , float D)
    {
        float PTP_Distance = 0;
        float tmp = A *A + B * B + C * C;
        tmp = (float) Math.sqrt(tmp);
        PTP_Distance = A * p.x + B * p.y + C * p.z + D;
        if (tmp < 0.00000001)
            PTP_Distance = 0;
        else
            PTP_Distance = PTP_Distance / tmp;
        return PTP_Distance;
    }


    //计算平面垂点坐标
    SpacePoint GetFootOf3DPlane(SpacePoint p, float A, float B, float C, float D)
    {
        float tmp = A * A + B * B + C* C;
        float tmp1 = A * (B * p.y + C * p.z + D);
        float x1 = (B * B + C * C)*p.x - tmp1;
        if (tmp < 0.00000001)
            x1 = 0;
        else
            x1 = x1 / tmp;

        tmp1 = B * (A * p.x + C * p.z + D);
        float y1 = (A * A + C * C)*p.y - tmp1;
        if (tmp < 0.00000001)
            y1 = 0;
        else
            y1 = y1 / tmp;

        tmp1 = C * (A * p.x + B * p.y + D);
        float z1 = (A  * A + B * B)*p.z - tmp1;
        if (tmp < 0.00000001)
            z1 = 0;
        else
            z1 = z1 / tmp;

        SpacePoint pt=new SpacePoint();
        pt.x = x1;
        pt.y = y1;
        pt.z = z1;

        return  pt;
    }


    //计算锤点到设计线垂足坐标
    SpacePoint GetFootOf3DPerpendicular(SpacePoint pt, SpacePoint begin, SpacePoint end)
    {
        SpacePoint retVal = new SpacePoint();
        float dx;
        dx = begin.x - end.x;
        float dy;
        dy = begin.y - end.y;
        float dz;
        dz = begin.z - end.z;
        if (Math.abs(dx) < 0.00000001 && Math.abs(dy) < 0.00000001 && Math.abs(dz) < 0.00000001)
        {
            return null;
        }

        float u = (pt.x - begin.x)*(begin.x - end.x) + (pt.y - begin.y)*(begin.y - end.y) + (pt.z - begin.z)*(begin.z - end.z);
        u = u / ((dx*dx) + (dy*dy) + (dz*dz));
        retVal = new SpacePoint();
        retVal.x = begin.x + u*dx;
        retVal.y = begin.y + u*dy;
        retVal.z = begin.z + u*dz;
        return retVal;

    }



    int  LeftOrRight(float x1, float y1, float x2, float y2, float x, float y)
    {
        float ax = x2 - x1;
        float ay = y2 - y1;
        float bx = x - x1;
        float by = y - y1;
        float judge = ax*by - ay*bx;
        if (judge>0.00000001)
            return 1;
        else if (judge<-0.00000001)
            return -1;
        else
            return 0;
    }


    float  DistancePoints(SpacePoint p1, SpacePoint p2) {
        float s = 0.0f;
        s = (p1.x - p2.x)*(p1.x - p2.x) + (p1.y - p2.y)*(p1.y - p2.y) + (p1.z - p2.z)*(p1.z - p2.z);
        s = (float)Math.sqrt(s);
        return s;
    }


    int  PointToSegDist(float x1, float y1, float x2, float y2, float x, float y)
    {
        float cross = (x2 - x1) * (x - x1) + (y2 - y1) * (y - y1);
        float d2 = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
        if (cross <= 0.00000001)
            return -1;
        else if (cross >= d2)
            return 1;
        else
            return 0;

    }



}

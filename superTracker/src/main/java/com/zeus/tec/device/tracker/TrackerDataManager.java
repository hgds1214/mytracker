package com.zeus.tec.device.tracker;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.zeus.tec.BuildConfig;
import com.zeus.tec.model.tracker.CollectTimeInfo;
import com.zeus.tec.model.tracker.DrillHoleInfo;
import com.zeus.tec.model.utils.log.SuperLogUtil;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by AllenWang on 2022/8/10.
 */
public class TrackerDataManager {
    private static String version = "YZG3.6";

    private static byte[] formatBytesToLength(int length, byte[] bytes) {
        byte[] result = new byte[length];
        int len = Math.min(bytes.length, result.length);
        for (int i=0; i<len; i++) {
            result[i] = bytes[i];
        }
        for (int i=len; i<result.length; i++) {
            result[i] = 0x20;
        }
        return result;
    }

    private static byte[] formatStringToBytes(int fixLen, String text) {
        byte[] bytes = text.getBytes(StandardCharsets.US_ASCII);
        return formatBytesToLength(fixLen, bytes);
    }

    public static byte[] getTimeBytes(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        String timeStr = sdf.format(new Date(time));
        byte[] dateBytes = formatStringToBytes(12, timeStr);

        sdf = new SimpleDateFormat("HH:mm:ss");
        timeStr = sdf.format(new Date(time));
        byte[] timeBytes = formatStringToBytes(12, timeStr);

        byte[] result = new byte[24];
        for (int i=0; i<12; i++) {
            result[i] = dateBytes[i];
            result[12+i] = timeBytes[i];
        }
        return result;
    }

    // 调用者确保参数合法
    private static void copyBytes(byte[] src, byte[] dst, int start) {
        for (int i=0; i<src.length; i++) {
            dst[start+i] = src[i];
        }
    }

    public static boolean writeTrackerInfoToFile(String path, DrillHoleInfo info, List<CollectTimeInfo> dataList) {
        File file = new File(path);
        FileUtils.delete(file);
        boolean success = FileUtils.createOrExistsFile(file);
        if (!success) {
            return false;
        }
        byte[] data = new byte[200 + 4 + (dataList.size()*10)];

        int size = fillHeaderBytes(data, info);
        if (BuildConfig.DEBUG) {
            SuperLogUtil.sd("header size: " + size);
        }
        fillDataBytes(data, size, dataList);
        boolean ret = FileIOUtils.writeFileFromBytesByChannel(path,data, false, true);
        if (BuildConfig.DEBUG) {
            SuperLogUtil.sd("zip文件已生成："+ret);
        }
        return true;
    }

    private static int fillDataBytes(byte[] bytes, int start, List<CollectTimeInfo> dataList) {
        int cnt = start;
        copyBytes(getU32Bytes(dataList.size()), bytes, cnt);
        cnt += 4;

        for ( CollectTimeInfo info: dataList) {
            bytes[cnt] = (byte) ((info.collectTime >> 24)&0xff);
            cnt++;
            bytes[cnt] = (byte) ((info.collectTime >> 16)&0xff);
            cnt++;
            bytes[cnt] = (byte) ((info.collectTime >> 8)&0xff);
            cnt++;
            bytes[cnt] = (byte) ((info.collectTime)&0xff);
            cnt++;

            bytes[cnt] = (byte) ((info.rollAngle>>8)&0xff);
            cnt++;
            bytes[cnt] = (byte) ((info.rollAngle)&0xff);
            cnt++;

            bytes[cnt] = (byte) ((info.omega>>8)&0xff);
            cnt++;
            bytes[cnt] = (byte) ((info.omega)&0xff);
            cnt++;

            bytes[cnt] = (byte) ((info.directionAngle>>8)&0xff);
            cnt++;
            bytes[cnt] = (byte) ((info.directionAngle)&0xff);
            cnt++;
        }

        return cnt;
    }

    private static int fillHeaderBytes(byte[] data, DrillHoleInfo info) {
        int cnt = 0;
        // 产品版本
        copyBytes(formatStringToBytes(12, version), data, cnt);
        cnt += 12;
        // 采集日期 采集时间
        copyBytes(getTimeBytes(info.collectionDateTime), data, cnt);
        cnt += 24;
        // 企业编号
        copyBytes(formatStringToBytes(32, info.companyId), data, cnt);
        cnt += 32;
        // 矿区编号
        copyBytes(formatStringToBytes(32, info.miningAreaId), data, cnt);
        cnt += 32;
        // 工作面名称
        copyBytes(formatStringToBytes(12, info.workspaceName), data, cnt);
        cnt += 12;
        // 钻孔编号
        copyBytes(formatStringToBytes(12, info.drillHoleId), data, cnt);
        cnt += 12;
        // 检测人员
        copyBytes(formatStringToBytes(24, info.detector), data, cnt);
        cnt += 24;
        // 护套长度
        copyBytes(getU32Bytes(info.jacketLength), data, cnt);
        cnt += 4;
        // 预留4字节
        copyBytes(new byte[]{0,0,0,0}, data, cnt);
        cnt += 4;

        // 设计倾角
        copyBytes(getS16Bytes(info.designAngle), data, cnt);
        cnt += 2;
        // 倾角修正角
        copyBytes(getS16Bytes(info.angleAdjustValue), data, cnt);
        cnt += 2;
        // 设计方位角度
        copyBytes(getU16Bytes(info.designDirection), data, cnt);
        cnt += 2;
        // 方位角修正
        copyBytes(getS16Bytes(info.directionAngleAdjustValue), data, cnt);
        cnt += 2;
        // 设计深度
//        copyBytes(getU32BytesLow3(info.designLength), data, cnt);
        copyBytes(getU32BytesLow3(info.drillHoleLength), data, cnt);
        cnt += 3;
        // 测量间距
        copyBytes(getU16Bytes(info.drillPipeLength), data, cnt);
        cnt += 2;
        // 动态阈值 u16
        copyBytes(getU16Bytes(info.dynamicThreshold), data, cnt);
        cnt += 2;
        // 设备ID u16
        copyBytes(getU32BytesLow3(info.deviceID), data, cnt);
        cnt += 3;
        // 世界坐标X
        copyBytes(getDBytes(info.holeX), data, cnt);
        cnt += 8;
        // 世界坐标Y
        copyBytes(getDBytes(info.holeY), data, cnt);
        cnt += 8;
        // 世界坐标Z
        copyBytes(getDBytes(info.holeZ), data, cnt);
        cnt += 8;
        // 预留2bytes
        copyBytes(new byte[]{0,0}, data, cnt);
        cnt += 2;
        return cnt;
    }

    private static byte[] getDBytes(double num) {
        int intPart = (int) num;
        // 原数减去整数部分，为小数部分
        long doublePart = new BigDecimal(String.valueOf(num))
                .subtract(new BigDecimal(intPart))
                .multiply(new BigDecimal(1000000))
                .abs()
                .longValue();
        byte[] intBytes = getS32Bytes(intPart);
        byte[] doublePartBytes = getU32Bytes(doublePart);
        byte[] result = new byte[8];
        for (int i=0; i<4; i++) {
            result[i] = intBytes[i];
            result[i+4] = doublePartBytes[i];
        }
        System.out.println(intPart);
        System.out.println(doublePart);
        return result;
    }

    private static byte[] getU16Bytes(int num) {
        byte[] result = new byte[2];
        for (int i=0; i<result.length; i++) {
            result[i] = (byte)((num >> (8 * (result.length-1-i))) & 0xff);
        }
        return result;
    }

    private static byte[] getS16Bytes(short num) {
        byte[] result = new byte[2];
        for (int i=0; i<result.length; i++) {
            result[i] = (byte)((num >> (8 * (result.length-1-i))) & 0xff);
        }
        return result;
    }

    private static byte[] getU32BytesLow3(long num) {
        byte[] result = new byte[3];
        for (int i=0; i<result.length; i++) {
            result[i] = (byte)((num >> (8 * (result.length-1-i))) & 0xff);
        }
        return result;
    }

    private static byte[] getU32Bytes(long num) {
        byte[] result = new byte[4];
        for (int i=0; i<result.length; i++) {
            result[i] = (byte)((num >> (8 * (result.length-1-i))) & 0xff);
        }
        return result;
    }

    private static byte[] getS32Bytes(int num) {
        byte[] result = new byte[4];
        for (int i=0; i<result.length; i++) {
            result[i] = (byte)((num >> (8 * (result.length-1-i))) & 0xff);
        }
        return result;
    }
}

package com.zeus.tec.ui.tracker.util;

import java.util.Calendar;
import java.util.TimeZone;

public class TimeUtil {

    public static long getGMTTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        Calendar gmtCL = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        gmtCL.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));

        return gmtCL.getTimeInMillis() / 1000;
    }

    //// 以下是下位机转换算法
    static class Tm {
        long hour;
        long min;
        long sec;
        //公历日月年周
        long w_year;
        long  w_month;
        long  w_date;

        @Override
        public String toString() {
            return "Tm{" +
                    "w_year=" + w_year +
                    ", w_month=" + w_month +
                    ", w_date=" + w_date +
                    "，hour=" + hour +
                    ", min=" + min +
                    ", sec=" + sec +
                    '}';
        }
    }
    static final int[] mon_table = new int[]{31,28,31,30,31,30,31,31,30,31,30,31};//月份日期数据表

    //探头中的时间通过下面的计算可以得到年月日时分秒和星期
    public static long getDateTime(long num) {
        /*Tm timer = new Tm();
        long daycnt=0;
        long temp=0;
        int temp1=0;

        temp=num/86400;   //得到天数(秒钟数对应的)
        if(daycnt!=temp) { //超过一天了
            daycnt=temp;
            temp1=1970;	//从1970年开始
            while(temp>=365) {
                if(Is_Leap_Year(temp1)) {//是闰年
                    if(temp>=366)temp-=366;//闰年的秒钟数
                    else break;
                }
                else temp-=365;	  //平年
                temp1++;
            }
            timer.w_year=temp1;//得到年份
            temp1=0;
            while(temp>=28) {//超过了一个月
                if(Is_Leap_Year(timer.w_year)&&temp1==1) {//当年是不是闰年/2月份
                    if(temp>=29)
                        temp-=29;//闰年的秒钟数
                    else
                        break;
                } else {
                    System.out.println(temp1);
                    if(temp>=mon_table[temp1]) temp-= mon_table[temp1];//平年
                    else break;
                }
                temp1++;
            }
            timer.w_month=temp1+1;//得到月份
            timer.w_date=temp+1;  //得到日期
        }
        temp=num%86400;     //得到秒钟数
        timer.hour=temp/3600;     //小时
        timer.min=(temp%3600)/60; //分钟
        timer.sec=(temp%3600)%60; //秒钟*/

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.setTimeInMillis(num * 1000L);

        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));

        return calendar2.getTimeInMillis() / 1000;
    }

    static boolean Is_Leap_Year(long year) {
        if(year%4==0) {//必须能被4整除
            if(year%100==0) {
                if(year%400==0)
                    return true;//如果以00结尾,还要能被400整除
                else
                    return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
}

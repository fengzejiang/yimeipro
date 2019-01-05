package com.yimeinew.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/10 17:10
 */
public class DateUtil {

    public static long TIMEDIFF = 0;

    public static String getCurrDateTime(int fmt){
        long now = System.currentTimeMillis();
        now+=TIMEDIFF;
        return dateToString(now,true,fmt);
    }

    public static String dateToString(long mills, boolean bdiv, int fmt) {
        Calendar calendar = Calendar.getInstance(Locale.CHINESE);
        calendar.setTimeInMillis(mills);
        return dateToString(calendar,bdiv,fmt);
    }

    /**
     * 日期转化成字符串，参数中指定格式化方式(YM,YMD,YMDT,T,...在常量表中定义)
     */
    public static String dateToString(Calendar calrd, boolean bdiv, int fmt) {
        if (calrd == null)
            return null;
        if (fmt == ICL.DF_Y)
            return String.valueOf(calrd.get(Calendar.YEAR));//--年
        if (fmt == ICL.DF_M)
            return String.valueOf(calrd.get(Calendar.MONTH) + 1);//--月
        int xs[] = dateToAry(calrd);
        int x0 = 0, x1 = 6;
        if (fmt == ICL.DF_YM)
            x1 = 2;//-年月
        else if (fmt == ICL.DF_YMD)
            x1 = 3;//-年月日
        else if (fmt == ICL.DF_YMDHM)
            x1 = 5;//-年月日时分秒
        else if (fmt == ICL.DF_T)
            x0 = 3;//时分秒
        else if (fmt == ICL.DF_HM) {
            x0 = 3;//时分
            x1 = 5;
        } else if (fmt == ICL.DF_DYNC)
            x1 = xs[6];//--时分秒一项不为0时,包括时分秒,否则只有日期。
        return dateToString(xs, x0, x1, bdiv);
    }
    /**
     * 日期数组转化成日期格式
     */
    public static String dateToString(int[] xs, int x0, int x1, boolean bdiv) {
        int iv;
        StringBuffer sb = new StringBuffer(20);
        if (bdiv) {
            //带分隔符。
            String sdiv = "--- :: ";
            while (x0 < x1) {
                sb.append(sdiv.charAt(x0));
                iv = xs[x0];
                if (iv < 10)
                    sb.append("0");
                sb.append(iv);
                x0++;
            }
            return sb.toString().substring(1);//--带分隔符,如:2011-10-10
        }
        while (x0 < x1) {
            iv = xs[x0];
            if (iv < 10)
                sb.append("0");
            sb.append(iv);
            x0++;
        }
        return sb.toString();//--不带分隔符,如:20111010
    }


    /**
     * 日期对象转化成整数,数组的长度为7,最后一项=6时表示有时分秒,=3时表示只有年月日。
     */
    public static int[] dateToAry(Calendar calrd) {
        if (calrd == null)
            return null;
        int[] xs = new int[7];
        xs[0] = calrd.get(Calendar.YEAR);
        xs[1] = calrd.get(Calendar.MONTH) + 1;
        xs[2] = calrd.get(Calendar.DATE);
        int h = calrd.get(Calendar.HOUR_OF_DAY), m = calrd.get(Calendar.MINUTE), s = calrd.get(Calendar.SECOND);
        boolean b0 = h > 0 || m > 0 || s > 0;
        if (b0) {
            xs[3] = h;
            xs[4] = m;
            xs[5] = s;
        }
        xs[6] = b0 ? 6 : 3;//;--标识是否有时分秒。
        return xs;
    }

    /***
     * 时间相减，获取相应的时间差
     * @param d1 日期1
     * @param d2 日期2
     * @param key 0:年;1:月(直接给天数30);2:天，3：小时，4：分钟，5：秒
     * @return
     */
    public static int subDate(String d1, String d2, int key) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date1 = df.parse(d1);
            Date date2 = df.parse(d2);
            long diff = date1.getTime() - date2.getTime();
            int k1 = 0;
            switch (key){
                case 0:
                    key = (int)(diff/(365*24*60*60*1000));
                    break;
                case 1:
                    k1 = (int)(diff/(24*60*60*1000*30));
                    break;
                case 2:
                    k1 = (int)(diff/(24*60*60*1000));
                    break;
                case 3:
                    k1 = (int)(diff/(60*60*1000));
                    break;
                case 4:
                    k1 = (int)(diff/(60*1000));
                    break;
                case 5:
                    k1 = (int)(diff/(1000));
                    break;
            }
            return k1;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}

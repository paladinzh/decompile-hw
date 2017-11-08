package com.huawei.watermark.manager.parse.util;

import com.android.gallery3d.settings.HicloudAccountReceiver;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;

public class WMLunarSearch {
    private static final HashMap<String, Integer> mLunarDayCheckMap = new HashMap();
    private final int[] lunarInfo = new int[]{19416, 19168, 42352, 21717, 53856, 55632, 21844, 22191, 39632, 21970, 19168, 42422, 42192, 53840, 53909, 46415, 54944, 44450, 38320, 18807, 18815, 42160, 46261, 27216, 27968, 43860, 11119, 38256, 21234, 18800, 25958, 54432, 59984, 27285, 23263, 11104, 34531, 37615, 51415, 51551, 54432, 55462, 46431, 22176, 42420, 9695, 37584, 53938, 43344, 46423, 27808, 46416, 21333, 19887, 42416, 17779, 21183, 43432, 59728, 27296, 44710, 43856, 19296, 43748, 42352, 21088, 62051, 55632, 23383, 22176, 38608, 19925, 19152, 42192, 54484, 53840, 54616, 46400, 46752, 38310, 38335, 18864, 43380, 42160, 45690, 27216, 27968, 44870, 43872, 38256, 19189, 18800, 25776, 29859, 59984, 27480, 23232, 43872, 38613, 37600, 51552, 55636, 54432, 55888, 30034, 22176, 43959, 9680, 37584, 51893, 43344, 46240, 47780, 44368, 21977, 19360, 42416, 20854, 21183, 43312, 31060, 27296, 44368, 23378, 19296, 42726, 42208, 53856, 60005, 54576, 23200, 30371, 38608, 19195, 19152, 42192, 53430, 53855, 54560, 56645, 46496, 22224, 21938, 18864, 42359, 42160, 43600, 45653, 27951, 44448, 19299, 37759, 18936, 18800, 25776, 26790, 59999, 27424, 42692, 43759, 37600, 53987, 51552, 54615, 54432, 55888, 23893, 22176, 42704, 21972, 21200, 43448, 43344, 46240, 46758, 44368, 21920, 43940, 42416, 21168, 45683, 26928, 29495, 27296, 44368, 19285, 19311, 42352, 21732, 53856, 59752, 54560, 55968, 27302, 22239, 19168, 43476, 42192, 53584, 62034, 54560};
    private int lunarYear;
    private Calendar solar;
    private int solarDay;
    private int solarMonth;
    private final String[] solarTermIdStr = new String[]{"xiaohan", "dahan", "lichun", "yushui", "jingzhe", "chunfen", "qingming", "guyu", "lixia", "xiaoman", "mangzhong", "xiazhi", "xiaoshu", "dashu", "liqiu", "chushu", "bailu", "qiufen", "hanlu", "shuangjiang", "lidong", "xiaoxue", "daxue", "dongzhi"};
    private final int[] solarTermInfo = new int[]{0, 21208, 42467, 63836, 85337, 107014, 128867, 150921, 173149, 195551, 218072, 240693, 263343, 285989, 308563, 331033, 353350, 375494, 397447, 419210, 440795, 462224, 483532, 504758};
    private int solarYear;
    private GregorianCalendar utcCal = null;

    public int[] getnearsolarTerm(int year, Date date) {
        Calendar[] jieqi = jieqilist(year);
        int[] returnValue = new int[2];
        boolean afterDongzhi = false;
        boolean beforeXiaohan = false;
        int i = 0;
        while (i < jieqi.length) {
            if (dateLaterAtDay(date, jieqi[i])) {
                if (i == jieqi.length - 1) {
                    afterDongzhi = true;
                }
                i++;
            } else {
                if (i == 0) {
                    beforeXiaohan = true;
                }
                returnValue[0] = i - 1;
                returnValue[1] = i;
                if (afterDongzhi || beforeXiaohan) {
                    returnValue[0] = jieqi.length - 1;
                    returnValue[1] = 0;
                }
                return returnValue;
            }
        }
        returnValue[0] = jieqi.length - 1;
        returnValue[1] = 0;
        return returnValue;
    }

    private boolean dateLaterAtDay(Date dateSrc, Calendar calAim) {
        Calendar calSrc = Calendar.getInstance();
        calSrc.setTime(dateSrc);
        if (calSrc.get(6) > calAim.get(6)) {
            return true;
        }
        return false;
    }

    public Calendar[] jieqilist(int year) {
        Calendar[] returnvalue = new Calendar[getSolarTermIdStrCount()];
        for (int i = 0; i < returnvalue.length; i++) {
            int day;
            Date t = getSolarTermCalendar(year, i);
            if (mLunarDayCheckMap.containsKey("" + year + "_" + i)) {
                day = ((Integer) mLunarDayCheckMap.get("" + year + "_" + i)).intValue();
            } else {
                day = getUTCDay(t);
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(t);
            cal.set(5, day);
            returnvalue[i] = cal;
        }
        return returnvalue;
    }

    static {
        mLunarDayCheckMap.put("1973_8", Integer.valueOf(5));
        mLunarDayCheckMap.put("1974_21", Integer.valueOf(23));
        mLunarDayCheckMap.put("1975_1", Integer.valueOf(21));
        mLunarDayCheckMap.put("1975_17", Integer.valueOf(23));
        mLunarDayCheckMap.put("1976_2", Integer.valueOf(5));
        mLunarDayCheckMap.put("1977_4", Integer.valueOf(6));
        mLunarDayCheckMap.put("1977_14", Integer.valueOf(7));
        mLunarDayCheckMap.put("1978_0", Integer.valueOf(6));
        mLunarDayCheckMap.put("1978_21", Integer.valueOf(23));
        mLunarDayCheckMap.put("1979_9", Integer.valueOf(21));
        mLunarDayCheckMap.put("1980_2", Integer.valueOf(5));
        mLunarDayCheckMap.put("1980_23", Integer.valueOf(22));
        mLunarDayCheckMap.put("1981_4", Integer.valueOf(6));
        mLunarDayCheckMap.put("1982_0", Integer.valueOf(6));
        mLunarDayCheckMap.put("1983_22", Integer.valueOf(8));
        mLunarDayCheckMap.put("1984_13", Integer.valueOf(22));
        mLunarDayCheckMap.put("1984_23", Integer.valueOf(22));
        mLunarDayCheckMap.put("1985_5", Integer.valueOf(21));
        mLunarDayCheckMap.put("1987_12", Integer.valueOf(7));
        mLunarDayCheckMap.put("1988_13", Integer.valueOf(22));
        mLunarDayCheckMap.put("1989_3", Integer.valueOf(19));
        mLunarDayCheckMap.put("1989_16", Integer.valueOf(7));
        mLunarDayCheckMap.put("1990_11", Integer.valueOf(21));
        mLunarDayCheckMap.put("1990_20", Integer.valueOf(8));
        mLunarDayCheckMap.put("1991_12", Integer.valueOf(7));
        mLunarDayCheckMap.put("1991_15", Integer.valueOf(23));
        mLunarDayCheckMap.put("1994_11", Integer.valueOf(21));
        mLunarDayCheckMap.put("1997_10", Integer.valueOf(5));
        mLunarDayCheckMap.put("2006_8", Integer.valueOf(5));
        mLunarDayCheckMap.put("2006_14", Integer.valueOf(7));
        mLunarDayCheckMap.put("2007_21", Integer.valueOf(23));
        mLunarDayCheckMap.put("2008_1", Integer.valueOf(21));
        mLunarDayCheckMap.put("2008_17", Integer.valueOf(22));
        mLunarDayCheckMap.put("2009_2", Integer.valueOf(4));
        mLunarDayCheckMap.put("2010_4", Integer.valueOf(6));
        mLunarDayCheckMap.put("2010_14", Integer.valueOf(7));
        mLunarDayCheckMap.put("2011_0", Integer.valueOf(6));
        mLunarDayCheckMap.put("2011_21", Integer.valueOf(23));
        mLunarDayCheckMap.put("2012_1", Integer.valueOf(21));
        mLunarDayCheckMap.put("2012_9", Integer.valueOf(20));
        mLunarDayCheckMap.put("2012_22", Integer.valueOf(7));
        mLunarDayCheckMap.put("2013_2", Integer.valueOf(4));
        mLunarDayCheckMap.put("2013_13", Integer.valueOf(22));
        mLunarDayCheckMap.put("2013_23", Integer.valueOf(22));
        mLunarDayCheckMap.put("2014_4", Integer.valueOf(6));
        mLunarDayCheckMap.put("2015_0", Integer.valueOf(6));
        mLunarDayCheckMap.put("2016_22", Integer.valueOf(7));
        mLunarDayCheckMap.put("2017_13", Integer.valueOf(22));
        mLunarDayCheckMap.put("2017_23", Integer.valueOf(22));
        mLunarDayCheckMap.put("2018_3", Integer.valueOf(19));
        mLunarDayCheckMap.put("2018_5", Integer.valueOf(21));
        mLunarDayCheckMap.put("2019_11", Integer.valueOf(21));
        mLunarDayCheckMap.put("2020_12", Integer.valueOf(6));
        mLunarDayCheckMap.put("2020_15", Integer.valueOf(22));
        mLunarDayCheckMap.put("2020_22", Integer.valueOf(7));
        mLunarDayCheckMap.put("2022_3", Integer.valueOf(19));
        mLunarDayCheckMap.put("2022_16", Integer.valueOf(7));
        mLunarDayCheckMap.put("2023_11", Integer.valueOf(21));
        mLunarDayCheckMap.put("2023_19", Integer.valueOf(24));
        mLunarDayCheckMap.put("2023_20", Integer.valueOf(8));
        mLunarDayCheckMap.put("2024_15", Integer.valueOf(22));
        mLunarDayCheckMap.put("2026_10", Integer.valueOf(5));
        mLunarDayCheckMap.put("2030_10", Integer.valueOf(5));
        mLunarDayCheckMap.put("2035_8", Integer.valueOf(5));
        mLunarDayCheckMap.put("2035_14", Integer.valueOf(7));
        mLunarDayCheckMap.put("2037_1", Integer.valueOf(20));
    }

    public int getSolarTermIdStrCount() {
        return this.solarTermIdStr.length;
    }

    public String getSolarTermIdStrByIndex(int index) {
        if (index < 0 || index >= getSolarTermIdStrCount()) {
            return null;
        }
        return this.solarTermIdStr[index];
    }

    private int getLunarLeapMonth(int lunarYear) {
        int leapMonth = this.lunarInfo[lunarYear - 1900] & 15;
        if (leapMonth == 15) {
            return 0;
        }
        return leapMonth;
    }

    private int getLunarLeapDays(int lunarYear) {
        if (getLunarLeapMonth(lunarYear) <= 0) {
            return 0;
        }
        if ((this.lunarInfo[lunarYear - 1899] & 15) == 15) {
            return 30;
        }
        return 29;
    }

    private int getLunarYearDays(int lunarYear) {
        int daysInLunarYear = 348;
        for (int i = 32768; i > 8; i >>= 1) {
            int i2;
            if ((this.lunarInfo[lunarYear - 1900] & i) != 0) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            daysInLunarYear += i2;
        }
        return daysInLunarYear + getLunarLeapDays(lunarYear);
    }

    private int getLunarMonthDays(int lunarYear, int lunarMonth) {
        if ((this.lunarInfo[lunarYear - 1900] & (HicloudAccountReceiver.MSG_HICLOUD_ACCOUNT_LOGOUT >> lunarMonth)) != 0) {
            return 30;
        }
        return 29;
    }

    private synchronized int getUTCDay(Date date) {
        int i;
        makeUTCCalendar();
        synchronized (this.utcCal) {
            this.utcCal.clear();
            this.utcCal.setTimeInMillis(date.getTime());
            i = this.utcCal.get(5);
        }
        return i;
    }

    private synchronized void makeUTCCalendar() {
        if (this.utcCal == null) {
            this.utcCal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        }
    }

    private synchronized long UTC(int y, int m, int d, int h, int min, int sec) {
        long timeInMillis;
        makeUTCCalendar();
        synchronized (this.utcCal) {
            this.utcCal.clear();
            this.utcCal.set(y, m, d, h, min, sec);
            timeInMillis = this.utcCal.getTimeInMillis();
        }
        return timeInMillis;
    }

    private int getSolarTermDay(int solarYear, int index) {
        if (mLunarDayCheckMap.containsKey("" + solarYear + "_" + index)) {
            return ((Integer) mLunarDayCheckMap.get("" + solarYear + "_" + index)).intValue();
        }
        return getUTCDay(getSolarTermCalendar(solarYear, index));
    }

    private Date getSolarTermCalendar(int solarYear, int index) {
        return new Date(((((long) (solarYear - 1900)) * 31556925974L) + (((long) this.solarTermInfo[index]) * 60000)) + UTC(1900, 0, 6, 2, 5, 0));
    }

    public void setTime(long TimeInMillis) {
        this.solar = Calendar.getInstance();
        this.solar.setTimeInMillis(TimeInMillis);
        this.solarYear = this.solar.get(1);
        this.solarMonth = this.solar.get(2);
        this.solarDay = this.solar.get(5);
        long offset = (TimeInMillis - new GregorianCalendar(1900, 0, 31).getTimeInMillis()) / 86400000;
        this.lunarYear = 1900;
        int daysInLunarYear = getLunarYearDays(this.lunarYear);
        while (this.lunarYear < 2100 && offset >= ((long) daysInLunarYear)) {
            offset -= (long) daysInLunarYear;
            int i = this.lunarYear + 1;
            this.lunarYear = i;
            daysInLunarYear = getLunarYearDays(i);
        }
        int lunarMonth = 1;
        int leapMonth = getLunarLeapMonth(this.lunarYear);
        boolean leapDec = false;
        boolean isLeap = false;
        while (lunarMonth < 13 && offset > 0) {
            int daysInLunarMonth;
            if (isLeap && leapDec) {
                daysInLunarMonth = getLunarLeapDays(this.lunarYear);
                leapDec = false;
            } else {
                daysInLunarMonth = getLunarMonthDays(this.lunarYear, lunarMonth);
            }
            if (offset >= ((long) daysInLunarMonth)) {
                offset -= (long) daysInLunarMonth;
                if (leapMonth != lunarMonth || isLeap) {
                    lunarMonth++;
                } else {
                    leapDec = true;
                    isLeap = true;
                }
            } else {
                return;
            }
        }
    }

    public String getTermIdString() {
        String termString = "";
        if (getSolarTermDay(this.solarYear, this.solarMonth * 2) == this.solarDay) {
            return getSolarTermIdStrByIndex(this.solarMonth * 2);
        }
        if (getSolarTermDay(this.solarYear, (this.solarMonth * 2) + 1) == this.solarDay) {
            return getSolarTermIdStrByIndex((this.solarMonth * 2) + 1);
        }
        return termString;
    }
}

package com.huawei.keyguard.util;

import android.content.Context;
import com.android.keyguard.R$string;
import fyusion.vislib.BuildConfig;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

public class LunarCalendar {
    private static int DAYS_OF_YEAR = 365;
    private static final int[] Gan = new int[]{R$string.gan_jia, R$string.gan_yi, R$string.gan_bing, R$string.gan_ding, R$string.gan_wu, R$string.gan_ji, R$string.gan_gen, R$string.gan_xin, R$string.gan_ren, R$string.gan_gui};
    private static final HashMap<String, Integer> TYPE_CNHOLIDAY = new HashMap();
    private static final HashMap<String, Integer> TYPE_TWHOLIDAY = new HashMap();
    private static final int[] Zhi = new int[]{R$string.zhi_zi, R$string.zhi_chu, R$string.zhi_yin, R$string.zhi_mao, R$string.zhi_chen, R$string.zhi_si, R$string.zhi_wu, R$string.zhi_wei, R$string.zhi_shen, R$string.zhi_you, R$string.zhi_xu, R$string.zhi_hai};
    private static final int[] chineseNum = new int[]{R$string.one, R$string.two, R$string.three, R$string.four, R$string.five, R$string.six, R$string.seven, R$string.eight, R$string.nine, R$string.ten};
    private static final char[] lunarDateInfo = new char[]{'䯘', '䫠', 'ꕰ', '哕', '퉠', '?', '啔', '嚯', '髐', '嗒', '䫠', 'ꖶ', 'ꓐ', '퉐', '튕', '땏', '횠', '궢', '閰', '䥷', '䥿', '꒰', '뒵', '橐', '浀', 'ꭔ', '⭯', '镰', '勲', '䥰', '敦', '풠', '', '檕', '嫟', '⭠', '蛣', '鋯', '죗', '쥟', '풠', '?', '땟', '嚠', 'ꖴ', '◟', '鋐', '튲', 'ꥐ', '땗', '沠', '땐', '单', '䶯', 'ꖰ', '䕳', '势', 'ꦨ', '', '檠', '꺦', 'ꭐ', '䭠', 'ꫤ', 'ꕰ', '剠', '', '?', '字', '嚠', '雐', '䷕', '䫐', 'ꓐ', '퓔', '퉐', '하', '땀', '뚠', '閦', '閿', '䦰', 'ꥴ', '꒰', '뉺', '橐', '浀', '꽆', 'ꭠ', '镰', '䫵', '䥰', '撰', '璣', '', '歘', '嫀', 'ꭠ', '雕', '鋠', '쥠', '?', '풠', '?', '畒', '嚠', 'ꮷ', '◐', '鋐', '쪵', 'ꥐ', '뒠', '몤', '교', '嗙', '䮠', 'ꖰ', '其', '势', 'ꤰ', '祔', '檠', '교', '孒', '䭠', 'ꛦ', 'ꓠ', '퉠', '', '픰', '媠', '皣', '雐', '䫻', '䫐', 'ꓐ', '킶', '퉟', '픠', '?', '떠', '囐', '喲', '䦰', 'ꕷ', '꒰', '꩐', '뉕', '洯', '궠', '䭣', '鍿', '䧸', '䥰', '撰', '梦', '', '欠', 'ꛄ', 'ꫯ', '鋠', '틣', '쥠', '핗', '풠', '?', '嵕', '嚠', 'ꛐ', '嗔', '勐', 'ꦸ', 'ꥐ', '뒠', '뚦', '교', '喠', 'ꮤ', 'ꖰ', '劰', '뉳', '椰', '猷', '檠', '교', '䭕', '䭯', 'ꕰ', '哤', '퉠', '', '픠', '?', '檦', '囟', '䫠', '꧔', 'ꓐ', '텐', '', '픠'};
    private static final HashMap<String, Integer> lunarFestivalInfo = new HashMap();
    private static final int[] lunarTerm = new int[]{R$string.lesser_cold, R$string.greater_cold, R$string.beginning_of_spring, R$string.rain_water, R$string.waking_of_insects, R$string.spring_equinox, R$string.pure_brightness, R$string.grain_rain, R$string.beginning_of_summer, R$string.lesser_fullness_of_grain, R$string.grain_in_beard, R$string.summer_solstice, R$string.lesser_heat, R$string.greater_heat, R$string.beginning_of_autumn, R$string.end_of_heat, R$string.white_dew, R$string.autumn_equinox, R$string.cold_dew, R$string.frosts_descent, R$string.beginning_of_winter, R$string.lesser_snow, R$string.greater_snow, R$string.winter_solstice};
    private static final char[] lunarTermInfo = new char[]{'隴', '隦', '鞗', '硹', '祩', '硷', '隤', '隖', '鞇', '祹', '祩', '硸', '隥', '螖', '螇', '祩', '楩', '硸', '蚥', '隥', '隗', '衸', '硹', '碇', '隴', '隦', '鞗', '硹', '祩', '硷', '隤', '隖', '鞗', '祹', '祩', '硸', '隥', '螖', '螇', '祩', '楩', '硸', '蚥', '隥', '隗', '衸', '硩', '碇', '隴', '隦', '鞗', '硹', '祩', '硷', '隤', '隖', '鞗', '祹', '祩', '硸', '隥', '螖', '螇', '祩', '楩', '硸', '蚥', '隥', '隗', '衸', '硩', '碇', '閴', '隦', '鞗', '硹', '祩', '硷', '隴', '隦', '鞗', '祹', '祩', '硸', '隥', '鞖', '鞇', '祹', '楩', '硸', '隥', '隥', '隗', '衸', '硹', '瞇', '閴', '隦', '隗', '硹', '硩', '碇', '隴', '隦', '鞗', '祹', '祩', '硷', '隥', '鞖', '鞇', '祹', '楩', '硸', '隥', '隥', '隗', '衸', '硹', '瞇', '閴', '隥', '隗', '硹', '硩', '碇', '隴', '隦', '鞗', '祹', '祩', '硷', '隤', '隖', '鞇', '祹', '楩', '硸', '隥', '隥', '隗', '衸', '硹', '瞇', '閴', '隥', '隗', '硹', '硩', '碇', '隴', '隦', '鞗', '硹', '祩', '硷', '隤', '隖', '鞇', '祹', '祩', '硸', '隥', '隥', '隖', '衸', '硸', '螇', '閴', '隥', '隗', '衸', '硹', '瞇', '隴', '隦', '鞗', '硹', '祩', '硷', '隤', '隖', '鞇', '祹', '祩', '硸', '隥', '隥', '隖', '衸', '硸', '螇', '閴', '隥', '隗', '衸', '硩', '碇', '隴', '隦', '鞗', '硹', '祩', '硷', '隤', '隖', '鞗', '祹', '祩', '硸', '隥', '隥', '隖', '衸', '硸', '螇', '閴', '隥', '隗', '衸', '硩', '碇', '隴', '隦', '鞗', '硹', '祩', '硷', '隤', '隖', '鞗', '祹', '祩', '硸', '隥', '隥', '隖', '衸', '硸', '螇', '閴', '隥', '隗', '衸', '硩', '碇', '隴', '隦', '鞗', '硹', '祩', '硷', '隤', '隖', '鞗', '祹', '祩', '硸', '隥', '隥', 'Ꚗ', '衸', '硸', '螇', '閴', '隥', '隗', '衸', '硹', '瞇', '閴', '隦', '鞗', '硹', '硩', '硷', '隴', '隦', '鞗', '祹', '祩', '硸', '隥', 'ꚥ', 'Ꚗ', '袈', '硸', '螇', 'ꖴ', '隥', '隗', '衹', '硹', '瞇', '閴', '隥', '隗', '硹', '硩', '硷', '隴', '隦', '鞗', '祹', '祩', '硸', '隥', 'ꚥ', 'Ꚗ', '袈', '硸', '螇', 'ꖴ', '隥', '隗', '衸', '硹', '瞇', '閴', '隥', '隗', '硹', '硨', '碇', '隴', '隦', '鞗', '硹', '祩', '硷', '隥', 'ꖥ', 'Ꚗ', '袈', '硸', '螇', 'ꖴ', '隥', '隗', '衸', '硹', '瞇', '閴', '隥', '隗', '衸', '硩', '碇', '隴', '隦', '鞗', '硹', '祩', '硷', '隤', 'ꖥ', 'Ꚗ', '袈', '衸', '螇', 'ꖴ', '隥', '隖', '衸', '硸', '螇', '隴', '隥', '隗', '衸', '硩', '碇', '隴', '隦', '鞗', '硹', '祩', '硷', '隤', 'ꖥ', 'Ꚗ', '袈', '衸', '螇', 'ꖴ', '隥', '隖', '衸', '硸', '螇', '閴', '隥', '隗', '衸', '硩', '碇', '隴', '隦', '鞗', '硹', '祩', '硷', '隤', 'ꖥ', 'ꚦ', '袈', '衸', '螇', 'ꖴ', '隥', '隖', '衸', '硸', '螇', '閴', '隥', '隗', '衸', '硩', '碇', '隴', '隦', '鞗', '硹', '祩', '硷', '隤', 'ꖥ', 'ꚦ', '袈', '衸', '螇', 'ꖵ', '隥', 'Ꚗ', '衸', '硸', '螇', '閴', '隥', '隗', '衸', '硩', '碇', '隴', '隦', '鞗', '硹', '硩', '硷', '隤', 'ꖵ', 'ꚦ', '袉', '衸', '螇', 'ꖴ', '隥', '隖', '袈', '硸', '螇', '閴', '隥', '隗', '衸', '硹', '碇', '隴', '隦', '隗', '硹', '硩', '硷', '隤', 'ꖵ', 'ꚦ', '袈', '衸', '螇', 'ꖴ', '隥', 'Ꚗ', '袈', '硸', '瞇', '閴', '隥', '隗', '衸', '硹', '瞇', '閴', '隥', '隗', '硹', '硩', '硷', '隴', 'ꖵ', 'ꚦ', '螈', '衸', '螇', 'ꖴ', 'ꚥ', 'Ꚗ', '袈', '硸', '螇', 'ꖴ', '隥', '隗', '衸', '硹', '瞇', '閴', '隥', '隗', '衹', '硩', '碇', '隴', 'ꖵ', 'ꚦ', '螈', '衸', '螆', 'ꖴ', 'ꖥ', 'Ꚗ', '袈', '衸', '螇', 'ꖴ', '隥', '隖', '衸', '硹', '瞇', '閴', '隥', '蚗', '衸', '硩', '碇', '隴', 'ꖵ', 'ꚦ', '螈', '衸', '螆', 'ꖳ', 'ꖥ', 'Ꚗ', '袈', '衸', '螇', 'ꖴ', '隥', '隖', '衸', '硸', '螇', '閴', '隥', '隗', '衶', '硩', '碇', '隴', 'ꖵ', 'ꚦ', '螈', '衸', '螆', 'ꖳ', 'ꖥ', 'ꚦ', '袈', '衸', '螇', 'ꖴ', '隥', '隖', '衸', '硸', '螇', '閴', '隥', '隗', '衸', '硩', '碇', '隴', 'ꖵ', 'ꚦ', '螈', '衸', '螆', 'ꖳ', 'ꖥ', 'ꚦ', '袈', '衸', '螇', 'ꖴ', '隥', '隖', '衸', '硸', '螇', '閴', '隥', '隗', '衸', '硩', '碇', '隴', 'ꖵ', 'ꚦ', '螈', '衸', '螆', 'ꖳ', 'ꖥ', 'ꚦ', '袈', '衸', '螇', 'ꖴ', '隥', 'Ꚗ', '袈', '硸', '螇', '閴', '隥', '隗', '衸', '硩', '碇', '隴', 'ꖵ', 'ꚦ', '螈', '蝸', '螆', 'ꖳ', 'ꖵ', 'ꚦ', '袈', '衸', '螇', 'ꖴ', '隥', 'Ꚗ', '袈', '硸', '螇', '閴', '隥', '隗', '衸', '硹', '碇', '隴', 'ꖵ', 'ꖦ', '螈', '蝸', '螆', 'ꖳ', 'ꖵ', 'ꚦ', '螈', '衸', '螇', 'ꖴ', '隥', 'Ꚗ', '袈', '硸', '螇', '閴', '隥', '隗', '衸', '硹', '瞇', '閴', 'ꖴ', 'ꖦ', '螈', '蝸', '螆', 'ꗃ', 'ꖵ', 'ꚦ', '螈', '衸', '螇', 'ꖴ', 'ꚥ', 'Ꚗ', '袈', '硸', '螇', 'ꖴ', '隥', '隖', '衸', '硹', '瞇', '閴', 'ꖴ', 'ꖦ', '鞇', '蝸', '螆', 'ꗃ', 'ꖵ', 'ꚦ', '螈', '衸', '螆', 'ꖴ', 'ꖥ', 'Ꚗ', '袈', '衸', '螇', 'ꖴ', '隥', '隖', '衸', '硹', '瞇', '閴', 'ꖴ', 'ꖦ', '鞇', '蝸', '螖', 'ꗃ', 'ꖵ', 'ꚦ', '螈', '衸', '螆', 'ꖳ', 'ꖥ', 'ꚦ', '袈', '衸', '螇', 'ꖴ', '隥', '隖', '衸', '硸', '螇', '閴', 'ꖴ', 'ꖦ', '鞇', '蝸', '螖', 'ꗃ', 'ꖵ', 'ꚦ', '螈', '衸', '螆', 'ꖳ', 'ꖥ', 'ꚦ', '袈', '衸', '螇', 'ꖴ', '隥', '隖', '衸', '硸', '螇', '閴', 'ꖴ', 'ꖦ', '鞇', '蝸', '螖', 'ꗃ', 'ꖵ', 'ꚦ', '袈', '衸', '螆', 'ꖳ', 'ꖥ', 'ꚦ', '衸', '衸', '螇', 'ꖴ', '隥', 'Ꚗ', '袈', '硸', '螇', '閴', 'ꖴ', 'ꖦ', '鞇', '蝸', '螖', 'ꗃ', 'ꖵ', 'ꚦ', '螈', '衸', '螆', 'ꖳ', 'ꖥ', 'ꚦ', '袈', '衸', '螇', 'ꖴ', '隥', 'Ꚗ', '袈', '硸', '螇', '閴', 'ꖴ', 'ꖦ', '鞇', '蝸', '螖', 'ꗃ', 'ꖵ', 'ꖦ', '螈', '蝸', '螆', 'ꖳ', 'ꖵ', 'ꚦ', '袈', '衸', '螇', 'ꖴ', '隥', 'Ꚗ', '袈', '硸', '螇', '閴', 'ꖴ', 'ꖦ', '鞇', '螈', '螖', 'ꗃ', 'ꖴ', 'ꖦ', '螈', '蝸', '螆', 'ꖳ', 'ꖵ', 'ꚦ', '螈', '衸', '螇', 'ꖴ', '隥', 'Ꚗ', '袈', '硸', '螇', '閴', 'ꖴ', 'ꖥ', '鞇', '螈', '蚖', '꓃', 'ꖥ', 'ꖦ', '鞇', '蝸', '螆', 'ꗃ', 'ꖵ', 'ꚦ', '螈', '硸', '螇'};
    private static final int[] shengXiao = new int[]{R$string.mouse, R$string.cattle, R$string.tiger, R$string.rabbit, R$string.dragon, R$string.snake, R$string.horse, R$string.sheep, R$string.monkey, R$string.chicken, R$string.dog, R$string.pig};
    private int daysCounts;
    private boolean isLeap;
    private int lunarDay;
    private int lunarMonth;
    private int lunarYear;
    private Context mContext;
    private int solarDay;
    private int solarMonth;
    private int solarYear;
    private boolean validate = false;

    static {
        lunarFestivalInfo.put("1.1", Integer.valueOf(R$string.spring_day));
        lunarFestivalInfo.put("1.15", Integer.valueOf(R$string.lanterns));
        lunarFestivalInfo.put("5.5", Integer.valueOf(R$string.dragon_boat));
        lunarFestivalInfo.put("7.7", Integer.valueOf(R$string.qixi));
        lunarFestivalInfo.put("8.15", Integer.valueOf(R$string.mid_autumn));
        lunarFestivalInfo.put("9.9", Integer.valueOf(R$string.chongyang));
        lunarFestivalInfo.put("12.8", Integer.valueOf(R$string.laba));
        TYPE_CNHOLIDAY.put("0101", Integer.valueOf(R$string.solar_calendar_jan_01));
        TYPE_CNHOLIDAY.put("0214", Integer.valueOf(R$string.solar_calendar_feb_14));
        TYPE_CNHOLIDAY.put("0308", Integer.valueOf(R$string.solar_calendar_mar_8));
        TYPE_CNHOLIDAY.put("0501", Integer.valueOf(R$string.solar_calendar_may_01));
        TYPE_CNHOLIDAY.put("0504", Integer.valueOf(R$string.solar_calendar_may_04));
        TYPE_CNHOLIDAY.put("0601", Integer.valueOf(R$string.solar_calendar_jun_01));
        TYPE_CNHOLIDAY.put("0910", Integer.valueOf(R$string.solar_calendar_sep_10));
        TYPE_CNHOLIDAY.put("1001", Integer.valueOf(R$string.solar_calendar_oct_01));
        TYPE_CNHOLIDAY.put("1225", Integer.valueOf(R$string.solar_calendar_dec_25));
        TYPE_TWHOLIDAY.put("0101", Integer.valueOf(R$string.solar_calendar_jan_01));
        TYPE_TWHOLIDAY.put("0214", Integer.valueOf(R$string.solar_calendar_feb_14));
        TYPE_TWHOLIDAY.put("0308", Integer.valueOf(R$string.solar_calendar_mar_8));
        TYPE_TWHOLIDAY.put("0501", Integer.valueOf(R$string.solar_calendar_may_01));
        TYPE_TWHOLIDAY.put("1225", Integer.valueOf(R$string.solar_calendar_dec_25));
    }

    public static int getCNHoliday(String name) {
        return TYPE_CNHOLIDAY.get(name) == null ? -1 : ((Integer) TYPE_CNHOLIDAY.get(name)).intValue();
    }

    public static int getTWHoliday(String name) {
        return TYPE_TWHOLIDAY.get(name) == null ? -1 : ((Integer) TYPE_TWHOLIDAY.get(name)).intValue();
    }

    public LunarCalendar(Context context) {
        this.mContext = context;
    }

    public void setLunarDate(int year, int month, int day) {
        validateDate(year, month, day);
        this.solarYear = year;
        this.solarMonth = month;
        this.solarDay = day;
        this.isLeap = false;
        calcDaysFromBaseDate();
        solarDatetoLunarDate();
    }

    private int getDaysPerLunarYear(int lunarYear) {
        if (lunarYear < 1900 || lunarYear > 2099) {
            return 0;
        }
        int i;
        int daysPerLunarYear = 348;
        for (int i2 = 32768; i2 > 8; i2 >>= 1) {
            if ((lunarDateInfo[lunarYear - 1900] & i2) == i2) {
                i = 1;
            } else {
                i = 0;
            }
            daysPerLunarYear += i;
        }
        if (!((lunarDateInfo[lunarYear - 1900] & 15) == 0 || 15 == (lunarDateInfo[lunarYear - 1900] & 15))) {
            if ((lunarDateInfo[(lunarYear - 1900) + 1] & 15) == 15) {
                i = 30;
            } else {
                i = 29;
            }
            daysPerLunarYear += i;
        }
        return daysPerLunarYear;
    }

    private void validateDate(int year, int month, int day) {
        this.validate = true;
        if (year >= 1901 && year <= 2037) {
            if (month >= 1 && month <= 12) {
                switch (day) {
                    case 1:
                    case 3:
                    case 5:
                    case 7:
                    case 8:
                    case 10:
                    case 12:
                        if (day < 1 || day > 31) {
                            this.validate = false;
                            break;
                        }
                    case 2:
                        if (!((GregorianCalendar) Calendar.getInstance(Locale.getDefault())).isLeapYear(year)) {
                            if (day < 1 || day > 28) {
                                this.validate = false;
                                break;
                            }
                        } else if (day < 1 || day > 29) {
                            this.validate = false;
                            break;
                        }
                    case 4:
                    case 6:
                    case 9:
                    case 11:
                        if (day < 1 || day > 30) {
                            this.validate = false;
                            break;
                        }
                    default:
                        break;
                }
            }
            this.validate = false;
        } else {
            this.validate = false;
        }
        if (!this.validate) {
            this.solarYear = -1;
            this.solarMonth = -1;
            this.solarDay = -1;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void calcDaysFromBaseDate() {
        if (this.validate) {
            Calendar cal = Calendar.getInstance(Locale.getDefault());
            this.daysCounts = DAYS_OF_YEAR * (this.solarYear - 1900);
            boolean leap = ((GregorianCalendar) cal).isLeapYear(this.solarYear);
            int FebDays;
            if (leap) {
                FebDays = 29;
            } else {
                FebDays = 28;
            }
            switch (this.solarMonth) {
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    break;
                case 6:
                    break;
                case 7:
                    break;
                case 8:
                    break;
                case 9:
                    break;
                case 10:
                    break;
                case 11:
                    break;
                case 12:
                    this.daysCounts += 30;
                    break;
            }
        }
    }

    private void solarDatetoLunarDate() {
        if (this.validate) {
            int daysInPerLunarYear = 0;
            int dyasInPreLunarMonth = 0;
            int tempDaysCounts = this.daysCounts - 30;
            int i = 1900;
            while (tempDaysCounts > 0 && i < 2099) {
                daysInPerLunarYear = getDaysPerLunarYear(i);
                tempDaysCounts -= daysInPerLunarYear;
                i++;
            }
            if (tempDaysCounts < 0) {
                tempDaysCounts += daysInPerLunarYear;
                i--;
            }
            this.lunarYear = i;
            int leapMonth = lunarDateInfo[this.lunarYear - 1900] & 15;
            if (15 == leapMonth) {
                leapMonth = 0;
            }
            this.isLeap = false;
            i = 1;
            while (i <= 12 && tempDaysCounts > 0) {
                if (leapMonth > 0 && i == leapMonth + 1 && !this.isLeap) {
                    i--;
                    this.isLeap = true;
                    if ((lunarDateInfo[(this.lunarYear - 1900) + 1] & 15) == 15) {
                        dyasInPreLunarMonth = 30;
                    } else {
                        dyasInPreLunarMonth = 29;
                    }
                } else if ((lunarDateInfo[this.lunarYear - 1900] & (32768 >> (i - 1))) == (32768 >> (i - 1))) {
                    dyasInPreLunarMonth = 30;
                } else {
                    dyasInPreLunarMonth = 29;
                }
                if (this.isLeap && i == leapMonth + 1) {
                    this.isLeap = false;
                }
                tempDaysCounts -= dyasInPreLunarMonth;
                i++;
            }
            if (tempDaysCounts == 0 && leapMonth > 0 && i == leapMonth + 1) {
                if (this.isLeap) {
                    this.isLeap = false;
                } else {
                    this.isLeap = true;
                    i--;
                }
            }
            if (tempDaysCounts < 0) {
                tempDaysCounts += dyasInPreLunarMonth;
                i--;
            }
            this.lunarMonth = i;
            this.lunarDay = tempDaysCounts + 1;
        }
    }

    public String getLunarFestival() {
        if (!this.validate) {
            return BuildConfig.FLAVOR;
        }
        if (12 == this.lunarMonth) {
            if ((lunarDateInfo[this.lunarYear - 1900] & (65536 >> this.lunarMonth)) == 0) {
                if (29 == this.lunarDay) {
                    return this.mContext.getString(R$string.chuxi);
                }
            } else if (30 == this.lunarDay) {
                return this.mContext.getString(R$string.chuxi);
            }
        }
        if (!this.isLeap) {
            Integer festivalID = (Integer) lunarFestivalInfo.get(this.lunarMonth + "." + this.lunarDay);
            if (festivalID != null) {
                return this.mContext.getString(festivalID.intValue());
            }
        }
        return BuildConfig.FLAVOR;
    }

    public String getLunarTerm() {
        if (!this.validate) {
            return BuildConfig.FLAVOR;
        }
        int index = (((this.solarYear - 1900) - 1) * 6) + ((this.solarMonth - 1) / 2);
        char termInfo;
        if (this.solarMonth % 2 == 0) {
            termInfo = (char) (lunarTermInfo[index] & 255);
        } else {
            termInfo = (char) (lunarTermInfo[index] >> 8);
        }
        if (this.solarDay < 15 && (termInfo >> 4) == 15 - this.solarDay) {
            return this.mContext.getString(lunarTerm[(this.solarMonth - 1) * 2]);
        }
        if (this.solarDay <= 15 || (termInfo & 15) != this.solarDay - 15) {
            return BuildConfig.FLAVOR;
        }
        return this.mContext.getString(lunarTerm[((this.solarMonth - 1) * 2) + 1]);
    }

    public int getLunarDate(int year, int month, int day) {
        setLunarDate(year, month, day);
        return (this.lunarMonth * 100) + this.lunarDay;
    }

    public String getChineseMonthDay() {
        String chineseFestival = getLunarFestival();
        if (chineseFestival != null && !chineseFestival.equals(BuildConfig.FLAVOR)) {
            return chineseFestival;
        }
        String chineseTerm = getLunarTerm();
        if (chineseTerm == null || chineseTerm.equals(BuildConfig.FLAVOR)) {
            return BuildConfig.FLAVOR;
        }
        return chineseTerm;
    }
}

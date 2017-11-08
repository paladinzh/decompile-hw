package com.android.contacts.util;

import android.text.format.Time;
import com.amap.api.services.core.AMapException;

public class LunarDate {
    private static final char[] lunarDateInfo = new char[]{'䯘', '䫠', 'ꕰ', '哕', '퉠', '?', '啔', '嚯', '髐', '嗒', '䫠', 'ꖶ', 'ꓐ', '퉐', '튕', '땏', '횠', '궢', '閰', '䥷', '䥿', '꒰', '뒵', '橐', '浀', 'ꭔ', '⭯', '镰', '勲', '䥰', '敦', '풠', '', '檕', '嫟', '⭠', '蛣', '鋯', '죗', '쥟', '풠', '?', '땟', '嚠', 'ꖴ', '◟', '鋐', '튲', 'ꥐ', '땗', '沠', '땐', '单', '䶯', 'ꖰ', '䕳', '势', 'ꦨ', '', '檠', '꺦', 'ꭐ', '䭠', 'ꫤ', 'ꕰ', '剠', '', '?', '字', '嚠', '雐', '䷕', '䫐', 'ꓐ', '퓔', '퉐', '하', '땀', '뚠', '閦', '閿', '䦰', 'ꥴ', '꒰', '뉺', '橐', '浀', '꽆', 'ꭠ', '镰', '䫵', '䥰', '撰', '璣', '', '歘', '嫀', 'ꭠ', '雕', '鋠', '쥠', '?', '풠', '?', '畒', '嚠', 'ꮷ', '◐', '鋐', '쪵', 'ꥐ', '뒠', '몤', '교', '嗙', '䮠', 'ꖰ', '其', '势', 'ꤰ', '祔', '檠', '교', '孒', '䭠', 'ꛦ', 'ꓠ', '퉠', '', '픰', '媠', '皣', '雐', '䫻', '䫐', 'ꓐ', '킶', '퉟', '픠', '?', '떠', '囐', '喲', '䦰', 'ꕷ', '꒰', '꩐', '뉕', '洯', '궠', '䭣', '鍿', '䧸', '䥰', '撰', '梦', '', '欠', 'ꛄ', 'ꫯ', '鋠', '틣', '쥠', '핗', '풠', '?', '嵕', '嚠', 'ꛐ', '嗔', '勐', 'ꦸ', 'ꥐ', '뒠', '뚦', '교', '喠', 'ꮤ', 'ꖰ', '劰', '뉳', '椰', '猷', '檠', '교', '䭕', '䭯', 'ꕰ', '哤', '퉠', '', '픠', '?', '檦', '囟', '䫠', '꧔', 'ꓐ', '텐', '', '픠'};
    private static final int[] lunarSumDaysPerYear = new int[]{384, 738, 1093, 1476, 1830, 2185, 2569, 2923, 3278, 3662, 4016, 4400, 4754, 5108, 5492, 5846, 6201, 6585, 6940, 7324, 7678, 8032, 8416, 8770, 9124, 9509, 9863, 10218, 10602, 10956, 11339, 11693, 12048, 12432, 12787, 13141, 13525, 13879, 14263, 14617, 14971, 15355, 15710, 16064, 16449, 16803, 17157, 17541, 17895, 18279, 18633, 18988, 19372, 19726, 20081, 20465, 20819, 21202, 21557, 21911, 22295, 22650, 23004, 23388, 23743, 24096, 24480, 24835, 25219, 25573, 25928, 26312, 26666, 27020, 27404, 27758, 28142, 28496, 28851, 29235, 29590, 29944, 30328, 30682, 31066, 31420, 31774, 32158, 32513, 32868, 33252, 33606, 33960, 34343, 34698, 35082, 35436, 35791, 36175, 36529, 36883, 37267, 37621, 37976, 38360, 38714, 39099, 39453, 39807, 40191, 40545, 40899, 41283, 41638, 42022, 42376, 42731, 43115, 43469, 43823, 44207, 44561, 44916, 45300, 45654, 46038, 46392, 46746, 47130, 47485, 47839, 48223, 48578, 48962, 49316, 49670, 50054, 50408, 50762, 51146, 51501, 51856, 52240, 52594, 52978, 53332, 53686, 54070, 54424, 54779, 55163, 55518, 55902, 56256, 56610, 56993, 57348, 57702, 58086, 58441, 58795, 59179, 59533, 59917, 60271, 60626, 61010, 61364, 61719, 62103, 62457, 62841, 63195, 63549, 63933, 64288, 64642, 65026, 65381, 65735, 66119, 66473, 66857, 67211, 67566, 67950, 68304, 68659, 69042, 69396, 69780, 70134, 70489, 70873, 71228, 71582, 71966, 72320, 72674};
    public int lunarDay = -1;
    public boolean lunarLeapMonth = false;
    public int lunarMonth = -1;
    public int lunarYear = -1;
    public int solarDay = -1;
    public int solarMonth = -1;
    public int solarYear = -1;

    public void set(int lunarYear, int lunarMonth, int lunarMonthday, boolean leapMonth, boolean isValidate) {
        this.lunarYear = lunarYear;
        this.lunarMonth = lunarMonth;
        this.lunarDay = lunarMonthday;
        this.lunarLeapMonth = leapMonth;
        normalize();
        lunarDate2SolarDate(isValidate);
    }

    public void set(int solarYear, int solarMonth, int solarMonthDay, boolean isValidate) {
        this.solarYear = solarYear;
        this.solarMonth = solarMonth;
        this.solarDay = solarMonthDay;
        solarDate2LunarDate(isValidate);
    }

    public void normalize() {
        if (this.lunarMonth < 0) {
            this.lunarMonth = lunarDateInfo[this.lunarYear - 1900] & 15;
            if (this.lunarMonth == 15) {
                throw new IllegalStateException("lunar " + this.lunarYear + " does not has leap month!");
            }
            this.lunarLeapMonth = true;
        }
    }

    private void lunarDate2SolarDate(boolean isValidate) {
        int i = 30;
        if (!isValidate || validateLunarDate()) {
            Time time = new Time("UTC");
            int sumDaysCount = lunarSumDaysPerYear[(this.lunarYear - 1900) - 1] + 30;
            time.setJulianDay(sumDaysCount + 2415021);
            time.normalize(true);
            int i2;
            int dyasInPreLunarMonth;
            if (this.lunarLeapMonth) {
                for (i2 = 1; i2 <= this.lunarMonth; i2++) {
                    if ((lunarDateInfo[this.lunarYear - 1900] & (32768 >> (i2 - 1))) == (32768 >> (i2 - 1))) {
                        dyasInPreLunarMonth = 30;
                    } else {
                        dyasInPreLunarMonth = 29;
                    }
                    sumDaysCount += dyasInPreLunarMonth;
                }
            } else {
                int leapMonth = lunarDateInfo[this.lunarYear - 1900] & 15;
                if (15 == leapMonth || leapMonth == 0) {
                    leapMonth = 13;
                }
                for (i2 = 1; i2 < this.lunarMonth; i2++) {
                    if ((lunarDateInfo[this.lunarYear - 1900] & (32768 >> (i2 - 1))) == (32768 >> (i2 - 1))) {
                        dyasInPreLunarMonth = 30;
                    } else {
                        dyasInPreLunarMonth = 29;
                    }
                    sumDaysCount += dyasInPreLunarMonth;
                }
                if (this.lunarMonth > leapMonth) {
                    if ((lunarDateInfo[(this.lunarYear - 1900) + 1] & 15) != 15) {
                        i = 29;
                    }
                    sumDaysCount += i;
                }
            }
            time.setJulianDay(sumDaysCount + 2415021);
            time.normalize(true);
            time.setJulianDay(((sumDaysCount + this.lunarDay) - 1) + 2415021);
            time.normalize(true);
            this.solarYear = time.year;
            this.solarMonth = time.month + 1;
            this.solarDay = time.monthDay;
            return;
        }
        throw new IllegalStateException("lunar date switch to solar date failed because of the invalid lunar date");
    }

    public void solarDate2LunarDate(boolean isValidate) {
        if (!isValidate || validateSolarDate()) {
            int dyasInPreLunarMonth = 0;
            int tempDaysCounts = calcDaysFromBaseDate() - 30;
            if (tempDaysCounts < lunarSumDaysPerYear[(this.solarYear - 1) - 1900]) {
                this.lunarYear = this.solarYear - 1;
            } else {
                this.lunarYear = this.solarYear;
            }
            tempDaysCounts -= lunarSumDaysPerYear[(this.lunarYear - 1) - 1900];
            int leapMonth = lunarDateInfo[this.lunarYear - 1900] & 15;
            if (15 == leapMonth) {
                leapMonth = 0;
            }
            this.lunarLeapMonth = false;
            int i = 1;
            while (i <= 12 && tempDaysCounts > 0) {
                if (leapMonth > 0 && i == leapMonth + 1 && !this.lunarLeapMonth) {
                    i--;
                    this.lunarLeapMonth = true;
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
                if (this.lunarLeapMonth && i == leapMonth + 1) {
                    this.lunarLeapMonth = false;
                }
                tempDaysCounts -= dyasInPreLunarMonth;
                i++;
            }
            if (tempDaysCounts == 0 && leapMonth > 0 && i == leapMonth + 1) {
                if (this.lunarLeapMonth) {
                    this.lunarLeapMonth = false;
                } else {
                    this.lunarLeapMonth = true;
                    i--;
                }
            }
            if (tempDaysCounts < 0) {
                tempDaysCounts += dyasInPreLunarMonth;
                i--;
            }
            this.lunarMonth = i;
            this.lunarDay = tempDaysCounts + 1;
            return;
        }
        throw new IllegalStateException("solar date switch to lunar date failed because of the invalid solar date");
    }

    private int calcDaysFromBaseDate() {
        int FebDays;
        int daysCounts = (this.solarYear - 1900) * 365;
        boolean leap = isSolarLeapYear(this.solarYear);
        if (leap) {
            FebDays = 29;
        } else {
            FebDays = 28;
        }
        int[] sum = new int[]{this.solarDay, 31, FebDays, 31, 30, 31, 30, 31, 31, 30, 31, 30};
        for (int i = 0; i < this.solarMonth; i++) {
            daysCounts += sum[i];
        }
        daysCounts = (daysCounts - 1) + ((this.solarYear - 1900) / 4);
        if (leap) {
            return daysCounts - 1;
        }
        return daysCounts;
    }

    public boolean validateLunarDate() {
        if (this.lunarYear < AMapException.CODE_AMAP_CLIENT_INVALID_PARAMETER || this.lunarYear > 2038) {
            return false;
        }
        if (this.lunarMonth < 0) {
            this.lunarMonth = lunarDateInfo[this.lunarYear - 1900] & 15;
            if (this.lunarMonth == 15) {
                throw new IllegalStateException("lunar " + this.lunarYear + " does not has leap month!");
            }
            this.lunarLeapMonth = true;
        }
        if (this.lunarMonth < 1 || this.lunarMonth > 12) {
            return false;
        }
        if (this.lunarLeapMonth && this.lunarMonth != (lunarDateInfo[this.lunarYear - 1900] & 15)) {
            return false;
        }
        int maxDayInMonth;
        if (this.lunarLeapMonth) {
            if ((lunarDateInfo[(this.lunarYear - 1900) + 1] & 15) == 15) {
                maxDayInMonth = 30;
            } else {
                maxDayInMonth = 29;
            }
        } else if ((lunarDateInfo[this.lunarYear - 1900] & (32768 >> (this.lunarMonth - 1))) == (32768 >> (this.lunarMonth - 1))) {
            maxDayInMonth = 30;
        } else {
            maxDayInMonth = 29;
        }
        return this.lunarDay >= 1 && this.lunarDay <= maxDayInMonth;
    }

    public boolean validateSolarDate() {
        if (this.solarYear < AMapException.CODE_AMAP_CLIENT_INVALID_PARAMETER || this.solarYear > 2038) {
            return false;
        }
        if (this.solarMonth < 1 || this.solarMonth > 12) {
            return false;
        }
        switch (this.solarMonth) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                if (this.solarDay < 1 || this.solarDay > 31) {
                    return false;
                }
                return true;
            case 2:
                if (isSolarLeapYear(this.solarYear)) {
                    if (this.solarDay < 1 || this.solarDay > 29) {
                        return false;
                    }
                    return true;
                } else if (this.solarDay < 1 || this.solarDay > 28) {
                    return false;
                } else {
                    return true;
                }
            case 4:
            case 6:
            case 9:
            case 11:
                if (this.solarDay < 1 || this.solarDay > 30) {
                    return false;
                }
                return true;
            default:
                return true;
        }
    }

    public boolean isSolarLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
    }

    public String toString() {
        String str;
        StringBuilder append = new StringBuilder().append("solar").append(this.solarYear).append(switchSingleDigit2DoubleDigit(this.solarMonth)).append(switchSingleDigit2DoubleDigit(this.solarDay)).append(",").append("lunar").append(this.lunarYear).append(switchSingleDigit2DoubleDigit(this.lunarMonth)).append(switchSingleDigit2DoubleDigit(this.lunarDay));
        if (this.lunarLeapMonth) {
            str = "leap";
        } else {
            str = "";
        }
        append.append(str);
        return super.toString();
    }

    private String switchSingleDigit2DoubleDigit(int digit) {
        if (digit < 10) {
            return "0" + digit;
        }
        return Integer.toString(digit);
    }

    public static int getDayCountsOfLunarMonth(int lunarYear, int lunarMonth, boolean isLeap) {
        int i = 30;
        if (isLeap) {
            if ((lunarDateInfo[(lunarYear - 1900) + 1] & 15) != 15) {
                i = 29;
            }
            return i;
        }
        if ((lunarDateInfo[lunarYear - 1900] & (32768 >> (lunarMonth - 1))) != (32768 >> (lunarMonth - 1))) {
            i = 29;
        }
        return i;
    }

    public static boolean hasLeapOfLunarMonth(int lunarYear, int lunarMonth) {
        if (lunarMonth != (lunarDateInfo[lunarYear - 1900] & 15)) {
            return false;
        }
        return true;
    }

    public static int getLeapMonthOfLunar(int lunarYear) {
        int leapMonth = lunarDateInfo[lunarYear - 1900] & 15;
        if (15 == leapMonth) {
            return 0;
        }
        return leapMonth;
    }

    public int getLunarYear() {
        return this.lunarYear;
    }

    public int getLunarMonth() {
        return this.lunarMonth;
    }

    public int getLunarDay() {
        return this.lunarDay;
    }

    public int getSolarYear() {
        return this.solarYear;
    }

    public int getSolarMonth() {
        return this.solarMonth;
    }

    public int getSolarDay() {
        return this.solarDay;
    }

    public boolean islunarLeapMonth() {
        return this.lunarLeapMonth;
    }
}

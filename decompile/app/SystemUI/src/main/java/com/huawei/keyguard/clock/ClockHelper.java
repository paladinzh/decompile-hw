package com.huawei.keyguard.clock;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.database.ContentObserver;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import com.huawei.keyguard.events.TimeZoneFinder;
import com.huawei.keyguard.events.TimeZoneFinder.TimeZoneListener;
import com.huawei.keyguard.events.TimeZoneManager;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.LunarCalendar;
import com.huawei.keyguard.util.OsUtils;
import fyusion.vislib.BuildConfig;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ClockHelper {
    private String[] mAMPMStrings;
    private Context mContext;
    private ContentObserver mDefTimeZoneObserver = new ContentObserver(null) {
        public void onChange(boolean selfChange) {
            TimeZoneManager.getInstance().setDefaultTimeZone(ClockHelper.this.mContext, OsUtils.getSystemString(ClockHelper.this.mContext, "keyguard_default_time_zone"));
            if (ClockHelper.this.mDualClockCallback != null) {
                ClockHelper.this.mDualClockCallback.onDualClockUpdate(ClockHelper.this.needDualClock(ClockHelper.this.mContext));
            }
        }
    };
    private DualClockCallback mDualClockCallback;
    private boolean mIsShowLunar = false;
    private ContentObserver mNitzTimeObserver = new ContentObserver(null) {
        public void onChange(boolean selfChange) {
            TimeZoneManager.getInstance().clearCacheTimeZone();
            TimeZoneManager.getInstance().reFinderTimeZone(ClockHelper.this.mContext);
        }
    };
    private boolean mShowAMPM;
    private String mTimeFormat;
    private TimeZoneListener mTimeZoneListener = new TimeZoneListener() {
        public void onTimeZoneChange(TimeZoneFinder finder, TimeZone timezone) {
            if (ClockHelper.this.mDualClockCallback != null) {
                ClockHelper.this.mDualClockCallback.onDualClockUpdate(ClockHelper.this.needDualClock(ClockHelper.this.mContext));
            }
        }
    };

    public static class DualClockCallback {
        protected void onDualClockUpdate(boolean needDualClock) {
        }
    }

    public ClockHelper(Context context, DualClockCallback callback) {
        boolean z;
        this.mContext = context;
        this.mDualClockCallback = callback;
        if (DateFormat.is24HourFormat(context, OsUtils.getCurrentUser())) {
            z = false;
        } else {
            z = true;
        }
        this.mShowAMPM = z;
        this.mAMPMStrings = new DateFormatSymbols().getAmPmStrings();
        this.mTimeFormat = this.mShowAMPM ? "h:mm" : "kk:mm";
        String lauguage = context.getResources().getConfiguration().locale.getCountry();
        if (lauguage.equals("CN") || lauguage.equals("HK") || lauguage.equals("TW")) {
            this.mIsShowLunar = true;
        } else {
            this.mIsShowLunar = false;
        }
    }

    public void registerDualClockListeners() {
        if (isDualClockEnabled(this.mContext)) {
            TimeZoneManager.getInstance().registerTimeZoneListener(this.mContext, this.mTimeZoneListener);
            OsUtils.registerContentObserver(this.mContext, System.getUriFor("nitz_timezone_info"), true, this.mNitzTimeObserver);
            OsUtils.registerContentObserver(this.mContext, System.getUriFor("keyguard_default_time_zone"), true, this.mDefTimeZoneObserver);
        }
    }

    public void unregisterDualClockListeners() {
        if (isDualClockEnabled(this.mContext)) {
            TimeZoneManager.getInstance().unregisterTimeZoneListener(this.mTimeZoneListener);
            this.mContext.getContentResolver().unregisterContentObserver(this.mNitzTimeObserver);
            this.mContext.getContentResolver().unregisterContentObserver(this.mDefTimeZoneObserver);
        }
    }

    public boolean isDualClockEnabled(Context context) {
        boolean z = true;
        try {
            if (OsUtils.getSystemInt(context, "dual_clocks", 0) <= 0) {
                z = false;
            }
            return z;
        } catch (Exception e) {
            return true;
        }
    }

    public boolean needDualClock(Context context) {
        if (!isDualClockEnabled(context)) {
            return false;
        }
        if (((TelephonyManager) context.getSystemService("phone")).isNetworkRoaming()) {
            return true;
        }
        TimeZone timeZone = TimeZoneManager.getInstance().getDefaultTimeZone(context);
        if (timeZone != null) {
            long currTime = System.currentTimeMillis();
            if (timeZone.getOffset(currTime) != TimeZone.getDefault().getOffset(currTime)) {
                return true;
            }
        }
        return false;
    }

    public Calendar getRoamingCalendar() {
        return Calendar.getInstance();
    }

    public Calendar getDefaultCalendar() {
        TimeZone deftimeZone = TimeZoneManager.getInstance().getDefaultTimeZone(this.mContext);
        if (deftimeZone != null) {
            return Calendar.getInstance(deftimeZone);
        }
        return Calendar.getInstance();
    }

    public boolean isShowAmpm() {
        return this.mShowAMPM;
    }

    public String getAmpmString(Calendar calendar) {
        int ampm = calendar.get(9);
        int hr = calendar.get(10);
        if (hr == 0) {
            hr = 12;
        }
        return ClockUtil.getFormatChinaDateTimeAmpm(this.mContext, this.mAMPMStrings[ampm], BuildConfig.FLAVOR + hr + ":");
    }

    public char[] getDigitCharForTime(Calendar calendar) {
        calendar.setTimeInMillis(System.currentTimeMillis());
        String timeStr = BuildConfig.FLAVOR;
        if ("kk:mm".equals(this.mTimeFormat)) {
            timeStr = zeroPad(calendar.get(11), 2);
        } else if ("h:mm".equals(this.mTimeFormat)) {
            timeStr = zeroPad(calendar.get(10), 2);
            if (timeStr.equals("00")) {
                timeStr = "12";
            }
        }
        timeStr = timeStr + zeroPad(calendar.get(12), 2);
        if (timeStr.length() == 4) {
            return timeStr.toCharArray();
        }
        HwLog.i("ClockHelper", "Lock get timeStr error !");
        return new char[4];
    }

    private String zeroPad(int inValue, int inMinDigits) {
        return String.format(Locale.US, "%0" + inMinDigits + "d", new Object[]{Integer.valueOf(inValue)});
    }

    public String getDateString(TimeZone timeZone, boolean showLunarHoliday) {
        if (timeZone == null) {
            return BuildConfig.FLAVOR;
        }
        String dateStr;
        long millis = System.currentTimeMillis();
        long newTime = millis + ((long) (timeZone.getOffset(millis) - TimeZone.getDefault().getOffset(millis)));
        if (this.mIsShowLunar) {
            dateStr = DateUtils.formatDateTime(this.mContext, newTime, 65544).replace(" ", BuildConfig.FLAVOR);
            dateStr = dateStr + DateUtils.formatDateTime(this.mContext, newTime, 32770);
        } else {
            dateStr = DateUtils.formatDateTime(this.mContext, new Date().getTime(), 98330);
        }
        if (this.mIsShowLunar && showLunarHoliday) {
            dateStr = dateStr + "  " + getLunar();
        }
        return dateStr;
    }

    private String getLunar() {
        String chinaDate = BuildConfig.FLAVOR;
        Calendar cal = Calendar.getInstance();
        int year = cal.get(1);
        int month = cal.get(2) + 1;
        int day = cal.get(5);
        chinaDate = getHolidayCalendar(month, day);
        if (chinaDate.equals(BuildConfig.FLAVOR)) {
            LunarCalendar lunarCal = new LunarCalendar(this.mContext);
            lunarCal.getLunarDate(year, month, day);
            return lunarCal.getChineseMonthDay();
        }
        HwLog.i("ClockHelper", "Showing solar Holiday");
        return chinaDate;
    }

    private String getHolidayCalendar(int month, int day) {
        String dateStr = String.format(Locale.US, "%02d", new Object[]{Integer.valueOf(month)}) + String.format(Locale.US, "%02d", new Object[]{Integer.valueOf(day)});
        String holidayStr = BuildConfig.FLAVOR;
        try {
            int holiday;
            if (this.mContext.getResources().getConfiguration().locale.getCountry().equals("TW")) {
                holiday = LunarCalendar.getTWHoliday(dateStr);
            } else {
                holiday = LunarCalendar.getCNHoliday(dateStr);
            }
            holidayStr = this.mContext.getResources().getString(holiday);
        } catch (NotFoundException e) {
            HwLog.i("ClockHelper", "no solar holiday!");
            holidayStr = BuildConfig.FLAVOR;
        }
        HwLog.i("ClockHelper", "holidayStr = " + holidayStr);
        return holidayStr;
    }
}

package com.huawei.keyguard.widget;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.keyguard.R$id;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.clock.ClockUtil;
import com.huawei.keyguard.events.EventCenter;
import com.huawei.keyguard.events.EventCenter.IEventListener;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.LunarCalendar;
import com.huawei.keyguard.util.WindowCounter;
import com.huawei.keyguard.view.widget.ClockView;
import fyusion.vislib.BuildConfig;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateLunarView extends LinearLayout implements IEventListener {
    private static WindowCounter mWndCounter = new WindowCounter(ClockView.class);
    private CharSequence mDateString;
    private TextView mDateView;
    private final Handler mHandler;
    private boolean mIsShowLunar;
    private Runnable mUpdater;

    public DateLunarView(Context context) {
        this(context, null, 0);
    }

    public DateLunarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DateLunarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mIsShowLunar = false;
        this.mHandler = GlobalContext.getUIHandler();
        this.mUpdater = new Runnable() {
            public void run() {
                DateLunarView.this.updateLunarDate();
            }
        };
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mWndCounter.onAttach();
        EventCenter.getInst().listen(1, this);
    }

    private void setLanguage() {
        String lauguage = getContext().getResources().getConfiguration().locale.getCountry();
        boolean equals = (lauguage.equals("CN") || lauguage.equals("HK")) ? true : lauguage.equals("TW");
        this.mIsShowLunar = equals;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        setLanguage();
        this.mDateView = (TextView) findViewById(R$id.date_view);
        updateLunarDate();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mWndCounter.onDetach();
        this.mHandler.removeCallbacks(this.mUpdater);
        EventCenter.getInst().stopListen(this);
    }

    public boolean onReceive(Context context, Intent intent) {
        this.mHandler.removeCallbacks(this.mUpdater);
        this.mHandler.post(this.mUpdater);
        return false;
    }

    private void updateLunarDate() {
        if (this.mIsShowLunar) {
            String formatString = DateUtils.formatDateTime(getContext(), new Date().getTime(), 65544).replace(" ", BuildConfig.FLAVOR);
            this.mDateString = formatString + DateUtils.formatDateTime(getContext(), new Date().getTime(), 2);
        } else {
            int flags = 98330;
            if (ClockUtil.isShowFullMonth()) {
                flags = 32794;
            }
            if (ClockUtil.isShowFrenchCustDate(getContext())) {
                flags = 22;
            }
            this.mDateString = DateUtils.formatDateTime(getContext(), new Date().getTime(), flags);
        }
        String lunar = BuildConfig.FLAVOR;
        if (this.mIsShowLunar && !getLunar().equals(BuildConfig.FLAVOR)) {
            this.mDateString += "  " + getLunar();
        }
        if (this.mDateView != null) {
            this.mDateString = this.mDateString.toString().replace(".", BuildConfig.FLAVOR);
            HwLog.e("DateLunarView", "mDateString is: " + this.mDateString);
            this.mDateView.setText(this.mDateString);
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        setLanguage();
        updateLunarDate();
        super.onConfigurationChanged(newConfig);
    }

    private String getLunar() {
        String mChinaDate = BuildConfig.FLAVOR;
        Calendar mCalendar = Calendar.getInstance();
        int year = mCalendar.get(1);
        int month = mCalendar.get(2) + 1;
        int day = mCalendar.get(5);
        mChinaDate = getHolidayCalendar(month, day);
        if (!mChinaDate.equals(BuildConfig.FLAVOR)) {
            return mChinaDate;
        }
        LunarCalendar mLunarCalendar = new LunarCalendar(getContext());
        mLunarCalendar.getLunarDate(year, month, day);
        return mLunarCalendar.getChineseMonthDay();
    }

    private String getHolidayCalendar(int month, int day) {
        String dateStr = String.format(Locale.US, "%02d", new Object[]{Integer.valueOf(month)}) + String.format(Locale.US, "%02d", new Object[]{Integer.valueOf(day)});
        String holidayStr = BuildConfig.FLAVOR;
        try {
            int holiday;
            if (getContext().getResources().getConfiguration().locale.getCountry().equals("TW")) {
                holiday = LunarCalendar.getTWHoliday(dateStr);
            } else {
                holiday = LunarCalendar.getCNHoliday(dateStr);
            }
            return getContext().getResources().getString(holiday);
        } catch (NotFoundException e) {
            return BuildConfig.FLAVOR;
        }
    }
}

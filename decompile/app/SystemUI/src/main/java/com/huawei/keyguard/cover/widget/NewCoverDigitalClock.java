package com.huawei.keyguard.cover.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.huawei.coverscreen.HwCustCoverClockView;
import com.android.huawei.coverscreen.HwCustCoverWeatherView;
import com.android.keyguard.R$array;
import com.android.keyguard.R$bool;
import com.android.keyguard.R$id;
import com.huawei.cust.HwCustUtils;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.clock.ClockHelper;
import com.huawei.keyguard.clock.ClockUtil;
import com.huawei.keyguard.cover.AnalogClockResourceUtils;
import com.huawei.keyguard.events.EventCenter;
import com.huawei.keyguard.events.EventCenter.IContentListener;
import com.huawei.keyguard.events.EventCenter.IEventListener;
import com.huawei.keyguard.events.WeatherMonitor;
import com.huawei.keyguard.events.weather.DispalyWeatherInfo;
import com.huawei.keyguard.events.weather.WeatherInfo;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.LunarCalendarUtils;
import com.huawei.keyguard.util.OsUtils;
import com.huawei.keyguard.util.Typefaces;
import fyusion.vislib.BuildConfig;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewCoverDigitalClock extends RelativeLayout implements IEventListener, IContentListener {
    private Calendar calendar;
    private HwCustCoverClockView hwCustCoverClockView;
    private TextView mAmPm;
    private CoverAnalogClockView mAnalogClock;
    private ClockHelper mClockHelper;
    private ImageView mCurTempeUnitView;
    private TextView mCurTempeView;
    private int mCurrentTemperature;
    private TextView mDateView;
    private TextView mDigitalTime;
    private final Handler mHandler;
    private boolean mIsChinese;
    private boolean mIsUseAndroidClockFont;
    private TextView mLunarDateView;
    private Handler mWeaHandler;
    private int mWeatherIconId;
    private ImageView mWeatherInfoIcon;

    public boolean onReceive(Context context, Intent intent) {
        onTimeChanged();
        return false;
    }

    public NewCoverDigitalClock(Context context) {
        this(context, null);
    }

    public NewCoverDigitalClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mIsChinese = false;
        this.mIsUseAndroidClockFont = true;
        this.calendar = Calendar.getInstance();
        this.mCurrentTemperature = -20000;
        this.mHandler = GlobalContext.getUIHandler();
        initHwCustCoverClockView();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mClockHelper = new ClockHelper(this.mContext, null);
        String lauguage = getContext().getResources().getConfiguration().locale.getCountry();
        if (lauguage.equals("CN") || lauguage.equals("HK") || lauguage.equals("TW")) {
            this.mIsChinese = true;
        }
        View v = findViewById(R$id.cover_digital_time_textview);
        if (v instanceof TextView) {
            this.mDigitalTime = (TextView) v;
        }
        v = findViewById(R$id.cover_lunar_date_textview);
        if (v instanceof TextView) {
            this.mLunarDateView = (TextView) v;
        }
        v = findViewById(R$id.cover_am_pm_textview);
        if (v instanceof TextView) {
            this.mAmPm = (TextView) v;
        }
        v = findViewById(R$id.cover_date_time_textview);
        if (v instanceof TextView) {
            this.mDateView = (TextView) v;
        }
        this.mCurTempeView = (TextView) findViewById(R$id.current_temperature);
        this.mCurTempeUnitView = (ImageView) findViewById(R$id.cover_temperature_unit);
        this.mWeatherInfoIcon = (ImageView) findViewById(R$id.weather_icon);
        v = findViewById(R$id.cover_analog_clock);
        HwLog.w("NewCoverDigitalClock", "v instanceof CoverAnalogClockView is " + (v instanceof CoverAnalogClockView));
        HwLog.w("NewCoverDigitalClock", "AnalogClockResourceUtils.isThemeAnalogClockType is " + AnalogClockResourceUtils.isThemeAnalogClockType());
        if (v instanceof CoverAnalogClockView) {
            this.mAnalogClock = (CoverAnalogClockView) v;
            if (AnalogClockResourceUtils.isThemeAnalogClockType()) {
                this.mAnalogClock.init();
            } else {
                this.mAnalogClock.setVisibility(8);
            }
        }
        this.mIsUseAndroidClockFont = getResources().getBoolean(R$bool.coverscreen_time_use_androidclock_font);
        initHandler();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventCenter eventCenter = EventCenter.getInst();
        eventCenter.listen(1, this);
        eventCenter.listenContent(1, this);
        setSystemUiVisibility(8388616);
        onTimeChanged();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mHandler.removeCallbacksAndMessages(null);
        EventCenter eventCenter = EventCenter.getInst();
        eventCenter.stopListen(this);
        eventCenter.stopListenContent(this);
        removeAllMessages();
    }

    private void initHwCustCoverClockView() {
        this.hwCustCoverClockView = (HwCustCoverClockView) HwCustUtils.createObj(HwCustCoverClockView.class, new Object[]{getContext()});
    }

    private void onTimeChanged() {
        if (this.mDigitalTime != null) {
            this.mDigitalTime.setText(getTime());
            try {
                if (this.mIsUseAndroidClockFont) {
                    Typeface t = Typefaces.get(getContext(), "/system/fonts/AndroidClock.ttf");
                    if (t != null) {
                        this.mDigitalTime.setTypeface(t);
                    }
                    if (this.hwCustCoverClockView != null) {
                        this.hwCustCoverClockView.setDigitalTimeFont(this.mDigitalTime);
                    }
                }
            } catch (Exception ex) {
                HwLog.w("NewCoverDigitalClock", "Set AndroidClock fontface for mDigitalTime fail and got error:", ex);
            }
        }
        if (this.mAmPm != null) {
            if (DateFormat.is24HourFormat(this.mContext, OsUtils.getCurrentUser())) {
                this.mAmPm.setVisibility(8);
            } else {
                this.mAmPm.setText(getAmPm());
                this.mAmPm.setVisibility(0);
            }
        }
        if (this.mDateView != null) {
            this.mDateView.setText(getDateTime());
        }
        if (this.mLunarDateView != null && this.mIsChinese) {
            if (TextUtils.isEmpty(getLunarDate())) {
                this.mLunarDateView.setVisibility(8);
                return;
            }
            this.mLunarDateView.setText(getLunarDate());
            this.mLunarDateView.setVisibility(0);
        }
    }

    private String getAmPm() {
        if (DateFormat.is24HourFormat(this.mContext, OsUtils.getCurrentUser())) {
            return null;
        }
        String ampmString = null;
        this.calendar.setTimeInMillis(System.currentTimeMillis());
        if (this.mClockHelper != null) {
            ampmString = this.mClockHelper.getAmpmString(this.calendar);
        }
        return ampmString;
    }

    private String getDateTime() {
        long millis = System.currentTimeMillis();
        long newTime = millis + ((long) (this.calendar.getTimeZone().getOffset(millis) - TimeZone.getDefault().getOffset(millis)));
        this.calendar.setTimeInMillis(System.currentTimeMillis());
        if (!this.mIsChinese) {
            return DateUtils.formatDateTime(getContext(), newTime, 98330);
        }
        String formatString = DateUtils.formatDateTime(getContext(), newTime, 65544).replace(" ", BuildConfig.FLAVOR);
        return formatString + DateUtils.formatDateTime(getContext(), newTime, 2);
    }

    private String getLunarDate() {
        return LunarCalendarUtils.getLunar(this.calendar, this.mContext);
    }

    private SpannableString getTime() {
        this.calendar.setTimeInMillis(System.currentTimeMillis());
        TimeZone ctz = this.calendar.getTimeZone();
        java.text.DateFormat df = new SimpleDateFormat(DateFormat.getTimeFormatString(this.mContext, OsUtils.getCurrentUser()));
        df.setTimeZone(ctz);
        String sysTimeStr = df.format(this.calendar.getTime());
        SpannableString ss = ClockUtil.setTimeStr(ClockUtil.formatChinaDateTime(getContext(), sysTimeStr));
        if (DateFormat.is24HourFormat(this.mContext, OsUtils.getCurrentUser())) {
            return ss;
        }
        Matcher m = Pattern.compile("\\D*(\\d+.\\d+).*").matcher(sysTimeStr);
        if (m.find()) {
            sysTimeStr = m.group(1);
        }
        return new SpannableString(sysTimeStr);
    }

    public void onContentChange(boolean selfChange) {
        onTimeChanged();
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
        if (this.mAnalogClock != null) {
            this.mAnalogClock.setCalendar(calendar);
        }
    }

    private void initHandler() {
        this.mWeaHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        NewCoverDigitalClock.this.refreshWeatherInfo();
                        return;
                    default:
                        return;
                }
            }
        };
    }

    private void refreshWeatherInfo() {
        setWeatherSource(this.mWeatherIconId);
        Integer tempeC = Integer.valueOf(this.mCurrentTemperature);
        if (-20000 == tempeC.intValue()) {
            setCurrentTemperature("N");
        } else {
            setCurrentTemperature(tempeC.toString());
        }
    }

    private void removeAllMessages() {
        if (this.mWeaHandler.hasMessages(0)) {
            this.mWeaHandler.removeMessages(0);
        }
    }

    public void setCurrentTemperature(String tempe) {
        if (this.mCurTempeView == null || this.mCurTempeUnitView == null) {
            HwLog.w("NewCoverDigitalClock", "setCurrentTemperature, mCurTempeView=" + this.mCurTempeView + ", mCurTempeUnitView=" + this.mCurTempeUnitView);
            return;
        }
        if (tempe == null || !tempe.equals("N")) {
            this.mCurTempeView.setText(tempe);
            this.mCurTempeView.setVisibility(0);
            this.mCurTempeUnitView.setVisibility(0);
            this.mWeatherInfoIcon.setVisibility(0);
        } else {
            this.mCurTempeView.setVisibility(8);
            this.mCurTempeUnitView.setVisibility(8);
            this.mWeatherInfoIcon.setVisibility(8);
        }
    }

    public void setWeatherSource(int sourceId) {
        if (this.mWeatherInfoIcon != null) {
            this.mWeatherInfoIcon.setImageResource(sourceId);
        }
    }

    private void onWeatherInfoChange(WeatherInfo info) {
        int resId;
        if (info == null) {
            HwLog.e("NewCoverDigitalClock", "coverscreen onWeatherChange : weatherInfo is null");
            info = new WeatherInfo();
        }
        DispalyWeatherInfo displayInfo = info.getDispalyWeatherInfo(System.currentTimeMillis(), 0);
        boolean isNight = false;
        if (displayInfo != null) {
            isNight = displayInfo.isNight(System.currentTimeMillis(), 0);
        }
        int status = info.getCurrentWeatherStatus();
        this.mCurrentTemperature = info.getCurrentTemperture();
        if (isNight) {
            resId = R$array.lockscreen_weather_icon_night;
        } else {
            resId = R$array.lockscreen_weather_icon;
        }
        HwCustCoverWeatherView hwCustCoverWeatherView = (HwCustCoverWeatherView) HwCustUtils.createObj(HwCustCoverWeatherView.class, new Object[]{getContext()});
        if (hwCustCoverWeatherView != null) {
            resId = hwCustCoverWeatherView.getWeatherResId(isNight, resId);
        }
        this.mWeatherIconId = WeatherMonitor.getWeatherIconId(this.mContext, resId, status);
        if (this.mWeaHandler != null) {
            this.mWeaHandler.sendEmptyMessage(0);
        }
    }

    public void setWeatherInfo(WeatherInfo weatherInfo) {
        onWeatherInfoChange(weatherInfo);
    }

    public void setDigitalTimeFontSize(Context context, int type) {
        if (this.hwCustCoverClockView != null) {
            this.hwCustCoverClockView.setDigitalTimeFontSize(context, this.mDigitalTime, type);
        }
    }
}

package com.android.deskclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.deskclock.alarmclock.MetaballPath;
import com.android.deskclock.smartcover.HwCustCoverAdapter;
import com.android.util.FormatTime;
import com.android.util.TypeFaces;
import com.android.util.Utils;
import com.huawei.cust.HwCustUtils;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import libcore.icu.LocaleData;

public class DigitalClock extends RelativeLayout {
    private boolean is24HourMode;
    private boolean isLonCover;
    private boolean isMtCover;
    private boolean mAttached;
    private Calendar mCalendar;
    private Context mContext;
    private HwCustCoverAdapter mCover;
    private TextView mDateTime;
    private TextView mFullAMPMTime;
    private TextView mFullTime;
    private final Handler mHandler;
    private final BroadcastReceiver mIntentReceiver;
    private TextView mLeftAmPm;
    private int mMode;
    private TextView mRightAmPm;
    private String mTimeZoneId;
    private int mfontType;
    private int mformatType;

    public DigitalClock(Context context) {
        this(context, null);
    }

    public DigitalClock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DigitalClock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mMode = 0;
        this.is24HourMode = false;
        this.mformatType = 0;
        this.mfontType = -1;
        this.mTimeZoneId = TimeZone.getDefault().getID();
        this.mCover = (HwCustCoverAdapter) HwCustUtils.createObj(HwCustCoverAdapter.class, new Object[0]);
        this.mHandler = new Handler();
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (!(intent == null || !"android.intent.action.TIMEZONE_CHANGED".equals(intent.getAction()) || 1 == DigitalClock.this.mMode)) {
                    String timeZoneId = intent.getStringExtra("time-zone");
                    if (timeZoneId != null) {
                        DigitalClock.this.mTimeZoneId = timeZoneId;
                    } else {
                        return;
                    }
                }
                if (!(DigitalClock.this.mMode == 0 || DigitalClock.this.mMode == 3)) {
                    if (DigitalClock.this.mMode == 4) {
                    }
                    if (DigitalClock.this.mMode == 1) {
                        DigitalClock.this.mCalendar = Calendar.getInstance(TimeZone.getTimeZone(DigitalClock.this.mTimeZoneId));
                    }
                    DigitalClock.this.mHandler.post(new Runnable() {
                        public void run() {
                            DigitalClock.this.updateTime();
                        }
                    });
                }
                DigitalClock.this.mCalendar = Calendar.getInstance();
                if (DigitalClock.this.mMode == 1) {
                    DigitalClock.this.mCalendar = Calendar.getInstance(TimeZone.getTimeZone(DigitalClock.this.mTimeZoneId));
                }
                DigitalClock.this.mHandler.post(/* anonymous class already generated */);
            }
        };
        this.mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.DigitalClock, defStyle, 0);
        this.mMode = a.getInt(0, 0);
        this.mformatType = a.getInt(2, 0);
        this.mfontType = a.getInt(1, -1);
        if (this.mCover != null && this.mCover.isLONPortCover()) {
            this.isLonCover = a.getBoolean(3, false);
        } else if (this.mCover != null && this.mCover.isNeedBoldText()) {
            this.isMtCover = a.getBoolean(4, false);
        }
        a.recycle();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mDateTime = (TextView) findViewById(R.id.digital_date_time);
        this.mLeftAmPm = (TextView) findViewById(R.id.digital_left_ampm);
        this.mRightAmPm = (TextView) findViewById(R.id.digital_right_ampm);
        this.mFullTime = (TextView) findViewById(R.id.digital_full_time);
        this.mFullAMPMTime = (TextView) findViewById(R.id.digital_full_ampm_time);
        setViewTypeface();
        this.mCalendar = Calendar.getInstance();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!this.mAttached) {
            this.mAttached = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.TIME_SET");
            switch (this.mMode) {
                case 0:
                case 1:
                case 3:
                case MetaballPath.POINT_NUM /*4*/:
                    filter.addAction("android.intent.action.TIME_TICK");
                    filter.addAction("android.intent.action.TIMEZONE_CHANGED");
                    break;
            }
            this.mContext.registerReceiver(this.mIntentReceiver, filter);
            updateTime();
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mHandler.removeCallbacksAndMessages(null);
        if (this.mAttached) {
            this.mAttached = false;
            this.mContext.unregisterReceiver(this.mIntentReceiver);
        }
    }

    public void setTimeZone(String timeZone) {
        if (this.mMode == 1) {
            Calendar calendar;
            if (timeZone != null) {
                calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
            } else {
                calendar = Calendar.getInstance();
            }
            if (this.mCalendar == null || !this.mCalendar.getTimeZone().getID().equals(calendar.getTimeZone().getID())) {
                if (timeZone == null) {
                    timeZone = this.mTimeZoneId;
                }
                this.mTimeZoneId = timeZone;
                this.mCalendar = calendar;
                updateTime();
            }
        }
    }

    private boolean isSameTime(Calendar newCld, Calendar oldCld) {
        int newHour = newCld.get(11);
        int newMinute = newCld.get(12);
        int oldHour = oldCld.get(11);
        int oldMinute = oldCld.get(12);
        if (newHour == oldHour && newMinute == oldMinute) {
            return true;
        }
        return false;
    }

    public void setTime(Calendar c) {
        if (this.mMode != 2) {
            return;
        }
        if (this.mCalendar == null || !isSameTime(c, this.mCalendar)) {
            this.mCalendar = c;
            updateTime();
        }
    }

    private void updateTime() {
        boolean z;
        FormatTime time = new FormatTime(this.mContext, this.mCalendar);
        updateFormatView(this.mDateTime, time, 9, false, true);
        updateFormatView(this.mFullTime, time, 7, false, true);
        if (this.mFullAMPMTime != null) {
            this.mFullAMPMTime.setText(FormatTime.getSpanTime(this.mContext, this.mCalendar));
        }
        TextView textView = this.mLeftAmPm;
        if (this.is24HourMode) {
            z = false;
        } else {
            z = true;
        }
        updateFormatView(textView, time, 2, false, z);
        textView = this.mRightAmPm;
        if (this.is24HourMode) {
            z = false;
        } else {
            z = true;
        }
        updateFormatView(textView, time, 3, false, z);
        if (this.mformatType == 1) {
            Utils.updateContentDescription(this, this.mCalendar);
            if (this.mFullTime != null) {
                Utils.updateContentDescription(this.mFullTime, this.mCalendar);
            }
        }
    }

    private void updateFormatView(TextView view, FormatTime time, int format, boolean upperCase, boolean visible) {
        if (view != null) {
            String text;
            if (1 == this.mformatType && R.id.digital_full_time == view.getId()) {
                text = FormatTime.getFormatTime(this.mContext, this.mCalendar);
            } else {
                text = time.getTimeString(format);
            }
            if (upperCase) {
                text = text.toUpperCase(Locale.ENGLISH);
            }
            boolean hasDST = TimeZone.getTimeZone(this.mTimeZoneId).inDaylightTime(this.mCalendar.getTime());
            if (1 == this.mformatType && R.id.digital_date_time == view.getId() && hasDST) {
                String dst = this.mContext.getResources().getString(R.string.world_digital_dst_tv);
                text = this.mContext.getResources().getString(R.string.date_DST, new Object[]{text, dst});
            }
            view.setText(text);
            isViewVisiable(view, visible, text);
        }
    }

    private void isViewVisiable(TextView view, boolean visible, String text) {
        int i = 0;
        if (this.mMode == 2) {
            if (visible && TextUtils.isEmpty(text) && (view.getId() == R.id.digital_left_ampm || view.getId() == R.id.digital_right_ampm)) {
                view.setVisibility(8);
                return;
            }
            if (!visible) {
                i = 4;
            }
            view.setVisibility(i);
        } else if (this.mMode == 4) {
            view.setText(getFullTime());
            if (!visible) {
                i = 4;
            }
            view.setVisibility(i);
        } else {
            if (!visible) {
                i = 4;
            }
            view.setVisibility(i);
        }
    }

    private String getFullTime() {
        DateFormat df;
        String sysTimeStr = "";
        Calendar calendar = Calendar.getInstance();
        TimeZone ctz = calendar.getTimeZone();
        Locale changeLocale = new Locale("en", "US");
        Locale currentLocale = getContext().getResources().getConfiguration().locale;
        if (currentLocale == null) {
            currentLocale = Locale.getDefault();
        }
        if ("zh".equals(currentLocale.getLanguage())) {
            df = android.text.format.DateFormat.getTimeFormat(getContext());
        } else {
            String str;
            LocaleData d = LocaleData.get(changeLocale);
            if (android.text.format.DateFormat.is24HourFormat(getContext())) {
                str = d.timeFormat_Hm;
            } else {
                str = d.timeFormat_hm;
            }
            df = new SimpleDateFormat(str, changeLocale);
        }
        df.setTimeZone(ctz);
        sysTimeStr = df.format(calendar.getTime());
        if (android.text.format.DateFormat.is24HourFormat(getContext())) {
            return sysTimeStr;
        }
        Matcher m = Pattern.compile("\\D*(\\d+.\\d+).*").matcher(sysTimeStr);
        if (m.find()) {
            sysTimeStr = m.group(1);
        }
        return sysTimeStr;
    }

    private void setViewTypeface() {
        Typeface tfLight = null;
        if (this.isLonCover) {
            tfLight = Typeface.create("sans-serif-condensed-light", 0);
        } else if (this.isMtCover) {
            tfLight = TypeFaces.get(this.mContext, "/system/fonts/AndroidClock.ttf");
        } else if (3 == this.mfontType) {
            tfLight = TypeFaces.get(this.mContext, "/system/fonts/Roboto-Thin.ttf");
        }
        if (tfLight != null && this.mFullTime != null) {
            TextPaint paint = this.mFullTime.getPaint();
            this.mFullTime.setTypeface(tfLight);
            if (paint != null && this.isMtCover) {
                paint.setFakeBoldText(true);
            }
        }
    }
}

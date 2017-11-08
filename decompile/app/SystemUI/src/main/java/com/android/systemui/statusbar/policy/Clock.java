package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.systemui.DemoMode;
import com.android.systemui.R$styleable;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.tuner.TunerService.Tunable;
import com.android.systemui.utils.UserSwitchUtils;
import fyusion.vislib.BuildConfig;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import libcore.icu.LocaleData;

public class Clock extends TextView implements DemoMode, Tunable {
    private final int mAmPmStyle;
    private boolean mAttached;
    private Calendar mCalendar;
    private SimpleDateFormat mClockFormat;
    private String mClockFormatString;
    private SimpleDateFormat mContentDescriptionFormat;
    private boolean mDemoMode;
    private final BroadcastReceiver mIntentReceiver;
    private Locale mLocale;
    private final BroadcastReceiver mScreenReceiver;
    private final Runnable mSecondTick;
    private Handler mSecondsHandler;
    private boolean mShowSeconds;

    public Clock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Clock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("android.intent.action.TIMEZONE_CHANGED")) {
                    Clock.this.mCalendar = Calendar.getInstance(TimeZone.getTimeZone(intent.getStringExtra("time-zone")));
                    if (Clock.this.mClockFormat != null) {
                        Clock.this.mClockFormat.setTimeZone(Clock.this.mCalendar.getTimeZone());
                    }
                } else if (action.equals("android.intent.action.CONFIGURATION_CHANGED")) {
                    Locale newLocale = Clock.this.getResources().getConfiguration().locale;
                    if (!newLocale.equals(Clock.this.mLocale)) {
                        Clock.this.mLocale = newLocale;
                        Clock.this.mClockFormatString = BuildConfig.FLAVOR;
                    }
                }
                Clock.this.updateClock();
            }
        };
        this.mScreenReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    if (Clock.this.mSecondsHandler != null) {
                        Clock.this.mSecondsHandler.removeCallbacks(Clock.this.mSecondTick);
                    }
                } else if ("android.intent.action.SCREEN_ON".equals(action) && Clock.this.mSecondsHandler != null) {
                    Clock.this.mSecondsHandler.postAtTime(Clock.this.mSecondTick, ((SystemClock.uptimeMillis() / 1000) * 1000) + 1000);
                }
            }
        };
        this.mSecondTick = new Runnable() {
            public void run() {
                if (Clock.this.mCalendar != null) {
                    Clock.this.updateClock();
                }
                Clock.this.mSecondsHandler.postAtTime(this, ((SystemClock.uptimeMillis() / 1000) * 1000) + 1000);
            }
        };
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R$styleable.Clock, 0, 0);
        try {
            this.mAmPmStyle = a.getInt(0, 2);
        } finally {
            a.recycle();
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!this.mAttached) {
            this.mAttached = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.TIME_TICK");
            filter.addAction("android.intent.action.TIME_SET");
            filter.addAction("android.intent.action.TIMEZONE_CHANGED");
            filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
            filter.addAction("android.intent.action.USER_SWITCHED");
            getContext().registerReceiverAsUser(this.mIntentReceiver, UserHandle.ALL, filter, null, getHandler());
            TunerService.get(getContext()).addTunable((Tunable) this, "clock_seconds", "icon_blacklist");
        }
        this.mCalendar = Calendar.getInstance(TimeZone.getDefault());
        updateClock();
        updateShowSeconds();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mAttached) {
            getContext().unregisterReceiver(this.mIntentReceiver);
            this.mAttached = false;
            TunerService.get(getContext()).removeTunable(this);
        }
    }

    final void updateClock() {
        if (!this.mDemoMode) {
            this.mCalendar.setTimeInMillis(System.currentTimeMillis());
            setText(getSmallTime());
            setContentDescription(this.mContentDescriptionFormat.format(this.mCalendar.getTime()));
        }
    }

    public void onTuningChanged(String key, String newValue) {
        int i = 0;
        if ("clock_seconds".equals(key)) {
            boolean z;
            if (!(newValue == null || Integer.parseInt(newValue) == 0)) {
                z = true;
            }
            this.mShowSeconds = z;
            updateShowSeconds();
        } else if ("icon_blacklist".equals(key)) {
            if (StatusBarIconController.getIconBlacklist(newValue).contains("clock")) {
                i = 8;
            }
            setVisibility(i);
        }
    }

    private void updateShowSeconds() {
        if (this.mShowSeconds) {
            if (this.mSecondsHandler == null && getDisplay() != null) {
                this.mSecondsHandler = new Handler();
                if (getDisplay().getState() == 2) {
                    this.mSecondsHandler.postAtTime(this.mSecondTick, ((SystemClock.uptimeMillis() / 1000) * 1000) + 1000);
                }
                IntentFilter filter = new IntentFilter("android.intent.action.SCREEN_OFF");
                filter.addAction("android.intent.action.SCREEN_ON");
                this.mContext.registerReceiver(this.mScreenReceiver, filter);
            }
        } else if (this.mSecondsHandler != null) {
            this.mContext.unregisterReceiver(this.mScreenReceiver);
            this.mSecondsHandler.removeCallbacks(this.mSecondTick);
            this.mSecondsHandler = null;
            updateClock();
        }
    }

    private final CharSequence getSmallTime() {
        SimpleDateFormat sdf;
        Context context = getContext();
        boolean is24 = DateFormat.is24HourFormat(context, UserSwitchUtils.getCurrentUser());
        LocaleData d = LocaleData.get(context.getResources().getConfiguration().locale);
        String format = this.mShowSeconds ? is24 ? d.timeFormat_Hms : d.timeFormat_hms : is24 ? d.timeFormat_Hm : d.timeFormat_hm;
        if (format.equals(this.mClockFormatString)) {
            sdf = this.mClockFormat;
        } else {
            this.mContentDescriptionFormat = new SimpleDateFormat(format);
            if (this.mAmPmStyle != 0) {
                int a = -1;
                boolean quoted = false;
                for (int i = 0; i < format.length(); i++) {
                    char c = format.charAt(i);
                    if (c == '\'') {
                        quoted = !quoted;
                    }
                    if (!quoted && c == 'a') {
                        a = i;
                        break;
                    }
                }
                if (a >= 0) {
                    int b = a;
                    while (a > 0 && Character.isWhitespace(format.charAt(a - 1))) {
                        a--;
                    }
                    format = format.substring(0, a) + '' + format.substring(a, b) + "a" + '' + format.substring(b + 1);
                }
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            this.mClockFormat = simpleDateFormat;
            this.mClockFormatString = format;
        }
        String result = sdf.format(this.mCalendar.getTime());
        if (this.mAmPmStyle != 0) {
            int magic1 = result.indexOf(61184);
            int magic2 = result.indexOf(61185);
            if (magic1 >= 0 && magic2 > magic1) {
                SpannableStringBuilder formatted = new SpannableStringBuilder(result);
                if (this.mAmPmStyle == 2) {
                    formatted.delete(magic1, magic2 + 1);
                } else {
                    if (this.mAmPmStyle == 1) {
                        formatted.setSpan(new RelativeSizeSpan(0.7f), magic1, magic2, 34);
                    }
                    formatted.delete(magic2, magic2 + 1);
                    formatted.delete(magic1, magic1 + 1);
                }
                return formatted;
            }
        }
        return result;
    }

    public void dispatchDemoCommand(String command, Bundle args) {
        if (!this.mDemoMode && command.equals("enter")) {
            this.mDemoMode = true;
        } else if (this.mDemoMode && command.equals("exit")) {
            this.mDemoMode = false;
            updateClock();
        } else if (this.mDemoMode && command.equals("clock")) {
            String millis = args.getString("millis");
            String hhmm = args.getString("hhmm");
            if (millis != null) {
                this.mCalendar.setTimeInMillis(Long.parseLong(millis));
            } else if (hhmm != null && hhmm.length() == 4) {
                int hh = Integer.parseInt(hhmm.substring(0, 2));
                int mm = Integer.parseInt(hhmm.substring(2));
                if (DateFormat.is24HourFormat(getContext(), UserSwitchUtils.getCurrentUser())) {
                    this.mCalendar.set(11, hh);
                } else {
                    this.mCalendar.set(10, hh);
                }
                this.mCalendar.set(12, mm);
            }
            setText(getSmallTime());
            setContentDescription(this.mContentDescriptionFormat.format(this.mCalendar.getTime()));
        }
    }
}

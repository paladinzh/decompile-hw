package com.android.keyguard;

import android.app.AlarmManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Slog;
import android.widget.GridLayout;
import android.widget.TextClock;
import android.widget.TextView;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.data.KeyguardInfo;
import com.huawei.keyguard.events.AppHandler;
import com.huawei.keyguard.inf.HwKeyguardPolicy;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.OsUtils;
import com.huawei.keyguard.view.widget.ClockView;
import fyusion.vislib.BuildConfig;
import java.util.Locale;

public class KeyguardStatusView extends GridLayout implements Callback {
    private final AlarmManager mAlarmManager;
    private ClockView mClockView;
    private TextClock mClockViewPre;
    private TextView mDateView;
    private KeyguardUpdateMonitorCallback mInfoCallback;
    private TextView mOwnerInfo;

    private static final class Patterns {
        static String cacheKey;
        static String clockView12;
        static String clockView24;
        static String dateView;

        private Patterns() {
        }

        static void update(Context context, boolean hasAlarm) {
            int i;
            Locale locale = Locale.getDefault();
            Resources res = context.getResources();
            if (hasAlarm) {
                i = R$string.abbrev_wday_month_day_no_year_alarm;
            } else {
                i = R$string.abbrev_wday_month_day_no_year;
            }
            String dateViewSkel = res.getString(i);
            String clockView12Skel = res.getString(R$string.clock_12hr_format);
            String clockView24Skel = res.getString(R$string.clock_24hr_format);
            String key = locale.toString() + dateViewSkel + clockView12Skel + clockView24Skel;
            if (!key.equals(cacheKey)) {
                dateView = DateFormat.getBestDateTimePattern(locale, dateViewSkel);
                clockView12 = DateFormat.getBestDateTimePattern(locale, clockView12Skel);
                if (!clockView12Skel.contains("a")) {
                    clockView12 = clockView12.replaceAll("a", BuildConfig.FLAVOR).trim();
                }
                clockView24 = DateFormat.getBestDateTimePattern(locale, clockView24Skel);
                clockView24 = clockView24.replace(':', '');
                clockView12 = clockView12.replace(':', '');
                cacheKey = key;
            }
        }
    }

    public KeyguardStatusView(Context context) {
        this(context, null, 0);
    }

    public KeyguardStatusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyguardStatusView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mInfoCallback = new KeyguardUpdateMonitorCallback() {
            public void onTimeChanged() {
                KeyguardStatusView.this.refresh();
            }

            public void onKeyguardVisibilityChanged(boolean showing) {
                if (showing) {
                    Slog.v("KeyguardStatusView", "refresh statusview showing:" + showing);
                    KeyguardStatusView.this.refresh();
                    KeyguardStatusView.this.updateOwnerInfo();
                }
            }

            public void onStartedWakingUp() {
                KeyguardStatusView.this.setEnableMarquee(true);
            }

            public void onFinishedGoingToSleep(int why) {
                KeyguardStatusView.this.setEnableMarquee(false);
            }
        };
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
    }

    private void setEnableMarquee(boolean enabled) {
        HwLog.v("KeyguardStatusView", (enabled ? "Enable" : "Disable") + " transport text marquee");
        if (this.mOwnerInfo != null) {
            this.mOwnerInfo.setSelected(enabled);
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mDateView = (TextView) findViewById(R$id.date_view);
        this.mClockView = (ClockView) findViewById(R$id.hw_clock_view);
        this.mClockViewPre = (TextClock) findViewById(R$id.clock_view);
        if (this.mClockViewPre != null) {
            this.mClockViewPre.setShowCurrentUserTime(true);
        }
        this.mOwnerInfo = (TextView) findViewById(R$id.owner_info);
        setEnableMarquee(KeyguardUpdateMonitor.getInstance(this.mContext).isDeviceInteractive());
        refresh();
        updateOwnerInfo();
        if (this.mClockView != null) {
            ((TextView) this.mClockView.findViewById(R$id.clock_text)).setElegantTextHeight(false);
        }
        if (this.mClockViewPre != null) {
            this.mClockViewPre.setElegantTextHeight(false);
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mClockView != null) {
            ((TextView) this.mClockView.findViewById(R$id.clock_text)).setTextSize(0, (float) getResources().getDimensionPixelSize(R$dimen.widget_big_font_size));
        }
        if (this.mClockViewPre != null) {
            this.mClockViewPre.setTextSize(0, (float) getResources().getDimensionPixelSize(R$dimen.widget_big_font_size));
        }
        this.mDateView.setTextSize(0, (float) getResources().getDimensionPixelSize(R$dimen.widget_label_font_size));
        if (this.mOwnerInfo != null) {
            this.mOwnerInfo.setTextSize(0, (float) getResources().getDimensionPixelSize(R$dimen.widget_label_font_size));
        }
    }

    public void refreshTime() {
        if (this.mClockView != null) {
            this.mClockView.updateTime();
        }
        if (this.mClockViewPre != null) {
            this.mClockViewPre.setFormat12Hour(Patterns.clockView12);
            this.mClockViewPre.setFormat24Hour(Patterns.clockView24);
        }
    }

    private void refreshStatusForUser() {
        if (HwKeyguardPolicy.isUseGgStatusView()) {
            setVisibility(0);
        } else {
            setVisibility(8);
        }
    }

    private void refresh() {
        Patterns.update(this.mContext, this.mAlarmManager.getNextAlarmClock(-2) != null);
        refreshTime();
    }

    public static String formatNextAlarm(Context context, AlarmClockInfo info) {
        if (info == null) {
            return BuildConfig.FLAVOR;
        }
        String skeleton;
        if (DateFormat.is24HourFormat(context, OsUtils.getCurrentUser())) {
            skeleton = "EHm";
        } else {
            skeleton = "Ehma";
        }
        return DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton), info.getTriggerTime()).toString();
    }

    private void updateOwnerInfo() {
        if (this.mOwnerInfo != null) {
            String ownerInfo = KeyguardInfo.getInst(this.mContext).getDeviceInfo();
            if (TextUtils.isEmpty(ownerInfo)) {
                this.mOwnerInfo.setVisibility(8);
            } else {
                this.mOwnerInfo.setVisibility(0);
                this.mOwnerInfo.setText(ownerInfo);
            }
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mInfoCallback);
        AppHandler.addListener(this);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        KeyguardUpdateMonitor.getInstance(this.mContext).removeCallback(this.mInfoCallback);
        AppHandler.removeListener(this);
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public boolean handleMessage(Message msg) {
        if (msg.what == 2 && HwKeyguardUpdateMonitor.getInstance().isShowing()) {
            refresh();
            updateOwnerInfo();
            refreshStatusForUser();
        }
        if (msg.what == 101) {
            updateOwnerInfo();
        }
        return false;
    }
}

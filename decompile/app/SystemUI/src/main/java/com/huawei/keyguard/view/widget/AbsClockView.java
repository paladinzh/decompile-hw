package com.huawei.keyguard.view.widget;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.R$id;
import com.huawei.keyguard.clock.ClockUtil;
import com.huawei.keyguard.events.weather.WeatherHelper;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.OsUtils;
import fyusion.vislib.BuildConfig;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AbsClockView extends RelativeLayout {
    protected Calendar mCalendar;
    protected TextView mDateView;
    protected Factory mFactory;
    protected final AtomicBoolean mFixedTimeZone;
    protected FrameLayout mTimeParent;
    protected TextView mTimeView;

    public interface Factory {
        void refreshDate();

        void setHwDateFormat();

        void updateHwTimeStyle();
    }

    private class HwClockView implements Factory {
        protected Context mContext;

        public HwClockView(Context context) {
            this.mContext = context;
            HwLog.e("HwClockView", "In HwClockView...");
        }

        public void setHwDateFormat() {
            AbsClockView.this.mTimeParent = (FrameLayout) AbsClockView.this.findViewById(R$id.clock_parent);
            float density = this.mContext.getResources().getDisplayMetrics().density;
            MarginLayoutParams lps = (MarginLayoutParams) AbsClockView.this.mTimeParent.getLayoutParams();
            if (!DateFormat.is24HourFormat(AbsClockView.this.getContext(), OsUtils.getCurrentUser())) {
                AbsClockView.this.mTimeParent.setLayoutParams(lps);
            }
        }

        public void updateHwTimeStyle() {
            TimeZone ctz = AbsClockView.this.mCalendar.getTimeZone();
            java.text.DateFormat df = new SimpleDateFormat(DateFormat.getTimeFormatString(AbsClockView.this.getContext(), KeyguardUpdateMonitor.getCurrentUser()));
            df.setTimeZone(ctz);
            String newTime = ClockUtil.formatChinaDateTime(AbsClockView.this.getContext(), getTime());
            if (!WeatherHelper.getInstance().isShowOneClock()) {
                newTime = ClockUtil.formatChinaDateTime(AbsClockView.this.getContext(), df.format(AbsClockView.this.mCalendar.getTime()));
            }
            AbsClockView.this.mTimeView.setText(ClockUtil.setTimeStr(newTime));
        }

        private String getTime() {
            Calendar calendar = Calendar.getInstance();
            TimeZone ctz = calendar.getTimeZone();
            java.text.DateFormat df = new SimpleDateFormat(DateFormat.getTimeFormatString(this.mContext, OsUtils.getCurrentUser()));
            df.setTimeZone(ctz);
            String sysTimeStr = df.format(calendar.getTime());
            if (DateFormat.is24HourFormat(this.mContext, OsUtils.getCurrentUser())) {
                return sysTimeStr;
            }
            Matcher m = Pattern.compile("\\D*(\\d+.\\d+).*").matcher(sysTimeStr);
            if (m.find()) {
                sysTimeStr = m.group(1);
            }
            return sysTimeStr;
        }

        public void refreshDate() {
            if (AbsClockView.this.mDateView != null && AbsClockView.this.mCalendar != null) {
                AbsClockView.this.mDateView.setText(getDateString(AbsClockView.this.mCalendar.getTimeZone()));
            }
        }

        private CharSequence getDateString(TimeZone timeZone) {
            String date;
            int offset = 0;
            long millis = System.currentTimeMillis();
            if (timeZone != null) {
                offset = timeZone.getOffset(millis) - TimeZone.getDefault().getOffset(millis);
            }
            long newTime = millis + ((long) offset);
            if (this.mContext.getResources().getConfiguration().locale.getLanguage().equals(Locale.CHINA.getLanguage())) {
                String day = DateUtils.formatDateTime(AbsClockView.this.getContext(), newTime, 65560);
                date = day + DateUtils.formatDateTime(AbsClockView.this.getContext(), newTime, 2);
            } else {
                int flags = 98330;
                if (ClockUtil.isShowFullMonth()) {
                    flags = 32794;
                }
                if (ClockUtil.isShowFrenchCustDate(AbsClockView.this.getContext())) {
                    flags = 22;
                }
                date = DateUtils.formatDateTime(AbsClockView.this.getContext(), newTime, flags);
            }
            return date.replace(".", BuildConfig.FLAVOR);
        }
    }

    public AbsClockView(Context context) {
        this(context, null);
    }

    public AbsClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mFixedTimeZone = new AtomicBoolean();
    }

    public void setFactory(Context context) {
        HwLog.e("AbsClockView", "In setFactory...");
        if (this.mFactory == null) {
            this.mFactory = new HwClockView(context);
        }
    }

    public void setFixedTimeZone(boolean isFixTimeZone) {
        this.mFixedTimeZone.set(isFixTimeZone);
    }

    public boolean getFixedTimeZone() {
        return this.mFixedTimeZone.get();
    }
}

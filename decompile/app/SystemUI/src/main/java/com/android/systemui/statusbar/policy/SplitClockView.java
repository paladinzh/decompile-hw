package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextClock;
import com.android.systemui.R;
import com.android.systemui.utils.UserSwitchUtils;
import fyusion.vislib.BuildConfig;

public class SplitClockView extends LinearLayout {
    private TextClock mAmPmView;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.TIME_SET".equals(action) || "android.intent.action.TIMEZONE_CHANGED".equals(action) || "android.intent.action.LOCALE_CHANGED".equals(action) || "android.intent.action.CONFIGURATION_CHANGED".equals(action) || "android.intent.action.USER_SWITCHED".equals(action)) {
                SplitClockView.this.updatePatterns();
            }
        }
    };
    private TextClock mTimeView;

    public SplitClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mTimeView = (TextClock) findViewById(R.id.time_view);
        this.mAmPmView = (TextClock) findViewById(R.id.am_pm_view);
        this.mTimeView.setShowCurrentUserTime(true);
        this.mAmPmView.setShowCurrentUserTime(true);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.TIME_SET");
        filter.addAction("android.intent.action.TIMEZONE_CHANGED");
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        filter.addAction("android.intent.action.USER_SWITCHED");
        getContext().registerReceiverAsUser(this.mIntentReceiver, UserHandle.ALL, filter, null, null);
        updatePatterns();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getContext().unregisterReceiver(this.mIntentReceiver);
    }

    private void updatePatterns() {
        String timeString;
        String amPmString;
        String formatString = DateFormat.getTimeFormatString(getContext(), UserSwitchUtils.getCurrentUser());
        int index = getAmPmPartEndIndex(formatString);
        if (index == -1) {
            timeString = formatString;
            amPmString = BuildConfig.FLAVOR;
        } else {
            timeString = formatString.substring(0, index);
            amPmString = formatString.substring(index);
        }
        this.mTimeView.setFormat12Hour(timeString);
        this.mTimeView.setFormat24Hour(timeString);
        this.mTimeView.setContentDescriptionFormat12Hour(formatString);
        this.mTimeView.setContentDescriptionFormat24Hour(formatString);
        this.mAmPmView.setFormat12Hour(amPmString);
        this.mAmPmView.setFormat24Hour(amPmString);
    }

    private static int getAmPmPartEndIndex(String formatString) {
        int i = -1;
        boolean hasAmPm = false;
        int length = formatString.length();
        int i2 = length - 1;
        while (i2 >= 0) {
            char c = formatString.charAt(i2);
            boolean isAmPm = c == 'a';
            boolean isWhitespace = Character.isWhitespace(c);
            if (isAmPm) {
                hasAmPm = true;
            }
            if (isAmPm || isWhitespace) {
                i2--;
            } else if (i2 == length - 1) {
                return -1;
            } else {
                if (hasAmPm) {
                    i = i2 + 1;
                }
                return i;
            }
        }
        if (hasAmPm) {
            i = 0;
        }
        return i;
    }
}

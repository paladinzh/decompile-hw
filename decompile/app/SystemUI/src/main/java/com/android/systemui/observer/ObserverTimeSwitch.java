package com.android.systemui.observer;

import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.System;
import android.text.format.DateFormat;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.UserSwitchUtils;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ObserverTimeSwitch extends ObserverItem<Boolean> {
    private boolean mIs24HourFormat = false;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat();

    public ObserverTimeSwitch(Handler handler) {
        super(handler);
    }

    public Uri getUri() {
        return System.getUriFor("time_12_24");
    }

    private String getTimeSplit(Calendar cal) {
        Calendar calendar = cal;
        if (cal == null) {
            try {
                calendar = Calendar.getInstance();
            } catch (Exception ex) {
                HwLog.e(this.TAG, "Got execption when getTimeSplit:" + ex.toString());
                return ":";
            }
        }
        Matcher m1 = Pattern.compile("(\\D*)(\\d+)(.)(\\d+)(.*)").matcher(DateFormat.getTimeFormat(this.mContext).format(calendar.getTime()));
        return m1.find() ? m1.group(3) : ":";
    }

    public void onChange() {
        this.mIs24HourFormat = DateFormat.is24HourFormat(this.mContext, UserSwitchUtils.getCurrentUser());
        String timeSplit = getTimeSplit(Calendar.getInstance());
        this.simpleDateFormat = new SimpleDateFormat(this.mIs24HourFormat ? "HH" + timeSplit + "mm" : "h" + timeSplit + "mm");
    }

    public Boolean getValue() {
        return Boolean.valueOf(this.mIs24HourFormat);
    }

    public Object getValue(int valueType) {
        if (1 == valueType) {
            return this.simpleDateFormat;
        }
        return super.getValue(valueType);
    }
}

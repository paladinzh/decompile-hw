package com.huawei.keyguard.events;

import android.content.Context;
import java.util.TimeZone;

public class TimeZoneFinder {

    public interface TimeZoneListener {
        void onTimeZoneChange(TimeZoneFinder timeZoneFinder, TimeZone timeZone);
    }

    public TimeZone getTimeZone(Context context) {
        return null;
    }
}

package com.huawei.keyguard.events.weather;

import android.text.TextUtils;
import java.util.TimeZone;

public class CityInfo {
    public String mCityAlias;
    public String mCityName;
    public String mCityNativeName;
    public String mTimeZone;
    public long mWeatherId = 0;

    public String getTimeZoneName() {
        return TextUtils.isEmpty(this.mTimeZone) ? getDefaultTimeZone() : this.mTimeZone;
    }

    public CityInfo copy() {
        CityInfo info = new CityInfo();
        info.mCityAlias = this.mCityAlias;
        info.mCityNativeName = this.mCityNativeName;
        info.mCityName = this.mCityName;
        info.mTimeZone = this.mTimeZone;
        info.mWeatherId = this.mWeatherId;
        return info;
    }

    public TimeZone getTimeZone() {
        return TimeZone.getTimeZone(getTimeZoneName());
    }

    public String getDisplayName() {
        if (!TextUtils.isEmpty(this.mCityAlias)) {
            return this.mCityAlias;
        }
        if (TextUtils.isEmpty(this.mCityNativeName)) {
            return this.mCityName;
        }
        return this.mCityNativeName;
    }

    private String getDefaultTimeZone() {
        int offset = TimeZone.getDefault().getOffset(System.currentTimeMillis());
        StringBuilder result = new StringBuilder("GMT");
        if (offset < 0) {
            result.append('-');
            offset = -offset;
        } else {
            result.append('+');
        }
        int hour = offset / 3600000;
        int minute = (offset / 60000) % 60;
        if (10 > hour) {
            result.append('0');
        }
        result.append(hour).append(':');
        if (10 > minute) {
            result.append('0');
        }
        result.append(minute);
        return result.toString();
    }
}

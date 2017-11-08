package com.android.settingslib.datetime;

import android.icu.text.TimeZoneNames;
import android.icu.text.TimeZoneNames.NameType;
import android.text.BidiFormatter;
import android.text.TextDirectionHeuristics;
import android.text.TextUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ZoneGetter {
    private ZoneGetter() {
    }

    public static String getTimeZoneOffsetAndName(TimeZone tz, Date now) {
        Locale locale = Locale.getDefault();
        String gmtString = getGmtOffsetString(locale, tz, now);
        String zoneNameString = getZoneLongName(TimeZoneNames.getInstance(locale), tz, now);
        if (zoneNameString == null) {
            return gmtString;
        }
        return gmtString + " " + zoneNameString;
    }

    private static String getZoneLongName(TimeZoneNames names, TimeZone tz, Date now) {
        NameType nameType;
        if (tz.inDaylightTime(now)) {
            nameType = NameType.LONG_DAYLIGHT;
        } else {
            nameType = NameType.LONG_STANDARD;
        }
        return names.getDisplayName(tz.getID(), nameType, now.getTime());
    }

    private static String getGmtOffsetString(Locale locale, TimeZone tz, Date now) {
        boolean isRtl = true;
        SimpleDateFormat gmtFormatter = new SimpleDateFormat("ZZZZ");
        gmtFormatter.setTimeZone(tz);
        String gmtString = gmtFormatter.format(now);
        BidiFormatter bidiFormatter = BidiFormatter.getInstance();
        if (TextUtils.getLayoutDirectionFromLocale(locale) != 1) {
            isRtl = false;
        }
        return bidiFormatter.unicodeWrap(gmtString, isRtl ? TextDirectionHeuristics.RTL : TextDirectionHeuristics.LTR);
    }
}

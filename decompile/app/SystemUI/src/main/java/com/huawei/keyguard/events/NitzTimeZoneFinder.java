package com.huawei.keyguard.events;

import android.content.Context;
import android.os.SystemProperties;
import com.huawei.keyguard.util.OsUtils;
import java.util.TimeZone;

public class NitzTimeZoneFinder extends TimeZoneFinder {
    public TimeZone getTimeZone(Context context) {
        String nitzStr = OsUtils.getSystemString(context, "nitz_timezone_info");
        if (nitzStr == null) {
            return null;
        }
        String[] nitzInfos = new String[3];
        int index = nitzStr.indexOf("||");
        if (index > 0) {
            nitzInfos[0] = nitzStr.substring(0, index);
        }
        int start = "||".length() + index;
        int nextIndex = nitzStr.indexOf("||", start);
        if (nextIndex > 0) {
            nitzInfos[1] = nitzStr.substring(start, nextIndex);
        }
        String numeric = SystemProperties.get("gsm.operator.numeric", null);
        if (numeric == null || numeric.length() <= 3 || nitzInfos[0] == null || nitzInfos[1] == null || !nitzInfos[1].equals(numeric.subSequence(0, 3))) {
            return null;
        }
        return TimeZone.getTimeZone(nitzInfos[0]);
    }
}

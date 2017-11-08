package com.huawei.mms.util;

import android.content.Intent;
import com.android.mms.MmsConfig;
import com.android.mms.ui.MessageUtils;

public class MmsCommon {
    public static final boolean PLATFORM_MTK = MessageUtils.isMTKPlatform();
    public static final boolean RUNNING_IN_CSP = MmsConfig.isCspVersion();
    private static long sRequestTimeMillis = 0;

    public static boolean isFromFloatMms(Intent intent) {
        return intent.getBooleanExtra("fromFloatMms", false);
    }

    public static boolean isFromNotification(Intent intent) {
        return intent.getBooleanExtra("fromNotification", false);
    }

    public static boolean isFromPeekReply(Intent intent) {
        return intent.getBooleanExtra("EXTRA_VALUE_PEEK_REPLY", false);
    }

    public static long getRequestTimeMillis() {
        return sRequestTimeMillis;
    }

    public static void setRequestTimeMillis(long requestTimeMillis) {
        sRequestTimeMillis = requestTimeMillis;
    }
}

package com.android.contacts.util;

import android.content.Context;
import android.content.Intent;

public final class CallerInfoCacheUtils {
    private CallerInfoCacheUtils() {
    }

    public static void sendUpdateCallerInfoCacheIntent(Context context) {
        context.sendBroadcast(new Intent("com.android.phone.UPDATE_CALLER_INFO_CACHE"));
    }
}

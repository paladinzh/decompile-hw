package com.android.common.userhappiness;

import android.content.Context;
import android.content.Intent;

public class UserHappinessSignals {
    private static boolean mHasVoiceLoggingInfo = false;

    public static void setHasVoiceLoggingInfo(boolean hasVoiceLogging) {
        mHasVoiceLoggingInfo = hasVoiceLogging;
    }

    public static void userAcceptedImeText(Context context) {
        if (mHasVoiceLoggingInfo) {
            Intent i = new Intent("com.android.common.speech.LOG_EVENT");
            i.putExtra("app_name", "voiceime");
            i.putExtra("extra_event", 21);
            i.putExtra("", context.getPackageName());
            i.putExtra("timestamp", System.currentTimeMillis());
            context.sendBroadcast(i);
            setHasVoiceLoggingInfo(false);
        }
    }
}

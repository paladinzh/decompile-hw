package com.huawei.mms.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.telephony.TelephonyManager;
import com.huawei.cspcommon.MLog;

public class AudioManagerUtils {
    private static AudioManager sAudioManager;
    private static OnAudioFocusChangeListener sOnAudioFocusChangeListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            MLog.i("AudioManagerUtils", "focusChange :" + focusChange);
        }
    };

    public static int requestAudioManagerFocus(Context context, int duringHint) {
        if (sAudioManager == null) {
            sAudioManager = (AudioManager) context.getSystemService("audio");
        }
        return sAudioManager.requestAudioFocus(sOnAudioFocusChangeListener, 3, duringHint);
    }

    public static int abandonAudioFocus(Context context) {
        if (sAudioManager == null) {
            sAudioManager = (AudioManager) context.getSystemService("audio");
        }
        return sAudioManager.abandonAudioFocus(sOnAudioFocusChangeListener);
    }

    public static boolean isTelephonyCalling(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        MLog.d("AudioManagerUtils", "callingState " + telephonyManager.getCallState());
        if (2 == telephonyManager.getCallState() || 1 == telephonyManager.getCallState()) {
            return true;
        }
        return false;
    }
}

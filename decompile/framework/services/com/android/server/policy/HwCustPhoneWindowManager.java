package com.android.server.policy;

import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.view.KeyEvent;
import com.android.server.policy.keyguard.KeyguardServiceDelegate;

public class HwCustPhoneWindowManager {
    static final String TAG = "HwCustPhoneWindowManager";

    public void dueKEYCODE_SOUNDTRIGGER_EVENT(int keyCode, Context context, boolean isMusicOrFMActive, boolean down, boolean keyguardShow) {
    }

    public int selectAnimationLw(int transit) {
        return 0;
    }

    public boolean isChargingAlbumSupported() {
        return false;
    }

    public void processCustInterceptKey(int keyCode, boolean down, Context context) {
    }

    public void volumnkeyWakeup(Context mContext, boolean isScreenOn, PowerManager mPowerManager) {
    }

    public boolean isVolumnkeyWakeup() {
        return false;
    }

    public boolean interceptVolumeUpKey(KeyEvent event, Context context, boolean isScreenOn, boolean keyguardActive, boolean isMusicOrFMOrVoiceCallActive, boolean isInjected, boolean down) {
        return false;
    }

    public boolean disableHomeKey(Context context) {
        return false;
    }

    public boolean isAuthenticationGrace(Context context) {
        return false;
    }

    public void skipAuthKeyguard(KeyguardServiceDelegate keyguardDelegate, Handler handler) {
    }
}

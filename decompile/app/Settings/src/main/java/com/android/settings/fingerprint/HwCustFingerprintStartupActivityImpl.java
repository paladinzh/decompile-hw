package com.android.settings.fingerprint;

import android.content.Context;
import android.content.Intent;
import com.android.settings.Utils;
import com.huawei.android.provider.SettingsEx.Systemex;

public class HwCustFingerprintStartupActivityImpl extends HwCustFingerprintStartupActivity {
    private static final String ACTION_HWSTARTUPGUIDE_VOICE_WAKEUP = "com.huawei.hwstartupguide.ACTION_HWSTARTUPGUIDE_VOICE_WAKEUP";
    private static final String ACTION_USINGACTIVITY_START = "com.huawei.hwstartupguide.ACTION_USINGACTIVITY_START";
    private static final String PROP_HWSTARTUPGUIDE_VOICE_WAKEUP = "hw_show_voice_wakeup_page";
    private Context mContext;

    public HwCustFingerprintStartupActivityImpl(FingerprintStartupActivity fingerprintStartupActivity) {
        super(fingerprintStartupActivity);
        this.mContext = fingerprintStartupActivity;
    }

    public void startActivityForVoliceWakeUp() {
        Intent mVoliceWakeUp = new Intent();
        mVoliceWakeUp.setClassName("com.huawei.hwstartupguide", "com.huawei.hwstartupguide.VoiceWakeupActivity");
        if (Utils.hasIntentActivity(this.mContext.getPackageManager(), mVoliceWakeUp)) {
            this.mContext.startActivity(new Intent(ACTION_HWSTARTUPGUIDE_VOICE_WAKEUP));
        } else {
            this.mContext.startActivity(new Intent(ACTION_USINGACTIVITY_START));
        }
    }

    public boolean isSupportVoliceWakeUp() {
        if (Systemex.getInt(this.mContext.getContentResolver(), PROP_HWSTARTUPGUIDE_VOICE_WAKEUP, 0) == 1) {
            return true;
        }
        return false;
    }
}

package com.android.settings.deviceinfo;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.SettingsPreferenceFragment;
import java.util.Locale;

public class CertificationListSettings extends SettingsPreferenceFragment {
    private static final String TAG = CertificationListSettings.class.getCanonicalName();

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(2131230752);
        String[] safetyCertificationArray = SystemProperties.get("ro.config.safety_certification").toLowerCase(Locale.US).split(",");
        boolean hasCe = false;
        boolean hasIc = false;
        boolean hasFcc = false;
        boolean hasHac = false;
        for (String safetyCertification : safetyCertificationArray) {
            String safetyCertification2;
            if (safetyCertification2 != null) {
                safetyCertification2 = safetyCertification2.trim();
            }
            if ("ce".equalsIgnoreCase(safetyCertification2)) {
                hasCe = true;
            }
            if ("ic".equalsIgnoreCase(safetyCertification2)) {
                hasIc = true;
            }
            if ("fcc".equalsIgnoreCase(safetyCertification2)) {
                hasFcc = true;
            }
            if ("hac".equalsIgnoreCase(safetyCertification2)) {
                hasHac = true;
            }
        }
        Log.d(TAG, ".onCreate:" + hasCe + "|hasIc:" + hasIc + "|hasFcc:" + hasFcc + "|hasHac:" + hasHac);
        if (!hasCe) {
            removePreference("ce");
        }
        if (!hasIc) {
            removePreference("ic");
        }
        if (!hasFcc) {
            removePreference("fcc");
        }
        if (!hasHac) {
            removePreference("hac");
        }
    }

    protected int getMetricsCategory() {
        return 100000;
    }

    public static boolean shouldDisplay(Context context) {
        if (context == null) {
            return false;
        }
        return !TextUtils.isEmpty(SystemProperties.get("ro.config.safety_certification"));
    }
}

package com.android.contacts.hap.encryptcall;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.util.Log;
import com.android.contacts.hap.sim.SimFactoryManager;

public class EncryptCallUtils {
    private static final boolean ENCRYPT_PROP = SystemProperties.getBoolean("ro.config.encrypt_version", false);
    public static final String TAG = EncryptCallUtils.class.getSimpleName();
    private static Boolean isEncryptCall = null;
    private static Context mContext;

    public static void init(Context context) {
        mContext = context;
    }

    public static boolean isEncryptCallEnable() {
        return isEncryptCallEnable(mContext);
    }

    public static boolean isEncryptCallEnable(Context context) {
        boolean z = true;
        if (!ENCRYPT_PROP || context == null || context.getContentResolver() == null) {
            return false;
        }
        if (isEncryptCall == null) {
            int encryptCallStatus = Secure.getInt(context.getContentResolver(), "encrypt_version", 0);
            Log.d(TAG, "encrypt call status is " + encryptCallStatus);
            if (1 != encryptCallStatus) {
                z = false;
            }
            isEncryptCall = Boolean.valueOf(z);
        }
        return isEncryptCall.booleanValue();
    }

    public static boolean isCallCard1Encrypt() {
        return isEncryptCallCard(0);
    }

    public static boolean isCallCard2Encrypt() {
        return isEncryptCallCard(1);
    }

    public static boolean isEncryptCallCard(int slot) {
        if (!isEncryptCallEnable()) {
            return false;
        }
        boolean result = SimFactoryManager.isCdma(SimFactoryManager.getSubscriptionIdBasedOnSlot(slot));
        Log.d(TAG, "slot" + slot + "_" + "isisEncryptCallCard =" + result);
        return result;
    }

    public static void buildEncryptIntent(Intent intent) {
        intent.putExtra("com.android.phone.ENCRYPT_CALL_EXTRA_EMERGENCY_CALL", true);
        intent.putExtra("com.android.phone.CALL_ENCRYPTED", 1);
    }
}

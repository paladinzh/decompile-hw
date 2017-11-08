package com.android.contacts.hap.util;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.util.Log;
import com.android.contacts.hap.sim.SimFactoryManager;

public class HapEncryptCallUtils {
    private static final boolean ENCRYPTCALL_PROP = SystemProperties.getBoolean("ro.config.support_encrypt", false);
    public static final String TAG = HapEncryptCallUtils.class.getSimpleName();
    private static Boolean isEncryptCall = null;
    private static Context mContext;

    public static void init(Context context) {
        mContext = context;
    }

    public static boolean isEncryptCallEnabled() {
        return isEncryptCallEnabled(mContext);
    }

    public static boolean isEncryptCallEnabled(Context context) {
        boolean z = true;
        if (!ENCRYPTCALL_PROP || context == null || context.getContentResolver() == null) {
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
        if (!isEncryptCallEnabled()) {
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

    public static boolean isCdmaBySlot(int slot) {
        if (!isEncryptCallEnabled()) {
            return false;
        }
        boolean simStatus = false;
        if (slot == 0) {
            if (SimFactoryManager.checkSIM1CardPresentState()) {
                simStatus = SimFactoryManager.isSimEnabled(0);
            } else {
                simStatus = false;
            }
        } else if (slot == 1) {
            if (SimFactoryManager.checkSIM2CardPresentState()) {
                simStatus = SimFactoryManager.isSimEnabled(1);
            } else {
                simStatus = false;
            }
        }
        if (simStatus) {
            return SimFactoryManager.isCdma(SimFactoryManager.getSubscriptionIdBasedOnSlot(slot));
        }
        return false;
    }
}

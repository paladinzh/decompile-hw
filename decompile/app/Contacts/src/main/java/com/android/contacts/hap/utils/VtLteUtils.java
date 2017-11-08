package com.android.contacts.hap.utils;

import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.telephony.HwTelephonyManager;
import com.android.contacts.CallUtil;
import com.android.contacts.calllog.IntentProvider;
import com.android.contacts.detail.HwCustContactDetailUtil;
import com.android.contacts.hap.util.MultiUsersUtils;

public class VtLteUtils {
    private static final boolean FEATURE_VOLTE_DYN = SystemProperties.getBoolean("ro.config.hw_volte_dyn", true);
    private static final boolean mIsVtLteSupported = SystemProperties.getBoolean("ro.config.hw_vtlte_on", false);
    private static boolean sVolteUserSwitch = false;

    public static boolean isVtLteSupport() {
        return mIsVtLteSupported;
    }

    public static void setImsSwitchOn(boolean switchOn) {
        sVolteUserSwitch = switchOn;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isVtLteOn(Context context) {
        boolean volteVisiable = false;
        if (!mIsVtLteSupported || !sVolteUserSwitch || !MultiUsersUtils.isCurrentUserOwner()) {
            return false;
        }
        if (!FEATURE_VOLTE_DYN) {
            return true;
        }
        if (context == null) {
            return false;
        }
        CarrierConfigManager cfgMgr = (CarrierConfigManager) context.getSystemService("carrier_config");
        if (cfgMgr == null) {
            return false;
        }
        PersistableBundle b = cfgMgr.getConfigForSubId(HwTelephonyManager.getDefault().getDefault4GSlotId());
        if (b != null) {
            volteVisiable = b.getBoolean("carrier_volte_available_bool");
        }
        return volteVisiable;
    }

    public static void startVideoCall(String number, Context context) {
        startVideoCall(-1, number, context);
    }

    public static void startVideoCall(long id, String number, Context context) {
        Intent intent = getVideoCallIntentProvider(id, number).getIntent(context);
        if (context != null) {
            context.startActivity(intent);
        }
    }

    public static IntentProvider getVideoCallIntentProvider(String number) {
        return getVideoCallIntentProvider(-1, number);
    }

    public static IntentProvider getVideoCallIntentProvider(final long id, final String number) {
        return new IntentProvider() {
            public Intent getIntent(Context context) {
                Intent intent = CallUtil.getCallIntent(number, null);
                if (id != -1) {
                    intent.putExtra(HwCustContactDetailUtil.CONTACT_ID_EXTRA, String.valueOf(id));
                }
                intent.putExtra("android.telecom.extra.START_CALL_WITH_VIDEO_STATE", 3);
                intent.addFlags(268435456);
                return intent;
            }
        };
    }

    public static boolean isLteServiceAbility() {
        return 1 == HwTelephonyManager.getDefault().getLteServiceAbility();
    }
}

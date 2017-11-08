package com.huawei.keyguard.support;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.os.UserManager;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.OsUtils;

public class PrivacyMode {
    public static boolean isPrivacyModeOn(Context context) {
        boolean z = true;
        if (context == null) {
            return false;
        }
        if (OsUtils.getSecureInt(context, "privacy_mode_on", 0) != 1) {
            z = false;
        }
        return z;
    }

    public static void setPrivacyState(Context context, int state) {
        if (context == null) {
            HwLog.w("KG_PrivacyMode", "input context is null");
        } else if (state == 0 || state == 1) {
            int oldState = OsUtils.getSecureInt(context, "privacy_mode_state", -1);
            HwLog.i("KG_PrivacyMode", "old privacy state:" + oldState);
            if (oldState != state) {
                HwLog.i("KG_PrivacyMode", "Set privacy state:" + state);
                OsUtils.getSecureInt(context, "privacy_mode_state", state);
                Intent intent = new Intent("android.intent.actions.PRIVACY_MODE_CHANGED");
                intent.putExtra("privacy_mode_value", state);
                context.sendBroadcastAsUser(intent, UserHandle.OWNER, "android.permission.RECV_PRIVACY_MODE_CHANGED");
            }
        } else {
            HwLog.w("KG_PrivacyMode", "invalid privacy state");
        }
    }

    public static int getUnlockDelay(Context context, int state) {
        if (state != 0 && state != 1) {
            return 0;
        }
        int delay = 0;
        if (state == 1 && state != OsUtils.getSecureInt(context, "privacy_mode_state", -1)) {
            delay = 400;
        }
        return delay;
    }

    public static int getPrivateUserId(Context context) {
        try {
            int privateUserId = ((UserManager) context.getSystemService("user")) == null ? -10000 : 10;
            HwLog.d("KG_PrivacyMode", "privateUserId is " + privateUserId);
            return privateUserId;
        } catch (RuntimeException e) {
            HwLog.d("KG_PrivacyMode", "getPrivateUserId fail. ");
            HwLog.d("KG_PrivacyMode", "Return user 10 as privacy user");
            return 10;
        } catch (NoSuchMethodError e2) {
            HwLog.d("KG_PrivacyMode", "getPrivateUserId not suppport. ");
            HwLog.d("KG_PrivacyMode", "Return user 10 as privacy user");
            return 10;
        }
    }

    public static int sendPrivacyBroadcast(Context context, boolean isGuest) {
        HwLog.d("KG_PrivacyMode", "sendPrivacyBroadcast for " + (isGuest ? "guest" : "owner"));
        int guestType = isGuest ? 1 : 0;
        setPrivacyState(context, guestType);
        return getUnlockDelay(context, guestType);
    }
}

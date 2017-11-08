package com.android.contacts.compatibility;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.telecom.TelecomManager;

public class TelecomManagerCompat {
    public static void placeCall(@Nullable Activity activity, @Nullable TelecomManager telecomManager, @Nullable Intent intent) {
        if (activity != null && telecomManager != null && intent != null) {
            if (CompatUtils.isMarshmallowCompatible()) {
                telecomManager.placeCall(intent.getData(), intent.getExtras());
            } else {
                activity.startActivityForResult(intent, 0);
            }
        }
    }
}

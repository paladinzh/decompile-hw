package com.android.systemui.utils;

import android.content.Intent;

public class SecurityCodeCheck {
    public static boolean isValidIntentAndAction(Intent intent) {
        return (intent == null || intent.getAction() == null) ? false : true;
    }
}

package com.android.contacts.compatibility;

import android.support.v4.os.BuildCompat;

public final class CompatUtils {
    private static final String TAG = CompatUtils.class.getSimpleName();

    public static boolean isCallSubjectCompatible() {
        return SdkVersionOverride.getSdkVersion(21) >= 23;
    }

    public static boolean isMarshmallowCompatible() {
        return SdkVersionOverride.getSdkVersion(21) >= 23;
    }

    public static boolean isNCompatible() {
        return BuildCompat.isAtLeastN();
    }

    public static boolean isLollipopCompatible() {
        return SdkVersionOverride.getSdkVersion(21) >= 21;
    }
}

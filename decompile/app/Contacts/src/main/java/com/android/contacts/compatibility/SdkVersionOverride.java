package com.android.contacts.compatibility;

import android.os.Build.VERSION;

public class SdkVersionOverride {
    private SdkVersionOverride() {
    }

    public static int getSdkVersion(int overrideVersion) {
        return VERSION.SDK_INT;
    }
}

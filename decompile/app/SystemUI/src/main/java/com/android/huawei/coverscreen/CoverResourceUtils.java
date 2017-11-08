package com.android.huawei.coverscreen;

import android.content.Context;
import android.os.SystemProperties;
import fyusion.vislib.BuildConfig;

public class CoverResourceUtils {
    private static final String RESOURCE_SUFFIX = SystemProperties.get("ro.config.small_cover_size", BuildConfig.FLAVOR);
    private static final String TAG = CoverResourceUtils.class.getSimpleName();

    private CoverResourceUtils() {
        throw new RuntimeException(TAG + ": Can not Create Instance of this class");
    }

    public static final int getResIdentifier(Context context, String name, String defType, String defPackage, int defResIdentifier) {
        int identifier = context.getResources().getIdentifier(name + RESOURCE_SUFFIX, defType, defPackage);
        if (identifier > 0) {
            return identifier;
        }
        return defResIdentifier;
    }
}

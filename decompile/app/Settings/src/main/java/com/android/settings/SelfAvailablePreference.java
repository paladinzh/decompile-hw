package com.android.settings;

import android.content.Context;

public interface SelfAvailablePreference {
    boolean isAvailable(Context context);
}

package com.android.settings.fingerprint.utils;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.os.SystemVibrator;
import android.os.Vibrator;
import android.util.Log;

public class HapticFeedback {
    private static final long[] DEFAULT_DURATION_PATTERN = new long[]{0, 10, 20, 30};
    private boolean mEnabled;
    private long[] mHapticPattern;
    private Vibrator mVibrator;

    public void init(Context context, boolean enabled) {
        this.mEnabled = enabled;
        if (enabled) {
            this.mVibrator = new SystemVibrator();
            if (!loadHapticSystemPattern(context.getResources())) {
                this.mHapticPattern = DEFAULT_DURATION_PATTERN;
            }
        }
    }

    private boolean loadHapticSystemPattern(Resources r) {
        this.mHapticPattern = null;
        try {
            int[] pattern = r.getIntArray(17236000);
            if (pattern == null || pattern.length == 0) {
                Log.e("HapticFeedback", "Haptic pattern is null or empty.");
                return false;
            }
            this.mHapticPattern = new long[pattern.length];
            for (int i = 0; i < pattern.length; i++) {
                this.mHapticPattern[i] = (long) pattern[i];
            }
            return true;
        } catch (NotFoundException nfe) {
            Log.e("HapticFeedback", "Vibrate pattern missing.", nfe);
            return false;
        }
    }
}

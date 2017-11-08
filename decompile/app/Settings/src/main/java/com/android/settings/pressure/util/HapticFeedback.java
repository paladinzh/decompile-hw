package com.android.settings.pressure.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.os.SystemVibrator;
import android.os.Vibrator;
import android.util.Log;

public class HapticFeedback {
    private SystemVibrator mVibrator;

    public HapticFeedback(Context context) {
        this.mVibrator = (SystemVibrator) ((Vibrator) context.getSystemService("vibrator"));
    }

    public void vibrate(int mode) {
        try {
            Class.forName("android.os.SystemVibrator").getMethod("hwVibrate", new Class[]{AudioAttributes.class, Integer.TYPE}).invoke(this.mVibrator, new Object[]{null, Integer.valueOf(mode)});
            Log.d("HapticFeedback", "Call SystemVibrator.hwVibrate success.");
        } catch (RuntimeException e) {
            e.printStackTrace();
            Log.e("HapticFeedback", ": reflection exception: " + e.getMessage());
        } catch (Exception e2) {
            e2.printStackTrace();
            Log.e("HapticFeedback", "vibrate(), exception occured " + e2.getMessage());
        }
    }
}

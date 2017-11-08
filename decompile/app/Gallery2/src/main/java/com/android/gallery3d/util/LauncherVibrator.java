package com.android.gallery3d.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.os.SystemVibrator;
import android.os.Vibrator;

public final class LauncherVibrator {
    private SystemVibrator mVibrator;

    public LauncherVibrator(Context context) {
        this.mVibrator = (SystemVibrator) ((Vibrator) context.getSystemService("vibrator"));
    }

    public void vibrate(AudioAttributes attributes, int mode) {
        try {
            Class.forName("android.os.SystemVibrator").getMethod("hwVibrate", new Class[]{AudioAttributes.class, Integer.TYPE}).invoke(this.mVibrator, new Object[]{attributes, Integer.valueOf(mode)});
        } catch (ClassNotFoundException e) {
            GalleryLog.w("LauncherVibrator", "vibrate(), class not found " + e.getMessage());
        } catch (NoSuchMethodException e2) {
            GalleryLog.w("LauncherVibrator", "vibrate(), no such method " + e2.getMessage());
        } catch (Exception e3) {
            GalleryLog.w("LauncherVibrator", "vibrate(), invocation exception " + e3.getMessage());
        } catch (Exception e32) {
            GalleryLog.w("LauncherVibrator", "vibrate(), exception occured " + e32.getMessage());
        }
    }
}

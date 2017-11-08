package com.fyusion.sdk.camera.impl;

import android.graphics.Point;
import android.media.CamcorderProfile;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.common.ext.util.b;

/* compiled from: Unknown */
public class g {
    private static final String a = g.class.getSimpleName();
    private static Point b;
    private static Point c;

    public static Point a(int i) {
        if (b == null) {
            b = new Point(0, 0);
        }
        CamcorderProfile camcorderProfile = CamcorderProfile.get(i, 1);
        if (camcorderProfile != null) {
            b.x = camcorderProfile.videoFrameWidth;
            b.y = camcorderProfile.videoFrameHeight;
        }
        return b;
    }

    public static CamcorderProfile a(int i, int i2) {
        Point a = a(i);
        Point b = b(i);
        DLog.d("CameraProfile", "Device: " + b.b());
        DLog.d("CameraProfile", "Device high h: " + a.y + " w: " + a.x);
        DLog.d("CameraProfile", "Device low h: " + b.y + " w: " + b.x);
        return !CamcorderProfile.hasProfile(i, i2) ? i2 != 6 ? b.y != 720 ? a.y != 720 ? null : CamcorderProfile.get(i, 1) : CamcorderProfile.get(i, 0) : (a.y == 1080 || a.y == 1088) ? CamcorderProfile.get(i, 1) : (b.y == 1080 || b.y == 1088) ? CamcorderProfile.get(i, 0) : null : CamcorderProfile.get(i, i2);
    }

    public static Point b(int i) {
        if (c == null) {
            c = new Point(0, 0);
        }
        CamcorderProfile camcorderProfile = CamcorderProfile.get(i, 0);
        if (camcorderProfile != null) {
            c.x = camcorderProfile.videoFrameWidth;
            c.y = camcorderProfile.videoFrameHeight;
        }
        return c;
    }
}

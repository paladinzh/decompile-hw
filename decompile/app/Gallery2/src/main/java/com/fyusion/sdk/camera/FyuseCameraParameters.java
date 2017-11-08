package com.fyusion.sdk.camera;

import android.util.Size;
import com.fyusion.sdk.camera.MeteringArea.Preset;
import com.fyusion.sdk.common.ext.Key;
import com.fyusion.sdk.common.ext.internal.Settings;
import com.fyusion.sdk.common.ext.internal.b;
import com.fyusion.sdk.common.ext.internal.b.a;

/* compiled from: Unknown */
public class FyuseCameraParameters extends b {
    public static final Key<Size> CAPTURE_RESOLUTION = new Key("com.fyusion.sdk.key.capture.resolution");
    public static final Key<MeteringArea[]> CUSTOM_EXPOSURE_METERING_AREAS = new Key("com.fyusion.sdk.camera.exposure.motionoptimized.meterareas");
    public static final Key<Preset> CUSTOM_EXPOSURE_METERING_PRESET = new Key("com.fyusion.sdk.camera.exposure.motionoptimized.meterpreset");
    public static final Key<Integer> EXPOSURE_TYPE = new Key("com.fyusion.sdk.camera.exposuretype");
    public static final Integer EXPOSURE_TYPE_DEFAULT = Integer.valueOf(1);
    public static final Integer EXPOSURE_TYPE_MOTION_OPTIMIZED = Integer.valueOf(2);

    /* compiled from: Unknown */
    public static class Builder extends a {
        public FyuseCameraParameters build() {
            return new FyuseCameraParameters(this.a);
        }
    }

    private FyuseCameraParameters(Settings settings) {
        super(settings);
    }
}

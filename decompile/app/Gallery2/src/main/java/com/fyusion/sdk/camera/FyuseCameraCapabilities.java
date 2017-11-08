package com.fyusion.sdk.camera;

import android.annotation.TargetApi;
import android.os.Build.VERSION;
import android.util.Size;
import com.fyusion.sdk.camera.impl.d;
import com.fyusion.sdk.common.ext.Key;
import com.fyusion.sdk.common.ext.internal.Settings;
import com.fyusion.sdk.common.ext.internal.a;
import java.util.List;

@TargetApi(21)
/* compiled from: Unknown */
public class FyuseCameraCapabilities {
    public static final Key<Boolean> FLASH_SUPPORTED = new Key("com.fyusion.sdk.key.camera.flash.supported");
    public static final Key<Boolean> MANUAL_EXPOSURE_CONTROL = new Key("com.fyusion.sdk.key.camera.manualexposure.control");
    public static final Key<Size[]> SUPPORTED_CAPTURE_RESOLUTIONS = new Key("com.fyusion.sdk.key.capture.resolution.supported");
    public static final Key<Integer[]> SUPPORTED_EXPOSURE_TYPES = new Key("com.fyusion.sdk.key.camera.exposuretype.supported");
    private static FyuseCameraCapabilities a;
    private Settings b;

    /* compiled from: Unknown */
    public static class Builder {
        String a;
        d b;

        public FyuseCameraCapabilities build() {
            if (this.a == null || this.b == null) {
                throw new IllegalArgumentException("CameraId and Provider must be set before build()");
            }
            Settings settings = new Settings();
            if (VERSION.SDK_INT >= 21) {
                if (this.b.a(this.a) != null) {
                    settings.set(FyuseCameraCapabilities.SUPPORTED_CAPTURE_RESOLUTIONS, this.b.a(this.a));
                }
                if (this.b.b(this.a) != null) {
                    settings.set(FyuseCameraCapabilities.SUPPORTED_EXPOSURE_TYPES, this.b.b(this.a));
                }
                if (this.b.c(this.a) != null) {
                    settings.set(FyuseCameraCapabilities.MANUAL_EXPOSURE_CONTROL, this.b.c(this.a));
                }
            }
            settings.set(FyuseCameraCapabilities.FLASH_SUPPORTED, Boolean.valueOf(this.b.d(this.a)));
            return new FyuseCameraCapabilities(settings);
        }

        public Builder cameraId(String str) {
            this.a = str;
            return this;
        }

        public Builder provider(d dVar) {
            this.b = dVar;
            return this;
        }
    }

    private FyuseCameraCapabilities() {
        this.b = new Settings();
        if (VERSION.SDK_INT >= 21) {
            Size size = new Size(1280, 720);
            Size size2 = new Size(1920, 1080);
            this.b.set(SUPPORTED_CAPTURE_RESOLUTIONS, new Size[]{size, size2});
            this.b.set(SUPPORTED_EXPOSURE_TYPES, new Integer[]{FyuseCameraParameters.EXPOSURE_TYPE_DEFAULT, FyuseCameraParameters.EXPOSURE_TYPE_MOTION_OPTIMIZED});
        }
        this.b.set(MANUAL_EXPOSURE_CONTROL, Boolean.TRUE);
        a.a(new String[]{"com.fyusion.sdk.ext.highres.CaptureCapabilities"}, this.b);
    }

    private FyuseCameraCapabilities(Settings settings) {
        this.b = new Settings();
        this.b = settings;
    }

    public static synchronized FyuseCameraCapabilities getInstance() {
        FyuseCameraCapabilities fyuseCameraCapabilities;
        synchronized (FyuseCameraCapabilities.class) {
            if (a == null) {
                a = new FyuseCameraCapabilities();
            }
            fyuseCameraCapabilities = a;
        }
        return fyuseCameraCapabilities;
    }

    public <T> T get(Key<T> key) {
        return this.b.get(key) != null ? this.b.get(key) : null;
    }

    public List<Key> getKeys() {
        return this.b.getKeys();
    }
}

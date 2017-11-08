package com.fyusion.sdk.camera;

import android.view.SurfaceView;
import android.view.TextureView;
import com.fyusion.sdk.camera.FyuseCamera.CameraType;
import com.fyusion.sdk.camera.impl.i;
import com.fyusion.sdk.camera.impl.j;
import com.fyusion.sdk.camera.impl.o;
import com.fyusion.sdk.camera.util.a;
import com.fyusion.sdk.common.AuthenticationException;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.common.FyuseSDK;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/* compiled from: Unknown */
public final class CameraBuilder {
    public static final int FYUSION_CAMERA_1 = 1;
    public static final int FYUSION_CAMERA_2 = 2;
    private static final String a = CameraBuilder.class.getSimpleName();
    private o b;
    private int c;
    private CameraType d;
    private boolean e = false;

    @Retention(RetentionPolicy.SOURCE)
    /* compiled from: Unknown */
    public @interface CAMERA_VERSION {
    }

    private CameraBuilder() {
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int a() throws FyuseCameraException, AuthenticationException {
        if (a.a()) {
            if (!a.b() || !a.c()) {
                if (this.d != CameraType.BACK_CAMERA || !a.b()) {
                    if (this.d == CameraType.FRONT_CAMERA) {
                    }
                }
            }
            return 2;
        }
        return 1;
    }

    private FyuseCamera a(int i) throws FyuseCameraException {
        switch (i) {
            case 1:
                DLog.i(a, "Using Camera API 1 Implementation");
                return new i(FyuseSDK.getContext(), this.b, 1);
            case 2:
                if (a.a()) {
                    DLog.i(a, "Using Camera API 2 Implementation");
                    FyuseCamera jVar = new j(FyuseSDK.getContext(), this.b, 2);
                    jVar.a(this.e);
                    return jVar;
                }
                throw new FyuseCameraException("Insufficient Support for Camera2. Requires Android API 21 or higher, and Camera2 device level support to be LIMITED or FULL ");
            default:
                throw new FyuseCameraException("Unknown camera version: " + i);
        }
    }

    public static CameraBuilder newInstance() {
        com.fyusion.sdk.common.a.a().d();
        return new CameraBuilder();
    }

    public FyuseCamera build() throws FyuseCameraException, AuthenticationException {
        if (com.fyusion.sdk.common.a.a().f("camera")) {
            throw new AuthenticationException("camera component is disabled.");
        } else if (this.b != null) {
            if (!(this.c == 1 || this.c == 2)) {
                this.c = a();
            }
            return a(this.c);
        } else {
            throw new IllegalStateException("Preview surface has not been set yet.");
        }
    }

    public CameraBuilder cameraType(CameraType cameraType) {
        this.d = cameraType;
        return this;
    }

    public CameraBuilder depthEnabled(boolean z) {
        this.e = z;
        return this;
    }

    public CameraBuilder previewSurface(SurfaceView surfaceView) {
        DLog.i(a, "Camera use SurfaceView");
        this.b = new o(surfaceView);
        return this;
    }

    public CameraBuilder previewSurface(TextureView textureView) {
        DLog.i(a, "Camera use TextureView");
        this.b = new o(textureView);
        return this;
    }

    public CameraBuilder version(int i) {
        this.c = i;
        return this;
    }
}

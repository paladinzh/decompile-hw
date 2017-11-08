package com.fyusion.sdk.camera.impl;

import android.annotation.TargetApi;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/* compiled from: Unknown */
public class o {
    private SurfaceView a;
    private TextureView b;

    /* compiled from: Unknown */
    private static class a implements Comparator<Size> {
        private a() {
        }

        @TargetApi(21)
        public int a(Size size, Size size2) {
            return Long.signum((((long) size.getWidth()) * ((long) size.getHeight())) - (((long) size2.getWidth()) * ((long) size2.getHeight())));
        }

        @TargetApi(21)
        public /* synthetic */ int compare(Object obj, Object obj2) {
            return a((Size) obj, (Size) obj2);
        }
    }

    public o(SurfaceView surfaceView) {
        if (surfaceView != null) {
            this.a = surfaceView;
            return;
        }
        throw new IllegalArgumentException("View can not be nulll");
    }

    public o(TextureView textureView) {
        if (textureView != null) {
            this.b = textureView;
            return;
        }
        throw new IllegalArgumentException("View can not be nulll");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @TargetApi(21)
    private static Size a(Display display, CameraCharacteristics cameraCharacteristics, int i, int i2, Size[] sizeArr) throws Exception {
        Point point = new Point();
        display.getRealSize(point);
        int rotation = display.getRotation();
        int intValue = ((Integer) cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)).intValue();
        switch (rotation) {
            case 0:
            case 2:
                if (!(intValue == 90 || intValue == 270)) {
                    break;
                }
            case 1:
            case 3:
                if (!(intValue == 0 || intValue == 180)) {
                    break;
                }
            default:
                Log.e("SurfaceAdapter", "Display rotation is invalid: " + rotation);
                break;
        }
    }

    @TargetApi(21)
    private static Size a(Size[] sizeArr, int i, int i2, int i3, int i4) throws Exception {
        int min = Math.min(i3, 1920);
        int min2 = Math.min(i4, 1080);
        Collection arrayList = new ArrayList();
        Collection arrayList2 = new ArrayList();
        for (Size size : sizeArr) {
            if (size.getWidth() <= min && size.getHeight() <= min2 && size.getHeight() == (size.getWidth() * min2) / min) {
                if (size.getWidth() >= i && size.getHeight() >= i2) {
                    arrayList.add(size);
                } else {
                    arrayList2.add(size);
                }
            }
        }
        if (arrayList.size() > 0) {
            return (Size) Collections.min(arrayList, new a());
        }
        if (arrayList2.size() > 0) {
            return (Size) Collections.max(arrayList2, new a());
        }
        Log.e("SurfaceAdapter", "Couldn't find any suitable preview size");
        throw new Exception("Couldn't find any suitable preview size");
    }

    @TargetApi(21)
    private static Size b(Display display, CameraCharacteristics cameraCharacteristics, int i, int i2) throws Exception {
        return a(display, cameraCharacteristics, i, i2, ((StreamConfigurationMap) cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)).getOutputSizes(SurfaceHolder.class));
    }

    @TargetApi(21)
    private static Size c(Display display, CameraCharacteristics cameraCharacteristics, int i, int i2) throws Exception {
        return a(display, cameraCharacteristics, i, i2, ((StreamConfigurationMap) cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)).getOutputSizes(SurfaceTexture.class));
    }

    public int a() {
        return this.a == null ? this.b.getWidth() : this.a.getWidth();
    }

    @TargetApi(21)
    public Size a(Display display, CameraCharacteristics cameraCharacteristics, int i, int i2) throws Exception {
        Size c = this.a == null ? c(display, cameraCharacteristics, i, i2) : b(display, cameraCharacteristics, i, i2);
        Log.d("SurfaceAdapter", "Optimal size: " + c);
        return h.a(c);
    }

    public void a(Camera camera) throws IOException {
        if (this.a == null) {
            camera.setPreviewTexture(this.b.getSurfaceTexture());
        } else {
            camera.setPreviewDisplay(this.a.getHolder());
        }
    }

    @TargetApi(21)
    public void a(Size size) {
        if (this.a == null) {
            this.b.getSurfaceTexture().setDefaultBufferSize(size.getWidth(), size.getHeight());
        } else {
            this.a.getHolder().setFixedSize(size.getWidth(), size.getHeight());
        }
    }

    @TargetApi(21)
    public void a(Display display, int i, int i2, Size size) {
        if (this.b != null) {
            int rotation = display.getRotation();
            Matrix matrix = new Matrix();
            RectF rectF = new RectF(0.0f, 0.0f, (float) i, (float) i2);
            RectF rectF2 = new RectF(0.0f, 0.0f, (float) size.getHeight(), (float) size.getWidth());
            float centerX = rectF.centerX();
            float centerY = rectF.centerY();
            if (1 == rotation || 3 == rotation) {
                rectF2.offset(centerX - rectF2.centerX(), centerY - rectF2.centerY());
                matrix.setRectToRect(rectF, rectF2, ScaleToFit.FILL);
                float max = Math.max(((float) i2) / ((float) size.getHeight()), ((float) i) / ((float) size.getWidth()));
                matrix.postScale(max, max, centerX, centerY);
                matrix.postRotate((float) ((rotation - 2) * 90), centerX, centerY);
            } else if (2 == rotation) {
                matrix.postRotate(BitmapDescriptorFactory.HUE_CYAN, centerX, centerY);
            }
            this.b.setTransform(matrix);
        }
    }

    public int b() {
        return this.a == null ? this.b.getHeight() : this.a.getHeight();
    }

    public Surface c() {
        return this.a == null ? new Surface(this.b.getSurfaceTexture()) : this.a.getHolder().getSurface();
    }

    @TargetApi(21)
    public Size d() {
        return this.a == null ? new Size(this.b.getWidth(), this.b.getHeight()) : new Size(this.a.getWidth(), this.a.getHeight());
    }
}

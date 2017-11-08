package com.fyusion.sdk.camera.impl;

import android.annotation.TargetApi;
import android.hardware.camera2.CaptureResult;
import android.media.Image;
import com.fyusion.sdk.camera.MeteringArea;
import com.fyusion.sdk.camera.MeteringArea.Preset;
import com.huawei.watermark.manager.parse.WMElement;
import java.util.ArrayList;
import java.util.Collection;

@TargetApi(21)
/* compiled from: Unknown */
public class ExposureControl {
    private static final String TAG = "expctrl";
    private static boolean nativeOK;
    private long jniControl;
    private long meterControl;

    /* compiled from: Unknown */
    /* renamed from: com.fyusion.sdk.camera.impl.ExposureControl$1 */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$fyusion$sdk$camera$MeteringArea$Preset = new int[Preset.values().length];

        static {
            try {
                $SwitchMap$com$fyusion$sdk$camera$MeteringArea$Preset[Preset.CENTER_ONLY.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$fyusion$sdk$camera$MeteringArea$Preset[Preset.CENTER_AND_CORNERS.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$fyusion$sdk$camera$MeteringArea$Preset[Preset.DIAMOND.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    /* compiled from: Unknown */
    public static class Delta {
        public float exposureDelta;
        public int isoDelta;

        public Delta() {
            this.exposureDelta = 0.0f;
            this.isoDelta = 0;
        }

        public Delta(float f, int i) {
            this.exposureDelta = f;
            this.isoDelta = i;
        }
    }

    static {
        nativeOK = false;
        try {
            System.loadLibrary(TAG);
            nativeOK = true;
        } catch (Throwable e) {
            e.printStackTrace();
        } catch (Throwable e2) {
            e2.printStackTrace();
        }
    }

    public ExposureControl(float f, float f2, float f3, int i, int i2) {
        if (nativeOK) {
            setupNative(f, f2, f3, i, i2);
        }
    }

    private native void addMeteringArea(MeteringArea meteringArea);

    private native void clearMeteringAreas();

    private native void computeDeltaNative(Image image, float f, int i, Delta delta);

    private native void finalizeMeteringAreas();

    private native void releaseNative();

    private native void setupNative(float f, float f2, float f3, int i, int i2);

    public Delta computeExposureDelta(Image image, CaptureResult captureResult) {
        if (!nativeOK) {
            return new Delta(0.0f, 0);
        }
        Delta delta = new Delta();
        computeDeltaNative(image, 0.001f * ((float) (((Long) captureResult.get(CaptureResult.SENSOR_EXPOSURE_TIME)).longValue() / 1000)), ((Integer) captureResult.get(CaptureResult.SENSOR_SENSITIVITY)).intValue(), delta);
        return delta;
    }

    protected void finalize() {
        if (nativeOK) {
            releaseNative();
        }
    }

    public void setMeteringAreas(Collection<MeteringArea> collection) {
        clearMeteringAreas();
        for (MeteringArea addMeteringArea : collection) {
            addMeteringArea(addMeteringArea);
        }
        finalizeMeteringAreas();
    }

    public void setMeteringPreset(Preset preset) {
        ArrayList arrayList = new ArrayList();
        switch (AnonymousClass1.$SwitchMap$com$fyusion$sdk$camera$MeteringArea$Preset[preset.ordinal()]) {
            case 1:
                arrayList.add(new MeteringArea(50, 50, 15, 15));
                break;
            case 2:
                arrayList.add(new MeteringArea(50, 50, 15, 15, WMElement.CAMERASIZEVALUE1B1));
                arrayList.add(new MeteringArea(5, 5, 10, 10, 0.5f));
                arrayList.add(new MeteringArea(5, 95, 10, 10, 0.5f));
                arrayList.add(new MeteringArea(95, 95, 10, 10, 0.5f));
                arrayList.add(new MeteringArea(95, 5, 10, 10, 0.5f));
                break;
            case 3:
                arrayList.add(new MeteringArea(50, 50, 10, 10, WMElement.CAMERASIZEVALUE1B1));
                arrayList.add(new MeteringArea(33, 50, 8, 8, 0.75f));
                arrayList.add(new MeteringArea(66, 50, 8, 8, 0.75f));
                arrayList.add(new MeteringArea(50, 33, 8, 8, 0.75f));
                arrayList.add(new MeteringArea(50, 66, 8, 8, 0.75f));
                arrayList.add(new MeteringArea(16, 50, 6, 6, 0.5f));
                arrayList.add(new MeteringArea(84, 50, 6, 6, 0.5f));
                break;
        }
        setMeteringAreas(arrayList);
    }
}

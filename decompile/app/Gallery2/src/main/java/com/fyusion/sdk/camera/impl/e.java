package com.fyusion.sdk.camera.impl;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.util.Range;
import com.fyusion.sdk.camera.MeteringArea;
import com.fyusion.sdk.camera.MeteringArea.Preset;
import com.fyusion.sdk.camera.impl.ExposureControl.Delta;
import com.fyusion.sdk.common.DLog;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

/* compiled from: Unknown */
public abstract class e {
    protected float b;
    protected int c;
    protected int d = 0;
    protected int e = 0;
    protected int f = 0;
    protected int g = 0;
    protected boolean h = false;
    protected boolean i = true;
    protected boolean j = false;
    protected boolean k = false;
    protected ExposureControl l = null;
    protected BlockingQueue<Image> m = null;
    protected BlockingQueue<CaptureResult> n = null;
    protected int o = -1;

    @TargetApi(21)
    protected static boolean a(CameraCharacteristics cameraCharacteristics) {
        boolean z = true;
        if (((Integer) cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)).intValue() != 2) {
            boolean z2;
            int[] iArr = (int[]) cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES);
            if (iArr == null) {
                z2 = false;
            } else {
                z2 = false;
                for (int i : iArr) {
                    if (i == 0) {
                        z2 = true;
                    }
                }
            }
            if (z2) {
                Range range = (Range) cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
                if (((Range) cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)) != null) {
                    if (range == null) {
                    }
                    return z;
                }
                z = false;
                return z;
            }
        }
        return false;
    }

    private int b(int i) {
        if (i < this.e) {
            i = this.e;
        }
        return i <= this.f ? i : this.f;
    }

    protected void a() {
        for (Image close : this.m) {
            close.close();
        }
        this.m.clear();
        this.n.clear();
    }

    protected void a(int i) {
        this.m = new LinkedBlockingQueue(i);
        this.n = new LinkedBlockingQueue(i + 1);
    }

    @TargetApi(21)
    protected void a(CaptureResult captureResult) {
        Image image = null;
        if (this.h) {
            try {
                long longValue = ((Long) captureResult.get(CaptureResult.SENSOR_TIMESTAMP)).longValue();
                int size = this.m.size();
                while (size > 0) {
                    Image image2 = (Image) this.m.take();
                    if (image2.getTimestamp() != longValue) {
                        if ((image2.getTimestamp() <= longValue ? 1 : null) == null) {
                            this.m.add(image2);
                        } else {
                            image2.close();
                        }
                        image2 = image;
                    }
                    size--;
                    image = image2;
                }
                if (image == null) {
                    try {
                        if (!this.n.add(captureResult)) {
                            DLog.w("cexphdl", "Dropping capture results since the queue is full");
                            return;
                        }
                        return;
                    } catch (IllegalStateException e) {
                        DLog.w("cexphdl", "Dropping capture results since the queue is full");
                        return;
                    }
                }
                a(image, captureResult);
                image.close();
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
        }
    }

    @TargetApi(21)
    protected void a(Image image, CaptureResult captureResult) {
        if (!this.j && this.i && captureResult.getSequenceId() == this.o) {
            int intValue = ((Integer) captureResult.get(CaptureResult.SENSOR_SENSITIVITY)).intValue();
            float longValue = 1.0E-6f * ((float) ((Long) captureResult.get(CaptureResult.SENSOR_EXPOSURE_TIME)).longValue());
            Delta computeExposureDelta = this.l.computeExposureDelta(image, captureResult);
            int intValue2 = ((Integer) captureResult.get(CaptureResult.SENSOR_SENSITIVITY)).intValue();
            if (computeExposureDelta.isoDelta + intValue2 > this.f) {
                computeExposureDelta.isoDelta = this.f - intValue2;
            }
            if (computeExposureDelta.isoDelta + intValue2 < this.e) {
                computeExposureDelta.isoDelta = intValue2 - this.e;
            }
            int max = Math.max(10, this.d / SmsCheckResult.ESCT_200);
            if ((Math.abs(computeExposureDelta.exposureDelta) > 0.35f) || Math.abs(computeExposureDelta.isoDelta) > max) {
                if (Math.abs(computeExposureDelta.exposureDelta) <= 0.35f) {
                    computeExposureDelta.exposureDelta = 0.0f;
                }
                if (Math.abs(computeExposureDelta.isoDelta) <= max) {
                    computeExposureDelta.isoDelta = 0;
                }
                float f = computeExposureDelta.exposureDelta + longValue;
                this.c = computeExposureDelta.isoDelta + intValue;
                this.b = f;
                b();
            } else {
                intValue2 = this.g + 1;
                this.g = intValue2;
                if (intValue2 >= 2) {
                    this.c = intValue;
                    this.b = longValue;
                    this.j = true;
                    this.i = false;
                    c();
                }
            }
        }
    }

    @TargetApi(21)
    protected boolean a(ImageReader imageReader) {
        CaptureResult captureResult = null;
        if (!this.h) {
            return false;
        }
        try {
            Image acquireLatestImage = imageReader.acquireLatestImage();
            if (acquireLatestImage == null) {
                return true;
            }
            try {
                int size = this.n.size();
                while (size > 0) {
                    CaptureResult captureResult2 = (CaptureResult) this.n.take();
                    long longValue = ((Long) captureResult2.get(CaptureResult.SENSOR_TIMESTAMP)).longValue();
                    if (longValue != acquireLatestImage.getTimestamp()) {
                        if (!(longValue <= acquireLatestImage.getTimestamp())) {
                            this.n.add(captureResult2);
                        }
                        captureResult2 = captureResult;
                    }
                    size--;
                    captureResult = captureResult2;
                }
                if (captureResult == null) {
                    try {
                        if (!this.m.add(acquireLatestImage)) {
                            DLog.w("cexphdl", "Dropping images since pending queue is full");
                            acquireLatestImage.close();
                        }
                    } catch (IllegalStateException e) {
                        DLog.w("cexphdl", "Dropping images since pending queue is full");
                        acquireLatestImage.close();
                    }
                } else {
                    a(acquireLatestImage, captureResult);
                    acquireLatestImage.close();
                }
                return true;
            } catch (InterruptedException e2) {
                e2.printStackTrace();
                return false;
            }
        } catch (IllegalStateException e3) {
            a();
            return false;
        }
    }

    protected abstract void b();

    @TargetApi(21)
    protected boolean b(CameraCharacteristics cameraCharacteristics) {
        this.l = null;
        this.g = 0;
        this.j = false;
        this.i = false;
        if (((Integer) cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)).intValue() != 2) {
            int[] iArr = (int[]) cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES);
            this.k = false;
            if (iArr != null) {
                for (int i : iArr) {
                    if (i == 0) {
                        this.k = true;
                    }
                }
            }
        }
        if (this.k) {
            float f;
            float f2;
            Range range = (Range) cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
            Range range2 = (Range) cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
            if (range == null) {
                this.k = false;
            } else {
                this.e = ((Integer) range.getLower()).intValue();
                this.f = ((Integer) range.getUpper()).intValue();
            }
            if (range2 == null) {
                this.k = false;
                f = 0.0f;
                f2 = 0.0f;
            } else {
                f = 1.0E-6f * ((float) ((Long) range2.getUpper()).longValue());
                f2 = 1.0E-6f * ((float) ((Long) range2.getLower()).longValue());
            }
            if (this.k) {
                this.d = this.f - this.e;
                this.b = 12.0f;
                this.c = (this.e + this.f) / 2;
                this.l = new ExposureControl(12.0f, f2, f, this.e, this.f);
                return true;
            }
        }
        return false;
    }

    protected abstract void c();

    public void decreaseBrightness(int i) {
        if (isCustomExposureAdjustmentEnabled() && !this.i && this.j && i > 0) {
            this.c = b(this.c - Math.max(1, (Math.min(i, 10) * this.d) / 500));
            b();
        }
    }

    public void increaseBrightness(int i) {
        if (isCustomExposureAdjustmentEnabled() && !this.i && this.j && i > 0) {
            this.c = b(Math.max(1, (Math.min(i, 10) * this.d) / 500) + this.c);
            b();
        }
    }

    public boolean isCustomExposureAdjustmentEnabled() {
        return isCustomExposureAdjustmentSupported() && this.h;
    }

    public boolean isCustomExposureAdjustmentSupported() {
        return this.k;
    }

    public void setCustomExposureMetering(Preset preset) {
        if (isCustomExposureAdjustmentSupported()) {
            if (!isCustomExposureAdjustmentEnabled()) {
                throw new IllegalStateException("Please enable custom exposure before setting parameters");
            } else if (this.l != null) {
                this.l.setMeteringPreset(preset);
            }
        }
    }

    public void setCustomExposureMetering(Collection<MeteringArea> collection) {
        if (isCustomExposureAdjustmentSupported()) {
            if (!isCustomExposureAdjustmentEnabled()) {
                throw new IllegalStateException("Please enable custom exposure before setting parameters");
            } else if (this.l != null) {
                this.l.setMeteringAreas(collection);
            }
        }
    }

    public void setEnableCustomExposureAdjustment(boolean z) {
        this.h = z;
    }

    protected void triggerCustomExposureAdjustment() {
        if (!this.i) {
            this.g = 0;
            this.j = false;
            this.i = true;
            this.o = -1;
        }
    }
}

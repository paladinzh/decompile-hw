package com.fyusion.sdk.camera;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Build.VERSION;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.fyusion.sdk.camera.a.b;
import com.fyusion.sdk.camera.a.c;
import com.fyusion.sdk.camera.impl.e;
import com.fyusion.sdk.camera.impl.h;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.common.a;
import com.fyusion.sdk.core.util.d;
import java.io.File;
import java.util.List;

@TargetApi(21)
/* compiled from: Unknown */
public class FyuseCameraProcessor extends e {
    public static final String STOPPED_REASON_CAMERA_MOVED_BACKWARDS = "Camera moved backwards";
    public static final String STOPPED_REASON_TERMINATED_PREMATURELY = "Capture terminated unexpectedly.";
    static final /* synthetic */ boolean a;
    private RecordingProgressListener A;
    private Handler B;
    private b C;
    private boolean D = false;
    private boolean E = false;
    private final Object F = new Object();
    private CaptureCallback G = new CaptureCallback(this) {
        final /* synthetic */ FyuseCameraProcessor a;

        {
            this.a = r1;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onCaptureCompleted(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, TotalCaptureResult totalCaptureResult) {
            synchronized (this.a.F) {
                if (cameraCaptureSession == null) {
                } else if (!this.a.D) {
                    this.a.a((CaptureResult) totalCaptureResult);
                }
            }
        }
    };
    private OnImageAvailableListener H = new OnImageAvailableListener(this) {
        final /* synthetic */ FyuseCameraProcessor a;

        {
            this.a = r1;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onImageAvailable(ImageReader imageReader) {
            boolean z = false;
            synchronized (this.a.F) {
                if (this.a.t == null) {
                } else if (this.a.D) {
                    r0 = imageReader.acquireNextImage();
                    if (r0 != null) {
                        try {
                            this.a.C.a(r0);
                        } catch (RuntimeException e) {
                            this.a.z.onCapture(CaptureEvent.createStoppedEvent(FyuseCameraProcessor.STOPPED_REASON_TERMINATED_PREMATURELY));
                        }
                        r0.close();
                    }
                } else {
                    if (this.a.isCustomExposureAdjustmentEnabled()) {
                        z = this.a.a(imageReader);
                    }
                    if (!(z || this.a.u == null)) {
                        r0 = this.a.u.acquireLatestImage();
                        if (r0 != null) {
                            r0.close();
                        }
                    }
                }
            }
        }
    };
    private OnImageAvailableListener I = new OnImageAvailableListener(this) {
        final /* synthetic */ FyuseCameraProcessor a;

        {
            this.a = r1;
        }

        public void onImageAvailable(ImageReader imageReader) {
            Image acquireNextImage = imageReader.acquireNextImage();
            if (acquireNextImage != null) {
                byte[] bArr = new byte[((acquireNextImage.getWidth() * 2) * acquireNextImage.getHeight())];
                acquireNextImage.getPlanes()[0].getBuffer().get(bArr);
                this.a.C.a(bArr);
                acquireNextImage.close();
            }
        }
    };
    private int p;
    private int q;
    private CameraCharacteristics r;
    private Builder s;
    private CameraCaptureSession t;
    private ImageReader u;
    private ImageReader v;
    private Surface w;
    private Surface x;
    private MotionHintsListener y;
    private CaptureEventListener z;

    static {
        boolean z = false;
        if (!FyuseCameraProcessor.class.desiredAssertionStatus()) {
            z = true;
        }
        a = z;
    }

    private FyuseCameraProcessor(int i, int i2, CameraCharacteristics cameraCharacteristics, Builder builder) {
        this.p = i;
        this.q = i2;
        this.r = cameraCharacteristics;
        this.s = builder;
        super.a(5);
        super.b(cameraCharacteristics);
        this.u = ImageReader.newInstance(i, i2, 35, 5);
    }

    private static Size a(Size[] sizeArr, int i) {
        for (Size size : sizeArr) {
            Log.d("FyuseCameraProcessor", "Output size available : " + size.getWidth() + " x " + size.getHeight());
            if (size.getHeight() == i) {
                return size;
            }
        }
        return null;
    }

    private void a(Builder builder) {
        if (isCustomExposureAdjustmentEnabled()) {
            builder.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(0));
            builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, Long.valueOf((long) (this.b * 1000000.0f)));
            builder.set(CaptureRequest.SENSOR_SENSITIVITY, Integer.valueOf(this.c));
        }
    }

    private void a(String str) throws CameraAccessException {
        this.s.removeTarget(this.w);
        if (this.x != null) {
            this.s.removeTarget(this.x);
        }
        this.s.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(2));
        long a = d.a();
        synchronized (this.F) {
            this.D = false;
            try {
                if (this.t != null) {
                    this.t.stopRepeating();
                }
                this.t = null;
            } catch (IllegalStateException e) {
                DLog.w("FyuseCameraProcessor", "Camera exception: " + e.getMessage());
                this.t = null;
            } catch (Throwable th) {
                this.t = null;
            }
        }
        Log.d("FyuseCameraProcessor", "stopRecording took: " + d.a(a));
        if (this.u != null) {
            this.u.setOnImageAvailableListener(null, null);
        }
        if (this.v != null) {
            this.v.setOnImageAvailableListener(null, null);
        }
        this.C.a(str);
        a();
    }

    private void b(Builder builder) {
        builder.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(1));
        if (isCustomExposureAdjustmentEnabled()) {
            builder.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(0));
            builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, Long.valueOf((long) (this.b * 1000000.0f)));
            builder.set(CaptureRequest.SENSOR_SENSITIVITY, Integer.valueOf(this.c));
        }
    }

    private static Size c(CameraCharacteristics cameraCharacteristics) {
        StreamConfigurationMap streamConfigurationMap = (StreamConfigurationMap) cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (!a && streamConfigurationMap == null) {
            throw new AssertionError();
        }
        Size[] outputSizes = streamConfigurationMap.getOutputSizes(SurfaceHolder.class);
        Size a = a(outputSizes, 1080);
        if (a == null) {
            a = a(outputSizes, 720);
        }
        return h.a(a);
    }

    private Surface d() {
        if (VERSION.SDK_INT < 23 || !this.E) {
            return null;
        }
        this.v = ImageReader.newInstance(this.p, this.q, 1144402265, 5);
        return this.v.getSurface();
    }

    public static FyuseCameraProcessor newInstance(CameraCharacteristics cameraCharacteristics, Builder builder) {
        Size c = c(cameraCharacteristics);
        builder.set(CaptureRequest.CONTROL_AF_MODE, Integer.valueOf(4));
        return new FyuseCameraProcessor(c.getWidth(), c.getHeight(), cameraCharacteristics, builder);
    }

    protected void a() {
        synchronized (this.F) {
            super.a();
            if (!(this.t == null || this.u == null)) {
                Image acquireLatestImage = this.u.acquireLatestImage();
                if (acquireLatestImage != null) {
                    acquireLatestImage.close();
                }
            }
        }
    }

    protected void a(Image image, CaptureResult captureResult) {
        synchronized (this.F) {
            if (this.t != null) {
                super.a(image, captureResult);
            }
        }
    }

    public void attachSurfacesToList(List<Surface> list) {
        if (this.u != null) {
            this.u.close();
        }
        this.u = ImageReader.newInstance(this.p, this.q, 35, 5);
        this.w = this.u.getSurface();
        list.add(this.w);
        this.x = d();
        if (this.x != null) {
            list.add(this.x);
        }
    }

    protected void b() {
        synchronized (this.F) {
            try {
                if (this.t != null) {
                    a(this.s);
                    this.t.setRepeatingRequest(this.s.build(), this.G, this.B);
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    protected void c() {
    }

    public Surface getSurface() {
        return this.u == null ? null : this.u.getSurface();
    }

    public void resumePreview(CameraCaptureSession cameraCaptureSession, Handler handler) {
        try {
            synchronized (this.F) {
                this.t = cameraCaptureSession;
                this.B = handler;
                a();
                if (this.u != null) {
                    this.u.setOnImageAvailableListener(this.H, handler);
                }
            }
            a(this.s);
            cameraCaptureSession.setRepeatingRequest(this.s.build(), this.G, handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public FyuseCameraProcessor setCaptureEventListener(CaptureEventListener captureEventListener) {
        this.z = captureEventListener;
        return this;
    }

    public void setDepthEnabled(boolean z) {
        this.E = z;
    }

    public FyuseCameraProcessor setMotionHintsListener(MotionHintsListener motionHintsListener) {
        this.y = motionHintsListener;
        return this;
    }

    public FyuseCameraProcessor setRecordingProgressListener(RecordingProgressListener recordingProgressListener) {
        this.A = recordingProgressListener;
        return this;
    }

    public FyuseCameraProcessor startRecording(CameraCaptureSession cameraCaptureSession, Handler handler) {
        this.t = cameraCaptureSession;
        this.B = handler;
        a.a().d();
        this.s.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(1));
        this.s.addTarget(this.w);
        if (this.x != null) {
            this.s.addTarget(this.x);
        }
        return this;
    }

    public void stop() {
        synchronized (this.F) {
            try {
                if (this.D) {
                    stopRecording();
                } else if (this.t != null) {
                    this.t.stopRepeating();
                }
                if (!this.D) {
                    a();
                    if (this.u != null) {
                        this.u.close();
                    }
                    this.u = null;
                }
                this.t = null;
            } catch (Exception e) {
                DLog.w("FyuseCameraProcessor", "Camera exception: " + e.getMessage());
                if (!this.D) {
                    a();
                    if (this.u != null) {
                        this.u.close();
                    }
                    this.u = null;
                }
                this.t = null;
            } catch (Throwable th) {
                if (!this.D) {
                    a();
                    if (this.u != null) {
                        this.u.close();
                    }
                    this.u = null;
                }
                this.t = null;
            }
        }
    }

    public void stopRecording() throws CameraAccessException {
        if (this.D) {
            a("User initiated");
        }
    }

    public void to(File file) throws CameraAccessException {
        if (!this.D) {
            if (file != null && file.getParentFile().exists()) {
                synchronized (this.F) {
                    if (this.t != null) {
                    } else {
                        throw new IllegalStateException("Please call startRecording() before to() method");
                    }
                }
                this.C = new b(this.r, this.p, this.q, file, new c.b(this) {
                    final /* synthetic */ FyuseCameraProcessor a;

                    {
                        this.a = r1;
                    }

                    public void onMovingBackwards() {
                        try {
                            this.a.a(FyuseCameraProcessor.STOPPED_REASON_CAMERA_MOVED_BACKWARDS);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    public void onTerminatedPrematurely() {
                        try {
                            this.a.a(FyuseCameraProcessor.STOPPED_REASON_TERMINATED_PREMATURELY);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }
                });
                this.C.a(this.y);
                this.C.a(this.z);
                this.C.a(this.A);
                this.u.setOnImageAvailableListener(this.H, this.B);
                if (this.v != null) {
                    this.v.setOnImageAvailableListener(this.I, this.B);
                }
                synchronized (this.F) {
                    a();
                    b(this.s);
                    this.t.setRepeatingRequest(this.s.build(), this.G, this.B);
                    this.D = true;
                }
                return;
            }
            throw new IllegalStateException("Recording path does not exist.");
        }
    }

    public void triggerCustomExposureAdjustment() {
        synchronized (this.F) {
            if (!this.D) {
                super.triggerCustomExposureAdjustment();
            }
        }
    }
}

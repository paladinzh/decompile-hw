package com.fyusion.sdk.camera.impl;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraDevice.StateCallback;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.Image;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import com.fyusion.sdk.camera.CameraStatus;
import com.fyusion.sdk.camera.CaptureEvent;
import com.fyusion.sdk.camera.CaptureEvent.CaptureStatus;
import com.fyusion.sdk.camera.CaptureEventListener;
import com.fyusion.sdk.camera.CustomExposureControl;
import com.fyusion.sdk.camera.FyuseCamera;
import com.fyusion.sdk.camera.FyuseCamera.CameraType;
import com.fyusion.sdk.camera.FyuseCamera.RotationDirection;
import com.fyusion.sdk.camera.FyuseCameraCallback;
import com.fyusion.sdk.camera.FyuseCameraCapabilities;
import com.fyusion.sdk.camera.FyuseCameraException;
import com.fyusion.sdk.camera.FyuseCameraParameters;
import com.fyusion.sdk.camera.MeteringArea;
import com.fyusion.sdk.camera.MeteringArea.Preset;
import com.fyusion.sdk.camera.MotionHintsListener;
import com.fyusion.sdk.camera.RecordingProgressListener;
import com.fyusion.sdk.camera.SnapShotCallback;
import com.fyusion.sdk.camera.util.ColorHelper;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.common.ext.Key;
import com.fyusion.sdk.common.ext.Size;
import com.fyusion.sdk.common.ext.util.b;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@TargetApi(21)
/* compiled from: Unknown */
public final class j extends e implements FyuseCamera, d {
    private static final String a = j.class.getSimpleName();
    private HandlerThread A;
    private Handler B;
    private Builder C = null;
    private CameraCaptureSession D = null;
    private Context E;
    private o F;
    private a G;
    private c H;
    private List<CaptureEventListener> I;
    private List<RecordingProgressListener> J;
    private HandlerThread K;
    private Handler L;
    private CameraType M = CameraType.BACK_CAMERA;
    private CameraManager N;
    private String O = null;
    private String P = null;
    private Surface Q;
    private Surface R;
    private FyuseCameraCallback S;
    private int T;
    private RotationDirection U;
    private float[] V;
    private int W = 0;
    private int X = 0;
    private a Y = null;
    private BlockingQueue<byte[]> Z = new LinkedBlockingQueue(5);
    private ImageReader aa;
    private ImageReader ab;
    private Size ac;
    private Size ad;
    private CameraCharacteristics ae;
    private FyuseCameraParameters af;
    private boolean ag;
    private int ah;
    private com.fyusion.sdk.camera.b.a ai = new com.fyusion.sdk.camera.b.a();
    private int aj;
    private long ak;
    private List<android.util.Size> al;
    private StateCallback am = new StateCallback(this) {
        final /* synthetic */ j a;

        {
            this.a = r1;
        }

        public void onDisconnected(CameraDevice cameraDevice) {
            this.a.s = null;
        }

        public void onError(CameraDevice cameraDevice, int i) {
        }

        public void onOpened(CameraDevice cameraDevice) {
            this.a.s = cameraDevice;
            try {
                this.a.l();
                this.a.n();
                this.a.v();
            } catch (FyuseCameraException e) {
                e.printStackTrace();
            }
        }
    };
    private CaptureCallback an = new CaptureCallback(this) {
        final /* synthetic */ j a;

        {
            this.a = r1;
        }

        private void a(CaptureResult captureResult) {
            if (this.a.v) {
                Integer num = (Integer) captureResult.get(CaptureResult.CONTROL_AE_STATE);
                if (num != null) {
                    switch (num.intValue()) {
                        case 2:
                        case 4:
                            this.a.B();
                            return;
                        default:
                            return;
                    }
                }
            }
        }

        private void b(CaptureResult captureResult) {
            if (this.a.w && !this.a.x) {
                Integer num = (Integer) captureResult.get(CaptureResult.CONTROL_AF_STATE);
                if (num != null) {
                    switch (num.intValue()) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                        case 6:
                            break;
                        case 4:
                            if (this.a.S != null) {
                                this.a.x = true;
                                MeteringRectangle[] meteringRectangleArr = (MeteringRectangle[]) captureResult.get(CaptureResult.CONTROL_AF_REGIONS);
                                this.a.c(true);
                                break;
                            }
                            break;
                        case 5:
                            if (this.a.S != null) {
                                this.a.c(false);
                                break;
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        public void onCaptureCompleted(@NonNull CameraCaptureSession cameraCaptureSession, @NonNull CaptureRequest captureRequest, @NonNull TotalCaptureResult totalCaptureResult) {
            if (!this.a.r.isRecording() && this.a.t.tryLock()) {
                try {
                    b(totalCaptureResult);
                    if (!this.a.isCustomExposureAdjustmentEnabled()) {
                        a(totalCaptureResult);
                    } else if (this.a.i) {
                        this.a.a((CaptureResult) totalCaptureResult);
                    }
                    this.a.t.unlock();
                } catch (Throwable th) {
                    this.a.t.unlock();
                }
            }
        }

        public void onCaptureProgressed(@NonNull CameraCaptureSession cameraCaptureSession, @NonNull CaptureRequest captureRequest, @NonNull CaptureResult captureResult) {
            if (!this.a.r.isRecording() && this.a.t.tryLock()) {
                try {
                    b(captureResult);
                    if (!this.a.isCustomExposureAdjustmentEnabled()) {
                        a(captureResult);
                    }
                    this.a.t.unlock();
                } catch (Throwable th) {
                    this.a.t.unlock();
                }
            }
        }
    };
    private OnImageAvailableListener ao = new OnImageAvailableListener(this) {
        final /* synthetic */ j a;

        {
            this.a = r1;
        }

        public void onImageAvailable(ImageReader imageReader) {
            Image acquireNextImage;
            if (this.a.r.isRecording()) {
                acquireNextImage = imageReader.acquireNextImage();
                if (acquireNextImage != null) {
                    this.a.X = this.a.X + 1;
                    this.a.b(acquireNextImage);
                    acquireNextImage.close();
                    return;
                }
                return;
            }
            acquireNextImage = imageReader.acquireLatestImage();
            if (acquireNextImage != null) {
                acquireNextImage.close();
            }
        }
    };
    private OnImageAvailableListener ap = new OnImageAvailableListener(this) {
        final /* synthetic */ j a;

        {
            this.a = r1;
        }

        public void onImageAvailable(ImageReader imageReader) {
            boolean z = false;
            Image acquireNextImage;
            if (this.a.r.isRecording()) {
                acquireNextImage = imageReader.acquireNextImage();
                if (acquireNextImage != null) {
                    this.a.W = this.a.W + 1;
                    this.a.a(acquireNextImage);
                    acquireNextImage.close();
                    return;
                }
                return;
            }
            if (this.a.i) {
                z = this.a.a(imageReader);
            }
            if (!z) {
                acquireNextImage = imageReader.acquireLatestImage();
                if (acquireNextImage != null) {
                    acquireNextImage.close();
                }
            }
        }
    };
    private boolean p;
    private boolean q = false;
    private final CameraStatus r;
    private CameraDevice s;
    private final Lock t = new ReentrantLock();
    private boolean u = false;
    private boolean v = false;
    private boolean w = false;
    private boolean x = false;
    private boolean y = false;
    private boolean z = false;

    /* compiled from: Unknown */
    /* renamed from: com.fyusion.sdk.camera.impl.j$9 */
    static /* synthetic */ class AnonymousClass9 {
        static final /* synthetic */ int[] a = new int[CameraType.values().length];

        static {
            try {
                a[CameraType.FRONT_CAMERA.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                a[CameraType.BACK_CAMERA.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    /* compiled from: Unknown */
    private class a {
        final /* synthetic */ j a;
        private boolean b;

        a(j jVar, boolean z) {
            this.a = jVar;
            this.b = z;
        }

        void a(boolean z) {
            this.b = z;
        }

        boolean a() {
            return this.b;
        }
    }

    public j(Context context, o oVar, int i) {
        this.E = context;
        this.F = oVar;
        this.I = new ArrayList();
        this.I.add(this.ai.a());
        this.J = new ArrayList();
        this.N = (CameraManager) context.getSystemService("camera");
        this.p = false;
        this.r = new CameraStatus(this);
        this.r.setImageFormat(17);
        this.K = new HandlerThread("Camera2 Thread");
        this.K.start();
        this.L = new Handler(this.K.getLooper());
        super.a(5);
        e();
        this.ah = i;
    }

    private Surface A() {
        if (this.ab != null) {
            this.ab.close();
        }
        this.ab = ImageReader.newInstance(this.r.getPreviewWidth(), this.r.getPreviewHeight(), 1144402265, 5);
        return this.ab.getSurface();
    }

    private void B() {
        this.v = false;
        this.u = true;
        this.C.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, Integer.valueOf(3));
        this.C.set(CaptureRequest.CONTROL_AE_LOCK, Boolean.valueOf(true));
        this.C.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, Integer.valueOf(0));
        a(this.C, this.an);
        if (this.S != null) {
            new Thread(this) {
                final /* synthetic */ j a;

                {
                    this.a = r1;
                }

                public void run() {
                    super.run();
                    this.a.S.onExposureLock(true, "Exposure locked");
                }
            }.start();
        }
    }

    private int a(int i, int i2, int i3) {
        return Math.max(i2, Math.min(i3, i));
    }

    private synchronized int a(Builder builder, CaptureCallback captureCallback) {
        if (this.D != null) {
            try {
                a();
                return this.D.setRepeatingRequest(builder.build(), captureCallback, this.B);
            } catch (CameraAccessException e) {
                e.printStackTrace();
                synchronized (this.Y) {
                    this.Y.a(true);
                    this.Y.notifyAll();
                }
            }
        }
        return 0;
    }

    private Builder a(float f, float f2, Builder builder) {
        Integer num = (Integer) this.ae.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF);
        if (this.w) {
            return builder;
        }
        if (num != null && num.intValue() <= 0) {
            String str = "Setting Focus not supported for front camera";
            if (this.S != null) {
                this.S.onAutoFocus(false, str);
            }
            return builder;
        }
        int[] iArr = (int[]) this.ae.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
        boolean z = false;
        boolean z2 = false;
        for (int i = 0; i < iArr.length; i++) {
            if (iArr[i] == 1) {
                z2 = true;
            } else if (iArr[i] == 2) {
                z = true;
            }
        }
        if (z2) {
            this.C.set(CaptureRequest.CONTROL_AF_MODE, Integer.valueOf(1));
        } else if (z) {
            this.C.set(CaptureRequest.CONTROL_AF_MODE, Integer.valueOf(2));
        }
        if (z2 || z) {
            if (a(f, f2) == null) {
                return builder;
            }
            builder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[]{r0});
            builder.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(1));
            this.x = false;
            this.w = true;
        }
        return builder;
    }

    private Builder a(Builder builder) {
        builder.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(0));
        builder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, Integer.valueOf(0));
        builder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, Integer.valueOf(3));
        return builder;
    }

    private MeteringRectangle a(float f, float f2) {
        Rect rect = (Rect) this.ae.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        int width = rect.width();
        int height = rect.height();
        int a = this.F.a();
        a = (((a - ((int) f)) * height) - 16) / a;
        width = a(((((int) f2) * width) - 16) / this.F.b(), 0, width);
        height = a(a, 0, height);
        return new MeteringRectangle(new Rect(width, height, width + 16, height + 16), 1000);
    }

    private void a(Image image) {
        byte[] toNV21 = ColorHelper.toNV21(image);
        b bVar = new b();
        bVar.a = toNV21;
        bVar.b = (byte[]) this.Z.poll();
        long timestamp = image.getTimestamp() / 1000000;
        if (this.aj != 1) {
            if (this.W == 1) {
                this.ak = Math.max(SystemClock.elapsedRealtime() - timestamp, 0);
            }
            bVar.c = timestamp + this.ak;
        } else {
            bVar.c = timestamp;
        }
        this.G.a(bVar);
    }

    private boolean a(android.util.Size size) {
        return this.al != null && this.al.contains(size);
    }

    private boolean a(CameraType cameraType) throws FyuseCameraException {
        if (cameraType == CameraType.BACK_CAMERA && com.fyusion.sdk.camera.util.a.b()) {
            return true;
        }
        if (cameraType == CameraType.FRONT_CAMERA && com.fyusion.sdk.camera.util.a.c()) {
            return true;
        }
        throw new FyuseCameraException("Insufficient Support for Camera2. Requires Android API 21 or higher, and Camera2 device level support to be LIMITED or FULL ");
    }

    private synchronized int b(Builder builder) {
        if (this.D != null) {
            try {
                a();
                return this.D.setRepeatingRequest(builder.build(), null, this.B);
            } catch (CameraAccessException e) {
                e.printStackTrace();
                synchronized (this.Y) {
                    this.Y.a(true);
                    this.Y.notifyAll();
                }
            }
        }
        return 0;
    }

    private Builder b(float f, float f2, Builder builder) {
        if (isCustomExposureAdjustmentEnabled() && !this.i) {
            triggerCustomExposureAdjustment();
            builder.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(0));
            builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, Long.valueOf((long) (this.b * 1000000.0f)));
            builder.set(CaptureRequest.SENSOR_SENSITIVITY, Integer.valueOf(this.c));
        } else {
            Integer num = (Integer) this.ae.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE);
            this.u = false;
            this.v = false;
            if (num.intValue() > 0) {
                builder.set(CaptureRequest.CONTROL_AE_REGIONS, new MeteringRectangle[]{a(f, f2)});
            }
            builder.set(CaptureRequest.CONTROL_AE_LOCK, Boolean.valueOf(false));
            builder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, Integer.valueOf(1));
        }
        this.v = true;
        return builder;
    }

    private void b(int i) {
        Integer num = (Integer) this.ae.get(CameraCharacteristics.SENSOR_ORIENTATION);
        if (num.intValue() != 270) {
            this.r.setMirrorVertically(false);
        } else {
            this.r.setMirrorVertically(true);
        }
        this.r.setCameraRotation(i * 90);
        this.r.setCameraOrientation(num.intValue());
    }

    private void b(Image image) {
        Object obj = new byte[(((ImageFormat.getBitsPerPixel(image.getFormat()) / 8) * image.getHeight()) * image.getWidth())];
        image.getPlanes()[0].getBuffer().get(obj);
        if (!this.Z.offer(obj)) {
        }
    }

    private void b(boolean z) {
        if (this.s != null) {
            a(this.C);
            if (z) {
                y();
            }
            a(this.C, this.an);
        }
    }

    private void c(final boolean z) {
        this.w = false;
        if (this.S != null) {
            new Thread(this) {
                final /* synthetic */ j b;

                public void run() {
                    super.run();
                    this.b.S.onAutoFocus(z, !z ? "Unable to focus" : "Focus locked");
                }
            }.start();
        }
    }

    private boolean d() {
        return this.r.isBackCamera();
    }

    private void e() {
        try {
            for (String str : this.N.getCameraIdList()) {
                int intValue = ((Integer) this.N.getCameraCharacteristics(str).get(CameraCharacteristics.LENS_FACING)).intValue();
                if (this.O == null && intValue == 1) {
                    this.O = str;
                } else if (this.P == null && intValue == 0) {
                    this.P = str;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(21)
    private void f() throws FyuseCameraException {
        CamcorderProfile camcorderProfile;
        int i = com.fyusion.sdk.common.ext.j.b.width;
        int i2 = com.fyusion.sdk.common.ext.j.b.height;
        int i3 = com.fyusion.sdk.common.ext.j.c.width;
        int i4 = com.fyusion.sdk.common.ext.j.c.height;
        try {
            android.util.Size[] outputSizes = ((StreamConfigurationMap) this.N.getCameraCharacteristics(!d() ? this.P : this.O).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)).getOutputSizes(35);
            if (outputSizes != null) {
                for (android.util.Size size : outputSizes) {
                    if (size.getWidth() == i && size.getHeight() == i2) {
                        this.y = true;
                    } else if (size.getWidth() != i3) {
                        continue;
                    } else if (size.getHeight() == i4) {
                        this.z = true;
                    }
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        if (this.y) {
            camcorderProfile = CamcorderProfile.get(6);
            this.ac = new Size(i, i2);
            this.ad = new Size(i3, i4);
            this.r.setPreviewWidth(i);
            this.r.setPreviewHeight(i2);
        } else if (this.z) {
            camcorderProfile = CamcorderProfile.get(5);
            this.ac = new Size(i3, i4);
            this.ad = this.ac;
            this.r.setPreviewWidth(i3);
            this.r.setPreviewHeight(i4);
        } else {
            Log.e(a, "Camera does not support 720p or 1080p");
            throw new FyuseCameraException("Camera does not support high-res recording");
        }
        b.a(camcorderProfile);
    }

    private void g() {
        android.util.Size size = null;
        if (this.r.isRecording()) {
            throw new IllegalStateException("Cannot adjust resolution while recording");
        }
        int i = com.fyusion.sdk.common.ext.j.b.width;
        int i2 = com.fyusion.sdk.common.ext.j.b.height;
        int i3 = com.fyusion.sdk.common.ext.j.c.width;
        int i4 = com.fyusion.sdk.common.ext.j.c.height;
        CamcorderProfile camcorderProfile = CamcorderProfile.get(6);
        if (this.af != null) {
            size = (android.util.Size) this.af.get(FyuseCameraParameters.CAPTURE_RESOLUTION);
        }
        boolean a = a(size);
        if (size != null && a) {
            this.ac = new Size(size.getWidth(), size.getHeight());
            this.ad = this.ac;
            float f = (float) camcorderProfile.videoBitRate;
            camcorderProfile.videoBitRate = (int) ((((float) (size.getWidth() * size.getHeight())) / ((float) (com.fyusion.sdk.common.ext.j.b.width * com.fyusion.sdk.common.ext.j.b.height))) * f);
            camcorderProfile.videoFrameWidth = this.ac.width;
            camcorderProfile.videoFrameHeight = this.ac.height;
            if (size.getWidth() >= i && size.getHeight() >= i2) {
                this.r.setPreviewWidth(i);
                this.r.setPreviewHeight(i2);
            } else {
                this.r.setPreviewWidth(i3);
                this.r.setPreviewHeight(i4);
            }
        }
        b.a(camcorderProfile);
        if (this.s != null && this.aa != null && this.D != null) {
            Log.d(a, "Capture resolution update on imagereader to " + this.ac.width + "x" + this.ac.height);
            x();
            v();
        }
    }

    private void h() {
        Preset preset = (Preset) this.af.get(FyuseCameraParameters.CUSTOM_EXPOSURE_METERING_PRESET);
        if (preset == null) {
            MeteringArea[] meteringAreaArr = (MeteringArea[]) this.af.get(FyuseCameraParameters.CUSTOM_EXPOSURE_METERING_AREAS);
            if (meteringAreaArr != null) {
                setCustomExposureMetering(Arrays.asList(meteringAreaArr));
                return;
            }
            return;
        }
        setCustomExposureMetering(preset);
    }

    private void i() {
        Integer num = (Integer) this.af.get(FyuseCameraParameters.EXPOSURE_TYPE);
        if (num != null) {
            if (num != FyuseCameraParameters.EXPOSURE_TYPE_DEFAULT) {
                setEnableCustomExposureAdjustment(true);
            } else {
                setEnableCustomExposureAdjustment(false);
            }
        }
    }

    private void j() throws Exception {
        Display defaultDisplay = ((WindowManager) this.E.getSystemService("window")).getDefaultDisplay();
        android.util.Size d = this.F.d();
        android.util.Size a = this.F.a(defaultDisplay, this.ae, d.getWidth(), d.getHeight());
        Log.d(a, "Preview size: " + a);
        this.F.a(a);
        this.F.a(defaultDisplay, d.getWidth(), d.getHeight(), a);
    }

    private void k() throws CameraAccessException {
        b(((WindowManager) this.E.getSystemService("window")).getDefaultDisplay().getRotation());
        this.r.setPortraitMode(this.E.getResources().getConfiguration().orientation == 1);
    }

    private synchronized void l() {
        if (this.s != null) {
            g();
            if (this.af != null) {
                i();
                h();
                m();
            }
        }
    }

    private void m() {
        for (Key key : this.af.getKeys()) {
            if (key.getName().equals("com.fyusion.sdk.capture.saveallframes")) {
                this.ag = ((Boolean) this.af.get(key)).booleanValue();
            }
        }
    }

    private void n() throws FyuseCameraException {
        try {
            k();
            this.S.cameraReady();
        } catch (Throwable e) {
            throw new FyuseCameraException(e.getMessage(), e);
        }
    }

    private synchronized void o() {
        if (this.s != null) {
            x();
            if (this.s != null) {
                this.s.close();
                this.s = null;
                u();
            }
        }
    }

    private void p() throws Exception {
        if (this.aa != null) {
            this.aa.setOnImageAvailableListener(this.ap, this.L);
        }
    }

    private void q() throws Exception {
        if (this.ab != null) {
            this.ab.setOnImageAvailableListener(this.ao, this.L);
        }
    }

    private void r() {
        CaptureEvent captureEvent = new CaptureEvent(CaptureStatus.CAPTURE_STOPPED, System.currentTimeMillis());
        captureEvent.setRecordingStatus("0");
        captureEvent.setUid(this.G.b());
        if (this.H.c()) {
            captureEvent.setDescription("CAPTURE_STOPPED : Camera moved backwards");
        } else {
            captureEvent.setDescription("CAPTURE_STOPPED : Recording stopped");
        }
        for (CaptureEventListener onCapture : this.I) {
            onCapture.onCapture(captureEvent);
        }
    }

    private void s() {
        if (!isCustomExposureAdjustmentEnabled()) {
            this.C.removeTarget(this.Q);
            this.q = false;
        }
        if (this.p) {
            this.C.removeTarget(this.R);
        }
        b(true);
    }

    private void t() {
        this.A = new HandlerThread("FyuseCam2BG");
        this.A.start();
        this.B = new Handler(this.A.getLooper());
    }

    private void u() {
        if (this.A != null) {
            this.A.quitSafely();
            try {
                this.A.join();
                this.A = null;
                this.B = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void v() {
        if (this.s != null) {
            try {
                w();
                if (this.C != null) {
                    b(true);
                } else {
                    this.C = this.s.createCaptureRequest(1);
                    List arrayList = new ArrayList();
                    Surface c = this.F.c();
                    arrayList.add(c);
                    this.C.addTarget(c);
                    this.Q = z();
                    arrayList.add(this.Q);
                    if (isCustomExposureAdjustmentEnabled()) {
                        this.C.addTarget(this.Q);
                    }
                    if (this.p) {
                        this.R = A();
                        arrayList.add(this.R);
                    }
                    p();
                    q();
                    if (this.D == null) {
                        this.Y = new a(this, true);
                        this.s.createCaptureSession(arrayList, new CameraCaptureSession.StateCallback(this) {
                            final a a = this.b.Y;
                            final /* synthetic */ j b;

                            {
                                this.b = r2;
                            }

                            public void onClosed(CameraCaptureSession cameraCaptureSession) {
                                super.onClosed(cameraCaptureSession);
                            }

                            public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                                synchronized (this.a) {
                                    this.a.a(true);
                                    this.a.notifyAll();
                                }
                            }

                            public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                                synchronized (this.a) {
                                    this.a.a(false);
                                    this.b.D = cameraCaptureSession;
                                }
                                this.b.b(true);
                            }

                            public void onReady(CameraCaptureSession cameraCaptureSession) {
                                synchronized (this.a) {
                                    this.a.a(true);
                                    this.a.notifyAll();
                                }
                                super.onReady(cameraCaptureSession);
                            }
                        }, this.B);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void w() {
        if (this.D != null) {
            try {
                synchronized (this.Y) {
                    if (this.D != null) {
                        if (!this.Y.a()) {
                            this.D.stopRepeating();
                            this.Y.wait();
                            a();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.Y.a(true);
            }
        }
    }

    private void x() {
        if (this.D != null) {
            try {
                synchronized (this.Y) {
                    if (this.D != null) {
                        this.D.stopRepeating();
                        if (!this.Y.a()) {
                            this.Y.wait();
                        }
                        this.D.close();
                        this.D = null;
                        a();
                    }
                    this.C = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.Y.a(true);
            }
        }
    }

    private void y() {
        this.C.set(CaptureRequest.CONTROL_AF_MODE, Integer.valueOf(4));
        this.C.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(1));
    }

    private Surface z() {
        if (this.aa != null) {
            this.aa.close();
        }
        this.aa = ImageReader.newInstance(this.ac.width, this.ac.height, 35, 5);
        a();
        return this.aa.getSurface();
    }

    public void a(boolean z) {
        this.p = z;
    }

    public android.util.Size[] a(String str) {
        try {
            Object obj;
            android.util.Size[] outputSizes = ((StreamConfigurationMap) this.N.getCameraCharacteristics(str).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)).getOutputSizes(35);
            List asList = Arrays.asList((Object[]) FyuseCameraCapabilities.getInstance().get(FyuseCameraCapabilities.SUPPORTED_CAPTURE_RESOLUTIONS));
            this.al = new ArrayList();
            if (com.fyusion.sdk.common.a.a().c("camera", "4k")) {
                obj = null;
            } else {
                int i = 1;
            }
            for (android.util.Size size : outputSizes) {
                if (size.getHeight() >= 2160) {
                    if (obj == null) {
                    }
                }
                if (asList.contains(size)) {
                    this.al.add(size);
                }
            }
            return (android.util.Size[]) this.al.toArray(new android.util.Size[this.al.size()]);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addCaptureEventListener(CaptureEventListener captureEventListener) {
        this.I.add(captureEventListener);
    }

    public void addRecordingProgressListener(RecordingProgressListener recordingProgressListener) {
        this.J.add(recordingProgressListener);
    }

    protected void b() {
        if (!this.r.isRecording()) {
            a(this.C);
            this.C.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(0));
            this.C.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, Integer.valueOf(3));
            this.C.set(CaptureRequest.SENSOR_EXPOSURE_TIME, Long.valueOf((long) (this.b * 1000000.0f)));
            this.C.set(CaptureRequest.SENSOR_SENSITIVITY, Integer.valueOf(this.c));
            this.o = a(this.C, this.an);
        }
    }

    public Integer[] b(String str) {
        try {
            return !e.a(this.N.getCameraCharacteristics(str)) ? new Integer[]{FyuseCameraParameters.EXPOSURE_TYPE_DEFAULT} : new Integer[]{FyuseCameraParameters.EXPOSURE_TYPE_DEFAULT, FyuseCameraParameters.EXPOSURE_TYPE_MOTION_OPTIMIZED};
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Boolean c(String str) {
        try {
            return Boolean.valueOf(e.a(this.N.getCameraCharacteristics(str)));
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return Boolean.valueOf(false);
        }
    }

    protected void c() {
        this.v = false;
        this.u = false;
        if (this.S != null) {
            new Thread(this) {
                final /* synthetic */ j a;

                {
                    this.a = r1;
                }

                public void run() {
                    super.run();
                    this.a.S.onExposureLock(true, "Custom exposure locked");
                }
            }.start();
        }
        a();
    }

    public boolean d(String str) {
        try {
            return ((Boolean) this.N.getCameraCharacteristics(str).get(CameraCharacteristics.FLASH_INFO_AVAILABLE)).booleanValue();
        } catch (CameraAccessException e) {
            DLog.w(a, "isFlashSupported() :: false :: " + e.getMessage());
            return false;
        }
    }

    public FyuseCameraCapabilities getCameraCapabilities(CameraType cameraType) {
        e();
        FyuseCameraCapabilities.Builder builder = new FyuseCameraCapabilities.Builder();
        switch (AnonymousClass9.a[cameraType.ordinal()]) {
            case 1:
                builder.cameraId(this.P);
                break;
            case 2:
                builder.cameraId(this.O);
                break;
            default:
                throw new IllegalArgumentException("Illegal camera type supplied");
        }
        builder.provider(this);
        return builder.build();
    }

    public int getCameraIdForType(CameraType cameraType) {
        e();
        return cameraType != CameraType.BACK_CAMERA ? cameraType != CameraType.FRONT_CAMERA ? -1 : new Integer(this.P).intValue() : new Integer(this.O).intValue();
    }

    public int getCameraVersion() {
        return this.ah;
    }

    public CustomExposureControl<?> getManualExposureController() {
        return (isCustomExposureAdjustmentSupported() && ((Boolean) FyuseCameraCapabilities.getInstance().get(FyuseCameraCapabilities.MANUAL_EXPOSURE_CONTROL)).booleanValue()) ? new CustomExposureControl(this) : null;
    }

    public synchronized void open(CameraType cameraType, FyuseCameraCallback fyuseCameraCallback) throws FyuseCameraException {
        this.M = cameraType;
        if (a(cameraType)) {
            open(fyuseCameraCallback);
            android.util.Size d = this.F.d();
            com.fyusion.sdk.camera.b.a aVar = this.ai;
            com.fyusion.sdk.camera.b.a.b(cameraType, d.getWidth(), d.getHeight());
        }
    }

    public synchronized void open(FyuseCameraCallback fyuseCameraCallback) throws FyuseCameraException {
        this.S = fyuseCameraCallback;
        o();
        if (this.M != CameraType.FRONT_CAMERA) {
            this.r.setBackCamera(true);
        } else {
            this.r.setBackCamera(false);
        }
        try {
            String str = !d() ? this.P : this.O;
            f();
            this.ae = this.N.getCameraCharacteristics(str);
            super.b(this.ae);
            j();
            t();
            if (ContextCompat.checkSelfPermission(this.E, "android.permission.CAMERA") != 0) {
                DLog.w(a, "Failed to get CAMERA permission");
                throw new FyuseCameraException("Failed to get CAMERA permission");
            } else if (ContextCompat.checkSelfPermission(this.E, "android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
                this.N.openCamera(str, this.am, null);
            } else {
                DLog.w(a, "Failed to get WRITE_EXTERNAL_STORAGE permission");
                throw new FyuseCameraException("Failed to get WRITE_EXTERNAL_STORAGE permission");
            }
        } catch (FyuseCameraException e) {
            DLog.w(a, "Failed to open camera " + e.getMessage());
            throw e;
        } catch (Exception e2) {
            String message = e2.getMessage();
            DLog.w(a, "Failed to open camera " + e2.getMessage());
            throw new FyuseCameraException(message);
        }
    }

    public synchronized void release() {
        o();
        if (this.H != null) {
            this.H.b();
        }
        com.fyusion.sdk.camera.b.a aVar = this.ai;
        com.fyusion.sdk.camera.b.a.b(this.M);
    }

    public void removeCaptureEventListener(CaptureEventListener captureEventListener) {
        this.I.remove(captureEventListener);
    }

    public void removeRecordingProgressListener(RecordingProgressListener recordingProgressListener) {
        this.J.remove(recordingProgressListener);
    }

    public void setCameraParameters(FyuseCameraParameters fyuseCameraParameters) throws IllegalStateException {
        this.af = fyuseCameraParameters;
        l();
    }

    public void setEnableCustomExposureAdjustment(boolean z) {
        super.setEnableCustomExposureAdjustment(z);
        if (this.D != null && this.C != null && z && !this.q) {
            this.C.addTarget(this.Q);
            try {
                p();
                this.q = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setExposure(float f, float f2) {
        if (!this.r.isRecording()) {
            this.t.lock();
            try {
                this.o = a(b(f, f2, a(this.C)), this.an);
            } finally {
                this.t.unlock();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setExposureAndFocus(float f, float f2) {
        if (!this.r.isRecording()) {
            this.t.lock();
            try {
                this.o = a(a(f, f2, b(f, f2, a(this.C))), this.an);
            } finally {
                this.t.unlock();
            }
        }
    }

    public void setFlash(boolean z) {
        if (!this.r.isRecording()) {
            if (z) {
                this.C.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(1));
                this.C.set(CaptureRequest.FLASH_MODE, Integer.valueOf(2));
            } else {
                this.C.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(0));
                this.C.set(CaptureRequest.FLASH_MODE, Integer.valueOf(0));
            }
            b(this.C);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setFocus(float f, float f2) {
        if (!this.r.isRecording()) {
            this.t.lock();
            try {
                if (this.C != null) {
                    a(a(f, f2, a(this.C)), this.an);
                }
            } finally {
                this.t.unlock();
            }
        }
    }

    public void setTargetRotation(int i, float[] fArr, RotationDirection rotationDirection) {
        this.T = i;
        this.V = fArr;
        this.U = rotationDirection;
    }

    public synchronized void startRecording(MotionHintsListener motionHintsListener, File file) throws FyuseCameraException {
        if (this.D != null) {
            this.ak = 0;
            this.aj = ((Integer) this.ae.get(CameraCharacteristics.SENSOR_INFO_TIMESTAMP_SOURCE)).intValue();
            if (this.aj == 1) {
                DLog.i(a, "Camera2 Timestamp Source = SENSOR_INFO_TIMESTAMP_SOURCE_REALTIME");
            } else if (this.aj == 0) {
                DLog.i(a, "Camera2 Timestamp Source = SENSOR_INFO_TIMESTAMP_SOURCE_UNKNOWN");
            }
            if (file.getParentFile().exists()) {
                this.H = new c(this.r, this.ac, this.ad);
                this.H.a(this.I);
                this.H.b(this.J);
                try {
                    this.G = new a(this.H, this.r, file);
                    this.G.c();
                    this.W = 0;
                    this.X = 0;
                    this.H.a(motionHintsListener);
                    this.H.b(d());
                    this.H.a(this.ag);
                    p();
                    q();
                    this.G.a(this.T, this.V, this.U);
                    if (!isCustomExposureAdjustmentEnabled()) {
                        this.C.addTarget(this.Q);
                    }
                    if (this.p) {
                        this.C.addTarget(this.R);
                    }
                    this.t.lock();
                    this.C.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(0));
                    if (this.u) {
                        if (isCustomExposureAdjustmentEnabled()) {
                            this.C.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(0));
                            this.C.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, Integer.valueOf(3));
                            this.C.set(CaptureRequest.SENSOR_EXPOSURE_TIME, Long.valueOf((long) (this.b * 1000000.0f)));
                            this.C.set(CaptureRequest.SENSOR_SENSITIVITY, Integer.valueOf(this.c));
                        } else {
                            this.C.set(CaptureRequest.CONTROL_AE_LOCK, Boolean.valueOf(true));
                        }
                    }
                    this.r.setRecording(true);
                    a();
                    a(this.C, this.an);
                    this.t.unlock();
                    File a = this.G.a();
                    CaptureEvent captureEvent = new CaptureEvent(CaptureStatus.CAPTURE_IN_PROGRESS, System.currentTimeMillis(), a.getAbsolutePath(), this.M);
                    captureEvent.setDescription("Capture Started, In Progress");
                    android.util.Size d = this.F.d();
                    com.fyusion.sdk.camera.b.a aVar = this.ai;
                    com.fyusion.sdk.camera.b.a.b(this.G.b(), this.M, d.getWidth(), d.getHeight(), this.ac.width, this.ac.height);
                    for (CaptureEventListener onCapture : this.I) {
                        onCapture.onCapture(captureEvent);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    throw new FyuseCameraException(e.getMessage(), e);
                } catch (IOException e2) {
                    e2.printStackTrace();
                    throw new FyuseCameraException(e2.getMessage());
                } catch (Throwable th) {
                    this.t.unlock();
                }
            }
            throw new FyuseCameraException("Directory path does not exist!");
        }
        throw new FyuseCameraException("Camera not initialized yet");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void stopRecording() throws FyuseCameraException {
        if (this.r.isRecording()) {
            if (this.D != null) {
                this.u = false;
                try {
                    synchronized (this.Y) {
                        if (!this.Y.a()) {
                            this.Y.wait();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.r.setRecording(false);
                r();
                if (!isCustomExposureAdjustmentEnabled()) {
                    this.aa.setOnImageAvailableListener(null, null);
                }
                this.G.d();
                s();
            }
        }
    }

    public void takeSnapShot(SnapShotCallback snapShotCallback) {
        if (this.r.isRecording()) {
            this.H.a(snapShotCallback);
        }
    }
}

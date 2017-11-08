package com.fyusion.sdk.camera.impl;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.media.CamcorderProfile;
import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.Log;
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
import com.fyusion.sdk.camera.FyuseCameraCapabilities.Builder;
import com.fyusion.sdk.camera.FyuseCameraException;
import com.fyusion.sdk.camera.FyuseCameraParameters;
import com.fyusion.sdk.camera.RecordingProgressListener;
import com.fyusion.sdk.camera.SnapShotCallback;
import com.fyusion.sdk.camera.b.a;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.common.ext.Key;
import com.fyusion.sdk.common.ext.Size;
import com.fyusion.sdk.common.ext.j;
import com.fyusion.sdk.common.ext.util.b;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* compiled from: Unknown */
public final class i implements FyuseCamera, d {
    private static final String a = i.class.getSimpleName();
    private int A;
    private a B = new a();
    private boolean C;
    private PreviewCallback D = new PreviewCallback(this) {
        final /* synthetic */ i a;

        {
            this.a = r1;
        }

        public void onPreviewFrame(byte[] bArr, Camera camera) {
            long elapsedRealtime = SystemClock.elapsedRealtime();
            if (bArr != null && bArr.length != 0) {
                this.a.E = this.a.E + 1;
                b bVar = new b();
                Object obj = new byte[bArr.length];
                System.arraycopy(bArr, 0, obj, 0, bArr.length);
                bVar.a = obj;
                bVar.c = elapsedRealtime;
                this.a.g.a(bVar);
                camera.addCallbackBuffer(bArr);
                if (!(camera == null || this.a.i.isRecording())) {
                    camera.setPreviewCallback(null);
                    this.a.g.d();
                }
            }
        }
    };
    private int E = 0;
    private CameraType b = CameraType.BACK_CAMERA;
    private Camera c;
    private FyuseCameraCallback d;
    private Context e;
    private o f;
    private a g;
    private c h;
    private CameraStatus i;
    private List<CaptureEventListener> j = new ArrayList();
    private List<RecordingProgressListener> k = new ArrayList();
    private int l = -1;
    private int m = -1;
    private Size n = new Size(0, 0);
    private Size o = new Size(0, 0);
    private int p;
    private RotationDirection q;
    private float[] r;
    private Size s = new Size(0, 0);
    private Size t = new Size(0, 0);
    private boolean u = false;
    private int v;
    private File w = null;
    private CamcorderProfile x = null;
    private CamcorderProfile y = null;
    private FyuseCameraParameters z;

    /* compiled from: Unknown */
    /* renamed from: com.fyusion.sdk.camera.impl.i$5 */
    static /* synthetic */ class AnonymousClass5 {
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

    public i(Context context, int i) {
        this.e = context;
        this.j.add(this.B.a());
        this.i = new CameraStatus(this);
        this.u = true;
        this.A = i;
    }

    public i(Context context, o oVar, int i) {
        this.e = context;
        this.f = oVar;
        this.j.add(this.B.a());
        this.i = new CameraStatus(this);
        this.A = i;
    }

    private void a() {
        for (Key key : this.z.getKeys()) {
            if (key.getName().equals("com.fyusion.sdk.capture.saveallframes")) {
                this.C = ((Boolean) this.z.get(key)).booleanValue();
            }
        }
    }

    private void a(int i, int i2) {
        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(i, cameraInfo);
        int i3 = i2 * 90;
        if (cameraInfo.orientation != 270) {
            this.i.setMirrorVertically(false);
        } else {
            this.i.setMirrorVertically(true);
        }
        this.i.setCameraOrientation(cameraInfo.orientation);
        this.i.setCameraRotation(i3);
        this.c.setDisplayOrientation(cameraInfo.facing != 1 ? ((cameraInfo.orientation - i3) + 360) % 360 : (360 - ((cameraInfo.orientation + i3) % 360)) % 360);
    }

    private void a(Parameters parameters) {
        int[] iArr;
        int i;
        int i2 = 0;
        int[] iArr2 = new int[]{0, 0};
        Iterator it = parameters.getSupportedPreviewFpsRange().iterator();
        while (true) {
            iArr = iArr2;
            if (!it.hasNext()) {
                break;
            }
            iArr2 = (int[]) it.next();
            if (iArr2[1] < iArr[1]) {
                iArr2 = iArr;
            } else if (iArr2[1] <= iArr[1] && iArr2[0] <= iArr[0]) {
                iArr2 = iArr;
            }
        }
        parameters.setPreviewFpsRange(iArr[0], iArr[1]);
        int g = g();
        this.i.setImageFormat(g);
        parameters.setPreviewFormat(g);
        if (b()) {
            i = this.o.width;
            g = this.o.height;
        } else {
            i = this.n.width;
            g = this.n.height;
        }
        int i3 = i;
        i = g;
        int i4 = 0;
        for (Camera.Size size : parameters.getSupportedPictureSizes()) {
            if (size.width > i4 && Math.abs(1.7777777777777777d - (((double) size.width) / ((double) size.height))) < 0.01d) {
                i4 = size.width;
                i2 = size.height;
            }
            i4 = i4;
            i2 = i2;
        }
        if (i4 <= 0) {
            parameters.setPictureSize(i3, i);
        } else {
            parameters.setPictureSize(i4, i2);
        }
        this.c.setParameters(parameters);
    }

    private void a(boolean z, CamcorderProfile camcorderProfile, Size size) {
        Size size2;
        int i;
        Size size3;
        if (z) {
            b.a(camcorderProfile);
            size3 = (size.height == 1080 || size.height == 1088) ? j.b : j.c;
            DLog.d(a, "Back camera processedSize : width : " + size3.width + ", height : " + size3.height);
            this.s.width = size.width;
            this.s.height = size.height;
            this.t.width = size3.width;
            size2 = this.t;
            i = size3.height;
        } else if (!z) {
            b.a(camcorderProfile);
            size3 = (size.height == 1080 || size.height == 1088) ? j.b : j.c;
            DLog.d(a, "Front camera processedSize : width : " + size3.width + ", height : " + size3.height);
            this.s.width = size.width;
            this.s.height = size.height;
            this.t.width = size3.width;
            size2 = this.t;
            i = size3.height;
        } else {
            return;
        }
        size2.height = i;
    }

    private boolean a(CamcorderProfile camcorderProfile) {
        if (camcorderProfile != null) {
            int i = camcorderProfile.videoFrameWidth;
            int i2 = camcorderProfile.videoFrameHeight;
            for (Camera.Size size : this.c.getParameters().getSupportedPreviewSizes()) {
                if (size.width == i && size.height == i2) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean a(List<String> list, String str) {
        for (int i = 0; i < list.size(); i++) {
            if (str.equals(list.get(i))) {
                return true;
            }
        }
        return false;
    }

    private void b(Parameters parameters) {
        try {
            parameters.set("fast-fps-mode", 1);
            parameters.set("instant-aec", "1");
            parameters.set("zsl", "on");
            this.c.setParameters(parameters);
        } catch (RuntimeException e) {
            e.printStackTrace();
            DLog.e(a, "setParameters failed: unsupported configuration.");
        }
    }

    private boolean b() {
        return this.i.isBackCamera();
    }

    private void c() {
        if (this.l <= -1 && this.m <= -1) {
            CameraInfo cameraInfo = new CameraInfo();
            int numberOfCameras = Camera.getNumberOfCameras();
            for (int i = 0; i < numberOfCameras; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (this.l == -1 && cameraInfo.facing == 0) {
                    this.l = i;
                }
                if (this.m == -1 && cameraInfo.facing == 1) {
                    this.m = i;
                }
            }
        }
    }

    private void d() {
        Size size;
        int i;
        if (this.m > -1) {
            this.x = g.a(this.m, 6);
            if (this.x != null && a(this.x)) {
                this.n.width = this.x.videoFrameWidth;
                size = this.n;
                i = this.x.videoFrameHeight;
                size.height = i;
            } else {
                this.x = g.a(this.m, 5);
                if (this.x != null && a(this.x)) {
                    this.n.width = this.x.videoFrameWidth;
                    size = this.n;
                    i = this.x.videoFrameHeight;
                    size.height = i;
                }
            }
            if (this.x != null) {
                DLog.d(a, "Front camera profile: " + this.x.videoFrameWidth + ", " + this.x.videoFrameHeight);
            }
        }
        if (this.l > -1) {
            this.y = g.a(this.l, 6);
            if (this.y != null && a(this.y)) {
                this.o.width = this.y.videoFrameWidth;
                size = this.o;
                i = this.y.videoFrameHeight;
                size.height = i;
            } else {
                this.y = g.a(this.l, 5);
                if (this.y != null && a(this.y)) {
                    this.o.width = this.y.videoFrameWidth;
                    size = this.o;
                    i = this.y.videoFrameHeight;
                    size.height = i;
                }
            }
            if (this.y != null) {
                DLog.d(a, "Back camera profile: " + this.y.videoFrameWidth + ", " + this.y.videoFrameHeight);
            }
        }
        if (b()) {
            a(b(), this.y, this.o);
        } else if (!b()) {
            a(b(), this.x, this.n);
        }
    }

    private void e() {
        int rotation = ((WindowManager) this.e.getSystemService("window")).getDefaultDisplay().getRotation();
        if (b()) {
            a(this.l, rotation);
        } else {
            a(this.m, rotation);
        }
        this.i.setPortraitMode(this.e.getResources().getConfiguration().orientation == 1);
    }

    private void f() {
        Parameters parameters = this.c.getParameters();
        Camera.Size preferredPreviewSizeForVideo = parameters.getPreferredPreviewSizeForVideo();
        CamcorderProfile a = b.a();
        if (a != null) {
            int i = a.videoFrameWidth;
            int i2 = a.videoFrameHeight;
            for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
                if (size.width == i && size.height == i2) {
                    parameters.setPreviewSize(i, i2);
                    this.c.setParameters(parameters);
                    this.i.setPreviewWidth(i);
                    this.i.setPreviewHeight(i2);
                    return;
                }
            }
        }
        parameters.setPreviewSize(preferredPreviewSizeForVideo.width, preferredPreviewSizeForVideo.height);
        this.s.width = preferredPreviewSizeForVideo.width;
        this.s.height = preferredPreviewSizeForVideo.height;
        this.t.width = preferredPreviewSizeForVideo.width;
        this.t.height = preferredPreviewSizeForVideo.height;
        this.i.setPreviewWidth(preferredPreviewSizeForVideo.width);
        this.i.setPreviewHeight(preferredPreviewSizeForVideo.height);
        this.i.setPreviewFPS(parameters.getPreviewFrameRate());
        this.c.setParameters(parameters);
    }

    private int g() {
        CodecCapabilities capabilitiesForType;
        MediaCodecInfo a = com.fyusion.sdk.camera.c.a.a("video/avc");
        CodecCapabilities codecCapabilities = new CodecCapabilities();
        try {
            capabilitiesForType = a.getCapabilitiesForType("video/avc");
        } catch (IllegalArgumentException e) {
            DLog.d("getImageFormat", e.getMessage());
            capabilitiesForType = codecCapabilities;
        }
        for (int i : capabilitiesForType.colorFormats) {
            if (i == 21) {
                return 17;
            }
        }
        return 842094169;
    }

    private void h() {
        if (this.c != null) {
            synchronized (this) {
                this.c.release();
                this.c = null;
            }
        }
    }

    private void i() {
        try {
            this.c.autoFocus(new AutoFocusCallback(this) {
                final /* synthetic */ i a;

                {
                    this.a = r1;
                }

                public void onAutoFocus(boolean z, Camera camera) {
                    this.a.c.cancelAutoFocus();
                }
            });
        } catch (RuntimeException e) {
            e.printStackTrace();
            DLog.e(a, "Triggering auto-focus failed.");
        }
    }

    private void j() throws IOException {
        boolean z = false;
        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(this.v, cameraInfo);
        CameraStatus cameraStatus = this.i;
        if (cameraInfo.facing == 0) {
            z = true;
        }
        cameraStatus.setBackCamera(z);
        c();
        e();
        Parameters parameters = this.c.getParameters();
        this.i.setPreviewFPS(parameters.getPreviewFrameRate());
        this.i.setImageFormat(g());
        if (g() == 842094169) {
            parameters.setPreviewFormat(842094169);
            this.c.setParameters(parameters);
            this.c.stopPreview();
            this.c.startPreview();
        }
        Camera.Size previewSize = parameters.getPreviewSize();
        this.i.setPreviewWidth(previewSize.width);
        this.i.setPreviewHeight(previewSize.height);
        this.s.width = previewSize.width;
        this.s.height = previewSize.height;
        this.t.width = previewSize.width;
        this.t.height = previewSize.height;
        this.h = new c(this.i, this.s, this.t);
        this.h.a(this.j);
        this.h.b(this.k);
        this.g = new a(this.h, this.i, this.w);
    }

    private void k() {
        CaptureEvent captureEvent = new CaptureEvent(CaptureStatus.CAPTURE_STOPPED, System.currentTimeMillis());
        captureEvent.setRecordingStatus("0");
        captureEvent.setUid(this.g.b());
        if (this.h.c()) {
            captureEvent.setDescription("CAPTURE_STOPPED : Camera moved backwards");
        } else {
            captureEvent.setDescription("CAPTURE_STOPPED : Recording stopped");
        }
        for (CaptureEventListener onCapture : this.j) {
            onCapture.onCapture(captureEvent);
        }
    }

    public void a(Camera camera, int i) {
        this.c = camera;
        this.v = i;
        this.u = true;
    }

    @RequiresApi(21)
    public android.util.Size[] a(String str) {
        Log.w(a, "Currently only supported in FyusionCamera2");
        return null;
    }

    public void addCaptureEventListener(CaptureEventListener captureEventListener) {
        this.j.add(captureEventListener);
    }

    public void addRecordingProgressListener(RecordingProgressListener recordingProgressListener) {
        this.k.add(recordingProgressListener);
    }

    @RequiresApi(21)
    public Integer[] b(String str) {
        Log.w(a, "Currently only supported in FyusionCamera2");
        return null;
    }

    @RequiresApi(21)
    public Boolean c(String str) {
        Log.w(a, "Currently only supported in FyusionCamera2");
        return null;
    }

    public boolean d(String str) {
        boolean z;
        if (this.c == null) {
            z = true;
        } else {
            List<String> supportedFlashModes = this.c.getParameters().getSupportedFlashModes();
            if (supportedFlashModes != null) {
                for (String equals : supportedFlashModes) {
                    if (equals.equals("torch")) {
                        return true;
                    }
                }
            }
            z = false;
        }
        return z;
    }

    public FyuseCameraCapabilities getCameraCapabilities(CameraType cameraType) {
        c();
        Builder builder = new Builder();
        switch (AnonymousClass5.a[cameraType.ordinal()]) {
            case 1:
                builder.cameraId(String.valueOf(this.m));
                break;
            case 2:
                builder.cameraId(String.valueOf(this.m));
                break;
            default:
                throw new IllegalArgumentException("Illegal camera type supplied");
        }
        builder.provider(this);
        return builder.build();
    }

    public int getCameraIdForType(CameraType cameraType) {
        c();
        return cameraType != CameraType.BACK_CAMERA ? cameraType != CameraType.FRONT_CAMERA ? -1 : this.m : this.l;
    }

    public int getCameraVersion() {
        return this.A;
    }

    public CustomExposureControl<?> getManualExposureController() {
        return null;
    }

    public synchronized void open(CameraType cameraType, FyuseCameraCallback fyuseCameraCallback) throws FyuseCameraException {
        if (this.u) {
            throw new FyuseCameraException("OEM Camera is already registered. Call release() first");
        }
        this.b = cameraType;
        open(fyuseCameraCallback);
        Camera.Size previewSize = this.c.getParameters().getPreviewSize();
        a aVar = this.B;
        a.a(cameraType, previewSize.width, previewSize.height);
    }

    public synchronized void open(FyuseCameraCallback fyuseCameraCallback) throws FyuseCameraException {
        if (this.u) {
            throw new FyuseCameraException("OEM Camera is already registered. Call release() first");
        }
        h();
        if (this.b != CameraType.FRONT_CAMERA) {
            this.i.setBackCamera(true);
        } else {
            this.i.setBackCamera(false);
        }
        this.d = fyuseCameraCallback;
        c();
        if (ContextCompat.checkSelfPermission(this.e, "android.permission.CAMERA") != 0) {
            DLog.w(a, "Failed to get CAMERA permission");
            throw new FyuseCameraException("Failed to get CAMERA permission");
        } else if (ContextCompat.checkSelfPermission(this.e, "android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
            try {
                this.c = Camera.open(!b() ? this.m : this.l);
                d();
                e();
                f();
                Parameters parameters = this.c.getParameters();
                a(parameters);
                b(parameters);
                this.f.a(this.c);
                this.c.startPreview();
                if (parameters.getMaxNumFocusAreas() > 0) {
                    i();
                }
                fyuseCameraCallback.cameraReady();
            } catch (Throwable e) {
                throw new FyuseCameraException("Camera can not used now", e);
            } catch (Exception e2) {
                throw new FyuseCameraException(e2.getMessage());
            }
        } else {
            DLog.w(a, "Failed to get WRITE_EXTERNAL_STORAGE permission");
            throw new FyuseCameraException("Failed to get WRITE_EXTERNAL_STORAGE permission");
        }
    }

    public synchronized void release() {
        if (this.c != null) {
            this.c.stopPreview();
            h();
        }
        if (this.h != null) {
            this.h.b();
        }
        this.u = false;
        a aVar = this.B;
        a.a(this.b);
    }

    public void removeCaptureEventListener(CaptureEventListener captureEventListener) {
        this.j.remove(captureEventListener);
    }

    public void removeRecordingProgressListener(RecordingProgressListener recordingProgressListener) {
        this.k.remove(recordingProgressListener);
    }

    public void setCameraParameters(FyuseCameraParameters fyuseCameraParameters) {
        this.z = fyuseCameraParameters;
        if (this.z != null) {
            a();
        }
    }

    public synchronized void setExposure(float f, float f2) {
        DLog.w(a, "setExposure() Unsupported for  - FYUSION_CAMERA_1 - version of fyusion camera");
    }

    public synchronized void setExposureAndFocus(float f, float f2) {
        DLog.w(a, "setExposureAndFocus() Unsupported for  - FYUSION_CAMERA_1 - version of fyusion camera");
    }

    public void setFlash(boolean z) {
        if (!this.i.isRecording()) {
            Parameters parameters = this.c.getParameters();
            if (z) {
                List supportedFlashModes = parameters.getSupportedFlashModes();
                if (supportedFlashModes != null && supportedFlashModes.contains("torch")) {
                    parameters.setFlashMode("torch");
                }
            } else {
                parameters.setFlashMode("off");
            }
            this.c.setParameters(parameters);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setFocus(float f, float f2) {
        if (!this.i.isRecording()) {
            int i = (int) f;
            int i2 = (int) f2;
            Rect rect = new Rect(i - 50, i2 - 50, i + 50, i2 + 50);
            Parameters parameters = this.c.getParameters();
            if (parameters.getMaxNumFocusAreas() > 0) {
                Rect rect2 = new Rect();
                this.i.isPortraitMode();
                i = this.f.a();
                i2 = this.f.b();
                int max = Math.max(((rect.left * 2000) / i) - 1000, -1000);
                int max2 = Math.max(((rect.top * 2000) / i2) - 1000, -1000);
                int min = Math.min(((rect.right * 2000) / i) - 1000, 1000);
                i = Math.min(((rect.bottom * 2000) / i2) - 1000, 1000);
                i2 = this.i.getDisplayOrientation();
                int i3;
                if (i2 == 90) {
                    i2 = -max;
                    i3 = max2;
                    max2 = -min;
                    min = i3;
                } else if (i2 != 180) {
                    i2 = min;
                    min = max2;
                    max2 = max;
                } else {
                    i2 = -max2;
                    max2 = -i;
                    i3 = i2;
                    i2 = -max;
                    i = i3;
                    int i4 = max2;
                    max2 = -min;
                    min = i4;
                }
                rect2.set(max2, min, i2, i);
                List arrayList;
                if (a(parameters.getSupportedFocusModes(), "auto")) {
                    parameters.setFocusMode("auto");
                    arrayList = new ArrayList();
                    arrayList.add(new Area(rect2, 1000));
                    parameters.setFocusAreas(arrayList);
                    if (parameters.getMaxNumMeteringAreas() != 0) {
                        parameters.setMeteringAreas(arrayList);
                    }
                    try {
                        this.c.setParameters(parameters);
                        this.c.autoFocus(new AutoFocusCallback(this) {
                            final /* synthetic */ i a;

                            {
                                this.a = r1;
                            }

                            public void onAutoFocus(boolean z, Camera camera) {
                                String str = !z ? "Failed to set focus" : "Focus set successfully";
                                if (this.a.d != null) {
                                    this.a.d.onAutoFocus(z, str);
                                }
                            }
                        });
                    } catch (Exception e) {
                    }
                } else if (a(parameters.getSupportedFocusModes(), "macro")) {
                    parameters.setFocusMode("macro");
                    arrayList = new ArrayList();
                    arrayList.add(new Area(rect2, 1000));
                    parameters.setFocusAreas(arrayList);
                    try {
                        this.c.setParameters(parameters);
                        this.c.autoFocus(new AutoFocusCallback(this) {
                            final /* synthetic */ i a;

                            {
                                this.a = r1;
                            }

                            public void onAutoFocus(boolean z, Camera camera) {
                                String str = !z ? "Failed to set focus" : "Focus set successfully";
                                if (this.a.d != null) {
                                    this.a.d.onAutoFocus(z, str);
                                }
                            }
                        });
                    } catch (Exception e2) {
                    }
                }
            } else {
                String str = "Setting Focus not supported for front camera";
                if (this.d != null) {
                    this.d.onAutoFocus(false, str);
                }
            }
        }
    }

    public void setTargetRotation(int i, float[] fArr, RotationDirection rotationDirection) {
        this.p = i;
        this.r = fArr;
        this.q = rotationDirection;
    }

    public synchronized void startRecording(com.fyusion.sdk.camera.MotionHintsListener r9, java.io.File r10) throws com.fyusion.sdk.camera.FyuseCameraException {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:com.fyusion.sdk.camera.impl.i.startRecording(com.fyusion.sdk.camera.MotionHintsListener, java.io.File):void. bs: [B:7:0x0012, B:25:0x00eb]
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:86)
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r8 = this;
        r0 = 0;
        monitor-enter(r8);
        r1 = r10.getParentFile();	 Catch:{ all -> 0x00e8 }
        r1 = r1.exists();	 Catch:{ all -> 0x00e8 }
        if (r1 == 0) goto L_0x00df;	 Catch:{ all -> 0x00e8 }
    L_0x000c:
        r8.w = r10;	 Catch:{ all -> 0x00e8 }
        r1 = r8.u;	 Catch:{ all -> 0x00e8 }
        if (r1 != 0) goto L_0x00eb;
    L_0x0012:
        r1 = new com.fyusion.sdk.camera.impl.c;	 Catch:{ IOException -> 0x00fe }
        r2 = r8.i;	 Catch:{ IOException -> 0x00fe }
        r3 = r8.s;	 Catch:{ IOException -> 0x00fe }
        r4 = r8.t;	 Catch:{ IOException -> 0x00fe }
        r1.<init>(r2, r3, r4);	 Catch:{ IOException -> 0x00fe }
        r8.h = r1;	 Catch:{ IOException -> 0x00fe }
        r1 = r8.h;	 Catch:{ IOException -> 0x00fe }
        r2 = r8.C;	 Catch:{ IOException -> 0x00fe }
        r1.a(r2);	 Catch:{ IOException -> 0x00fe }
        r1 = r8.h;	 Catch:{ IOException -> 0x00fe }
        r2 = r8.j;	 Catch:{ IOException -> 0x00fe }
        r1.a(r2);	 Catch:{ IOException -> 0x00fe }
        r1 = r8.h;	 Catch:{ IOException -> 0x00fe }
        r2 = r8.k;	 Catch:{ IOException -> 0x00fe }
        r1.b(r2);	 Catch:{ IOException -> 0x00fe }
        r1 = new com.fyusion.sdk.camera.impl.a;	 Catch:{ IOException -> 0x00fe }
        r2 = r8.h;	 Catch:{ IOException -> 0x00fe }
        r3 = r8.i;	 Catch:{ IOException -> 0x00fe }
        r1.<init>(r2, r3, r10);	 Catch:{ IOException -> 0x00fe }
        r8.g = r1;	 Catch:{ IOException -> 0x00fe }
    L_0x003f:
        r1 = r8.g;	 Catch:{ all -> 0x00e8 }
        r1.c();	 Catch:{ all -> 0x00e8 }
        r1 = r8.c;	 Catch:{ all -> 0x00e8 }
        r2 = 0;	 Catch:{ all -> 0x00e8 }
        r1.setPreviewCallbackWithBuffer(r2);	 Catch:{ all -> 0x00e8 }
        r1 = r8.h;	 Catch:{ all -> 0x00e8 }
        r1.a(r9);	 Catch:{ all -> 0x00e8 }
        r1 = r8.h;	 Catch:{ all -> 0x00e8 }
        r2 = r8.b();	 Catch:{ all -> 0x00e8 }
        r1.b(r2);	 Catch:{ all -> 0x00e8 }
        r1 = r8.c;	 Catch:{ all -> 0x00e8 }
        r1 = r1.getParameters();	 Catch:{ all -> 0x00e8 }
        r2 = r8.i;	 Catch:{ all -> 0x00e8 }
        r2 = r2.getPreviewWidth();	 Catch:{ all -> 0x00e8 }
        r3 = r8.i;	 Catch:{ all -> 0x00e8 }
        r3 = r3.getPreviewHeight();	 Catch:{ all -> 0x00e8 }
        r4 = r8.g;	 Catch:{ all -> 0x00e8 }
        r5 = r8.p;	 Catch:{ all -> 0x00e8 }
        r6 = r8.r;	 Catch:{ all -> 0x00e8 }
        r7 = r8.q;	 Catch:{ all -> 0x00e8 }
        r4.a(r5, r6, r7);	 Catch:{ all -> 0x00e8 }
        r2 = r2 * r3;	 Catch:{ all -> 0x00e8 }
        r1 = r1.getPreviewFormat();	 Catch:{ all -> 0x00e8 }
        r1 = android.graphics.ImageFormat.getBitsPerPixel(r1);	 Catch:{ all -> 0x00e8 }
        r1 = r1 * r2;	 Catch:{ all -> 0x00e8 }
        r1 = r1 / 8;	 Catch:{ all -> 0x00e8 }
    L_0x0081:
        r2 = 5;	 Catch:{ all -> 0x00e8 }
        if (r0 < r2) goto L_0x0109;	 Catch:{ all -> 0x00e8 }
    L_0x0084:
        r0 = r8.c;	 Catch:{ all -> 0x00e8 }
        r1 = r8.D;	 Catch:{ all -> 0x00e8 }
        r0.setPreviewCallbackWithBuffer(r1);	 Catch:{ all -> 0x00e8 }
        r0 = r8.i;	 Catch:{ all -> 0x00e8 }
        r1 = 1;	 Catch:{ all -> 0x00e8 }
        r0.setRecording(r1);	 Catch:{ all -> 0x00e8 }
        r0 = r8.g;	 Catch:{ all -> 0x00e8 }
        r4 = r0.a();	 Catch:{ all -> 0x00e8 }
        r2 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x00e8 }
        r0 = new com.fyusion.sdk.camera.CaptureEvent;	 Catch:{ all -> 0x00e8 }
        r1 = com.fyusion.sdk.camera.CaptureEvent.CaptureStatus.CAPTURE_IN_PROGRESS;	 Catch:{ all -> 0x00e8 }
        r4 = r4.getAbsolutePath();	 Catch:{ all -> 0x00e8 }
        r5 = r8.b;	 Catch:{ all -> 0x00e8 }
        r0.<init>(r1, r2, r4, r5);	 Catch:{ all -> 0x00e8 }
        r1 = r8.c;	 Catch:{ all -> 0x00e8 }
        r1 = r1.getParameters();	 Catch:{ all -> 0x00e8 }
        r4 = r1.getPreviewSize();	 Catch:{ all -> 0x00e8 }
        r1 = r8.B;	 Catch:{ all -> 0x00e8 }
        r1 = r8.g;	 Catch:{ all -> 0x00e8 }
        r1 = r1.b();	 Catch:{ all -> 0x00e8 }
        r2 = r8.b;	 Catch:{ all -> 0x00e8 }
        r3 = r4.width;	 Catch:{ all -> 0x00e8 }
        r4 = r4.height;	 Catch:{ all -> 0x00e8 }
        r5 = r8.s;	 Catch:{ all -> 0x00e8 }
        r5 = r5.width;	 Catch:{ all -> 0x00e8 }
        r6 = r8.s;	 Catch:{ all -> 0x00e8 }
        r6 = r6.height;	 Catch:{ all -> 0x00e8 }
        com.fyusion.sdk.camera.b.a.a(r1, r2, r3, r4, r5, r6);	 Catch:{ all -> 0x00e8 }
        r1 = "Capture Started, In Progress";	 Catch:{ all -> 0x00e8 }
        r0.setDescription(r1);	 Catch:{ all -> 0x00e8 }
        r1 = r8.j;	 Catch:{ all -> 0x00e8 }
        r2 = r1.iterator();	 Catch:{ all -> 0x00e8 }
    L_0x00d7:
        r1 = r2.hasNext();	 Catch:{ all -> 0x00e8 }
        if (r1 != 0) goto L_0x0114;
    L_0x00dd:
        monitor-exit(r8);
        return;
    L_0x00df:
        r0 = new com.fyusion.sdk.camera.FyuseCameraException;	 Catch:{ all -> 0x00e8 }
        r1 = "Directory path does not exist!";	 Catch:{ all -> 0x00e8 }
        r0.<init>(r1);	 Catch:{ all -> 0x00e8 }
        throw r0;	 Catch:{ all -> 0x00e8 }
    L_0x00e8:
        r0 = move-exception;
        monitor-exit(r8);
        throw r0;
    L_0x00eb:
        r8.j();	 Catch:{ IOException -> 0x00f0 }
        goto L_0x003f;
    L_0x00f0:
        r0 = move-exception;
        r0.printStackTrace();	 Catch:{ all -> 0x00e8 }
        r1 = new com.fyusion.sdk.camera.FyuseCameraException;	 Catch:{ all -> 0x00e8 }
        r0 = r0.getMessage();	 Catch:{ all -> 0x00e8 }
        r1.<init>(r0);	 Catch:{ all -> 0x00e8 }
        throw r1;	 Catch:{ all -> 0x00e8 }
    L_0x00fe:
        r0 = move-exception;	 Catch:{ all -> 0x00e8 }
        r1 = new com.fyusion.sdk.camera.FyuseCameraException;	 Catch:{ all -> 0x00e8 }
        r2 = r0.getMessage();	 Catch:{ all -> 0x00e8 }
        r1.<init>(r2, r0);	 Catch:{ all -> 0x00e8 }
        throw r1;	 Catch:{ all -> 0x00e8 }
    L_0x0109:
        r2 = new byte[r1];	 Catch:{ all -> 0x00e8 }
        r3 = r8.c;	 Catch:{ all -> 0x00e8 }
        r3.addCallbackBuffer(r2);	 Catch:{ all -> 0x00e8 }
        r0 = r0 + 1;	 Catch:{ all -> 0x00e8 }
        goto L_0x0081;	 Catch:{ all -> 0x00e8 }
    L_0x0114:
        r1 = r2.next();	 Catch:{ all -> 0x00e8 }
        r1 = (com.fyusion.sdk.camera.CaptureEventListener) r1;	 Catch:{ all -> 0x00e8 }
        r1.onCapture(r0);	 Catch:{ all -> 0x00e8 }
        goto L_0x00d7;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fyusion.sdk.camera.impl.i.startRecording(com.fyusion.sdk.camera.MotionHintsListener, java.io.File):void");
    }

    public synchronized void stopRecording() throws FyuseCameraException {
        if (this.i.isRecording()) {
            this.i.setRecording(false);
            this.c.setPreviewCallbackWithBuffer(null);
            this.g.d();
            k();
        }
    }

    public void takeSnapShot(SnapShotCallback snapShotCallback) {
        if (this.i.isRecording()) {
            this.h.a(snapShotCallback);
        }
    }
}

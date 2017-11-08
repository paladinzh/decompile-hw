package com.fyusion.sdk.camera.a;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.media.Image;
import android.os.SystemClock;
import android.util.Log;
import com.fyusion.sdk.camera.CaptureEvent;
import com.fyusion.sdk.camera.CaptureEventListener;
import com.fyusion.sdk.camera.FyuseCameraProcessor;
import com.fyusion.sdk.camera.MotionHintsListener;
import com.fyusion.sdk.camera.RecordingProgressListener;
import com.fyusion.sdk.camera.util.ColorHelper;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.core.util.d;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* compiled from: Unknown */
public class b {
    private ExecutorService a = Executors.newSingleThreadExecutor();
    private c b;
    private d c;
    private CaptureEventListener d;
    private boolean e;
    private byte[] f;
    private a g;
    private final Integer h;
    private final int i;
    private final int j;
    private long k = -1;
    private final boolean l;
    private final Object m = new Object();
    private boolean n;

    /* compiled from: Unknown */
    private static class a implements Runnable {
        private final byte[] a;
        private final byte[] b;
        private final long c;
        private final c d;
        private final d e;

        a(c cVar, d dVar, byte[] bArr, byte[] bArr2, long j) {
            this.d = cVar;
            this.e = dVar;
            this.a = bArr;
            this.b = bArr2;
            this.c = j / 1000000;
        }

        public void run() {
            d.a();
            if (this.d.a(this.a, this.b, this.c)) {
                d.a();
                try {
                    this.e.a(this.a, false);
                } catch (RuntimeException e) {
                    com.fyusion.sdk.camera.a.c.b a = this.d.a();
                    if (a != null) {
                        a.onTerminatedPrematurely();
                    }
                }
            }
        }
    }

    /* compiled from: Unknown */
    private static class b implements Runnable {
        private final c a;
        private final d b;
        private a c;
        private CaptureEventListener d;

        b(c cVar, d dVar, a aVar, CaptureEventListener captureEventListener) {
            this.a = cVar;
            this.b = dVar;
            this.c = aVar;
            this.d = captureEventListener;
        }

        private void a() {
            if (this.a != null) {
                try {
                    this.c.a(this.a.d());
                } catch (Throwable e) {
                    Log.w("ImageProcessor", "Unable to save thumbnail.", e);
                }
            }
        }

        public void run() {
            CaptureEvent createCompletedEvent;
            d.a();
            this.a.c();
            this.a.b();
            this.b.a(null, true);
            this.b.a();
            if (this.a.e() > 5) {
                a();
                this.c.f();
                createCompletedEvent = CaptureEvent.createCompletedEvent(this.c.c().getAbsolutePath());
            } else {
                this.c.g();
                String str = "CAPTURE_FAILED. Not enough frames recorded.";
                Log.w("ImageProcessor", "CAPTURE_FAILED. Not enough frames recorded.");
                createCompletedEvent = CaptureEvent.createFailedEvent("CAPTURE_FAILED. Not enough frames recorded.");
                com.fyusion.sdk.camera.b.a.a(this.c.a(), "C_100", "CAPTURE_FAILED. Not enough frames recorded.");
            }
            if (this.d != null) {
                this.d.onCapture(createCompletedEvent);
            }
        }
    }

    @TargetApi(21)
    public b(CameraCharacteristics cameraCharacteristics, int i, int i2, File file, com.fyusion.sdk.camera.a.c.b bVar) {
        boolean z;
        boolean z2 = false;
        this.i = i;
        this.j = i2;
        this.h = (Integer) cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
        if (this.h == null) {
            z = true;
        } else {
            z = this.h.intValue() == 1;
        }
        if (((Integer) cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_TIMESTAMP_SOURCE)).intValue() == 1) {
            z2 = true;
        }
        this.l = z2;
        DLog.i("ImageProcessor", "Timestamp source real time: " + this.l);
        Integer num = (Integer) cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        int i3 = 90;
        if (num != null) {
            i3 = num.intValue();
        }
        this.b = new c(i, i2, z, i3, bVar);
        this.g = new a(file);
        this.b.a(this.g, 360);
        this.c = new d(i, i2, this.g.d().getPath());
    }

    public void a(Image image) {
        if (!this.e) {
            this.e = true;
            com.fyusion.sdk.camera.b.a.a(this.g.a(), this.h.intValue(), 0, 0, this.i, this.j);
            if (this.d != null) {
                this.d.onCapture(CaptureEvent.createInProgressEvent(this.g.b().getPath()));
            }
        }
        byte[] toNV21 = ColorHelper.toNV21(image);
        byte[] bArr = this.f;
        long timestamp = image.getTimestamp();
        if (!this.l) {
            if (this.k == -1) {
                this.k = Math.max(SystemClock.elapsedRealtimeNanos() - timestamp, 0);
            }
            timestamp += this.k;
        }
        this.a.submit(new a(this.b, this.c, toNV21, bArr, timestamp));
    }

    public void a(CaptureEventListener captureEventListener) {
        this.d = captureEventListener;
    }

    public void a(MotionHintsListener motionHintsListener) {
        this.b.a(motionHintsListener);
    }

    public void a(RecordingProgressListener recordingProgressListener) {
        this.b.a(recordingProgressListener);
    }

    public void a(String str) {
        synchronized (this.m) {
            if (this.n) {
                return;
            }
            if (this.d != null) {
                this.d.onCapture(CaptureEvent.createStoppedEvent("CAPTURE_STOPPED. " + str));
            }
            String str2 = "0";
            if (FyuseCameraProcessor.STOPPED_REASON_CAMERA_MOVED_BACKWARDS.equals(str)) {
                str2 = "1";
            }
            com.fyusion.sdk.camera.b.a.a(this.g.a(), str2, str);
            this.a.submit(new b(this.b, this.c, this.g, this.d));
            this.n = true;
        }
    }

    public void a(byte[] bArr) {
        this.f = bArr;
    }
}

package com.fyusion.sdk.camera.impl;

import android.media.CamcorderProfile;
import android.support.annotation.NonNull;
import com.fyusion.sdk.camera.CameraStatus;
import com.fyusion.sdk.camera.CaptureEvent;
import com.fyusion.sdk.camera.CaptureEvent.CaptureStatus;
import com.fyusion.sdk.camera.CaptureEventListener;
import com.fyusion.sdk.camera.FyuseCamera.RotationDirection;
import com.fyusion.sdk.camera.FyuseCameraException;
import com.fyusion.sdk.camera.FyuseCameraProcessor;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.common.ext.j;
import com.fyusion.sdk.common.ext.util.b;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/* compiled from: Unknown */
public class a {
    private static final String a = a.class.getSimpleName();
    private static final int b = j.l;
    private c c;
    private int d = 90;
    private boolean e = false;
    private com.fyusion.sdk.camera.c.a f;
    private com.fyusion.sdk.camera.a.a g;
    private BlockingQueue<b> h = null;
    private BlockingQueue<b> i = null;
    private ExecutorService j = Executors.newCachedThreadPool();
    private CameraStatus k;
    private volatile boolean l = false;
    private volatile boolean m = false;
    private Semaphore n = new Semaphore(2);
    private volatile boolean o = false;
    private Runnable p = new Runnable(this) {
        final /* synthetic */ a a;

        {
            this.a = r1;
        }

        public void run() {
            while (true) {
                try {
                    if (this.a.m) {
                        if (this.a.h.isEmpty()) {
                            break;
                        }
                    }
                    b bVar = (b) this.a.h.poll(30, TimeUnit.MILLISECONDS);
                    if (bVar != null) {
                        long currentTimeMillis = System.currentTimeMillis();
                        this.a.f.a(bVar.a, false);
                        long currentTimeMillis2 = System.currentTimeMillis() - currentTimeMillis;
                        if (this.a.e) {
                            DLog.d(a.a, "onPreviewFrame : encoding Time " + currentTimeMillis2);
                        }
                    }
                } catch (InterruptedException e) {
                    r1 = CaptureEvent.createStoppedEvent(FyuseCameraProcessor.STOPPED_REASON_TERMINATED_PREMATURELY);
                    for (CaptureEventListener onCapture : this.a.c.a()) {
                        CaptureEvent createStoppedEvent;
                        onCapture.onCapture(createStoppedEvent);
                    }
                    this.a.o = true;
                }
            }
            this.a.f.a(null, true);
            this.a.h();
        }
    };
    private Runnable q = new Runnable(this) {
        final /* synthetic */ a a;

        {
            this.a = r1;
        }

        public void run() {
            if (this.a.e) {
                DLog.d(a.a, "processing thread started");
            }
            while (!this.a.o) {
                try {
                    if (this.a.l) {
                        if (this.a.i.isEmpty()) {
                            break;
                        }
                    }
                    b bVar = (b) this.a.i.poll(30, TimeUnit.MILLISECONDS);
                    if (bVar != null) {
                        long j = bVar.c;
                        boolean a = this.a.c.a(bVar.a, bVar.b, this.a.d, this.a.k.isMirrorVertically(), j);
                        if (this.a.e) {
                            DLog.d(a.a, "writeframetovideo=" + a);
                        }
                        if (a) {
                            long currentTimeMillis = System.currentTimeMillis() - j;
                            if (this.a.e) {
                                DLog.d(a.a, "onPreviewFrame : processing time " + currentTimeMillis);
                            }
                            this.a.h.put(bVar);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.a.m = true;
            if (this.a.e) {
                DLog.d(a.a, "Processor done");
            }
            this.a.n.release();
        }
    };

    public a(@NonNull c cVar, CameraStatus cameraStatus, File file) throws IOException {
        a(cVar, cameraStatus);
        this.g = new com.fyusion.sdk.camera.a.a(file);
    }

    private void a(@NonNull c cVar, CameraStatus cameraStatus) {
        this.c = cVar;
        this.k = cameraStatus;
        this.h = new LinkedBlockingQueue(5);
        this.i = new LinkedBlockingQueue(5);
    }

    private void f() {
        long currentTimeMillis = System.currentTimeMillis();
        if (this.c.g() > 5) {
            if (this.e) {
                DLog.d(a, "analyzeLatestRecording() : Capture Completed");
            }
            g();
            if (this.e) {
                DLog.d(a, "analyzeLatestRecording(): Saving thumnail DONE");
            }
            this.g.f();
            File c = this.g.c();
            CaptureEvent captureEvent = new CaptureEvent(CaptureStatus.CAPTURE_COMPLETED, currentTimeMillis, c.getAbsolutePath());
            captureEvent.setDescription("Capture Completed, fyuse file : " + c.getName());
            captureEvent.setUid(this.g.a());
            for (CaptureEventListener onCapture : this.c.a()) {
                onCapture.onCapture(captureEvent);
            }
            return;
        }
        this.g.g();
        CaptureEvent captureEvent2 = new CaptureEvent(CaptureStatus.CAPTURE_FAILED, currentTimeMillis);
        captureEvent2.setDescription("CAPTURE_FAILED. Not enough frames recorded.");
        captureEvent2.setRecordingStatus("C_100");
        captureEvent2.setUid(this.g.a());
        for (CaptureEventListener onCapture2 : this.c.a()) {
            onCapture2.onCapture(captureEvent2);
        }
    }

    private void g() {
        try {
            this.g.a(this.c.f());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void h() {
        if (this.f != null) {
            if (this.o) {
                this.f.b();
                try {
                    this.k.getFyuseCamera().stopRecording();
                } catch (FyuseCameraException e) {
                    DLog.e(a, e.getMessage());
                }
            } else {
                this.f.a();
            }
            if (this.e) {
                DLog.d("stopEncoderWhenFinished", "finished with encoder");
            }
        }
        this.c.e();
        this.c.d();
        f();
        this.n.release();
    }

    public File a() {
        return this.g.c();
    }

    public void a(int i, float[] fArr, RotationDirection rotationDirection) throws FyuseCameraException {
        com.fyusion.sdk.camera.c.a aVar;
        String absolutePath = this.g.d().getAbsolutePath();
        if (b.a() == null) {
            aVar = new com.fyusion.sdk.camera.c.a(this.k.getPreviewWidth(), this.k.getPreviewHeight(), b, absolutePath, false, this.k.getPreviewFPS());
        } else {
            CamcorderProfile a = b.a();
            aVar = new com.fyusion.sdk.camera.c.a(a.videoFrameWidth, a.videoFrameHeight, a.videoBitRate, absolutePath, false, a.videoFrameRate);
        }
        this.f = aVar;
        this.c.a(this.g, i, fArr, rotationDirection);
        this.o = false;
        this.l = false;
        this.m = false;
        this.j.execute(this.p);
        this.j.execute(this.q);
        if (this.e) {
            DLog.d("initiateRecording", "Set preview callback.");
        }
    }

    public void a(b bVar) {
        if (!this.i.offer(bVar) && this.e) {
            DLog.w(a, "processNV21Frame() processorQueue queue full. Frame could not be added for processing");
        }
    }

    public String b() {
        return this.g.a();
    }

    public void c() throws FyuseCameraException {
        if (this.k.isRecording() || !this.n.tryAcquire(2)) {
            throw new FyuseCameraException("Previous recording not completed");
        }
    }

    public void d() {
        this.l = true;
    }
}

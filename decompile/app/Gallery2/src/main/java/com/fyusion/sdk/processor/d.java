package com.fyusion.sdk.processor;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.opengl.GLES20;
import android.util.Log;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.common.ext.FyuseDescriptor;
import com.fyusion.sdk.common.ext.FyuseState;
import com.fyusion.sdk.common.ext.ProcessError;
import com.fyusion.sdk.common.ext.ProcessItem;
import com.fyusion.sdk.common.ext.ProcessItem.ProcessState;
import com.fyusion.sdk.common.ext.ProcessorListener;
import com.fyusion.sdk.common.ext.Size;
import com.fyusion.sdk.common.ext.e;
import com.fyusion.sdk.common.ext.j;
import com.fyusion.sdk.common.ext.k;
import com.fyusion.sdk.common.ext.l;
import com.fyusion.sdk.core.util.pool.ByteBufferPool;
import com.fyusion.sdk.core.util.pool.c;
import com.huawei.watermark.manager.parse.WMElement;
import fyusion.vislib.FloatVec;
import fyusion.vislib.FyuseSize;
import fyusion.vislib.TransformationParameters;
import fyusion.vislib.VislibJavaHelper;
import fyusion.vislib.VislibJavaHelper.ZoomMode;
import fyusion.vislib.VisualizationMeshStorage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.mtnwrw.pdqimg.CompressionService;
import org.mtnwrw.pdqimg.CompressionService.quality;
import org.mtnwrw.pdqimg.ConversionService;
import org.mtnwrw.pdqimg.PDQBuffer;
import org.mtnwrw.pdqimg.PDQImage;

/* compiled from: Unknown */
public class d extends Thread {
    private static Executor A = Executors.newSingleThreadExecutor(new ThreadFactory() {
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "LegacyFyuseStabilizer");
        }
    });
    private static Executor B = Executors.newSingleThreadExecutor(new ThreadFactory() {
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "LegacyFyuseStabilizer");
        }
    });
    private static Semaphore z = new Semaphore(0);
    private int C = 0;
    private int D = 0;
    ProcessItem a;
    BlockingQueue<b> b;
    private l c;
    private e d;
    private FyuseDescriptor e;
    private Matrix f;
    private boolean g = false;
    private com.fyusion.sdk.processor.mjpegutils.a h;
    private ProcessorListener i;
    private boolean j;
    private boolean k;
    private Bitmap l;
    private a m = a.CMD_IDLE;
    private final Lock n = new ReentrantLock();
    private final Lock o = new ReentrantLock();
    private final Condition p = this.n.newCondition();
    private final Condition q = this.o.newCondition();
    private boolean r = false;
    private Size s = null;
    private Size t = null;
    private boolean u = false;
    private BlockingQueue<Integer> v = new ArrayBlockingQueue(1);
    private BlockingQueue<Integer> w = new ArrayBlockingQueue(1);
    private ByteBufferPool x;
    private boolean y = false;

    /* compiled from: Unknown */
    /* renamed from: com.fyusion.sdk.processor.d$5 */
    static /* synthetic */ class AnonymousClass5 {
        static final /* synthetic */ int[] a = new int[a.values().length];

        static {
            try {
                a[a.CMD_IDLE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                a[a.CMD_CREATE_SURFACE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                a[a.CMD_WAIT_FOR_IMAGE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    /* compiled from: Unknown */
    private enum a {
        CMD_IDLE,
        CMD_CREATE_SURFACE,
        CMD_WAIT_FOR_IMAGE
    }

    /* compiled from: Unknown */
    private class b {
        int a;
        ByteBuffer b;
        Matrix c;
        final /* synthetic */ d d;

        b(d dVar, int i, ByteBuffer byteBuffer, Matrix matrix) {
            this.d = dVar;
            this.a = i;
            this.b = byteBuffer;
            this.c = matrix;
        }
    }

    public d(l lVar) {
        this.c = lVar;
        this.x = new ByteBufferPool(2);
    }

    private File a(int i) {
        return new File(this.c.c(), String.format(Locale.US, j.aH, new Object[]{Integer.valueOf(i)}));
    }

    private void a(int i, MediaCodec mediaCodec, int i2, boolean z) {
        if (a(i2).exists() || z) {
            if (this.w.contains(Integer.valueOf(i))) {
                try {
                    this.w.offer(Integer.valueOf(i), 1000, TimeUnit.MILLISECONDS);
                    this.w.poll();
                } catch (Throwable e) {
                    DLog.e("LegacyFyuseStabilizer", "Decoding interrupted", e);
                    return;
                }
            }
            mediaCodec.releaseOutputBuffer(i, false);
            return;
        }
        boolean z2 = false;
        while (!z2 && !this.a.isCancelled()) {
            try {
                this.w.offer(Integer.valueOf(i), 1000, TimeUnit.MILLISECONDS);
                this.v.offer(Integer.valueOf(i2), 1000, TimeUnit.MILLISECONDS);
                mediaCodec.releaseOutputBuffer(i, true);
                z2 = true;
            } catch (Throwable e2) {
                Log.e("LegacyFyuseStabilizer", "Interrupt in decoding", e2);
            }
        }
    }

    private void a(Bitmap bitmap, int i, int i2) {
        PDQImage createFromBitmap;
        if (!this.y) {
            try {
                createFromBitmap = PDQImage.createFromBitmap(bitmap, 17, null);
                if (createFromBitmap == null) {
                    DLog.e("LegacyFyuseStabilizer", "Unable to save thumbnail because img is null");
                } else {
                    createFromBitmap.swapUVChannels(true);
                    Bitmap createBitmap = Bitmap.createBitmap(i, i2, Config.ARGB_8888);
                    ConversionService.convertPDQImageToBitmap(createFromBitmap, createBitmap, false, false);
                    this.c.a(com.fyusion.sdk.common.ext.util.a.b(createBitmap, this.d), this.d);
                }
                if (createFromBitmap != null) {
                    createFromBitmap.close();
                }
                this.y = true;
            } catch (Throwable e) {
                DLog.e("LegacyFyuseStabilizer", "Unable to save thumbnail", e);
            } catch (Throwable th) {
                if (createFromBitmap != null) {
                    createFromBitmap.close();
                }
            }
        }
    }

    private void a(com.fyusion.sdk.processor.mjpegutils.a aVar, int i) {
        a(this.e.transformParameters.get(i), this.e.globalScale, this.k, this.t.width, this.t.height);
        float[] fArr = new float[16];
        float[] fArr2 = new float[16];
        r2 = new float[9];
        android.opengl.Matrix.setIdentityM(fArr, 0);
        this.f.getValues(r2);
        fArr[0] = r2[0];
        fArr[4] = r2[1];
        fArr[12] = r2[2];
        fArr[1] = r2[3];
        fArr[5] = r2[4];
        fArr[13] = r2[5];
        fArr[10] = WMElement.CAMERASIZEVALUE1B1;
        fArr[15] = WMElement.CAMERASIZEVALUE1B1;
        android.opengl.Matrix.invertM(fArr2, 0, fArr, 0);
        aVar.a(fArr2);
        if (!this.a.isCancelled()) {
            Matrix matrix = new Matrix();
            matrix.set(this.f);
            aVar.a(true);
            b bVar = new b(this, i, g(), matrix);
            while (!this.a.isCancelled()) {
                try {
                    if (this.r) {
                        break;
                    } else if (this.b.offer(bVar, 100, TimeUnit.MILLISECONDS)) {
                        return;
                    }
                } catch (InterruptedException e) {
                    DLog.d("LegacyFyuseStabilizer", e.getMessage());
                    return;
                }
            }
            this.x.release(bVar.b);
        }
    }

    private void a(TransformationParameters transformationParameters, float f, boolean z, int i, int i2) {
        if (this.f == null) {
            this.f = new Matrix();
        }
        this.f.reset();
        FloatVec transform = VislibJavaHelper.getTransformForParameters(transformationParameters).getTransform();
        float[] fArr = new float[9];
        for (int i3 = 0; i3 < 6; i3++) {
            fArr[i3] = transform.get(i3) * f;
        }
        fArr[6] = transform.get(6);
        fArr[7] = transform.get(7);
        fArr[8] = WMElement.CAMERASIZEVALUE1B1;
        this.f.setValues(fArr);
        if (z) {
            this.f.preScale(GroundOverlayOptions.NO_DIMENSION, GroundOverlayOptions.NO_DIMENSION);
        }
        this.f.preScale((float) i, (float) i2);
        this.f.preTranslate(-0.5f, -0.5f);
        this.f.postScale(WMElement.CAMERASIZEVALUE1B1 / ((float) i), WMElement.CAMERASIZEVALUE1B1 / ((float) i2));
        this.f.postTranslate(0.5f, 0.5f);
    }

    private boolean a(int i, int i2) {
        boolean z;
        Lock lock = null;
        this.n.lock();
        this.o.lock();
        this.m = a.CMD_CREATE_SURFACE;
        this.s = new Size(i, i2);
        this.p.signalAll();
        this.n.unlock();
        try {
            this.q.await();
            z = this.u;
        } catch (Throwable e) {
            DLog.e("LegacyFyuseStabilizer", "Interrupted in createOutputSurface()", e);
            return lock;
        } finally {
            lock = this.o;
            lock.unlock();
        }
        return z;
    }

    private boolean a(MediaCodec mediaCodec, MediaExtractor mediaExtractor, ByteBuffer[] byteBufferArr, int[] iArr) {
        int dequeueInputBuffer = mediaCodec.dequeueInputBuffer(1000000);
        if (dequeueInputBuffer < 0) {
            DLog.i("extractInput", "dequeueInputBuffer returns " + dequeueInputBuffer);
            return false;
        }
        int readSampleData = mediaExtractor.readSampleData(byteBufferArr[dequeueInputBuffer], 0);
        if (readSampleData >= 0) {
            mediaCodec.queueInputBuffer(dequeueInputBuffer, 0, readSampleData, mediaExtractor.getSampleTime(), 0);
            mediaExtractor.advance();
            iArr[0] = iArr[0] + 1;
            return false;
        }
        mediaCodec.queueInputBuffer(dequeueInputBuffer, 0, 0, 0, 4);
        return true;
    }

    private boolean a(MediaExtractor mediaExtractor, MediaCodec mediaCodec, com.fyusion.sdk.processor.mjpegutils.a aVar, int i) {
        BufferInfo bufferInfo = new BufferInfo();
        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
        int[] iArr = new int[]{0};
        boolean z = false;
        int i2 = 0;
        int i3 = 0;
        while (!this.a.isCancelled()) {
            if (this.g) {
                return true;
            }
            if (!z) {
                z = a(mediaCodec, mediaExtractor, inputBuffers, iArr);
            }
            int dequeueOutputBuffer = mediaCodec.dequeueOutputBuffer(bufferInfo, 1000000);
            if ((bufferInfo.flags & 4) != 0) {
                return true;
            }
            if (dequeueOutputBuffer == -1) {
                DLog.i("extractFrames", "dequeueOutputBuffer try again " + i3);
                dequeueOutputBuffer = i3 + 1;
                if (i3 > 500) {
                    return false;
                }
                try {
                    Thread.sleep(1, 0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i3 = dequeueOutputBuffer;
            } else if (dequeueOutputBuffer == -2) {
                mediaCodec.getOutputFormat();
            } else if (dequeueOutputBuffer != -3 && dequeueOutputBuffer >= 0) {
                boolean dropFrame = this.e.dropFrame(i2, 0);
                a(dequeueOutputBuffer, mediaCodec, i, dropFrame);
                if (!dropFrame) {
                    i++;
                }
                i2++;
            }
        }
        DLog.i("LegacyFyuseStabilizer", "Processing is cancelled. " + this.a.getFile());
        return false;
    }

    private void b() {
        this.n.lock();
        this.m = a.CMD_WAIT_FOR_IMAGE;
        this.p.signalAll();
        this.n.unlock();
    }

    private boolean c() {
        File l = this.c.l();
        DLog.d("extractFramesAndProcess", "extracting frames for: " + l.getAbsolutePath());
        MediaExtractor mediaExtractor = new MediaExtractor();
        try {
            mediaExtractor.setDataSource(l.getPath());
            String str = null;
            int i = 0;
            while (i < mediaExtractor.getTrackCount()) {
                try {
                    str = mediaExtractor.getTrackFormat(i).getString("mime");
                    if (str.startsWith("video/")) {
                        mediaExtractor.selectTrack(i);
                        break;
                    }
                    i++;
                } catch (IllegalArgumentException e) {
                    DLog.e("LegacyFyuseStabilizer", "unable to get track format");
                    return false;
                }
            }
            i = -1;
            if (i >= 0) {
                this.t = new Size(this.e.getMagic().getCameraWidth(), this.e.getMagic().getCameraHeight());
                if (!a(this.e.getMagic().getWidth(), this.e.getMagic().getHeight())) {
                    return false;
                }
                try {
                    MediaCodec createDecoderByType = MediaCodec.createDecoderByType(str);
                    try {
                        com.fyusion.sdk.common.util.b.a(createDecoderByType, mediaExtractor.getTrackFormat(i), this.h.c(), 0);
                        b();
                        createDecoderByType.start();
                        boolean a = a(mediaExtractor, createDecoderByType, this.h, this.d.getStartFrame());
                        mediaExtractor.release();
                        return a;
                    } catch (Exception e2) {
                        h();
                        return false;
                    }
                } catch (IOException e3) {
                    h();
                    return false;
                }
            }
            DLog.e("LegacyFyuseStabilizer", "unable to find a video track");
            return false;
        } catch (IOException e4) {
            DLog.e("LegacyFyuseStabilizer", "could not set data source");
            return false;
        }
    }

    private boolean d() {
        DLog.d("doVislibProcessing", "beginning vislib processing");
        try {
            boolean z;
            File j;
            Object visualizationMeshStorage;
            ZoomMode zoomMode;
            boolean z2;
            boolean z3;
            this.c.h();
            this.d = this.c.d();
            int cameraOrientation = this.d.getCameraOrientation();
            if (cameraOrientation != 90) {
                if (this.d.wasRecordedUsingFrontCamera() || cameraOrientation == 270) {
                    z = true;
                    this.k = z;
                    this.e = f();
                    this.e.setFyusePath(this.c.c().getPath());
                    j = this.c.j();
                    visualizationMeshStorage = new VisualizationMeshStorage();
                    zoomMode = ZoomMode.FULL_WITH_NONE_FOR_360;
                    if (this.a.getFyuseProcessorParameters() != null) {
                        z2 = false;
                        z3 = false;
                    } else {
                        zoomMode.setValue(this.a.getFyuseProcessorParameters().getZoomMode().getValue());
                        z3 = this.a.getFyuseProcessorParameters().getForceCheckLoopClosure();
                        z2 = this.a.getFyuseProcessorParameters().getEnableLoopClosure();
                    }
                    DLog.i("LegacyFyuseStabilizer", "Zoom mode: " + zoomMode.toString());
                    DLog.i("LegacyFyuseStabilizer", "Enable LC: " + z2);
                    DLog.i("LegacyFyuseStabilizer", "Force LC check: " + z3);
                    if (VislibJavaHelper.startPipeline(visualizationMeshStorage, this.e, j.getPath(), true, z3, false, zoomMode, z2)) {
                        return false;
                    }
                    this.c.a(FyuseState.PROCESSED);
                    if (this.d.a(j.getPath())) {
                        return false;
                    }
                    this.D = (int) this.e.transformParameters.size();
                    this.C = this.c.a(this.D);
                    return true;
                }
            }
            z = false;
            this.k = z;
            this.e = f();
            this.e.setFyusePath(this.c.c().getPath());
            j = this.c.j();
            visualizationMeshStorage = new VisualizationMeshStorage();
            zoomMode = ZoomMode.FULL_WITH_NONE_FOR_360;
            if (this.a.getFyuseProcessorParameters() != null) {
                zoomMode.setValue(this.a.getFyuseProcessorParameters().getZoomMode().getValue());
                z3 = this.a.getFyuseProcessorParameters().getForceCheckLoopClosure();
                z2 = this.a.getFyuseProcessorParameters().getEnableLoopClosure();
            } else {
                z2 = false;
                z3 = false;
            }
            DLog.i("LegacyFyuseStabilizer", "Zoom mode: " + zoomMode.toString());
            DLog.i("LegacyFyuseStabilizer", "Enable LC: " + z2);
            DLog.i("LegacyFyuseStabilizer", "Force LC check: " + z3);
            if (VislibJavaHelper.startPipeline(visualizationMeshStorage, this.e, j.getPath(), true, z3, false, zoomMode, z2)) {
                return false;
            }
            this.c.a(FyuseState.PROCESSED);
            if (this.d.a(j.getPath())) {
                return false;
            }
            this.D = (int) this.e.transformParameters.size();
            this.C = this.c.a(this.D);
            return true;
        } catch (Throwable e) {
            DLog.e("LegacyFyuseStabilizer", "Error when processing: ", e);
            return false;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void e() {
        try {
            b bVar = (b) this.b.take();
        } catch (InterruptedException e) {
            DLog.d("LegacyFyuseStabilizer", e.getMessage());
            bVar = null;
        }
        if (bVar.a != -1) {
            PDQImage createFromBitmap;
            PDQBuffer pDQBuffer;
            FileOutputStream fileOutputStream;
            ProcessorListener processorListener;
            ProcessItem processItem;
            int i;
            k kVar;
            File file = new File(this.c.c(), String.format(Locale.US, j.aH, new Object[]{Integer.valueOf(bVar.a)}) + ".temp");
            this.e.transformParameters.get(bVar.a);
            float f = this.e.globalScale;
            int width = this.e.getMagic().getWidth();
            int height = this.e.getMagic().getHeight();
            if (this.l != null) {
                if (this.l.getWidth() == width && this.l.getHeight() == height) {
                    this.l.copyPixelsFromBuffer(bVar.b);
                    this.x.release(bVar.b);
                    a(this.l, width, height);
                    createFromBitmap = PDQImage.createFromBitmap(this.l, 17, null);
                    if (createFromBitmap != null) {
                        try {
                            pDQBuffer = (PDQBuffer) c.a.acquire(width * height);
                            if (CompressionService.compressPDQImage(createFromBitmap, quality.QUALITY_LOW, pDQBuffer)) {
                                fileOutputStream = new FileOutputStream(file);
                                fileOutputStream.getChannel().write(pDQBuffer.getBuffer());
                                fileOutputStream.close();
                            }
                            c.a.release(pDQBuffer);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        } catch (Throwable th) {
                            c.a.release(pDQBuffer);
                        }
                        createFromBitmap.close();
                    }
                    file.renameTo(a(bVar.a));
                    if (this.i instanceof k) {
                        processorListener = this.i;
                        processItem = this.a;
                        i = this.C;
                        this.C = i + 1;
                        processorListener.onProgress(processItem, i, this.D, this.l);
                    } else {
                        kVar = (k) this.i;
                        processItem = this.a;
                        i = this.C;
                        this.C = i + 1;
                        kVar.a(processItem, i, this.D, this.l, bVar.c);
                    }
                }
            }
            if (!(this.l == null || this.l.isRecycled())) {
                this.l.recycle();
            }
            this.l = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            this.l.copyPixelsFromBuffer(bVar.b);
            this.x.release(bVar.b);
            a(this.l, width, height);
            createFromBitmap = PDQImage.createFromBitmap(this.l, 17, null);
            if (createFromBitmap != null) {
                pDQBuffer = (PDQBuffer) c.a.acquire(width * height);
                if (CompressionService.compressPDQImage(createFromBitmap, quality.QUALITY_LOW, pDQBuffer)) {
                    fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.getChannel().write(pDQBuffer.getBuffer());
                    fileOutputStream.close();
                }
                c.a.release(pDQBuffer);
                createFromBitmap.close();
            }
            file.renameTo(a(bVar.a));
            if (this.i instanceof k) {
                kVar = (k) this.i;
                processItem = this.a;
                i = this.C;
                this.C = i + 1;
                kVar.a(processItem, i, this.D, this.l, bVar.c);
            } else {
                processorListener = this.i;
                processItem = this.a;
                i = this.C;
                this.C = i + 1;
                processorListener.onProgress(processItem, i, this.D, this.l);
            }
        } else {
            this.x.clear();
            this.g = true;
        }
    }

    private FyuseDescriptor f() {
        FyuseDescriptor fyuseDescriptor = new FyuseDescriptor(this.d, this.c.c().getName());
        fyuseDescriptor.getMagic().setCameraWidth((int) this.d.getCameraSize().width);
        fyuseDescriptor.getMagic().setCameraHeight((int) this.d.getCameraSize().height);
        return fyuseDescriptor;
    }

    private ByteBuffer g() {
        int width = this.e.getMagic().getWidth();
        int height = this.e.getMagic().getHeight();
        ByteBuffer byteBuffer = (ByteBuffer) this.x.mustAcquire((width * height) * 4);
        byteBuffer.rewind();
        GLES20.glReadPixels(0, 0, width, height, 6408, 5121, byteBuffer);
        byteBuffer.rewind();
        return byteBuffer;
    }

    private void h() {
        this.o.lock();
        this.r = true;
        try {
            this.q.await();
            this.o.unlock();
            join();
        } catch (Throwable e) {
            DLog.e("LegacyFyuseStabilizer", "Interrupted", e);
        }
    }

    private boolean i() {
        if (this.h != null) {
            this.h.a();
        }
        try {
            this.h = new com.fyusion.sdk.processor.mjpegutils.a(this.s.width, this.s.height);
            return true;
        } catch (Throwable e) {
            this.h = null;
            DLog.e("LegacyFyuseStabilizer", "createCodecSurfaceInGLThread() failed", e);
            return false;
        }
    }

    private void j() {
        if (this.h != null) {
            this.h.a();
        }
        this.h = null;
    }

    public void a(final ProcessorListener processorListener, final ProcessItem processItem) {
        DLog.d("processing", "processing for " + this.c.c());
        try {
            this.o.lock();
            start();
            this.q.await();
            this.i = processorListener;
            this.a = processItem;
            this.b = new ArrayBlockingQueue(4, true);
            Runnable anonymousClass3 = new Runnable(this) {
                final /* synthetic */ d a;

                {
                    this.a = r1;
                }

                public void run() {
                    this.a.g = false;
                    com.fyusion.sdk.processor.a.b.a(this.a.c.a());
                    CompressionService.initialize(Runtime.getRuntime().availableProcessors());
                    System.nanoTime();
                    if (this.a.d()) {
                        this.a.j = this.a.c();
                    } else {
                        this.a.j = false;
                    }
                    this.a.h();
                    System.nanoTime();
                    try {
                        this.a.b.put(new b(this.a, -1, null, null));
                    } catch (InterruptedException e) {
                        DLog.d("LegacyFyuseStabilizer", e.getMessage());
                    }
                    try {
                        d.z.acquire();
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    }
                }
            };
            Runnable anonymousClass4 = new Runnable(this) {
                final /* synthetic */ d c;

                public void run() {
                    b bVar;
                    while (!this.c.g) {
                        if (processItem.isCancelled()) {
                            try {
                                bVar = (b) this.c.b.poll(0, TimeUnit.MILLISECONDS);
                                if (bVar != null) {
                                    this.c.x.release(bVar.b);
                                }
                                this.c.c.i();
                            } catch (InterruptedException e) {
                                DLog.e("LegacyFyuseStabilizer", e.getMessage());
                            } catch (IllegalStateException e2) {
                                DLog.d("LegacyFyuseStabilizer", e2.getMessage());
                            } catch (Throwable th) {
                            }
                            processItem.setState(ProcessState.CANCELLED);
                            processorListener.onError(processItem, ProcessError.USER_CANCEL_REQUEST);
                            d.z.release();
                            return;
                        }
                        this.c.e();
                    }
                    if (this.c.j) {
                        try {
                            this.c.c.k();
                            this.c.d.a(this.c.c.c().getPath());
                            this.c.d.setProcessedSize(new FyuseSize((double) this.c.e.getMagic().getWidth(), (double) this.c.e.getMagic().getHeight()));
                            this.c.d.setNumberOfProcessedFrames(this.c.C - 1);
                            this.c.c.a(this.c.d);
                            this.c.c.a(FyuseState.WRITTEN);
                            processItem.setFyuseClass(this.c.d);
                            try {
                                this.c.c.a(true);
                                com.fyusion.sdk.processor.a.b.a(this.c.c.a(), this.c.d.getNumberOfProcessedFrames(), this.c.d.isLoopClosed());
                            } catch (IOException e3) {
                                DLog.e("LegacyFyuseStabilizer", "Error opening fyuse file: " + e3.getMessage());
                                this.c.c.i();
                                com.fyusion.sdk.processor.a.b.a(this.c.c.a(), "P_100", "Fail during packing file." + e3.getMessage());
                            }
                            processorListener.onImageDataReady(processItem);
                            processorListener.onProcessComplete(processItem);
                        } catch (Throwable e4) {
                            DLog.e("LegacyFyuseStabilizer", "Copy from temp dir failed.", e4);
                            processorListener.onError(processItem, ProcessError.CORRUPT_DATA);
                            return;
                        }
                    }
                    try {
                        bVar = (b) this.c.b.poll(0, TimeUnit.MILLISECONDS);
                        if (bVar != null) {
                            this.c.x.release(bVar.b);
                        }
                        com.fyusion.sdk.processor.a.b.a(this.c.c.a(), "P_101", "Frames extractions failed");
                    } catch (InterruptedException e5) {
                        DLog.e("LegacyFyuseStabilizer", e5.getMessage());
                        com.fyusion.sdk.processor.a.b.a(this.c.c.a(), "P_999", "Fail during packing file." + e5.getMessage());
                    } catch (IllegalStateException e22) {
                        DLog.d("LegacyFyuseStabilizer", e22.getMessage());
                        com.fyusion.sdk.processor.a.b.a(this.c.c.a(), "P_999", "Fail during packing file." + e22.getMessage());
                    } finally {
                        this.c.c.i();
                    }
                    processItem.setState(ProcessState.CANCELLED);
                    if (processItem.isCancelled()) {
                        processorListener.onError(processItem, ProcessError.USER_CANCEL_REQUEST);
                    } else {
                        DLog.d("LegacyFyuseStabilizer", "Corrupt data");
                        processorListener.onError(processItem, ProcessError.CORRUPT_DATA);
                    }
                    d.z.release();
                    return;
                    this.c.c.i();
                }
            };
            A.execute(anonymousClass3);
            B.execute(anonymousClass4);
        } catch (Throwable e) {
            DLog.e("LegacyFyuseStabilizer", "startup problems", e);
        } finally {
            this.o.unlock();
        }
    }

    public void run() {
        setName("[Processor] CodecSurfThread");
        this.o.lock();
        this.q.signalAll();
        this.o.unlock();
        this.m = a.CMD_IDLE;
        this.n.lock();
        while (true) {
            if (this.r) {
                try {
                    if (this.v.isEmpty()) {
                        j();
                        this.o.lock();
                        this.q.signalAll();
                        this.o.unlock();
                        return;
                    }
                } catch (Throwable e) {
                    DLog.e("LegacyFyuseStabilizer", "SurfaceThread was interrupted", e);
                    this.r = true;
                } finally {
                    this.n.unlock();
                }
            }
            int i = this.m != a.CMD_WAIT_FOR_IMAGE ? 0 : 1;
            if (i != 0) {
                int i2 = i;
            } else {
                boolean await = this.p.await(25, TimeUnit.MILLISECONDS);
            }
            if (((this.m == a.CMD_IDLE ? 0 : 1) | i2) != 0) {
                this.u = false;
                switch (AnonymousClass5.a[this.m.ordinal()]) {
                    case 1:
                        this.u = true;
                        break;
                    case 2:
                        this.m = a.CMD_IDLE;
                        if (this.s != null) {
                            this.u = i();
                        } else {
                            DLog.e("LegacyFyuseStabilizer", "Illegal surface size supplied, ignoring request");
                        }
                        i = 1;
                        break;
                    case 3:
                        if (this.h != null && this.h.a(25)) {
                            i = ((Integer) this.v.poll()).intValue();
                            this.w.poll();
                            a(this.h, i);
                            break;
                        }
                }
                Object obj = null;
                if (obj != null) {
                    this.o.lock();
                    this.q.signalAll();
                    this.o.unlock();
                }
            }
        }
    }
}

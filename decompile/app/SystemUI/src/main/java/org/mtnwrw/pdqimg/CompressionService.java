package org.mtnwrw.pdqimg;

import android.graphics.YuvImage;
import android.media.Image;
import android.media.MediaFormat;
import java.nio.ByteBuffer;

/* compiled from: Unknown */
public class CompressionService {
    private static Object InitLock = new Object();
    private static CompressionService Instance = null;
    private static final String LOGTAG = "CompServ";
    private static final int MAX_THREADS = 8;
    private static boolean NativeOK;
    private int CompressionThreads = 0;
    private boolean Running = false;

    /* compiled from: Unknown */
    public enum quality {
        QUALITY_LOW(0),
        QUALITY_MEDIUM(1),
        QUALITY_HIGH(2);
        
        private final int Key;

        private quality(int i) {
            this.Key = i;
        }
    }

    static {
        NativeOK = false;
        try {
            System.loadLibrary("pdqimg");
            NativeOK = true;
        } catch (Throwable e) {
            e.printStackTrace();
        } catch (Throwable e2) {
            e2.printStackTrace();
        }
    }

    private CompressionService() {
    }

    private native void cleanupCompression();

    public static boolean compress(Image image, quality quality, PDQBuffer pDQBuffer) {
        if (!NativeOK) {
            return false;
        }
        synchronized (Instance) {
            if (Instance.Running) {
                boolean compressSynchronousImage = Instance.compressSynchronousImage(image, quality.ordinal(), pDQBuffer);
                return compressSynchronousImage;
            }
            return false;
        }
    }

    private native boolean compressMediaCodecBuffer(ByteBuffer byteBuffer, MediaFormat mediaFormat, int i, PDQBuffer pDQBuffer);

    public static boolean compressMediaCodecBuffer(ByteBuffer byteBuffer, MediaFormat mediaFormat, quality quality, PDQBuffer pDQBuffer) {
        if (!NativeOK || !byteBuffer.isDirect()) {
            return false;
        }
        synchronized (Instance) {
            if (Instance.Running) {
                boolean compressMediaCodecBuffer = Instance.compressMediaCodecBuffer(byteBuffer, mediaFormat, quality.ordinal(), pDQBuffer);
                return compressMediaCodecBuffer;
            }
            return false;
        }
    }

    public static boolean compressPDQImage(PDQImage pDQImage, quality quality, PDQBuffer pDQBuffer) {
        if (!NativeOK) {
            return false;
        }
        synchronized (Instance) {
            if (Instance.Running) {
                boolean compressSynchronousPDQImage = Instance.compressSynchronousPDQImage(pDQImage, quality.ordinal(), pDQBuffer);
                return compressSynchronousPDQImage;
            }
            return false;
        }
    }

    private native boolean compressSynchronousImage(Image image, int i, PDQBuffer pDQBuffer);

    private native boolean compressSynchronousPDQImage(PDQImage pDQImage, int i, PDQBuffer pDQBuffer);

    private native boolean compressSynchronousYUVImage(YuvImage yuvImage, int i, PDQBuffer pDQBuffer);

    public static boolean compressYuvImage(YuvImage yuvImage, quality quality, PDQBuffer pDQBuffer) {
        if (!NativeOK) {
            return false;
        }
        synchronized (Instance) {
            if (Instance.Running) {
                boolean compressSynchronousYUVImage = Instance.compressSynchronousYUVImage(yuvImage, quality.ordinal(), pDQBuffer);
                return compressSynchronousYUVImage;
            }
            return false;
        }
    }

    public static CompressionService getInstance() {
        return Instance;
    }

    private static boolean init(int i) {
        if (i == 0) {
            i = Runtime.getRuntime().availableProcessors();
        }
        synchronized (InitLock) {
            if (Instance == null) {
                Instance = new CompressionService();
            }
        }
        if (!NativeOK) {
            return false;
        }
        synchronized (Instance) {
            if (Instance.CompressionThreads > 0 && Instance.CompressionThreads != i) {
                Instance.shutdown();
            }
            if (Instance.CompressionThreads == i) {
                return true;
            }
            CompressionService compressionService = Instance;
            if (i > 8) {
                i = 8;
            } else if (i <= 0) {
                i = 1;
            }
            compressionService.CompressionThreads = i;
            Instance.Running = Instance.setupCompression(Instance.CompressionThreads);
            boolean z = Instance.Running;
            return z;
        }
    }

    public static boolean initialize(int i) {
        return init(i);
    }

    private native boolean setupCompression(int i);

    public synchronized void shutdown() {
        synchronized (Instance) {
            this.Running = false;
        }
        synchronized (InitLock) {
            cleanupCompression();
            this.CompressionThreads = 0;
        }
    }
}

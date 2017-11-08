package org.mtnwrw.pdqimg;

import java.nio.ByteBuffer;

/* compiled from: Unknown */
public class PDQBuffer {
    private static boolean NativeOK;
    private long Base = 0;
    private ByteBuffer Buffer = null;

    /* compiled from: Unknown */
    public static class PDQBufferError extends Exception {
        PDQBufferError(String str) {
            super(str);
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

    private PDQBuffer() {
    }

    public static PDQBuffer allocateBuffer(long j) throws PDQBufferError {
        Object obj = null;
        if (j > 0) {
            obj = 1;
        }
        if (obj == null) {
            throw new PDQBufferError("Illegal size " + j + " specified");
        } else if (NativeOK) {
            PDQBuffer allocateBufferNative = allocateBufferNative(j);
            if (allocateBufferNative != null) {
                return allocateBufferNative;
            }
            throw new PDQBufferError("Unable to allocate buffer of size " + j);
        } else {
            throw new PDQBufferError("Native code not initialized");
        }
    }

    private static native PDQBuffer allocateBufferNative(long j);

    private static native void releaseBufferNative(PDQBuffer pDQBuffer);

    protected void finalize() {
        synchronized (this) {
            if (this.Buffer != null && NativeOK) {
                releaseBufferNative(this);
            }
        }
    }

    public synchronized ByteBuffer getBuffer() throws PDQBufferError {
        if (this.Buffer == null) {
            throw new PDQBufferError("Trying to work with invalid buffer");
        }
        return this.Buffer;
    }

    public synchronized boolean isValid() {
        return this.Buffer != null;
    }

    public synchronized void release() {
        if (this.Buffer != null) {
            if (NativeOK) {
                releaseBufferNative(this);
            }
        }
    }
}

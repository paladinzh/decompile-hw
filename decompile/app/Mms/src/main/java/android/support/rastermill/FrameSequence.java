package android.support.rastermill;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import java.io.InputStream;

public class FrameSequence {
    private final int mDefaultLoopCount;
    private final int mFrameCount;
    private final int mHeight;
    private final long mNativeFrameSequence;
    private final boolean mOpaque;
    private final int mWidth;

    static class State {
        private long mNativeState;

        public State(long nativeState) {
            this.mNativeState = nativeState;
        }

        public void destroy() {
            if (this.mNativeState != 0) {
                FrameSequence.nativeDestroyState(this.mNativeState);
                this.mNativeState = 0;
            }
        }

        public long getFrame(int frameNr, Bitmap output, int previousFrameNr) {
            if (output == null || output.getConfig() != Config.ARGB_8888) {
                throw new IllegalArgumentException("Bitmap passed must be non-null and ARGB_8888");
            } else if (this.mNativeState != 0) {
                return FrameSequence.nativeGetFrame(this.mNativeState, frameNr, output, previousFrameNr);
            } else {
                throw new IllegalStateException("attempted to draw destroyed FrameSequenceState");
            }
        }
    }

    private static native long nativeCreateState(long j);

    private static native FrameSequence nativeDecodeStream(InputStream inputStream, byte[] bArr);

    private static native void nativeDestroyFrameSequence(long j);

    private static native void nativeDestroyState(long j);

    private static native long nativeGetFrame(long j, int i, Bitmap bitmap, int i2);

    static {
        System.loadLibrary("framesequence");
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public boolean isOpaque() {
        return this.mOpaque;
    }

    public int getFrameCount() {
        return this.mFrameCount;
    }

    public int getDefaultLoopCount() {
        return this.mDefaultLoopCount;
    }

    public static FrameSequence decodeStream(InputStream stream) {
        if (stream != null) {
            return nativeDecodeStream(stream, new byte[16384]);
        }
        throw new IllegalArgumentException();
    }

    State createState() {
        if (this.mNativeFrameSequence == 0) {
            throw new IllegalStateException("attempted to use incorrectly built FrameSequence");
        }
        long nativeState = nativeCreateState(this.mNativeFrameSequence);
        if (nativeState == 0) {
            return null;
        }
        return new State(nativeState);
    }

    protected void finalize() throws Throwable {
        try {
            if (this.mNativeFrameSequence != 0) {
                nativeDestroyFrameSequence(this.mNativeFrameSequence);
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }
}

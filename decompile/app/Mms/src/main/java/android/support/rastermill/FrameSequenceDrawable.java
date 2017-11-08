package android.support.rastermill;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;

public class FrameSequenceDrawable extends Drawable implements Animatable, Runnable {
    private static BitmapProvider sAllocatingBitmapProvider = new BitmapProvider() {
        public Bitmap acquireBitmap(int minWidth, int minHeight) {
            return Bitmap.createBitmap(minWidth, minHeight, Config.ARGB_8888);
        }

        public void releaseBitmap(Bitmap bitmap) {
        }
    };
    private static HandlerThread sDecodingThread;
    private static Handler sDecodingThreadHandler;
    private static final Object sLock = new Object();
    private Bitmap mBackBitmap;
    private BitmapShader mBackBitmapShader;
    private final BitmapProvider mBitmapProvider;
    private Runnable mCallbackRunnable;
    private boolean mCircleMaskEnabled;
    private int mCurrentLoop;
    private Runnable mDecodeRunnable;
    private boolean mDestroyed;
    private final FrameSequence mFrameSequence;
    private final State mFrameSequenceState;
    private Bitmap mFrontBitmap;
    private BitmapShader mFrontBitmapShader;
    private long mLastSwap;
    private final Object mLock;
    private int mLoopBehavior;
    private int mNextFrameToDecode;
    private long mNextSwap;
    private OnFinishedListener mOnFinishedListener;
    private final Paint mPaint;
    private final Rect mSrcRect;
    private int mState;

    public interface BitmapProvider {
        Bitmap acquireBitmap(int i, int i2);

        void releaseBitmap(Bitmap bitmap);
    }

    public interface OnFinishedListener {
        void onFinished(FrameSequenceDrawable frameSequenceDrawable);
    }

    private static void initializeDecodingThread() {
        synchronized (sLock) {
            if (sDecodingThread != null) {
                return;
            }
            sDecodingThread = new HandlerThread("FrameSequence decoding thread", 10);
            sDecodingThread.start();
            sDecodingThreadHandler = new Handler(sDecodingThread.getLooper());
        }
    }

    private static Bitmap acquireAndValidateBitmap(BitmapProvider bitmapProvider, int minWidth, int minHeight) {
        Bitmap bitmap = bitmapProvider.acquireBitmap(minWidth, minHeight);
        if (bitmap.getWidth() >= minWidth && bitmap.getHeight() >= minHeight && bitmap.getConfig() == Config.ARGB_8888) {
            return bitmap;
        }
        throw new IllegalArgumentException("Invalid bitmap provided");
    }

    public FrameSequenceDrawable(FrameSequence frameSequence) {
        this(frameSequence, sAllocatingBitmapProvider);
    }

    public FrameSequenceDrawable(FrameSequence frameSequence, BitmapProvider bitmapProvider) {
        this.mLock = new Object();
        this.mDestroyed = false;
        this.mLoopBehavior = 3;
        this.mDecodeRunnable = new Runnable() {
            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                synchronized (FrameSequenceDrawable.this.mLock) {
                    if (FrameSequenceDrawable.this.mDestroyed) {
                        return;
                    }
                    int nextFrame = FrameSequenceDrawable.this.mNextFrameToDecode;
                    if (nextFrame < 0) {
                    } else {
                        Bitmap bitmap = FrameSequenceDrawable.this.mBackBitmap;
                        FrameSequenceDrawable.this.mState = 2;
                    }
                }
            }
        };
        this.mCallbackRunnable = new Runnable() {
            public void run() {
                if (FrameSequenceDrawable.this.mOnFinishedListener != null) {
                    FrameSequenceDrawable.this.mOnFinishedListener.onFinished(FrameSequenceDrawable.this);
                }
            }
        };
        if (frameSequence == null || bitmapProvider == null) {
            throw new IllegalArgumentException();
        }
        this.mFrameSequence = frameSequence;
        this.mFrameSequenceState = frameSequence.createState();
        int width = frameSequence.getWidth();
        int height = frameSequence.getHeight();
        this.mBitmapProvider = bitmapProvider;
        this.mFrontBitmap = acquireAndValidateBitmap(bitmapProvider, width, height);
        this.mBackBitmap = acquireAndValidateBitmap(bitmapProvider, width, height);
        this.mSrcRect = new Rect(0, 0, width, height);
        this.mPaint = new Paint();
        this.mPaint.setFilterBitmap(true);
        this.mFrontBitmapShader = new BitmapShader(this.mFrontBitmap, TileMode.CLAMP, TileMode.CLAMP);
        this.mBackBitmapShader = new BitmapShader(this.mBackBitmap, TileMode.CLAMP, TileMode.CLAMP);
        this.mLastSwap = 0;
        this.mNextFrameToDecode = -1;
        this.mFrameSequenceState.getFrame(0, this.mFrontBitmap, -1);
        initializeDecodingThread();
    }

    private void checkDestroyedLocked() {
        if (this.mDestroyed) {
            throw new IllegalStateException("Cannot perform operation on recycled drawable");
        }
    }

    public void destroy() {
        if (this.mBitmapProvider == null) {
            throw new IllegalStateException("BitmapProvider must be non-null");
        }
        Bitmap bitmapToReleaseA;
        Bitmap bitmap = null;
        synchronized (this.mLock) {
            checkDestroyedLocked();
            bitmapToReleaseA = this.mFrontBitmap;
            this.mFrontBitmap = null;
            if (this.mState != 2) {
                bitmap = this.mBackBitmap;
                this.mBackBitmap = null;
            }
            this.mDestroyed = true;
        }
        this.mBitmapProvider.releaseBitmap(bitmapToReleaseA);
        if (bitmap != null) {
            this.mBitmapProvider.releaseBitmap(bitmap);
        }
    }

    protected void finalize() throws Throwable {
        try {
            this.mFrameSequenceState.destroy();
        } finally {
            super.finalize();
        }
    }

    public void draw(Canvas canvas) {
        synchronized (this.mLock) {
            checkDestroyedLocked();
            if (this.mState == 3 && this.mNextSwap - SystemClock.uptimeMillis() <= 0) {
                this.mState = 4;
            }
            if (isRunning() && this.mState == 4) {
                Bitmap tmp = this.mBackBitmap;
                this.mBackBitmap = this.mFrontBitmap;
                this.mFrontBitmap = tmp;
                BitmapShader tmpShader = this.mBackBitmapShader;
                this.mBackBitmapShader = this.mFrontBitmapShader;
                this.mFrontBitmapShader = tmpShader;
                this.mLastSwap = SystemClock.uptimeMillis();
                boolean continueLooping = true;
                if (this.mNextFrameToDecode == this.mFrameSequence.getFrameCount() - 1) {
                    this.mCurrentLoop++;
                    if (!(this.mLoopBehavior == 1 && this.mCurrentLoop == 1)) {
                        if (this.mLoopBehavior == 3 && this.mCurrentLoop == this.mFrameSequence.getDefaultLoopCount()) {
                        }
                    }
                    continueLooping = false;
                }
                if (continueLooping) {
                    scheduleDecodeLocked();
                } else {
                    scheduleSelf(this.mCallbackRunnable, 0);
                }
            }
        }
        if (this.mCircleMaskEnabled) {
            Rect bounds = getBounds();
            this.mPaint.setShader(this.mFrontBitmapShader);
            float width = (float) bounds.width();
            float height = (float) bounds.height();
            canvas.drawCircle(width / 2.0f, height / 2.0f, Math.min(width, height) / 2.0f, this.mPaint);
            return;
        }
        this.mPaint.setShader(null);
        canvas.drawBitmap(this.mFrontBitmap, this.mSrcRect, getBounds(), this.mPaint);
    }

    private void scheduleDecodeLocked() {
        this.mState = 1;
        this.mNextFrameToDecode = (this.mNextFrameToDecode + 1) % this.mFrameSequence.getFrameCount();
        sDecodingThreadHandler.post(this.mDecodeRunnable);
    }

    public void run() {
        boolean invalidate = false;
        synchronized (this.mLock) {
            if (this.mNextFrameToDecode >= 0 && this.mState == 3) {
                this.mState = 4;
                invalidate = true;
            }
        }
        if (invalidate) {
            invalidateSelf();
        }
    }

    public void start() {
        if (!isRunning()) {
            synchronized (this.mLock) {
                checkDestroyedLocked();
                if (this.mState == 1) {
                } else {
                    this.mCurrentLoop = 0;
                    scheduleDecodeLocked();
                }
            }
        }
    }

    public void stop() {
        if (isRunning()) {
            unscheduleSelf(this);
        }
    }

    public boolean isRunning() {
        boolean z = false;
        synchronized (this.mLock) {
            if (this.mNextFrameToDecode > -1 && !this.mDestroyed) {
                z = true;
            }
        }
        return z;
    }

    public void unscheduleSelf(Runnable what) {
        synchronized (this.mLock) {
            this.mNextFrameToDecode = -1;
            this.mState = 0;
        }
        super.unscheduleSelf(what);
    }

    public boolean setVisible(boolean visible, boolean restart) {
        boolean changed = super.setVisible(visible, restart);
        if (!visible) {
            stop();
        } else if (restart || changed) {
            stop();
            start();
        }
        return changed;
    }

    public void setFilterBitmap(boolean filter) {
        this.mPaint.setFilterBitmap(filter);
    }

    public void setAlpha(int alpha) {
        this.mPaint.setAlpha(alpha);
    }

    public void setColorFilter(ColorFilter colorFilter) {
        this.mPaint.setColorFilter(colorFilter);
    }

    public int getIntrinsicWidth() {
        return this.mFrameSequence.getWidth();
    }

    public int getIntrinsicHeight() {
        return this.mFrameSequence.getHeight();
    }

    public int getOpacity() {
        return this.mFrameSequence.isOpaque() ? -1 : -2;
    }
}

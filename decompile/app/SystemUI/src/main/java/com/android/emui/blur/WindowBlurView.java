package com.android.emui.blur;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import com.android.emui.blur.WindowBlur.OnBlurObserver;
import com.android.systemui.utils.HwLog;

public class WindowBlurView extends View implements OnBlurObserver {
    private int mAlpha = 255;
    private boolean mAnimate = false;
    private boolean mAutoStart = false;
    protected BitmapDrawable mBlurDrawable;
    private int mBlurRadius = 10;
    public BlurStateListener mBlurStateListener;
    protected final Drawable mDefaultDrawable = new ColorDrawable(-16777216);
    private Runnable mFrashRunnable = new Runnable() {
        public void run() {
            WindowBlurView.this.refresh();
        }
    };
    private BitmapDrawable mLastDrawable;
    private Drawable mMaskDrawable;
    protected int mMaxLayer = Integer.MAX_VALUE;
    protected int mMinLayer = 0;
    private long mRefreshDuration = -1;
    private boolean mReleased = false;
    protected float mScale = 1.0f;
    protected Rect mShotRect;
    private boolean mShowBlur = true;
    protected int mStartLeft = 0;
    protected int mStartTop = 0;
    private long mSwitchTime = 0;
    private WindowBlur mWindowBlur;

    public interface BlurStateListener {
        void onBlurFailed();

        void onBlurSuccess(Bitmap bitmap);
    }

    public WindowBlurView(Context context) {
        super(context);
        initBlurArg();
    }

    public WindowBlurView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initBlurArg();
    }

    public WindowBlurView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initBlurArg();
    }

    public WindowBlurView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initBlurArg();
    }

    private void initBlurArg() {
        this.mBlurRadius = (int) (getContext().getResources().getDisplayMetrics().density * 10.0f);
        if (this.mBlurRadius > 25) {
            this.mBlurRadius = 25;
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        refreshCoordinate();
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        refreshCoordinate();
    }

    public void setProgress(float delta) {
        int alpha = (int) (255.0f * delta);
        if (this.mAlpha != alpha) {
            this.mAlpha = alpha;
            HwLog.i("WindowBlurView", "setProgress: " + alpha);
            if (this.mAlpha > 255) {
                this.mAlpha = 255;
            } else if (this.mAlpha < 0) {
                this.mAlpha = 0;
            }
            if (this.mBlurDrawable != null) {
                this.mBlurDrawable.setAlpha(this.mAlpha);
            }
            if (this.mMaskDrawable != null) {
                this.mMaskDrawable.setAlpha(this.mAlpha);
            }
            if (this.mDefaultDrawable != null) {
                this.mDefaultDrawable.setAlpha(this.mAlpha);
            }
            invalidate();
        }
    }

    public void setShowBlur(boolean showBlur) {
        if (this.mShowBlur != showBlur) {
            this.mShowBlur = showBlur;
            invalidate();
        }
    }

    public void setMaskDrawable(Drawable able) {
        this.mMaskDrawable = able;
    }

    private void releaseBitmapDrawable(BitmapDrawable able) {
        if (able != null) {
            Bitmap bitmap = able.getBitmap();
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        HwLog.d("WindowBlurView", "onDraw mBlurDrawable=" + this.mBlurDrawable + " mAlpha:" + this.mAlpha + " mStartLeft:" + this.mStartLeft + " mStartTop:" + this.mStartTop + " getRight:" + getRight() + " getBottom:" + getBottom());
        if (this.mBlurDrawable != null) {
            canvas.save();
            canvas.clipRect(0, 0, getRight(), getBottom());
            if (this.mAnimate) {
                drawLastDrawable(canvas);
                float enterAlpha = (float) (this.mLastDrawable == null ? 255 : 255 - this.mLastDrawable.getAlpha());
                if (enterAlpha < 255.0f) {
                    this.mBlurDrawable.setAlpha((int) ((((float) this.mAlpha) * enterAlpha) / 255.0f));
                    invalidate();
                } else {
                    this.mBlurDrawable.setAlpha(this.mAlpha);
                }
            }
            if (this.mShowBlur) {
                this.mBlurDrawable.draw(canvas);
                canvas.restore();
            }
            if (this.mMaskDrawable != null) {
                this.mMaskDrawable.setBounds(0, 0, getWidth(), getHeight());
                this.mMaskDrawable.draw(canvas);
            }
        } else if (this.mDefaultDrawable != null) {
            this.mDefaultDrawable.setBounds(0, 0, getRight(), getBottom());
            this.mDefaultDrawable.draw(canvas);
        }
    }

    private void drawLastDrawable(Canvas canvas) {
        if (this.mLastDrawable != null) {
            int alpha = (((int) (System.currentTimeMillis() - this.mSwitchTime)) * 255) / 200;
            if (alpha > 255) {
                alpha = 255;
            } else if (alpha < 0) {
                alpha = 0;
            }
            if (alpha == 255) {
                releaseBitmapDrawable(this.mLastDrawable);
                this.mLastDrawable = null;
            } else {
                this.mLastDrawable.setAlpha(255 - alpha);
                this.mLastDrawable.draw(canvas);
            }
        }
    }

    public void refresh() {
        HwLog.i("WindowBlurView", "refresh");
        this.mReleased = false;
        if (this.mWindowBlur == null) {
            this.mWindowBlur = new WindowBlur(getContext());
            this.mWindowBlur.setOnBlurObserver(this);
        }
        this.mWindowBlur.setBlurRadius(this.mBlurRadius);
        this.mWindowBlur.start();
    }

    public void setRrefreshDuration(long mills) {
        this.mRefreshDuration = mills;
        if (-1 == this.mRefreshDuration) {
            this.mAutoStart = false;
            removeCallbacks(this.mFrashRunnable);
            return;
        }
        if (this.mRefreshDuration < 1000) {
            this.mRefreshDuration = 1000;
        }
        this.mAutoStart = true;
    }

    public void setScreenLayer(int minLayer, int maxLayer) {
        this.mMinLayer = minLayer;
        this.mMaxLayer = maxLayer;
    }

    public void setScale(float scale) {
        this.mScale = scale;
    }

    public void setBlurRadius(int blurRadius) {
        this.mBlurRadius = blurRadius;
    }

    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == 0) {
            refresh();
        }
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == 0) {
            refresh();
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        checkAutoFresh();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private void refreshCoordinate() {
        int[] location = getLocationOnScreen();
        this.mStartLeft = location[0];
        this.mStartTop = location[1];
        setDrawableBound();
    }

    private void setDrawableBound() {
        setDrawableBound(this.mBlurDrawable);
    }

    protected void setDrawableBound(Drawable able) {
        if (able != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay().getRealMetrics(displayMetrics);
            able.setBounds(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean checkAutoFresh() {
        if (this.mRefreshDuration < 0 || !this.mAutoStart || getWindowVisibility() != 0 || !isAttachedToWindow()) {
            return false;
        }
        postDelayed(this.mFrashRunnable, this.mRefreshDuration);
        return true;
    }

    public void onBlurFinish(Bitmap blurBitmap) {
        HwLog.i("WindowBlurView", "onBlurFinish");
        if (this.mReleased) {
            if (blurBitmap != null) {
                blurBitmap.recycle();
            }
            return;
        }
        Bitmap currBitmap;
        if (this.mBlurDrawable == null) {
            currBitmap = null;
        } else {
            currBitmap = this.mBlurDrawable.getBitmap();
        }
        if (!(currBitmap == null || currBitmap == blurBitmap)) {
            this.mLastDrawable = this.mBlurDrawable;
            this.mSwitchTime = System.currentTimeMillis();
            setDrawableBound(this.mLastDrawable);
        }
        if (blurBitmap != null) {
            this.mBlurDrawable = new BitmapDrawable(getContext().getResources(), blurBitmap);
            this.mBlurDrawable.setAlpha(this.mAlpha);
            setDrawableBound();
        } else {
            this.mBlurDrawable = null;
        }
        invalidate();
        if (this.mBlurStateListener != null) {
            if (this.mBlurDrawable == null && this.mDefaultDrawable == null) {
                Log.d("WindowBlurView", "blur failed");
                this.mBlurStateListener.onBlurFailed();
            } else {
                this.mBlurStateListener.onBlurSuccess(blurBitmap);
            }
        }
    }

    public Bitmap getBaseBitmap() {
        return HwScreenShot.screenShotBitmap(getContext(), this.mMinLayer, this.mMaxLayer, this.mScale, this.mShotRect);
    }

    public void setBlurStateListener(BlurStateListener blurStateListener) {
        this.mBlurStateListener = blurStateListener;
    }
}

package com.android.mms.attachment.ui.mediapicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.places.PlacesStatusCodes;

public class BaseSoundModelAnimationView extends View {
    private static final int[] DRAWABLE_LEVEL = new int[]{0, LocationRequest.PRIORITY_NO_POWER, 265, 502, 852, 1379, 2185, 3314, 4488, 5438, 6166, 6740, 7204, 7589, 7917, 8199, 8443, 8654, 8843, PlacesStatusCodes.KEY_EXPIRED, 9155, 9284, 9399, 9501, 9590, 9670, 9740, 9800, 9853, 9898, 9936, 9968, 9994};
    public int mAlpha;
    protected Drawable mBackgroundDrawable;
    public Bitmap mBitmapInner;
    public Bitmap mBitmapOuter;
    protected Status mCurrentStatus;
    public Handler mHandler;
    public int mIndex;
    public Matrix mMatrix;
    public Drawable mMicProcessingGlowDrawable;
    public float mOuterZoom;
    protected Paint mPaint;
    public boolean mPressed;
    public int mRecordingBarResId;
    public int mSoundLevel;
    public float mZoomHalfPx;

    protected enum Status {
        INIT,
        RECORDING,
        SPEECHING,
        PROCESSING,
        MICOCCUPIED,
        UNKNOWN
    }

    public BaseSoundModelAnimationView(Context context) {
        this(context, null);
    }

    public BaseSoundModelAnimationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseSoundModelAnimationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mIndex = 0;
        this.mBackgroundDrawable = null;
        this.mMicProcessingGlowDrawable = null;
        this.mPressed = false;
        this.mSoundLevel = 0;
        this.mOuterZoom = ContentUtil.FONT_SIZE_NORMAL;
        this.mBitmapOuter = null;
        this.mBitmapInner = null;
        this.mZoomHalfPx = 288.0f;
        this.mMatrix = new Matrix();
        this.mPaint = new Paint();
        this.mAlpha = 255;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        BaseSoundModelAnimationView.this.handleRecording();
                        return;
                    case 2:
                        BaseSoundModelAnimationView.this.handleSpeeching(msg.arg1);
                        return;
                    case 3:
                        BaseSoundModelAnimationView.this.handleProcessing();
                        return;
                    default:
                        return;
                }
            }
        };
    }

    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (Status.MICOCCUPIED != this.mCurrentStatus) {
        }
    }

    public int getDrawableAlpha() {
        int alpha = 255;
        float zoom = this.mOuterZoom - 0.371f;
        if (zoom <= 0.074f) {
            alpha = (int) (zoom * 3460.0f);
        } else if (zoom >= 0.555f) {
            alpha = (255 - ((int) ((zoom - 0.555f) * 3460.0f))) + 1;
        }
        if (alpha > 255) {
            return 255;
        }
        return alpha;
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        Drawable d = this.mBackgroundDrawable;
        if (d != null && d.isStateful()) {
            d.setState(getDrawableState());
        }
        Drawable d4 = this.mMicProcessingGlowDrawable;
        if (d4 != null && d4.isStateful()) {
            d4.setState(getDrawableState());
        }
    }

    public void setVisibility(int visibility) {
        boolean z = true;
        super.setVisibility(visibility);
        if (this.mBackgroundDrawable != null) {
            boolean z2;
            Drawable drawable = this.mBackgroundDrawable;
            if (visibility == 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            drawable.setVisible(z2, false);
        }
        if (this.mMicProcessingGlowDrawable != null) {
            Drawable drawable2 = this.mMicProcessingGlowDrawable;
            if (visibility != 0) {
                z = false;
            }
            drawable2.setVisible(z, false);
        }
    }

    protected void onAttachedToWindow() {
        boolean z = true;
        super.onAttachedToWindow();
        if (this.mBackgroundDrawable != null) {
            boolean z2;
            Drawable drawable = this.mBackgroundDrawable;
            if (getVisibility() == 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            drawable.setVisible(z2, false);
        }
        if (this.mMicProcessingGlowDrawable != null) {
            Drawable drawable2 = this.mMicProcessingGlowDrawable;
            if (getVisibility() != 0) {
                z = false;
            }
            drawable2.setVisible(z, false);
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mBackgroundDrawable != null) {
            this.mBackgroundDrawable.setVisible(false, false);
        }
        if (this.mMicProcessingGlowDrawable != null) {
            this.mMicProcessingGlowDrawable.setVisible(false, false);
        }
    }

    public void clearHandlerMessages() {
        if (this.mHandler != null) {
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(2);
            this.mHandler.removeMessages(3);
        }
    }

    public void handleProcessing() {
        invalidate();
        if (this.mCurrentStatus == Status.PROCESSING) {
            if (this.mMicProcessingGlowDrawable != null) {
                this.mMicProcessingGlowDrawable.setLevel(DRAWABLE_LEVEL[this.mIndex]);
                this.mIndex++;
            }
            if (this.mIndex > DRAWABLE_LEVEL.length - 1) {
                this.mIndex = 0;
            }
            Message msg1 = new Message();
            msg1.what = 3;
            this.mHandler.sendMessageDelayed(msg1, 30);
        }
    }

    public void handleSpeeching(int soundLevel) {
        if (this.mCurrentStatus == Status.SPEECHING) {
            this.mSoundLevel = soundLevel;
            invalidate();
        }
    }

    public void handleRecording() {
        invalidate();
        if (this.mCurrentStatus == Status.RECORDING) {
            Message msg1 = new Message();
            msg1.what = 1;
            this.mHandler.sendMessageDelayed(msg1, 30);
        }
    }

    public void destoryAnimationView() {
        if (!(this.mCurrentStatus == Status.INIT || this.mCurrentStatus == Status.UNKNOWN)) {
            clearHandlerMessages();
        }
        if (!(this.mBitmapInner == null || this.mBitmapInner.isRecycled())) {
            this.mBitmapInner.recycle();
        }
        if (!(this.mBitmapOuter == null || this.mBitmapOuter.isRecycled())) {
            this.mBitmapOuter.recycle();
        }
        setBackground(null);
        if (this.mBackgroundDrawable != null) {
            this.mBackgroundDrawable = null;
        }
    }
}

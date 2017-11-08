package com.android.mms.attachment.ui.mediapicker;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;

public class RcsSoundModelAnimationView extends BaseSoundModelAnimationView {
    private static final /* synthetic */ int[] -com-android-mms-attachment-ui-mediapicker-BaseSoundModelAnimationView$StatusSwitchesValues = null;

    private static /* synthetic */ int[] -getcom-android-mms-attachment-ui-mediapicker-BaseSoundModelAnimationView$StatusSwitchesValues() {
        if (-com-android-mms-attachment-ui-mediapicker-BaseSoundModelAnimationView$StatusSwitchesValues != null) {
            return -com-android-mms-attachment-ui-mediapicker-BaseSoundModelAnimationView$StatusSwitchesValues;
        }
        int[] iArr = new int[Status.values().length];
        try {
            iArr[Status.INIT.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Status.MICOCCUPIED.ordinal()] = 5;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Status.PROCESSING.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Status.RECORDING.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Status.SPEECHING.ordinal()] = 4;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Status.UNKNOWN.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        -com-android-mms-attachment-ui-mediapicker-BaseSoundModelAnimationView$StatusSwitchesValues = iArr;
        return iArr;
    }

    public RcsSoundModelAnimationView(Context context) {
        this(context, null);
    }

    public RcsSoundModelAnimationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RcsSoundModelAnimationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mCurrentStatus = Status.UNKNOWN;
        this.mContext = context;
        try {
            this.mBitmapInner = BitmapFactory.decodeResource(getResources(), R.drawable.mic_st_widget_speeching_glow);
        } catch (OutOfMemoryError e) {
            MLog.e("SoundLevelsAnimationWidgetView", "Bitmap OOM!!!");
        }
        this.mPaint.setFlags(1);
        try {
            this.mBackgroundDrawable = getResources().getDrawable(R.drawable.rcs_mic_widget_normal);
        } catch (OutOfMemoryError e2) {
            MLog.e("SoundLevelsAnimationWidgetView", "Background OOM!!!");
        }
        if (this.mBackgroundDrawable != null) {
            setBackgroundDrawable(this.mBackgroundDrawable);
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!this.mPressed) {
            this.mRecordingBarResId = R.drawable.rcs_mic_st_widget_recording_bar_glow;
            this.mBitmapOuter = BitmapFactory.decodeResource(getResources(), this.mRecordingBarResId);
            if (this.mBitmapOuter != null) {
                this.mZoomHalfPx = ((float) this.mBitmapOuter.getWidth()) / 2.0f;
            }
            switch (-getcom-android-mms-attachment-ui-mediapicker-BaseSoundModelAnimationView$StatusSwitchesValues()[this.mCurrentStatus.ordinal()]) {
                case 1:
                    break;
                case 2:
                    if (this.mMicProcessingGlowDrawable != null) {
                        this.mMicProcessingGlowDrawable.draw(canvas);
                        break;
                    }
                    break;
                case 3:
                    if (this.mBitmapOuter == null) {
                        this.mBitmapOuter = BitmapFactory.decodeResource(getResources(), this.mRecordingBarResId);
                    }
                    this.mOuterZoom += 0.0185f;
                    if (this.mOuterZoom > ContentUtil.FONT_SIZE_NORMAL) {
                        this.mOuterZoom = 0.63265f;
                    }
                    this.mAlpha = getDrawableAlpha();
                    this.mPaint.setAlpha(this.mAlpha);
                    this.mMatrix.setScale(this.mOuterZoom, this.mOuterZoom, this.mZoomHalfPx, this.mZoomHalfPx);
                    canvas.drawBitmap(this.mBitmapOuter, this.mMatrix, this.mPaint);
                    break;
                case 4:
                    if (this.mBitmapOuter == null) {
                        this.mBitmapOuter = BitmapFactory.decodeResource(getResources(), this.mRecordingBarResId);
                    }
                    this.mOuterZoom += 0.0185f;
                    if (this.mOuterZoom > ContentUtil.FONT_SIZE_NORMAL) {
                        this.mOuterZoom = 0.63265f;
                    }
                    this.mAlpha = getDrawableAlpha();
                    this.mPaint.setAlpha(this.mAlpha);
                    this.mMatrix.setScale(this.mOuterZoom, this.mOuterZoom, this.mZoomHalfPx, this.mZoomHalfPx);
                    canvas.drawBitmap(this.mBitmapOuter, this.mMatrix, this.mPaint);
                    if (this.mBitmapInner == null) {
                        this.mBitmapInner = BitmapFactory.decodeResource(getResources(), R.drawable.mic_st_widget_speeching_glow);
                    }
                    MLog.d("RcsSoundModelAnimationView", "SPEECHINGmSoundLevel " + this.mSoundLevel);
                    this.mPaint.setAlpha(255);
                    float innerZoom = (((float) this.mSoundLevel) * 0.13f) + 0.2f;
                    this.mMatrix.setScale(innerZoom, innerZoom, this.mZoomHalfPx, this.mZoomHalfPx);
                    canvas.drawBitmap(this.mBitmapInner, this.mMatrix, this.mPaint);
                    break;
                default:
                    MLog.d("RcsSoundModelAnimationView", "onDraw enter default");
                    break;
            }
        }
    }

    public void startRecordingAnimation() {
        MLog.d("RcsSoundModelAnimationView", "startAnimation Recording");
        if (this.mCurrentStatus != Status.RECORDING) {
            this.mBackgroundDrawable = getResources().getDrawable(R.drawable.rcs_mic_widget_normal);
            setBackgroundDrawable(this.mBackgroundDrawable);
            this.mCurrentStatus = Status.RECORDING;
            this.mHandler.sendEmptyMessage(1);
        }
    }

    public void stopAndClearAnimations() {
        if (this.mCurrentStatus != Status.INIT && this.mCurrentStatus != Status.UNKNOWN) {
            MLog.d("RcsSoundModelAnimationView", "stopAndClearAnimations");
            clearHandlerMessages();
            this.mCurrentStatus = Status.INIT;
            this.mBackgroundDrawable = getResources().getDrawable(R.drawable.rcs_mic_widget_normal);
            setBackgroundDrawable(this.mBackgroundDrawable);
        }
    }
}

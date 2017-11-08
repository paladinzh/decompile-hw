package com.huawei.systemmanager.mainscreen.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import com.huawei.systemmanager.comm.anima.AnimaRoller;
import com.huawei.systemmanager.comm.widget.CircleViewNew;
import com.huawei.systemmanager.util.HwLog;

public class MainCircleProgressView extends CircleViewNew {
    public static final int SHADE_REF = 28;
    private static final String TAG = "MainCircleProgressView";
    private int alphaRef;
    public boolean isShading;
    private final float mArcEndAngle;
    private Status mCompleteStatus;
    private Status mCurrentStatus;
    private AnimaRoller mRoller;
    private Runnable mRunnable;
    private Status mScanStatus;
    private int value;

    private static class Status {
        private Status() {
        }

        public void enter() {
        }

        public void onDraw(Canvas canvas) {
        }
    }

    private class CompleteStatus extends Status {
        private CompleteStatus() {
            super();
        }

        public void enter() {
            MainCircleProgressView.this.isShading = false;
        }

        public void onDraw(Canvas canvas) {
            MainCircleProgressView.this.drawBackground(canvas);
            MainCircleProgressView.this.drawPartProgress(canvas, 0.0f, (((MainCircleProgressView.this.mArcEndAngle - 0.0f) * ((float) MainCircleProgressView.this.value)) / 100.0f) + 0.0f);
        }
    }

    private class ScanStatus extends Status {
        private static final float SWEEP_SPEED = 3.0f;
        float mRotateAngle;

        private ScanStatus() {
            super();
            this.mRotateAngle = 0.0f;
        }

        public void enter() {
            MainCircleProgressView.this.alphaRef = 0;
        }

        public void onDraw(Canvas canvas) {
            this.mRotateAngle += SWEEP_SPEED;
            if (this.mRotateAngle > 360.0f) {
                this.mRotateAngle -= 360.0f;
            }
            if (MainCircleProgressView.this.alphaRef > 28) {
                MainCircleProgressView.this.alphaRef = 28;
            }
            MainCircleProgressView.this.drawBackground(canvas);
            if (MainCircleProgressView.this.isShading) {
                MainCircleProgressView.this.drawCircle_roll(canvas, this.mRotateAngle, MainCircleProgressView.this.getProgressPaint(), MainCircleProgressView.this.alphaRef);
                MainCircleProgressView mainCircleProgressView = MainCircleProgressView.this;
                mainCircleProgressView.alphaRef = mainCircleProgressView.alphaRef + 1;
                MainCircleProgressView.this.invalidate();
                return;
            }
            MainCircleProgressView.this.drawCircle_roll(canvas, this.mRotateAngle, MainCircleProgressView.this.getProgressPaint(), 0);
            MainCircleProgressView.this.invalidate();
        }
    }

    public MainCircleProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainCircleProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.value = 0;
        this.isShading = false;
        this.mScanStatus = new ScanStatus();
        this.mCompleteStatus = new CompleteStatus();
        this.mCurrentStatus = this.mScanStatus;
        this.mRunnable = new Runnable() {
            public void run() {
                if (MainCircleProgressView.this.mRoller == null) {
                    HwLog.e(MainCircleProgressView.TAG, "mRunnable mRoller is null!");
                    return;
                }
                boolean more = MainCircleProgressView.this.mRoller.computeRoll();
                MainCircleProgressView.this.updateScoreInner((int) MainCircleProgressView.this.mRoller.value());
                if (more) {
                    MainCircleProgressView.this.post(MainCircleProgressView.this.mRunnable);
                }
            }
        };
        this.mArcEndAngle = 360.0f;
    }

    protected void onDraw(Canvas canvas) {
        if (this.mCurrentStatus != null) {
            this.mCurrentStatus.onDraw(canvas);
        }
    }

    protected void drawPartProgress(Canvas canvas, float startAngle, float endAngle) {
        drawCircle_color(canvas, startAngle, endAngle, getProgressPaint());
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(this.mRunnable);
    }

    public void updateScoreImmidiately(int target) {
        updateSocre(target, 0);
    }

    public void updateScore(int target) {
        updateSocre(target, 800);
    }

    public void updateSocre(int target, int duration) {
        removeCallbacks(this.mRunnable);
        if (duration <= 0 || this.mCurrentStatus != this.mCompleteStatus) {
            HwLog.w(TAG, "setNumberByDuration, duration is nagative:" + duration + ", or curStatus is not completeStatus");
            updateScoreInner(target);
            return;
        }
        if (this.mRoller == null) {
            this.mRoller = new AnimaRoller();
            this.mRoller.startRoll((float) this.value, (float) target, duration);
        } else {
            this.mRoller.continueRoll((float) target, duration);
        }
        post(this.mRunnable);
    }

    private void updateScoreInner(int score) {
        this.value = score;
        invalidate();
    }

    public void setCompleteStatus() {
        setStatus(this.mCompleteStatus);
    }

    private void setStatus(Status s) {
        if (s != null) {
            s.enter();
        }
        this.mCurrentStatus = s;
        invalidate();
    }
}

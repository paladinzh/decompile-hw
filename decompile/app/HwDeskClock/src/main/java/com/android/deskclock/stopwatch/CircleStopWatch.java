package com.android.deskclock.stopwatch;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;
import com.android.deskclock.DeskClockApplication;
import com.android.deskclock.R;
import com.android.deskclock.stopwatch.PanelPath.Callback;
import com.android.util.HwLog;
import com.android.util.Utils;
import java.lang.ref.WeakReference;
import java.util.Locale;

public class CircleStopWatch extends View implements Callback {
    private static final int FHD_DIGITAL_HEIGHT = (Utils.getCurrentDisplayMetricsDensity() * 98);
    private int afterAlpha;
    private int alphaBg;
    private int alphaPre;
    private int beforeAlpha;
    private Drawable bgNumber;
    private long chazhi;
    private float degree;
    private float density;
    private String drawStrBack;
    private String drawStrPre;
    private String drawStrShow;
    private FontMetrics fm;
    private float fmHeight;
    private LinearInterpolator interpolator;
    private boolean isCalculation;
    private boolean isFirStartAnimate;
    private boolean isShowCountNum;
    private boolean isStartRun;
    private Drawable littleDiscal;
    private boolean mAttached;
    private final Interpolator mBallInterpolator;
    private Rect mBgNumRect;
    private boolean mChanged;
    private ValueAnimator mCircleHoleAnimator;
    private int mCount;
    private PaintFlagsDrawFilter mDrawFilter;
    private LocalHandler mHandler;
    private int mHoleDialHeight;
    private int mHoleDialWidth;
    private Rect mHoleRect;
    private long mLastClickTime;
    private Paint mLinePaint;
    private int mNumDialHeight;
    private int mNumDialWidth;
    private Paint mPaint;
    private PanelPath mPanelPath;
    private long mPauseTime;
    private int mProcessDialHeight;
    private int mProcessDialWidth;
    private Rect mProcessRect;
    private int mQCount;
    private float mScaleHole;
    private long mStartTime;
    private ValueAnimator mTextAlphaAnimator;
    private Paint mTextPaintBg;
    private Paint mTextPaintPre;
    private Paint mTextPaintShow;
    private int mUperDialHeight;
    private int mUperDialWidth;
    private Rect mUpperRect;
    private float offset;
    private Drawable processingDiscal;
    private float scale;
    private boolean scaled;
    private boolean showTextAnimator;
    private float startScale;
    private int statusType;
    private Drawable upperDiscal;
    private int x;
    private int y;

    static class LocalHandler extends Handler {
        private WeakReference<CircleStopWatch> mView;

        public LocalHandler(CircleStopWatch context) {
            this.mView = new WeakReference(context);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            CircleStopWatch playStatusView = (CircleStopWatch) this.mView.get();
            if (playStatusView != null) {
                switch (msg.what) {
                    case 500:
                        playStatusView.invalidate();
                        return;
                    default:
                        return;
                }
            }
        }
    }

    public CircleStopWatch(Context context) {
        this(context, null);
    }

    public CircleStopWatch(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleStopWatch(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.scale = 1.0f;
        this.x = 0;
        this.y = 0;
        this.scaled = false;
        this.density = 3.0f;
        this.startScale = 0.6f;
        this.beforeAlpha = 0;
        this.afterAlpha = 0;
        this.showTextAnimator = false;
        this.drawStrPre = "";
        this.drawStrBack = "";
        this.drawStrShow = "";
        this.mCount = 0;
        this.mQCount = 0;
        this.offset = 1.0f;
        this.alphaPre = 255;
        this.alphaBg = 0;
        this.mHandler = new LocalHandler(this);
        this.isFirStartAnimate = true;
        this.chazhi = 0;
        this.mPauseTime = 0;
        this.degree = 0.0f;
        this.mStartTime = -1;
        this.interpolator = new LinearInterpolator();
        this.mScaleHole = 0.0f;
        this.mBallInterpolator = new PathInterpolator(0.72f, 0.06f, 0.13f, 1.0f);
        this.isShowCountNum = false;
        this.mLastClickTime = -1;
        this.isCalculation = false;
        this.isStartRun = false;
        this.statusType = 0;
        this.mPanelPath = new PanelPath(getContext(), this);
        float mDex = 1.0f;
        if (DeskClockApplication.isBtvPadDevice()) {
            mDex = 0.8f;
        }
        this.littleDiscal = getResources().getDrawable(R.drawable.img_clock_stopwatch_littledial);
        this.processingDiscal = getResources().getDrawable(R.drawable.img_clock_stopwatch_processing);
        this.upperDiscal = getResources().getDrawable(R.drawable.analogclock_widget_preview);
        this.bgNumber = getResources().getDrawable(R.drawable.img_clock_stopwatch_littledial_number);
        this.mHoleDialHeight = this.littleDiscal.getIntrinsicHeight();
        this.mHoleDialWidth = this.littleDiscal.getIntrinsicWidth();
        this.mProcessDialWidth = this.processingDiscal.getIntrinsicWidth();
        this.mProcessDialHeight = this.processingDiscal.getIntrinsicHeight();
        this.mUperDialWidth = (int) (((float) this.upperDiscal.getIntrinsicWidth()) * mDex);
        this.mUperDialHeight = (int) (((float) this.upperDiscal.getIntrinsicHeight()) * mDex);
        this.mNumDialWidth = this.bgNumber.getIntrinsicWidth();
        this.mNumDialHeight = this.bgNumber.getIntrinsicHeight();
        this.density = getResources().getDisplayMetrics().density;
        this.mPaint = new Paint();
        this.mPaint.setStyle(Style.FILL);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setFilterBitmap(true);
        this.mPaint.setDither(true);
        this.mPaint.setStrokeWidth(this.density * 1.0f);
        this.mDrawFilter = new PaintFlagsDrawFilter(0, 2);
        this.mHoleRect = new Rect();
        this.mProcessRect = new Rect();
        this.mUpperRect = new Rect();
        this.mBgNumRect = new Rect();
        this.mLinePaint = new Paint();
        this.mLinePaint.setStyle(Style.STROKE);
        this.mLinePaint.setAntiAlias(true);
        this.mLinePaint.setFilterBitmap(true);
        this.mLinePaint.setDither(true);
        this.mLinePaint.setStrokeWidth(this.density * 0.3f);
        this.mTextPaintPre = new Paint();
        this.mTextPaintPre.setStyle(Style.STROKE);
        this.mTextPaintPre.setAntiAlias(true);
        this.mTextPaintPre.setFilterBitmap(true);
        this.mTextPaintPre.setDither(true);
        this.mTextPaintPre.setTextSize(this.density * 12.0f);
        this.mTextPaintPre.setTextAlign(Align.LEFT);
        this.mTextPaintBg = new Paint();
        this.mTextPaintBg.setStyle(Style.STROKE);
        this.mTextPaintBg.setAntiAlias(true);
        this.mTextPaintBg.setFilterBitmap(true);
        this.mTextPaintBg.setDither(true);
        this.mTextPaintBg.setTextSize(this.density * 12.0f);
        this.mTextPaintBg.setTextAlign(Align.LEFT);
        this.mTextPaintShow = new Paint();
        this.mTextPaintShow.setStyle(Style.STROKE);
        this.mTextPaintShow.setAntiAlias(true);
        this.mTextPaintShow.setFilterBitmap(true);
        this.mTextPaintShow.setDither(true);
        this.mTextPaintShow.setTextSize(this.density * 12.0f);
        this.mTextPaintShow.setTextAlign(Align.LEFT);
        this.startScale = ((float) this.mUperDialWidth) / ((float) this.mHoleDialWidth);
        initAnimation();
    }

    private void initAnimation() {
        this.mTextAlphaAnimator = ValueAnimator.ofInt(new int[]{0, 255});
        this.mTextAlphaAnimator.setStartDelay(733);
        this.mTextAlphaAnimator.setDuration(270);
        this.mTextAlphaAnimator.setInterpolator(this.mBallInterpolator);
        this.mTextAlphaAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                int alpha = ((Integer) animation.getAnimatedValue()).intValue();
                CircleStopWatch.this.beforeAlpha = alpha;
                CircleStopWatch.this.afterAlpha = 255 - alpha;
            }
        });
        this.mTextAlphaAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                CircleStopWatch.this.showTextAnimator = true;
                CircleStopWatch.this.updateCountText(CircleStopWatch.this.mCount);
            }

            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                CircleStopWatch.this.showTextAnimator = false;
            }
        });
        this.mCircleHoleAnimator = ValueAnimator.ofFloat(new float[]{this.startScale, 1.0f});
        this.mCircleHoleAnimator.setStartDelay(333);
        this.mCircleHoleAnimator.setDuration(400);
        this.mCircleHoleAnimator.setInterpolator(this.mBallInterpolator);
        this.mCircleHoleAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                CircleStopWatch.this.mScaleHole = ((Float) animation.getAnimatedValue()).floatValue();
            }
        });
        this.mCircleHoleAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                CircleStopWatch.this.mScaleHole = 0.0f;
            }

            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                CircleStopWatch.this.isFirStartAnimate = false;
            }
        });
    }

    private void quickCountAction() {
        if (this.mTextAlphaAnimator != null && this.mTextAlphaAnimator.isStarted()) {
            this.mTextAlphaAnimator.end();
        }
        updateCountText(this.mQCount);
    }

    private void updateCountText(int count) {
        Locale locale = Locale.getDefault();
        this.drawStrPre = String.format(locale, "%2d", new Object[]{Integer.valueOf(count - 1)});
        this.drawStrBack = String.format(locale, "%2d", new Object[]{Integer.valueOf(count)});
        this.drawStrShow = String.format(locale, "%2d", new Object[]{Integer.valueOf(count)});
    }

    private void calculateAlphaAnimation() {
        int alphaA = 0;
        int alphaB = 0;
        if (this.mTextAlphaAnimator != null && this.mTextAlphaAnimator.isRunning()) {
            alphaA = this.beforeAlpha;
            alphaB = this.afterAlpha;
        }
        alphaA %= 256;
        alphaB %= 256;
        if (this.mCount == 1) {
            this.alphaPre = 0;
            this.alphaBg = alphaA;
            return;
        }
        this.alphaPre = alphaB;
        this.alphaBg = alphaA;
    }

    private void drawTextView(Canvas canvas) {
        calculateAlphaAnimation();
        float moveX = (float) (((double) ((((float) this.mHoleRect.width()) / 2.0f) + (this.offset * this.density))) * Math.sin(0.7853981633974483d));
        float moveY = (float) (((double) ((((float) this.mHoleRect.width()) / 2.0f) + (this.offset * this.density))) * Math.cos(0.7853981633974483d));
        if (this.showTextAnimator) {
            this.mTextPaintPre.setAlpha(this.alphaPre);
            this.fm = this.mTextPaintPre.getFontMetrics();
            this.fmHeight = (this.fm.ascent + this.fm.descent) / 2.0f;
            float textWidth = this.mTextPaintPre.measureText(this.drawStrPre);
            canvas.save();
            canvas.translate(moveX, -moveY);
            canvas.drawText(this.drawStrPre, ((float) this.x) - (textWidth / 2.0f), (((float) this.y) - this.fmHeight) + 3.0f, this.mTextPaintPre);
            canvas.restore();
            this.mTextPaintBg.setAlpha(this.alphaBg);
            this.fm = this.mTextPaintBg.getFontMetrics();
            this.fmHeight = (this.fm.ascent + this.fm.descent) / 2.0f;
            textWidth = this.mTextPaintBg.measureText(this.drawStrBack);
            canvas.save();
            canvas.translate(moveX, -moveY);
            canvas.drawText(this.drawStrBack, ((float) this.x) - (textWidth / 2.0f), (((float) this.y) - this.fmHeight) + 3.0f, this.mTextPaintBg);
            canvas.restore();
            return;
        }
        this.mTextPaintShow.setAlpha(255);
        this.fm = this.mTextPaintShow.getFontMetrics();
        this.fmHeight = (this.fm.ascent + this.fm.descent) / 2.0f;
        textWidth = this.mTextPaintShow.measureText(this.drawStrShow);
        canvas.save();
        canvas.translate(moveX, -moveY);
        canvas.drawText(this.drawStrShow, ((float) this.x) - (textWidth / 2.0f), (((float) this.y) - this.fmHeight) + 3.0f, this.mTextPaintShow);
        canvas.restore();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!this.mAttached) {
            this.mAttached = true;
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mAttached) {
            this.mAttached = false;
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        float hScale = 1.0f;
        float vScale = 1.0f;
        if (widthMode != 0 && widthSize < this.mHoleDialWidth) {
            hScale = ((float) widthSize) / ((float) this.mHoleDialWidth);
        }
        if (heightMode != 0 && heightSize < this.mHoleDialHeight) {
            vScale = ((float) heightSize) / ((float) this.mHoleDialHeight);
        }
        this.scale = Math.min(hScale, vScale);
        setMeasuredDimension(resolveSizeAndState((int) (((float) this.mHoleDialWidth) * this.scale), widthMeasureSpec, 0), resolveSizeAndState((int) (((float) this.mHoleDialHeight) * this.scale), heightMeasureSpec, 0));
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int availableWidth = this.mRight - this.mLeft;
        int availableHeight = this.mBottom - this.mTop;
        this.x = availableWidth >> 1;
        this.y = availableHeight >> 1;
        if (availableWidth < this.mHoleDialWidth || availableHeight < this.mHoleDialHeight) {
            this.scaled = true;
            this.scale = Math.min(((float) availableWidth) / ((float) this.mHoleDialWidth), ((float) availableHeight) / ((float) this.mHoleDialHeight));
        }
        if (availableWidth > this.mHoleDialWidth || availableHeight > this.mHoleDialHeight) {
            this.scaled = true;
            this.scale = Math.min(((float) availableWidth) / ((float) this.mHoleDialWidth), ((float) availableHeight) / ((float) this.mHoleDialHeight));
        }
        this.mHoleRect.set(this.x - (this.mHoleDialWidth >> 1), this.y - (this.mHoleDialHeight >> 1), this.x + (this.mHoleDialWidth >> 1), this.y + (this.mHoleDialHeight >> 1));
        this.mProcessRect.set(this.x - (this.mProcessDialWidth >> 1), this.y - (this.mProcessDialHeight >> 1), this.x + (this.mProcessDialWidth >> 1), this.y + (this.mProcessDialHeight >> 1));
        this.mUpperRect.set(this.x - (this.mUperDialWidth >> 1), this.y - (this.mUperDialHeight >> 1), this.x + (this.mUperDialWidth >> 1), this.y + (this.mUperDialHeight >> 1));
        this.mBgNumRect.set(this.x - (this.mNumDialWidth >> 1), this.y - (this.mNumDialHeight >> 1), this.x + (this.mNumDialWidth >> 1), this.y + (this.mNumDialHeight >> 1));
        this.mChanged = true;
        this.mPanelPath.setPosition(this.x, this.y, (float) (this.mUperDialWidth >> 1), (float) (this.mNumDialWidth >> 1), ((float) (this.mHoleDialWidth >> 1)) + (this.offset * this.density));
    }

    public void writeToSharedPrefForStatus(SharedPreferences prefs, String key) {
        Editor editor = prefs.edit();
        editor.putInt(key + "ring_statusType", this.statusType);
        editor.apply();
    }

    public void writeToSharedPref(SharedPreferences prefs, String key) {
        Editor editor = prefs.edit();
        editor.putInt(key + "ring_count", this.mQCount > this.mCount ? this.mQCount : this.mCount);
        editor.putString(key + "ring_count_text_show", this.drawStrShow);
        editor.putString(key + "ring_count_text_pre", this.drawStrPre);
        editor.putString(key + "ring_count_text_back", this.drawStrBack);
        editor.putLong(key + "ring_chazhi", this.chazhi);
        editor.putBoolean(key + "ring_isstartrun", this.isStartRun);
        editor.putLong(key + "ring_pauseTime", this.mPauseTime);
        editor.putFloat(key + "ring_degree", this.degree);
        editor.putLong(key + "ring_startTime", this.mStartTime);
        editor.putInt(key + "ring_statusType", this.statusType);
        editor.apply();
    }

    public void readFromSharedPref(SharedPreferences prefs, String key, int state) {
        this.mCount = prefs.getInt(key + "ring_count", 0);
        this.drawStrPre = prefs.getString(key + "ring_count_text_pre", "");
        this.drawStrBack = prefs.getString(key + "ring_count_text_back", "");
        this.drawStrShow = prefs.getString(key + "ring_count_text_show", "");
        this.isStartRun = prefs.getBoolean(key + "ring_isstartrun", false);
        this.chazhi = prefs.getLong(key + "ring_chazhi", 0);
        this.mPauseTime = prefs.getLong(key + "ring_pauseTime", 0);
        this.degree = prefs.getFloat(key + "ring_degree", 0.0f);
        this.mStartTime = prefs.getLong(key + "ring_startTime", -1);
        this.statusType = prefs.getInt(key + "ring_statusType", 0);
        if (this.mCount != 0) {
            this.isShowCountNum = true;
            this.isFirStartAnimate = false;
            if (this.mPanelPath != null) {
                this.mPanelPath.setIsFirstBall(false);
            }
            updateCountText(this.mCount);
            invalidate();
        }
    }

    private void caculateHoleScale() {
        if (!this.isFirStartAnimate) {
            this.mScaleHole = 1.0f;
        }
    }

    private void drawBGHoleView(Canvas canvas) {
        caculateHoleScale();
        canvas.save();
        canvas.scale(this.mScaleHole, this.mScaleHole, (float) this.x, (float) this.y);
        this.littleDiscal.setBounds(this.mHoleRect);
        this.littleDiscal.draw(canvas);
        canvas.restore();
    }

    private void drawRotatingDisc(Canvas canvas) {
        canvas.save();
        canvas.rotate(this.degree, (float) this.x, (float) this.y);
        this.processingDiscal.setBounds(this.mProcessRect);
        this.processingDiscal.draw(canvas);
        canvas.restore();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(this.mDrawFilter);
        if (this.scaled) {
            canvas.save();
            canvas.scale(this.scale, this.scale, (float) this.x, (float) this.y);
        }
        if (this.isStartRun && this.statusType == 1) {
            updateDegree();
        }
        drawRotatingDisc(canvas);
        if (this.isShowCountNum) {
            drawBGHoleView(canvas);
        }
        drawBubblesView(canvas);
        if (this.isShowCountNum) {
            drawTextView(canvas);
        }
        if (this.scaled) {
            canvas.restore();
        }
        if (this.mChanged) {
            this.mChanged = false;
        }
        if (this.isStartRun) {
            if (this.mHandler.hasMessages(500)) {
                this.mHandler.removeMessages(500);
            }
            this.mHandler.sendEmptyMessageDelayed(500, 35);
        }
    }

    private void updateDegree() {
        long time = System.currentTimeMillis();
        if (-1 == this.mStartTime) {
            this.mStartTime = time;
        }
        this.chazhi = (time - this.mStartTime) + this.mPauseTime;
        this.degree = this.interpolator.getInterpolation(((float) (this.chazhi % 20000)) / 20000.0f) * 360.0f;
    }

    public void increaseCount(int count) {
        this.mCount = count;
        this.mQCount = count;
    }

    public void startCountAnimation(boolean isStart, int count) {
        if (this.isCalculation) {
            if (this.mTextAlphaAnimator != null && this.mTextAlphaAnimator.isRunning()) {
                this.mTextAlphaAnimator.end();
            }
            long time = System.currentTimeMillis();
            if (-1 == this.mLastClickTime) {
                this.mLastClickTime = time;
            } else {
                this.isFirStartAnimate = false;
            }
            long chazhi = time - this.mLastClickTime;
            if (chazhi < 400 && chazhi > 0) {
                quickCountAction();
            } else if (this.mTextAlphaAnimator != null) {
                this.mTextAlphaAnimator.start();
            }
            this.mLastClickTime = time;
            if (this.isFirStartAnimate) {
                this.mCircleHoleAnimator.start();
            }
            start();
            this.isShowCountNum = true;
            postInvalidate();
        }
    }

    public void startAnimation() {
        if (!this.isStartRun || this.statusType != 1) {
            this.mStartTime = -1;
            if (this.statusType == 0) {
                this.mPauseTime = 0;
                this.mCount = 0;
                this.mQCount = 0;
            }
            this.isStartRun = true;
            this.statusType = 1;
            this.isCalculation = true;
            postInvalidate();
        }
    }

    public void stopAnimation() {
        this.degree = 0.0f;
        this.isCalculation = false;
        this.isFirStartAnimate = true;
        this.mScaleHole = 0.0f;
        this.isStartRun = false;
        this.statusType = 0;
        this.mStartTime = -1;
        this.mPauseTime = 0;
        this.mCount = 0;
        this.mQCount = 0;
        this.isShowCountNum = false;
        this.alphaPre = 0;
        this.alphaBg = 0;
        this.drawStrPre = "";
        this.drawStrBack = "";
        this.drawStrShow = "";
        if (this.mTextAlphaAnimator != null && this.mTextAlphaAnimator.isRunning()) {
            this.mTextAlphaAnimator.end();
        }
        if (this.mCircleHoleAnimator != null && this.mCircleHoleAnimator.isRunning()) {
            this.mCircleHoleAnimator.end();
        }
        this.mLastClickTime = -1;
        this.mPanelPath.resetTO();
        postInvalidate();
    }

    public void pauseAnimation() {
        if (this.isStartRun && this.statusType != 2) {
            checkCount();
            this.mStartTime = -1;
            this.statusType = 2;
            this.mPauseTime = this.chazhi;
            this.isCalculation = false;
            this.isStartRun = false;
            postInvalidate();
        }
    }

    private void checkCount() {
        int drawshowCount = 0;
        try {
            if (!TextUtils.isEmpty(this.drawStrShow)) {
                drawshowCount = Integer.parseInt(this.drawStrShow);
            }
            if (drawshowCount != this.mCount) {
                HwLog.i("CircleStopWatch", "show count is error.");
                updateCountText(this.mCount);
            }
        } catch (NumberFormatException e) {
            HwLog.w("CircleStopWatch", "number format errror");
        }
    }

    public void onUpdateUI() {
        invalidate();
    }

    private void drawBubblesView(Canvas canvas) {
        this.mPanelPath.draw(canvas);
    }

    public void start() {
        this.mPanelPath.start();
    }
}

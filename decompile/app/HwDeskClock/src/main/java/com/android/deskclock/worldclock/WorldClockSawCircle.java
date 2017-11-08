package com.android.deskclock.worldclock;

import android.animation.TimeInterpolator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import com.android.deskclock.R;
import com.huawei.hwtransition.interpolator.TriangleInterpolator;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.TimeZone;

public class WorldClockSawCircle extends View {
    private int LINE_MAX_HEIGHT;
    private int arh;
    private int arw;
    int heightScale;
    private Drawable mAnimatePoint;
    private int mAnimatePointHeight;
    private int mAnimatePointWidth;
    private boolean mAttached;
    private Calendar mCalendar;
    private boolean mChanged;
    private float mDegree;
    private int mDialHeight;
    private int mDialWidth;
    private Bitmap mDiscalBuffer;
    private PaintFlagsDrawFilter mDrawFilter;
    private LocalHandler mHandler;
    private final BroadcastReceiver mIntentReceiver;
    private TimeInterpolator mInterpolator;
    private int mLineHeight;
    private Paint mPaint;
    private float mPaintWidth;
    private int mPeriod;
    private int mPointRect;
    private Rect mRect;
    private int mRh;
    private Rect rect;
    private Rect rectLine;
    private float scale;
    private boolean scaled;
    int widthScale;
    private int x;
    private int y;

    static class LocalHandler extends Handler {
        private WeakReference<WorldClockSawCircle> mContextWR;

        public LocalHandler(WorldClockSawCircle context) {
            this.mContextWR = new WeakReference(context);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            WorldClockSawCircle wCircle = (WorldClockSawCircle) this.mContextWR.get();
            if (wCircle != null) {
                switch (msg.what) {
                    case 1:
                        if (hasMessages(1)) {
                            removeMessages(1);
                        }
                        wCircle.invalidate();
                        return;
                    default:
                        return;
                }
            }
        }
    }

    public WorldClockSawCircle(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WorldClockSawCircle(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mPaintWidth = 2.5f;
        this.scale = 1.0f;
        this.mPeriod = 1000;
        this.x = 0;
        this.y = 0;
        this.scaled = false;
        this.LINE_MAX_HEIGHT = 66;
        this.mLineHeight = 34;
        this.mPointRect = 48;
        this.mInterpolator = new TriangleInterpolator();
        this.mDegree = 0.0f;
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    if ("android.intent.action.TIMEZONE_CHANGED".equals(intent.getAction())) {
                        String tz = intent.getStringExtra("time-zone");
                        if (tz != null) {
                            WorldClockSawCircle.this.mCalendar = Calendar.getInstance(TimeZone.getTimeZone(tz));
                        } else {
                            return;
                        }
                    }
                    WorldClockSawCircle.this.onTimeChanged();
                    WorldClockSawCircle.this.invalidate();
                }
            }
        };
        this.LINE_MAX_HEIGHT = getResources().getDimensionPixelSize(R.dimen.line_max_height);
        this.mLineHeight = getResources().getDimensionPixelSize(R.dimen.line_height);
        this.mPointRect = getResources().getDimensionPixelSize(R.dimen.point_rect_main);
        Drawable mDiscal = getResources().getDrawable(R.drawable.time_circle);
        this.mAnimatePoint = getResources().getDrawable(R.drawable.ic_animate_point);
        this.mPaintWidth = (float) getResources().getDimensionPixelSize(R.dimen.worldclock_circle_paint);
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setColor(-1);
        this.mPaint.setStrokeWidth(2.0f);
        this.mDialWidth = mDiscal.getIntrinsicWidth();
        this.mDialHeight = mDiscal.getIntrinsicHeight();
        this.mAnimatePointWidth = this.mAnimatePoint.getIntrinsicWidth();
        this.mAnimatePointHeight = this.mAnimatePoint.getIntrinsicHeight();
        this.mDrawFilter = new PaintFlagsDrawFilter(0, 2);
        this.mRect = new Rect();
        this.rect = new Rect();
        this.rectLine = new Rect();
        this.mCalendar = Calendar.getInstance();
        this.mHandler = new LocalHandler(this);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!this.mAttached) {
            this.mAttached = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.TIME_SET");
            filter.addAction("android.intent.action.TIMEZONE_CHANGED");
            getContext().registerReceiver(this.mIntentReceiver, filter, null, this.mHandler);
        }
        this.mCalendar = Calendar.getInstance();
        onTimeChanged();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mHandler.removeCallbacksAndMessages(null);
        if (this.mAttached) {
            this.mAttached = false;
            getContext().unregisterReceiver(this.mIntentReceiver);
        }
        if (this.mDiscalBuffer != null) {
            this.mDiscalBuffer.recycle();
        }
        this.mDiscalBuffer = null;
        if (this.mCalendar != null) {
            this.mCalendar.clear();
            this.mCalendar = null;
        }
        this.mAnimatePoint = null;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        float hScale = 1.0f;
        float vScale = 1.0f;
        if (widthMode != 0 && widthSize < this.mDialWidth + this.LINE_MAX_HEIGHT) {
            hScale = ((float) widthSize) / ((float) (this.mDialWidth + this.LINE_MAX_HEIGHT));
        }
        if (heightMode != 0 && heightSize < this.mDialHeight + this.LINE_MAX_HEIGHT) {
            vScale = ((float) heightSize) / ((float) (this.mDialHeight + this.LINE_MAX_HEIGHT));
        }
        this.scale = Math.min(hScale, vScale);
        setMeasuredDimension(resolveSizeAndState((int) (((float) (this.mDialWidth + this.LINE_MAX_HEIGHT)) * this.scale), widthMeasureSpec, 0), resolveSizeAndState((int) (((float) (this.mDialHeight + this.LINE_MAX_HEIGHT)) * this.scale), heightMeasureSpec, 0));
        this.rect.set(this.x - this.arw, (this.y - this.mRh) + this.mPointRect, this.x + this.arw, ((this.y - this.mRh) + (this.arh << 1)) + this.mPointRect);
        this.widthScale = (int) (((double) (((float) this.mDialWidth) * this.scale)) + 0.5d);
        this.heightScale = (int) (((double) (((float) this.mDialHeight) * this.scale)) + 0.5d);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int availableWidth = this.mRight - this.mLeft;
        int availableHeight = this.mBottom - this.mTop;
        this.x = availableWidth >> 1;
        this.y = availableHeight >> 1;
        if (availableWidth < this.mDialWidth + this.LINE_MAX_HEIGHT || availableHeight < this.mDialHeight + this.LINE_MAX_HEIGHT) {
            this.scaled = true;
            this.scale = Math.min(((float) availableWidth) / ((float) (this.mDialWidth + this.LINE_MAX_HEIGHT)), ((float) availableHeight) / ((float) (this.mDialHeight + this.LINE_MAX_HEIGHT)));
        }
        this.mRect.set(this.x - (this.mDialWidth >> 1), this.y - (this.mDialHeight >> 1), this.x + (this.mDialWidth >> 1), this.y + (this.mDialHeight >> 1));
        this.mChanged = true;
        this.mRh = this.mDialHeight >> 1;
        this.arw = this.mAnimatePointWidth >> 1;
        this.arh = this.mAnimatePointHeight >> 1;
        this.rect.set(this.x - this.arw, (this.y - this.mRh) + this.mPointRect, this.x + this.arw, ((this.y - this.mRh) + (this.arh << 1)) + this.mPointRect);
        this.rectLine.set(this.x - 1, this.y - this.mRh, this.x + 1, (this.y - this.mRh) + this.mLineHeight);
        this.widthScale = (int) (((double) (((float) this.mDialWidth) * this.scale)) + 0.5d);
        this.heightScale = (int) (((double) (((float) this.mDialHeight) * this.scale)) + 0.5d);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        onTimeChanged();
        if (this.scaled) {
            canvas.save();
            canvas.scale(this.scale, this.scale, (float) this.x, (float) this.y);
        }
        this.mCalendar = Calendar.getInstance();
        this.mDegree = ((float) (this.mCalendar.getTimeInMillis() % 60000)) * 0.006f;
        canvas.setDrawFilter(this.mDrawFilter);
        canvas.save();
        this.mAnimatePoint.setBounds(this.rect);
        canvas.rotate(this.mDegree, (float) this.x, (float) this.y);
        this.mAnimatePoint.draw(canvas);
        canvas.restore();
        drawRaised(canvas, this.mDegree);
        if (this.scaled) {
            canvas.restore();
        }
        drawDiscal(canvas);
        if (this.mChanged) {
            this.mChanged = false;
        }
        this.mHandler.sendEmptyMessageDelayed(1, 45);
    }

    private void drawDiscal(Canvas canvas) {
        if (this.mDiscalBuffer == null) {
            this.mDiscalBuffer = Bitmap.createBitmap(this.widthScale, this.heightScale, Config.ARGB_4444);
            Canvas c = new Canvas(this.mDiscalBuffer);
            c.setDrawFilter(this.mDrawFilter);
            float cxScale = (float) (this.widthScale >> 1);
            float cyScale = (float) (this.heightScale >> 1);
            int rScale = (this.widthScale + this.heightScale) >> 2;
            int lengthScale = (int) (((float) this.mLineHeight) * this.scale);
            this.mPaint.setStrokeWidth(this.mPaintWidth * this.scale);
            for (int i = 0; i < 120; i++) {
                c.save();
                c.rotate((float) (i * 3), cxScale, cyScale);
                float f = cxScale;
                c.drawLine(cxScale, cyScale - ((float) rScale), f, ((float) lengthScale) + (cyScale - ((float) rScale)), this.mPaint);
                c.restore();
            }
            this.mPaint.setStrokeWidth(this.mPaintWidth);
        }
        canvas.drawBitmap(this.mDiscalBuffer, (float) (this.x - (this.widthScale >> 1)), (float) (this.y - (this.heightScale >> 1)), null);
    }

    private void drawRaised(Canvas canvas, float degree) {
        degree %= 360.0f;
        int indexCenterBar = (int) (degree / 3.0f);
        float angleDelta = degree - (((float) indexCenterBar) * 3.0f);
        for (int i = 0; i <= 16; i++) {
            float dt = (((float) (16 - i)) + (angleDelta / 3.0f)) / 16.0f;
            if (dt > 1.0f) {
                dt = 1.0f;
            }
            dt = this.mInterpolator.getInterpolation(dt);
            canvas.save();
            canvas.rotate(((float) ((i - 8) + indexCenterBar)) * 3.0f, (float) this.x, (float) this.y);
            canvas.drawLine((float) this.x, 1.0f + (((float) (this.y - this.mRh)) - (((float) this.mLineHeight) * dt)), (float) this.x, (float) ((this.y - this.mRh) + 1), this.mPaint);
            canvas.restore();
        }
    }

    private void onTimeChanged() {
        this.mChanged = true;
    }
}

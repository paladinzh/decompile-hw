package com.android.deskclock.widgetlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import com.android.alarmclock.WorldAnalogClock;
import com.android.deskclock.R;
import com.android.deskclock.R$styleable;
import com.android.util.Utils;
import java.lang.ref.WeakReference;

public class AnimateTextView extends TextView {
    private String drawStr;
    private float dx;
    private float dy;
    private FontMetrics fm;
    private float fmHeight;
    private int height;
    private long lastTime;
    private int mAppType;
    private int[] mGradientColors;
    private LocalHandler mHandler;
    private LinearInterpolator mLinearInterpolator;
    private int mOrientation;
    private Paint mPaint;
    private int mRatio;
    private boolean mStart;
    private Matrix matrix;
    private Shader shader;
    private int width;

    static class LocalHandler extends Handler {
        private WeakReference<AnimateTextView> mContextWR;

        public LocalHandler(AnimateTextView context) {
            this.mContextWR = new WeakReference(context);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            AnimateTextView animateTextView = (AnimateTextView) this.mContextWR.get();
            if (animateTextView != null) {
                switch (msg.what) {
                    case 1:
                        if (hasMessages(1)) {
                            removeMessages(1);
                        }
                        animateTextView.invalidate();
                        return;
                    default:
                        return;
                }
            }
        }
    }

    private void init() {
        this.mPaint = new Paint();
        this.mPaint.setColor(-16776961);
        this.mPaint.setAntiAlias(true);
        this.mRatio = getContext().getResources().getDimensionPixelSize(R.dimen.density_ratio);
        this.mPaint.setTextSize((float) this.mRatio);
        this.mPaint.setTextAlign(Align.CENTER);
        this.mPaint.setTypeface(Utils.getmRobotoXianBlackTypeface());
        this.mPaint.setShader(this.shader);
        this.fm = this.mPaint.getFontMetrics();
        this.fmHeight = (this.fm.ascent + this.fm.descent) / 2.0f;
        this.height = getHeight();
        this.width = getWidth();
        if (1 == this.mAppType) {
            this.drawStr = getResources().getString(R.string.tips_clock_closetimer);
        } else {
            this.drawStr = getResources().getString(R.string.tips_clock_closealarm);
        }
        this.mHandler = new LocalHandler(this);
    }

    public AnimateTextView(Context context) {
        this(context, null);
    }

    public AnimateTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimateTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, 0);
        this.mAppType = 0;
        this.dx = 0.0f;
        this.lastTime = -1;
        this.mStart = true;
        this.matrix = new Matrix();
        this.mGradientColors = new int[]{Color.argb(255, 120, 120, 120), Color.argb(255, 120, 120, 120), Color.argb(255, 255, 255, 255)};
        this.shader = new LinearGradient(0.0f, 0.0f, 200.0f, 0.0f, this.mGradientColors, new float[]{0.0f, 0.7f, 1.0f}, TileMode.MIRROR);
        this.mOrientation = 0;
        this.dy = 0.0f;
        this.mLinearInterpolator = new LinearInterpolator();
        this.mRatio = 15;
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.SliderView, defStyle, 0);
        try {
            this.mOrientation = a.getInt(0, 0);
            a = context.obtainStyledAttributes(attrs, R$styleable.AnimateView, defStyle, 0);
            try {
                this.mAppType = a.getInt(0, 0);
                if (this.mOrientation == 0) {
                    this.shader = new LinearGradient(0.0f, 0.0f, 200.0f, 0.0f, this.mGradientColors, new float[]{0.0f, 0.7f, 1.0f}, TileMode.MIRROR);
                } else if (this.mOrientation == 1) {
                    this.shader = new LinearGradient(0.0f, 0.0f, 0.0f, WorldAnalogClock.DEGREE_ONE_HOUR, this.mGradientColors, new float[]{0.0f, 0.7f, 1.0f}, TileMode.MIRROR);
                }
                init();
            } finally {
                a.recycle();
            }
        } finally {
            a.recycle();
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.width = w >> 1;
        this.height = h >> 1;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    protected void onDraw(Canvas canvas) {
        long now = System.currentTimeMillis();
        if (this.lastTime == -1) {
            this.lastTime = now;
        }
        int textWidth = (int) this.mPaint.measureText(this.drawStr);
        int textHeight = (int) this.mPaint.getTextSize();
        if (this.mOrientation == 0) {
            this.dx = ((float) this.width) + (this.mLinearInterpolator.getInterpolation(((float) ((now - this.lastTime) % 3000)) / 3000.0f) * ((float) textWidth));
            this.dy = 0.0f;
        } else if (this.mOrientation == 1) {
            this.dy = ((float) this.height) - (this.mLinearInterpolator.getInterpolation(((float) ((now - this.lastTime) % 2000)) / 2000.0f) * ((float) textHeight));
            this.dx = 0.0f;
        }
        if (this.mStart) {
            this.matrix.setTranslate(this.dx, this.dy);
            this.mHandler.sendEmptyMessageDelayed(1, 60);
        } else {
            this.matrix.setTranslate(0.0f, 0.0f);
        }
        this.shader.setLocalMatrix(this.matrix);
        canvas.drawText(this.drawStr, (float) this.width, ((float) this.height) - this.fmHeight, this.mPaint);
        super.onDraw(canvas);
    }
}

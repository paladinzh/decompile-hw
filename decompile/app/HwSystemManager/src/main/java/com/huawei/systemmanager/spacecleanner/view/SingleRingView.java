package com.huawei.systemmanager.spacecleanner.view;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.support.v4.internal.view.SupportMenu;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.CircleViewNew;

public class SingleRingView extends View {
    private int mBackGroundColor;
    private Paint mBgCirclePaint;
    private Paint mCirclePaint;
    private int mCircleWidth;
    private int[] mColors;
    private int mHeight;
    private RectF mRectF;
    private SweepGradient mSweepGradient;
    private float mValue;
    private int mWidth;

    private static class InterpolatorImpl implements Interpolator {
        private InterpolatorImpl() {
        }

        public float getInterpolation(float v) {
            return Utility.ALPHA_MAX - (((Utility.ALPHA_MAX - v) * (Utility.ALPHA_MAX - v)) * (Utility.ALPHA_MAX - v));
        }
    }

    public SingleRingView(Context context) {
        super(context);
        this.mColors = new int[2];
        this.mBgCirclePaint = new Paint();
        this.mCirclePaint = new Paint();
    }

    public SingleRingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SingleRingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mColors = new int[2];
        this.mBgCirclePaint = new Paint();
        this.mCirclePaint = new Paint();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ring_circle);
        if (a != null) {
            this.mColors[0] = a.getColor(0, SupportMenu.CATEGORY_MASK);
            this.mColors[1] = a.getColor(1, -16776961);
            this.mCircleWidth = a.getDimensionPixelOffset(2, 55);
            this.mBackGroundColor = a.getColor(3, -1);
            a.recycle();
            initCirclePaint();
            initBgCirclePaint();
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mWidth = w;
        this.mHeight = h;
        int margin = GlobalContext.getDimensionPixelOffset(R.dimen.single_ring_view_margin);
        this.mRectF = new RectF((float) ((this.mCircleWidth / 2) + margin), (float) ((this.mCircleWidth / 2) + margin), (float) ((this.mWidth - (this.mCircleWidth / 2)) - margin), (float) ((this.mHeight - (this.mCircleWidth / 2)) - margin));
        this.mSweepGradient = new SweepGradient((float) (this.mWidth / 2), (float) (this.mHeight / 2), this.mColors, null);
        this.mCirclePaint.setShader(this.mSweepGradient);
    }

    public void setValue(float value) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(new float[]{this.mValue, value});
        valueAnimator.setDuration(500);
        valueAnimator.setInterpolator(new InterpolatorImpl());
        valueAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Object value = valueAnimator.getAnimatedValue();
                if (value != null) {
                    SingleRingView.this.mValue = ((Float) value).floatValue();
                    SingleRingView.this.invalidate();
                }
            }
        });
        valueAnimator.start();
    }

    protected void onDraw(Canvas canvas) {
        canvas.drawOval(this.mRectF, this.mBgCirclePaint);
        canvas.rotate(CircleViewNew.START_ANGLE, (float) (this.mHeight / 2), (float) (this.mWidth / 2));
        canvas.drawArc(this.mRectF, 0.0f, this.mValue, false, this.mCirclePaint);
    }

    private void initCirclePaint() {
        this.mCirclePaint.setAntiAlias(true);
        this.mCirclePaint.setStyle(Style.STROKE);
        this.mCirclePaint.setStrokeWidth((float) this.mCircleWidth);
        this.mCirclePaint.setStrokeCap(Cap.ROUND);
        this.mCirclePaint.setStrokeJoin(Join.MITER);
    }

    private void initBgCirclePaint() {
        this.mBgCirclePaint.setStyle(Style.STROKE);
        this.mBgCirclePaint.setStrokeWidth((float) this.mCircleWidth);
        this.mBgCirclePaint.setColor(this.mBackGroundColor);
        this.mBgCirclePaint.setAntiAlias(true);
    }
}

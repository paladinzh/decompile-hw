package com.huawei.systemmanager.comm.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.optimize.base.Const;

public abstract class CircleBaseView extends View {
    public static final float ANGLE_MAX = 360.0f;
    public static final int COLOR_RED = GlobalContext.getContext().getResources().getColor(R.color.hsm_widget_circleview_red);
    public static final int COLOR_YELLOW = GlobalContext.getContext().getResources().getColor(R.color.hsm_widget_circleview_yellow);
    public static final int GREEN_ONE = GlobalContext.getContext().getResources().getColor(R.color.hsm_widget_circleview_green);
    public float mAngleOffset = Utility.ALPHA_MAX;
    protected float[] mCirclePoints;
    public MyRollingCommand mCounterRunable = new MyRollingCommand(this.mHandler, "CircleBaseView");
    protected DrawStatus mDrawStatus;
    public Handler mHandler = new Handler();
    protected float mHeight;
    public IdleStatus mIdleStatus = new IdleStatus();
    private Paint mPaint;
    public float mPaintLenth = 0.0f;
    protected float mRadius;
    protected float mWidth;
    protected int mX;
    protected int mY;

    public interface DrawStatus {
        boolean doDraw(Canvas canvas);

        void onDrawBegin();

        void onDrawFinished();
    }

    public class IdleStatus implements DrawStatus {
        public void onDrawBegin() {
            CircleBaseView.this.iDleBegin();
        }

        public void onDrawFinished() {
            CircleBaseView.this.iDleFinished();
        }

        public boolean doDraw(Canvas canvas) {
            return CircleBaseView.this.iDleDraw(canvas);
        }
    }

    public class MyRollingCommand extends RollingCommand {
        public MyRollingCommand(Handler handler, String type) {
            super(handler, type);
        }

        protected void onNumberUpate(int count) {
            CircleBaseView.this.setProgress((float) count);
        }
    }

    public CircleBaseView(Context context) {
        super(context);
        initView(context);
    }

    public CircleBaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public CircleBaseView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    public void setProgress(float progress) {
    }

    private void initView(Context context) {
        this.mPaint = new Paint();
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStrokeWidth(2.0f);
        this.mPaint.setColor(Color.argb(77, 255, 255, 255));
        this.mAngleOffset = context.getResources().getDimension(R.dimen.circle_view_scale);
        transState(this.mIdleStatus);
    }

    protected void onDraw(Canvas canvas) {
        canvas.drawCircle((float) this.mX, (float) this.mY, this.mRadius, this.mPaint);
        if (this.mDrawStatus != null && this.mDrawStatus.doDraw(canvas)) {
            postInvalidateDelayed(20);
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        this.mX = getWidth() / 2;
        this.mY = getHeight() / 2;
    }

    public void transState(DrawStatus target) {
        if (this.mDrawStatus == null) {
            this.mDrawStatus = target;
            return;
        }
        this.mDrawStatus.onDrawFinished();
        this.mDrawStatus = target;
        target.onDrawBegin();
    }

    public boolean iDleDraw(Canvas canvas) {
        return false;
    }

    public void iDleFinished() {
    }

    public void iDleBegin() {
    }

    public void drawCircle(Canvas canvas, float startAngle, float endAngle, Paint paint) {
        if (endAngle > 0.0f) {
            int offset = (int) ((startAngle / this.mAngleOffset) * 4.0f);
            int count = (int) (((endAngle - startAngle) / this.mAngleOffset) * 4.0f);
            if ((offset | count) >= 0 && offset + count <= this.mCirclePoints.length) {
                canvas.drawLines(this.mCirclePoints, offset, count, paint);
            }
        }
    }

    private void removeAllCallbacks() {
        this.mHandler.removeCallbacks(this.mCounterRunable);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeAllCallbacks();
    }

    public void circlePoints(float beginAngle, float endAngle) {
        this.mCirclePoints = new float[((int) ((720.0f / this.mAngleOffset) * 4.0f))];
        float angle = beginAngle;
        int i = 0;
        while (angle < endAngle) {
            int i2 = i + 1;
            this.mCirclePoints[i] = ((float) this.mX) + ((float) (((double) this.mRadius) * Math.cos(Math.toRadians((double) angle))));
            i = i2 + 1;
            this.mCirclePoints[i2] = ((float) this.mY) + ((float) (((double) this.mRadius) * Math.sin(Math.toRadians((double) angle))));
            i2 = i + 1;
            this.mCirclePoints[i] = ((float) this.mX) + ((float) (((double) (this.mRadius + this.mPaintLenth)) * Math.cos(Math.toRadians((double) angle))));
            i = i2 + 1;
            this.mCirclePoints[i2] = ((float) this.mY) + ((float) (((double) (this.mRadius + this.mPaintLenth)) * Math.sin(Math.toRadians((double) angle))));
            angle += this.mAngleOffset;
        }
    }

    public float caculateAngle(float progress) {
        int topLimit = (int) (((float) ((int) Math.floor((double) (progress / this.mAngleOffset)))) * this.mAngleOffset);
        int BottomLimit = (int) (((float) ((int) Math.ceil((double) (progress / this.mAngleOffset)))) * this.mAngleOffset);
        if (Math.abs(((float) topLimit) - progress) <= Math.abs(((float) BottomLimit) - progress)) {
            BottomLimit = topLimit;
        }
        return (float) BottomLimit;
    }

    public int getStatusColor(float rate) {
        if (Float.compare(0.7f, rate) > 0) {
            return GREEN_ONE;
        }
        if (Float.compare(Const.FREE_MEMORY_DANGEROUS_FLOAT, rate) > 0) {
            return COLOR_YELLOW;
        }
        return COLOR_RED;
    }

    protected void drawBackGround(Canvas canvas, float backWidth) {
        this.mPaint.setColor(Color.argb(77, 255, 255, 255));
        this.mPaint.setStrokeWidth(backWidth);
        drawCircle(canvas, 0.0f, 360.0f, this.mPaint);
    }
}

package com.huawei.systemmanager.spacecleanner.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;

public class RoundRadarView extends View {
    private static final String TAG = "BaseRadarView";
    private CircleDataHolder[] mCircleList;
    protected final Paint mCirclePaint;
    protected IRadarState mCurState;
    protected final Paint mLinePaint;
    protected IRadarState mRounddingState;
    protected IRadarState mStopState;
    protected float mWidth;
    protected float mXCenter;
    protected float mYCenter;

    private static class CircleDataHolder {
        int color;
        boolean enable;
        int width;

        private CircleDataHolder() {
        }
    }

    public static abstract class IRadarState {
        abstract boolean onDrawing(Canvas canvas);

        void onPrepareDraw() {
        }

        void setRoundding() {
        }

        void setStop() {
        }
    }

    class RounddingState extends IRadarState {
        private static final long SHADE_DURATION = 2000;
        private int mCircleAlpha;
        private float mRotateAngle = 0.0f;
        private Drawable mScanRegion;
        private long mTransStartTime;
        private boolean mTransToStop;

        RounddingState() {
        }

        public void onPrepareDraw() {
            this.mRotateAngle = -180.0f;
            this.mCircleAlpha = 255;
            this.mTransToStop = false;
            this.mTransStartTime = 0;
        }

        public boolean onDrawing(Canvas canvas) {
            RoundRadarView.this.drawBackground(canvas);
            canvas.save();
            int width = RoundRadarView.this.getWidth();
            canvas.rotate(this.mRotateAngle, ((float) width) / 2.0f, ((float) width) / 2.0f);
            Drawable d = getScanRegion();
            d.setBounds(0, 0, width, width);
            d.setAlpha(this.mCircleAlpha);
            d.draw(canvas);
            canvas.restore();
            this.mRotateAngle = (this.mRotateAngle + 2.0f) % 360.0f;
            if (this.mTransToStop) {
                this.mCircleAlpha -= 5;
                if (this.mCircleAlpha <= 0 || SystemClock.elapsedRealtime() - this.mTransStartTime > SHADE_DURATION) {
                    RoundRadarView.this.transState(RoundRadarView.this.mStopState);
                }
            }
            return true;
        }

        public void setStop() {
            this.mTransToStop = true;
            this.mTransStartTime = SystemClock.elapsedRealtime();
        }

        private Drawable getScanRegion() {
            if (this.mScanRegion != null) {
                return this.mScanRegion;
            }
            this.mScanRegion = RoundRadarView.this.getResources().getDrawable(R.drawable.pic_scan_light_single, null);
            return this.mScanRegion;
        }
    }

    class StopState extends IRadarState {
        StopState() {
        }

        public boolean onDrawing(Canvas canvas) {
            RoundRadarView.this.drawBackground(canvas);
            return false;
        }

        void setRoundding() {
            RoundRadarView.this.transState(RoundRadarView.this.mRounddingState);
        }
    }

    public RoundRadarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundRadarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mCircleList = new CircleDataHolder[3];
        this.mCurState = null;
        this.mRounddingState = new RounddingState();
        this.mStopState = new StopState();
        this.mCurState = this.mStopState;
        this.mLinePaint = createLinePaint(attrs);
        this.mCirclePaint = createCirclePaint(attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RadarViewAttrs, defStyleAttr, 0);
        CircleDataHolder circle1st = new CircleDataHolder();
        circle1st.enable = a.getBoolean(2, true);
        if (circle1st.enable) {
            circle1st.color = a.getColor(0, -1);
            circle1st.width = a.getDimensionPixelSize(1, 0);
        }
        this.mCircleList[0] = circle1st;
        CircleDataHolder circle2nd = new CircleDataHolder();
        circle2nd.enable = a.getBoolean(5, true);
        if (circle2nd.enable) {
            circle2nd.color = a.getColor(3, -1);
            circle2nd.width = a.getDimensionPixelSize(4, 0);
        }
        this.mCircleList[1] = circle2nd;
        CircleDataHolder circle3th = new CircleDataHolder();
        circle3th.enable = a.getBoolean(8, true);
        if (circle3th.enable) {
            circle3th.color = a.getColor(6, -1);
            circle3th.width = a.getDimensionPixelSize(7, 0);
        }
        this.mCircleList[2] = circle3th;
        a.recycle();
    }

    private Paint createLinePaint(AttributeSet attrs) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Style.STROKE);
        paint.setColor(getResources().getColor(R.color.hwsystemmanager_white_alpha30_color));
        paint.setStrokeWidth(Utility.ALPHA_MAX);
        return paint;
    }

    private Paint createCirclePaint(AttributeSet attrs) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Style.STROKE);
        return paint;
    }

    public void setRounding() {
        this.mCurState.setRoundding();
    }

    public void setStop() {
        this.mCurState.setStop();
    }

    private void drawLines(Canvas canvas) {
        int offset = this.mCircleList[2].width;
        canvas.drawLine(this.mXCenter, (float) offset, this.mXCenter, (this.mXCenter * 2.0f) - ((float) offset), this.mLinePaint);
        canvas.drawLine((float) offset, this.mXCenter, (this.mXCenter * 2.0f) - ((float) offset), this.mXCenter, this.mLinePaint);
    }

    private void drawCircles(Canvas canvas) {
        if (this.mCircleList[0].enable) {
            this.mCirclePaint.setColor(this.mCircleList[0].color);
            this.mCirclePaint.setStrokeWidth((float) this.mCircleList[0].width);
            canvas.drawCircle(this.mXCenter, this.mYCenter, this.mWidth / 6.0f, this.mCirclePaint);
        }
        if (this.mCircleList[1].enable) {
            this.mCirclePaint.setColor(this.mCircleList[1].color);
            this.mCirclePaint.setStrokeWidth((float) this.mCircleList[1].width);
            canvas.drawCircle(this.mXCenter, this.mYCenter, this.mWidth / 3.0f, this.mCirclePaint);
        }
        if (this.mCircleList[2].enable) {
            this.mCirclePaint.setColor(this.mCircleList[2].color);
            this.mCirclePaint.setStrokeWidth((float) this.mCircleList[2].width);
            canvas.drawCircle(this.mXCenter, this.mYCenter, (this.mWidth / 2.0f) - (((float) this.mCircleList[2].width) / 2.0f), this.mCirclePaint);
        }
    }

    protected void drawBackground(Canvas canvas) {
        drawCircles(canvas);
        drawLines(canvas);
    }

    protected void transState(IRadarState target) {
        this.mCurState = target;
        this.mCurState.onPrepareDraw();
        invalidate();
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        this.mWidth = (float) getWidth();
        this.mXCenter = (float) (getWidth() / 2);
        this.mYCenter = (float) (getHeight() / 2);
    }

    protected void onDraw(Canvas canvas) {
        if (this.mCurState != null && this.mCurState.onDrawing(canvas)) {
            invalidate();
        }
    }
}

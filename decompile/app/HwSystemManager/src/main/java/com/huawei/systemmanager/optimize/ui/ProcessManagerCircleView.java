package com.huawei.systemmanager.optimize.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.widget.CircleBaseView;
import com.huawei.systemmanager.comm.widget.CircleView;
import com.huawei.systemmanager.optimize.base.Const;

public class ProcessManagerCircleView extends CircleBaseView {
    private static final float ANGLE_MAX = 360.0f;
    private float mBackgroundWidth = 0.0f;
    private int mColor;
    private Paint mPaint;
    private Paint mPaintPoint;
    private float mPaintWidth = 0.0f;
    private float mSweepAngleRate = 0.0f;

    public ProcessManagerCircleView(Context context) {
        super(context);
        initView(context);
    }

    public ProcessManagerCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ProcessManagerCircleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(Context context) {
        Resources res = context.getResources();
        this.mPaintLenth = res.getDimension(R.dimen.circle_view_scale_lenth) * 2.0f;
        float dimension = res.getDimension(R.dimen.process_manager_circle_width);
        this.mHeight = dimension;
        this.mWidth = dimension;
        this.mPaintWidth = res.getDimension(R.dimen.process_manager_paint_width);
        this.mBackgroundWidth = res.getDimension(R.dimen.process_manager_paint_width_background);
        this.mX = (int) (this.mWidth / 2.0f);
        this.mY = (int) (this.mHeight / 2.0f);
        this.mRadius = (float) ((int) (((float) this.mX) - res.getDimension(R.dimen.space_circle_radius_offset)));
        circlePoints(0.0f, 720.0f);
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setColor(Color.argb(77, 255, 255, 255));
        this.mPaint.setStrokeWidth(this.mBackgroundWidth);
        this.mPaint.setColor(getResources().getColor(R.color.hwsystemmanager_white_color));
        this.mPaint.setStrokeWidth(this.mPaintWidth);
        this.mPaintPoint = new Paint();
        this.mPaintPoint.setAntiAlias(true);
    }

    protected void onDraw(Canvas canvas) {
        drawBackGround(canvas, this.mBackgroundWidth);
        drawCicle(canvas);
    }

    public void setProgress(float progress) {
        showResult(progress);
    }

    private void showResult(float progress) {
        float percent = progress / 100.0f;
        float angle = percent * 360.0f;
        this.mColor = getStatusColor(percent);
        this.mSweepAngleRate = caculateAngle(angle);
        invalidate();
    }

    private void drawCicle(Canvas canvas) {
        this.mPaint.setStrokeWidth(this.mPaintWidth);
        this.mPaint.setColor(this.mColor);
        drawCircle(canvas, CircleView.START_ANGLE, this.mSweepAngleRate + CircleView.START_ANGLE, this.mPaint);
        float radius = this.mRadius - 13.0f;
        float x = ((float) this.mX) + ((float) (((double) radius) * Math.cos(Math.toRadians((double) ((this.mSweepAngleRate + CircleView.START_ANGLE) - this.mAngleOffset)))));
        float y = ((float) this.mY) + ((float) (((double) radius) * Math.sin(Math.toRadians((double) ((this.mSweepAngleRate + CircleView.START_ANGLE) - this.mAngleOffset)))));
        this.mPaintPoint.setColor(this.mColor);
        canvas.drawCircle(x, y, getResources().getDimension(R.dimen.processmanager_scan_point_raduis), this.mPaintPoint);
    }

    public int getStatusColor(float rate) {
        if (Float.compare(Const.FREE_MEMORY_RISK_FLOAT, rate) > 0) {
            return GREEN_ONE;
        }
        if (Float.compare(Const.FREE_MEMORY_DANGEROUS_FLOAT, rate) > 0) {
            return COLOR_YELLOW;
        }
        return COLOR_RED;
    }
}

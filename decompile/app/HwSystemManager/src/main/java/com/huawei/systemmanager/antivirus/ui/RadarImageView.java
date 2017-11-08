package com.huawei.systemmanager.antivirus.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.SystemManagerConst;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.util.HwLog;
import java.util.Random;

public class RadarImageView extends View {
    private static final int POINT_NUM = 4;
    private static final String TAG = "RadarImageView";
    private boolean isScanning;
    private int mAlpha;
    private Paint mBackgroundPaint;
    private float mCicleAngle;
    private float mCircleCenterX;
    private float mCircleCenterY;
    private float mCircleRidus2;
    private float mCircleRidus3;
    private int mPointOffset;
    private Drawable mScanPoint;
    private Drawable mScanRegion;
    private int mViewWidth;
    private final ImagePoint[] points;
    private int resHeight;
    private int resWidth;

    private static class ImagePoint extends Point {
        private int alpha;
        private int connectUnit = 10;
        private boolean hasScanedOut = false;
        private int mDelay = 0;
        private int mPointOffset = 0;
        private int mSize = 15;
        private int maxAlpha;
        private double rangle;

        public ImagePoint(int al, int size) {
            this.alpha = al;
            this.maxAlpha = al;
            this.mSize = size;
            this.connectUnit = al / size;
        }

        public void clear() {
            this.alpha = 0;
            this.hasScanedOut = false;
        }

        public void setXY(int sX, int sY, int sRangle) {
            this.rangle = (double) sRangle;
            this.x = sX;
            this.y = sY;
        }

        public int getAlpha() {
            return this.alpha;
        }

        public boolean isScanOut(float cicleAngle) {
            if (!this.hasScanedOut && this.rangle <= ((double) (181.0f + cicleAngle)) && this.rangle >= ((double) (179.0f + cicleAngle))) {
                this.hasScanedOut = true;
                HwLog.d(RadarImageView.TAG, "isScanOut: x = " + this.x + " y = " + this.y + " rangle = " + this.rangle + " mCicleAngle = " + cicleAngle);
            }
            return this.hasScanedOut;
        }

        public Rect getBoundsWithOffset(int pointOffset) {
            this.mDelay++;
            if (this.mPointOffset > this.mSize) {
                this.mPointOffset = 0;
            }
            if (this.mDelay == 10) {
                this.mDelay = 0;
                this.mPointOffset += pointOffset;
            }
            return new Rect(this.x - this.mPointOffset, this.y - this.mPointOffset, this.x + this.mPointOffset, this.y + this.mPointOffset);
        }

        public void updateAlpha() {
            this.alpha = this.maxAlpha - (this.connectUnit * this.mPointOffset);
        }
    }

    public RadarImageView(Context context) {
        super(context);
        this.mCircleCenterX = 87.0f;
        this.mCircleCenterY = 87.0f;
        this.mCircleRidus2 = 50.0f;
        this.mCircleRidus3 = 86.0f;
        this.mCicleAngle = 0.0f;
        this.points = new ImagePoint[4];
        this.mContext = context;
    }

    public RadarImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mCircleCenterX = 87.0f;
        this.mCircleCenterY = 87.0f;
        this.mCircleRidus2 = 50.0f;
        this.mCircleRidus3 = 86.0f;
        this.mCicleAngle = 0.0f;
        this.points = new ImagePoint[4];
        this.mContext = context;
        this.mScanRegion = getResources().getDrawable(R.drawable.img_scan_motion);
        this.mScanPoint = getResources().getDrawable(R.drawable.img_scan_blue_dot);
        this.points[0] = new ImagePoint(240, 15);
        this.points[1] = new ImagePoint(240, 18);
        this.points[2] = new ImagePoint(240, 14);
        this.points[3] = new ImagePoint(240, 10);
        this.mBackgroundPaint = new Paint();
        this.mBackgroundPaint.setAntiAlias(true);
        this.mBackgroundPaint.setStyle(Style.STROKE);
        Resources res = context.getResources();
        this.mPointOffset = 1;
        if (res.getDimension(R.dimen.radar_circle_delta) != 0.0f) {
            float radarDelta = res.getDimension(R.dimen.radar_circle_delta);
            this.mCircleCenterX += radarDelta;
            this.mCircleCenterY += radarDelta;
            this.mCircleRidus2 += radarDelta;
            this.mCircleRidus3 += radarDelta;
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < 4; i++) {
            if (this.points[i].isScanOut(this.mCicleAngle)) {
                if (!this.isScanning) {
                    this.points[i].clear();
                }
                this.mScanPoint.setAlpha(this.points[i].getAlpha());
                this.mScanPoint.setBounds(this.points[i].getBoundsWithOffset(this.mPointOffset));
                this.mScanPoint.draw(canvas);
                this.points[i].updateAlpha();
            }
        }
        if (this.mAlpha > 0) {
            canvas.save();
            canvas.rotate(this.mCicleAngle, ((float) this.mViewWidth) / 2.0f, ((float) this.mViewWidth) / 2.0f);
            this.mScanRegion.setBounds(0, 0, this.mViewWidth, this.mViewWidth);
            this.mScanRegion.setAlpha(this.mAlpha);
            this.mScanRegion.draw(canvas);
            canvas.restore();
            refreshScanRegion();
        }
        draw2ndCircle(canvas);
        draw3rdCircle(canvas);
        drawHorizontalLine(canvas);
        drawVerticalLine(canvas);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.resWidth = MeasureSpec.getSize(widthMeasureSpec);
        this.resHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.mViewWidth = this.resWidth;
        setMeasuredDimension(this.resWidth, this.resHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public int dip2px(float dipValue) {
        return (int) ((dipValue * this.mContext.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public void startScan() {
        for (int i = 0; i < 4; i++) {
            setRandomPoint(this.mViewWidth / 2, this.mViewWidth / 2, this.points[i]);
            HwLog.d(TAG, "x = " + this.points[i].x + " y = " + this.points[i].y);
        }
        this.mCicleAngle = -180.0f;
        this.mAlpha = 255;
        this.isScanning = true;
        refreshScanRegion();
    }

    public void startScan(boolean isNeedPoints) {
        if (isNeedPoints) {
            for (int i = 0; i < 4; i++) {
                setRandomPoint(this.mViewWidth / 2, this.mViewWidth / 2, this.points[i]);
                HwLog.d(TAG, "x = " + this.points[i].x + " y = " + this.points[i].y);
            }
        }
        this.mCicleAngle = -180.0f;
        this.mAlpha = 255;
        this.isScanning = true;
        refreshScanRegion();
    }

    public void stopScan() {
        this.isScanning = false;
        refreshScanRegion();
    }

    private void refreshScanRegion() {
        this.mCicleAngle += 2.0f;
        if (this.mCicleAngle > 360.0f) {
            this.mCicleAngle %= 360.0f;
        }
        if (!this.isScanning) {
            this.mAlpha -= 30;
        }
        invalidate();
    }

    private void draw3rdCircle(Canvas canvas) {
        this.mBackgroundPaint.setColor(getResources().getColor(R.color.emui5_theme_alpha5));
        this.mBackgroundPaint.setStrokeWidth((float) dip2px(Utility.ALPHA_MAX));
        canvas.drawCircle((float) dip2px(this.mCircleCenterX), (float) dip2px(this.mCircleCenterY), (float) (dip2px(this.mCircleRidus3) + 2), this.mBackgroundPaint);
    }

    private void draw2ndCircle(Canvas canvas) {
        this.mBackgroundPaint.setColor(getResources().getColor(R.color.emui5_theme_alpha));
        this.mBackgroundPaint.setStrokeWidth((float) dip2px(Utility.ALPHA_MAX));
        canvas.drawCircle((float) dip2px(this.mCircleCenterX), (float) dip2px(this.mCircleCenterY), (float) dip2px(this.mCircleRidus2), this.mBackgroundPaint);
    }

    private void drawVerticalLine(Canvas canvas) {
        this.mBackgroundPaint.setColor(getResources().getColor(R.color.emui5_theme_alpha));
        this.mBackgroundPaint.setStrokeWidth((float) dip2px(Utility.ALPHA_MAX));
        canvas.drawLine((float) dip2px(this.mCircleCenterX), 0.0f, (float) dip2px(this.mCircleCenterX), (float) dip2px(this.mCircleCenterX * 2.0f), this.mBackgroundPaint);
    }

    private void drawHorizontalLine(Canvas canvas) {
        this.mBackgroundPaint.setColor(getResources().getColor(R.color.emui5_theme_alpha));
        this.mBackgroundPaint.setStrokeWidth((float) dip2px(Utility.ALPHA_MAX));
        canvas.drawLine(0.0f, (float) dip2px(this.mCircleCenterX), (float) dip2px(this.mCircleCenterX * 2.0f), (float) dip2px(this.mCircleCenterX), this.mBackgroundPaint);
    }

    private void setRandomPoint(int centX, int centY, ImagePoint points) {
        int x;
        int y;
        Random random = new Random();
        int nextRandom = centX - ((centX * 25) / 36);
        if (nextRandom <= 0) {
            nextRandom = 1;
        }
        int raduis = random.nextInt(nextRandom) + ((centX * 25) / 48);
        int rangle = random.nextInt(SystemManagerConst.CIRCLE_DEGREE);
        double m = (((double) rangle) * 3.141592653589793d) / 180.0d;
        if (rangle >= 0 && rangle < 90) {
            x = centX + ((int) (((double) raduis) * Math.cos(m)));
            y = centY - ((int) (((double) raduis) * Math.sin(m)));
        } else if (90 > rangle || rangle >= 180) {
            x = centX + ((int) (((double) raduis) * Math.sin(m)));
            y = centY - ((int) (((double) raduis) * Math.cos(m)));
        } else {
            x = centX - ((int) (((double) raduis) * Math.cos(m)));
            y = centY + ((int) (((double) raduis) * Math.sin(m)));
        }
        points.setXY(x, y, rangle);
    }
}

package com.android.settings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.net.NetworkStatsHistory;
import android.net.NetworkStatsHistory.Entry;
import android.util.AttributeSet;
import android.view.View;
import com.android.internal.util.Preconditions;
import com.android.settings.R$styleable;

public class ChartNetworkSeriesView extends View {
    private long mEnd;
    private long mEndTime;
    private boolean mEstimateVisible;
    private ChartAxis mHoriz;
    private long mMax;
    private long mMaxEstimate;
    private Paint mPaintEstimate;
    private Paint mPaintFill;
    private Paint mPaintFillSecondary;
    private Paint mPaintStroke;
    private Path mPathEstimate;
    private Path mPathFill;
    private Path mPathStroke;
    private boolean mPathValid;
    private int mSafeRegion;
    private boolean mSecondary;
    private long mStart;
    private NetworkStatsHistory mStats;
    private ChartAxis mVert;

    public ChartNetworkSeriesView(Context context) {
        this(context, null, 0);
    }

    public ChartNetworkSeriesView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChartNetworkSeriesView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mEndTime = Long.MIN_VALUE;
        this.mPathValid = false;
        this.mEstimateVisible = false;
        this.mSecondary = false;
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.ChartNetworkSeriesView, defStyle, 0);
        int stroke = a.getColor(1, -65536);
        int fill = a.getColor(2, -65536);
        int fillSecondary = a.getColor(3, -65536);
        int safeRegion = a.getDimensionPixelSize(0, 0);
        setChartColor(stroke, fill, fillSecondary);
        setSafeRegion(safeRegion);
        setWillNotDraw(false);
        a.recycle();
        this.mPathStroke = new Path();
        this.mPathFill = new Path();
        this.mPathEstimate = new Path();
    }

    void init(ChartAxis horiz, ChartAxis vert) {
        this.mHoriz = (ChartAxis) Preconditions.checkNotNull(horiz, "missing horiz");
        this.mVert = (ChartAxis) Preconditions.checkNotNull(vert, "missing vert");
    }

    public void setChartColor(int stroke, int fill, int fillSecondary) {
        this.mPaintStroke = new Paint();
        this.mPaintStroke.setStrokeWidth(getResources().getDisplayMetrics().density * 4.0f);
        this.mPaintStroke.setColor(stroke);
        this.mPaintStroke.setStyle(Style.STROKE);
        this.mPaintStroke.setAntiAlias(true);
        this.mPaintFill = new Paint();
        this.mPaintFill.setColor(fill);
        this.mPaintFill.setStyle(Style.FILL);
        this.mPaintFill.setAntiAlias(true);
        this.mPaintFillSecondary = new Paint();
        this.mPaintFillSecondary.setColor(fillSecondary);
        this.mPaintFillSecondary.setStyle(Style.FILL);
        this.mPaintFillSecondary.setAntiAlias(true);
        this.mPaintEstimate = new Paint();
        this.mPaintEstimate.setStrokeWidth(3.0f);
        this.mPaintEstimate.setColor(fillSecondary);
        this.mPaintEstimate.setStyle(Style.STROKE);
        this.mPaintEstimate.setAntiAlias(true);
        this.mPaintEstimate.setPathEffect(new DashPathEffect(new float[]{10.0f, 10.0f}, 1.0f));
    }

    public void setSafeRegion(int safeRegion) {
        this.mSafeRegion = safeRegion;
    }

    public void invalidatePath() {
        this.mPathValid = false;
        this.mMax = 0;
        invalidate();
    }

    private void generatePath() {
        this.mMax = 0;
        this.mPathStroke.reset();
        this.mPathFill.reset();
        this.mPathEstimate.reset();
        this.mPathValid = true;
        if (this.mStats != null && this.mStats.size() >= 1) {
            int width = getWidth();
            int height = getHeight();
            float lastX = 0.0f;
            float lastY = (float) height;
            long lastTime = this.mHoriz.convertToValue(0.0f);
            this.mPathStroke.moveTo(0.0f, lastY);
            this.mPathFill.moveTo(0.0f, lastY);
            long totalData = 0;
            Entry entry = null;
            int start = this.mStats.getIndexBefore(this.mStart);
            int end = this.mStats.getIndexAfter(this.mEnd);
            for (int i = start; i <= end; i++) {
                entry = this.mStats.getValues(i, entry);
                long startTime = entry.bucketStart;
                long endTime = startTime + entry.bucketDuration;
                float startX = this.mHoriz.convertToPoint(startTime);
                float endX = this.mHoriz.convertToPoint(endTime);
                if (endX >= 0.0f) {
                    totalData += entry.rxBytes + entry.txBytes;
                    float startY = lastY;
                    float endY = this.mVert.convertToPoint(totalData);
                    if (lastTime != startTime) {
                        this.mPathStroke.lineTo(startX, startY);
                        this.mPathFill.lineTo(startX, startY);
                    }
                    this.mPathStroke.lineTo(endX, endY);
                    this.mPathFill.lineTo(endX, endY);
                    lastX = endX;
                    lastY = endY;
                    lastTime = endTime;
                }
            }
            if (lastTime < this.mEndTime) {
                lastX = this.mHoriz.convertToPoint(this.mEndTime);
                this.mPathStroke.lineTo(lastX, lastY);
                this.mPathFill.lineTo(lastX, lastY);
            }
            this.mPathFill.lineTo(lastX, (float) height);
            this.mPathFill.lineTo(0.0f, (float) height);
            this.mMax = totalData;
            invalidate();
        }
    }

    public void setEstimateVisible(boolean estimateVisible) {
        this.mEstimateVisible = false;
        invalidate();
    }

    public long getMaxEstimate() {
        return this.mMaxEstimate;
    }

    public long getMaxVisible() {
        long maxVisible = this.mEstimateVisible ? this.mMaxEstimate : this.mMax;
        if (maxVisible > 0 || this.mStats == null) {
            return maxVisible;
        }
        Entry entry = this.mStats.getValues(this.mStart, this.mEnd, null);
        return entry.rxBytes + entry.txBytes;
    }

    protected void onDraw(Canvas canvas) {
        int save;
        if (!this.mPathValid) {
            generatePath();
        }
        if (this.mEstimateVisible) {
            save = canvas.save();
            canvas.clipRect(0, 0, getWidth(), getHeight());
            canvas.drawPath(this.mPathEstimate, this.mPaintEstimate);
            canvas.restoreToCount(save);
        }
        Paint paintFill = this.mSecondary ? this.mPaintFillSecondary : this.mPaintFill;
        save = canvas.save();
        canvas.clipRect(this.mSafeRegion, 0, getWidth(), getHeight() - this.mSafeRegion);
        canvas.drawPath(this.mPathFill, paintFill);
        canvas.restoreToCount(save);
    }
}

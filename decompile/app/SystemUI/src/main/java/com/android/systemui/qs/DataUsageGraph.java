package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import com.android.systemui.R;

public class DataUsageGraph extends View {
    private long mLimitLevel;
    private final int mMarkerWidth;
    private long mMaxLevel;
    private final int mOverlimitColor;
    private final Paint mTmpPaint = new Paint();
    private final RectF mTmpRect = new RectF();
    private final int mTrackColor;
    private final int mUsageColor;
    private long mUsageLevel;
    private final int mWarningColor;
    private long mWarningLevel;

    public DataUsageGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources res = context.getResources();
        this.mTrackColor = context.getColor(R.color.data_usage_graph_track);
        this.mUsageColor = context.getColor(R.color.system_accent_color);
        this.mOverlimitColor = context.getColor(R.color.system_warning_color);
        this.mWarningColor = context.getColor(R.color.data_usage_graph_warning);
        this.mMarkerWidth = res.getDimensionPixelSize(R.dimen.data_usage_graph_marker_width);
    }

    public void setLevels(long limitLevel, long warningLevel, long usageLevel) {
        this.mLimitLevel = Math.max(0, limitLevel);
        this.mWarningLevel = Math.max(0, warningLevel);
        this.mUsageLevel = Math.max(0, usageLevel);
        this.mMaxLevel = Math.max(Math.max(Math.max(this.mLimitLevel, this.mWarningLevel), this.mUsageLevel), 1);
        postInvalidate();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF r = this.mTmpRect;
        Paint p = this.mTmpPaint;
        int w = getWidth();
        int h = getHeight();
        boolean overLimit = this.mLimitLevel > 0 && this.mUsageLevel > this.mLimitLevel;
        float usageRight = ((float) w) * (((float) this.mUsageLevel) / ((float) this.mMaxLevel));
        if (overLimit) {
            usageRight = Math.min(Math.max((((float) w) * (((float) this.mLimitLevel) / ((float) this.mMaxLevel))) - ((float) (this.mMarkerWidth / 2)), (float) this.mMarkerWidth), (float) (w - (this.mMarkerWidth * 2)));
            r.set(((float) this.mMarkerWidth) + usageRight, 0.0f, (float) w, (float) h);
            p.setColor(this.mOverlimitColor);
            canvas.drawRect(r, p);
        } else {
            r.set(0.0f, 0.0f, (float) w, (float) h);
            p.setColor(this.mTrackColor);
            canvas.drawRect(r, p);
        }
        r.set(0.0f, 0.0f, usageRight, (float) h);
        p.setColor(this.mUsageColor);
        canvas.drawRect(r, p);
        float warningLeft = Math.min(Math.max((((float) w) * (((float) this.mWarningLevel) / ((float) this.mMaxLevel))) - ((float) (this.mMarkerWidth / 2)), 0.0f), (float) (w - this.mMarkerWidth));
        r.set(warningLeft, 0.0f, ((float) this.mMarkerWidth) + warningLeft, (float) h);
        p.setColor(this.mWarningColor);
        canvas.drawRect(r, p);
    }
}

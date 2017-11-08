package com.android.settings.deviceinfo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;
import com.android.settings.R$styleable;
import java.util.Collection;

public class PercentageBarChart extends View {
    private final Paint mEmptyPaint = new Paint();
    private Collection<Entry> mEntries;
    private int mMinTickWidth = 1;

    public static class Entry implements Comparable<Entry> {
        public final int order;
        public final Paint paint;
        public final float percentage;

        public int compareTo(Entry another) {
            return this.order - another.order;
        }
    }

    public PercentageBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.PercentageBarChart);
        this.mMinTickWidth = a.getDimensionPixelSize(1, 1);
        int emptyColor = a.getColor(0, -16777216);
        a.recycle();
        this.mEmptyPaint.setColor(emptyColor);
        this.mEmptyPaint.setStyle(Style.FILL);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int left = getPaddingLeft();
        int right = getWidth() - getPaddingRight();
        int top = getPaddingTop();
        int bottom = getHeight() - getPaddingBottom();
        int width = right - left;
        float nextX;
        float entryWidth;
        float lastX;
        if (isLayoutRtl()) {
            nextX = (float) right;
            if (this.mEntries != null) {
                for (Entry e : this.mEntries) {
                    if (e.percentage == 0.0f) {
                        entryWidth = 0.0f;
                    } else {
                        entryWidth = Math.max((float) this.mMinTickWidth, ((float) width) * e.percentage);
                    }
                    lastX = nextX - entryWidth;
                    if (lastX < ((float) left)) {
                        canvas.drawRect((float) left, (float) top, nextX, (float) bottom, e.paint);
                        return;
                    }
                    canvas.drawRect(lastX, (float) top, nextX, (float) bottom, e.paint);
                    nextX = lastX;
                }
            }
            canvas.drawRect((float) left, (float) top, nextX, (float) bottom, this.mEmptyPaint);
        } else {
            lastX = (float) left;
            if (this.mEntries != null) {
                for (Entry e2 : this.mEntries) {
                    if (e2.percentage == 0.0f) {
                        entryWidth = 0.0f;
                    } else {
                        entryWidth = Math.max((float) this.mMinTickWidth, ((float) width) * e2.percentage);
                    }
                    nextX = lastX + entryWidth;
                    if (nextX > ((float) right)) {
                        canvas.drawRect(lastX, (float) top, (float) right, (float) bottom, e2.paint);
                        return;
                    }
                    canvas.drawRect(lastX, (float) top, nextX, (float) bottom, e2.paint);
                    lastX = nextX;
                }
            }
            canvas.drawRect(lastX, (float) top, (float) right, (float) bottom, this.mEmptyPaint);
        }
    }

    public void setBackgroundColor(int color) {
        this.mEmptyPaint.setColor(color);
    }
}

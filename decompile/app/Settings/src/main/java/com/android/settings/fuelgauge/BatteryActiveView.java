package com.android.settings.fuelgauge;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.View;

public class BatteryActiveView extends View {
    private final Paint mPaint = new Paint();
    private BatteryActiveProvider mProvider;

    public interface BatteryActiveProvider {
        SparseIntArray getColorArray();

        long getPeriod();

        boolean hasData();
    }

    public BatteryActiveView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setProvider(BatteryActiveProvider provider) {
        this.mProvider = provider;
        if (getWidth() != 0) {
            postInvalidate();
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (getWidth() != 0) {
            postInvalidate();
        }
    }

    protected void onDraw(Canvas canvas) {
        if (this.mProvider != null) {
            SparseIntArray array = this.mProvider.getColorArray();
            float period = (float) this.mProvider.getPeriod();
            for (int i = 0; i < array.size() - 1; i++) {
                drawColor(canvas, array.keyAt(i), array.keyAt(i + 1), array.valueAt(i), period);
            }
        }
    }

    private void drawColor(Canvas canvas, int start, int end, int color, float period) {
        if (color != 0) {
            this.mPaint.setColor(color);
            Canvas canvas2 = canvas;
            canvas2.drawRect(((float) getWidth()) * (((float) start) / period), 0.0f, ((float) getWidth()) * (((float) end) / period), (float) getHeight(), this.mPaint);
        }
    }
}

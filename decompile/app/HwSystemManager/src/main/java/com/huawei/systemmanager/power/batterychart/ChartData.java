package com.huawei.systemmanager.power.batterychart;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;

public class ChartData {
    private static final int[] BADNESS_COLORS = new int[]{GlobalContext.getContext().getResources().getColor(R.color.net_chartdata_badness0), GlobalContext.getContext().getResources().getColor(R.color.net_chartdata_badness1), GlobalContext.getContext().getResources().getColor(R.color.net_chartdata_badness2), GlobalContext.getContext().getResources().getColor(R.color.net_chartdata_badness3), GlobalContext.getContext().getResources().getColor(R.color.net_chartdata_badness4), GlobalContext.getContext().getResources().getColor(R.color.net_chartdata_badness5), GlobalContext.getContext().getResources().getColor(R.color.net_chartdata_badness6)};
    public static final int CHART_DATA_BIN_MASK = GlobalContext.getContext().getResources().getColor(R.color.net_chartdata_bin_mask);
    public static final int CHART_DATA_BIN_SHIFT = 16;
    public static final int CHART_DATA_X_MASK = GlobalContext.getContext().getResources().getColor(R.color.net_chartdata_x_mask);
    private int mLastBin;
    private int mNumTicks;
    private Paint[] mPaints;
    private int[] mTicks;

    public ChartData() {
        this(BADNESS_COLORS);
    }

    public ChartData(int[] colors) {
        setColors(colors);
    }

    private void setColors(int[] colors) {
        this.mPaints = new Paint[colors.length];
        for (int i = 0; i < colors.length; i++) {
            this.mPaints[i] = new Paint();
            this.mPaints[i].setColor(colors[i]);
            this.mPaints[i].setStyle(Style.FILL);
        }
    }

    public void init(int width) {
        if (width > 0) {
            this.mTicks = new int[(width * 2)];
        } else {
            this.mTicks = null;
        }
        this.mNumTicks = 0;
        this.mLastBin = 0;
    }

    public void addTick(int x, int bin) {
        if (bin != this.mLastBin && this.mNumTicks < this.mTicks.length) {
            this.mTicks[this.mNumTicks] = (CHART_DATA_X_MASK & x) | (bin << 16);
            this.mNumTicks++;
            this.mLastBin = bin;
        }
    }

    public void finish(int width) {
        if (this.mLastBin != 0) {
            addTick(width, 0);
        }
    }

    public void draw(Canvas canvas, int top, int height) {
        int lastBin = 0;
        int lastX = 0;
        int bottom = top + height;
        for (int i = 0; i < this.mNumTicks; i++) {
            int tick = this.mTicks[i];
            int x = tick & CHART_DATA_X_MASK;
            int bin = (CHART_DATA_BIN_MASK & tick) >> 16;
            if (lastBin != 0) {
                canvas.drawRect((float) lastX, (float) top, (float) x, (float) bottom, this.mPaints[lastBin]);
            }
            lastBin = bin;
            lastX = x;
        }
    }
}

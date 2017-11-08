package com.android.keyguard.hwlockscreen.magazine;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.keyguard.R$color;
import com.huawei.keyguard.util.HwLog;

public class MagazineSimpleTitleView extends TextView {
    private static float TITLE_END_SIZE = 16.0f;
    private static float TITLE_START_SIZE = 15.0f;
    private Paint mColorPaint;
    private float mDy;
    private PorterDuffColorFilter mFilter;
    private PorterDuffColorFilter mFilterNormal;

    public MagazineSimpleTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mDy = 0.0f;
        this.mColorPaint = null;
        this.mFilter = null;
        this.mFilterNormal = null;
        this.mColorPaint = getPaint();
        this.mFilter = new PorterDuffColorFilter(getResources().getColor(R$color.magazine_detail_title_color), Mode.SRC_IN);
        this.mFilterNormal = new PorterDuffColorFilter(-1, Mode.SRC_IN);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public void refreshTextView(float dy, int h) {
        super.setTextColor((-2130706433 - ((int) ((2.13070643E9f * dy) / ((float) h)))) & -16777216);
        super.setTextSize(TITLE_START_SIZE - (((TITLE_END_SIZE - TITLE_START_SIZE) * dy) / ((float) h)));
        refreshTextView(dy);
    }

    public void refreshTextView(float dy) {
        this.mDy = dy;
        invalidate();
    }

    public void setTextColor(ColorStateList colors) {
        if (colors != null) {
            this.mFilterNormal = new PorterDuffColorFilter(colors.getColorForState(getDrawableState(), 0), Mode.SRC_IN);
            colors.withAlpha(255);
        } else {
            HwLog.w("MagazineSimpleTitleView", "setTextColor colors is null");
        }
        super.setTextColor(colors);
    }

    public void setTextColor(int color) {
        this.mFilterNormal = new PorterDuffColorFilter(color, Mode.SRC_IN);
        super.setTextColor(color | -16777216);
    }

    protected void onDraw(Canvas canvas) {
        if (this.mColorPaint == null || this.mFilterNormal == null || this.mFilter == null) {
            super.onDraw(canvas);
            HwLog.w("MagazineSimpleTitleView", "onDraw mColorPaint, mFilterNormal or mFilter is null");
            return;
        }
        canvas.save();
        canvas.clipRect(0.0f, 0.0f, (float) getWidth(), ((float) getHeight()) + this.mDy);
        this.mColorPaint.setColorFilter(this.mFilterNormal);
        super.onDraw(canvas);
        canvas.restore();
        canvas.save();
        canvas.clipRect(0.0f, ((float) getHeight()) + this.mDy, (float) getWidth(), (float) getHeight());
        this.mColorPaint.setColorFilter(this.mFilter);
        super.onDraw(canvas);
        this.mColorPaint.setColorFilter(null);
        canvas.restore();
    }
}

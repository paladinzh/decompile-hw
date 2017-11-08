package com.android.settingslib;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

public class RestrictedLockImageSpan extends ImageSpan {
    private Context mContext;
    private final float mExtraPadding = ((float) this.mContext.getResources().getDimensionPixelSize(R$dimen.restricted_icon_padding));
    private final Drawable mRestrictedPadlock = RestrictedLockUtils.getRestrictedPadlock(this.mContext);

    public RestrictedLockImageSpan(Context context) {
        super((Drawable) null);
        this.mContext = context;
    }

    public Drawable getDrawable() {
        return this.mRestrictedPadlock;
    }

    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        Drawable drawable = getDrawable();
        canvas.save();
        canvas.translate(x + this.mExtraPadding, ((float) (bottom - drawable.getBounds().bottom)) / 2.0f);
        drawable.draw(canvas);
        canvas.restore();
    }

    public int getSize(Paint paint, CharSequence text, int start, int end, FontMetricsInt fontMetrics) {
        return (int) (((float) super.getSize(paint, text, start, end, fontMetrics)) + (this.mExtraPadding * 2.0f));
    }
}

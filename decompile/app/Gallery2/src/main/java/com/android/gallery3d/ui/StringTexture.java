package com.android.gallery3d.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint.FontMetricsInt;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;

public class StringTexture extends CanvasTexture {
    private final FontMetricsInt mMetrics;
    private final TextPaint mPaint;
    private final String mText;

    private StringTexture(String text, TextPaint paint, FontMetricsInt metrics, int width, int height) {
        super(width, height);
        this.mText = text;
        this.mPaint = paint;
        this.mMetrics = metrics;
    }

    public static TextPaint getDefaultPaint(float textSize, int color) {
        TextPaint paint = new TextPaint();
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);
        paint.setColor(color);
        return paint;
    }

    public static StringTexture newInstance(String text, float textSize, int color) {
        return newInstance(text, getDefaultPaint(textSize, color));
    }

    public static StringTexture newInstance(String text, float lengthLimit, TextPaint paint) {
        if (lengthLimit > 0.0f) {
            text = TextUtils.ellipsize(text, paint, lengthLimit, TruncateAt.END).toString();
        }
        return newInstance(text, paint);
    }

    public static StringTexture newInstance(String text, TextPaint paint) {
        FontMetricsInt metrics = paint.getFontMetricsInt();
        int width = (int) Math.ceil((double) paint.measureText(text));
        int height = metrics.bottom - metrics.top;
        if (width <= 0) {
            width = 1;
        }
        if (height <= 0) {
            height = 1;
        }
        return new StringTexture(text, paint, metrics, width, height);
    }

    protected void onDraw(Canvas canvas, Bitmap backing) {
        canvas.translate(0.0f, (float) (-this.mMetrics.top));
        canvas.drawText(this.mText, 0.0f, 0.0f, this.mPaint);
    }
}

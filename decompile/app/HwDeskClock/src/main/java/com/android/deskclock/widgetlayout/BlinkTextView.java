package com.android.deskclock.widgetlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.deskclock.R;
import com.android.deskclock.R$styleable;

public class BlinkTextView extends TextView {
    private String drawStr;
    private FontMetrics fm;
    private float fmHeight;
    private int height;
    private boolean isUpdateLength;
    private int mAppType;
    private int mFontSize;
    private Paint mPaint;
    private int width;

    private void init() {
        this.mPaint = new Paint();
        this.mPaint.setColor(Color.argb(127, 255, 255, 255));
        this.mPaint.setAntiAlias(true);
        this.mFontSize = getContext().getResources().getDimensionPixelSize(R.dimen.blink_text_font);
        this.mPaint.setTextSize((float) this.mFontSize);
        this.mPaint.setTextAlign(Align.CENTER);
        this.fm = this.mPaint.getFontMetrics();
        this.fmHeight = (this.fm.ascent + this.fm.descent) / 2.0f;
        this.height = getHeight();
        this.width = getWidth();
        if (1 == this.mAppType) {
            this.drawStr = getResources().getString(R.string.tips_clock_closetimer);
            return;
        }
        this.drawStr = getResources().getString(R.string.tips_clock_closealarm);
        if (2 == this.mAppType) {
            this.mPaint.setTextSize((float) getContext().getResources().getDimensionPixelSize(R.dimen.cover_blink_text_font));
        }
    }

    public BlinkTextView(Context context) {
        this(context, null);
    }

    public BlinkTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BlinkTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, 0);
        this.mAppType = 0;
        this.mFontSize = 15;
        this.isUpdateLength = false;
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.AnimateView, defStyle, 0);
        try {
            this.mAppType = a.getInt(0, 0);
            init();
        } finally {
            a.recycle();
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.width = w >> 1;
        this.height = h >> 1;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!this.isUpdateLength && this.width > 0) {
            refitText(this.drawStr);
            this.isUpdateLength = true;
        }
        canvas.drawText(this.drawStr, (float) this.width, ((float) this.height) - this.fmHeight, this.mPaint);
    }

    private void refitText(String text) {
        Rect rect = new Rect();
        this.mPaint.getTextBounds(text, 0, text.length(), rect);
        float textSize = this.mPaint.getTextSize();
        for (int textWidths = rect.width(); textWidths > this.width * 2; textWidths = rect.width()) {
            textSize -= 1.0f;
            this.mPaint.setTextSize(textSize);
            this.mPaint.getTextBounds(text, 0, text.length(), rect);
        }
    }
}

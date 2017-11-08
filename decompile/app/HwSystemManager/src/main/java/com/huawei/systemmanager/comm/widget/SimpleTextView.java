package com.huawei.systemmanager.comm.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import com.huawei.systemmanager.R;

public class SimpleTextView extends View {
    private static final int DEFAULT_PADDING = 14;
    protected float mAscent;
    protected Rect mBounds = new Rect();
    private int mColor = getResources().getColor(R.color.hwsystemmanager_white_color);
    protected String mText = "";
    protected Paint mTextPaint;
    private int mTextSize = 20;
    private boolean mWrap;

    public SimpleTextView(Context context) {
        super(context);
        initPaint();
    }

    public SimpleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BaseTextView);
        try {
            String text = a.getString(2);
            if (text != null) {
                setText(text);
            }
            setTextColor(a.getColor(1, getResources().getColor(R.color.emui_list_primary_text)));
            int textSize = a.getDimensionPixelOffset(0, 0);
            if (textSize > 0) {
                setTextSize(textSize);
            }
            this.mWrap = a.getBoolean(4, true);
            Typeface tf = TypefaceUtil.getTypefaceFromName(context, a.getString(3));
            if (tf != null) {
                this.mTextPaint.setTypeface(tf);
            }
            a.recycle();
            if (this.mWrap) {
                setPaddingRelative(getPaddingStart(), 14, getPaddingEnd(), 14);
                resetBounds();
            }
        } catch (Throwable th) {
            a.recycle();
        }
    }

    protected final void initPaint() {
        this.mTextPaint = new Paint();
        this.mTextPaint.setAntiAlias(true);
        this.mTextPaint.setTextSize((float) this.mTextSize);
        this.mTextPaint.setColor(this.mColor);
    }

    public void setText(String text) {
        if (text == null) {
            text = "";
        }
        this.mText = text;
        resetBounds();
        requestLayout();
        invalidate();
    }

    public void setTextColor(int color) {
        this.mTextPaint.setColor(color);
        invalidate();
    }

    public void setTextSize(int size) {
        if (this.mTextSize != size) {
            this.mTextSize = size;
            this.mTextPaint.setTextSize((float) size);
            resetBounds();
            requestLayout();
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == 1073741824) {
            return specSize;
        }
        int result = (((int) this.mTextPaint.measureText(this.mText)) + getPaddingStart()) + getPaddingEnd();
        if (specMode == Integer.MIN_VALUE) {
            return Math.min(result, specSize);
        }
        return result;
    }

    private int measureHeight(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        this.mAscent = this.mTextPaint.ascent();
        if (specMode == 1073741824) {
            return specSize;
        }
        int result = (((int) ((-this.mAscent) + this.mTextPaint.descent())) + getPaddingTop()) + getPaddingBottom();
        if (specMode == Integer.MIN_VALUE) {
            return Math.min(result, specSize);
        }
        return result;
    }

    protected final void resetBounds() {
        if (this.mWrap) {
            onResetBounds(this.mBounds);
        }
    }

    protected void onResetBounds(Rect r) {
        if (TextUtils.isEmpty(this.mText)) {
            r.setEmpty();
        } else {
            this.mTextPaint.getTextBounds(this.mText, 0, this.mText.length(), this.mBounds);
        }
    }

    protected Paint getTextPaint() {
        return this.mTextPaint;
    }

    protected void onDraw(Canvas canvas) {
        float y;
        super.onDraw(canvas);
        float x = 0.0f;
        if (this.mWrap) {
            y = (float) (this.mBounds.height() + getPaddingTop());
        } else {
            x = (float) getPaddingStart();
            y = ((float) getPaddingTop()) - this.mAscent;
        }
        canvas.drawText(this.mText, x, y, this.mTextPaint);
    }
}

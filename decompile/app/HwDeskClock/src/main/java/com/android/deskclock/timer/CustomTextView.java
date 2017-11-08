package com.android.deskclock.timer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

public class CustomTextView extends TextView {
    private float cTextSize;
    private Paint testPaint;

    public CustomTextView(Context context) {
        super(context, null);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void refitText(String text, int textWidth) {
        if (textWidth > 0) {
            this.testPaint = new Paint();
            this.testPaint.set(getPaint());
            Rect rect = new Rect();
            this.testPaint.getTextBounds(text, 0, text.length(), rect);
            int textWidths = rect.width();
            float trySize = getTextSize();
            this.cTextSize = trySize;
            float targetWidth = TypedValue.applyDimension(1, 48.0f, getResources().getDisplayMetrics());
            Log.e("TEXT_SIZE1", "textWidths:" + textWidths + "cTextSize:" + this.cTextSize + "trySize:" + trySize + " /availableWidth:" + "targetWidth:" + targetWidth);
            while (((float) textWidths) > targetWidth) {
                this.cTextSize -= 1.0f;
                this.testPaint.setTextSize(this.cTextSize);
                this.testPaint.getTextBounds(text, 0, text.length(), rect);
                textWidths = rect.width();
                Log.e("TEXT_SIZE2", "cTextSize" + this.cTextSize + "textWidths:" + textWidths);
            }
            setTextSize(0, this.cTextSize);
        }
    }

    protected void onDraw(Canvas canvas) {
        refitText(getText().toString(), getWidth());
        super.onDraw(canvas);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        setTextSize(1, 48.0f);
        refitText(getText().toString(), getWidth());
        requestLayout();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        refitText(text.toString(), getWidth());
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        requestLayout();
    }
}

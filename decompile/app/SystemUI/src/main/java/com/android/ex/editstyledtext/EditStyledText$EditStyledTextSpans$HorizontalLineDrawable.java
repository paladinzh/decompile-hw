package com.android.ex.editstyledtext;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

public class EditStyledText$EditStyledTextSpans$HorizontalLineDrawable extends ShapeDrawable {
    private static boolean DBG_HL = false;
    private Spannable mSpannable;
    private int mWidth;

    public EditStyledText$EditStyledTextSpans$HorizontalLineDrawable(int color, int width, Spannable spannable) {
        super(new RectShape());
        this.mSpannable = spannable;
        this.mWidth = width;
        renewColor(color);
        renewBounds(width);
    }

    public void draw(Canvas canvas) {
        renewColor();
        canvas.drawRect(new Rect(0, 9, this.mWidth, 11), getPaint());
    }

    public void renewBounds(int width) {
        if (DBG_HL) {
            Log.d("EditStyledTextSpan", "--- renewBounds:" + width);
        }
        if (width > 20) {
            width -= 20;
        }
        this.mWidth = width;
        setBounds(0, 0, width, 20);
    }

    private void renewColor(int color) {
        if (DBG_HL) {
            Log.d("EditStyledTextSpan", "--- renewColor:" + color);
        }
        getPaint().setColor(color);
    }

    private void renewColor() {
        EditStyledText$EditStyledTextSpans$HorizontalLineSpan parent = getParentSpan();
        Spannable text = this.mSpannable;
        ForegroundColorSpan[] spans = (ForegroundColorSpan[]) text.getSpans(text.getSpanStart(parent), text.getSpanEnd(parent), ForegroundColorSpan.class);
        if (DBG_HL) {
            Log.d("EditStyledTextSpan", "--- renewColor:" + spans.length);
        }
        if (spans.length > 0) {
            renewColor(spans[spans.length - 1].getForegroundColor());
        }
    }

    private EditStyledText$EditStyledTextSpans$HorizontalLineSpan getParentSpan() {
        int i = 0;
        Spannable text = this.mSpannable;
        EditStyledText$EditStyledTextSpans$HorizontalLineSpan[] images = (EditStyledText$EditStyledTextSpans$HorizontalLineSpan[]) text.getSpans(0, text.length(), EditStyledText$EditStyledTextSpans$HorizontalLineSpan.class);
        if (images.length > 0) {
            int length = images.length;
            while (i < length) {
                EditStyledText$EditStyledTextSpans$HorizontalLineSpan image = images[i];
                if (image.getDrawable() == this) {
                    return image;
                }
                i++;
            }
        }
        Log.e("EditStyledTextSpan", "---renewBounds: Couldn't find");
        return null;
    }
}

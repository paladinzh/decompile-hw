package com.android.ex.editstyledtext;

import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.style.DynamicDrawableSpan;

public class EditStyledText$EditStyledTextSpans$HorizontalLineSpan extends DynamicDrawableSpan {
    EditStyledText$EditStyledTextSpans$HorizontalLineDrawable mDrawable;

    public EditStyledText$EditStyledTextSpans$HorizontalLineSpan(int color, int width, Spannable spannable) {
        super(0);
        this.mDrawable = new EditStyledText$EditStyledTextSpans$HorizontalLineDrawable(color, width, spannable);
    }

    public Drawable getDrawable() {
        return this.mDrawable;
    }

    public void resetWidth(int width) {
        this.mDrawable.renewBounds(width);
    }
}

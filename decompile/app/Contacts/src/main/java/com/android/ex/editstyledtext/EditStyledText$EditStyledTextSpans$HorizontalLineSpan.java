package com.android.ex.editstyledtext;

import android.graphics.drawable.Drawable;
import android.text.style.DynamicDrawableSpan;

public class EditStyledText$EditStyledTextSpans$HorizontalLineSpan extends DynamicDrawableSpan {
    EditStyledText$EditStyledTextSpans$HorizontalLineDrawable mDrawable;

    public Drawable getDrawable() {
        return this.mDrawable;
    }

    public void resetWidth(int width) {
        this.mDrawable.renewBounds(width);
    }
}

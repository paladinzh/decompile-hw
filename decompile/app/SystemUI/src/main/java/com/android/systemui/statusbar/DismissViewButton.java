package com.android.systemui.statusbar;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class DismissViewButton extends AlphaOptimizedButton {
    public DismissViewButton(Context context) {
        this(context, null);
    }

    public DismissViewButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DismissViewButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DismissViewButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void getDrawingRect(Rect outRect) {
        super.getDrawingRect(outRect);
        float translationX = ((ViewGroup) this.mParent).getTranslationX();
        float translationY = ((ViewGroup) this.mParent).getTranslationY();
        outRect.left = (int) (((float) outRect.left) + translationX);
        outRect.right = (int) (((float) outRect.right) + translationX);
        outRect.top = (int) (((float) outRect.top) + translationY);
        outRect.bottom = (int) (((float) outRect.bottom) + translationY);
    }
}

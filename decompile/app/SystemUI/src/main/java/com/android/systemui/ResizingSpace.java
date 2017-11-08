package com.android.systemui;

import android.R;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;

public class ResizingSpace extends View {
    private final int mHeight;
    private final int mWidth;

    public ResizingSpace(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (getVisibility() == 0) {
            setVisibility(4);
        }
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewGroup_Layout);
        this.mWidth = a.getResourceId(0, 0);
        this.mHeight = a.getResourceId(1, 0);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LayoutParams params = getLayoutParams();
        boolean changed = false;
        if (this.mWidth > 0) {
            int width = getContext().getResources().getDimensionPixelOffset(this.mWidth);
            if (width != params.width) {
                params.width = width;
                changed = true;
            }
        }
        if (this.mHeight > 0) {
            int height = getContext().getResources().getDimensionPixelOffset(this.mHeight);
            if (height != params.height) {
                params.height = height;
                changed = true;
            }
        }
        if (changed) {
            setLayoutParams(params);
        }
    }

    public void draw(Canvas canvas) {
    }

    private static int getDefaultSize2(int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case Integer.MIN_VALUE:
                return Math.min(size, specSize);
            case 0:
                return size;
            case 1073741824:
                return specSize;
            default:
                return result;
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize2(getSuggestedMinimumWidth(), widthMeasureSpec), getDefaultSize2(getSuggestedMinimumHeight(), heightMeasureSpec));
    }
}

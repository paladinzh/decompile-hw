package com.android.settings.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;
import com.android.settings.R$styleable;

public class RoundImageView extends ImageView {
    public float mBorderRadius;

    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.RoundImageView);
        this.mBorderRadius = (float) a.getDimensionPixelSize(0, (int) TypedValue.applyDimension(1, 30.0f, getResources().getDisplayMetrics()));
        a.recycle();
    }

    protected void onDraw(Canvas canvas) {
        Path clipPath = new Path();
        clipPath.addRoundRect(new RectF(0.0f, 0.0f, (float) getWidth(), (float) getHeight()), this.mBorderRadius, this.mBorderRadius, Direction.CW);
        canvas.clipPath(clipPath);
        super.onDraw(canvas);
    }
}

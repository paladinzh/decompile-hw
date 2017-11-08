package com.huawei.systemmanager.spacecleanner.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;

public class MultiImageView extends ImageView {
    private float mAlpha1;
    private float mAlpha2;
    private Drawable mBackground;
    private Drawable mDrawable;
    private int mHeight1;
    private int mHeight2;
    private int mOffset1;
    private int mOffset2;
    private Rect mRect;
    private Rect mRect1;
    private Rect mRect2;
    private int mRotation;
    private Drawable mStackBackground1;
    private Drawable mStackBackground2;
    private Drawable mStackDrawable1;
    private Drawable mStackDrawable2;
    private int mWidth1;
    private int mWidth2;
    private Rect rect;

    public MultiImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mRotation = 0;
        this.mRect = new Rect();
        this.mRect1 = new Rect();
        this.mRect2 = new Rect();
        this.rect = new Rect();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Multi_Image_View, defStyle, 0);
        this.mDrawable = getDrawable();
        this.mBackground = a.getDrawable(0);
        this.mStackDrawable1 = a.getDrawable(3);
        this.mStackBackground1 = a.getDrawable(4);
        this.mWidth1 = a.getDimensionPixelSize(1, 0);
        this.mHeight1 = a.getDimensionPixelSize(2, 0);
        this.mOffset1 = a.getDimensionPixelSize(5, 0);
        this.mAlpha1 = a.getFloat(6, Utility.ALPHA_MAX);
        this.mStackDrawable2 = a.getDrawable(9);
        this.mStackBackground2 = a.getDrawable(10);
        this.mWidth2 = a.getDimensionPixelSize(7, 0);
        this.mHeight2 = a.getDimensionPixelSize(8, 0);
        this.mOffset2 = a.getDimensionPixelSize(11, 0);
        this.mAlpha2 = a.getFloat(12, Utility.ALPHA_MAX);
        a.recycle();
    }

    private void drawStack1(Canvas canvas, Drawable drawable) {
        if (drawable != null) {
            int top = this.mOffset1;
            int left = (getWidth() - this.mWidth1) / 2;
            drawable.setAlpha(Math.round(this.mAlpha1 * 255.0f));
            this.mRect1.set(left, top, this.mWidth1 + left, this.mHeight1 + top);
            drawable.setBounds(this.mRect1);
            drawStackRegion(canvas, drawable, this.mRect1, this.mRect);
        }
    }

    private void drawStack2(Canvas canvas, Drawable drawable) {
        if (drawable != null) {
            int top = this.mOffset2;
            int left = (getWidth() - this.mWidth2) / 2;
            drawable.setAlpha(Math.round(this.mAlpha2 * 255.0f));
            this.mRect2.set(left, top, this.mWidth2 + left, this.mHeight2 + top);
            drawable.setBounds(this.mRect2);
            drawStackRegion(canvas, drawable, this.mRect2, this.mRect1);
        }
    }

    private void drawStackRegion(Canvas canvas, Drawable drawable, Rect clip, Rect diff) {
        canvas.save();
        canvas.clipRect(clip);
        canvas.clipRect(diff, Op.DIFFERENCE);
        drawable.draw(canvas);
        canvas.restore();
    }

    protected void onDraw(Canvas canvas) {
        this.mRect.set(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
        drawStack1(canvas, this.mStackBackground1);
        drawStack1(canvas, this.mStackDrawable1);
        drawStack2(canvas, this.mStackBackground2);
        drawStack2(canvas, this.mStackDrawable2);
        if (this.mBackground != null) {
            this.mBackground.setBounds(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
            this.mBackground.draw(canvas);
        }
        this.rect.set(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
        drawDrawable(canvas, this.mDrawable, this.rect, this.mRotation);
    }

    private void drawDrawable(Canvas canvas, Drawable drawable, Rect bound, int rotation) {
        if (drawable != null) {
            drawable.setBounds(bound);
            drawable.draw(canvas);
        }
    }

    public void setStackBackground(Bitmap backgroundBitmap1, Bitmap backgroundBitmap2) {
        Drawable drawable = null;
        this.mStackBackground1 = backgroundBitmap1 == null ? null : new BitmapDrawable(getResources(), backgroundBitmap1);
        if (backgroundBitmap2 != null) {
            drawable = new BitmapDrawable(getResources(), backgroundBitmap2);
        }
        this.mStackBackground2 = drawable;
    }

    public void setImage(Bitmap bmp) {
        this.mDrawable = new BitmapDrawable(getResources(), bmp);
        invalidate();
    }

    public void setImageRotation(int rotation) {
        this.mRotation = rotation;
    }
}

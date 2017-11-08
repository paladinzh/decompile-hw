package com.android.gallery3d.ui;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.android.gallery3d.R;
import com.android.gallery3d.R$styleable;
import com.huawei.watermark.manager.parse.WMElement;

public class MultiImageView extends ImageView {
    private float mAlpha1;
    private float mAlpha2;
    private Drawable mBackground;
    private Drawable mDrawable;
    private Drawable mGridBackground;
    private Drawable mGridDrawable1;
    private Drawable mGridDrawable2;
    private Drawable mGridDrawable3;
    private Drawable mGridDrawable4;
    private int mGridHeight;
    private int mGridMarginBottom;
    private int mGridMarginLeft;
    private int mGridMarginRight;
    private int mGridMarginTop;
    private int mGridWidth;
    private int mHeight1;
    private int mHeight2;
    private Mode mMode;
    private int mOffset1;
    private int mOffset2;
    private Paint mPaint;
    private Rect mRect;
    private Rect mRect1;
    private Rect mRect2;
    private int mResourceId;
    private int mRotation;
    private int mRotation1;
    private int mRotation2;
    private int mRotation3;
    private int mRotation4;
    private Drawable mStackBackground1;
    private Drawable mStackBackground2;
    private Drawable mStackDrawable1;
    private Drawable mStackDrawable2;
    private int mWidth1;
    private int mWidth2;

    public enum Mode {
        NORMAL(0),
        GRID(1);
        
        final int mValue;

        private Mode(int value) {
            this.mValue = value;
        }
    }

    public MultiImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mMode = Mode.NORMAL;
        this.mRotation = 0;
        this.mRotation1 = 0;
        this.mRotation2 = 0;
        this.mRotation3 = 0;
        this.mRotation4 = 0;
        this.mRect = new Rect();
        this.mRect1 = new Rect();
        this.mRect2 = new Rect();
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.Multi_Image_View, defStyle, 0);
        this.mDrawable = getDrawable();
        this.mBackground = a.getDrawable(0);
        this.mStackDrawable1 = a.getDrawable(3);
        this.mStackBackground1 = a.getDrawable(4);
        this.mWidth1 = a.getDimensionPixelSize(1, 0);
        this.mHeight1 = a.getDimensionPixelSize(2, 0);
        this.mOffset1 = a.getDimensionPixelSize(5, 0);
        this.mAlpha1 = a.getFloat(6, WMElement.CAMERASIZEVALUE1B1);
        this.mStackDrawable2 = a.getDrawable(9);
        this.mStackBackground2 = a.getDrawable(10);
        this.mWidth2 = a.getDimensionPixelSize(7, 0);
        this.mHeight2 = a.getDimensionPixelSize(8, 0);
        this.mOffset2 = a.getDimensionPixelSize(11, 0);
        this.mAlpha2 = a.getFloat(12, WMElement.CAMERASIZEVALUE1B1);
        this.mGridDrawable1 = a.getDrawable(21);
        this.mGridDrawable2 = a.getDrawable(22);
        this.mGridDrawable3 = a.getDrawable(23);
        this.mGridDrawable4 = a.getDrawable(24);
        this.mGridWidth = a.getDimensionPixelSize(13, 0);
        this.mGridHeight = a.getDimensionPixelSize(14, 0);
        this.mGridMarginLeft = a.getDimensionPixelSize(18, 0);
        this.mGridMarginTop = a.getDimensionPixelSize(17, 0);
        this.mGridMarginRight = a.getDimensionPixelSize(20, 0);
        this.mGridMarginBottom = a.getDimensionPixelSize(19, 0);
        this.mGridBackground = a.getDrawable(15);
        a.recycle();
        this.mPaint = new Paint();
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
        if (this.mMode == Mode.GRID) {
            Rect rect = new Rect(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
            drawDrawable(canvas, this.mGridBackground, rect, 0);
            int spacingLeft = getPaddingLeft() + this.mGridMarginLeft;
            int spacingTop = getPaddingTop() + this.mGridMarginTop;
            int spacingRight = getPaddingRight() + this.mGridMarginRight;
            int spacingBottom = getPaddingBottom() + this.mGridMarginBottom;
            int right;
            int bottom;
            if (getLayoutDirection() == 1) {
                drawGrid(canvas, this.mGridDrawable1, this.mRotation1, (getWidth() - spacingRight) - this.mGridWidth, spacingTop, getWidth() - spacingRight, spacingTop + this.mGridHeight);
                drawGrid(canvas, this.mGridDrawable2, this.mRotation2, spacingLeft, spacingTop, spacingLeft + this.mGridWidth, spacingTop + this.mGridHeight);
                right = getWidth() - spacingRight;
                bottom = getHeight() - spacingBottom;
                drawGrid(canvas, this.mGridDrawable3, this.mRotation3, right - this.mGridWidth, bottom - this.mGridHeight, right, bottom);
                drawGrid(canvas, this.mGridDrawable4, this.mRotation4, spacingLeft, (getHeight() - spacingBottom) - this.mGridHeight, spacingLeft + this.mGridWidth, getHeight() - spacingBottom);
                return;
            }
            drawGrid(canvas, this.mGridDrawable1, this.mRotation1, spacingLeft, spacingTop, spacingLeft + this.mGridWidth, spacingTop + this.mGridHeight);
            drawGrid(canvas, this.mGridDrawable2, this.mRotation2, (getWidth() - spacingRight) - this.mGridWidth, spacingTop, getWidth() - spacingRight, spacingTop + this.mGridHeight);
            drawGrid(canvas, this.mGridDrawable3, this.mRotation3, spacingLeft, (getHeight() - spacingBottom) - this.mGridHeight, spacingLeft + this.mGridWidth, getHeight() - spacingBottom);
            right = getWidth() - spacingRight;
            bottom = getHeight() - spacingBottom;
            drawGrid(canvas, this.mGridDrawable4, this.mRotation4, right - this.mGridWidth, bottom - this.mGridHeight, right, bottom);
            return;
        }
        Drawable drawable = this.mDrawable;
        if (getScaleType() != ScaleType.CENTER) {
            rect = new Rect(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
            drawDrawable(canvas, this.mDrawable, rect, this.mRotation);
        } else if (drawable != null) {
            this.mPaint.setColor(getResources().getColor(R.color.album_cover_background));
            this.mPaint.setAntiAlias(true);
            this.mPaint.setStyle(Style.FILL);
            canvas.drawRect(0.0f, 0.0f, (float) getWidth(), (float) (getHeight() - (this.mHeight1 - this.mHeight2)), this.mPaint);
            rect = new Rect(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            rect.offset((((getWidth() - getPaddingLeft()) - getPaddingRight()) - drawable.getIntrinsicWidth()) / 2, (((getHeight() - getPaddingTop()) - getPaddingBottom()) - drawable.getIntrinsicHeight()) / 2);
            drawDrawable(canvas, drawable, rect, this.mRotation);
        }
    }

    private void drawDrawable(Canvas canvas, Drawable drawable, Rect bound, int rotation) {
        if (drawable != null) {
            if (rotation == 0) {
                drawable.setBounds(bound);
                drawable.draw(canvas);
            } else {
                int dx = bound.width() / 2;
                int dy = bound.height() / 2;
                canvas.save();
                canvas.translate((float) bound.left, (float) bound.top);
                canvas.rotate((float) rotation, (float) dx, (float) dy);
                bound.offsetTo(0, 0);
                drawable.setBounds(bound);
                drawable.draw(canvas);
                canvas.restore();
            }
        }
    }

    private void drawGrid(Canvas canvas, Drawable drawable, int rotation, int left, int top, int right, int bottom) {
        if (drawable != null) {
            if (rotation == 0) {
                drawable.setBounds(left, top, right, bottom);
                drawable.draw(canvas);
            } else {
                int dx = this.mGridWidth / 2;
                int dy = this.mGridHeight / 2;
                canvas.save();
                canvas.translate((float) left, (float) top);
                canvas.rotate((float) rotation, (float) dx, (float) dy);
                drawable.setBounds(0, 0, this.mGridWidth, this.mGridHeight);
                drawable.draw(canvas);
                canvas.restore();
            }
        }
    }

    public void resetToEmpty() {
        resetToEmpty(R.drawable.pic_gallery_album_empty_album_cover);
    }

    public void resetToEmpty(int imageResId) {
        setScaleType(ScaleType.CENTER);
        setBackground(R.drawable.album_cover_background);
        setImage(imageResId);
        setImageRotation(0);
        setStackBackground((int) R.drawable.album_cover_background, (int) R.drawable.album_cover_background);
    }

    public void setBackground(int resid) {
        this.mBackground = resid == 0 ? null : getResources().getDrawable(resid);
    }

    public void setStackBackground(Bitmap backgroundBitmap1, Bitmap backgroundBitmap2) {
        Drawable drawable = null;
        this.mStackBackground1 = backgroundBitmap1 == null ? null : new BitmapDrawable(getResources(), backgroundBitmap1);
        if (backgroundBitmap2 != null) {
            drawable = new BitmapDrawable(getResources(), backgroundBitmap2);
        }
        this.mStackBackground2 = drawable;
    }

    public void setGridDrawable(Drawable d1, Drawable d2, Drawable d3, Drawable d4) {
        if (this.mMode == Mode.NORMAL) {
            throw new IllegalStateException("Mode is not GRID !");
        }
        this.mGridDrawable1 = d1;
        this.mGridDrawable2 = d2;
        this.mGridDrawable3 = d3;
        this.mGridDrawable4 = d4;
        invalidate();
    }

    public void setStackBackground(int resId1, int resId2) {
        Resources res = getResources();
        this.mStackBackground1 = res.getDrawable(resId1);
        this.mStackBackground2 = res.getDrawable(resId2);
    }

    public void setGridRotation(int r1, int r2, int r3, int r4) {
        this.mRotation1 = r1;
        this.mRotation2 = r2;
        this.mRotation3 = r3;
        this.mRotation4 = r4;
    }

    public void setImage(Bitmap bmp) {
        this.mDrawable = new BitmapDrawable(getResources(), bmp);
        this.mResourceId = 0;
        invalidate();
    }

    public void setImage(int resId) {
        if (this.mResourceId != resId) {
            this.mResourceId = resId;
            this.mDrawable = getResources().getDrawable(resId);
            invalidate();
        }
    }

    public void setImageRotation(int rotation) {
        this.mRotation = rotation;
    }

    public void switchTo(Mode mode) {
        if (this.mMode != mode) {
            this.mMode = mode;
            if (mode == Mode.NORMAL) {
                this.mGridDrawable1 = null;
                this.mGridDrawable2 = null;
                this.mGridDrawable3 = null;
                this.mGridDrawable4 = null;
                this.mRotation1 = 0;
                this.mRotation2 = 0;
                this.mRotation3 = 0;
                this.mRotation4 = 0;
            } else {
                setImageDrawable(null);
                this.mDrawable = null;
                this.mResourceId = 0;
                this.mRotation = 0;
            }
        }
    }
}

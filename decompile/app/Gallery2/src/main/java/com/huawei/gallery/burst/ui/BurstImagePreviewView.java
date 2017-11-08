package com.huawei.gallery.burst.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.View;
import com.android.gallery3d.R;
import com.huawei.gallery.burst.StateSelectable;
import com.huawei.gallery.burst.ui.MyGallery.LayoutParams;

public class BurstImagePreviewView extends View implements StateSelectable {
    private int mAnimationArea;
    private Bitmap mBitmap;
    private int mBitmapHeight;
    private int mBitmapWidth;
    private int mBound;
    private Drawable mCheckOffBitmap;
    private Drawable mCheckOnBitmap;
    private int mDrawableSize;
    private boolean mIsCurrentSelected = false;
    private boolean mIsTouched = false;
    private int mRotation;
    private float mScale;
    private int mSpacing;
    private Rect mTmpRect = new Rect();
    private Rect mVisibleRect = new Rect();
    private int mWidthPixels;

    public BurstImagePreviewView(Context context) {
        super(context);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        this.mWidthPixels = Math.min(metrics.widthPixels, metrics.heightPixels);
        this.mCheckOffBitmap = getResources().getDrawable(R.drawable.btn_gallery_check_off);
        this.mCheckOnBitmap = getResources().getDrawable(R.drawable.btn_gallery_check_on);
        this.mDrawableSize = this.mCheckOffBitmap.getIntrinsicWidth();
        this.mCheckOffBitmap.setBounds(0, 0, this.mDrawableSize, this.mDrawableSize);
        this.mCheckOnBitmap.setBounds(0, 0, this.mDrawableSize, this.mDrawableSize);
        this.mSpacing = getResources().getDimensionPixelSize(R.dimen.burst_photo_preview_spacing);
        this.mAnimationArea = this.mBound + 50;
        this.mScale = ((float) (this.mAnimationArea - this.mDrawableSize)) / 50.0f;
    }

    public void setAspect(int width, int height) {
        this.mBitmapWidth = width;
        this.mBitmapHeight = height;
    }

    public void setImgRotation(int rotation) {
        this.mRotation = rotation;
    }

    public void updateView(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    public void onHeightChange(int parentHeight) {
        int bmWidth = this.mBitmapWidth;
        int bmHeight = this.mBitmapHeight;
        if (this.mRotation % 180 == 90) {
            bmWidth = this.mBitmapHeight;
            bmHeight = this.mBitmapWidth;
        }
        int selfHeight = (int) (((double) parentHeight) * 0.9d);
        int selfWidth = (selfHeight * bmWidth) / bmHeight;
        if (selfWidth > this.mWidthPixels) {
            selfWidth = this.mWidthPixels;
            selfHeight = (selfWidth * bmHeight) / bmWidth;
        }
        this.mBound = ((this.mWidthPixels - bmWidth) - (this.mSpacing * 2)) / 2;
        setLayoutParams(new LayoutParams(selfWidth, selfHeight));
    }

    public void setSelectState(boolean select) {
        if (this.mIsCurrentSelected != select) {
            this.mIsCurrentSelected = select;
            invalidate();
        }
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        boolean pressed = isPressed();
        if (this.mIsTouched != pressed) {
            this.mIsTouched = pressed;
            invalidate();
        }
    }

    protected void onDraw(Canvas canvas) {
        int xPosition;
        Bitmap bitmap = this.mBitmap;
        if (bitmap != null) {
            float scaleX;
            float scaleY;
            float w = (float) getWidth();
            float h = (float) getHeight();
            boolean land = this.mRotation % 180 == 90;
            canvas.save();
            canvas.translate(w / 2.0f, h / 2.0f);
            if (this.mRotation != 0) {
                canvas.rotate((float) this.mRotation);
            }
            if (land) {
                scaleX = w / ((float) this.mBitmapHeight);
                scaleY = h / ((float) this.mBitmapWidth);
                canvas.translate((-h) / 2.0f, (-w) / 2.0f);
            } else {
                scaleX = w / ((float) this.mBitmapWidth);
                scaleY = h / ((float) this.mBitmapHeight);
                canvas.translate((-w) / 2.0f, (-h) / 2.0f);
            }
            canvas.scale(scaleX, scaleY);
            canvas.drawBitmap(bitmap, 0.0f, 0.0f, null);
            canvas.restore();
        }
        canvas.save();
        getLocalVisibleRect(this.mVisibleRect);
        if (this.mVisibleRect.right >= getWidth()) {
            xPosition = getWidth() - this.mDrawableSize;
        } else if (this.mVisibleRect.right <= this.mBound) {
            xPosition = 0;
        } else if (this.mVisibleRect.right < this.mAnimationArea) {
            xPosition = (int) (((float) (this.mVisibleRect.right - this.mBound)) * this.mScale);
        } else {
            xPosition = this.mVisibleRect.right - this.mDrawableSize;
        }
        canvas.translate((float) xPosition, (float) (getHeight() - this.mDrawableSize));
        (this.mIsCurrentSelected ? this.mCheckOnBitmap : this.mCheckOffBitmap).draw(canvas);
        canvas.restore();
    }

    public boolean isWholeWidthVisible() {
        getLocalVisibleRect(this.mTmpRect);
        return this.mTmpRect.width() >= getWidth();
    }

    public void offsetLeftAndRight(int offset) {
        super.offsetLeftAndRight(offset);
        getLocalVisibleRect(this.mTmpRect);
        if (this.mTmpRect.right != this.mVisibleRect.right) {
            invalidate();
        }
    }
}

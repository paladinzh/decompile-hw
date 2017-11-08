package com.huawei.gallery.burst.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.burst.StateSelectable;

public class BurstImageThumbView extends ImageView implements StateSelectable {
    private Drawable mBest;
    private int mDrawableSize = 0;
    private boolean mIsBest = false;
    private boolean mIsCurrentSelected = false;
    private int mRotation;
    private Drawable mSelectFrame;

    public BurstImageThumbView(Context context, Bitmap bitmap) {
        super(context);
        setImageBitmap(bitmap);
        setScaleType(ScaleType.CENTER_CROP);
        this.mSelectFrame = getResources().getDrawable(R.drawable.pic_frame_selected);
        this.mBest = getResources().getDrawable(R.drawable.ic_gallery_bestphoto);
        this.mDrawableSize = this.mBest.getIntrinsicWidth();
        this.mBest.setBounds(0, 0, this.mDrawableSize, this.mDrawableSize);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mSelectFrame.setBounds(0, 0, w, h);
    }

    public void setBest(boolean isBest) {
        this.mIsBest = isBest;
    }

    public void setImgRotation(int rotation) {
        this.mRotation = rotation;
    }

    public void setSelectState(boolean select) {
        if (this.mIsCurrentSelected != select) {
            this.mIsCurrentSelected = select;
            invalidate();
        }
    }

    protected void onDraw(Canvas canvas) {
        int xPos = 0;
        canvas.save();
        if (this.mRotation != 0) {
            canvas.rotate((float) this.mRotation, ((float) getWidth()) / 2.0f, ((float) getHeight()) / 2.0f);
        }
        super.onDraw(canvas);
        canvas.restore();
        if (this.mIsCurrentSelected) {
            this.mSelectFrame.draw(canvas);
        }
        if (this.mIsBest) {
            canvas.save();
            if (getLayoutDirection() != 1) {
                xPos = (getWidth() - this.mDrawableSize) - GalleryUtils.dpToPixel(4);
            }
            canvas.translate((float) xPos, (float) ((getHeight() - this.mDrawableSize) - GalleryUtils.dpToPixel(2)));
            this.mBest.draw(canvas);
            canvas.restore();
        }
    }
}

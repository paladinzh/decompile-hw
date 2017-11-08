package com.android.mms.attachment.ui;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import com.android.mms.exif.ExifInterface;
import com.android.mms.exif.ExifInterface.OrientationParams;

public class OrientedBitmapDrawable extends BitmapDrawable {
    private boolean mApplyGravity = true;
    private int mCenterX;
    private int mCenterY;
    private final Rect mDstRect = new Rect();
    private final OrientationParams mOrientationParams;

    public static BitmapDrawable create(int orientation, Resources res, Bitmap bitmap) {
        if (orientation <= 1) {
            return new BitmapDrawable(res, bitmap);
        }
        return new OrientedBitmapDrawable(orientation, res, bitmap);
    }

    private OrientedBitmapDrawable(int orientation, Resources res, Bitmap bitmap) {
        super(res, bitmap);
        this.mOrientationParams = ExifInterface.getOrientationParams(orientation);
    }

    public int getIntrinsicWidth() {
        if (this.mOrientationParams.invertDimensions) {
            return super.getIntrinsicHeight();
        }
        return super.getIntrinsicWidth();
    }

    public int getIntrinsicHeight() {
        if (this.mOrientationParams.invertDimensions) {
            return super.getIntrinsicWidth();
        }
        return super.getIntrinsicHeight();
    }

    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        this.mApplyGravity = true;
    }

    public void draw(Canvas canvas) {
        if (this.mApplyGravity) {
            Gravity.apply(getGravity(), getIntrinsicWidth(), getIntrinsicHeight(), getBounds(), this.mDstRect);
            this.mCenterX = this.mDstRect.centerX();
            this.mCenterY = this.mDstRect.centerY();
            if (this.mOrientationParams.invertDimensions) {
                Matrix matrix = new Matrix();
                matrix.setRotate((float) this.mOrientationParams.rotation, (float) this.mCenterX, (float) this.mCenterY);
                RectF rotatedRect = new RectF(this.mDstRect);
                matrix.mapRect(rotatedRect);
                this.mDstRect.set((int) rotatedRect.left, (int) rotatedRect.top, (int) rotatedRect.right, (int) rotatedRect.bottom);
            }
            this.mApplyGravity = false;
        }
        canvas.save();
        canvas.scale((float) this.mOrientationParams.scaleX, (float) this.mOrientationParams.scaleY, (float) this.mCenterX, (float) this.mCenterY);
        canvas.rotate((float) this.mOrientationParams.rotation, (float) this.mCenterX, (float) this.mCenterY);
        canvas.drawBitmap(getBitmap(), (Rect) null, this.mDstRect, getPaint());
        canvas.restore();
    }
}

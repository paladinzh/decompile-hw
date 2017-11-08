package com.huawei.gallery.editor.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.editor.filters.FilterIllusionRepresentation;
import com.huawei.gallery.editor.ui.IllusionView.Listener;

public class FullScreenControl extends ShapeControl {
    public void setScrImageInfo(Rect viewBounds, Listener listener, FilterIllusionRepresentation representation) {
        Rect imageBounds = new Rect(0, 0, viewBounds.width(), viewBounds.height());
        if (this.mImageBounds == null) {
            this.mImageBounds = new Rect(imageBounds);
            initPoints(representation);
        } else {
            float scale = ((float) imageBounds.width()) / ((float) this.mImageBounds.width());
            this.mImageBounds.set(imageBounds);
            updatePoints(representation, scale);
        }
        if (this.mListener == null) {
            this.mListener = listener;
        }
        actionUp(representation, true);
    }

    public void actionDown(float x, float y) {
        this.mDownX = x;
        this.mDownY = y;
        this.mNeedCover = true;
        this.mTouchStyle = 1;
    }

    public void actionDown(float x1, float y1, float x2, float y2) {
        this.mDown1X = x1;
        this.mDown1Y = y1;
        this.mDown2X = x2;
        this.mDown2Y = y2;
        this.mTouchStyle = 2;
        this.mNeedCover = true;
    }

    public void actionUp(FilterIllusionRepresentation representation, boolean force) {
        this.mTouchStyle = 0;
    }

    public void actionMove(float x, float y) {
        this.mNeedCover = true;
    }

    public void actionMove(float x1, float y1, float x2, float y2) {
        this.mNeedCover = true;
    }

    public void draw(Canvas canvas) {
        if (this.mNeedCover && this.mListener != null) {
            Matrix matrix = new Matrix();
            Bitmap bitmap = this.mListener.getApplyBitmap();
            if (bitmap == null || bitmap.isRecycled()) {
                GalleryLog.d("Circle", "bitmap not right");
                return;
            }
            matrix.setTranslate(((float) (-bitmap.getWidth())) / 2.0f, ((float) (-bitmap.getHeight())) / 2.0f);
            matrix.postScale(((float) this.mImageBounds.width()) / ((float) bitmap.getWidth()), ((float) this.mImageBounds.height()) / ((float) bitmap.getHeight()));
            matrix.postTranslate(((float) this.mImageBounds.width()) / 2.0f, ((float) this.mImageBounds.height()) / 2.0f);
            canvas.drawBitmap(bitmap, matrix, null);
        }
    }
}

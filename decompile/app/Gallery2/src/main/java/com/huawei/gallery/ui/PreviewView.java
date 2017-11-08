package com.huawei.gallery.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.widget.ImageView;

public class PreviewView extends ImageView {
    private Bitmap mBitmap = null;
    private int mRotate = 0;
    private TYPE mType = TYPE.NORMAL;

    private enum TYPE {
        NORMAL,
        CROP
    }

    public PreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void updateType(int w, int h) {
        if (this.mRotate % 90 == 0 && this.mRotate % 180 != 0) {
            int t = w;
            w = h;
            h = t;
        }
        float style = ((float) h) / ((float) w);
        if (getResources().getConfiguration().orientation != 1) {
            this.mType = TYPE.NORMAL;
        } else if (style >= 1.7778f || style <= 1.4807f) {
            this.mType = TYPE.NORMAL;
        } else {
            this.mType = TYPE.CROP;
        }
    }

    public void setRotate(int rotate) {
        this.mRotate = rotate;
    }

    public void setBitmap(Bitmap bitmap) {
        if (bitmap != null && bitmap != this.mBitmap) {
            updateType(bitmap.getWidth(), bitmap.getHeight());
            this.mBitmap = bitmap;
            if (this.mType != TYPE.NORMAL || (this.mRotate % 90 == 0 && this.mRotate % 180 != 0)) {
                invalidate();
            } else {
                setImageBitmap(bitmap);
            }
        }
    }

    protected void onDraw(Canvas canvas) {
        if (this.mBitmap != null) {
            if (this.mType == TYPE.NORMAL && this.mRotate == 0) {
                super.onDraw(canvas);
            } else if (this.mType == TYPE.NORMAL) {
                canvas.drawBitmap(this.mBitmap, getRotateMatrix(canvas), null);
            } else {
                canvas.drawBitmap(this.mBitmap, getCropMatrix(canvas), null);
            }
        }
    }

    private Matrix getRotateMatrix(Canvas canvas) {
        float scale;
        Matrix matrix = new Matrix();
        if (this.mRotate % 90 != 0 || this.mRotate % 180 == 0) {
            scale = Math.min(((float) canvas.getWidth()) / ((float) this.mBitmap.getWidth()), ((float) canvas.getHeight()) / ((float) this.mBitmap.getHeight()));
        } else {
            scale = Math.min(((float) canvas.getWidth()) / ((float) this.mBitmap.getHeight()), ((float) canvas.getHeight()) / ((float) this.mBitmap.getWidth()));
        }
        matrix.postTranslate(((float) (-this.mBitmap.getWidth())) / 2.0f, ((float) (-this.mBitmap.getHeight())) / 2.0f);
        matrix.postScale(scale, scale);
        matrix.postRotate((float) this.mRotate);
        matrix.postTranslate(((float) canvas.getWidth()) / 2.0f, ((float) canvas.getHeight()) / 2.0f);
        return matrix;
    }

    private Matrix getCropMatrix(Canvas canvas) {
        Matrix matrix = new Matrix();
        float width = (float) canvas.getWidth();
        int width2 = (this.mRotate % 90 != 0 || this.mRotate % 180 == 0) ? this.mBitmap.getWidth() : this.mBitmap.getHeight();
        float scale = width / ((float) width2);
        matrix.postTranslate(((float) (-this.mBitmap.getWidth())) / 2.0f, ((float) (-this.mBitmap.getHeight())) / 2.0f);
        matrix.postScale(scale, scale);
        matrix.postRotate((float) this.mRotate);
        matrix.postTranslate(((float) canvas.getWidth()) / 2.0f, ((float) canvas.getHeight()) / 2.0f);
        return matrix;
    }
}

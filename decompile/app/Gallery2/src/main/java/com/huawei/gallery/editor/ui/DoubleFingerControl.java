package com.huawei.gallery.editor.ui;

import android.graphics.Matrix;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.android.gallery3d.util.GalleryUtils;

public class DoubleFingerControl {
    private static final int DEFAULT_RADIUS = GalleryUtils.dpToPixel(90);
    private Delegate mDelegate;
    private float mDown1X;
    private float mDown1Y;
    private float mDown2X;
    private float mDown2Y;
    private Matrix mMatrix;

    public interface Delegate {
        float[] getValidDisplacement(float f, float f2);
    }

    public DoubleFingerControl() {
        clear();
    }

    public void setMatrix(Matrix matrix) {
        this.mMatrix = matrix;
    }

    public Matrix getMatrix() {
        return this.mMatrix;
    }

    public void clear() {
        this.mDown1X = GroundOverlayOptions.NO_DIMENSION;
        this.mDown1Y = GroundOverlayOptions.NO_DIMENSION;
        this.mDown2X = GroundOverlayOptions.NO_DIMENSION;
        this.mDown2Y = GroundOverlayOptions.NO_DIMENSION;
    }

    public void actionDown(float x1, float y1, float x2, float y2) {
        this.mDown1X = x1;
        this.mDown1Y = y1;
        this.mDown2X = x2;
        this.mDown2Y = y2;
    }

    public void actionMove(float x1, float y1, float x2, float y2) {
        if (this.mDown1X < 0.0f || this.mDown1Y < 0.0f || this.mDown2X < 0.0f || this.mDown2Y < 0.0f) {
            actionDown(x1, y1, x2, y2);
            return;
        }
        calculateMatrix(x1, y1, x2, y2);
        this.mDown1X = x1;
        this.mDown1Y = y1;
        this.mDown2X = x2;
        this.mDown2Y = y2;
    }

    public void reset() {
        if (this.mMatrix != null) {
            this.mMatrix.reset();
        }
    }

    public void setDelegate(Delegate delegate) {
        if (this.mDelegate == null) {
            this.mDelegate = delegate;
        }
    }

    private boolean isVaildScale(float scale) {
        return scale < 10.0f && scale > 0.5f;
    }

    private boolean needScale(float scale) {
        float[] values = new float[9];
        this.mMatrix.getValues(values);
        if (isVaildScale(values[0] * scale)) {
            return isVaildScale(values[4] * scale);
        }
        return false;
    }

    private void calculateMatrix(float x1, float y1, float x2, float y2) {
        if (this.mMatrix != null) {
            float oldLength = getLength(this.mDown1X, this.mDown1Y, this.mDown2X, this.mDown2Y);
            float dx = ((x1 + x2) / 2.0f) - ((this.mDown1X + this.mDown2X) / 2.0f);
            float dy = ((y1 + y2) / 2.0f) - ((this.mDown1Y + this.mDown2Y) / 2.0f);
            float scale = (((float) DEFAULT_RADIUS) + ((getLength(x1, y1, x2, y2) - oldLength) / 2.0f)) / ((float) DEFAULT_RADIUS);
            if (needScale(scale)) {
                this.mMatrix.postScale(scale, scale);
            }
            if (this.mDelegate != null) {
                float[] tmpDal = this.mDelegate.getValidDisplacement(dx, dy);
                this.mMatrix.postTranslate(tmpDal[0], tmpDal[1]);
            } else {
                this.mMatrix.postTranslate(dx, dy);
            }
        }
    }

    private float getLength(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (float) Math.sqrt((double) ((dx * dx) + (dy * dy)));
    }
}

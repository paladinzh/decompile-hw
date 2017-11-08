package com.huawei.gallery.editor.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.view.View;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.editor.filters.FilterMosaicRepresentation;
import com.huawei.gallery.editor.filters.FilterMosaicRepresentation.StrokeData;
import com.huawei.gallery.editor.tools.EditorUtils;
import com.huawei.gallery.editor.ui.MosaicView.Delegate;
import com.huawei.gallery.util.ResourceUtils;

public class MagnifierControl {
    public static final int ABOVE_LENGHT = GalleryUtils.dpToPixel(20);
    private static final int MARGIN_COLOR = GalleryUtils.dpToPixel(2);
    private static final int MARGIN_ERASER = GalleryUtils.dpToPixel(3);
    private static final int RADIUS_COLOR = GalleryUtils.dpToPixel(20);
    public static final int RADIUS_ERASER = GalleryUtils.dpToPixel(43);
    private Delegate mDelegate;
    private FilterMosaicRepresentation mFilterMosaicRepresentation;
    private NinePatchDrawable mFrame;
    private Rect mImageBounds;
    private Rect mImageSource;
    private boolean mIsSupport;
    private boolean mIsVisible;
    private Path mPath;
    private float mPointX;
    private float mPointY;
    private float mRadius;
    private int mStyle;

    public void setFilterMosaicRepresentation(FilterMosaicRepresentation representation) {
        this.mFilterMosaicRepresentation = representation;
    }

    public void clear() {
        this.mIsVisible = false;
        this.mPointX = GroundOverlayOptions.NO_DIMENSION;
        this.mPointY = GroundOverlayOptions.NO_DIMENSION;
    }

    public int getMarginStyle(float x, float y) {
        int style = 0;
        if (x <= this.mRadius) {
            style = 1;
        } else if (x >= ((float) this.mImageBounds.width()) - this.mRadius) {
            style = 8;
        }
        if (y <= this.mRadius) {
            return style | 4;
        }
        if (y >= ((float) this.mImageBounds.height()) - this.mRadius) {
            return style | 16;
        }
        return style;
    }

    public void setSupport(boolean isSupport) {
        this.mIsSupport = isSupport;
    }

    public boolean getVisible() {
        return this.mIsVisible;
    }

    private void updatePoints(float scale) {
        if (EditorUtils.isAlmostEquals(this.mPointX, Float.NaN) || EditorUtils.isAlmostEquals(this.mPointY, Float.NaN)) {
            this.mPointX *= scale;
            this.mPointY *= scale;
        }
    }

    private Path getCirclePath(int cx, int cy, int radius) {
        Path path = new Path();
        if (this.mPath == null) {
            int x;
            float dx;
            path.moveTo((float) (cx - radius), (float) cy);
            for (x = cx - radius; x <= cx + radius; x++) {
                dx = (float) Math.abs(cx - x);
                path.lineTo((float) x, ((float) cy) + ((float) Math.sqrt((double) (((float) (radius * radius)) - (dx * dx)))));
            }
            for (x = cx + radius; x >= cx - radius; x--) {
                dx = (float) Math.abs(cx - x);
                path.lineTo((float) x, ((float) cy) - ((float) Math.sqrt((double) (((float) (radius * radius)) - (dx * dx)))));
            }
            path.lineTo((float) (cx - radius), (float) cy);
        } else {
            Matrix matrix = new Matrix();
            matrix.setTranslate((float) cx, (float) cy);
            path.addPath(this.mPath, matrix);
        }
        return path;
    }

    public void setScrImageInfo(Rect viewBounds, Rect source, Delegate delegate) {
        Rect imageBounds = new Rect(0, 0, viewBounds.width(), viewBounds.height());
        if (this.mImageBounds == null) {
            this.mImageBounds = new Rect(imageBounds);
        } else {
            float scale = ((float) imageBounds.width()) / ((float) this.mImageBounds.width());
            this.mImageBounds.set(imageBounds);
            updatePoints(scale);
        }
        this.mImageSource.set(source);
        if (this.mDelegate == null) {
            this.mDelegate = delegate;
        }
    }

    public MagnifierControl(View view) {
        this.mFrame = null;
        this.mIsVisible = false;
        this.mIsSupport = false;
        this.mImageSource = new Rect();
        this.mPointX = GroundOverlayOptions.NO_DIMENSION;
        this.mPointY = GroundOverlayOptions.NO_DIMENSION;
        this.mStyle = 0;
        this.mRadius = 0.0f;
        this.mPath = null;
        this.mPath = getCirclePath(0, 0, RADIUS_ERASER);
        this.mFrame = (NinePatchDrawable) ResourceUtils.getDrawable(view.getResources(), Integer.valueOf(R.drawable.erasing));
    }

    public boolean centerIsInside(float x, float y) {
        if (this.mImageBounds == null) {
            return false;
        }
        return new Rect((int) this.mRadius, (int) this.mRadius, (int) (((float) this.mImageBounds.width()) - this.mRadius), (int) (((float) this.mImageBounds.height()) - this.mRadius)).contains((int) x, (int) y);
    }

    public void actionDown(float x, float y, float radius) {
        if (!(this.mDelegate == null || this.mDelegate.getExpandBitmap() == null)) {
            this.mRadius = (((float) this.mImageBounds.width()) * radius) / ((float) this.mImageSource.width());
        }
        if (centerIsInside(x, y)) {
            this.mIsVisible = true;
            this.mPointX = x;
            this.mPointY = y;
            return;
        }
        this.mIsVisible = false;
        actionMove(x, y);
    }

    public boolean isPointerValid() {
        return this.mIsVisible || this.mStyle == 0;
    }

    public void actionMove(float x, float y) {
        if (centerIsInside(x, y)) {
            this.mIsVisible = true;
            this.mPointX = x;
            this.mPointY = y;
            return;
        }
        this.mStyle = getMarginStyle(x, y);
        if ((this.mStyle & 1) != 0) {
            this.mPointX = this.mRadius;
            if ((this.mStyle & 4) != 0) {
                this.mPointY = this.mRadius;
            } else if ((this.mStyle & 16) != 0) {
                this.mPointY = ((float) this.mImageBounds.height()) - this.mRadius;
            } else {
                this.mPointY = y;
            }
        } else if ((this.mStyle & 8) != 0) {
            this.mPointX = ((float) this.mImageBounds.width()) - this.mRadius;
            if ((this.mStyle & 4) != 0) {
                this.mPointY = this.mRadius;
            } else if ((this.mStyle & 16) != 0) {
                this.mPointY = ((float) this.mImageBounds.height()) - this.mRadius;
            } else {
                this.mPointY = y;
            }
        } else {
            this.mPointX = x;
            if ((this.mStyle & 4) != 0) {
                this.mPointY = this.mRadius;
            } else if ((this.mStyle & 16) != 0) {
                this.mPointY = ((float) this.mImageBounds.height()) - this.mRadius;
            } else {
                this.mPointY = y;
            }
        }
    }

    public float getX() {
        return this.mPointX;
    }

    public float getY() {
        return this.mPointY;
    }

    public void actionUp() {
        this.mIsVisible = false;
    }

    public void draw(Canvas canvas, StrokeData sd, int style, Matrix matrix) {
        if (this.mDelegate != null) {
            float[] tmpPoint = this.mDelegate.getAbsolutePreviewPointWithOutCrop(this.mPointX, this.mPointY);
            switch (style) {
                case 1:
                    if (this.mIsSupport && this.mIsVisible && sd != null) {
                        drawCanvasWithBitmap(canvas, sd, (int) this.mPointX, (((int) this.mPointY) - ABOVE_LENGHT) - RADIUS_ERASER, RADIUS_ERASER, (int) tmpPoint[0], (int) tmpPoint[1], matrix.mapRadius(1.5f) * this.mDelegate.getExpandBitmapInitScale());
                        break;
                    }
                case 2:
                    if (this.mIsVisible) {
                        drawCanvasWithColor(canvas, (int) this.mPointX, (((int) this.mPointY) - RADIUS_COLOR) - ABOVE_LENGHT, RADIUS_COLOR, this.mDelegate.getColor((int) tmpPoint[0], (int) tmpPoint[1]));
                        break;
                    }
                    break;
            }
        }
    }

    private void drawCanvasWithColor(Canvas canvas, int x, int y, int radius, int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawCircle((float) x, (float) y, (float) radius, paint);
        drawFrame(canvas, x, y, MARGIN_COLOR + radius);
    }

    private Matrix getMatrix(int cx, int cy, int bx, int by, float scale) {
        Matrix matrix = new Matrix();
        matrix.postTranslate((float) (-bx), (float) (-by));
        matrix.postScale(scale, scale);
        matrix.postTranslate((float) cx, (float) cy);
        return matrix;
    }

    private void drawCanvasWithBitmap(Canvas canvas, StrokeData sd, int cx, int cy, int radius, int bx, int by, float expandScale) {
        if (this.mDelegate != null) {
            Bitmap target = this.mDelegate.getExpandBitmap();
            if (target != null) {
                Matrix matrix = getMatrix(cx, cy, bx, by, expandScale);
                canvas.clipPath(getCirclePath(cx + 0, cy + 0, radius));
                canvas.drawColor(-16777216);
                canvas.drawBitmap(target, matrix, null);
                Bitmap bmp = this.mDelegate.getCurrentDrawBitmap(this.mFilterMosaicRepresentation.getCurrentSegment());
                if (bmp != null) {
                    float[] tmpPoint = this.mDelegate.getAbsolutePreviewPoint(this.mPointX, this.mPointY);
                    canvas.drawBitmap(bmp, getMatrix(cx, cy, (int) tmpPoint[0], (int) tmpPoint[1], expandScale), null);
                }
                Paint paint = new Paint();
                drawFrame(canvas, cx, cy, MARGIN_ERASER + radius);
                float eraserRadius = sd.mRadius;
                paint.setColor(-1);
                paint.setStyle(Style.STROKE);
                paint.setStrokeWidth(2.0f);
                canvas.drawCircle((float) cx, (float) cy, expandScale * eraserRadius, paint);
            }
        }
    }

    protected void drawFrame(Canvas canvas, int cx, int cy, int radius) {
        this.mFrame.setBounds(cx - radius, cy - radius, cx + radius, cy + radius);
        this.mFrame.draw(canvas);
    }
}

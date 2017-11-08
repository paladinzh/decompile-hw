package com.huawei.gallery.photorectify;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;
import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.ui.BitmapTexture;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.PositionController;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.huawei.gallery.util.LayoutHelper;
import com.huawei.watermark.manager.parse.WMElement;

public class PhotoRectifyView extends GLView {
    private BitmapTexture mBitmapTexture;
    private float mCenterX;
    private float mCenterY;
    private DetectBoundsView mDetectBoundsView;
    private int mDisplayHeight = -1;
    private int mDisplayWidth = -1;
    private SynchronizedHandler mHandler;
    private boolean mHasModified;
    private int mImageHeight = -1;
    private int mImageWidth = -1;
    private Matrix mMatrix;
    private Paint mPaint = new Paint();
    private RelativeLayout mRootView;
    private int mRotation;
    private float mScale = WMElement.CAMERASIZEVALUE1B1;

    private class DetectBoundsView extends View implements OnTouchListener {
        private Bitmap mCutPoint;
        private RectBounds mDetectRatioRect = new RectBounds();
        private boolean mHasSetRectBounds;
        private PointF mLastPosition = new PointF();
        private float mReferenceX;
        private float mReferenceY;
        private TouchPos mTouchType = null;

        public DetectBoundsView(Context context) {
            super(context);
            this.mCutPoint = BitmapFactory.decodeResource(context.getResources(), R.drawable.cut_point);
        }

        private void moveRectBoundsToTarget(MotionEvent event) {
            float dx = ((event.getX() - this.mReferenceX) / PhotoRectifyView.this.mScale) / ((float) PhotoRectifyView.this.mImageWidth);
            float dy = ((event.getY() - this.mReferenceY) / PhotoRectifyView.this.mScale) / ((float) PhotoRectifyView.this.mImageHeight);
            this.mReferenceX = event.getX();
            this.mReferenceY = event.getY();
            RectBounds rb = this.mDetectRatioRect;
            PointF point = new PointF();
            point.set(this.mReferenceX, this.mReferenceY);
            PhotoRectifyView.this.inverseMapPoint(point);
            if (this.mTouchType != null) {
                if (this.mTouchType == TouchPos.RECT_POINT_INNER) {
                    rb.offset(Utils.clamp(dx, Math.max(-rb.topLeft.x, -rb.bottomLeft.x), Math.min(WMElement.CAMERASIZEVALUE1B1 - rb.topRight.x, WMElement.CAMERASIZEVALUE1B1 - rb.bottomRight.x)), Utils.clamp(dy, Math.max(-rb.topLeft.y, -rb.topRight.y), Math.min(WMElement.CAMERASIZEVALUE1B1 - rb.bottomLeft.y, WMElement.CAMERASIZEVALUE1B1 - rb.bottomRight.y)));
                } else {
                    rb.moveRectBounds(point.x, point.y, this.mTouchType);
                }
                PhotoRectifyView.this.mHasModified = true;
                invalidate();
            }
        }

        private void setTouchType(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            if (PhotoRectifyView.this.mapRect(this.mDetectRatioRect, 0.8f).contains(x, y)) {
                this.mTouchType = TouchPos.RECT_POINT_INNER;
                return;
            }
            this.mTouchType = PhotoRectifyView.this.mapRect(this.mDetectRatioRect).getTouchPosition(x, y);
            this.mLastPosition.set(x, y);
            PhotoRectifyView.this.inverseMapPoint(this.mLastPosition);
        }

        public int[] getDefaultRectifyBounds() {
            this.mDetectRatioRect.set(new RectF(0.25f, 0.25f, 0.75f, 0.75f));
            return getRectBounds();
        }

        public int[] getRectBounds() {
            float[] rb = this.mDetectRatioRect.getRectBounds();
            int[] bounds = new int[8];
            for (int i = 0; i < 4; i++) {
                bounds[i * 2] = (int) (rb[i * 2] * ((float) PhotoRectifyView.this.mImageWidth));
                bounds[(i * 2) + 1] = (int) (rb[(i * 2) + 1] * ((float) PhotoRectifyView.this.mImageHeight));
            }
            return PhotoRectifyView.this.inverseRotate(PhotoRectifyView.this.mRotation, bounds);
        }

        public void setRectBounds(float[] rb) {
            if (rb != null && rb.length == 8) {
                rb = PhotoRectifyView.this.rotate(PhotoRectifyView.this.mRotation, rb);
                float[] bounds = new float[8];
                for (int i = 0; i < 4; i++) {
                    bounds[i * 2] = rb[i * 2] / ((float) PhotoRectifyView.this.mImageWidth);
                    bounds[(i * 2) + 1] = rb[(i * 2) + 1] / ((float) PhotoRectifyView.this.mImageHeight);
                }
                this.mDetectRatioRect.set(bounds);
                this.mHasSetRectBounds = true;
                invalidate();
            }
        }

        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case 0:
                    this.mReferenceX = event.getX();
                    this.mReferenceY = event.getY();
                    setTouchType(event);
                    break;
                case 1:
                case 3:
                    this.mDetectRatioRect.convexProcess(this.mTouchType, PhotoRectifyView.this.mapRect(this.mDetectRatioRect), this.mLastPosition);
                    this.mTouchType = null;
                    break;
                case 2:
                    moveRectBoundsToTarget(event);
                    break;
            }
            if (RectBounds.isConvex(PhotoRectifyView.this.mapRect(this.mDetectRatioRect))) {
                PhotoRectifyView.this.mPaint.setColor(Integer.MAX_VALUE);
            } else {
                PhotoRectifyView.this.mPaint.setColor(2147418112);
            }
            invalidate();
            return true;
        }

        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            if (changed) {
                postInvalidate();
            }
        }

        protected void onDraw(Canvas canvas) {
            if (this.mHasSetRectBounds) {
                RectBounds r = PhotoRectifyView.this.mapRect(this.mDetectRatioRect);
                drawDetectBoundsLine(canvas, r);
                drawDetectBoundsVertex(canvas, r);
            }
        }

        private void drawDetectBoundsVertex(Canvas canvas, RectBounds r) {
            canvas.drawBitmap(this.mCutPoint, r.topLeft.x - (((float) this.mCutPoint.getWidth()) / 2.0f), r.topLeft.y - (((float) this.mCutPoint.getHeight()) / 2.0f), null);
            canvas.drawBitmap(this.mCutPoint, r.topRight.x - (((float) this.mCutPoint.getWidth()) / 2.0f), r.topRight.y - (((float) this.mCutPoint.getHeight()) / 2.0f), null);
            canvas.drawBitmap(this.mCutPoint, r.bottomRight.x - (((float) this.mCutPoint.getWidth()) / 2.0f), r.bottomRight.y - (((float) this.mCutPoint.getHeight()) / 2.0f), null);
            canvas.drawBitmap(this.mCutPoint, r.bottomLeft.x - (((float) this.mCutPoint.getWidth()) / 2.0f), r.bottomLeft.y - (((float) this.mCutPoint.getHeight()) / 2.0f), null);
        }

        private void drawDetectBoundsLine(Canvas canvas, RectBounds rb) {
            Path path = new Path();
            path.moveTo(rb.topLeft.x, rb.topLeft.y);
            path.lineTo(rb.topRight.x, rb.topRight.y);
            path.lineTo(rb.bottomRight.x, rb.bottomRight.y);
            path.lineTo(rb.bottomLeft.x, rb.bottomLeft.y);
            path.lineTo(rb.topLeft.x, rb.topLeft.y);
            canvas.drawPath(path, PhotoRectifyView.this.mPaint);
        }
    }

    public PhotoRectifyView(Activity activity, RelativeLayout root, GLRoot glRoot) {
        this.mRootView = root;
        this.mHandler = new SynchronizedHandler(glRoot);
        this.mDetectBoundsView = new DetectBoundsView(activity);
        this.mDetectBoundsView.setId(R.id.photo_rectify);
        this.mDetectBoundsView.setVisibility(4);
        this.mDetectBoundsView.setOnTouchListener(this.mDetectBoundsView);
        this.mRootView.addView(this.mDetectBoundsView, 1);
        this.mPaint.setColor(Integer.MAX_VALUE);
        this.mPaint.setStrokeWidth(4.0f);
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setAntiAlias(true);
        this.mMatrix = new Matrix();
    }

    public void onLayout(boolean changed, int l, int t, int r, int b) {
        if (this.mImageHeight != -1) {
            initialize();
        }
    }

    public void render(GLCanvas canvas) {
        if (this.mBitmapTexture != null) {
            Bitmap bitmap = this.mBitmapTexture.getBitmap();
            if (bitmap != null) {
                Rect rect = computeDisplayRect(bitmap.getWidth(), bitmap.getHeight());
                this.mBitmapTexture.draw(canvas, rect.left, rect.top, rect.width(), rect.height());
            }
        }
    }

    private void recycle() {
        if (this.mBitmapTexture != null) {
            this.mBitmapTexture.recycle();
        }
    }

    public void updateBackGroundBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            Bitmap textureBitmap = getBitmap();
            if (!(textureBitmap == null || textureBitmap == bitmap)) {
                textureBitmap.recycle();
            }
            recycle();
            this.mBitmapTexture = new BitmapTexture(bitmap);
            invalidate();
        }
    }

    public Bitmap getBitmap() {
        if (this.mBitmapTexture == null) {
            return null;
        }
        return this.mBitmapTexture.getBitmap();
    }

    private void initDisplayWidthAndHeight() {
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        if (viewWidth < viewHeight) {
            this.mDisplayWidth = viewWidth;
            this.mDisplayHeight = viewHeight - (LayoutHelper.getActionBarHeight() * 2);
            return;
        }
        this.mDisplayWidth = viewWidth - LayoutHelper.getNavigationBarHeight();
        this.mDisplayHeight = viewHeight - (LayoutHelper.getActionBarHeight() * 2);
    }

    private Rect computeDisplayRect(int imageWidth, int imageHeight) {
        initDisplayWidthAndHeight();
        float scale = PositionController.getMinimalScale(imageWidth, imageHeight, getDisplayWidth(), getDisplayHeight());
        float renderWidth = ((float) imageWidth) * scale;
        float renderHeight = ((float) imageHeight) * scale;
        int offsetX = Math.round((((float) getWidth()) - renderWidth) / 2.0f);
        int offsetY = Math.round((((float) getHeight()) - renderHeight) / 2.0f);
        return new Rect(offsetX, offsetY, Math.round(renderWidth) + offsetX, Math.round(renderHeight) + offsetY);
    }

    private int getDisplayWidth() {
        return this.mDisplayWidth == -1 ? getWidth() : this.mDisplayWidth;
    }

    private int getDisplayHeight() {
        return this.mDisplayHeight == -1 ? getWidth() : this.mDisplayHeight;
    }

    public void initialize() {
        this.mCenterX = ((float) this.mImageWidth) / 2.0f;
        this.mCenterY = ((float) this.mImageHeight) / 2.0f;
        initDisplayWidthAndHeight();
        this.mScale = PositionController.getMinimalScale(this.mImageWidth, this.mImageHeight, getDisplayWidth(), getDisplayHeight());
    }

    public void initImageData(int ImageWidth, int ImageHeight, int rotation) {
        if (this.mImageWidth == -1 || this.mImageHeight == -1) {
            if (((rotation / 90) & 1) != 0) {
                this.mImageWidth = ImageHeight;
                this.mImageHeight = ImageWidth;
            } else {
                this.mImageWidth = ImageWidth;
                this.mImageHeight = ImageHeight;
            }
            this.mRotation = rotation;
            initialize();
        }
    }

    public void destroy() {
        Bitmap textureBitmap = getBitmap();
        if (textureBitmap != null) {
            textureBitmap.recycle();
        }
        recycle();
        this.mRootView.removeView(this.mDetectBoundsView);
    }

    public void setRectifyBounds(float[] bounds) {
        this.mDetectBoundsView.setRectBounds(bounds);
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                PhotoRectifyView.this.mDetectBoundsView.setVisibility(0);
                PhotoRectifyView.this.invalidate();
            }
        }, 200);
    }

    public int[] getRectifyBounds() {
        return this.mDetectBoundsView.getRectBounds();
    }

    public int[] getDefaultRectifyBounds() {
        return this.mDetectBoundsView.getDefaultRectifyBounds();
    }

    private float[] rotate(int rotation, float[] srcRb) {
        if (rotation % 360 == 0) {
            return srcRb;
        }
        this.mMatrix.reset();
        if (rotation % 180 == 0) {
            this.mMatrix.setRotate((float) rotation, this.mCenterX, this.mCenterY);
        } else {
            this.mMatrix.preTranslate(-this.mCenterY, -this.mCenterX);
            this.mMatrix.postRotate((float) rotation);
            this.mMatrix.postTranslate(this.mCenterX, this.mCenterY);
        }
        float[] targetRb = new float[srcRb.length];
        this.mMatrix.mapPoints(targetRb, srcRb);
        return targetRb;
    }

    private int[] inverseRotate(int rotation, int[] srcRb) {
        if (rotation % 360 == 0) {
            return srcRb;
        }
        int i;
        Matrix invertMatrix = new Matrix();
        this.mMatrix.invert(invertMatrix);
        float[] targetRb = new float[srcRb.length];
        float[] srcRbF = new float[srcRb.length];
        for (i = 0; i < srcRb.length; i++) {
            srcRbF[i] = (float) srcRb[i];
        }
        invertMatrix.mapPoints(targetRb, srcRbF);
        int[] targetRbI = new int[targetRb.length];
        for (i = 0; i < targetRb.length; i++) {
            targetRbI[i] = (int) targetRb[i];
        }
        return targetRbI;
    }

    public boolean hasModifiedRect() {
        return this.mHasModified;
    }

    public void inverseMapPoint(PointF point) {
        float s = this.mScale;
        point.x = Utils.clamp((((point.x - (((float) getWidth()) * 0.5f)) / s) + this.mCenterX) / ((float) this.mImageWidth), 0.0f, (float) WMElement.CAMERASIZEVALUE1B1);
        point.y = Utils.clamp((((point.y - (((float) getHeight()) * 0.5f)) / s) + this.mCenterY) / ((float) this.mImageHeight), 0.0f, (float) WMElement.CAMERASIZEVALUE1B1);
    }

    public RectBounds mapRect(RectBounds input) {
        return mapRect(input, WMElement.CAMERASIZEVALUE1B1);
    }

    public RectBounds mapRect(RectBounds input, float ratio) {
        RectBounds output = new RectBounds();
        float offsetX = ((float) getWidth()) * 0.5f;
        float offsetY = ((float) getHeight()) * 0.5f;
        float x = this.mCenterX;
        float y = this.mCenterY;
        float s = this.mScale;
        float deltaX = (((((float) this.mImageWidth) * ((((input.topLeft.x + input.topRight.x) + input.bottomRight.x) + input.bottomLeft.x) / 4.0f)) - x) * s) * (WMElement.CAMERASIZEVALUE1B1 - ratio);
        float deltaY = (((((float) this.mImageHeight) * ((((input.topLeft.y + input.topRight.y) + input.bottomRight.y) + input.bottomLeft.y) / 4.0f)) - y) * s) * (WMElement.CAMERASIZEVALUE1B1 - ratio);
        output.set((((((input.topLeft.x * ((float) this.mImageWidth)) - x) * s) * ratio) + offsetX) + deltaX, (((((input.topLeft.y * ((float) this.mImageHeight)) - y) * s) * ratio) + offsetY) + deltaY, (((((input.topRight.x * ((float) this.mImageWidth)) - x) * s) * ratio) + offsetX) + deltaX, (((((input.topRight.y * ((float) this.mImageHeight)) - y) * s) * ratio) + offsetY) + deltaY, (((((input.bottomRight.x * ((float) this.mImageWidth)) - x) * s) * ratio) + offsetX) + deltaX, (((((input.bottomRight.y * ((float) this.mImageHeight)) - y) * s) * ratio) + offsetY) + deltaY, (((((input.bottomLeft.x * ((float) this.mImageWidth)) - x) * s) * ratio) + offsetX) + deltaX, (((((input.bottomLeft.y * ((float) this.mImageHeight)) - y) * s) * ratio) + offsetY) + deltaY);
        return output;
    }
}

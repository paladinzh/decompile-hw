package com.huawei.gallery.ui;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.os.Message;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.android.gallery3d.R;
import com.android.gallery3d.anim.Animation;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.ui.AnimationTime;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLPaint;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.GestureRecognizer;
import com.android.gallery3d.ui.GestureRecognizer.Listener;
import com.android.gallery3d.ui.ResourceTexture;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.ui.TileImageView;
import com.android.gallery3d.ui.TileImageView.Model;
import com.android.gallery3d.util.GalleryUtils;
import com.autonavi.amap.mapcore.MapConfig;
import com.huawei.gallery.util.MyPrinter;
import com.huawei.watermark.manager.parse.WMElement;
import com.huawei.watermark.ui.WMComponent;
import java.util.ArrayList;
import javax.microedition.khronos.opengles.GL11;

public class SimpleCropView extends BaseCropView {
    private static final MyPrinter PRT = new MyPrinter("SimpleCropView");
    private GalleryContext mActivity;
    private AnimationController mAnimation = new AnimationController();
    private FaceHighlightView mFaceHighlightView;
    private GLPaint mFacePaint = new GLPaint();
    private final MyGestureListener mGestureListener;
    private final GestureRecognizer mGestureRecognizer;
    private HighlightRectangle mHighlightRectangle;
    private float mImageScale = WMElement.CAMERASIZEVALUE1B1;
    private TileImageView mImageView;
    private SynchronizedHandler mMainHandler;
    private GLPaint mPaint = new GLPaint();
    boolean mScrollable;
    private boolean mUseFaceRect;
    int mWallpaperH;
    int mWallpaperW;

    private class AnimationController extends Animation {
        private RectF mBound = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
        private float mCurrentScale;
        private float mCurrentX;
        private float mCurrentY;
        private float mFocusX;
        private float mFocusY;
        private float mMinScale;
        private float mStartScale;
        private float mStartX;
        private float mStartY;
        private float mTargetScale;
        private float mTargetX;
        private float mTargetY;

        public AnimationController() {
            setDuration(1250);
            setInterpolator(new DecelerateInterpolator(4.0f));
        }

        public void initialize() {
            float f = ((float) SimpleCropView.this.mImageWidth) / 2.0f;
            this.mCurrentX = f;
            this.mTargetX = f;
            f = ((float) SimpleCropView.this.mImageHeight) / 2.0f;
            this.mCurrentY = f;
            this.mTargetY = f;
            float faceMinSize = ((float) SimpleCropView.this.getWidth()) / ((float) Math.min(SimpleCropView.this.mImageWidth, SimpleCropView.this.mImageHeight));
            float maxSize = Math.max(((float) SimpleCropView.this.getWidth()) / ((float) SimpleCropView.this.mImageWidth), ((float) SimpleCropView.this.getHeight()) / ((float) SimpleCropView.this.mImageHeight));
            if (SimpleCropView.this.mUseFaceRect) {
                f = faceMinSize;
            } else {
                f = maxSize;
            }
            this.mCurrentScale = f;
            if (SimpleCropView.this.mUseFaceRect) {
                maxSize = (faceMinSize * 2.0f) * 0.38f;
            } else if (SimpleCropView.this.mScrollable) {
                maxSize = Math.max(((float) SimpleCropView.this.mHighlightRectangle.getRectWidth()) / ((float) SimpleCropView.this.mImageWidth), ((float) SimpleCropView.this.mHighlightRectangle.getRectHeight()) / ((float) SimpleCropView.this.mImageHeight));
            }
            this.mMinScale = maxSize;
            if (!SimpleCropView.this.mUseFaceRect) {
                this.mCurrentScale = this.mMinScale;
            }
        }

        public void startParkingAnimation(float targetX, float targetY, float targetScale) {
            if (!Utils.equal(this.mCurrentX, targetX) || !Utils.equal(this.mCurrentY, targetY)) {
                this.mStartX = this.mCurrentX;
                this.mStartY = this.mCurrentY;
                this.mStartScale = this.mCurrentScale;
                this.mTargetX = targetX;
                this.mTargetY = targetY;
                this.mTargetScale = targetScale;
                start();
            }
        }

        protected void onCalculate(float progress) {
            this.mCurrentX = this.mStartX + ((this.mTargetX - this.mStartX) * progress);
            this.mCurrentY = this.mStartY + ((this.mTargetY - this.mStartY) * progress);
            this.mCurrentScale = this.mStartScale + ((this.mTargetScale - this.mStartScale) * progress);
            if (Utils.equal(this.mCurrentX, this.mTargetX) && Utils.equal(this.mCurrentY, this.mTargetY) && Utils.equal(this.mCurrentScale, this.mTargetScale)) {
                forceStop();
            }
            dumpAnimation("onCalculate");
        }

        public float getCenterX() {
            return this.mCurrentX;
        }

        public float getCenterY() {
            return this.mCurrentY;
        }

        public float getScale() {
            return this.mCurrentScale;
        }

        public void beginScale(float focusX, float focusY) {
            this.mFocusX = this.mCurrentX + ((focusX - (((float) SimpleCropView.this.getWidth()) / 2.0f)) / this.mCurrentScale);
            this.mFocusY = this.mCurrentY + ((focusY - (((float) SimpleCropView.this.getHeight()) / 2.0f)) / this.mCurrentScale);
        }

        public int scaleBy(float focusX, float focusY, float scale) {
            scale *= this.mCurrentScale;
            float currentViewX = this.mFocusX - ((focusX - (((float) SimpleCropView.this.getWidth()) / 2.0f)) / scale);
            float currentViewY = this.mFocusY - ((focusY - (((float) SimpleCropView.this.getHeight()) / 2.0f)) / scale);
            if (scale < this.mMinScale) {
                scale = this.mMinScale;
            }
            calculateStableBound(scale);
            currentViewX = Utils.clamp(currentViewX, this.mBound.left, this.mBound.right);
            currentViewY = Utils.clamp(currentViewY, this.mBound.top, this.mBound.bottom);
            this.mTargetX = currentViewX;
            this.mCurrentX = currentViewX;
            this.mStartX = currentViewX;
            this.mTargetY = currentViewY;
            this.mCurrentY = currentViewY;
            this.mStartY = currentViewY;
            this.mCurrentScale = scale;
            this.mTargetScale = scale;
            this.mStartScale = scale;
            SimpleCropView.this.invalidate();
            return 0;
        }

        public void endScale() {
        }

        public void scrollBy(int dxi, int dyi) {
            float currentX = this.mCurrentX + (((float) dxi) / this.mCurrentScale);
            float currentY = this.mCurrentY + (((float) dyi) / this.mCurrentScale);
            calculateStableBound(this.mCurrentScale);
            currentX = Utils.clamp(currentX, this.mBound.left, this.mBound.right);
            currentY = Utils.clamp(currentY, this.mBound.top, this.mBound.bottom);
            this.mTargetX = currentX;
            this.mCurrentX = currentX;
            this.mStartX = currentX;
            this.mTargetY = currentY;
            this.mCurrentY = currentY;
            this.mStartY = currentY;
            float f = this.mCurrentScale;
            this.mTargetScale = f;
            this.mStartScale = f;
            SimpleCropView.this.invalidate();
        }

        private void calculateStableBound(float scale) {
            int width = SimpleCropView.this.getWidth();
            int height = SimpleCropView.this.getHeight();
            if (SimpleCropView.this.mScrollable) {
                width = SimpleCropView.this.mHighlightRectangle.getRectWidth();
                height = SimpleCropView.this.mHighlightRectangle.getRectHeight();
            }
            if (SimpleCropView.this.mUseFaceRect) {
                width = SimpleCropView.this.mFaceHighlightView.mFaceRect.width();
                height = SimpleCropView.this.mFaceHighlightView.mFaceRect.height();
            }
            float vw = ((float) width) / scale;
            float vh = ((float) height) / scale;
            this.mBound.left = vw / 2.0f;
            this.mBound.right = ((float) SimpleCropView.this.mImageWidth) - (vw / 2.0f);
            this.mBound.top = vh / 2.0f;
            this.mBound.bottom = ((float) SimpleCropView.this.mImageHeight) - (vh / 2.0f);
            if (((float) SimpleCropView.this.mImageWidth) <= vw) {
                RectF rectF = this.mBound;
                float f = ((float) SimpleCropView.this.mImageWidth) / 2.0f;
                this.mBound.right = f;
                rectF.left = f;
            }
            if (((float) SimpleCropView.this.mImageHeight) <= vh) {
                rectF = this.mBound;
                f = ((float) SimpleCropView.this.mImageHeight) / 2.0f;
                this.mBound.bottom = f;
                rectF.top = f;
            }
        }

        public Point inverseMapPoint(PointF in, Point out) {
            float s = this.mCurrentScale;
            SimpleCropView.PRT.d(" input point : x->" + in.x + ", y->" + in.y);
            int width = (int) (((double) (((float) SimpleCropView.this.mImageWidth) * s)) + 0.5d);
            int height = (int) (((double) (((float) SimpleCropView.this.mImageHeight) * s)) + 0.5d);
            int x = (int) (((double) (in.x * ((float) width))) + 0.5d);
            int y = (int) (((double) (in.y * ((float) height))) + 0.5d);
            int left = ((width - SimpleCropView.this.getWidth()) + 1) / 2;
            int top = ((height - SimpleCropView.this.getHeight()) + 1) / 2;
            SimpleCropView.PRT.d(String.format("x->%s, y->%s,  left->%s,  top->%s,  width->%s,  height->%s, ", new Object[]{Integer.valueOf(x), Integer.valueOf(y), Integer.valueOf(left), Integer.valueOf(top), Integer.valueOf(SimpleCropView.this.getWidth()), Integer.valueOf(SimpleCropView.this.getHeight())}));
            if (x < left || x > SimpleCropView.this.getWidth() + left || y < top || y > SimpleCropView.this.getHeight() + top) {
                out.x = -1;
                out.y = -1;
            } else {
                out.x = x - left;
                out.y = y - top;
            }
            return out;
        }

        public FaceInfo mapFaceRect(FaceInfo face) {
            Point c = inverseMapPoint(face.mCenter, new Point());
            if (c.x < 0) {
                return null;
            }
            float s = this.mCurrentScale;
            float r = face.mRadius;
            int side = Math.round(r <= 0.0f ? ((float) SimpleCropView.this.getWidth()) * 0.38f : (((float) SimpleCropView.this.getImageWidth()) * r) * s);
            face.rect = new Rect(c.x - side, c.y - side, c.x + side, c.y + side);
            face.rect.intersect(new Rect(0, 0, SimpleCropView.this.getWidth(), SimpleCropView.this.getHeight()));
            SimpleCropView.PRT.d(String.format("face r->%s, rect->%s, scale->%s, radius->%s", new Object[]{Float.valueOf(r), face.rect, Float.valueOf(s), Integer.valueOf(side)}));
            return face;
        }

        public Rect mapRect() {
            int h;
            int w;
            if (SimpleCropView.this.mUseFaceRect) {
                SimpleCropView.PRT.d("mUseFaceRect");
                Rect rect = SimpleCropView.this.mFaceHighlightView.getFaceRect();
                int side = scaleToOrigin(Math.max(rect.width(), rect.height()));
                h = side;
                w = side;
            } else {
                w = SimpleCropView.this.mScrollable ? scaleToOrigin(SimpleCropView.this.mHighlightRectangle.getRectWidth()) : widthOfView();
                h = SimpleCropView.this.mScrollable ? scaleToOrigin(SimpleCropView.this.mHighlightRectangle.getRectHeight()) : heightOfView();
            }
            int halfW = w / 2;
            int halfH = h / 2;
            SimpleCropView.PRT.d("[mapRect] mImageWidth->" + SimpleCropView.this.mImageWidth + ", mImageHeight->" + SimpleCropView.this.mImageHeight);
            SimpleCropView.PRT.d("[mapRect] halfW->" + halfW + ", halfH->" + halfH);
            int centerX = (int) this.mCurrentX;
            int centerY = (int) this.mCurrentY;
            return new Rect(centerX - halfW, centerY - halfH, centerX + halfW, centerY + halfH);
        }

        private int scaleToOrigin(int side) {
            return (int) (((double) (((float) side) / this.mCurrentScale)) + 0.5d);
        }

        private int heightOfView() {
            return scaleToOrigin(SimpleCropView.this.getHeight());
        }

        private int widthOfView() {
            return scaleToOrigin(SimpleCropView.this.getWidth());
        }

        private void dumpAnimation(String method) {
            SimpleCropView.PRT.d(String.format("[%s] mStartX = %s , mStartY = %s, mCurrentX = %s, mCurrentY = %s, mTargetX = %s, mTargetY = %s", new Object[]{method, Float.valueOf(this.mStartX), Float.valueOf(this.mStartY), Float.valueOf(this.mCurrentX), Float.valueOf(this.mCurrentY), Float.valueOf(this.mTargetX), Float.valueOf(this.mTargetY)}));
        }
    }

    private class DetectFaceTask extends Thread {
        private final Bitmap mFaceBitmap;
        private int mFaceCount;
        private final Face[] mFaces = new Face[3];

        public DetectFaceTask(Bitmap bitmap) {
            this.mFaceBitmap = bitmap;
            setName("face-detect");
        }

        public void run() {
            Bitmap bitmap = this.mFaceBitmap;
            this.mFaceCount = new FaceDetector(bitmap.getWidth(), bitmap.getHeight(), 3).findFaces(bitmap, this.mFaces);
            SimpleCropView.this.mMainHandler.sendMessage(SimpleCropView.this.mMainHandler.obtainMessage(1, this));
        }

        private FaceInfo getFaceInfo(Face face) {
            PointF point = new PointF();
            face.getMidPoint(point);
            int width = this.mFaceBitmap.getWidth();
            int height = this.mFaceBitmap.getHeight();
            float r = face.eyesDistance() * 2.0f;
            RectF rect = new RectF(point.x - r, point.y - r, point.x + r, point.y + r);
            rect.intersect(0.0f, 0.0f, (float) width, (float) height);
            point.x = rect.centerX();
            point.y = rect.centerY();
            r = Math.min(rect.width(), rect.height()) / 2.0f;
            point.x /= (float) width;
            point.y /= (float) height;
            return new FaceInfo(point, r / ((float) width));
        }

        public void updateFaces() {
            if (this.mFaceCount > 1) {
                int n = this.mFaceCount;
                for (int i = 0; i < n; i++) {
                    SimpleCropView.this.mFaceHighlightView.addFace(getFaceInfo(this.mFaces[i]));
                }
            } else if (this.mFaceCount == 1) {
                SimpleCropView.this.mFaceHighlightView.addFace(getFaceInfo(this.mFaces[0]));
                SimpleCropView.this.mFaceHighlightView.setPressedFace(0);
            } else {
                SimpleCropView.this.mFaceHighlightView.addFace(FaceInfo.DEFAULT);
                SimpleCropView.this.mFaceHighlightView.setPressedFace(0);
            }
        }
    }

    private class FaceHighlightView extends GLView {
        private ResourceTexture mFaceMask;
        private Rect mFaceRect = new Rect();
        private ArrayList<FaceInfo> mFaces = new ArrayList();
        private int mPressedFaceIndex = -1;

        public FaceHighlightView() {
            this.mFaceMask = new ResourceTexture(SimpleCropView.this.mActivity.getAndroidContext(), R.drawable.cut_contacts_mask);
        }

        public void addFace(FaceInfo face) {
            if (SimpleCropView.this.mAnimation.mapFaceRect(face) == null) {
                SimpleCropView.PRT.d("faceinfo rect is out of bound.");
                if (this.mFaces.isEmpty()) {
                    FaceInfo f = SimpleCropView.this.mAnimation.mapFaceRect(FaceInfo.DEFAULT);
                    SimpleCropView.PRT.d(" use default face rect " + f.rect);
                    this.mFaces.add(f);
                }
                return;
            }
            this.mFaces.add(face);
            invalidate();
        }

        public Rect getFaceRect() {
            return this.mFaceRect;
        }

        private void renderFace(GLCanvas canvas, RectF r, boolean pressed) {
            GL11 gl = canvas.getGLInstance();
            if (pressed) {
                gl.glEnable(2960);
                gl.glClear(1024);
                gl.glStencilOp(7680, 7680, 7681);
                gl.glStencilFunc(519, 1, 1);
            }
            if (GalleryUtils.isTabletProduct(getGLRoot().getContext())) {
                int targetRadius = Math.round(((float) Math.min(getWidth(), getHeight())) * 0.38f);
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                r.set((float) (centerX - targetRadius), (float) (centerY - targetRadius), (float) (centerX + targetRadius), (float) (centerY + targetRadius));
            }
            canvas.fillRect(r.left, r.top, r.width(), r.height(), 0);
            if (pressed) {
                this.mFaceMask.draw(canvas, (int) r.left, (int) r.top, Math.round(r.width() + 0.5f), Math.round(r.height() + 0.5f));
            } else {
                canvas.drawRect(r.left, r.top, r.width(), r.height(), SimpleCropView.this.mFacePaint);
            }
            if (pressed) {
                gl.glStencilOp(7680, 7680, 7680);
            }
        }

        protected void renderBackground(GLCanvas canvas) {
            ArrayList<FaceInfo> faces = this.mFaces;
            int i = 0;
            int n = faces.size();
            while (i < n) {
                FaceInfo face = (FaceInfo) faces.get(i);
                if (face.show) {
                    renderFace(canvas, new RectF(face.rect), i == this.mPressedFaceIndex);
                }
                i++;
            }
            GL11 gl = canvas.getGLInstance();
            if (this.mPressedFaceIndex != -1) {
                gl.glStencilFunc(517, 1, 1);
                canvas.fillRect(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), -1275068416);
                gl.glDisable(2960);
            }
        }

        private void setPressedFace(int index) {
            if (index != -1 && this.mPressedFaceIndex != index) {
                ArrayList<FaceInfo> faces = this.mFaces;
                if (((FaceInfo) faces.get(index)).show) {
                    int i = 0;
                    int n = faces.size();
                    while (i < n) {
                        ((FaceInfo) faces.get(i)).show = index == i;
                        i++;
                    }
                    this.mPressedFaceIndex = index;
                    FaceInfo face = (FaceInfo) faces.get(this.mPressedFaceIndex);
                    Rect rect = face.rect;
                    int targetRadius = Math.round(((float) getWidth()) * 0.38f);
                    float scale = (((float) targetRadius) * 2.0f) / ((float) Math.min(rect.width(), rect.height()));
                    int centerX = getWidth() / 2;
                    int centerY = getHeight() / 2;
                    this.mFaceRect.set(centerX - targetRadius, centerY - targetRadius, centerX + targetRadius, centerY + targetRadius);
                    PointF center = face.mCenter;
                    rect.set(this.mFaceRect);
                    SimpleCropView.this.mAnimation.startParkingAnimation(((float) SimpleCropView.this.mImageWidth) * center.x, ((float) SimpleCropView.this.mImageHeight) * center.y, SimpleCropView.this.mAnimation.mCurrentScale * scale);
                    invalidate();
                }
            }
        }

        private int getFaceIndexByPosition(float x, float y) {
            ArrayList<FaceInfo> faces = this.mFaces;
            RectF r = new RectF();
            int n = faces.size();
            for (int i = 0; i < n; i++) {
                r.set(((FaceInfo) faces.get(i)).rect);
                if (r.contains(x, y)) {
                    return i;
                }
            }
            return -1;
        }

        protected boolean onTouch(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            switch (event.getAction()) {
                case 0:
                    setPressedFace(getFaceIndexByPosition(x, y));
                    break;
            }
            return false;
        }
    }

    private static class FaceInfo {
        static final FaceInfo DEFAULT = new FaceInfo(new PointF(0.5f, 0.5f), GroundOverlayOptions.NO_DIMENSION);
        final PointF mCenter;
        final float mRadius;
        Rect rect;
        boolean show = true;

        FaceInfo(PointF center, float radius) {
            this.mCenter = center;
            this.mRadius = radius;
        }
    }

    private class HighlightRectangle extends GLView {
        private int mRectHeight;
        private int mRectWidth;

        private HighlightRectangle() {
        }

        public int getRectWidth() {
            return this.mRectWidth;
        }

        public int getRectHeight() {
            return this.mRectHeight;
        }

        protected void onLayout(boolean changeSize, int left, int top, int right, int bottom) {
            super.onLayout(changeSize, left, top, right, bottom);
            int width = getWidth();
            int height = getHeight();
            float scaleX = ((float) width) / ((float) SimpleCropView.this.mWallpaperW);
            float scaleY = ((float) height) / ((float) SimpleCropView.this.mWallpaperH);
            if (scaleY > scaleX) {
                this.mRectWidth = width;
                this.mRectHeight = (int) (((double) (((float) SimpleCropView.this.mWallpaperH) * scaleX)) + 0.5d);
                return;
            }
            this.mRectWidth = (int) (((double) (((float) SimpleCropView.this.mWallpaperW) * scaleY)) + 0.5d);
            this.mRectHeight = height;
        }

        protected void renderBackground(GLCanvas canvas) {
            GL11 gl = canvas.getGLInstance();
            gl.glLineWidth(MapConfig.MIN_ZOOM);
            gl.glEnable(2848);
            gl.glEnable(2960);
            gl.glClear(1024);
            gl.glStencilOp(7680, 7680, 7681);
            gl.glStencilFunc(519, 1, 1);
            canvas.fillRect((float) ((getWidth() - this.mRectWidth) / 2), (float) ((getHeight() - this.mRectHeight) / 2), (float) this.mRectWidth, (float) this.mRectHeight, 0);
            gl.glStencilFunc(517, 1, 1);
            gl.glStencilOp(7680, 7680, 7681);
            canvas.fillRect(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), -1275068416);
            gl.glStencilFunc(517, 1, 1);
            gl.glStencilOp(7680, 7680, 7680);
            canvas.fillRect(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), 0);
            gl.glDisable(2960);
        }
    }

    private class MyGestureListener implements Listener {
        private MyGestureListener() {
        }

        public boolean onSingleTapUp(float x, float y) {
            return false;
        }

        public boolean onDoubleTap(float x, float y) {
            return false;
        }

        public boolean onScroll(float dx, float dy, float totalX, float totalY) {
            SimpleCropView.this.mAnimation.scrollBy((int) (dx + 0.5f), (int) (dy + 0.5f));
            return true;
        }

        public boolean onFling(float velocityX, float velocityY) {
            return true;
        }

        public boolean onScaleBegin(float focusX, float focusY) {
            SimpleCropView.this.mAnimation.beginScale(focusX, focusY);
            return true;
        }

        public boolean onScale(float focusX, float focusY, float scale) {
            SimpleCropView.this.mAnimation.scaleBy(focusX, focusY, scale);
            return true;
        }

        public void onScaleEnd() {
            SimpleCropView.this.mAnimation.endScale();
        }

        public void onLongPress(MotionEvent e) {
        }

        public void onDown(float x, float y) {
            SimpleCropView.PRT.d("onDown");
        }

        public void onUp() {
            SimpleCropView.PRT.d("onUp");
        }
    }

    public SimpleCropView(GalleryContext activity) {
        this.mActivity = activity;
        this.mImageView = new TileImageView(activity);
        this.mHighlightRectangle = new HighlightRectangle();
        this.mFaceHighlightView = new FaceHighlightView();
        addComponent(this.mImageView);
        addComponent(this.mHighlightRectangle);
        addComponent(this.mFaceHighlightView);
        this.mPaint.setColor(this.mActivity.getAndroidContext().getResources().getColor(R.color.crop_bolder_selected));
        this.mPaint.setLineWidth(MapConfig.MIN_ZOOM);
        this.mFacePaint.setColor(-16777216);
        this.mFacePaint.setLineWidth(MapConfig.MIN_ZOOM);
        this.mGestureListener = new MyGestureListener();
        this.mGestureRecognizer = new GestureRecognizer(activity.getActivityContext(), this.mGestureListener);
        this.mMainHandler = new SynchronizedHandler(activity.getGLRoot()) {
            public void handleMessage(Message message) {
                boolean z = true;
                if (message.what != 1) {
                    z = false;
                }
                Utils.assertTrue(z);
                ((DetectFaceTask) message.obj).updateFaces();
            }
        };
    }

    public void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = r - l;
        int height = b - t;
        this.mImageView.layout(0, 0, width, height);
        this.mHighlightRectangle.layout(0, 0, width, height);
        this.mFaceHighlightView.layout(0, 0, width, height);
        if (this.mImageHeight != -1) {
            this.mAnimation.initialize();
        }
    }

    private boolean setImageViewPosition(float centerX, float centerY, float scale) {
        float inverseX = ((float) this.mImageWidth) - centerX;
        float inverseY = ((float) this.mImageHeight) - centerY;
        TileImageView t = this.mImageView;
        int rotation = this.mImageRotation;
        switch (rotation) {
            case 0:
                return t.setPosition(centerX, centerY, scale, 0);
            case WMComponent.ORI_90 /*90*/:
                return t.setPosition(centerY, inverseX, scale, 90);
            case 180:
                return t.setPosition(inverseX, inverseY, scale, 180);
            case 270:
                return t.setPosition(inverseY, centerX, scale, 270);
            default:
                throw new IllegalArgumentException(String.valueOf(rotation));
        }
    }

    public void render(GLCanvas canvas) {
        AnimationController a = this.mAnimation;
        if (a.calculate(AnimationTime.get())) {
            PRT.d("invalidate with animation");
            invalidate();
        }
        setImageViewPosition(a.getCenterX(), a.getCenterY(), a.getScale());
        super.render(canvas);
    }

    public void renderBackground(GLCanvas canvas) {
        super.renderBackground(canvas);
    }

    public Rect getCropRectangle() {
        return this.mAnimation.mapRect();
    }

    protected boolean onTouch(MotionEvent event) {
        this.mGestureRecognizer.onTouchEvent(event);
        return true;
    }

    protected void onDataModelChanged(Model dataModel) {
        super.onDataModelChanged(dataModel);
        this.mImageView.setModel(dataModel);
        this.mAnimation.initialize();
    }

    public void setWallpaperSize(int w, int h) {
        this.mWallpaperW = w;
        this.mWallpaperH = h;
    }

    public void setScrollableWallper(boolean scrollable) {
        this.mScrollable = scrollable;
        this.mAnimation.initialize();
        invalidate();
        this.mHighlightRectangle.setVisibility(this.mScrollable ? 0 : 1);
    }

    public void resume() {
        this.mImageView.prepareTextures();
    }

    public void pause() {
        this.mImageView.freeTextures();
    }

    public void detectFaces(Bitmap bitmap) {
        Bitmap faceBitmap;
        this.mUseFaceRect = true;
        int rotation = this.mImageRotation;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scale = (float) Math.sqrt(120000.0d / ((double) (width * height)));
        int w;
        int h;
        Canvas canvas;
        if (((rotation / 90) & 1) == 0) {
            w = Math.round(((float) width) * scale) & -2;
            h = Math.round(((float) height) * scale);
            faceBitmap = Bitmap.createBitmap(w, h, Config.RGB_565);
            canvas = new Canvas(faceBitmap);
            canvas.rotate((float) rotation, ((float) w) / 2.0f, ((float) h) / 2.0f);
            canvas.scale(((float) w) / ((float) width), ((float) h) / ((float) height));
            canvas.drawBitmap(bitmap, 0.0f, 0.0f, new Paint(2));
        } else {
            w = Math.round(((float) height) * scale) & -2;
            h = Math.round(((float) width) * scale);
            faceBitmap = Bitmap.createBitmap(w, h, Config.RGB_565);
            canvas = new Canvas(faceBitmap);
            canvas.translate(((float) w) / 2.0f, ((float) h) / 2.0f);
            canvas.rotate((float) rotation);
            canvas.translate(((float) (-h)) / 2.0f, ((float) (-w)) / 2.0f);
            canvas.scale(((float) w) / ((float) height), ((float) h) / ((float) width));
            canvas.drawBitmap(bitmap, 0.0f, 0.0f, new Paint(2));
        }
        new DetectFaceTask(faceBitmap).start();
    }

    public void setGLRoot(GLRoot root) {
        this.mMainHandler.setGLRoot(root);
    }
}

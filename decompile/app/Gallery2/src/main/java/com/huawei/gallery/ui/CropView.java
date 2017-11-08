package com.huawei.gallery.ui;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.os.Message;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import com.amap.api.maps.model.BitmapDescriptorFactory;
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
import com.android.gallery3d.ui.ResourceTexture;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.ui.TileImageView;
import com.android.gallery3d.ui.TileImageView.Model;
import com.android.gallery3d.util.GalleryUtils;
import com.autonavi.amap.mapcore.MapConfig;
import com.huawei.watermark.manager.parse.WMElement;
import com.huawei.watermark.ui.WMComponent;
import java.util.ArrayList;
import javax.microedition.khronos.opengles.GL11;

public class CropView extends BaseCropView {
    private static final float MIN_SELECTION_LENGTH = GalleryUtils.dpToPixel(45.0f);
    private GalleryContext mActivity;
    private AnimationController mAnimation = new AnimationController();
    private float mAspectRatio = GroundOverlayOptions.NO_DIMENSION;
    private FaceHighlightView mFaceDetectionView;
    private GLPaint mFacePaint = new GLPaint();
    private boolean mFullScale = false;
    private HighlightRectangle mHighlightRectangle;
    private TileImageView mImageView;
    private SynchronizedHandler mMainHandler;
    private GLPaint mPaint = new GLPaint();
    private float mSpotlightRatioX = 0.0f;
    private float mSpotlightRatioY = 0.0f;

    private class AnimationController extends Animation {
        private float mCurrentScale;
        private float mCurrentX;
        private float mCurrentY;
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
            this.mCurrentX = ((float) CropView.this.mImageWidth) / 2.0f;
            this.mCurrentY = ((float) CropView.this.mImageHeight) / 2.0f;
            this.mCurrentScale = Math.min(((float) CropView.this.getWidth()) / ((float) CropView.this.mImageWidth), ((float) CropView.this.getHeight()) / ((float) CropView.this.mImageHeight));
        }

        public void startParkingAnimation(RectF highlight) {
        }

        public void parkNow(RectF highlight) {
            calculateTarget(highlight);
            forceStop();
            float f = this.mTargetX;
            this.mCurrentX = f;
            this.mStartX = f;
            f = this.mTargetY;
            this.mCurrentY = f;
            this.mStartY = f;
            f = this.mTargetScale;
            this.mCurrentScale = f;
            this.mStartScale = f;
        }

        public void inverseMapPoint(PointF point) {
            float s = this.mCurrentScale;
            point.x = Utils.clamp((((point.x - (((float) CropView.this.getWidth()) * 0.5f)) / s) + this.mCurrentX) / ((float) CropView.this.mImageWidth), 0.0f, (float) WMElement.CAMERASIZEVALUE1B1);
            point.y = Utils.clamp((((point.y - (((float) CropView.this.getHeight()) * 0.5f)) / s) + this.mCurrentY) / ((float) CropView.this.mImageHeight), 0.0f, (float) WMElement.CAMERASIZEVALUE1B1);
        }

        public RectF mapRect(RectF input, RectF output) {
            float offsetX = ((float) CropView.this.getWidth()) * 0.5f;
            float offsetY = ((float) CropView.this.getHeight()) * 0.5f;
            float x = this.mCurrentX;
            float y = this.mCurrentY;
            float s = this.mCurrentScale;
            output.set((((input.left * ((float) CropView.this.mImageWidth)) - x) * s) + offsetX, (((input.top * ((float) CropView.this.mImageHeight)) - y) * s) + offsetY, (((input.right * ((float) CropView.this.mImageWidth)) - x) * s) + offsetX, (((input.bottom * ((float) CropView.this.mImageHeight)) - y) * s) + offsetY);
            return output;
        }

        protected void onCalculate(float progress) {
            this.mCurrentX = this.mStartX + ((this.mTargetX - this.mStartX) * progress);
            this.mCurrentY = this.mStartY + ((this.mTargetY - this.mStartY) * progress);
            this.mCurrentScale = this.mStartScale + ((this.mTargetScale - this.mStartScale) * progress);
            if (Utils.equal(this.mCurrentX, this.mTargetX) && Utils.equal(this.mCurrentY, this.mTargetY) && Utils.equal(this.mCurrentScale, this.mTargetScale)) {
                forceStop();
            }
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

        private void calculateTarget(RectF highlight) {
            float width = (float) CropView.this.getWidth();
            float height = (float) CropView.this.getHeight();
            if (CropView.this.mImageWidth != -1) {
                float centerX;
                float centerY;
                float scale = Utils.clamp(Math.min(width / (highlight.width() * ((float) CropView.this.mImageWidth)), height / (highlight.height() * ((float) CropView.this.mImageHeight))) * 0.6f, Math.min(width / ((float) CropView.this.mImageWidth), height / ((float) CropView.this.mImageHeight)), 2.0f);
                if (((float) Math.round(((float) CropView.this.mImageWidth) * scale)) > width) {
                    float limitX = (width * 0.5f) / scale;
                    centerX = Utils.clamp(((highlight.left + highlight.right) * ((float) CropView.this.mImageWidth)) / 2.0f, limitX, ((float) CropView.this.mImageWidth) - limitX);
                } else {
                    centerX = ((float) CropView.this.mImageWidth) / 2.0f;
                }
                if (((float) Math.round(((float) CropView.this.mImageHeight) * scale)) > height) {
                    float limitY = (height * 0.5f) / scale;
                    centerY = Utils.clamp(((highlight.top + highlight.bottom) * ((float) CropView.this.mImageHeight)) / 2.0f, limitY, ((float) CropView.this.mImageHeight) - limitY);
                } else {
                    centerY = ((float) CropView.this.mImageHeight) / 2.0f;
                }
                this.mTargetX = centerX;
                this.mTargetY = centerY;
                this.mTargetScale = scale;
            }
        }
    }

    private class DetectFaceTask extends Thread {
        private final Bitmap mFaceBitmap;
        private int mFaceCount;
        private final Face[] mFaces = new Face[3];

        public DetectFaceTask(Bitmap bitmap) {
            this.mFaceBitmap = bitmap;
            setName("detect-face-thread");
        }

        public void run() {
            Bitmap bitmap = this.mFaceBitmap;
            this.mFaceCount = new FaceDetector(bitmap.getWidth(), bitmap.getHeight(), 3).findFaces(bitmap, this.mFaces);
            CropView.this.mMainHandler.sendMessage(CropView.this.mMainHandler.obtainMessage(1, this));
        }

        private RectF getFaceRect(Face face) {
            PointF point = new PointF();
            face.getMidPoint(point);
            int width = this.mFaceBitmap.getWidth();
            int height = this.mFaceBitmap.getHeight();
            float rx = face.eyesDistance() * 2.0f;
            float ry = rx;
            float aspect = CropView.this.mAspectRatio;
            if (aspect != GroundOverlayOptions.NO_DIMENSION) {
                if (aspect > WMElement.CAMERASIZEVALUE1B1) {
                    rx *= aspect;
                } else {
                    ry = rx / aspect;
                }
            }
            RectF r = new RectF(point.x - rx, point.y - ry, point.x + rx, point.y + ry);
            r.intersect(0.0f, 0.0f, (float) width, (float) height);
            if (aspect != GroundOverlayOptions.NO_DIMENSION) {
                if (r.width() / r.height() > aspect) {
                    float w = r.height() * aspect;
                    r.left = ((r.left + r.right) - w) * 0.5f;
                    r.right = r.left + w;
                } else {
                    float h = r.width() / aspect;
                    r.top = ((r.top + r.bottom) - h) * 0.5f;
                    r.bottom = r.top + h;
                }
            }
            r.left /= (float) width;
            r.right /= (float) width;
            r.top /= (float) height;
            r.bottom /= (float) height;
            return r;
        }

        public void updateFaces() {
            if (this.mFaceCount > 1) {
                int n = this.mFaceCount;
                for (int i = 0; i < n; i++) {
                    CropView.this.mFaceDetectionView.addFace(getFaceRect(this.mFaces[i]));
                }
                CropView.this.mFaceDetectionView.setVisibility(0);
            } else if (this.mFaceCount == 1) {
                CropView.this.mFaceDetectionView.setVisibility(1);
                CropView.this.mHighlightRectangle.setRectangle(getFaceRect(this.mFaces[0]));
                CropView.this.mHighlightRectangle.setVisibility(0);
            } else {
                CropView.this.mHighlightRectangle.setInitRectangle();
                CropView.this.mHighlightRectangle.setVisibility(0);
            }
        }
    }

    private class FaceHighlightView extends GLView {
        private ArrayList<RectF> mFaces;
        private int mPressedFaceIndex;
        private RectF mRect;

        private FaceHighlightView() {
            this.mFaces = new ArrayList();
            this.mRect = new RectF();
            this.mPressedFaceIndex = -1;
        }

        public void addFace(RectF faceRect) {
            this.mFaces.add(faceRect);
            invalidate();
        }

        private void renderFace(GLCanvas canvas, RectF face, boolean pressed) {
            GL11 gl = canvas.getGLInstance();
            if (pressed) {
                gl.glEnable(2960);
                gl.glClear(1024);
                gl.glStencilOp(7680, 7680, 7681);
                gl.glStencilFunc(519, 1, 1);
            }
            RectF r = CropView.this.mAnimation.mapRect(face, this.mRect);
            canvas.fillRect(r.left, r.top, r.width(), r.height(), 0);
            canvas.drawRect(r.left, r.top, r.width(), r.height(), CropView.this.mFacePaint);
            if (pressed) {
                gl.glStencilOp(7680, 7680, 7680);
            }
        }

        protected void renderBackground(GLCanvas canvas) {
            ArrayList<RectF> faces = this.mFaces;
            int i = 0;
            int n = faces.size();
            while (i < n) {
                renderFace(canvas, (RectF) faces.get(i), i == this.mPressedFaceIndex);
                i++;
            }
            GL11 gl = canvas.getGLInstance();
            if (this.mPressedFaceIndex != -1) {
                gl.glStencilFunc(517, 1, 1);
                canvas.fillRect(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), 1711276032);
                gl.glDisable(2960);
            }
        }

        private void setPressedFace(int index) {
            if (this.mPressedFaceIndex != index) {
                this.mPressedFaceIndex = index;
                invalidate();
            }
        }

        private int getFaceIndexByPosition(float x, float y) {
            ArrayList<RectF> faces = this.mFaces;
            int n = faces.size();
            for (int i = 0; i < n; i++) {
                if (CropView.this.mAnimation.mapRect((RectF) faces.get(i), this.mRect).contains(x, y)) {
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
                case 2:
                    setPressedFace(getFaceIndexByPosition(x, y));
                    break;
                case 1:
                case 3:
                    int index = this.mPressedFaceIndex;
                    setPressedFace(-1);
                    if (index != -1) {
                        CropView.this.mHighlightRectangle.setRectangle((RectF) this.mFaces.get(index));
                        CropView.this.mHighlightRectangle.setVisibility(0);
                        setVisibility(1);
                        break;
                    }
                    break;
            }
            return true;
        }
    }

    private class HighlightRectangle extends GLView {
        private ResourceTexture mArrow;
        private RectF mHighlightRect = new RectF(0.25f, 0.25f, 0.75f, 0.75f);
        private int mMovingEdges = 0;
        private float mReferenceX;
        private float mReferenceY;
        private PointF mTempPoint = new PointF();
        private RectF mTempRect = new RectF();

        public HighlightRectangle() {
            this.mArrow = new ResourceTexture(CropView.this.mActivity.getAndroidContext(), R.drawable.cut_point);
        }

        public void setInitRectangle() {
            float f = 0.6f;
            float targetRatio = CropView.this.mAspectRatio == GroundOverlayOptions.NO_DIMENSION ? WMElement.CAMERASIZEVALUE1B1 : (CropView.this.mAspectRatio * ((float) CropView.this.mImageHeight)) / ((float) CropView.this.mImageWidth);
            float w = (CropView.this.mFullScale ? WMElement.CAMERASIZEVALUE1B1 : 0.6f) / 2.0f;
            if (CropView.this.mFullScale) {
                f = WMElement.CAMERASIZEVALUE1B1;
            }
            float h = f / 2.0f;
            if (targetRatio > WMElement.CAMERASIZEVALUE1B1) {
                h = ((float) CropView.this.mImageHeight) >= CropView.MIN_SELECTION_LENGTH ? w / targetRatio : 0.5f;
            } else {
                w = ((float) CropView.this.mImageWidth) >= CropView.MIN_SELECTION_LENGTH ? h * targetRatio : 0.5f;
            }
            this.mHighlightRect.set(0.5f - w, 0.5f - h, 0.5f + w, 0.5f + h);
        }

        public void setRectangle(RectF faceRect) {
            this.mHighlightRect.set(faceRect);
            CropView.this.mAnimation.startParkingAnimation(faceRect);
            invalidate();
        }

        private void moveEdges(MotionEvent event) {
            float scale = CropView.this.mAnimation.getScale();
            float dx = ((event.getX() - this.mReferenceX) / scale) / ((float) CropView.this.mImageWidth);
            float dy = ((event.getY() - this.mReferenceY) / scale) / ((float) CropView.this.mImageHeight);
            this.mReferenceX = event.getX();
            this.mReferenceY = event.getY();
            RectF r = this.mHighlightRect;
            if ((this.mMovingEdges & 16) != 0) {
                dx = Utils.clamp(dx, -r.left, WMElement.CAMERASIZEVALUE1B1 - r.right);
                dy = Utils.clamp(dy, -r.top, WMElement.CAMERASIZEVALUE1B1 - r.bottom);
                r.top += dy;
                r.bottom += dy;
                r.left += dx;
                r.right += dx;
            } else {
                PointF point = this.mTempPoint;
                point.set(this.mReferenceX, this.mReferenceY);
                CropView.this.mAnimation.inverseMapPoint(point);
                float left = Utils.clamp(r.left + (CropView.MIN_SELECTION_LENGTH / (((float) CropView.this.mImageWidth) * scale)), 0.0f, (float) WMElement.CAMERASIZEVALUE1B1);
                float right = Utils.clamp(r.right - (CropView.MIN_SELECTION_LENGTH / (((float) CropView.this.mImageWidth) * scale)), 0.0f, (float) WMElement.CAMERASIZEVALUE1B1);
                float top = Utils.clamp(r.top + (CropView.MIN_SELECTION_LENGTH / (((float) CropView.this.mImageHeight) * scale)), 0.0f, (float) WMElement.CAMERASIZEVALUE1B1);
                float bottom = Utils.clamp(r.bottom - (CropView.MIN_SELECTION_LENGTH / (((float) CropView.this.mImageHeight) * scale)), 0.0f, (float) WMElement.CAMERASIZEVALUE1B1);
                if ((this.mMovingEdges & 4) != 0) {
                    r.right = Utils.clamp(point.x, left, (float) WMElement.CAMERASIZEVALUE1B1);
                }
                if ((this.mMovingEdges & 1) != 0) {
                    r.left = Utils.clamp(point.x, 0.0f, right);
                }
                if ((this.mMovingEdges & 2) != 0) {
                    r.top = Utils.clamp(point.y, 0.0f, bottom);
                }
                if ((this.mMovingEdges & 8) != 0) {
                    r.bottom = Utils.clamp(point.y, top, (float) WMElement.CAMERASIZEVALUE1B1);
                }
                if (CropView.this.mAspectRatio != GroundOverlayOptions.NO_DIMENSION) {
                    float height;
                    float width;
                    float targetRatio = (CropView.this.mAspectRatio * ((float) CropView.this.mImageHeight)) / ((float) CropView.this.mImageWidth);
                    if (r.width() / r.height() > targetRatio) {
                        height = r.width() / targetRatio;
                        if ((this.mMovingEdges & 8) != 0) {
                            r.bottom = Utils.clamp(r.top + height, top, (float) WMElement.CAMERASIZEVALUE1B1);
                        } else {
                            r.top = Utils.clamp(r.bottom - height, 0.0f, bottom);
                        }
                    } else {
                        width = r.height() * targetRatio;
                        if ((this.mMovingEdges & 1) != 0) {
                            r.left = Utils.clamp(r.right - width, 0.0f, right);
                        } else {
                            r.right = Utils.clamp(r.left + width, left, (float) WMElement.CAMERASIZEVALUE1B1);
                        }
                    }
                    if (r.width() / r.height() > targetRatio) {
                        width = r.height() * targetRatio;
                        if ((this.mMovingEdges & 1) != 0) {
                            r.left = Utils.clamp(r.right - width, 0.0f, right);
                        } else {
                            r.right = Utils.clamp(r.left + width, left, (float) WMElement.CAMERASIZEVALUE1B1);
                        }
                    } else {
                        height = r.width() / targetRatio;
                        if ((this.mMovingEdges & 8) != 0) {
                            r.bottom = Utils.clamp(r.top + height, top, (float) WMElement.CAMERASIZEVALUE1B1);
                        } else {
                            r.top = Utils.clamp(r.bottom - height, 0.0f, bottom);
                        }
                    }
                }
            }
            invalidate();
        }

        private void setMovingEdges(MotionEvent event) {
            RectF r = CropView.this.mAnimation.mapRect(this.mHighlightRect, this.mTempRect);
            float x = event.getX();
            float y = event.getY();
            if (x <= r.left + BitmapDescriptorFactory.HUE_ORANGE || x >= r.right - BitmapDescriptorFactory.HUE_ORANGE || y <= r.top + BitmapDescriptorFactory.HUE_ORANGE || y >= r.bottom - BitmapDescriptorFactory.HUE_ORANGE) {
                boolean inVerticalRange = r.top - BitmapDescriptorFactory.HUE_ORANGE <= y && y <= r.bottom + BitmapDescriptorFactory.HUE_ORANGE;
                boolean inHorizontalRange = r.left - BitmapDescriptorFactory.HUE_ORANGE <= x && x <= r.right + BitmapDescriptorFactory.HUE_ORANGE;
                if (inVerticalRange) {
                    boolean left = Math.abs(x - r.left) <= BitmapDescriptorFactory.HUE_ORANGE;
                    boolean right = Math.abs(x - r.right) <= BitmapDescriptorFactory.HUE_ORANGE;
                    if (left && right) {
                        left = Math.abs(x - r.left) < Math.abs(x - r.right);
                        right = !left;
                    }
                    if (left) {
                        this.mMovingEdges |= 1;
                    }
                    if (right) {
                        this.mMovingEdges |= 4;
                    }
                    if (CropView.this.mAspectRatio != GroundOverlayOptions.NO_DIMENSION && inHorizontalRange) {
                        this.mMovingEdges = (y > (r.top + r.bottom) / 2.0f ? 8 : 2) | this.mMovingEdges;
                    }
                }
                if (inHorizontalRange) {
                    boolean top = Math.abs(y - r.top) <= BitmapDescriptorFactory.HUE_ORANGE;
                    boolean bottom = Math.abs(y - r.bottom) <= BitmapDescriptorFactory.HUE_ORANGE;
                    if (top && bottom) {
                        top = Math.abs(y - r.top) < Math.abs(y - r.bottom);
                        bottom = !top;
                    }
                    if (top) {
                        this.mMovingEdges |= 2;
                    }
                    if (bottom) {
                        this.mMovingEdges |= 8;
                    }
                    if (CropView.this.mAspectRatio != GroundOverlayOptions.NO_DIMENSION && inVerticalRange) {
                        int i;
                        int i2 = this.mMovingEdges;
                        if (x > (r.left + r.right) / 2.0f) {
                            i = 4;
                        } else {
                            i = 1;
                        }
                        this.mMovingEdges = i | i2;
                    }
                }
                return;
            }
            this.mMovingEdges = 16;
        }

        protected boolean onTouch(MotionEvent event) {
            switch (event.getAction()) {
                case 0:
                    this.mReferenceX = event.getX();
                    this.mReferenceY = event.getY();
                    setMovingEdges(event);
                    invalidate();
                    return true;
                case 1:
                case 3:
                    this.mMovingEdges = 0;
                    CropView.this.mAnimation.startParkingAnimation(this.mHighlightRect);
                    invalidate();
                    return true;
                case 2:
                    moveEdges(event);
                    break;
            }
            return true;
        }

        protected void renderBackground(GLCanvas canvas) {
            RectF r = CropView.this.mAnimation.mapRect(this.mHighlightRect, this.mTempRect);
            drawHighlightRectangle(canvas, r);
            float centerY = (r.top + r.bottom) / 2.0f;
            float centerX = (r.left + r.right) / 2.0f;
            boolean notMoving = this.mMovingEdges == 0;
            if ((this.mMovingEdges & 4) != 0 || notMoving) {
                this.mArrow.draw(canvas, Math.round(r.right) - (this.mArrow.getWidth() / 2), Math.round(centerY) - (this.mArrow.getHeight() / 2));
            }
            if ((this.mMovingEdges & 1) != 0 || notMoving) {
                this.mArrow.draw(canvas, Math.round(r.left) - (this.mArrow.getWidth() / 2), Math.round(centerY) - (this.mArrow.getHeight() / 2));
            }
            if ((this.mMovingEdges & 2) != 0 || notMoving) {
                this.mArrow.draw(canvas, Math.round(centerX) - (this.mArrow.getWidth() / 2), Math.round(r.top) - (this.mArrow.getHeight() / 2));
            }
            if ((this.mMovingEdges & 8) != 0 || notMoving) {
                this.mArrow.draw(canvas, Math.round(centerX) - (this.mArrow.getWidth() / 2), Math.round(r.bottom) - (this.mArrow.getHeight() / 2));
            }
            if (CropView.this.mAspectRatio == GroundOverlayOptions.NO_DIMENSION) {
                if ((this.mMovingEdges & 1) == 0 && (this.mMovingEdges & 2) == 0) {
                    if (notMoving) {
                    }
                    if ((this.mMovingEdges & 1) == 0 && (this.mMovingEdges & 8) == 0) {
                        if (notMoving) {
                        }
                        if ((this.mMovingEdges & 4) == 0 && (this.mMovingEdges & 2) == 0) {
                            if (notMoving) {
                            }
                            if ((this.mMovingEdges & 4) == 0 && (this.mMovingEdges & 8) == 0) {
                                if (notMoving) {
                                }
                            }
                            this.mArrow.draw(canvas, Math.round(r.right) - (this.mArrow.getWidth() / 2), Math.round(r.bottom) - (this.mArrow.getHeight() / 2));
                        }
                        this.mArrow.draw(canvas, Math.round(r.right) - (this.mArrow.getWidth() / 2), Math.round(r.top) - (this.mArrow.getHeight() / 2));
                        if (notMoving) {
                            this.mArrow.draw(canvas, Math.round(r.right) - (this.mArrow.getWidth() / 2), Math.round(r.bottom) - (this.mArrow.getHeight() / 2));
                        }
                    }
                    this.mArrow.draw(canvas, Math.round(r.left) - (this.mArrow.getWidth() / 2), Math.round(r.bottom) - (this.mArrow.getHeight() / 2));
                    if (notMoving) {
                        this.mArrow.draw(canvas, Math.round(r.right) - (this.mArrow.getWidth() / 2), Math.round(r.top) - (this.mArrow.getHeight() / 2));
                    }
                    if (notMoving) {
                        this.mArrow.draw(canvas, Math.round(r.right) - (this.mArrow.getWidth() / 2), Math.round(r.bottom) - (this.mArrow.getHeight() / 2));
                    }
                }
                this.mArrow.draw(canvas, Math.round(r.left) - (this.mArrow.getWidth() / 2), Math.round(r.top) - (this.mArrow.getHeight() / 2));
                if (notMoving) {
                    this.mArrow.draw(canvas, Math.round(r.left) - (this.mArrow.getWidth() / 2), Math.round(r.bottom) - (this.mArrow.getHeight() / 2));
                }
                if (notMoving) {
                    this.mArrow.draw(canvas, Math.round(r.right) - (this.mArrow.getWidth() / 2), Math.round(r.top) - (this.mArrow.getHeight() / 2));
                }
                if (notMoving) {
                    this.mArrow.draw(canvas, Math.round(r.right) - (this.mArrow.getWidth() / 2), Math.round(r.bottom) - (this.mArrow.getHeight() / 2));
                }
            }
        }

        private void drawHighlightRectangle(GLCanvas canvas, RectF r) {
            GL11 gl = canvas.getGLInstance();
            gl.glLineWidth(MapConfig.MIN_ZOOM);
            gl.glEnable(2848);
            gl.glEnable(2960);
            gl.glClear(1024);
            gl.glStencilOp(7680, 7680, 7681);
            gl.glStencilFunc(519, 1, 1);
            if (!(CropView.this.mSpotlightRatioX == 0.0f || CropView.this.mSpotlightRatioY == 0.0f)) {
                float sx = r.width() * CropView.this.mSpotlightRatioX;
                float sy = r.height() * CropView.this.mSpotlightRatioY;
                canvas.drawRect(r.centerX() - (sx / 2.0f), r.centerY() - (sy / 2.0f), sx, sy, CropView.this.mPaint);
            }
            canvas.fillRect(r.left, r.top, r.width(), r.height(), 0);
            canvas.drawRect(r.left, r.top, r.width(), r.height(), CropView.this.mPaint);
            gl.glStencilFunc(517, 1, 1);
            gl.glStencilOp(7680, 7680, 7681);
            RectF imageRect = new RectF();
            CropView.this.mAnimation.mapRect(new RectF(0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1), imageRect);
            canvas.fillRect(imageRect.left, imageRect.top, imageRect.width(), imageRect.height(), 1711276032);
            gl.glStencilFunc(517, 1, 1);
            gl.glStencilOp(7680, 7680, 7680);
            canvas.fillRect(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), 0);
            gl.glDisable(2960);
        }
    }

    public CropView(GalleryContext activity) {
        this.mActivity = activity;
        this.mImageView = new TileImageView(activity);
        this.mFaceDetectionView = new FaceHighlightView();
        this.mHighlightRectangle = new HighlightRectangle();
        addComponent(this.mImageView);
        addComponent(this.mFaceDetectionView);
        addComponent(this.mHighlightRectangle);
        this.mHighlightRectangle.setVisibility(1);
        this.mPaint.setColor(this.mActivity.getAndroidContext().getResources().getColor(R.color.crop_bolder_selected));
        this.mPaint.setLineWidth(MapConfig.MIN_ZOOM);
        this.mFacePaint.setColor(-16777216);
        this.mFacePaint.setLineWidth(MapConfig.MIN_ZOOM);
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

    public void setAspectRatio(float ratio) {
        this.mAspectRatio = ratio;
    }

    public void setSpotlightRatio(float ratioX, float ratioY) {
        this.mSpotlightRatioX = ratioX;
        this.mSpotlightRatioY = ratioY;
    }

    public void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = r - l;
        int height = b - t;
        this.mFaceDetectionView.layout(0, 0, width, height);
        this.mHighlightRectangle.layout(0, 0, width, height);
        this.mImageView.layout(0, 0, width, height);
        if (this.mImageHeight != -1) {
            this.mAnimation.initialize();
            if (this.mHighlightRectangle.getVisibility() == 0) {
                this.mAnimation.parkNow(this.mHighlightRectangle.mHighlightRect);
            }
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
            invalidate();
        }
        setImageViewPosition(a.getCenterX(), a.getCenterY(), a.getScale());
        super.render(canvas);
    }

    public Rect getCropRectangle() {
        if (this.mHighlightRectangle.getVisibility() == 1) {
            return null;
        }
        RectF rect = this.mHighlightRectangle.mHighlightRect;
        return new Rect((int) (rect.left * ((float) this.mImageWidth)), (int) (rect.top * ((float) this.mImageHeight)), (int) (rect.right * ((float) this.mImageWidth)), (int) (rect.bottom * ((float) this.mImageHeight)));
    }

    protected void onDataModelChanged(Model dataModel) {
        this.mImageView.setModel(dataModel);
        this.mAnimation.initialize();
        super.onDataModelChanged(dataModel);
    }

    public void detectFaces(Bitmap bitmap) {
        Bitmap faceBitmap;
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

    public void initializeHighlightRectangle() {
        this.mHighlightRectangle.setInitRectangle();
        this.mHighlightRectangle.setVisibility(0);
    }

    public void resume() {
        this.mImageView.prepareTextures();
    }

    public void pause() {
        this.mImageView.freeTextures();
    }

    public void setGLRoot(GLRoot root) {
        this.mMainHandler.setGLRoot(root);
    }
}

package com.huawei.gallery.editor.imageshow;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
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
import com.android.gallery3d.util.GalleryUtils;
import com.autonavi.amap.mapcore.MapConfig;
import com.huawei.gallery.animation.CubicBezierInterpolator;
import com.huawei.gallery.editor.glrender.BaseRender.EditorStateDelegate;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.gallery.editor.glrender.SupportExpandMenuRender;
import com.huawei.gallery.editor.tools.EditorUtils;
import com.huawei.gallery.editor.tools.EditorUtils.RectComputer;
import com.huawei.gallery.editor.ui.BaseEditorView;
import com.huawei.gallery.editor.ui.DoubleFingerControl;
import com.huawei.gallery.editor.ui.DoubleFingerControl.Delegate;
import com.huawei.gallery.editor.ui.MosaicView.StyleManager;
import com.huawei.watermark.manager.parse.WMElement;
import javax.microedition.khronos.opengles.GL11;

public class ImageCrop {
    private static final float MIN_SELECTION_LENGTH = GalleryUtils.dpToPixel(50.0f);
    private static final int TOUCH_TOLERANCE = GalleryUtils.dpToPixel(20);
    private float mAspectRatio = GroundOverlayOptions.NO_DIMENSION;
    private final DoubleFingerControl mDoubleFingerControl;
    private final HighlightRectangle mHighlightRectangle;
    private int mImageHeight = -1;
    private final ImageView mImageView;
    private int mImageWidth = -1;
    private GLPaint mPaint = new GLPaint();
    private final BaseEditorView mRoot;
    private final StyleManager mStyleManager;
    private GLPaint mThirdPaint = new GLPaint();
    protected float[] mTmpPoint = new float[2];

    private class HighlightRectangle extends GLView {
        private ResourceTexture mArrowBottomLeft;
        private ResourceTexture mArrowBottomRight;
        private ResourceTexture mArrowTopLeft;
        private ResourceTexture mArrowTopRight;
        private RectF mBaseLineRect;
        private boolean mForce = true;
        private RectF mHighlightImageSource;
        private RectF mHighlightRect = new RectF(0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1);
        private ResourceTexture mHorizontalArrow;
        private int mMovingEdges = 0;
        private RectAnimation mRectAnimation = new RectAnimation();
        private float mReferenceX;
        private float mReferenceY;
        private RectF mTempRect = new RectF();
        private ResourceTexture mVerticalArrow;

        public HighlightRectangle() {
            this.mArrowTopLeft = new ResourceTexture(ImageCrop.this.mRoot.getContext(), R.drawable.btn_edit_crop_handle_topleft);
            this.mArrowTopRight = new ResourceTexture(ImageCrop.this.mRoot.getContext(), R.drawable.btn_edit_crop_handle_topright);
            this.mArrowBottomLeft = new ResourceTexture(ImageCrop.this.mRoot.getContext(), R.drawable.btn_edit_crop_handle_bottomleft);
            this.mArrowBottomRight = new ResourceTexture(ImageCrop.this.mRoot.getContext(), R.drawable.btn_edit_crop_handle_bottomright);
            this.mHorizontalArrow = new ResourceTexture(ImageCrop.this.mRoot.getContext(), R.drawable.btn_edit_crop_handle_horizon);
            this.mVerticalArrow = new ResourceTexture(ImageCrop.this.mRoot.getContext(), R.drawable.btn_edit_crop_handle_vertical);
        }

        public void reset() {
            this.mHighlightRect = new RectF(0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1);
            this.mBaseLineRect = null;
        }

        private void updateHighlightRect(float w, float h) {
            RectF ratio = new RectF(0.5f - w, 0.5f - h, 0.5f + w, 0.5f + h);
            if (this.mBaseLineRect == null) {
                this.mHighlightRect.set(ratio);
                return;
            }
            ImageCrop.this.mImageView.initBaseTarget();
            this.mTempRect.set(RectComputer.getRatioTargetRect(this.mBaseLineRect, ratio));
            float scale = Math.min(ImageCrop.this.mImageView.mBaseTarget.width() / this.mTempRect.width(), ImageCrop.this.mImageView.mBaseTarget.height() / this.mTempRect.height());
            if (scale < WMElement.CAMERASIZEVALUE1B1) {
                this.mHighlightRect.set(0.5f - (w * scale), 0.5f - (h * scale), (w * scale) + 0.5f, (h * scale) + 0.5f);
            } else {
                this.mHighlightRect.set(ratio);
            }
        }

        public void setInitRectangle(RectF initRect, boolean needAnime) {
            if (initRect == null) {
                float targetRatio = ImageCrop.this.mAspectRatio == GroundOverlayOptions.NO_DIMENSION ? WMElement.CAMERASIZEVALUE1B1 : (ImageCrop.this.mAspectRatio * ((float) ImageCrop.this.mImageHeight)) / ((float) ImageCrop.this.mImageWidth);
                float w = 0.5f;
                float h = 0.5f;
                if (targetRatio > WMElement.CAMERASIZEVALUE1B1) {
                    h = ((float) ImageCrop.this.mImageHeight) >= ImageCrop.MIN_SELECTION_LENGTH ? 0.5f / targetRatio : 0.5f;
                } else {
                    w = ((float) ImageCrop.this.mImageWidth) >= ImageCrop.MIN_SELECTION_LENGTH ? 0.5f * targetRatio : 0.5f;
                }
                RectF src = new RectF(this.mHighlightRect);
                if (this.mBaseLineRect != null) {
                    updateHighlightRect(w, h);
                } else {
                    this.mHighlightRect.set(0.5f - w, 0.5f - h, 0.5f + w, 0.5f + h);
                }
                if (needAnime) {
                    this.mRectAnimation.init(src, new RectF(this.mHighlightRect));
                    this.mRectAnimation.start();
                }
            } else {
                if (EditorUtils.isAlmostEquals(this.mHighlightRect, new RectF(0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1)) && needAnime) {
                    this.mRectAnimation.init(new RectF(this.mHighlightRect), initRect);
                    this.mRectAnimation.start();
                }
                this.mHighlightRect.set(initRect);
            }
            this.mForce = true;
            invalidate();
        }

        private void moveEdges(MotionEvent event) {
            float dx = (event.getX() - this.mReferenceX) / this.mBaseLineRect.width();
            float dy = (event.getY() - this.mReferenceY) / this.mBaseLineRect.height();
            this.mReferenceX = event.getX();
            this.mReferenceY = event.getY();
            RectF r = this.mHighlightRect;
            if ((this.mMovingEdges & 16) != 0) {
                dx = Utils.clamp(dx, -r.left, WMElement.CAMERASIZEVALUE1B1 - r.right);
                dy = Utils.clamp(dy, -r.top, WMElement.CAMERASIZEVALUE1B1 - r.bottom);
                r.left += dx;
                r.top += dy;
                r.right += dx;
                r.bottom += dy;
            } else {
                float x = (this.mReferenceX - this.mBaseLineRect.left) / this.mBaseLineRect.width();
                float y = (this.mReferenceY - this.mBaseLineRect.top) / this.mBaseLineRect.height();
                float left = Utils.clamp(r.left + (ImageCrop.MIN_SELECTION_LENGTH / this.mBaseLineRect.width()), 0.0f, (float) WMElement.CAMERASIZEVALUE1B1);
                float right = Utils.clamp(r.right - (ImageCrop.MIN_SELECTION_LENGTH / this.mBaseLineRect.width()), 0.0f, (float) WMElement.CAMERASIZEVALUE1B1);
                float top = Utils.clamp(r.top + (ImageCrop.MIN_SELECTION_LENGTH / this.mBaseLineRect.height()), 0.0f, (float) WMElement.CAMERASIZEVALUE1B1);
                float bottom = Utils.clamp(r.bottom - (ImageCrop.MIN_SELECTION_LENGTH / this.mBaseLineRect.height()), 0.0f, (float) WMElement.CAMERASIZEVALUE1B1);
                if ((this.mMovingEdges & 4) != 0) {
                    r.right = Utils.clamp(x, left, (float) WMElement.CAMERASIZEVALUE1B1);
                }
                if ((this.mMovingEdges & 1) != 0) {
                    r.left = Utils.clamp(x, 0.0f, right);
                }
                if ((this.mMovingEdges & 2) != 0) {
                    r.top = Utils.clamp(y, 0.0f, bottom);
                }
                if ((this.mMovingEdges & 8) != 0) {
                    r.bottom = Utils.clamp(y, top, (float) WMElement.CAMERASIZEVALUE1B1);
                }
                if (ImageCrop.this.mAspectRatio != GroundOverlayOptions.NO_DIMENSION) {
                    float height;
                    float width;
                    float targetRatio = (ImageCrop.this.mAspectRatio * ((float) ImageCrop.this.mImageHeight)) / ((float) ImageCrop.this.mImageWidth);
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
            RectF r = this.mTempRect;
            r.set(RectComputer.getRatioTargetRect(this.mBaseLineRect, this.mHighlightRect));
            float x = event.getX();
            float y = event.getY();
            if (x <= r.left + ((float) ImageCrop.TOUCH_TOLERANCE) || x >= r.right - ((float) ImageCrop.TOUCH_TOLERANCE) || y <= r.top + ((float) ImageCrop.TOUCH_TOLERANCE) || y >= r.bottom - ((float) ImageCrop.TOUCH_TOLERANCE)) {
                boolean inVerticalRange = r.top - ((float) ImageCrop.TOUCH_TOLERANCE) <= y ? y <= r.bottom + ((float) ImageCrop.TOUCH_TOLERANCE) : false;
                boolean inHorizontalRange = r.left - ((float) ImageCrop.TOUCH_TOLERANCE) <= x ? x <= r.right + ((float) ImageCrop.TOUCH_TOLERANCE) : false;
                if (inVerticalRange) {
                    boolean left = Math.abs(x - r.left) <= ((float) ImageCrop.TOUCH_TOLERANCE);
                    boolean right = Math.abs(x - r.right) <= ((float) ImageCrop.TOUCH_TOLERANCE);
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
                    if (ImageCrop.this.mAspectRatio != GroundOverlayOptions.NO_DIMENSION && inHorizontalRange) {
                        this.mMovingEdges = (y > (r.top + r.bottom) / 2.0f ? 8 : 2) | this.mMovingEdges;
                    }
                }
                if (inHorizontalRange) {
                    boolean top = Math.abs(y - r.top) <= ((float) ImageCrop.TOUCH_TOLERANCE);
                    boolean bottom = Math.abs(y - r.bottom) <= ((float) ImageCrop.TOUCH_TOLERANCE);
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
                    if (ImageCrop.this.mAspectRatio != GroundOverlayOptions.NO_DIMENSION && inVerticalRange) {
                        this.mMovingEdges = (x > (r.left + r.right) / 2.0f ? 4 : 1) | this.mMovingEdges;
                    }
                }
                return;
            }
            this.mMovingEdges = 16;
        }

        private boolean shouldIgnoreTouch() {
            return ImageCrop.this.mImageView.mRender.isMatrixAnimationActived() || this.mBaseLineRect == null;
        }

        protected boolean onTouch(MotionEvent event) {
            if (shouldIgnoreTouch()) {
                return true;
            }
            switch (event.getAction()) {
                case 0:
                    ImageCrop.this.mStyleManager.setStyle(1);
                    ImageCrop.this.mDoubleFingerControl.setMatrix(ImageCrop.this.mImageView.mRender.getDoubleFingerControlMatrix());
                    this.mReferenceX = event.getX();
                    this.mReferenceY = event.getY();
                    this.mForce = true;
                    setMovingEdges(event);
                    invalidate();
                    return true;
                case 1:
                case 3:
                    if (ImageCrop.this.mStyleManager.getStyle() == 2 && event.getPointerCount() == 1) {
                        ImageCrop.this.mImageView.mRender.prepareMatrixAnimation(ImageCrop.this.mDoubleFingerControl.getMatrix(), true);
                    }
                    this.mMovingEdges = 0;
                    ImageCrop.this.mStyleManager.setStyle(0);
                    invalidate();
                    return true;
                case 2:
                    int historySize = event.getHistorySize();
                    for (int h = 0; h < historySize; h++) {
                        if (event.getPointerCount() != 1) {
                            switch (ImageCrop.this.mStyleManager.getStyle()) {
                                case 0:
                                    ImageCrop.this.mDoubleFingerControl.clear();
                                    ImageCrop.this.mDoubleFingerControl.actionMove(event.getHistoricalX(0, h), event.getHistoricalY(0, h), event.getHistoricalX(1, h), event.getHistoricalY(1, h));
                                    ImageCrop.this.mStyleManager.setStyle(2);
                                    break;
                                case 1:
                                    event.setAction(3);
                                    return onTouch(event);
                                case 2:
                                    ImageCrop.this.mDoubleFingerControl.actionMove(event.getHistoricalX(0, h), event.getHistoricalY(0, h), event.getHistoricalX(1, h), event.getHistoricalY(1, h));
                                    ImageCrop.this.mImageView.mRender.prepareMatrixAnimation(ImageCrop.this.mDoubleFingerControl.getMatrix(), false);
                                    this.mForce = true;
                                    break;
                                default:
                                    break;
                            }
                        }
                        switch (ImageCrop.this.mStyleManager.getStyle()) {
                            case 0:
                                event.setAction(0);
                                return onTouch(event);
                            case 1:
                                moveEdges(event);
                                break;
                            case 2:
                                event.setAction(3);
                                return onTouch(event);
                            default:
                                break;
                        }
                    }
                    break;
            }
            return true;
        }

        public void updateParams(Rect newBaseLineRect, RectF newImageSourceRect) {
            if (this.mBaseLineRect == null) {
                this.mBaseLineRect = new RectF(newBaseLineRect);
            }
            if (this.mHighlightImageSource == null) {
                this.mHighlightImageSource = new RectF();
                this.mHighlightImageSource.set(RectComputer.getRatioTargetRect(newImageSourceRect, this.mHighlightRect));
            }
            RectF rectF = this.mBaseLineRect;
            RectF currentRect = (this.mRectAnimation == null || !this.mRectAnimation.isActive()) ? this.mHighlightRect : this.mRectAnimation.getCurrentRect();
            RectF r = RectComputer.getRatioTargetRect(rectF, currentRect);
            if (r != null) {
                this.mTempRect.set(r);
                r = this.mTempRect;
                this.mBaseLineRect.set(newBaseLineRect);
                ImageCrop.this.mImageWidth = (int) this.mBaseLineRect.width();
                ImageCrop.this.mImageHeight = (int) this.mBaseLineRect.height();
                if (this.mForce) {
                    float left = (r.left - this.mBaseLineRect.left) / this.mBaseLineRect.width();
                    float top = (r.top - this.mBaseLineRect.top) / this.mBaseLineRect.height();
                    float right = WMElement.CAMERASIZEVALUE1B1 - ((this.mBaseLineRect.right - r.right) / this.mBaseLineRect.width());
                    float bottom = WMElement.CAMERASIZEVALUE1B1 - ((this.mBaseLineRect.bottom - r.bottom) / this.mBaseLineRect.height());
                    if ((left < 0.0f || top < 0.0f || right > WMElement.CAMERASIZEVALUE1B1 || bottom > WMElement.CAMERASIZEVALUE1B1) && ImageCrop.this.mStyleManager.getStyle() == 0) {
                        setInitRectangle(null, false);
                    } else {
                        this.mHighlightRect.set(left, top, right, bottom);
                    }
                } else {
                    this.mHighlightRect.set(RectComputer.getRatioRect(this.mHighlightImageSource, newImageSourceRect));
                }
                this.mHighlightImageSource.set(RectComputer.getRatioTargetRect(newImageSourceRect, this.mHighlightRect));
            }
        }

        protected void renderBackground(GLCanvas canvas) {
            if (this.mBaseLineRect != null) {
                boolean more = this.mRectAnimation.calculate(AnimationTime.get());
                this.mTempRect.set(RectComputer.getRatioTargetRect(this.mBaseLineRect, this.mRectAnimation.isActive() ? this.mRectAnimation.getCurrentRect() : this.mHighlightRect));
                drawHighlightRectangle(canvas, this.mTempRect);
                drawRuleOfThird(canvas, this.mTempRect);
                drawAspectRatioArrow(canvas, this.mTempRect);
                if (more) {
                    invalidate();
                }
            }
        }

        private void drawRuleOfThird(GLCanvas canvas, RectF bounds) {
            float stepX = bounds.width() / MapConfig.MIN_ZOOM;
            float stepY = bounds.height() / MapConfig.MIN_ZOOM;
            float x = bounds.left + stepX;
            float y = bounds.top + stepY;
            for (int i = 0; i < 2; i++) {
                canvas.drawLine(x, bounds.top, x, bounds.bottom, ImageCrop.this.mThirdPaint);
                x += stepX;
            }
            float y2 = y;
            for (int j = 0; j < 2; j++) {
                canvas.drawLine(bounds.left, y2, bounds.right, y2, ImageCrop.this.mThirdPaint);
                y2 += stepY;
            }
        }

        private void drawAspectRatioArrow(GLCanvas canvas, RectF r) {
            float centerY = (r.top + r.bottom) / 2.0f;
            float centerX = (r.left + r.right) / 2.0f;
            if (this.mMovingEdges == 0) {
            }
            boolean isFreeAspect = ImageCrop.this.mAspectRatio == GroundOverlayOptions.NO_DIMENSION;
            this.mArrowBottomRight.draw(canvas, Math.round(r.right - (((float) this.mArrowBottomRight.getWidth()) / 2.0f)), Math.round(r.bottom - (((float) this.mArrowBottomRight.getHeight()) / 2.0f)));
            this.mArrowTopLeft.draw(canvas, Math.round(r.left - (((float) this.mArrowTopLeft.getWidth()) / 2.0f)), Math.round(r.top - (((float) this.mArrowTopLeft.getHeight()) / 2.0f)));
            this.mArrowTopRight.draw(canvas, Math.round(r.right - (((float) this.mArrowTopRight.getWidth()) / 2.0f)), Math.round(r.top - (((float) this.mArrowTopRight.getHeight()) / 2.0f)));
            this.mArrowBottomLeft.draw(canvas, Math.round(r.left - (((float) this.mArrowBottomLeft.getWidth()) / 2.0f)), Math.round(r.bottom - (((float) this.mArrowBottomLeft.getHeight()) / 2.0f)));
            if (isFreeAspect) {
                this.mVerticalArrow.draw(canvas, Math.round(r.right - (((float) this.mVerticalArrow.getWidth()) / 2.0f)), Math.round(centerY - (((float) this.mVerticalArrow.getHeight()) / 2.0f)));
                this.mVerticalArrow.draw(canvas, Math.round(r.left - (((float) this.mVerticalArrow.getWidth()) / 2.0f)), Math.round(centerY - (((float) this.mVerticalArrow.getHeight()) / 2.0f)));
                this.mHorizontalArrow.draw(canvas, Math.round(centerX - (((float) this.mHorizontalArrow.getWidth()) / 2.0f)), Math.round(r.top - (((float) this.mHorizontalArrow.getHeight()) / 2.0f)));
                this.mHorizontalArrow.draw(canvas, Math.round(centerX - (((float) this.mHorizontalArrow.getWidth()) / 2.0f)), Math.round(r.bottom - (((float) this.mHorizontalArrow.getHeight()) / 2.0f)));
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
            canvas.fillRect(r.left, r.top, r.width(), r.height(), 0);
            canvas.drawRect(r.left, r.top, r.width(), r.height(), ImageCrop.this.mPaint);
            gl.glStencilFunc(517, 1, 1);
            gl.glStencilOp(7680, 7680, 7681);
            canvas.fillRect((float) this.mBounds.left, (float) this.mBounds.top, (float) this.mBounds.width(), (float) this.mBounds.height(), 1711276032);
            gl.glStencilFunc(517, 1, 1);
            gl.glStencilOp(7680, 7680, 7680);
            canvas.fillRect(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), 0);
            gl.glDisable(2960);
        }
    }

    private class ImageView extends GLView {
        private final int DISPLAY_PADDING_BOTTOM = GalleryUtils.dpToPixel(24);
        private final int DISPLAY_PADDING_LEFT = GalleryUtils.dpToPixel(30);
        private final int DISPLAY_PADDING_RIGHT = GalleryUtils.dpToPixel(30);
        private final int DISPLAY_PADDING_TOP = GalleryUtils.dpToPixel(24);
        private RectF mBaseTarget = new RectF();
        private int mCurBitmapHeight = -1;
        private int mCurBitmapWidth = -1;
        private final SupportExpandMenuRender mRender;
        private RectF mSource = new RectF();
        private final EditorStateDelegate mStateDelegate;

        public ImageView(EditorViewDelegate viewDelegate, EditorStateDelegate stateDelegate) {
            this.mStateDelegate = stateDelegate;
            updateCurBitmapSize();
            this.mRender = new SupportExpandMenuRender(viewDelegate, this.mStateDelegate);
            this.mRender.setDisplayPaddingRect(this.DISPLAY_PADDING_LEFT, this.DISPLAY_PADDING_TOP, this.DISPLAY_PADDING_RIGHT, this.DISPLAY_PADDING_BOTTOM);
        }

        public void setSourceRect(RectF rect) {
            this.mSource.set(rect);
        }

        private boolean isCurSizeChange() {
            int curBitmapHeight = -1;
            int curBitmapWidth = -1;
            Bitmap bmp = this.mStateDelegate.computeRenderTexture();
            if (bmp != null) {
                curBitmapHeight = bmp.getHeight();
                curBitmapWidth = bmp.getWidth();
            }
            if (curBitmapHeight == this.mCurBitmapHeight && curBitmapWidth == this.mCurBitmapWidth) {
                return false;
            }
            return true;
        }

        private void updateCurBitmapSize() {
            Bitmap bmp = this.mStateDelegate.computeRenderTexture();
            if (bmp != null) {
                this.mCurBitmapHeight = bmp.getHeight();
                this.mCurBitmapWidth = bmp.getWidth();
            }
        }

        public void initBaseTarget() {
            Rect target1 = new Rect();
            Rect target2 = new Rect();
            RectComputer.computerRect(this.mRender, null, target1);
            RectComputer.computeCropMaskRect(this.mRender, target2);
            this.mBaseTarget.set(RectComputer.getOverRect(target1, target2));
        }

        public RectF getSourceRect() {
            return this.mSource;
        }

        public int getImageWidth() {
            return this.mStateDelegate.getPreviewTexture().getWidth();
        }

        public int getImageHeight() {
            return this.mStateDelegate.getPreviewTexture().getHeight();
        }

        protected void render(GLCanvas canvas) {
            if (isCurSizeChange()) {
                ImageCrop.this.mDoubleFingerControl.reset();
            }
            this.mRender.render(canvas);
        }
    }

    public static class RectAnimation extends Animation {
        public RectF mCurrentRect;
        public RectF mSrcRect;
        public RectF mTargetRect;

        public void init(RectF src, RectF target) {
            this.mSrcRect = src;
            this.mTargetRect = target;
            this.mCurrentRect = new RectF(src);
            setDuration(300);
            setInterpolator(new CubicBezierInterpolator());
        }

        protected void onCalculate(float progress) {
            this.mCurrentRect.set(this.mSrcRect.left + ((this.mTargetRect.left - this.mSrcRect.left) * progress), this.mSrcRect.top + ((this.mTargetRect.top - this.mSrcRect.top) * progress), this.mSrcRect.right + ((this.mTargetRect.right - this.mSrcRect.right) * progress), this.mSrcRect.bottom + ((this.mTargetRect.bottom - this.mSrcRect.bottom) * progress));
        }

        public RectF getCurrentRect() {
            return this.mCurrentRect;
        }
    }

    public ImageCrop(BaseEditorView root, final EditorStateDelegate state) {
        GalleryContext context = root.getGalleryContext();
        this.mRoot = root;
        this.mImageView = new ImageView(root, state);
        this.mHighlightRectangle = new HighlightRectangle();
        this.mDoubleFingerControl = new DoubleFingerControl();
        this.mDoubleFingerControl.setDelegate(new Delegate() {
            public float[] getValidDisplacement(float x, float y) {
                int i = 0;
                ImageCrop.this.mTmpPoint[0] = 0.0f;
                ImageCrop.this.mTmpPoint[1] = 0.0f;
                if (ImageCrop.this.mHighlightRectangle.mBaseLineRect == null) {
                    return ImageCrop.this.mTmpPoint;
                }
                int top = (int) ImageCrop.this.mHighlightRectangle.mBaseLineRect.top;
                int right = (int) ImageCrop.this.mHighlightRectangle.mBaseLineRect.right;
                int bottom = (int) ImageCrop.this.mHighlightRectangle.mBaseLineRect.bottom;
                if (((int) ImageCrop.this.mHighlightRectangle.mBaseLineRect.left) <= 0 && x > 0.0f) {
                    ImageCrop.this.mTmpPoint[0] = x;
                }
                if (right >= ImageCrop.this.mRoot.getWidth() - (ImageCrop.this.mRoot.isPort() ? 0 : ImageCrop.this.mRoot.getNavigationBarHeight()) && x < 0.0f) {
                    ImageCrop.this.mTmpPoint[0] = x;
                }
                if (top <= ImageCrop.this.mRoot.getActionBarHeight() && y > 0.0f) {
                    ImageCrop.this.mTmpPoint[1] = y;
                }
                int height = ImageCrop.this.mRoot.getHeight();
                int menuHeight = state.getMenuHeight() + state.getSubMenuHeight();
                if (ImageCrop.this.mRoot.isPort()) {
                    i = ImageCrop.this.mRoot.getNavigationBarHeight();
                }
                if (bottom >= height - (i + menuHeight) && y < 0.0f) {
                    ImageCrop.this.mTmpPoint[1] = y;
                }
                return ImageCrop.this.mTmpPoint;
            }
        });
        this.mStyleManager = new StyleManager();
        this.mPaint.setColor(context.getResources().getColor(R.color.crop_bolder_selected));
        this.mPaint.setLineWidth(MapConfig.MIN_ZOOM);
        this.mThirdPaint.setColor(context.getResources().getColor(R.color.crop_bolder_selected));
        this.mThirdPaint.setLineWidth(WMElement.CAMERASIZEVALUE1B1);
    }

    public void setAspectRatio(float ratio, RectF initRect) {
        this.mAspectRatio = ratio;
        reset(initRect);
    }

    public void onLayout(boolean changed, int l, int t, int r, int b) {
        this.mImageView.layout(l, t, r, b);
        this.mHighlightRectangle.layout(l, t, r, b);
        if (changed) {
            this.mHighlightRectangle.mForce = false;
        }
        this.mHighlightRectangle.invalidate();
    }

    public void onConfigurationChanged() {
        changeHighlightRectangle(true);
    }

    public void onNavigationBarChanged() {
        changeHighlightRectangle(false);
    }

    private void changeHighlightRectangle(boolean needResetScale) {
        GLRoot root = this.mRoot.getGLRoot();
        if (root != null) {
            root.lockRenderThread();
            try {
                this.mHighlightRectangle.mForce = false;
                if (needResetScale) {
                    this.mDoubleFingerControl.reset();
                }
                this.mHighlightRectangle.invalidate();
            } finally {
                root.unlockRenderThread();
            }
        }
    }

    public RectF getCropRectangle() {
        if (this.mHighlightRectangle.getVisibility() == 1) {
            return null;
        }
        RectF rect = new RectF();
        rect.set(RectComputer.getRatioTargetRect(this.mImageView.getSourceRect(), this.mHighlightRectangle.mHighlightRect, (float) this.mImageView.getImageWidth(), (float) this.mImageView.getImageHeight()));
        return rect;
    }

    public SupportExpandMenuRender getRender() {
        return this.mImageView.mRender;
    }

    public void onAnimationRenderFinished(Rect source, Rect target) {
        Rect baseline = RectComputer.getOverRect(target, this.mImageView.bounds());
        this.mImageView.setSourceRect(RectComputer.getRatioTargetRect(new RectF(source), new RectF(((float) (baseline.left - target.left)) / ((float) target.width()), ((float) (baseline.top - target.top)) / ((float) target.height()), (((float) (baseline.right - target.right)) / ((float) target.width())) + WMElement.CAMERASIZEVALUE1B1, (((float) (baseline.bottom - target.bottom)) / ((float) target.height())) + WMElement.CAMERASIZEVALUE1B1)));
        if (isConfigurateFinished()) {
            this.mHighlightRectangle.updateParams(RectComputer.getOverRect(target, this.mImageView.bounds()), this.mImageView.getSourceRect());
        }
        if (!this.mImageView.mRender.isEditorOpenOrQuitEffectActived()) {
            this.mHighlightRectangle.setVisibility(0);
        }
        if (this.mImageView.isCurSizeChange()) {
            this.mHighlightRectangle.setInitRectangle(null, false);
            this.mImageView.updateCurBitmapSize();
        }
    }

    public boolean isConfigurateFinished() {
        boolean z = true;
        if (this.mRoot.isPort()) {
            if (this.mImageView.bounds().height() < this.mImageView.getWidth()) {
                z = false;
            }
            return z;
        }
        if (this.mImageView.bounds().height() > this.mImageView.getWidth()) {
            z = false;
        }
        return z;
    }

    public Rect getOpenAnimationRect() {
        if (this.mHighlightRectangle.mBaseLineRect == null) {
            return null;
        }
        this.mHighlightRectangle.mTempRect.set(RectComputer.getRatioTargetRect(this.mHighlightRectangle.mBaseLineRect, this.mHighlightRectangle.mHighlightRect));
        return new Rect((int) this.mHighlightRectangle.mTempRect.left, (int) this.mHighlightRectangle.mTempRect.top, (int) this.mHighlightRectangle.mTempRect.right, (int) this.mHighlightRectangle.mTempRect.bottom);
    }

    public void setPadding(int x, int y) {
        this.mImageView.mRender.setDisplayPaddingRect(x, y, x, y);
    }

    public void initializeHighlightRectangle(RectF initRect) {
        this.mHighlightRectangle.setInitRectangle(initRect, true);
    }

    public void resume() {
        this.mRoot.addComponent(this.mImageView);
        this.mRoot.addComponent(this.mHighlightRectangle);
        this.mHighlightRectangle.setVisibility(1);
        this.mImageWidth = this.mImageView.getImageWidth();
        this.mImageHeight = this.mImageView.getImageHeight();
        this.mImageView.setSourceRect(new RectF(0.0f, 0.0f, (float) this.mImageWidth, (float) this.mImageHeight));
    }

    public void pause() {
        this.mRoot.removeComponent(this.mImageView);
        this.mRoot.removeComponent(this.mHighlightRectangle);
        this.mHighlightRectangle.reset();
    }

    public void reset(RectF initRect) {
        initializeHighlightRectangle(initRect);
        this.mRoot.invalidate();
    }
}

package com.huawei.gallery.refocus.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.os.Message;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.android.gallery3d.R;
import com.android.gallery3d.anim.Animation;
import com.android.gallery3d.anim.ClickScaleTransition;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.ui.AbsPhotoView;
import com.android.gallery3d.ui.AbsPhotoView.Size;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.GestureRecognizer;
import com.android.gallery3d.ui.ProgressSpinner;
import com.android.gallery3d.ui.ResourceTexture;
import com.android.gallery3d.ui.ScreenNail;
import com.android.gallery3d.ui.SupportDisplayEngineTileImageView;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.ui.TileImageView;
import com.android.gallery3d.ui.TileImageView.DirectShowNail;
import com.android.gallery3d.ui.TiledScreenNail;
import com.android.gallery3d.util.DisplayEngineUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.RangeArray;
import com.huawei.gallery.app.AbsAlbumPage;
import com.huawei.gallery.app.AbsPhotoPage.Model;
import com.huawei.gallery.displayengine.ScreenNailCommonDisplayEnginePool;
import com.huawei.gallery.refocus.app.RefocusPositionController;
import com.huawei.watermark.manager.parse.WMElement;
import com.huawei.watermark.ui.WMComponent;

public class RefocusView extends GLView {
    private static float TRANSITION_SCALE_FACTOR = 0.74f;
    private AccelerateInterpolator mAlphaInterpolator = new AccelerateInterpolator(0.9f);
    private SlotAnimation mAnimation;
    private boolean mCancelExtraScalingPending;
    private ClickScaleTransition mClickScaleTransition;
    private int mCompensation = 0;
    private Context mContext;
    private int mDisplayRotation = 0;
    private final MyGestureListener mGestureListener;
    private final GestureRecognizer mGestureRecognizer;
    private SynchronizedHandler mHandler;
    private int mHolding;
    private Listener mListener;
    private ProgressSpinner mLoadingSpinner;
    private Model mModel;
    private boolean mNeedHandleActionUp;
    private int mNextBound;
    private ResourceTexture mNoThumbTexture;
    private Picture mPicture = null;
    private final RangeArray<Picture> mPictures = new RangeArray(-3, 3);
    private final int mPlaceholderColor;
    private final RefocusPositionController mPositionController;
    private int mPrevBound;
    private Rect mRect = null;
    private ZInterpolator mScaleInterpolator = new ZInterpolator(0.5f);
    private Size[] mSizes = new Size[7];
    private TileImageView mTileView;
    private int mTouchBoxIndex = Integer.MAX_VALUE;

    public interface Listener {
        int getActionBarHeight();

        boolean isRangeMeasureMode();

        void onDecodeImageComplete();

        void onLoadStateChange(int i);

        void onSingleTapUp(int i, int i2);

        boolean onTouch(MotionEvent motionEvent);

        void refreshRangeMeasureView();
    }

    abstract class AbstractDirectShowNail implements DirectShowNail {
        private int mImageHeight;
        private int mImageWidth;
        private int mRotation;
        protected ScreenNail mScreenNail;

        protected abstract int getImageHeight();

        protected abstract int getImageWidth();

        AbstractDirectShowNail() {
        }

        protected void init() {
            this.mImageWidth = getImageWidth();
            this.mImageHeight = getImageHeight();
            this.mRotation = getRotation();
        }

        public void draw(GLCanvas canvas) {
            if (this.mScreenNail != null) {
                int imageWidth = this.mImageWidth;
                int imageHeight = this.mImageHeight;
                if (imageWidth > 0 && imageHeight > 0) {
                    if (90 == this.mRotation || 270 == this.mRotation) {
                        imageWidth = this.mImageHeight;
                        imageHeight = this.mImageWidth;
                    }
                    float scale = RefocusPositionController.getMinimalScale(imageWidth, imageHeight, RefocusView.this.mTileView.getWidth(), RefocusView.this.mTileView.getHeight());
                    float renderWidth = ((float) this.mImageWidth) * scale;
                    float renderHeight = ((float) this.mImageHeight) * scale;
                    GLCanvas gLCanvas = canvas;
                    this.mScreenNail.draw(gLCanvas, Math.round((((float) RefocusView.this.mTileView.getWidth()) - renderWidth) / 2.0f), Math.round((((float) RefocusView.this.mTileView.getHeight()) - renderHeight) / 2.0f), Math.round(renderWidth), Math.round(renderHeight));
                }
            }
        }

        public void recycle() {
        }
    }

    private interface Picture {
        void draw(GLCanvas gLCanvas, Rect rect);

        Size getSize();

        void reload();

        void setScreenNail(ScreenNail screenNail);
    }

    class FullPicture implements Picture {
        private int mLoadingState = 0;
        private int mRotation;
        private Size mSize = new Size();

        public FullPicture() {
            RefocusView.this.mLoadingSpinner.startAnimation();
        }

        public void reload() {
            RefocusView.this.mTileView.notifyModelInvalidated();
            int lastState = this.mLoadingState;
            this.mLoadingState = RefocusView.this.mModel.getLoadingState(0);
            if (lastState != this.mLoadingState) {
                if (this.mLoadingState == 0) {
                    RefocusView.this.mLoadingSpinner.startAnimation();
                }
                if (RefocusView.this.mListener != null) {
                    RefocusView.this.mListener.onLoadStateChange(this.mLoadingState);
                }
            }
            setScreenNail(RefocusView.this.mModel.getScreenNail(0));
            updateSize();
        }

        public Size getSize() {
            return this.mSize;
        }

        private void updateSize() {
            this.mRotation = RefocusView.this.mModel.getImageRotation(0);
            int w = RefocusView.this.mTileView.getImageWidth();
            int h = RefocusView.this.mTileView.getImageHeight();
            this.mSize.width = RefocusView.getRotated(this.mRotation, w, h);
            this.mSize.height = RefocusView.getRotated(this.mRotation, h, w);
        }

        public void draw(GLCanvas canvas, Rect r) {
            drawTileView(canvas, r);
        }

        public void setScreenNail(ScreenNail s) {
            RefocusView.this.mTileView.setScreenNail(s);
        }

        private void drawTileView(GLCanvas canvas, Rect r) {
            boolean wantsCardEffect;
            float imageScale = RefocusView.this.mPositionController.getImageScale();
            int viewW = RefocusView.this.getWidth();
            int viewH = RefocusView.this.getHeight();
            float cx = r.exactCenterX();
            float cy = r.exactCenterY();
            canvas.save(3);
            float filmRatio = RefocusView.this.mPositionController.getFilmRatio();
            if (filmRatio == WMElement.CAMERASIZEVALUE1B1 || RefocusView.this.mPositionController.inOpeningAnimation()) {
                wantsCardEffect = false;
            } else {
                wantsCardEffect = !RefocusView.this.mPositionController.inDeleteSnapBackAnimation();
            }
            if (wantsCardEffect) {
                int left = r.left;
                int right = r.right;
                float progress = Utils.clamp(RefocusView.calculateMoveOutProgress(left, right, viewW), (float) GroundOverlayOptions.NO_DIMENSION, (float) WMElement.CAMERASIZEVALUE1B1);
                if (progress < 0.0f) {
                    float cxPage;
                    float scale = RefocusView.this.getScrollScale(progress);
                    float alpha = RefocusView.this.getScrollAlpha(progress);
                    scale = RefocusView.interpolate(filmRatio, scale, WMElement.CAMERASIZEVALUE1B1);
                    imageScale *= scale;
                    canvas.multiplyAlpha(RefocusView.interpolate(filmRatio, alpha, WMElement.CAMERASIZEVALUE1B1));
                    if (right - left <= viewW) {
                        cxPage = ((float) viewW) / 2.0f;
                    } else {
                        cxPage = (((float) (right - left)) * scale) / 2.0f;
                    }
                    cx = RefocusView.interpolate(filmRatio, cxPage, cx);
                }
            }
            setTileViewPosition(cx, cy, viewW, viewH, imageScale);
            RefocusView.this.renderChild(canvas, RefocusView.this.mTileView);
            if (this.mLoadingState == 2) {
                RefocusView.this.drawLoadingFailMessage(canvas);
            }
            canvas.translate((float) ((int) ((-cx) - 0.5f)), (float) ((int) ((-cy) - 0.5f)));
            canvas.restore();
        }

        private void setTileViewPosition(float cx, float cy, int viewW, int viewH, float scale) {
            int x;
            int y;
            int imageW = RefocusView.this.mPositionController.getImageWidth();
            int imageH = RefocusView.this.mPositionController.getImageHeight();
            int centerX = (int) (((((float) imageW) / 2.0f) + (((((float) viewW) / 2.0f) - cx) / scale)) + 0.5f);
            int centerY = (int) (((((float) imageH) / 2.0f) + (((((float) viewH) / 2.0f) - cy) / scale)) + 0.5f);
            int inverseX = imageW - centerX;
            int inverseY = imageH - centerY;
            switch (this.mRotation) {
                case 0:
                    x = centerX;
                    y = centerY;
                    break;
                case WMComponent.ORI_90 /*90*/:
                    x = centerY;
                    y = inverseX;
                    break;
                case 180:
                    x = inverseX;
                    y = inverseY;
                    break;
                case 270:
                    x = inverseY;
                    y = centerX;
                    break;
                default:
                    throw new RuntimeException(String.valueOf(this.mRotation));
            }
            RefocusView.this.mTileView.setPosition((float) x, (float) y, scale, this.mRotation);
        }
    }

    class MediaItemDirectShowNail extends AbstractDirectShowNail {
        private MediaItem mItem;

        public MediaItemDirectShowNail(MediaItem item) {
            super();
            Bitmap bmp = item.getScreenNailBitmap(1);
            if (bmp != null) {
                if (DisplayEngineUtils.isDisplayEngineEnable()) {
                    bmp = DisplayEngineUtils.processScreenNailACE(bmp, item, null);
                }
                this.mScreenNail = new TiledScreenNail(bmp);
            }
            this.mItem = item;
            init();
        }

        protected int getImageWidth() {
            return this.mItem.getWidth();
        }

        protected int getImageHeight() {
            return this.mItem.getHeight();
        }

        public int getRotation() {
            return this.mItem.getRotation();
        }

        public void recycle() {
            if (this.mScreenNail != null) {
                this.mScreenNail.recycle();
                this.mScreenNail = null;
            }
        }
    }

    private class MyGestureListener implements com.android.gallery3d.ui.GestureRecognizer.Listener {
        private boolean mIgnoreScalingGesture;
        private boolean mIgnoreScrollingGesture;
        private boolean mIgnoreSwipingGesture;
        private boolean mIgnoreUpEvent;
        private boolean mScrolledAfterDown;

        private MyGestureListener() {
            this.mIgnoreUpEvent = false;
        }

        public boolean onSingleTapUp(float x, float y) {
            if ((VERSION.SDK_INT < 14 && (RefocusView.this.mHolding & 1) == 0) || this.mIgnoreSwipingGesture) {
                return true;
            }
            RefocusView refocusView = RefocusView.this;
            refocusView.mHolding = refocusView.mHolding & -2;
            if (RefocusView.this.mListener != null) {
                Matrix m = RefocusView.this.getGLRoot().getCompensationMatrix();
                Matrix inv = new Matrix();
                m.invert(inv);
                float[] pts = new float[]{x, y};
                inv.mapPoints(pts);
                RefocusView.this.mListener.onSingleTapUp((int) (pts[0] + 0.5f), (int) (pts[1] + 0.5f));
            }
            GalleryLog.printDFXLog("onSingleTap in refocusView");
            return true;
        }

        public boolean onDoubleTap(float x, float y) {
            if (this.mIgnoreSwipingGesture) {
                return true;
            }
            RefocusPositionController controller = RefocusView.this.mPositionController;
            if (controller.isAtMinimalScale()) {
                return true;
            }
            float scale = controller.getImageScale();
            this.mIgnoreUpEvent = true;
            float newScale = calculateScale(controller.getImageWidth(), controller.getImageHeight(), scale);
            if (controller.isAtMinimalScale()) {
                controller.zoomIn(x, y, Math.max(newScale, WMElement.CAMERASIZEVALUE1B1 * scale));
            } else {
                controller.resetToFullView();
            }
            return true;
        }

        private float calculateScale(int width, int height, float origScale) {
            float scale;
            float minScreenLength = (float) Math.min(RefocusView.this.mContext.getResources().getDisplayMetrics().widthPixels, RefocusView.this.mContext.getResources().getDisplayMetrics().heightPixels);
            float x = (float) Math.min(width, height);
            if (x <= minScreenLength / 2.0f) {
                scale = 2.0f;
            } else if (x < minScreenLength * 2.0f) {
                scale = 0.33333334f + ((5.0f * minScreenLength) / (6.0f * x));
            } else {
                scale = 0.5f + ((0.5f * minScreenLength) / x);
            }
            if (((double) Math.abs(origScale - scale)) < 0.1d) {
                return origScale;
            }
            return scale;
        }

        public boolean onScroll(float dx, float dy, float totalX, float totalY) {
            if (this.mIgnoreSwipingGesture) {
                return true;
            }
            if (!this.mScrolledAfterDown) {
                this.mScrolledAfterDown = true;
            }
            if (this.mIgnoreScrollingGesture) {
                return true;
            }
            RefocusView.this.mPositionController.scrollPage((int) ((-dx) + 0.5f), (int) ((-dy) + 0.5f));
            return true;
        }

        public boolean onFling(float velocityX, float velocityY) {
            if (this.mIgnoreSwipingGesture) {
                return true;
            }
            flingImages(velocityX, velocityY);
            return true;
        }

        private boolean flingImages(float velocityX, float velocityY) {
            return RefocusView.this.mPositionController.flingPage((int) (velocityX + 0.5f), (int) (velocityY + 0.5f));
        }

        public boolean onScaleBegin(float focusX, float focusY) {
            if (this.mIgnoreSwipingGesture) {
                return true;
            }
            RefocusView.this.mPositionController.beginScale(focusX, focusY);
            return true;
        }

        public boolean onScale(float focusX, float focusY, float scale) {
            if (this.mIgnoreSwipingGesture || this.mIgnoreScalingGesture) {
                return true;
            }
            if (Float.isNaN(scale) || Float.isInfinite(scale)) {
                return false;
            }
            if (RefocusView.this.mPositionController.scaleBy(scale, focusX, focusY) != 0) {
                startExtraScalingIfNeeded();
            } else {
                stopExtraScalingIfNeeded();
            }
            return true;
        }

        public void onLongPress(MotionEvent e) {
        }

        public void onScaleEnd() {
            if (!this.mIgnoreSwipingGesture && !this.mIgnoreScalingGesture) {
                stopExtraScalingIfNeeded();
                RefocusView.this.mPositionController.endScale();
            }
        }

        private void startExtraScalingIfNeeded() {
            if (!RefocusView.this.mCancelExtraScalingPending) {
                RefocusView.this.mPositionController.setExtraScalingRange(true);
                RefocusView.this.mCancelExtraScalingPending = true;
            }
        }

        private void stopExtraScalingIfNeeded() {
            if (RefocusView.this.mCancelExtraScalingPending) {
                RefocusView.this.mHandler.removeMessages(2);
                RefocusView.this.mPositionController.setExtraScalingRange(false);
                RefocusView.this.mCancelExtraScalingPending = false;
            }
        }

        public void onDown(float x, float y) {
            if (!this.mIgnoreSwipingGesture) {
                RefocusView refocusView = RefocusView.this;
                refocusView.mHolding = refocusView.mHolding | 1;
                this.mScrolledAfterDown = false;
                RefocusView.this.mTouchBoxIndex = Integer.MAX_VALUE;
            }
        }

        public void onUp() {
            if (!this.mIgnoreSwipingGesture) {
                RefocusView refocusView = RefocusView.this;
                refocusView.mHolding = refocusView.mHolding & -2;
                if (this.mIgnoreUpEvent) {
                    this.mIgnoreUpEvent = false;
                }
            }
        }

        public void setScaleEnable(boolean enable) {
            this.mIgnoreScalingGesture = !enable;
        }

        public void setScrollEnable(boolean enable) {
            this.mIgnoreScrollingGesture = !enable;
        }
    }

    class MyHandler extends SynchronizedHandler {
        public MyHandler(GLRoot root) {
            super(root);
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 2:
                    RefocusView.this.mGestureRecognizer.cancelScale();
                    RefocusView.this.mPositionController.setExtraScalingRange(false);
                    RefocusView.this.mCancelExtraScalingPending = false;
                    return;
                case 3:
                    if (RefocusView.this.mListener != null) {
                        RefocusView.this.mListener.refreshRangeMeasureView();
                        return;
                    }
                    return;
                default:
                    throw new AssertionError(message.what);
            }
        }
    }

    private class ScreenNailPicture implements Picture {
        private int mIndex;
        private int mLoadingState = 0;
        private int mRotation;
        private ScreenNail mScreenNail;
        private Size mSize = new Size();

        public ScreenNailPicture(int index) {
            this.mIndex = index;
        }

        public void reload() {
            this.mLoadingState = RefocusView.this.mModel.getLoadingState(this.mIndex);
            setScreenNail(RefocusView.this.mModel.getScreenNail(this.mIndex));
            updateSize();
        }

        public Size getSize() {
            return this.mSize;
        }

        public void draw(GLCanvas canvas, Rect r) {
            if (this.mScreenNail == null) {
                if (this.mIndex >= RefocusView.this.mPrevBound && this.mIndex <= RefocusView.this.mNextBound) {
                    RefocusView.this.drawPlaceHolder(canvas, r);
                }
                return;
            }
            int w = RefocusView.this.getWidth();
            int h = RefocusView.this.getHeight();
            if (r.left >= w || r.right <= 0 || r.top >= h || r.bottom <= 0) {
                this.mScreenNail.noDraw();
                return;
            }
            boolean wantsCardEffect;
            float filmRatio = RefocusView.this.mPositionController.getFilmRatio();
            if (this.mIndex > 0) {
                wantsCardEffect = filmRatio != WMElement.CAMERASIZEVALUE1B1;
            } else {
                wantsCardEffect = false;
            }
            int cx = wantsCardEffect ? (int) (RefocusView.interpolate(filmRatio, ((float) w) / 2.0f, (float) r.centerX()) + 0.5f) : r.centerX();
            int cy = r.centerY();
            canvas.save(3);
            canvas.translate((float) cx, (float) cy);
            if (wantsCardEffect) {
                GalleryLog.printDFXLog("refocusView wantsCardEffect");
                float progress = Utils.clamp(((float) ((w / 2) - r.centerX())) / ((float) w), (float) GroundOverlayOptions.NO_DIMENSION, (float) WMElement.CAMERASIZEVALUE1B1);
                float alpha = RefocusView.this.getScrollAlpha(progress);
                float scale = RefocusView.this.getScrollScale(progress);
                alpha = RefocusView.interpolate(filmRatio, alpha, WMElement.CAMERASIZEVALUE1B1);
                scale = RefocusView.interpolate(filmRatio, scale, WMElement.CAMERASIZEVALUE1B1);
                canvas.multiplyAlpha(alpha);
                canvas.scale(scale, scale, WMElement.CAMERASIZEVALUE1B1);
            }
            if (this.mRotation != 0) {
                canvas.rotate((float) this.mRotation, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
            }
            int drawW = RefocusView.getRotated(this.mRotation, r.width(), r.height());
            int drawH = RefocusView.getRotated(this.mRotation, r.height(), r.width());
            this.mScreenNail.draw(canvas, (-drawW) / 2, (-drawH) / 2, drawW, drawH);
            if (isScreenNailAnimating()) {
                RefocusView.this.invalidate();
            }
            if (this.mLoadingState == 2) {
                RefocusView.this.drawLoadingFailMessage(canvas);
            }
        }

        private boolean isScreenNailAnimating() {
            return this.mScreenNail instanceof TiledScreenNail ? ((TiledScreenNail) this.mScreenNail).isAnimating() : false;
        }

        public void setScreenNail(ScreenNail s) {
            this.mScreenNail = s;
        }

        private void updateSize() {
            this.mRotation = RefocusView.this.mModel.getImageRotation(this.mIndex);
            if (this.mScreenNail != null) {
                this.mSize.width = this.mScreenNail.getWidth();
                this.mSize.height = this.mScreenNail.getHeight();
            } else {
                RefocusView.this.mModel.getImageSize(this.mIndex, this.mSize);
            }
            GalleryLog.printDFXLog("updateSize for refocusView");
            int w = this.mSize.width;
            int h = this.mSize.height;
            this.mSize.width = RefocusView.getRotated(this.mRotation, w, h);
            this.mSize.height = RefocusView.getRotated(this.mRotation, h, w);
        }
    }

    public static abstract class SlotAnimation extends Animation {
        public abstract void apply(GLCanvas gLCanvas, Rect rect);

        public SlotAnimation() {
            setInterpolator(new DecelerateInterpolator(4.0f));
            setDuration(AbsAlbumPage.LAUNCH_QUIK_ACTIVITY);
        }
    }

    private static class ZInterpolator {
        private float focalLength;

        public ZInterpolator(float foc) {
            this.focalLength = foc;
        }

        public float getInterpolation(float input) {
            return (WMElement.CAMERASIZEVALUE1B1 - (this.focalLength / (this.focalLength + input))) / (WMElement.CAMERASIZEVALUE1B1 - (this.focalLength / (this.focalLength + WMElement.CAMERASIZEVALUE1B1)));
        }
    }

    public RefocusView(GalleryContext activity, GLRoot glRoot) {
        TileImageView supportDisplayEngineTileImageView;
        if (DisplayEngineUtils.isDisplayEngineEnable()) {
            supportDisplayEngineTileImageView = new SupportDisplayEngineTileImageView(activity);
        } else {
            supportDisplayEngineTileImageView = new TileImageView(activity);
        }
        this.mTileView = supportDisplayEngineTileImageView;
        addComponent(this.mTileView);
        this.mContext = activity.getAndroidContext();
        this.mPlaceholderColor = this.mContext.getResources().getColor(R.color.photo_placeholder);
        this.mNoThumbTexture = new ResourceTexture(this.mContext, R.drawable.pic_gallery_album_empty_photo);
        this.mLoadingSpinner = new ProgressSpinner(activity.getGalleryApplication().getAndroidContext());
        setGLRoot(glRoot);
        this.mGestureListener = new MyGestureListener();
        this.mGestureRecognizer = new GestureRecognizer(this.mContext, this.mGestureListener);
        this.mPositionController = new RefocusPositionController(this.mContext, new com.huawei.gallery.refocus.app.RefocusPositionController.Listener() {
            public void invalidate() {
                RefocusView.this.invalidate();
                if (RefocusView.this.mListener != null && RefocusView.this.mListener.isRangeMeasureMode()) {
                    RefocusView.this.mHandler.sendEmptyMessage(3);
                }
            }

            public boolean isHoldingDown() {
                return (RefocusView.this.mHolding & 1) != 0;
            }

            public boolean isHoldingDelete() {
                return (RefocusView.this.mHolding & 4) != 0;
            }
        });
        for (int i = -3; i <= 3; i++) {
            if (i == 0) {
                this.mPictures.put(i, new FullPicture());
            } else {
                this.mPictures.put(i, new ScreenNailPicture(i));
            }
        }
        this.mClickScaleTransition = new ClickScaleTransition();
    }

    public void setGLRoot(GLRoot glRoot) {
        if (glRoot != null) {
            this.mHandler = new MyHandler(glRoot);
        }
    }

    public void setModel(Model model) {
        this.mModel = model;
        this.mTileView.setModel(this.mModel);
    }

    public void notifyImageChange(int index) {
        boolean hasDirectShowNail = false;
        if (index == 0) {
            hasDirectShowNail = this.mTileView.hasDirectShowNail();
        }
        ((Picture) this.mPictures.get(index)).reload();
        setPictureSize(index);
        if (hasDirectShowNail) {
            this.mPositionController.skipAnimation();
        }
        invalidate();
    }

    private void setPictureSize(int index) {
        this.mPositionController.setImageSize(index, ((Picture) this.mPictures.get(index)).getSize(), null);
    }

    protected void onLayout(boolean changeSize, int left, int top, int right, int bottom) {
        this.mTileView.layout(0, 0, right - left, bottom - top);
        GLRoot root = getGLRoot();
        int displayRotation = root.getDisplayRotation();
        int compensation = root.getCompensation();
        if (!(this.mDisplayRotation == displayRotation && this.mCompensation == compensation)) {
            this.mDisplayRotation = displayRotation;
            this.mCompensation = compensation;
        }
        if (changeSize) {
            this.mPositionController.setViewSize(getWidth(), getHeight());
        }
    }

    private void drawPlaceHolder(GLCanvas canvas, Rect r) {
        canvas.fillRect((float) r.left, (float) r.top, (float) r.width(), (float) r.height(), this.mPlaceholderColor);
    }

    private void drawLoadingFailMessage(GLCanvas canvas) {
        ResourceTexture m = this.mNoThumbTexture;
        m.draw(canvas, (-m.getWidth()) / 2, (-m.getHeight()) / 2);
    }

    private static int getRotated(int degree, int original, int theother) {
        return degree % 180 == 0 ? original : theother;
    }

    protected boolean onTouch(MotionEvent event) {
        if (this.mListener == null || !this.mListener.onTouch(event)) {
            if (!this.mNeedHandleActionUp) {
                this.mGestureRecognizer.onTouchEvent(event);
            }
            return true;
        }
        if ((event.getActionMasked() & 255) == 0) {
            this.mNeedHandleActionUp = true;
        } else if ((event.getActionMasked() & 255) == 1) {
            this.mNeedHandleActionUp = false;
        }
        return true;
    }

    public void setScaleEnable(boolean enabled) {
        this.mGestureListener.setScaleEnable(enabled);
    }

    public void setScrollEnable(boolean enabled) {
        this.mGestureListener.setScrollEnable(enabled);
    }

    public void pause() {
        this.mPositionController.skipAnimation();
        this.mTileView.freeTextures();
        for (int i = -3; i <= 3; i++) {
            ((Picture) this.mPictures.get(i)).setScreenNail(null);
        }
    }

    private void setDisableGesture() {
        if (this.mListener == null || !this.mListener.isRangeMeasureMode()) {
            setScrollEnable(false);
            setScaleEnable(false);
        }
    }

    public void resume() {
        this.mTileView.prepareTextures();
        this.mPositionController.skipToFinalPosition();
        setDisableGesture();
    }

    public void destroy() {
        AbsPhotoView.Model model = this.mModel;
        if (model != null) {
            ScreenNailCommonDisplayEnginePool screenNailCommonDisplayEnginePool = model.getScreenNailCommonDisplayEnginePool();
            if (screenNailCommonDisplayEnginePool != null) {
                screenNailCommonDisplayEnginePool.clear();
            }
        }
    }

    protected void render(GLCanvas canvas) {
        int neighbors;
        boolean inPageMode = this.mPositionController.getFilmRatio() == 0.0f;
        boolean inCaptureAnimation = (this.mHolding & 2) != 0;
        if (!inPageMode || inCaptureAnimation) {
            neighbors = 3;
        } else {
            neighbors = 1;
        }
        boolean isActive = false;
        if (this.mAnimation != null) {
            isActive = this.mAnimation.isActive();
        }
        this.mRect = this.mPositionController.getPosition(0);
        this.mPicture = (Picture) this.mPictures.get(0);
        if (this.mAnimation == null || !r3) {
            for (int i = neighbors; i >= (-neighbors); i--) {
                ((Picture) this.mPictures.get(i)).draw(canvas, this.mPositionController.getPosition(i));
            }
        } else {
            canvas.save(3);
            this.mAnimation.apply(canvas, this.mRect);
            this.mPicture.draw(canvas, this.mRect);
            canvas.restore();
        }
        this.mPositionController.advanceAnimation();
    }

    private static float calculateMoveOutProgress(int left, int right, int viewWidth) {
        int w = right - left;
        GalleryLog.printDFXLog("RefocusView");
        if (w < viewWidth) {
            int zx = (viewWidth / 2) - (w / 2);
            if (left > zx) {
                return ((float) (-(left - zx))) / ((float) (viewWidth - zx));
            }
            return ((float) (left - zx)) / ((float) ((-w) - zx));
        }
        GalleryLog.printDFXLog("RefocusView");
        if (left > 0) {
            return ((float) (-left)) / ((float) viewWidth);
        }
        if (right < viewWidth) {
            return ((float) (viewWidth - right)) / ((float) viewWidth);
        }
        return 0.0f;
    }

    private float getScrollAlpha(float scrollProgress) {
        return scrollProgress < 0.0f ? this.mAlphaInterpolator.getInterpolation(WMElement.CAMERASIZEVALUE1B1 - Math.abs(scrollProgress)) : WMElement.CAMERASIZEVALUE1B1;
    }

    private float getScrollScale(float scrollProgress) {
        float interpolatedProgress = this.mScaleInterpolator.getInterpolation(Math.abs(scrollProgress));
        return (WMElement.CAMERASIZEVALUE1B1 - interpolatedProgress) + (TRANSITION_SCALE_FACTOR * interpolatedProgress);
    }

    private static float interpolate(float ratio, float from, float to) {
        return (((to - from) * ratio) * ratio) + from;
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public void setMediaItemScreenNail(MediaItem mediaItem) {
        this.mTileView.setDirectShowNail(new MediaItemDirectShowNail(mediaItem));
    }

    public Point getTouchPositionInImage(Point touchPosion) {
        float imageScale = this.mPositionController.getImageScale();
        int imageWidth = this.mPositionController.getImageWidth();
        int imageHeight = this.mPositionController.getImageHeight();
        Point centerPoint = new Point();
        this.mTileView.getImageCenter(centerPoint);
        centerPoint.y = (this.mListener != null ? this.mListener.getActionBarHeight() : 0) + centerPoint.y;
        Point newPosition = new Point((int) ((((((float) touchPosion.x) + 0.5f) - ((float) centerPoint.x)) / imageScale) + (((float) imageWidth) / 2.0f)), (int) ((((((float) touchPosion.y) + 0.5f) - ((float) centerPoint.y)) / imageScale) + (((float) imageHeight) / 2.0f)));
        GalleryLog.i("RefocusView", "centerPoint: (" + centerPoint.x + "," + centerPoint.y + ")");
        GalleryLog.i("RefocusView", "touch position: (" + touchPosion.x + "," + touchPosion.y + ")");
        GalleryLog.i("RefocusView", "position in image: (" + newPosition.x + "," + newPosition.y + ")");
        return newPosition;
    }

    public Point transformToScreenCoordinate(Point focusPoint) {
        float imageScale = this.mPositionController.getImageScale();
        int imageWidth = this.mPositionController.getImageWidth();
        int imageHeight = this.mPositionController.getImageHeight();
        float newX = GroundOverlayOptions.NO_DIMENSION;
        float newY = GroundOverlayOptions.NO_DIMENSION;
        Point centerPoint = new Point();
        this.mTileView.getImageCenter(centerPoint);
        centerPoint.y = (this.mListener != null ? this.mListener.getActionBarHeight() : 0) + centerPoint.y;
        if (!(focusPoint.x == -1 || focusPoint.y == -1)) {
            newX = (((((float) focusPoint.x) - (((float) imageWidth) / 2.0f)) * imageScale) + ((float) centerPoint.x)) - 0.5f;
            newY = (((((float) focusPoint.y) - (((float) imageHeight) / 2.0f)) * imageScale) + ((float) centerPoint.y)) - 0.5f;
        }
        Point newPosition = new Point((int) newX, (int) newY);
        GalleryLog.i("RefocusView", "centerPoint: (" + centerPoint.x + "," + centerPoint.y + ")");
        GalleryLog.i("RefocusView", "focus position in Image: (" + focusPoint.x + "," + focusPoint.y + ")");
        GalleryLog.i("RefocusView", "position on screen: (" + newPosition.x + "," + newPosition.y + ")");
        return newPosition;
    }

    public void refresh() {
        this.mTileView.notifyModelInvalidated();
    }

    public void onRefocusRegionDecode() {
        this.mListener.onDecodeImageComplete();
    }

    public Point getRealPositionInImage(Point touchPosition) {
        int imageWidth = this.mPositionController.getImageWidth();
        int imageHeight = this.mPositionController.getImageHeight();
        Point position = getTouchPositionInImage(touchPosition);
        if (position.x < 0 || position.y < 0 || position.x > imageWidth || position.y > imageHeight) {
            return new Point(-1, -1);
        }
        return position;
    }
}

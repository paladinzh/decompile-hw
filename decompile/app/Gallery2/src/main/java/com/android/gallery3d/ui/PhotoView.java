package com.android.gallery3d.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build.VERSION;
import android.os.Message;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.android.gallery3d.R;
import com.android.gallery3d.anim.FloatAnimation;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.IVideo;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.AbsPhotoView.Listener;
import com.android.gallery3d.ui.AbsPhotoView.Model;
import com.android.gallery3d.ui.AbsPhotoView.Size;
import com.android.gallery3d.ui.MenuExecutor.ExtraActionListener;
import com.android.gallery3d.ui.PhotoMagnifierManager.AnimationType;
import com.android.gallery3d.ui.PhotoMagnifierManager.MagnifierAnimation;
import com.android.gallery3d.ui.PhotoMagnifierManager.PhotoMagnifierModeListener;
import com.android.gallery3d.ui.PhotoMagnifierManager.PhotoMagnifierState;
import com.android.gallery3d.ui.TileImageView.DirectShowNail;
import com.android.gallery3d.util.DisplayEngineUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MultiWindowStatusHolder;
import com.android.gallery3d.util.RangeArray;
import com.android.gallery3d.util.ReportToBigData;
import com.android.gallery3d.util.TraceController;
import com.autonavi.amap.mapcore.MapConfig;
import com.huawei.gallery.anim.PhotoFallbackEffect;
import com.huawei.gallery.animation.DeletePhotoAnimationFactor;
import com.huawei.gallery.displayengine.BoostFullScreenNailDisplay;
import com.huawei.gallery.displayengine.ScreenNailCommonDisplayEnginePool;
import com.huawei.gallery.freeshare.FreeShareAdapter;
import com.huawei.gallery.util.UIUtils;
import com.huawei.watermark.manager.parse.WMElement;
import com.huawei.watermark.ui.WMComponent;

public class PhotoView extends AbsPhotoView {
    private AccelerateInterpolator mAlphaInterpolator = new AccelerateInterpolator(0.9f);
    private boolean mAutoSlideMode;
    private int mBackgroundColor;
    private Rect mCameraRect = new Rect();
    private Rect mCameraRelativeFrame = new Rect();
    private ScreenNail mCameraScreenNail;
    private boolean mCancelExtraScalingPending;
    private int mCompensation = 0;
    private Context mContext;
    private int mDisplayRotation = 0;
    private int mDoneRubberBandEffectEdge = 0;
    private long mEventCounter;
    private ExtraActionListener mExtraActionListener;
    private boolean mFilmMode = false;
    private boolean mFilmModeAllowed = true;
    private final MyGestureListener mGestureListener;
    private final GestureRecognizer mGestureRecognizer;
    private GifParseThread mGifThread;
    private SynchronizedHandler mHandler;
    private int mHolding;
    private Listener mListener;
    private ProgressSpinner mLoadingSpinner;
    private boolean mLockPhotoMagnifier = false;
    private Model mModel;
    private int mNeedRubberBandEffectEdge = 0;
    private boolean mNeedUnlockSwipeByDeletePhoto;
    private int mNextBound;
    private ResourceTexture mNoThumbTexture;
    private PhotoMagnifierView mPhotoMagnifier;
    private MotionEvent mPhotoMagnifierEvent;
    private PhotoMagnifierManager mPhotoMagnifierManager;
    private PhotoMagnifierModeListener mPhotoMagnifierModeListener;
    private final Runnable mPhotoMagnifierMoveTask = new Runnable() {
        public void run() {
            MotionEvent event = PhotoView.this.mPhotoMagnifierEvent;
            if (event != null) {
                float pressure = event.getPressure();
                if (PhotoView.this.mPhotoMagnifierManager.inMagnifierMode()) {
                    FloatAnimation animation = PhotoView.this.mPhotoMagnifierManager.getAnimation();
                    if (animation != null && animation.isAnimating()) {
                        PhotoView.this.mPhotoMagnifier.setScale(PhotoView.this.getMagnifierScaleForAnimation(pressure, animation.get()));
                    } else if (Float.compare(Math.abs(PhotoView.this.mPrePressure - pressure), 0.007f) > 0 || Float.compare(PhotoView.this.mPreTouchX, event.getX()) != 0 || Float.compare(PhotoView.this.mPreTouchY, event.getY()) != 0) {
                        PhotoView.this.mPrePressure = pressure;
                        PhotoView.this.mPreTouchX = event.getX();
                        PhotoView.this.mPreTouchY = event.getY();
                        PhotoView.this.mPhotoMagnifier.setScale(PhotoView.this.getMagnifierScale(pressure));
                    } else {
                        return;
                    }
                    PhotoView.this.mPhotoMagnifier.draw(event.getX(), event.getY());
                } else if (PhotoView.this.supportPhotoMagnifier(pressure)) {
                    PhotoView.this.enterMagnifierMode(event);
                }
            }
        }
    };
    private Runnable mPhotoMagnifierPendingHide;
    private final RangeArray<Picture> mPictures = new RangeArray(-3, 3);
    private final int mPlaceholderColor;
    private final PositionController mPositionController;
    private float mPrePressure;
    private float mPreTouchX;
    private float mPreTouchY;
    private float mPressureValueThreshold;
    private int mPrevBound;
    private ZInterpolator mScaleInterpolator = new ZInterpolator(0.5f);
    private SimpleGestureListener mSimpleGestureListner;
    private Size[] mSizes = new Size[7];
    private boolean mSlideToNext = true;
    private TileImageView mTileView;
    private boolean mTouchBoxDeletable;
    private int mTouchBoxIndex = Integer.MAX_VALUE;
    private int mUndoBarState;
    private int mUndoIndexHint = Integer.MAX_VALUE;
    private Texture mVideoPlayIcon;
    private boolean mWantPictureCenterCallbacks = false;
    private boolean mWantPictureFullViewCallbacks = false;

    abstract class AbstractDirectShowNail implements DirectShowNail {
        protected boolean mFromCache;
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
            int imageWidth = this.mImageWidth;
            int imageHeight = this.mImageHeight;
            if (imageWidth > 0 && imageHeight > 0) {
                if (90 == this.mRotation || 270 == this.mRotation) {
                    imageWidth = this.mImageHeight;
                    imageHeight = this.mImageWidth;
                }
                float scale = PositionController.getMinimalScale(imageWidth, imageHeight, PhotoView.this.mTileView.getWidth(), PhotoView.this.mTileView.getHeight());
                float renderWidth = ((float) this.mImageWidth) * scale;
                float renderHeight = ((float) this.mImageHeight) * scale;
                int offsetX = Math.round((((float) PhotoView.this.mTileView.getWidth()) - renderWidth) / 2.0f);
                int offsetY = Math.round((((float) PhotoView.this.mTileView.getHeight()) - renderHeight) / 2.0f);
                if (this.mScreenNail == null) {
                    canvas.fillRect((float) offsetX, (float) offsetY, (float) Math.round(renderWidth), (float) Math.round(renderHeight), TiledScreenNail.getPlaceholderColor());
                } else {
                    this.mScreenNail.draw(canvas, offsetX, offsetY, Math.round(renderWidth), Math.round(renderHeight));
                }
            }
        }

        public void recycle() {
        }
    }

    private abstract class Picture {
        private Rect mFullPageRect;
        protected int mIndex;
        protected MediaItem mItem;
        protected Rect mMaskRect;
        protected int mRotation;
        protected Rect mViewRect;

        public abstract void draw(GLCanvas gLCanvas, Rect rect, boolean z, boolean z2);

        public abstract void forceSize();

        public abstract Size getSize();

        public abstract boolean isCamera();

        public abstract boolean isDeletable();

        public abstract boolean isVideo();

        public abstract void reload();

        public abstract void setScreenNail(ScreenNail screenNail);

        private Picture() {
            this.mViewRect = new Rect();
            this.mFullPageRect = new Rect();
            this.mMaskRect = new Rect();
        }

        public MediaItem getMediaItem() {
            return this.mItem;
        }

        public void calculateMaskArgs(Rect viewRect, Rect targetRect, boolean pageMode) {
            if (pageMode) {
                this.mViewRect.set(viewRect);
                this.mFullPageRect.set(targetRect);
                calculateMaskRect();
            }
        }

        int calculateRectOffset() {
            if (Math.abs(this.mIndex) != 1) {
                return 0;
            }
            int currentMaskWidth = this.mMaskRect.right;
            int fullPageMaskWidth = ((Picture) PhotoView.this.mPictures.get(0)).mMaskRect.right;
            if (currentMaskWidth == 0 && fullPageMaskWidth == 0) {
                return 0;
            }
            int currentGap = currentMaskWidth + fullPageMaskWidth;
            if (this.mIndex == 1) {
                return -currentGap;
            }
            if (this.mIndex == -1) {
                return currentGap;
            }
            return 0;
        }

        private void calculateMaskRect() {
            this.mMaskRect.left = Integer.MAX_VALUE;
            this.mMaskRect.right = 0;
            boolean isFillShortScale = PhotoView.this.mPositionController.isFillShortScale();
            if (this.mIndex != 0 || isFillShortScale) {
                int left = this.mViewRect.left;
                int top = this.mViewRect.top;
                int right = this.mViewRect.right;
                int width = this.mViewRect.width();
                int height = this.mViewRect.height();
                float maxMaskWidth = ((float) width) * 0.125f;
                int viewWidth = PhotoView.this.getWidth();
                int maskWidth;
                if (left < this.mFullPageRect.left && left < 0) {
                    maskWidth = (int) Math.min(maxMaskWidth, Math.abs((((float) left) * maxMaskWidth) / ((float) width)));
                    this.mMaskRect.set(right - maskWidth, top, maskWidth, height);
                } else if (left < this.mFullPageRect.left && right > viewWidth) {
                    this.mMaskRect.set(left, top, (int) Math.max(0.0f, maxMaskWidth - Math.abs((((float) (viewWidth - left)) * maxMaskWidth) / ((float) width))), height);
                } else if (left > this.mFullPageRect.left && right > viewWidth) {
                    this.mMaskRect.set(left, top, (int) Math.min(maxMaskWidth, Math.abs((((float) (right - viewWidth)) * maxMaskWidth) / ((float) width))), height);
                } else if (left > this.mFullPageRect.left && left < 0) {
                    maskWidth = (int) Math.max(0.0f, maxMaskWidth - Math.abs((((float) right) * maxMaskWidth) / ((float) width)));
                    this.mMaskRect.set(right - maskWidth, top, maskWidth, height);
                }
            }
        }
    }

    class FullPicture extends Picture {
        private boolean mIsCamera;
        private boolean mIsDeletable;
        private boolean mIsPanorama;
        private boolean mIsStaticCamera;
        private boolean mIsVideo;
        private int mLoadingState;
        private int mRotation;
        private Size mSize;

        public FullPicture() {
            super();
            this.mLoadingState = 0;
            this.mSize = new Size();
            this.mIndex = 0;
            PhotoView.this.mLoadingSpinner.startAnimation();
        }

        public void reload() {
            TraceController.traceBegin("PhotoView.FullPicture.reload");
            PhotoView.this.mTileView.notifyModelInvalidated();
            this.mIsCamera = PhotoView.this.mModel.isCamera(0);
            this.mIsPanorama = PhotoView.this.mModel.isPanorama(0);
            this.mIsStaticCamera = PhotoView.this.mModel.isStaticCamera(0);
            this.mIsVideo = PhotoView.this.mModel.isVideo(0);
            this.mIsDeletable = false;
            this.mItem = PhotoView.this.mModel.getMediaItem(0);
            int lastState = this.mLoadingState;
            this.mLoadingState = PhotoView.this.mModel.getLoadingState(0);
            if (lastState != this.mLoadingState) {
                if (this.mLoadingState == 0) {
                    PhotoView.this.mLoadingSpinner.startAnimation();
                }
                if (PhotoView.this.mListener != null) {
                    PhotoView.this.mListener.onLoadStateChange(this.mLoadingState);
                }
            }
            if (this.mIsCamera && PhotoView.this.mModel.getScreenNail(0) == null) {
                GalleryLog.e("PhotoView", "ScreenNail lost @ FullPicture");
                setScreenNail(PhotoView.this.mCameraScreenNail);
                PhotoView.this.mTileView.updateScreenNailSize(PhotoView.this.getWidth(), PhotoView.this.getHeight());
            } else {
                setScreenNail(PhotoView.this.mModel.getScreenNail(0));
            }
            PhotoView.this.parseGifIfNecessary(PhotoView.this.mModel.getMediaItem(0), PhotoView.this.mModel.getScreenNail(0));
            updateSize();
            TraceController.traceEnd();
        }

        public Size getSize() {
            return this.mSize;
        }

        public void forceSize() {
            updateSize();
            PhotoView.this.mPositionController.forceImageSize(0, this.mSize);
        }

        private void updateSize() {
            if (this.mIsPanorama) {
                this.mRotation = PhotoView.this.getPanoramaRotation();
            } else if (!this.mIsCamera || this.mIsStaticCamera) {
                this.mRotation = PhotoView.this.mModel.getImageRotation(0);
            } else {
                this.mRotation = PhotoView.this.getCameraRotation();
            }
            int w = PhotoView.this.mTileView.mImageWidth;
            int h = PhotoView.this.mTileView.mImageHeight;
            this.mSize.width = PhotoView.getRotated(this.mRotation, w, h);
            this.mSize.height = PhotoView.getRotated(this.mRotation, h, w);
        }

        private void drawMask(GLCanvas canvas) {
            if (this.mMaskRect.right != 0) {
                if (this.mMaskRect.left == this.mViewRect.left) {
                    canvas.fillRect((float) (this.mMaskRect.left - 3), (float) (this.mMaskRect.top - 3), (float) (this.mMaskRect.right + 3), (float) (this.mMaskRect.bottom + 3), PhotoView.this.mBackgroundColor);
                } else {
                    canvas.fillRect((float) this.mMaskRect.left, (float) this.mMaskRect.top, (float) (this.mMaskRect.right + 3), (float) (this.mMaskRect.bottom + 3), PhotoView.this.mBackgroundColor);
                }
            }
        }

        public void draw(GLCanvas canvas, Rect r, boolean pageMode, boolean isFillShortScale) {
            boolean z = false;
            TraceController.traceBegin("PhotoView.FullPicture.draw");
            drawTileView(canvas, r);
            if (!PhotoView.this.mAutoSlideMode && pageMode && isFillShortScale) {
                drawMask(canvas);
            }
            if (PhotoView.this.mListener != null) {
                Listener -get9 = PhotoView.this.mListener;
                float centerX = (float) r.centerX();
                float centerY = (float) r.centerY();
                int i = this.mIndex;
                if (pageMode) {
                    z = isFillShortScale;
                }
                -get9.onPhotoTranslationChange(centerX, centerY, i, z, PhotoView.this.mModel.getMediaItem(this.mIndex));
            }
            if ((PhotoView.this.mHolding & -2) == 0) {
                if (PhotoView.this.mWantPictureCenterCallbacks && PhotoView.this.mPositionController.isCenter() && PhotoView.this.mPositionController.isFillShortScale() && !PhotoView.this.mPositionController.isScrolling()) {
                    PhotoView.this.mListener.onPictureCenter(this.mIsCamera);
                }
                if (PhotoView.this.mWantPictureFullViewCallbacks && PhotoView.this.mPositionController.isCenter() && PhotoView.this.mPositionController.isFillShortScale()) {
                    PhotoView.this.mListener.onPictureFullView();
                }
                TraceController.traceEnd();
            }
        }

        public void setScreenNail(ScreenNail s) {
            TraceController.traceBegin("PhotoView.FullPicture.setScreenNail ScreenNail=" + s);
            boolean needToAce = true;
            if (PhotoView.this.mModel != null) {
                MediaItem item = PhotoView.this.mModel.getMediaItem(0);
                if (item != null && "image/gif".equals(item.getMimeType())) {
                    needToAce = false;
                }
            } else {
                needToAce = false;
            }
            if (s == null) {
                needToAce = false;
                this.mLoadingState = 0;
            }
            PhotoView.this.mTileView.setScreenNail(s, needToAce);
            TraceController.traceEnd();
        }

        public boolean isCamera() {
            return this.mIsCamera;
        }

        public boolean isDeletable() {
            return this.mIsDeletable;
        }

        public boolean isVideo() {
            return this.mIsVideo;
        }

        private boolean wantsCardEffect() {
            return PhotoView.this.mPositionController.getFilmRatio() != WMElement.CAMERASIZEVALUE1B1 ? PhotoView.this.mAutoSlideMode : false;
        }

        private void drawTileView(GLCanvas canvas, Rect r) {
            TraceController.traceBegin("PhotoView.FullPicture.drawTileView");
            float imageScale = PhotoView.this.mPositionController.getImageScale();
            int viewW = PhotoView.this.getWidth();
            int viewH = PhotoView.this.getHeight();
            float cx = r.exactCenterX();
            float cy = r.exactCenterY();
            float scale = WMElement.CAMERASIZEVALUE1B1;
            canvas.save(3);
            float filmRatio = PhotoView.this.mPositionController.getFilmRatio();
            boolean wantsCardEffect = wantsCardEffect();
            boolean wantsOffsetEffect = (this.mIsDeletable && filmRatio == WMElement.CAMERASIZEVALUE1B1) ? r.centerY() != viewH / 2 : false;
            if (wantsCardEffect) {
                int left = r.left;
                int right = r.right;
                float progress = Utils.clamp(PhotoView.calculateMoveOutProgress(left, right, viewW), (float) GroundOverlayOptions.NO_DIMENSION, WMElement.CAMERASIZEVALUE1B1);
                if (progress < 0.0f || PhotoView.this.mAutoSlideMode) {
                    float cxPage;
                    scale = PhotoView.this.getScrollScale(progress);
                    float alpha = PhotoView.this.getScrollAlpha(progress);
                    scale = PhotoView.interpolate(filmRatio, scale, WMElement.CAMERASIZEVALUE1B1);
                    alpha = PhotoView.interpolate(filmRatio, alpha, WMElement.CAMERASIZEVALUE1B1);
                    if (PhotoView.this.mAutoSlideMode) {
                        alpha = DeletePhotoAnimationFactor.getCurrentContentAlpha(progress);
                        scale = WMElement.CAMERASIZEVALUE1B1;
                    }
                    imageScale *= scale;
                    canvas.multiplyAlpha(alpha);
                    if (right - left <= viewW) {
                        cxPage = ((float) viewW) / 2.0f;
                    } else {
                        cxPage = (((float) (right - left)) * scale) / 2.0f;
                    }
                    cx = PhotoView.interpolate(filmRatio, cxPage, cx);
                }
            } else if (wantsOffsetEffect) {
                canvas.multiplyAlpha(PhotoView.this.getOffsetAlpha(((float) (r.centerY() - (viewH / 2))) / ((float) viewH)));
            }
            setTileViewPosition(cx, cy, viewW, viewH, imageScale);
            if (PhotoView.this.mListener == null || !PhotoView.this.mListener.calledToSimpleEditor()) {
                PhotoView.this.renderChild(canvas, PhotoView.this.mTileView);
            }
            canvas.translate((float) ((int) (0.5f + cx)), (float) ((int) (0.5f + cy)));
            int s = (int) ((((float) Math.min(r.width(), r.height())) * scale) + 0.5f);
            if (this.mIsVideo) {
                PhotoView.this.drawVideoPlayIcon(canvas, s);
            }
            if (this.mLoadingState == 0 && PhotoView.this.mListener != null && PhotoView.this.mListener.calledToSimpleEditor()) {
                PhotoView.this.mLoadingSpinner.draw(canvas);
                PhotoView.this.invalidate();
            } else if (this.mLoadingState == 2 && (!this.mIsCamera || PhotoView.this.mCameraScreenNail == null)) {
                PhotoView.this.drawLoadingFailMessage(canvas, this.mIsVideo);
            }
            canvas.restore();
            TraceController.traceEnd();
        }

        private void setTileViewPosition(float cx, float cy, int viewW, int viewH, float scale) {
            float x;
            float y;
            int imageW = PhotoView.this.mPositionController.getImageWidth();
            int imageH = PhotoView.this.mPositionController.getImageHeight();
            float centerX = (((float) imageW) / 2.0f) + (((((float) viewW) / 2.0f) - cx) / scale);
            float centerY = (((float) imageH) / 2.0f) + (((((float) viewH) / 2.0f) - cy) / scale);
            float inverseX = ((float) imageW) - centerX;
            float inverseY = ((float) imageH) - centerY;
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
            PhotoView.this.mTileView.setPosition(x, y, scale, this.mRotation);
        }
    }

    class MediaItemDirectShowNail extends AbstractDirectShowNail {
        private MediaItem mItem;
        private long mStartClickFromCameraTime;
        final /* synthetic */ PhotoView this$0;

        public MediaItemDirectShowNail(PhotoView this$0, MediaItem item) {
            boolean z = true;
            this.this$0 = this$0;
            super();
            this.mFromCache = false;
            boolean fileSaveComplete = true;
            TraceController.traceBegin("getScreenNailBitmap");
            Bitmap bmp = item.getScreenNailBitmap(1);
            TraceController.traceEnd();
            if (bmp == null) {
                TraceController.traceBegin("getLatestCacheImage");
                bmp = item.getLatestCacheImage();
                if (bmp == null) {
                    z = false;
                }
                this.mFromCache = z;
                fileSaveComplete = item.getSize() > 0;
                TraceController.traceEnd();
            }
            if (bmp != null) {
                GalleryLog.d("PhotoView", "MediaItemDirectShowNail from camera cache:" + this.mFromCache + ", fileSaveComplete:" + fileSaveComplete);
                this.mScreenNail = new TiledScreenNail(bmp, this.mFromCache, fileSaveComplete);
            }
            this.mItem = item;
            init();
        }

        protected int getImageWidth() {
            if (!(this.mItem instanceof IVideo)) {
                int width = this.mItem.getWidth();
                if (this.mScreenNail != null) {
                    width = this.mScreenNail.getWidth();
                }
                return width;
            } else if (this.mScreenNail == null) {
                return 0;
            } else {
                return this.mScreenNail.getWidth();
            }
        }

        protected int getImageHeight() {
            if (!(this.mItem instanceof IVideo)) {
                int height = this.mItem.getHeight();
                if (this.mScreenNail != null) {
                    height = this.mScreenNail.getHeight();
                }
                return height;
            } else if (this.mScreenNail == null) {
                return 0;
            } else {
                return this.mScreenNail.getHeight();
            }
        }

        public int getRotation() {
            return this.mFromCache ? 0 : this.mItem.getRotation();
        }

        public void recycle() {
            if (this.mScreenNail != null) {
                if (this.mStartClickFromCameraTime != 0) {
                    GalleryLog.d("PhotoView", "[HDTEST] Shot2Review=" + (System.currentTimeMillis() - this.mStartClickFromCameraTime) + "ms");
                }
                this.mScreenNail.recycle();
                this.mScreenNail = null;
            }
        }

        public void setStartClickFromCameraTime(long startClickFromCameraTime) {
            this.mStartClickFromCameraTime = startClickFromCameraTime;
        }
    }

    private class MyGestureListener implements GestureRecognizer.Listener {
        private float mAccScale;
        private boolean mCanChangeMode;
        private boolean mDownInScrolling;
        private boolean mFirstScrollX;
        private boolean mHadFling;
        private boolean mIgnoreScalingGesture;
        private boolean mIgnoreSwipingGesture;
        private boolean mIgnoreUpEvent;
        private boolean mModeChanged;
        private boolean mScrolledAfterDown;

        public void onUp() {
            /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.gallery3d.ui.PhotoView.MyGestureListener.onUp():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 6 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.gallery3d.ui.PhotoView.MyGestureListener.onUp():void");
        }

        private MyGestureListener() {
            this.mIgnoreUpEvent = false;
        }

        public boolean onSingleTapUp(float x, float y) {
            if (VERSION.SDK_INT < 14 && (PhotoView.this.mHolding & 1) == 0) {
                return true;
            }
            if (this.mIgnoreSwipingGesture && !PhotoView.this.isCamera()) {
                return true;
            }
            PhotoView photoView = PhotoView.this;
            photoView.mHolding = photoView.mHolding & -2;
            if (PhotoView.this.mFilmMode && !this.mDownInScrolling) {
                PhotoView.this.switchToHitPicture((int) (x + 0.5f), (int) (y + 0.5f));
                MediaItem item = PhotoView.this.mModel.getMediaItem(0);
                int supported = 0;
                if (item != null) {
                    supported = item.getSupportedOperations();
                }
                if ((32768 & supported) == 0) {
                    PhotoView.this.setFilmMode(false);
                    this.mIgnoreUpEvent = true;
                    return true;
                }
            }
            if (PhotoView.this.mListener != null) {
                Matrix m = PhotoView.this.getGLRoot().getCompensationMatrix();
                Matrix inv = new Matrix();
                m.invert(inv);
                float[] pts = new float[]{x, y};
                inv.mapPoints(pts);
                PhotoView.this.mListener.onSingleTapUp((int) (pts[0] + 0.5f), (int) (pts[1] + 0.5f));
            }
            return true;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean onDoubleTap(float x, float y) {
            if (this.mIgnoreSwipingGesture || !PhotoView.this.mModel.isLCDDownloaded() || PhotoView.this.mPhotoMagnifierManager.inMagnifierMode()) {
                return true;
            }
            if (((Picture) PhotoView.this.mPictures.get(0)).isCamera()) {
                return false;
            }
            PositionController controller = PhotoView.this.mPositionController;
            if (((Picture) PhotoView.this.mPictures.get(0)).isVideo() && controller.isFillShortScale()) {
                return false;
            }
            int i;
            this.mIgnoreUpEvent = true;
            float newScale = controller.getNextDoubleTapScale();
            controller.zoomIn(x, y, newScale);
            PhotoView photoView = PhotoView.this;
            if (newScale > controller.getScaleShort()) {
                i = 10;
            } else {
                i = 0;
            }
            photoView.mNeedRubberBandEffectEdge = i;
            PhotoView.this.mDoneRubberBandEffectEdge = 0;
            return true;
        }

        public boolean onScroll(float dx, float dy, float totalX, float totalY) {
            boolean z = false;
            if (this.mIgnoreSwipingGesture || PhotoView.this.mPhotoMagnifierManager.inMagnifierMode()) {
                return true;
            }
            if (PhotoView.this.mSimpleGestureListner != null && PhotoView.this.mSimpleGestureListner.onScroll(dx, dy, totalX, totalY)) {
                return true;
            }
            if (!this.mScrolledAfterDown) {
                this.mScrolledAfterDown = true;
                if (Math.abs(dx) > Math.abs(dy)) {
                    z = true;
                }
                this.mFirstScrollX = z;
            }
            if (isFreeShareMode()) {
                return true;
            }
            int dxi = (int) ((-dx) + 0.5f);
            int dyi = (int) ((-dy) + 0.5f);
            if (!PhotoView.this.mFilmMode) {
                if (PhotoView.this.mPositionController.isZoomIn()) {
                    PhotoView photoView;
                    int edge = PhotoView.this.mPositionController.getImageAtEdges();
                    if ((edge & 1) == 0) {
                        photoView = PhotoView.this;
                        photoView.mNeedRubberBandEffectEdge = photoView.mNeedRubberBandEffectEdge | 2;
                        photoView = PhotoView.this;
                        photoView.mDoneRubberBandEffectEdge = photoView.mDoneRubberBandEffectEdge & -3;
                    }
                    if ((edge & 2) == 0) {
                        photoView = PhotoView.this;
                        photoView.mNeedRubberBandEffectEdge = photoView.mNeedRubberBandEffectEdge | 8;
                        photoView = PhotoView.this;
                        photoView.mDoneRubberBandEffectEdge = photoView.mDoneRubberBandEffectEdge & -9;
                    }
                    if (!((PhotoView.this.mPositionController.isNeedRubberBandEffect(2) && PhotoView.this.mPositionController.isNeedRubberBandEffect(8)) || PhotoView.this.mListener == null)) {
                        PhotoView.this.mListener.onScroll(dx, dy, totalX, totalY);
                    }
                } else if (PhotoView.this.mListener != null) {
                    PhotoView.this.mListener.onScroll(dx, dy, totalX, totalY);
                }
                PhotoView.this.mPositionController.scrollPage(dxi, dyi);
            } else if (this.mFirstScrollX) {
                PhotoView.this.mPositionController.scrollFilmX(dxi);
            }
            return true;
        }

        public boolean onFling(float velocityX, float velocityY) {
            if (!PhotoView.this.mPhotoMagnifierManager.isEnableFlingAndScale() || doFreeShare(velocityX, velocityY) || this.mIgnoreSwipingGesture || this.mModeChanged) {
                return true;
            }
            if (PhotoView.this.swipeImages(velocityX, velocityY)) {
                this.mIgnoreUpEvent = true;
            } else {
                flingImages(velocityX, velocityY);
            }
            this.mHadFling = true;
            return true;
        }

        private boolean flingImages(float velocityX, float velocityY) {
            int vx = (int) (velocityX + 0.5f);
            int vy = (int) (velocityY + 0.5f);
            if (!PhotoView.this.mFilmMode) {
                return PhotoView.this.mPositionController.flingPage(vx, vy);
            }
            if (Math.abs(velocityX) > Math.abs(velocityY)) {
                return PhotoView.this.mPositionController.flingFilmX(vx);
            }
            return false;
        }

        public boolean onScaleBegin(float focusX, float focusY) {
            if (this.mIgnoreSwipingGesture || !PhotoView.this.mPhotoMagnifierManager.isEnableFlingAndScale()) {
                return true;
            }
            this.mIgnoreScalingGesture = PhotoView.this.isCamera();
            if (this.mIgnoreScalingGesture) {
                return true;
            }
            boolean z;
            PhotoView.this.mPositionController.beginScale(focusX, focusY);
            if (PhotoView.this.mFilmMode) {
                z = true;
            } else if (PhotoView.this.mPositionController.isZoomIn()) {
                z = false;
            } else {
                z = true;
            }
            this.mCanChangeMode = z;
            this.mAccScale = WMElement.CAMERASIZEVALUE1B1;
            return true;
        }

        private boolean ignoreScale(float scale) {
            if (this.mIgnoreSwipingGesture || this.mIgnoreScalingGesture || !PhotoView.this.mPhotoMagnifierManager.isEnableFlingAndScale() || this.mModeChanged || (((Picture) PhotoView.this.mPictures.get(0)).isVideo() && scale > WMElement.CAMERASIZEVALUE1B1 && !PhotoView.this.mFilmMode)) {
                return true;
            }
            if (PhotoView.this.mModel.isLCDDownloaded()) {
                return false;
            }
            return true;
        }

        public boolean onScale(float focusX, float focusY, float scale) {
            if (ignoreScale(scale)) {
                return true;
            }
            if (Float.isNaN(scale) || Float.isInfinite(scale)) {
                return false;
            }
            int outOfRange = PhotoView.this.mPositionController.scaleBy(scale, focusX, focusY);
            this.mAccScale *= scale;
            boolean largeEnough = this.mAccScale < 0.97f || this.mAccScale > 1.03f;
            if (scale < WMElement.CAMERASIZEVALUE1B1 && ((Picture) PhotoView.this.mPictures.get(0)).isVideo()) {
                largeEnough = true;
                outOfRange = -1;
            }
            if (this.mCanChangeMode && largeEnough && ((outOfRange < 0 && !PhotoView.this.mFilmMode) || (outOfRange > 0 && PhotoView.this.mFilmMode))) {
                stopExtraScalingIfNeeded();
                PhotoView photoView = PhotoView.this;
                photoView.mHolding = photoView.mHolding & -2;
                PhotoView.this.setFilmMode(!PhotoView.this.mFilmMode);
                onScaleEnd();
                this.mModeChanged = true;
                return true;
            }
            if (outOfRange != 0) {
                startExtraScalingIfNeeded();
            } else {
                stopExtraScalingIfNeeded();
            }
            return true;
        }

        public void onScaleEnd() {
            if (!this.mIgnoreSwipingGesture && !this.mIgnoreScalingGesture && PhotoView.this.mPhotoMagnifierManager.isEnableFlingAndScale()) {
                int i;
                stopExtraScalingIfNeeded();
                PhotoView.this.mPositionController.endScale();
                PhotoView photoView = PhotoView.this;
                if (PhotoView.this.mFilmMode || !PhotoView.this.mPositionController.isZoomIn()) {
                    i = 0;
                } else {
                    i = 10;
                }
                photoView.mNeedRubberBandEffectEdge = i;
                PhotoView.this.mDoneRubberBandEffectEdge = 0;
                if (!this.mModeChanged) {
                    PhotoView.this.mPositionController.snapback();
                }
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onLongPress(MotionEvent e) {
            if (!(this.mIgnoreSwipingGesture || PhotoView.this.mFilmMode || PhotoView.this.mPositionController.isScrolling() || PhotoView.this.mSimpleGestureListner == null)) {
                PhotoView.this.mSimpleGestureListner.onLongPress(e);
            }
        }

        private void startExtraScalingIfNeeded() {
            if (!PhotoView.this.mCancelExtraScalingPending) {
                PhotoView.this.mPositionController.setExtraScalingRange(true);
                PhotoView.this.mCancelExtraScalingPending = true;
            }
        }

        private void stopExtraScalingIfNeeded() {
            if (PhotoView.this.mCancelExtraScalingPending) {
                PhotoView.this.mHandler.removeMessages(2);
                PhotoView.this.mPositionController.setExtraScalingRange(false);
                PhotoView.this.mCancelExtraScalingPending = false;
            }
        }

        public void onDown(float x, float y) {
            PhotoView.this.checkHideUndoBar(4);
            this.mModeChanged = false;
            if (!this.mIgnoreSwipingGesture) {
                PhotoView photoView = PhotoView.this;
                photoView.mHolding = photoView.mHolding | 1;
                if (PhotoView.this.mFilmMode && PhotoView.this.mPositionController.isScrolling()) {
                    this.mDownInScrolling = true;
                    PhotoView.this.mPositionController.stopScrolling();
                } else {
                    this.mDownInScrolling = false;
                }
                this.mHadFling = false;
                this.mScrolledAfterDown = false;
                if (PhotoView.this.mFilmMode) {
                    PhotoView.this.mTouchBoxIndex = PhotoView.this.mPositionController.hitTest((int) (x + 0.5f), (int) (y + 0.5f));
                    if (PhotoView.this.mTouchBoxIndex < PhotoView.this.mPrevBound || PhotoView.this.mTouchBoxIndex > PhotoView.this.mNextBound) {
                        PhotoView.this.mTouchBoxIndex = Integer.MAX_VALUE;
                    } else {
                        PhotoView.this.mTouchBoxDeletable = ((Picture) PhotoView.this.mPictures.get(PhotoView.this.mTouchBoxIndex)).isDeletable();
                    }
                } else {
                    if (PhotoView.this.mSimpleGestureListner != null) {
                        PhotoView.this.mSimpleGestureListner.onDown(x, y);
                    }
                    PhotoView.this.mTouchBoxIndex = Integer.MAX_VALUE;
                }
            }
        }

        public void setSwipingEnabled(boolean enabled) {
            this.mIgnoreSwipingGesture = !enabled;
        }

        private boolean isFreeShareVelocity(float velocityX, float velocityY) {
            if (Math.abs(velocityY) <= Math.abs(velocityX) * 1.63f || Math.abs(velocityY) <= 200.0f) {
                return false;
            }
            return true;
        }

        private boolean isFreeShareMode() {
            if (this.mIgnoreSwipingGesture || PhotoView.this.mFilmMode || this.mFirstScrollX || PhotoView.this.mPositionController.isZoomIn()) {
                return false;
            }
            return true;
        }

        private boolean canFreeShare(float velocityX, float velocityY) {
            return (FreeShareAdapter.FREESHARE_SUPPORTED && isFreeShareMode()) ? isFreeShareVelocity(velocityX, velocityY) : false;
        }

        private boolean doFreeShare(float velocityX, float velocityY) {
            if (!canFreeShare(velocityX, velocityY)) {
                return false;
            }
            if (velocityY < -200.0f) {
                PhotoView.this.mListener.onFlingUp();
            } else if (velocityY > 200.0f) {
                PhotoView.this.mListener.onFlingDown();
            }
            return true;
        }
    }

    class MyHandler extends SynchronizedHandler {
        public MyHandler(GLRoot root) {
            super(root);
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 2:
                    PhotoView.this.mGestureRecognizer.cancelScale();
                    PhotoView.this.mPositionController.setExtraScalingRange(false);
                    PhotoView.this.mCancelExtraScalingPending = false;
                    return;
                case 3:
                    PhotoView.this.switchFocus();
                    return;
                case 5:
                    PhotoView.this.mListener.onDeleteImage((Path) message.obj, message.arg1);
                    PhotoView.this.mHandler.removeMessages(6);
                    PhotoView.this.mHandler.sendMessageDelayed(PhotoView.this.mHandler.obtainMessage(6), 2000);
                    int numberOfPictures = (PhotoView.this.mNextBound - PhotoView.this.mPrevBound) + 1;
                    if (numberOfPictures == 2 && (PhotoView.this.mModel.isCamera(PhotoView.this.mNextBound) || PhotoView.this.mModel.isCamera(PhotoView.this.mPrevBound))) {
                        numberOfPictures--;
                    }
                    PhotoView.this.showUndoBar(numberOfPictures <= 1);
                    return;
                case 6:
                    if (!PhotoView.this.mHandler.hasMessages(5)) {
                        PhotoView photoView = PhotoView.this;
                        photoView.mHolding = photoView.mHolding & -5;
                        PhotoView.this.snapback();
                        return;
                    }
                    return;
                case 7:
                    PhotoView.this.checkHideUndoBar(2);
                    return;
                case 8:
                    PhotoView.this.checkHideUndoBar(8);
                    return;
                case 9:
                    PhotoView.this.onExtraActionEnd();
                    return;
                case 10:
                    PhotoView.this.setSwipingEnabled(true);
                    PhotoView.this.mNeedUnlockSwipeByDeletePhoto = false;
                    return;
                default:
                    throw new AssertionError(message.what);
            }
        }
    }

    private class ScreenNailPicture extends Picture {
        private boolean mIsCamera;
        private boolean mIsDeletable;
        private boolean mIsPanorama;
        private boolean mIsStaticCamera;
        private boolean mIsVideo;
        private int mLoadingState = 0;
        private ScreenNail mScreenNail;
        private Size mSize = new Size();
        private RectF mSourceRect = new RectF();
        private RectF mTargetRect = new RectF();

        public ScreenNailPicture(int index) {
            super();
            this.mIndex = index;
        }

        public void reload() {
            this.mIsCamera = PhotoView.this.mModel.isCamera(this.mIndex);
            this.mIsPanorama = PhotoView.this.mModel.isPanorama(this.mIndex);
            this.mIsStaticCamera = PhotoView.this.mModel.isStaticCamera(this.mIndex);
            this.mIsVideo = PhotoView.this.mModel.isVideo(this.mIndex);
            this.mIsDeletable = false;
            this.mItem = PhotoView.this.mModel.getMediaItem(this.mIndex);
            int lastState = this.mLoadingState;
            this.mLoadingState = PhotoView.this.mModel.getLoadingState(this.mIndex);
            if (this.mIndex == 0 && lastState != this.mLoadingState && MultiWindowStatusHolder.isInMultiWindowMode() && PhotoView.this.mListener != null) {
                PhotoView.this.mListener.onLoadStateChange(this.mLoadingState);
            }
            if (this.mIsCamera && PhotoView.this.mModel.getScreenNail(this.mIndex) == null) {
                GalleryLog.e("PhotoView", "ScreenNail lost @ ScreenNailPicture");
                setScreenNail(PhotoView.this.mCameraScreenNail);
            } else {
                setScreenNail(PhotoView.this.mModel.getScreenNail(this.mIndex));
            }
            if (MultiWindowStatusHolder.isInMultiMaintained() && this.mIndex == 0) {
                PhotoView.this.parseGifIfNecessary(PhotoView.this.mModel.getMediaItem(0), PhotoView.this.mModel.getScreenNail(0));
            }
            updateSize();
        }

        public Size getSize() {
            return this.mSize;
        }

        private boolean wantsCardEffect() {
            return PhotoView.this.mPositionController.getFilmRatio() != WMElement.CAMERASIZEVALUE1B1 ? PhotoView.this.mAutoSlideMode : false;
        }

        public void draw(GLCanvas canvas, Rect rect, boolean pageMode, boolean isFillShortScale) {
            this.mViewRect.set(rect);
            Rect r = this.mViewRect;
            if (!PhotoView.this.mAutoSlideMode && pageMode) {
                r.offset(calculateRectOffset(), 0);
            }
            if (this.mScreenNail == null) {
                if (this.mIndex >= PhotoView.this.mPrevBound && this.mIndex <= PhotoView.this.mNextBound) {
                    PhotoView.this.drawPlaceHolder(canvas, r);
                }
                return;
            }
            int w = PhotoView.this.getWidth();
            int h = PhotoView.this.getHeight();
            if (r.intersects(0, 0, w, h)) {
                int cx;
                float filmRatio = PhotoView.this.mPositionController.getFilmRatio();
                boolean wantsCardEffect = wantsCardEffect();
                boolean wantsOffsetEffect = (this.mIsDeletable && filmRatio == WMElement.CAMERASIZEVALUE1B1) ? r.centerY() != h / 2 : false;
                if (wantsCardEffect) {
                    cx = (int) (PhotoView.interpolate(filmRatio, ((float) w) / 2.0f, (float) r.centerX()) + 0.5f);
                } else {
                    cx = r.centerX();
                }
                int cy = r.centerY();
                canvas.save(3);
                canvas.translate((float) cx, (float) cy);
                if (wantsCardEffect) {
                    float progress = Utils.clamp(((float) ((w / 2) - r.centerX())) / ((float) w), (float) GroundOverlayOptions.NO_DIMENSION, (float) WMElement.CAMERASIZEVALUE1B1);
                    float alpha = PhotoView.this.getScrollAlpha(progress);
                    float scale = PhotoView.this.getScrollScale(progress);
                    alpha = PhotoView.interpolate(filmRatio, alpha, WMElement.CAMERASIZEVALUE1B1);
                    scale = PhotoView.interpolate(filmRatio, scale, WMElement.CAMERASIZEVALUE1B1);
                    if (PhotoView.this.mAutoSlideMode) {
                        alpha = DeletePhotoAnimationFactor.getOverlayAlpha(progress);
                        scale = DeletePhotoAnimationFactor.getOverlayScale(progress);
                    }
                    canvas.multiplyAlpha(alpha);
                    canvas.scale(scale, scale, WMElement.CAMERASIZEVALUE1B1);
                } else if (wantsOffsetEffect) {
                    canvas.multiplyAlpha(PhotoView.this.getOffsetAlpha(((float) (r.centerY() - (h / 2))) / ((float) h)));
                }
                if (this.mRotation != 0) {
                    canvas.rotate((float) this.mRotation, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
                }
                int drawW = PhotoView.getRotated(this.mRotation, r.width(), r.height());
                int drawH = PhotoView.getRotated(this.mRotation, r.height(), r.width());
                if (!PhotoView.this.mAutoSlideMode && pageMode && Math.abs(this.mIndex) == 1) {
                    calculateRect(drawW, drawH, r);
                    this.mScreenNail.draw(canvas, this.mSourceRect, this.mTargetRect);
                } else {
                    this.mScreenNail.draw(canvas, (-drawW) / 2, (-drawH) / 2, drawW, drawH);
                }
                if (PhotoView.this.mListener != null) {
                    PhotoView.this.mListener.onPhotoTranslationChange((float) r.centerX(), (float) r.centerY(), Math.abs(this.mIndex), pageMode ? isFillShortScale : false, PhotoView.this.mModel.getMediaItem(this.mIndex));
                }
                if (isScreenNailAnimating()) {
                    PhotoView.this.invalidate();
                }
                int s = Math.min(drawW, drawH);
                if (this.mIsVideo) {
                    PhotoView.this.drawVideoPlayIcon(canvas, s);
                }
                if (this.mLoadingState == 2 && (!this.mIsCamera || PhotoView.this.mCameraScreenNail == null)) {
                    PhotoView.this.drawLoadingFailMessage(canvas, this.mIsVideo);
                }
                canvas.restore();
                if (this.mIndex == 0 && PhotoView.this.mWantPictureCenterCallbacks && PhotoView.this.mPositionController.isCenter() && isFillShortScale && !PhotoView.this.mPositionController.isScrolling()) {
                    PhotoView.this.mListener.onPictureCenter(this.mIsCamera);
                }
                return;
            }
            this.mScreenNail.noDraw();
        }

        private void calculateRect(int drawW, int drawH, Rect r) {
            int rotate = this.mIndex == 1 ? this.mRotation : this.mRotation + 180;
            if (this.mScreenNail != null) {
                int screenTextureWidth = this.mScreenNail.getWidth();
                int screenTextureHeight = this.mScreenNail.getHeight();
                float maskProgress = ((float) this.mMaskRect.right) / ((float) r.width());
                switch (rotate % 360) {
                    case 0:
                        this.mSourceRect.set(((float) screenTextureWidth) * maskProgress, 0.0f, (float) screenTextureWidth, (float) screenTextureHeight);
                        this.mTargetRect.set((((float) (-drawW)) / 2.0f) + (((float) drawW) * maskProgress), ((float) (-drawH)) / 2.0f, ((float) drawW) / 2.0f, ((float) drawH) / 2.0f);
                        break;
                    case WMComponent.ORI_90 /*90*/:
                        this.mSourceRect.set(0.0f, 0.0f, (float) screenTextureWidth, ((float) screenTextureHeight) * (WMElement.CAMERASIZEVALUE1B1 - maskProgress));
                        this.mTargetRect.set(((float) (-drawW)) / 2.0f, ((float) (-drawH)) / 2.0f, ((float) drawW) / 2.0f, (((float) drawH) / 2.0f) - (((float) drawH) * maskProgress));
                        break;
                    case 180:
                        this.mSourceRect.set(0.0f, 0.0f, ((float) screenTextureWidth) * (WMElement.CAMERASIZEVALUE1B1 - maskProgress), (float) screenTextureHeight);
                        this.mTargetRect.set(((float) (-drawW)) / 2.0f, ((float) (-drawH)) / 2.0f, (((float) drawW) / 2.0f) - (((float) drawW) * maskProgress), ((float) drawH) / 2.0f);
                        break;
                    case 270:
                        this.mSourceRect.set(0.0f, ((float) screenTextureHeight) * maskProgress, (float) screenTextureWidth, (float) screenTextureHeight);
                        this.mTargetRect.set(((float) (-drawW)) / 2.0f, (((float) (-drawH)) / 2.0f) + (((float) drawH) * maskProgress), ((float) drawW) / 2.0f, ((float) drawH) / 2.0f);
                        break;
                }
            }
        }

        private boolean isScreenNailAnimating() {
            if (this.mScreenNail instanceof TiledScreenNail) {
                return ((TiledScreenNail) this.mScreenNail).isAnimating();
            }
            return false;
        }

        public void setScreenNail(ScreenNail s) {
            this.mScreenNail = s;
            if (MultiWindowStatusHolder.isInMultiWindowMode() && this.mIndex == 0) {
                PhotoView.this.clearAnimationProxyView(s);
            }
            if (s == null) {
                this.mLoadingState = 0;
            }
        }

        public void forceSize() {
            updateSize();
            PhotoView.this.mPositionController.forceImageSize(this.mIndex, this.mSize);
        }

        private void updateSize() {
            if (this.mIsPanorama) {
                this.mRotation = PhotoView.this.getPanoramaRotation();
            } else if (!this.mIsCamera || this.mIsStaticCamera) {
                this.mRotation = PhotoView.this.mModel.getImageRotation(this.mIndex);
            } else {
                this.mRotation = PhotoView.this.getCameraRotation();
            }
            if (this.mScreenNail != null) {
                this.mSize.width = this.mScreenNail.getWidth();
                this.mSize.height = this.mScreenNail.getHeight();
            } else {
                PhotoView.this.mModel.getImageSize(this.mIndex, this.mSize);
            }
            int w = this.mSize.width;
            int h = this.mSize.height;
            this.mSize.width = PhotoView.getRotated(this.mRotation, w, h);
            this.mSize.height = PhotoView.getRotated(this.mRotation, h, w);
        }

        public boolean isCamera() {
            return this.mIsCamera;
        }

        public boolean isDeletable() {
            return this.mIsDeletable;
        }

        public boolean isVideo() {
            return this.mIsVideo;
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

    public PhotoView(GalleryContext activity, GLRoot glRoot) {
        TileImageView supportDisplayEngineTileImageView;
        TraceController.traceBegin("PhotoView.PhotoView");
        if (DisplayEngineUtils.isDisplayEngineEnable()) {
            supportDisplayEngineTileImageView = new SupportDisplayEngineTileImageView(activity);
        } else {
            supportDisplayEngineTileImageView = new TileImageView(activity);
        }
        this.mTileView = supportDisplayEngineTileImageView;
        this.mPhotoMagnifier = new PhotoMagnifierView(activity);
        addComponent(this.mTileView);
        addComponent(this.mPhotoMagnifier);
        this.mTileView.setReleativeView(this.mPhotoMagnifier);
        this.mContext = activity.getAndroidContext();
        this.mPlaceholderColor = this.mContext.getResources().getColor(R.color.photo_placeholder);
        this.mNoThumbTexture = new ResourceTexture(this.mContext, R.drawable.ic_gallery_list_damage_photo);
        Context context = activity.getGalleryApplication().getAndroidContext();
        this.mBackgroundColor = context.getColor(R.color.photo_background);
        this.mLoadingSpinner = new ProgressSpinner(context);
        this.mHandler = new MyHandler(glRoot);
        this.mGestureListener = new MyGestureListener();
        this.mGestureRecognizer = new GestureRecognizer(this.mContext, this.mGestureListener);
        this.mPositionController = new PositionController(this.mContext, new PositionController.Listener() {
            public void onEdgeRubberBand(int r1) {
                /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.gallery3d.ui.PhotoView.2.onEdgeRubberBand(int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 6 more
*/
                /*
                // Can't load method instructions.
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.gallery3d.ui.PhotoView.2.onEdgeRubberBand(int):void");
            }

            public void invalidate() {
                PhotoView.this.invalidate();
            }

            public boolean isHoldingDown() {
                return (PhotoView.this.mHolding & 1) != 0;
            }

            public boolean isHoldingDelete() {
                return (PhotoView.this.mHolding & 4) != 0;
            }

            public int needRubberBandEffectEdge() {
                return PhotoView.this.mNeedRubberBandEffectEdge;
            }
        });
        this.mVideoPlayIcon = new ResourceTexture(this.mContext, R.drawable.btn_video_play);
        boolean isMW = MultiWindowStatusHolder.isInMultiMaintained();
        for (int i = -3; i <= 3; i++) {
            if (i == 0) {
                this.mPictures.put(i, isMW ? new ScreenNailPicture(0) : new FullPicture());
            } else {
                this.mPictures.put(i, new ScreenNailPicture(i));
            }
        }
        this.mPhotoMagnifierManager = PhotoMagnifierManager.getInstance();
        this.mPhotoMagnifierModeListener = new PhotoMagnifierModeListener() {
            public void onEnterMagnifierMode() {
                PhotoView.this.mPhotoMagnifier.setVisibility(0);
                PhotoView.this.mPhotoMagnifier.setPhotoMagnifierListener(PhotoView.this);
                PhotoView.this.mPhotoMagnifierManager.enableFlingAndScale(false);
                PhotoView.this.invalidate();
                ReportToBigData.report(59);
            }

            public void onLeaveMagnifierMode() {
                PhotoView.this.mPhotoMagnifier.setVisibility(1);
                PhotoView.this.mPhotoMagnifier.setPhotoMagnifierListener(null);
                PhotoView.this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        PhotoView.this.mPhotoMagnifierManager.enableFlingAndScale(true);
                    }
                }, 100);
                PhotoView.this.invalidate();
            }

            public void onHideAnimationEnd() {
                PhotoView.this.mHandler.post(new Runnable() {
                    public void run() {
                        PhotoView.this.leaveMagnifierMode();
                    }
                });
            }
        };
        TraceController.traceEnd();
    }

    public void lockPhotoMagnifier() {
        this.mLockPhotoMagnifier = true;
    }

    private void unLockPhotoMagnifier() {
        this.mLockPhotoMagnifier = false;
    }

    public void setModel(Model model) {
        TraceController.traceBegin("PhotoView.setModel");
        this.mModel = model;
        this.mTileView.setModel(this.mModel);
        TraceController.traceEnd();
    }

    public void setWantPictureCenterCallbacks(boolean wanted) {
        this.mWantPictureCenterCallbacks = wanted;
    }

    public void setWantPictureFullViewCallbacks(boolean wanted) {
        this.mWantPictureFullViewCallbacks = wanted;
    }

    public void notifyDataChange(int[] fromIndex, int prevBound, int nextBound, int maskOffset) {
        int i;
        TraceController.traceBegin("PhotoView.notifyDataChange");
        this.mPrevBound = prevBound;
        this.mNextBound = nextBound;
        if (this.mTouchBoxIndex != Integer.MAX_VALUE) {
            int k = this.mTouchBoxIndex;
            this.mTouchBoxIndex = Integer.MAX_VALUE;
            for (i = 0; i < 7; i++) {
                if (fromIndex[i] == k) {
                    this.mTouchBoxIndex = i - 3;
                    break;
                }
            }
        }
        if (this.mUndoIndexHint != Integer.MAX_VALUE && Math.abs(this.mUndoIndexHint - this.mModel.getCurrentIndex()) >= 3) {
            hideUndoBar();
        }
        for (i = -3; i <= 3; i++) {
            Picture p = (Picture) this.mPictures.get(i);
            p.reload();
            this.mSizes[i + 3] = p.getSize();
        }
        boolean wasDeleting = this.mPositionController.hasDeletingBox();
        this.mPositionController.moveBox(fromIndex, this.mPrevBound < 0, this.mNextBound > 0, this.mModel.isCamera(0), this.mSizes, maskOffset);
        for (i = -3; i <= 3; i++) {
            setPictureSize(i);
        }
        boolean isDeleting = this.mPositionController.hasDeletingBox();
        if (wasDeleting && !isDeleting) {
            this.mHandler.removeMessages(6);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(6), 600);
        }
        if (!this.mPositionController.isZoomIn()) {
            this.mNeedRubberBandEffectEdge = 0;
            this.mDoneRubberBandEffectEdge = 0;
        }
        invalidate();
        TraceController.traceEnd();
    }

    public void notifyImageChange(int index) {
        TraceController.traceBegin("PhotoView.notifyImageChange");
        boolean hasDirectShowNail = false;
        if (index == 0) {
            this.mListener.onCurrentImageUpdated();
            hasDirectShowNail = this.mTileView.hasDirectShowNail();
        }
        ((Picture) this.mPictures.get(index)).reload();
        setPictureSize(index);
        if (hasDirectShowNail) {
            this.mPositionController.skipAnimation();
        }
        if (index == 0 && !this.mPositionController.isZoomIn()) {
            this.mNeedRubberBandEffectEdge = 0;
            this.mDoneRubberBandEffectEdge = 0;
        }
        invalidate();
        TraceController.traceEnd();
    }

    private void setPictureSize(int index) {
        Picture p = (Picture) this.mPictures.get(index);
        PositionController positionController = this.mPositionController;
        Size size = p.getSize();
        Rect rect = (index == 0 && p.isCamera()) ? this.mCameraRect : null;
        positionController.setImageSize(index, size, rect);
    }

    protected void onLayout(boolean changeSize, int left, int top, int right, int bottom) {
        TraceController.traceBegin("PhotoView.onLayout");
        this.mTileView.layout(0, 0, right - left, bottom - top);
        GLRoot root = getGLRoot();
        int displayRotation = root.getDisplayRotation();
        int compensation = root.getCompensation();
        if (!(this.mDisplayRotation == displayRotation && this.mCompensation == compensation)) {
            this.mDisplayRotation = displayRotation;
            this.mCompensation = compensation;
            for (int i = -3; i <= 3; i++) {
                Picture p = (Picture) this.mPictures.get(i);
                if (p.isCamera()) {
                    p.forceSize();
                }
            }
        }
        updateCameraRect();
        this.mPositionController.setConstrainedFrame(this.mCameraRect);
        if (changeSize) {
            this.mPositionController.setViewSize(getWidth(), getHeight());
        }
        TraceController.traceEnd();
    }

    private void updateCameraRect() {
        int w = getWidth();
        int h = getHeight();
        if (this.mCompensation % 180 != 0) {
            int tmp = w;
            w = h;
            h = tmp;
        }
        int l = this.mCameraRelativeFrame.left;
        int t = this.mCameraRelativeFrame.top;
        int r = this.mCameraRelativeFrame.right;
        int b = this.mCameraRelativeFrame.bottom;
        switch (this.mCompensation) {
            case 0:
                this.mCameraRect.set(l, t, r, b);
                break;
            case WMComponent.ORI_90 /*90*/:
                this.mCameraRect.set(h - b, l, h - t, r);
                break;
            case 180:
                this.mCameraRect.set(w - r, h - b, w - l, h - t);
                break;
            case 270:
                this.mCameraRect.set(t, w - r, b, w - l);
                break;
        }
        GalleryLog.d("PhotoView", "compensation = " + this.mCompensation + ", CameraRelativeFrame = " + this.mCameraRelativeFrame + ", mCameraRect = " + this.mCameraRect);
    }

    public int getCameraRotation() {
        return ((this.mCompensation - this.mDisplayRotation) + 360) % 360;
    }

    private int getPanoramaRotation() {
        boolean invert = true;
        boolean invertPortrait = this.mContext.getResources().getConfiguration().orientation == 1 ? this.mDisplayRotation == 90 || this.mDisplayRotation == 270 : false;
        if (this.mDisplayRotation < 180) {
            invert = false;
        }
        if (invert != invertPortrait) {
            return (this.mCompensation + 180) % 360;
        }
        return this.mCompensation;
    }

    private void drawPlaceHolder(GLCanvas canvas, Rect r) {
        canvas.fillRect((float) r.left, (float) r.top, (float) r.width(), (float) r.height(), this.mPlaceholderColor);
    }

    private void drawVideoPlayIcon(GLCanvas canvas, int side) {
        this.mVideoPlayIcon.draw(canvas, (-this.mVideoPlayIcon.getWidth()) / 2, (-this.mVideoPlayIcon.getHeight()) / 2);
    }

    private void drawLoadingFailMessage(GLCanvas canvas, boolean isVideo) {
        ResourceTexture m = this.mNoThumbTexture;
        if (isVideo) {
            m.draw(canvas, (-m.getWidth()) / 2, (((-m.getHeight()) / 2) - (this.mVideoPlayIcon.getHeight() / 2)) - ((int) ((this.mContext.getResources().getDisplayMetrics().density * 18.0f) + 0.5f)));
        } else {
            m.draw(canvas, (-m.getWidth()) / 2, (-m.getHeight()) / 2);
        }
    }

    private static int getRotated(int degree, int original, int theother) {
        return degree % 180 == 0 ? original : theother;
    }

    private float getMagnifierScaleForAnimation(float pressure, float progress) {
        float minScale = getMagnifierScale(this.mPressureValueThreshold);
        return ((getMagnifierScale(pressure) - minScale) * Utils.clamp(progress < 0.7f ? 0.0f : (progress - 0.7f) * 3.4f, 0.0f, (float) WMElement.CAMERASIZEVALUE1B1)) + minScale;
    }

    private float getMagnifierScale(float pressure) {
        boolean z = false;
        float minScale = this.mPositionController.getImageScale() * 1.1f;
        float limitedScale = this.mPositionController.getScaleMax() * 1.4f;
        float maxScale = this.mPositionController.getScaleMax() * MapConfig.MIN_ZOOM;
        float preesureValueMaxScaleThreshold = this.mPressureValueThreshold + 0.42f;
        if (Float.compare(preesureValueMaxScaleThreshold, this.mPressureValueThreshold) > 0) {
            z = true;
        }
        Utils.assertTrue(z);
        if (pressure <= this.mPressureValueThreshold) {
            return minScale;
        }
        if (pressure <= preesureValueMaxScaleThreshold * 0.7f && pressure > this.mPressureValueThreshold) {
            return (((pressure - this.mPressureValueThreshold) * (limitedScale - minScale)) / ((preesureValueMaxScaleThreshold * 0.7f) - this.mPressureValueThreshold)) + minScale;
        }
        if (pressure > preesureValueMaxScaleThreshold || pressure <= preesureValueMaxScaleThreshold * 0.7f) {
            return maxScale;
        }
        return (((pressure - (preesureValueMaxScaleThreshold * 0.7f)) * (maxScale - limitedScale)) / (0.3f * preesureValueMaxScaleThreshold)) + limitedScale;
    }

    private boolean supportPhotoMagnifier(float preesure) {
        boolean isVideo = false;
        boolean isDetailsShow = false;
        if (!(this.mModel == null || this.mModel.getMediaItem(0) == null)) {
            isVideo = this.mModel.getMediaItem(0).getMediaType() == 4;
        }
        if (this.mListener != null) {
            isDetailsShow = this.mListener.isDetailsShow();
        }
        if (this.mFilmMode || !this.mPositionController.isCenter() || isVideo || isDetailsShow || !this.mPositionController.isFillShortScale() || !GalleryUtils.isSupportPressureResponseMagnifier(this.mContext) || Float.compare(preesure, this.mPressureValueThreshold) <= 0 || this.mLockPhotoMagnifier) {
            return false;
        }
        return true;
    }

    protected boolean onTouch(MotionEvent event) {
        switch (event.getAction()) {
            case 0:
                this.mEventCounter = 0;
                break;
            case 1:
            case 3:
                if (event.getAction() == 1) {
                    unLockPhotoMagnifier();
                }
                this.mHandler.removeCallbacks(this.mPhotoMagnifierMoveTask);
                if (this.mPhotoMagnifierManager.inMagnifierMode() && this.mPhotoMagnifierPendingHide == null) {
                    this.mPhotoMagnifierPendingHide = getPhotoMagnifierPending();
                    if (!this.mPhotoMagnifierManager.inAnimationState()) {
                        this.mPhotoMagnifierPendingHide.run();
                        break;
                    }
                }
                break;
            case 2:
                if (GalleryUtils.isSupportPressureResponseMagnifier(this.mContext)) {
                    if (event.getPointerCount() != 1 || this.mEventCounter <= 10 || this.mPhotoMagnifierPendingHide != null) {
                        if (event.getPointerCount() <= 1) {
                            this.mEventCounter++;
                            break;
                        }
                        this.mEventCounter = 0;
                        break;
                    }
                    this.mPhotoMagnifierEvent = MotionEvent.obtain(event);
                    this.mHandler.removeCallbacks(this.mPhotoMagnifierMoveTask);
                    this.mHandler.post(this.mPhotoMagnifierMoveTask);
                    this.mEventCounter = 11;
                    break;
                }
                break;
        }
        this.mGestureRecognizer.onTouchEvent(event);
        if (this.mListener != null) {
            this.mListener.onTouchEventReceived(event);
        }
        return true;
    }

    private Runnable getPhotoMagnifierPending() {
        return new Runnable() {
            public void run() {
                PhotoView.this.hidePhotoMagnifier();
            }
        };
    }

    private void hidePhotoMagnifier() {
        MotionEvent event = this.mPhotoMagnifierEvent;
        if (event != null) {
            FloatAnimation photoMagnifierAnimation = this.mPhotoMagnifierManager.createAnimation(AnimationType.HIDE);
            this.mPhotoMagnifier.setAnimation(photoMagnifierAnimation);
            photoMagnifierAnimation.start();
            this.mPhotoMagnifierManager.setMagnifierState(PhotoMagnifierState.ANIM);
            this.mPhotoMagnifier.draw(event.getX(), event.getY());
            return;
        }
        leaveMagnifierMode();
    }

    private void enterMagnifierMode(MotionEvent event) {
        if (!this.mPhotoMagnifierManager.inMagnifierMode()) {
            Rect photoViewEdge = new Rect();
            getPhotoViewEdge(photoViewEdge);
            if (photoViewEdge.contains((int) event.getX(), (int) event.getY())) {
                if (this.mModel != null) {
                    GLRoot root = getGLRoot();
                    if (root != null) {
                        root.lockRenderThread();
                        try {
                            this.mPhotoMagnifier.setBitmap(UIUtils.getBitmapFromScreenNail(this.mModel.getScreenNail()));
                        } finally {
                            root.unlockRenderThread();
                        }
                    } else {
                        return;
                    }
                }
                FloatAnimation photoMagnifierAnimation = this.mPhotoMagnifierManager.createAnimation(AnimationType.SHOW);
                this.mPhotoMagnifier.setAnimation(photoMagnifierAnimation);
                photoMagnifierAnimation.start();
                float progress = photoMagnifierAnimation.get();
                this.mPhotoMagnifierManager.enterMagnifierMode();
                this.mListener.onEnterPhotoMagnifierMode();
                this.mPrePressure = event.getPressure();
                this.mPreTouchX = event.getX();
                this.mPreTouchY = event.getY();
                this.mPhotoMagnifier.setScale(getMagnifierScaleForAnimation(this.mPrePressure, progress));
                this.mPhotoMagnifier.draw(this.mPreTouchX, this.mPreTouchY);
            }
        }
    }

    private void leaveMagnifierMode() {
        if (this.mPhotoMagnifierManager.inMagnifierMode()) {
            this.mHandler.removeCallbacks(this.mPhotoMagnifierMoveTask);
            this.mPhotoMagnifierManager.leaveMagnifierMode();
            this.mPhotoMagnifierEvent = null;
            if (this.mListener != null) {
                this.mListener.onLeavePhotoMagnifierMode();
            }
            this.mTileView.layoutTiles();
            this.mPhotoMagnifierPendingHide = null;
        }
    }

    private void getPhotoViewEdge(Rect photoViewEdge) {
        if (photoViewEdge != null) {
            int photoViewWidth = (int) (((float) this.mPositionController.getImageWidth()) * this.mPositionController.getImageScale());
            int photoViewHeight = (int) (((float) this.mPositionController.getImageHeight()) * this.mPositionController.getImageScale());
            photoViewEdge.left = (getWidth() - photoViewWidth) / 2;
            photoViewEdge.right = photoViewEdge.left + photoViewWidth;
            photoViewEdge.top = (getHeight() - photoViewHeight) / 2;
            photoViewEdge.bottom = photoViewEdge.top + photoViewHeight;
        }
    }

    private PointF rotatePointInPhoto(PointF point, int rotation) {
        float x = point.x;
        float y = point.y;
        switch ((rotation + 360) % 360) {
            case WMComponent.ORI_90 /*90*/:
                point.x = y;
                point.y = ((float) this.mPositionController.getImageWidth()) - x;
                break;
            case 180:
                point.x = ((float) this.mPositionController.getImageWidth()) - x;
                point.y = ((float) this.mPositionController.getImageHeight()) - y;
                break;
            case 270:
                point.x = ((float) this.mPositionController.getImageHeight()) - y;
                point.y = x;
                break;
        }
        return point;
    }

    private RectF getPhotoRange(float x, float y) {
        float scale = this.mPhotoMagnifier.getScale();
        float width = ((float) PhotoMagnifierView.MAGNIFIER_SQUARE_WIDTH) / scale;
        float height = ((float) PhotoMagnifierView.MAGNIFIER_SQUARE_WIDTH) / scale;
        int rotation = this.mModel == null ? 0 : this.mModel.getImageRotation(0);
        int imageWidth = this.mPositionController.getImageWidth();
        int imageHeight = this.mPositionController.getImageHeight();
        RectF range = new RectF();
        x /= this.mPositionController.getImageScale();
        y /= this.mPositionController.getImageScale();
        if (width > ((float) imageWidth)) {
            range.left = 0.0f;
            range.right = (float) imageWidth;
        } else {
            range.left = (float) ((int) (x - (width / 2.0f)));
            range.left = Math.max(range.left, 0.0f);
            range.right = range.left + width;
            if (range.right > ((float) imageWidth)) {
                range.right = (float) imageWidth;
                range.left = range.right - width;
            }
        }
        if (width > ((float) imageHeight)) {
            range.top = 0.0f;
            range.bottom = (float) imageHeight;
        } else {
            range.top = y - (height / 2.0f);
            range.top = Math.max(range.top, 0.0f);
            range.bottom = range.top + height;
            if (range.bottom > ((float) imageHeight)) {
                range.bottom = (float) imageHeight;
                range.top = range.bottom - height;
            }
        }
        PointF point = new PointF(range.left, range.top);
        PointF point1 = new PointF(range.right, range.bottom);
        rotatePointInPhoto(point, rotation);
        rotatePointInPhoto(point1, rotation);
        range.set(point.x, point.y, point1.x, point1.y);
        return range;
    }

    public void drawPhotoMagnifier(float x, float y) {
        int width = PhotoMagnifierView.MAGNIFIER_WIDTH;
        int height = PhotoMagnifierView.MAGNIFIER_HEIGHT;
        int padding = this.mPhotoMagnifier.getPadding();
        Rect photoMagnifierLayout = new Rect();
        Rect photoViewEdge = new Rect();
        getPhotoViewEdge(photoViewEdge);
        if (((int) x) < photoViewEdge.left) {
            x = (float) photoViewEdge.left;
        } else if (((int) x) > photoViewEdge.right) {
            x = (float) photoViewEdge.right;
        }
        if (((int) y) < photoViewEdge.top) {
            y = (float) photoViewEdge.top;
        } else if (((int) y) > photoViewEdge.bottom) {
            y = (float) photoViewEdge.bottom;
        }
        int photoViewHeight = photoViewEdge.bottom - photoViewEdge.top;
        if (photoViewEdge.right - photoViewEdge.left < width) {
            photoMagnifierLayout.left = ((int) x) - (width / 2);
            photoMagnifierLayout.right = photoMagnifierLayout.left + width;
        } else {
            photoMagnifierLayout.left = ((int) x) - (width / 2);
            photoMagnifierLayout.left = Math.max(photoMagnifierLayout.left, photoViewEdge.left + padding);
            photoMagnifierLayout.right = photoMagnifierLayout.left + width;
            if (photoMagnifierLayout.right > photoViewEdge.right - padding) {
                photoMagnifierLayout.right = photoViewEdge.right - padding;
                photoMagnifierLayout.left = photoMagnifierLayout.right - width;
            }
        }
        int gap = this.mPhotoMagnifier.getGapBetweenMagnifierAndTouchPoint();
        if (photoViewHeight < height) {
            photoMagnifierLayout.top = (((int) y) - gap) - (height / 2);
            photoMagnifierLayout.bottom = photoMagnifierLayout.top + height;
        } else {
            photoMagnifierLayout.top = (((int) y) - gap) - (height / 2);
            photoMagnifierLayout.top = Math.max(photoMagnifierLayout.top, padding);
            photoMagnifierLayout.bottom = photoMagnifierLayout.top + height;
            if (photoMagnifierLayout.bottom > photoViewEdge.bottom) {
                photoMagnifierLayout.bottom = photoViewEdge.bottom;
                photoMagnifierLayout.top = photoMagnifierLayout.bottom - height;
            }
        }
        this.mPhotoMagnifier.setPhotoRange(getPhotoRange((float) (((int) x) - photoViewEdge.left), (float) (((int) y) - photoViewEdge.top)));
        this.mPhotoMagnifier.layout(photoMagnifierLayout.left, photoMagnifierLayout.top, photoMagnifierLayout.right, photoMagnifierLayout.bottom);
        this.mPhotoMagnifier.setPhotoPosition();
        invalidate();
    }

    public TileImageView getTileImageView() {
        return this.mTileView;
    }

    public void onMagnifierAnimationEnd() {
        Runnable pendingHide = this.mPhotoMagnifierPendingHide;
        MagnifierAnimation animation = this.mPhotoMagnifierManager.getAnimation();
        if (pendingHide != null && animation != null && AnimationType.SHOW.equal(animation.getType())) {
            pendingHide.run();
        }
    }

    public float getScaleForAnimation(float progress) {
        MotionEvent event = this.mPhotoMagnifierEvent;
        if (event != null) {
            return getMagnifierScaleForAnimation(event.getPressure(), progress);
        }
        return GroundOverlayOptions.NO_DIMENSION;
    }

    public void setSwipingEnabled(boolean enabled) {
        this.mGestureListener.setSwipingEnabled(enabled);
    }

    private void updateActionBar() {
        if (!((Picture) this.mPictures.get(0)).isCamera() || this.mFilmMode) {
            this.mListener.onActionBarAllowed(true);
            if (this.mFilmMode) {
                this.mListener.onActionBarWanted();
                return;
            }
            return;
        }
        this.mListener.onActionBarAllowed(false);
    }

    public void setFilmModeAllowed(boolean allow) {
        if (this.mFilmModeAllowed != allow) {
            this.mFilmModeAllowed = allow;
            if (this.mFilmMode && !this.mFilmModeAllowed) {
                setFilmMode(this.mFilmModeAllowed);
            }
        }
    }

    public void setFilmMode(boolean enabled) {
        int i = 1;
        if (this.mFilmModeAllowed && this.mFilmMode != enabled) {
            boolean z;
            this.mFilmMode = enabled;
            if (this.mFilmMode) {
                this.mNeedRubberBandEffectEdge = 0;
                this.mDoneRubberBandEffectEdge = 0;
            }
            this.mPositionController.setFilmMode(this.mFilmMode);
            Model model = this.mModel;
            if (enabled) {
                z = false;
            } else {
                z = true;
            }
            model.setNeedFullImage(z);
            Model model2 = this.mModel;
            if (!this.mFilmMode) {
                i = 0;
            }
            model2.setFocusHintDirection(i);
            updateActionBar();
            this.mListener.onFilmModeChanged(enabled);
        }
    }

    public boolean getFilmMode() {
        return this.mFilmMode;
    }

    public void pause() {
        if (this.mNeedUnlockSwipeByDeletePhoto) {
            this.mHandler.removeMessages(10);
            setSwipingEnabled(true);
            this.mNeedUnlockSwipeByDeletePhoto = false;
        }
        leaveMagnifierMode();
        this.mPositionController.skipAnimation();
        this.mTileView.freeTextures();
        Model model = this.mModel;
        if (model != null) {
            ScreenNailCommonDisplayEnginePool screenNailCommonDisplayEnginePool = model.getScreenNailCommonDisplayEnginePool();
            if (screenNailCommonDisplayEnginePool != null) {
                screenNailCommonDisplayEnginePool.clear();
            }
        }
        BoostFullScreenNailDisplay.recycleAll();
        for (int i = -3; i <= 3; i++) {
            ((Picture) this.mPictures.get(i)).setScreenNail(null);
        }
        if (this.mGifThread != null) {
            this.mGifThread.interrupt();
        }
        hideUndoBar();
        this.mPhotoMagnifierManager.setMagnifieModeListener(null);
    }

    public void resume() {
        boolean needPrepareTextures = true;
        if (this.mListener != null && (this.mListener.inEditorMode() || this.mListener.inBurstMode())) {
            needPrepareTextures = false;
        }
        if (needPrepareTextures) {
            this.mTileView.prepareTextures();
        }
        this.mPressureValueThreshold = Math.min(0.58000004f, GalleryUtils.getPressureResponseThreshold(this.mContext) * 0.6f);
        this.mPressureValueThreshold = Math.max(this.mPressureValueThreshold, 0.08f);
        leaveMagnifierMode();
        this.mPositionController.skipToFinalPosition();
        parseGifIfNecessary(this.mModel.getMediaItem(0), this.mModel.getScreenNail(0));
        this.mPhotoMagnifierManager.setMagnifieModeListener(this.mPhotoMagnifierModeListener);
    }

    private void showUndoBar(boolean deleteLast) {
        this.mHandler.removeMessages(7);
        this.mUndoBarState = 1;
        if (deleteLast) {
            this.mUndoBarState |= 16;
        }
        this.mHandler.sendEmptyMessageDelayed(7, 3000);
        if (this.mListener != null) {
            this.mListener.onUndoBarVisibilityChanged(true);
        }
    }

    private void hideUndoBar() {
        this.mHandler.removeMessages(7);
        this.mListener.onCommitDeleteImage();
        this.mUndoBarState = 0;
        this.mUndoIndexHint = Integer.MAX_VALUE;
        this.mListener.onUndoBarVisibilityChanged(false);
    }

    private void checkHideUndoBar(int addition) {
        this.mUndoBarState |= addition;
        if ((this.mUndoBarState & 1) != 0) {
            boolean timeout = (this.mUndoBarState & 2) != 0;
            boolean touched = (this.mUndoBarState & 4) != 0;
            boolean fullCamera = (this.mUndoBarState & 8) != 0;
            boolean deleteLast = (this.mUndoBarState & 16) != 0;
            if ((timeout && deleteLast) || fullCamera || touched) {
                hideUndoBar();
            }
        }
    }

    protected void render(GLCanvas canvas) {
        int neighbors;
        int i;
        TraceController.traceBegin("PhotoView render");
        boolean full;
        if (this.mFilmMode || !this.mPositionController.isCenter()) {
            full = false;
        } else {
            full = this.mPositionController.isFillShortScale();
        }
        if (this.mAutoSlideMode && r0) {
            onExtraActionEnd();
        }
        boolean inPageMode = this.mPositionController.getFilmRatio() == 0.0f;
        boolean inCaptureAnimation = (this.mHolding & 2) != 0;
        if (!inPageMode || inCaptureAnimation) {
            neighbors = 3;
        } else {
            neighbors = 1;
        }
        boolean isFillShortScale = this.mPositionController.isFillShortScale();
        for (i = neighbors; i >= (-neighbors); i--) {
            ((Picture) this.mPictures.get(i)).calculateMaskArgs(this.mPositionController.getPosition(i), this.mPositionController.getTargetPosition(i), inPageMode);
        }
        ((Picture) this.mPictures.get(0)).draw(canvas, this.mPositionController.getPosition(0), inPageMode, isFillShortScale);
        for (i = neighbors; i >= (-neighbors); i--) {
            if (i != 0) {
                ((Picture) this.mPictures.get(i)).draw(canvas, this.mPositionController.getPosition(i), inPageMode, isFillShortScale);
            }
        }
        renderChild(canvas, this.mPhotoMagnifier);
        this.mPositionController.advanceAnimation();
        checkFocusSwitching();
        TraceController.traceEnd();
        if (this.mListener != null) {
            this.mListener.onRenderFinish();
        }
    }

    private void checkFocusSwitching() {
        if (!(!this.mFilmMode || this.mHandler.hasMessages(3) || switchPosition() == 0)) {
            this.mHandler.sendEmptyMessage(3);
        }
    }

    private void switchFocus() {
        if (this.mHolding == 0) {
            switch (switchPosition()) {
                case -1:
                    this.mSlideToNext = false;
                    switchToPrevImage();
                    break;
                case 1:
                    this.mSlideToNext = true;
                    switchToNextImage();
                    break;
            }
        }
    }

    private int switchPosition() {
        Rect curr = this.mPositionController.getPosition(0);
        int center = getWidth() / 2;
        if (curr.left > center && this.mPrevBound < 0) {
            if (center - this.mPositionController.getPosition(-1).right < curr.left - center) {
                return -1;
            }
        } else if (curr.right < center && this.mNextBound > 0) {
            if (this.mPositionController.getPosition(1).left - center < center - curr.right) {
                return 1;
            }
        }
        return 0;
    }

    private void switchToHitPicture(int x, int y) {
        if (this.mPrevBound < 0 && this.mPositionController.getPosition(-1).right >= x) {
            slideToPrevPicture();
        } else if (this.mNextBound > 0 && this.mPositionController.getPosition(1).left <= x) {
            slideToNextPicture();
        }
    }

    private boolean swipeImages(float velocityX, float velocityY) {
        if (this.mFilmMode) {
            return false;
        }
        PositionController controller = this.mPositionController;
        boolean isZoomIn = controller.isZoomIn();
        int edges = controller.getImageAtEdges();
        if (isZoomIn && Math.abs(velocityY) > Math.abs(velocityX) && ((edges & 4) == 0 || (edges & 8) == 0)) {
            return false;
        }
        if (velocityX < -300.0f && !isZoomIn) {
            if (this.mListener != null && this.mNextBound > 0) {
                this.mListener.onSwipeImages(velocityX, velocityY);
            }
            return slideToNextPicture();
        } else if (velocityX <= BitmapDescriptorFactory.HUE_MAGENTA || isZoomIn) {
            return false;
        } else {
            if (this.mListener != null && this.mPrevBound < 0) {
                this.mListener.onSwipeImages(velocityX, velocityY);
            }
            return slideToPrevPicture();
        }
    }

    private void snapback() {
        if ((this.mHolding & -5) == 0) {
            if (this.mFilmMode || !snapToNeighborImage()) {
                this.mPositionController.snapback();
            }
        }
    }

    private boolean snapToNeighborImage() {
        Rect r = this.mPositionController.getPosition(0);
        int viewW = getWidth();
        int threshold = PositionController.RUBBER_BAND_RANGE + gapToSide(r.width(), viewW);
        if (viewW - r.right > threshold) {
            return slideToNextPicture();
        }
        if (r.left > threshold) {
            return slideToPrevPicture();
        }
        return false;
    }

    public void switchPictureByFingerprintKey(boolean isForward) {
        GLRoot root = getGLRoot();
        if (root != null) {
            ReportToBigData.reportForFingerprintToSlidingPictures();
            root.lockRenderThread();
            if (isForward) {
                try {
                    slideToNextPicture();
                } catch (Throwable th) {
                    root.unlockRenderThread();
                }
            } else {
                slideToPrevPicture();
            }
            root.unlockRenderThread();
        }
    }

    private boolean slideToNextPicture() {
        if (this.mNextBound <= 0) {
            return false;
        }
        switchToNextImage();
        this.mPositionController.startHorizontalSlide(this.mAutoSlideMode);
        if (!(getFilmMode() || this.mListener == null)) {
            this.mListener.onSlidePicture();
        }
        this.mSlideToNext = true;
        return true;
    }

    private boolean slideToPrevPicture() {
        if (this.mPrevBound >= 0) {
            return false;
        }
        switchToPrevImage();
        this.mPositionController.startHorizontalSlide(this.mAutoSlideMode);
        if (!(getFilmMode() || this.mListener == null)) {
            this.mListener.onSlidePicture();
        }
        this.mSlideToNext = false;
        return true;
    }

    private static int gapToSide(int imageWidth, int viewWidth) {
        return Math.max(0, (viewWidth - imageWidth) / 2);
    }

    public void autoSlidePicture(ExtraActionListener listener) {
        if (this.mFilmMode) {
            GalleryLog.d("PhotoView", "auto slide picture, film mode:" + this.mFilmMode);
            listener.onExecuteExtraActionEnd();
            return;
        }
        boolean z;
        if (this.mNextBound > 0 || this.mPrevBound < 0) {
            z = true;
        } else {
            z = false;
        }
        this.mAutoSlideMode = z;
        boolean ret = this.mSlideToNext ? !slideToNextPicture() ? slideToPrevPicture() : true : !slideToPrevPicture() ? slideToNextPicture() : true;
        if (ret) {
            this.mExtraActionListener = listener;
            setSwipingEnabled(false);
            this.mHandler.sendEmptyMessageDelayed(9, 800);
            this.mNeedUnlockSwipeByDeletePhoto = true;
            this.mHandler.sendEmptyMessageDelayed(10, 1500);
        } else {
            GalleryLog.d("PhotoView", "auto slide picture, slide false");
            listener.onExecuteExtraActionEnd();
        }
    }

    private void switchToNextImage() {
        this.mModel.moveTo(this.mModel.getCurrentIndex() + 1, this.mPositionController.getFilmRatio() == 0.0f ? ((Picture) this.mPictures.get(1)).calculateRectOffset() : 0);
    }

    private void switchToPrevImage() {
        this.mModel.moveTo(this.mModel.getCurrentIndex() - 1, this.mPositionController.getFilmRatio() == 0.0f ? ((Picture) this.mPictures.get(-1)).calculateRectOffset() : 0);
    }

    private static float calculateMoveOutProgress(int left, int right, int viewWidth) {
        int w = right - left;
        if (w < viewWidth) {
            int zx = (viewWidth / 2) - (w / 2);
            if (left > zx) {
                return ((float) (-(left - zx))) / ((float) (viewWidth - zx));
            }
            return ((float) (left - zx)) / ((float) ((-w) - zx));
        } else if (left > 0) {
            return ((float) (-left)) / ((float) viewWidth);
        } else {
            if (right < viewWidth) {
                return ((float) (viewWidth - right)) / ((float) viewWidth);
            }
            return 0.0f;
        }
    }

    private float getScrollAlpha(float scrollProgress) {
        return scrollProgress < 0.0f ? this.mAlphaInterpolator.getInterpolation(WMElement.CAMERASIZEVALUE1B1 - Math.abs(scrollProgress)) : WMElement.CAMERASIZEVALUE1B1;
    }

    private float getScrollScale(float scrollProgress) {
        float interpolatedProgress = this.mScaleInterpolator.getInterpolation(Math.abs(scrollProgress));
        return (WMElement.CAMERASIZEVALUE1B1 - interpolatedProgress) + (0.74f * interpolatedProgress);
    }

    private static float interpolate(float ratio, float from, float to) {
        return (((to - from) * ratio) * ratio) + from;
    }

    private float getOffsetAlpha(float offset) {
        offset /= 0.5f;
        return Utils.clamp(offset > 0.0f ? WMElement.CAMERASIZEVALUE1B1 - offset : WMElement.CAMERASIZEVALUE1B1 + offset, 0.03f, (float) WMElement.CAMERASIZEVALUE1B1);
    }

    public void setSimpleGestureListener(SimpleGestureListener listner) {
        this.mSimpleGestureListner = listner;
    }

    public MediaItem getMediaItem(int offset) {
        if (offset < -3 || offset > 3) {
            return null;
        }
        return ((Picture) this.mPictures.get(offset)).getMediaItem();
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public Rect getPhotoRect(int index) {
        return this.mPositionController.getPosition(index);
    }

    public PhotoFallbackEffect buildFallbackEffect(GLView root, GLCanvas canvas) {
        Rect location = new Rect();
        Utils.assertTrue(root.getBoundsOf(this, location));
        Rect fullRect = bounds();
        PhotoFallbackEffect effect = new PhotoFallbackEffect();
        for (int i = -3; i <= 3; i++) {
            MediaItem item = this.mModel.getMediaItem(i);
            if (item != null) {
                ScreenNail sc = this.mModel.getScreenNail(i);
                if ((sc instanceof TiledScreenNail) && !((TiledScreenNail) sc).isShowingPlaceholder()) {
                    Rect rect = new Rect(getPhotoRect(i));
                    if (Rect.intersects(fullRect, rect)) {
                        RawTexture texture;
                        rect.offset(location.left, location.top);
                        int width = sc.getWidth();
                        int height = sc.getHeight();
                        int rotation = this.mModel.getImageRotation(i);
                        if (rotation % 180 == 0) {
                            texture = new RawTexture(width, height, true);
                            canvas.beginRenderTarget(texture);
                            canvas.translate(((float) width) / 2.0f, ((float) height) / 2.0f);
                        } else {
                            texture = new RawTexture(height, width, true);
                            canvas.beginRenderTarget(texture);
                            canvas.translate(((float) height) / 2.0f, ((float) width) / 2.0f);
                        }
                        canvas.rotate((float) rotation, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
                        canvas.translate(((float) (-width)) / 2.0f, ((float) (-height)) / 2.0f);
                        sc.draw(canvas, 0, 0, width, height);
                        canvas.endRenderTarget();
                        effect.addEntry(item.getPath(), rect, texture, i);
                    }
                }
            }
        }
        return effect;
    }

    private void parseGifIfNecessary(MediaItem item, ScreenNail screenNail) {
        if (this.mGifThread != null) {
            this.mGifThread.interrupt();
            this.mGifThread = null;
        }
        if (item != null && "image/gif".equals(item.getMimeType())) {
            this.mGifThread = new GifParseThread(this.mContext, item, this.mTileView, screenNail);
            this.mGifThread.start();
        }
    }

    public void setMediaItemScreenNail(MediaItem mediaItem, long startFromCamera) {
        TraceController.traceBegin("PhotoView.setMediaItemScreenNail startFromCamera=" + startFromCamera);
        MediaItemDirectShowNail directShowNail = new MediaItemDirectShowNail(this, mediaItem);
        directShowNail.setStartClickFromCameraTime(startFromCamera);
        this.mTileView.setDirectShowNail(directShowNail);
        TraceController.traceEnd();
    }

    public void setMediaItemScreenNail(MediaItem mediaItem) {
        TraceController.traceBegin("PhotoView.setMediaItemScreenNail");
        this.mTileView.setDirectShowNail(new MediaItemDirectShowNail(this, mediaItem));
        TraceController.traceEnd();
    }

    public boolean isTileViewFromCache() {
        if (this.mTileView != null) {
            return this.mTileView.isScreenNailFromCache();
        }
        return false;
    }

    public boolean isCamera() {
        if (this.mModel == null) {
            return false;
        }
        return this.mModel.isCamera(0);
    }

    private void onExtraActionEnd() {
        GalleryLog.d("PhotoView", "on extra action end, delete animation end, auto slide mode:" + this.mAutoSlideMode);
        if (this.mAutoSlideMode) {
            if (this.mExtraActionListener != null) {
                this.mHandler.removeMessages(9);
                this.mExtraActionListener.onExecuteExtraActionEnd();
                this.mExtraActionListener = null;
            }
            this.mAutoSlideMode = false;
        }
    }

    public boolean isExtraActionDoing() {
        return this.mAutoSlideMode;
    }

    public boolean resetToFullView() {
        if (this.mPositionController.isFillShortScale()) {
            return false;
        }
        this.mPositionController.resetToFullView();
        this.mNeedRubberBandEffectEdge = 0;
        this.mDoneRubberBandEffectEdge = 0;
        return true;
    }

    public void onDeleteDelay() {
        if (this.mHandler.hasMessages(9)) {
            onExtraActionEnd();
        }
    }

    public void onDataUpdate() {
        if (this.mNeedUnlockSwipeByDeletePhoto) {
            this.mNeedUnlockSwipeByDeletePhoto = false;
            this.mHandler.removeMessages(10);
            setSwipingEnabled(true);
        }
    }

    public void prepareTextures() {
        this.mTileView.prepareTextures();
    }

    public void freeTextures() {
        this.mTileView.freeTextures();
    }

    private void clearAnimationProxyView(ScreenNail s) {
        GLRoot root = getGLRoot();
        if (s != null && root != null) {
            root.clearAnimationProxyView(false);
        }
    }
}

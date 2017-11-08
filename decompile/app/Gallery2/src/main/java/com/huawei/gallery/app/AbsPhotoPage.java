package com.huawei.gallery.app;

import android.app.Activity;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings.System;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.Window;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.app.PhotoDataAdapter;
import com.android.gallery3d.app.PhotoDataAdapter.DataListener;
import com.android.gallery3d.app.SinglePhotoDataAdapter;
import com.android.gallery3d.app.TransitionStore;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.data.UriAlbum;
import com.android.gallery3d.settings.GallerySettings;
import com.android.gallery3d.ui.AbsPhotoView;
import com.android.gallery3d.ui.AbsPhotoView.Listener;
import com.android.gallery3d.ui.BitmapScreenNail;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.GLRoot.OnGLIdleListener;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.MenuExecutor;
import com.android.gallery3d.ui.PhotoView;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.ui.TiledScreenNail;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ReportToBigData;
import com.android.gallery3d.util.TraceController;
import com.fyusion.sdk.common.FyuseSDK;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.GalleryActionBar;
import com.huawei.gallery.anim.PhotoFallbackEffect;
import com.huawei.gallery.util.UIUtils;
import java.util.List;

public abstract class AbsPhotoPage extends ActivityState implements Listener {
    protected static final int HIDE_BARS_TIMEOUT = GalleryUtils.getDelayTime(3500);
    private String clientid = "h84GfImnOp9sd44UyIfEwT";
    private String clientsecret = "2OJS2V_kk3f0h82khgwr9ghbrwuibgn4";
    protected GalleryActionBar mActionBar;
    protected volatile boolean mActionBarAllowed = true;
    private Handler mAsyncProcessingHandler;
    protected int mCurrentIndex = 0;
    protected MediaItem mCurrentPhoto;
    protected long mCurrentPhotoVersion = -1;
    private DataListener mDataListener = new DataListener() {
        public void onLoadingStarted() {
            AbsPhotoPage.this.onLoadingStarted();
        }

        public void onLoadingFinished(boolean loadingFailed) {
            AbsPhotoPage.this.onLoadingFinished(loadingFailed);
        }

        public void onVisibleRangeLoadFinished() {
        }

        public void onPhotoChanged(int index, Path item) {
            TraceController.traceBegin("AbsPhotoPage.DataListener.onPhotoChanged");
            AbsPhotoPage.this.onPhotoChanged(index, item);
            TraceController.traceEnd();
        }
    };
    private long mDeferUpdateUntil = Long.MAX_VALUE;
    private boolean mDeferredUpdateWaiting = false;
    protected Handler mHandler;
    private HandlerThread mHandlerThread;
    protected boolean mIsActive;
    protected MediaSet mMediaSet;
    protected MenuExecutor mMenuExecutor;
    protected boolean mMenuVisible = false;
    protected Model mModel;
    private int mNavigationBarColor;
    protected MyOrientationManager mOrientationManager;
    protected String mOriginalSetPathString;
    protected AbsPhotoView mPhotoView;
    protected final GLView mRootPane = new GLView() {
        protected void onLayout(boolean changeSize, int left, int top, int right, int bottom) {
            AbsPhotoPage.this.onGLRootLayout(changeSize, left, top, right, bottom);
        }
    };
    protected SelectionManager mSelectionManager;
    protected String mSetPathString;
    protected boolean mShowBars = true;
    private int mStatusBarColor;
    protected final OnSystemUiVisibilityChangeListener mSystemUiVisibilityChangeListener = new OnSystemUiVisibilityChangeListener() {
        public void onSystemUiVisibilityChange(int visibility) {
            if (visibility == 0 && !AbsPhotoPage.this.mMenuVisible) {
                AbsPhotoPage.this.mHandler.removeMessages(1);
                AbsPhotoPage.this.mHandler.sendEmptyMessageDelayed(1, (long) AbsPhotoPage.HIDE_BARS_TIMEOUT);
            }
        }
    };
    protected String mTitle;

    public interface Model extends com.android.gallery3d.ui.AbsPhotoView.Model {
        void invalidateData(Bitmap bitmap);

        void invalidateData(BitmapScreenNail bitmapScreenNail);

        void invalidateData(byte[] bArr, int i, int i2);

        boolean isEmpty();

        void pause();

        void resume();

        void resume(byte[] bArr, int i, int i2);

        void setCurrentPhoto(Path path, int i);
    }

    protected class MyOrientationManager extends ContentObserver {
        protected boolean mNeedResetOrientation = false;
        private int mOriginRequestedOrientation = -1;

        public MyOrientationManager(Handler handler) {
            super(handler);
        }

        public void resume() {
            AbsPhotoPage.this.mHost.getActivity().getContentResolver().registerContentObserver(System.getUriFor("accelerometer_rotation"), false, this);
            this.mOriginRequestedOrientation = AbsPhotoPage.this.mHost.getActivity().getRequestedOrientation();
            onChange(true);
        }

        public void pause() {
            AbsPhotoPage.this.mHost.getActivity().getContentResolver().unregisterContentObserver(this);
            if (this.mNeedResetOrientation) {
                this.mNeedResetOrientation = false;
                AbsPhotoPage.this.mHost.getActivity().setRequestedOrientation(this.mOriginRequestedOrientation);
            }
        }

        public void onChange(boolean selfChange) {
            int i;
            boolean z = false;
            boolean rotationLocked = System.getInt(AbsPhotoPage.this.mHost.getActivity().getContentResolver(), "accelerometer_rotation", 0) == 0;
            boolean alwaysLock = GallerySettings.isAlwaysRotateSettingEnabled(AbsPhotoPage.this.mHost.getActivity());
            if (rotationLocked) {
                z = alwaysLock;
            }
            this.mNeedResetOrientation = z;
            FragmentActivity activity = AbsPhotoPage.this.mHost.getActivity();
            if (rotationLocked && alwaysLock) {
                i = 4;
            } else {
                i = -1;
            }
            activity.setRequestedOrientation(i);
        }
    }

    private class PreparePhotoFallback implements OnGLIdleListener {
        private boolean mAlreadyForceInterrupted;
        private PhotoFallbackEffect mPhotoFallback;
        private boolean mResultReady;

        private PreparePhotoFallback() {
            this.mPhotoFallback = new PhotoFallbackEffect();
            this.mResultReady = false;
            this.mAlreadyForceInterrupted = false;
        }

        public synchronized PhotoFallbackEffect get() {
            int roundCount = 0;
            do {
                if (this.mResultReady) {
                    break;
                }
                Utils.waitWithoutInterrupt(this, 100);
                roundCount++;
            } while (roundCount < 30);
            this.mAlreadyForceInterrupted = true;
            return this.mPhotoFallback;
        }

        public boolean onGLIdle(GLCanvas canvas, boolean renderRequested) {
            synchronized (this) {
                if (this.mAlreadyForceInterrupted) {
                    return false;
                }
                this.mPhotoFallback = AbsPhotoPage.this.mPhotoView.buildFallbackEffect(AbsPhotoPage.this.mRootPane, canvas);
                this.mResultReady = true;
                notifyAll();
                return false;
            }
        }
    }

    protected abstract void onGLRootLayout(boolean z, int i, int i2, int i3, int i4);

    protected void onCreate(Bundle data, Bundle storedState) {
        TraceController.traceBegin("AbsPhotoPage.onCreate");
        super.onCreate(data, storedState);
        if (ApiHelper.HAS_MODIFY_STATUS_BAR_COLOR) {
            Window window = this.mHost.getActivity().getWindow();
            this.mStatusBarColor = UIUtils.getStatusBarColor(window);
            UIUtils.setStatusBarColor(window, 0);
            this.mNavigationBarColor = UIUtils.getNavigationBarColor(window);
            UIUtils.setNavigationBarColor(window, 0);
        }
        this.mFlags |= 48;
        TraceController.traceBegin("AbsPhotoPage.onCreate.getActionbar");
        this.mActionBar = this.mHost.getGalleryActionBar();
        TraceController.endSection();
        GalleryContext context = this.mHost.getGalleryContext();
        FyuseSDK.initWithUserConsent(context.getActivityContext(), this.clientid, this.clientsecret);
        this.mOrientationManager = new MyOrientationManager(null);
        TraceController.traceBegin("AbsPhotoPage.onCreate.createSelectionManager");
        this.mSelectionManager = createSelectionManager(context);
        TraceController.endSection();
        this.mMenuExecutor = new MenuExecutor(context, this.mSelectionManager);
        this.mMenuExecutor.setGLRoot(context.getGLRoot());
        this.mPhotoView = createPhotoView(context, this.mHost.getGLRoot());
        if (storedState != null) {
            this.mPhotoView.freeTextures();
        }
        this.mPhotoView.setListener(this);
        this.mRootPane.addComponent(this.mPhotoView);
        this.mSetPathString = this.mData.getString("media-set-path");
        this.mOriginalSetPathString = this.mSetPathString;
        this.mCurrentIndex = this.mData.getInt("index-hint", 0);
        this.mHandler = new SynchronizedHandler(this.mHost.getGLRoot()) {
            public void handleMessage(Message msg) {
                AbsPhotoPage.this.onHandleMessage(msg);
            }
        };
        this.mHandlerThread = new HandlerThread("AbsPhotoPageAsynchronousHandler", -2);
        this.mHandlerThread.start();
        this.mAsyncProcessingHandler = new Handler(this.mHandlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 6:
                        TraceController.traceBegin("AbsPhotoPage.MSG_UNFREEZE_GLROOT, lock gl thread");
                        AbsPhotoPage.this.mHost.getGLRoot().unfreeze();
                        TraceController.traceEnd();
                        return;
                    default:
                        return;
                }
            }
        };
        initializeData(data);
        TraceController.traceEnd();
    }

    protected void resetFlag() {
        this.mFlags &= -49;
    }

    protected SelectionManager createSelectionManager(GalleryContext context) {
        return new SelectionManager(context, false);
    }

    protected void onHandleMessage(Message msg) {
        Object obj = null;
        switch (msg.what) {
            case 1:
                TraceController.traceBegin("AbsPhotoPage.MSG_HIDE_BARS, lock gl thread");
                boolean hasWindowFocus = true;
                Activity activity = this.mHost.getActivity();
                if (activity != null) {
                    hasWindowFocus = activity.hasWindowFocus();
                }
                if (hasWindowFocus) {
                    hideBars(true);
                } else {
                    refreshHidingMessage();
                }
                TraceController.traceEnd();
                return;
            case 14:
                TraceController.traceBegin("AbsPhotoPage.MSG_UPDATE_DEFERRED, lock gl thread");
                long nextUpdate = this.mDeferUpdateUntil - SystemClock.uptimeMillis();
                if (nextUpdate <= 0) {
                    this.mDeferredUpdateWaiting = false;
                    updateUIForCurrentPhoto();
                } else {
                    this.mHandler.sendEmptyMessageDelayed(14, nextUpdate);
                }
                TraceController.traceEnd();
                return;
            case 49:
                TraceController.traceBegin("AbsPhotoPage.MSG_GOTO_GALLERY, lock gl thread");
                Intent intent = new Intent(this.mHost.getActivity(), GalleryMain.class);
                intent.putExtra("key-no-page-history", true);
                this.mHost.getActivity().startActivity(intent);
                this.mHost.getActivity().finish();
                this.mHost.getActivity().overridePendingTransition(0, 0);
                TraceController.traceEnd();
                return;
            case 50:
                if (this.mIsActive) {
                    TraceController.traceBegin("AbsPhotoPage.MSG_FINISH_STATE, lock gl thread");
                    if (msg.arg1 == 1) {
                        this.mHost.getTransitionStore().put(AbsAlbumPage.KEY_PHOTO_PAGE_IS_EMPTY, Boolean.valueOf(true));
                        TransitionStore transitionStore = this.mHost.getTransitionStore();
                        String str = AbsAlbumPage.KEY_PHOTO_PAGE_MEDIA_SET_PATH;
                        if (this.mMediaSet != null) {
                            obj = this.mMediaSet.getPath();
                        }
                        transitionStore.put(str, obj);
                    }
                    this.mHost.getStateManager().finishState(this);
                    TraceController.traceEnd();
                    return;
                }
                return;
            case 100:
                startSlideShow();
                return;
            default:
                throw new AssertionError(msg.what);
        }
    }

    protected void initializeData(Bundle data) {
        TraceController.traceBegin("AbsPhotoPage.initializeData");
        boolean fromCamera = data.getBoolean("local-merge-camera-album", false);
        Path itemPath = getItemPath(data);
        if (this.mSetPathString != null) {
            TraceController.traceBegin("AbsPhotoPage.initializeData.getMediaSet");
            this.mMediaSet = this.mHost.getGalleryContext().getDataManager().getMediaSet(this.mSetPathString);
            TraceController.traceEnd();
            this.mSelectionManager.setSourceMediaSet(this.mMediaSet);
            if (itemPath == null) {
                if (fromCamera) {
                    TraceController.traceBegin("AbsPhotoPage.initializeData.mMediaSet.reload");
                    this.mMediaSet.reload();
                    TraceController.traceEnd();
                }
                TraceController.traceBegin("AbsPhotoPage.initializeData.getMediaItemCount");
                int mediaItemCount = fromCamera ? 1 : this.mMediaSet.getMediaItemCount();
                TraceController.traceEnd();
                if (mediaItemCount > 0) {
                    if (this.mCurrentIndex >= mediaItemCount) {
                        this.mCurrentIndex = 0;
                    }
                    TraceController.traceBegin("AbsPhotoPage.initializeData.mMediaSet.getMediaItem");
                    List<MediaItem> itemList = this.mMediaSet.getMediaItem(this.mCurrentIndex, 1);
                    TraceController.traceEnd();
                    if (itemList.isEmpty()) {
                        TraceController.traceEnd();
                        return;
                    }
                    itemPath = ((MediaItem) itemList.get(0)).getPath();
                    if (itemPath == null) {
                        TraceController.traceEnd();
                        return;
                    }
                }
                TraceController.traceEnd();
                return;
            }
            setProxyBitmap(data, itemPath);
            if (this.mMediaSet instanceof UriAlbum) {
                UriAlbum album = this.mMediaSet;
                album.initMediaItem(this.mHost.getActivity().getIntent());
                album.addItemPath(itemPath);
                this.mTitle = data.getString("android.intent.extra.TITLE");
            }
            PhotoDataAdapter pda = new PhotoDataAdapter(this.mHost.getGalleryContext(), this.mHost.getGLRoot(), this.mPhotoView, this.mMediaSet, itemPath, this.mCurrentIndex, -1, false, false, this.mData.getInt("media-count", fromCamera ? 1 : 0));
            this.mModel = pda;
            this.mPhotoView.setModel(this.mModel);
            pda.setFromCamera(fromCamera);
            pda.setDataListener(this.mDataListener);
        } else if (itemPath == null) {
            ContextedUtils.showToastQuickly(this.mHost.getActivity(), (int) R.string.fail_to_load_image_Toast, 1);
            TraceController.traceEnd();
            return;
        } else {
            TraceController.traceBegin("AbsPhotoPage.initializeData.getMediaObject 2nd");
            MediaItem mediaItem = (MediaItem) this.mHost.getGalleryContext().getDataManager().getMediaObject(itemPath);
            TraceController.traceEnd();
            if (mediaItem == null) {
                ContextedUtils.showToastQuickly(this.mHost.getActivity(), (int) R.string.fail_to_load_image_Toast, 1);
                TraceController.traceEnd();
                return;
            }
            this.mModel = new SinglePhotoDataAdapter(this.mHost.getGalleryContext(), this.mHost.getGLRoot(), this.mPhotoView, mediaItem);
            this.mPhotoView.setModel(this.mModel);
            updateCurrentPhoto(mediaItem);
        }
        TraceController.traceEnd();
    }

    private void setProxyBitmap(Bundle data, Path itemPath) {
        GLRoot glRoot = this.mHost.getGLRoot();
        if (!(glRoot != null ? glRoot.hasAnimationProxyView() : false)) {
            TraceController.traceBegin("AbsPhotoPage.initializeData.getMediaObject");
            MediaItem item = (MediaItem) this.mHost.getGalleryContext().getDataManager().getMediaObject(itemPath);
            TraceController.traceEnd();
            if (item != null) {
                Bitmap bitmap = (Bitmap) data.getParcelable("media-set-bitmap");
                data.remove("media-set-bitmap");
                if (bitmap != null) {
                    item.setScreenNailBitmapProxy(bitmap);
                }
                TraceController.traceBegin("AbsPhotoPage.initializeData.setMediaItemScreenNail");
                this.mPhotoView.setMediaItemScreenNail(item, data.getLong("camera_to_gallery_time", 0));
                TraceController.traceEnd();
            }
        }
    }

    protected Path getItemPath(Bundle data) {
        String itemPathString = data.getString("media-item-path");
        if (itemPathString != null) {
            return Path.fromString(itemPathString);
        }
        return null;
    }

    protected boolean updateCurrentPhoto(MediaItem photo) {
        TraceController.traceBegin("AbsPhotoPage.updateCurrentPhoto");
        if (this.mCurrentPhoto == photo) {
            updateTitle();
            TraceController.traceEnd();
            return false;
        }
        this.mCurrentPhoto = photo;
        this.mCurrentPhotoVersion = this.mCurrentPhoto.getDataVersion();
        TraceController.traceEnd();
        return true;
    }

    protected void updateTitle() {
    }

    protected void updateUIForCurrentPhoto() {
    }

    protected void onResume() {
        TraceController.traceBegin("AbsPhotoPage.onResume");
        super.onResume();
        if (ApiHelper.HAS_MODIFY_STATUS_BAR_COLOR) {
            Window window = this.mHost.getActivity().getWindow();
            UIUtils.setStatusBarColor(window, 0);
            UIUtils.setNavigationBarColor(window, 0);
        }
        this.mOrientationManager.resume();
        this.mIsActive = true;
        if (this.mModel == null) {
            if (this.mData.getBoolean("is-kids-mode", false) || !this.mData.getBoolean("local-merge-camera-album", false) || this.mData.getBoolean("is-secure-camera-album", false)) {
                this.mHandler.sendEmptyMessageDelayed(50, 100);
            } else {
                this.mHandler.sendEmptyMessageDelayed(49, 100);
            }
            TraceController.traceEnd();
            return;
        }
        this.mHost.getGLRoot().freeze();
        setContentPane(this.mRootPane);
        onModelResume();
        this.mPhotoView.resume();
        this.mAsyncProcessingHandler.sendEmptyMessageDelayed(6, 150);
        TraceController.traceEnd();
    }

    protected boolean onItemSelected(Action action) {
        this.mMenuVisible = false;
        return false;
    }

    protected final Intent getDefaultResult() {
        Intent result = new Intent();
        result.putExtra("return-index-hint", this.mCurrentIndex);
        return result;
    }

    protected void onPause() {
        super.onPause();
        this.mOrientationManager.pause();
        this.mIsActive = false;
        this.mHost.getGLRoot().unfreeze();
        this.mAsyncProcessingHandler.removeMessages(6);
        if (this.mModel != null) {
            if (isFinishing()) {
                preparePhotoFallbackView();
            }
            this.mModel.pause();
        }
        this.mPhotoView.pause();
        this.mMenuExecutor.pause();
        if (!(this.mActionBar == null || this.mActionBar.getCurrentMode() == null)) {
            this.mActionBar.getCurrentMode().hide();
        }
        this.mHandler.removeMessages(1);
    }

    protected void onDestroy() {
        super.onDestroy();
        if (ApiHelper.HAS_MODIFY_STATUS_BAR_COLOR) {
            Window window = this.mHost.getActivity().getWindow();
            UIUtils.setStatusBarColor(window, this.mStatusBarColor);
            UIUtils.setNavigationBarColor(window, this.mNavigationBarColor);
        }
        this.mHandlerThread.quitSafely();
        this.mOrientationManager = null;
        this.mHandler.removeCallbacksAndMessages(null);
        this.mMenuExecutor.dismissAlertDialog();
        GLRoot glRoot = this.mHost.getGLRoot();
        if (glRoot != null) {
            glRoot.clearAnimationProxyView(true);
        }
    }

    protected void toggleBars() {
        if (this.mShowBars) {
            hideBars(true);
        } else if (canShowBars()) {
            ReportToBigData.report(139);
            showBars(true);
        }
    }

    protected void showBars(boolean barWithAnim) {
    }

    protected void hideBars(boolean barWithAnim) {
    }

    private boolean canShowBars() {
        return this.mActionBarAllowed;
    }

    protected void refreshHidingMessage() {
        this.mHandler.removeMessages(1);
        if (!this.mPhotoView.getFilmMode()) {
            this.mHandler.sendEmptyMessageDelayed(1, (long) HIDE_BARS_TIMEOUT);
        }
    }

    protected void requestDeferredUpdate() {
        this.mDeferUpdateUntil = SystemClock.uptimeMillis() + 250;
        if (!this.mDeferredUpdateWaiting) {
            this.mDeferredUpdateWaiting = true;
            this.mHandler.sendEmptyMessageDelayed(14, 250);
        }
    }

    protected boolean isValidImage() {
        if (this.mCurrentPhoto == null || this.mCurrentPhoto.getMediaType() != 2) {
            return false;
        }
        return true;
    }

    private void preparePhotoFallbackView() {
        GLRoot root = this.mHost.getGLRoot();
        PreparePhotoFallback task = new PreparePhotoFallback();
        root.unlockRenderThread();
        Object anim = null;
        try {
            if (root.addOnGLIdleListener(task)) {
                anim = task.get();
            }
            root.lockRenderThread();
            this.mHost.getTransitionStore().put(AbsAlbumPage.KEY_RESUME_ANIMATION, anim);
        } catch (Throwable th) {
            root.lockRenderThread();
        }
    }

    protected void onLoadingStarted() {
        GalleryLog.d("AbsPhotoPage", "onLoadingStarted");
    }

    protected void onLoadingFinished(boolean loadingFailed) {
        GalleryLog.d("AbsPhotoPage", "onLoadingFinished:" + loadingFailed);
        TiledScreenNail.enableDrawPlaceholder();
        if (this.mModel.isEmpty()) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(50, 1, 0));
        }
    }

    protected void onPhotoChanged(int index, Path item) {
        this.mCurrentIndex = index;
        if (item != null) {
            MediaItem photo = this.mModel.getMediaItem(0);
            if (photo != null) {
                updateCurrentPhoto(photo);
                GalleryLog.d("AbsPhotoPage", "current will be: index = " + index + ", path:" + photo.getFilePath());
            }
        }
    }

    protected boolean onKeyDown(int keyCode, KeyEvent event) {
        if (onFingprintKeyActivated()) {
            if (513 == keyCode) {
                this.mPhotoView.switchPictureByFingerprintKey(true);
                return true;
            } else if (514 == keyCode) {
                this.mPhotoView.switchPictureByFingerprintKey(false);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    protected boolean onKeyUp(int keyCode, KeyEvent event) {
        if (onFingprintKeyActivated() && (513 == keyCode || 514 == keyCode)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    protected boolean onFingprintKeyActivated() {
        return false;
    }

    public void onSingleTapUp(int x, int y) {
    }

    public void onActionBarAllowed(boolean allowed) {
        this.mActionBarAllowed = allowed;
    }

    public void onActionBarWanted() {
    }

    public void onSlidePicture() {
        hideBars(true);
    }

    public void onCurrentImageUpdated() {
    }

    public void onDeleteImage(Path path, int offset) {
    }

    public void onCommitDeleteImage() {
    }

    public void onFilmModeChanged(boolean enabled) {
    }

    public void onPictureCenter(boolean isCamera) {
    }

    public void onUndoBarVisibilityChanged(boolean visible) {
    }

    public void onFlingUp() {
    }

    public void onFlingDown() {
    }

    public void onLoadStateChange(int state) {
    }

    public boolean calledToSimpleEditor() {
        return false;
    }

    public void onPictureFullView() {
    }

    public void onScroll(float dx, float dy, float totalX, float totalY) {
    }

    public void onPhotoTranslationChange(float x, float y, int index, boolean visible, MediaItem item) {
    }

    public void onSnapback() {
    }

    public void onSwipeImages(float velocityX, float velocityY) {
    }

    public void onEnterPhotoMagnifierMode() {
    }

    public void onLeavePhotoMagnifierMode() {
    }

    public boolean isDetailsShow() {
        return false;
    }

    public boolean inEditorMode() {
        return false;
    }

    public boolean inBurstMode() {
        return false;
    }

    public void onRenderFinish() {
    }

    protected void onModelResume() {
        this.mModel.resume();
    }

    protected boolean needClearAnimationProxyViewWhenResume() {
        return false;
    }

    public void onTouchEventReceived(MotionEvent event) {
    }

    protected void startSlideShow() {
        if (this.mMediaSet != null && this.mModel != null && this.mIsActive) {
            MediaItem current = this.mModel.getMediaItem(0);
            if (current != null) {
                Path path = current.getPath();
                Bundle data = new Bundle();
                data.putString("media-set-path", this.mMediaSet.getPath().toString());
                data.putString("media-item-path", path.toString());
                data.putInt("photo-index", this.mModel.getCurrentIndex());
                data.putString("media-preview-item-path", path.toString());
                data.putInt("media-preview-item-index", this.mModel.getCurrentIndex());
                data.putBoolean("repeat", true);
                this.mHost.getStateManager().startStateForResult(SlideShowPage.class, 1, data);
            }
        }
    }

    protected void onStateResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (data != null) {
                    String path = data.getStringExtra("media-item-path");
                    int index = data.getIntExtra("photo-index", 0);
                    if (path != null) {
                        updateCurrentPhoto((MediaItem) this.mHost.getGalleryContext().getDataManager().getMediaObject(path));
                        this.mModel.setCurrentPhoto(Path.fromString(path), index);
                        return;
                    }
                    return;
                }
                return;
            default:
                super.onStateResult(requestCode, resultCode, data);
                return;
        }
    }

    protected AbsPhotoView createPhotoView(GalleryContext context, GLRoot root) {
        return new PhotoView(context, root);
    }
}

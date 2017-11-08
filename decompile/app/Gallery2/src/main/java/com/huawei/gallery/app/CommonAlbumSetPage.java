package com.huawei.gallery.app;

import android.annotation.SuppressLint;
import android.app.AbsWallpaperManager.IBlurWallpaperCallback;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MotionEvent;
import com.android.gallery3d.R;
import com.android.gallery3d.app.Config$CommonAlbumSetPage;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.app.LoadingListener;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.AlbumSlotScrollBarView;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.ui.SelectionManager.SelectionListener;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MultiWindowStatusHolder;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.GalleryActionBar;
import com.huawei.gallery.actionbar.StandardTitleActionMode;
import com.huawei.gallery.ui.CommonAlbumSetSlotRender;
import com.huawei.gallery.ui.CommonAlbumSetSlotView;
import com.huawei.gallery.ui.CommonAlbumSlotView.Listener;
import com.huawei.gallery.ui.PlaceHolderView;
import com.huawei.gallery.util.LayoutHelper;
import java.lang.reflect.InvocationTargetException;

public class CommonAlbumSetPage extends ActivityState implements SelectionListener, IBlurWallpaperCallback, Listener {
    protected GalleryActionBar mActionBar;
    protected CommonAlbumSetSlotRender mAlbumRender;
    protected CommonAlbumSetDataLoader mDataLoader;
    protected boolean mGetContent;
    protected Handler mHandler;
    protected boolean mIsActive = false;
    protected MediaSet mMediaSet;
    protected Path mMediaSetPath;
    protected final GLView mRootPane = new GLView() {
        private final float[] mMatrix = new float[16];

        protected void onLayout(boolean changeSize, int left, int top, int right, int bottom) {
            CommonAlbumSetPage.this.onGLRootLayout(left, top, right, bottom);
            GalleryUtils.setViewPointMatrix(this.mMatrix, ((float) (right - left)) / 2.0f, ((float) (bottom - top)) / 2.0f, -CommonAlbumSetPage.this.mUserDistance);
        }

        protected void render(GLCanvas canvas) {
            canvas.save(2);
            canvas.multiplyMatrix(this.mMatrix, 0);
            super.render(canvas);
            canvas.restore();
        }

        public Rect getAnimRect() {
            return null;
        }
    };
    protected AlbumSlotScrollBarView mScrollBar;
    protected SelectionManager mSelectionManager;
    protected CommonAlbumSetSlotView mSlotView;
    protected PlaceHolderView mTopCover;
    protected String mUnNamedString;
    private final float mUserDistance = ((float) GalleryUtils.meterToPixel(0.3f));

    protected class DataLoaderListener implements LoadingListener {
        protected DataLoaderListener() {
        }

        public void onLoadingStarted() {
            CommonAlbumSetPage.this.onLoadingStarted();
        }

        public void onLoadingFinished(boolean loadingFailed) {
            CommonAlbumSetPage.this.onLoadingFinished(loadingFailed);
        }

        public void onVisibleRangeLoadFinished() {
            CommonAlbumSetPage.this.onVisibleRangeLoadFinished();
        }
    }

    protected boolean onCreateActionBar(Menu menu) {
        StandardTitleActionMode am = this.mActionBar.enterStandardTitleActionMode(false);
        am.setTitle(this.mMediaSet.getName());
        am.setBothAction(Action.NONE, Action.NONE);
        am.show();
        this.mHost.requestFeature(298);
        return true;
    }

    protected void onGLRootLayout(int left, int top, int right, int bottom) {
        this.mAlbumRender.setHighlightItemPath(null);
        int w = right - left;
        int h = bottom - top;
        boolean isPort = LayoutHelper.isPort();
        int navigationBarHeight = LayoutHelper.getNavigationBarHeight();
        int paddingTop = (MultiWindowStatusHolder.isInMultiMaintained() ? 0 : LayoutHelper.getStatusBarHeight()) + this.mActionBar.getActionBarHeight();
        int paddingRight = isPort ? 0 : navigationBarHeight;
        int paddingBottom = (!isPort || MultiWindowStatusHolder.isInMultiWindowMode()) ? 0 : navigationBarHeight;
        this.mSlotView.layout(0, paddingTop, w - paddingRight, h - paddingBottom);
        this.mScrollBar.layout(0, paddingTop, w - paddingRight, h - paddingBottom);
        this.mTopCover.layout(left, 0, right, paddingTop);
    }

    protected CommonAlbumSetSlotView onCreateSlotView() {
        return new CommonAlbumSetSlotView(this.mHost.getGalleryContext(), Config$CommonAlbumSetPage.get(this.mHost.getActivity()).slotViewSpec);
    }

    protected CommonAlbumSetDataLoader onCreateDataLoader(MediaSet mediaSet) {
        return new CommonAlbumSetDataLoader(mediaSet, 256);
    }

    protected CommonAlbumSetSlotRender onCreateSlotRender() {
        return new CommonAlbumSetSlotRender(this.mHost.getGalleryContext(), this.mSlotView, this.mSelectionManager, Config$CommonAlbumSetPage.get(this.mHost.getActivity()).placeholderColor);
    }

    public void onDown(int index) {
        this.mAlbumRender.setPressedIndex(index);
    }

    public void onUp(boolean followedByLongPress) {
        this.mAlbumRender.setPressedIndex(-1);
    }

    public void onSingleTapUp(int index, boolean cornerPressed) {
    }

    public void onLongTap(int index) {
    }

    public void onScrollPositionChanged(int position, int total) {
    }

    public void onScroll(int index) {
    }

    public boolean inSelectionMode() {
        return this.mSelectionManager.inSelectionMode();
    }

    public void onTouchUp(MotionEvent event) {
    }

    public void onTouchMove(MotionEvent event) {
    }

    public void onTouchDown(MotionEvent event) {
    }

    public boolean onDeleteSlotAnimationStart() {
        return false;
    }

    public boolean onDeleteSlotAnimationEnd() {
        return false;
    }

    public void onSelectionModeChange(int mode) {
    }

    public void onSelectionChange(Path path, boolean selected) {
    }

    public void onSelectionLimitExceed() {
    }

    protected void onLoadingStarted() {
        this.mAlbumRender.onLoadingStarted();
    }

    protected void onLoadingFinished(boolean loadingFailed) {
        this.mAlbumRender.onLoadingFinished();
    }

    protected void onVisibleRangeLoadFinished() {
    }

    protected void onCreate(Bundle data, Bundle storedState) {
        super.onCreate(data, storedState);
        this.mActionBar = this.mHost.getGalleryActionBar();
        this.mHandler = new Handler(this.mHost.getActivity().getMainLooper());
        initialize(data);
        setBlurWallpaperChanged();
    }

    private void initialize(Bundle data) {
        initializeData(data);
        initializeView();
    }

    protected void initializeData(Bundle data) {
        this.mMediaSetPath = Path.fromString(data.getString("media-path"));
        this.mGetContent = data.getBoolean("get-content", false);
        GalleryContext context = this.mHost.getGalleryContext();
        this.mMediaSet = context.getDataManager().getMediaSet(this.mMediaSetPath);
        if (this.mMediaSet == null) {
            Utils.fail("MediaSet is null. Path = %s", this.mMediaSetPath);
        }
        this.mUnNamedString = this.mHost.getActivity().getString(R.string.photoshare_tag_unnamed);
        this.mSelectionManager = new SelectionManager(context, true);
        this.mSelectionManager.setAutoLeaveSelectionMode(false);
        this.mSelectionManager.setSourceMediaSet(this.mMediaSet);
        this.mSelectionManager.setSelectionListener(this);
        if (this.mDataLoader == null) {
            this.mDataLoader = onCreateDataLoader(this.mMediaSet);
            this.mDataLoader.setGLRoot(this.mHost.getGLRoot());
        }
        this.mDataLoader.setLoadingListener(new DataLoaderListener());
    }

    protected void initializeView() {
        this.mScrollBar = new AlbumSlotScrollBarView(this.mHost.getGalleryContext(), R.drawable.bg_scrollbar, R.drawable.bg_quick_scrollbar_gallery);
        this.mScrollBar.setGLRoot(this.mHost.getGLRoot());
        this.mSlotView = onCreateSlotView();
        this.mAlbumRender = onCreateSlotRender();
        this.mAlbumRender.setModel(this.mDataLoader);
        this.mAlbumRender.setGLRoot(this.mHost.getGLRoot());
        this.mSlotView.setSlotRenderer(this.mAlbumRender);
        this.mSlotView.setListener(this);
        this.mRootPane.addComponent(this.mSlotView);
        this.mSlotView.setScrollBar(this.mScrollBar);
        this.mRootPane.addComponent(this.mScrollBar);
        this.mTopCover = new PlaceHolderView(this.mHost.getActivity());
        this.mRootPane.addComponent(this.mTopCover);
    }

    protected void onResume() {
        super.onResume();
        this.mIsActive = true;
        setContentPane(this.mRootPane);
        this.mDataLoader.resume();
        this.mAlbumRender.resume();
        this.mAlbumRender.setPressedIndex(-1);
    }

    protected void onPause() {
        super.onPause();
        this.mIsActive = false;
        this.mDataLoader.pause();
        this.mSlotView.pause();
        this.mAlbumRender.pause(needFreeSlotContent());
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mDataLoader != null) {
            this.mDataLoader.setLoadingListener(null);
            this.mDataLoader.setGLRoot(null);
        }
        this.mSelectionManager.setSelectionListener(null);
        this.mHandler.removeCallbacksAndMessages(null);
        this.mAlbumRender.destroy();
        this.mAlbumRender.setGLRoot(null);
    }

    protected boolean onBackPressed() {
        if (!this.mSelectionManager.inSelectionMode() || this.mGetContent) {
            return super.onBackPressed();
        }
        this.mSelectionManager.leaveSelectionMode();
        return true;
    }

    protected int getBackgroundColor(Context context) {
        return context.getResources().getColor(R.color.album_background);
    }

    public void onNavigationBarChanged(boolean show, int height) {
        this.mRootPane.requestLayout();
    }

    @SuppressLint({"ServiceCast"})
    private void setBlurWallpaperChanged() {
        WallpaperManager wm = (WallpaperManager) this.mHost.getActivity().getSystemService("wallpaper");
        try {
            wm.getClass().getDeclaredMethod("setCallback", new Class[]{IBlurWallpaperCallback.class}).invoke(wm, new Object[]{this});
        } catch (NoSuchMethodException e) {
            GalleryLog.d("AbsAlbumSetPage", "can not find setCallback: NoSuchMethodException !!!");
        } catch (RuntimeException e2) {
            GalleryLog.d("AbsAlbumSetPage", "can not find setCallback: RuntimeException !!!");
        } catch (InvocationTargetException e3) {
            GalleryLog.d("AbsAlbumSetPage", "can not find setCallback: InvocationTargetException  !!!");
        } catch (IllegalAccessException e4) {
            GalleryLog.d("AbsAlbumSetPage", "can not find setCallback: IllegalAccessException !!!");
        }
    }
}

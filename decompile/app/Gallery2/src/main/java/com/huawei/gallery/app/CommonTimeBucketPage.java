package com.huawei.gallery.app;

import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.MotionEvent;
import com.android.gallery3d.app.Config$LocalCameraAlbumPage;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.ui.TimeAxisSelectionManager;
import com.android.gallery3d.ui.TimeAxisSelectionManager.Delegate;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ReportToBigData;
import com.android.gallery3d.util.TraceController;
import com.huawei.gallery.anim.PhotoFallbackEffect.PositionProvider;
import com.huawei.gallery.data.AbsGroupData;
import com.huawei.gallery.ui.ListSlotRender;
import com.huawei.gallery.ui.ListSlotScrollSelectionManager;
import com.huawei.gallery.ui.ListSlotScrollSelectionManager.Listener;
import com.huawei.gallery.ui.ListSlotView;
import com.huawei.gallery.ui.ListSlotView.ItemCoordinate;
import com.huawei.gallery.ui.ListSlotView.ScrollOverListener;
import com.huawei.gallery.ui.SlotView;
import com.huawei.gallery.ui.SlotView.SlotRenderInterface;
import java.util.ArrayList;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public abstract class CommonTimeBucketPage extends AbsAlbumPage implements Listener, Delegate, ListSlotView.Listener, ScrollOverListener {
    private static final String TAG = "CommonTimeBucketPage";
    private MediaItemsDataLoader mAlbumDataAdapter;
    private PositionProvider mPositionProvider = new PositionProvider() {
        public Rect getPosition(int index) {
            Rect rect = CommonTimeBucketPage.this.mSlotView.getSlotRect(CommonTimeBucketPage.this.mSlotView.getItemCoordinate(index));
            Rect bounds = CommonTimeBucketPage.this.mSlotView.bounds();
            rect.offset(bounds.left - CommonTimeBucketPage.this.mSlotView.getScrollX(), bounds.top - CommonTimeBucketPage.this.mSlotView.getScrollY());
            return rect;
        }
    };
    protected ListSlotScrollSelectionManager mScrollSelectionManager;
    protected ListSlotRender mSlotRender;
    protected ListSlotView mSlotView;
    protected TimeAxisSelectionManager mTimeAxisSelectionManager;

    protected abstract Config$LocalCameraAlbumPage getConfig();

    protected void setDataLoader(MediaItemsDataLoader dataLoader) {
        this.mAlbumDataAdapter = dataLoader;
    }

    protected SelectionManager createSelectionManager(GalleryContext context) {
        this.mTimeAxisSelectionManager = new TimeAxisSelectionManager(context, this);
        return this.mTimeAxisSelectionManager;
    }

    protected int getItemIndex(ItemCoordinate item) {
        if (item.isTitle()) {
            return -1;
        }
        int index = 0;
        ArrayList<AbsGroupData> groupData = this.mAlbumDataAdapter.getGroupDatas();
        if (item.group >= groupData.size() || item.subIndex >= ((AbsGroupData) groupData.get(item.group)).count) {
            return -1;
        }
        for (int i = 0; i < item.group; i++) {
            index += ((AbsGroupData) groupData.get(i)).count;
        }
        if (item.subIndex != -1) {
            index += item.subIndex;
        }
        return index;
    }

    protected MediaItem getMediaItem(ItemCoordinate index) {
        int itemIndex = getItemIndex(index);
        if (itemIndex < 0) {
            return null;
        }
        return this.mAlbumDataAdapter.get(itemIndex);
    }

    protected MediaItem getMediaItem(int index) {
        if (index < 0) {
            return null;
        }
        return this.mAlbumDataAdapter.get(index);
    }

    protected void layoutListSlotView(int left, int top, int right, int bottom) {
        int height = bottom - top;
        int width = right - left;
        this.mSlotView.layout(0, 0, width, height);
        this.mScrollBar.layout(0, 0, width, height);
    }

    protected Rect getAnimSlotRect() {
        return this.mSlotView.getAnimRect();
    }

    protected SlotView getSlotView() {
        return this.mSlotView;
    }

    protected ListSlotRender createListSlotRender(Config$LocalCameraAlbumPage config) {
        return new ListSlotRender(this.mHost.getGalleryContext(), this.mSlotView, this.mSelectionManager, config.placeholderColor);
    }

    protected void onCreate(Bundle data, Bundle storedState) {
        super.onCreate(data, storedState);
        this.mScrollSelectionManager = new ListSlotScrollSelectionManager();
        this.mScrollSelectionManager.setListener(this);
        Config$LocalCameraAlbumPage config = getConfig();
        TraceController.beginSection("ListSlotView");
        this.mSlotView = new ListSlotView(this.mHost.getGalleryContext(), config.slotViewSpec);
        this.mSlotView.setScrollBar(this.mScrollBar);
        this.mAlbumDataAdapter.setGLRoot(this.mHost.getGLRoot());
        TraceController.endSection();
        TraceController.beginSection("ListSlotRender");
        this.mSlotRender = createListSlotRender(config);
        TraceController.endSection();
        this.mSlotView.setListener(this);
        this.mSlotView.setScrollOverListener(this);
        this.mSlotRender.setListSlotView(this.mSlotView);
        this.mSlotView.setSlotRenderer(this.mSlotRender);
        this.mSlotRender.setModel(this.mAlbumDataAdapter);
        this.mSlotRender.updateGlRoot(this.mHost.getGLRoot());
    }

    protected void onResume() {
        super.onResume();
        this.mSlotRender.resume();
        this.mSlotRender.setPressedIndex(null);
    }

    protected void onPause() {
        super.onPause();
        this.mSlotRender.pause(needFreeSlotContent());
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mSlotRender.destroy();
        this.mSlotRender.updateGlRoot(null);
        this.mAlbumDataAdapter.destroy();
    }

    protected void onLoadingFinished(boolean loadingFailed) {
        if (this.mTimeAxisSelectionManager.inSelectionMode()) {
            this.mTimeAxisSelectionManager.updateGroupData(this.mAlbumDataAdapter.getGroupDatas());
        }
        super.onLoadingFinished(loadingFailed);
        this.mSlotRender.onLoadingFinished();
    }

    protected void onLoadingStarted() {
        super.onLoadingStarted();
        this.mSlotRender.onLoadingStarted();
    }

    protected SlotRenderInterface getSlotRenderInterface() {
        return this.mSlotRender;
    }

    public ArrayList<AbsGroupData> getGroupDatas() {
        return this.mAlbumDataAdapter.getGroupDatas();
    }

    public void onScrollSelect(ItemCoordinate index, boolean selected) {
        if (this.mSelectionManager.inSelectionMode() && !this.mSelectionManager.inSingleMode()) {
            MediaItem item = getMediaItem(getItemIndex(index));
            if (item != null) {
                if (this.mTimeAxisSelectionManager.isItemSelected(index, item.getPath()) != selected) {
                    this.mTimeAxisSelectionManager.toggle(index, item.getPath());
                    this.mSlotView.invalidate();
                }
                ReportToBigData.report(SmsCheckResult.ESCT_176);
            }
        }
    }

    public boolean isSelected(ItemCoordinate index) {
        if (!this.mSelectionManager.inSelectionMode()) {
            return false;
        }
        MediaItem item = getMediaItem(getItemIndex(index));
        if (item == null) {
            return false;
        }
        return this.mTimeAxisSelectionManager.isItemSelected(index, item.getPath());
    }

    protected boolean autoFinishWhenNoItems() {
        return false;
    }

    public void onSelectionModeChange(int mode) {
        super.onSelectionModeChange(mode);
        if (mode == 1) {
            this.mSlotView.updateSelectionMode(true);
        } else if (mode == 2) {
            this.mSlotView.updateSelectionMode(false);
        }
    }

    protected void onPhotoFallback() {
        this.mSlotRender.setSlotFilter(this.mResumeEffect);
        this.mResumeEffect.setPositionProvider(this.mFocusIndex, this.mPositionProvider);
        this.mResumeEffect.start();
    }

    public void onDown(ItemCoordinate index) {
        this.mSlotRender.setPressedIndex(index);
    }

    public void onUp(boolean followedByLongPress) {
        this.mSlotRender.setPressedIndex(null);
    }

    public void onSingleTapUp(ItemCoordinate slotIndex, boolean cornerPressed) {
        if (this.mIsActive && !this.mSlotView.isAnimating()) {
            int index = getItemIndex(slotIndex);
            MediaItem item = getMediaItem(index);
            if (this.mSelectionManager.inSelectionMode()) {
                if (cornerPressed) {
                    if (slotIndex.isTitle()) {
                        this.mTimeAxisSelectionManager.toggleGroup(slotIndex);
                    } else if (item != null) {
                        this.mTimeAxisSelectionManager.toggle(slotIndex, item.getPath());
                    } else {
                        return;
                    }
                    this.mSlotView.invalidate();
                } else if (item != null) {
                    this.mTimeAxisSelectionManager.setGroupData(this.mAlbumDataAdapter.getGroupDatas());
                    pickPhotoWithAnimation(this.mSlotView, 102, slotIndex, index, item);
                }
            } else if (slotIndex.isTitle()) {
                boolean isAddressDrew = this.mSlotRender.isTitleEntryAddressDrew(slotIndex);
                RectF addressRect = this.mSlotRender.getAddressRect(slotIndex);
                String startToEndDate = this.mSlotRender.getStartToEndDate();
                if (!(!isAddressDrew || addressRect == null || ((double) addressRect.left) == 0.0d)) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("KEY_INIT_VISIBLE_MAP_RECT", addressRect);
                    bundle.putString("KEY_START_TO_END_DATE", startToEndDate);
                    bundle.putString("KEY_INIT_VISIBLE_MAP_FOR_PATH", getMapPath());
                    bundle.putBoolean("KEY_SUPPORT_GLOBEL", isSupportGlobelMap());
                    GalleryUtils.startMapAlbum(this.mHost.getActivity(), bundle);
                    onJumpToMapAlbum(slotIndex);
                }
            } else {
                pickPhotoWithAnimation(this.mSlotView, 100, slotIndex, index, item);
            }
        }
    }

    protected String getMapPath() {
        return TimeBucketPage.SOURCE_DATA_PATH;
    }

    protected boolean inSelectionMode() {
        return this.mSelectionManager.inSelectionMode();
    }

    protected void onJumpToMapAlbum(ItemCoordinate slotIndex) {
    }

    protected boolean isSupportGlobelMap() {
        GalleryLog.d(TAG, "enter Map from TimeBucketPage");
        return true;
    }

    public void onLongTap(MotionEvent event) {
        if (!this.mSelectionManager.inSelectionMode()) {
            ItemCoordinate slotIndex = this.mSlotView.getSlotIndexByPosition(event.getX(), event.getY());
            if (slotIndex != null) {
                MediaItem item = getMediaItem(getItemIndex(slotIndex));
                if (item != null) {
                    this.mTimeAxisSelectionManager.toggle(slotIndex, item.getPath());
                    this.mSlotView.invalidate();
                    this.mSlotView.startClickSlotAnimation(slotIndex, null);
                }
            }
        }
    }

    public void onScroll(ItemCoordinate index) {
        this.mScrollSelectionManager.addScrollIndex(index);
    }

    public boolean enableScrollSelection() {
        return this.mSelectionManager.inSelectionMode();
    }

    public void onTouchUp(MotionEvent event) {
        this.mScrollSelectionManager.clearup();
    }

    public void onTouchDown(MotionEvent event) {
    }

    public void onCancel() {
    }

    public void onMove(MotionEvent event) {
    }

    public boolean onDeleteSlotAnimationStart() {
        return super.onDeleteSlotAnimationStart();
    }

    public boolean onDeleteSlotAnimationEnd() {
        return super.onDeleteSlotAnimationEnd();
    }

    public void onScrollOver(float distance) {
    }

    public void onScrollOverBegin() {
    }

    public void onScrollOverDone() {
    }

    public float getOffset() {
        return 0.0f;
    }

    public void renderHeadCover(GLCanvas canvas, int left, int top, int right, int bottom) {
    }

    public int getOverflowMarginTop() {
        return 0;
    }
}

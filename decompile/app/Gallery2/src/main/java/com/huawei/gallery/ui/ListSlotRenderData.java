package com.huawei.gallery.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.data.TimeBucketPageViewMode;
import com.android.gallery3d.ui.BitmapTexture;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.ui.TextureUploader;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.JobLimiter;
import com.android.gallery3d.util.ThreadPool.Job;
import com.huawei.gallery.app.MediaItemsDataLoader;
import com.huawei.gallery.app.MediaItemsDataLoader.DataListener;
import com.huawei.gallery.data.AbsGroupData;
import com.huawei.gallery.ui.ListSlotView.ItemCoordinate;
import com.huawei.gallery.ui.TimeAxisLabel.BaseSpec;
import com.huawei.gallery.ui.TimeAxisLabel.TitleSpec;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class ListSlotRenderData implements DataListener, ItemEntrySetListener, TitleEntrySetListener, TitleLoaderListener {
    protected ArrayList<AbsGroupData> mGroupData;
    protected Handler mHandler;
    private boolean mHasFreeSlotContent = true;
    private boolean mIsActive = false;
    protected EntrySet mItemData;
    protected Listener mListener;
    protected EntrySet mOldTitleData;
    protected int mSize;
    protected final MediaItemsDataLoader mSource;
    private TextureUploader mTextureUploader;
    private final JobLimiter mThreadPool;
    protected EntrySet mTitleData;
    protected final TimeAxisLabel mTitleLabel;
    private boolean mUpdateFinish;

    public interface Listener {
        TimeBucketPageViewMode getViewMode();

        void onActiveTextureReady();

        void onContentChanged();

        void onSizeChanged(int i, ArrayList<AbsGroupData> arrayList, TimeBucketPageViewMode timeBucketPageViewMode);
    }

    private static class BmpData {
        public Bitmap bmp;
        public Object obj;

        private BmpData() {
        }
    }

    protected abstract void initEntrySet(int i);

    public ListSlotRenderData(GalleryContext activity, MediaItemsDataLoader source, int cacheSize, TitleSpec titleSpec) {
        source.setDataListener(this);
        this.mSource = source;
        this.mSize = source.size();
        this.mGroupData = source.getGroupDatas();
        this.mThreadPool = new JobLimiter(activity.getThreadPool(), 2);
        this.mTitleLabel = createTimeAxisLabel(activity.getAndroidContext(), titleSpec);
        initEntrySet(cacheSize);
    }

    public TimeAxisLabel createTimeAxisLabel(Context context, TitleSpec titleSpec) {
        return new TimeAxisLabel(context, titleSpec);
    }

    public synchronized void updateGlRoot(GLRoot glRoot) {
        if (this.mTextureUploader != null) {
            this.mTextureUploader.clear();
        }
        if (this.mHandler != null) {
            this.mHandler.removeCallbacksAndMessages(null);
        }
        if (glRoot != null) {
            this.mTextureUploader = new TextureUploader(glRoot);
            this.mHandler = new SynchronizedHandler(glRoot) {
                public void handleMessage(Message message) {
                    ListSlotRenderData.this.onHandleMessage(message);
                }
            };
        }
    }

    protected void onHandleMessage(Message message) {
        if (!this.mIsActive) {
            GalleryLog.d("ListSlotRenderData", "current is not active, handleMessage msg:" + message.what + ", hasFreeSlotContent:" + this.mHasFreeSlotContent);
        }
        ThumbnailLoader loader;
        switch (message.what) {
            case 0:
                loader = message.obj;
                if (loader instanceof TitleLoader) {
                    this.mTitleData.updateTexture(loader.mSlotIndex, loader.getBitmap(), false);
                    return;
                } else {
                    this.mItemData.updateTexture(loader.mSlotIndex, loader.getBitmap(), loader.isStateError());
                    return;
                }
            case 4:
                BmpData data = message.obj;
                loader = (ThumbnailLoader) data.obj;
                if (loader instanceof TitleLoader) {
                    this.mTitleData.updatePreviewTexture(loader.mSlotIndex, data.bmp);
                    return;
                } else {
                    this.mItemData.updatePreviewTexture(loader.mSlotIndex, data.bmp);
                    return;
                }
            case 6:
                GalleryLog.d("ListSlotRenderData", "MSG_LOAD_ACTIVE_TEXTURE_OVER_TIME");
                requestNoneActiveEntry();
                return;
            case 7:
                freeEntry();
                this.mHasFreeSlotContent = true;
                return;
            default:
                return;
        }
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public int getItemIndex(ItemCoordinate item) {
        int index = 0;
        if (this.mGroupData == null || item.group >= this.mGroupData.size()) {
            return 0;
        }
        for (int i = 0; i < item.group; i++) {
            index += ((AbsGroupData) this.mGroupData.get(i)).count;
        }
        if (item.subIndex != -1) {
            index += item.subIndex;
        }
        return index;
    }

    private ItemCoordinate getItemCoordinateByAbsIndex(int absIndex) {
        int i;
        int index = 0;
        int size = this.mGroupData.size();
        int group = 0;
        while (group < size) {
            int count = ((AbsGroupData) this.mGroupData.get(group)).count;
            index += count;
            if (index > absIndex) {
                GalleryLog.printDFXLog("found group index");
                index -= count;
                i = group - 1;
                i = Math.max(0, group);
                break;
            }
            group++;
        }
        i = group;
        return new ItemCoordinate(i, absIndex - index);
    }

    public void setActiveWindow(ItemCoordinate start, ItemCoordinate end) {
        if (start.isSmall(end) && this.mGroupData != null) {
            int itemStart = getItemIndex(start);
            int itemEnd = getItemIndex(end);
            int titleStart = start.group;
            int titleEnd = Math.min(end.group + 1, this.mGroupData.size());
            if (!this.mItemData.checkActiveRegion(itemStart, itemEnd) || !this.mTitleData.checkActiveRegion(titleStart, titleEnd)) {
                if (this.mIsActive) {
                    this.mTextureUploader.clear();
                }
                this.mItemData.setActiveWindow(itemStart, itemEnd);
                this.mTitleData.setActiveWindow(titleStart, titleEnd);
            }
        }
    }

    public void setActiveWindow(ItemCoordinate start, ItemCoordinate end, ItemCoordinate titleVisibleStart, ItemCoordinate titleVisibleEnd) {
        if (start.isSmall(end) && this.mGroupData != null) {
            int itemStart = getItemIndex(start);
            int itemEnd = getItemIndex(end);
            if (!this.mItemData.checkActiveRegion(itemStart, itemEnd)) {
                if (this.mIsActive) {
                    this.mTextureUploader.clear();
                }
                this.mItemData.setActiveWindow(itemStart, itemEnd);
                if (titleVisibleStart.isSmall(titleVisibleEnd) && this.mGroupData != null) {
                    int titleStart = titleVisibleStart.group;
                    int titleEnd = Math.min(titleVisibleEnd.group + 1, this.mGroupData.size());
                    if (!this.mTitleData.checkActiveRegion(titleStart, titleEnd)) {
                        this.mTitleData.setActiveWindow(titleStart, titleEnd);
                    }
                }
            }
        }
    }

    private void saveOldData() {
        this.mOldTitleData.freeEntry();
        this.mOldTitleData.set(this.mTitleData);
    }

    public void onSizeChanged(int size, ArrayList<AbsGroupData> groupDatas, TimeBucketPageViewMode mode) {
        if (!(this.mListener == null || this.mListener.getViewMode() == mode)) {
            saveOldData();
        }
        this.mSize = size;
        this.mGroupData = groupDatas;
        this.mItemData.updateRange(this.mSize);
        this.mTitleData.freeEntry();
        this.mTitleData.updateRange(this.mGroupData.size());
        this.mTitleData.prepareEntry();
        if (this.mListener != null) {
            this.mListener.onSizeChanged(this.mSize, groupDatas, mode);
        }
    }

    public void onContentChanged(int index) {
        if (this.mIsActive) {
            this.mItemData.updateEntry(index);
            if (this.mListener != null && this.mItemData.isActive(index)) {
                this.mListener.onContentChanged();
            }
        }
    }

    public void resume() {
        this.mIsActive = true;
        this.mItemData.resume();
        this.mTitleData.resume();
        this.mOldTitleData.resume();
        GalleryLog.d("ListSlotRenderData", "resume hasFreeSlotContent:" + this.mHasFreeSlotContent);
        if (this.mHandler != null) {
            this.mHandler.removeMessages(7);
        }
        if (this.mHasFreeSlotContent) {
            this.mTitleData.prepareEntry();
            this.mItemData.prepareEntry();
        } else {
            this.mTitleData.updateAllRequest();
            this.mItemData.updateAllRequest();
        }
        this.mHasFreeSlotContent = false;
    }

    public void pause(boolean needFreeSlotContent) {
        this.mIsActive = false;
        this.mItemData.pause();
        this.mTitleData.pause();
        this.mOldTitleData.pause();
        if (!needFreeSlotContent) {
            this.mHasFreeSlotContent = false;
        } else if (this.mHandler != null) {
            this.mHandler.sendEmptyMessageDelayed(7, 250);
        } else {
            freeEntry();
        }
        GalleryLog.d("ListSlotRenderData", "pause freeSlotContent:" + needFreeSlotContent);
        if (this.mHandler != null) {
            this.mHandler.removeMessages(6);
        }
    }

    public void destroy() {
        if (this.mHandler != null) {
            this.mHandler.removeMessages(7);
        }
        if (!this.mHasFreeSlotContent) {
            freeEntry();
            this.mHasFreeSlotContent = true;
        }
    }

    protected void freeEntry() {
        this.mTextureUploader.clear();
        this.mItemData.freeEntry();
        this.mTitleData.freeEntry();
        this.mOldTitleData.freeEntry();
        this.mTitleLabel.clearRecycledLabels();
    }

    public void addFgTexture(BitmapTexture texture) {
        if (texture != null) {
            this.mTextureUploader.addFgTexture(texture);
        }
    }

    public void addBgTexture(BitmapTexture texture) {
        if (texture != null) {
            this.mTextureUploader.addBgTexture(texture);
        }
    }

    public void invalidate() {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onContentChanged();
        }
    }

    public void updateSourceRange(int start, int end) {
        this.mSource.setActiveWindow(start, end);
    }

    public void updateUIRange(int start, int end) {
        this.mSource.setUIWindow(start, end);
    }

    public Object getItemObjectIndex(int index) {
        return getItemCoordinateByAbsIndex(index);
    }

    public void requestNoneActiveEntry() {
        if (this.mListener != null && this.mUpdateFinish && this.mIsActive) {
            this.mListener.onActiveTextureReady();
        }
    }

    public Future<Bitmap> submit(Job<Bitmap> job, FutureListener<Bitmap> listener, int startMode) {
        return this.mThreadPool.submit(job, listener, startMode);
    }

    public void onComplete(Object object) {
        this.mHandler.obtainMessage(0, object).sendToTarget();
    }

    public void onPreviewLoad(Object object, Bitmap bmp) {
        BmpData data = new BmpData();
        data.obj = object;
        data.bmp = bmp;
        this.mHandler.obtainMessage(4, data).sendToTarget();
    }

    public boolean isActiveSlot(ItemCoordinate index) {
        if (index.isTitle()) {
            return this.mTitleData.isActive(index.group);
        }
        return this.mItemData.isActive(getItemIndex(index));
    }

    public boolean isActiveSlot(ItemCoordinate index, boolean fromTo) {
        if (!index.isTitle()) {
            return this.mItemData.isActive(getItemIndex(index));
        }
        return fromTo ? this.mOldTitleData.isActive(index.group) : this.mTitleData.isActive(index.group);
    }

    public BaseEntry get(ItemCoordinate index) {
        return get(index, false);
    }

    public BaseEntry get(ItemCoordinate index, boolean fromTo) {
        if (index.isTitle()) {
            return fromTo ? this.mOldTitleData.get(index.group) : this.mTitleData.get(index.group);
        } else {
            return this.mItemData.get(getItemIndex(index));
        }
    }

    public boolean scale(boolean beBigger) {
        return false;
    }

    public BaseSpec getCurrentSpec() {
        return this.mTitleLabel.mCurrentSpec;
    }

    public void onLoadingStarted() {
        this.mUpdateFinish = false;
    }

    public void onLoadingFinished() {
        this.mUpdateFinish = true;
        if (this.mIsActive) {
            this.mItemData.updateAllRequest();
            this.mHandler.removeMessages(6);
            this.mHandler.sendEmptyMessageDelayed(6, 2000);
        }
    }

    public Object getTitleObjectIndex(int index) {
        return new ItemCoordinate(index, -1);
    }

    public MediaItem getMediaItem(int index) {
        return this.mSource.get(index);
    }

    public AbsGroupData getGroupData(int index) {
        return (AbsGroupData) this.mGroupData.get(index);
    }

    public void recycleTitle(Bitmap title) {
        this.mTitleLabel.recycleLabel(title);
    }

    public int getCurrentTitleModeValue() {
        return this.mTitleLabel.getCurrentTitleModeValue();
    }

    public void prepareVisibleRangeItemIndex(HashMap<Path, Object> visiblePathMap, HashMap<Object, Object> visibleIndexMap) {
        this.mTitleData.prepareVisibleRangeItemIndex(visiblePathMap, visibleIndexMap);
        this.mItemData.prepareVisibleRangeItemIndex(visiblePathMap, visibleIndexMap);
    }

    public void freeVisibleRangeItem(HashMap<Path, Object> visiblePathMap) {
        this.mTitleData.freeVisibleRangeItem(visiblePathMap);
        this.mItemData.freeVisibleRangeItem(visiblePathMap);
    }
}

package com.huawei.gallery.app;

import android.os.Message;
import android.os.Process;
import com.android.gallery3d.app.LoadingListener;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.ContentListener;
import com.android.gallery3d.data.DiscoverStoryAlbumSet;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.TraceController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class CommonAlbumSetDataLoader {
    private int mActiveEnd;
    private int mActiveStart;
    protected int mContentEnd;
    protected int mContentStart;
    protected final MediaItem[][] mCoverItem;
    protected final MediaSet[] mData;
    private DataListener mDataListener;
    protected final long[] mItemVersion;
    private LoadingListener mLoadingListener;
    protected final SynchronizedHandler mMainHandler;
    protected volatile boolean mReloadLock;
    protected ReloadTask mReloadTask;
    protected final long[] mSetVersion;
    private int mSize;
    protected final MediaSet mSource;
    private final MySourceListener mSourceListener;
    private long mSourceVersion;
    private int mThumbnailType;
    protected final int[] mTotalCount;
    protected final int[] mTotalVideoCount;
    private int mUIEnd;
    private int mUIStart;

    public interface DataListener {
        void onContentChanged(int i);

        void onSizeChanged(int i);
    }

    protected class GetUpdateInfo implements Callable<UpdateInfo> {
        private final Random mRandom;
        private final long mVersion;

        public GetUpdateInfo(long version) {
            this.mVersion = version;
            if (CommonAlbumSetDataLoader.this.needRandomUpdate()) {
                this.mRandom = new Random();
                this.mRandom.setSeed(System.currentTimeMillis());
                return;
            }
            this.mRandom = null;
        }

        private int getInvalidIndex(long version) {
            int i;
            long[] setVersion = CommonAlbumSetDataLoader.this.mSetVersion;
            int length = setVersion.length;
            int range = Math.min(CommonAlbumSetDataLoader.this.mUIEnd, CommonAlbumSetDataLoader.this.mSize) - CommonAlbumSetDataLoader.this.mUIStart;
            if (CommonAlbumSetDataLoader.this.mUIStart >= 0 && range > 0) {
                int start = range;
                if (this.mRandom != null) {
                    start = this.mRandom.nextInt(range);
                }
                for (i = 0; i < range; i++) {
                    int index = ((start + i) % range) + CommonAlbumSetDataLoader.this.mUIStart;
                    if (setVersion[index % length] != version) {
                        return index;
                    }
                }
            }
            int n = CommonAlbumSetDataLoader.this.mContentEnd;
            for (i = CommonAlbumSetDataLoader.this.mContentStart; i < n; i++) {
                if (setVersion[i % length] != version) {
                    return i;
                }
            }
            return -1;
        }

        public UpdateInfo call() throws Exception {
            int index = getInvalidIndex(this.mVersion);
            if (index == -1 && CommonAlbumSetDataLoader.this.mSourceVersion == this.mVersion) {
                return null;
            }
            long j;
            UpdateInfo info = new UpdateInfo();
            info.version = CommonAlbumSetDataLoader.this.mSourceVersion;
            info.index = index;
            info.size = CommonAlbumSetDataLoader.this.mSize;
            if (index == -1) {
                j = -1;
            } else {
                j = CommonAlbumSetDataLoader.this.mItemVersion[index % CommonAlbumSetDataLoader.this.mItemVersion.length];
            }
            info.oldItemVersion = j;
            return info;
        }
    }

    private class MySourceListener implements ContentListener {
        private MySourceListener() {
        }

        public void onContentDirty() {
            if (CommonAlbumSetDataLoader.this.mReloadTask != null && !CommonAlbumSetDataLoader.this.mReloadLock) {
                CommonAlbumSetDataLoader.this.mReloadTask.notifyDirty();
            }
        }
    }

    protected class ReloadTask extends Thread {
        private volatile boolean mActive = true;
        private volatile boolean mDirty = true;
        private volatile boolean mIsLoading = false;

        protected ReloadTask() {
        }

        private void updateLoading(boolean loading) {
            if (this.mIsLoading != loading) {
                this.mIsLoading = loading;
                CommonAlbumSetDataLoader.this.mMainHandler.sendEmptyMessage(loading ? 1 : 2);
            }
        }

        public void run() {
            Process.setThreadPriority(0);
            boolean z = false;
            while (this.mActive) {
                synchronized (this) {
                    if (!this.mActive || ((this.mDirty || !r1) && !CommonAlbumSetDataLoader.this.mReloadLock)) {
                        this.mDirty = false;
                        updateLoading(true);
                        TraceController.printDebugInfo("start");
                        long version = CommonAlbumSetDataLoader.this.mSource.reload();
                        UpdateInfo info = (UpdateInfo) CommonAlbumSetDataLoader.this.executeAndWait(new GetUpdateInfo(version));
                        if (info == null) {
                            z = true;
                        } else {
                            z = false;
                        }
                        if (!z) {
                            if (info.version != version) {
                                info.version = version;
                                info.size = CommonAlbumSetDataLoader.this.mSource.getSubMediaSetCount();
                                if (info.index >= info.size) {
                                    info.index = -1;
                                }
                            }
                            if (info.index != -1) {
                                info.item = CommonAlbumSetDataLoader.this.mSource.getSubMediaSet(info.index);
                                if (info.item != null) {
                                    info.itemVersion = info.item.getDataVersion();
                                    if (info.oldItemVersion != info.itemVersion) {
                                        info.totalCount = info.item.getTotalMediaItemCount();
                                        info.covers = info.item.getMultiCoverMediaItem();
                                        info.totalVideoCount = info.item.getTotalVideoCount();
                                    }
                                }
                            }
                            CommonAlbumSetDataLoader.this.executeAndWait(new UpdateContent(info));
                            TraceController.printDebugInfo(String.format("end version:%s  mSize:%s info.size:%s info.index:%s", new Object[]{Long.valueOf(version), Integer.valueOf(CommonAlbumSetDataLoader.this.mSize), Integer.valueOf(info.size), Integer.valueOf(info.index)}));
                        }
                    } else {
                        if (!CommonAlbumSetDataLoader.this.mSource.isLoading()) {
                            updateLoading(false);
                        }
                        Utils.waitWithoutInterrupt(this);
                    }
                }
            }
            updateLoading(false);
        }

        public synchronized void notifyDirty() {
            this.mDirty = true;
            notifyAll();
        }

        public synchronized void terminate() {
            this.mActive = false;
            notifyAll();
        }

        boolean isLoading() {
            return this.mIsLoading;
        }
    }

    private class UpdateContent implements Callable<Void> {
        private final UpdateInfo mUpdateInfo;

        public UpdateContent(UpdateInfo info) {
            this.mUpdateInfo = info;
        }

        public Void call() {
            if (CommonAlbumSetDataLoader.this.mReloadTask == null) {
                return null;
            }
            UpdateInfo info = this.mUpdateInfo;
            CommonAlbumSetDataLoader.this.mSourceVersion = info.version;
            if (CommonAlbumSetDataLoader.this.mSize != info.size) {
                CommonAlbumSetDataLoader.this.mSize = info.size;
                if (CommonAlbumSetDataLoader.this.mDataListener != null) {
                    CommonAlbumSetDataLoader.this.mDataListener.onSizeChanged(CommonAlbumSetDataLoader.this.mSize);
                }
                if (CommonAlbumSetDataLoader.this.mContentEnd > CommonAlbumSetDataLoader.this.mSize) {
                    CommonAlbumSetDataLoader.this.mContentEnd = CommonAlbumSetDataLoader.this.mSize;
                }
                if (CommonAlbumSetDataLoader.this.mActiveEnd > CommonAlbumSetDataLoader.this.mSize) {
                    CommonAlbumSetDataLoader.this.mActiveEnd = CommonAlbumSetDataLoader.this.mSize;
                }
            }
            if (info.index >= CommonAlbumSetDataLoader.this.mContentStart && info.index < CommonAlbumSetDataLoader.this.mContentEnd) {
                int pos = info.index % CommonAlbumSetDataLoader.this.mCoverItem.length;
                CommonAlbumSetDataLoader.this.mSetVersion[pos] = info.version;
                long itemVersion = info.itemVersion;
                if (CommonAlbumSetDataLoader.this.mItemVersion[pos] == itemVersion) {
                    return null;
                }
                CommonAlbumSetDataLoader.this.mItemVersion[pos] = itemVersion;
                CommonAlbumSetDataLoader.this.mData[pos] = info.item;
                CommonAlbumSetDataLoader.this.mCoverItem[pos] = info.covers;
                CommonAlbumSetDataLoader.this.mTotalCount[pos] = info.totalCount;
                CommonAlbumSetDataLoader.this.mTotalVideoCount[pos] = info.totalVideoCount;
                if (CommonAlbumSetDataLoader.this.mDataListener != null && info.index >= CommonAlbumSetDataLoader.this.mActiveStart && info.index < CommonAlbumSetDataLoader.this.mActiveEnd) {
                    CommonAlbumSetDataLoader.this.mDataListener.onContentChanged(info.index);
                }
                if (CommonAlbumSetDataLoader.this.mLoadingListener != null && (info.index < CommonAlbumSetDataLoader.this.mUIStart || info.index >= CommonAlbumSetDataLoader.this.mUIEnd)) {
                    CommonAlbumSetDataLoader.this.mLoadingListener.onVisibleRangeLoadFinished();
                }
            }
            return null;
        }
    }

    public static class UpdateInfo {
        public MediaItem[] covers;
        public int index;
        public MediaSet item;
        public long itemVersion;
        public long oldItemVersion;
        public int size;
        public int totalCount;
        public int totalVideoCount;
        public long version;
    }

    public CommonAlbumSetDataLoader(MediaSet albumSet, int cacheSize, int type, GLRoot root) {
        this.mActiveStart = 0;
        this.mActiveEnd = 0;
        this.mContentStart = 0;
        this.mContentEnd = 0;
        this.mSourceVersion = -1;
        this.mSourceListener = new MySourceListener();
        this.mThumbnailType = 2;
        this.mSource = (MediaSet) Utils.checkNotNull(albumSet);
        this.mCoverItem = new MediaItem[cacheSize][];
        this.mData = new MediaSet[cacheSize];
        this.mTotalCount = new int[cacheSize];
        this.mTotalVideoCount = new int[cacheSize];
        this.mItemVersion = new long[cacheSize];
        this.mSetVersion = new long[cacheSize];
        Arrays.fill(this.mItemVersion, -1);
        Arrays.fill(this.mSetVersion, -1);
        this.mThumbnailType = type;
        this.mMainHandler = new SynchronizedHandler(root) {
            public void handleMessage(Message message) {
                CommonAlbumSetDataLoader.this.handleMessage(message);
            }
        };
    }

    public void setGLRoot(GLRoot root) {
        if (this.mMainHandler != null) {
            this.mMainHandler.setGLRoot(root);
        }
    }

    public int getThumbnailType() {
        return this.mThumbnailType;
    }

    public CommonAlbumSetDataLoader(MediaSet albumSet, int cacheSize) {
        this(albumSet, cacheSize, 2, null);
    }

    public CommonAlbumSetDataLoader(MediaSet albumSet, int cacheSize, int type) {
        this(albumSet, cacheSize, type, null);
    }

    private void handleMessage(Message message) {
        switch (message.what) {
            case 1:
                if (this.mLoadingListener != null) {
                    this.mLoadingListener.onLoadingStarted();
                }
                return;
            case 2:
                if (this.mLoadingListener != null) {
                    this.mLoadingListener.onLoadingFinished(false);
                }
                return;
            case 3:
                ((Runnable) message.obj).run();
                return;
            default:
                return;
        }
    }

    public void pause() {
        if (this.mReloadTask != null) {
            this.mReloadTask.terminate();
            this.mReloadTask = null;
            this.mSource.removeContentListener(this.mSourceListener);
        }
    }

    public void resume() {
        if (this.mReloadTask != null) {
            GalleryLog.d("AbsAlbumSetDataLoader", "Reload task is not null");
            return;
        }
        this.mSource.addContentListener(this.mSourceListener);
        this.mReloadTask = new ReloadTask();
        this.mReloadTask.start();
    }

    public MediaSet getMediaSet(int index) {
        return this.mData[index % this.mData.length];
    }

    public ArrayList<MediaItem> getCoverItems() {
        if (this.mSource instanceof DiscoverStoryAlbumSet) {
            return ((DiscoverStoryAlbumSet) this.mSource).getCoverItems();
        }
        return null;
    }

    public MediaItem[] getCoverItem(int index) {
        return this.mCoverItem[index % this.mCoverItem.length];
    }

    public int getTotalCount(int index) {
        return this.mTotalCount[index % this.mTotalCount.length];
    }

    public int getTotalVideoCount(int index) {
        return this.mTotalVideoCount[index % this.mTotalVideoCount.length];
    }

    public boolean isContentValid(int index) {
        if (index < this.mContentStart || index >= this.mContentEnd || getMediaSet(index) == null) {
            return false;
        }
        return true;
    }

    public int size() {
        return this.mSize;
    }

    public int findSet(Path id) {
        int length = this.mData.length;
        for (int i = this.mContentStart; i < this.mContentEnd; i++) {
            MediaSet set = this.mData[i % length];
            if (set != null && id == set.getPath()) {
                return i;
            }
        }
        return -1;
    }

    private void clearSlot(int slotIndex) {
        this.mData[slotIndex] = null;
        this.mCoverItem[slotIndex] = null;
        this.mTotalCount[slotIndex] = 0;
        this.mTotalVideoCount[slotIndex] = 0;
        this.mItemVersion[slotIndex] = -1;
        this.mSetVersion[slotIndex] = -1;
    }

    public void setUIRange(int activeStart, int activeEnd) {
        this.mUIStart = activeStart;
        this.mUIEnd = activeEnd;
    }

    private void setContentWindow(int contentStart, int contentEnd) {
        if (contentStart != this.mContentStart || contentEnd != this.mContentEnd) {
            int length = this.mCoverItem.length;
            int start = this.mContentStart;
            int end = this.mContentEnd;
            this.mContentStart = contentStart;
            this.mContentEnd = contentEnd;
            int i;
            if (contentStart >= end || start >= contentEnd) {
                for (i = start; i < end; i++) {
                    clearSlot(i % length);
                }
            } else {
                for (i = start; i < contentStart; i++) {
                    clearSlot(i % length);
                }
                for (i = contentEnd; i < end; i++) {
                    clearSlot(i % length);
                }
            }
            if (this.mReloadTask != null) {
                this.mReloadTask.notifyDirty();
            }
        }
    }

    public void setActiveWindow(int start, int end) {
        if (start != this.mActiveStart || end != this.mActiveEnd) {
            boolean z = (start > end || end - start > this.mData.length) ? false : end <= this.mSize;
            Utils.assertTrue(z);
            int length = this.mData.length;
            this.mActiveStart = start;
            this.mActiveEnd = end;
            if (start != end) {
                int contentStart = Utils.clamp(((start + end) / 2) - (length / 2), 0, Math.max(0, this.mSize - length));
                int contentEnd = Math.min(contentStart + length, this.mSize);
                if (this.mContentStart <= start && this.mContentEnd >= end) {
                    if (Math.abs(contentStart - this.mContentStart) > 4) {
                    }
                }
                setContentWindow(contentStart, contentEnd);
            }
        }
    }

    public void setModelListener(DataListener listener) {
        this.mDataListener = listener;
    }

    public void setLoadingListener(LoadingListener listener) {
        this.mLoadingListener = listener;
    }

    public boolean needRandomUpdate() {
        return false;
    }

    private <T> T executeAndWait(Callable<T> callable) {
        FutureTask<T> task = new FutureTask(callable);
        this.mMainHandler.sendMessage(this.mMainHandler.obtainMessage(3, task));
        try {
            return task.get();
        } catch (InterruptedException e) {
            return null;
        } catch (ExecutionException e2) {
            throw new RuntimeException(e2);
        }
    }

    public void freeze() {
        this.mReloadLock = true;
    }

    public void unfreeze() {
        this.mReloadLock = false;
        if (this.mSourceListener != null) {
            this.mSourceListener.onContentDirty();
        }
    }
}

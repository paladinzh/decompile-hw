package com.huawei.gallery.app;

import android.os.Message;
import android.os.Process;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.app.LoadingListener;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.ContentListener;
import com.android.gallery3d.data.IGroupAlbum;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.TimeBucketPageViewMode;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.TraceController;
import com.huawei.gallery.data.AbsGroupData;
import com.huawei.gallery.servicemanager.CloudManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class MediaItemsDataLoader extends AlbumDataLoader {
    protected static final Object GROUPCOUNT_LOCK = new Object();
    protected int mActiveEnd = 0;
    private int mActiveStart = 0;
    protected int mContentEnd = 0;
    private int mContentStart = 0;
    protected GalleryContext mContext;
    private final MediaItem[] mData;
    protected DataListener mDataListener;
    private long mFailedVersion = -1;
    protected ArrayList<AbsGroupData> mGroupDatas = new ArrayList();
    private final long[] mItemVersion;
    private LoadCountListener mLoadCountListener;
    private LoadingListener mLoadingListener;
    protected SynchronizedHandler mMainHandler;
    protected ReloadTask mReloadTask;
    private int mRunCount = 0;
    private final long[] mSetVersion;
    private int mShowingEnd = 0;
    private int mShowingStart = 0;
    protected int mSize = 0;
    protected final MediaSet mSource;
    private MySourceListener mSourceListener = new MySourceListener();
    private long mSourceVersion = -1;

    public interface DataListener {
        void onContentChanged(int i);

        void onSizeChanged(int i, ArrayList<AbsGroupData> arrayList, TimeBucketPageViewMode timeBucketPageViewMode);
    }

    protected class GetUpdateInfo implements Callable<UpdateInfo> {
        private final long mVersion;

        public GetUpdateInfo(long version) {
            this.mVersion = version;
        }

        protected UpdateInfo createUpdateInfo() {
            return new UpdateInfo();
        }

        public UpdateInfo call() throws Exception {
            int i;
            UpdateInfo info = createUpdateInfo();
            long version = this.mVersion;
            info.version = MediaItemsDataLoader.this.mSourceVersion;
            info.size = MediaItemsDataLoader.this.mSize;
            synchronized (MediaItemsDataLoader.GROUPCOUNT_LOCK) {
                info.groupDatas = MediaItemsDataLoader.this.mGroupDatas;
            }
            long[] setVersion = MediaItemsDataLoader.this.mSetVersion;
            int n = MediaItemsDataLoader.this.mShowingEnd;
            for (i = MediaItemsDataLoader.this.mShowingStart; i < n; i++) {
                if (setVersion[i % 256] != version) {
                    info.reloadStart = i;
                    info.reloadCount = Math.max(0, n - i);
                    return info;
                }
            }
            n = MediaItemsDataLoader.this.mContentEnd;
            for (i = MediaItemsDataLoader.this.mContentStart; i < n; i++) {
                if (setVersion[i % 256] != version) {
                    info.reloadStart = i;
                    info.reloadCount = Math.min(64, n - i);
                    GalleryLog.printDFXLog("MediaItemsDataLoader for DFX reloadCount " + info.reloadCount);
                    return info;
                }
            }
            if (MediaItemsDataLoader.this.mSourceVersion == this.mVersion) {
                info = null;
            }
            return info;
        }
    }

    public interface LoadCountListener {
        void onLoadCountChange(int i);
    }

    private class MySourceListener implements ContentListener {
        private MySourceListener() {
        }

        public void onContentDirty() {
            if (MediaItemsDataLoader.this.mReloadTask != null && !MediaItemsDataLoader.this.mReloadLock) {
                MediaItemsDataLoader.this.mReloadTask.notifyDirty();
            }
        }
    }

    protected class ReloadTask extends Thread {
        private volatile boolean mActive = true;
        private volatile boolean mDirty = true;
        private ArrayList<Integer> mIdCache = new ArrayList();
        private boolean mIsLoading = false;

        protected ReloadTask() {
        }

        public void run() {
            Process.setThreadPriority(10);
            boolean z = false;
            while (this.mActive) {
                synchronized (this) {
                    if (!this.mActive || ((this.mDirty || !r3) && !MediaItemsDataLoader.this.mReloadLock)) {
                        UpdateInfo call;
                        this.mDirty = false;
                        updateLoading(true);
                        long version = MediaItemsDataLoader.this.mSource.reload();
                        GetUpdateInfo getUpdateInfo = MediaItemsDataLoader.this.createGetUpdateInfo(version);
                        if (MediaItemsDataLoader.this.mRunCount <= 2) {
                            MediaItemsDataLoader mediaItemsDataLoader = MediaItemsDataLoader.this;
                            mediaItemsDataLoader.mRunCount = mediaItemsDataLoader.mRunCount + 1;
                            try {
                                call = getUpdateInfo.call();
                            } catch (Exception e) {
                                GalleryLog.d("MediaItemsDataLoader", "GetUpdateInfo.call error");
                                call = null;
                            }
                        } else {
                            call = (UpdateInfo) MediaItemsDataLoader.this.executeAndWait(getUpdateInfo);
                        }
                        if (call == null) {
                            z = true;
                        } else {
                            z = false;
                        }
                        if (!z) {
                            updateUpdateInfo(version, call);
                            MediaItemsDataLoader.this.executeAndWait(MediaItemsDataLoader.this.createUpdateContent(call));
                        }
                    } else {
                        updateLoading(false);
                        if (MediaItemsDataLoader.this.mFailedVersion != -1) {
                            GalleryLog.d("MediaItemsDataLoader", "MediaItemsDataLoader reload pause");
                        }
                        Utils.waitWithoutInterrupt(this);
                        if (this.mActive && MediaItemsDataLoader.this.mFailedVersion != -1) {
                            GalleryLog.d("MediaItemsDataLoader", "MediaItemsDataLoader reload resume");
                        }
                    }
                }
            }
            updateLoading(false);
        }

        private void updateUpdateInfo(long version, UpdateInfo info) {
            if (info.version != version) {
                info.size = Math.max(0, MediaItemsDataLoader.this.mSource.getMediaItemCount());
                info.groupDatas = ((IGroupAlbum) MediaItemsDataLoader.this.mSource).getGroupData();
                info.version = version;
                MediaItemsDataLoader.this.decorateUpdateInfo(info);
                if (MediaItemsDataLoader.this.mSource.supportCacheQuery() && MediaItemsDataLoader.this.mSource.resetIdCache(this.mIdCache)) {
                    MediaItemsDataLoader.this.mSource.setIdCache(this.mIdCache, info.size);
                }
            } else if (MediaItemsDataLoader.this.mSource.supportCacheQuery() && this.mIdCache.isEmpty()) {
                MediaItemsDataLoader.this.mSource.setIdCache(this.mIdCache, info.size);
            }
            if (info.reloadCount <= 0) {
                return;
            }
            if (MediaItemsDataLoader.this.mSource.supportCacheQuery() && MediaItemsDataLoader.this.mSource.isIdCacheReady(this.mIdCache, info.reloadStart, info.reloadCount)) {
                info.items = MediaItemsDataLoader.this.mSource.getMediaItemFromCache(this.mIdCache, info.reloadStart, info.reloadCount);
            } else {
                info.items = MediaItemsDataLoader.this.mSource.getMediaItem(info.reloadStart, info.reloadCount);
            }
        }

        public synchronized void notifyDirty() {
            this.mDirty = true;
            notifyAll();
        }

        public synchronized void terminate() {
            this.mActive = false;
            notifyAll();
        }

        private void updateLoading(boolean loading) {
            if (this.mIsLoading != loading) {
                this.mIsLoading = loading;
                MediaItemsDataLoader.this.mMainHandler.sendEmptyMessage(loading ? 1 : 2);
            }
        }
    }

    protected class UpdateContent implements Callable<Void> {
        private UpdateInfo mUpdateInfo;

        public UpdateContent(UpdateInfo info) {
            this.mUpdateInfo = info;
        }

        protected void detectChange(UpdateInfo info) {
            if (MediaItemsDataLoader.this.mSize != info.size || MediaItemsDataLoader.this.groupDatasChange(info.groupDatas)) {
                if (MediaItemsDataLoader.this.mSize != info.size) {
                    MediaItemsDataLoader.this.mMainHandler.obtainMessage(4, info.size, 0).sendToTarget();
                }
                MediaItemsDataLoader.this.mSize = info.size;
                synchronized (MediaItemsDataLoader.GROUPCOUNT_LOCK) {
                    MediaItemsDataLoader.this.mGroupDatas = info.groupDatas;
                    if (MediaItemsDataLoader.this.mGroupDatas == null) {
                        MediaItemsDataLoader.this.mGroupDatas = new ArrayList();
                    }
                }
                if (MediaItemsDataLoader.this.mDataListener != null) {
                    MediaItemsDataLoader.this.mDataListener.onSizeChanged(MediaItemsDataLoader.this.mSize, MediaItemsDataLoader.this.getGroupDatas(), TimeBucketPageViewMode.DAY);
                }
                if (MediaItemsDataLoader.this.mContentEnd > MediaItemsDataLoader.this.mSize) {
                    MediaItemsDataLoader.this.mContentEnd = MediaItemsDataLoader.this.mSize;
                }
                if (MediaItemsDataLoader.this.mActiveEnd > MediaItemsDataLoader.this.mSize) {
                    MediaItemsDataLoader.this.mActiveEnd = MediaItemsDataLoader.this.mSize;
                }
            }
        }

        public Void call() throws Exception {
            if (MediaItemsDataLoader.this.mReloadTask == null) {
                return null;
            }
            UpdateInfo info = this.mUpdateInfo;
            MediaItemsDataLoader.this.mSourceVersion = info.version;
            detectChange(info);
            ArrayList<MediaItem> items = info.items;
            MediaItemsDataLoader.this.mFailedVersion = -1;
            if (items == null || items.isEmpty()) {
                if (info.reloadCount > 0) {
                    MediaItemsDataLoader.this.mFailedVersion = info.version;
                    GalleryLog.d("MediaItemsDataLoader", "loading failed: " + MediaItemsDataLoader.this.mFailedVersion);
                }
                return null;
            }
            int start = Math.max(info.reloadStart, MediaItemsDataLoader.this.mContentStart);
            int end = Math.min(info.reloadStart + items.size(), MediaItemsDataLoader.this.mContentEnd);
            int i = start;
            while (i < end) {
                int index = i % 256;
                MediaItemsDataLoader.this.mSetVersion[index] = info.version;
                MediaItem updateItem = (MediaItem) items.get(i - info.reloadStart);
                long itemVersion = updateItem.getDataVersion();
                if (MediaItemsDataLoader.this.mItemVersion[index] != itemVersion) {
                    MediaItemsDataLoader.this.mItemVersion[index] = itemVersion;
                    MediaItemsDataLoader.this.mData[index] = updateItem;
                    if (MediaItemsDataLoader.this.mDataListener != null && i >= MediaItemsDataLoader.this.mActiveStart && i < MediaItemsDataLoader.this.mActiveEnd) {
                        MediaItemsDataLoader.this.mDataListener.onContentChanged(i);
                    }
                }
                i++;
            }
            if (MediaItemsDataLoader.this.mLoadingListener != null && (start <= MediaItemsDataLoader.this.mShowingStart || MediaItemsDataLoader.this.mShowingEnd > end)) {
                MediaItemsDataLoader.this.mLoadingListener.onVisibleRangeLoadFinished();
            }
            return null;
        }
    }

    protected static class UpdateInfo {
        public ArrayList<AbsGroupData> groupDatas;
        public ArrayList<MediaItem> items;
        public int reloadCount;
        public int reloadStart;
        public int size;
        public long version;

        protected UpdateInfo() {
        }
    }

    public void setLoadCountListener(LoadCountListener listenter) {
        this.mLoadCountListener = listenter;
    }

    public MediaItemsDataLoader(GalleryContext context, MediaSet mediaSet) {
        TraceController.beginSection("MediaItemsDataLoader");
        Utils.assertTrue(mediaSet instanceof IGroupAlbum);
        this.mSource = mediaSet;
        this.mData = new MediaItem[256];
        this.mItemVersion = new long[256];
        this.mSetVersion = new long[256];
        Arrays.fill(this.mItemVersion, -1);
        Arrays.fill(this.mSetVersion, -1);
        this.mContext = context;
        TraceController.endSection();
    }

    public void setGLRoot(GLRoot root) {
        if (this.mMainHandler != null) {
            this.mMainHandler.setGLRoot(root);
        } else {
            this.mMainHandler = new SynchronizedHandler(this.mContext.getMainLooper(), root) {
                public void handleMessage(Message message) {
                    switch (message.what) {
                        case 1:
                            if (MediaItemsDataLoader.this.mLoadingListener != null) {
                                MediaItemsDataLoader.this.mLoadingListener.onLoadingStarted();
                                return;
                            }
                            return;
                        case 2:
                            if (MediaItemsDataLoader.this.mLoadingListener != null) {
                                if (MediaItemsDataLoader.this.mSource.isQuickMode()) {
                                    MediaItemsDataLoader.this.mSource.setQuickMode(false);
                                    if (MediaItemsDataLoader.this.mReloadTask != null) {
                                        MediaItemsDataLoader.this.mReloadTask.notifyDirty();
                                    }
                                }
                                MediaItemsDataLoader.this.mLoadingListener.onLoadingFinished(MediaItemsDataLoader.this.mFailedVersion != -1);
                                return;
                            }
                            return;
                        case 3:
                            ((Runnable) message.obj).run();
                            return;
                        case 4:
                            if (MediaItemsDataLoader.this.mLoadCountListener != null) {
                                MediaItemsDataLoader.this.mLoadCountListener.onLoadCountChange(message.arg1);
                                return;
                            }
                            return;
                        default:
                            return;
                    }
                }
            };
        }
    }

    public void resume() {
        if (this.mReloadTask != null) {
            GalleryLog.d("MediaItemsDataLoader", "Reload task is not null");
            return;
        }
        this.mSource.addContentListener(this.mSourceListener);
        this.mReloadTask = new ReloadTask();
        this.mReloadTask.start();
    }

    public void pause() {
        if (this.mReloadTask != null) {
            this.mReloadTask.terminate();
            this.mReloadTask = null;
            this.mSource.removeContentListener(this.mSourceListener);
        }
    }

    public void destroy() {
    }

    public MediaItem get(int index) {
        if (isActive(index)) {
            return this.mData[index % this.mData.length];
        }
        return null;
    }

    public ArrayList<AbsGroupData> getGroupDatas() {
        ArrayList<AbsGroupData> result = new ArrayList();
        synchronized (GROUPCOUNT_LOCK) {
            result.addAll(this.mGroupDatas);
        }
        return result;
    }

    protected boolean groupDatasChange(ArrayList<AbsGroupData> groupDatas) {
        synchronized (GROUPCOUNT_LOCK) {
            if (this.mGroupDatas == null) {
                return true;
            }
            if (this.mGroupDatas.size() != (groupDatas != null ? groupDatas.size() : 0)) {
                return true;
            }
            return false;
        }
    }

    public boolean isActive(int index) {
        return index >= this.mActiveStart && index < this.mActiveEnd;
    }

    public int size() {
        return this.mSize;
    }

    public int preSize() {
        return 0;
    }

    private void clearSlot(int index) {
        this.mData[index] = null;
        this.mItemVersion[index] = -1;
        this.mSetVersion[index] = -1;
    }

    private void setContentWindow(int contentStart, int contentEnd) {
        if (contentStart != this.mContentStart || contentEnd != this.mContentEnd) {
            int end = this.mContentEnd;
            int start = this.mContentStart;
            this.mContentStart = contentStart;
            this.mContentEnd = contentEnd;
            int n;
            int i;
            if (contentStart >= end || start >= contentEnd) {
                n = end;
                for (i = start; i < end; i++) {
                    clearSlot(i % 256);
                }
            } else {
                for (i = start; i < contentStart; i++) {
                    clearSlot(i % 256);
                }
                n = end;
                for (i = contentEnd; i < end; i++) {
                    clearSlot(i % 256);
                }
            }
            if (this.mReloadTask != null) {
                this.mReloadTask.notifyDirty();
            }
        }
    }

    public void setUIWindow(int start, int end) {
        boolean z = false;
        if (start <= end && start >= this.mActiveStart && end <= this.mActiveEnd) {
            z = true;
        }
        Utils.assertTrue(z);
        this.mShowingStart = start;
        this.mShowingEnd = end;
    }

    public void setActiveWindow(int start, int end) {
        if (start != this.mActiveStart || end != this.mActiveEnd) {
            boolean z = (start > end || end - start > this.mData.length) ? false : end <= this.mSize;
            Utils.assertTrue(z);
            int length = this.mData.length;
            this.mActiveStart = start;
            this.mActiveEnd = end;
            int contentStart = Utils.clamp(((start + end) / 2) - (length / 2), 0, Math.max(0, this.mSize - length));
            int contentEnd = Math.min(contentStart + length, this.mSize);
            if (this.mContentStart <= start && this.mContentEnd >= end && this.mContentEnd <= this.mSize) {
                if (Math.abs(contentStart - this.mContentStart) > 32) {
                }
            }
            setContentWindow(contentStart, contentEnd);
        }
    }

    public void unfreeze() {
        super.unfreeze();
        if (this.mSourceListener != null) {
            this.mSourceListener.onContentDirty();
        }
        CloudManager cloudManager = (CloudManager) this.mContext.getGalleryApplication().getAppComponent(CloudManager.class);
        if (cloudManager != null) {
            cloudManager.forceRefresh();
        }
    }

    public void setDataListener(DataListener listener) {
        this.mDataListener = listener;
    }

    public void setLoadingListener(LoadingListener listener) {
        this.mLoadingListener = listener;
    }

    private <T> T executeAndWait(Callable<T> callable) {
        FutureTask<T> task = new FutureTask(callable);
        this.mMainHandler.sendMessage(this.mMainHandler.obtainMessage(3, task));
        try {
            return task.get();
        } catch (InterruptedException e) {
            return null;
        } catch (ExecutionException e2) {
            GalleryLog.printDFXLog("MediaItemsDataLoader ExecutionException for DFX");
            throw new RuntimeException(e2);
        }
    }

    protected GetUpdateInfo createGetUpdateInfo(long version) {
        return new GetUpdateInfo(version);
    }

    protected void decorateUpdateInfo(UpdateInfo info) {
    }

    protected UpdateContent createUpdateContent(UpdateInfo info) {
        return new UpdateContent(info);
    }
}

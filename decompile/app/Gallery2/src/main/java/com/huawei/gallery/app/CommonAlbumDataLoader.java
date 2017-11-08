package com.huawei.gallery.app;

import android.os.Message;
import android.os.Process;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.app.LoadingListener;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.ContentListener;
import com.android.gallery3d.data.LocalMergeCardAlbum;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.TraceController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class CommonAlbumDataLoader extends AlbumDataLoader {
    private int mActiveEnd = 0;
    private int mActiveStart = 0;
    private int mContentEnd = 0;
    private int mContentStart = 0;
    private GalleryContext mContext;
    private final MediaItem[] mData;
    private DataListener mDataListener;
    private long mFailedVersion = -1;
    private final long[] mItemVersion;
    private LoadingListener mLoadingListener;
    private SynchronizedHandler mMainHandler;
    private int mPreSize = 0;
    private ReloadTask mReloadTask;
    private final long[] mSetVersion;
    private int mSize = 0;
    private final MediaSet mSource;
    private MySourceListener mSourceListener = new MySourceListener();
    private long mSourceVersion = -1;

    public interface DataListener {
        void onContentChanged(int i);

        void onSizeChanged(int i);
    }

    private class GetUpdateInfo implements Callable<UpdateInfo> {
        private final long mVersion;

        public GetUpdateInfo(long version) {
            this.mVersion = version;
        }

        public UpdateInfo call() throws Exception {
            UpdateInfo info = new UpdateInfo();
            long version = this.mVersion;
            info.version = CommonAlbumDataLoader.this.mSourceVersion;
            info.size = CommonAlbumDataLoader.this.mSize;
            info.preSize = CommonAlbumDataLoader.this.mPreSize;
            long[] setVersion = CommonAlbumDataLoader.this.mSetVersion;
            int n = CommonAlbumDataLoader.this.mContentEnd;
            for (int i = CommonAlbumDataLoader.this.mContentStart; i < n; i++) {
                if (setVersion[i % 256] != version) {
                    info.reloadStart = i;
                    info.reloadCount = Math.min(64, n - i);
                    GalleryLog.printDFXLog("CommonAlbumDataLoader for DFX reloadCount " + info.reloadCount);
                    return info;
                }
            }
            if (CommonAlbumDataLoader.this.mSourceVersion == this.mVersion) {
                info = null;
            }
            return info;
        }
    }

    private class MySourceListener implements ContentListener {
        private MySourceListener() {
        }

        public void onContentDirty() {
            if (CommonAlbumDataLoader.this.mReloadTask != null && !CommonAlbumDataLoader.this.mReloadLock) {
                CommonAlbumDataLoader.this.mReloadTask.notifyDirty();
            }
        }
    }

    private class ReloadTask extends Thread {
        private volatile boolean mActive;
        private volatile boolean mDirty;
        private ArrayList<Integer> mIdCache;
        private boolean mIsLoading;

        private ReloadTask() {
            this.mActive = true;
            this.mDirty = true;
            this.mIsLoading = false;
            this.mIdCache = new ArrayList();
        }

        public void run() {
            Process.setThreadPriority(10);
            boolean updateComplete = false;
            while (this.mActive) {
                synchronized (this) {
                    if (this.mActive && !this.mDirty && updateComplete) {
                        updateLoading(false);
                        if (CommonAlbumDataLoader.this.mFailedVersion != -1) {
                            GalleryLog.d("CommonAlbumDataLoader", "CommonAlbumDataLoader reload pause");
                        }
                        Utils.waitWithoutInterrupt(this);
                        if (this.mActive && CommonAlbumDataLoader.this.mFailedVersion != -1) {
                            GalleryLog.d("CommonAlbumDataLoader", "CommonAlbumDataLoader reload resume");
                        }
                    } else {
                        this.mDirty = false;
                        updateLoading(true);
                        long version = CommonAlbumDataLoader.this.mSource.reload();
                        UpdateInfo info = (UpdateInfo) CommonAlbumDataLoader.this.executeAndWait(new GetUpdateInfo(version));
                        if (info == null) {
                            updateComplete = true;
                        } else {
                            updateComplete = false;
                        }
                        if (!updateComplete) {
                            GalleryLog.e("YUN", "run start");
                            updateInfo(info, version);
                            GalleryLog.e("YUN", "run end");
                            CommonAlbumDataLoader.this.executeAndWait(new UpdateContent(info));
                        }
                    }
                }
            }
            updateLoading(false);
        }

        private void updateInfo(UpdateInfo info, long version) {
            if (info.version != version) {
                info.size = Math.max(0, CommonAlbumDataLoader.this.mSource.getMediaItemCount());
                info.preSize = Math.max(0, CommonAlbumDataLoader.this.mSource.getPreViewCount());
                info.version = version;
                if (CommonAlbumDataLoader.this.mSource.supportCacheQuery() && CommonAlbumDataLoader.this.mSource.resetIdCache(this.mIdCache)) {
                    CommonAlbumDataLoader.this.mSource.setIdCache(this.mIdCache, info.size);
                }
            } else if (CommonAlbumDataLoader.this.mSource.supportCacheQuery() && this.mIdCache.isEmpty()) {
                CommonAlbumDataLoader.this.mSource.setIdCache(this.mIdCache, info.size);
            }
            if (info.reloadCount <= 0) {
                return;
            }
            if (CommonAlbumDataLoader.this.mSource.supportCacheQuery() && CommonAlbumDataLoader.this.mSource.isIdCacheReady(this.mIdCache, info.reloadStart, info.reloadCount)) {
                info.items = CommonAlbumDataLoader.this.mSource.getMediaItemFromCache(this.mIdCache, info.reloadStart, info.reloadCount);
            } else {
                info.items = CommonAlbumDataLoader.this.mSource.getMediaItem(info.reloadStart, info.reloadCount);
            }
        }

        private void updateLoading(boolean loading) {
            if (this.mIsLoading != loading) {
                this.mIsLoading = loading;
                CommonAlbumDataLoader.this.mMainHandler.sendEmptyMessage(loading ? 1 : 2);
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
    }

    private class UpdateContent implements Callable<Void> {
        private UpdateInfo mUpdateInfo;

        public UpdateContent(UpdateInfo info) {
            this.mUpdateInfo = info;
        }

        public Void call() throws Exception {
            if (CommonAlbumDataLoader.this.mReloadTask == null) {
                return null;
            }
            UpdateInfo info = this.mUpdateInfo;
            CommonAlbumDataLoader.this.mSourceVersion = info.version;
            if (CommonAlbumDataLoader.this.mPreSize != info.preSize) {
                CommonAlbumDataLoader.this.mPreSize = info.preSize;
            }
            if (CommonAlbumDataLoader.this.mSize != info.size) {
                CommonAlbumDataLoader.this.mSize = info.size;
                if (CommonAlbumDataLoader.this.mDataListener != null) {
                    CommonAlbumDataLoader.this.mDataListener.onSizeChanged(CommonAlbumDataLoader.this.mSize);
                }
                if (CommonAlbumDataLoader.this.mContentEnd > CommonAlbumDataLoader.this.mSize) {
                    CommonAlbumDataLoader.this.mContentEnd = CommonAlbumDataLoader.this.mSize;
                }
                if (CommonAlbumDataLoader.this.mActiveEnd > CommonAlbumDataLoader.this.mSize) {
                    CommonAlbumDataLoader.this.mActiveEnd = CommonAlbumDataLoader.this.mSize;
                }
            }
            ArrayList<MediaItem> items = info.items;
            CommonAlbumDataLoader.this.mFailedVersion = -1;
            if (items == null || items.isEmpty()) {
                if (info.reloadCount > 0) {
                    CommonAlbumDataLoader.this.mFailedVersion = info.version;
                    GalleryLog.d("CommonAlbumDataLoader", "DFX CommonAlbumDataLoader loading failed: " + CommonAlbumDataLoader.this.mFailedVersion);
                }
                return null;
            }
            int end = Math.min(info.reloadStart + items.size(), CommonAlbumDataLoader.this.mContentEnd);
            int i = Math.max(info.reloadStart, CommonAlbumDataLoader.this.mContentStart);
            while (i < end) {
                int index = i % 256;
                CommonAlbumDataLoader.this.mSetVersion[index] = info.version;
                MediaItem updateItem = (MediaItem) items.get(i - info.reloadStart);
                long itemVersion = updateItem.getDataVersion();
                if (CommonAlbumDataLoader.this.mItemVersion[index] != itemVersion) {
                    CommonAlbumDataLoader.this.mItemVersion[index] = itemVersion;
                    CommonAlbumDataLoader.this.mData[index] = updateItem;
                    if (CommonAlbumDataLoader.this.mDataListener != null && i >= CommonAlbumDataLoader.this.mActiveStart && i < CommonAlbumDataLoader.this.mActiveEnd) {
                        CommonAlbumDataLoader.this.mDataListener.onContentChanged(i);
                    }
                }
                i++;
            }
            return null;
        }
    }

    private static class UpdateInfo {
        public ArrayList<MediaItem> items;
        public int preSize;
        public int reloadCount;
        public int reloadStart;
        public int size;
        public long version;

        private UpdateInfo() {
        }
    }

    public CommonAlbumDataLoader(GalleryContext context, MediaSet mediaSet) {
        this.mSource = mediaSet;
        TraceController.beginSection("CommonAlbumDataLoader");
        this.mContext = context;
        this.mData = new MediaItem[256];
        this.mItemVersion = new long[256];
        this.mSetVersion = new long[256];
        Arrays.fill(this.mItemVersion, -1);
        Arrays.fill(this.mSetVersion, -1);
        TraceController.endSection();
    }

    public void setGLRoot(GLRoot root) {
        if (this.mMainHandler != null) {
            this.mMainHandler.setGLRoot(root);
            GalleryLog.printDFXLog("setGLRoot for DFX");
            return;
        }
        this.mMainHandler = new SynchronizedHandler(this.mContext.getMainLooper(), root) {
            public void handleMessage(Message message) {
                switch (message.what) {
                    case 1:
                        if (CommonAlbumDataLoader.this.mLoadingListener != null) {
                            CommonAlbumDataLoader.this.mLoadingListener.onLoadingStarted();
                        }
                        GalleryLog.printDFXLog("MSG_LOAD_START for DFX");
                        return;
                    case 2:
                        if (CommonAlbumDataLoader.this.mLoadingListener != null) {
                            CommonAlbumDataLoader.this.mLoadingListener.onLoadingFinished(CommonAlbumDataLoader.this.mFailedVersion != -1);
                        }
                        return;
                    case 3:
                        ((Runnable) message.obj).run();
                        return;
                    default:
                        return;
                }
            }
        };
    }

    public void resume() {
        this.mSource.addContentListener(this.mSourceListener);
        this.mReloadTask = new ReloadTask();
        this.mReloadTask.start();
    }

    public void pause() {
        this.mReloadTask.terminate();
        this.mReloadTask = null;
        this.mSource.removeContentListener(this.mSourceListener);
    }

    public MediaItem get(int index) {
        if (!isActive(index)) {
            ArrayList<MediaItem> mediaItems = this.mSource.getMediaItem(index, 1);
            if (mediaItems != null && mediaItems.size() > 0) {
                return (MediaItem) mediaItems.get(0);
            }
        }
        return this.mData[index % this.mData.length];
    }

    public boolean isActive(int index) {
        return index >= this.mActiveStart && index < this.mActiveEnd;
    }

    public int size() {
        return this.mSize;
    }

    public int preSize() {
        return this.mPreSize;
    }

    private void clearSlot(int slotIndex) {
        this.mData[slotIndex] = null;
        this.mItemVersion[slotIndex] = -1;
        this.mSetVersion[slotIndex] = -1;
    }

    private void setContentWindow(int contentStart, int contentEnd) {
        if (contentStart != this.mContentStart || contentEnd != this.mContentEnd) {
            int end = this.mContentEnd;
            int start = this.mContentStart;
            this.mContentStart = contentStart;
            this.mContentEnd = contentEnd;
            int j;
            int i;
            if (contentStart >= end || start >= contentEnd) {
                j = end;
                for (i = start; i < end; i++) {
                    clearSlot(i % 256);
                }
            } else {
                for (i = start; i < contentStart; i++) {
                    clearSlot(i % 256);
                }
                j = end;
                for (i = contentEnd; i < end; i++) {
                    clearSlot(i % 256);
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
                    if (Math.abs(contentStart - this.mContentStart) > 32) {
                    }
                }
                setContentWindow(contentStart, contentEnd);
            }
        }
    }

    public void unfreeze() {
        super.unfreeze();
        if (this.mSourceListener != null) {
            this.mSourceListener.onContentDirty();
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
            GalleryLog.printDFXLog("CommonAlbumDataLoader InterruptedException for DFX");
            return null;
        } catch (ExecutionException e2) {
            throw new RuntimeException(e2);
        }
    }

    public void filterCameraLocation(int locationType) {
        if (this.mSource instanceof LocalMergeCardAlbum) {
            this.mSource.filterMergeCardLocation(locationType);
            if (this.mReloadTask != null) {
                this.mReloadTask.notifyDirty();
            }
        }
    }
}

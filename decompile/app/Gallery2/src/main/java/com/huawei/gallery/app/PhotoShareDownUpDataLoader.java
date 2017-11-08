package com.huawei.gallery.app;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import com.android.gallery3d.app.LoadingListener;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.ContentListener;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.PhotoShareDownUpAlbum;
import com.android.gallery3d.util.GalleryLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class PhotoShareDownUpDataLoader {
    private int mActiveEnd = 0;
    private int mActiveStart = 0;
    private int mContentEnd = 0;
    private int mContentStart = 0;
    private final MediaItem[] mData;
    private long mFailedVersion = -1;
    private final long[] mItemVersion;
    private DataListener mListener;
    private LoadingListener mLoadingListener;
    private final Handler mMainHandler;
    private ReloadTask mReloadTask;
    private final long[] mSetVersion;
    private int mSize = 0;
    private final PhotoShareDownUpAlbum mSource;
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
            if (PhotoShareDownUpDataLoader.this.mFailedVersion == this.mVersion) {
                return null;
            }
            UpdateInfo info = new UpdateInfo();
            long version = this.mVersion;
            info.version = PhotoShareDownUpDataLoader.this.mSourceVersion;
            info.size = PhotoShareDownUpDataLoader.this.mSize;
            long[] setVersion = PhotoShareDownUpDataLoader.this.mSetVersion;
            int n = PhotoShareDownUpDataLoader.this.mContentEnd;
            for (int i = PhotoShareDownUpDataLoader.this.mContentStart; i < n; i++) {
                if (setVersion[i % 256] != version) {
                    info.reloadStart = i;
                    info.reloadCount = Math.min(64, n - i);
                    GalleryLog.printDFXLog("PhotoShareDownUpDataLoader for DFX reloadCount " + info.reloadCount);
                    return info;
                }
            }
            if (PhotoShareDownUpDataLoader.this.mSourceVersion == this.mVersion) {
                info = null;
            }
            return info;
        }
    }

    private class MySourceListener implements ContentListener {
        private MySourceListener() {
        }

        public void onContentDirty() {
            if (PhotoShareDownUpDataLoader.this.mReloadTask != null) {
                PhotoShareDownUpDataLoader.this.mReloadTask.notifyDirty();
            }
        }
    }

    private class ReloadTask extends Thread {
        private volatile boolean mActive;
        private volatile boolean mDirty;
        private volatile boolean mIsLoading;

        private ReloadTask() {
            this.mActive = true;
            this.mDirty = true;
            this.mIsLoading = false;
        }

        private void updateLoading(boolean loading) {
            if (this.mIsLoading != loading) {
                this.mIsLoading = loading;
                PhotoShareDownUpDataLoader.this.mMainHandler.sendEmptyMessage(loading ? 1 : 2);
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

        public void run() {
            Process.setThreadPriority(10);
            boolean updateComplete = false;
            while (this.mActive) {
                synchronized (this) {
                    if (this.mActive && !this.mDirty && updateComplete) {
                        updateLoading(false);
                        if (PhotoShareDownUpDataLoader.this.mFailedVersion != -1) {
                            GalleryLog.d("PhotoShareDownUpDataLoader", "PhotoShareDownUpDataLoader reload pause");
                        }
                        Utils.waitWithoutInterrupt(this);
                        if (this.mActive && PhotoShareDownUpDataLoader.this.mFailedVersion != -1) {
                            GalleryLog.d("PhotoShareDownUpDataLoader", "CommonAlbumDataLoader reload resume");
                        }
                    } else {
                        this.mDirty = false;
                        GalleryLog.printDFXLog("Photosharedownuploader start loading");
                        updateLoading(true);
                        long version = PhotoShareDownUpDataLoader.this.mSource.reload();
                        UpdateInfo info = (UpdateInfo) PhotoShareDownUpDataLoader.this.executeAndWait(new GetUpdateInfo(version));
                        if (info == null) {
                            updateComplete = true;
                        } else {
                            updateComplete = false;
                        }
                        if (!updateComplete) {
                            if (info.version != version) {
                                info.size = PhotoShareDownUpDataLoader.this.mSource.getMediaItemCount();
                                info.version = version;
                            }
                            if (info.reloadCount > 0) {
                                info.items = PhotoShareDownUpDataLoader.this.mSource.getMediaItem(info.reloadStart, info.reloadCount);
                            }
                            PhotoShareDownUpDataLoader.this.executeAndWait(new UpdateContent(info));
                        }
                    }
                }
            }
            updateLoading(false);
        }
    }

    private class UpdateContent implements Callable<Void> {
        private UpdateInfo mUpdateInfo;

        public UpdateContent(UpdateInfo info) {
            this.mUpdateInfo = info;
        }

        public Void call() throws Exception {
            UpdateInfo info = this.mUpdateInfo;
            PhotoShareDownUpDataLoader.this.mSourceVersion = info.version;
            if (PhotoShareDownUpDataLoader.this.mSize != info.size) {
                PhotoShareDownUpDataLoader.this.mSize = info.size;
                if (PhotoShareDownUpDataLoader.this.mListener != null) {
                    PhotoShareDownUpDataLoader.this.mListener.onSizeChanged(PhotoShareDownUpDataLoader.this.mSize);
                }
                if (PhotoShareDownUpDataLoader.this.mContentEnd > PhotoShareDownUpDataLoader.this.mSize) {
                    PhotoShareDownUpDataLoader.this.mContentEnd = PhotoShareDownUpDataLoader.this.mSize;
                }
                if (PhotoShareDownUpDataLoader.this.mActiveEnd > PhotoShareDownUpDataLoader.this.mSize) {
                    PhotoShareDownUpDataLoader.this.mActiveEnd = PhotoShareDownUpDataLoader.this.mSize;
                }
            }
            ArrayList<MediaItem> items = info.items;
            PhotoShareDownUpDataLoader.this.mFailedVersion = -1;
            if (items == null || items.isEmpty()) {
                if (info.reloadCount > 0) {
                    PhotoShareDownUpDataLoader.this.mFailedVersion = info.version;
                    GalleryLog.d("PhotoShareDownUpDataLoader", "loading info failed: " + PhotoShareDownUpDataLoader.this.mFailedVersion);
                }
                return null;
            }
            int start = Math.max(info.reloadStart, PhotoShareDownUpDataLoader.this.mContentStart);
            int end = Math.min(info.reloadStart + items.size(), PhotoShareDownUpDataLoader.this.mContentEnd);
            GalleryLog.printDFXLog("PhotoShareDownUpDataLoader.UpdateContent.call start update info");
            int i = start;
            while (i < end) {
                int index = i % 256;
                PhotoShareDownUpDataLoader.this.mSetVersion[index] = info.version;
                MediaItem updateItem = (MediaItem) items.get(i - info.reloadStart);
                long itemVersion = updateItem.getDataVersion();
                if (PhotoShareDownUpDataLoader.this.mItemVersion[index] != itemVersion) {
                    PhotoShareDownUpDataLoader.this.mItemVersion[index] = itemVersion;
                    PhotoShareDownUpDataLoader.this.mData[index] = updateItem;
                    if (PhotoShareDownUpDataLoader.this.mListener != null && i >= PhotoShareDownUpDataLoader.this.mActiveStart && i < PhotoShareDownUpDataLoader.this.mActiveEnd) {
                        PhotoShareDownUpDataLoader.this.mListener.onContentChanged(i);
                    }
                }
                i++;
            }
            return null;
        }
    }

    private static class UpdateInfo {
        public ArrayList<MediaItem> items;
        public int reloadCount;
        public int reloadStart;
        public int size;
        public long version;

        private UpdateInfo() {
        }
    }

    public PhotoShareDownUpDataLoader(Context context, PhotoShareDownUpAlbum source) {
        this.mSource = source;
        this.mData = new MediaItem[256];
        this.mItemVersion = new long[256];
        this.mSetVersion = new long[256];
        Arrays.fill(this.mItemVersion, -1);
        Arrays.fill(this.mSetVersion, -1);
        this.mMainHandler = new Handler() {
            public void handleMessage(Message message) {
                switch (message.what) {
                    case 1:
                        GalleryLog.printDFXLog("PhotoShareDownUpDataLoader MSG_LOAD_START");
                        if (PhotoShareDownUpDataLoader.this.mLoadingListener != null) {
                            PhotoShareDownUpDataLoader.this.mLoadingListener.onLoadingStarted();
                        }
                        return;
                    case 2:
                        if (PhotoShareDownUpDataLoader.this.mLoadingListener != null) {
                            PhotoShareDownUpDataLoader.this.mLoadingListener.onLoadingFinished(false);
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

    public void setListener(DataListener listener) {
        this.mListener = listener;
    }

    public void setLoadListener(LoadingListener listener) {
        this.mLoadingListener = listener;
    }

    public void pause() {
        if (this.mReloadTask != null) {
            this.mReloadTask.terminate();
            this.mReloadTask = null;
            this.mSource.removeContentListener(this.mSourceListener);
        }
    }

    public void resume() {
        if (this.mReloadTask == null) {
            this.mSource.addContentListener(this.mSourceListener);
            this.mReloadTask = new ReloadTask();
            this.mReloadTask.start();
        }
    }

    public int size() {
        return this.mSize;
    }

    private void clearSlot(int index) {
        this.mData[index] = null;
        this.mItemVersion[index] = -1;
        this.mSetVersion[index] = -1;
    }

    private void setContentWindow(int contentStart, int contentEnd) {
        if (contentStart != this.mContentStart || contentEnd != this.mContentEnd) {
            int oldStart = this.mContentStart;
            int oldEnd = this.mContentEnd;
            this.mContentStart = contentStart;
            this.mContentEnd = contentEnd;
            int n;
            int i;
            if (contentStart >= oldEnd || oldStart >= contentEnd) {
                n = oldEnd;
                for (i = oldStart; i < oldEnd; i++) {
                    clearSlot(i % 256);
                }
            } else {
                for (i = oldStart; i < contentStart; i++) {
                    clearSlot(i % 256);
                }
                n = oldEnd;
                for (i = contentEnd; i < oldEnd; i++) {
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
            boolean z;
            if (start > end || end - start > this.mData.length || end > this.mSize) {
                z = false;
            } else {
                z = true;
            }
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

    private boolean isActive(int index) {
        return index >= this.mActiveStart && index < this.mActiveEnd;
    }

    public MediaItem get(int i) {
        if (isActive(i)) {
            return this.mData[i % 256];
        }
        return null;
    }

    private <T> T executeAndWait(Callable<T> callable) {
        FutureTask<T> task = new FutureTask(callable);
        this.mMainHandler.sendMessage(this.mMainHandler.obtainMessage(3, task));
        try {
            return task.get();
        } catch (InterruptedException e) {
            GalleryLog.printDFXLog("DFX PhotoShareDownUpDataLoader InterruptedException");
            return null;
        } catch (ExecutionException e2) {
            throw new RuntimeException(e2);
        }
    }
}

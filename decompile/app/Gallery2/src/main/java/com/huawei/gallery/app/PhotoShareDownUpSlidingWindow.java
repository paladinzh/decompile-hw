package com.huawei.gallery.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.BitmapPool;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.PhotoShareDownUpItem;
import com.android.gallery3d.ui.BitmapLoader;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.JobLimiter;
import com.huawei.gallery.app.PhotoShareDownUpDataLoader.DataListener;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils.DownLoadProgressListener;

public class PhotoShareDownUpSlidingWindow implements DownLoadProgressListener, DataListener, Callback {
    private int mActiveEnd = 0;
    private int mActiveRequestCount = 0;
    private int mActiveStart = 0;
    private int mContentEnd = 0;
    private int mContentStart = 0;
    private final Context mContext;
    private final ItemsEntry[] mData;
    private Handler mHandler;
    private boolean mIsActive = false;
    private Listener mListener;
    private int mSize;
    private final PhotoShareDownUpDataLoader mSource;
    private final JobLimiter mThreadPool;

    public interface Listener {
        void onContentChanged(int i);

        void onSizeChanged(int i);
    }

    private class IconLoader extends BitmapLoader {
        private final int mIndex;
        private final MediaItem mItem;

        public IconLoader(int index, MediaItem item) {
            this.mIndex = index;
            this.mItem = item;
        }

        protected Future<Bitmap> submitBitmapTask(FutureListener<Bitmap> l) {
            return PhotoShareDownUpSlidingWindow.this.mThreadPool.submit(this.mItem.requestImage(2), l);
        }

        protected void recycleBitmap(Bitmap bitmap) {
            BitmapPool pool = MediaItem.getMicroThumbPool();
            if (pool != null) {
                pool.recycle(bitmap);
            }
        }

        protected void onLoadComplete(Bitmap bitmap) {
            PhotoShareDownUpSlidingWindow.this.mHandler.obtainMessage(1, this).sendToTarget();
        }

        protected void onPreviewLoad(Bitmap bitmap) {
            onLoadComplete(bitmap);
        }

        public void updateIcon() {
            ItemsEntry entry = PhotoShareDownUpSlidingWindow.this.mData[this.mIndex % PhotoShareDownUpSlidingWindow.this.mData.length];
            if (entry != null) {
                Bitmap bitmap = getBitmap();
                if (bitmap == null) {
                    bitmap = BitmapFactory.decodeResource(PhotoShareDownUpSlidingWindow.this.mContext.getResources(), R.drawable.ic_list_album_damage2);
                } else if (bitmap.isRecycled()) {
                    return;
                }
                entry.icon = bitmap;
                if (PhotoShareDownUpSlidingWindow.this.isActiveSlot(this.mIndex)) {
                    PhotoShareDownUpSlidingWindow photoShareDownUpSlidingWindow = PhotoShareDownUpSlidingWindow.this;
                    photoShareDownUpSlidingWindow.mActiveRequestCount = photoShareDownUpSlidingWindow.mActiveRequestCount - 1;
                    if (PhotoShareDownUpSlidingWindow.this.mActiveRequestCount == 0) {
                        PhotoShareDownUpSlidingWindow.this.requestNonactiveImages();
                    }
                    if (PhotoShareDownUpSlidingWindow.this.mListener != null) {
                        PhotoShareDownUpSlidingWindow.this.mListener.onContentChanged(this.mIndex);
                    }
                }
            }
        }
    }

    public static class ItemsEntry {
        public String fileName;
        public String filePath;
        public long fileSize;
        public Bitmap icon;
        public IconLoader iconLoader;
        public MediaItem mediaItem;
        public int percent;
        public int rotation;
        public int state;
        public long time;
    }

    public PhotoShareDownUpSlidingWindow(Activity context, PhotoShareDownUpDataLoader source) {
        this.mContext = context;
        this.mThreadPool = new JobLimiter(((GalleryApp) context.getApplication()).getThreadPool(), 2);
        this.mHandler = new Handler(this);
        this.mSource = source;
        this.mSource.setListener(this);
        this.mSize = this.mSource.size();
        this.mData = new ItemsEntry[32];
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public void resume() {
        this.mIsActive = true;
        PhotoShareUtils.addListener(this);
        int n = this.mContentEnd;
        for (int i = this.mContentStart; i < n; i++) {
            prepareSlotContent(i);
        }
        updateAllImageRequests();
    }

    public void pause() {
        this.mIsActive = false;
        PhotoShareUtils.removeListener(this);
        int n = this.mContentEnd;
        for (int i = this.mContentStart; i < n; i++) {
            freeSlotContent(i);
        }
    }

    private boolean isActiveSlot(int slotIndex) {
        return slotIndex >= this.mActiveStart && slotIndex < this.mActiveEnd;
    }

    public void setActiveWindow(int start, int end) {
        if (start != this.mActiveStart || end != this.mActiveEnd) {
            this.mActiveStart = start;
            this.mActiveEnd = end;
            ItemsEntry[] data = this.mData;
            int contentStart = Utils.clamp(((start + end) / 2) - (data.length / 2), 0, Math.max(0, this.mSize - data.length));
            setContentWindow(contentStart, Math.min(data.length + contentStart, this.mSize));
            if (this.mIsActive) {
                updateAllImageRequests();
            }
        }
    }

    private void setContentWindow(int contentStart, int contentEnd) {
        if (contentStart != this.mContentStart || contentEnd != this.mContentEnd) {
            int n;
            int i;
            if (contentStart >= this.mContentEnd || this.mContentStart >= contentEnd) {
                n = this.mContentEnd;
                for (i = this.mContentStart; i < n; i++) {
                    freeSlotContent(i);
                }
                this.mSource.setActiveWindow(contentStart, contentEnd);
                GalleryLog.printDFXLog("PhotoShareDownUpSlidingWindow.setContentWindow setActiveWindow");
                for (i = contentStart; i < contentEnd; i++) {
                    prepareSlotContent(i);
                }
            } else {
                for (i = this.mContentStart; i < contentStart; i++) {
                    freeSlotContent(i);
                }
                n = this.mContentEnd;
                for (i = contentEnd; i < n; i++) {
                    freeSlotContent(i);
                }
                this.mSource.setActiveWindow(contentStart, contentEnd);
                GalleryLog.printDFXLog("PhotoShareDownUpSlidingWindow.setContentWindow setActiveWindow");
                n = this.mContentStart;
                for (i = contentStart; i < n; i++) {
                    prepareSlotContent(i);
                }
                for (i = this.mContentEnd; i < contentEnd; i++) {
                    prepareSlotContent(i);
                }
            }
            this.mContentStart = contentStart;
            this.mContentEnd = contentEnd;
        }
    }

    private void updateAllImageRequests() {
        this.mActiveRequestCount = 0;
        int n = this.mActiveEnd;
        for (int i = this.mActiveStart; i < n; i++) {
            if (requestSlotImage(i)) {
                this.mActiveRequestCount++;
            }
        }
        if (this.mActiveRequestCount == 0) {
            requestNonactiveImages();
        } else {
            cancelNonactiveImages();
        }
    }

    private boolean requestSlotImage(int index) {
        if (index < this.mContentStart || index >= this.mContentEnd) {
            return false;
        }
        ItemsEntry entry = this.mData[index % this.mData.length];
        if (entry.iconLoader == null || entry.filePath == null) {
            return false;
        }
        entry.iconLoader.startLoad();
        return entry.iconLoader.isRequestInProgress();
    }

    private void cancelSlotImage(int slotIndex) {
        if (slotIndex >= this.mContentStart && slotIndex < this.mContentEnd) {
            ItemsEntry entry = this.mData[slotIndex % this.mData.length];
            if (!(entry == null || entry.iconLoader == null)) {
                entry.iconLoader.cancelLoad();
            }
        }
    }

    private void requestNonactiveImages() {
        int range = Math.max(this.mContentEnd - this.mActiveEnd, this.mActiveStart - this.mContentStart);
        for (int i = 0; i < range; i++) {
            requestSlotImage(this.mActiveEnd + i);
            requestSlotImage((this.mActiveStart - 1) - i);
        }
    }

    private void cancelNonactiveImages() {
        int range = Math.max(this.mContentEnd - this.mActiveEnd, this.mActiveStart - this.mContentStart);
        for (int i = 0; i < range; i++) {
            cancelSlotImage(this.mActiveEnd + i);
            cancelSlotImage((this.mActiveStart - 1) - i);
        }
    }

    private void updateSlotContent(ItemsEntry entry, int index) {
        MediaItem item = this.mSource.get(index);
        if (item != null) {
            PhotoShareDownUpItem mediaItem = (PhotoShareDownUpItem) item;
            entry.mediaItem = mediaItem;
            entry.fileName = mediaItem.getFileName();
            entry.filePath = mediaItem.getFilePath();
            entry.fileSize = mediaItem.getFileSize();
            entry.rotation = mediaItem.getRotation();
            entry.state = mediaItem.getState();
            if (entry.state == 16) {
                entry.time = mediaItem.getFinishTime();
            } else {
                entry.time = mediaItem.getAddTime();
            }
            entry.percent = 0;
            entry.iconLoader = new IconLoader(index, mediaItem);
        }
    }

    private void prepareSlotContent(int index) {
        ItemsEntry entry = new ItemsEntry();
        updateSlotContent(entry, index);
        this.mData[index % this.mData.length] = entry;
    }

    private void freeSlotContent(int index) {
        ItemsEntry[] data = this.mData;
        int i = index % data.length;
        ItemsEntry entry = data[i];
        if (entry != null) {
            if (entry.iconLoader != null) {
                entry.iconLoader.recycle();
            }
            data[i] = null;
        }
    }

    public int getCount() {
        return this.mSize;
    }

    public ItemsEntry getItem(int index) {
        if (this.mIsActive) {
            return this.mData[index % this.mData.length];
        }
        return null;
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                ((IconLoader) msg.obj).updateIcon();
                return true;
            default:
                return false;
        }
    }

    public void onContentChanged(int index) {
        if (this.mIsActive && index >= this.mContentStart && index < this.mContentEnd) {
            freeSlotContent(index);
            prepareSlotContent(index);
            updateAllImageRequests();
            if (this.mListener != null && isActiveSlot(index)) {
                this.mListener.onContentChanged(index);
            }
        }
    }

    public void onSizeChanged(int size) {
        if (this.mSize != size) {
            this.mSize = size;
            if (this.mListener != null) {
                this.mListener.onSizeChanged(this.mSize);
            }
            if (this.mContentEnd > this.mSize) {
                this.mContentEnd = this.mSize;
            }
            if (this.mActiveEnd > this.mSize) {
                this.mActiveEnd = this.mSize;
            }
        }
    }

    public void downloadProgress(String hash, String albumId, String uniqueId, int thumbType, Long totalSize, Long currentSize) {
        if (thumbType == 0) {
            int i = 0;
            while (i < this.mData.length) {
                ItemsEntry entry = this.mData[i];
                if (entry == null || entry.mediaItem == null || !entry.mediaItem.equal(albumId, hash)) {
                    i++;
                } else {
                    entry.percent = (int) ((currentSize.longValue() * 100) / totalSize.longValue());
                    if (this.mListener != null) {
                        this.mListener.onSizeChanged(this.mSize);
                    }
                    return;
                }
            }
        }
    }

    public void downloadFinish(String hash, String albumId, String uniqueId, int thumbType, int result) {
    }
}

package com.huawei.gallery.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.BitmapLoader;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.JobLimiter;
import com.huawei.gallery.app.AlbumSetDataLoader;
import com.huawei.gallery.app.AlbumSetDataLoader.DataListener;
import com.huawei.gallery.util.GalleryPool;
import java.util.ArrayList;
import java.util.HashMap;

public class AlbumSetSlidingWindow implements DataListener, Callback {
    private int mActiveEnd = 0;
    private int mActiveRequestCount = 0;
    private int mActiveStart = 0;
    private AlbumCoverController mAlbumCoverController;
    private int mContentEnd = 0;
    private int mContentStart = 0;
    private Activity mContext;
    private final AlbumSetEntry[] mData;
    private AlbumSetEntry mDragEntry;
    private Handler mHandler;
    private boolean mIsActive = false;
    private Listener mListener;
    private int mSize;
    private final AlbumSetDataLoader mSource;
    private final JobLimiter mThreadPool;

    public interface AlbumCoverController {
        int changModifyTimeBy();

        int getThumbnailType();
    }

    public interface Listener {
        void onContentChanged(int i);

        void onSizeChanged(int i);
    }

    private class AlbumCoverLoader extends BitmapLoader {
        private int mChangeTypeBy = 0;
        private boolean mIsOtherAlbum = false;
        private final MediaItem mItem;
        private int mSlotIndex;
        private int mThumbnailType = 2;

        public AlbumCoverLoader(int slotIndex, MediaItem item, boolean isOtherAlbum) {
            this.mSlotIndex = slotIndex;
            this.mItem = item;
            this.mIsOtherAlbum = isOtherAlbum;
            initAlbumCoverParam();
        }

        private void initAlbumCoverParam() {
            if (AlbumSetSlidingWindow.this.mAlbumCoverController != null) {
                this.mThumbnailType = AlbumSetSlidingWindow.this.mAlbumCoverController.getThumbnailType();
                this.mChangeTypeBy = AlbumSetSlidingWindow.this.mAlbumCoverController.changModifyTimeBy();
            }
        }

        protected void recycleBitmap(Bitmap bitmap) {
            GalleryPool.recycle(getPath(), this.mItem.getDateModifiedInSec() + ((long) this.mChangeTypeBy), bitmap, this.mItem.isDrm());
        }

        protected Future<Bitmap> submitBitmapTask(FutureListener<Bitmap> l) {
            return AlbumSetSlidingWindow.this.mThreadPool.submit(this.mItem.requestImage(this.mThumbnailType), l);
        }

        protected void onLoadComplete(Bitmap bitmap) {
            AlbumSetSlidingWindow.this.mHandler.obtainMessage(1, this).sendToTarget();
        }

        protected void onPreviewLoad(Bitmap bitmap) {
            onLoadComplete(bitmap);
        }

        protected Path getPath() {
            return this.mItem.getPath();
        }

        protected long getTimeModified() {
            return this.mItem.getDateModifiedInSec() + ((long) this.mChangeTypeBy);
        }

        public void changeSlotIndex(int newSlotIndex) {
            this.mSlotIndex = newSlotIndex;
        }

        public void updateEntry() {
            boolean z = false;
            AlbumSetEntry entry = AlbumSetSlidingWindow.this.mData[this.mSlotIndex % AlbumSetSlidingWindow.this.mData.length];
            if (entry != null) {
                Bitmap bitmap = getBitmap();
                if (bitmap == null) {
                    z = true;
                }
                entry.isNoThumb = z;
                if (bitmap == null) {
                    bitmap = AlbumSetSlidingWindow.this.dealBitmapIsNull(entry, isStateError(), this.mIsOtherAlbum);
                } else if (bitmap.isRecycled()) {
                    return;
                }
                if (bitmap != null) {
                    entry.bitmapContainer.put(this.mItem.getPath(), bitmap);
                }
                if (AlbumSetSlidingWindow.this.isActiveSlot(this.mSlotIndex)) {
                    AlbumSetSlidingWindow albumSetSlidingWindow = AlbumSetSlidingWindow.this;
                    albumSetSlidingWindow.mActiveRequestCount = albumSetSlidingWindow.mActiveRequestCount - 1;
                    if (AlbumSetSlidingWindow.this.mActiveRequestCount == 0) {
                        AlbumSetSlidingWindow.this.requestNonactiveImages();
                    }
                    if (AlbumSetSlidingWindow.this.mListener != null) {
                        AlbumSetSlidingWindow.this.mListener.onContentChanged(this.mSlotIndex);
                    }
                }
            }
        }
    }

    public static class AlbumSetEntry {
        public HashMap<Path, Bitmap> bitmapContainer = new HashMap();
        public ArrayList<AlbumCoverLoader> contentLoaderList = new ArrayList();
        public boolean isDragging;
        public boolean isNoThumb;
    }

    public AlbumSetSlidingWindow(Activity activity, AlbumSetDataLoader source, int cacheSize) {
        source.setModelListener(this);
        this.mContext = activity;
        this.mSource = source;
        this.mData = new AlbumSetEntry[cacheSize];
        this.mSize = source.size();
        this.mThreadPool = new JobLimiter(((GalleryApp) activity.getApplication()).getThreadPool(), 2);
        this.mHandler = new Handler(this);
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public void setAlbmCoverController(AlbumCoverController controller) {
        this.mAlbumCoverController = controller;
    }

    public AlbumSetEntry get(int slotIndex) {
        if (this.mIsActive) {
            return this.mData[slotIndex % this.mData.length];
        }
        return null;
    }

    public void setActiveWindow(int start, int end) {
        if (start != this.mActiveStart || end != this.mActiveEnd) {
            this.mActiveStart = start;
            this.mActiveEnd = end;
            AlbumSetEntry[] data = this.mData;
            int contentStart = Utils.clamp(((start + end) / 2) - (data.length / 2), 0, Math.max(0, this.mSize - data.length));
            int contentEnd = Math.min(data.length + contentStart, this.mSize);
            this.mSource.setUIRange(this.mActiveStart, this.mActiveEnd);
            setContentWindow(contentStart, contentEnd);
            if (this.mIsActive) {
                updateAllImageRequests();
            }
        }
    }

    public boolean isActiveSlot(int slotIndex) {
        return slotIndex >= this.mActiveStart && slotIndex < this.mActiveEnd;
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

    private void requestNonactiveImages() {
        int range = Math.max(this.mContentEnd - this.mActiveEnd, this.mActiveStart - this.mContentStart);
        for (int i = 0; i < range; i++) {
            requestImagesInSlot(this.mActiveEnd + i);
            requestImagesInSlot((this.mActiveStart - 1) - i);
        }
    }

    private void cancelNonactiveImages() {
        int range = Math.max(this.mContentEnd - this.mActiveEnd, this.mActiveStart - this.mContentStart);
        for (int i = 0; i < range; i++) {
            cancelImagesInSlot(this.mActiveEnd + i);
            cancelImagesInSlot((this.mActiveStart - 1) - i);
        }
    }

    private void requestImagesInSlot(int slotIndex) {
        if (slotIndex >= this.mContentStart && slotIndex < this.mContentEnd) {
            AlbumSetEntry entry = this.mData[slotIndex % this.mData.length];
            if (entry != null) {
                for (AlbumCoverLoader loader : entry.contentLoaderList) {
                    if (loader != null) {
                        loader.startLoad();
                    }
                }
            }
        }
    }

    private void cancelImagesInSlot(int slotIndex) {
        if (slotIndex >= this.mContentStart && slotIndex < this.mContentEnd) {
            AlbumSetEntry entry = this.mData[slotIndex % this.mData.length];
            if (entry != null) {
                for (AlbumCoverLoader loader : entry.contentLoaderList) {
                    if (loader != null) {
                        loader.cancelLoad();
                    }
                }
            }
        }
    }

    private void freeSlotContent(int index) {
        AlbumSetEntry[] data = this.mData;
        int i = index % data.length;
        AlbumSetEntry entry = data[i];
        if (entry != null && !entry.isDragging) {
            for (AlbumCoverLoader loader : entry.contentLoaderList) {
                if (loader != null) {
                    loader.recycle();
                }
            }
            entry.contentLoaderList.clear();
            entry.bitmapContainer.clear();
            data[i] = null;
        }
    }

    private void updateAlbumSetEntry(AlbumSetEntry entry, int slotIndex) {
        MediaSet mediaSet = this.mSource.getMediaSet(slotIndex);
        MediaItem[] coverItems = this.mSource.getCoverItem(slotIndex);
        int totalCount = this.mSource.getTotalCount(slotIndex);
        if (mediaSet != null && entry != null && coverItems != null && totalCount != 0) {
            boolean equalsIgnoreCase;
            for (AlbumCoverLoader loader : entry.contentLoaderList) {
                if (loader != null) {
                    loader.recycle();
                }
            }
            entry.bitmapContainer.clear();
            entry.contentLoaderList.clear();
            if (mediaSet.isVirtual()) {
                equalsIgnoreCase = "other".equalsIgnoreCase(mediaSet.getLabel());
            } else {
                equalsIgnoreCase = false;
            }
            for (int i = Math.min(coverItems.length, 4) - 1; i >= 0; i--) {
                if (coverItems[i] != null) {
                    entry.contentLoaderList.add(new AlbumCoverLoader(slotIndex, coverItems[i], equalsIgnoreCase));
                }
            }
        }
    }

    private void prepareSlotContent(int slotIndex) {
        AlbumSetEntry entry = new AlbumSetEntry();
        updateAlbumSetEntry(entry, slotIndex);
        this.mData[slotIndex % this.mData.length] = entry;
    }

    private static boolean startLoadBitmap(BitmapLoader loader) {
        if (loader == null) {
            return false;
        }
        loader.startLoad();
        return loader.isRequestInProgress();
    }

    private void updateAllImageRequests() {
        this.mActiveRequestCount = 0;
        int n = this.mActiveEnd;
        for (int i = this.mActiveStart; i < n; i++) {
            AlbumSetEntry entry = this.mData[i % this.mData.length];
            if (entry != null) {
                for (AlbumCoverLoader loader : entry.contentLoaderList) {
                    if (startLoadBitmap(loader)) {
                        this.mActiveRequestCount++;
                    }
                }
            }
        }
        if (this.mActiveRequestCount == 0) {
            requestNonactiveImages();
        } else {
            cancelNonactiveImages();
        }
    }

    public void onSizeChanged(int size) {
        if (this.mIsActive && this.mSize != size) {
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

    public void onDragStart(int position) {
        this.mDragEntry = this.mData[position % this.mData.length];
        if (this.mDragEntry != null) {
            this.mDragEntry.isDragging = true;
        }
    }

    public void onDragEnd() {
        this.mDragEntry = null;
    }

    public boolean onExchange(int fromPosition, int toPosition) {
        if (fromPosition == toPosition || this.mDragEntry == null || toPosition < this.mContentStart || toPosition >= this.mContentEnd) {
            return false;
        }
        int relativeStart = (fromPosition < this.mContentStart || fromPosition >= this.mContentEnd) ? fromPosition < toPosition ? 0 : this.mData.length - 1 : fromPosition;
        if (fromPosition < toPosition) {
            swapFrontToBack(this.mData, relativeStart, toPosition);
        } else {
            swapBackToFront(this.mData, relativeStart, toPosition);
        }
        this.mData[toPosition % this.mData.length] = this.mDragEntry;
        for (AlbumCoverLoader loader : this.mDragEntry.contentLoaderList) {
            if (loader != null) {
                loader.changeSlotIndex(toPosition);
            }
        }
        return true;
    }

    private void swapFrontToBack(AlbumSetEntry[] source, int from, int to) {
        int relativeTo = to % source.length;
        int relativeFrom = from % source.length;
        AlbumSetEntry[] clone = (AlbumSetEntry[]) source.clone();
        for (int index = relativeFrom; index < relativeTo; index++) {
            if (index == 0) {
                freeSlotContent(index);
            }
            source[index] = clone[index + 1];
            AlbumSetEntry entry = source[index];
            if (entry != null) {
                for (AlbumCoverLoader loader : entry.contentLoaderList) {
                    if (loader != null) {
                        loader.changeSlotIndex((from + index) - relativeFrom);
                    }
                }
            }
        }
    }

    private void swapBackToFront(AlbumSetEntry[] source, int from, int to) {
        int relativeTo = to % source.length;
        int relativeFrom = from % source.length;
        AlbumSetEntry[] clone = (AlbumSetEntry[]) source.clone();
        for (int index = relativeTo + 1; index <= relativeFrom; index++) {
            if (index == source.length - 1) {
                freeSlotContent(index);
            }
            source[index] = clone[index - 1];
            AlbumSetEntry entry = source[index];
            if (entry != null) {
                for (AlbumCoverLoader loader : entry.contentLoaderList) {
                    if (loader != null) {
                        loader.changeSlotIndex((to + index) - relativeTo);
                    }
                }
            }
        }
    }

    public void onContentChanged(int index) {
        if (!this.mIsActive) {
            return;
        }
        if (index < this.mContentStart || index >= this.mContentEnd) {
            GalleryLog.w("AlbumSetSlidingWindow", String.format("invalid update: %s is outside (%s, %s)", new Object[]{Integer.valueOf(index), Integer.valueOf(this.mContentStart), Integer.valueOf(this.mContentEnd)}));
            return;
        }
        updateAlbumSetEntry(this.mData[index % this.mData.length], index);
        updateAllImageRequests();
        if (this.mListener != null && isActiveSlot(index)) {
            this.mListener.onContentChanged(index);
        }
    }

    protected Bitmap dealBitmapIsNull(AlbumSetEntry entry, boolean isStateError, boolean isOtherAlbum) {
        return BitmapFactory.decodeResource(this.mContext.getResources(), isOtherAlbum ? R.drawable.ic_list_album_damage : R.drawable.ic_list_album_damage2);
    }

    public void resume() {
        this.mIsActive = true;
        int n = this.mContentEnd;
        for (int i = this.mContentStart; i < n; i++) {
            prepareSlotContent(i);
        }
        updateAllImageRequests();
    }

    public void pause() {
        this.mIsActive = false;
        int n = this.mContentEnd;
        for (int i = this.mContentStart; i < n; i++) {
            freeSlotContent(i);
        }
    }

    public boolean handleMessage(Message msg) {
        boolean z = false;
        switch (msg.what) {
            case 1:
                if (msg.what == 1) {
                    z = true;
                }
                Utils.assertTrue(z);
                ((AlbumCoverLoader) msg.obj).updateEntry();
                return true;
            default:
                return false;
        }
    }
}

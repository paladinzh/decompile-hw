package com.huawei.gallery.app;

import android.app.Activity;
import android.content.Intent;
import com.android.gallery3d.app.AlbumSetDataBackup;
import com.android.gallery3d.app.AlbumSetDataBackup.BackUpKey;
import com.android.gallery3d.data.GalleryRecycleAlbum;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.util.GalleryLog;

public class AlbumSetDataLoader extends CommonAlbumSetDataLoader {
    private final String mAction;
    private DataListener mDataListener;
    private DataEntry mDragEntry;

    public static class DataEntry {
        public MediaItem[] coverItem;
        public MediaSet mediaSet;
        public int totalCount;
        public int totalVideoCount;
    }

    public interface DataListener extends com.huawei.gallery.app.CommonAlbumSetDataLoader.DataListener {
        void onDragEnd();

        void onDragStart(int i);

        boolean onExchange(int i, int i2);
    }

    public AlbumSetDataLoader(Activity activity, MediaSet albumSet, int cacheSize) {
        String str = null;
        super(albumSet, cacheSize);
        Intent intent = activity.getIntent();
        if (intent != null) {
            str = intent.getAction();
        }
        this.mAction = str;
        BackUpKey backUpKey = new BackUpKey(this.mSource.getPath().toString(), this.mAction);
        GalleryLog.d("AlbumSetDataAdapter", "DFX path:" + this.mSource.getPath().toString() + " ;mAction:" + this.mAction);
        AlbumSetDataBackup.getInstance().comeback(backUpKey, this.mData, this.mCoverItem, this.mTotalCount, this.mTotalVideoCount, this.mItemVersion, this.mSetVersion);
    }

    public void setModelListener(DataListener listener) {
        super.setModelListener(listener);
        this.mDataListener = listener;
    }

    public void backupData() {
        if (this.mContentStart == 0) {
            AlbumSetDataBackup.getInstance().backup(new BackUpKey(this.mSource.getPath().toString(), this.mAction), this.mData, this.mCoverItem, this.mTotalCount, this.mTotalVideoCount, this.mItemVersion, this.mSetVersion);
            return;
        }
        AlbumSetDataBackup.getInstance().clear();
    }

    public boolean hasAnyItem() {
        int i = this.mContentStart;
        int end = this.mContentEnd;
        while (i < end) {
            if (!(getMediaSet(i) instanceof GalleryRecycleAlbum) && getTotalCount(i) > 0) {
                return true;
            }
            i++;
        }
        int size = size();
        for (int index = 0; index < size; index++) {
            MediaSet mediaSet = this.mSource.getSubMediaSet(index);
            if (!(mediaSet instanceof GalleryRecycleAlbum) && mediaSet != null && mediaSet.getMediaItemCount() > 0) {
                return true;
            }
        }
        return false;
    }

    public void onDragStart(int position) {
        this.mDragEntry = new DataEntry();
        int relativePosition = position % this.mData.length;
        this.mDragEntry.mediaSet = this.mData[relativePosition];
        this.mDragEntry.coverItem = this.mCoverItem[relativePosition];
        this.mDragEntry.totalCount = this.mTotalCount[relativePosition];
        this.mDragEntry.totalVideoCount = this.mTotalVideoCount[relativePosition];
        if (this.mDataListener != null) {
            this.mDataListener.onDragStart(position);
        }
    }

    public void onDragEnd() {
        this.mDragEntry = null;
        if (this.mDataListener != null) {
            this.mDataListener.onDragEnd();
        }
    }

    public MediaSet getDraggedSet() {
        if (this.mDragEntry == null) {
            return null;
        }
        return this.mDragEntry.mediaSet;
    }

    public boolean exchangeMediaSet(int fromPosition, int toPosition) {
        if (fromPosition == toPosition || this.mDragEntry == null || ((this.mReloadTask != null && this.mReloadTask.isLoading()) || toPosition < this.mContentStart || toPosition >= this.mContentEnd)) {
            return false;
        }
        int relativeStart;
        if (fromPosition >= this.mContentStart && fromPosition < this.mContentEnd) {
            relativeStart = fromPosition;
        } else if (fromPosition < toPosition) {
            relativeStart = 0;
        } else {
            relativeStart = this.mData.length - 1;
        }
        if (fromPosition < toPosition) {
            swapFrontToBack(this.mData, relativeStart, toPosition);
            swapFrontToBack(this.mCoverItem, relativeStart, toPosition);
            swapFrontToBack(this.mTotalCount, relativeStart, toPosition);
            swapFrontToBack(this.mTotalVideoCount, relativeStart, toPosition);
        } else {
            swapBackToFront(this.mData, relativeStart, toPosition);
            swapBackToFront(this.mCoverItem, relativeStart, toPosition);
            swapBackToFront(this.mTotalCount, relativeStart, toPosition);
            swapBackToFront(this.mTotalVideoCount, relativeStart, toPosition);
        }
        int index = toPosition % this.mData.length;
        this.mData[index] = this.mDragEntry.mediaSet;
        this.mCoverItem[index] = this.mDragEntry.coverItem;
        this.mTotalCount[index] = this.mDragEntry.totalCount;
        this.mTotalVideoCount[index] = this.mDragEntry.totalVideoCount;
        if (this.mDataListener != null) {
            return this.mDataListener.onExchange(fromPosition, toPosition);
        }
        return true;
    }

    private <T> void swapFrontToBack(T[] source, int from, int to) {
        from %= source.length;
        System.arraycopy((Object[]) source.clone(), from + 1, source, from, (to % source.length) - from);
    }

    private void swapFrontToBack(int[] source, int from, int to) {
        from %= source.length;
        System.arraycopy((int[]) source.clone(), from + 1, source, from, (to % source.length) - from);
    }

    private <T> void swapBackToFront(T[] source, int from, int to) {
        to %= source.length;
        System.arraycopy((Object[]) source.clone(), to, source, to + 1, (from % source.length) - to);
    }

    private void swapBackToFront(int[] source, int from, int to) {
        to %= source.length;
        System.arraycopy((int[]) source.clone(), to, source, to + 1, (from % source.length) - to);
    }
}

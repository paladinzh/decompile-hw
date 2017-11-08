package com.android.gallery3d.data;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.os.Looper;
import com.android.gallery3d.util.BusinessRadar;
import com.android.gallery3d.util.BusinessRadar.BugType;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.TraceController;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.WeakHashMap;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public abstract class MediaSet extends MediaObject {
    private static final Future<Integer> FUTURE_STUB = new Future<Integer>() {
        public void cancel() {
        }

        public boolean isCancelled() {
            return false;
        }

        public boolean isDone() {
            return true;
        }

        public Integer get() {
            return Integer.valueOf(0);
        }

        public void waitDone() {
        }
    };
    protected String mBucketPath = "";
    protected boolean mIsHidden;
    private WeakHashMap<ContentListener, Object> mListeners = new WeakHashMap();

    public static abstract class ItemConsumer {
        public abstract void consume(int i, MediaItem mediaItem);

        public boolean dynamic() {
            return false;
        }
    }

    public abstract String getName();

    public abstract long reload();

    public MediaSet(Path path, long version) {
        super(path, version);
    }

    public int getMediaItemCount() {
        return 0;
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        return new ArrayList();
    }

    public void setIdCache(ArrayList<Integer> arrayList, int mediaItemsCount) {
    }

    public ArrayList<MediaItem> getMediaItemFromCache(ArrayList<Integer> arrayList, int start, int count) {
        return new ArrayList();
    }

    public boolean isIdCacheReady(ArrayList<Integer> arrayList, int start, int count) {
        return false;
    }

    public boolean resetIdCache(ArrayList<Integer> arrayList) {
        return false;
    }

    public boolean supportCacheQuery() {
        return false;
    }

    public MediaItem getCoverMediaItem() {
        ArrayList<MediaItem> items = getMediaItem(0, 1);
        if (items.size() > 0) {
            return (MediaItem) items.get(0);
        }
        int n = getSubMediaSetCount();
        for (int i = 0; i < n; i++) {
            MediaSet subSet = getSubMediaSet(i);
            if (subSet != null) {
                MediaItem cover = subSet.getCoverMediaItem();
                if (cover != null) {
                    return cover;
                }
            }
        }
        return null;
    }

    public MediaItem[] getMultiCoverMediaItem() {
        if (getCoverMediaItem() == null) {
            return null;
        }
        return new MediaItem[]{getCoverMediaItem()};
    }

    public int getSubMediaSetCount() {
        return 0;
    }

    public MediaSet getSubMediaSet(int index) {
        throw new IndexOutOfBoundsException();
    }

    public boolean isLeafAlbum() {
        return false;
    }

    public boolean isLoading() {
        return false;
    }

    public int getTotalMediaItemCount() {
        int total = getMediaItemCount();
        if (isLeafAlbum()) {
            return total;
        }
        synchronized (this) {
            int n = getSubMediaSetCount();
            for (int i = 0; i < n; i++) {
                MediaSet subSet = getSubMediaSet(i);
                if (subSet != null) {
                    if (subSet instanceof VirtualOtherAlbum) {
                        return 1;
                    }
                    total += subSet.getTotalMediaItemCount();
                }
            }
            return total;
        }
    }

    public int getIndexOfItem(Path path, int hint) {
        int start = Math.max(0, getHintIndex(path, hint) - 250);
        TraceController.traceBegin("MediaSet.getIndexOfItem.getMediaItem");
        ArrayList<MediaItem> list = getMediaItem(start, 500);
        TraceController.traceEnd();
        TraceController.traceBegin("MediaSet.getIndexOfItem.getIndexOf");
        int index = getIndexOf(path, list);
        TraceController.traceEnd();
        if (index != -1) {
            return start + index;
        }
        start = start == 0 ? 500 : 0;
        TraceController.traceBegin("MediaSet.getIndexOfItem.getMediaItem 2nd");
        list = getMediaItem(start, 500);
        TraceController.traceEnd();
        while (true) {
            TraceController.traceBegin("MediaSet.getIndexOf.getIndexOfItem in while");
            index = getIndexOf(path, list);
            TraceController.traceEnd();
            if (index != -1) {
                return start + index;
            }
            if (list.size() < 500) {
                return -1;
            }
            start += 500;
            TraceController.traceBegin("MediaSet.getIndexOfItem.getMediaItem in while");
            list = getMediaItem(start, 500);
            TraceController.traceEnd();
        }
    }

    protected int getIndexOf(Path path, ArrayList<MediaItem> list) {
        int n = list.size();
        for (int i = 0; i < n; i++) {
            MediaObject item = (MediaObject) list.get(i);
            if (item != null && item.mPath == path) {
                return i;
            }
        }
        return -1;
    }

    public String getSubName() {
        return "";
    }

    public String getDefaultAlbumName() {
        return getName();
    }

    public void addContentListener(ContentListener listener) {
        synchronized (this.mListeners) {
            if (this.mListeners.containsKey(listener)) {
                throw new IllegalArgumentException();
            }
            this.mListeners.put(listener, null);
        }
    }

    public void removeContentListener(ContentListener listener) {
        synchronized (this.mListeners) {
            if (this.mListeners.containsKey(listener)) {
                this.mListeners.remove(listener);
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    public void notifyContentChanged() {
        HashSet<ContentListener> tmpListenerKeySet = new HashSet();
        synchronized (this.mListeners) {
            tmpListenerKeySet.addAll(this.mListeners.keySet());
        }
        for (ContentListener listener : tmpListenerKeySet) {
            listener.onContentDirty();
        }
    }

    public void updateDataVersion() {
        this.mDataVersion = MediaObject.nextVersionNumber();
    }

    public MediaDetails getDetails() {
        MediaDetails details = super.getDetails();
        details.addDetail(1, getName());
        int subMediaItemCount = getMediaItemCount();
        details.addDetail(150, Integer.valueOf(subMediaItemCount));
        long sum = 0;
        for (MediaItem item : getMediaItem(0, subMediaItemCount)) {
            sum += item.getSize();
        }
        if (!"other".equalsIgnoreCase(getLabel())) {
            details.addDetail(5, Long.valueOf(sum));
        }
        if (!isVirtual() && getAlbumType() == 0) {
            details.addDetail(SmsCheckResult.ESCT_200, getBucketPath());
        }
        return details;
    }

    public void enumerateMediaItems(ItemConsumer consumer) {
        enumerateMediaItems(consumer, 0);
    }

    public void enumerateTotalMediaItems(ItemConsumer consumer) {
        enumerateTotalMediaItems(consumer, 0);
    }

    protected int enumerateMediaItemsWithDynamicSource(ItemConsumer consumer) {
        int total = getMediaItemCount();
        int consumerCount = 0;
        while (consumerCount < total) {
            ArrayList<MediaItem> items = getMediaItem(0, 500);
            consumerCount += items.size();
            if (items.size() == 0) {
                break;
            }
            int n = items.size();
            for (int i = 0; i < n; i++) {
                consumer.consume(i, (MediaItem) items.get(i));
            }
        }
        return total;
    }

    protected int enumerateMediaItems(ItemConsumer consumer, int startIndex) {
        if (consumer.dynamic()) {
            return enumerateMediaItemsWithDynamicSource(consumer);
        }
        int total = getMediaItemCount();
        int start = 0;
        while (start < total) {
            int count = Math.min(500, total - start);
            ArrayList<MediaItem> items = getMediaItem(start, count);
            int n = items.size();
            for (int i = 0; i < n; i++) {
                consumer.consume((startIndex + start) + i, (MediaItem) items.get(i));
            }
            start += count;
        }
        return total;
    }

    protected int enumerateTotalMediaItems(ItemConsumer consumer, int startIndex) {
        int i;
        int m = getSubMediaSetCount();
        MediaSet[] set = new MediaSet[m];
        for (i = 0; i < m; i++) {
            set[i] = getSubMediaSet(i);
        }
        int start = enumerateMediaItems(consumer, startIndex) + 0;
        for (i = 0; i < m; i++) {
            MediaSet subSet = set[i];
            if (subSet != null) {
                start += subSet.enumerateTotalMediaItems(consumer, startIndex + start);
            }
        }
        return start;
    }

    public boolean isHidden() {
        return this.mIsHidden;
    }

    public void setHidden(boolean isHidden) {
        this.mIsHidden = isHidden;
    }

    public String getBucketPath() {
        return this.mBucketPath;
    }

    public void setBucketPath(String bucketPath) {
        this.mBucketPath = bucketPath;
    }

    public boolean isVirtual() {
        return false;
    }

    public String getLabel() {
        return null;
    }

    public boolean isEmptyAlbum() {
        return false;
    }

    public int getNewPictureCount() {
        return 0;
    }

    public int getBucketId() {
        return 0;
    }

    public int getTotalVideoCount() {
        return 0;
    }

    public boolean isSdcardIconNeedShow() {
        return false;
    }

    public void setStartTakenTime(long takenTime) {
    }

    public long getStartTakenTime() {
        return 0;
    }

    public String getWhereQueryClause() {
        throw new UnsupportedOperationException();
    }

    public String[] getWhereQueryClauseArgs(long startTakenTime) {
        throw new UnsupportedOperationException();
    }

    public PhotoShareAlbumInfo getAlbumInfo() {
        throw new UnsupportedOperationException();
    }

    public void setAlbumInfo(PhotoShareAlbumInfo albumInfo) {
        throw new UnsupportedOperationException();
    }

    public void setName(String name) {
        throw new UnsupportedOperationException();
    }

    public void setSubName(String name) {
        throw new UnsupportedOperationException();
    }

    public void setAlbumType(int albumType) {
    }

    public int getAlbumType() {
        return 0;
    }

    public int getPreViewCount() {
        return 0;
    }

    public boolean isQuickMode() {
        return false;
    }

    public void setQuickMode(boolean enable) {
    }

    protected final void printExcuteInfo(long startTime, String msg) {
        long timeCost = System.currentTimeMillis() - startTime;
        boolean inMainThread = Looper.myLooper() == Looper.getMainLooper();
        String msgStr = String.format("[%s]%s cost time: %d", new Object[]{this, msg, Long.valueOf(timeCost)});
        if (inMainThread) {
            msgStr = msgStr + " in mainThread";
        }
        if (timeCost >= 200) {
            GalleryLog.v("MediaSet", msgStr);
            if (inMainThread) {
                BusinessRadar.report(BugType.JUST_PRINT, msgStr, new Exception("There are haeavy task in main thread."));
            }
        } else if (inMainThread) {
            GalleryLog.v("MediaSet", msgStr);
        }
    }

    public ArrayList<String> getVideoFileList() {
        return null;
    }

    public void reset() {
    }

    protected static Uri decorateQueryExternalFileUri(Uri fileUri, String sortParams, int start, int count) {
        Builder uriBuilder = fileUri.buildUpon();
        String order_by = sortParams;
        uriBuilder.appendQueryParameter("limit", start + "," + count);
        if (sortParams.indexOf("datetaken") != -1) {
            uriBuilder.appendQueryParameter("force_index", "sort_index");
        } else if (sortParams.indexOf("title") != -1) {
            uriBuilder.appendQueryParameter("force_index", "title_idx");
        } else if (sortParams.indexOf("showDateToken") != -1) {
            uriBuilder.appendQueryParameter("force_index", "gallery_sort_index");
        }
        return uriBuilder.build();
    }

    protected static Uri decorateQueryExternalFileUri(Uri fileUri) {
        Builder uriBuilder = fileUri.buildUpon();
        uriBuilder.appendQueryParameter("nonotify", "1");
        return uriBuilder.build();
    }

    protected int getHintIndex(Path path, int hint) {
        return hint;
    }

    public void recycle(SQLiteDatabase db, Bundle data) {
        delete();
    }
}

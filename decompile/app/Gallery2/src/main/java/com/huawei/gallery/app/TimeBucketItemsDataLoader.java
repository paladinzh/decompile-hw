package com.huawei.gallery.app;

import android.graphics.RectF;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.SparseArray;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.ITimeLatLng;
import com.android.gallery3d.data.ITimeLatLng.LatitudeLongitude;
import com.android.gallery3d.data.LocalMediaAlbum.LocalGroupData;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.TimeBucketPageViewMode;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ReportToBigData;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.huawei.gallery.data.LocationAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class TimeBucketItemsDataLoader extends MediaItemsDataLoader implements AddressAcquire {
    private static final Object RECT_LOCK = new Object();
    private final HandlerThread mHandlerThread;
    private final LocationAddress mLocationAddress;
    private MediaSetRegion mMediaSetRegion = new MediaSetRegion(new RectF(0.0f, 0.0f, 0.0f, 0.0f), true);
    private TimeBucketPageViewMode mMode = TimeBucketPageViewMode.DAY;
    private final Handler mThreadHandler;

    private class GetTimeBucketUpdateInfo extends GetUpdateInfo {
        public GetTimeBucketUpdateInfo(long version) {
            super(version);
        }

        protected UpdateInfo createUpdateInfo() {
            TimeBucketUpdateInfo timeBucketUpdateInfo = new TimeBucketUpdateInfo();
            timeBucketUpdateInfo.mode = TimeBucketItemsDataLoader.this.mMode;
            return timeBucketUpdateInfo;
        }
    }

    public static class MediaSetRegion {
        public final boolean mNoPictureHasLatLng;
        public final RectF mRectF;

        public MediaSetRegion(RectF rect, boolean noPic) {
            this.mRectF = rect;
            this.mNoPictureHasLatLng = noPic;
        }
    }

    private class MyThreadHandler extends Handler {
        public MyThreadHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    MediaSetRegion mediaSetRegion = TimeBucketItemsDataLoader.this.getMediaSetRegion();
                    synchronized (TimeBucketItemsDataLoader.RECT_LOCK) {
                        TimeBucketItemsDataLoader.this.mMediaSetRegion = mediaSetRegion;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private class TimeBucketUpdateContent extends UpdateContent {
        public TimeBucketUpdateContent(UpdateInfo info) {
            super(info);
        }

        protected void detectChange(UpdateInfo info) {
            if (info instanceof TimeBucketUpdateInfo) {
                TimeBucketUpdateInfo timeBucketUpdateInfo = (TimeBucketUpdateInfo) info;
                if (TimeBucketItemsDataLoader.this.mSize == info.size && TimeBucketItemsDataLoader.this.mMode == timeBucketUpdateInfo.mode) {
                    if (TimeBucketItemsDataLoader.this.groupDatasChange(info.groupDatas)) {
                    }
                    return;
                }
                if (TimeBucketItemsDataLoader.this.mSize != info.size) {
                    TimeBucketItemsDataLoader.this.mThreadHandler.removeMessages(0);
                    TimeBucketItemsDataLoader.this.mThreadHandler.sendEmptyMessage(0);
                    TimeBucketItemsDataLoader.this.mMainHandler.obtainMessage(4, info.size, 0).sendToTarget();
                }
                TimeBucketItemsDataLoader.this.mSize = info.size;
                TimeBucketItemsDataLoader.this.mMode = timeBucketUpdateInfo.mode;
                synchronized (TimeBucketItemsDataLoader.GROUPCOUNT_LOCK) {
                    TimeBucketItemsDataLoader.this.mGroupDatas = info.groupDatas;
                    if (TimeBucketItemsDataLoader.this.mGroupDatas == null) {
                        TimeBucketItemsDataLoader.this.mGroupDatas = new ArrayList();
                    }
                }
                if (TimeBucketItemsDataLoader.this.mDataListener != null) {
                    TimeBucketItemsDataLoader.this.mDataListener.onSizeChanged(TimeBucketItemsDataLoader.this.mSize, TimeBucketItemsDataLoader.this.getGroupDatas(), TimeBucketItemsDataLoader.this.mMode);
                }
                if (TimeBucketItemsDataLoader.this.mContentEnd > TimeBucketItemsDataLoader.this.mSize) {
                    TimeBucketItemsDataLoader.this.mContentEnd = TimeBucketItemsDataLoader.this.mSize;
                }
                if (TimeBucketItemsDataLoader.this.mActiveEnd > TimeBucketItemsDataLoader.this.mSize) {
                    TimeBucketItemsDataLoader.this.mActiveEnd = TimeBucketItemsDataLoader.this.mSize;
                }
                return;
            }
            GalleryLog.w("TimeBucketItemsDataLoader", "detectChange invalid update info:" + info);
        }
    }

    private static class TimeBucketUpdateInfo extends UpdateInfo {
        public TimeBucketPageViewMode mode;

        private TimeBucketUpdateInfo() {
        }
    }

    public TimeBucketItemsDataLoader(GalleryContext context, MediaSet mediaSet) {
        super(context, mediaSet);
        this.mLocationAddress = new LocationAddress(context.getAndroidContext());
        this.mHandlerThread = new HandlerThread("Thread for update the address rect");
        this.mHandlerThread.start();
        this.mThreadHandler = new MyThreadHandler(this.mHandlerThread.getLooper());
    }

    public MediaSetRegion getRegion() {
        MediaSetRegion region;
        synchronized (RECT_LOCK) {
            region = this.mMediaSetRegion;
        }
        return region;
    }

    public void pause() {
        super.pause();
        this.mThreadHandler.removeCallbacksAndMessages(null);
    }

    public void destroy() {
        this.mHandlerThread.quit();
    }

    public TimeBucketPageViewMode getMode() {
        return this.mMode;
    }

    private List<LatitudeLongitude> getLatLng(int groupIndex) {
        LocalGroupData localGroupData = null;
        synchronized (GROUPCOUNT_LOCK) {
            if (groupIndex >= 0) {
                if (groupIndex < this.mGroupDatas.size()) {
                    localGroupData = (LocalGroupData) this.mGroupDatas.get(groupIndex);
                }
            }
        }
        if (localGroupData == null) {
            return null;
        }
        return this.mSource.getLatLongByDatetaken(localGroupData.startDatetaken, localGroupData.endDatetaken);
    }

    public String getAddressString(int index, boolean isWeekView, boolean fromCache, JobContext jc) {
        List<LatitudeLongitude> latLnges = getLatLng(index);
        if (latLnges == null || latLnges.size() == 0 || jc.isCancelled()) {
            return null;
        }
        SparseArray<Set<String>> cache = new SparseArray(6);
        Iterable segSet = null;
        for (LatitudeLongitude latLng : latLnges) {
            if (jc.isCancelled()) {
                return null;
            }
            segSet = getSegSet(6, cache, segSet, this.mLocationAddress.getAddress(latLng.latitude, latLng.longitude, fromCache).loccationDetails);
        }
        if (jc.isCancelled()) {
            return "HAS_LOCATION_ITEM";
        }
        int idx = 0;
        int usedIndex = 0;
        while (idx < 6) {
            segSet = (Set) cache.get(idx);
            if (segSet.size() > 1) {
                GalleryLog.d("TimeBucketItemsDataLoader", index + " found location " + idx);
                usedIndex = idx;
                break;
            }
            idx++;
        }
        if (segSet.isEmpty()) {
            GalleryLog.d("TimeBucketItemsDataLoader", index + " not found location, should be back lookup");
            while (idx > 0) {
                idx--;
                usedIndex = idx;
                segSet = (Set) cache.get(idx);
                if (!segSet.isEmpty()) {
                    GalleryLog.d("TimeBucketItemsDataLoader", index + " found location when back:" + idx);
                    break;
                }
            }
        }
        if (segSet.isEmpty()) {
            GalleryLog.d("TimeBucketItemsDataLoader", index + " not found location, there is no location");
            return "HAS_LOCATION_ITEM";
        }
        StringBuilder stringBuilder = getStringBuilder(6, cache, usedIndex, new StringBuilder());
        String format = this.mContext.getResources().getString(R.string.one_area);
        Iterator segName$iterator = segSet.iterator();
        while (segName$iterator.hasNext()) {
            StringBuilder stringBuilder2 = stringBuilder;
            stringBuilder2.append(String.format(format, new Object[]{(String) segName$iterator.next()}));
        }
        GalleryLog.v("TimeBucketItemsDataLoader", "invalid res: 2131297175213129717621312971772131297178");
        String wholeAddr = stringBuilder.toString();
        return wholeAddr.substring(0, wholeAddr.length() - 1);
    }

    private Set<String> getSegSet(int segCount, SparseArray<Set<String>> cache, Set<String> segSet, String[] location) {
        for (int idx = 0; idx < segCount; idx++) {
            segSet = (Set) cache.get(idx);
            if (segSet == null) {
                segSet = new HashSet(10);
                cache.put(idx, segSet);
            }
            if (!TextUtils.isEmpty(location[idx])) {
                segSet.add(location[idx]);
            }
        }
        return segSet;
    }

    private StringBuilder getStringBuilder(int segCount, SparseArray<Set<String>> cache, int usedIndex, StringBuilder stringBuilder) {
        if (usedIndex != segCount - 1) {
            return stringBuilder;
        }
        Iterator hd$iterator = ((Set) cache.get(usedIndex - 1)).iterator();
        if (hd$iterator.hasNext()) {
            return new StringBuilder((String) hd$iterator.next());
        }
        return stringBuilder;
    }

    public RectF getAddressRect(int index) {
        LocalGroupData localGroupData = null;
        synchronized (GROUPCOUNT_LOCK) {
            if (index >= 0) {
                if (index < this.mGroupDatas.size()) {
                    localGroupData = (LocalGroupData) this.mGroupDatas.get(index);
                }
            }
        }
        if (localGroupData == null) {
            return null;
        }
        return this.mSource.getRectByDatetaken(localGroupData.startDatetaken, localGroupData.endDatetaken);
    }

    private MediaSetRegion getMediaSetRegion() {
        RectF rect = this.mSource.getRectByDatetaken(Long.MIN_VALUE, Long.MAX_VALUE);
        boolean noPic = false;
        if (rect == null) {
            rect = getCurrentLatLng();
            noPic = true;
        }
        return new MediaSetRegion(rect, noPic);
    }

    private RectF getCurrentLatLng() {
        return new RectF(0.0f, 0.0f, 0.0f, 0.0f);
    }

    public boolean updateMode(boolean beBigger) {
        boolean changed = this.mSource.updateMode(beBigger);
        if (this.mReloadTask != null && changed) {
            this.mReloadTask.notifyDirty();
        }
        if (beBigger) {
            ReportToBigData.report(179, String.format("{PhototabMode:%s}", new Object[]{"DAY"}));
        } else {
            ReportToBigData.report(179, String.format("{PhototabMode:%s}", new Object[]{"MONTH"}));
        }
        return changed;
    }

    protected GetUpdateInfo createGetUpdateInfo(long version) {
        return new GetTimeBucketUpdateInfo(version);
    }

    protected void decorateUpdateInfo(UpdateInfo info) {
        if (info instanceof TimeBucketUpdateInfo) {
            ((TimeBucketUpdateInfo) info).mode = ((ITimeLatLng) this.mSource).getMode();
            return;
        }
        GalleryLog.w("TimeBucketItemsDataLoader", "detectChange invalid update info:" + info);
    }

    protected UpdateContent createUpdateContent(UpdateInfo info) {
        return new TimeBucketUpdateContent(info);
    }
}

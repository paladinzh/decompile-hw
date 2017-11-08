package com.huawei.gallery.ui;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Message;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.TimeBucketPageViewMode;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.JobLimiter;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.huawei.gallery.app.TimeBucketItemsDataLoader;
import com.huawei.gallery.data.AbsGroupData;
import com.huawei.gallery.data.AddressRegionLoader;
import com.huawei.gallery.data.AddressRegionLoader.AddressRegionListener;
import com.huawei.gallery.data.AddressStringLoader;
import com.huawei.gallery.data.AddressStringLoader.AddressStringListener;
import com.huawei.gallery.data.GroupAddressRectJob;
import com.huawei.gallery.data.GroupAddressStringJob;
import com.huawei.gallery.ui.TimeAxisLabel.GroupAddressListener;
import com.huawei.gallery.ui.TimeAxisLabel.TitleArgs;
import com.huawei.gallery.ui.TimeAxisLabel.TitleSpec;
import java.util.ArrayList;

public class TimeBucketSlidingWindow extends ListSlotRenderData implements GroupAddressListener, AddressRegionListener, AddressStringListener {
    private final JobLimiter mThreadPoolForGroupAddress;
    private final JobLimiter mThreadPoolForGroupAddressSting;
    private final JobLimiter mThreadPoolForGroupTitle;

    public TimeBucketSlidingWindow(GalleryContext activity, TimeBucketItemsDataLoader source, int cacheSize, TitleSpec titleSpec) {
        super(activity, source, cacheSize, titleSpec);
        this.mThreadPoolForGroupTitle = new JobLimiter(activity.getThreadPool(), 2);
        this.mThreadPoolForGroupAddress = new JobLimiter(activity.getThreadPool(), 1);
        this.mThreadPoolForGroupAddressSting = new JobLimiter(activity.getThreadPool(), 1);
    }

    protected void initEntrySet(int cacheSize) {
        this.mItemData = new ItemEntrySet(this, this, cacheSize, this.mSize);
        this.mTitleData = new TitleEntrySet(this, this, this, this, 16, cacheSize / 2);
        this.mOldTitleData = new TitleEntrySet(this, this, this, this, 16, cacheSize / 2);
    }

    protected void onHandleMessage(Message message) {
        switch (message.what) {
            case 1:
                AddressRegionLoader loader = message.obj;
                this.mTitleData.updateAddressRect(loader.mSlotIndex, loader.get());
                return;
            case 3:
                ((TitleEntrySet) this.mTitleData).updateAddressFlag(message.arg1);
                return;
            case 5:
                AddressStringLoader loader2 = message.obj;
                ((TitleEntrySet) this.mTitleData).updateAddressString(loader2.mSlotIndex, loader2.get());
                return;
            default:
                super.onHandleMessage(message);
                return;
        }
    }

    public void onSizeChanged(int size, ArrayList<AbsGroupData> groupDatas, TimeBucketPageViewMode mode) {
        this.mTitleLabel.setDefaultMode(mode);
        super.onSizeChanged(size, groupDatas, mode);
    }

    public Future<Bitmap> submit(Job<Bitmap> job, FutureListener<Bitmap> listener, int startMode) {
        if (startMode == 3) {
            return this.mThreadPoolForGroupTitle.submit(job, listener, startMode);
        }
        return super.submit(job, listener, startMode);
    }

    public Job<Bitmap> requestTitle(TitleEntrySetListener titleEntrySetListener, TitleArgs titleArgs) {
        return this.mTitleLabel.requestTitle(titleEntrySetListener, titleArgs, this);
    }

    public String getAddressStringFromCache(int index, boolean isWeekView, JobContext jc) {
        if (!(this.mSource instanceof TimeBucketItemsDataLoader)) {
            return null;
        }
        String addressString = ((TimeBucketItemsDataLoader) this.mSource).getAddressString(index, isWeekView, true, jc);
        if (jc.isCancelled()) {
            return null;
        }
        if (!(addressString == null || addressString.equals("HAS_LOCATION_ITEM"))) {
            this.mHandler.obtainMessage(3, index, index).sendToTarget();
        }
        return addressString;
    }

    public void onAddressRegion(Object object) {
        this.mHandler.obtainMessage(1, object).sendToTarget();
    }

    public Future<RectF> submitAddressRectTask(FutureListener<RectF> listener, int index) {
        if (this.mSource instanceof TimeBucketItemsDataLoader) {
            return this.mThreadPoolForGroupAddress.submit(new GroupAddressRectJob((TimeBucketItemsDataLoader) this.mSource, index), listener, 5);
        }
        return null;
    }

    public void onAddressString(Object object) {
        this.mHandler.obtainMessage(5, object).sendToTarget();
    }

    public Future<String> submitAddressStringTask(FutureListener<String> listener, int index) {
        if (!(this.mSource instanceof TimeBucketItemsDataLoader)) {
            return null;
        }
        return this.mThreadPoolForGroupAddressSting.submit(new GroupAddressStringJob((TimeBucketItemsDataLoader) this.mSource, index, this.mTitleLabel.mCurrentSpec == this.mTitleLabel.mDaySpec), listener, 2);
    }

    public boolean scale(boolean beBigger) {
        if (this.mSource instanceof TimeBucketItemsDataLoader) {
            return ((TimeBucketItemsDataLoader) this.mSource).updateMode(beBigger);
        }
        return false;
    }
}

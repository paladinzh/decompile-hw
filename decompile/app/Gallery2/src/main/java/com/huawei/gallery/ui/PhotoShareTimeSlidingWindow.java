package com.huawei.gallery.ui;

import android.content.Context;
import android.graphics.Bitmap;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.util.ThreadPool.Job;
import com.huawei.gallery.app.MediaItemsDataLoader;
import com.huawei.gallery.ui.TimeAxisLabel.TitleArgs;
import com.huawei.gallery.ui.TimeAxisLabel.TitleSpec;

public class PhotoShareTimeSlidingWindow extends ListSlotRenderData {
    public PhotoShareTimeSlidingWindow(GalleryContext activity, MediaItemsDataLoader source, int cacheSize, TitleSpec titleSpec) {
        super(activity, source, cacheSize, titleSpec);
    }

    public TimeAxisLabel createTimeAxisLabel(Context context, TitleSpec titleSpec) {
        return new PhotoShareTimeAxisLabel(context, titleSpec);
    }

    protected void initEntrySet(int cacheSize) {
        this.mItemData = new ItemEntrySet(this, this, cacheSize, this.mSize);
        this.mTitleData = new TitleEntrySet(this, this, null, null, 16, cacheSize / 2);
        this.mOldTitleData = new TitleEntrySet(this, this, null, null, 16, cacheSize / 2);
    }

    public Job<Bitmap> requestTitle(TitleEntrySetListener titleEntrySetListener, TitleArgs titleArgs) {
        return this.mTitleLabel.requestTitle(titleEntrySetListener, titleArgs, null);
    }
}

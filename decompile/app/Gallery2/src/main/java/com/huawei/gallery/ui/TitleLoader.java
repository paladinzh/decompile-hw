package com.huawei.gallery.ui;

import android.graphics.Bitmap;
import com.android.gallery3d.data.LocalMediaAlbum.LocalGroupData;
import com.android.gallery3d.util.ThreadPool.Job;
import com.huawei.gallery.ui.TimeAxisLabel.TitleArgs;
import com.huawei.gallery.util.GalleryPool;

public class TitleLoader extends ThumbnailLoader {
    private final TitleLoaderListener mListener;
    private final int mModeValue;
    private final TitleArgs mTitleArgs;
    private final TitleEntrySetListener mTitleEntrySetListener;

    public TitleLoader(TitleEntrySetListener titleEntrySetListener, TitleLoaderListener titleLoaderListener, TitleArgs titleArgs) {
        super(titleLoaderListener, titleArgs.index);
        this.mTitleEntrySetListener = titleEntrySetListener;
        this.mListener = titleLoaderListener;
        this.mModeValue = titleLoaderListener.getCurrentTitleModeValue();
        this.mTitleArgs = titleArgs;
    }

    protected void recycleBitmap(Bitmap bitmap) {
        GalleryPool.recycle(this.mTitleArgs.groupData.defaultTitle, this.mModeValue, bitmap, this.mListener);
    }

    protected Job<Bitmap> requestJob() {
        return this.mListener.requestTitle(this.mTitleEntrySetListener, this.mTitleArgs);
    }

    protected int getThreadMode() {
        return 3;
    }

    public synchronized void startLoad() {
        if (this.mTitleArgs.groupData instanceof LocalGroupData) {
            Bitmap bmp = GalleryPool.get(this.mTitleArgs.groupData.defaultTitle, this.mModeValue);
            if (bmp != null) {
                onPreviewLoad(bmp);
            }
        }
        super.startLoad();
    }
}

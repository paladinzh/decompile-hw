package com.huawei.gallery.ui;

import android.graphics.Bitmap;
import com.android.gallery3d.util.ThreadPool.Job;
import com.huawei.gallery.ui.TimeAxisLabel.TitleArgs;

public interface TitleLoaderListener extends ThumbnailLoaderListener {
    int getCurrentTitleModeValue();

    void recycleTitle(Bitmap bitmap);

    Job<Bitmap> requestTitle(TitleEntrySetListener titleEntrySetListener, TitleArgs titleArgs);
}

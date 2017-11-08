package com.huawei.gallery.ui;

import com.android.gallery3d.ui.BitmapTexture;

public interface EntrySetListener {
    void addBgTexture(BitmapTexture bitmapTexture);

    void addFgTexture(BitmapTexture bitmapTexture);

    void invalidate();
}

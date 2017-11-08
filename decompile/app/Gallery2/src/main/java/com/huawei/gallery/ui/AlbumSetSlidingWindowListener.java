package com.huawei.gallery.ui;

import com.android.gallery3d.common.Utils;
import com.huawei.gallery.ui.AlbumSetSlidingWindow.Listener;

public class AlbumSetSlidingWindowListener implements Listener {
    private DataAdapterProxy mProxy;

    public AlbumSetSlidingWindowListener(DataAdapterProxy proxy) {
        Utils.assertTrue(proxy != null);
        this.mProxy = proxy;
    }

    public void onSizeChanged(int size) {
        this.mProxy.notifyDataSetChanged();
    }

    public void onContentChanged(int index) {
        this.mProxy.updateView(index);
    }
}

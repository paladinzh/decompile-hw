package com.huawei.gallery.recycle.app;

import com.huawei.gallery.app.GLFragment;

public class RecycleAlbumHost extends GLFragment {
    protected void onInflateFinished() {
        getStateManager().startState(RecycleAlbumPage.class, getArguments());
    }
}

package com.huawei.gallery.map.app;

import com.huawei.gallery.app.GLFragment;

public class MapSlotAlbumHost extends GLFragment {
    protected void onInflateFinished() {
        getStateManager().startState(MapAlbumPage.class, getArguments());
    }
}

package com.huawei.gallery.app;

public class SlotAlbumHost extends GLFragment {
    protected void onInflateFinished() {
        getStateManager().startState(SlotAlbumPage.class, getArguments());
    }
}

package com.huawei.gallery.story.app;

import com.huawei.gallery.app.GLFragment;

public class StoryAlbumSetHost extends GLFragment {
    protected void onInflateFinished() {
        getStateManager().startState(StoryAlbumSetPage.class, getArguments());
    }
}

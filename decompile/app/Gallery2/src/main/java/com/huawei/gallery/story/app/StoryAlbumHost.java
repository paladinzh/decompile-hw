package com.huawei.gallery.story.app;

import com.huawei.gallery.app.GLFragment;

public class StoryAlbumHost extends GLFragment {
    protected void onInflateFinished() {
        getStateManager().startState(StoryAlbumPage.class, getArguments());
    }
}

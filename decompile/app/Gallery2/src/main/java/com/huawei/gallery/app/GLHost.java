package com.huawei.gallery.app;

import android.support.v4.app.FragmentActivity;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.app.TransitionStore;
import com.android.gallery3d.ui.GLRoot;
import com.huawei.gallery.actionbar.GalleryActionBar;

public interface GLHost {
    FragmentActivity getActivity();

    GLRoot getGLRoot();

    GalleryActionBar getGalleryActionBar();

    GalleryContext getGalleryContext();

    StateManager getStateManager();

    TransitionStore getTransitionStore();

    boolean inflatable();

    void requestFeature(int i);
}

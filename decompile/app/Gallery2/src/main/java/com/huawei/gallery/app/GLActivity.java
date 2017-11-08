package com.huawei.gallery.app;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.app.TransitionStore;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.GLRootView;
import com.android.gallery3d.util.GalleryLog;
import com.android.photos.data.GalleryBitmapPool;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.GalleryActionBar;
import com.huawei.gallery.util.LayoutHelper;
import com.huawei.gallery.util.UIUtils;

public class GLActivity extends AbstractGalleryActivity implements GLHost {
    private GLRootView mGLRootView;
    private int mNavigationFeature = -1;
    private StateManager mStateManager;
    private TransitionStore mTransitionStore = new TransitionStore();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(null);
    }

    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        this.mGLRootView = (GLRootView) findViewById(R.id.gl_root_view);
    }

    protected void onStart() {
        super.onStart();
        this.mGLRootView.lockRenderThread();
        try {
            getStateManager().start();
        } finally {
            this.mGLRootView.unlockRenderThread();
        }
    }

    protected void onResume() {
        super.onResume();
        this.mGLRootView.lockRenderThread();
        try {
            getStateManager().resume();
            getGalleryContext().getDataManager().resume();
            this.mGLRootView.onResume();
        } finally {
            this.mGLRootView.unlockRenderThread();
        }
    }

    public void onActionItemClicked(Action action) {
        this.mGLRootView.lockRenderThread();
        try {
            getStateManager().itemSelected(action);
        } finally {
            this.mGLRootView.unlockRenderThread();
        }
    }

    public void onBackPressed() {
        boolean z = false;
        GLRoot root = getGLRoot();
        root.lockRenderThread();
        try {
            z = getStateManager().onBackPressed();
            if (!z) {
                super.onBackPressed();
            }
        } finally {
            root.unlockRenderThread();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getStateManager().onConfigurationChange(newConfig);
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        this.mGLRootView.lockRenderThread();
        try {
            getStateManager().saveState(outState);
        } finally {
            this.mGLRootView.unlockRenderThread();
        }
    }

    protected void onPause() {
        super.onPause();
        this.mGLRootView.onPause();
        this.mGLRootView.lockRenderThread();
        GalleryLog.d("GLActivity", "onPause is called.");
        try {
            getStateManager().pause();
            getGalleryContext().getDataManager().pause();
            GalleryBitmapPool.getInstance().clear();
            MediaItem.getBytesBufferPool().clear();
        } finally {
            this.mGLRootView.unlockRenderThread();
        }
    }

    protected void onStop() {
        super.onStop();
        this.mGLRootView.lockRenderThread();
        try {
            getStateManager().stop();
        } finally {
            this.mGLRootView.unlockRenderThread();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mGLRootView.lockRenderThread();
        try {
            getStateManager().destroy();
        } finally {
            this.mGLRootView.unlockRenderThread();
        }
    }

    public void onNavigationBarChanged(boolean show, int height) {
        if (getWindow().getDecorView() != null && navigationFeatureOverlay()) {
            GalleryActionBar actionBar = getGalleryActionBar();
            actionBar.setNavigationMargin(height);
            actionBar.onNavigationBarChanged(show);
        }
        getStateManager().onNavigationBarChanged(show, height);
    }

    public StateManager getStateManager() {
        if (this.mStateManager == null) {
            this.mStateManager = new StateManager(this);
        }
        return this.mStateManager;
    }

    public FragmentActivity getActivity() {
        return this;
    }

    public TransitionStore getTransitionStore() {
        return this.mTransitionStore;
    }

    public GalleryContext getGalleryContext() {
        return this;
    }

    public GLRoot getGLRoot() {
        return this.mGLRootView;
    }

    public void requestFeature(int feature) {
        if (this.mNavigationFeature != feature) {
            boolean z;
            View view = getWindow().getDecorView();
            this.mNavigationFeature = feature;
            GalleryActionBar actionBar = getGalleryActionBar();
            if ((feature & 4) != 0) {
                actionBar.setHeadBackground(0);
                actionBar.setActionPanelStyle(1);
            } else {
                actionBar.setHeadDefaultBackground();
                actionBar.setActionPanelStyle(0);
            }
            if ((feature & 2) == 0) {
                z = true;
            } else {
                z = false;
            }
            actionBar.setActionPanelVisible(z);
            if ((feature & 1) == 0) {
                z = true;
            } else {
                z = false;
            }
            actionBar.setHeadBarVisible(z);
            if ((feature & 256) != 0) {
                UIUtils.setNavigationBarIsOverlay(view, true);
                actionBar.setNavigationMargin(LayoutHelper.getNavigationBarHeight());
            } else {
                UIUtils.setNavigationBarIsOverlay(view, false);
                actionBar.setNavigationMargin(0);
            }
        }
    }

    private boolean navigationFeatureOverlay() {
        return (this.mNavigationFeature & 256) != 0;
    }

    public boolean inflatable() {
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.mGLRootView.lockRenderThread();
        try {
            getStateManager().notifyActivityResult(requestCode, resultCode, data);
        } finally {
            this.mGLRootView.unlockRenderThread();
        }
    }

    public boolean onKeyDown(int downKeyCode, KeyEvent event) {
        if (getStateManager().getStateCount() <= 0) {
            return super.onKeyDown(downKeyCode, event);
        }
        if (getStateManager().getTopState().onKeyDown(downKeyCode, event)) {
            return true;
        }
        return super.onKeyDown(downKeyCode, event);
    }

    public boolean onKeyUp(int upKeyCode, KeyEvent event) {
        if (getStateManager().getStateCount() <= 0) {
            return super.onKeyUp(upKeyCode, event);
        }
        if (getStateManager().getTopState().onKeyUp(upKeyCode, event)) {
            return true;
        }
        return super.onKeyUp(upKeyCode, event);
    }
}

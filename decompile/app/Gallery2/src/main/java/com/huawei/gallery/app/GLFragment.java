package com.huawei.gallery.app;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import com.android.gallery3d.R;
import com.android.gallery3d.app.TransitionStore;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.GLRootView;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.TraceController;
import com.android.photos.data.GalleryBitmapPool;
import com.huawei.gallery.actionbar.Action;

@TargetApi(19)
public abstract class GLFragment extends AbstractGalleryFragment implements GLHost {
    private static final String TAG = GLFragment.class.getSimpleName();
    protected GLRootView mGLRootView;
    private StateManager mStateManager;
    private TransitionStore mTransitionStore = new TransitionStore();

    protected abstract void onInflateFinished();

    public void onCreate(Bundle savedInstanceState) {
        TraceController.beginSection("GLFragment.onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().getWindow().setBackgroundDrawable(null);
        TraceController.endSection();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TraceController.beginSection("GLFragment.onCreateView");
        View view = inflater.inflate(getContentViewId(), container, false);
        TraceController.endSection();
        return view;
    }

    protected int getContentViewId() {
        return R.layout.layout_glfragment_default;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        TraceController.beginSection("GLFragment.onViewCreated");
        this.mGLRootView = (GLRootView) view.findViewById(R.id.gl_root_view);
        if (savedInstanceState == null) {
            onInflateFinished();
        } else {
            GalleryLog.i(TAG, "restore states");
            getStateManager().restoreFromState(savedInstanceState);
            onRestoreFinished();
        }
        TraceController.endSection();
    }

    protected void onRestoreFinished() {
    }

    protected void onCreateActionBar(Menu menu) {
        TraceController.beginSection("GLFragment.onCreateActionBar");
        if (this.mGLRootView != null && getUserVisibleHint()) {
            this.mGLRootView.lockRenderThread();
            try {
                getStateManager().createOptionsMenu(menu);
                TraceController.endSection();
            } finally {
                this.mGLRootView.unlockRenderThread();
            }
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

    public boolean onBackPressed() {
        boolean z = false;
        this.mGLRootView.lockRenderThread();
        try {
            z = getStateManager().onBackPressed();
            return z;
        } finally {
            this.mGLRootView.unlockRenderThread();
        }
    }

    public void onStart() {
        TraceController.beginSection("GLFragment.onStart");
        super.onStart();
        this.mGLRootView.lockRenderThread();
        try {
            getStateManager().start();
            TraceController.endSection();
        } finally {
            this.mGLRootView.unlockRenderThread();
        }
    }

    public void onResume() {
        TraceController.beginSection("GLFragment.onResume");
        super.onResume();
        this.mGLRootView.lockRenderThread();
        try {
            getStateManager().resume();
            getGalleryContext().getDataManager().resume();
            this.mGLRootView.onResume();
            TraceController.endSection();
        } finally {
            this.mGLRootView.unlockRenderThread();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        GalleryLog.v(TAG, "onConfigurationChanged");
        getStateManager().onConfigurationChange(newConfig);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        this.mGLRootView.lockRenderThread();
        try {
            getStateManager().saveState(outState);
            GalleryLog.v(TAG, "onSaveInstanceState");
        } finally {
            this.mGLRootView.unlockRenderThread();
        }
    }

    public void onPause() {
        super.onPause();
        this.mGLRootView.onPause();
        this.mGLRootView.lockRenderThread();
        try {
            getStateManager().pause();
            getGalleryContext().getDataManager().pause();
            GalleryLog.printDFXLog("GLFragment");
            GalleryBitmapPool.getInstance().clear();
            MediaItem.getBytesBufferPool().clear();
            GalleryLog.v(TAG, "onPause");
        } finally {
            this.mGLRootView.unlockRenderThread();
        }
    }

    public void onStop() {
        super.onStop();
        this.mGLRootView.lockRenderThread();
        try {
            getStateManager().stop();
            GalleryLog.v(TAG, "onStop");
        } finally {
            this.mGLRootView.unlockRenderThread();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        this.mGLRootView.lockRenderThread();
        try {
            getStateManager().destroy();
            GalleryLog.v(TAG, "onDestroy");
        } finally {
            this.mGLRootView.unlockRenderThread();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.mGLRootView.lockRenderThread();
        try {
            getStateManager().notifyActivityResult(requestCode, resultCode, data);
        } finally {
            this.mGLRootView.unlockRenderThread();
        }
    }

    public boolean inflatable() {
        return getUserVisibleHint();
    }

    public GLRoot getGLRoot() {
        return this.mGLRootView;
    }

    public StateManager getStateManager() {
        if (this.mStateManager == null) {
            this.mStateManager = new StateManager(this);
        }
        return this.mStateManager;
    }

    public TransitionStore getTransitionStore() {
        return this.mTransitionStore;
    }

    protected void setWindowPadding(int feature) {
        getView().setPadding(0, 0, 0, 0);
    }

    public void onNavigationBarChanged(boolean show, int height) {
        super.onNavigationBarChanged(show, height);
        getStateManager().onNavigationBarChanged(show, height);
    }

    protected boolean onKeyDown(int keyCode, KeyEvent event) {
        if (getStateManager().getStateCount() <= 0) {
            return super.onKeyDown(keyCode, event);
        }
        if (getStateManager().getTopState().onKeyDown(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected boolean onKeyUp(int keyCode, KeyEvent event) {
        if (getStateManager().getStateCount() <= 0) {
            return super.onKeyUp(keyCode, event);
        }
        if (getStateManager().getTopState().onKeyUp(keyCode, event)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    protected void onUserSelected(boolean selected) {
        super.onUserSelected(selected);
        if (this.mGLRootView != null) {
            this.mGLRootView.lockRenderThread();
            try {
                getStateManager().onUserSelected(selected);
            } finally {
                this.mGLRootView.unlockRenderThread();
            }
        }
    }
}

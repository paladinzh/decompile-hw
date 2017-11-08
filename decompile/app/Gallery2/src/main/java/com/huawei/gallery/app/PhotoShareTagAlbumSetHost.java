package com.huawei.gallery.app;

import android.os.Bundle;
import android.view.View;
import com.android.gallery3d.R;
import com.android.gallery3d.ui.GLRootView;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;

public class PhotoShareTagAlbumSetHost extends GLFragment {
    private boolean mIsDestroyed = false;

    public void onViewCreated(View view, final Bundle savedInstanceState) {
        if (PhotoShareUtils.getServer() != null || getArguments().getBoolean("local-only")) {
            super.onViewCreated(view, savedInstanceState);
            return;
        }
        this.mGLRootView = (GLRootView) view.findViewById(R.id.gl_root_view);
        PhotoShareUtils.setRunnable(new Runnable() {
            public void run() {
                if (!PhotoShareTagAlbumSetHost.this.mIsDestroyed) {
                    PhotoShareTagAlbumSetHost.this.initializeState(savedInstanceState);
                }
            }
        });
    }

    protected void onInflateFinished() {
        getStateManager().startState(PhotoShareTagAlbumSetPage.class, getArguments());
    }

    private void initializeState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            onInflateFinished();
            return;
        }
        GalleryLog.i("PhotoShareTagAlbumSetHost", "DFX lazy restore states");
        getStateManager().restoreFromState(savedInstanceState);
        onRestoreFinished();
        this.mGLRootView.lockRenderThread();
        try {
            getStateManager().lazyResumeTopState();
        } finally {
            this.mGLRootView.unlockRenderThread();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        this.mIsDestroyed = true;
    }
}

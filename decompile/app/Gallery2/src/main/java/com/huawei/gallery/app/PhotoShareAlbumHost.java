package com.huawei.gallery.app;

import android.os.Bundle;
import android.view.View;
import com.android.gallery3d.R;
import com.android.gallery3d.ui.GLRootView;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;

public class PhotoShareAlbumHost extends GLFragment {
    private boolean mIsDestroyed = false;
    protected String mTag;

    public PhotoShareAlbumHost() {
        initTag();
    }

    protected void initTag() {
        this.mTag = "PhotoShareAlbumHost";
    }

    protected void onInflateFinished() {
        getStateManager().startState(PhotoShareAlbumPage.class, getArguments());
    }

    public void onViewCreated(View view, final Bundle savedInstanceState) {
        if (PhotoShareUtils.getServer() != null || getArguments().getBoolean("local-only")) {
            super.onViewCreated(view, savedInstanceState);
            return;
        }
        GalleryLog.printDFXLog("PhotoShareAlbumHost");
        this.mGLRootView = (GLRootView) view.findViewById(R.id.gl_root_view);
        PhotoShareUtils.setRunnable(new Runnable() {
            public void run() {
                if (!PhotoShareAlbumHost.this.mIsDestroyed) {
                    PhotoShareAlbumHost.this.initializeState(savedInstanceState);
                }
            }
        });
    }

    public void onDestroy() {
        super.onDestroy();
        this.mIsDestroyed = true;
    }

    private void initializeState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            onInflateFinished();
            return;
        }
        GalleryLog.i(this.mTag, "lazy restore states");
        getStateManager().restoreFromState(savedInstanceState);
        onRestoreFinished();
        this.mGLRootView.lockRenderThread();
        try {
            getStateManager().lazyResumeTopState();
        } finally {
            this.mGLRootView.unlockRenderThread();
        }
    }
}

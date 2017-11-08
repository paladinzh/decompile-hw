package com.huawei.gallery.editor.screenshotseditor.ui;

import android.app.Activity;
import android.content.res.Configuration;
import android.net.Uri;
import com.android.gallery3d.app.IntentChooser.IntentChooserDialogClickListener;
import com.android.gallery3d.app.TransitionStore;
import com.android.gallery3d.ui.GLRoot;
import com.huawei.gallery.editor.screenshotseditor.app.ScreenShotsPreviewState;
import com.huawei.gallery.editor.ui.BaseEditorView;
import com.huawei.gallery.editor.ui.BaseEditorView.Delegate;

public class ScreenShotsEditorView extends BaseEditorView implements IntentChooserDialogClickListener {
    private Uri mLeaveEditorUri;

    public ScreenShotsEditorView(Delegate delegate, Activity activity) {
        super(delegate, activity);
    }

    public void enterEditor() {
        setVisibility(0);
        requestLayout();
        super.enterEditor();
        if (this.mPreviewState == null) {
            this.mPreviewState = new ScreenShotsPreviewState(this.mActivity, this.mParentLayout, this);
        }
        changeState(this.mPreviewState);
        updateBackground();
    }

    public void onServiceConnected() {
        if (this.mPreviewState == null) {
            this.mPreviewState = new ScreenShotsPreviewState(this.mActivity, this.mParentLayout, this);
        }
        this.mPreviewState.prepareEditorState();
    }

    public void onNavigationBarChange(int currHeight) {
        super.onNavigationBarChange(currHeight);
    }

    protected void notifyStateOnNavigationBarChanged() {
        super.notifyStateOnNavigationBarChanged();
        this.mPreviewState.onNavigationBarChanged();
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        this.mPreviewState.onConfigurationChanged(config);
        this.mPreviewState.onNavigationBarChanged();
    }

    public void leaveEditor(Uri uri, boolean needAnime) {
        this.mLeaveEditorUri = uri;
        hideSavingProgress();
        if (this.mExitMode == 1) {
            Uri shareUri = uri;
            if (uri == null) {
                shareUri = this.mEditorManager.getUri();
            }
            this.mDelegate.share(shareUri, this);
            return;
        }
        super.leaveEditor(uri, needAnime);
        TransitionStore transitionStore = getTransitionStore();
        if (transitionStore != null) {
            transitionStore.clear();
        }
    }

    public void onClickItem() {
        this.mExitMode = 0;
        GLRoot glRoot = getGLRoot();
        if (glRoot == null) {
            leaveEditor(this.mLeaveEditorUri, false);
            return;
        }
        glRoot.lockRenderThread();
        try {
            leaveEditor(this.mLeaveEditorUri, false);
        } finally {
            glRoot.unlockRenderThread();
        }
    }

    public void onClickCancel() {
        this.mExitMode = 0;
    }
}

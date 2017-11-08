package com.huawei.gallery.refocus.app;

import android.opengl.GLSurfaceView;
import android.view.View;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.ui.GLView;
import com.huawei.gallery.refocus.allfocus.app.AllFocusState;
import com.huawei.gallery.refocus.wideaperture.app.WideApertureState;

public class StateManager {
    private State mCurrentState = null;

    public void enterState(GalleryContext context, View parentLayout, AbsRefocusDelegate delegate, int state, GLView glView, GLSurfaceView glSurfaceView) {
        switch (state) {
            case 0:
                this.mCurrentState = new AllFocusState(context, parentLayout, delegate);
                return;
            case 1:
                this.mCurrentState = new WideApertureState(context, parentLayout, delegate);
                return;
            case 2:
            case 3:
                return;
            default:
                throw new IllegalArgumentException("invalid state: " + state);
        }
    }

    public State getCurrentState() {
        return this.mCurrentState;
    }
}

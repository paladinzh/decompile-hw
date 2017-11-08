package com.android.gallery3d.ui;

import android.view.Window;
import android.view.WindowManager.LayoutParams;
import com.android.gallery3d.util.MultiWindowStatusHolder;

public class WindowFlag {
    private int mAddFlag;

    public WindowFlag(Window window) {
        if (!MultiWindowStatusHolder.isInMultiWindowMode()) {
            LayoutParams params = window.getAttributes();
            if ((params.flags & 134217728) == 0) {
                this.mAddFlag |= 134217728;
            }
            params.flags |= this.mAddFlag;
            window.setAttributes(params);
        }
    }

    public void reset(Window window) {
        if (!MultiWindowStatusHolder.isInMultiWindowMode()) {
            LayoutParams params = window.getAttributes();
            if ((this.mAddFlag & 134217728) != 0) {
                params.flags &= -134217729;
            }
            window.setAttributes(params);
            this.mAddFlag = 0;
        }
    }
}

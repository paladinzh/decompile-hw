package com.android.systemui.recents.views;

import com.android.systemui.recents.model.TaskStack.DockState;

/* compiled from: RecentsViewTouchHandler */
class DockRegion {
    public static DockState[] PHONE_LANDSCAPE = new DockState[]{DockState.LEFT};
    public static DockState[] PHONE_PORTRAIT = new DockState[]{DockState.TOP};
    public static DockState[] TABLET_LANDSCAPE = PHONE_LANDSCAPE;
    public static DockState[] TABLET_PORTRAIT = PHONE_PORTRAIT;

    DockRegion() {
    }
}

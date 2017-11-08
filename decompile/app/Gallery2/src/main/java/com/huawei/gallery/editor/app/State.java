package com.huawei.gallery.editor.app;

import com.android.gallery3d.ui.GestureRecognizer.Listener;

public interface State extends Listener {
    void onLayout(boolean z, int i, int i2, int i3, int i4);

    void onLeaveEditor();
}

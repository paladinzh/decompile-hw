package com.android.systemui;

import android.graphics.Rect;
import android.view.Display;

public interface RecentsComponent {
    void cancelPreloadingRecents();

    boolean dockTopTask(int i, int i2, Rect rect, int i3);

    void hideRecents(boolean z, boolean z2);

    void onDraggingInRecents(float f);

    void onDraggingInRecentsEnded(float f);

    void preloadRecents();

    void showNextAffiliatedTask();

    void showPrevAffiliatedTask();

    void showRecents(boolean z, boolean z2);

    void toggleRecents(Display display);
}

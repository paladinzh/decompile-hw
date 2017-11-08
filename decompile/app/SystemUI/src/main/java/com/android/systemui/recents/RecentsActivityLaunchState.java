package com.android.systemui.recents;

public class RecentsActivityLaunchState {
    public boolean launchedFromApp;
    public boolean launchedFromHome;
    public int launchedNumVisibleTasks;
    public int launchedNumVisibleThumbnails;
    public int launchedToTaskId;
    public boolean launchedViaDockGesture;
    public boolean launchedViaDragGesture;
    public boolean launchedWithAltTab;

    public void reset() {
        this.launchedFromHome = false;
        this.launchedFromApp = false;
        this.launchedToTaskId = -1;
        this.launchedWithAltTab = false;
        this.launchedViaDragGesture = false;
        this.launchedViaDockGesture = false;
    }

    public int getInitialFocusTaskIndex(int numTasks) {
        RecentsDebugFlags debugFlags = Recents.getDebugFlags();
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        if (this.launchedFromApp) {
            if (launchState.launchedWithAltTab || !debugFlags.isFastToggleRecentsEnabled()) {
                return Math.max(0, numTasks - 2);
            }
            return numTasks - 1;
        } else if (launchState.launchedWithAltTab || !debugFlags.isFastToggleRecentsEnabled()) {
            return numTasks - 1;
        } else {
            return -1;
        }
    }
}

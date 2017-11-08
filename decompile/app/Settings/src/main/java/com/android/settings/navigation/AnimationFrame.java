package com.android.settings.navigation;

public class AnimationFrame {
    private int mDuration;
    private int mResourceId;

    AnimationFrame(int resourceId, int duration) {
        this.mResourceId = resourceId;
        this.mDuration = duration;
    }

    public int getResourceId() {
        return this.mResourceId;
    }

    public int getDuration() {
        return this.mDuration;
    }
}

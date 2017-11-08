package com.android.deskclock;

public class QuickActionConfig {
    private final int mIconId;
    private final int mNameId;

    public QuickActionConfig(int nameId, int iconId) {
        this.mNameId = nameId;
        this.mIconId = iconId;
    }

    public int getNameId() {
        return this.mNameId;
    }

    public int getIcon() {
        return this.mIconId;
    }
}

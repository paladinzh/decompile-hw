package com.huawei.systemmanager.spacecleanner.ui.trashlistadapter;

import android.view.LayoutInflater;
import com.huawei.systemmanager.spacecleanner.ui.SpaceState;

public abstract class ViewType {
    private final LayoutInflater mLayoutInflater;
    protected SpaceState mState = SpaceState.NORMAL_SCANNING;

    abstract int getType();

    public ViewType(LayoutInflater inflater) {
        this.mLayoutInflater = inflater;
    }

    public LayoutInflater getInflater() {
        return this.mLayoutInflater;
    }

    void setSpace(SpaceState state) {
        this.mState = state;
    }
}

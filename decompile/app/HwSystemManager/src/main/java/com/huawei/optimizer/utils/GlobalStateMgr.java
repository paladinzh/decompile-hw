package com.huawei.optimizer.utils;

public final class GlobalStateMgr {
    private static final String TAG = "GlobalStateMgr";
    private static GlobalStateMgr sInstance = new GlobalStateMgr();
    private boolean mRooted = false;

    public static GlobalStateMgr getInstance() {
        return sInstance;
    }

    public void onAppQuit() {
    }

    public boolean isRooted() {
        return this.mRooted;
    }
}

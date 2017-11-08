package com.huawei.keyguard.inf;

public interface ILiftAbleView {

    public interface ILiftStateListener {
        void onLiftModeStateChange(float f);
    }

    void registerLiftStateListener(ILiftStateListener iLiftStateListener);

    void unregisterLiftStateListener(ILiftStateListener iLiftStateListener);
}

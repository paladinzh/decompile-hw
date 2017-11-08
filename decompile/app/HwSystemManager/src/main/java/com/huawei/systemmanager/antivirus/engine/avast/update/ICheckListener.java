package com.huawei.systemmanager.antivirus.engine.avast.update;

public interface ICheckListener {
    void onCheckCanceled();

    void onCheckEvent(int i);

    void onCheckFinished(boolean z);

    void onCheckStarted();
}

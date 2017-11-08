package com.huawei.systemmanager.antivirus.engine.avast.update;

public interface IUpdateListener {
    void onProgressChanged(Object obj, int i);

    void onUpdateCanceled();

    void onUpdateEvent(Object obj, int i);

    void onUpdateFinished();

    void onUpdateStarted();
}

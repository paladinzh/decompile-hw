package com.huawei.harassmentinterception.update;

public interface IHwUpdateListener {
    void onBackgroundUpdateFinished(int i);

    void onUpdateCancel(int i);

    void onUpdateError(int i);

    void onUpdateFinished(int i);

    void onUpdateProgress(int i);

    void onUpdateStart(int i);
}

package com.huawei.timekeeper;

public interface TimeObserver {
    void onTimeFinish();

    void onTimeTick(TimeTickInfo timeTickInfo);
}

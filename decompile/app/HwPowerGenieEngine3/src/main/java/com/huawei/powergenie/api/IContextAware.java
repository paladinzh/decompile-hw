package com.huawei.powergenie.api;

public interface IContextAware {
    int getUserState();

    long getUserStationaryDuration();

    boolean startMotionDetection(long j);

    void stopMotionDetection();
}

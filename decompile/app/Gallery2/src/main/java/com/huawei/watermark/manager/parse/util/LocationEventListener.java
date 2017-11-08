package com.huawei.watermark.manager.parse.util;

public interface LocationEventListener {
    void onLocationFailed();

    void onLocationSuccess(double d, double d2);
}

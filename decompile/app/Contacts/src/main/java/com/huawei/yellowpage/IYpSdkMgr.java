package com.huawei.yellowpage;

import android.content.Context;
import android.view.View;

public interface IYpSdkMgr {
    View getView(Context context);

    boolean initPlug(Context context);

    void onConfigurationChanged(Context context);

    void onPageSelected(Context context);

    void onPlugDestory(Context context);

    void onPlugIn(Context context);

    void onPlugOut(Context context);

    void onPlugPause(Context context);

    void onPlugResume(Context context);

    void onPlugStart(Context context);

    void onPlugStop(Context context);
}

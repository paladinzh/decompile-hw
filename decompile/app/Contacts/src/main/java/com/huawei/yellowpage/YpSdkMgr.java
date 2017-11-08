package com.huawei.yellowpage;

import android.content.Context;
import android.view.View;

public class YpSdkMgr {
    private static YpSdkMgr mInstance = null;
    private IYpSdkMgr ypsdkPlug = YpSdkManager.getInstance();

    public static YpSdkMgr getInstance() {
        if (mInstance == null) {
            mInstance = new YpSdkMgr();
        }
        return mInstance;
    }

    public boolean initPlug(Context context) {
        return this.ypsdkPlug.initPlug(context);
    }

    public View getView(Context context) {
        return this.ypsdkPlug.getView(context);
    }

    public void onPlugIn(Context context) {
        this.ypsdkPlug.onPlugIn(context);
    }

    public void onPlugOut(Context context) {
        this.ypsdkPlug.onPlugOut(context);
    }

    public void onPlugStart(Context context) {
        this.ypsdkPlug.onPlugStart(context);
    }

    public void onPlugResume(Context context) {
        this.ypsdkPlug.onPlugResume(context);
    }

    public void onPlugPause(Context context) {
        this.ypsdkPlug.onPlugPause(context);
    }

    public void onPlugStop(Context context) {
        this.ypsdkPlug.onPlugStop(context);
    }

    public void onPlugDestory(Context context) {
        this.ypsdkPlug.onPlugDestory(context);
    }

    public void onPageSelected(Context context) {
        this.ypsdkPlug.onPageSelected(context);
    }

    public void onConfigurationChanged(Context context) {
        this.ypsdkPlug.onConfigurationChanged(context);
    }
}

package com.huawei.powergenie.api;

import java.util.ArrayList;

public interface IAppType {
    int getAppType(String str);

    ArrayList<String> getAppsByType(int i);

    String getCurLiveWallpaper();

    String getDefaultInputMethod();

    String getDefaultLauncher();

    String getDefaultSmsApplication();

    String getUsingLauncher();

    void updateAppType(int i, String str);
}

package com.huawei.powergenie.api;

import java.util.ArrayList;

public interface IScenario {
    ArrayList<String> getAboveLauncherPkgs();

    String getAutoFrontPkgAfterScrOff();

    String getFrontPkg();

    String getTopBgPkg();
}

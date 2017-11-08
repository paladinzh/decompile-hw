package com.huawei.powergenie.modules.apppower.hibernation;

import java.util.ArrayList;
import java.util.Map;

public interface ASHStateInterface {
    ArrayList<String> getAboveLauncherPkgs();

    Map getApplicationMap();
}

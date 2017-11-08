package com.huawei.systemmanager.addviewmonitor;

import java.util.HashSet;
import java.util.Set;

public class AddViewUtils {
    private static Set<String> silentList = new HashSet();

    static {
        silentList.add("com.tencent.pb");
    }

    public static boolean shouldBeSilent(String pkg) {
        return silentList.contains(pkg);
    }
}

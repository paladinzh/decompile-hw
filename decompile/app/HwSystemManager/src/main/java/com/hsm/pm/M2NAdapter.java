package com.hsm.pm;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import java.util.List;

public class M2NAdapter {
    public static List<PackageInfo> getInstalledPackagesAsUser(PackageManager pm, int flags, int userId) {
        return pm.getInstalledPackagesAsUser((524288 | flags) | 262144, userId);
    }
}

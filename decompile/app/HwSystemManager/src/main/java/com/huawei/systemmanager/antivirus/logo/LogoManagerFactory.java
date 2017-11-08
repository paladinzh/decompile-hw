package com.huawei.systemmanager.antivirus.logo;

import android.view.ViewGroup;
import com.huawei.systemmanager.antivirus.logo.impl.AbroadLogoMgr;
import com.huawei.systemmanager.antivirus.logo.impl.ChinaLogoMgr;
import com.huawei.systemmanager.antivirus.utils.AntiVirusTools;

public class LogoManagerFactory {
    public static ILogoManager newInstance(ViewGroup viewGroup) {
        if (AntiVirusTools.isAbroad()) {
            return new AbroadLogoMgr(viewGroup);
        }
        return new ChinaLogoMgr(viewGroup);
    }
}

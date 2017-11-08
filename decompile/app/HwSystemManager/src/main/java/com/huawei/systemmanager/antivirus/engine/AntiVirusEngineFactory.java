package com.huawei.systemmanager.antivirus.engine;

import com.huawei.systemmanager.antivirus.engine.avast.AvastVirusEngine;
import com.huawei.systemmanager.antivirus.engine.tencent.TencentAntiVirusEngine;
import com.huawei.systemmanager.antivirus.utils.AntiVirusTools;

public class AntiVirusEngineFactory {
    public static IAntiVirusEngine newInstance() {
        if (AntiVirusTools.isAbroad()) {
            return new AvastVirusEngine();
        }
        return new TencentAntiVirusEngine();
    }
}

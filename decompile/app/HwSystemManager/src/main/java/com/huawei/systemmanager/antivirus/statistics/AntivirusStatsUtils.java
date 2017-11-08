package com.huawei.systemmanager.antivirus.statistics;

import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;

public class AntivirusStatsUtils {
    private static final String HASH = "HASH";
    private static final String VENDOR = "VENDOR";
    public static final String VENDOR_TENCENT = "TENCENT";
    public static final String VENDOR_TRUSTLOOK = "TRUSTLOOK";
    private static final String VERSION = "VERSION";

    public static void reportScanVirusInfo(String pkg, String version, String hash, String vendor) {
        String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_PKG, pkg, HsmStatConst.PARAM_KEY, VERSION, HsmStatConst.PARAM_VAL, version, HsmStatConst.PARAM_KEY, HASH, HsmStatConst.PARAM_VAL, hash, HsmStatConst.PARAM_KEY, VENDOR, HsmStatConst.PARAM_VAL, vendor);
        HsmStat.statE((int) Events.E_VIRUS_SCAN_INFO, statParam);
    }

    public static void reportScanCloudCount(VirusInfoBuilder builder) {
        HsmStat.statE((int) Events.E_VIRUS_SCAN_COUNT, builder.toString());
    }
}

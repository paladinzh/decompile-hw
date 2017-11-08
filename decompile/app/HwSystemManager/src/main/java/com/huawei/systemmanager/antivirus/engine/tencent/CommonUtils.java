package com.huawei.systemmanager.antivirus.engine.tencent;

import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.antivirus.statistics.AntivirusStatsUtils;
import com.huawei.systemmanager.antivirus.statistics.VirusInfoBuilder;
import java.util.Set;
import tmsdk.common.module.qscanner.QScanResultEntity;

public class CommonUtils {
    public static void calculateVirusInfo(ScanResultEntity entity, VirusInfoBuilder builder, String vendor) {
        if (ScanResultEntity.isRiskORVirus(entity)) {
            AntivirusStatsUtils.reportScanVirusInfo(entity.packageName, entity.mVersion, entity.mHash, vendor);
            builder.increaseVirusScanCount();
        }
        if (entity.type == 302) {
            builder.increaseUnKnownCount();
        }
    }

    public static void calculateVirusInfo(QScanResultEntity entity, VirusInfoBuilder builder, String vendor) {
        if (isRiskOrVirus(entity)) {
            AntivirusStatsUtils.reportScanVirusInfo(entity.packageName, entity.version, entity.dexSha1, vendor);
            builder.increaseVirusScanCount();
        }
        if (entity.type == 0) {
            builder.increaseUnKnownCount();
        }
    }

    public static boolean isRiskOrVirus(QScanResultEntity entity) {
        if (entity.type == 3 || entity.type == 2) {
            return true;
        }
        return false;
    }

    public static boolean isTecentTellDangerApk(QScanResultEntity result) {
        return result.safeLevel != 0 || result.plugins.size() > 0;
    }

    public static boolean isFindAPK(Set<String> dataSet, String key) {
        for (String data : dataSet) {
            if (key.equalsIgnoreCase(data)) {
                return true;
            }
        }
        return false;
    }
}

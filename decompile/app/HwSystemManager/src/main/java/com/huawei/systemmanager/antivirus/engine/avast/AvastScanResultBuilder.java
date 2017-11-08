package com.huawei.systemmanager.antivirus.engine.avast;

import android.content.pm.PackageInfo;
import android.text.TextUtils;
import com.avast.android.sdk.engine.ScanResultStructure;
import com.avast.android.sdk.engine.ScanResultStructure.DetectionType;
import com.avast.android.sdk.engine.ScanResultStructure.ScanResult;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.antivirus.utils.AntiVirusTools;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import java.util.List;

public class AvastScanResultBuilder {
    public static ScanResultEntity parseScanResultEntity(PackageInfo packageInfo, String filepath, List<ScanResultStructure> results, boolean isUninstalledApk) {
        ScanResultEntity entity = new ScanResultEntity(GlobalContext.getContext(), packageInfo);
        if (TextUtils.isEmpty(filepath)) {
            entity.apkFilePath = packageInfo.applicationInfo.sourceDir;
        } else {
            entity.apkFilePath = filepath;
            packageInfo.applicationInfo.publicSourceDir = filepath;
            packageInfo.applicationInfo.sourceDir = filepath;
        }
        entity.isUninstalledApk = isUninstalledApk;
        if (ScanResult.RESULT_OK != ((ScanResultStructure) results.get(0)).result) {
            return parseScanResultEntity(entity, results);
        }
        entity.virusName = "";
        entity.type = 301;
        return entity;
    }

    public static ScanResultEntity parseScanResultEntity(PackageInfo packageInfo, List<ScanResultStructure> results, boolean isUninstalledApk) {
        return parseScanResultEntity(packageInfo, null, results, isUninstalledApk);
    }

    private static ScanResultEntity parseScanResultEntity(ScanResultEntity entity, List<ScanResultStructure> results) {
        int type = 301;
        for (ScanResultStructure result : results) {
            if (result.addonCategories != null) {
                type = AntiVirusTools.TYPE_ADVERTISE;
                entity.mPlugNames.add(parseInfectionTypeName(result.infectionType));
            } else {
                if (!(result.detectionType == DetectionType.TYPE_CRYPTOR || result.detectionType == DetectionType.TYPE_DIALER || result.detectionType == DetectionType.TYPE_DROPPER || result.detectionType == DetectionType.TYPE_EXPLOIT || result.detectionType == DetectionType.TYPE_HEURISTICS || result.detectionType == DetectionType.TYPE_JOKE || result.detectionType == DetectionType.TYPE_PUP || result.detectionType == DetectionType.TYPE_ROOTKIT || result.detectionType == DetectionType.TYPE_SPYWARE || result.detectionType == DetectionType.TYPE_SUSPICIOUS || result.detectionType == DetectionType.TYPE_TOOL || result.detectionType == DetectionType.TYPE_TROJAN || result.detectionType == DetectionType.TYPE_VIRUS_MAKING_KIT)) {
                    if (result.detectionType == DetectionType.TYPE_WORM) {
                    }
                    if (result.detectionType == DetectionType.TYPE_UNKNOWN && type != AntiVirusTools.TYPE_VIRUS) {
                        type = 302;
                    }
                    entity.virusName = parseInfectionTypeName(result.infectionType);
                    if (result.detectionType == DetectionType.TYPE_ADWARE && type != AntiVirusTools.TYPE_VIRUS) {
                        type = AntiVirusTools.TYPE_ADVERTISE;
                    }
                }
                type = AntiVirusTools.TYPE_VIRUS;
                type = 302;
                entity.virusName = parseInfectionTypeName(result.infectionType);
                type = AntiVirusTools.TYPE_ADVERTISE;
            }
        }
        entity.type = type;
        return entity;
    }

    public static String parseInfectionTypeName(ScanResultStructure scanResult) {
        return parseInfectionTypeName(scanResult.infectionType);
    }

    public static String parseInfectionTypeName(String infectionType) {
        int start = 0;
        if (TextUtils.isEmpty(infectionType)) {
            return "";
        }
        int end;
        int colonIndex = infectionType.indexOf(58);
        int dashIndex = infectionType.indexOf("[");
        int length = infectionType.length();
        if (colonIndex >= 0 && colonIndex + 1 < length) {
            start = colonIndex + 1;
        }
        if (dashIndex > start) {
            end = dashIndex;
        } else {
            end = length;
        }
        return infectionType.substring(start, end).trim();
    }
}

package com.huawei.systemmanager.antivirus.engine.avast.scan;

import android.content.Context;
import android.content.pm.PackageInfo;
import com.avast.android.sdk.engine.CloudScanResultStructure;
import com.avast.android.sdk.engine.CloudScanResultStructure.CloudScanResult;
import com.avast.android.sdk.engine.EngineInterface;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.antivirus.utils.AntiVirusTools;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CloudScanMgr {
    public static List<ScanResultEntity> cloudScan(Context context, List<File> filesToScan, Map<String, PackageInfo> pkgMap, boolean isUninstalledApk) {
        if (filesToScan == null || filesToScan.isEmpty()) {
            return new ArrayList();
        }
        return parseResult(EngineInterface.cloudScan(context, null, null, filesToScan), pkgMap, isUninstalledApk);
    }

    private static List<ScanResultEntity> parseResult(Map<String, CloudScanResultStructure> data, Map<String, PackageInfo> pkgMap, boolean isUninstalledApk) {
        List<ScanResultEntity> results = new ArrayList();
        if (data == null) {
            return results;
        }
        for (Entry<String, CloudScanResultStructure> entry : data.entrySet()) {
            CloudScanResultStructure cloudResultStructure = (CloudScanResultStructure) entry.getValue();
            String filePath = (String) entry.getKey();
            if (CloudScanResult.RESULT_INFECTED == cloudResultStructure.getResult()) {
                PackageInfo packInfo = (PackageInfo) pkgMap.get(filePath);
                if (packInfo != null) {
                    results.add(parseResult(cloudResultStructure, packInfo, filePath, isUninstalledApk));
                }
            }
        }
        return results;
    }

    private static ScanResultEntity parseResult(CloudScanResultStructure cloudResultStructure, PackageInfo packageInfo, String filepath, boolean isUninstalledApk) {
        ScanResultEntity entity = new ScanResultEntity(GlobalContext.getContext(), packageInfo);
        entity.apkFilePath = filepath;
        packageInfo.applicationInfo.publicSourceDir = filepath;
        packageInfo.applicationInfo.sourceDir = filepath;
        entity.isUninstalledApk = isUninstalledApk;
        entity.type = AntiVirusTools.TYPE_VIRUS;
        entity.virusName = cloudResultStructure.getInfectionName();
        return entity;
    }
}

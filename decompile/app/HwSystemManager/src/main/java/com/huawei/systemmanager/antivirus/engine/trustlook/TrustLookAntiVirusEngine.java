package com.huawei.systemmanager.antivirus.engine.trustlook;

import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.antivirus.engine.IScanPackageMgr;
import com.huawei.systemmanager.antivirus.utils.AntiVirusTools;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;
import com.trustlook.sdk.cloudscan.CloudScanClient;
import com.trustlook.sdk.cloudscan.CloudScanClient.Builder;
import com.trustlook.sdk.cloudscan.ScanResult;
import com.trustlook.sdk.data.AppInfo;
import com.trustlook.sdk.data.PkgInfo;
import com.trustlook.sdk.data.Region;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TrustLookAntiVirusEngine implements IScanPackageMgr {
    private static final String CLIENT_KEY = "5e459eea740bd3dc9fdd680eba4d43f4601f2d27450c4e8332c2587d";
    private static final int CONNECT_TIMEOUT = 3000;
    private static final String KEY_ADWARE = "adware";
    private static final int SCORE_HIGH_RISK_LEVEL = 8;
    private static final int SCORE_LOW_RISK_LEVEL = 7;
    private static final int SCORE_SAFE_LEVEL = 6;
    private static final int SCORE_UNKNOWN_LEVEL = -1;
    private static final int SOCKET_TIMEOUT = 5000;
    private static final String TAG = "TrustLookAntiVirusEngine";
    private CloudScanClient mCloudScanClient;

    public TrustLookAntiVirusEngine() {
        this.mCloudScanClient = null;
        this.mCloudScanClient = new Builder().setContext(GlobalContext.getContext()).setRegion(Region.CHN).setConnectionTimeout(3000).setSocketTimeout(5000).setToken(CLIENT_KEY).build();
    }

    public ScanResultEntity scanPackage(ScanResultEntity entity) {
        return scanPackage(entity, "", "");
    }

    public ScanResultEntity scanPackage(ScanResultEntity entity, String path, String source) {
        PkgInfo info = this.mCloudScanClient.populatePkgInfo(entity.packageName, entity.apkFilePath);
        if (entity.isUninstalledApk) {
            info.setPkgPath(path);
            info.setPkgSource(source);
            HwLog.i(TAG, "tl scan uninstalled apk " + entity.packageName + " source=" + source);
        }
        List<PkgInfo> pkgList = new ArrayList();
        pkgList.add(info);
        return valueOf(entity, this.mCloudScanClient.cloudScan(pkgList));
    }

    public List<ScanResultEntity> scanPackage(Map<String, ScanResultEntity> scanPackageMap) {
        List<PkgInfo> pkgList = new ArrayList();
        for (ScanResultEntity scanPackage : scanPackageMap.values()) {
            pkgList.add(this.mCloudScanClient.populatePkgInfo(scanPackage.packageName, scanPackage.apkFilePath));
        }
        return valueOf((Map) scanPackageMap, this.mCloudScanClient.cloudScan(pkgList));
    }

    private static ScanResultEntity valueOf(ScanResultEntity entity, ScanResult result) {
        HwLog.d(TAG, "scan status: pkg=" + entity.packageName + ",result=" + result.isSuccess() + ", error=" + result.getError());
        if (result.isSuccess()) {
            return parseResult(entity, (AppInfo) result.getList().get(0));
        }
        return entity;
    }

    private static List<ScanResultEntity> valueOf(Map<String, ScanResultEntity> scanPackageMap, ScanResult result) {
        HwLog.d(TAG, "scan status: result=" + result.isSuccess() + ", error=" + result.getError());
        List<ScanResultEntity> results = new ArrayList();
        if (!result.isSuccess()) {
            return results;
        }
        for (AppInfo app : result.getList()) {
            ScanResultEntity entity = (ScanResultEntity) scanPackageMap.get(app.getApkPath());
            if (entity != null) {
                results.add(parseResult(entity, app));
            }
        }
        return results;
    }

    private static ScanResultEntity parseResult(ScanResultEntity entity, AppInfo info) {
        entity.mHash = info.getMd5();
        if (ScanResultEntity.isRiskORVirus(entity)) {
            return entity;
        }
        if (info.getScore() == -1) {
            if (!ScanResultEntity.isDangerType(entity)) {
                entity.type = 302;
            }
        } else if (info.getScore() > 6) {
            if (info.getScore() == 7) {
                if (info.getVirusNameInCloud().indexOf(KEY_ADWARE) >= 0) {
                    entity.type = AntiVirusTools.TYPE_ADVERTISE;
                    entity.virusName = info.getVirusNameInCloud();
                    List<String> plugs = new ArrayList();
                    plugs.add(info.getVirusNameInCloud());
                    entity.mPlugNames = plugs;
                }
            } else if (info.getScore() >= 8) {
                entity.type = AntiVirusTools.TYPE_VIRUS;
                entity.virusName = info.getVirusNameInCloud();
            }
        }
        return entity;
    }
}

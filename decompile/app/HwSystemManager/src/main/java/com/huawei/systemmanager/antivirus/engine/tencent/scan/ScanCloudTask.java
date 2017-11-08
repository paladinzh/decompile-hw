package com.huawei.systemmanager.antivirus.engine.tencent.scan;

import android.os.AsyncTask;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.antivirus.engine.IScanPackageMgr;
import com.huawei.systemmanager.antivirus.engine.ScanPackageMgrFactory;
import com.huawei.systemmanager.antivirus.statistics.AntivirusStatsUtils;
import com.huawei.systemmanager.antivirus.statistics.VirusInfoBuilder;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;

public abstract class ScanCloudTask extends AsyncTask<Set<String>, Void, Boolean> {
    protected WeakReference<AbsPkgScanTask> mCloudScanTaskReference = null;
    protected WeakReference<Map<String, ScanResultEntity>> mScanPackageReference = null;
    private VirusInfoBuilder mTrustLookBuilder = null;

    protected abstract boolean isNeedRefreshData(Set<String> set, ScanResultEntity scanResultEntity);

    protected abstract void onCompleteWork(Boolean bool);

    public ScanCloudTask(AbsPkgScanTask cloudScanTask, Map<String, ScanResultEntity> scanPackageMap) {
        this.mCloudScanTaskReference = new WeakReference(cloudScanTask);
        this.mScanPackageReference = new WeakReference(scanPackageMap);
        this.mTrustLookBuilder = new VirusInfoBuilder(AntivirusStatsUtils.VENDOR_TRUSTLOOK);
    }

    protected Boolean doInBackground(Set<String>... whiteList) {
        AbsPkgScanTask cloudScanTask = (AbsPkgScanTask) this.mCloudScanTaskReference.get();
        if (cloudScanTask == null) {
            return Boolean.valueOf(false);
        }
        IScanPackageMgr scanMgr = ScanPackageMgrFactory.newInstance();
        Map scanPackageMap = (Map) this.mScanPackageReference.get();
        if (scanPackageMap == null) {
            return Boolean.valueOf(false);
        }
        this.mTrustLookBuilder.setCloudScanCount((long) scanPackageMap.size());
        for (ScanResultEntity entity : scanMgr.scanPackage(scanPackageMap)) {
            if (ScanResultEntity.isRiskORVirus(entity)) {
                this.mTrustLookBuilder.increaseVirusScanCount();
                AntivirusStatsUtils.reportScanVirusInfo(entity.packageName, entity.virusName, entity.mHash, AntivirusStatsUtils.VENDOR_TRUSTLOOK);
            }
            if (entity.type == 302) {
                this.mTrustLookBuilder.increaseUnKnownCount();
            }
            cloudScanTask.incProgress();
            if (isNeedRefreshData(whiteList[0], entity)) {
                cloudScanTask.handleScanResult(2, cloudScanTask.getProgress(), entity, false);
            }
        }
        AntivirusStatsUtils.reportScanCloudCount(this.mTrustLookBuilder);
        return Boolean.valueOf(true);
    }

    protected void onPostExecute(Boolean retVal) {
        onCompleteWork(retVal);
    }
}

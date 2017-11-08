package com.huawei.systemmanager.antivirus.engine.trustlook;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class CloudQuickScanAyncTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "CloudQuickScanAyncTask";
    protected List<ScanResultEntity> mCloudResults = null;
    private Context mContext;
    protected AtomicBoolean mIsFinishScan = new AtomicBoolean(false);

    protected CloudQuickScanAyncTask(Context context) {
        this.mContext = context.getApplicationContext();
    }

    protected void onCancelled() {
        super.onCancelled();
    }

    protected final Boolean doInBackground(Void... params) {
        return Boolean.valueOf(scanLocalApps());
    }

    public List<ScanResultEntity> getResult() {
        return this.mCloudResults;
    }

    public boolean isCompleted() {
        return this.mIsFinishScan.get();
    }

    private boolean scanLocalApps() {
        boolean z = false;
        PackageManager pm = this.mContext.getPackageManager();
        List<ApplicationInfo> pkgList = TrustLookUtils.queryLocalApps(this.mContext);
        Map scanPackageMap = new HashMap();
        Iterator<ApplicationInfo> iterator = pkgList.iterator();
        while (iterator.hasNext() && !isCancelled()) {
            try {
                ApplicationInfo appInfo = (ApplicationInfo) iterator.next();
                scanPackageMap.put(appInfo.sourceDir, TrustLookUtils.createScanResultEntity(PackageManagerWrapper.getPackageInfo(pm, appInfo.packageName, 0), appInfo.sourceDir, false));
            } catch (NameNotFoundException e) {
            } catch (Throwable localThrowable) {
                HwLog.e(TAG, "Scanning error", localThrowable);
                return false;
            }
        }
        if (isCancelled()) {
            return false;
        }
        this.mCloudResults = new TrustLookAntiVirusEngine().scanPackage(scanPackageMap);
        if (!isCancelled()) {
            z = true;
        }
        return z;
    }
}

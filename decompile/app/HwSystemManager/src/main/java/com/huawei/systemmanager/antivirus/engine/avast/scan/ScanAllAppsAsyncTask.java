package com.huawei.systemmanager.antivirus.engine.avast.scan;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import com.avast.android.sdk.engine.EngineInterface;
import com.avast.android.sdk.engine.ScanResultStructure;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class ScanAllAppsAsyncTask extends AsyncTask<Boolean, ScanProgress, Boolean> {
    private static final String TAG = "ScanAllAppsAsyncTask";
    protected List<ScanResultEntity> mCloudResults = null;
    private Context mContext;

    protected abstract void onPostExecute(Boolean bool);

    protected abstract void onPostScanProgressUpdate(ScanProgress scanProgress);

    protected abstract void onPreExecute();

    protected abstract void onPreScanProgressUpdate(ScanProgress scanProgress);

    protected ScanAllAppsAsyncTask(Context context) {
        this.mContext = context.getApplicationContext();
    }

    protected final void onProgressUpdate(ScanProgress... values) {
        ScanProgress localScanProgress = values[0];
        if (localScanProgress.mScanResult == null) {
            onPreScanProgressUpdate(localScanProgress);
        } else {
            onPostScanProgressUpdate(localScanProgress);
        }
    }

    protected void onCancelled() {
        super.onCancelled();
        onPostExecute(Boolean.valueOf(false));
    }

    protected final Boolean doInBackground(Boolean... params) {
        return Boolean.valueOf(scanLocalApps(params[0]));
    }

    private boolean scanLocalApps(Boolean isCloudEnable) {
        PackageManager pm = this.mContext.getPackageManager();
        List<ApplicationInfo> pkgList = filter(pm, this.mContext.getPackageName());
        int i = 0;
        List<File> filesToScan = new ArrayList();
        Map<String, PackageInfo> pkgMap = new HashMap();
        Iterator<ApplicationInfo> iterator = pkgList.iterator();
        while (iterator.hasNext() && !isCancelled()) {
            try {
                ApplicationInfo appInfo = (ApplicationInfo) iterator.next();
                PackageInfo packageInfo = PackageManagerWrapper.getPackageInfo(pm, appInfo.packageName, 0);
                publishProgress(new ScanProgress[]{new ScanProgress(pkgList.size(), i, appInfo.packageName, null)});
                File apkFile = new File(appInfo.sourceDir);
                filesToScan.add(apkFile);
                pkgMap.put(appInfo.sourceDir, packageInfo);
                List<ScanResultStructure> result = EngineInterface.scan(this.mContext, null, apkFile, packageInfo, 33);
                i++;
                publishProgress(new ScanProgress[]{new ScanProgress(pkgList.size(), i, appInfo.packageName, result)});
            } catch (NameNotFoundException e) {
            } catch (Throwable localThrowable) {
                HwLog.e(TAG, "Scanning error", localThrowable);
                return false;
            }
        }
        if (isCancelled()) {
            return false;
        }
        if (isCloudEnable.booleanValue()) {
            this.mCloudResults = CloudScanMgr.cloudScan(this.mContext, filesToScan, pkgMap, false);
        }
        return !isCancelled();
    }

    private List<ApplicationInfo> filter(PackageManager pm, String... whiteLists) {
        List<ApplicationInfo> pkgLists = pm.getInstalledApplications(0);
        Iterator<ApplicationInfo> iterator = pkgLists.iterator();
        while (iterator.hasNext()) {
            ApplicationInfo appInfo = (ApplicationInfo) iterator.next();
            if ((appInfo.flags & 1) != 0) {
                iterator.remove();
            } else if (whiteLists != null) {
                for (String str : whiteLists) {
                    if (appInfo.packageName.equals(str)) {
                        HwLog.d(TAG, "skip" + str);
                        iterator.remove();
                        break;
                    }
                }
            }
        }
        return pkgLists;
    }
}

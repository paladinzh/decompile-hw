package com.huawei.systemmanager.antivirus.engine.trustlook;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.comm.Storage.StorageHelper;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class CloudGlobalScanAyncTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "CloudQuickScanAyncTask";
    protected List<ScanResultEntity> mCloudResults = null;
    private Context mContext;
    protected AtomicBoolean mIsFinishScan = new AtomicBoolean(false);

    protected CloudGlobalScanAyncTask(Context context) {
        this.mContext = context.getApplicationContext();
    }

    protected void onCancelled() {
        super.onCancelled();
    }

    protected final Boolean doInBackground(Void... params) {
        return Boolean.valueOf(scanApks());
    }

    public boolean isCompleted() {
        return this.mIsFinishScan.get();
    }

    public List<ScanResultEntity> getResults() {
        return this.mCloudResults;
    }

    private boolean scanApks() {
        boolean z = false;
        PackageManager pm = this.mContext.getPackageManager();
        Map scanPackageMap = new HashMap();
        Iterator<ApplicationInfo> iterator = TrustLookUtils.queryLocalApps(this.mContext).iterator();
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
        List<String> rootDir = new ArrayList();
        rootDir.add(StorageHelper.getStorage().getInnerRootPath());
        rootDir.addAll(StorageHelper.getStorage().getSdcardRootPath());
        scanPackageMap.putAll(queryApks(this.mContext, rootDir));
        if (isCancelled()) {
            return false;
        }
        this.mCloudResults = new TrustLookAntiVirusEngine().scanPackage(scanPackageMap);
        if (!isCancelled()) {
            z = true;
        }
        return z;
    }

    private Map<String, ScanResultEntity> queryApks(Context context, List<String> rootDirs) {
        Stack<File> fileStacks = new Stack();
        Map<String, ScanResultEntity> scanPkgMap = new HashMap();
        String APK_POSIX = ".apk";
        for (String root : rootDirs) {
            fileStacks.add(new File(root));
        }
        while (!fileStacks.isEmpty() && !isCancelled()) {
            File file = (File) fileStacks.pop();
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File info : files) {
                        fileStacks.push(info);
                    }
                }
            } else if (file.exists()) {
                String fileName = file.getName();
                int startIndex = fileName.length() - APK_POSIX.length();
                if (startIndex > 0 && fileName.substring(startIndex).equalsIgnoreCase(APK_POSIX)) {
                    PackageInfo packInfo = context.getPackageManager().getPackageArchiveInfo(file.getAbsolutePath(), 1);
                    if (packInfo != null) {
                        scanPkgMap.put(file.getAbsolutePath(), TrustLookUtils.createScanResultEntity(packInfo, file.getAbsolutePath(), true));
                    }
                }
            }
        }
        return scanPkgMap;
    }
}

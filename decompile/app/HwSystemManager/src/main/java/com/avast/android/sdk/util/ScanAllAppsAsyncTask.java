package com.avast.android.sdk.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import com.avast.android.sdk.engine.CloudScanResultStructure;
import com.avast.android.sdk.engine.CloudScanResultStructure.CloudScanResult;
import com.avast.android.sdk.engine.EngineInterface;
import com.avast.android.sdk.engine.obfuscated.ao;
import com.avast.android.sdk.engine.obfuscated.bi;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* compiled from: Unknown */
public abstract class ScanAllAppsAsyncTask extends AsyncTask<Void, ScanProgress, Boolean> {
    private Context a;

    protected ScanAllAppsAsyncTask(Context context) {
        this.a = context.getApplicationContext();
    }

    private boolean a() {
        int i;
        boolean z = false;
        PackageManager packageManager = this.a.getPackageManager();
        List a = bi.a(packageManager, this.a.getPackageName());
        Map cloudScan = EngineInterface.cloudScan(this.a, null, a, null);
        int size = a.size();
        if (cloudScan == null) {
            i = 0;
        } else {
            Iterator it = a.iterator();
            i = 0;
            while (it.hasNext() && !isCancelled()) {
                CloudScanResultStructure cloudScanResultStructure = (CloudScanResultStructure) cloudScan.get(((ApplicationInfo) it.next()).sourceDir);
                if (cloudScanResultStructure != null) {
                    if (CloudScanResult.RESULT_OK.equals(cloudScanResultStructure.getResult()) || CloudScanResult.RESULT_SUSPICIOUS.equals(cloudScanResultStructure.getResult()) || CloudScanResult.RESULT_INFECTED.equals(cloudScanResultStructure.getResult())) {
                        it.remove();
                        i++;
                        publishProgress(new ScanProgress[]{new ScanProgress(size, i, r5.packageName, null, cloudScanResultStructure)});
                    }
                }
                i = i;
            }
        }
        Iterator it2 = a.iterator();
        while (it2.hasNext() && !isCancelled()) {
            try {
                ApplicationInfo applicationInfo = (ApplicationInfo) it2.next();
                PackageInfo packageInfo = packageManager.getPackageInfo(applicationInfo.packageName, 0);
                ao.a("scanning " + applicationInfo.sourceDir + " for viruses");
                publishProgress(new ScanProgress[]{new ScanProgress(size, i, applicationInfo.packageName, null, null)});
                List scan = EngineInterface.scan(this.a, null, new File(applicationInfo.sourceDir), packageInfo, 33);
                i++;
                publishProgress(new ScanProgress[]{new ScanProgress(size, i, applicationInfo.packageName, scan, null)});
            } catch (NameNotFoundException e) {
            } catch (Throwable th) {
                ao.d("Scanning error", th);
                return false;
            }
        }
        if (!isCancelled()) {
            z = true;
        }
        return z;
    }

    protected final Boolean doInBackground(Void... voidArr) {
        return Boolean.valueOf(a());
    }

    protected void onCancelled() {
        super.onCancelled();
        onPostExecute(Boolean.valueOf(false));
    }

    protected abstract void onPostExecute(Boolean bool);

    protected abstract void onPostScanProgressUpdate(ScanProgress scanProgress);

    protected abstract void onPreExecute();

    protected abstract void onPreScanProgressUpdate(ScanProgress scanProgress);

    protected final void onProgressUpdate(ScanProgress... scanProgressArr) {
        ScanProgress scanProgress = scanProgressArr[0];
        if (scanProgress.mScanResult == null && scanProgress.mCloudScanResult == null) {
            onPreScanProgressUpdate(scanProgress);
        } else {
            onPostScanProgressUpdate(scanProgress);
        }
    }
}

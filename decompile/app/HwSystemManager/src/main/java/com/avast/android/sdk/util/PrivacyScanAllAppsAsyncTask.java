package com.avast.android.sdk.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import com.avast.android.sdk.engine.EngineInterface;
import com.avast.android.sdk.engine.PrivacyScanResult;
import com.avast.android.sdk.engine.obfuscated.ao;
import com.avast.android.sdk.engine.obfuscated.bi;
import java.util.Iterator;

/* compiled from: Unknown */
public abstract class PrivacyScanAllAppsAsyncTask extends AsyncTask<Void, PrivacyScanProgress, Boolean> {
    private Context a;

    protected PrivacyScanAllAppsAsyncTask(Context context) {
        if (context != null) {
            this.a = context.getApplicationContext();
            return;
        }
        throw new IllegalArgumentException("Context can't be null");
    }

    private boolean a() {
        boolean z = false;
        Iterator it = bi.a(this.a.getPackageManager(), this.a.getPackageName()).iterator();
        int i = 0;
        while (it.hasNext() && !isCancelled()) {
            try {
                ApplicationInfo applicationInfo = (ApplicationInfo) it.next();
                ao.a("privacy scanning " + applicationInfo.sourceDir);
                publishProgress(new PrivacyScanProgress[]{new PrivacyScanProgress(r4.size(), i, applicationInfo.packageName, null)});
                PrivacyScanResult privacyInformation = EngineInterface.getPrivacyInformation(this.a, null, applicationInfo.packageName, null);
                i++;
                publishProgress(new PrivacyScanProgress[]{new PrivacyScanProgress(r4.size(), i, applicationInfo.packageName, privacyInformation)});
            } catch (Throwable th) {
                ao.d("Privacy scan error", th);
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

    protected abstract void onPostScanProgressUpdate(PrivacyScanProgress privacyScanProgress);

    protected abstract void onPreScanProgressUpdate(PrivacyScanProgress privacyScanProgress);

    protected final void onProgressUpdate(PrivacyScanProgress... privacyScanProgressArr) {
        PrivacyScanProgress privacyScanProgress = privacyScanProgressArr[0];
        if (privacyScanProgress.mPrivacyScanResult != null) {
            onPostScanProgressUpdate(privacyScanProgress);
        } else {
            onPreScanProgressUpdate(privacyScanProgress);
        }
    }
}

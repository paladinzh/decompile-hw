package com.avast.android.sdk.shield.appinstallshield;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import com.avast.android.sdk.engine.EngineInterface;
import com.avast.android.sdk.engine.ScanResultStructure;
import java.io.File;
import java.util.List;

/* compiled from: Unknown */
public abstract class AppInstallService extends IntentService {
    public AppInstallService() {
        super("AppInstallService");
    }

    public AppInstallService(String str) {
        super(str);
    }

    private PackageInfo a(PackageManager packageManager, String str) {
        try {
            return packageManager.getPackageInfo(str, 0);
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    private List<ScanResultStructure> a(String str) {
        PackageInfo a = a(getPackageManager(), str);
        if (a == null) {
            return null;
        }
        return EngineInterface.scan(this, null, new File(a.applicationInfo.sourceDir), a, 34);
    }

    public abstract void onAppScanFailed(Context context, String str, Bundle bundle);

    public abstract void onAppScanResult(Context context, String str, Bundle bundle, List<ScanResultStructure> list);

    protected void onHandleIntent(Intent intent) {
        Uri uri = null;
        if (intent != null) {
            uri = intent.getData();
        }
        if (uri != null) {
            Context applicationContext = getApplicationContext();
            String substring = uri.toString().substring(intent.getDataString().indexOf(":") + 1);
            onPreAppScan(applicationContext, substring, intent.getExtras());
            List a = a(substring);
            if (a == null) {
                onAppScanFailed(applicationContext, substring, intent.getExtras());
            } else {
                onAppScanResult(applicationContext, substring, intent.getExtras(), a);
            }
        }
    }

    public abstract void onPreAppScan(Context context, String str, Bundle bundle);
}

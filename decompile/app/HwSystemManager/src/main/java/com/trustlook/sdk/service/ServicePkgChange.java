package com.trustlook.sdk.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.util.Log;
import com.trustlook.sdk.Constants;
import com.trustlook.sdk.cloudscan.CloudScanClient;
import com.trustlook.sdk.cloudscan.CloudScanClient.Builder;
import com.trustlook.sdk.cloudscan.PkgUtils;
import com.trustlook.sdk.data.AppInfo;
import com.trustlook.sdk.data.DataUtils;
import com.trustlook.sdk.data.PkgInfo;
import com.trustlook.sdk.database.DBManager;
import java.util.ArrayList;
import java.util.List;

public class ServicePkgChange extends IntentService {
    CloudScanClient a;
    PkgInfo b;
    List<PkgInfo> c = new ArrayList();
    List<AppInfo> d = new ArrayList();

    public ServicePkgChange() {
        super("ServicePkgChange");
    }

    public ServicePkgChange(String str) {
        super(str);
    }

    public void onCreate() {
        super.onCreate();
        this.a = new Builder().setContext(this).setRegion(DataUtils.getRegionValue(this, 0)).setConnectionTimeout(DataUtils.getIntValue(this, Constants.CLIENT_CONNECTION_TIMEOUT, 3000)).setSocketTimeout(DataUtils.getIntValue(this, Constants.CLIENT_SOCKET_TIMEOUT, 5000)).setToken(DataUtils.getStringValue(this, Constants.CLIENT_TOKEN, "")).build();
    }

    protected void onHandleIntent(Intent intent) {
        this.b = null;
        if (intent != null) {
            String action = intent.getAction();
            this.c.clear();
            PackageInfo packageInfo;
            if (action.equals("android.intent.action.PACKAGE_ADDED")) {
                try {
                    packageInfo = PkgUtils.getPackageInfo(this, intent.getData().getEncodedSchemeSpecificPart());
                    if (packageInfo != null) {
                        this.b = PkgUtils.populatePkgInfo(packageInfo.packageName, packageInfo.applicationInfo.publicSourceDir);
                        if (this.b != null) {
                            this.c.add(this.b);
                            this.d = this.a.cloudScan(this.c).getList();
                            if (this.d != null) {
                                if (this.d.size() > 0) {
                                    DBManager.getInstance(this).getAppInfoDataSource().batchInsertAppInfoList(this.d);
                                }
                            }
                            new StringBuilder("[ServicePkgChange] package added: ").append(this.b.getPkgName());
                        }
                    }
                } catch (Exception e) {
                    Log.e(Constants.TAG, "Service Error");
                }
            } else if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                try {
                    new StringBuilder("[ServicePkgChange] package removed: ").append(intent.getData().getEncodedSchemeSpecificPart());
                } catch (Exception e2) {
                    Log.e(Constants.TAG, "Service Error");
                }
            } else if (action.equals("android.intent.action.PACKAGE_REPLACED")) {
                try {
                    packageInfo = PkgUtils.getPackageInfo(this, intent.getData().getEncodedSchemeSpecificPart());
                    if (packageInfo != null) {
                        this.b = PkgUtils.populatePkgInfo(packageInfo.packageName, packageInfo.applicationInfo.publicSourceDir);
                        new StringBuilder("[ServicePkgChange] package replaced: ").append(this.b.getPkgName());
                        if (this.b != null) {
                            this.c.add(this.b);
                            this.d = this.a.cloudScan(this.c).getList();
                            if (this.d != null && this.d.size() > 0) {
                                DBManager.getInstance(this).getAppInfoDataSource().batchInsertAppInfoList(this.d);
                            }
                        }
                    }
                } catch (Exception e3) {
                    Log.e(Constants.TAG, "Service Error");
                }
            }
        }
    }
}

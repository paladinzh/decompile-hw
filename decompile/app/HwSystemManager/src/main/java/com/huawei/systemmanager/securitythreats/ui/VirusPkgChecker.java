package com.huawei.systemmanager.securitythreats.ui;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class VirusPkgChecker {
    private static final String TAG = "VirusPkgChecker";
    private static VirusPkgChecker sInstance;
    private final Context mAppContext;
    private final Map<String, VirusPkg> mVirusMap = new VirusPkgParser(this.mAppContext).parse();

    public VirusPkgChecker(Context context) {
        this.mAppContext = context.getApplicationContext();
    }

    public static synchronized VirusPkgChecker getInstance(boolean isUiProcess) {
        VirusPkgChecker instance;
        synchronized (VirusPkgChecker.class) {
            instance = getInstance(GlobalContext.getContext(), isUiProcess, false);
        }
        return instance;
    }

    public static synchronized VirusPkgChecker getInstance(Context context, boolean isUiProcess, boolean update) {
        synchronized (VirusPkgChecker.class) {
            if (isUiProcess) {
                if (sInstance == null || update) {
                    sInstance = new VirusPkgChecker(context);
                }
                VirusPkgChecker virusPkgChecker = sInstance;
                return virusPkgChecker;
            }
            virusPkgChecker = new VirusPkgChecker(context);
            return virusPkgChecker;
        }
    }

    public List<ScanResultEntity> checkAll() {
        HwLog.i(TAG, "checkAll");
        List<ScanResultEntity> result = Lists.newArrayList();
        if (this.mVirusMap.isEmpty()) {
            HwLog.i(TAG, "checkAll mVirusMap is empty");
            return result;
        }
        for (Entry<String, VirusPkg> entry : this.mVirusMap.entrySet()) {
            String pkgName = (String) entry.getKey();
            HsmPkgInfo info = null;
            try {
                info = HsmPackageManager.getInstance().getPkgInfo(pkgName, 0);
            } catch (NameNotFoundException e) {
                HwLog.i(TAG, "checkAll NameNotFoundException ignore=" + pkgName);
            }
            if (info == null) {
                HwLog.i(TAG, "checkAll pkgName not found=" + pkgName);
            } else {
                VirusPkg virus = (VirusPkg) entry.getValue();
                boolean match = virus.match(info);
                HwLog.i(TAG, "checkAll pkgName=" + pkgName + ", match=" + match);
                if (match) {
                    result.add(new VirusScanResultEntity(info, virus.getVirus(), virus.getDescription()));
                }
            }
        }
        return result;
    }

    public ScanResultEntity checkOne(String pkgName) {
        if (!this.mVirusMap.isEmpty() && this.mVirusMap.containsKey(pkgName)) {
            HsmPkgInfo info = HsmPackageManager.getInstance().getPkgInfo(pkgName);
            VirusPkg virus = (VirusPkg) this.mVirusMap.get(pkgName);
            boolean match = virus.match(info);
            HwLog.i(TAG, "checkOne pkgName=" + pkgName + ", match=" + match);
            if (match) {
                return new VirusScanResultEntity(info, virus.getVirus(), virus.getDescription());
            }
        }
        return null;
    }
}

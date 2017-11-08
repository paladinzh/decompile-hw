package com.huawei.systemmanager.securitythreats.ui;

import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.antivirus.utils.AntiVirusTools;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import java.util.ArrayList;

public class VirusScanResultEntity extends ScanResultEntity {
    private static final long serialVersionUID = -1;

    public VirusScanResultEntity(HsmPkgInfo info, String virus, String description) {
        this.appName = info.label();
        this.packageName = info.getPackageName();
        this.virusName = virus;
        this.virusInfo = description;
        this.isUninstalledApk = info.isInstalled();
        this.mVersion = String.valueOf(info.getVersionCode());
        this.type = AntiVirusTools.TYPE_VIRUS;
        this.apkFilePath = info.getPath() + "/" + info.mFileName;
        this.mDescribtion = "";
        this.mPlugNames = new ArrayList();
    }
}

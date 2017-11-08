package com.huawei.systemmanager.antivirus.engine;

import android.text.TextUtils;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.securitythreats.ui.VirusPkgChecker;
import com.huawei.systemmanager.util.HwLog;
import tmsdk.common.module.qscanner.QScanResultEntity;

public class CustomizeVirusCheck {
    private static final String TAG = "CustomizeVirusCheck";

    public static void checkVirusByCloudConfig(QScanResultEntity result, boolean isUiProcess) {
        if (result.type != 3 && result.type != 2 && result.type != 8) {
            ScanResultEntity entity = VirusPkgChecker.getInstance(isUiProcess).checkOne(result.packageName);
            if (entity != null) {
                HwLog.i(TAG, "checkVirusByCloudConfig pkg=" + result.packageName);
                result.type = 3;
                if (!TextUtils.isEmpty(entity.virusName)) {
                    result.name = entity.virusName;
                }
                if (!TextUtils.isEmpty(entity.virusInfo)) {
                    result.discription = entity.virusInfo;
                }
            }
        }
    }
}

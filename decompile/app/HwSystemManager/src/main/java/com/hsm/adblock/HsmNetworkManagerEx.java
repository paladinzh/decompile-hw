package com.hsm.adblock;

import com.huawei.android.os.NetworkManagerEx;
import com.huawei.systemmanager.util.HwLog;
import java.util.HashMap;
import java.util.List;

public class HsmNetworkManagerEx {
    private static final String TAG = HsmNetworkManagerEx.class.getSimpleName();

    public static synchronized void setAdFilterRules(HashMap<String, List<String>> adStrategy, boolean needReset) {
        synchronized (HsmNetworkManagerEx.class) {
            try {
                NetworkManagerEx.setAdFilterRules(adStrategy, needReset);
            } catch (Exception e) {
                HwLog.e(TAG, "setAdFilterRules", e);
            } catch (Error e2) {
                HwLog.e(TAG, "setAdFilterRules", e2);
            }
        }
    }

    public static synchronized void setApkDlFilterRules(String[] pkgName, boolean needReset) {
        synchronized (HsmNetworkManagerEx.class) {
            try {
                NetworkManagerEx.setApkDlFilterRules(pkgName, needReset);
            } catch (Exception e) {
                HwLog.e(TAG, "setApkDlFilterRules", e);
            } catch (Error e2) {
                HwLog.e(TAG, "setApkDlFilterRules", e2);
            }
        }
    }

    public static synchronized void clearAdOrApkDlFilterRules(String[] pkgName, boolean needReset, int strategy) {
        synchronized (HsmNetworkManagerEx.class) {
            try {
                NetworkManagerEx.clearAdOrApkDlFilterRules(pkgName, needReset, strategy);
            } catch (Exception e) {
                HwLog.e(TAG, "clearAdOrApkDlFilterRules", e);
            } catch (Error e2) {
                HwLog.e(TAG, "clearAdOrApkDlFilterRules", e2);
            }
        }
    }

    public static synchronized void printAdOrApkDlFilterRules(int strategy) {
        synchronized (HsmNetworkManagerEx.class) {
            try {
                NetworkManagerEx.printAdOrApkDlFilterRules(strategy);
            } catch (Exception e) {
                HwLog.e(TAG, "printAdOrApkDlFilterRules", e);
            } catch (Error e2) {
                HwLog.e(TAG, "printAdOrApkDlFilterRules", e2);
            }
        }
    }

    public static synchronized void setApkDlUrlUserResult(String downloadId, boolean result) {
        synchronized (HsmNetworkManagerEx.class) {
            try {
                NetworkManagerEx.setApkDlUrlUserResult(downloadId, result);
            } catch (Exception e) {
                HwLog.e(TAG, "setApkDlUrlUserResult", e);
            } catch (Error e2) {
                HwLog.e(TAG, "setApkDlUrlUserResult", e2);
            }
        }
    }
}

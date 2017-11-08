package com.hsm.adblock;

import com.huawei.android.os.AdCleanerManagerEx;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;
import java.util.Map;

public class HsmAdCleanerManagerEx {
    private static final String TAG = HsmAdCleanerManagerEx.class.getSimpleName();

    public static synchronized int printRuleMaps() {
        synchronized (HsmAdCleanerManagerEx.class) {
            try {
                int printRuleMaps = AdCleanerManagerEx.printRuleMaps();
                return printRuleMaps;
            } catch (Exception e) {
                HwLog.e(TAG, "printRuleMaps", e);
                return 0;
            } catch (Error e2) {
                HwLog.e(TAG, "printRuleMaps", e2);
                return 0;
            }
        }
    }

    public static synchronized int cleanAdFilterRules(List<String> adAppList, boolean needReset) {
        synchronized (HsmAdCleanerManagerEx.class) {
            try {
                int cleanAdFilterRules = AdCleanerManagerEx.cleanAdFilterRules(adAppList, needReset);
                return cleanAdFilterRules;
            } catch (Exception e) {
                HwLog.e(TAG, "cleanAdFilterRules", e);
                return 0;
            } catch (Error e2) {
                HwLog.e(TAG, "cleanAdFilterRules", e2);
                return 0;
            }
        }
    }

    public static synchronized int setAdFilterRules(Map<String, List<String>> adViewMap, Map<String, List<String>> adIdMap, boolean needReset) {
        synchronized (HsmAdCleanerManagerEx.class) {
            try {
                int adFilterRules = AdCleanerManagerEx.setAdFilterRules(adViewMap, adIdMap, needReset);
                return adFilterRules;
            } catch (Exception e) {
                HwLog.e(TAG, "cleanAdFilterRules", e);
                return 0;
            } catch (Error e2) {
                HwLog.e(TAG, "cleanAdFilterRules", e2);
                return 0;
            }
        }
    }
}

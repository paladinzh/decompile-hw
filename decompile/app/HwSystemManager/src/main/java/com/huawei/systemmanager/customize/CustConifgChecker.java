package com.huawei.systemmanager.customize;

import java.util.HashMap;
import java.util.Map;

public class CustConifgChecker {
    private static final String ENABLE = "true";
    public static final int FEATURE_DISABLED = 1;
    public static final int FEATURE_ENABLED = 0;
    public static final int NO_CONFIG = -1;
    private static final Map<Integer, String> mCode2Name = new HashMap<Integer, String>() {
        private static final long serialVersionUID = 1;

        {
            put(Integer.valueOf(1), "hsm_hold_dialog");
            put(Integer.valueOf(2), "hsm_cloud_message_filter");
            put(Integer.valueOf(4), FearureConfigration.ANTIVIRUS_MANAGER);
            put(Integer.valueOf(3), FearureConfigration.PERMISSION_MANAGER);
            put(Integer.valueOf(5), FearureConfigration.AD_DETECT_MANAGER);
            put(Integer.valueOf(7), "hsm_dx_db_cloud");
            put(Integer.valueOf(8), "hsm_net_access_dialog");
            put(Integer.valueOf(20), "hsm_huawei_cloud");
            put(Integer.valueOf(9), "hsm_num_location");
            put(Integer.valueOf(23), "hsm_stat");
            put(Integer.valueOf(10), "hsm_net_support_4g");
            put(Integer.valueOf(40), "hsm_trash_tms_support");
        }
    };
    private Map<String, String> mConfig = XmlConfigParser.parseConfig();

    public int isFeatureEnabled(int featureCode) {
        if (!mCode2Name.containsKey(Integer.valueOf(featureCode))) {
            return -1;
        }
        String featureName = (String) mCode2Name.get(Integer.valueOf(featureCode));
        if (!this.mConfig.containsKey(featureName)) {
            return -1;
        }
        return "true".equals((String) this.mConfig.get(featureName)) ? 0 : 1;
    }

    public int getFeatureIntConfig(String featureName) {
        if (this.mConfig.containsKey(featureName)) {
            return Integer.parseInt((String) this.mConfig.get(featureName));
        }
        return -1;
    }
}

package com.huawei.systemmanager.rainbow.client.connect.result;

import com.google.android.collect.Maps;
import com.huawei.systemmanager.rainbow.client.base.CloudSpfKeys.CloudReqVerSpfKeys;
import com.huawei.systemmanager.rainbow.client.connect.vercfg.AbsVerConfigItem;
import com.huawei.systemmanager.rainbow.client.connect.vercfg.AppListsConfig;
import com.huawei.systemmanager.rainbow.client.connect.vercfg.AppRightsConfig;
import com.huawei.systemmanager.rainbow.client.connect.vercfg.RecommendConfig;
import java.util.Map;

public class ClientServerSync {
    private static long mIntervalTime = 259200000;
    private static Map<String, AbsVerConfigItem> mVersionMap = Maps.newHashMap();

    static {
        mVersionMap.put("right", new AppRightsConfig(CloudReqVerSpfKeys.RIGHT_LIST_VERSION_SPF));
        mVersionMap.put("v2_0005", new AppListsConfig(CloudReqVerSpfKeys.BACKGROUND_LIST_VERSION_SPF));
        mVersionMap.put("wbList_0009", new AppListsConfig(CloudReqVerSpfKeys.CONTROL_BLACK_LIST_VERSION_SPF));
        mVersionMap.put("wbList_0010", new AppListsConfig(CloudReqVerSpfKeys.CONTROL_WHITE_LIST_VERSION_SPF));
        mVersionMap.put("wbList_0011", new AppListsConfig(CloudReqVerSpfKeys.PUSH_LIST_VERSION_SPF));
        mVersionMap.put("wbList_0030", new AppListsConfig(CloudReqVerSpfKeys.PHONE_LIST_VERSION_SPF));
        mVersionMap.put("recRight", new RecommendConfig(CloudReqVerSpfKeys.RECOMMEND_RIGHTS_SPF));
        mVersionMap.put("dozeVer", new AppListsConfig(CloudReqVerSpfKeys.UNIFIED_POWER_APPS_SPF));
        mVersionMap.put("startupVer", new AppListsConfig(CloudReqVerSpfKeys.STARTUP_SPF));
        mVersionMap.put("notificationVer", new AppListsConfig(CloudReqVerSpfKeys.NOTIFICATION_SPF));
        mVersionMap.put("wbList_0034", new RecommendConfig(CloudReqVerSpfKeys.COMPETITOR_SPF));
        mVersionMap.put("messageSafe", new AppListsConfig(CloudReqVerSpfKeys.MESSAGE_SAFE_SPF));
    }

    public static void setVersionAndUrl(CheckVersionConfig config) {
        AbsVerConfigItem item = getItem(config.getVersionName());
        if (item != null) {
            config.setVerConfig(item);
        }
    }

    public static long getVersion(String fieldKey) {
        AbsVerConfigItem item = getItem(fieldKey);
        if (item != null) {
            return item.getVersion();
        }
        return 0;
    }

    public static String getUrl(String fieldKey) {
        AbsVerConfigItem item = getItem(fieldKey);
        if (item != null) {
            return item.getUrl();
        }
        return "";
    }

    public static void setIntervalTimeFromServer(long newIntervalTime) {
        mIntervalTime = newIntervalTime;
    }

    public static long getIntervalTimeFromServer() {
        return mIntervalTime;
    }

    private static AbsVerConfigItem getItem(String fieldKey) {
        return (AbsVerConfigItem) mVersionMap.get(fieldKey);
    }
}

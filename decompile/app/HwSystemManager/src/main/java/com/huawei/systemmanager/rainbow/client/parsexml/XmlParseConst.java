package com.huawei.systemmanager.rainbow.client.parsexml;

import android.text.TextUtils;
import com.huawei.systemmanager.rainbow.client.base.CloudSpfKeys.CloudReqVerSpfKeys;
import com.huawei.systemmanager.util.HwLog;
import java.util.HashMap;
import java.util.Map;

public class XmlParseConst {
    protected static final String COMMON_MAP_ADDVIEW_FILENAME = "cloud/map/addView.xml";
    protected static final String COMMON_MAP_ATTR_KEY_NAME = "name";
    protected static final String COMMON_MAP_ATTR_VALUE_NAME = "defaultValue";
    protected static final String COMMON_MAP_BOOTSTARTUP_FILENAME = "cloud/map/bootStartup.xml";
    protected static final String COMMON_MAP_GETAPPLIST_FILENAME = "cloud/map/getAppList.xml";
    protected static final String COMMON_MAP_NOTIFICATIONEX_FILENAME = "cloud/map/notificationEx.xml";
    protected static final String COMMON_MAP_NOTIFICATION_FILENAME = "cloud/map/notification.xml";
    protected static final String COMMON_MAP_TAG_NAME = "package";
    protected static final String COMMON_SIMPLE_ATTR_NAME = "name";
    protected static final String COMMON_SIMPLE_BACKGROUND_FILENAME = "cloud/simple/background.xml";
    protected static final String COMMON_SIMPLE_BACKGROUND_VERSIONNAME = "backgroundVersion";
    protected static final String COMMON_SIMPLE_COLUMN_PACKAGENAME = "packageName";
    protected static final String COMMON_SIMPLE_CONTROL_BLACK_FILENAME = "cloud/simple/control_black.xml";
    protected static final String COMMON_SIMPLE_CONTROL_BLACK_VERSIONNAME = "controlBlackVersion";
    protected static final String COMMON_SIMPLE_CONTROL_WHILE_FILENAME = "cloud/simple/control_white.xml";
    protected static final String COMMON_SIMPLE_CONTROL_WHILE_VERSIONNAME = "controlWhiteVersion";
    protected static final String COMMON_SIMPLE_PHONENUM_FILENAME = "cloud/simple/phone.xml";
    protected static final String COMMON_SIMPLE_PHONENUM_VERSIONNAME = "phoneVersion";
    protected static final String COMMON_SIMPLE_PUSH_FILENAME = "cloud/simple/push.xml";
    protected static final String COMMON_SIMPLE_PUSH_VERSIONNAME = "pushVersion";
    protected static final String COMMON_SIMPLE_TAG_NAME = "package";
    public static final String COMMON_UPDATE_FLAG = "UpdateFlag";
    public static final String TAG = "XmlParseConst";
    private static final Map<String, String> UPDATE_FLAG_MAP = new HashMap();

    static {
        UPDATE_FLAG_MAP.put(CloudReqVerSpfKeys.RIGHT_LIST_VERSION_SPF, "appsRightsVersionUpdateFlag");
        UPDATE_FLAG_MAP.put("backgroundVersion", "backgroundVersionUpdateFlag");
        UPDATE_FLAG_MAP.put("controlBlackVersion", "controlBlackVersionUpdateFlag");
        UPDATE_FLAG_MAP.put("controlWhiteVersion", "controlWhiteVersionUpdateFlag");
        UPDATE_FLAG_MAP.put("pushVersion", "pushVersionUpdateFlag");
        UPDATE_FLAG_MAP.put("phoneVersion", "phoneVersionUpdateFlag");
        UPDATE_FLAG_MAP.put(CloudReqVerSpfKeys.RECOMMEND_RIGHTS_SPF, "recommendRightsVersionUpdateFlag");
    }

    public static String getUpdateFlagForSpfKey(String spfKey) {
        if (TextUtils.isEmpty(spfKey)) {
            HwLog.e(TAG, "getUpdateFlagForSpfKey: Invalid spfKey");
            return "";
        }
        String flag = (String) UPDATE_FLAG_MAP.get(spfKey);
        if (TextUtils.isEmpty(flag)) {
            flag = spfKey + COMMON_UPDATE_FLAG;
            HwLog.w(TAG, "getUpdateFlagForSpfKey: No predefined update key for " + spfKey + ", default to " + flag);
        }
        return flag;
    }
}

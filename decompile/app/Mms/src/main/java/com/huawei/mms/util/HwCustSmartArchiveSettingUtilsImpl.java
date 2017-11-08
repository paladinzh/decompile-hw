package com.huawei.mms.util;

import android.os.SystemProperties;
import com.google.android.gms.R;
import com.huawei.mms.util.SmartArchiveSettingUtils.SmartArchiveSettingItem;
import java.util.ArrayList;

public class HwCustSmartArchiveSettingUtilsImpl extends HwCustSmartArchiveSettingUtils {
    public static final String PREF_KEY_ARCHIVE_NUM_SERVICE_MESSAGE = "archive_num_service_message";
    public static final boolean SERVICE_MESSAGE_ARCHIVAL_ENABLED = SystemProperties.getBoolean("ro.config.smart_info_message", false);
    private static final Object[][] SMART_ARCHIVE_SETTINGS_CONF_SERVICE_MESSAGE;

    static {
        Object[][] objArr = new Object[1][];
        objArr[0] = new Object[]{PREF_KEY_ARCHIVE_NUM_SERVICE_MESSAGE, Integer.valueOf(2), "[A-Za-z]{2}-[\\dA-Za-z]{6}", Integer.valueOf(R.string.smart_archive_setting_title), Integer.valueOf(R.string.archive_num_bak_summary), Boolean.valueOf(true)};
        SMART_ARCHIVE_SETTINGS_CONF_SERVICE_MESSAGE = objArr;
    }

    public ArrayList<SmartArchiveSettingItem> createDefaultSettingItemsForServiceMessage() {
        ArrayList<SmartArchiveSettingItem> lItems = new ArrayList();
        for (int i = 0; i < SMART_ARCHIVE_SETTINGS_CONF_SERVICE_MESSAGE.length; i++) {
            lItems.add(new SmartArchiveSettingItem(SMART_ARCHIVE_SETTINGS_CONF_SERVICE_MESSAGE[i][0], ((Integer) SMART_ARCHIVE_SETTINGS_CONF_SERVICE_MESSAGE[i][1]).intValue(), ((String) SMART_ARCHIVE_SETTINGS_CONF_SERVICE_MESSAGE[i][2]).split(","), ((Integer) SMART_ARCHIVE_SETTINGS_CONF_SERVICE_MESSAGE[i][3]).intValue(), ((Integer) SMART_ARCHIVE_SETTINGS_CONF_SERVICE_MESSAGE[i][4]).intValue(), ((Boolean) SMART_ARCHIVE_SETTINGS_CONF_SERVICE_MESSAGE[i][5]).booleanValue()));
        }
        return lItems;
    }

    public boolean isServiceMessageArchivalEnabled() {
        return SERVICE_MESSAGE_ARCHIVAL_ENABLED;
    }
}

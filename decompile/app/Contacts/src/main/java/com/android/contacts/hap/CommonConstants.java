package com.android.contacts.hap;

import android.net.Uri;
import android.os.SystemProperties;
import android.provider.ContactsContract;
import com.android.contacts.util.HwLog;
import java.nio.charset.Charset;

public interface CommonConstants {
    public static final Uri CALL_LOG_SEARCH_URI = Uri.parse("content://call_log/hw_calls/filter");
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    public static final boolean IS_DIALER_EMERGENCY_NO = SystemProperties.getBoolean("ro.config.hw_dlr_emgcy_no", false);
    public static final boolean IS_HW_CUSTOM_NUMBER_MATCHING_ENABLED = SystemProperties.getBoolean("ro.config.hw_caller_info", true);
    public static final boolean IS_PREDEFINED_NUMBERS_ENABLED = SystemProperties.getBoolean("ro.config.hw_enable_preset_num", true);
    public static final boolean IS_SHOW_DUAL_DIALPAD;
    public static final boolean IS_VVM_FILTER_ON = SystemProperties.getBoolean("ro.config.hw_vvm_filter_on", false);
    public static final Uri LOCATION_CONTENT_URI = Uri.parse("content://com.huawei.numberlocation.nlcontentprovider/numberlocation");
    public static final Uri LOCATION_CONTENT_URI_DSDA = Uri.parse("content://com.huawei.numberlocation/numberlocation");
    public static final boolean LOG_DEBUG = HwLog.HWDBG;
    public static final boolean LOG_INFO = HwLog.HWFLOW;
    public static final boolean LOG_VERBOSE = HwLog.HWDBG;
    public static final Uri SMART_SEARCH_URI = Uri.parse("content://com.android.contacts");
    public static final boolean isSNSSupportEnabled = SystemProperties.getBoolean("ro.config.hw_enable_sns", false);
    public static final boolean sRo_config_hw_dsda = SystemProperties.getBoolean("ro.config.hw_dsda", false);

    public static final class DatabaseConstants {
        public static final Uri DATA_USAGE_DELETE_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "contacts/delete_usage");
        public static final Uri DUPLICATE_CONTACTS = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "duplicate_contacts");
        public static final Uri RAW_CONTACTS_MIMETYPE_COUNT_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "raw_contacts/mimetypes/count");
    }

    static {
        boolean z = true;
        if (!sRo_config_hw_dsda) {
            z = false;
        }
        IS_SHOW_DUAL_DIALPAD = z;
    }
}

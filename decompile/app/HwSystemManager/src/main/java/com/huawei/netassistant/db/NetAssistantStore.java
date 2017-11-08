package com.huawei.netassistant.db;

import android.net.Uri;

public class NetAssistantStore {
    public static final int ACCESS_RESTRICT_MOBLIE_WIFI = 0;
    public static final int ACCESS_RESTRICT_NONE = 3;
    public static final int ACCESS_RESTRICT_ONLY_MOBLIE = 1;
    public static final int ACCESS_RESTRICT_ONLY_WIFI = 2;
    public static final int ADJUST_COLUMNS_ADJUST_BRAND = 8;
    public static final int ADJUST_COLUMNS_ADJUST_CITY = 6;
    public static final int ADJUST_COLUMNS_ADJUST_DATE = 4;
    public static final int ADJUST_COLUMNS_ADJUST_PROVIDER = 7;
    public static final int ADJUST_COLUMNS_ADJUST_PROVINCE = 5;
    public static final int ADJUST_COLUMNS_ADJUST_TYPE = 3;
    public static final int ADJUST_COLUMNS_ADJUST_VALUE = 2;
    public static final int ADJUST_COLUMNS_ID = 0;
    public static final int ADJUST_COLUMNS_IMSI = 1;
    public static final int ADJUST_TYPE_AUTO = 0;
    public static final int ADJUST_TYPE_MANUAL = 1;
    public static final String AUTHORITY = "com.huawei.systemmanager.NetAssistantProvider";
    public static final String CONTENT_PREFIX = "content://com.huawei.systemmanager.NetAssistantProvider/";
    public static final long DAILY_WARN_NOTIFY_DEFAULT = 10;
    public static final int EXCESS_MONTH_TYPE_DISABLE_NET = 2;
    public static final int EXCESS_MONTH_TYPE_NOTIFY = 1;
    public static final int IS_AFTER_LOCKED_NO = 0;
    public static final int IS_AFTER_LOCKED_YES = 1;
    public static final int IS_NOTIFICATION_NO = 0;
    public static final int IS_NOTIFICATION_YES = 1;
    public static final int IS_OVERMARK_DAY_NO = 0;
    public static final int IS_OVERMARK_DAY_YES = 1;
    public static final int IS_OVERMARK_MONTH_NO = 0;
    public static final int IS_OVERMARK_MONTH_YES = 1;
    public static final long MONTH_WARN_NOTIFY_DEFAULT = 80;
    public static final int NET_ACCESS_COLUMNS_ID = 0;
    public static final int NET_ACCESS_COLUMNS_PACKAGE_NAME = 1;
    public static final int NET_ACCESS_COLUMNS_PACKAGE_NETACCESS_TYPE = 4;
    public static final int NET_ACCESS_COLUMNS_PACKAGE_TRUST = 3;
    public static final int NET_ACCESS_COLUMNS_PACKAGE_UID = 2;
    public static final int REGULAR_ADJUST_TYPE_CLOSE = 0;
    public static final int REGULAR_ADJUST_TYPE_ONE_DAY = 1;
    public static final int REGULAR_ADJUST_TYPE_ONE_WEEK = 7;
    public static final int REGULAR_ADJUST_TYPE_THREE_DAY = 3;
    public static final int SETTING_COLUMNS_BEGIN_DATE = 3;
    public static final int SETTING_COLUMNS_DAILY_NOTIFY = 16;
    public static final int SETTING_COLUMNS_DAILY_NOTIFY_SNOOZE = 17;
    public static final int SETTING_COLUMNS_EXCESS_MONTH_TYPE = 6;
    public static final int SETTING_COLUMNS_ID = 0;
    public static final int SETTING_COLUMNS_IMSI = 1;
    public static final int SETTING_COLUMNS_IS_AFTER_LOCKED = 9;
    public static final int SETTING_COLUMNS_IS_NOTIFICATION = 10;
    public static final int SETTING_COLUMNS_IS_OVERMARK_DAY = 8;
    public static final int SETTING_COLUMNS_IS_OVERMARK_MONTH = 7;
    public static final int SETTING_COLUMNS_IS_SPEED_NOTIFICATION = 11;
    public static final int SETTING_COLUMNS_MONTH_LIMITE = 12;
    public static final int SETTING_COLUMNS_MONTH_LIMITE_SNOOZE = 13;
    public static final int SETTING_COLUMNS_MONTH_NOTIFY = 14;
    public static final int SETTING_COLUMNS_MONTH_NOTIFY_SNOOZE = 15;
    public static final int SETTING_COLUMNS_PACKAGE_TOTAL = 2;
    public static final int SETTING_COLUMNS_REGULAR_ADJUST_BEGIN_TIME = 5;
    public static final int SETTING_COLUMNS_REGULAR_ADJUST_TYPE = 4;
    public static final int TABLE_INDEX_NET_ACCESS_INFO = 5;
    public static final int TABLE_INDEX_NET_ACCESS_INFO_ID = 6;
    public static final int TABLE_INDEX_SETTING_INFO = 1;
    public static final int TABLE_INDEX_SETTING_INFO_BACKUP = 7;
    public static final int TABLE_INDEX_SETTING_INFO_ID = 2;
    public static final int TABLE_INDEX_TRAFFIC_ADJUST_INFO = 3;
    public static final int TABLE_INDEX_TRAFFIC_ADJUST_INFO_ID = 4;
    public static final String TABLE_NAME_NET_ACCESS_INFO = "netaccessinfo";
    public static final String TABLE_NAME_SETTING_INFO = "settinginfo";
    public static final String TABLE_NAME_SETTING_INFO_BACKUP = "settinginfo/bak";
    public static final String TABLE_NAME_TRAFFIC_ADJUST_INFO = "trafficadjustinfo";
    public static final String UNSET_CITY_OPERATER_CODE = "";
    public static final float UNSET_VALUE_FLOAT = 0.0f;
    public static final int UNSET_VALUE_INT = -1;
    public static final long UNSET_VALUE_LONG = -1;

    public static final class NetAccessTable {

        public interface Columns {
            public static final String PACKAGE_NAME = "package_name";
            public static final String PACKAGE_NETACCESS_TYPE = "net_access_type";
            public static final String PACKAGE_TRUST = "package_trust";
            public static final String PACKAGE_UID = "package_uid";
            public static final String _ID = "id";
        }

        public static Uri getContentUri() {
            return Uri.parse("content://com.huawei.systemmanager.NetAssistantProvider/netaccessinfo");
        }
    }

    public static final class SettingTable {

        public interface Columns {
            public static final String BEGIN_DATE = "begin_date";
            public static final String DAILY_WARN = "daily_warn_byte";
            public static final String DAILY_WARN_SNOOZE = "daily_limit_snooze";
            public static final String EXCESS_MONTH_TYPE = "excess_monty_type";
            public static final String IMSI = "imsi";
            public static final String IS_AFTER_LOCKED = "is_after_locked";
            public static final String IS_NOTIFICATION = "is_notification";
            public static final String IS_OVERMARK_DAY = "is_overmark_day";
            public static final String IS_OVERMARK_MONTH = "is_overmark_month";
            public static final String IS_SPEED_NOTIFICATION = "is_speed_notification";
            public static final String MONTH_LIMIT = "month_limit_byte";
            public static final String MONTH_LIMIT_SNOOZE = "month_limit_snooze";
            public static final String MONTH_WARN = "month_warn_byte";
            public static final String MONTH_WARN_SNOOZE = "month_warn_snooze";
            public static final String PACKAGE_TOTAL = "package_total";
            public static final String REGULAR_ADJUST_BEGIN_TIME = "regular_adjust_begin_time";
            public static final String REGULAR_ADJUST_TYPE = "regular_adjust_type";
            public static final String _ID = "id";
        }

        public static Uri getContentUri() {
            return Uri.parse("content://com.huawei.systemmanager.NetAssistantProvider/settinginfo");
        }
    }

    public static final class TrafficAdjustTable {

        public interface Columns {
            public static final String ADJUST_BRAND = "adjust_brand";
            public static final String ADJUST_CITY = "adjust_city";
            public static final String ADJUST_DATE = "adjust_date";
            public static final String ADJUST_PROVIDER = "adjust_provider";
            public static final String ADJUST_PROVINCE = "adjust_province";
            public static final String ADJUST_TYPE = "adjust_type";
            public static final String ADJUST_VALUE = "adjust_value";
            public static final String IMSI = "imsi";
            public static final String _ID = "id";
        }

        public static Uri getContentUri() {
            return Uri.parse("content://com.huawei.systemmanager.NetAssistantProvider/trafficadjustinfo");
        }
    }

    public static String[] getSettingColumns() {
        return new String[]{"id", "imsi", Columns.PACKAGE_TOTAL, Columns.BEGIN_DATE, Columns.REGULAR_ADJUST_TYPE, Columns.REGULAR_ADJUST_BEGIN_TIME, Columns.EXCESS_MONTH_TYPE, Columns.IS_OVERMARK_MONTH, Columns.IS_OVERMARK_DAY, Columns.IS_AFTER_LOCKED, Columns.IS_NOTIFICATION, Columns.IS_SPEED_NOTIFICATION, Columns.MONTH_LIMIT, Columns.MONTH_LIMIT_SNOOZE, Columns.MONTH_WARN, Columns.MONTH_WARN_SNOOZE, Columns.DAILY_WARN, Columns.DAILY_WARN_SNOOZE};
    }

    public static String[] getTrafficAdjustColumns() {
        return new String[]{"id", "imsi", Columns.ADJUST_VALUE, Columns.ADJUST_TYPE, Columns.ADJUST_DATE, Columns.ADJUST_PROVINCE, Columns.ADJUST_CITY, Columns.ADJUST_PROVIDER, Columns.ADJUST_BRAND};
    }

    public static String[] getNetAccessColumns() {
        return new String[]{"id", "package_name", Columns.PACKAGE_UID, Columns.PACKAGE_TRUST, Columns.PACKAGE_NETACCESS_TYPE};
    }
}

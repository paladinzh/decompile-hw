package com.huawei.systemmanager.optimize.smcs;

import android.net.Uri;
import android.text.TextUtils;

public final class SMCSDatabaseConstant {
    public static final String AD_BLOCK_TABLE = "ad_block_table";
    static final String BACKUP_PROTECTED_PKGS_TABLE = "backup_protected_pkgs_table";
    public static final String COLUMN_CONTROLL = "control";
    public static final String COLUMN_KEY_TASK = "keytask";
    public static final String COLUMN_PROTECTED = "protect";
    static final int DATABASE_VERSION = 17;
    public static final String DL_BLOCK_TABLE = "dl_block_table";
    public static final String METHOD_INIT_DEFAULT_VALUE_TABLE = "method_init_default_value_table";
    static final String PREVENT_WHITE_LIST_TABLE_BACKUP = "whitelist_backup";
    public static final String SMCS_AUTHORITY_URI = "content://smcs/";
    static final String SMCS_DATABASE_PATH_NAME = "stusagestat.db";
    static final String SQL_BEGIN_TRANSACTION = "BEGIN TRANSACTION";
    static final String SQL_COMMIT_TRANSACTION = "COMMIT TRANSACTION";
    static final String ST_BASIC_PARA_SELF_AUTO_TRIMER_TYPE = "st_basic_para_self_auto_trimer";
    static final String ST_BASIC_PARA_TABLE = "st_basic_para_table";
    static final String ST_GLOBAL_ENABLE_TABLE = "st_global_enable_table";
    static final String ST_GLOBAL_ENABLE_VALUE = "st_global_enable_value";
    static final String ST_KEY_PROCESS = "st_key_process";
    static final String ST_KEY_PROCS_TABLE = "st_key_procs_table";
    static final String ST_MEMORY_THRESHOLD = "st_memory_threshold";
    static final String ST_MEMORY_THRESHOLD_PROTECTED_LIMIT = "protected_limit";
    static final String ST_MEMORY_THRESHOLD_UPPER_LIMIT = "upper_limit";
    static final String ST_PKG_USED_OB_PERIOD = "st_pkg_used_ob_period";
    static final String ST_PKG_USED_OB_PER_OB_INDEX = "ob_period_index";
    static final String ST_PKG_USED_OB_PER_PKGNAME = "pkg_name";
    static final String ST_PKG_USED_OB_PER_USED_TIMES = "used_times";
    static final String ST_PKG_USED_STAT_PERIOD = "st_pkg_used_stat_period";
    static final String ST_PKG_USED_STAT_PERIOD_PKG_NAME = "pkg_name";
    static final String ST_PKG_USED_STAT_PERIOD_STAT_INDEX = "stat_period_index";
    static final String ST_PKG_USED_STAT_PERIOD_USED_TIME = "used_time";
    static final String ST_PROCESS_BLACKLIST_PROCESSNAME = "st_process_blacklist_processname";
    static final String ST_PROCESS_BLACKLIST_TABLE = "st_process_blacklist_table";
    static final String ST_PROCESS_RELATION_CLIENT = "st_process_relation_client";
    static final String ST_PROCESS_RELATION_SERVER = "st_process_relation_server";
    static final String ST_PROCESS_RELATION_TABLE = "st_process_relation_table";
    public static final String ST_PROTECTED_PKGS_NAME = "pkg_name";
    public static final String ST_PROTECTED_PKGS_TABLE = "st_protected_pkgs_table";
    public static final String ST_PROTECTED_PKG_CHECK = "is_checked";
    public static final String ST_PROTECTED_USR_CHANGE_FLAG = "userchanged";
    static final String ST_TIME_TABLE = "st_time_table";
    static final String ST_TIME_TABLE_NAME = "time_name";
    static final String ST_TIME_TABLE_VALUE = "time_value";
    static final String ST_USAGE_TABLE = "st_usage_table";
    static final String ST_USAGE_TABLE_LATEST_USED_TIME = "used_latest_time";
    static final String ST_USAGE_TABLE_OB_PERIOD_INDEX = "used_ob_index";
    public static final String ST_USAGE_TABLE_PKGNAME = "pkg_name";
    public static final String TABLE_DEFAULT_VALUE = "default_value_table";
    public static final Uri URI_BACKUP_END = Uri.parse("content://smcs/backup_end");
    public static final int VALUE_FALSE = 1;
    public static final int VALUE_NULL = 0;
    public static final int VALUE_TRUE = 2;
    public static final int VALUE_USR_CHANGED_NO = 0;
    public static final int VALUE_USR_CHANGED_YES = 1;
    static final String WHITE_LIST = "whitelist";
    static final String WHITE_LIST_ONE = "whitelist/#";

    public interface AdBlockColumns {
        public static final String COLUMN_DIRTY = "dirty";
        public static final String COLUMN_DL_CHECK = "dl_check";
        public static final String COLUMN_ENABLE = "enable";
        public static final String COLUMN_PKGNAME = "pkg_name";
        public static final String COLUMN_TX_URLS = "tx_urls";
        public static final String COLUMN_URLS = "urls";
        public static final String COLUMN_USER_TENCENT = "use_tencent";
        public static final String COLUMN_VERSION_CODE = "version_code";
        public static final String COLUMN_VERSION_NAME = "version_name";
        public static final String COLUMN_VIEWS = "views";
        public static final String COLUMN_VIEW_IDS = "view_ids";
    }

    public interface DlBlockColumns {
        public static final String COLUMN_DOWNLOADER_PKGNAME = "downloader_pkg_name";
        public static final String COLUMN_DOWNLOAD_APK_APPNAME = "download_apk_app_name";
        public static final String COLUMN_DOWNLOAD_APK_PKG_NAME = "download_apk_pkg_name";
        public static final String COLUMN_OPT_POLICY = "opt_policy";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_UID_PKGNAME = "uid_pkg";
    }

    public interface VirusTableConst {
        public static final String APKFILEPATH = "apk_file_path";
        public static final String APPNAME = "app_name";
        public static final String CREATE_VIRUS_APPS_TABLE = " CREATE TABLE IF NOT EXISTS virus(package_name TEXT PRIMARY KEY, app_name TEXT, type INTEGER, apk_file_path TEXT, virus_name TEXT, virus_info TEXT, plug_names TEXT, plug_url TEXT, version TEXT, scanType INTEGER, uid INTEGER);";
        public static final String CREATE_VIRUS_APPS_TABLE_INDEX_PACKAGENAME = " CREATE INDEX packagenameIndex ON virus(package_name)";
        public static final String PACKAGENAME = "package_name";
        public static final String PLUGNAMES = "plug_names";
        public static final String PLUGURL = "plug_url";
        public static final String SCANTYPE = "scanType";
        public static final String TYPE = "type";
        public static final String UID = "uid";
        public static final Uri URI = Uri.parse("content://smcs/virus");
        public static final String VERSION = "version";
        public static final String VIRUSINFO = "virus_info";
        public static final String VIRUSNAME = "virus_name";
        public static final String VIRUS_TABLE = "virus";
    }

    public static int changeStringToInt(String value) {
        if (TextUtils.isEmpty(value)) {
            return 0;
        }
        if ("true".equalsIgnoreCase(value)) {
            return 2;
        }
        if ("false".equalsIgnoreCase(value)) {
            return 1;
        }
        return 0;
    }
}

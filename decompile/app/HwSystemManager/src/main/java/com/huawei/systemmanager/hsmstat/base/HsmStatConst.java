package com.huawei.systemmanager.hsmstat.base;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.permissionmanager.db.DBAdapter;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.customize.CustomizeManager;
import com.huawei.systemmanager.util.HwLog;
import java.util.HashMap;
import java.util.Map.Entry;

public class HsmStatConst {
    public static final int ACTIVITY_ACTION_CREATE = 1;
    public static final int ACTIVITY_ACTION_DESTROY = 4;
    public static final int ACTIVITY_ACTION_PAUSE = 3;
    public static final int ACTIVITY_ACTION_RESUME = 2;
    public static final String BRAODCAST_ACTION_STAT_DIALLY = "com.huawei.systemmanager.stat.dially";
    public static final String CONTACTS_PACKAGE_NAME = "com.android.contacts";
    public static final long DATE_LAST_TIME = 72;
    public static final long DATE_MAX_LENTH = 102400;
    public static final String FIRST = "1";
    public static final String FROM_SHORT_CUT = "sc";
    public static final String HANDLE_INSTALLED_PACKAGE_SERVICE_NAME = "HandleInstalledPackageInfoService";
    public static final String KEY_ADD_BLACKLIST_FROM_CALLLOG = "3";
    public static final String KEY_ADD_BLACKLIST_FROM_CONTACT = "2";
    public static final String KEY_ADD_BLACKLIST_FROM_MANUAL = "0";
    public static final String KEY_ADD_BLACKLIST_FROM_NUM_MATCH = "1";
    public static final String KEY_ADD_BLACKLIST_FROM_SMS = "4";
    public static final String KEY_ADD_WHITELIST_FROM_BLOCK_CALLLOG = "1";
    public static final String KEY_ADD_WHITELIST_FROM_BLOCK_SMS = "0";
    public static final String KEY_ADD_WHITELIST_FROM_MANUAL = "2";
    public static final String KEY_ANTISPAM_RULE_BLACKLIST = "1";
    public static final String KEY_ANTISPAM_RULE_INTELLIGENT = "0";
    public static final String KEY_ANTISPAM_RULE_KEYWORDS = "4";
    public static final String KEY_ANTISPAM_RULE_STRANGER = "2";
    public static final String KEY_ANTISPAM_RULE_UNKNOWN = "3";
    public static final String KEY_NM_HEADSUP = "2";
    public static final String KEY_NM_HIDE_CONTENT = "5";
    public static final String KEY_NM_LOOKSCREEN = "3";
    public static final String KEY_NM_MAIN = "0";
    public static final String KEY_NM_PRIORITY_SHOW = "4";
    public static final String KEY_NM_SOUND = "6";
    public static final String KEY_NM_STATUSBAR = "1";
    public static final String KEY_NM_VIBRATE = "7";
    public static final String KEY_NOTFICATION_EVENT = "noti_event";
    public static final String KEY_PACKAGE_NAME = "package_name";
    public static final String KEY_PROCESS_ID = "process_id";
    public static final String KEY_SHOULD_STAT = "should_stat";
    public static final String KEY_TRAFFIC_DAY = "DAY";
    public static final String KEY_TRAFFIC_LIMIT = "LIMIT";
    public static final String KEY_TRAFFIC_LOC = "LOC";
    public static final boolean LOG_ENABLE = true;
    public static final String MEMORY_ACCELERATOR_LIST_CANCEL_ALL = "0";
    public static final String MEMORY_ACCELERATOR_LIST_SELECT_ALL = "1";
    public static final String NOTIFICATION_NAME = "notification";
    public static final String PARAM_COUNT = "COUNT";
    public static final String PARAM_FLAG = "FLAG";
    public static final String PARAM_FREEPERCENT = "FREEPERCENT";
    public static final String PARAM_FROM_SHORT_CUT = "PARAM_FROM_SHORTCUT";
    public static final String PARAM_ID = "ID";
    public static final String PARAM_KEY = "KEY";
    public static final String PARAM_LABEL = "LABEL";
    public static final String PARAM_LABEL2 = "LABEL2";
    public static final String PARAM_LOC = "LOC";
    public static final String PARAM_OP = "OP";
    public static final String PARAM_PKG = "PKG";
    public static final String PARAM_PKG2 = "PKG2";
    public static final String PARAM_PKGS = "PKGS";
    public static final String PARAM_RESET = "reset_launcher";
    public static final String PARAM_SUB = "SUB";
    public static final String PARAM_UNINSTALL = "uninstall_launcher";
    public static final String PARAM_VAL = "VAL";
    public static final String PARAM_VERSION = "VER";
    public static final String PHONE_SERVICE_PACKAGE_NAME = "com.huawei.phoneservice";
    public static final String POWER_SAVE_MODE = "1";
    public static final String POWER_SMART_MODE = "0";
    public static final String POWER_SUPERSAVE_MODE = "2";
    public static final String SECOND = "2";
    public static final String SETTING_PACKAGE_NAME = "com.android.settings";
    public static final String SHARE_PREFERENCE_NAME = "systemmanager";
    private static final String SHORT_NAME_COM_HUAWEI = "h";
    private static final String SHORT_NAME_COM_HUAWEI_SYSTEMMANAGER = "sm";
    public static final String SPACE_SCAN_LIST_ITEM_COLLAPSE = "0";
    public static final String SPACE_SCAN_LIST_ITEM_EXPAND = "1";
    public static final String SPACE_SCAN_STOP_BACK = "0";
    public static final String SPACE_SCAN_STOP_BUTTON = "1";
    public static final long STAT_DELAY_ONE_DAY = 86400000;
    public static final String SYSTEM_MANAGER_PACKAGE_NAME = "com.huawei.systemmanager";
    public static final String TAG = "HsmStat_info";
    public static final String TAG_EVENT = "HsmStat_event";
    public static final String THIRD = "3";
    public static final String TOOL_BOX_PACKAGE_NAME = "com.android.toolbox";
    public static final String USER_EXPERIENCE_SWITCH_NAME = "user_experience_involved";
    public static final int USER_EXPERIENCE_SWITCH_ON = 1;
    public static final String VAL_ANTISPAM_RECORD_ADD_CONTACT = "2";
    public static final String VAL_ANTISPAM_RECORD_ADD_WHITELIST = "1";
    public static final String VAL_ANTISPAM_RECORD_DEL = "3";
    public static final String VAL_ANTISPAM_RECORD_RESTORE = "0";
    public static final int VAL_CHOICE_CANCEL = 0;
    public static final int VAL_CHOICE_DL_APP_MARKET = 1;
    public static final int VAL_CHOICE_DL_ORIGIN = 2;
    public static final int VAL_CHOICE_TIME_OUT = 9;
    public static final String VAL_MOBILE = "m";
    public static final String VAL_OFF = "0";
    public static final String VAL_ON = "1";
    public static final String VAL_POWER_MODE_NORMAL = "0";
    public static final String VAL_POWER_MODE_SMART = "1";
    public static final String VAL_POWER_MODE_SUPER = "2";
    public static final String VAL_SEPERATOR = " ";
    public static final String VAL_WLAN = "w";
    private static volatile Boolean sStatEnable;

    public static class AdDetect {
        public static final String ACTION_CLICK_UNINSTALL = "cu";
        public static final String KEY_WHERE_CLICK = "w";
        public static final String NAME = "ad";
        public static final String VALUE_CLICK_IN_DETAIL = "d";
        public static final String VALUE_CLICK_IN_LIST = "l";
    }

    public static class CloudStat {
        public static final String ACTION_RECOMMEND = "cloud_rc";
        public static final String KEY_LAST_REPORT_RECOMMEND_DATE = "last_cloud_recommend_bi_date";
        public static final String NAME = "cloud";
    }

    public static class Events {
        public static final int E_ADDVIEW_SET = 111;
        public static final int E_ADDVIEW_SET_ALL = 112;
        public static final int E_ADVERTISE_ENTER = 1202;
        public static final int E_ADVERTISE_URL_BLOCKED = 1211;
        public static final int E_AD_UNSTALL = 108;
        public static final int E_AMTIMAL_RESTORE_LAUNCHER = 3803;
        public static final int E_ANTIMAL_ALERT_RESULT = 3801;
        public static final int E_ANTIMAL_BASE_INFO = 3800;
        public static final int E_ANTIMAL_UNINSTALL_LAUNCHER = 3802;
        public static final int E_ANTISPAM_ADD_BLACKLIST = 72;
        public static final int E_ANTISPAM_ADD_WHITELIST = 76;
        public static final int E_ANTISPAM_AUTO_UPDATE = 79;
        public static final int E_ANTISPAM_CHECK_BLACKLIST = 71;
        public static final int E_ANTISPAM_CHECK_CALL = 64;
        public static final int E_ANTISPAM_CHECK_MSG = 59;
        public static final int E_ANTISPAM_CHECK_WHITELIST = 75;
        public static final int E_ANTISPAM_CLEAR_CALL = 65;
        public static final int E_ANTISPAM_CLEAR_MSG = 61;
        public static final int E_ANTISPAM_DEL_BLACKLIST = 73;
        public static final int E_ANTISPAM_DEL_WHITELIST = 77;
        public static final int E_ANTISPAM_HANDLE_CALL = 67;
        public static final int E_ANTISPAM_HANDLE_MMS = 2207;
        public static final int E_ANTISPAM_HANDLE_MMS_DEL = 2209;
        public static final int E_ANTISPAM_HANDLE_MMS_RESTORE = 2208;
        public static final int E_ANTISPAM_HANDLE_MSG = 63;
        public static final int E_ANTISPAM_MANUAL_UPDATE = 78;
        public static final int E_ANTISPAM_QUERY_NUMBERMARK = 2210;
        public static final int E_ANTISPAM_REMINDER = 70;
        public static final int E_ANTISPAM_SET_RULE = 69;
        public static final int E_ANTISPAM_SWITCH = 68;
        public static final int E_ANTISPAM_UPDATE_WLAN = 80;
        public static final int E_ANTISPAM_VIEW_CALL = 66;
        public static final int E_ANTISPAM_VIEW_MMS = 2206;
        public static final int E_ANTISPAM_VIEW_MSG = 62;
        public static final int E_ANTIVIRUS_AUTO_UPDATE_SETTING = 86;
        public static final int E_ANTIVIRUS_MANUAL_UPDATE = 85;
        public static final int E_ANTIVIRUS_MODE = 84;
        public static final int E_ANTIVIRUS_RESCAN = 83;
        public static final int E_ANTIVIRUS_SCAN = 82;
        public static final int E_ANTIVIRUS_UPDATE_WLAN = 87;
        public static final int E_APPLOCK_ENTER_FROM_SYSTEMMANAGER = 2600;
        public static final int E_APPLOCK_ENTER_SETTING_CLICKED = 2607;
        public static final int E_APPLOCK_FORGET_PWD_CLICKED = 2609;
        public static final int E_APPLOCK_INIT_FINISH_PROTECT_QUESTION = 2603;
        public static final int E_APPLOCK_INIT_SKIP_PROTECT_QUESTION = 2602;
        public static final int E_APPLOCK_IS_FINGER_OR_PASSWORD = 2601;
        public static final int E_APPLOCK_MODIFY_LOCK_STATUS = 2604;
        public static final int E_APPLOCK_REOPEN_IN_APPLIST = 2606;
        public static final int E_APPLOCK_SET_GLOBAL_SWITCH = 2605;
        public static final int E_APPLOCK_SET_PROTECT_QUESTION_FINISH = 2608;
        public static final int E_APPMANAGER_CLICK_ITEM = 3200;
        public static final int E_APPMARKET_INSTALL = 1213;
        public static final int E_APP_DOWNLOAD = 1212;
        public static final int E_COMPETITOR_GUIDE_BANNER_CLICK = 3401;
        public static final int E_COMPETITOR_GUIDE_BANNER_SHOW = 3400;
        public static final int E_COMPETITOR_VIEW_LIST = 3402;
        public static final int E_COMPETITOR_VIEW_UNINSTALL = 3403;
        public static final int E_DATA_SAVER_CLOSE_CLICKED = 3602;
        public static final int E_DATA_SAVER_OPEN_CLICKED = 3601;
        public static final int E_DATA_SAVER_SHOW_PERSONAL_APP_SELECTED = 3605;
        public static final int E_DATA_SAVER_SHOW_SYSTEM_APP_SELECTED = 3604;
        public static final int E_DATA_SAVER_UNRESTRICT_APP_SELECTING_CLICKED = 3603;
        public static final int E_ENTER_PROTECTEDAPP = 1409;
        public static final int E_FROM_MAINSCREEN_TO_AD_CLICK_VIRUS_ITEM = 1203;
        public static final int E_FROM_MAINSCREEN_TO_AD_UNINSTALL_VIRUS_ITEM = 1204;
        public static final int E_HARASSMENT_ADD_BLACKLIST = 2203;
        public static final int E_HARASSMENT_ADD_WHITELIST = 2204;
        public static final int E_HARASSMENT_BLOCK_ADD_KEYWORDS = 2201;
        public static final int E_HARASSMENT_BLOCK_CALL = 2251;
        public static final int E_HARASSMENT_BLOCK_DEL_KEYWORDS = 2202;
        public static final int E_HARASSMENT_BLOCK_MSG = 2252;
        public static final int E_HARASSMENT_BLOCK_NOTIFY = 2200;
        public static final int E_HARASSMENT_CHECK_URL = 2211;
        public static final int E_HARASSMENT_CLICK_INTELL_CALL_THRESHOLD = 2246;
        public static final int E_HARASSMENT_CLICK_INTELL_HARASS_CALL = 2235;
        public static final int E_HARASSMENT_REPORT_NUMBER_MARK = 2253;
        public static final int E_HARASSMENT_RESTORE_MESSAGE = 2205;
        public static final int E_HARASSMENT_SELECT_INTELL_UPDATE = 2233;
        public static final int E_HARASSMENT_SET_BLOCK_ALL_CALL = 2238;
        public static final int E_HARASSMENT_SET_BLOCK_INTELL_ADVER_CALL = 2244;
        public static final int E_HARASSMENT_SET_BLOCK_INTELL_CALL = 2241;
        public static final int E_HARASSMENT_SET_BLOCK_INTELL_ESTATE_CALL = 2245;
        public static final int E_HARASSMENT_SET_BLOCK_INTELL_HARASS_CALL = 2242;
        public static final int E_HARASSMENT_SET_BLOCK_INTELL_MSG = 2239;
        public static final int E_HARASSMENT_SET_BLOCK_INTELL_SCAM_CALL = 2243;
        public static final int E_HARASSMENT_SET_BLOCK_STRANGER_CALL = 2236;
        public static final int E_HARASSMENT_SET_BLOCK_STRANGER_MSG = 2240;
        public static final int E_HARASSMENT_SET_BLOCK_UNKNOW_CALL = 2237;
        public static final int E_HARASSMENT_SET_DULACARD = 2234;
        public static final int E_HARASSMENT_SET_INTELL_ADVER_CALL_THRESHOLD = 2249;
        public static final int E_HARASSMENT_SET_INTELL_ESATE_CALL_THRESHOLD = 2250;
        public static final int E_HARASSMENT_SET_INTELL_HARASS_CALL_THRESHOLD = 2247;
        public static final int E_HARASSMENT_SET_INTELL_SCAM_CALL_THRESHOLD = 2248;
        public static final int E_MAINSCREEN_CLICK_FEED_BACK = 2833;
        public static final int E_MAINSCREEN_DO_OPTIMIZE_NUMBER_MARK = 2802;
        public static final int E_MAINSCREEN_DO_OPTIMZE_BLUETOOTH = 2801;
        public static final int E_MAINSCREEN_DO_OPTIMZE_CLICK_FINISH = 2820;
        public static final int E_MAINSCREEN_DO_OPTIMZE_SCROLL = 2821;
        public static final int E_MAINSCREEN_DO_OPTIMZE_SECPATCH = 2803;
        public static final int E_MAINSCREEN_DO_OPTIMZE_TRAFFIC = 2804;
        public static final int E_MAINSCREEN_DO_OPTIMZE_WIFI = 2800;
        public static final int E_MAINSCREEN_DO_OPTIMZE_WIFISEC = 2822;
        public static final int E_MAINSCREEN_FLIP_PAGE_TO_1 = 2830;
        public static final int E_MAINSCREEN_FLIP_PAGE_TO_2 = 2831;
        public static final int E_NETASSISTANT_APP_DETAIL = 2400;
        public static final int E_NETASSISTANT_APP_DETAIL_CHANGE = 2427;
        public static final int E_NETASSISTANT_CALIBRATE_NOT_RESPONCE_WTHERE_SEND_SMS = 2435;
        public static final int E_NETASSISTANT_CALIBRATE_ONERROR = 2439;
        public static final int E_NETASSISTANT_CALIBRATE_RECEIVER_SMS = 2437;
        public static final int E_NETASSISTANT_CALIBRATE_RECEIVER_TENCENT_ANALYSIS = 2438;
        public static final int E_NETASSISTANT_CALIBRATE_RESPONCE_SEND_SMS = 2436;
        public static final int E_NETASSISTANT_CHANGE_BRAND = 2422;
        public static final int E_NETASSISTANT_CHANGE_OPERATOR = 2421;
        public static final int E_NETASSISTANT_CLICK_DIFF_DAY_TRAFFIC = 2425;
        public static final int E_NETASSISTANT_DEFAULT_OPERATOR_CHANGE = 2420;
        public static final int E_NETASSISTANT_ENTER_LEISURE = 2412;
        public static final int E_NETASSISTANT_ENTER_ROAMING = 2410;
        public static final int E_NETASSISTANT_FINISH_PACKAGE_SET = 2423;
        public static final int E_NETASSISTANT_FLOW_SHOW = 2403;
        public static final int E_NETASSISTANT_FST_PACKAGE_SET_NEXT = 2419;
        public static final int E_NETASSISTANT_LEISURE_END_TIME = 2416;
        public static final int E_NETASSISTANT_LEISURE_START_TIME = 2415;
        public static final int E_NETASSISTANT_LEISURE_SWITCH = 2413;
        public static final int E_NETASSISTANT_LEISURE_VALUE = 2414;
        public static final int E_NETASSISTANT_LOCK_NOTIFY_SWITCH = 2418;
        public static final int E_NETASSISTANT_MANUAL_ADJUST = 2404;
        public static final int E_NETASSISTANT_MMS_ADJUST = 2405;
        public static final int E_NETASSISTANT_NEP_APP_SWITCH_CHANGE = 2428;
        public static final int E_NETASSISTANT_OTHER_PACKAGE_SET_NEXT = 2417;
        public static final int E_NETASSISTANT_PACKAGE_INIT = 2406;
        public static final int E_NETASSISTANT_PACKAGE_SETTINGS = 2402;
        public static final int E_NETASSISTANT_PACKAGE_UNIT = 2407;
        public static final int E_NETASSISTANT_ROAMING_APP_SWITCH_CHANGE = 2429;
        public static final int E_NETASSISTANT_SET_EXTRA = 2401;
        public static final int E_NETASSISTANT_SET_EXTRA_SIZE = 2408;
        public static final int E_NETASSISTANT_SWITCH_CARD_PAGE = 2424;
        public static final int E_NETASSISTANT_TRAFFIC_CORRECTION_FAILED = 141;
        public static final int E_NETASSISTANT_TRAFFIC_CORRECTION_SUCCESS = 140;
        public static final int E_NETASSISTANT_TRAFFIC_MONTHLY_TOTAL_RESET = 2431;
        public static final int E_NETASSISTANT_TRAFFIC_PER_DAY = 2432;
        public static final int E_NETASSISTANT_TRAFFIC_PER_MONTH = 2434;
        public static final int E_NETASSISTANT_TRAFFIC_PER_WEEK = 2433;
        public static final int E_NETASSISTANT_TRAFFIC_RANKING_PAGE_CHANGE = 2426;
        public static final int E_NETASSISTANT_TRAFFIC_RANKING_WIFI_LIST = 2430;
        public static final int E_NETASSISTANT_VALUE_ROAMING = 2411;
        public static final int E_NETASSISTANT_VALUE_SET_UNIT = 2409;
        public static final int E_NETMGR_SET = 103;
        public static final int E_NMMGR_SET = 43;
        public static final int E_ONEKEY_CLEAN_CLICK = 131;
        public static final int E_ONEKEY_CLEAN_CREATE = 130;
        public static final int E_ONEKEY_CLEAN_DELETE = 132;
        public static final int E_OPTMIZE_ADVERTISE_VIEW = 1201;
        public static final int E_OPTMIZE_AUTO_UPDATE_LIB = 1809;
        public static final int E_OPTMIZE_BACK = 9;
        public static final int E_OPTMIZE_CLEAN = 4;
        public static final int E_OPTMIZE_CLEAN_CACHE_DAILY = 1806;
        public static final int E_OPTMIZE_CLOSE_ALL = 12;
        public static final int E_OPTMIZE_CLOSE_APPS = 13;
        public static final int E_OPTMIZE_DEEP_SCANNING_CANCEL = 1805;
        public static final int E_OPTMIZE_ENTER_FROM_SYSTEMMANAGER = 1803;
        public static final int E_OPTMIZE_HOME = 10;
        public static final int E_OPTMIZE_MANUAL_UPDATE_LIB = 1808;
        public static final int E_OPTMIZE_NOTIFY_WHEN_SLOW = 15;
        public static final int E_OPTMIZE_ONE_KEY_CLEAN = 1802;
        public static final int E_OPTMIZE_ONE_KEY_CLEAN_CANCEL = 1804;
        public static final int E_OPTMIZE_REPORT_ALL_SELECT_OP = 1864;
        public static final int E_OPTMIZE_REPORT_AUTO_CLEAN_RESULT = 1817;
        public static final int E_OPTMIZE_REPORT_AUTO_CLEAN_SERVICE_TRIGGED = 1824;
        public static final int E_OPTMIZE_REPORT_AUTO_CLEAN_TASK_START = 1825;
        public static final int E_OPTMIZE_REPORT_AUTO_CLEAN_TRASH_SIZE = 1823;
        public static final int E_OPTMIZE_REPORT_DEEPITEM_CLICK_OP = 1861;
        public static final int E_OPTMIZE_REPORT_DEEP_CLEAN_TRASH_SIZE = 1816;
        public static final int E_OPTMIZE_REPORT_DEEP_ITEM_TRASH_SIZE = 1881;
        public static final int E_OPTMIZE_REPORT_DOWNLOAD_DELETE_FILE_OP = 1863;
        public static final int E_OPTMIZE_REPORT_DOWNLOAD_DETAIL_OP = 1865;
        public static final int E_OPTMIZE_REPORT_DOWNLOAD_SORT_DIALOG_CLICK_OP = 1862;
        public static final int E_OPTMIZE_REPORT_ENTER_DEEP_MANAGER = 1815;
        public static final int E_OPTMIZE_REPORT_ENTER_SPACE_MANAGER_FROM_LOW_MEM_TIPS = 1827;
        public static final int E_OPTMIZE_REPORT_EXPAND_SPACE_SCAN_ITEM_OP = 1871;
        public static final int E_OPTMIZE_REPORT_FILE_ANALYSIS_CLEAN_RESULT = 1880;
        public static final int E_OPTMIZE_REPORT_FILE_ANALYSIS_NOTIFICATION_OP = 1878;
        public static final int E_OPTMIZE_REPORT_FILE_ANALYSIS_RESULT = 1877;
        public static final int E_OPTMIZE_REPORT_FILE_ANALYSIS_SWITCH_OP = 1879;
        public static final int E_OPTMIZE_REPORT_FROM_DEEPITEM_ENTERENCE = 1819;
        public static final int E_OPTMIZE_REPORT_INTERNAL_FREE_PERCENT = 1820;
        public static final int E_OPTMIZE_REPORT_LOW_MEM_FIRST_OP = 1821;
        public static final int E_OPTMIZE_REPORT_LOW_MEM_OP = 1822;
        public static final int E_OPTMIZE_REPORT_LOW_MEM_TIPS_ENTRANCE_OP = 1826;
        public static final int E_OPTMIZE_REPORT_MAX_STORAGE_APP = 1818;
        public static final int E_OPTMIZE_REPORT_MEDIA_FILE_INFO = 1811;
        public static final int E_OPTMIZE_REPORT_MEMORY_ACCELERATOR_LIST_SELECT_ALL_OP = 1876;
        public static final int E_OPTMIZE_REPORT_ONE_KEY_CLEAN_FINISH_OP = 1875;
        public static final int E_OPTMIZE_REPORT_ONE_KEY_CLEAN_PKG_INFO = 1872;
        public static final int E_OPTMIZE_REPORT_ONE_KEY_CLEAN_TRASH = 1814;
        public static final int E_OPTMIZE_REPORT_ONE_KEY_CLEAN_TRASH_ITEM = 1873;
        public static final int E_OPTMIZE_REPORT_SPACE_MANAGER_EXPAND_LIST_DELETE = 1850;
        public static final int E_OPTMIZE_REPORT_SPACE_MANAGER_EXPAND_LIST_ITEM_PREVIEW = 1852;
        public static final int E_OPTMIZE_REPORT_SPACE_MANAGER_EXPAND_LIST_SELECTALL = 1851;
        public static final int E_OPTMIZE_REPORT_SPACE_MANAGER_LIST_DELETE = 1830;
        public static final int E_OPTMIZE_REPORT_SPACE_MANAGER_LIST_GRID_DELETE = 1840;
        public static final int E_OPTMIZE_REPORT_SPACE_MANAGER_LIST_GRID_ITEM_PREVIEW = 1842;
        public static final int E_OPTMIZE_REPORT_SPACE_MANAGER_LIST_GRID_SELECTALL = 1841;
        public static final int E_OPTMIZE_REPORT_SPACE_MANAGER_LIST_ITEM_PREVIEW = 1832;
        public static final int E_OPTMIZE_REPORT_SPACE_MANAGER_LIST_SELECTALL = 1831;
        public static final int E_OPTMIZE_REPORT_SPACE_MANAGER_TRASH_SIZE_FROM_LOW_MEM_TIPS = 1828;
        public static final int E_OPTMIZE_REPORT_SPACE_SCAN_FILE_DETAIL = 1874;
        public static final int E_OPTMIZE_REPORT_STOP_SPACE_SCAN_OP = 1870;
        public static final int E_OPTMIZE_REPORT_TRASH_SCAN_MAX_LIMIT = 1813;
        public static final int E_OPTMIZE_REPORT_TRASH_SCAN_RESULT = 1812;
        public static final int E_OPTMIZE_SCROLL = 6;
        public static final int E_OPTMIZE_STARTUPMGR_VIEW = 1007;
        public static final int E_OPTMIZE_TIMING_NOTIFY = 1807;
        public static final int E_OPTMIZE_VIEW = 5;
        public static final int E_OPTMIZE_VIRUS_VIEW = 1200;
        public static final int E_OPTMIZE_WHITLIST_FROM_SPACE_CLEAN = 1801;
        public static final int E_OPTMIZE_WHITLIST_VIEW = 1800;
        public static final int E_OPTMIZE_WIFI_ONLY_UPDATE = 1810;
        public static final int E_PERMISSIONMGR_HISTORY = 40;
        public static final int E_PERMISSIONMGR_NOTIFICATION_SETTING = 41;
        public static final int E_PERMISSIONMGR_PRIVACY_SETTING = 39;
        public static final int E_PERMISSIONMGR_RECOMMEND_SETTING = 38;
        public static final int E_PERMISSIONMGR_SET = 36;
        public static final int E_PERMISSIONMGR_TAB = 35;
        public static final int E_PERMISSIONMGR_TRUST = 37;
        public static final int E_PERMISSION_LISTITEM_CLICK = 2001;
        public static final int E_PERMISSION_RECOMMEND_CLICK = 2000;
        public static final int E_POWER_ABOUT_ULTRA_MODE = 32;
        public static final int E_POWER_APP_AUTOCLEAR_LIST = 1414;
        public static final int E_POWER_BATTERYHISTORY_CLICK_DETAIL = 1405;
        public static final int E_POWER_BATTERYHISTORY_DRAGGING_BAR = 1407;
        public static final int E_POWER_BATTERYHISTORY_ENTER_CONSUMELEVEL = 1406;
        public static final int E_POWER_BATTERY_PERCENT = 1411;
        public static final int E_POWER_BGCONSUME_CHEACK = 1424;
        public static final int E_POWER_BGCONSUME_CHEACKALL = 1423;
        public static final int E_POWER_BGCONSUME_LIST_CLICK = 1425;
        public static final int E_POWER_CLICK_SETTING = 1410;
        public static final int E_POWER_CLOSE_ALL_BKG_APPS = 25;
        public static final int E_POWER_CONSUMELEVLE_HARDWARE_CLICK = 1417;
        public static final int E_POWER_CONSUMELEVLE_SOFTWARE_CLICK = 1418;
        public static final int E_POWER_CONSUMPTION_DATA = 33;
        public static final int E_POWER_DARK_THEME_SWITCH = 1442;
        public static final int E_POWER_DETAIL_AUTO_STARTUP = 1430;
        public static final int E_POWER_DETAIL_CLOSE_CLICK = 1419;
        public static final int E_POWER_DETAIL_HIGHPOWER_REMINDER = 1428;
        public static final int E_POWER_DETAIL_INFO_CLICK = 1420;
        public static final int E_POWER_DETAIL_SCREEN_OFF_RUN = 1431;
        public static final int E_POWER_DETAIL_TIME_WAKEUP = 1429;
        public static final int E_POWER_HISTORY_DETAIL = 1400;
        public static final int E_POWER_KEEP_MOBILEDATA_ON = 30;
        public static final int E_POWER_KEEP_WLAN_ON = 29;
        public static final int E_POWER_MODE = 23;
        public static final int E_POWER_MONITOR = 18;
        public static final int E_POWER_ONEKEY_SAVE = 19;
        public static final int E_POWER_OPTIMIZE = 22;
        public static final int E_POWER_POWERMODE_SELECT = 1439;
        public static final int E_POWER_POWERMODE_SWITCH_STATUS = 1440;
        public static final int E_POWER_PROMPT = 27;
        public static final int E_POWER_PROTECT_DIALOG_ENTER = 1443;
        public static final int E_POWER_ROG_DIALOG = 1403;
        public static final int E_POWER_ROG_REPORT = 1404;
        public static final int E_POWER_ROG_SWITCH = 1402;
        public static final int E_POWER_SCREEN_ON_DURATION_RATIO = 1408;
        public static final int E_POWER_SCROLL = 20;
        public static final int E_POWER_SUPERPOWERMODE_SWITCH_STATUS = 1441;
        public static final int E_POWER_SUPERPOWER_REMIND_DIALOG = 1416;
        public static final int E_POWER_SUPERPOWER_REMIND_LIST = 1415;
        public static final int E_POWER_SUPERPOWER_SHOW_DIALOG = 1432;
        public static final int E_POWER_SUPERSVAEMODE_DIALOG_ENTER = 1437;
        public static final int E_POWER_SUPERSVAEMODE_DIALOG_REMIND = 1436;
        public static final int E_POWER_SUPERSVAEMODE_NOTIFICATION_BUTTON_ENTER = 1434;
        public static final int E_POWER_SUPER_POWERCONSUME_APP_AUTOCLEAR = 1412;
        public static final int E_POWER_SUPER_POWERCONSUME_APP_AUTOCLEAR_DIALOG = 1413;
        public static final int E_POWER_SVAEMODE_DIALOG_REMIND = 1435;
        public static final int E_POWER_SVAEMODE_NOTIFICATION_BUTTON_ENTER = 1433;
        public static final int E_POWER_SVAEMODE_NOTIFICATION_BUTTON_QUIT = 1438;
        public static final int E_POWER_ULTRA_REMINDER = 31;
        public static final int E_POWER_VIEW_BKG_APPS = 24;
        public static final int E_POWER_VIEW_CONSUMPTION_LEVEL = 26;
        public static final int E_POWER_WHOLECHECK_ADVICEITEM_CLICK = 1421;
        public static final int E_POWER_WHOLECHECK_BGCONSUME_SHOW = 1422;
        public static final int E_POWER_WHOLECHECK_CANCLE = 1426;
        public static final int E_POWER_WHOLECHECK_COMPLETE = 1427;
        public static final int E_PREVENT_ALLOW_CALLS = 55;
        public static final int E_PREVENT_ALLOW_MESSAGES = 56;
        public static final int E_PREVENT_EVENTS_AND_REMINDER = 53;
        public static final int E_PREVENT_REPEAT_CALLS = 57;
        public static final int E_PREVENT_SET_MODE = 51;
        public static final int E_PREVENT_SET_WHITELIST = 54;
        public static final int E_PREVENT_SWITCH = 50;
        public static final int E_PROTECTEDAPP_ENTER_FROM_SYSTEMMANAGER = 1401;
        public static final int E_PROTECTED_APP_SET = 115;
        public static final int E_PROTECTED_APP_SET_ALL = 116;
        public static final int E_SECURITY_PATCH_FUNC_DESC = 1600;
        public static final int E_SECURITY_PATCH_SEARCH = 1602;
        public static final int E_SECURITY_PATCH_UPDATE = 1601;
        public static final int E_STARTUPMGR_AUTO_STARTUP_ALL = 1001;
        public static final int E_STARTUPMGR_AUTO_STARTUP_PERIOD_STATISTICS = 1002;
        public static final int E_STARTUPMGR_AUTO_STARTUP_SINGLE = 1000;
        public static final int E_STARTUPMGR_AWAKED_RECORD_STARTUP_PERIOD_STATISTICS = 1009;
        public static final int E_STARTUPMGR_AWAKED_STARTUP_ALL = 1004;
        public static final int E_STARTUPMGR_AWAKED_STARTUP_PERIOD_STATISTICS = 1005;
        public static final int E_STARTUPMGR_AWAKED_STARTUP_SINGLE = 1003;
        public static final int E_STARTUPMGR_ENTER_FROM_PERMISSION = 1006;
        public static final int E_STARTUPMGR_NORMAL_RECORD_STARTUP_PERIOD_STATISTICS = 1008;
        public static final int E_TRAFFIC_CALIBRATE = 89;
        public static final int E_TRAFFIC_CALIBRATE_BY_SMS = 90;
        public static final int E_TRAFFIC_CALIBRATE_SCHEDULE = 93;
        public static final int E_TRAFFIC_LIMIT_AND_DAY = 91;
        public static final int E_TRAFFIC_LOCATION = 92;
        public static final int E_TRAFFIC_LOCKSCREEN_REMINDER = 99;
        public static final int E_TRAFFIC_MAIN_ENTER_DATA_SAVER_CLICKED = 3600;
        public static final int E_TRAFFIC_MONTH_LIMIT = 95;
        public static final int E_TRAFFIC_OVER_DAILY_MARK = 98;
        public static final int E_TRAFFIC_OVER_LIMIT_ACTION = 96;
        public static final int E_TRAFFIC_OVER_MONTHLY_MARK = 97;
        public static final int E_TRAFFIC_RANKLIST = 102;
        public static final int E_TRAFFIC_RESTRICT_BKG_DATA = 104;
        public static final int E_TRAFFIC_SHOW_SPEED = 101;
        public static final int E_TRAFFIC_SHOW_STATS = 100;
        public static final int E_TRAFFIC_WLAN_AP = 106;
        public static final int E_VIRUS_ADVERTISE_ITEM = 1215;
        public static final int E_VIRUS_ADVERTISE_SWITCH = 1214;
        public static final int E_VIRUS_CLOUD_SCAN_SWITCH = 1206;
        public static final int E_VIRUS_GLOBAL_TIMER_REMAIND = 1207;
        public static final int E_VIRUS_SCAN_COUNT = 1210;
        public static final int E_VIRUS_SCAN_INFO = 1209;
        public static final int E_VIRUS_STOP_SCAN = 1205;
        public static final int E_VIRUS_USER_UNINSTALL = 1208;
        public static final int E_WIFI_SECURE_ARP_FAKE = 3002;
        public static final int E_WIFI_SECURE_CLICK_NOTIFICATION = 3003;
        public static final int E_WIFI_SECURE_DNS_FAKE = 3000;
        public static final int E_WIFI_SECURE_PHISHING_FAKE = 3001;
    }

    public static class HarassmentFilter {
        public static final String ACTION_CLICK_HARASSMENT_NOTIFICATION = "cn_ha";
        public static final String NAME = "hf";
    }

    public static class InstallerPkgDisplay {
        public static final String ACTION_GET_INSTALLATION_INFO = "p_ipd";
        public static final String KEY_INSTALLATION_METHOD = "im";
        public static final String KEY_INSTALLED_PACKAGE_NAME = "n";
        public static final String KEY_INSTALLED_PACKAGE_VERSION_CODE = "vc";
        public static final String KEY_INSTALLED_PACKAGE_VERSION_NAME = "vn";
        public static final String KEY_PACKAGE_INSTALL_FAILED_REASON = "rsn";
        public static final String KEY_PACKAGE_INSTALL_RESULT = "r";
        public static final String KEY_PACKAGE_INSTALL_SOURCE = "s";
        public static final String KEY_PACKAGE_INSTALL_TIME = "t";
        public static final String KEY_PACKAGE_INSTALL_UPDATE = "u";
        public static final String KEY_SILENT_INSTALLATION = "si";
        public static final String NAME = "ipd";
    }

    public static class MainScreen {
        public static final String ACTION_CLICK_SCAN_BUTTON = "cs ";
        public static final String KEY_SCAN_BUTTON_STATE = "s";
        public static final String NAME = "ms";
        public static final int VALUE_SCAN_BUTTON_BACK = 2;
        public static final int VALUE_SCAN_BUTTON_OPTIMIZE = 3;
        public static final int VALUE_SCAN_BUTTON_SCAN = 0;
        public static final int VALUE_SCAN_BUTTON_STOP = 1;
    }

    public static class NetWorkMgr {
        public static final String ACTION_CLICK_NOTIFICATION_DAILY_MARK = "cn_nw_dm";
        public static final String ACTION_CLICK_NOTIFICATION_MAIN_PAGE = "cn_nw_mp";
        public static final String ACTION_CLICK_NOTIFICATION_MONTH_LIMIT = "cn_nw_ml";
        public static final String ACTION_CLICK_NOTIFICATION_MONTH_MARK = "cn_nw_mk";
        public static final String ACTION_CLICK_NOTIFICATION_SCREEN_LOCK = "cn_nw_sl";
        public static final String ACTION_NETWORK_PERMISSION_DIALOG = "wd";
        public static final String ACTION_TRAFFIC_PACKAGE = "tp";
        public static final String KEY_CARD1_TOTAL = "1t";
        public static final String KEY_CARD1_USED = "1u";
        public static final String KEY_CARD2_TOTAL = "2t";
        public static final String KEY_CARD2_USED = "2u";
        public static final String KEY_DIALOG_TYPE = "t";
        public static final String KEY_LAST_REPORT_TRAFFICT_PACKAGE = "last_traffic_package_date";
        public static final String KEY_USER_ACTION = "a";
        public static final String NAME = "nw";
        public static final String VALUE_CLICK_ALLOW_IN_DIALOG = "ad";
        public static final String VALUE_CLICK_CANCEL_IN_DIALOG = "cd";
        public static final String VALUE_DIALOG_SHOW = "d";
        public static final int VALUE_TYPE_MOBILDE_NETWORK = 1;
        public static final int VALUE_TYPE_WIFI_NETWORK = 2;
    }

    public static class Nodisturbe {
        public static final String ACTION_CLICK_NOTIFICATION = "cn_nd";
        public static final String ACTION_NO_DISCTUBE = "nd";
        public static final String KEY_USER_ACTION = "a";
        public static final String NAME = "nd";
        public static final String VALUE_USER_ACTION_CLOSE = "c";
        public static final String VALUE_USER_ACTION_OPEN = "o";
        public static final String VALUE_USER_CANCEL_IN_DIALOG = "oc";
        public static final String VALUE_USER_OPEN_IN_DIALOG = "od";
        public static final String VALUE_USER_SHOW_DIALOG = "d";
    }

    public static class Normal {
        public static final String ACTION_ENTER_ACTIVITY = "ea";
        public static final String ACTION_LEAVE_ACTIVITY = "la";
        public static final String ACTION_LEAVE_SYSTEMMANAGER = "ls";
        public static final String KEY_ENTER_FROM_OUT_SIDE = "o";
        public static final String KEY_FROM = "f";
        public static final String KEY_MODULE_NAME = "m";
        public static final String KEY_USER_ACTION = "a";
        public static final String VALUE_FROM_CONTACTS = "c";
        public static final String VALUE_FROM_DESKTOP = "l";
        public static final String VALUE_FROM_NOTIFICATION = "n";
        public static final String VALUE_FROM_PHONE_SERVICE = "ps";
        public static final String VALUE_FROM_RECENT_LIST = "r";
        public static final String VALUE_FROM_SETTING = "s";
        public static final String VALUE_FROM_SYSTEMMANAGER = "sm";
        public static final String VALUE_FROM_TOOLBOX = "t";
        private static final HashMap<String, String> sPackageMap = new HashMap(6);

        static {
            sPackageMap.put(HsmStatConst.SETTING_PACKAGE_NAME, "s");
            sPackageMap.put(HsmStatConst.TOOL_BOX_PACKAGE_NAME, "t");
            sPackageMap.put(HsmStatConst.CONTACTS_PACKAGE_NAME, "c");
            sPackageMap.put("com.huawei.systemmanager", "sm");
            sPackageMap.put(HsmStatConst.PHONE_SERVICE_PACKAGE_NAME, "ps");
        }

        public static String getFromByPackagesName(String from) {
            return (String) sPackageMap.get(from);
        }
    }

    public static class NotificationMgr {
        public static final String ACTION_CLICK_NOTIFICATION_FILTER = "cn_na";
        public static final String KEY_IF_ALLOW_NOTIFICATION = "a";
        public static final String KEY_PACKAGE_NAME = "p";
        public static final String NAME = "nm";
        public static final String VALUE_CLICK_ALLOW_NOTIFICATION = "a";
        public static final String VALUE_CLICK_FROBIDDEN_NOTIFICATION = "f";
    }

    public static class PermissionMgr {
        public static final String ACTION_PERMISSION_DIALOG = "pd";
        public static final String ACTION_PERMISSION_LIST = "pl";
        public static final String KEY_PERMISSION_SELECT = "select";
        public static final String KEY_PERMISSION_TYPE = "t";
        public static final String KEY_USER_ACTION = "a";
        public static final String NAME = "pm";
        public static final String PACKAGE_NAME = "pm";
        public static final String VALUE_CLICK_ALLOW = "a";
        public static final String VALUE_CLICK_FORBDDIEN = "f";
        public static final String VALUE_DIALOG_SHOW = "d";
    }

    public static class PhoneAccelerate {
        public static final String ACTION_CLICK_RUN_SLOW_NOTIFICATION = "cn_rs";
        public static final String NAME = "pa";
    }

    public static class PowerSavingMgr {
        public static final String ACTION_CLICK_POWER_COST_NOTIFICATION = "cn_pc";
        public static final String ACTION_ENTER_SUPER_POWER_NOTIFICATION = "cn_es";
        public static final String ACTION_SUPER_POWER_DIALOG = "ps_spd";
        public static final String KEY_ENTER_POWER = "p";
        public static final String KEY_ENTER_TIME = "t";
        public static final String KEY_IGNORE = "i";
        public static final String NAME = "ps";
        public static final String VALUE_SUPER_POWER_DIALOG_CLICK_CANCEL = "dc";
        public static final String VALUE_SUPER_POWER_DIALOG_CLICK_OPEN = "do";
        public static final String VALUE_SUPER_POWER_DIALOG_SHOW = "d";
    }

    public static class ProtectApp {
        public static final String NAME = "pr";
    }

    public static class Shortcut {
        public static final String BI_CREATE_SHORTCUT = "c";
        public static final String BI_DELETE_SHORTCUT = "d";
        public static final String NAME = "shortcut";
    }

    public static class SpaceClear {
        public static final String ACTION_CLICK_LOW_STORAGE_NOTIFICATION = "cn_ls";
        public static final String KEY_LOW_STORAGE_POSITION = "p";
        public static final String NAME = "sc";
        public static final String VALUE_LOW_STORAGE_INNER = "i";
        public static final String VALUE_LOW_STORAGE_OUTCARD = "o";
    }

    public static class VirusScanncer {
        public static final String NAME = "vs";
    }

    public static String constructValue(String... values) {
        if (values == null) {
            return "";
        }
        int length = values.length;
        if (length == 1) {
            return values[0];
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(values[i]);
            if (i % 2 == 0) {
                builder.append("=");
            } else if (i < length - 1) {
                builder.append(ConstValues.SEPARATOR_KEYWORDS_EN);
            }
        }
        return builder.toString();
    }

    public static String constructJsonParams(String... values) {
        if (values == null) {
            return "";
        }
        int length = values.length;
        if (length % 2 != 0) {
            HwLog.w(TAG, "constructParams: Invalid key-value match format");
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (int i = 0; i < length; i++) {
            builder.append(values[i]);
            if (i % 2 == 0) {
                builder.append(":");
            } else if (i < length - 1) {
                builder.append(ConstValues.SEPARATOR_KEYWORDS_EN);
            }
        }
        builder.append("}");
        return builder.toString();
    }

    public static String hashMapToJson(HashMap map) {
        if (map == null) {
            HwLog.e(TAG, "hashMapToJson,but map is null");
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("{");
        for (Entry e : map.entrySet()) {
            stringBuffer.append("'").append(e.getKey()).append("':").append("'").append(e.getValue()).append("',");
        }
        String string = stringBuffer.toString();
        return string.substring(0, string.lastIndexOf(ConstValues.SEPARATOR_KEYWORDS_EN)) + "}";
    }

    public static String constructEnterAcValue(String acName, String from, boolean fromOutSide) {
        StringBuilder builder = new StringBuilder(acName);
        if (!TextUtils.isEmpty(from)) {
            builder.append(ConstValues.SEPARATOR_KEYWORDS_EN).append("f").append("=").append(from);
        }
        if (fromOutSide) {
            builder.append(ConstValues.SEPARATOR_KEYWORDS_EN).append("o");
        }
        return builder.toString();
    }

    public static String cutActivityName(String acName) {
        return acName.replace("com.huawei.systemmanager", "sm").replace(DBAdapter.APP_HUAWEI, SHORT_NAME_COM_HUAWEI);
    }

    public static boolean isFeatureEnable() {
        Context ctx = GlobalContext.getContext();
        if (ctx == null) {
            HwLog.e(TAG, "liyuan called, but ctx is null!");
            return false;
        }
        if (sStatEnable == null) {
            boolean featureEnable = CustomizeManager.getInstance().isFeatureEnabled(ctx, 23);
            HwLog.i(TAG, "feature enable:" + featureEnable);
            sStatEnable = Boolean.valueOf(featureEnable);
        }
        return sStatEnable.booleanValue();
    }
}

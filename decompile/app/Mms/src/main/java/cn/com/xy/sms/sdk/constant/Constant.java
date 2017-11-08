package cn.com.xy.sms.sdk.constant;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.WindowManager;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.sdk.util.f;
import java.io.File;

/* compiled from: Unknown */
public class Constant {
    public static final int ACTION_CALL = 2;
    public static final int ACTION_COPY_CODE = 8;
    public static final int ACTION_DATE_REMIND = 10;
    public static final int ACTION_EXPRESS_FLOW = 13;
    public static final int ACTION_FLIGHT_TREND = 14;
    public static final int ACTION_INSTALMENT_PLAN = 15;
    public static final int ACTION_LIVE_SERVER = 18;
    public static final int ACTION_MAP = 4;
    public static final int ACTION_NEAR_SITE = 17;
    public static final int ACTION_OPEN_APP = 9;
    public static final int ACTION_OTHER = 7;
    public static final String ACTION_PARSE = "-2";
    public static final int ACTION_PAY_CHONGZHI = 5;
    public static final int ACTION_PAY_HUANKUAN = 6;
    public static final int ACTION_SMS = 1;
    public static final int ACTION_TRAFFIC_ORDER = 12;
    public static final int ACTION_TRAIN_STATION = 16;
    public static final int ACTION_URL = 3;
    public static final int ACTION_WATER_GAS = 11;
    public static final String AESKEY = "AESKEY";
    public static final String ALGORITHM_VERSION_FILE = "duoqu_parse_version.txt";
    public static final String APPVERSION = "APPVERSION";
    public static final String AREA_CODE = "areaCode";
    public static final String AUTO_UPDATE_DATA = "AUTO_UPDATE_DATA";
    public static final String BEFORE_HAND_PARSE_SMS_TIME = "BEFORE_HAND_PARSE_SMS_TIME";
    public static final String CHANNEL = "CHANNEL";
    public static final String COMPARE_PUBNUM_OPERATOR = "COMPARE_PUBNUM_OPERATOR";
    public static final String CONFIG_NOTIFY_TIMEMS = "CONFIG_NOTIFY_TIMEMS";
    public static final String CUSTOM_LOCATION_SERVER_URL = "CUSTOM_LOCATION_SERVER_URL";
    public static final String CUSTOM_LOGO_DOWN_URL = "CUSTOM_LOGO_DOWN_URL";
    public static final String CUSTOM_PUBINFO_SERVER_URL = "CUSTOM_PUBINFO_SERVER_URL";
    public static final String CUSTOM_PUBLIC_SERVER_URL = "CUSTOM_PUBLIC_SERVER_URL";
    public static final String CUSTOM_SDK_RES_DOWNLAOD_URL = "CUSTOM_SDK_RES_DOWNLAOD_URL";
    public static final String CUSTOM_SDK_SERVER_URL = "CUSTOM_SDK_SERVER_URL";
    public static final long DAY = 86400000;
    public static final String DRAWABLE_VERSION = "DrawableVersion";
    public static final String DRAWABLE_VERSION_FILE = "duoqu_drawable_version.txt";
    public static String DRAWBLE_PATH = null;
    public static final String DUOQU_EMBED_NUMBER_DIR = "duoqu_embed_number";
    public static final String DUOQU_PUBLIC_LOGO_DIR = "duoqu_publiclogo";
    public static final String Delimiter = "__ARR__";
    public static final String EMPTY_JSON = "{}";
    public static final String FAVORITE_TYPE_SMS = "1";
    public static String FILE_PATH = null;
    public static final String FIND_CMD_STATUS = "200";
    public static final long FIVE_MINUTES = 300000;
    public static final long FirstpostqueryIccidScene = 1209600000;
    public static final String HAS_IMPORT_DRAWABLE_DATA = "hasImportDrawableData";
    public static final long HOUR = 3600000;
    public static final String HTTPTOKEN = "HTTPTOKEN";
    public static String INITNQSQL_PATH = null;
    public static final String INIT_MAIN_PROCCESS = "INIT_MAIN_PROCCESS";
    public static final String ISCLEANOLDTOKEN = "isCleanOldToken";
    public static final String IS_FAVORITE = "is_favorite";
    public static final String IS_MARK = "1";
    public static final String KEY_ALLOW_PERSONAL_MSG = "ALLOW_PERSONAL_MSG";
    public static final String KEY_ALLOW_VERCODE_MSG = "ALLOW_VERCODE_MSG";
    public static final String KEY_HW_PARSE_TIME = "hwParseTime";
    public static final String KEY_IS_SAFE_VERIFY_CODE = "isSafeVerifyCode";
    public static final String KEY_SDK_BATCH_PHONE_NULL_PUB_ID_UPDATE_TIME = "batch_null_pubid_update_lasttime";
    public static final String KEY_SDK_BATCH_PHONE_PUB_ID_UPDATE_TIME = "batch_pubid_update_lasttime";
    public static final String KEY_SDK_PUB_ID_UP_TIME = "pubid_update_lasttime";
    public static final String KEY_SDK_PUB_UP_TIME = "LastPublicUpdate";
    public static final String LOACTION = "LOACTION";
    public static final String LOACTION_LATITUDE = "lat";
    public static final String LOACTION_LONGITUDE = "lng";
    public static final String LOACTION_TIME = "locationTime";
    public static final String MENU_SHOW_TIMES = "show";
    public static final String MENU_VERSION = "MenuVersion";
    public static final String MENU_VERSION_FILE = "duoqu_nqsql_version.txt";
    public static final long MINUTE = 60000;
    public static final long NETQUERYPRIO = 604800000;
    public static final long NET_MAX_SIZE = 26214400;
    public static final String NEWHTTPTOKEN = "NEWHTTPTOKEN";
    public static final String ONLINE_UPDATE_RES_PERIOD = "ONLINE_UPDATE_RES_PERIOD";
    public static final String ONLINE_UPDATE_SDK = "ONLINE_UPDATE_SDK";
    public static final String ONLINE_UPDATE_SDK_PERIOD = "ONLINE_UPDATE_SDK_PERIOD";
    public static final String OPEN_POPUP_DRAG = "OPEN_POPUP_DRAG";
    public static String PARSE_PATH = null;
    public static final String PATTERN = "yyyy-MM-dd";
    public static final String POPUP_BG_TYPE = "POPUP_BG_TYPE";
    public static final String POPUP_SHOW_BANK = "POPUP_SHOW_BANK";
    public static final String POPUP_SHOW_LIFE = "POPUP_SHOW_LIFE";
    public static final String POPUP_SHOW_MASTER = "POPUP_SHOW_MASTER";
    public static final String POPUP_SHOW_SP = "POPUP_SHOW_SP";
    public static final byte POWER_COMPANY_INFO = (byte) 5;
    public static final byte POWER_MENU = (byte) 4;
    public static final byte POWER_SMS_CLASSIFY = (byte) 6;
    public static final byte POWER_SMS_ENTERPRISE = (byte) 7;
    public static final byte POWER_SMS_SPECIAL_VALUE = (byte) 12;
    public static final byte POWER_TOBUBBLE = (byte) 2;
    public static final byte POWER_TOBUBBLE_VIEW = (byte) 9;
    public static final byte POWER_TOMAP = (byte) 1;
    public static final byte POWER_TOSIMPLEBUBBLE_VIEW = (byte) 11;
    public static final byte POWER_TOWINDOW = (byte) 3;
    public static final byte POWER_TO_REMIND = (byte) 10;
    public static final byte POWER_TO_YUNCARD = (byte) 8;
    public static final String PRELOADENABLE = "PRELOADENABLE";
    public static final String PUBIDC_ENHANCE = "pubid__enhance";
    public static final String PUBLIC_LOGO_VERSION = "PublicLogoVersion";
    public static final String PUBLIC_LOGO_VERSION_FILE = "duoqu_publiclogo_version.txt";
    public static final String QUERY_ONLINE = "QUERY_ONLINE";
    public static final String RECOGNIZE_LEVEL = "RECOGNIZE_LEVEL";
    public static final String RECORD_FUNCTION_STATE = "RECORD_FUNCTION_STATE";
    public static final String REPARSE_BUBBLE_CYCLE = "REPARSE_BUBBLE_CYCLE";
    public static final int RETRY_TIME = 2;
    public static final String RSAPRVKEY = "RSAPRVKEY";
    public static final String SCENE_CENSUS_ONLINE = "SCENE_CENSUS_ONLINE";
    public static final String SECRETKEY = "SECRETKEY";
    public static final int SERVICE_TYPE_DISABLE = 0;
    public static final int SERVICE_TYPE_ENABLE = 1;
    public static final String SIM_ICCID = "SIM_ICCID";
    public static final String SMARTSMS_ENHANCE = "smartsms_enhance";
    public static final String SMART_ALGORITHM_PVER = "SMART_ALGORITHM_PVER";
    public static final String SMART_DATA_UPDATE_TIME = "SMART_DATA_UPDATE_TIME";
    public static final String SMSLOCATEENABLE = "SMSLOCATEENABLE";
    public static final String SMS_LOCATE = "SMS_LOCATE";
    public static final int SMS_TYPE_CALL = 2;
    public static final int SMS_TYPE_CODE = 1;
    public static final String SUPPORT_NETWORK_TYPE = "SUPPORT_NETWORK_TYPE";
    public static final String SUPPORT_NETWORK_TYPE_MAJOR = "SUPPORT_NETWORK_TYPE_MAJOR";
    public static final long SceneRuleUpdatePrio = 1209600000;
    public static final String TAG = "XIAOYUAN";
    public static final int TYPE_ACTION = 3;
    public static final int TYPE_IDENTIFY = 1;
    public static final int TYPE_SHOW = 2;
    public static boolean Test = false;
    public static final String UNIQUE_CODE = "UNIQUE_CODE";
    public static final String URLS = "url";
    private static int a = 0;
    public static final long addTaskForTime = 600000;
    public static final long addVersionChangeTime = 600000;
    private static int b = 0;
    public static final String bubble_version = "20151014";
    public static LruCache<String, Long> checkCodeIccidMap = new LruCache(100);
    public static LruCache<String, Long> checkJarMap = new LruCache(100);
    public static Context context = null;
    public static String current_bubble_version = "";
    public static final long emergencyTimeCyc = 600000;
    public static final long jarsubFileUpdatePrio = 86400000;
    public static final int jieruma = 1;
    public static long lastEmergencyUpdateTime = 0;
    public static long lastSqlUpdateTime = 0;
    public static final long lastTimeUpdateTime = 0;
    public static long lastVersionChangeTime = 0;
    public static final long month = 2592000000L;
    public static final int phone = 2;
    public static boolean popupDefault = false;
    public static final long postqueryIccidScene = 5184000000L;
    public static final long sceneConfigupdatePrio = 1209600000;
    public static final String sdk_version = "201412291953";
    public static final long sqlUpdateTimeCyc = 1800000;
    public static final String suanfa_version = "20150202";
    public static final long week = 604800000;
    public static final long weekTime = 172800000;

    public static Context getContext() {
        return context;
    }

    public static String getDRAWBLE_PATH() {
        if (DRAWBLE_PATH == null) {
            DRAWBLE_PATH = getPath("drawable");
        }
        return DRAWBLE_PATH;
    }

    public static String getFilePath() {
        if (FILE_PATH == null && getContext() != null) {
            FILE_PATH = new StringBuilder(String.valueOf(getContext().getFilesDir().getPath())).append(File.separator).toString();
        }
        return FILE_PATH;
    }

    public static int getHeight(Context context) {
        if (a == 0) {
            intiDisplayScreen(context);
        }
        return a;
    }

    public static String getINITSQL_PATH() {
        if (INITNQSQL_PATH == null) {
            INITNQSQL_PATH = getPath("nqsql");
        }
        return INITNQSQL_PATH;
    }

    public static String getInidb_PATH() {
        return getPARSE_PATH() + "init.sql";
    }

    public static String getJarPath() {
        return f.d(getPARSE_PATH(), "parseUtilMain_", "jar");
    }

    public static String getNQSQL_PATH() {
        return getINITSQL_PATH() + "menu.sql";
    }

    public static String getPARSE_PATH() {
        return getPath("parse");
    }

    public static String getPath(String str) {
        String str2 = "";
        try {
            str2 = new StringBuilder(String.valueOf(getContext().getFilesDir().getPath())).append(File.separator).append(str).append(File.separator).toString();
            File file = new File(str2);
            if (!file.exists() || file.isFile()) {
                file.mkdirs();
            }
        } catch (Throwable th) {
        }
        return str2;
    }

    public static String getTempPARSE_PATH() {
        return getPath("parse_temp");
    }

    public static int getWidth(Context context) {
        if (b == 0) {
            intiDisplayScreen(context);
        }
        return b;
    }

    public static String getXCode3() {
        return "3737364635393330";
    }

    public static void initContext(Context context) {
        Context applicationContext = context.getApplicationContext();
        context = applicationContext;
        if (applicationContext == null) {
            context = context;
        }
    }

    public static void intiDisplayScreen(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService("window");
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        if (context.getResources().getConfiguration().orientation != 2) {
            a = displayMetrics.heightPixels;
            b = displayMetrics.widthPixels;
            return;
        }
        a = displayMetrics.widthPixels;
        b = displayMetrics.heightPixels;
    }

    public static boolean isDefaultImageExist() {
        return ViewUtil.getChannelType() != 1 ? (ViewUtil.getChannelType() == 2 || ViewUtil.getChannelType() == 8) ? f.a(new StringBuilder(String.valueOf(getDRAWBLE_PATH())).append("1_6aba2f9cf8a52365e40c404ecc89e52d.png").toString()) && f.a(getDRAWBLE_PATH() + "1_ec3910a4ef4a64e2f8c78c9a80eef68d.png") : ViewUtil.getChannelType() != 5 ? ViewUtil.getChannelType() != 3 ? f.a(new StringBuilder(String.valueOf(getDRAWBLE_PATH())).append("1_2c6aa6ee55d09f274f312f9701982947.png").toString()) && f.a(getDRAWBLE_PATH() + "1_4a43b040c5ce163d5db96bdde9724fd5.png") : f.a(new StringBuilder(String.valueOf(getDRAWBLE_PATH())).append("1_01dfb9f94b6ab802656e16619b0e0e6f.png").toString()) && f.a(getDRAWBLE_PATH() + "1_430b2237f18c4c793d4e025abe5dcd4b.png") : f.a(getDRAWBLE_PATH() + "1_f40a881c456538d330ed67afa042134a.png") : f.a(new StringBuilder(String.valueOf(getDRAWBLE_PATH())).append("1_d19e64f3c2c90fc61ad6eb8b0a214aff.png").toString()) && f.a(getDRAWBLE_PATH() + "1_6099165f4f4f01d911252818cc0e851c.png");
    }
}

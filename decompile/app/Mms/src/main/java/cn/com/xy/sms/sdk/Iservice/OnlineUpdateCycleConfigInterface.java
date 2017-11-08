package cn.com.xy.sms.sdk.Iservice;

/* compiled from: Unknown */
public interface OnlineUpdateCycleConfigInterface {
    public static final int CONFIG_URL_REFERER = 4;
    public static final int CONFIG_WEB_TOP_MENU = 2;
    public static final int CONFIG_WEIXIN_INFO = 3;
    public static final int ON_OFF_MENU_SCENE_REPORT = 0;
    public static final int ON_OFF_SCENE_REPORT = 1;
    public static final int TYPE_BATCH_PUBLIC_NUM_INFO_UPDATE_CYCLE = 1;
    public static final int TYPE_CALLS_NUMBER_INFO_EFFECTIVE_CYCLE = 39;
    public static final int TYPE_CENTER_NUM_LOCATION_INFO_CHECK_CYCLE = 0;
    public static final int TYPE_CHECK_INIT_CYCLE = 7;
    public static final int TYPE_DELETE_INVALID_NET_QUERY_NUM_CYCLE = 40;
    public static final int TYPE_EXIST_PUBLIC_NUM_INFO_UPDATE_CYCLE = 2;
    public static final int TYPE_FIND_MENU_LIST_CYCLE = 5;
    public static final int TYPE_FIRST_POST_ICCID_SCENE_COUNT_CYCLE = 12;
    public static final int TYPE_HISTORY_SHARD_DELETE_CYCLE = 35;
    public static final int TYPE_JAR_FILE_UPDATE_CYCLE = 8;
    public static final int TYPE_JAR_UPDATE_CYCLE = 6;
    public static final int TYPE_LAST_LOAD_TIME_ADD = 18;
    public static final int TYPE_LOGO_RESOURSE_UPDATE_CYCLE = 9;
    public static final int TYPE_MATCHID_TIME_OUT_CYCLE = 32;
    public static final int TYPE_NET_ERROR_REQUERY_PUBINFO_CYCLE = 41;
    public static final int TYPE_OTA_EMBED_NUMBER_CYCLE = 42;
    public static final int TYPE_PARSE_FAIL_REPARSE_CYCLE = 23;
    public static final int TYPE_PHONE_SERVICE_CYCLE = 43;
    public static final int TYPE_PHONE_SERVICE_CYCLE_NODATA = 44;
    public static final int TYPE_POST_ICCID_SCENE_COUNT_CYCLE = 13;
    public static final int TYPE_PUBLIC_NUM_INFO_UPDATE_CYCLE = 3;
    public static final int TYPE_QUERY_BATCH_PHONE_NULL_PUB_ID_UPDATE_CYCLE = 31;
    public static final int TYPE_QUERY_BATCH_PHONE_PUB_ID_UPDATE_CYCLE = 30;
    public static final int TYPE_QUERY_CALLS_NUMBER_INFO_CYCLE = 38;
    public static final int TYPE_QUERY_CONTACTS_CYCLE = 25;
    public static final int TYPE_QUERY_OPERATOR_INFO_CYCLE = 27;
    public static final int TYPE_QUERY_OPERATOR_MSG_INFO_CYCLE = 26;
    public static final int TYPE_QUERY_PUB_ID_CYCLE = 29;
    public static final int TYPE_QUERY_PUB_INFO_CYCLE = 24;
    public static final int TYPE_REDOWNLOAD_LOGO_INTERVAL = 19;
    public static final int TYPE_REDOWNLOAD_RESOURSE_CYCLE = 17;
    public static final int TYPE_RELOAD_ICCID_LOCATE_CYCLE = 16;
    public static final int TYPE_REPARSE_BUBBLE_CYCLE = 14;
    public static final int TYPE_REPARSE_CYCLE = 22;
    public static final int TYPE_REPARSE_SIMPLE_CYCLE = 15;
    public static final int TYPE_REQUERY_ICCID_INFO_CYCLE = 4;
    public static final int TYPE_RUN_RESOURSE_QUEUE_CYCLE = 20;
    public static final int TYPE_SCENE_CONFIG_UPDATE_CYCLE = 10;
    public static final int TYPE_SCENE_RULE_UPDATE_CYCLE = 11;
    public static final int TYPE_SIGN_QUERY_CYCLE = 34;
    public static final int TYPE_TRAIN_DATA_CYCLE = 37;
    public static final int TYPE_TRAIN_DATA_QUERY_CYCLE = 36;
    public static final int TYPE_TRAIN_DATA_VALID_CYCLE = 21;
    public static final int TYPE_UPDATE_LAST_PARSE_TIME_CYCLE = 33;
    public static final int TYPE_URL_VAILD_CYCLE = 28;

    long getUpdateCycle(int i, long j);
}

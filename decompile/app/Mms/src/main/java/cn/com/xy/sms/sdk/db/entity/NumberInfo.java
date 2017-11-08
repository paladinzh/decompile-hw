package cn.com.xy.sms.sdk.db.entity;

/* compiled from: Unknown */
public class NumberInfo {
    public static final String AMOUNT_KEY = "amount";
    public static final String AREA_CODE_KEY = "areaCode";
    public static final String AUTH_KEY = "auth";
    public static final String KEY_AMOUNT = "amount";
    public static final String KEY_TAG = "tag";
    public static final String LOCAL_LOGO_KEY = "logo_exist";
    public static final String LOGO_KEY = "logo";
    public static final String LOGO_NAME_KEY = "logoName";
    public static final String NAME_KEY = "name";
    public static final String NUM_KEY = "pubnum";
    public static final String NUM_TYPE_KEY = "numtype";
    public static final String SERVER_URL_KEY = "serverUrl";
    public static final String SOURCE_KEY = "src";
    public static final String TAG_KEY = "tag";
    public static final String TYPE_KEY = "type";
    public static final int UPLOAD_STATUS_NO_UPLOAD = 0;
    public static final int UPLOAD_STATUS_UPLOADED = 1;
    public static final String USER_TAG_KEY = "userTag";
    public static final String USER_TAG_TYPE_KEY = "userTagType";
    public static final String USER_TAG_UPLOAD_STATUS_KEY = "u";
    public static final String VERSION_KEY = "version";
    public int id;
    public long lastQueryTime = 0;
    public String num;
    public String result;
    public String version;

    public NumberInfo(int i, String str, String str2, String str3, long j) {
        this.id = i;
        this.num = str;
        this.result = str2;
        this.version = str3;
        this.lastQueryTime = j;
    }
}

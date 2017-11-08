package cn.com.xy.sms.sdk.util;

/* compiled from: Unknown */
public class CallBackState {
    public static final int CURRENT_THREAD_HAS_DATA = 1;
    public static final int CURRENT_THREAD_NEED_DOWNLOAD = -3;
    public static final int CURRENT_THREAD_NEED_QUERY = -2;
    public static final int CURRENT_THREAD_NO_DATA = -1;
    public static final int CURRENT_THREAD_REPEAT_REQUEST = -5;
    public static final int CURRENT_THREAD_SCROLLING = -4;
    public static final int DOWNLOAD_COMPLETE = 5;
    public static final int DOWNLOAD_FAILED = -6;
    public static final int ERROR = -10;
    public static final int HAS_DATA = 2;
    public static final int NEED_QUERY = -7;
    public static final int NO_DATA = -8;
    public static final int REQUEST_SUCCESS = 3;
    public static final int UNKNOWN = -9;
    public static final int UPLOAD_COMPLETE = 4;
}

package cn.com.xy.sms.sdk.action;

import android.app.Activity;
import android.os.Handler;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/* compiled from: Unknown */
public class NearbyPoint {
    public static final int DO_GET_LOCATION = 4101;
    public static final int GET_QUERY_URL_FAILURE = 4099;
    public static final int QUERY_PARAM_ERROR = 4098;
    public static final int QUERY_REQUEST_ERROR = 4100;
    public static final String QUERY_RESULT = "queryResult";
    public static final int QUERY_RESULT_RECEIVE = 4097;
    private Activity a;
    private e b = null;
    private Handler c;
    private double d;
    private double e;
    private String f;
    private int g = 0;

    public NearbyPoint(Activity activity, Handler handler) {
        this.c = handler;
    }

    private String a(String str, double d, double d2, int i, int i2) {
        return a("6a0ddfcfdf1a1e7a1f38501fc5d218bf", str, d, d2, i, "json", 2, i2);
    }

    private static String a(String str, String str2, double d, double d2, int i, String str3, int i2, int i3) {
        if (str.length() == 32 && str2 != null) {
            if ((d < 0.0d ? 1 : null) == null) {
                if ((d2 < 0.0d ? 1 : null) == null && i > 0) {
                    if (str3.equalsIgnoreCase("json") || str3.equalsIgnoreCase("xml")) {
                        StringBuffer stringBuffer = new StringBuffer();
                        stringBuffer.append("http://android.bizport.cn:9998/AndroidWeb/getPlaceListAPI?");
                        stringBuffer.append("query=");
                        try {
                            stringBuffer.append(URLEncoder.encode(str2, "UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                        }
                        stringBuffer.append("&lat=");
                        stringBuffer.append(d);
                        stringBuffer.append("&lng=");
                        stringBuffer.append(d2);
                        stringBuffer.append("&radius=");
                        stringBuffer.append(i);
                        stringBuffer.append("&scope=");
                        stringBuffer.append(2);
                        stringBuffer.append("&page_num=");
                        stringBuffer.append(i3);
                        stringBuffer.append("&output=");
                        stringBuffer.append(str3);
                        return stringBuffer.toString();
                    }
                }
            }
        }
        return null;
    }

    public double getLocationLatitude() {
        return this.d;
    }

    public double getLocationLongitude() {
        return this.e;
    }

    public void sendMapQueryUrl(String str, double d, double d2, int i) {
        if (this.b != null) {
            this.b.isInterrupted();
            this.b = null;
        }
        this.f = str;
        this.d = d;
        this.e = d2;
        this.g = i;
        this.b = new e();
        this.b.start();
    }
}

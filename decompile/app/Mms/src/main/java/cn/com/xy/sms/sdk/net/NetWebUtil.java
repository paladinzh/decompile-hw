package cn.com.xy.sms.sdk.net;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.a.a;
import cn.com.xy.sms.sdk.net.util.j;
import cn.com.xy.sms.sdk.util.KeyManager;
import cn.com.xy.sms.sdk.util.f;
import com.amap.api.maps.model.WeightedLatLng;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

/* compiled from: Unknown */
public class NetWebUtil {
    public static String WEBACTIVITY_URL = "http://x.bizport.cn/get_url_red";
    public static String WEB_SERVER_URL = ("http://android" + ((int) ((Math.random() * 2.0d) + WeightedLatLng.DEFAULT_INTENSITY)) + ".bizport.cn:9998/AndroidWeb/");
    public static String WEB_SERVER_URL2 = ("http://android" + ((int) ((Math.random() * 2.0d) + WeightedLatLng.DEFAULT_INTENSITY)) + ".bizport.cn:9998/AndroidWeb/kbAction");
    public static String WEB_SERVER_URL_COMMING_MOVIE = "https://service.bizport.cn/commerce/movie/sdk/getMovieComming";
    public static String WEB_SERVER_URL_DISCOVER = "http://android.bizport.cn:9998/AndroidWeb/findservice";
    public static String WEB_SERVER_URL_FLIGHT = "http://android.bizport.cn:9998/AndroidWeb/flightAction";
    public static String WEB_SERVER_URL_MOVIE_POSTERS = "https://service.bizport.cn/commerce/movie/sdk/posterpath";

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String b(String str, String str2, XyCallBack xyCallBack) {
        PrintWriter printWriter;
        InputStream inputStream;
        Throwable th;
        PrintWriter printWriter2;
        InputStream inputStream2 = null;
        String str3 = "";
        try {
            KeyManager.initAppKey();
            URLConnection openConnection = !str.toLowerCase().startsWith("https") ? new URL(str).openConnection() : b.b(str);
            openConnection.setConnectTimeout(a.timeoutConnection);
            openConnection.setReadTimeout(a.readTimeout);
            openConnection.addRequestProperty("sdkversion", NetUtil.APPVERSION);
            openConnection.setRequestProperty("accept", "*/*");
            openConnection.setRequestProperty("connection", "Keep-Alive");
            openConnection.setRequestProperty("app_key", l.b);
            openConnection.addRequestProperty("p", a.getDeviceId(true));
            openConnection.addRequestProperty("x", j.b());
            openConnection.setDoOutput(true);
            openConnection.setDoInput(true);
            printWriter = new PrintWriter(openConnection.getOutputStream());
            try {
                printWriter.print(str2);
                printWriter.flush();
                inputStream = openConnection.getInputStream();
            } catch (Throwable th2) {
                th = th2;
                if (inputStream2 != null) {
                    inputStream2.close();
                }
                if (printWriter != null) {
                    printWriter.close();
                }
                throw th;
            }
            try {
                String str4 = new String(f.b(inputStream), "UTF-8");
                xyCallBack.execute("0", str4);
                if (inputStream != null) {
                    inputStream.close();
                }
                try {
                    printWriter.close();
                } catch (Throwable th3) {
                }
                return str4;
            } catch (Throwable th4) {
                th = th4;
                inputStream2 = inputStream;
                if (inputStream2 != null) {
                    inputStream2.close();
                }
                if (printWriter != null) {
                    printWriter.close();
                }
                throw th;
            }
        } catch (Throwable th5) {
            th = th5;
            printWriter = null;
            if (inputStream2 != null) {
                inputStream2.close();
            }
            if (printWriter != null) {
                printWriter.close();
            }
            throw th;
        }
    }

    public static void executeRunnable(Runnable runnable) {
        a.h.execute(runnable);
    }

    public static void sendPostRequest(String str, String str2, XyCallBack xyCallBack) {
        a.h.execute(new j(str, str2, xyCallBack));
    }
}

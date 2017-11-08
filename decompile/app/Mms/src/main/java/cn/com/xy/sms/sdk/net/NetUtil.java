package cn.com.xy.sms.sdk.net;

import android.text.TextUtils;
import android.util.LruCache;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.a.a;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.IccidInfo;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.net.util.j;
import cn.com.xy.sms.sdk.util.KeyManager;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import com.amap.api.maps.model.WeightedLatLng;
import com.autonavi.amap.mapcore.VTMCDataCache;
import java.util.Map;

/* compiled from: Unknown */
public class NetUtil {
    public static String APPVERSION = "201701171515";
    public static String BIZPORT_DOWN_URL = "http://down2.bizport.cn/publicnum/upload/";
    public static final String CheckResourseRequest = "checkResourseRequest";
    public static final String GET_PHONE_MENU = "phonemenu";
    public static final int HTTP_ACCESS_FALIE = -8;
    public static final int HTTP_CONN_OUTTIME = -6;
    public static final int HTTP_NO_BODYDATA = -5;
    public static final int HTTP_PACKAGE_TO_BIG = -9;
    public static final int HTTP_SSL_EXCEPTION = -12;
    public static final int HTTP_THROWS_EXCEPTION = -7;
    public static String POPUP_SERVER_URL_HTTPS = ("https://sdkapi" + ((int) ((Math.random() * 10.0d) + WeightedLatLng.DEFAULT_INTENSITY)) + ".bizport.cn:8943/popupservice/");
    public static String PUBINFO_SERVER_URL_HTTPS = ("https://pubapi" + ((int) ((Math.random() * 10.0d) + WeightedLatLng.DEFAULT_INTENSITY)) + ".bizport.cn:9443/pubNumService/");
    public static final String QuerySceneRequest = "queryscene";
    public static final String REQ_NUM_MARK = "phonemark";
    public static final String REQ_QUERY_CHECI = "checi";
    public static final String REQ_QUERY_LOCATION = "location";
    public static final String REQ_QUERY_MENUINFO = "menuinfo";
    public static final String REQ_QUERY_NUM = "phonenum";
    public static final String REQ_QUERY_OPERATOR = "opinfo";
    public static final String REQ_QUERY_OPERATOR_MSG = "opanalysis";
    public static final String REQ_QUERY_PUBINFO = "pubinfo";
    public static final String REQ_QUERY_TOEKN = "token";
    public static final String REQ_QUERY_UPGRADE = "phoneupgrade";
    public static String STATSERVICE_URL = ("http://scene" + ((int) ((Math.random() * 10.0d) + WeightedLatLng.DEFAULT_INTENSITY)) + ".bizport.cn:8981/statservice/stat/");
    public static final String URL_LOG_SERVICE = "logserver";
    public static final String URL_MENU_CLICKED = "menuclick";
    public static final String URL_PUB_NUMBER = "pubnumber";
    public static final String URL_VALIDITY = "URLValidity";
    public static final String UpdatePublicInfoRequest = "updatepublic";
    public static final String UpdateRecognitionJarRequest = "updatejar";
    private static String a;
    private static int b = 0;
    private static final Object c = new Object();
    private static String d = null;
    private static final LruCache<String, Long> e = new LruCache(VTMCDataCache.MAXSIZE);
    private static long f = 0;
    private static int g = 0;
    public static String prex = "http://down1.bizport.cn";
    public static String publicInfoServerUrl = ("http://olapi" + ((int) ((Math.random() * 10.0d) + WeightedLatLng.DEFAULT_INTENSITY)) + ".bizport.cn/");
    public static String serverUrl = ("http://smssdk" + ((int) ((Math.random() * 10.0d) + WeightedLatLng.DEFAULT_INTENSITY)) + ".bizport.cn/popupservice/api/");
    public static String serverUrl2 = ("http://pubserver" + ((int) ((Math.random() * 10.0d) + WeightedLatLng.DEFAULT_INTENSITY)) + ".bizport.cn:9998/pubNumService/");

    public static void QueryTokenRequest(String str) {
        try {
            XyCallBack fVar = new f();
            String a = j.a(str);
            if (!StringUtils.isNull(a)) {
                executeAllNetHttpRequest(a, "990005", fVar, false, true, REQ_QUERY_TOEKN, false);
            }
        } catch (Throwable th) {
        }
    }

    private static boolean a() {
        return !(((f + Constant.FIVE_MINUTES) > System.currentTimeMillis() ? 1 : ((f + Constant.FIVE_MINUTES) == System.currentTimeMillis() ? 0 : -1)) <= 0);
    }

    private static boolean a(Map<String, String> map) {
        if (d == null) {
            synchronized (c) {
                if (d == null) {
                    syncRequestNewToken(map);
                    d = getToken();
                }
            }
        }
        return !StringUtils.isNull(d);
    }

    public static boolean addLastServiceRequestTiem(String str, long j) {
        synchronized (e) {
            Long l = (Long) e.get(str);
            if (l != null) {
                if (l.longValue() + j >= System.currentTimeMillis()) {
                    return false;
                }
            }
            e.put(str, Long.valueOf(System.currentTimeMillis()));
            return true;
        }
    }

    private static void b(String str, String str2, XyCallBack xyCallBack, boolean z, boolean z2, boolean z3, Map<String, String> map) {
        Runnable newXyHttpRunnable = new NewXyHttpRunnable(str, str2, xyCallBack, z2, z3, map);
        if (z) {
            executeRunnable(newXyHttpRunnable);
        } else {
            newXyHttpRunnable.run();
        }
    }

    private static boolean b(Map<String, String> map) {
        String token = getToken();
        queryNewTokenRequest(map);
        String token2 = getToken();
        return (StringUtils.isNull(token2) || token2.equals(token)) ? false : true;
    }

    public static boolean checkAccessNetWork() {
        return checkAccessNetWork(2);
    }

    public static boolean checkAccessNetWork(int i) {
        int intParam = SysParamEntityManager.getIntParam(Constant.getContext(), Constant.SUPPORT_NETWORK_TYPE);
        if (intParam == 0) {
            return false;
        }
        intParam = i != 2 ? XyUtil.checkNetWork(Constant.getContext(), 1) : intParam != 2 ? XyUtil.checkNetWork(Constant.getContext(), 1) : XyUtil.checkNetWork(Constant.getContext(), 2);
        return intParam == 0;
    }

    public static boolean checkAccessNetWork(Map<String, String> map) {
        try {
            int checkNetWork = XyUtil.checkNetWork(Constant.getContext());
            if (checkNetWork == -1) {
                return false;
            }
            if (map == null || map.isEmpty()) {
                return checkAccessNetWork(2);
            }
            String str = (String) map.get(Constant.SUPPORT_NETWORK_TYPE);
            int intValue = StringUtils.isNull(str) ? 1 : Integer.valueOf(str).intValue();
            if (intValue != 0) {
                if (checkNetWork == 1) {
                    if (intValue != 1) {
                    }
                }
                return true;
            }
            return false;
        } catch (Throwable th) {
        }
    }

    public static void executeAllNetHttpRequest(String str, String str2, XyCallBack xyCallBack, boolean z, boolean z2, String str3, boolean z3) {
        if (isEnhance()) {
            String stringParam = SysParamEntityManager.getStringParam(Constant.getContext(), Constant.CUSTOM_PUBLIC_SERVER_URL);
            if (str3 == null) {
                str3 = "";
            }
            Runnable lVar = new l(!StringUtils.isNull(stringParam) ? new StringBuilder(String.valueOf(stringParam)).append(str3).append("/").toString() : getPubNumServiceUrl() + str3 + "/", str, "", str2, z2, xyCallBack, Boolean.valueOf(z3));
            if (z) {
                executeRunnable(lVar);
            } else {
                lVar.run();
            }
        }
    }

    public static void executeHttpPublicRequest(String str, String str2, XyCallBack xyCallBack, String str3, Map<String, String> map) {
        executeRunnable(new l(str3, str2, null, "990005", false, xyCallBack, Boolean.valueOf(true)));
    }

    public static void executeHttpRequest(int i, int i2, String str, XyCallBack xyCallBack, String str2, boolean z) {
        Runnable kVar = new k(i2, str2, str, xyCallBack, true);
        if (z) {
            executeRunnable(kVar);
        } else {
            kVar.run();
        }
    }

    public static void executeHttpRequest(int i, String str, XyCallBack xyCallBack, String str2, Map<String, String> map, boolean z) {
        Runnable kVar = new k(-1, str2, str, xyCallBack, true);
        if (z) {
            executeRunnable(kVar);
        } else {
            kVar.run();
        }
    }

    public static void executeHttpRequest(String str, String str2, XyCallBack xyCallBack) {
        if (StringUtils.isNull(str)) {
            throw new Exception("request url is null...");
        } else if (!checkAccessNetWork()) {
            XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-10), "No NetWork!");
        } else if (TextUtils.isEmpty(getToken())) {
            queryNewTokenRequest(null, true, new h(str, str2, xyCallBack));
        } else {
            b(str, str2, xyCallBack, true, false, false, null);
        }
    }

    public static void executeLoginBeforeHttpRequest(String str, String str2, XyCallBack xyCallBack, String str3, boolean z) {
        executeRunnable(new a(str3, null, str, false, str2, xyCallBack, z));
    }

    public static void executeNewServiceHttpRequest(String str, String str2, XyCallBack xyCallBack, boolean z, boolean z2, boolean z3, Map<String, String> map) {
        String stringParam = SysParamEntityManager.getStringParam(Constant.getContext(), Constant.CUSTOM_PUBINFO_SERVER_URL);
        if (str == null) {
            str = "";
        }
        Runnable newXyHttpRunnable = new NewXyHttpRunnable(!StringUtils.isNull(stringParam) ? new StringBuilder(String.valueOf(stringParam)).append(str).toString() : getPublicInfoServiceUrl() + str, str2, xyCallBack, z2, z3, map);
        if (z) {
            executeRunnable(newXyHttpRunnable);
        } else {
            newXyHttpRunnable.run();
        }
    }

    public static void executePubNumServiceHttpRequest(String str, String str2, XyCallBack xyCallBack, String str3, boolean z, boolean z2, String str4, boolean z3) {
        String stringParam = SysParamEntityManager.getStringParam(Constant.getContext(), Constant.CUSTOM_PUBLIC_SERVER_URL);
        if (str4 == null) {
            str4 = "";
        }
        Runnable lVar = new l(!StringUtils.isNull(stringParam) ? new StringBuilder(String.valueOf(stringParam)).append(str4).append("/").toString() : getPubNumServiceUrl() + str4 + "/", str, "", str2, z2, xyCallBack, Boolean.valueOf(z3));
        if (z) {
            a.b.execute(lVar);
        } else {
            lVar.run();
        }
    }

    public static void executeRunnable(Runnable runnable) {
        a.d.execute(runnable);
    }

    public static void executeServiceHttpRequest(String str, String str2, Map<String, String> map, XyCallBack xyCallBack) {
        try {
            if (StringUtils.isNull(str2)) {
                throw new Exception("reqeustContent == null");
            } else if (!isEnhance()) {
                throw new Exception("enhance == false");
            } else if (a(map)) {
                executeNewServiceHttpRequest(str, str2, new i(str, str2, map, getToken(), xyCallBack), false, false, false, map);
            } else {
                throw new Exception("AESkey == null");
            }
        } catch (Throwable th) {
            XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-10), th.getMessage());
        }
    }

    public static String getPopupServiceUrl() {
        return !isUseHttps() ? serverUrl : POPUP_SERVER_URL_HTTPS;
    }

    public static String getPubNumServiceUrl() {
        return !isUseHttps() ? serverUrl2 : PUBINFO_SERVER_URL_HTTPS;
    }

    public static String getPublicInfoServiceUrl() {
        return publicInfoServerUrl;
    }

    public static String getToken() {
        return SysParamEntityManager.getStringParam(Constant.getContext(), Constant.NEWHTTPTOKEN);
    }

    public static String getUrlWithPara(String str) {
        return str;
    }

    public static boolean hasNewToken(Map<String, String> map) {
        requestNewTokenIfNeed(map);
        return !StringUtils.isNull(getToken());
    }

    public static boolean isEnhance() {
        boolean booleanParam = SysParamEntityManager.getBooleanParam(Constant.getContext(), "smartsms_enhance", true);
        if (!booleanParam) {
            return booleanParam;
        }
        String str = KeyManager.channel;
        return ("VMhlWdEwVNEW_LENOVO".equals(str) || "1w36SBLwVNEW_ZTE".equals(str)) ? checkAccessNetWork(2) : booleanParam;
    }

    public static boolean isUseHttps() {
        if (b != 0) {
            return b == 1;
        } else {
            try {
                KeyManager.initAppKey();
                String[] strArr = new String[]{SmartSmsSdkUtil.DUOQU_SDK_CHANNEL, "3GdfMSKwHUAWEI", "5Mj22a4wHUAWEICARD", "J8KeTyOROASamsungReminder", "SAMBANKVwIDAQAB", "SAMCLASSFIYVwIDAQAB", "5xKI47wSAMALL", "XYTEST"};
                for (int i = 0; i < 8; i++) {
                    if (strArr[i].equals(KeyManager.channel)) {
                        b = 1;
                        return true;
                    }
                }
            } catch (Throwable th) {
            }
            b = 2;
            return false;
        }
    }

    public static void queryNewTokenRequest(Map<String, String> map) {
        queryNewTokenRequest(map, false, null);
    }

    public static void queryNewTokenRequest(Map<String, String> map, boolean z, XyCallBack xyCallBack) {
        try {
            XyCallBack gVar = new g(xyCallBack);
            String a = j.a();
            if (!StringUtils.isNull(a)) {
                executeNewServiceHttpRequest(REQ_QUERY_TOEKN, a, gVar, z, true, false, map);
            }
        } catch (Throwable th) {
        }
    }

    public static void requestNewTokenAndPostRequestAgain(String str, String str2, long j, boolean z, boolean z2, boolean z3, Map<String, String> map, String str3, XyCallBack xyCallBack) {
        syncRequestNewToken(map);
        String token = getToken();
        if (StringUtils.isNull(token) || token.equals(str3) || !addLastServiceRequestTiem(str2, j)) {
            XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-9999), "server error");
            return;
        }
        try {
            executeNewServiceHttpRequest(str, str2, xyCallBack, z, z2, z3, map);
        } catch (Throwable th) {
            XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-9999), "error:" + th.getMessage());
        }
    }

    public static void requestNewTokenIfNeed(Map<String, String> map) {
        if (StringUtils.isNull(getToken())) {
            queryNewTokenRequest(map);
        }
    }

    public static void requestTokenIfNeed(String str) {
        if (StringUtils.isNull(SysParamEntityManager.getStringParam(Constant.getContext(), Constant.HTTPTOKEN))) {
            if (StringUtils.isNull(str)) {
                IccidInfo queryDeftIccidInfo = IccidInfoManager.queryDeftIccidInfo(Constant.getContext());
                if (queryDeftIccidInfo != null) {
                    str = queryDeftIccidInfo.iccid;
                }
            }
            QueryTokenRequest(str);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean syncRequestNewToken(Map<String, String> map) {
        boolean z = true;
        if (a()) {
            return false;
        }
        try {
            synchronized (c) {
                if (a()) {
                    return true;
                }
                String token = getToken();
                queryNewTokenRequest(map);
                String token2 = getToken();
                if (!StringUtils.isNull(token2)) {
                    if (token2.equals(token)) {
                    }
                    g++;
                    if (z || g >= 3) {
                        f = System.currentTimeMillis();
                        g = 0;
                    }
                }
                z = false;
                g++;
                if (z) {
                }
                f = System.currentTimeMillis();
                g = 0;
            }
        } catch (Throwable th) {
            return false;
        }
    }
}

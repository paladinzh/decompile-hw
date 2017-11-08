package tmsdk.common;

import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.text.TextUtils;
import java.util.HashMap;
import java.util.Map;
import tmsdk.bg.creator.ManagerCreatorB;
import tmsdk.bg.module.network.TrafficCorrectionManager;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.utils.d;
import tmsdkobf.dc;
import tmsdkobf.fw;
import tmsdkobf.hz;
import tmsdkobf.ik;
import tmsdkobf.jn;
import tmsdkobf.jq;
import tmsdkobf.jw;
import tmsdkobf.ku;
import tmsdkobf.ly;
import tmsdkobf.ma;
import tmsdkobf.mg;
import tmsdkobf.mh;
import tmsdkobf.nc;
import tmsdkobf.pd;
import tmsdkobf.pg;
import tmsdkobf.py;

/* compiled from: Unknown */
public final class TMSDKContext {
    public static final String BUFFALO_LIBNAME = "buffalo_name";
    public static final String CON_APP_BUILD_TYPE = "app_build_type";
    public static final String CON_ATHENA_NAME = "athena_name";
    public static final String CON_AUTO_REPORT = "auto_report";
    public static final String CON_BOA_LIBNAME = "boa_libname";
    public static final String CON_BUILD = "build";
    public static final String CON_CHANNEL = "channel";
    public static final String CON_CVERSION = "cversion";
    public static final String CON_HOST_URL = "host_url";
    public static final String CON_HOTFIX = "hotfix";
    public static final String CON_IS_TEST = "is_t";
    public static final String CON_LC = "lc";
    public static final String CON_LOGIN_HOST_URL = "login_host_url";
    public static final String CON_PKGKEY = "pkgkey";
    public static final String CON_PLATFORM = "platform";
    public static final String CON_PRE_LIB_PATH = "pre_lib_path";
    public static final String CON_PRODUCT = "product";
    public static final String CON_PVERSION = "pversion";
    public static final String CON_ROOT_CHANGE_ACTION = "root_change_action";
    public static final String CON_ROOT_DAEMON_START_ACTION = "root_daemon_start_action";
    public static final String CON_ROOT_GOT_ACTION = "root_got_action";
    public static final String CON_SDK_LIBNAME = "sdk_libname";
    public static final String CON_SOFTVERSION = "softversion";
    public static final String CON_SUB_PLATFORM = "sub_platform";
    public static final String CON_SU_CMD = "su_cmd";
    public static final String CON_VIRUS_SCAN_LIBNAME = "virus_scan_libname";
    public static final String FAKE_BS_LIBNAME = "fake_bs_check_lib";
    public static final String INTELLI_SMSCHECK_LIBNAME = "intelli_smscheck_libname";
    private static final String SDK_VERSION = "5.6.6";
    private static final String SDK_VERSION_INFO = "5.6.0 20161008110301";
    private static final String SDK_VERSION_MFR = "5.6.0";
    public static final String SPIRIT_LIBNAME = "spirit_libname";
    private static final String TAG = "TMSDKContext";
    public static final String TCP_SERVER_ADDRESS = "tcp_server_address";
    private static final String TMS_LIB_VERSION = "2.0.8-mfr";
    public static final String USE_IP_LIST = "use_ip_list";
    private static Context sApplication;
    private static Map<String, String> sEnvMap = new HashMap();
    private static Class<? extends TMSService> sSecureServiceClass;

    static {
        sEnvMap.put(CON_BOA_LIBNAME, "boa-1.0.3");
        if (VERSION.SDK_INT < 23) {
            sEnvMap.put(CON_VIRUS_SCAN_LIBNAME, "ams-1.1.7-mfr");
        } else {
            sEnvMap.put(CON_VIRUS_SCAN_LIBNAME, "ams-1.1.7-m-mfr");
        }
        sEnvMap.put(CON_SDK_LIBNAME, "Tmsdk-2.0.8-mfr");
        sEnvMap.put(FAKE_BS_LIBNAME, "optimus-1.0.0-mfr");
        sEnvMap.put(INTELLI_SMSCHECK_LIBNAME, "bumblebee-1.0.4-mfr");
        sEnvMap.put(BUFFALO_LIBNAME, "buffalo-1.0.0-mfr");
        sEnvMap.put(SPIRIT_LIBNAME, "libspirit-1.0.1");
        sEnvMap.put(CON_PRE_LIB_PATH, null);
        sEnvMap.put(CON_LOGIN_HOST_URL, "sync.3g.qq.com");
        sEnvMap.put(CON_SU_CMD, "su");
        sEnvMap.put(CON_SOFTVERSION, SDK_VERSION);
        sEnvMap.put(CON_BUILD, "3017");
        sEnvMap.put(CON_HOST_URL, "http://pmir.3g.qq.com");
        sEnvMap.put(CON_IS_TEST, "false");
        sEnvMap.put(TCP_SERVER_ADDRESS, "mazu.3g.qq.com");
        sEnvMap.put(USE_IP_LIST, "true");
        sEnvMap.put(CON_LC, "5D93B610AF11BE86");
        sEnvMap.put(CON_CHANNEL, "null");
        sEnvMap.put(CON_PLATFORM, "default");
        sEnvMap.put(CON_PVERSION, "5");
        sEnvMap.put(CON_CVERSION, "6");
        sEnvMap.put(CON_HOTFIX, "6");
        sEnvMap.put(CON_AUTO_REPORT, "true");
        sEnvMap.put(CON_SUB_PLATFORM, String.valueOf(SmsCheckResult.ESCT_201));
        sEnvMap.put(CON_PRODUCT, String.valueOf(13));
        sEnvMap.put(CON_ATHENA_NAME, "athena_v4_2-mfr.dat");
        sEnvMap.put(CON_PKGKEY, "null");
    }

    public static boolean checkLisence() {
        return jw.cH().cD();
    }

    private static native int doRegisterNatives(int i, Class<?> cls);

    public static Context getApplicaionContext() {
        return sApplication.getApplicationContext();
    }

    public static boolean getBooleanFromEnvMap(String str) {
        synchronized (TMSDKContext.class) {
            String str2 = (String) sEnvMap.get(str);
            if (TextUtils.isEmpty(str2)) {
                return false;
            }
            boolean booleanValue = Boolean.valueOf(str2).booleanValue();
            return booleanValue;
        }
    }

    public static int getIntFromEnvMap(String str) {
        synchronized (TMSDKContext.class) {
            String str2 = (String) sEnvMap.get(str);
            if (TextUtils.isEmpty(str2)) {
                return 0;
            }
            int intValue = Integer.valueOf(str2).intValue();
            return intValue;
        }
    }

    public static String getSDKVersionInfo() {
        return SDK_VERSION_INFO;
    }

    public static String getStrFromEnvMap(String str) {
        String str2;
        synchronized (TMSDKContext.class) {
            str2 = (String) sEnvMap.get(str);
            if (str.equals(CON_SOFTVERSION)) {
                if (str2 == null || str2.contains("0.0.0")) {
                    py b = TMServiceFactory.getSystemInfoService().b(getApplicaionContext().getPackageName(), 8);
                    if (b != null) {
                        str2 = b.getVersion();
                    }
                }
            }
        }
        return str2;
    }

    public static boolean getTmsliteSwitch() {
        return jq.getTmsliteSwitch();
    }

    public static <T extends TMSService> boolean init(Context context, Class<T> cls, ITMSApplicaionConfig iTMSApplicaionConfig) {
        if (context != null) {
            int a;
            d.g(TAG, "TMSDK version=" + getSDKVersionInfo());
            sApplication = context.getApplicationContext();
            sSecureServiceClass = cls;
            mh.eN();
            String packageName = context.getPackageName();
            d.e(TAG, "pkgName:[" + packageName + "]uid: [" + sApplication.getApplicationInfo().uid + "]android version:[" + VERSION.SDK_INT + "]");
            if (packageName.compareTo("com.tencent.tmsecure.demo") != 0 && VERSION.SDK_INT >= 21) {
                if (mg.eM() != null) {
                    ma.bx(1320016);
                    a = mg.a(true, sApplication.getPackageName(), "OP_SYSTEM_ALERT_WINDOW");
                    if (a == 1) {
                        ma.bx(1320018);
                    } else if (a == 2) {
                        d.e(TAG, "tryOpenOps no permission");
                        ma.bx(1320019);
                        throw new RuntimeException("permission not enough!");
                    }
                }
                d.e(TAG, "getTopActivity no permission");
                ma.bx(1320017);
                throw new RuntimeException("permission not enough!");
            }
            String[] split = getStrFromEnvMap(CON_SOFTVERSION).trim().split("[\\.]");
            if (split.length >= 3) {
                sEnvMap.put(CON_PVERSION, split[0]);
                sEnvMap.put(CON_CVERSION, split[1]);
                sEnvMap.put(CON_HOTFIX, split[2]);
            }
            ik.bM().b(context.getApplicationContext());
            synchronized (TMSDKContext.class) {
                Object cE = jw.cH().cE();
                Map map = sEnvMap;
                String str = CON_CHANNEL;
                if (cE == null) {
                    cE = "null";
                }
                map.put(str, cE);
                a = dc.n(jw.cH().cF()).value();
                sEnvMap.put(CON_PRODUCT, String.valueOf(a));
                d.d("demo", "pid: " + a);
                sEnvMap.put(CON_ROOT_GOT_ACTION, context.getPackageName() + "ACTION_ROOT_GOT");
                sEnvMap.put(CON_ROOT_DAEMON_START_ACTION, context.getPackageName() + "ACTION_ROOT_DAEMON_START");
                if (iTMSApplicaionConfig != null) {
                    sEnvMap = iTMSApplicaionConfig.config(new HashMap(sEnvMap));
                }
            }
            if (!jq.cr()) {
                return false;
            }
            hz.bJ();
            start();
            ly.en();
            ManagerCreatorB.getManager(TrafficCorrectionManager.class);
            return true;
        }
        throw new RuntimeException("contxt is null when TMSDK init!");
    }

    public static void onImsiChanged() {
        d.g("ImsiChecker", "[API]onImsiChanged");
        ku.dq().onImsiChanged();
        jq.cu().onImsiChanged();
    }

    public static void registerNatives(int i, Class<?> cls) {
        jq.cs();
        int doRegisterNatives = doRegisterNatives(i, cls);
        if (doRegisterNatives != 0) {
            throw new UnsatisfiedLinkError("Failed to register " + cls.toString() + "(err=" + doRegisterNatives + ")");
        }
    }

    public static void reportChannelInfo() {
        jn.reportChannelInfo();
    }

    public static void setAutoConnectionSwitch(boolean z) {
        jq.setAutoConnectionSwitch(z);
        if (sApplication != null && z && getStrFromEnvMap(CON_AUTO_REPORT).equals("true")) {
            reportChannelInfo();
            ly.ep();
        }
    }

    public static void setDualPhoneInfoFetcher(IDualPhoneInfoFetcher iDualPhoneInfoFetcher) {
        jq.setDualPhoneInfoFetcher(iDualPhoneInfoFetcher);
    }

    public static void setIntToEnvMap(String str, int i) {
        synchronized (TMSDKContext.class) {
            sEnvMap.put(str, String.valueOf(i));
        }
    }

    public static void setStrToEnvMap(String str, String str2) {
        synchronized (TMSDKContext.class) {
            sEnvMap.put(str, str2);
        }
    }

    public static void setTMSDKLogEnable(boolean z) {
        d.P(z);
    }

    public static void setTmsliteSwitch(boolean z) {
    }

    private static void start() {
        int i = false;
        if (sSecureServiceClass == null) {
            throw new RuntimeException("Secure service is null!");
        }
        sApplication.startService(new Intent(sApplication, sSecureServiceClass));
        nc ncVar = new nc("tms");
        ncVar.a("reportlc", false, true);
        if (fw.w().G().booleanValue()) {
            i = 1;
        }
        ma.o(29989, i);
        String string = ncVar.getString("reportsig", null);
        if (string != null) {
            ma.d(29967, string);
        }
        if (jq.cq()) {
            reportChannelInfo();
            ly.ep();
        }
    }

    public static boolean startPersistentLink() {
        if (jq.cu() == null) {
            return false;
        }
        pg.gB().a(((pd) ManagerCreatorC.getManager(pd.class)).gm());
        pg.gB().start();
        return true;
    }

    public static void stopPersistentLink() {
        pg.gB();
        pg.stop();
    }
}

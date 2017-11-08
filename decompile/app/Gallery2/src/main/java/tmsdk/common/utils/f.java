package tmsdk.common.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Proxy;
import android.os.Build.VERSION;
import tmsdk.common.TMSDKContext;
import tmsdk.common.TMServiceFactory;
import tmsdkobf.cz;
import tmsdkobf.ml;

/* compiled from: Unknown */
public class f {
    private static int KZ;

    public static int A(Context context) {
        if (!ml.Bc) {
            ml.Bc = false;
            ml.n(context);
        }
        switch (ml.Bg) {
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            case 6:
                return 6;
            case 7:
                return 7;
            case 8:
                return 8;
            case 9:
                return 9;
            case 10:
                return 10;
            case 11:
                return 11;
            case 12:
                return 12;
            case 13:
                return 13;
            case 14:
                return 14;
            case 15:
                return 15;
            case 16:
                return 16;
            case 17:
                return 0;
            default:
                return 0;
        }
    }

    public static boolean B(Context context) {
        try {
            NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getNetworkInfo(1);
            return networkInfo != null && networkInfo.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    public static String getNetworkName() {
        NetworkInfo activeNetworkInfo;
        String str = "";
        try {
            activeNetworkInfo = TMServiceFactory.getSystemInfoService().getActiveNetworkInfo();
        } catch (NullPointerException e) {
            d.f("getActiveNetworkInfo", " getActiveNetworkInfo NullPointerException--- \n" + e.getMessage());
            activeNetworkInfo = null;
        }
        if (activeNetworkInfo == null) {
            return str;
        }
        String extraInfo = activeNetworkInfo.getType() != 1 ? activeNetworkInfo.getExtraInfo() : p.getSSID();
        if (extraInfo == null) {
            extraInfo = "";
        }
        return extraInfo;
    }

    public static boolean hv() {
        NetworkInfo hw = hw();
        return hw != null ? hw.isConnected() : false;
    }

    public static NetworkInfo hw() {
        try {
            return TMServiceFactory.getSystemInfoService().getActiveNetworkInfo();
        } catch (NullPointerException e) {
            d.f("getActiveNetworkInfo", " getActiveNetworkInfo NullPointerException--- \n" + e.getMessage());
            return null;
        }
    }

    public static boolean iA() {
        if (j.iM() < 11) {
            return true;
        }
        if (KZ < 1) {
            KZ = TMSDKContext.getApplicaionContext().getApplicationInfo().targetSdkVersion;
        }
        return KZ < 10;
    }

    public static boolean iu() {
        try {
            ConnectivityManager z = z(TMSDKContext.getApplicaionContext());
            if (z != null) {
                NetworkInfo[] allNetworkInfo = z.getAllNetworkInfo();
                if (allNetworkInfo != null) {
                    for (NetworkInfo state : allNetworkInfo) {
                        if (state.getState() == State.CONNECTED) {
                            return true;
                        }
                    }
                }
            }
        } catch (Throwable th) {
            d.c("NetworkUtil", th);
        }
        return false;
    }

    public static boolean iv() {
        NetworkInfo activeNetworkInfo;
        try {
            activeNetworkInfo = TMServiceFactory.getSystemInfoService().getActiveNetworkInfo();
        } catch (NullPointerException e) {
            d.f("getActiveNetworkInfo", " getActiveNetworkInfo NullPointerException--- \n" + e.getMessage());
            activeNetworkInfo = null;
        }
        if (activeNetworkInfo != null && activeNetworkInfo.getType() == 0) {
            if (activeNetworkInfo.getSubtype() == 1 || activeNetworkInfo.getSubtype() == 4 || activeNetworkInfo.getSubtype() == 2) {
                return true;
            }
        }
        return false;
    }

    public static cz iw() {
        NetworkInfo activeNetworkInfo;
        try {
            activeNetworkInfo = TMServiceFactory.getSystemInfoService().getActiveNetworkInfo();
        } catch (NullPointerException e) {
            d.f("getActiveNetworkInfo", " getActiveNetworkInfo NullPointerException--- \n" + e.getMessage());
            activeNetworkInfo = null;
        }
        if (activeNetworkInfo == null) {
            return cz.gB;
        }
        if (activeNetworkInfo.getType() == 1) {
            return cz.gD;
        }
        if (activeNetworkInfo.getType() != 0) {
            return cz.gF;
        }
        String iy = iy();
        return (iy != null && iy.length() > 0 && iz() > 0) ? cz.gE : cz.gF;
    }

    public static boolean ix() {
        return VERSION.SDK_INT >= 14;
    }

    public static String iy() {
        return !ix() ? Proxy.getHost(TMSDKContext.getApplicaionContext()) : System.getProperty("http.proxyHost");
    }

    public static int iz() {
        int parseInt;
        if (ix()) {
            try {
                parseInt = Integer.parseInt(System.getProperty("http.proxyPort"));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        parseInt = Proxy.getPort(TMSDKContext.getApplicaionContext());
        return parseInt;
    }

    public static ConnectivityManager z(Context context) {
        try {
            return (ConnectivityManager) context.getSystemService("connectivity");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

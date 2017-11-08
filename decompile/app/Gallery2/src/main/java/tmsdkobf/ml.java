package tmsdkobf;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.telephony.TelephonyManager;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class ml {
    public static int AX = 4;
    public static String AY = null;
    public static int AZ = 80;
    public static byte Ba = (byte) 0;
    public static boolean Bb = false;
    public static boolean Bc = false;
    public static byte Bd = (byte) 4;
    public static String Be = "unknown";
    public static byte Bf = (byte) 9;
    public static int Bg = 17;

    private static int a(Context context, NetworkInfo networkInfo) {
        int i = 1;
        if (networkInfo == null) {
            return 0;
        }
        try {
            if (1 != networkInfo.getType()) {
                if (networkInfo.getType() == 0) {
                    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
                    if (telephonyManager != null) {
                        switch (telephonyManager.getNetworkType()) {
                            case 1:
                                i = 2;
                                break;
                            case 2:
                                i = 3;
                                break;
                            case 3:
                                i = 4;
                                break;
                            case 4:
                                i = 8;
                                break;
                            case 5:
                                i = 9;
                                break;
                            case 6:
                                i = 10;
                                break;
                            case 7:
                                i = 11;
                                break;
                            case 8:
                                i = 5;
                                break;
                            case 9:
                                i = 6;
                                break;
                            case 10:
                                i = 7;
                                break;
                            default:
                                i = 17;
                                break;
                        }
                    }
                }
                i = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }

    private static boolean bC(int i) {
        return i == 2 || i == 0;
    }

    private static void cv(String str) {
        if (str != null) {
            if (str.contains("cmwap")) {
                Be = "cmwap";
                Bf = (byte) 0;
            } else if (str.contains("cmnet")) {
                Be = "cmnet";
                Bf = (byte) 1;
            } else if (str.contains("3gwap")) {
                Be = "3gwap";
                Bf = (byte) 2;
            } else if (str.contains("3gnet")) {
                Be = "3gnet";
                Bf = (byte) 3;
            } else if (str.contains("uniwap")) {
                Be = "uniwap";
                Bf = (byte) 4;
            } else if (str.contains("uninet")) {
                Be = "uninet";
                Bf = (byte) 5;
            } else if (str.contains("ctwap")) {
                Be = "ctwap";
                Bf = (byte) 6;
            } else if (str.contains("ctnet")) {
                Be = "ctnet";
                Bf = (byte) 7;
            } else if (str.contains("#777")) {
                Be = "#777";
                Bf = (byte) 8;
            }
        }
    }

    public static void init(Context context) {
        NetworkInfo activeNetworkInfo;
        String str = null;
        try {
            activeNetworkInfo = TMServiceFactory.getSystemInfoService().getActiveNetworkInfo();
        } catch (NullPointerException e) {
            d.f("getActiveNetworkInfo", " getActiveNetworkInfo NullPointerException--- \n" + e.getMessage());
            activeNetworkInfo = null;
        }
        d.e("Apn", "networkInfo : " + activeNetworkInfo);
        int i = -1;
        try {
            AX = 0;
            Bd = (byte) 4;
            if (activeNetworkInfo != null) {
                i = activeNetworkInfo.getType();
                d.e("Apn", "type: " + activeNetworkInfo.getType());
                d.e("Apn", "typeName: " + activeNetworkInfo.getTypeName());
                str = activeNetworkInfo.getExtraInfo();
                if (str != null) {
                    str = str.trim().toLowerCase();
                } else {
                    AX = 0;
                }
            }
            d.e("Apn", "extraInfo : " + str);
            if (i != 1) {
                cv(str);
                if (str == null) {
                    AX = 0;
                } else if (str.contains("cmwap") || str.contains("uniwap") || str.contains("3gwap") || str.contains("ctwap")) {
                    Bd = (byte) 1;
                    if (str.contains("3gwap")) {
                        Bd = (byte) 2;
                    }
                    AX = 2;
                } else if (str.contains("cmnet") || str.contains("uninet") || str.contains("3gnet") || str.contains("ctnet")) {
                    Bd = (byte) 1;
                    if (str.contains("3gnet") || str.contains("ctnet")) {
                        Bd = (byte) 2;
                    }
                    AX = 1;
                } else if (str.contains("#777")) {
                    Bd = (byte) 2;
                    AX = 0;
                } else {
                    AX = 0;
                }
                Bb = false;
                if (bC(AX)) {
                    AY = Proxy.getDefaultHost();
                    AZ = Proxy.getDefaultPort();
                    if (AY != null) {
                        AY = AY.trim();
                    }
                    if (AY == null || "".equals(AY)) {
                        Bb = false;
                        AX = 1;
                    } else {
                        Bb = true;
                        AX = 2;
                        if ("10.0.0.200".equals(AY)) {
                            Ba = (byte) 1;
                        } else {
                            Ba = (byte) 0;
                        }
                    }
                }
            } else {
                AX = 4;
                Bb = false;
                Bd = (byte) 3;
                Be = "unknown";
                Bf = (byte) 9;
            }
            d.e("Apn", "NETWORK_TYPE : " + Bd);
            d.e("Apn", "M_APN_TYPE : " + AX);
            d.e("Apn", "M_USE_PROXY : " + Bb);
            d.e("Apn", "M_APN_PROXY : " + AY);
            d.e("Apn", "M_APN_PORT : " + AZ);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        Bg = a(context, activeNetworkInfo);
        d.d("Apn", "init() Apn.APN_NAME_VALUE: " + Bf + " APN_NAME_DRI: " + Be + " NETWORK_TYPE: " + Bd + " ENT_VALUE: " + Bg);
    }

    public static void n(Context context) {
        if (!Bc) {
            synchronized (ml.class) {
                if (Bc) {
                    return;
                }
                init(context);
                Bc = true;
            }
        }
    }
}

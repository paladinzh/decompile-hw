package com.huawei.netassistant.wifisecure;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.ActionListener;
import android.text.TextUtils;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.util.HwLog;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

public class WifiConfigHelper {
    private static final String FIELD_WIFICONFIG_SECRESULT = "cloudSecurityCheck";
    private static final int SECURITY_EAP = 3;
    private static final int SECURITY_NONE = 0;
    private static final int SECURITY_PSK = 2;
    private static final int SECURITY_WAPI_CERT = 5;
    private static final int SECURITY_WAPI_PSK = 4;
    private static final int SECURITY_WEP = 1;
    private static final String TAG = "WifiConfigHelper";

    public static WifiConfiguration getConnectedWifiConfig(Context context) {
        if (context == null) {
            HwLog.w(TAG, "getConnectedWifiConfig: Invalid context");
            return null;
        }
        WifiManager wifiMgr = (WifiManager) context.getSystemService("wifi");
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        if (wifiInfo != null) {
            return getWifiConfigByNetworkId(wifiMgr, wifiInfo.getNetworkId());
        }
        HwLog.w(TAG, "getConnectedWifiConfig: Fail to get connection info");
        return null;
    }

    public static boolean isCurrentlyConnected(Context context, WifiConfiguration config) {
        if (context == null || config == null) {
            HwLog.e(TAG, "isCurrentlyConnected: Invalid params");
            return false;
        }
        WifiConfiguration curConfig = getConnectedWifiConfig(context);
        if (curConfig != null) {
            return isWifiConfigMatched(curConfig, config);
        }
        HwLog.w(TAG, "isCurrentlyConnected: Fail to get connected wifi info");
        return false;
    }

    public static boolean isWifiConfigMatched(WifiConfiguration configA, WifiConfiguration configB) {
        if (configA == null || configB == null) {
            HwLog.e(TAG, "isCurrentlyConnected: Invalid configs");
            return false;
        } else if (configA.isPasspoint() && configB.isPasspoint()) {
            return configA.FQDN.equals(configB.FQDN);
        } else {
            if (getSecurity(configA) != getSecurity(configB)) {
                return false;
            }
            return removeDoubleQuotes(configA.SSID).equals(removeDoubleQuotes(configB.SSID));
        }
    }

    public static int saveDetectResult(Context context, WifiConfiguration config, WifiDetectResult result, ActionListener listener) {
        WifiConfiguration curConfig = getConnectedWifiConfig(context);
        if (curConfig == null) {
            HwLog.w(TAG, "saveDetectResult: Fail to get currently connected wifi info");
            return -1;
        } else if (isWifiConfigMatched(curConfig, config)) {
            int nLastCheckResult = getWifiSecConfig(curConfig);
            int nNewCheckResult = result.getUpdateCheckResult(nLastCheckResult);
            if (nLastCheckResult == nNewCheckResult) {
                HwLog.i(TAG, "saveDetectResult: SSID = " + curConfig.SSID + ", sec status doesn't change = " + nLastCheckResult);
                return nNewCheckResult;
            }
            HwLog.i(TAG, "saveDetectResult: SSID = " + curConfig.SSID + ", current sec status = " + nLastCheckResult + ", update to " + nNewCheckResult);
            setWifiSecConfig(curConfig, nNewCheckResult);
            ((WifiManager) context.getSystemService("wifi")).save(curConfig, listener);
            HwLog.i(TAG, "saveDetectResult: Do save , curConfig.cloudSecurityCheck = " + nLastCheckResult + ", SSID = " + curConfig.SSID);
            return nNewCheckResult;
        } else {
            HwLog.w(TAG, "saveDetectResult: Not currently connected wifi, skip");
            return -1;
        }
    }

    public static boolean disconnectWifi(Context context) {
        if (context != null) {
            return ((WifiManager) context.getSystemService("wifi")).disconnect();
        }
        HwLog.w(TAG, "disconnectWifi: Invalid context");
        return true;
    }

    private static WifiConfiguration getWifiConfigByNetworkId(WifiManager wifiMgr, int networkId) {
        if (wifiMgr.isWifiEnabled()) {
            List<WifiConfiguration> configs = wifiMgr.getConfiguredNetworks();
            if (Utility.isNullOrEmptyList(configs)) {
                HwLog.w(TAG, "getWifiConfigByNetworkId: Fail to get configured networks");
                return null;
            }
            HwLog.i(TAG, "getWifiConfigByNetworkId: networkId = " + networkId);
            for (WifiConfiguration config : configs) {
                HwLog.i(TAG, "SSID = " + config.SSID);
            }
            for (WifiConfiguration config2 : configs) {
                if (networkId == config2.networkId && (!config2.selfAdded || config2.numAssociation != 0)) {
                    return config2;
                }
            }
            return null;
        }
        HwLog.i(TAG, "getWifiConfigByNetworkId: Wifi is disabled");
        return null;
    }

    private static String removeDoubleQuotes(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        int length = string.length();
        if (length > 1 && string.charAt(0) == '\"' && string.charAt(length - 1) == '\"') {
            return string.substring(1, length - 1);
        }
        return string;
    }

    private static int getSecurity(WifiConfiguration config) {
        int i = 1;
        if (config.allowedKeyManagement.get(1)) {
            return 2;
        }
        if (config.allowedKeyManagement.get(2) || config.allowedKeyManagement.get(3)) {
            return 3;
        }
        if (config.allowedKeyManagement.get(6) || config.allowedKeyManagement.get(8)) {
            return 4;
        }
        if (config.allowedKeyManagement.get(7) || config.allowedKeyManagement.get(9)) {
            return 5;
        }
        if (config.wepKeys[0] == null) {
            i = 0;
        }
        return i;
    }

    public static boolean isFwkSupportedWifiSecConfig() {
        try {
            final Field field = Class.forName("android.net.wifi.WifiConfiguration").getDeclaredField(FIELD_WIFICONFIG_SECRESULT);
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Void run() {
                    field.setAccessible(true);
                    return null;
                }
            });
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        } catch (NoSuchFieldException e2) {
            return false;
        } catch (Error e3) {
            return false;
        }
    }

    public static boolean setWifiSecConfig(WifiConfiguration config, int nSecResult) {
        try {
            final Field field = config.getClass().getDeclaredField(FIELD_WIFICONFIG_SECRESULT);
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Void run() {
                    field.setAccessible(true);
                    return null;
                }
            });
            field.set(config, Integer.valueOf(nSecResult));
            return true;
        } catch (IllegalAccessException e) {
            return false;
        } catch (NoSuchFieldException e2) {
            return false;
        } catch (Error e3) {
            return false;
        }
    }

    public static int getWifiSecConfig(WifiConfiguration config) {
        try {
            final Field field = config.getClass().getDeclaredField(FIELD_WIFICONFIG_SECRESULT);
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Void run() {
                    field.setAccessible(true);
                    return null;
                }
            });
            return field.getInt(config);
        } catch (IllegalAccessException e) {
            return 0;
        } catch (NoSuchFieldException e2) {
            return 0;
        } catch (Error e3) {
            return 0;
        }
    }
}

package com.android.server.wifipro;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpResults;
import android.net.LinkAddress;
import android.net.NetworkInfo;
import android.net.StaticIpConfiguration;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.net.wifi.wifipro.WifiProStatusUtils;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.util.AsyncChannel;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class WifiProCommonUtils {
    public static final int CMD_UPDATE_WIFI_CONFIGURATIONS = 131672;
    public static final String COUNTRY_CODE_CN = "460";
    public static final int DNS_REACHALBE = 0;
    public static final int DNS_UNREACHALBE = -1;
    public static final int ENTERPRISE_HOTSPOT_THRESHOLD = 4;
    public static final int HISTORY_ITEM_INTERNET = 1;
    public static final int HISTORY_ITEM_NO_INTERNET = 0;
    public static final int HISTORY_ITEM_PORTAL = 2;
    public static final int HISTORY_ITEM_UNCHECKED = -1;
    public static final int HISTORY_TYPE_EMPTY = 103;
    public static final int HISTORY_TYPE_INTERNET = 100;
    public static final int HISTORY_TYPE_PORTAL = 102;
    public static final int HTTP_REACHALBE_GOOLE = 204;
    public static final int HTTP_REACHALBE_HOME = 200;
    public static final int HTTP_REDIRECTED = 302;
    public static final int HTTP_UNREACHALBE = 599;
    public static final String HUAWEI_SETTINGS = "com.android.settings";
    public static final String HUAWEI_SETTINGS_WLAN = "com.android.settings.Settings$WifiSettingsActivity";
    private static final String HW_WIFI_SELF_CURING = "net.wifi.selfcuring";
    public static final String KEY_WIFIPRO_MANUAL_CONNECT = "wifipro_manual_connect_ap";
    private static final String KEY_WIFI_SECURE = "wifi_cloud_security_check";
    private static final String PORAL_BACKGROUND = "net.portal.background";
    public static final long RECHECK_DELAYED_MS = 3600000;
    public static final float RECOVERY_PERCENTAGE = 0.5f;
    public static final int RESP_CODE_ABNORMAL_SERVER = 604;
    public static final int RESP_CODE_CONN_RESET = 606;
    public static final int RESP_CODE_GATEWAY = 602;
    public static final int RESP_CODE_INVALID_URL = 603;
    public static final int RESP_CODE_REDIRECTED_HOST_CHANGED = 605;
    public static final int RESP_CODE_TIMEOUT = 600;
    public static final int RESP_CODE_UNSTABLE = 601;
    public static final long TRUSTED_DAYS_MS = 259200000;
    public static final String[] TRUSTED_PORTAL_LIST = new String[]{"\"Huawei-Guest\""};
    private static final String WIFI_BACKGROUND_CONN_TAG = "wifipro_recommending_access_points";
    private static final Object mBackgroundLock = new Object();

    public static boolean isWifiProEnable(Context context) {
        return WifiProStatusUtils.isWifiProEnabledViaXml(context);
    }

    public static void updateWifiConfig(WifiConfiguration config, int what, AsyncChannel asyncChannel) {
        if (config != null && asyncChannel != null) {
            Message msg = Message.obtain();
            msg.what = what;
            msg.obj = config;
            asyncChannel.sendMessage(msg);
        }
    }

    public static WifiConfiguration getCurrentWifiConfig(WifiManager wifiManager) {
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            List<WifiConfiguration> configNetworks = wifiManager.getConfiguredNetworks();
            if (!(configNetworks == null || wifiInfo == null || !SupplicantState.isConnecting(wifiInfo.getSupplicantState()))) {
                for (int i = 0; i < configNetworks.size(); i++) {
                    WifiConfiguration config = (WifiConfiguration) configNetworks.get(i);
                    if (config.networkId == wifiInfo.getNetworkId() && config.networkId != -1) {
                        return config;
                    }
                }
            }
        }
        return null;
    }

    public static String getCurrentSsid(WifiManager wifiManager) {
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null && SupplicantState.isConnecting(wifiInfo.getSupplicantState())) {
                return wifiInfo.getSSID();
            }
        }
        return null;
    }

    public static String getCurrentBssid(WifiManager wifiManager) {
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null && SupplicantState.isConnecting(wifiInfo.getSupplicantState())) {
                return wifiInfo.getBSSID();
            }
        }
        return null;
    }

    public static int getCurrentRssi(WifiManager wifiManager) {
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null && SupplicantState.isConnecting(wifiInfo.getSupplicantState())) {
                return wifiInfo.getRssi();
            }
        }
        return -127;
    }

    public static boolean isWifiConnected(WifiManager wifiManager) {
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null && wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                return true;
            }
        }
        return false;
    }

    public static boolean isQueryActivityMatched(Context context, String activityName) {
        if (!(context == null || activityName == null)) {
            List<RunningTaskInfo> runningTaskInfos = ((ActivityManager) context.getSystemService("activity")).getRunningTasks(1);
            if (!(runningTaskInfos == null || runningTaskInfos.isEmpty())) {
                ComponentName cn = ((RunningTaskInfo) runningTaskInfos.get(0)).topActivity;
                return (cn == null || cn.getClassName() == null || !cn.getClassName().startsWith(activityName)) ? false : true;
            }
        }
    }

    public static boolean isManualConnecting(Context context) {
        boolean z = true;
        if (context == null) {
            return false;
        }
        if (System.getInt(context.getContentResolver(), KEY_WIFIPRO_MANUAL_CONNECT, 0) != 1) {
            z = false;
        }
        return z;
    }

    public static boolean isInMonitorList(String ssid, String[] list) {
        if (!(ssid == null || list == null)) {
            for (Object equals : list) {
                if (ssid.equals(equals)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean withinTrustedPeriod(WifiConfiguration config, long daysMs) {
        if (config != null && config.lastHasInternetTimestamp > 0) {
            long deltaMs = System.currentTimeMillis() - config.lastHasInternetTimestamp;
            if (deltaMs > 0 && deltaMs < daysMs) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMobileDataOff(Context context) {
        boolean z = true;
        if (context == null) {
            return false;
        }
        if (Global.getInt(context.getContentResolver(), "mobile_data", 1) != 0) {
            z = false;
        }
        return z;
    }

    public static boolean isMobileDataInactive(Context context) {
        if (context != null) {
            NetworkInfo ni = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
            if (ni != null && ni.isConnected() && ni.getType() == 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean isCalling(Context context) {
        if (context != null) {
            int callState = ((TelephonyManager) context.getSystemService("phone")).getCallState();
            if (2 == callState || 1 == callState) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNoSIMCard(Context context) {
        boolean z = true;
        if (context == null) {
            return false;
        }
        if (((TelephonyManager) context.getSystemService("phone")).getSimState() != 1) {
            z = false;
        }
        return z;
    }

    public static boolean useOperatorOverSea() {
        String operator = TelephonyManager.getDefault().getNetworkOperator();
        if (operator == null || operator.length() <= 0 || operator.startsWith(COUNTRY_CODE_CN)) {
            return false;
        }
        return true;
    }

    public static boolean allowRecheckForNoInternet(WifiConfiguration config, ScanResult scanResult, Context context) {
        boolean allowed = false;
        synchronized (mBackgroundLock) {
            if (config != null) {
                if (config.noInternetAccess && !NetworkHistoryUtils.allowWifiConfigRecovery(config.internetHistory)) {
                    if (!(config.internetRecoveryStatus != 3 || isQueryActivityMatched(context, HUAWEI_SETTINGS_WLAN) || scanResult == null)) {
                        if (!scanResult.is24GHz() || scanResult.level < -75) {
                            if (scanResult.is5GHz() && scanResult.level >= -75) {
                            }
                        }
                        allowed = true;
                    }
                }
            }
        }
        return allowed;
    }

    public static void setBackgroundConnTag(Context context, boolean background) {
        synchronized (mBackgroundLock) {
            if (context != null) {
                Secure.putInt(context.getContentResolver(), WIFI_BACKGROUND_CONN_TAG, background ? 1 : 0);
            }
        }
    }

    public static void portalBackgroundStatusChanged(boolean background) {
        synchronized (mBackgroundLock) {
            try {
                SystemProperties.set(PORAL_BACKGROUND, String.valueOf(background));
            } catch (RuntimeException e) {
                Log.e("WifiProCommonUtils", "SystemProperties set RuntimeException.");
            }
        }
    }

    public static boolean isPortalBackground() {
        boolean equals;
        synchronized (mBackgroundLock) {
            equals = "true".equals(SystemProperties.get(PORAL_BACKGROUND, AppHibernateCst.INVALID_PKG));
        }
        return equals;
    }

    public static void setWifiSelfCureStatus(boolean curing) {
        try {
            SystemProperties.set(HW_WIFI_SELF_CURING, String.valueOf(curing));
        } catch (RuntimeException e) {
            Log.e("WifiProCommonUtils", "SystemProperties set RuntimeException.");
        }
    }

    public static boolean isWifiSelfCuring() {
        return "true".equals(SystemProperties.get(HW_WIFI_SELF_CURING, AppHibernateCst.INVALID_PKG));
    }

    public static boolean isWifiSecDetectOn(Context context) {
        boolean z = true;
        if (context == null) {
            return false;
        }
        if (Global.getInt(context.getContentResolver(), KEY_WIFI_SECURE, 0) != 1) {
            z = false;
        }
        return z;
    }

    public static boolean matchedRequestByHistory(String internetHistory, int type) {
        boolean matched = false;
        if (internetHistory == null || internetHistory.lastIndexOf("/") == -1) {
            return false;
        }
        String[] temp = internetHistory.split("/");
        int[] items = new int[temp.length];
        int numChecked = 0;
        int numNoInet = 0;
        int numHasInet = 0;
        int numPortal = 0;
        int numTarget = 0;
        for (int i = 0; i < temp.length; i++) {
            items[i] = Integer.parseInt(temp[i]);
            if (items[i] != -1) {
                numChecked++;
            }
            if (items[i] == 0) {
                numNoInet++;
            } else if (items[i] == 1) {
                numHasInet++;
            } else if (items[i] == 2) {
                numPortal++;
            }
        }
        int itemValue = -1;
        if (type == 100) {
            itemValue = 1;
            numTarget = numHasInet;
        } else if (type == 102) {
            return numPortal >= 1;
        } else if (type == 103) {
            return numChecked == 0;
        }
        if (numChecked >= 1 && items[0] == itemValue) {
            matched = true;
        }
        if (!matched && numChecked == 2 && (items[0] == itemValue || items[1] == itemValue)) {
            matched = true;
        }
        if (!matched && numChecked >= 3 && ((float) numTarget) / ((float) numChecked) >= 0.5f) {
            matched = true;
        }
        return matched;
    }

    public static boolean isOpenType(WifiConfiguration config) {
        boolean z = true;
        if (config == null || config.allowedKeyManagement.cardinality() > 1) {
            return false;
        }
        if (config.getAuthType() != 0) {
            z = false;
        }
        return z;
    }

    public static boolean isOpenAndPortal(WifiConfiguration config) {
        return isOpenType(config) ? config.portalNetwork : false;
    }

    public static boolean isOpenAndMaybePortal(WifiConfiguration config) {
        if (!isOpenType(config) || config.noInternetAccess) {
            return false;
        }
        return matchedRequestByHistory(config.internetHistory, 103);
    }

    public static String parseHostByUrlLocation(String requestUrl) {
        if (requestUrl != null) {
            int start = 0;
            if (requestUrl.startsWith("http://")) {
                start = 7;
            } else if (requestUrl.startsWith("https://")) {
                start = 8;
            }
            String TAG = "/";
            int end = requestUrl.indexOf("/", start);
            if (end == -1 || "/".length() + end > requestUrl.length()) {
                end = requestUrl.indexOf("?", start);
            } else {
                int tmpEnd = requestUrl.substring(start, end).indexOf("?", 0);
                if (tmpEnd != -1) {
                    end = tmpEnd + start;
                }
            }
            if (end != -1 && end >= 0 && "/".length() + end <= requestUrl.length()) {
                return requestUrl.substring(0, end);
            }
        }
        return requestUrl;
    }

    public static boolean invalidUrlLocation(String location) {
        if (location == null || (!location.startsWith("http://") && !location.startsWith("https://"))) {
            return true;
        }
        return false;
    }

    public static String dhcpResults2String(DhcpResults dhcpResults, int cellid) {
        if (dhcpResults == null || dhcpResults.ipAddress == null || dhcpResults.ipAddress.getAddress() == null || dhcpResults.dnsServers == null) {
            return null;
        }
        StringBuilder lastDhcpResults = new StringBuilder();
        lastDhcpResults.append(String.valueOf(cellid)).append("|");
        lastDhcpResults.append(dhcpResults.domains == null ? AppHibernateCst.INVALID_PKG : dhcpResults.domains).append("|");
        lastDhcpResults.append(dhcpResults.ipAddress.getAddress().getHostAddress()).append("|");
        lastDhcpResults.append(dhcpResults.ipAddress.getPrefixLength()).append("|");
        lastDhcpResults.append(dhcpResults.ipAddress.getFlags()).append("|");
        lastDhcpResults.append(dhcpResults.ipAddress.getScope()).append("|");
        lastDhcpResults.append(dhcpResults.gateway != null ? dhcpResults.gateway.getHostAddress() : AppHibernateCst.INVALID_PKG).append("|");
        for (InetAddress dnsServer : dhcpResults.dnsServers) {
            lastDhcpResults.append(dnsServer.getHostAddress()).append("|");
        }
        return lastDhcpResults.toString();
    }

    public static StaticIpConfiguration dhcpResults2StaticIpConfig(String lastDhcpResults) {
        if (lastDhcpResults != null && lastDhcpResults.length() > 0) {
            StaticIpConfiguration staticIpConfig = new StaticIpConfiguration();
            String[] dhcpResults = lastDhcpResults.split("\\|");
            InetAddress ipAddr = null;
            int prefLength = -1;
            int flag = -1;
            int scope = -1;
            int i = 0;
            while (i < dhcpResults.length) {
                try {
                    if (i != 0) {
                        if (i == 1) {
                            staticIpConfig.domains = dhcpResults[i];
                        } else if (i == 2) {
                            ipAddr = InetAddress.getByName(dhcpResults[i]);
                        } else if (i == 3) {
                            prefLength = Integer.parseInt(dhcpResults[i]);
                        } else if (i == 4) {
                            flag = Integer.parseInt(dhcpResults[i]);
                        } else if (i == 5) {
                            scope = Integer.parseInt(dhcpResults[i]);
                        } else if (i == 6) {
                            staticIpConfig.gateway = InetAddress.getByName(dhcpResults[i]);
                        } else {
                            staticIpConfig.dnsServers.add(InetAddress.getByName(dhcpResults[i]));
                        }
                    }
                    i++;
                } catch (UnknownHostException e) {
                } catch (IllegalArgumentException e2) {
                }
            }
            if (!(ipAddr == null || prefLength == -1 || staticIpConfig.gateway == null || staticIpConfig.dnsServers.size() <= 0)) {
                staticIpConfig.ipAddress = new LinkAddress(ipAddr, prefLength, flag, scope);
                return staticIpConfig;
            }
        }
        return null;
    }

    public static String dhcpResults2Gateway(String lastDhcpResults) {
        if (lastDhcpResults != null && lastDhcpResults.length() > 0) {
            try {
                String[] dhcpResults = lastDhcpResults.split("\\|");
                if (dhcpResults.length >= 7) {
                    return InetAddress.getByName(dhcpResults[6]).toString();
                }
            } catch (UnknownHostException e) {
            }
        }
        return null;
    }

    public static boolean isAllowWifiSwitch(List<ScanResult> scanResults, List<WifiConfiguration> configNetworks, String currBssid, String currSsid, String currConfigKey, int rssiRequired) {
        if (scanResults == null || scanResults.size() == 0) {
            return false;
        }
        if (configNetworks == null || configNetworks.size() == 0) {
            return false;
        }
        for (int i = 0; i < scanResults.size(); i++) {
            ScanResult nextResult = (ScanResult) scanResults.get(i);
            String scanSsid = "\"" + nextResult.SSID + "\"";
            String scanResultEncrypt = nextResult.capabilities;
            boolean equals = currBssid != null ? currBssid.equals(nextResult.BSSID) : false;
            boolean sameConfigKey;
            if (currSsid == null || !currSsid.equals(scanSsid)) {
                sameConfigKey = false;
            } else {
                sameConfigKey = isSameEncryptType(scanResultEncrypt, currConfigKey);
            }
            if (!(equals || r7 || nextResult.level < rssiRequired)) {
                for (int j = 0; j < configNetworks.size(); j++) {
                    WifiConfiguration nextConfig = (WifiConfiguration) configNetworks.get(j);
                    int disableReason = nextConfig.getNetworkSelectionStatus().getNetworkSelectionDisableReason();
                    if ((!nextConfig.noInternetAccess || NetworkHistoryUtils.allowWifiConfigRecovery(nextConfig.internetHistory)) && disableReason <= 0 && !isOpenAndPortal(nextConfig) && nextConfig.SSID != null && nextConfig.SSID.equals(scanSsid) && isSameEncryptType(scanResultEncrypt, nextConfig.configKey())) {
                        return true;
                    }
                }
                continue;
            }
        }
        return false;
    }

    public static boolean unreachableRespCode(int code) {
        return code == HTTP_UNREACHALBE || code == 600;
    }

    public static boolean httpUnreachableOrAbnormal(int code) {
        return code >= HTTP_UNREACHALBE;
    }

    public static boolean isRedirectedRespCode(int respCode) {
        return respCode == MemoryConstant.MSG_BOOST_SIGKILL_SWITCH || respCode == 302 || respCode == MemoryConstant.MSG_DIRECT_SWAPPINESS || respCode == MemoryConstant.MSG_PROTECTLRU_SET_PROTECTRATIO;
    }

    public static boolean isRedirectedRespCodeByGoogle(int respCode) {
        if (respCode == HTTP_REACHALBE_GOOLE || respCode < 200 || respCode > 399) {
            return false;
        }
        return true;
    }

    public static boolean httpReachableOrRedirected(int code) {
        return code >= 200 && code <= 399;
    }

    public static boolean httpReachableHome(int code) {
        return code == 200;
    }

    public static int getReachableCode(boolean googleServer) {
        return googleServer ? HTTP_REACHALBE_GOOLE : 200;
    }

    public static boolean isEncryptionWep(String encryption) {
        return encryption.contains("WEP");
    }

    public static boolean isEncryptionPsk(String encryption) {
        return encryption.contains("PSK");
    }

    public static boolean isEncryptionEap(String encryption) {
        return encryption.contains("EAP");
    }

    public static boolean isOpenNetwork(String encryption) {
        if (isEncryptionWep(encryption) || isEncryptionPsk(encryption) || isEncryptionEap(encryption)) {
            return false;
        }
        return true;
    }

    public static boolean isSameEncryptType(String encryption1, String encryption2) {
        if (encryption1 == null || encryption2 == null) {
            return false;
        }
        if ((isEncryptionWep(encryption1) && isEncryptionWep(encryption2)) || ((isEncryptionPsk(encryption1) && isEncryptionPsk(encryption2)) || ((isEncryptionEap(encryption1) && isEncryptionEap(encryption2)) || (isOpenNetwork(encryption1) && isOpenNetwork(encryption2))))) {
            return true;
        }
        return false;
    }

    public static boolean isPortalAPHaveInternetLastTime(WifiConfiguration config) {
        if (config == null || config.internetHistory.lastIndexOf("/") == -1) {
            return false;
        }
        if (config.lastHasInternetTimestamp == 0 || System.currentTimeMillis() - config.lastHasInternetTimestamp > 172800000) {
            Log.d("WifiProCommonUtils", "lastHasInternetTimestamp failed config.lastHasInternetTimestamp = " + config.lastHasInternetTimestamp);
            return false;
        }
        String[] temp = config.internetHistory.split("/");
        if (temp.length <= 0) {
            return false;
        }
        int lastHistory = Integer.parseInt(temp[0]);
        int secHistory = Integer.parseInt(temp[1]);
        Log.d("WifiProCommonUtils", "isPortalAPHaveInternetLastTime lastHistory = " + lastHistory + " secHistory = " + secHistory);
        if (lastHistory == 1 && secHistory == 1) {
            Log.d("WifiProCommonUtils", "isPortalAPHaveInternetLastTime return true");
            return true;
        }
        Log.d("WifiProCommonUtils", "isPortalAPHaveInternetLastTime return flase");
        return false;
    }
}

package com.android.settingslib.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiSsid;
import android.provider.Settings.Global;
import android.text.TextUtils;
import com.android.settingslib.R$string;

public class AccessPointHwBase {
    protected String bssid;
    protected int cloudSecurityCheck;
    protected int internetAccessType;
    protected boolean isHiLinkNetwork;
    protected boolean isTempCreated;
    protected WifiConfiguration mConfig;
    private final Context mContext;
    protected boolean mIsRecommendingAccessPoints;
    protected int networkId = -1;
    protected int networkQosLevel;
    protected int networkSecurity = 0;
    protected String nonNullBssid;
    protected String oriSsid = "";
    protected int security;
    protected String ssid;
    protected boolean wpsAvailable = false;

    public AccessPointHwBase(Context context) {
        this.mContext = context;
    }

    public Context getContext() {
        return this.mContext;
    }

    public int getNetworkId() {
        return this.networkId;
    }

    protected void appendSecuritySummary(StringBuilder summary) {
        if (this.security != 0) {
            String securityStrFormat;
            if (summary.length() == 0) {
                securityStrFormat = this.mContext.getString(R$string.wifi_encrypted_first_item);
            } else {
                securityStrFormat = this.mContext.getString(R$string.wifi_encrypted_second_item);
            }
            summary.append(securityStrFormat);
        }
    }

    protected void appendWpsSummary(StringBuilder summary) {
        if (this.mConfig != null || !this.wpsAvailable) {
            return;
        }
        if (summary.length() == 0) {
            summary.append(this.mContext.getString(R$string.wifi_wps_available_first_item));
        } else {
            summary.append(this.mContext.getString(R$string.wifi_wps_available_second_item));
        }
    }

    public String getOriSsid() {
        return this.oriSsid;
    }

    protected String getOriSsidFromWifiSsid(Object obj) {
        String oriSsidString = "";
        try {
            return (String) WifiSsid.class.getDeclaredField("oriSsid").get(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return oriSsidString;
        }
    }

    public boolean isTempCreated() {
        return this.isTempCreated;
    }

    public boolean isCloudSecurityCheckDangerous() {
        return this.cloudSecurityCheck > 0;
    }

    protected int getInternetAccessTypeByReflection(Object obj) {
        try {
            if (obj instanceof ScanResult) {
                this.internetAccessType = ((Integer) ScanResult.class.getDeclaredField("internetAccessType").get(obj)).intValue();
            } else if (obj instanceof WifiConfiguration) {
                this.internetAccessType = ((Integer) WifiConfiguration.class.getDeclaredField("internetAccessType").get(obj)).intValue();
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return this.internetAccessType;
    }

    protected int getNetworkQosLevelByReflection(Object obj) {
        try {
            if (obj instanceof ScanResult) {
                this.networkQosLevel = ((Integer) ScanResult.class.getDeclaredField("networkQosLevel").get(obj)).intValue();
                this.networkSecurity = ((Integer) ScanResult.class.getDeclaredField("networkSecurity").get(obj)).intValue();
            } else if (obj instanceof WifiConfiguration) {
                this.networkQosLevel = ((Integer) WifiConfiguration.class.getDeclaredField("networkQosLevel").get(obj)).intValue();
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return this.networkQosLevel;
    }

    protected boolean getIsTempCreatedByReflection(Object obj) {
        try {
            if (obj instanceof WifiConfiguration) {
                this.isTempCreated = ((Boolean) WifiConfiguration.class.getDeclaredField("isTempCreated").get(obj)).booleanValue();
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return this.isTempCreated;
    }

    protected void updateRecommendProperties(Object obj) {
        getInternetAccessTypeByReflection(obj);
        getNetworkQosLevelByReflection(obj);
        getIsTempCreatedByReflection(obj);
        getCloudSecurityCheckByReflection(obj);
        getIsHiLinkNetworkByReflection(obj);
    }

    protected boolean buildRecommendNetworkSummary(StringBuilder summary, boolean linkPlusEnabled) {
        boolean securitySwitchOn = true;
        boolean handled = false;
        if (!linkPlusEnabled && (this.isTempCreated || this.internetAccessType != 2)) {
            return false;
        }
        switch (this.internetAccessType) {
            case 1:
                if (summary.length() > 0) {
                    summary.append(this.mContext.getString(R$string.wifi_status_recommend_network_congestion_second_item));
                }
                handled = true;
                break;
            case 2:
                if (summary.length() > 0) {
                    summary.append(this.mContext.getString(R$string.wifi_status_recommend_no_internet_access_second_item));
                }
                handled = true;
                break;
            case 3:
                if (summary.length() > 0) {
                    summary.append(this.mContext.getString(R$string.wifi_status_recommend_unauthorized_second_item));
                }
                handled = true;
                break;
            case 4:
                if (summary.length() > 0) {
                    if (this.networkSecurity >= 2) {
                        if (Global.getInt(this.mContext.getContentResolver(), "wifi_cloud_security_check", 0) != 1) {
                            securitySwitchOn = false;
                        }
                        if (securitySwitchOn) {
                            summary.append(this.mContext.getString(R$string.wifi_security_risks_second_item));
                            break;
                        }
                    }
                    switch (this.networkQosLevel) {
                        case 1:
                            summary.append(this.mContext.getString(R$string.wifi_status_recommend_internet_access_poor_second_item));
                            break;
                        case 2:
                            summary.append(this.mContext.getString(R$string.wifi_status_recommend_internet_access_normal_second_item));
                            break;
                        case 3:
                            summary.append(this.mContext.getString(R$string.wifi_status_recommend_internet_access_good_second_item));
                            break;
                        default:
                            summary.append(this.mContext.getString(R$string.wifi_status_recommend_internet_access_second_item));
                            break;
                    }
                }
                handled = true;
                break;
        }
        return handled;
    }

    protected int getCloudSecurityCheckByReflection(Object obj) {
        try {
            if (obj instanceof WifiConfiguration) {
                this.cloudSecurityCheck = ((Integer) WifiConfiguration.class.getDeclaredField("cloudSecurityCheck").get(obj)).intValue();
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return this.cloudSecurityCheck;
    }

    protected boolean appendCloudSecurityCheckSummary(StringBuilder summary) {
        boolean isWifiCloudSecurityCheckOn = true;
        if (!isCloudSecurityCheckDangerous() || this.mConfig == null) {
            return false;
        }
        if (Global.getInt(this.mContext.getContentResolver(), "wifi_cloud_security_check", 0) != 1) {
            isWifiCloudSecurityCheckOn = false;
        }
        if (!isWifiCloudSecurityCheckOn || summary.length() <= 0) {
            return false;
        }
        summary.append(this.mContext.getString(R$string.wifi_security_risks_second_item));
        return true;
    }

    protected boolean getIsHiLinkNetworkByReflection(Object obj) {
        if ((obj instanceof ScanResult) && reviseIsHiLinkNetwork((ScanResult) obj)) {
            return this.isHiLinkNetwork;
        }
        if (this.isHiLinkNetwork && this.wpsAvailable) {
            return this.isHiLinkNetwork;
        }
        try {
            if (obj instanceof ScanResult) {
                this.isHiLinkNetwork = ((Boolean) ScanResult.class.getDeclaredField("isHiLinkNetwork").get(obj)).booleanValue();
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return this.isHiLinkNetwork;
    }

    protected boolean getIsHiLinkNetworkFromScanResult(ScanResult result) {
        boolean isHiLinkNetworkLocal = false;
        if (result != null) {
            try {
                isHiLinkNetworkLocal = ((Boolean) ScanResult.class.getDeclaredField("isHiLinkNetwork").get(result)).booleanValue();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return isHiLinkNetworkLocal;
    }

    public boolean isHiLinkNetwork() {
        return this.isHiLinkNetwork;
    }

    public void setHiLinkNetwork(boolean paramIsHiLinkNetwork) {
        this.isHiLinkNetwork = paramIsHiLinkNetwork;
    }

    public String getNonNullBssid() {
        String bssidStr = this.nonNullBssid;
        if (TextUtils.isEmpty(bssidStr)) {
            return this.bssid;
        }
        return bssidStr;
    }

    protected boolean reviseIsHiLinkNetwork(ScanResult result) {
        boolean handled = false;
        if (result == null) {
            return false;
        }
        ConnectivityManager connectivity = (ConnectivityManager) getContext().getSystemService("connectivity");
        boolean wifiConnected = false;
        if (connectivity != null) {
            NetworkInfo networkInfo = connectivity.getNetworkInfo(1);
            if (networkInfo != null) {
                wifiConnected = networkInfo.isConnected();
            }
        }
        if (wifiConnected) {
            WifiInfo wifiInfo = ((WifiManager) getContext().getSystemService("wifi")).getConnectionInfo();
            if (wifiInfo != null && this.ssid.equals(WifiInfo.removeDoubleQuotes(wifiInfo.getSSID()))) {
                if (TextUtils.equals(result.BSSID, wifiInfo.getBSSID())) {
                    this.isHiLinkNetwork = getIsHiLinkNetworkFromScanResult(result);
                    handled = true;
                }
            }
        }
        return handled;
    }
}

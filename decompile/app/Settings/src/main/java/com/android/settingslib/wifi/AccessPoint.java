package com.android.settingslib.wifi;

import android.app.AppGlobals;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.NetworkSelectionStatus;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TtsSpan.VerbatimBuilder;
import android.util.Log;
import android.util.LruCache;
import com.android.settings.deviceinfo.HwCustMSimSubscriptionStatusTabFragmentImpl;
import com.android.settingslib.R$array;
import com.android.settingslib.R$string;
import com.android.settingslib.WirelessUtils;
import java.util.ArrayList;

public class AccessPoint extends AccessPointHwBase implements Comparable<AccessPoint> {
    private boolean foundInScanResult = false;
    private AccessPointListener mAccessPointListener;
    private final Context mContext;
    private WifiInfo mInfo;
    private NetworkInfo mNetworkInfo;
    private int mRssi = HwCustMSimSubscriptionStatusTabFragmentImpl.INVALID;
    public LruCache<String, ScanResult> mScanResultCache = new LruCache(32);
    private long mSeen = 0;
    private Object mTag;
    private int pskType = 0;

    public interface AccessPointListener {
        void onAccessPointChanged(AccessPoint accessPoint);

        void onLevelChanged(AccessPoint accessPoint);
    }

    public void setFoundInScanResult(boolean scanResult) {
        this.foundInScanResult = scanResult;
    }

    public AccessPoint(Context context, Bundle savedState) {
        super(context);
        this.mContext = context;
        this.mConfig = (WifiConfiguration) savedState.getParcelable("key_config");
        if (this.mConfig != null) {
            loadConfig(this.mConfig);
        }
        if (savedState.containsKey("key_ssid")) {
            this.ssid = savedState.getString("key_ssid");
        }
        if (savedState.containsKey("key_ori_ssid")) {
            String savedOriSsid = savedState.getString("key_ori_ssid");
            if (!TextUtils.isEmpty(savedOriSsid)) {
                this.oriSsid = savedOriSsid;
            }
        }
        if (savedState.containsKey("key_security")) {
            this.security = savedState.getInt("key_security");
        }
        if (savedState.containsKey("key_psktype")) {
            this.pskType = savedState.getInt("key_psktype");
        }
        this.mInfo = (WifiInfo) savedState.getParcelable("key_wifiinfo");
        if (savedState.containsKey("key_networkinfo")) {
            this.mNetworkInfo = (NetworkInfo) savedState.getParcelable("key_networkinfo");
        }
        if (savedState.containsKey("key_scanresultcache")) {
            ArrayList<ScanResult> scanResultArrayList = savedState.getParcelableArrayList("key_scanresultcache");
            this.mScanResultCache.evictAll();
            for (ScanResult result : scanResultArrayList) {
                this.mScanResultCache.put(result.BSSID, result);
            }
        }
        update(this.mConfig, this.mInfo, this.mNetworkInfo);
        this.mRssi = getRssi();
        this.mSeen = getSeen();
    }

    AccessPoint(Context context, ScanResult result) {
        super(context);
        this.mContext = context;
        initWithScanResult(result);
    }

    AccessPoint(Context context, WifiConfiguration config) {
        super(context);
        this.mContext = context;
        loadConfig(config);
    }

    public int compareTo(@NonNull AccessPoint other) {
        boolean z = false;
        if (Secure.getInt(this.mContext.getContentResolver(), "wifipro_recommending_access_points", 0) == 1) {
            z = true;
        }
        this.mIsRecommendingAccessPoints = z;
        if (!this.mIsRecommendingAccessPoints) {
            if (isActive() && !other.isActive()) {
                return -1;
            }
            if (!isActive() && other.isActive()) {
                return 1;
            }
        }
        if (this.mRssi != HwCustMSimSubscriptionStatusTabFragmentImpl.INVALID && other.mRssi == HwCustMSimSubscriptionStatusTabFragmentImpl.INVALID) {
            return -1;
        }
        if (this.mRssi == HwCustMSimSubscriptionStatusTabFragmentImpl.INVALID && other.mRssi != HwCustMSimSubscriptionStatusTabFragmentImpl.INVALID) {
            return 1;
        }
        int internetAccessTypeDiff = other.internetAccessType - this.internetAccessType;
        if (internetAccessTypeDiff != 0) {
            return internetAccessTypeDiff;
        }
        int networkQosLevelDiff = other.networkQosLevel - this.networkQosLevel;
        if (networkQosLevelDiff != 0) {
            return networkQosLevelDiff;
        }
        if (this.networkId != -1 && other.networkId == -1) {
            return -1;
        }
        if (this.networkId == -1 && other.networkId != -1) {
            return 1;
        }
        int difference = WirelessUtils.calculateSignalLevelUnrevised(other.mRssi) - WirelessUtils.calculateSignalLevelUnrevised(this.mRssi);
        if (difference != 0) {
            return difference;
        }
        int securityDiff = other.security - this.security;
        if (securityDiff != 0) {
            return securityDiff;
        }
        return this.ssid.compareToIgnoreCase(other.ssid);
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (!(other instanceof AccessPoint)) {
            return false;
        }
        if (compareTo((AccessPoint) other) == 0) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        int result = 0;
        if (this.mInfo != null) {
            result = (this.mInfo.hashCode() * 13) + 0;
        }
        return ((result + (this.mRssi * 19)) + (this.networkId * 23)) + (this.ssid.hashCode() * 29);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder().append("AccessPoint(").append(this.ssid);
        if (isSaved()) {
            builder.append(',').append("saved");
        }
        if (isActive()) {
            builder.append(',').append("active");
        }
        if (isEphemeral()) {
            builder.append(',').append("ephemeral");
        }
        if (isConnectable()) {
            builder.append(',').append("connectable");
        }
        if (this.security != 0) {
            builder.append(',').append(securityToString(this.security, this.pskType));
        }
        return builder.append(')').toString();
    }

    public boolean matches(ScanResult result) {
        return this.ssid.equals(result.SSID) && this.security == getSecurity(result);
    }

    public boolean matches(WifiConfiguration config) {
        boolean z = true;
        if (config.isPasspoint() && this.mConfig != null && this.mConfig.isPasspoint()) {
            return config.FQDN.equals(this.mConfig.providerFriendlyName);
        }
        if (!this.ssid.equals(removeDoubleQuotes(config.SSID)) || this.security != getSecurity(config)) {
            z = false;
        } else if (!(this.mConfig == null || this.mConfig.shared == config.shared)) {
            z = false;
        }
        return z;
    }

    public WifiConfiguration getConfig() {
        return this.mConfig;
    }

    public void clearConfig() {
        this.mConfig = null;
        this.networkId = -1;
    }

    public WifiInfo getInfo() {
        return this.mInfo;
    }

    public int getLevel() {
        if (this.mRssi == HwCustMSimSubscriptionStatusTabFragmentImpl.INVALID) {
            return -1;
        }
        return WirelessUtils.calculateSignalLevel(this.mRssi);
    }

    public int getRssi() {
        int rssi = Integer.MIN_VALUE;
        for (ScanResult result : this.mScanResultCache.snapshot().values()) {
            if (result.level > rssi) {
                rssi = result.level;
            }
        }
        return rssi;
    }

    public long getSeen() {
        long seen = 0;
        for (ScanResult result : this.mScanResultCache.snapshot().values()) {
            if (result.timestamp > seen) {
                seen = result.timestamp;
            }
        }
        return seen;
    }

    public NetworkInfo getNetworkInfo() {
        return this.mNetworkInfo;
    }

    public int getSecurity() {
        return this.security;
    }

    public String getSecurityString(boolean concise) {
        Context context = this.mContext;
        String string;
        if (this.mConfig == null || !this.mConfig.isPasspoint()) {
            switch (this.security) {
                case 1:
                    if (concise) {
                        string = context.getString(R$string.wifi_security_short_wep);
                    } else {
                        string = context.getString(R$string.wifi_security_wep);
                    }
                    return string;
                case 2:
                    switch (this.pskType) {
                        case 1:
                            if (concise) {
                                string = context.getString(R$string.wifi_security_short_wpa);
                            } else {
                                string = context.getString(R$string.wifi_security_wpa);
                            }
                            return string;
                        case 2:
                            if (concise) {
                                string = context.getString(R$string.wifi_security_short_wpa2);
                            } else {
                                string = context.getString(R$string.wifi_security_wpa2);
                            }
                            return string;
                        case 3:
                            if (concise) {
                                string = context.getString(R$string.wifi_security_short_wpa_wpa2);
                            } else {
                                string = context.getString(R$string.wifi_security_wpa_wpa2);
                            }
                            return string;
                        default:
                            if (concise) {
                                string = context.getString(R$string.wifi_security_short_psk_generic);
                            } else {
                                string = context.getString(R$string.wifi_security_psk_generic);
                            }
                            return string;
                    }
                case 3:
                    if (concise) {
                        string = context.getString(R$string.wifi_security_short_eap);
                    } else {
                        string = context.getString(R$string.wifi_security_eap);
                    }
                    return string;
                case 4:
                    return context.getString(R$string.wifi_security_wapi_psk);
                case 5:
                    return context.getString(R$string.wifi_security_wapi_cert);
                default:
                    if (concise) {
                        string = "";
                    } else {
                        string = context.getString(R$string.wifi_security_none);
                    }
                    return string;
            }
        }
        if (concise) {
            string = context.getString(R$string.wifi_security_short_eap);
        } else {
            string = context.getString(R$string.wifi_security_eap);
        }
        return string;
    }

    public String getSsidStr() {
        return this.ssid;
    }

    public String getBssid() {
        return this.bssid;
    }

    public CharSequence getSsid() {
        SpannableString str = new SpannableString(this.ssid);
        str.setSpan(new VerbatimBuilder(this.ssid).build(), 0, this.ssid.length(), 18);
        return str;
    }

    public String getConfigName() {
        if (this.mConfig == null || !this.mConfig.isPasspoint()) {
            return this.ssid;
        }
        return this.mConfig.providerFriendlyName;
    }

    public DetailedState getDetailedState() {
        return this.mNetworkInfo != null ? this.mNetworkInfo.getDetailedState() : null;
    }

    public String getSavedNetworkSummary() {
        if (this.mConfig != null) {
            String systemName = this.mContext.getPackageManager().getNameForUid(1000);
            int userId = UserHandle.getUserId(this.mConfig.creatorUid);
            ApplicationInfo appInfo = null;
            if (this.mConfig.creatorName == null || !this.mConfig.creatorName.equals(systemName)) {
                try {
                    appInfo = AppGlobals.getPackageManager().getApplicationInfo(this.mConfig.creatorName, 0, userId);
                } catch (RemoteException e) {
                }
            } else {
                appInfo = this.mContext.getApplicationInfo();
            }
            if (!(appInfo == null || appInfo.packageName.equals(this.mContext.getString(R$string.settings_package)) || appInfo.packageName.equals(this.mContext.getString(R$string.certinstaller_package)))) {
                return this.mContext.getString(R$string.saved_network, new Object[]{appInfo.loadLabel(pm)});
            }
        }
        return "";
    }

    public String getSettingsSummary(boolean linkPlusEnabled) {
        this.mIsRecommendingAccessPoints = Secure.getInt(this.mContext.getContentResolver(), "wifipro_recommending_access_points", 0) == 1;
        StringBuilder summary = new StringBuilder();
        if (!this.mIsRecommendingAccessPoints && isActive()) {
            buildSummaryActive(summary, linkPlusEnabled);
            buildCloudSecurityCheckSummary(summary);
        } else if (this.mConfig == null || !this.mConfig.isPasspoint()) {
            if (this.mConfig != null && !this.mConfig.getNetworkSelectionStatus().isNetworkEnabled()) {
                switch (this.mConfig.getNetworkSelectionStatus().getNetworkSelectionDisableReason()) {
                    case -1:
                        summary.append(this.mContext.getString(R$string.wifi_disabled_generic));
                        break;
                    case 2:
                        if (System.getInt(this.mContext.getContentResolver(), "wifi_association_reject_status_code", 1) != 17) {
                            summary.append(this.mContext.getString(R$string.wifi_assoc_reject_common));
                            break;
                        }
                        summary.append(this.mContext.getString(R$string.wifi_assoc_reject_server_full));
                        break;
                    case 3:
                        if (this.security != 0) {
                            summary.append(this.mContext.getString(R$string.wifi_disabled_password_failure));
                            break;
                        }
                        summary.append(this.mContext.getString(R$string.wifi_authentication_failure));
                        break;
                    case 4:
                    case 5:
                        summary.append(this.mContext.getString(R$string.wifi_disabled_network_failure));
                        break;
                    default:
                        Log.d("SettingsLib.AccessPoint", "WifiConfiguration status disabled,into default.");
                        summary.append(this.mContext.getString(R$string.wifi_disabled_generic));
                        break;
                }
            } else if (this.mRssi == Integer.MAX_VALUE) {
                summary.append(this.mContext.getString(R$string.wifi_not_in_range));
            } else {
                if (this.mConfig != null && !this.isTempCreated) {
                    summary.append(this.mContext.getString(R$string.wifi_remembered));
                } else if (this.security == 0) {
                    summary.append(this.mContext.getString(R$string.hotspot_security_open));
                }
                appendSecuritySummary(summary);
                boolean handled = appendCloudSecurityCheckSummary(summary);
                if (!handled) {
                    handled = buildRecommendNetworkSummary(summary, linkPlusEnabled);
                }
                if (!handled) {
                    appendWpsSummary(summary);
                }
            }
        } else {
            summary.append(String.format(this.mContext.getString(R$string.available_via_passpoint), new Object[]{this.mConfig.providerFriendlyName}));
        }
        if (WifiTracker.sVerboseLogging > 0) {
            if (!(this.mInfo == null || this.mNetworkInfo == null)) {
                summary.append(" f=").append(Integer.toString(this.mInfo.getFrequency()));
            }
            summary.append(" ").append(getVisibilityStatus());
            if (!(this.mConfig == null || this.mConfig.getNetworkSelectionStatus().isNetworkEnabled())) {
                summary.append(" (").append(this.mConfig.getNetworkSelectionStatus().getNetworkStatusString());
                if (this.mConfig.getNetworkSelectionStatus().getDisableTime() > 0) {
                    long diff = (System.currentTimeMillis() - this.mConfig.getNetworkSelectionStatus().getDisableTime()) / 1000;
                    long sec = diff % 60;
                    long min = (diff / 60) % 60;
                    long hour = (min / 60) % 60;
                    summary.append(", ");
                    if (hour > 0) {
                        summary.append(Long.toString(hour)).append("h ");
                    }
                    summary.append(Long.toString(min)).append("m ");
                    summary.append(Long.toString(sec)).append("s ");
                }
                summary.append(")");
            }
            if (this.mConfig != null) {
                NetworkSelectionStatus networkStatus = this.mConfig.getNetworkSelectionStatus();
                for (int index = 0; index < 11; index++) {
                    if (networkStatus.getDisableReasonCounter(index) != 0) {
                        summary.append(" ").append(NetworkSelectionStatus.getNetworkDisableReasonString(index)).append("=").append(networkStatus.getDisableReasonCounter(index));
                    }
                }
            }
        }
        return summary.toString();
    }

    private String getVisibilityStatus() {
        StringBuilder visibility = new StringBuilder();
        StringBuilder scans24GHz = null;
        StringBuilder scans5GHz = null;
        Object obj = null;
        long now = System.currentTimeMillis();
        WifiInfo wifiInfo = this.mInfo;
        if (wifiInfo != null) {
            obj = wifiInfo.getBSSID();
            if (obj != null) {
                visibility.append(" ").append(obj);
            }
            visibility.append(" rssi=").append(wifiInfo.getRssi());
            visibility.append(" ");
            visibility.append(" score=").append(wifiInfo.score);
            visibility.append(String.format(" tx=%.1f,", new Object[]{Double.valueOf(wifiInfo.txSuccessRate)}));
            visibility.append(String.format("%.1f,", new Object[]{Double.valueOf(wifiInfo.txRetriesRate)}));
            visibility.append(String.format("%.1f ", new Object[]{Double.valueOf(wifiInfo.txBadRate)}));
            visibility.append(String.format("rx=%.1f", new Object[]{Double.valueOf(wifiInfo.rxSuccessRate)}));
        }
        int rssi5 = WifiConfiguration.INVALID_RSSI;
        int rssi24 = WifiConfiguration.INVALID_RSSI;
        int num5 = 0;
        int num24 = 0;
        int n24 = 0;
        int n5 = 0;
        for (ScanResult result : this.mScanResultCache.snapshot().values()) {
            if (result.frequency >= 4900 && result.frequency <= 5900) {
                num5++;
            } else if (result.frequency >= 2400 && result.frequency <= 2500) {
                num24++;
            }
            if (result.frequency >= 4900 && result.frequency <= 5900) {
                if (result.level > rssi5) {
                    rssi5 = result.level;
                }
                if (n5 < 4) {
                    if (scans5GHz == null) {
                        scans5GHz = new StringBuilder();
                    }
                    scans5GHz.append(" \n{").append(result.BSSID);
                    if (obj != null && result.BSSID.equals(obj)) {
                        scans5GHz.append("*");
                    }
                    scans5GHz.append("=").append(result.frequency);
                    scans5GHz.append(",").append(result.level);
                    scans5GHz.append("}");
                    n5++;
                }
            } else if (result.frequency >= 2400 && result.frequency <= 2500) {
                if (result.level > rssi24) {
                    rssi24 = result.level;
                }
                if (n24 < 4) {
                    if (scans24GHz == null) {
                        scans24GHz = new StringBuilder();
                    }
                    scans24GHz.append(" \n{").append(result.BSSID);
                    if (obj != null && result.BSSID.equals(obj)) {
                        scans24GHz.append("*");
                    }
                    scans24GHz.append("=").append(result.frequency);
                    scans24GHz.append(",").append(result.level);
                    scans24GHz.append("}");
                    n24++;
                }
            }
        }
        visibility.append(" [");
        if (num24 > 0) {
            visibility.append("(").append(num24).append(")");
            if (n24 > 4) {
                visibility.append("max=").append(rssi24);
                if (scans24GHz != null) {
                    visibility.append(",").append(scans24GHz.toString());
                }
            } else if (scans24GHz != null) {
                visibility.append(scans24GHz.toString());
            }
        }
        visibility.append(";");
        if (num5 > 0) {
            visibility.append("(").append(num5).append(")");
            if (n5 > 4) {
                visibility.append("max=").append(rssi5);
                if (scans5GHz != null) {
                    visibility.append(",").append(scans5GHz.toString());
                }
            } else if (scans5GHz != null) {
                visibility.append(scans5GHz.toString());
            }
        }
        visibility.append("]");
        return visibility.toString();
    }

    public boolean isActive() {
        if (this.mNetworkInfo != null) {
            return (this.networkId == -1 && this.mNetworkInfo.getState() == State.DISCONNECTED) ? false : true;
        } else {
            return false;
        }
    }

    public boolean isConnectable() {
        return getLevel() != -1 && getDetailedState() == null;
    }

    public boolean isEphemeral() {
        if (this.mInfo == null || !this.mInfo.isEphemeral() || this.mNetworkInfo == null || this.mNetworkInfo.getState() == State.DISCONNECTED) {
            return false;
        }
        return true;
    }

    public boolean isPasspoint() {
        return this.mConfig != null ? this.mConfig.isPasspoint() : false;
    }

    private boolean isInfoForThisAccessPoint(WifiConfiguration config, WifiInfo info) {
        if (!isPasspoint() && this.networkId != -1) {
            return this.networkId == info.getNetworkId();
        } else if (config != null) {
            return matches(config);
        } else {
            return this.ssid.equals(removeDoubleQuotes(info.getSSID()));
        }
    }

    public boolean isSaved() {
        return this.networkId != -1;
    }

    public Object getTag() {
        return this.mTag;
    }

    public void setTag(Object tag) {
        this.mTag = tag;
    }

    public void generateOpenNetworkConfig() {
        if (this.security != 0) {
            throw new IllegalStateException();
        } else if (this.mConfig == null) {
            WifiConfiguration localConfig = new WifiConfiguration();
            localConfig.SSID = convertToQuotedString(this.ssid);
            localConfig.oriSsid = this.oriSsid;
            localConfig.allowedKeyManagement.set(0);
            this.mConfig = localConfig;
        }
    }

    void loadConfig(WifiConfiguration config) {
        if (config.isPasspoint()) {
            this.ssid = config.providerFriendlyName;
        } else {
            this.ssid = config.SSID == null ? "" : removeDoubleQuotes(config.SSID);
        }
        if (config.oriSsid != null) {
            this.oriSsid = config.oriSsid;
        }
        this.bssid = config.BSSID;
        this.security = getSecurity(config);
        this.networkId = config.networkId;
        this.mConfig = config;
        updateRecommendProperties(config);
    }

    private void initWithScanResult(ScanResult result) {
        this.ssid = result.SSID;
        String oriSsidString = getOriSsidFromWifiSsid(result.wifiSsid);
        if (!(result.wifiSsid == null || TextUtils.isEmpty(oriSsidString))) {
            this.oriSsid = oriSsidString;
        }
        this.bssid = result.BSSID;
        this.nonNullBssid = result.BSSID;
        this.security = getSecurity(result);
        this.wpsAvailable = this.security != 3 ? result.capabilities.contains("WPS") : false;
        if (this.security == 2) {
            this.pskType = getPskType(result);
        }
        this.mRssi = result.level;
        this.mSeen = result.timestamp;
        updateRecommendProperties(result);
    }

    public void saveWifiState(Bundle savedState) {
        if (this.ssid != null) {
            savedState.putString("key_ssid", getSsidStr());
        }
        savedState.putInt("key_security", this.security);
        savedState.putInt("key_psktype", this.pskType);
        if (this.mConfig != null) {
            savedState.putParcelable("key_config", this.mConfig);
        }
        savedState.putParcelable("key_wifiinfo", this.mInfo);
        savedState.putParcelableArrayList("key_scanresultcache", new ArrayList(this.mScanResultCache.snapshot().values()));
        if (this.mNetworkInfo != null) {
            savedState.putParcelable("key_networkinfo", this.mNetworkInfo);
        }
    }

    public void setListener(AccessPointListener listener) {
        this.mAccessPointListener = listener;
    }

    boolean update(ScanResult result) {
        boolean z = false;
        if (!matches(result)) {
            return false;
        }
        this.mScanResultCache.get(result.BSSID);
        this.mScanResultCache.put(result.BSSID, result);
        if (!this.wpsAvailable) {
            if (this.security != 3) {
                z = result.capabilities.contains("WPS");
            }
            this.wpsAvailable = z;
        }
        if (TextUtils.isEmpty(this.nonNullBssid) || getIsHiLinkNetworkFromScanResult(result)) {
            this.nonNullBssid = result.BSSID;
        }
        int oldLevel = getLevel();
        int oldRssi = getRssi();
        this.mSeen = getSeen();
        if (getDetailedState() != DetailedState.CONNECTED) {
            this.mRssi = (getRssi() + oldRssi) / 2;
            int newLevel = getLevel();
            if (!(newLevel <= 0 || newLevel == oldLevel || this.mAccessPointListener == null)) {
                this.mAccessPointListener.onLevelChanged(this);
            }
        }
        if (this.security == 2) {
            this.pskType = getPskType(result);
        }
        updateRecommendProperties(result);
        if (this.mAccessPointListener != null) {
            this.mAccessPointListener.onAccessPointChanged(this);
        }
        return true;
    }

    boolean update(WifiConfiguration config, WifiInfo info, NetworkInfo networkInfo) {
        boolean reorder = false;
        if (info != null && isInfoForThisAccessPoint(config, info)) {
            reorder = this.mInfo == null;
            boolean connected = false;
            if (networkInfo != null && networkInfo.getType() == 1) {
                connected = networkInfo.isConnected();
            }
            if (info.getSupplicantState() == SupplicantState.COMPLETED || r0) {
                this.mRssi = info.getRssi();
            }
            this.mInfo = info;
            this.mNetworkInfo = networkInfo;
            if (this.mAccessPointListener != null) {
                this.mAccessPointListener.onAccessPointChanged(this);
            }
        } else if (this.mInfo != null) {
            reorder = true;
            this.mInfo = null;
            this.mNetworkInfo = null;
            if (this.mAccessPointListener != null) {
                this.mAccessPointListener.onAccessPointChanged(this);
            }
        }
        return reorder;
    }

    void update(WifiConfiguration config) {
        this.mConfig = config;
        this.networkId = config.networkId;
        updateRecommendProperties(config);
        if (this.mAccessPointListener != null) {
            this.mAccessPointListener.onAccessPointChanged(this);
        }
    }

    void setRssi(int rssi) {
        this.mRssi = rssi;
    }

    public boolean isNoInternetAccess() {
        if (this.internetAccessType == 2) {
            return true;
        }
        if (this.mConfig != null) {
            return this.mConfig.wifiProNoInternetAccess;
        }
        return false;
    }

    public int getNoInternetReason() {
        if (this.mConfig != null) {
            return this.mConfig.wifiProNoInternetReason;
        }
        return -1;
    }

    public boolean isNoHandoverNetwork() {
        if (this.mConfig != null) {
            return this.mConfig.wifiProNoHandoverNetwork;
        }
        return false;
    }

    private void buildNormalSummary(StringBuilder summary) {
        boolean z = false;
        if (this.mConfig == null || !this.mConfig.isPasspoint()) {
            Context context = this.mContext;
            DetailedState detailedState = getDetailedState();
            if (this.mInfo != null) {
                z = this.mInfo.isEphemeral();
            }
            summary.append(getSummary(context, detailedState, z));
            return;
        }
        summary.append(getSummary(this.mContext, getDetailedState(), false, this.mConfig.providerFriendlyName));
    }

    private void buildSummaryActive(StringBuilder summary, boolean linkPlusEnabled) {
        Log.d("SettingsLib.AccessPoint", "buildSummaryActive detailState = " + getDetailedState() + ", isNoInternetAccess = " + isNoInternetAccess() + ", isNoHandoverNetwork = " + isNoHandoverNetwork() + ", getNoInternetReason = " + getNoInternetReason());
        boolean hasWifiProProperty = SystemProperties.getBoolean("ro.config.hw_wifipro_enable", false);
        if (getDetailedState() == DetailedState.CONNECTED) {
            if (!isNoInternetAccess() || !hasWifiProProperty) {
                buildNormalSummary(summary);
                if (linkPlusEnabled) {
                    buildRecommendNetworkSummary(summary, linkPlusEnabled);
                }
            } else if (isNoHandoverNetwork() && linkPlusEnabled) {
                summary.append(this.mContext.getString(R$string.wifi_status_connected_offline_unswitchable));
            } else {
                int reason = getNoInternetReason();
                if (reason == 0) {
                    summary.append(this.mContext.getString(R$string.wifi_status_connected_no_internet_access));
                } else if (reason == 1) {
                    summary.append(this.mContext.getString(R$string.wifi_status_connected_offline_unauthorized2));
                } else {
                    Log.w("SettingsLib.AccessPoint", "Unexpected no-internet reason = " + reason);
                    summary.append(this.mContext.getString(R$string.wifi_status_connected_no_internet_access));
                }
            }
        } else if (getDetailedState() != DetailedState.VERIFYING_POOR_LINK) {
            buildNormalSummary(summary);
        } else if (isNoInternetAccess()) {
            summary.append(this.mContext.getString(R$string.wifi_status_offline_on_mobile_data));
        } else {
            summary.append(this.mContext.getString(R$string.wifi_status_poor_connection_on_mobile_data));
        }
    }

    public static String getSummary(Context context, String ssid, DetailedState state, boolean isEphemeral, String passpointProvider) {
        if (state == null) {
            return "";
        }
        if (state == DetailedState.CONNECTED && ssid == null) {
            if (!TextUtils.isEmpty(passpointProvider)) {
                return String.format(context.getString(R$string.connected_via_passpoint), new Object[]{passpointProvider});
            } else if (isEphemeral) {
                return context.getString(R$string.connected_via_wfa);
            }
        }
        String[] formats = context.getResources().getStringArray(ssid == null ? R$array.wifi_status : R$array.wifi_status_with_ssid);
        int index = state.ordinal();
        if (index >= formats.length || formats[index].length() == 0) {
            return "";
        }
        return String.format(formats[index], new Object[]{ssid});
    }

    public static String getSummary(Context context, DetailedState state, boolean isEphemeral) {
        return getSummary(context, null, state, isEphemeral, null);
    }

    public static String getSummary(Context context, DetailedState state, boolean isEphemeral, String passpointProvider) {
        return getSummary(context, null, state, isEphemeral, passpointProvider);
    }

    public static String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    }

    private static int getPskType(ScanResult result) {
        boolean wpa = result.capabilities.contains("WPA-PSK");
        boolean wpa2 = result.capabilities.contains("WPA2-PSK");
        if (wpa2 && wpa) {
            return 3;
        }
        if (wpa2) {
            return 2;
        }
        if (wpa) {
            return 1;
        }
        Log.w("SettingsLib.AccessPoint", "Received abnormal flag string: " + result.capabilities);
        return 0;
    }

    public static int getSecurity(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return 1;
        }
        if (result.capabilities.contains("WAPI-PSK") || result.capabilities.contains("QUALCOMM-WAPI-PSK")) {
            return 4;
        }
        if (result.capabilities.contains("WAPI-CERT") || result.capabilities.contains("QUALCOMM-WAPI-CERT")) {
            return 5;
        }
        if (result.capabilities.contains("PSK")) {
            return 2;
        }
        if (result.capabilities.contains("EAP")) {
            return 3;
        }
        return 0;
    }

    public static int getSecurity(WifiConfiguration config) {
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

    public static String securityToString(int security, int pskType) {
        if (security == 1) {
            return "WEP";
        }
        if (security == 2) {
            if (pskType == 1) {
                return "WPA";
            }
            if (pskType == 2) {
                return "WPA2";
            }
            if (pskType == 3) {
                return "WPA_WPA2";
            }
            return "PSK";
        } else if (security == 3) {
            return "EAP";
        } else {
            return "NONE";
        }
    }

    static String removeDoubleQuotes(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        int length = string.length();
        if (length > 1 && string.charAt(0) == '\"' && string.charAt(length - 1) == '\"') {
            return string.substring(1, length - 1);
        }
        return string;
    }

    protected void buildCloudSecurityCheckSummary(StringBuilder summary) {
        boolean isWifiCloudSecurityCheckOn = true;
        if (getDetailedState() == DetailedState.CONNECTED && isCloudSecurityCheckDangerous()) {
            if (Global.getInt(this.mContext.getContentResolver(), "wifi_cloud_security_check", 0) != 1) {
                isWifiCloudSecurityCheckOn = false;
            }
            if (isWifiCloudSecurityCheckOn) {
                summary.delete(0, summary.length());
                summary.append(this.mContext.getString(R$string.wifi_status_connected_security_risks));
            }
        }
    }
}

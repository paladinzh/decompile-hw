package android.net.wifi;

import android.content.Context;
import android.net.ProxyInfo;
import java.util.HashMap;
import java.util.List;

public class DummyHwInnerWifiManager implements HwInnerWifiManager {
    private static HwInnerWifiManager mInstance = new DummyHwInnerWifiManager();

    public HwInnerWifiManager getDefault() {
        return mInstance;
    }

    public String getAppendSsidWithRandomUuid(WifiConfiguration config, Context context) {
        return ProxyInfo.LOCAL_EXCL_LIST;
    }

    public int calculateSignalLevelHW(int rssi) {
        return WifiManager.calculateSignalLevel(rssi, 5);
    }

    public String getWpaSuppConfig() {
        return ProxyInfo.LOCAL_EXCL_LIST;
    }

    public boolean setWifiEnterpriseConfigEapMethod(int eapMethod, HashMap<String, String> hashMap) {
        return false;
    }

    public boolean getHwMeteredHint(Context context) {
        return false;
    }

    public PPPOEInfo getPPPOEInfo() {
        return null;
    }

    public void startPPPOE(PPPOEConfig config) {
    }

    public void stopPPPOE() {
    }

    public List<String> getApLinkedStaList() {
        return null;
    }

    public void setSoftapMacFilter(String macFilter) {
    }

    public void setSoftapDisassociateSta(String mac) {
    }

    public void userHandoverWifi() {
    }

    public int[] getChannelListFor5G() {
        return null;
    }

    public void setWifiApEvaluateEnabled(boolean enabled) {
    }

    public byte[] fetchWifiSignalInfoForVoWiFi() {
        return null;
    }

    public boolean setVoWifiDetectMode(WifiDetectConfInfo info) {
        return false;
    }

    public WifiDetectConfInfo getVoWifiDetectMode() {
        return null;
    }

    public boolean setVoWifiDetectPeriod(int period) {
        return false;
    }

    public int getVoWifiDetectPeriod() {
        return -1;
    }

    public boolean isSupportVoWifiDetect() {
        return false;
    }

    public void enableHiLinkHandshake(boolean uiEnable, String bssid) {
    }
}

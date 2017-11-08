package com.android.server.wifi;

import android.content.Context;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.util.Log;
import java.io.File;

public class HwWifiDFTUtilImpl implements HwWifiDFTUtil {
    public static final short SUB_EVENT_DUALBAND_HANDOVER_PINGPONG = (short) 4;
    public static final short SUB_EVENT_DUALBAND_HANDOVER_SNAPSHOP = (short) 5;
    public static final short SUB_EVENT_DUALBAND_HANDOVER_TOO_SLOW = (short) 1;
    public static final short SUB_EVENT_DUALBAND_HANDOVER_TO_BAD_5G = (short) 2;
    public static final short SUB_EVENT_DUALBAND_HANDOVER_USER_REJECT = (short) 3;
    public static final short SUB_EVENT_DUALBAND_OTHER_REASON = (short) 0;
    private static final String TAG = "HwWifiDFTUtilImpl";
    private static HwWifiDFTUtil hwWifiDFTUtil = new HwWifiDFTUtilImpl();
    private static int mScanAlwaysSwCnt = 0;
    private static int mWifiNotifationSwCnt = 0;
    private static int mWifiProSwcnt = 0;
    private static int mWifiSleepSwCnt = 0;
    private static int mWifiToPdpSwCnt = 0;
    private Context mContext = null;
    public short mOpenCloseFailed = (short) -1;
    private int mSettingNetAvailableNotify = -1;
    private int mSettingScanAlways = -1;
    private int mSettingSleepPolicy = -1;
    private int mSettingWiFiProState = -1;
    private int mSettingWiFiToPDP = -1;
    private int mSubErrorCloseSupplicantConnectFailed = 6;
    private int mSubErrorConnectSupplicantFailed = 5;
    private int mSubErrorDriverFailed = 2;
    private int mSubErrorFirmwareFailed = 3;
    public int mSubErrorOfOpenCloseFailed = -1;
    private int mSubErrorStartSupplicantFailed = 4;
    private int mSubErrorTimeOut = 1;
    private HwWifiCHRService mWifiCHRService = null;
    public short mWifiProExceptionDualbandSubError = (short) -1;

    public static HwWifiDFTUtil getDefault() {
        return hwWifiDFTUtil;
    }

    public void updateWifiScanAlwaysState(Context mContext) {
        if (mContext != null) {
            try {
                int wifiScanAlwaysState = Global.getInt(mContext.getContentResolver(), "wifi_scan_always_enabled", 0);
                if (this.mSettingScanAlways != wifiScanAlwaysState) {
                    mScanAlwaysSwCnt++;
                    this.mSettingScanAlways = wifiScanAlwaysState;
                }
            } catch (Exception e) {
                Log.e(TAG, "update the wifi scan state error. ");
            }
        }
    }

    public int getScanAlwaysSwCnt() {
        return mScanAlwaysSwCnt;
    }

    public void updateWifiSleepPolicyState(Context mContext) {
        if (mContext != null) {
            try {
                int wifiSleepPolicyState = Global.getInt(mContext.getContentResolver(), "wifi_sleep_policy", 0);
                if (this.mSettingSleepPolicy != wifiSleepPolicyState) {
                    mWifiSleepSwCnt++;
                    this.mSettingSleepPolicy = wifiSleepPolicyState;
                }
            } catch (Exception e) {
                Log.e(TAG, "update the wifi sleep policy state error. ");
            }
        }
    }

    public int getWifiSleepSwCnt() {
        return mWifiSleepSwCnt;
    }

    public void updateWifiNetworkNotificationState(Context mContext) {
        if (mContext != null) {
            try {
                int wifiNotificationState = Global.getInt(mContext.getContentResolver(), "wifi_networks_available_notification_on", 0);
                if (this.mSettingNetAvailableNotify != wifiNotificationState) {
                    mWifiNotifationSwCnt++;
                    this.mSettingNetAvailableNotify = wifiNotificationState;
                }
            } catch (Exception e) {
                Log.e(TAG, "update the wifi network notification state error. ");
            }
        }
    }

    public int getWifiNotifationSwCnt() {
        return mWifiNotifationSwCnt;
    }

    public void updateWifiToPdpState(Context mContext) {
        if (mContext != null) {
            try {
                int wifiToPdpState = System.getInt(mContext.getContentResolver(), "wifi_to_pdp", 0);
                if (this.mSettingWiFiToPDP != wifiToPdpState) {
                    mWifiToPdpSwCnt++;
                    this.mSettingWiFiToPDP = wifiToPdpState;
                }
            } catch (Exception e) {
                Log.e(TAG, "update the wifi to pdp state error. ");
            }
        }
    }

    public int getWifiToPdpSwCnt() {
        return mWifiToPdpSwCnt;
    }

    public void updateWifiProState(Context mContext) {
        if (mContext != null) {
            try {
                int wifiProState = System.getInt(mContext.getContentResolver(), "smart_network_switching", 0);
                if (this.mSettingWiFiProState != wifiProState) {
                    mWifiProSwcnt++;
                    this.mSettingWiFiProState = wifiProState;
                }
            } catch (Exception e) {
                Log.e(TAG, "update the wifipro state error. ");
            }
        }
    }

    public int getWifiProSwcnt() {
        return mWifiProSwcnt;
    }

    public void checkAndCreatWifiLogDir() {
        try {
            File file = new File("/data/log/wifi");
            if (file.exists() || !file.mkdirs()) {
                Log.e(TAG, "creat directory failed");
            } else {
                Log.d(TAG, "creat directory successful");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public short getAccessNetFailedCount() {
        HwArpVerifier arpVerifier = HwArpVerifier.getDefault();
        return (short) (arpVerifier != null ? arpVerifier.getAccessNetFailedCount() : 0);
    }

    public boolean getWifiProState() {
        this.mWifiCHRService = HwWifiCHRServiceImpl.getDefault();
        if (this.mWifiCHRService != null) {
            return this.mWifiCHRService.getWIFIProStatus() == 11;
        } else {
            return false;
        }
    }

    public boolean getWifiAlwaysScanState() {
        this.mWifiCHRService = HwWifiCHRServiceImpl.getDefault();
        if (this.mWifiCHRService != null) {
            return this.mWifiCHRService.getPersistedScanAlwaysAvailable() == 1;
        } else {
            return false;
        }
    }

    public boolean getWifiNetworkNotificationState() {
        this.mWifiCHRService = HwWifiCHRServiceImpl.getDefault();
        if (this.mWifiCHRService != null) {
            return this.mWifiCHRService.getWIFINetworkAvailableNotificationON() == 1;
        } else {
            return false;
        }
    }

    public byte getWifiSleepPolicyState() {
        this.mWifiCHRService = HwWifiCHRServiceImpl.getDefault();
        if (this.mWifiCHRService != null) {
            return (byte) this.mWifiCHRService.getWIFISleepPolicy();
        }
        return (byte) 0;
    }

    public byte getWifiToPdpState() {
        this.mWifiCHRService = HwWifiCHRServiceImpl.getDefault();
        if (this.mWifiCHRService != null) {
            return (byte) this.mWifiCHRService.getWIFITOPDP();
        }
        return (byte) 0;
    }

    public void updateWifiDFTEvent(int type, String subErrorCode) {
        switch (type) {
            case HwWifiCHRConstImpl.WIFI_OPEN_FAILED /*80*/:
                try {
                    this.mOpenCloseFailed = (short) type;
                    if (subErrorCode.equals("TIMEOUT")) {
                        this.mSubErrorOfOpenCloseFailed = this.mSubErrorTimeOut;
                        return;
                    } else if (subErrorCode.equals("DIRVER_FAILED")) {
                        this.mSubErrorOfOpenCloseFailed = this.mSubErrorDriverFailed;
                        return;
                    } else if (subErrorCode.equals("FIRMWARE_FAILED")) {
                        this.mSubErrorOfOpenCloseFailed = this.mSubErrorFirmwareFailed;
                        return;
                    } else if (subErrorCode.equals("START_SUPPLICANT_FAILED")) {
                        this.mSubErrorOfOpenCloseFailed = this.mSubErrorStartSupplicantFailed;
                        return;
                    } else if (subErrorCode.equals("CONNECT_SUPPLICANT_FAILED")) {
                        this.mSubErrorOfOpenCloseFailed = this.mSubErrorConnectSupplicantFailed;
                        return;
                    } else {
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            case HwWifiCHRConstImpl.WIFI_CLOSE_FAILED /*81*/:
                this.mOpenCloseFailed = (short) type;
                if (subErrorCode.equals("TIMEOUT")) {
                    this.mSubErrorOfOpenCloseFailed = this.mSubErrorTimeOut;
                    return;
                } else if (subErrorCode.equals("CLOSE_SUPPLICANT_CONNECT_FAILED")) {
                    this.mSubErrorOfOpenCloseFailed = this.mSubErrorCloseSupplicantConnectFailed;
                    return;
                } else {
                    return;
                }
            case HwWifiCHRStateManagerImpl.WIFI_WIFIPRO_DUALBAND_EXCEPTION_EVENT /*125*/:
                if (subErrorCode.equals("DUALBAND_HANDOVER_TOO_SLOW")) {
                    this.mWifiProExceptionDualbandSubError = (short) 1;
                    return;
                } else if (subErrorCode.equals("DUALBAND_HANDOVER_TO_BAD_5G")) {
                    this.mWifiProExceptionDualbandSubError = (short) 2;
                    return;
                } else if (subErrorCode.equals("DUALBAND_HANDOVER_USER_REJECT")) {
                    this.mWifiProExceptionDualbandSubError = (short) 3;
                    return;
                } else if (subErrorCode.equals("DUALBAND_HANDOVER_PINGPONG")) {
                    this.mWifiProExceptionDualbandSubError = (short) 4;
                    return;
                } else if (subErrorCode.equals("DUALBAND_HANDOVER_SNAPSHOP")) {
                    this.mWifiProExceptionDualbandSubError = (short) 5;
                    return;
                } else {
                    return;
                }
            default:
                return;
        }
    }

    public int getDFTEventType(int type) {
        switch (type) {
            case HwWifiCHRConstImpl.WIFI_OPEN_FAILED /*80*/:
            case HwWifiCHRConstImpl.WIFI_CLOSE_FAILED /*81*/:
                return 909002011;
            case HwWifiCHRConstImpl.WIFI_ABNORMAL_DISCONNECT /*85*/:
                return 909002012;
            case HwWifiCHRConstImpl.WIFI_SCAN_FAILED /*86*/:
                return 909002013;
            case HwWifiCHRConstImpl.WIFI_ACCESS_INTERNET_FAILED /*87*/:
                return 909002015;
            case 102:
                return 909002016;
            case HwWifiCHRConstImpl.WIFI_WIFIPRO_EXCEPTION_EVENT /*122*/:
                return 909002018;
            case HwWifiCHRStateManagerImpl.WIFI_WIFIPRO_DUALBAND_EXCEPTION_EVENT /*125*/:
                return 909002019;
            case 208:
                return 909002017;
            default:
                return -1;
        }
    }

    public void clearSwCnt() {
        mScanAlwaysSwCnt = 0;
        mWifiSleepSwCnt = 0;
        mWifiNotifationSwCnt = 0;
        mWifiToPdpSwCnt = 0;
        mWifiProSwcnt = 0;
    }
}

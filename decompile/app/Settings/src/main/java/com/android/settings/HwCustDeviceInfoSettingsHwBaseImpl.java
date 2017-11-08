package com.android.settings;

import android.content.Context;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.HwTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.deviceinfo.HwCustStatusImpl;
import java.text.NumberFormat;

public class HwCustDeviceInfoSettingsHwBaseImpl extends HwCustDeviceInfoSettingsHwBase {
    private static final String CPU_INFO = SystemProperties.get("ro.config.cpu_info_display", "");
    private static final String CPU_LEVEL_INFO = SystemProperties.get("ro.hardware.alter", "");
    private static final String CPU_VERSION_INFO = SystemProperties.get("ro.config.cpu_display_product", "");
    private static final int CUSTOM_MEID_LENGTH = 14;
    private static final boolean FACK_INFO_FOR_CMCCIOT = SystemProperties.getBoolean("ro.fackInfoForCmccIot", false);
    private static final boolean HIDE_EMUI_INFO = SystemProperties.getBoolean("ro.config.hw_hideEmuiInfo", false);
    private static final boolean HIDE_PRODUCT_INFO = SystemProperties.getBoolean("ro.build.hide", false);
    private static final boolean IS_CUSTOM_MEID = SystemProperties.getBoolean("ro.config.hide_meid", false);
    private static final String KEY_DEC_MEID_NUMBER = "meid_dec";
    private static final String KEY_HEX_MEID_NUMBER = "meid";
    private static final String LOG_TAG = "HwCustDeviceInfoSettingsHwBaseImpl";
    private static final String defaultSettings = "4;1.2 GHz;1.0GB;405 MB;4.00 GB;540 x 960;4.3;3.10.30;2.0";
    private static final boolean isTracfone = ("378".equals(SystemProperties.get("ro.config.hw_opta", "0")) ? "840".equals(SystemProperties.get("ro.config.hw_optb", "0")) : false);
    private static String[] mHideSettings = null;
    private static final int numSettings = 9;
    private HwTelephonyManager hwTelephonyManager = HwTelephonyManager.getDefault();
    private Preference meidDecPreference;
    private Preference meidHexPreference;
    private PreferenceScreen root;

    public HwCustDeviceInfoSettingsHwBaseImpl(DeviceInfoSettings deviceInfoSettings) {
        super(deviceInfoSettings);
        mHideSettings = SystemProperties.get("ro.build.hide.settings", defaultSettings).split(";");
        if (mHideSettings.length != 9) {
            mHideSettings = defaultSettings.split(";");
        }
    }

    public String updateCustCupInfo(Resources res, String cpuInfo, String maxCpuFreq) {
        if (res == null) {
            return "";
        }
        if (HIDE_PRODUCT_INFO || FACK_INFO_FOR_CMCCIOT) {
            int custCpuCount;
            String custMaxCpuFreq = gecustMaxCpuFreqString(mHideSettings[1], res);
            String custCpuInfo = "";
            try {
                custCpuCount = Integer.parseInt(mHideSettings[0]);
            } catch (NumberFormatException e) {
                custCpuCount = 4;
            }
            if (custCpuCount == 2) {
                custCpuInfo = res.getString(2131627404);
            } else if (custCpuCount == 4) {
                custCpuInfo = res.getString(2131627405);
            } else if (custCpuCount == 8) {
                custCpuInfo = res.getString(2131627406);
            }
            return res.getString(2131627757, new Object[]{custCpuInfo, custMaxCpuFreq});
        } else if (!CPU_INFO.equals("")) {
            return CPU_INFO;
        } else {
            if (CPU_VERSION_INFO.equals("cpu_info_qcom8952_vns")) {
                return res.getString(2131629306);
            }
            if (!CPU_LEVEL_INFO.equals("") && !CPU_LEVEL_INFO.equals(HwCustStatusImpl.SUMMARY_UNKNOWN)) {
                return CPU_LEVEL_INFO;
            }
            return res.getString(2131627757, new Object[]{cpuInfo, maxCpuFreq});
        }
    }

    private String gecustMaxCpuFreqString(String str, Resources res) {
        try {
            float numFloat = Float.parseFloat(str.substring(0, str.length() - 3));
            return res.getString(2131627756, new Object[]{NumberFormat.getInstance().format((double) numFloat)});
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return str;
        }
    }

    public void removeEmuiLogo(DeviceInfoSettingsHwBase context, PreferenceGroup preferenceGroup, String preference) {
        if (HIDE_EMUI_INFO) {
            try {
                preferenceGroup.removePreference(context.findPreference(preference));
            } catch (RuntimeException e) {
                Log.d(LOG_TAG, "Missing " + preference + " preference");
            }
        }
    }

    public boolean isHideEmuiInfo() {
        return HIDE_EMUI_INFO;
    }

    public void updateCustResource(Context context) {
        this.root = this.mDeviceInfoSettingsHwBase.getPreferenceScreen();
        this.mDeviceInfoSettingsHwBase.getPreferenceManager().inflateFromResource(context, 2131230772, this.root);
        this.root = this.mDeviceInfoSettingsHwBase.getPreferenceScreen();
        this.meidDecPreference = this.root.findPreference(KEY_DEC_MEID_NUMBER);
        TelephonyManager telephony = (TelephonyManager) context.getSystemService("phone");
        if (isTracfone && telephony.getCurrentPhoneType() == 2) {
            this.meidHexPreference = this.root.findPreference(KEY_HEX_MEID_NUMBER);
            if (this.meidHexPreference != null && this.meidDecPreference != null) {
                this.meidHexPreference.setTitle(2131629312);
                int preferenceCount = this.root.getPreferenceCount();
                int insertPosition = this.meidHexPreference.getOrder();
                for (int i = 0; i < preferenceCount; i++) {
                    Preference tempPreference = this.root.getPreference(i);
                    if (tempPreference != null && tempPreference.getOrder() > insertPosition) {
                        tempPreference.setOrder(tempPreference.getOrder() + 1);
                    }
                }
                this.meidDecPreference.setOrder(insertPosition + 1);
                setMeidSummaryText();
            }
        } else if (this.meidDecPreference != null) {
            this.root.removePreference(this.meidDecPreference);
        }
    }

    private void setMeidSummaryText() {
        String meid = this.hwTelephonyManager.getMeid();
        if (!TextUtils.isEmpty(meid)) {
            this.meidDecPreference.setSummary(convertMEIDHexToSpecialDec(meid));
        }
    }

    private static String convertMEIDHexToSpecialDec(String meidHex) {
        String meidHex1 = meidHex.substring(0, 8);
        String meidHex2 = meidHex.substring(8, 14);
        long meidDec1 = 0;
        long meidDec2 = 0;
        try {
            meidDec1 = Long.parseLong(meidHex1, 16);
            meidDec2 = Long.parseLong(meidHex2, 16);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return String.format("%010d%08d", new Object[]{Long.valueOf(meidDec1), Long.valueOf(meidDec2)});
    }

    public String checkSpareDigit(String meidStr) {
        if (IS_CUSTOM_MEID && !TextUtils.isEmpty(meidStr) && meidStr.length() > 14) {
            return meidStr.substring(0, 14);
        }
        return meidStr;
    }
}

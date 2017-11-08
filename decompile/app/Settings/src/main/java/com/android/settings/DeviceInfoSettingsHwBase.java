package com.android.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.HwTelephonyManager;
import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.HwSecureWaterMark;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.deviceinfo.AuthenticationInformationActivity;
import com.android.settings.deviceinfo.CertificationListSettings;
import com.android.settings.deviceinfo.CpuManager;
import com.android.settings.deviceinfo.DeviceNameSettings;
import com.android.settings.deviceinfo.TelecInfo;
import com.huawei.android.util.SystemInfo;
import com.huawei.cust.HwCustUtils;
import java.text.NumberFormat;
import java.util.Locale;

public class DeviceInfoSettingsHwBase extends SettingsPreferenceFragment {
    private static final boolean IS_ALL_NETWORK_SUPPORT = SystemProperties.getBoolean("ro.config.full_network_support", false);
    private static final boolean IS_OVERSEA_SUPPORT_CDMA = SystemProperties.getBoolean("ro.config.cdma_quiet", false);
    private HwCustDeviceInfoSettingsHwBase mCustDeviceInfoSettingsHwBase;
    protected Reflect mReflect = null;
    private Bitmap mWaterMarkBitmap;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mCustDeviceInfoSettingsHwBase = (HwCustDeviceInfoSettingsHwBase) HwCustUtils.createObj(HwCustDeviceInfoSettingsHwBase.class, new Object[]{this});
        addPreferencesFromResource(2131230770);
        evaluateTelecomEpush();
        evaluateMtkVersion();
        asyncUpdateCpuInfo();
        updateStorageUsage();
        setStringSummary("resolution", getResolution());
        removePreferenceIfEmuiMissing(getPreferenceScreen(), "emui_version", "ro.build.version.emui");
        redirectStatus();
        evaluateCertification();
        PreferenceGroup parentPreference = (PreferenceGroup) findPreference("container");
        PreferenceScreen huawei_copyright = (PreferenceScreen) findPreference("huawei_copyright");
        if (System.getInt(getContentResolver(), "is_show_huawei_copyright", 1) == 0 && huawei_copyright != null) {
            parentPreference.removePreference(huawei_copyright);
        }
        removePreference("system_update_settings");
        removePreference("apps_update_settings");
        updateAuthInfoPrefs();
        initEmuiBuildNumber();
        if (Utils.isWifiOnly(getActivity())) {
            Log.d("DeviceInfoSettingsHwBase", "onCreate->wifi only, remove imei and meid");
            removePreference("meid");
            removePreference("imei");
        } else {
            TelephonyManager telephony = (TelephonyManager) getSystemService("phone");
            if (telephony == null) {
                Log.e("DeviceInfoSettingsHwBase", "onCreate-> telephony is null, can not init imei or meid");
            } else {
                initIMEI(telephony);
                initMEID(telephony);
            }
        }
        if (this.mCustDeviceInfoSettingsHwBase != null) {
            this.mCustDeviceInfoSettingsHwBase.removeEmuiLogo(this, getPreferenceScreen(), "emui_logo");
        }
    }

    public void onResume() {
        super.onResume();
        updateDeviceName();
    }

    protected void updateAuthInfoPrefs() {
        if (SettingsExtUtils.isGlobalVersion()) {
            if (!AuthenticationInformationActivity.shouldDisplay(getActivity())) {
                removePreference("authentication_info");
            }
            if (!CertificationListSettings.shouldDisplay(getActivity())) {
                removePreference("certification_list");
                return;
            }
            return;
        }
        removePreference("certification_list");
        if (!AuthenticationInformationActivity.shouldDisplay(getActivity())) {
            removePreference("authentication_info");
        }
    }

    protected void updateDeviceName() {
        Preference deviceNamePreference = findPreference("device_name_settings");
        if (deviceNamePreference != null) {
            deviceNamePreference.setSummary(DeviceNameSettings.getDeviceName(getPrefContext()));
        }
    }

    protected void setStringSummary(String preference, String value) {
        try {
            findPreference(preference).setSummary((CharSequence) value);
        } catch (RuntimeException e) {
            MLog.e("DeviceInfoSettingsHwBase", "RuntimeException e: " + e);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        if (HwSecureWaterMark.isWatermarkEnable()) {
            Bitmap bitmap = createBackgroundBitmap();
            this.mWaterMarkBitmap = HwSecureWaterMark.addWatermark(bitmap);
            if (!(bitmap == null || bitmap.isRecycled())) {
                bitmap.recycle();
            }
            root.setBackground(new BitmapDrawable(this.mWaterMarkBitmap));
        }
        return root;
    }

    private Bitmap createBackgroundBitmap() {
        Activity context = getActivity();
        if (context == null) {
            return null;
        }
        DisplayMetrics displaymetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return Bitmap.createBitmap(displaymetrics.widthPixels, displaymetrics.heightPixels, Config.ARGB_8888);
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mWaterMarkBitmap != null && !this.mWaterMarkBitmap.isRecycled()) {
            this.mWaterMarkBitmap.recycle();
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        handlePreferenceClick(preference);
        if (preference.getKey().equals("china_telecom_epush")) {
            try {
                Intent intent = new Intent();
                intent.setClassName("com.ctc.epush", "com.ctc.epush.IndexActivity");
                startActivity(intent);
            } catch (Exception e) {
                MLog.e("DeviceInfoSettingsHwBase", "Exception e: " + e);
                e.printStackTrace();
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    protected void redirectStatus() {
        if (Utils.isMultiSimEnabled()) {
            findPreference("status_info").getIntent().setClassName("com.android.settings", "com.android.settings.Settings$MSimStatusActivity");
        }
    }

    protected String getResolution() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getRealSize(point);
        return point.x + " x " + point.y;
    }

    protected void updateStorageUsage() {
        long totalStorage = 0;
        long freeStorage = Utils.getStorageAvailableSize((StorageManager) getSystemService("storage"));
        String totalStorageString = SystemInfo.getDeviceEmmc();
        if (!(totalStorageString == null || totalStorageString.isEmpty())) {
            totalStorage = Utils.ceilToSdcardSize(Long.parseLong(totalStorageString) * 1024);
        }
        String sizeStr = Formatter.formatFileSize(getActivity(), freeStorage);
        String sizeStr2 = Formatter.formatFileSize(getActivity(), totalStorage);
        sizeStr = Utils.addBlankIntoText(sizeStr);
        sizeStr2 = Utils.addBlankIntoText(sizeStr2);
        setStringSummary("internal_storage", getResources().getString(2131625714, new Object[]{sizeStr}) + "\n" + getResources().getString(2131627408, new Object[]{sizeStr2}));
        long ramLong = Long.parseLong(SystemInfo.getDeviceRam());
        ramLong = ((((ramLong / 262144) + (((double) (ramLong % 262144)) / 262144.0d < 0.01d ? 0 : 1)) * 256) * 1024) * 1024;
        String sizeStr3 = "";
        if (ramLong != 0) {
            setStringSummary("ram", Formatter.formatShortFileSize(getActivity(), ramLong));
        }
    }

    protected String getCpuInfo() {
        Context context = getActivity();
        if (context == null) {
            return "";
        }
        Resources res = context.getResources();
        String cpuInfo = "";
        int cpuCount = CpuManager.getCpuCount();
        if (cpuCount == 2) {
            cpuInfo = res.getString(2131627404);
        } else if (cpuCount == 4) {
            cpuInfo = res.getString(2131627405);
        } else if (cpuCount == 8) {
            cpuInfo = res.getString(2131627406);
        }
        String maxCpuFreq = CpuManager.getMaxCpuFreq();
        if (!"N/A".equals(maxCpuFreq)) {
            try {
                float temp = ((float) Math.round((Float.parseFloat(maxCpuFreq) / 1000000.0f) * 10.0f)) / 10.0f;
                maxCpuFreq = res.getString(2131627756, new Object[]{NumberFormat.getInstance().format((double) temp)});
            } catch (NumberFormatException e) {
                MLog.e("DeviceInfoSettingsHwBase", "NumberFormatException e: " + e);
                e.printStackTrace();
            }
        }
        if (this.mCustDeviceInfoSettingsHwBase != null) {
            cpuInfo = this.mCustDeviceInfoSettingsHwBase.updateCustCupInfo(res, cpuInfo, maxCpuFreq);
        } else {
            cpuInfo = res.getString(2131627757, new Object[]{cpuInfo, maxCpuFreq});
        }
        return cpuInfo;
    }

    protected String getMtkVersion() {
        String mtkVersion = "";
        try {
            this.mReflect = Reflect.getDefaultInstance();
            this.mReflect.initClass();
            String[] response = this.mReflect.callATCommand("AT+CGMR");
            if (response == null || response.length < 3) {
                return mtkVersion;
            }
            Log.d("DeviceInfoSettingsHwBase", "response.length == " + response.length);
            if (response[0] == null || response[1] == null || response[2] == null || !response[0].equals("1") || !response[1].equalsIgnoreCase("OK")) {
                return mtkVersion;
            }
            int index = response[2].indexOf(":");
            return (index <= -1 || index >= response[2].length()) ? mtkVersion : response[2].substring(index + 1);
        } catch (Exception e) {
            Log.d("DeviceInfoSettingsHwBase", e.toString());
            return " ";
        }
    }

    protected void removePreferenceIfEmuiMissing(PreferenceGroup preferenceGroup, String preference, String property) {
        if (SystemProperties.get(property).equals("") || (this.mCustDeviceInfoSettingsHwBase != null && this.mCustDeviceInfoSettingsHwBase.isHideEmuiInfo())) {
            try {
                preferenceGroup.removePreference(findPreference(preference));
                return;
            } catch (RuntimeException e) {
                Log.d("DeviceInfoSettingsHwBase", "Property " + property + " missing and no " + preference + " preference");
                return;
            }
        }
        try {
            CharSequence emuiSummary = getEmuiVersion(SystemProperties.get(property));
            if (emuiSummary.equals("")) {
                preferenceGroup.removePreference(findPreference(preference));
            } else {
                findPreference("emui_version").setSummary(emuiSummary);
            }
        } catch (RuntimeException e2) {
            Log.d("DeviceInfoSettingsHwBase", "EmuiVersion missing");
        }
    }

    protected String getEmuiVersion(String fullVersionInfo) {
        if (fullVersionInfo == null) {
            return "";
        }
        String[] str = fullVersionInfo.split("_");
        if (str == null || str.length < 2) {
            return "";
        }
        return str[1];
    }

    protected void evaluateTelecomEpush() {
        if (!SystemProperties.getBoolean("ro.config.enable.telecom_epush", false)) {
            try {
                getPreferenceScreen().removePreference(findPreference("china_telecom_epush"));
            } catch (RuntimeException e) {
                Log.e("DeviceInfoSettingsHwBase", e.toString());
            }
        } else if (!Utils.isCheckAppExist(getActivity(), "com.ctc.epush")) {
            try {
                getPreferenceScreen().removePreference(findPreference("china_telecom_epush"));
            } catch (RuntimeException e2) {
                Log.e("DeviceInfoSettingsHwBase", e2.toString());
            }
        }
    }

    protected void evaluateMtkVersion() {
        try {
            if (MSimTelephonyManager.getDefault().isMultiSimEnabled() && SystemProperties.get("ro.config.xgold_modem", "").equals("mtk")) {
                CharSequence mtkVersion = getMtkVersion();
                if (mtkVersion.length() > 0) {
                    findPreference("mtk_version").setSummary(mtkVersion);
                    return;
                }
                return;
            }
            try {
                getPreferenceScreen().removePreference(findPreference("mtk_version"));
            } catch (RuntimeException e) {
                Log.e("DeviceInfoSettingsHwBase", e.toString());
            }
        } catch (Exception e2) {
            MLog.e("DeviceInfoSettingsHwBase", e2.toString());
            e2.printStackTrace();
        }
    }

    protected void evaluateCertification() {
        if (!TelecInfo.hasCertification(getActivity())) {
            getPreferenceScreen().removePreference(findPreference("telec_info"));
        }
    }

    private void handlePreferenceClick(Preference preference) {
        if (preference != null) {
            String keyStr = preference.getKey();
            if (keyStr.equals("status_info") || keyStr.equals("container") || keyStr.equals("authentication_info") || keyStr.equals("telec_info") || keyStr.equals("safetylegal") || keyStr.equals("regulatory_info") || keyStr.equals("apps_update_settings") || keyStr.equals("system_update_settings") || keyStr.equals("device_name_settings") || keyStr.equals("additional_system_update_settings")) {
                ItemUseStat.getInstance().handleClick(getActivity(), 2, keyStr);
            }
        }
    }

    public void onPause() {
        super.onPause();
        ItemUseStat.getInstance().cacheData(getActivity());
    }

    private void initEmuiBuildNumber() {
        CharSequence emuiVersion = SystemProperties.get("ro.build.version.emui");
        if (!emuiVersion.equals("")) {
            emuiVersion = emuiVersion.substring(emuiVersion.lastIndexOf("_") + 1);
        }
        if (this.mCustDeviceInfoSettingsHwBase == null || !this.mCustDeviceInfoSettingsHwBase.isHideEmuiInfo()) {
            findPreference("emui_logo").setTitle(emuiVersion);
        }
    }

    private void asyncUpdateCpuInfo() {
        new AsyncTask<String, Void, String>() {
            protected String doInBackground(String... params) {
                return DeviceInfoSettingsHwBase.this.getCpuInfo();
            }

            protected void onPostExecute(String result) {
                if (result != null) {
                    DeviceInfoSettingsHwBase.this.setStringSummary("cpu", result);
                }
            }
        }.execute(new String[0]);
    }

    private void initIMEI(TelephonyManager telephony) {
        String imei = "";
        boolean removeImei = false;
        Log.d("DeviceInfoSettingsHwBase", "initIMEI->is multi sim:" + Utils.isMultiSimEnabled());
        if (Utils.isMultiSimEnabled()) {
            int mainCardSlotId = Utils.getMainCardSlotId();
            int asistantSlotId = getAsistantSlotId(mainCardSlotId);
            imei = telephony.getImei(mainCardSlotId);
            String secondImei = telephony.getImei(asistantSlotId);
            if (TextUtils.isEmpty(imei) || TextUtils.isEmpty(secondImei) || imei.equals(secondImei)) {
                Log.d("DeviceInfoSettingsHwBase", "initIMEI->some imei is empty or two imeis are the same");
            } else {
                imei = imei + "\n" + secondImei;
            }
        } else if (!isCDMAPhone(telephony.getCurrentPhoneType()) || telephony.getLteOnCdmaMode() == 1 || IS_OVERSEA_SUPPORT_CDMA) {
            imei = telephony.getImei();
        } else {
            removeImei = true;
            Log.d("DeviceInfoSettingsHwBase", "initIMEI->current is cdma phone and not on lte mode, should remove imei");
        }
        if (removeImei) {
            removePreference("imei");
        } else {
            setStringSummary("imei", formatImeiOrMeid(imei));
        }
    }

    private void initMEID(TelephonyManager telephony) {
        HwTelephonyManager hwTelephonyManager = HwTelephonyManager.getDefault();
        String meid = "";
        boolean showMeid = false;
        Log.d("DeviceInfoSettingsHwBase", "initMEID->");
        if (Utils.isMultiSimEnabled()) {
            if (Utils.isChinaTelecomArea() || IS_ALL_NETWORK_SUPPORT) {
                showMeid = true;
                int mainCardSlotId = Utils.getMainCardSlotId();
                int asistantSlotId = getAsistantSlotId(mainCardSlotId);
                int choosedSlot = mainCardSlotId;
                if (isCDMAPhone(telephony.getCurrentPhoneType(mainCardSlotId))) {
                    choosedSlot = mainCardSlotId;
                } else if (isCDMAPhone(telephony.getCurrentPhoneType(asistantSlotId))) {
                    choosedSlot = asistantSlotId;
                } else {
                    choosedSlot = mainCardSlotId;
                }
                meid = hwTelephonyManager.getMeid(choosedSlot);
            } else {
                Log.d("DeviceInfoSettingsHwBase", "initMEID->not AL , not CL and then remove meid preference");
            }
        } else if (isCDMAPhone(telephony.getCurrentPhoneType())) {
            if (!IS_OVERSEA_SUPPORT_CDMA) {
                showMeid = true;
            }
            meid = hwTelephonyManager.getMeid();
            if (this.mCustDeviceInfoSettingsHwBase != null) {
                meid = this.mCustDeviceInfoSettingsHwBase.checkSpareDigit(meid);
            }
        } else {
            Log.d("DeviceInfoSettingsHwBase", "initMEID->phone type is not cdma");
        }
        if (showMeid) {
            setStringSummary("meid", formatImeiOrMeid(meid));
        } else {
            removePreference("meid");
        }
    }

    private int getAsistantSlotId(int mainCardSlotId) {
        return mainCardSlotId == 0 ? 1 : 0;
    }

    private boolean isCDMAPhone(int phoneType) {
        return 2 == phoneType;
    }

    private String formatImeiOrMeid(String srcStr) {
        if (TextUtils.isEmpty(srcStr)) {
            return getResources().getString(2131624355);
        }
        return srcStr.toUpperCase(Locale.US);
    }

    protected int getMetricsCategory() {
        return 100000;
    }
}

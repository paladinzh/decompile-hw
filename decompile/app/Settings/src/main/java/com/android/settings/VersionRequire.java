package com.android.settings;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.CellLocation;
import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.android.util.NoExtAPIException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

public class VersionRequire extends SettingsPreferenceFragment {
    private static final boolean IS_ALL_NETWORK_SUPPORT = SystemProperties.getBoolean("ro.config.full_network_support", false);
    private String hw_version = null;
    private int telecom_hardware_version = 0;

    private static class GsmCdmaPhoneVerInfo {
        private Context mContext;
        String mIccidNumber;
        String mMlplVersion;
        String mMsplVersion;
        String mNid;
        String mPRLVersion;
        private GsmCdmaPhone mPhone = getGsmCdmaPhone();
        String mSid;
        String mUIMid;

        public GsmCdmaPhoneVerInfo(Context context) {
            this.mContext = context;
            init();
        }

        private GsmCdmaPhone getGsmCdmaPhone() {
            if (VersionRequire.IS_ALL_NETWORK_SUPPORT) {
                if (isMSimStateOk(0)) {
                    return (GsmCdmaPhone) PhoneFactory.getPhone(0);
                }
                if (isMSimStateOk(1)) {
                    return (GsmCdmaPhone) PhoneFactory.getPhone(1);
                }
                return null;
            } else if (isValidSimState(getSimState())) {
                return (GsmCdmaPhone) PhoneFactory.getPhone(Utils.getMainCardSlotId());
            } else {
                return null;
            }
        }

        private void init() {
            initPRLVersion();
            initUIMid();
            initIccidNumber();
            initMlplMsplVersion();
            initSidNid();
        }

        private boolean isMSimStateOk(int subid) {
            return isValidSimState(MSimTelephonyManager.getDefault().getSimState(subid));
        }

        private int getSimState() {
            if (Utils.isMultiSimEnabled()) {
                return MSimTelephonyManager.getDefault().getSimState(0);
            }
            return ((TelephonyManager) this.mContext.getSystemService("phone")).getSimState();
        }

        private boolean isValidSimState(int state) {
            return (state == 1 || state == 0) ? false : true;
        }

        private void initPRLVersion() {
            this.mPRLVersion = "0";
            if (this.mPhone != null) {
                String prlVer = this.mPhone.getCdmaPrlVersion();
                if (prlVer == null) {
                    prlVer = "0";
                }
                this.mPRLVersion = prlVer;
            }
        }

        private void initUIMid() {
            this.mUIMid = "0";
            if (this.mPhone != null) {
                this.mUIMid = "" + this.mPhone.getEsn();
            }
        }

        private void initIccidNumber() {
            this.mIccidNumber = "0";
            if (this.mPhone != null) {
                String iccid = this.mPhone.getIccSerialNumber();
                if (iccid == null) {
                    iccid = "0";
                }
                this.mIccidNumber = iccid;
            }
        }

        private void initMlplMsplVersion() {
            this.mMlplVersion = "0";
            this.mMsplVersion = "0";
            try {
                this.mMlplVersion = TelephonyManagerEx.getCdmaMlplVersion();
                this.mMsplVersion = TelephonyManagerEx.getCdmaMsplVersion();
            } catch (NoExtAPIException e) {
                this.mMlplVersion = null;
                this.mMsplVersion = null;
            }
        }

        private void initSidNid() {
            this.mSid = "0";
            this.mNid = "0";
            if (this.mPhone != null) {
                CellLocation location = this.mPhone.getCellLocation();
                if (location instanceof CdmaCellLocation) {
                    CdmaCellLocation loc = (CdmaCellLocation) location;
                    if (loc.getSystemId() != -1) {
                        this.mSid = "" + loc.getSystemId();
                    }
                    if (loc.getNetworkId() != -1) {
                        this.mNid = "" + loc.getNetworkId();
                    }
                }
            }
        }
    }

    public String readHWAppinfo(String filepath, String target) {
        IOException e;
        Throwable th;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        FileInputStream fileInputStream = null;
        String str = null;
        try {
            FileInputStream fis = new FileInputStream(filepath);
            try {
                InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                try {
                    BufferedReader reader = new BufferedReader(isr);
                    do {
                        try {
                            str = reader.readLine();
                            if (str == null) {
                                break;
                            }
                            str = str.trim();
                        } catch (IOException e2) {
                            e = e2;
                            fileInputStream = fis;
                            bufferedReader = reader;
                            inputStreamReader = isr;
                        } catch (Throwable th2) {
                            th = th2;
                            fileInputStream = fis;
                            bufferedReader = reader;
                            inputStreamReader = isr;
                        }
                    } while (!target.equals(str));
                    str = reader.readLine();
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e3) {
                            Log.e("VersionRequire", "close the BufferedReader error" + e3.toString());
                        }
                    }
                    if (isr != null) {
                        try {
                            isr.close();
                        } catch (IOException e32) {
                            Log.e("VersionRequire", "close the InputStreamReader error" + e32.toString());
                        }
                    }
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e322) {
                            Log.e("VersionRequire", "close the InputStreamReader error" + e322.toString());
                            e322.printStackTrace();
                        }
                    }
                    inputStreamReader = isr;
                } catch (IOException e4) {
                    e322 = e4;
                    fileInputStream = fis;
                    inputStreamReader = isr;
                    try {
                        Log.e("VersionRequire", "read the file error" + e322.toString());
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e3222) {
                                Log.e("VersionRequire", "close the BufferedReader error" + e3222.toString());
                            }
                        }
                        if (inputStreamReader != null) {
                            try {
                                inputStreamReader.close();
                            } catch (IOException e32222) {
                                Log.e("VersionRequire", "close the InputStreamReader error" + e32222.toString());
                            }
                        }
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e322222) {
                                Log.e("VersionRequire", "close the InputStreamReader error" + e322222.toString());
                                e322222.printStackTrace();
                            }
                        }
                        return str;
                    } catch (Throwable th3) {
                        th = th3;
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e3222222) {
                                Log.e("VersionRequire", "close the BufferedReader error" + e3222222.toString());
                            }
                        }
                        if (inputStreamReader != null) {
                            try {
                                inputStreamReader.close();
                            } catch (IOException e32222222) {
                                Log.e("VersionRequire", "close the InputStreamReader error" + e32222222.toString());
                            }
                        }
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e322222222) {
                                Log.e("VersionRequire", "close the InputStreamReader error" + e322222222.toString());
                                e322222222.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    fileInputStream = fis;
                    inputStreamReader = isr;
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw th;
                }
            } catch (IOException e5) {
                e322222222 = e5;
                fileInputStream = fis;
                Log.e("VersionRequire", "read the file error" + e322222222.toString());
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return str;
            } catch (Throwable th5) {
                th = th5;
                fileInputStream = fis;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
        } catch (IOException e6) {
            e322222222 = e6;
            Log.e("VersionRequire", "read the file error" + e322222222.toString());
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return str;
        }
        return str;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230923);
        setStringSummary("device_model", Build.MODEL);
        setStringSummary("build_number", Build.DISPLAY);
        this.telecom_hardware_version = System.getInt(getContentResolver(), "telecom_hardware_version", 0);
        if (1 == this.telecom_hardware_version) {
            this.hw_version = getActivity().getBaseContext().getString(2131627345);
        } else {
            this.hw_version = readHWAppinfo("/proc/app_info", "hw_version:");
        }
        String custHardVersion = SystemProperties.get("ro.product.hardwareversion", "");
        if (!custHardVersion.equals("")) {
            this.hw_version = custHardVersion;
        }
        if (this.hw_version != null) {
            setStringSummary("hardware_version", this.hw_version);
        }
        updatePhoneVerInfoPreference();
        updateImeiMeidPreference();
    }

    private void updatePhoneVerInfoPreference() {
        PreferenceScreen parentPreference = (PreferenceScreen) findPreference("info_screen");
        if (Utils.isChinaTelecomArea() || SystemProperties.get("ro.config.dsds_mode", "").equals("cdma_gsm")) {
            Preference prefPRL = parentPreference.findPreference("PRL_version");
            GsmCdmaPhoneVerInfo verInfos = new GsmCdmaPhoneVerInfo(getActivity());
            findAndSetPreferenceSummary(parentPreference, "PRL_version", verInfos.mPRLVersion);
            findAndSetPreferenceSummary(parentPreference, "uim_id", verInfos.mUIMid);
            if (!"-1".equals(verInfos.mSid)) {
                findAndSetPreferenceSummary(parentPreference, "sid", verInfos.mSid);
            }
            if (!"-1".equals(verInfos.mNid)) {
                findAndSetPreferenceSummary(parentPreference, "nid", verInfos.mNid);
            }
            findAndSetPreferenceSummary(parentPreference, "ICCID_number", verInfos.mIccidNumber);
            setPreferenceOrder(parentPreference, "ICCID_number", prefPRL.getOrder() + 1);
            if (verInfos.mMlplVersion == null) {
                removePreferenceFromScreen("MLPL_version");
            } else {
                findAndSetPreferenceSummary(parentPreference, "MLPL_version", verInfos.mMlplVersion);
                setPreferenceOrder(parentPreference, "MLPL_version", prefPRL.getOrder() + 1);
            }
            if (verInfos.mMsplVersion == null) {
                removePreferenceFromScreen("MSPL_version");
                return;
            }
            findAndSetPreferenceSummary(parentPreference, "MSPL_version", verInfos.mMsplVersion);
            setPreferenceOrder(parentPreference, "MSPL_version", prefPRL.getOrder() + 1);
            return;
        }
        removePreferenceFromScreen("PRL_version");
        removePreferenceFromScreen("uim_id");
        removePreferenceFromScreen("sid");
        removePreferenceFromScreen("nid");
        removePreferenceFromScreen("meid");
    }

    private void updateImeiMeidPreference() {
        if (Utils.isWifiOnly(getActivity())) {
            removePreferenceFromScreen("imei");
            removePreferenceFromScreen("meid");
            removePreferenceFromScreen("imei_1");
        } else if (Utils.isMultiSimEnabled()) {
            setImeiAndMeid();
        } else {
            String imeiInfo = PhoneFactory.getDefaultPhone().getImei();
            if (TextUtils.isEmpty(imeiInfo)) {
                imeiInfo = getResources().getString(2131624355);
            } else {
                imeiInfo = imeiInfo.toUpperCase();
            }
            setStringSummary("imei", imeiInfo);
            removePreferenceFromScreen("imei_1");
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (16908332 == item.getItemId()) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void finish() {
        Activity activity = getActivity();
        if (activity != null) {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
            } else {
                activity.finish();
            }
        }
    }

    private void setStringSummary(String preference, String value) {
        try {
            findPreference(preference).setSummary((CharSequence) value);
        } catch (RuntimeException e) {
        }
    }

    protected int getMetricsCategory() {
        return 100000;
    }

    private void removePreferenceFromScreen(String key) {
        Preference pref = findPreference(key);
        if (pref != null) {
            getPreferenceScreen().removePreference(pref);
        }
    }

    private String formatImeiOrMeid(String srcStr) {
        if (TextUtils.isEmpty(srcStr)) {
            return getResources().getString(2131624355);
        }
        return srcStr.toUpperCase(Locale.US);
    }

    private void setSummaryText(String preference, String text) {
        if (TextUtils.isEmpty(text)) {
            CharSequence text2 = getString(2131624355);
        }
        if (findPreference(preference) != null) {
            findPreference(preference).setSummary(text2);
        }
    }

    private void setImeiAndMeid() {
        boolean z = true;
        int i = 0;
        if (Utils.isMultiSimEnabled()) {
            int mainCardSlotId = Utils.getMainCardSlotId();
            Phone phone1 = PhoneFactory.getPhone(mainCardSlotId);
            if (mainCardSlotId == 0) {
                i = 1;
            }
            Phone phone2 = PhoneFactory.getPhone(i);
            Object obj = null;
            Object obj2 = null;
            if (phone1 != null) {
                obj = phone1.getImei();
                setSummaryText("imei", formatImeiOrMeid(obj));
            }
            if (phone2 != null) {
                obj2 = phone2.getImei();
                setSummaryText("imei_1", formatImeiOrMeid(obj2));
            }
            if (TextUtils.isEmpty(obj)) {
                removePreferenceFromScreen("imei");
            }
            if (obj != null && obj.equals(r1)) {
                removePreferenceFromScreen("imei_1");
            }
            Preference imeiPref = findPreference("imei");
            Preference imeiPref2 = findPreference("imei_1");
            if (!(imeiPref == null || imeiPref2 == null)) {
                imeiPref.setTitle(2131627848);
            }
            if (!Utils.isChinaTelecomArea()) {
                z = IS_ALL_NETWORK_SUPPORT;
            }
            if (!z) {
                removePreferenceFromScreen("meid");
            } else if (phone1 != null && 2 == phone1.getPhoneType()) {
                setSummaryText("meid", formatImeiOrMeid(phone1.getMeid()));
            } else if (phone2 != null && 2 == phone2.getPhoneType()) {
                setSummaryText("meid", formatImeiOrMeid(phone2.getMeid()));
            } else if (phone1 != null) {
                setSummaryText("meid", formatImeiOrMeid(phone1.getMeid()));
            }
        }
    }

    private void findAndSetPreferenceSummary(PreferenceScreen root, String key, String summary) {
        if (root != null && key != null && summary != null) {
            Preference pref = root.findPreference(key);
            if (pref != null) {
                pref.setSummary((CharSequence) summary);
            }
        }
    }

    private void setPreferenceOrder(PreferenceScreen root, String key, int order) {
        if (root != null && key != null) {
            Preference pref = root.findPreference(key);
            if (pref != null) {
                pref.setOrder(order);
            }
        }
    }
}

package com.android.settings;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.provider.SettingsEx.Systemex;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import com.android.settings.deviceinfo.HwCustStatusImpl;
import com.huawei.android.telephony.SmsManagerEx;
import com.huawei.android.util.SystemInfo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HwCustDeviceInfoSettingsImpl extends HwCustDeviceInfoSettings {
    private static final String CPU_INFO = SystemProperties.get("ro.config.cpu_info_display", "");
    private static final double DEVIATION = 0.01d;
    private static final boolean DISPLAY_SMSC_NUMBER = SystemProperties.getBoolean("ro.config.hw_smsc", false);
    private static final boolean FACK_INFO_FOR_CMCCIOT = SystemProperties.getBoolean("ro.fackInfoForCmccIot", false);
    private static final String FILENAME_PROC_VERSION = "/proc/version";
    private static final long GIGABYTE = 1073741824;
    private static final boolean HIDE_EMUI_INFO = SystemProperties.getBoolean("ro.config.hw_hideEmuiInfo", false);
    private static final boolean HIDE_PRODUCT_INFO = SystemProperties.getBoolean("ro.build.hide", false);
    private static final String KEY_BASEBAND_VERSION = "baseband_version";
    private static final String KEY_BUILD_NUMBER = "build_number";
    private static final String KEY_COTA_VERSION = "cota_version";
    private static final String KEY_CPU = "cpu";
    private static final String KEY_DEVICE_MODEL = "device_model";
    private static final String KEY_EMUI_LOGO = "emui_logo";
    private static final String KEY_EMUI_VERSION = "emui_version";
    private static final String KEY_FIRMWARE_VERSION = "firmware_version";
    private static final String KEY_HARDWARE_VERSION = "hardware_version";
    private static final String KEY_INTERNAL_STORAGE = "internal_storage";
    private static final String KEY_KERNEL_VERSION = "kernel_version";
    private static final String KEY_RAM = "ram";
    private static final String KEY_RESOLUTION = "resolution";
    private static final String KEY_SMSC_NUMBER = "smsc_number";
    private static final String KEY_SUTEL_VERSION = "sutel_version";
    private static final String LOG_TAG = "HwCustDeviceInfoSettingsImpl";
    private static final long MEGABYTE = 1048576;
    private static final String MODIFY_RAM_SHOW = "modify_ram_show";
    public static final int UNIT_LENGTH = 2;
    private static String[] mHideSettings = null;
    private static String mReverseImei = "";
    private static final int numSettings = 9;
    private String defaultSettings = "4;1.2 GHz;1.0GB;405 MB;4.00 GB;540 x 960;4.3;3.10.30;2.0";

    public HwCustDeviceInfoSettingsImpl(DeviceInfoSettings deviceInfoSettings) {
        super(deviceInfoSettings);
    }

    public void updateCustPreference(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) this.mDeviceInfoSettings.getSystemService("phone");
        String deveceId = telephonyManager.getDeviceId();
        if ((HIDE_PRODUCT_INFO || FACK_INFO_FOR_CMCCIOT) && deveceId != null) {
            mReverseImei = reverseString(deveceId);
        }
        mHideSettings = SystemProperties.get("ro.build.hide.settings", this.defaultSettings).split(";");
        if (mHideSettings.length != 9) {
            mHideSettings = this.defaultSettings.split(";");
        }
        this.mDeviceInfoSettings.getPreferenceManager().inflateFromResource(context, 2131230771, this.mDeviceInfoSettings.getPreferenceScreen());
        PreferenceScreen root = this.mDeviceInfoSettings.getPreferenceScreen();
        updateCustVersion(context, root);
        updateCotaVersion(context, root);
        updateHardwareVersion(context, root);
        updateSutelVersion(context, root);
        Preference moperator_country = root.findPreference(HwCustDeviceInfoSettings.KEY_OPERATOR_COUNTRY_INFO);
        if (!SystemProperties.getBoolean("ro.config.hw_operator", false)) {
            root.removePreference(moperator_country);
        }
        updateBaseBandVersion();
        this.mDeviceInfoSettings.findPreference(KEY_KERNEL_VERSION).setSummary(getFormattedKernelVersion());
        changeDeviceModel();
        showFakeInfoIfNeeded(context);
        showRamInfo(context);
        Preference mSmscNumberPreference = root.findPreference(KEY_SMSC_NUMBER);
        if (mSmscNumberPreference != null) {
            displaySmscNumber(mSmscNumberPreference, telephonyManager, root);
        }
    }

    private void updateBaseBandVersion() {
        CharSequence basebandVersion = SystemProperties.get("gsm.version.baseband", "");
        if (mReverseImei.length() > 13) {
            if (basebandVersion.indexOf(",") > 0) {
                String[] temp = basebandVersion.split(",");
                String basebandVersion2 = "20.201." + mReverseImei.substring(0, 2) + "." + mReverseImei.substring(2, 4) + "." + mReverseImei.substring(4, 7);
                if (temp[0].equals(temp[1])) {
                    basebandVersion = basebandVersion2 + "," + basebandVersion2;
                } else {
                    basebandVersion = basebandVersion2 + "," + temp[1];
                }
            } else {
                basebandVersion = "20.201." + mReverseImei.substring(0, 2) + "." + mReverseImei.substring(2, 4) + "." + mReverseImei.substring(4, 7);
            }
        } else if (HIDE_PRODUCT_INFO || FACK_INFO_FOR_CMCCIOT) {
            basebandVersion = this.mDeviceInfoSettings.getResources().getString(2131624355);
        }
        if (this.mDeviceInfoSettings.findPreference(KEY_BASEBAND_VERSION) != null) {
            this.mDeviceInfoSettings.findPreference(KEY_BASEBAND_VERSION).setSummary(basebandVersion);
        }
    }

    private void updateSutelVersion(Context context, PreferenceScreen root) {
        String sutelVersion = getSutelVersion(context);
        String mcc_mnc = TelephonyManager.getDefault().getSimOperator();
        HashMap<String, String> mccmncSutelMap = getMccmncSutelMap();
        if (mccmncSutelMap.containsKey(mcc_mnc)) {
            Preference mBuildNumberPreference = root.findPreference(KEY_BUILD_NUMBER);
            Preference mCpuPreference = root.findPreference(KEY_CPU);
            Preference mSutelVersionPreference = root.findPreference(KEY_SUTEL_VERSION);
            if (mBuildNumberPreference != null && mSutelVersionPreference != null) {
                mSutelVersionPreference.setOrder(mBuildNumberPreference.getOrder() + 1);
                if (mCpuPreference != null && mBuildNumberPreference.getOrder() + 1 == mCpuPreference.getOrder()) {
                    mCpuPreference.setOrder(mCpuPreference.getOrder() + 1);
                }
                this.mDeviceInfoSettings.setStringSummary(KEY_SUTEL_VERSION, (String) mccmncSutelMap.get(mcc_mnc));
                return;
            }
            return;
        }
        boolean showSutelInLegalInfo = SystemProperties.getBoolean("ro.config.showSutelInLegalInfo", false);
        if ("".equals(sutelVersion) || showSutelInLegalInfo) {
            root.removePreference(root.findPreference(KEY_SUTEL_VERSION));
        } else {
            this.mDeviceInfoSettings.setStringSummary(KEY_SUTEL_VERSION, sutelVersion);
        }
    }

    private void updateHardwareVersion(Context context, PreferenceScreen root) {
        String hardWareVersion = SystemProperties.get("ro.product.hardwareversion", "");
        String custVersion = getHardWareVersion(context);
        if (!(custVersion == null || "".equals(custVersion))) {
            hardWareVersion = custVersion;
        }
        if (SystemProperties.getBoolean("ro.config.showHardwareVersion", false)) {
            hardWareVersion = readFileByChars("/proc/device-tree/hisi,boardname");
        }
        if (!SystemProperties.getBoolean("ro.product.show_hardwareversion", false) || "".equals(hardWareVersion)) {
            root.removePreference(root.findPreference(KEY_HARDWARE_VERSION));
        } else {
            this.mDeviceInfoSettings.setStringSummary(KEY_HARDWARE_VERSION, hardWareVersion);
        }
    }

    private void updateCustVersion(Context context, PreferenceScreen root) {
        Preference custVersionPre = root.findPreference("custom_version");
        if (custVersionPre != null) {
            String custC = SystemProperties.get("ro.product.CustCVersion", "");
            String custD = SystemProperties.get("ro.product.CustDVersion", "");
            CharSequence custVer = System.getString(context.getContentResolver(), "industry_version_name");
            if (!TextUtils.isEmpty(custVer)) {
                custVersionPre.setSummary(custVer);
            } else if (isNormalVersion(custC) || TextUtils.isEmpty(custD) || !SystemProperties.getBoolean("ro.config.hw_custverdisplay", false)) {
                root.removePreference(custVersionPre);
            } else {
                StringBuilder custVersion = new StringBuilder("CUST");
                custVersion.append(custC);
                custVersion.append(custD);
                custVersionPre.setSummary(custVersion.toString());
            }
        }
    }

    private void updateCotaVersion(Context context, PreferenceScreen root) {
        if (SystemProperties.getBoolean("ro.config.show_cotaversion", false)) {
            String atlVersion = Systemex.getString(context.getContentResolver(), "cota.upgrade.version.atl");
            String btlVersion = Systemex.getString(context.getContentResolver(), "cota.upgrade.version.btl");
            if (TextUtils.isEmpty(atlVersion) && TextUtils.isEmpty(btlVersion)) {
                root.removePreference(root.findPreference(KEY_COTA_VERSION));
            } else {
                StringBuilder cotaVersion = new StringBuilder();
                if (!TextUtils.isEmpty(btlVersion)) {
                    cotaVersion.append(btlVersion);
                }
                if (!TextUtils.isEmpty(atlVersion)) {
                    cotaVersion.append(atlVersion);
                }
                this.mDeviceInfoSettings.setStringSummary(KEY_COTA_VERSION, cotaVersion.toString());
            }
            Preference custVersionPre = root.findPreference("custom_version");
            if (!TextUtils.isEmpty(atlVersion) && custVersionPre != null) {
                root.removePreference(custVersionPre);
                return;
            }
            return;
        }
        root.removePreference(root.findPreference(KEY_COTA_VERSION));
    }

    private void showRamInfo(Context context) {
        long ramLong = Long.parseLong(SystemInfo.getDeviceRam());
        ramLong = ((((ramLong / 262144) + (((double) (ramLong % 262144)) / 262144.0d < DEVIATION ? 0 : 1)) * 256) * 1024) * 1024;
        String sizeStr3 = "";
        if (ramLong != 0) {
            if (SystemProperties.get(MODIFY_RAM_SHOW, "false").equals("true")) {
                if (ramLong <= GIGABYTE) {
                    ramLong = GIGABYTE;
                } else if (ramLong > GIGABYTE && ramLong <= 2147483648L) {
                    ramLong = 2147483648L;
                } else if (ramLong > 2147483648L && ramLong <= 3221225472L) {
                    ramLong = 3221225472L;
                } else if (ramLong > 3221225472L && ramLong < 4294967296L) {
                    ramLong = 4294967296L;
                }
            }
            if (HIDE_PRODUCT_INFO || FACK_INFO_FOR_CMCCIOT) {
                ramLong = getLongfromDefaultSetting(mHideSettings[2]);
            }
            sizeStr3 = Formatter.formatShortFileSize(context, ramLong);
            if ("ru".equals(Locale.getDefault().getLanguage())) {
                sizeStr3 = Utils.addBlankIntoText(sizeStr3);
            }
            this.mDeviceInfoSettings.setStringSummary(KEY_RAM, sizeStr3);
        }
    }

    private void showFakeInfoIfNeeded(Context context) {
        String fackProductMode = SystemProperties.get("ro.product.fackProductMode", "");
        String fackKernelVersion1 = SystemProperties.get("ro.product.fackKernelVersion1", "");
        String fackKernelVersion2 = SystemProperties.get("ro.product.fackKernelVersion2", "");
        String fackBuildMumber = SystemProperties.get("ro.product.fackBuildMumber", "");
        String fackKernelVersion = fackKernelVersion1 + "\n" + fackKernelVersion2;
        String fackBasebandVersion = SystemProperties.get("ro.product.fackBasebandVersion", "");
        String fackBasebandVersion2 = SystemProperties.get("ro.product.fackBasebandVersion2", "");
        if (!"".equals(fackBuildMumber)) {
            this.mDeviceInfoSettings.setStringSummary(KEY_BUILD_NUMBER, fackBuildMumber);
        }
        if (!"\n".equals(fackKernelVersion)) {
            this.mDeviceInfoSettings.setStringSummary(KEY_KERNEL_VERSION, String.format(fackKernelVersion, new Object[]{"\n"}));
        }
        if (!"".equals(fackBasebandVersion)) {
            if (!"".equals(fackBasebandVersion2)) {
                fackBasebandVersion = fackBasebandVersion + "\n" + fackBasebandVersion2;
            }
            this.mDeviceInfoSettings.setStringSummary(KEY_BASEBAND_VERSION, fackBasebandVersion);
        }
        if (!"".equals(fackProductMode)) {
            this.mDeviceInfoSettings.setStringSummary(KEY_DEVICE_MODEL, fackProductMode);
        }
        if (HIDE_PRODUCT_INFO || FACK_INFO_FOR_CMCCIOT) {
            this.mDeviceInfoSettings.setStringSummary(KEY_RESOLUTION, mHideSettings[5]);
            String fackEmuiVersion = mHideSettings[8];
            this.mDeviceInfoSettings.setStringSummary(KEY_EMUI_VERSION, fackEmuiVersion);
            if (!HIDE_EMUI_INFO) {
                this.mDeviceInfoSettings.findPreference(KEY_EMUI_LOGO).setTitle("" + mHideSettings[8]);
            }
            this.mDeviceInfoSettings.setStringSummary(KEY_FIRMWARE_VERSION, mHideSettings[6]);
            if (Build.MODEL != null && Build.MODEL.contains("M100") && Build.MODEL.contains("L10")) {
                mHideSettings[3] = "27 GB";
                mHideSettings[4] = "32 GB";
            }
            long backgoundLong = getLongfromDefaultSetting(mHideSettings[3]);
            long totalLong = getLongfromDefaultSetting(mHideSettings[4]);
            String backgrounSize = Formatter.formatShortFileSize(context, backgoundLong);
            String totalSize = Formatter.formatShortFileSize(context, totalLong);
            String background = this.mDeviceInfoSettings.getResources().getString(2131625714, new Object[]{backgrounSize});
            this.mDeviceInfoSettings.setStringSummary(KEY_INTERNAL_STORAGE, background + "\n" + this.mDeviceInfoSettings.getResources().getString(2131627408, new Object[]{totalSize}));
        }
    }

    private void displaySmscNumber(Preference mSmscNumberPreference, TelephonyManager telephonyManager, PreferenceScreen root) {
        if (telephonyManager.getPhoneCount() != 1) {
            root.removePreference(mSmscNumberPreference);
        } else if (DISPLAY_SMSC_NUMBER) {
            String number = SmsManagerEx.getSmscAddr(SmsManager.getDefault());
            if (number != null && !number.equals("")) {
                String[] strArray = number.split("\"");
                if (strArray.length > 1) {
                    number = strArray[1];
                }
                this.mDeviceInfoSettings.setStringSummary(KEY_SMSC_NUMBER, number);
            }
        } else {
            root.removePreference(mSmscNumberPreference);
        }
    }

    private String reverseString(String str) {
        String result = "";
        for (int i = 0; i < str.length(); i++) {
            result = str.charAt(i) + result;
        }
        return result;
    }

    private static String readLine(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename), 256);
        String str = null;
        try {
            str = reader.readLine();
            return str;
        } finally {
            reader.close();
        }
    }

    private boolean isNormalVersion(String cVersion) {
        boolean z = true;
        if (TextUtils.isEmpty(cVersion)) {
            return true;
        }
        cVersion = cVersion.trim();
        if (cVersion.length() <= 1) {
            return true;
        }
        int versionNum = 1;
        try {
            versionNum = Integer.parseInt(cVersion.substring(1));
        } catch (Exception e) {
            Log.e(LOG_TAG, "Integer.parseInt(custC.substring(1)) occurs error, message is " + e.getMessage());
        }
        if (versionNum != 0) {
            z = false;
        }
        return z;
    }

    private String getFormattedKernelVersion() {
        try {
            String kernelVersion = readLine(FILENAME_PROC_VERSION);
            if (kernelVersion == null) {
                return "Unavailable";
            }
            if (kernelVersion.contains("-dirty")) {
                kernelVersion = kernelVersion.replaceAll("-dirty", "");
            }
            return formatKernelVersion(kernelVersion);
        } catch (IOException e) {
            Log.e(LOG_TAG, "IO Exception when getting kernel version for Device Info screen", e);
            return "Unavailable";
        }
    }

    private String formatKernelVersion(String rawKernelVersion) {
        String PROC_VERSION_REGEX = "Linux version (\\S+) \\((\\S+?)\\) (?:\\(gcc.+? \\)) (#\\d+) (?:.*?)?((Sun|Mon|Tue|Wed|Thu|Fri|Sat).+)";
        Matcher m = Pattern.compile("Linux version (\\S+) \\((\\S+?)\\) (?:\\(gcc.+? \\)) (#\\d+) (?:.*?)?((Sun|Mon|Tue|Wed|Thu|Fri|Sat).+)").matcher(rawKernelVersion);
        if (!m.matches()) {
            Log.d(LOG_TAG, "Regex did not match on /proc/version: " + rawKernelVersion);
            return "Unavailable";
        } else if (m.groupCount() < 4) {
            Log.d(LOG_TAG, "Regex match on /proc/version only returned " + m.groupCount() + " groups");
            return "Unavailable";
        } else {
            String linuxVersion = "3.10.30-";
            if (mHideSettings != null && mHideSettings.length >= 9) {
                linuxVersion = mHideSettings[7] + "-";
            }
            if (mReverseImei.length() > 13) {
                linuxVersion = linuxVersion + mReverseImei.substring(7, 12) + "-" + mReverseImei.substring(12, mReverseImei.length());
            } else if (HIDE_PRODUCT_INFO || FACK_INFO_FOR_CMCCIOT) {
                linuxVersion = linuxVersion + HwCustStatusImpl.SUMMARY_UNKNOWN;
            } else {
                linuxVersion = m.group(1);
            }
            return linuxVersion + "\n" + m.group(2) + " " + m.group(3) + "\n" + m.group(4);
        }
    }

    public Dialog getOperatorAndCountryDialog() {
        return new Builder(this.mDeviceInfoSettings.getActivity()).setTitle(2131629182).setMessage(getOperatorAndCountryInfo()).setPositiveButton(2131629189, null).create();
    }

    public String getOperatorAndCountryInfo() {
        String operatorCountryQuery;
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        File operatorCountryInfoFile = new File(HwCustDeviceInfoSettings.OPERATOR_COUNTRY_FILE_NAME);
        if (!operatorCountryInfoFile.exists()) {
            return this.mDeviceInfoSettings.getResources().getString(2131629186);
        }
        if (!operatorCountryInfoFile.canRead()) {
            return this.mDeviceInfoSettings.getResources().getString(2131629185);
        }
        BufferedReader bufferedReader = null;
        try {
            BufferedReader inReader = new BufferedReader(new InputStreamReader(new FileInputStream(operatorCountryInfoFile), "UTF-8"));
            try {
                operatorCountryQuery = inReader.readLine();
                if (operatorCountryQuery != null) {
                    int pos = operatorCountryQuery.lastIndexOf(File.separatorChar);
                    String carryName = operatorCountryQuery.substring(0, pos);
                    String countryName = operatorCountryQuery.substring(pos + 1);
                    operatorCountryQuery = String.format(this.mDeviceInfoSettings.getResources().getString(2131629187), new Object[]{carryName}) + "\n" + String.format(this.mDeviceInfoSettings.getResources().getString(2131629188), new Object[]{countryName});
                }
                if (inReader == null) {
                    return operatorCountryQuery;
                }
                try {
                    inReader.close();
                    return operatorCountryQuery;
                } catch (IOException e3) {
                    return operatorCountryQuery;
                }
            } catch (FileNotFoundException e4) {
                e = e4;
                bufferedReader = inReader;
                Log.e(LOG_TAG, "FileNotFoundException happened.", e);
                operatorCountryQuery = this.mDeviceInfoSettings.getResources().getString(2131629184);
                if (bufferedReader != null) {
                    return operatorCountryQuery;
                }
                try {
                    bufferedReader.close();
                    return operatorCountryQuery;
                } catch (IOException e5) {
                    return operatorCountryQuery;
                }
            } catch (IOException e6) {
                e2 = e6;
                bufferedReader = inReader;
                try {
                    Log.e(LOG_TAG, "IOException happened.", e2);
                    operatorCountryQuery = this.mDeviceInfoSettings.getResources().getString(2131629184);
                    if (bufferedReader != null) {
                        return operatorCountryQuery;
                    }
                    try {
                        bufferedReader.close();
                        return operatorCountryQuery;
                    } catch (IOException e7) {
                        return operatorCountryQuery;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e8) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                bufferedReader = inReader;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e9) {
            e = e9;
            Log.e(LOG_TAG, "FileNotFoundException happened.", e);
            operatorCountryQuery = this.mDeviceInfoSettings.getResources().getString(2131629184);
            if (bufferedReader != null) {
                return operatorCountryQuery;
            }
            bufferedReader.close();
            return operatorCountryQuery;
        } catch (IOException e10) {
            e2 = e10;
            Log.e(LOG_TAG, "IOException happened.", e2);
            operatorCountryQuery = this.mDeviceInfoSettings.getResources().getString(2131629184);
            if (bufferedReader != null) {
                return operatorCountryQuery;
            }
            bufferedReader.close();
            return operatorCountryQuery;
        }
    }

    private long getLongfromDefaultSetting(String str) {
        int strLength = str.length();
        float numFloat = 0.0f;
        String numString = str.substring(0, strLength - 2);
        String unitString = str.substring(strLength - 2);
        try {
            numFloat = Float.parseFloat(numString);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (unitString.equals("GB")) {
            return ((long) numFloat) * GIGABYTE;
        }
        if (unitString.equals("MB")) {
            return ((long) numFloat) * MEGABYTE;
        }
        return 0;
    }

    public String readFileByChars(String fileName) {
        IOException e1;
        Throwable th;
        File file = new File(fileName);
        if (!file.exists() || !file.canRead()) {
            return "";
        }
        Reader reader = null;
        char[] tempChars = new char[512];
        StringBuilder sb = new StringBuilder();
        try {
            Reader reader2 = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
            while (true) {
                try {
                    int charRead = reader2.read(tempChars);
                    if (charRead == -1) {
                        break;
                    }
                    sb.append(tempChars, 0, charRead);
                } catch (IOException e) {
                    e1 = e;
                    reader = reader2;
                } catch (Throwable th2) {
                    th = th2;
                    reader = reader2;
                }
            }
            if (reader2 != null) {
                try {
                    reader2.close();
                } catch (IOException e2) {
                }
            }
        } catch (IOException e3) {
            e1 = e3;
            try {
                e1.printStackTrace();
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e4) {
                    }
                }
                return sb.toString();
            } catch (Throwable th3) {
                th = th3;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e5) {
                    }
                }
                throw th;
            }
        }
        return sb.toString();
    }

    public HashMap<String, String> getMccmncSutelMap() {
        HashMap<String, String> mccmncSutelMap = new HashMap();
        String mccmncSutelNumberStr = Systemex.getString(this.mDeviceInfoSettings.getContentResolver(), "hw_showSutelBySim");
        String[] units = null;
        if (!TextUtils.isEmpty(mccmncSutelNumberStr)) {
            units = mccmncSutelNumberStr.split(";");
        }
        if (units != null && units.length > 0) {
            for (int i = 0; i < units.length; i++) {
                if (!TextUtils.isEmpty(units[i])) {
                    String[] mccmncSutelUnit = units[i].split(",");
                    if (2 == mccmncSutelUnit.length) {
                        mccmncSutelMap.put(mccmncSutelUnit[0], mccmncSutelUnit[1]);
                    }
                }
            }
        }
        return mccmncSutelMap;
    }

    public static String getSutelVersion(Context context) {
        String sutelVersion = Systemex.getString(context.getContentResolver(), "sutelversion");
        if (sutelVersion == null) {
            return "";
        }
        String[] strs = sutelVersion.split(";");
        HashMap<String, String> map = new HashMap();
        for (String temp : strs) {
            if (temp.contains(":")) {
                String key = temp.substring(0, temp.indexOf(":")).trim();
                if (!"".equals(key)) {
                    map.put(key, temp.substring(temp.indexOf(":") + 1));
                }
            }
        }
        String productName = SystemProperties.get("ro.product.name").trim();
        if ("".equals(productName) || !map.containsKey(productName)) {
            return "";
        }
        return (String) map.get(productName);
    }

    public static String getHardWareVersion(Context context) {
        String hardWareVersion = System.getString(context.getContentResolver(), "hardwareversion");
        if (hardWareVersion == null) {
            return "";
        }
        String[] strs = hardWareVersion.split(";");
        HashMap<String, String> map = new HashMap();
        for (String temp : strs) {
            if (temp.contains(":")) {
                String key = temp.substring(0, temp.indexOf(":")).trim();
                if (!"".equals(key)) {
                    map.put(key, temp.substring(temp.indexOf(":") + 1));
                }
            }
        }
        String productName = SystemProperties.get("ro.product.name").trim();
        if ("".equals(productName) || !map.containsKey(productName)) {
            return "";
        }
        return (String) map.get(productName);
    }

    private void changeDeviceModel() {
        if ("LON-AL00-PD".equals(Build.MODEL)) {
            this.mDeviceInfoSettings.setStringSummary(KEY_DEVICE_MODEL, "LON-AL00");
        } else if ("LON-L29-PD".equals(Build.MODEL)) {
            this.mDeviceInfoSettings.setStringSummary(KEY_DEVICE_MODEL, "LON-L29");
        }
    }
}

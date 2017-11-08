package com.huawei.systemmanager.antivirus.utils;

import android.app.AlarmManager;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.XmlResourceParser;
import android.provider.Settings.System;
import android.util.Xml;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.antivirus.cache.VirusAppsManager;
import com.huawei.systemmanager.antivirus.ui.AntiVirusActivity;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.customize.AbroadUtils;
import com.huawei.systemmanager.util.HwLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Random;
import org.xmlpull.v1.XmlPullParser;

public class AntiVirusTools {
    public static final String ACTION_AUTO_UPDATE_VIRUS_LIB = "huawei.intent.action.antivirus.autoupdate";
    public static final String ACTION_CHECK_URL = "huawei.intent.action.antivirus.checkurl";
    public static final String ACTION_DELETE_VIRUS_APK = "huawei.intent.action.antivirus.deleteVirusApk";
    public static final String ACTION_FOUND_VIRUSSCAN_APP = "com.huawei.systemmanager.action.found_virusscan_app";
    public static final String ACTION_SCAN_ONE_PACKAGE = "huawei.intent.action.antivirus.scanonepackage";
    public static final String ACTION_UPDATE_VIRUS_DATA = "com.huawei.systemmanager.action.update.update.virus.data";
    public static final int ADAPTER_ITEM_APP = 2;
    public static final int ADAPTER_ITEM_RESULT = 1;
    public static final int ADATPER_ITEM_SCAN = 0;
    public static final int ALARM_REQUEST_CODE = 0;
    public static final String ANTIVIRIS_CONFIG_FILE = "data/cust/xml/antivirus_config.xml";
    public static final String ANTIVIRUS_ENGINE_360 = "360";
    public static final String ANTIVIRUS_ENGINE_TENCENT = "tencent";
    public static final String ANTIVIRUS_STATUS = "antivirus_status";
    public static final int ANTIVIRUS_STATUS_DANGER = 1;
    public static final int ANTIVIRUS_STATUS_SECURITY = 0;
    public static final String ANTI_VIRUS_ENGINE = "anti_virus_engine";
    public static final String AUTO_UPDATE_TIMESTAMP = "auto_update_timestamp";
    public static final String CLOUD_SCAN_SWITCH = "cloud_scan_switch";
    public static final int CLOUD_SECURITY_CLOSE = 0;
    public static final int CLOUD_SECURITY_OPEN = 1;
    public static final String CLOUD_SECURITY_STATE = "cloudsecuritystate";
    public static final boolean DEFAULT_AUTO_UPDATE = false;
    public static final int DEFAULT_SCAN_MODE = 0;
    public static final int DEFAULT_UPDATE_RATE = 3;
    public static final boolean DEFAULT_WIFI_ONLY_UPDATE = true;
    public static final String DELETE_ITEM = "delete_item";
    public static final int FROM_MAIN_SCREEN_TO_AD = 1;
    public static final String GLOBAL_SCAN_MODE = "global_can";
    public static final String GLOBAL_TIMER_REMIND = "global_timer_remind";
    public static final String IS_AUTO_UPDATE_VIRUS_LIB = "is_auto_update_virus_lib";
    public static final String IS_IN_SDCARD_FILE = "is_in_sdcard_file";
    public static final String IS_WIFI_ONLY_UPDATE = "is_wifi_only_update";
    public static final String KEY_FROM_MAIN_SCREEN_TO_AD = "from_mainscreen_to_ad";
    public static final String KEY_RESULT = "key_result";
    public static final String MANUAL_UPDATE_TIMESTAMP = "manual_update_timestamp";
    public static final String MANUAL_UPDATE_VIRUS_LIB = "manual_update_virus_lib";
    public static final int MSG_CHECK_VIRUS_LIB_CANCEL = 2;
    public static final int MSG_CHECK_VIRUS_LIB_ERROR = 1;
    public static final int MSG_CHECK_VIRUS_LIB_FINISH = 3;
    public static final int MSG_CHECK_VIRUS_LIB_START = 0;
    public static final int MSG_DISMISS_DIALOG = 4;
    public static final int MSG_HANDLE_CONNECT_CHANGE = 11;
    public static final int MSG_HANDLE_UPDATE_TIME_OUT = 12;
    public static final int MSG_SCAN_CANCEL = 16;
    public static final int MSG_SCAN_CLOUD_SCAN = 13;
    public static final int MSG_SCAN_CLOUD_SCAN_ERROR = 14;
    public static final int MSG_SCAN_CONTINUE = 18;
    public static final int MSG_SCAN_ENGINE_INIT = 19;
    public static final int MSG_SCAN_ERROR = 15;
    public static final int MSG_SCAN_FINISH = 12;
    public static final int MSG_SCAN_INTERRUPT = 30;
    public static final int MSG_SCAN_PAUSE = 17;
    public static final int MSG_SCAN_PROGRESS_CHANGE = 11;
    public static final int MSG_SCAN_START = 10;
    public static final int MSG_UPDATE_LOGO_UI = 24;
    public static final int MSG_UPDATE_RISKPERM_PKGS = 23;
    public static final int MSG_UPDATE_VIRUS_LIB_CANCEL = 6;
    public static final int MSG_UPDATE_VIRUS_LIB_ERROR = 8;
    public static final int MSG_UPDATE_VIRUS_LIB_FINISH = 9;
    public static final int MSG_UPDATE_VIRUS_LIB_NO_WORK = 10;
    public static final int MSG_UPDATE_VIRUS_LIB_PROGRESS = 7;
    public static final int MSG_UPDATE_VIRUS_LIB_START = 5;
    public static final int MSG_URL_CHECK_HARM = 20;
    public static final int MSG_URL_CHECK_OK = 19;
    public static final int MSG_URL_CHECK_SHADINESS = 21;
    public static final int NOTIFICATION_ID_AD_BLOCK_SOFT = 4;
    public static final int NOTIFICATION_ID_NOT_OFFICIAL_SOFT = 6;
    public static final int NOTIFICATION_ID_RISK_SOFT = 3;
    public static final int NOTIFICATION_ID_SCANNING = 0;
    public static final int NOTIFICATION_ID_SECURE_SOFT = 1;
    public static final int NOTIFICATION_ID_UNKNOW_SOFT = 5;
    public static final int NOTIFICATION_ID_VIRUS_SOFT = 2;
    public static final int NOTIFICATION_SINGLE_SCAN = 7;
    public static final long ONE_DAY_TO_TIMESTAMP = 86400000;
    public static final String PACKAGE_NAME = "package_name";
    public static final int PROCESS_VALUE_MAX = 100;
    public static final int PROCESS_VALUE_MAX_FAKE = 80;
    public static final String QUICK_SCAN_MODE = "quick_scan";
    public static final int RANDOM_MINITES = 21600000;
    public static final int REALTIME_CLOSE = 0;
    public static final int REALTIME_OPEN = 1;
    public static final String REAL_TIME_PROTECTION = "real_time_protection";
    public static final int REQUEST_CODE = 10001;
    public static final int REQUEST_CODE_GLOBAL_SCAN = 10004;
    public static final int REQUEST_CODE_RISK_PERM = 10003;
    public static final int RESULT_CODE = 10002;
    public static final String RESULT_ITEM = "result";
    public static final String RESULT_LIST = "resultList";
    public static final String RESULT_TYPE = "resultType";
    public static final int SCAN_ALL_PACKAGES = 1000;
    public static final int SCAN_CLOUD = 2;
    public static final int SCAN_INDEX_TYPE_CLOUD_SCAN = 3;
    public static final int SCAN_INDEX_TYPE_COMPETITOR = 3;
    public static final int SCAN_INDEX_TYPE_NOT_OFFICIAL = 2;
    public static final int SCAN_INDEX_TYPE_RISK = 1;
    public static final int SCAN_INDEX_TYPE_VIRUS = 0;
    public static final int SCAN_INSTALLEDPKGS = 0;
    public static final String SCAN_MODE = "scan_mode";
    public static final int SCAN_MODE_GLOBAL = 1;
    public static final int SCAN_MODE_QUICK = 0;
    public static final int SCAN_ONE_PACKAGE = 1001;
    public static final int SCAN_UNINSTALLEDAPKS = 1;
    public static final String SETTING_NAME = "com.huawei.systemmanager.antivirus_setting_preference";
    private static final String TAG = "AntiVirusTools";
    public static final String TIMER_REMIND_TIME_STAMP = "time_remind_time_stamp";
    public static final int TOAST_DURATION = 3000;
    public static final int TYPE_ADVERTISE = 307;
    public static final int TYPE_ADVERTISE_CN = 309;
    public static final int TYPE_AD_BLOCK = 306;
    public static final int TYPE_CLOUD_SCAN = 310;
    public static final int TYPE_NOT_OFFICIAL = 304;
    public static final int TYPE_OK = 301;
    public static final int TYPE_RISK = 303;
    public static final int TYPE_RISKPERM = 308;
    public static final int TYPE_UNKNOW = 302;
    public static final int TYPE_VIRUS = 305;
    public static final String UPDATE_VIRUS_LIB_RATE = "update_virus_lib_rate";
    public static final String URL = "url";
    public static final String VIRUS_LIB_VERSION = "virus_lib_version";
    public static final String VIRUS_SCAN_TIMESTAMP = "virus_scan_timestamp";

    public static void setRealTimeProtectionMode(Context c, int enable) {
        Editor editor = c.getSharedPreferences(SETTING_NAME, 4).edit();
        editor.putInt(REAL_TIME_PROTECTION, enable);
        editor.commit();
    }

    public static void setScanMode(Context c, int scanMode) {
        Editor editor = c.getSharedPreferences(SETTING_NAME, 4).edit();
        editor.putInt(SCAN_MODE, scanMode);
        editor.commit();
    }

    public static void setVirusLibVersion(Context c, String virusLibVersion) {
        Editor editor = c.getSharedPreferences(SETTING_NAME, 4).edit();
        editor.putString(VIRUS_LIB_VERSION, virusLibVersion);
        editor.commit();
    }

    public static void setCloudScan(Context c, boolean bCloudScan) {
        Editor editor = c.getSharedPreferences(SETTING_NAME, 4).edit();
        editor.putBoolean(CLOUD_SCAN_SWITCH, bCloudScan);
        editor.commit();
    }

    public static void setGlobalTimerRemind(Context c, boolean bRemind) {
        Editor editor = c.getSharedPreferences(SETTING_NAME, 4).edit();
        editor.putBoolean(GLOBAL_TIMER_REMIND, bRemind);
        editor.commit();
    }

    public static void setAutoUpdate(Context c, boolean bAutoUpdate) {
        Editor editor = c.getSharedPreferences(SETTING_NAME, 4).edit();
        editor.putBoolean(IS_AUTO_UPDATE_VIRUS_LIB, bAutoUpdate);
        editor.commit();
    }

    public static void setWifiOnlyUpdate(Context c, boolean bWifiOnlyUpdate) {
        Editor editor = c.getSharedPreferences(SETTING_NAME, 4).edit();
        editor.putBoolean(IS_WIFI_ONLY_UPDATE, bWifiOnlyUpdate);
        editor.commit();
    }

    public static void setUpdateRate(Context c, int updateRate) {
        Editor editor = c.getSharedPreferences(SETTING_NAME, 4).edit();
        editor.putInt(UPDATE_VIRUS_LIB_RATE, updateRate);
        editor.commit();
    }

    public static void setAntiVirusEngineName(Context c, String antiVirusEngineName) {
        Editor editor = c.getSharedPreferences(SETTING_NAME, 4).edit();
        editor.putString(ANTI_VIRUS_ENGINE, antiVirusEngineName);
        editor.commit();
    }

    public static void setAutoUpdateStamp(Context c, long timestamp) {
        Editor editor = c.getSharedPreferences(SETTING_NAME, 4).edit();
        editor.putLong(AUTO_UPDATE_TIMESTAMP, timestamp);
        editor.commit();
    }

    public static void setManualUpdateStamp(Context c, long timestamp) {
        Editor editor = c.getSharedPreferences(SETTING_NAME, 4).edit();
        editor.putLong(MANUAL_UPDATE_TIMESTAMP, timestamp);
        editor.commit();
    }

    public static void setAntiVirusStatus(Context c, int status) {
        Editor editor = c.getSharedPreferences(SETTING_NAME, 4).edit();
        editor.putInt(ANTIVIRUS_STATUS, status);
        editor.commit();
    }

    public static int getAntiVirusStatus(Context c) {
        return c.getSharedPreferences(SETTING_NAME, 4).getInt(ANTIVIRUS_STATUS, 0);
    }

    public static boolean getRealTimeProtectionMode(Context c) {
        if (c.getSharedPreferences(SETTING_NAME, 4).getInt(REAL_TIME_PROTECTION, 0) == 0) {
            return false;
        }
        return true;
    }

    public static int getScanMode(Context c) {
        return c.getSharedPreferences(SETTING_NAME, 4).getInt(SCAN_MODE, 0);
    }

    public static String getVirusLibVersion(Context c) {
        return c.getSharedPreferences(SETTING_NAME, 4).getString(VIRUS_LIB_VERSION, "");
    }

    public static boolean isCloudScanSwitchOn(Context c) {
        return c.getSharedPreferences(SETTING_NAME, 4).getBoolean(CLOUD_SCAN_SWITCH, true);
    }

    public static boolean isGlobalTimerSwitchOn(Context c) {
        return c.getSharedPreferences(SETTING_NAME, 4).getBoolean(GLOBAL_TIMER_REMIND, false);
    }

    public static boolean isAutoUpdate(Context c) {
        return c.getSharedPreferences(SETTING_NAME, 4).getBoolean(IS_AUTO_UPDATE_VIRUS_LIB, false);
    }

    public static boolean isWiFiOnlyUpdate(Context c) {
        return c.getSharedPreferences(SETTING_NAME, 4).getBoolean(IS_WIFI_ONLY_UPDATE, true);
    }

    public static int getUpdateRate(Context c) {
        return c.getSharedPreferences(SETTING_NAME, 4).getInt(UPDATE_VIRUS_LIB_RATE, 3);
    }

    public static String getEngineNameFromSharedPre(Context c) {
        return c.getSharedPreferences(SETTING_NAME, 4).getString(ANTI_VIRUS_ENGINE, ANTIVIRUS_ENGINE_TENCENT);
    }

    public static long getAutoUpdateStamp(Context c) {
        return c.getSharedPreferences(SETTING_NAME, 4).getLong(AUTO_UPDATE_TIMESTAMP, 0);
    }

    public static void setVirusScanStamp(Context c, long timestamp) {
        Editor editor = c.getSharedPreferences(SETTING_NAME, 4).edit();
        editor.putLong(MANUAL_UPDATE_TIMESTAMP, timestamp);
        editor.commit();
    }

    public static long getVirusScanStamp(Context c) {
        return c.getSharedPreferences(SETTING_NAME, 4).getLong(VIRUS_SCAN_TIMESTAMP, System.currentTimeMillis());
    }

    public static long getManualUpdateStamp(Context c) {
        return c.getSharedPreferences(SETTING_NAME, 4).getLong(MANUAL_UPDATE_TIMESTAMP, 0);
    }

    public static int getScanModeFromXml(Context c) {
        String value = getItemValueFromXml(c, SCAN_MODE);
        if (value == null || "".equals(value)) {
            return 0;
        }
        return Integer.parseInt(value);
    }

    public static String getEngineNameFromXml(Context c) {
        return getItemValueFromXml(c, ANTI_VIRUS_ENGINE);
    }

    public static boolean getIsAutoUpdateFromXml(Context c) {
        String value = getItemValueFromXml(c, IS_AUTO_UPDATE_VIRUS_LIB);
        if (value == null || "".equals(value)) {
            return false;
        }
        return Boolean.valueOf(value).booleanValue();
    }

    public static boolean getIsWifiOnlyUpdateFromXml(Context c) {
        String value = getItemValueFromXml(c, IS_WIFI_ONLY_UPDATE);
        if (value == null || "".equals(value)) {
            return true;
        }
        return Boolean.valueOf(value).booleanValue();
    }

    public static int getUpdateRateFromXml(Context c) {
        String value = getItemValueFromXml(c, UPDATE_VIRUS_LIB_RATE);
        if (value == null || "".equals(value)) {
            return 3;
        }
        return Integer.parseInt(value);
    }

    public static String getItemValueFromXml(Context c, String key) {
        if (new File(ANTIVIRIS_CONFIG_FILE).exists()) {
            return getItemValueFromCustXml(key);
        }
        return getItemValueFromResourceXml(c, key);
    }

    private static String getItemValueFromCustXml(String key) {
        Exception e;
        Throwable th;
        InputStream inputStream = null;
        XmlPullParser xmlPullParser = null;
        try {
            InputStream inputStream2 = new FileInputStream(ANTIVIRIS_CONFIG_FILE);
            try {
                xmlPullParser = Xml.newPullParser();
                xmlPullParser.setInput(inputStream2, "utf-8");
                while (xmlPullParser.next() != 1) {
                    if (xmlPullParser.getName() != null && xmlPullParser.getName().equals("setting") && xmlPullParser.getEventType() == 2) {
                        String itemKey = xmlPullParser.getAttributeValue(null, "name");
                        if (key != null && key.equals(itemKey) && xmlPullParser.next() == 4) {
                            String text = xmlPullParser.getText();
                            if (inputStream2 != null) {
                                try {
                                    inputStream2.close();
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                }
                            }
                            if (xmlPullParser != null) {
                                try {
                                    xmlPullParser.setInput(null);
                                } catch (Exception e22) {
                                    e22.printStackTrace();
                                }
                            }
                            return text;
                        }
                    }
                }
                if (inputStream2 != null) {
                    try {
                        inputStream2.close();
                    } catch (Exception e222) {
                        e222.printStackTrace();
                    }
                }
                if (xmlPullParser != null) {
                    try {
                        xmlPullParser.setInput(null);
                    } catch (Exception e2222) {
                        e2222.printStackTrace();
                    }
                }
            } catch (Exception e3) {
                e2222 = e3;
                inputStream = inputStream2;
                try {
                    e2222.printStackTrace();
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Exception e22222) {
                            e22222.printStackTrace();
                        }
                    }
                    if (xmlPullParser != null) {
                        try {
                            xmlPullParser.setInput(null);
                        } catch (Exception e222222) {
                            e222222.printStackTrace();
                        }
                    }
                    return "";
                } catch (Throwable th2) {
                    th = th2;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Exception e2222222) {
                            e2222222.printStackTrace();
                        }
                    }
                    if (xmlPullParser != null) {
                        try {
                            xmlPullParser.setInput(null);
                        } catch (Exception e22222222) {
                            e22222222.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                inputStream = inputStream2;
                if (inputStream != null) {
                    inputStream.close();
                }
                if (xmlPullParser != null) {
                    xmlPullParser.setInput(null);
                }
                throw th;
            }
        } catch (Exception e4) {
            e22222222 = e4;
            e22222222.printStackTrace();
            if (inputStream != null) {
                inputStream.close();
            }
            if (xmlPullParser != null) {
                xmlPullParser.setInput(null);
            }
            return "";
        }
        return "";
    }

    private static String getItemValueFromResourceXml(Context c, String key) {
        String resources = c.getResources();
        XmlResourceParser parser = resources.getXml(R.xml.antivirus_config);
        while (parser.next() != 1) {
            try {
                resources = parser.getName();
                if (resources != null) {
                    resources = parser.getName().equals("setting");
                    if (resources != null) {
                        resources = "name";
                        String itemkey = parser.getAttributeValue(null, resources);
                        if (key != null) {
                            resources = key.equals(itemkey);
                            if (resources != null) {
                                resources = parser.next();
                                if (resources == 4) {
                                    resources = parser.getText();
                                    return resources;
                                }
                            } else {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                parser.close();
            }
        }
        parser.close();
        return "";
    }

    public static void saveAntiVirusSettingsFromXml(Context c) {
        SharedPreferences sp = c.getSharedPreferences(SETTING_NAME, 4);
        String engineName = sp.getString(ANTI_VIRUS_ENGINE, "");
        if (engineName == null || "".equals(engineName)) {
            Editor editor = sp.edit();
            engineName = getEngineNameFromXml(c);
            int scanMode = getScanModeFromXml(c);
            boolean isAutoUpdate = getIsAutoUpdateFromXml(c);
            boolean isWifiOnlyUpdate = getIsWifiOnlyUpdateFromXml(c);
            int updateRate = getUpdateRateFromXml(c);
            editor.putString(ANTI_VIRUS_ENGINE, engineName);
            editor.putInt(SCAN_MODE, scanMode);
            editor.putBoolean(IS_AUTO_UPDATE_VIRUS_LIB, isAutoUpdate);
            editor.putBoolean(IS_WIFI_ONLY_UPDATE, isWifiOnlyUpdate);
            editor.putInt(UPDATE_VIRUS_LIB_RATE, updateRate);
            editor.commit();
        }
    }

    public static void startAutoUpdateVirusLibAlarm(Context context, int day) {
        if (context == null) {
            HwLog.e(TAG, "the context is null");
            return;
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        Random random = new Random();
        long lastTime = getAutoUpdateStamp(context);
        long currentTime = System.currentTimeMillis();
        if (currentTime <= 0 || currentTime < lastTime) {
            lastTime = currentTime + ((long) random.nextInt(21600000));
            setAutoUpdateStamp(context, lastTime);
        }
        if (currentTime - lastTime > 157680000000L) {
            HwLog.i(TAG, "the time not update have more than five year ,do not update");
            setAutoUpdateStamp(context, currentTime);
            return;
        }
        long intervalMillis = ((long) day) * 86400000;
        alarmManager.setRepeating(1, lastTime + intervalMillis, intervalMillis, createAotuUpdateIntent(context));
    }

    public static PendingIntent createAotuUpdateIntent(Context context) {
        Intent intent = new Intent(ACTION_AUTO_UPDATE_VIRUS_LIB);
        intent.setPackage("com.huawei.systemmanager");
        return PendingIntent.getService(context, 0, intent, 134217728);
    }

    public static void updateTimerRemindTimeStamp(Context c) {
        c.getSharedPreferences(SETTING_NAME, 4).edit().putLong(TIMER_REMIND_TIME_STAMP, System.currentTimeMillis()).commit();
    }

    public static long getTimerRemindTimeStamp(Context c) {
        return c.getSharedPreferences(SETTING_NAME, 4).getLong(TIMER_REMIND_TIME_STAMP, 0);
    }

    public static void cancelAutoUpdateVirusLibAlarm(Context context) {
        ((AlarmManager) context.getSystemService("alarm")).cancel(createAotuUpdateIntent(context));
    }

    public static String getApplicationName(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return packageName;
        }
        try {
            return (String) pm.getApplicationInfo(packageName, 1).loadLabel(pm);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return packageName;
        }
    }

    public static void sendMessageForScanInstallApk(Context context, String message) {
        sendNotificationInSingleScan(context, message);
    }

    public static boolean deleteApkFile(String apkFilePath) {
        File file = new File(apkFilePath);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    public static boolean isCloudClose(Context context) {
        return System.getInt(context.getContentResolver(), CLOUD_SECURITY_STATE, 1) == 0;
    }

    public static void checkVirusForNewInstalledApk(Context context, Intent intent) {
        if (context != null && intent != null) {
            String data = intent.getDataString();
            Intent serviceIntent = new Intent(ACTION_SCAN_ONE_PACKAGE);
            serviceIntent.putExtra("package_name", data);
            serviceIntent.setPackage(context.getPackageName());
            context.startService(serviceIntent);
        }
    }

    public static void deleteVirusApk(Context context, Intent intent) {
        if (context != null && intent != null) {
            String data = intent.getDataString();
            Intent serviceIntent = new Intent(ACTION_DELETE_VIRUS_APK);
            serviceIntent.putExtra("package_name", data);
            serviceIntent.setPackage(context.getPackageName());
            context.startService(serviceIntent);
        }
    }

    private static void sendNotificationInSingleScan(Context context, String message) {
        Builder mBuilder = new Builder(context).setSmallIcon(R.drawable.ic_virus_notification).setContentTitle(message).setContentText(GlobalContext.getString(R.string.roaming_traffic_notification_summary));
        mBuilder.setTicker(message);
        mBuilder.setAutoCancel(true);
        mBuilder.setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, AntiVirusActivity.class), 134217728));
        ((NotificationManager) context.getSystemService("notification")).notify(7, mBuilder.build());
    }

    public static boolean isAbroad() {
        return AbroadUtils.isAbroad();
    }

    public static boolean isMaliciousApk(ScanResultEntity result) {
        if (result.type == TYPE_VIRUS || result.type == 303 || result.type == 304 || result.type == TYPE_ADVERTISE) {
            return true;
        }
        return false;
    }

    public static void refreshData(Context context, ScanResultEntity result) {
        VirusAppsManager virusAppsManager = VirusAppsManager.getIntance();
        if (isMaliciousApk(result)) {
            HwLog.i(TAG, "isMaliciousApk = true");
            virusAppsManager.insertVirusApp(context, result);
            return;
        }
        HwLog.i(TAG, "isMaliciousApk = false");
        virusAppsManager.deleteVirusApp(result.packageName);
    }
}

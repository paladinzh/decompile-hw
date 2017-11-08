package com.huawei.mms.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import com.android.mms.MmsConfig;
import com.android.mms.ui.CommonPhraseFragment;
import com.huawei.cspcommon.MLog;
import java.io.File;
import java.util.Map.Entry;

public class HwCloudBackUpUtils {
    private static final /* synthetic */ int[] -com-huawei-mms-util-HwCloudBackUpUtils$PreferenceTypeSwitchesValues = null;

    public enum PreferenceType {
        BOOLEAN,
        STRING,
        LONG,
        INT,
        FLOAT
    }

    private static /* synthetic */ int[] -getcom-huawei-mms-util-HwCloudBackUpUtils$PreferenceTypeSwitchesValues() {
        if (-com-huawei-mms-util-HwCloudBackUpUtils$PreferenceTypeSwitchesValues != null) {
            return -com-huawei-mms-util-HwCloudBackUpUtils$PreferenceTypeSwitchesValues;
        }
        int[] iArr = new int[PreferenceType.values().length];
        try {
            iArr[PreferenceType.BOOLEAN.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[PreferenceType.FLOAT.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[PreferenceType.INT.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[PreferenceType.LONG.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[PreferenceType.STRING.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        -com-huawei-mms-util-HwCloudBackUpUtils$PreferenceTypeSwitchesValues = iArr;
        return iArr;
    }

    public static String getPreferenceDirPath(Context context) {
        StringBuilder pathStringBuilder = new StringBuilder();
        pathStringBuilder.append(context.getFilesDir().getParent()).append(File.separator).append("shared_prefs");
        return pathStringBuilder.toString();
    }

    public static String getMmsPreferenceRestoreUnzipDir(Context context) {
        StringBuilder pathStringBuilder = new StringBuilder();
        pathStringBuilder.append(context.getCacheDir().getAbsolutePath()).append(File.separator).append("mms_restore_shared_prefs");
        return pathStringBuilder.toString();
    }

    public static void restoreBackupXmlToSharedPreference(Context context) {
        String restoreFileName = "restore_" + Long.valueOf(System.currentTimeMillis()) + "com.android.mms_preferences.xml";
        File file = new File(getMmsPreferenceRestoreUnzipDir(context) + File.separator + "com.android.mms_preferences.xml");
        File dirFile = new File(getPreferenceDirPath(context), restoreFileName);
        if (ZipUtils.copyFile(file.getAbsolutePath(), dirFile.getAbsolutePath())) {
            SharedPreferences sharedPreferencesRestore = context.getSharedPreferences(restoreFileName.substring(0, restoreFileName.lastIndexOf(".")), 0);
            Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            writeToPreferenceFromPreferenceRestore("smartsms_enhance", String.valueOf(sharedPreferencesRestore.getBoolean("smartsms_enhance", false)), PreferenceType.BOOLEAN, editor);
            SmartSmsSdkUtil.setEnhance(context, sharedPreferencesRestore.getBoolean("smartsms_enhance", false));
            writeToPreferenceFromPreferenceRestore(SmartSmsSdkUtil.SMARTSMS_BUBBLE, String.valueOf(sharedPreferencesRestore.getInt(SmartSmsSdkUtil.SMARTSMS_BUBBLE, 0)), PreferenceType.INT, editor);
            SmartSmsSdkUtil.setBubbleStyle(context, sharedPreferencesRestore.getInt(SmartSmsSdkUtil.SMARTSMS_BUBBLE, 0));
            writeToPreferenceFromPreferenceRestore(SmartSmsSdkUtil.SMARTSMS_UPDATE_TYPE, String.valueOf(sharedPreferencesRestore.getInt(SmartSmsSdkUtil.SMARTSMS_UPDATE_TYPE, 0)), PreferenceType.INT, editor);
            SmartSmsSdkUtil.setUpdateType(context, sharedPreferencesRestore.getInt(SmartSmsSdkUtil.SMARTSMS_UPDATE_TYPE, 0));
            writeToPreferenceFromPreferenceRestore("pref_key_risk_url_check", String.valueOf(sharedPreferencesRestore.getBoolean("pref_key_risk_url_check", false)), PreferenceType.BOOLEAN, editor);
            writeToPreferenceFromPreferenceRestore("pref_key_cancel_send_enable", String.valueOf(sharedPreferencesRestore.getBoolean("pref_key_cancel_send_enable", false)), PreferenceType.BOOLEAN, editor);
            writeToPreferenceFromPreferenceRestore("pref_key_delivery_reports", String.valueOf(sharedPreferencesRestore.getInt("pref_key_delivery_reports", MmsConfig.getDefaultDeliveryReportState())), PreferenceType.INT, editor);
            writeToPreferenceFromPreferenceRestore("pref_key_signature", String.valueOf(sharedPreferencesRestore.getBoolean("pref_key_signature", false)), PreferenceType.BOOLEAN, editor);
            writeToPreferenceFromPreferenceRestore("pref_key_signature_content", String.valueOf(sharedPreferencesRestore.getString("pref_key_signature_content", MmsConfig.getDefaultSignatureText())), PreferenceType.STRING, editor);
            writeToPreferenceFromPreferenceRestore("pref_key_mms_auto_retrieval", String.valueOf(sharedPreferencesRestore.getBoolean("pref_key_mms_auto_retrieval", true)), PreferenceType.BOOLEAN, editor);
            writeToPreferenceFromPreferenceRestore("pref_key_mms_retrieval_during_roaming", String.valueOf(sharedPreferencesRestore.getBoolean("pref_key_mms_retrieval_during_roaming", false)), PreferenceType.BOOLEAN, editor);
            writeToPreferenceFromPreferenceRestore("autoReceiveMms", String.valueOf(sharedPreferencesRestore.getInt("autoReceiveMms", MmsConfig.getDefaultAutoRetrievalMms())), PreferenceType.INT, editor);
            writeToPreferenceFromPreferenceRestore("pref_key_mms_auto_retrieval_mms", String.valueOf(sharedPreferencesRestore.getString("pref_key_mms_auto_retrieval_mms", "")), PreferenceType.STRING, editor);
            writeToPreferenceFromPreferenceRestore("pref_key_sms_wappush_enable", String.valueOf(sharedPreferencesRestore.getBoolean("pref_key_sms_wappush_enable", false)), PreferenceType.BOOLEAN, editor);
            writeToPreferenceFromPreferenceRestore("pref_key_verifition_sms_protect_enable", String.valueOf(sharedPreferencesRestore.getBoolean("pref_key_verifition_sms_protect_enable", true)), PreferenceType.BOOLEAN, editor);
            writeToPreferenceFromPreferenceRestore("pref_key_verifition_sms_protect_enable", String.valueOf(sharedPreferencesRestore.getBoolean("pref_key_verifition_sms_protect_enable", true)), PreferenceType.BOOLEAN, editor);
            writeToPreferenceFromPreferenceRestore("pref_key_smart_archive_enable", String.valueOf(sharedPreferencesRestore.getBoolean("pref_key_smart_archive_enable", SmartArchiveSettingUtils.getsSmartArchiveEnabledDefault())), PreferenceType.BOOLEAN, editor);
            writeToPreferenceFromPreferenceRestore("pref_key_smart_archive_auto_delete", String.valueOf(sharedPreferencesRestore.getBoolean("pref_key_smart_archive_auto_delete", false)), PreferenceType.BOOLEAN, editor);
            writeToPreferenceFromPreferenceRestore("archive_num_huawei", String.valueOf(sharedPreferencesRestore.getBoolean("archive_num_huawei", false)), PreferenceType.BOOLEAN, editor);
            writeToPreferenceFromPreferenceRestore("archive_num_106", String.valueOf(sharedPreferencesRestore.getBoolean("archive_num_106", false)), PreferenceType.BOOLEAN, editor);
            writeToPreferenceFromPreferenceRestore("archive_num_bak", String.valueOf(sharedPreferencesRestore.getBoolean("archive_num_bak", false)), PreferenceType.BOOLEAN, editor);
            writeToPreferenceFromPreferenceRestore("archive_num_comm_operator", String.valueOf(sharedPreferencesRestore.getBoolean("archive_num_comm_operator", false)), PreferenceType.BOOLEAN, editor);
            writeToPreferenceFromPreferenceRestore("pref_key_vibrate", String.valueOf(sharedPreferencesRestore.getBoolean("pref_key_vibrate", false)), PreferenceType.BOOLEAN, editor);
            writeToPreferenceFromPreferenceRestore("pref_key_vibrateWhen", String.valueOf(sharedPreferencesRestore.getBoolean("pref_key_vibrateWhen", false)), PreferenceType.BOOLEAN, editor);
            writeToPreferenceFromPreferenceRestore("pref_key_vibrateWhen_sub0", String.valueOf(sharedPreferencesRestore.getBoolean("pref_key_vibrateWhen_sub0", false)), PreferenceType.BOOLEAN, editor);
            writeToPreferenceFromPreferenceRestore("pref_key_vibrateWhen_sub1", String.valueOf(sharedPreferencesRestore.getBoolean("pref_key_vibrateWhen_sub1", false)), PreferenceType.BOOLEAN, editor);
            writeToPreferenceFromPreferenceRestore("pref_key_ringtone", String.valueOf(sharedPreferencesRestore.getString("pref_key_ringtone", "")), PreferenceType.STRING, editor);
            writeToPreferenceFromPreferenceRestore("pref_key_ringtone_sub0", String.valueOf(sharedPreferencesRestore.getString("pref_key_ringtone_sub0", "")), PreferenceType.STRING, editor);
            writeToPreferenceFromPreferenceRestore("pref_key_ringtone_sub1", String.valueOf(sharedPreferencesRestore.getString("pref_key_ringtone_sub1", "")), PreferenceType.STRING, editor);
            writeToPreferenceFromPreferenceRestore("pref_key_vibrateSp", String.valueOf(sharedPreferencesRestore.getString("pref_key_vibrateSp", "")), PreferenceType.STRING, editor);
            writeToPreferenceFromPreferenceRestore("pref_key_vibrateSp_sub0", String.valueOf(sharedPreferencesRestore.getString("pref_key_vibrateSp_sub0", "")), PreferenceType.STRING, editor);
            writeToPreferenceFromPreferenceRestore("pref_key_vibrateSp_sub1", String.valueOf(sharedPreferencesRestore.getString("pref_key_vibrateSp_sub1", "")), PreferenceType.STRING, editor);
            writeToPreferenceFromPreferenceRestore("pref_key_ringtoneSp", String.valueOf(sharedPreferencesRestore.getString("pref_key_ringtoneSp", "")), PreferenceType.STRING, editor);
            writeToPreferenceFromPreferenceRestore("pref_key_ringtoneSp_sub0", String.valueOf(sharedPreferencesRestore.getString("pref_key_ringtoneSp_sub0", "")), PreferenceType.STRING, editor);
            writeToPreferenceFromPreferenceRestore("pref_key_ringtoneSp_sub1", String.valueOf(sharedPreferencesRestore.getString("pref_key_ringtoneSp_sub1", "")), PreferenceType.STRING, editor);
            String commomPreferenceFileName = CommonPhraseFragment.getCommonPhrasePreferencesName(context);
            if (commomPreferenceFileName != null) {
                File srcCommomPreferenceFile = getCommonRestoreSrcFile(context, new File(getMmsPreferenceRestoreUnzipDir(context)), commomPreferenceFileName);
                File desCommomPreferenceFile = new File(getPreferenceDirPath(context), "restore_" + System.currentTimeMillis() + commomPreferenceFileName + ".xml");
                if (srcCommomPreferenceFile.exists()) {
                    ZipUtils.copyFile(srcCommomPreferenceFile.getAbsolutePath(), desCommomPreferenceFile.getAbsolutePath());
                    refreshPreferenceChanged(context, commomPreferenceFileName, desCommomPreferenceFile.getName());
                }
                boolean result = editor.commit();
                boolean delete = dirFile.delete();
                if (result && delete) {
                    MLog.i("HwCloudBackUpUtils", "recover success");
                }
                if (desCommomPreferenceFile.delete()) {
                    MLog.i("HwCloudBackUpUtils", "delete desCommomPreferenceFile success");
                }
            }
            return;
        }
        MLog.e("HwCloudBackUpUtils", "copy retore file failed");
    }

    private static void refreshPreferenceChanged(Context context, String commomPreferenceFileName, String commonPreferenceFileBackName) {
        if (commomPreferenceFileName != null && commomPreferenceFileName.endsWith(".xml")) {
            commomPreferenceFileName = commomPreferenceFileName.substring(0, commomPreferenceFileName.indexOf(".xml"));
        }
        if (commonPreferenceFileBackName.endsWith(".xml")) {
            commonPreferenceFileBackName = commonPreferenceFileBackName.substring(0, commonPreferenceFileBackName.indexOf(".xml"));
        }
        SharedPreferences sharedPreferencesOld = context.getSharedPreferences(commomPreferenceFileName, 0);
        SharedPreferences sharedPreferencesNew = context.getSharedPreferences(commonPreferenceFileBackName, 0);
        int lineNumber = sharedPreferencesNew.getInt("LINE_NUMBERS", 0);
        CharSequence value = null;
        for (Entry<String, ?> entry : sharedPreferencesNew.getAll().entrySet()) {
            String key = (String) entry.getKey();
            if ((entry.getValue() instanceof String) && !TextUtils.equals(r7, "NO_VALUE")) {
                value = (String) entry.getValue();
                sharedPreferencesOld.edit().putString(key, value).apply();
            }
        }
        sharedPreferencesOld.edit().putString("FIRST_USE", "NO").apply();
        sharedPreferencesOld.edit().putInt("LINE_NUMBERS", lineNumber).apply();
    }

    private static void writeToPreferenceFromPreferenceRestore(String preferenceKey, String value, PreferenceType type, Editor editor) {
        switch (-getcom-huawei-mms-util-HwCloudBackUpUtils$PreferenceTypeSwitchesValues()[type.ordinal()]) {
            case 1:
                editor.putBoolean(preferenceKey, Boolean.parseBoolean(value));
                return;
            case 2:
                editor.putFloat(preferenceKey, Float.parseFloat(value));
                return;
            case 3:
                editor.putInt(preferenceKey, Integer.parseInt(value));
                return;
            case 4:
                editor.putLong(preferenceKey, Long.parseLong(value));
                return;
            case 5:
                editor.putString(preferenceKey, value);
                return;
            default:
                return;
        }
    }

    private static File getCommonRestoreSrcFile(Context context, File restoreFileDir, String commomPreferenceFileName) {
        int i = 0;
        if (!restoreFileDir.isDirectory()) {
            return new File(getMmsPreferenceRestoreUnzipDir(context), commomPreferenceFileName + ".xml");
        }
        if (commomPreferenceFileName != null && commomPreferenceFileName.contains("_#")) {
            commomPreferenceFileName = commomPreferenceFileName.substring(0, commomPreferenceFileName.indexOf("_#"));
        }
        String[] list = restoreFileDir.list();
        int length = list.length;
        while (i < length) {
            String file = list[i];
            if (commomPreferenceFileName != null && file.startsWith(commomPreferenceFileName) && file.endsWith(".xml")) {
                return new File(restoreFileDir, file);
            }
            i++;
        }
        return new File(getMmsPreferenceRestoreUnzipDir(context), commomPreferenceFileName + ".xml");
    }
}

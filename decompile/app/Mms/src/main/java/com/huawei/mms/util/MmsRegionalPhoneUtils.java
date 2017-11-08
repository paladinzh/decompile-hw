package com.huawei.mms.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import com.android.mms.MmsConfig;
import com.android.mms.ui.AdvancedPreferenceFragment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

public class MmsRegionalPhoneUtils {
    private static final HashMap<String, String> mAllBackupMap = new HashMap();

    public static boolean isReginalPhoneActivated(Context context) {
        if (!SystemProperties.getBoolean("ro.config.region_phone_feature", false)) {
            return false;
        }
        Log.d("MmsRegionalPhoneUtils", "regional phone prop open");
        if (!isVendorCountryChanged(context)) {
            return false;
        }
        Log.d("MmsRegionalPhoneUtils", "vendor country has been changed, activate regional phone mms settings");
        return true;
    }

    public static boolean isVendorCountryChanged(Context context) {
        String vendorCountryNameBackup = System.getString(context.getContentResolver(), "vendor_country_name_backup");
        String currentVendorcountry = SystemProperties.get("ro.hw.custPath", "");
        if (TextUtils.isEmpty(vendorCountryNameBackup)) {
            System.putString(context.getContentResolver(), "vendor_country_name_backup", currentVendorcountry);
            Log.d("MmsRegionalPhoneUtils", "vendorCountryNameBackup is null, backup the name, not activate regional phone");
            return false;
        } else if (TextUtils.isEmpty(currentVendorcountry) || currentVendorcountry.equalsIgnoreCase(vendorCountryNameBackup)) {
            return false;
        } else {
            System.putString(context.getContentResolver(), "vendor_country_name_backup", currentVendorcountry);
            Log.d("MmsRegionalPhoneUtils", "vendor country changed, save new currentVendorcountry = " + currentVendorcountry);
            return true;
        }
    }

    public static void processRegionalPhoneXmls(Context context) {
        Log.d("MmsRegionalPhoneUtils", "processRegionalPhoneXmls");
        readBackupXml(context);
        writeNewSettingsToSp(context);
    }

    public static void readBackupXml(Context context) {
        Log.v("MmsRegionalPhoneUtils", "readBackupXml");
        try {
            FileInputStream fis = new FileInputStream(new File(context.getFilesDir().getAbsolutePath(), "mms_config-backup.xml"));
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fis, "utf-8");
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                String tagName = parser.getName();
                switch (eventType) {
                    case 2:
                        if (!"int".equals(tagName)) {
                            if (!"bool".equals(tagName)) {
                                if (!"string".equals(tagName)) {
                                    break;
                                }
                                mAllBackupMap.put(parser.getAttributeName(0), parser.getAttributeValue(0));
                                break;
                            }
                            if ("mmsReadReportChecked".equals(parser.getAttributeName(0))) {
                                String attributeValue = parser.getAttributeValue(null, "mmsReadReportChecked");
                            }
                            if ("defaultAutoReplyReadReports".equals(parser.getAttributeName(0))) {
                                String attributeValue2 = parser.getAttributeValue(null, "defaultAutoReplyReadReports");
                            }
                            mAllBackupMap.put(parser.getAttributeName(0), parser.getAttributeValue(0));
                            break;
                        }
                        if ("defaultAlwaysAllowMms".equals(parser.getAttributeName(0))) {
                            String attributeValue3 = parser.getAttributeValue(null, "defaultAlwaysAllowMms");
                        }
                        if ("enableDeliveryReportState".equals(parser.getAttributeName(0))) {
                            String attributeValue4 = parser.getAttributeValue(null, "enableDeliveryReportState");
                        }
                        if ("defaultAutoRetrievalMms".equals(parser.getAttributeName(0))) {
                            String attributeValue5 = parser.getAttributeValue(null, "defaultAutoRetrievalMms");
                        }
                        mAllBackupMap.put(parser.getAttributeName(0), parser.getAttributeValue(0));
                        break;
                    default:
                        break;
                }
            }
        } catch (FileNotFoundException eFile) {
            eFile.printStackTrace();
        } catch (XmlPullParserException eXml) {
            eXml.printStackTrace();
        } catch (IOException eIO) {
            eIO.printStackTrace();
        }
    }

    public static void writeNewSettingsToSp(Context context) {
        Log.d("MmsRegionalPhoneUtils", "writeNewSettingsToSp");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sp.edit();
        if (mAllBackupMap.containsKey("defaultAlwaysAllowMms") && sp.getInt("alwaysAllowMms", MmsConfig.getDefaultAlwaysAllowMms()) == Integer.parseInt((String) mAllBackupMap.get("defaultAlwaysAllowMms"))) {
            editor.putInt("alwaysAllowMms", MmsConfig.getDefaultAlwaysAllowMms()).commit();
            AdvancedPreferenceFragment.setAlwaysReceiveAndSendMmsPrefState(MmsConfig.getDefaultAlwaysAllowMms());
        }
        if (mAllBackupMap.containsKey("enableDeliveryReportState") && sp.getInt("pref_key_delivery_reports", MmsConfig.getDefaultDeliveryReportState()) == Integer.parseInt((String) mAllBackupMap.get("enableDeliveryReportState"))) {
            MmsConfig.initDefaultDeliverReportState(MmsConfig.getDefaultDeliveryReportState());
        }
        if (mAllBackupMap.containsKey("defaultAutoRetrievalMms") && sp.getInt("autoReceiveMms", MmsConfig.getDefaultAutoRetrievalMms()) == Integer.parseInt((String) mAllBackupMap.get("defaultAutoRetrievalMms"))) {
            editor.putInt("autoReceiveMms", MmsConfig.getDefaultAutoRetrievalMms()).commit();
        }
        if (mAllBackupMap.containsKey("mmsReadReportChecked")) {
            if (String.valueOf(sp.getBoolean("pref_key_mms_read_reports", MmsConfig.getDefaultMMSReadReports())).equals((String) mAllBackupMap.get("mmsReadReportChecked"))) {
                editor.putBoolean("pref_key_mms_read_reports", MmsConfig.getDefaultMMSReadReports()).commit();
            }
        }
        if (mAllBackupMap.containsKey("defaultAutoReplyReadReports")) {
            if (String.valueOf(sp.getBoolean("pref_key_mms_auto_reply_read_reports", MmsConfig.getDefaultMMSAutoReplyReadReports())).equals((String) mAllBackupMap.get("mmsReadReportChecked"))) {
                editor.putBoolean("pref_key_mms_auto_reply_read_reports", MmsConfig.getDefaultMMSAutoReplyReadReports()).commit();
            }
        }
        editor.commit();
    }

    public static void backupMmsConfigXml(Context context) {
        if (SystemProperties.getBoolean("ro.config.region_phone_feature", false) && System.getInt(context.getContentResolver(), "first_boot_flag", 0) == 0) {
            Log.d("MmsRegionalPhoneUtils", "first time boot, backup mms config xml");
            writeXml(context);
            System.putInt(context.getContentResolver(), "first_boot_flag", 1);
        }
    }

    public static void writeXml(Context context) {
        FileOutputStream fileOutputStream;
        FileOutputStream out;
        IOException e;
        Throwable th;
        Log.v("MmsRegionalPhoneUtils", "writeXml");
        File mmsConfig = new File(context.getFilesDir().getAbsolutePath(), "mms_config-backup.xml");
        try {
            if (mmsConfig.exists()) {
                Log.v("MmsRegionalPhoneUtils", "mms_config-backup.xml file already exist");
                fileOutputStream = null;
                out = new FileOutputStream(mmsConfig);
                out.write(produceXmlString().getBytes(Charset.defaultCharset()));
                if (out != null) {
                    out.close();
                }
                fileOutputStream = out;
            }
            if (mmsConfig.createNewFile()) {
                Log.v("MmsRegionalPhoneUtils", "create mms_config-backup xml success");
            } else {
                Log.v("MmsRegionalPhoneUtils", "create mms_config-backup xml failed");
            }
            fileOutputStream = null;
            try {
                out = new FileOutputStream(mmsConfig);
                try {
                    out.write(produceXmlString().getBytes(Charset.defaultCharset()));
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException ioException) {
                            Log.e("MmsRegionalPhoneUtils", ioException.getMessage());
                        }
                    }
                    fileOutputStream = out;
                } catch (IOException e2) {
                    e = e2;
                    fileOutputStream = out;
                    try {
                        e.printStackTrace();
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException ioException2) {
                                Log.e("MmsRegionalPhoneUtils", ioException2.getMessage());
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException ioException22) {
                                Log.e("MmsRegionalPhoneUtils", ioException22.getMessage());
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fileOutputStream = out;
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    throw th;
                }
            } catch (IOException e3) {
                e = e3;
                e.printStackTrace();
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String produceXmlString() {
        StringWriter stringWriter = new StringWriter();
        try {
            XmlSerializer xmlSerializer = XmlPullParserFactory.newInstance().newSerializer();
            xmlSerializer.setOutput(stringWriter);
            xmlSerializer.startDocument("utf-8", Boolean.valueOf(true));
            xmlSerializer.startTag(null, "config");
            xmlSerializer.startTag(null, "int");
            xmlSerializer.attribute(null, "defaultAlwaysAllowMms", String.valueOf(MmsConfig.getDefaultAlwaysAllowMms()));
            xmlSerializer.endTag(null, "int");
            xmlSerializer.startTag(null, "int");
            xmlSerializer.attribute(null, "enableDeliveryReportState", String.valueOf(MmsConfig.getDefaultDeliveryReportState()));
            xmlSerializer.endTag(null, "int");
            xmlSerializer.startTag(null, "int");
            xmlSerializer.attribute(null, "defaultAutoRetrievalMms", String.valueOf(MmsConfig.getDefaultAutoRetrievalMms()));
            xmlSerializer.endTag(null, "int");
            xmlSerializer.startTag(null, "bool");
            xmlSerializer.attribute(null, "mmsReadReportChecked", String.valueOf(MmsConfig.getMmsBoolConfig("mmsReadReportChecked", false)));
            xmlSerializer.endTag(null, "bool");
            xmlSerializer.startTag(null, "bool");
            xmlSerializer.attribute(null, "defaultAutoReplyReadReports", String.valueOf(MmsConfig.getMmsBoolConfig("defaultAutoReplyReadReports", false)));
            xmlSerializer.endTag(null, "bool");
            xmlSerializer.endTag(null, "config");
            xmlSerializer.endDocument();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }
}

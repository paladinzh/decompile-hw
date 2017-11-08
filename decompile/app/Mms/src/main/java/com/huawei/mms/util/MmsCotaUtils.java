package com.huawei.mms.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.XmlResourceParser;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Xml;
import com.android.mms.MmsConfig;
import com.android.mms.ui.AdvancedPreferenceFragment;
import com.android.mms.ui.MessageUtils;
import com.google.android.gms.location.places.Place;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class MmsCotaUtils {
    private static Map mMmsConfigCotaBtlMap = new HashMap();

    public static void processCotaAtlXml(Context context) {
        Log.d("MmsCotaUtils", "come to processCotaAtlXml");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sp.edit();
        if (System.getInt(context.getContentResolver(), "always_send_recv_mms_click", 0) == 0) {
            AdvancedPreferenceFragment.setAlwaysReceiveAndSendMmsPrefState(MmsConfig.getDefaultAlwaysAllowMms());
        }
        String custRingStr = SystemProperties.get("ro.config.messagesound", "");
        if (!TextUtils.isEmpty(custRingStr) && !sp.getString("key_cust_message_ring", "no_cust_message_ring").equals(custRingStr)) {
            editor.putString("key_cust_message_ring", custRingStr);
            setFollowNotification(sp, editor);
            editor.commit();
        }
    }

    public static void processCotaBtlXml(Context context) {
        Log.d("MmsCotaUtils", "come to processCotaAtlXml");
        String generalNewDigest = getGeneralNewDigest("data/cota/btl/current/xml/mms_config.xml");
        if (generalNewDigest == null) {
            Log.d("MmsCotaUtils", "no btl mms_config.xml exist");
            return;
        }
        boolean mIsCotaFileChanged = false;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sp.edit();
        if (!generalNewDigest.equals(sp.getString("general_defaults_digest", "still_no_digest"))) {
            editor.putString("general_defaults_digest", generalNewDigest).apply();
            mIsCotaFileChanged = true;
        }
        Log.d("MmsCotaUtils", "mms_config_cota.xml exist, load it");
        MmsConfig.readMmsConfigXML(context, "data/cota/btl/current/xml/mms_config.xml");
        if (mIsCotaFileChanged) {
            saveItemsInCota(context, "data/cota/btl/current/xml/mms_config.xml");
            setCustCotaConfigs(context);
        }
    }

    public static void saveItemsInCota(Context context, String path) {
        XmlPullParserException e;
        Throwable th;
        XmlPullParser xmlPullParser = null;
        InputStream inputStream = null;
        try {
            InputStream in = new FileInputStream(path);
            try {
                xmlPullParser = Xml.newPullParser();
                xmlPullParser.setInput(in, null);
                loadMmsCotaSettings(context, xmlPullParser);
                Log.d("MmsCotaUtils", path);
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e2) {
                        inputStream = in;
                        return;
                    } catch (XmlPullParserException e3) {
                        inputStream = in;
                        Log.e("MmsCotaUtils", "load " + path + " MmsSettings XmlPullParserException ");
                    }
                }
                inputStream = in;
                if (xmlPullParser != null) {
                    try {
                        if (!XmlResourceParser.class.isInstance(xmlPullParser)) {
                            xmlPullParser.setInput(null);
                        }
                    } catch (IOException e4) {
                    } catch (XmlPullParserException e5) {
                        Log.e("MmsCotaUtils", "load " + path + " MmsSettings XmlPullParserException ");
                    }
                }
            } catch (FileNotFoundException e6) {
                inputStream = in;
                Log.e("MmsCotaUtils", "load " + path + " MmsSettings caught FileNotFoundException");
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e7) {
                        return;
                    } catch (XmlPullParserException e8) {
                        Log.e("MmsCotaUtils", "load " + path + " MmsSettings XmlPullParserException ");
                        return;
                    }
                }
                if (xmlPullParser != null) {
                }
            } catch (XmlPullParserException e9) {
                e = e9;
                inputStream = in;
                try {
                    Log.e("MmsCotaUtils", "load " + path + " MmsSettings caught XmlPullParserException");
                    Log.e("MmsCotaUtils", "load " + path + " MmsSettings caught ", e);
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e10) {
                            return;
                        } catch (XmlPullParserException e11) {
                            Log.e("MmsCotaUtils", "load " + path + " MmsSettings XmlPullParserException ");
                            return;
                        }
                    }
                    if (xmlPullParser != null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e12) {
                        } catch (XmlPullParserException e13) {
                            Log.e("MmsCotaUtils", "load " + path + " MmsSettings XmlPullParserException ");
                        }
                    }
                    if (!(xmlPullParser == null || XmlResourceParser.class.isInstance(xmlPullParser))) {
                        xmlPullParser.setInput(null);
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                inputStream = in;
                if (inputStream != null) {
                    inputStream.close();
                }
                xmlPullParser.setInput(null);
                throw th;
            }
        } catch (FileNotFoundException e14) {
            Log.e("MmsCotaUtils", "load " + path + " MmsSettings caught FileNotFoundException");
            if (inputStream != null) {
                inputStream.close();
            }
            if (xmlPullParser != null && !XmlResourceParser.class.isInstance(xmlPullParser)) {
                xmlPullParser.setInput(null);
            }
        } catch (XmlPullParserException e15) {
            e = e15;
            Log.e("MmsCotaUtils", "load " + path + " MmsSettings caught XmlPullParserException");
            Log.e("MmsCotaUtils", "load " + path + " MmsSettings caught ", e);
            if (inputStream != null) {
                inputStream.close();
            }
            if (xmlPullParser != null && !XmlResourceParser.class.isInstance(xmlPullParser)) {
                xmlPullParser.setInput(null);
            }
        }
    }

    private static void loadMmsCotaSettings(Context context, XmlPullParser parser) {
        try {
            MmsConfig.beginDocument(parser, "mms_config");
            while (true) {
                MmsConfig.nextElement(parser);
                String tag = parser.getName();
                if (tag == null) {
                    break;
                }
                String name = parser.getAttributeName(0);
                String value = parser.getAttributeValue(0);
                String text = null;
                if (parser.next() == 4) {
                    text = parser.getText();
                }
                Log.d("MmsCotaUtils", "tag: " + tag + " value: " + value + " - " + text);
                if (MmsConfig.isStringEqual("name", name)) {
                    if (MmsConfig.isStringEqual("bool", tag)) {
                        mMmsConfigCotaBtlMap.put(value, Boolean.valueOf("true".equalsIgnoreCase(text)));
                    } else if ("int".equals(tag)) {
                        mMmsConfigCotaBtlMap.put(value, Integer.valueOf(Integer.parseInt(text)));
                    } else if ("string".equals(tag)) {
                        mMmsConfigCotaBtlMap.put(value, text);
                    }
                }
            }
            if (parser instanceof XmlResourceParser) {
                ((XmlResourceParser) parser).close();
            }
            if (parser instanceof KXmlParser) {
                try {
                    ((KXmlParser) parser).close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (XmlPullParserException e2) {
            Log.e("MmsCotaUtils", "loadMmsSettings caught ", e2);
            if (parser instanceof XmlResourceParser) {
                ((XmlResourceParser) parser).close();
            }
            if (parser instanceof KXmlParser) {
                try {
                    ((KXmlParser) parser).close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
        } catch (NumberFormatException e4) {
            Log.e("MmsCotaUtils", "loadMmsSettings caught ", e4);
            if (parser instanceof XmlResourceParser) {
                ((XmlResourceParser) parser).close();
            }
            if (parser instanceof KXmlParser) {
                try {
                    ((KXmlParser) parser).close();
                } catch (IOException e32) {
                    e32.printStackTrace();
                }
            }
        } catch (IOException e322) {
            Log.e("MmsCotaUtils", "loadMmsSettings caught ", e322);
            if (parser instanceof XmlResourceParser) {
                ((XmlResourceParser) parser).close();
            }
            if (parser instanceof KXmlParser) {
                try {
                    ((KXmlParser) parser).close();
                } catch (IOException e3222) {
                    e3222.printStackTrace();
                }
            }
        } catch (Throwable th) {
            Throwable th2 = th;
            if (parser instanceof XmlResourceParser) {
                ((XmlResourceParser) parser).close();
            }
            if (parser instanceof KXmlParser) {
                try {
                    ((KXmlParser) parser).close();
                } catch (IOException e32222) {
                    e32222.printStackTrace();
                }
            }
        }
    }

    public static void setCustCotaConfigs(Context context) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        if (mMmsConfigCotaBtlMap.containsKey("defaultAutoRetrievalMms")) {
            MmsConfig.setAutoReceivePrefState();
        }
        if (mMmsConfigCotaBtlMap.containsKey("enableDeliveryReportState")) {
            setMMSDeliveryReportsState(editor);
        }
        if (mMmsConfigCotaBtlMap.containsKey("prefPlaymode")) {
            editor.putString("pref_key_play_mode", MmsConfig.getPrefPlaymode());
        }
        editor.apply();
    }

    public static String getGeneralNewDigest(String path) {
        try {
            return Base64.encodeToString(computeSha1Digest(path), 4).trim();
        } catch (Exception e) {
            Log.e("MmsCotaUtils", "Got execption convert disgest to string.", e);
            return null;
        }
    }

    public static byte[] computeSha1Digest(String path) {
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        NoSuchAlgorithmException e3;
        FileInputStream fileInputStream = null;
        try {
            FileInputStream fis = new FileInputStream(path);
            try {
                MessageDigest sha1 = MessageDigest.getInstance("SHA1");
                byte[] data = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
                while (true) {
                    int read = fis.read(data);
                    if (read == -1) {
                        break;
                    }
                    sha1.update(data, 0, read);
                }
                byte[] digest = sha1.digest();
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e4) {
                        Log.e("MmsCotaUtils", "Got execption close fileinputstream.", e4);
                    }
                }
                return digest;
            } catch (FileNotFoundException e5) {
                e2 = e5;
                fileInputStream = fis;
                try {
                    Log.e("MmsCotaUtils", "Got execption FileNotFound.", e2);
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e42) {
                            Log.e("MmsCotaUtils", "Got execption close fileinputstream.", e42);
                        }
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e422) {
                            Log.e("MmsCotaUtils", "Got execption close fileinputstream.", e422);
                        }
                    }
                    throw th;
                }
            } catch (NoSuchAlgorithmException e6) {
                e3 = e6;
                fileInputStream = fis;
                Log.e("MmsCotaUtils", "Got execption NoSuchAlgorithm.", e3);
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e4222) {
                        Log.e("MmsCotaUtils", "Got execption close fileinputstream.", e4222);
                    }
                }
                return null;
            } catch (IOException e7) {
                e4222 = e7;
                fileInputStream = fis;
                Log.e("MmsCotaUtils", "Got execption IOException.", e4222);
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e42222) {
                        Log.e("MmsCotaUtils", "Got execption close fileinputstream.", e42222);
                    }
                }
                return null;
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = fis;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e8) {
            e2 = e8;
            Log.e("MmsCotaUtils", "Got execption FileNotFound.", e2);
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return null;
        } catch (NoSuchAlgorithmException e9) {
            e3 = e9;
            Log.e("MmsCotaUtils", "Got execption NoSuchAlgorithm.", e3);
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return null;
        } catch (IOException e10) {
            e42222 = e10;
            Log.e("MmsCotaUtils", "Got execption IOException.", e42222);
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return null;
        }
    }

    private static void setMMSDeliveryReportsState(Editor editor) {
        int deliveryReportState = MmsConfig.getDefaultDeliveryReportState();
        boolean smsReportMode = false;
        boolean mmsReportMode = false;
        if (1 == deliveryReportState) {
            smsReportMode = true;
            mmsReportMode = false;
        } else if (2 == deliveryReportState) {
            smsReportMode = false;
            mmsReportMode = true;
        } else if (3 == deliveryReportState) {
            smsReportMode = true;
            mmsReportMode = true;
        }
        editor.putInt("pref_key_delivery_reports", deliveryReportState);
        editor.putBoolean("pref_key_mms_delivery_reports", mmsReportMode);
        editor.putBoolean("pref_key_sms_delivery_reports", smsReportMode);
        editor.putBoolean("pref_key_sms_delivery_reports_sub0", smsReportMode);
        editor.putBoolean("pref_key_sms_delivery_reports_sub1", smsReportMode);
    }

    public static void setFollowNotification(SharedPreferences sp, Editor editor) {
        if (MessageUtils.isMultiSimEnabled()) {
            editor.putBoolean("pref_mms_is_follow_notification_sub0", false);
            if (!sp.contains("pref_mms_is_follow_notification_sub1")) {
                editor.putBoolean("pref_mms_is_follow_notification_sub1", true);
            }
        } else if (!sp.getBoolean("key_user_has_selected_ring", false)) {
            editor.putBoolean("pref_mms_is_follow_notification", false);
        }
    }
}

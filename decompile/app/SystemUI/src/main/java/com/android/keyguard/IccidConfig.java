package com.android.keyguard;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.XmlResourceParser;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import com.android.keyguard.KeyguardSecurityModel.SecurityMode;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.cust.HwCfgFilePolicy;
import fyusion.vislib.BuildConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class IccidConfig {
    private static final String DATA_CUST_FILE = ("/data/cust/" + PATH_CFG);
    private static final String GENERAL_CFG_FILE = ("/system/etc/" + PATH_CFG);
    private static String PATH_CFG = "xml/iccid_config.xml";
    private static boolean hasInit = false;
    private static Context mContext;
    private static Map<String, Map<String, Object>> mCustIccidMap = new HashMap();
    private static String mIccid;
    private static SecurityMode mSecurityMode;

    public static synchronized void init() {
        synchronized (IccidConfig.class) {
            if (!hasInit) {
                loadConfig();
                hasInit = true;
            }
        }
    }

    public static void loadConfig() {
        ArrayList<File> iccidFileList = null;
        boolean isHuaweiConfigPolicySupported = false;
        try {
            iccidFileList = HwCfgFilePolicy.getCfgFileList(PATH_CFG, 0);
            isHuaweiConfigPolicySupported = true;
        } catch (NoExtAPIException ex) {
            Log.e("IccidConfig", ex.getMessage());
        } catch (NoClassDefFoundError er) {
            Log.e("IccidConfig", er.getMessage());
        }
        if (!isHuaweiConfigPolicySupported || iccidFileList == null) {
            parseConfigXml(new File(GENERAL_CFG_FILE));
            parseConfigXml(new File(DATA_CUST_FILE));
            return;
        }
        for (File file : iccidFileList) {
            parseConfigXml(file);
        }
    }

    public static void parseConfigXml(File iccidfile) {
        XmlPullParserException e;
        Throwable th;
        XmlPullParser xmlPullParser = null;
        InputStream inputStream = null;
        try {
            InputStream in = new FileInputStream(iccidfile);
            try {
                xmlPullParser = Xml.newPullParser();
                xmlPullParser.setInput(in, null);
                loadIccidSettings(xmlPullParser);
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e2) {
                        inputStream = in;
                        Log.e("IccidConfig", "IOException ");
                    } catch (XmlPullParserException e3) {
                        inputStream = in;
                        Log.e("IccidConfig", "XmlPullParserException ");
                    }
                }
                inputStream = in;
                if (xmlPullParser != null) {
                    try {
                        if (!XmlResourceParser.class.isInstance(xmlPullParser)) {
                            xmlPullParser.setInput(null);
                        }
                    } catch (IOException e4) {
                        Log.e("IccidConfig", "IOException ");
                    } catch (XmlPullParserException e5) {
                        Log.e("IccidConfig", "XmlPullParserException ");
                    }
                }
            } catch (FileNotFoundException e6) {
                inputStream = in;
                Log.e("IccidConfig", "parseConfigXml caught FileNotFoundException");
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e7) {
                        Log.e("IccidConfig", "IOException ");
                        return;
                    } catch (XmlPullParserException e8) {
                        Log.e("IccidConfig", "XmlPullParserException ");
                        return;
                    }
                }
                if (xmlPullParser != null && !XmlResourceParser.class.isInstance(xmlPullParser)) {
                    xmlPullParser.setInput(null);
                }
            } catch (XmlPullParserException e9) {
                e = e9;
                inputStream = in;
                Log.e("IccidConfig", "parseConfigXml caught XmlPullParserException");
                Log.e("IccidConfig", "parseConfigXml caught ", e);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e10) {
                        Log.e("IccidConfig", "IOException ");
                        return;
                    } catch (XmlPullParserException e11) {
                        Log.e("IccidConfig", "XmlPullParserException ");
                        return;
                    }
                }
                if (xmlPullParser != null && !XmlResourceParser.class.isInstance(xmlPullParser)) {
                    xmlPullParser.setInput(null);
                }
            } catch (Exception e12) {
                inputStream = in;
                try {
                    Log.e("IccidConfig", "parseConfigXml caught Exception");
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e13) {
                            Log.e("IccidConfig", "IOException ");
                            return;
                        } catch (XmlPullParserException e14) {
                            Log.e("IccidConfig", "XmlPullParserException ");
                            return;
                        }
                    }
                    if (xmlPullParser != null && !XmlResourceParser.class.isInstance(xmlPullParser)) {
                        xmlPullParser.setInput(null);
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e15) {
                            Log.e("IccidConfig", "IOException ");
                        } catch (XmlPullParserException e16) {
                            Log.e("IccidConfig", "XmlPullParserException ");
                        }
                    }
                    xmlPullParser.setInput(null);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                inputStream = in;
                if (inputStream != null) {
                    inputStream.close();
                }
                if (!(xmlPullParser == null || XmlResourceParser.class.isInstance(xmlPullParser))) {
                    xmlPullParser.setInput(null);
                }
                throw th;
            }
        } catch (FileNotFoundException e17) {
            Log.e("IccidConfig", "parseConfigXml caught FileNotFoundException");
            if (inputStream != null) {
                inputStream.close();
            }
            if (xmlPullParser != null) {
            }
        } catch (XmlPullParserException e18) {
            e = e18;
            Log.e("IccidConfig", "parseConfigXml caught XmlPullParserException");
            Log.e("IccidConfig", "parseConfigXml caught ", e);
            if (inputStream != null) {
                inputStream.close();
            }
            if (xmlPullParser != null) {
            }
        } catch (Exception e19) {
            Log.e("IccidConfig", "parseConfigXml caught Exception");
            if (inputStream != null) {
                inputStream.close();
            }
            if (xmlPullParser != null) {
            }
        }
    }

    public static void loadIccidSettings(XmlPullParser xpp) throws Exception {
        Object iccid = null;
        Map itemMap = null;
        int eventType = xpp.next();
        while (eventType != 1) {
            switch (eventType) {
                case 2:
                    String tag = xpp.getName();
                    String name = xpp.getAttributeName(0);
                    String value = xpp.getAttributeValue(0);
                    if (!"simcardtype".equals(tag)) {
                        if ("string".equals(tag) && itemMap != null) {
                            itemMap.put(name, value);
                            break;
                        }
                    }
                    String iccid2 = value;
                    itemMap = new HashMap();
                    break;
                case 3:
                    if (!"simcardtype".equals(xpp.getName())) {
                        break;
                    }
                    mCustIccidMap.put(iccid, itemMap);
                    break;
                default:
                    break;
            }
            eventType = xpp.next();
        }
    }

    public static boolean isCustIccid(Context context) {
        mContext = context;
        if (!SystemProperties.getBoolean("ro.config.iccid_language", false) || hasFirstIccid()) {
            return false;
        }
        return isSimPinLock();
    }

    private static boolean isSimPinLock() {
        mSecurityMode = KeyguardSecurityModel.getInst(mContext).getSecurityMode();
        return SecurityMode.SimPin == mSecurityMode;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void setCustLanguage() {
        synchronized (IccidConfig.class) {
            mIccid = getCurrentIccid();
            Log.d("IccidConfig", "mCustIccidMap.size = " + mCustIccidMap.size());
            if (!TextUtils.isEmpty(mIccid)) {
                String subIccid = BuildConfig.FLAVOR;
                int i = 5;
                while (i >= 4) {
                    subIccid = mIccid.substring(0, i);
                    if (mCustIccidMap.containsKey(subIccid)) {
                        Map<String, Object> iccidMap = (Map) mCustIccidMap.get(subIccid);
                        Locale locale = new Locale((String) iccidMap.get("language"), (String) iccidMap.get("country"));
                        String currentLocalStr = Locale.getDefault().toString();
                        Log.d("IccidConfig", " setCustLanguage before current local = " + currentLocalStr + " setCustLanguage locale = " + locale.toString());
                        if (currentLocalStr.equals(locale.toString())) {
                            return;
                        }
                        try {
                            Class clzIActMag = Class.forName("android.app.IActivityManager");
                            Class clzActMagNative = Class.forName("android.app.ActivityManagerNative");
                            Object objIActMag = clzActMagNative.getDeclaredMethod("getDefault", new Class[0]).invoke(clzActMagNative, new Object[0]);
                            ((Configuration) clzIActMag.getDeclaredMethod("getConfiguration", new Class[0]).invoke(objIActMag, new Object[0])).setLocale(locale);
                            clzIActMag.getDeclaredMethod("updatePersistentConfiguration", new Class[]{Configuration.class}).invoke(objIActMag, new Object[]{config});
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        setFirstIccid(subIccid);
                    } else {
                        i--;
                    }
                }
            }
        }
    }

    private static String getCurrentIccid() {
        String iccid = BuildConfig.FLAVOR;
        if (MSimTelephonyManager.from(mContext).isMultiSimEnabled()) {
            iccid = MSimTelephonyManager.from(mContext).getSimSerialNumber(0);
            if (TextUtils.isEmpty(iccid)) {
                iccid = MSimTelephonyManager.from(mContext).getSimSerialNumber(1);
                Log.d("IccidConfig", "current is MultiSim-sim2");
            }
            Log.d("IccidConfig", "current is MultiSim-sim1");
        } else {
            iccid = TelephonyManager.from(mContext).getSimSerialNumber();
            Log.d("IccidConfig", "current is single sim");
        }
        return TextUtils.isEmpty(iccid) ? BuildConfig.FLAVOR : iccid;
    }

    public static boolean hasFirstIccid() {
        return !TextUtils.isEmpty(PreferenceManager.getDefaultSharedPreferences(mContext).getString("first_iccid_key", BuildConfig.FLAVOR));
    }

    public static void setFirstIccid(String firstIccid) {
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString("first_iccid_key", firstIccid).commit();
    }
}

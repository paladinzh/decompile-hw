package com.android.systemui.apksimcardadapter;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.XmlResourceParser;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import com.android.systemui.utils.FileUtil;
import com.huawei.android.provider.SettingsEx.Systemex;
import fyusion.vislib.BuildConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class DisableCloudAppReceiver extends BroadcastReceiver {
    private static String TAG = "DisableCloudAppReceiver";
    private ContentResolver cr;
    private Context mContext;
    private PackageManager pm;

    static class LogTools {
        LogTools() {
        }

        static void logD(String tag, String msg) {
        }
    }

    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            this.mContext = context;
            this.pm = this.mContext.getPackageManager();
            this.cr = this.mContext.getContentResolver();
            String filePath = FileUtil.getPresetPath("cloudapklist.xml");
            File file = filePath != null ? new File(filePath) : null;
            String action = intent.getAction();
            String lastDealedNumber = getString(this.cr, "last_dealed_icc_number", "-2");
            LogTools.logD(TAG, "lastDealedNumber is " + lastDealedNumber);
            if ("android.intent.action.SIM_STATE_CHANGED".equals(action) && "LOADED".equals(intent.getStringExtra("ss"))) {
                LogTools.logD(TAG, "Recieve the broadcast ACTION_SIM_STATE_CHANGED and sim is loaded");
                if (!processOtaUpdated(lastDealedNumber)) {
                    try {
                        String iccNumber = ((TelephonyManager) context.getSystemService("phone")).getSimOperator();
                        if (iccNumber == null || BuildConfig.FLAVOR.equals(iccNumber)) {
                            Log.e(TAG, "the current iccNumber is null ,an error must occur in the system.");
                            return;
                        }
                        LogTools.logD(TAG, "the current iccNumber we get from system is " + iccNumber);
                        int i = iccNumber.indexOf(",");
                        if (i != -1) {
                            iccNumber = i >= 3 ? iccNumber.substring(0, i) : iccNumber.substring(i + 1);
                        }
                        LogTools.logD(TAG, "current mcc/mnc is " + iccNumber);
                        if ("0".equals(lastDealedNumber) || "-2".equals(lastDealedNumber)) {
                            customizeCloudApk(file, lastDealedNumber, iccNumber);
                            context.getPackageManager().setComponentEnabledSetting(new ComponentName(context.getApplicationContext(), DisableCloudAppReceiver.class), 2, 1);
                        } else {
                            LogTools.logD(TAG, "we have already disabled the app of the MCC number " + lastDealedNumber);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Huawei TelephonyManager is not realized!" + e);
                    }
                }
            } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                LogTools.logD(TAG, "Recieve the broadcast ACTION_BOOT_COMPLETED");
                if (!processOtaUpdated(lastDealedNumber)) {
                    if ("-2".equals(lastDealedNumber)) {
                        customizeCloudApk(file, null, "0");
                    } else {
                        LogTools.logD(TAG, "we have already disabled the app of the MCC number for default");
                    }
                }
            }
        }
    }

    private void customizeCloudApk(File file, String iccNumOld, String iccNumCurrent) {
        if (iccNumCurrent != null && !BuildConfig.FLAVOR.equals(iccNumCurrent)) {
            if (file == null || !file.exists()) {
                Log.e(TAG, "The file cloudapklist.xml not exits");
            } else {
                List<String> currentPkgName = getDelPkgName(file, iccNumCurrent);
                List<String> oldPkgName = getDelPkgName(file, iccNumOld);
                if (oldPkgName != null) {
                    for (String allPkg : oldPkgName) {
                        if (currentPkgName == null || !currentPkgName.contains(allPkg)) {
                            setApplicationEnabledSetting(allPkg, 1);
                        } else {
                            currentPkgName.remove(allPkg);
                        }
                    }
                }
                if (currentPkgName != null) {
                    for (String strPkg : currentPkgName) {
                        setApplicationEnabledSetting(strPkg, 2);
                    }
                }
            }
            if (this.mContext != null) {
                Intent i = new Intent("com.android.launcher.action.MATCH_CARD");
                int mcc = iccNumCurrent.length() >= 5 ? parseIntUtils(iccNumCurrent.substring(0, 3), 0) : 0;
                i.putExtra("mcc", mcc);
                i.putExtra("operator", iccNumCurrent);
                this.mContext.sendBroadcast(i, "com.huawei.android.launcher.permission.SEND_MATCH_CARD");
                LogTools.logD(TAG, "We have already disabled the apps under Iccnum " + mcc + ". and send a broadcast to the related apps like Launcher.");
            }
            LogTools.logD(TAG, "We have already disabled the apps under Iccnum " + iccNumCurrent + ". and put the icc into settings.");
            putString(this.cr, "last_dealed_icc_number", iccNumCurrent);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private List<String> getDelPkgName(File file, String iccNumArg) {
        XmlPullParserException e;
        IOException e2;
        Throwable th;
        List<String> list;
        Object obj;
        if (file == null || iccNumArg == null || BuildConfig.FLAVOR.equals(iccNumArg) || "-2".equals(iccNumArg)) {
            return null;
        }
        String[] target;
        String str;
        StringBuilder append;
        String str2;
        Reader reader = null;
        XmlPullParser xmlPullParser = null;
        String[] strArr = null;
        boolean isNameFound = false;
        String[] strArr2 = null;
        boolean isMccFound = false;
        String[] strArr3 = null;
        String substring = iccNumArg.length() >= 5 ? iccNumArg.substring(0, 3) : null;
        try {
            Reader input = new InputStreamReader(new FileInputStream(file), "UTF-8");
            try {
                xmlPullParser = Xml.newPullParser();
                xmlPullParser.setInput(input);
                XmlUtils.beginDocument(xmlPullParser, "no-permissions");
                while (true) {
                    XmlUtils.nextElement(xmlPullParser);
                    if (xmlPullParser.getEventType() == 1) {
                        break;
                    }
                    if ("operator".equals(xmlPullParser.getName())) {
                        String iccNum = xmlPullParser.getAttributeValue(null, "iccNum");
                        String delPkgName = xmlPullParser.getAttributeValue(null, "delPkgName");
                        if (iccNum == null) {
                            LogTools.logD(TAG, "iccNum is null.");
                        } else if (delPkgName == null) {
                            LogTools.logD(TAG, "delPkgName is null.");
                        } else if (iccNum.equals(iccNumArg)) {
                            break;
                        } else if (substring != null && substring.equals(iccNum)) {
                            LogTools.logD(TAG, "we have found the iccnum only mcc " + iccNum);
                            strArr2 = delPkgName.split(";");
                            isMccFound = true;
                        } else if ("-1".equals(iccNum)) {
                            strArr3 = delPkgName.split(";");
                        }
                    }
                    XmlUtils.skipCurrentTag(xmlPullParser);
                }
                if (input != null) {
                    try {
                        input.close();
                    } catch (Exception e3) {
                        e3.printStackTrace();
                    }
                }
                if (!(xmlPullParser == null || XmlResourceParser.class.isInstance(xmlPullParser))) {
                    try {
                        xmlPullParser.setInput(null);
                    } catch (Exception e32) {
                        e32.printStackTrace();
                    }
                }
            } catch (XmlPullParserException e4) {
                e = e4;
                reader = input;
            } catch (IOException e5) {
                e2 = e5;
                reader = input;
            } catch (Throwable th2) {
                th = th2;
                reader = input;
            }
        } catch (XmlPullParserException e6) {
            e = e6;
            try {
                Log.e(TAG, "Got execption parsing permissions 1:" + e);
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e322) {
                        e322.printStackTrace();
                    }
                }
                if (!(xmlPullParser == null || XmlResourceParser.class.isInstance(xmlPullParser))) {
                    try {
                        xmlPullParser.setInput(null);
                    } catch (Exception e3222) {
                        e3222.printStackTrace();
                    }
                }
                target = isNameFound ? isMccFound ? strArr3 : strArr2 : strArr;
                list = null;
                if (target != null) {
                    list = new ArrayList(Arrays.asList(target));
                }
                str = TAG;
                append = new StringBuilder().append("the num ").append(iccNumArg).append(": ");
                if (target == null) {
                    obj = list;
                } else {
                    str2 = "null.we will not do anything";
                }
                LogTools.logD(str, append.append(str2).toString());
                return list;
            } catch (Throwable th3) {
                th = th3;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e32222) {
                        e32222.printStackTrace();
                    }
                }
                if (!(xmlPullParser == null || XmlResourceParser.class.isInstance(xmlPullParser))) {
                    try {
                        xmlPullParser.setInput(null);
                    } catch (Exception e322222) {
                        e322222.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (IOException e7) {
            e2 = e7;
            Log.e(TAG, "Got execption parsing permissions 2:" + e2);
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e3222222) {
                    e3222222.printStackTrace();
                }
            }
            if (!(xmlPullParser == null || XmlResourceParser.class.isInstance(xmlPullParser))) {
                try {
                    xmlPullParser.setInput(null);
                } catch (Exception e32222222) {
                    e32222222.printStackTrace();
                }
            }
            if (isNameFound) {
            }
            list = null;
            if (target != null) {
                list = new ArrayList(Arrays.asList(target));
            }
            str = TAG;
            append = new StringBuilder().append("the num ").append(iccNumArg).append(": ");
            if (target == null) {
                str2 = "null.we will not do anything";
            } else {
                obj = list;
            }
            LogTools.logD(str, append.append(str2).toString());
            return list;
        }
        if (isNameFound) {
        }
        list = null;
        if (target != null) {
            list = new ArrayList(Arrays.asList(target));
        }
        str = TAG;
        append = new StringBuilder().append("the num ").append(iccNumArg).append(": ");
        if (target == null) {
            str2 = "null.we will not do anything";
        } else {
            obj = list;
        }
        LogTools.logD(str, append.append(str2).toString());
        return list;
    }

    private void setApplicationEnabledSetting(String pkgName, int enabledStatus) {
        try {
            if (isAppExits(pkgName)) {
                this.pm.setApplicationEnabledSetting(pkgName, enabledStatus, 0);
                LogTools.logD(TAG, "the pkg " + pkgName + " enablestatus: " + enabledStatus);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            LogTools.logD(TAG, "Unable to change enabled state of package xxx SecurityException: " + pkgName + e.toString());
        } catch (Exception e2) {
            e2.printStackTrace();
            LogTools.logD(TAG, "Unable to change enabled state of package 1: " + pkgName + e2.toString());
        }
    }

    private boolean isAppExits(String pkgName) {
        boolean z = false;
        if (this.pm == null || pkgName == null || BuildConfig.FLAVOR.equals(pkgName)) {
            return false;
        }
        try {
            if (this.pm.getPackageInfo(pkgName, 0) != null) {
                z = true;
            }
            return z;
        } catch (NameNotFoundException e) {
            LogTools.logD(TAG, "The packageName " + pkgName + " is not exit: \n" + e.toString());
            return false;
        }
    }

    private String getString(ContentResolver r, String colums, String defaultValue) {
        String result = Systemex.getString(r, colums);
        if (TextUtils.isEmpty(result)) {
            return defaultValue;
        }
        return result;
    }

    private void putString(ContentResolver r, String colums, String value) {
        Systemex.putString(r, colums, value);
    }

    private boolean hasOtaUpdated() {
        boolean result = false;
        long begin = System.currentTimeMillis();
        try {
            result = ((Boolean) Class.forName("com.huawei.android.ota.HwOta").getMethod("hasOtaUpdated", new Class[0]).invoke(null, new Object[0])).booleanValue();
        } catch (ClassNotFoundException e) {
            LogTools.logD(TAG, "hasOtaUpdated  " + e.toString());
        } catch (Exception e2) {
            LogTools.logD(TAG, e2.toString());
        }
        LogTools.logD(TAG, "Invoke hasOtaUpdated usage in millis: " + (System.currentTimeMillis() - begin) + " hasOtaUpdated result " + result);
        return result;
    }

    private boolean processOtaUpdated(String iccNumber) {
        if (!"-2".equals(iccNumber)) {
            return false;
        }
        if ("true".equals(getString(this.cr, "SystemUI_boot_after_hota_updated", "false"))) {
            return true;
        }
        if (!hasOtaUpdated()) {
            return false;
        }
        LogTools.logD(TAG, "recieve the broadcast after an ota-updated, we will do nothing for disable-app");
        putString(this.cr, "SystemUI_boot_after_hota_updated", "true");
        this.pm.setComponentEnabledSetting(new ComponentName(this.mContext.getApplicationContext(), DisableCloudAppReceiver.class), 2, 1);
        return true;
    }

    private int parseIntUtils(String target, int defValue) {
        int i = defValue;
        try {
            i = Integer.parseInt(target);
        } catch (NumberFormatException e) {
            String str = TAG;
            StringBuilder append = new StringBuilder().append("Format the mcc number ");
            if (target == null) {
                target = "null";
            }
            LogTools.logD(str, append.append(target).append(" failed!").toString());
        }
        return i;
    }
}

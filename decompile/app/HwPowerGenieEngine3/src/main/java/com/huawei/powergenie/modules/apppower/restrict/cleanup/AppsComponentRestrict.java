package com.huawei.powergenie.modules.apppower.restrict.cleanup;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Xml;
import com.huawei.powergenie.R;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class AppsComponentRestrict {
    private static boolean FEATURE_SWITCHER = true;
    private static String mFrontApp;
    private static HashMap<String, ArrayList<String>> mRestrictCpAppList = new HashMap();
    private static AppsComponentRestrict sComponentRestrict;
    private BackupDataHandler mBackupDataHandler;
    private Context mContext;
    private final PackageManager mPackageManager;
    private HandlerThread mThread = new HandlerThread("AppCpCheckTheard", 10);

    private final class BackupDataHandler extends Handler {
        public BackupDataHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1001:
                    AppsComponentRestrict.this.checkConfigChange();
                    if (AppsComponentRestrict.this.mThread != null) {
                        AppsComponentRestrict.this.mThread.getLooper().quit();
                        Log.i("AppsComponentRestrict", "AppCpCheckTheard quit...");
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public static AppsComponentRestrict getInstance(ICoreContext coreContext) {
        if (sComponentRestrict == null) {
            sComponentRestrict = new AppsComponentRestrict(coreContext);
        }
        return sComponentRestrict;
    }

    public AppsComponentRestrict(ICoreContext coreContext) {
        this.mContext = coreContext.getContext();
        this.mPackageManager = this.mContext.getPackageManager();
        FEATURE_SWITCHER = initCustPolicy();
        startBackupDataTheard();
    }

    protected void restrictAppComponent(ArrayList<String> cleanApplist) {
        if (FEATURE_SWITCHER && cleanApplist != null && cleanApplist.size() > 0) {
            for (String name : cleanApplist) {
                if (mRestrictCpAppList.containsKey(name)) {
                    Log.i("AppsComponentRestrict", "restrictCp " + name);
                    restrictComponent(name, true);
                }
            }
        }
    }

    protected void restoreAppComponent(String pkgName) {
        if (pkgName != null && !pkgName.equals(mFrontApp)) {
            mFrontApp = pkgName;
            if (mRestrictCpAppList.containsKey(pkgName)) {
                Log.i("AppsComponentRestrict", "restoreCp " + pkgName);
                restrictComponent(pkgName, false);
            }
        }
    }

    private void restrictComponent(String pkgName, boolean isRestrict) {
        ArrayList<String> custComponent = (ArrayList) mRestrictCpAppList.get(pkgName);
        if (custComponent == null || custComponent.size() <= 0) {
            Log.i("AppsComponentRestrict", "no cp config : " + pkgName);
            return;
        }
        for (String cp : custComponent) {
            disableAppComponent(new ComponentName(pkgName, cp), isRestrict);
        }
    }

    private void disableAppComponent(ComponentName cp, boolean isRestrict) {
        try {
            int state = this.mPackageManager.getComponentEnabledSetting(cp);
            if (isRestrict) {
                if (state != 2) {
                    this.mPackageManager.setComponentEnabledSetting(cp, 2, 1);
                    Log.i("AppsComponentRestrict", cp + " was disabled !");
                }
            } else if (state == 2) {
                this.mPackageManager.setComponentEnabledSetting(cp, 0, 1);
                Log.i("AppsComponentRestrict", cp + " was enabled !");
            }
        } catch (Exception e) {
            Log.w("AppsComponentRestrict", "Fail to process " + cp + ", isRestrict = " + isRestrict);
        }
    }

    private boolean initCustPolicy() {
        FileNotFoundException e;
        Throwable th;
        XmlPullParserException e2;
        IOException e3;
        Exception e4;
        boolean ret = false;
        XmlPullParser xmlPullParser = null;
        InputStream inputStream = null;
        String custPath = "/product/etc/hwpg/";
        try {
            File file = new File(this.mContext.getDir("cloud_config", 0), "app_cp_restrict.xml");
            if (!file.exists()) {
                Log.i("AppsComponentRestrict", "no cloud cust app list: app_cp_restrict.xml");
                file = new File(custPath, "app_cp_restrict.xml");
            }
            if (file.exists()) {
                InputStream in = new FileInputStream(file);
                try {
                    xmlPullParser = Xml.newPullParser();
                    if (xmlPullParser == null) {
                        Log.w("AppsComponentRestrict", "Parser is null...");
                        if (in != null) {
                            try {
                                in.close();
                            } catch (Exception e5) {
                                Log.w("AppsComponentRestrict", "Close Input stream error!");
                            }
                        }
                        if (xmlPullParser != null && (xmlPullParser instanceof XmlResourceParser)) {
                            ((XmlResourceParser) xmlPullParser).close();
                        }
                        return false;
                    }
                    xmlPullParser.setInput(in, null);
                    inputStream = in;
                } catch (FileNotFoundException e6) {
                    e = e6;
                    inputStream = in;
                    try {
                        e.printStackTrace();
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Exception e7) {
                                Log.w("AppsComponentRestrict", "Close Input stream error!");
                            }
                        }
                        if (xmlPullParser != null && (xmlPullParser instanceof XmlResourceParser)) {
                            ((XmlResourceParser) xmlPullParser).close();
                        }
                        return ret;
                    } catch (Throwable th2) {
                        th = th2;
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Exception e8) {
                                Log.w("AppsComponentRestrict", "Close Input stream error!");
                                throw th;
                            }
                        }
                        if (xmlPullParser != null && (xmlPullParser instanceof XmlResourceParser)) {
                            ((XmlResourceParser) xmlPullParser).close();
                        }
                        throw th;
                    }
                } catch (XmlPullParserException e9) {
                    e2 = e9;
                    inputStream = in;
                    e2.printStackTrace();
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Exception e10) {
                            Log.w("AppsComponentRestrict", "Close Input stream error!");
                        }
                    }
                    if (xmlPullParser != null && (xmlPullParser instanceof XmlResourceParser)) {
                        ((XmlResourceParser) xmlPullParser).close();
                    }
                    return ret;
                } catch (IOException e11) {
                    e3 = e11;
                    inputStream = in;
                    e3.printStackTrace();
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Exception e12) {
                            Log.w("AppsComponentRestrict", "Close Input stream error!");
                        }
                    }
                    if (xmlPullParser != null && (xmlPullParser instanceof XmlResourceParser)) {
                        ((XmlResourceParser) xmlPullParser).close();
                    }
                    return ret;
                } catch (Exception e13) {
                    e4 = e13;
                    inputStream = in;
                    e4.printStackTrace();
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Exception e14) {
                            Log.w("AppsComponentRestrict", "Close Input stream error!");
                        }
                    }
                    if (xmlPullParser != null && (xmlPullParser instanceof XmlResourceParser)) {
                        ((XmlResourceParser) xmlPullParser).close();
                    }
                    return ret;
                } catch (Throwable th3) {
                    th = th3;
                    inputStream = in;
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    ((XmlResourceParser) xmlPullParser).close();
                    throw th;
                }
            }
            Log.i("AppsComponentRestrict", "no cust app list: app_cp_restrict.xml");
            xmlPullParser = this.mContext.getResources().getXml(R.xml.app_cp_restrict);
            if (xmlPullParser == null) {
                Log.w("AppsComponentRestrict", "Parser is null...");
                if (xmlPullParser != null) {
                    try {
                        if (xmlPullParser instanceof XmlResourceParser) {
                            ((XmlResourceParser) xmlPullParser).close();
                        }
                    } catch (Exception e15) {
                        Log.w("AppsComponentRestrict", "Close Input stream error!");
                    }
                }
                return false;
            }
            mRestrictCpAppList.clear();
            Object pkgName = null;
            ArrayList list = null;
            for (int eventType = xmlPullParser.getEventType(); eventType != 1; eventType = xmlPullParser.next()) {
                switch (eventType) {
                    case NativeAdapter.PLATFORM_HI /*2*/:
                        String tag = xmlPullParser.getName();
                        if (!"array".equals(tag)) {
                            if ("item".equals(tag) && list != null) {
                                list.add(xmlPullParser.nextText());
                                break;
                            }
                        }
                        list = new ArrayList();
                        pkgName = xmlPullParser.getAttributeValue(0);
                        break;
                    case NativeAdapter.PLATFORM_K3V3 /*3*/:
                        if (pkgName != null) {
                            if (list.size() <= 0) {
                                break;
                            }
                            mRestrictCpAppList.put(pkgName, list);
                            break;
                        }
                        continue;
                    default:
                        break;
                }
            }
            Log.i("AppsComponentRestrict", "mRestrictCpAppList = " + mRestrictCpAppList);
            if (mRestrictCpAppList.size() > 0) {
                ret = true;
            } else {
                ret = false;
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e16) {
                    Log.w("AppsComponentRestrict", "Close Input stream error!");
                }
            }
            if (xmlPullParser != null && (xmlPullParser instanceof XmlResourceParser)) {
                ((XmlResourceParser) xmlPullParser).close();
            }
        } catch (FileNotFoundException e17) {
            e = e17;
        } catch (XmlPullParserException e18) {
            e2 = e18;
        } catch (IOException e19) {
            e3 = e19;
        } catch (Exception e20) {
            e4 = e20;
        }
        return ret;
    }

    private void startBackupDataTheard() {
        this.mThread.start();
        this.mBackupDataHandler = new BackupDataHandler(this.mThread.getLooper());
        this.mBackupDataHandler.removeMessages(1001);
        this.mBackupDataHandler.sendMessageDelayed(this.mBackupDataHandler.obtainMessage(1001), 10000);
    }

    private void checkConfigChange() {
        if (isChanged()) {
            HashMap<String, ArrayList<String>> oldCustList = readCustList();
            if (oldCustList.size() > 0) {
                ArrayList<String> cpList = new ArrayList();
                for (Entry entry : oldCustList.entrySet()) {
                    String pkg = (String) entry.getKey();
                    for (String cp : (ArrayList) entry.getValue()) {
                        disableAppComponent(new ComponentName(pkg, cp), false);
                    }
                }
            } else {
                Log.i("AppsComponentRestrict", "maybe no cust file...");
            }
            removeSettingsKey("app_cp_record");
            backupCustList();
            return;
        }
        Log.i("AppsComponentRestrict", "there is nothing to change...");
    }

    private boolean isChanged() {
        try {
            long oldConfigTime;
            File cloudConfigFile = new File(this.mContext.getDir("cloud_config", 0), "app_cp_restrict.xml");
            if (cloudConfigFile.exists()) {
                oldConfigTime = getLongSettings("cloud_file_time", 0);
                if (0 == oldConfigTime || oldConfigTime != cloudConfigFile.lastModified()) {
                    updateLongSettings("cloud_file_time", cloudConfigFile.lastModified());
                    Log.i("AppsComponentRestrict", "cloud cust file change...");
                    return true;
                }
            }
            String verName = this.mContext.getPackageManager().getPackageInfo("com.huawei.powergenie", 0).versionName;
            String oldVersion = getSettings("PGVersion");
            if (oldVersion == null || !oldVersion.equals(verName)) {
                updateSettings("PGVersion", verName);
                Log.i("AppsComponentRestrict", "PG version change...");
                return true;
            }
            File configFile = new File("/product/etc/hwpg/app_cp_restrict.xml");
            if (configFile.exists()) {
                oldConfigTime = getLongSettings("file_time", 0);
                if (0 == oldConfigTime || oldConfigTime != configFile.lastModified()) {
                    updateLongSettings("file_time", configFile.lastModified());
                    Log.i("AppsComponentRestrict", "cust file change...");
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Log.e("AppsComponentRestrict", "isNeedRestoreAll error...");
        }
    }

    private void backupCustList() {
        Log.i("AppsComponentRestrict", "backup cust app cp list...");
        HashMap<String, ArrayList<String>> list = (HashMap) mRestrictCpAppList.clone();
        if (list == null || list.size() == 0) {
            Log.i("AppsComponentRestrict", "There is no config need to backup...");
            return;
        }
        Editor editor = this.mContext.getSharedPreferences("app_cp_record", 0).edit();
        int listCount = list.size();
        int count = 0;
        ArrayList<String> cpList = new ArrayList();
        StringBuilder sb = new StringBuilder();
        for (Entry entry : list.entrySet()) {
            cpList = (ArrayList) entry.getValue();
            sb.append((String) entry.getKey());
            sb.append("#");
            for (String cp : cpList) {
                sb.append(cp);
                sb.append("~");
            }
            count++;
            if (count != listCount) {
                sb.append("\\|");
            }
        }
        editor.putString("component", sb.toString());
        editor.commit();
    }

    private HashMap<String, ArrayList<String>> readCustList() {
        Log.i("AppsComponentRestrict", "read cust app cp list...");
        HashMap<String, ArrayList<String>> custList = new HashMap();
        String custListStr = this.mContext.getSharedPreferences("app_cp_record", 0).getString("component", "");
        if (custListStr == null || "".equals(custListStr)) {
            return custList;
        }
        String[] item = custListStr.split("\\|");
        if (item == null) {
            return custList;
        }
        for (String pkgAndCp : item) {
            String[] pkgCp = pkgAndCp.split("#");
            if (!(pkgCp == null || pkgCp.length < 2 || pkgCp[1] == null)) {
                String[] cp = pkgCp[1].split("~");
                ArrayList<String> cpList = new ArrayList();
                if (!(cp == null || "".equals(cp))) {
                    int i = 0;
                    while (i < cp.length) {
                        if (!(cp[i] == null || "".equals(cp[i]))) {
                            cpList.add(cp[i]);
                        }
                        i++;
                    }
                }
                custList.put(pkgCp[0], cpList);
            }
        }
        return custList;
    }

    private String getSettings(String key) {
        SharedPreferences pref = getSettingsPref();
        if (pref == null || key == null) {
            return null;
        }
        return pref.getString(key, null);
    }

    private void updateSettings(String key, String value) {
        if (getSettingsPref() != null) {
            Editor prefEditor = getSettingsPref().edit();
            if (prefEditor != null && key != null) {
                prefEditor.putString(key, value);
                prefEditor.commit();
            }
        }
    }

    private void removeSettingsKey(String key) {
        if (getSettingsPref() != null) {
            Editor prefEditor = getSettingsPref().edit();
            if (prefEditor != null && key != null) {
                prefEditor.remove(key);
                prefEditor.commit();
                Log.i("AppsComponentRestrict", "remove record ok...");
            }
        }
    }

    private void updateLongSettings(String key, long value) {
        if (getSettingsPref() != null) {
            Editor prefEditor = getSettingsPref().edit();
            if (prefEditor != null && key != null) {
                prefEditor.putLong(key, value);
                prefEditor.commit();
            }
        }
    }

    private long getLongSettings(String key, long def) {
        SharedPreferences pref = getSettingsPref();
        if (pref != null) {
            return pref.getLong(key, def);
        }
        return def;
    }

    private SharedPreferences getSettingsPref() {
        return this.mContext.getSharedPreferences("app_cp_record", 0);
    }
}

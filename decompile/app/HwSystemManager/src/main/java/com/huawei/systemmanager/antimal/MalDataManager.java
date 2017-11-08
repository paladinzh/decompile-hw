package com.huawei.systemmanager.antimal;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.util.Xml;
import com.android.internal.widget.LockPatternUtils;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HwLog;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import libcore.io.IoUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class MalDataManager {
    private static final String TAG = "MalDataManager";
    private static final Object mLock = new Object();
    private boolean acbmRecorded = false;
    private int appCounts = 0;
    private boolean hasInstalledMal = false;
    private int insertAppCount = 0;
    private boolean isInstallMalware = false;
    MalAppInfo lastAppInfo = null;
    private List<MalAppInfo> mAllAppList = null;
    private BaseData mBaseData = null;
    private Context mContext = null;
    private List<MalAppInfo> mMalAppList = null;
    private String malBlackListPath = null;
    private String malInfoPath = null;
    private String oldInfoPath = null;
    private long totalInsSpaceTime = 0;

    public static class BaseData {
        public boolean hasLauncherApp = false;
        public boolean hasSetPassword = false;
        public boolean isDefaultHomeTamper = false;
        public int mAppCntBeforeMal = 0;
        public int mDevMgrAppCount = 0;
        public int mInstalledAppCount = 0;
        public int mMalAppCount = 0;

        public String toString() {
            return "BaseData mInstalledAppCount:" + this.mInstalledAppCount + " mMalAppCount:" + this.mMalAppCount + " mAppCntBeforeMal:" + this.mAppCntBeforeMal + " mDevMgrAppCount:" + this.mDevMgrAppCount + " hasSetPassword:" + this.hasSetPassword + " isDefaultHomeTamper:" + this.isDefaultHomeTamper + " hasLauncherApp:" + this.hasLauncherApp;
        }

        public static void writeBaseDataInfo(XmlSerializer serializer, BaseData baseData) {
            int i = 1;
            try {
                int i2;
                serializer.startTag(null, MalwareConst.MAL_BASE_DATA);
                serializer.attribute(null, MalwareConst.REPORT_INSTALLED_APP_COUNT, String.valueOf(baseData.mInstalledAppCount));
                serializer.attribute(null, MalwareConst.REPORT_MAL_APP_COUNT, String.valueOf(baseData.mMalAppCount));
                serializer.attribute(null, MalwareConst.REPORT_APP_COUNT_BEFORE_MAL, String.valueOf(baseData.mAppCntBeforeMal));
                String str = MalwareConst.REPORT_HAS_SET_PASSWORD;
                if (baseData.hasSetPassword) {
                    i2 = 1;
                } else {
                    i2 = 0;
                }
                serializer.attribute(null, str, String.valueOf(i2));
                str = MalwareConst.REPORT_DEFAULT_LAUNCHER_TAMPER;
                if (baseData.isDefaultHomeTamper) {
                    i2 = 1;
                } else {
                    i2 = 0;
                }
                serializer.attribute(null, str, String.valueOf(i2));
                String str2 = "la";
                if (!baseData.hasLauncherApp) {
                    i = 0;
                }
                serializer.attribute(null, str2, String.valueOf(i));
                serializer.attribute(null, MalwareConst.DEV_MGR_APP_COUNT, String.valueOf(baseData.mDevMgrAppCount));
                serializer.endTag(null, MalwareConst.MAL_BASE_DATA);
            } catch (IOException e) {
                HwLog.e(MalDataManager.TAG, "writeMalAppInfo IOException:" + e);
            }
        }

        public static BaseData readBaseDataInfo(XmlPullParser xmlParser) {
            boolean z = false;
            BaseData baseData = new BaseData();
            if (MalwareConst.REPORT_INSTALLED_APP_COUNT.equals(xmlParser.getAttributeName(0))) {
                baseData.mInstalledAppCount = Integer.parseInt(xmlParser.getAttributeValue(null, MalwareConst.REPORT_INSTALLED_APP_COUNT));
            }
            if (MalwareConst.REPORT_MAL_APP_COUNT.equals(xmlParser.getAttributeName(1))) {
                baseData.mMalAppCount = Integer.parseInt(xmlParser.getAttributeValue(null, MalwareConst.REPORT_MAL_APP_COUNT));
            }
            if (MalwareConst.REPORT_APP_COUNT_BEFORE_MAL.equals(xmlParser.getAttributeName(2))) {
                baseData.mAppCntBeforeMal = Integer.parseInt(xmlParser.getAttributeValue(null, MalwareConst.REPORT_APP_COUNT_BEFORE_MAL));
            }
            if (MalwareConst.REPORT_HAS_SET_PASSWORD.equals(xmlParser.getAttributeName(3))) {
                baseData.hasSetPassword = Integer.parseInt(xmlParser.getAttributeValue(null, MalwareConst.REPORT_HAS_SET_PASSWORD)) != 0;
            }
            if (MalwareConst.REPORT_DEFAULT_LAUNCHER_TAMPER.equals(xmlParser.getAttributeName(4))) {
                boolean z2;
                if (Integer.parseInt(xmlParser.getAttributeValue(null, MalwareConst.REPORT_DEFAULT_LAUNCHER_TAMPER)) == 0) {
                    z2 = false;
                } else {
                    z2 = true;
                }
                baseData.isDefaultHomeTamper = z2;
            }
            if ("la".equals(xmlParser.getAttributeName(5))) {
                if (Integer.parseInt(xmlParser.getAttributeValue(null, "la")) != 0) {
                    z = true;
                }
                baseData.hasLauncherApp = z;
            }
            if (MalwareConst.DEV_MGR_APP_COUNT.equals(xmlParser.getAttributeName(6))) {
                baseData.mDevMgrAppCount = Integer.parseInt(xmlParser.getAttributeValue(null, MalwareConst.DEV_MGR_APP_COUNT));
            }
            return baseData;
        }
    }

    public MalDataManager(Context context) {
        this.mContext = context;
    }

    private void initFilePath(String antimalPath) {
        this.malInfoPath = antimalPath + "/" + MalwareConst.ANTI_MAL_INFO_FILE;
        this.malBlackListPath = this.mContext.getCacheDir() + "/" + MalwareConst.ANTI_MAL_MODULE + "/" + MalwareConst.ANTI_MAL_BLACK_LIST_FILE;
    }

    public void init(boolean hasInstalledMal, boolean upgrade, String antimalPath, String oldPath) {
        this.mBaseData = new BaseData();
        initFilePath(antimalPath);
        if (upgrade) {
            this.oldInfoPath = oldPath + "/" + MalwareConst.ANTI_MAL_INFO_FILE;
            readMalInfoFromFile(this.oldInfoPath);
            writeMalInfoToFile(this.malInfoPath);
        } else {
            readMalInfoFromFile(this.malInfoPath);
        }
        if (hasInstalledMal) {
            synchronized (mLock) {
                if (this.mMalAppList != null) {
                    this.mMalAppList.clear();
                    this.mMalAppList = null;
                }
                this.mMalAppList = new ArrayList();
                for (MalAppInfo app : this.mAllAppList) {
                    if ((app.mAppStatus & 1) == 1) {
                        this.mMalAppList.add(app);
                    }
                }
            }
        }
    }

    public boolean getHomeStatus() {
        return this.mBaseData != null ? this.mBaseData.isDefaultHomeTamper : false;
    }

    public boolean getSetPswdStatus() {
        return this.mBaseData != null ? this.mBaseData.hasSetPassword : false;
    }

    public boolean getLauncherAppStatus() {
        return this.mBaseData != null ? this.mBaseData.hasLauncherApp : false;
    }

    private int getAllAppCnt() {
        return this.mBaseData != null ? this.mBaseData.mInstalledAppCount : 0;
    }

    private int getAppCntBeforeMal() {
        return this.mBaseData != null ? this.mBaseData.mAppCntBeforeMal : 0;
    }

    public int getDevMgrAppCnt() {
        return this.mBaseData != null ? this.mBaseData.mDevMgrAppCount : 0;
    }

    public String getBaseDataString() {
        return this.mBaseData != null ? this.mBaseData.toString() : "";
    }

    public void insertAppInfo(MalAppInfo appInfo, AntiMalConfig config, UserBehaviorManager ubm) {
        synchronized (mLock) {
            if (appInfo != null) {
                if (!(TextUtils.isEmpty(appInfo.mPackageName) || this.mAllAppList == null)) {
                    this.mAllAppList.add(appInfo);
                    analyzeContinuity(this.lastAppInfo, appInfo, config, ubm);
                    this.lastAppInfo = appInfo;
                    HwLog.i(TAG, "insertAppInfo " + appInfo.toString());
                    writeMalInfoToFile(this.malInfoPath);
                    return;
                }
            }
            HwLog.e(TAG, "insertAppInfo input param error.");
        }
    }

    public void analyzeContinuity(MalAppInfo lastApp, MalAppInfo currentApp, AntiMalConfig config, UserBehaviorManager ubm) {
        if (currentApp == null || this.mBaseData == null || config == null) {
            HwLog.e(TAG, "analyzeContinuity currentApp or mBaseData is null.");
            return;
        }
        if (this.appCounts == 0) {
            this.appCounts = this.mBaseData.mInstalledAppCount;
        }
        this.appCounts++;
        if (lastApp != null) {
            currentApp.mSpaceTime = currentApp.mInstallBeginTime - lastApp.mInstallEndTime;
            if (currentApp.mInstaller == 1) {
                this.totalInsSpaceTime += currentApp.mSpaceTime;
                if (currentApp.mSpaceTime >= config.mCfgMaxInsSpaceTime || currentApp.mSpaceTime < 0) {
                    this.isInstallMalware = false;
                    this.insertAppCount = 0;
                    this.totalInsSpaceTime = 0;
                } else {
                    this.insertAppCount++;
                    if (!this.isInstallMalware && this.insertAppCount >= config.mCfgMinInsSameCount - 1 && this.totalInsSpaceTime / ((long) this.insertAppCount) < config.mCfgMaxAvgSpaceTime) {
                        this.isInstallMalware = true;
                        this.mBaseData.hasSetPassword = getHasSetPassword();
                        if (ubm != null) {
                            ubm.insertUserBehavior(3);
                        }
                    }
                }
                if (this.isInstallMalware) {
                    this.mBaseData.mMalAppCount = this.insertAppCount + 1;
                    if (!this.acbmRecorded && this.mBaseData.mAppCntBeforeMal == 0) {
                        this.mBaseData.mAppCntBeforeMal = this.appCounts - this.mBaseData.mMalAppCount;
                    }
                }
            }
        }
    }

    private boolean getHasSetPassword() {
        return new LockPatternUtils(this.mContext).isSecure(ActivityManager.getCurrentUser());
    }

    private boolean isDefaultHomeTamper(List<String> launcherList) {
        if (launcherList == null || launcherList.size() == 0) {
            HwLog.i(TAG, "isDefaultHomeTamper launcherList is null or size is 0.");
            return false;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        ResolveInfo res = this.mContext.getPackageManager().resolveActivity(intent, 0);
        if (res == null || res.activityInfo == null || TextUtils.isEmpty(res.activityInfo.packageName)) {
            HwLog.e(TAG, "isDefaultHomeTamper param is null.");
            return false;
        } else if (res.activityInfo.packageName.equals("com.huawei.android.launcher") || !launcherList.contains(res.activityInfo.packageName)) {
            return false;
        } else {
            HwLog.i(TAG, "isDefaultHomeTamper res.activityInfo.packageName:" + res.activityInfo.packageName);
            return true;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean analyze(AntiMalConfig config) {
        if (this.mBaseData == null) {
            HwLog.e(TAG, "analyze mBaseData is null.");
            return false;
        }
        int installerSameCount = 0;
        long totalSpaceTime = 0;
        int devMgrAppCount = 0;
        boolean installedMalApp = false;
        boolean hasLauncherApp = false;
        List<String> launcherList = new ArrayList();
        synchronized (mLock) {
            if (this.mAllAppList == null || this.mAllAppList.size() == 0) {
                HwLog.e(TAG, "analyze input param is null.");
                return false;
            }
            if (this.mMalAppList != null) {
                this.mMalAppList.clear();
            }
            this.mMalAppList = new ArrayList();
            this.mAllAppList = MalAppInfo.getSortAppList(this.mAllAppList);
            for (int i = 0; i < this.mAllAppList.size(); i++) {
                MalAppInfo malApp = (MalAppInfo) this.mAllAppList.get(i);
                malApp.analyze();
                if (malApp.mInstaller == 1) {
                    totalSpaceTime += malApp.mSpaceTime;
                    long avgSpaceTime = totalSpaceTime / ((long) (i + 1));
                    if (malApp.mSpaceTime < config.mCfgMaxInsSpaceTime && malApp.mSpaceTime >= 0) {
                        this.mMalAppList.add(malApp);
                        if (i != 0) {
                            installerSameCount++;
                            if (avgSpaceTime < config.mCfgMaxAvgSpaceTime && installerSameCount >= config.mCfgMinInsSameCount - 1) {
                                installedMalApp = true;
                            }
                        }
                    } else if (installedMalApp) {
                        break;
                    } else {
                        installerSameCount = 0;
                        totalSpaceTime = 0;
                        if (this.mMalAppList.size() != 0) {
                            this.mMalAppList.clear();
                        }
                        this.mMalAppList.add(malApp);
                    }
                    HwLog.i(TAG, "analyze installerSameCount:" + (installerSameCount + 1));
                }
            }
            if (installedMalApp) {
                for (MalAppInfo malApp2 : this.mMalAppList) {
                    if ((malApp2.mAppStatus & 2) != 0) {
                        devMgrAppCount++;
                    }
                    if ((malApp2.mAppStatus & 8) != 0) {
                        hasLauncherApp = true;
                        launcherList.add(malApp2.mPackageName);
                    }
                    for (MalAppInfo app : this.mAllAppList) {
                        if (TextUtils.equals(malApp2.mPackageName, app.mPackageName)) {
                            app.mAppStatus |= 1;
                        }
                    }
                }
            }
        }
    }

    public List<String> getMalAppList() {
        List<String> malAppList = new ArrayList();
        synchronized (mLock) {
            for (MalAppInfo malApp : this.mMalAppList) {
                boolean duplicate = false;
                try {
                    for (String pkgName : malAppList) {
                        if (TextUtils.equals(pkgName, malApp.mPackageName)) {
                            duplicate = true;
                        }
                    }
                    if (!(this.mContext.getPackageManager().getPackageInfo(malApp.mPackageName, 0) == null || duplicate)) {
                        malAppList.add(malApp.mPackageName);
                    }
                } catch (NameNotFoundException nnfe) {
                    HwLog.e(TAG, "NameNotFoundException:" + nnfe.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return malAppList;
    }

    public boolean report(UserBehaviorManager ubm) {
        synchronized (mLock) {
            if (this.mMalAppList == null || this.mMalAppList.size() == 0) {
                HwLog.e(TAG, "report mMalAppList is null.");
                return false;
            }
            try {
                int size = this.mMalAppList.size();
                int sendCnt = size / 5;
                JSONObject listJson = new JSONObject();
                for (int j = 0; j < sendCnt; j++) {
                    JSONArray jsonArry = new JSONArray();
                    for (int i = 0; i < 5; i++) {
                        jsonArry.put(((MalAppInfo) this.mMalAppList.get((j * 5) + i)).toJson(true));
                    }
                    listJson.put(MalwareConst.PACKAGE_LIST, jsonArry);
                    HwLog.i(TAG, "reportAntiMalDataToBD LENGTH = " + listJson.toString().length() + "\n ANTIMAL data : " + listJson.toString());
                    HsmStat.statE(Events.E_ANTIMAL_BASE_INFO, listJson.toString());
                }
                int left = size - (size % 5);
                JSONObject leftJson = new JSONObject();
                leftJson.put(MalwareConst.REPORT_INSTALLED_APP_COUNT, getAllAppCnt());
                leftJson.put(MalwareConst.REPORT_MAL_APP_COUNT, this.mMalAppList.size());
                leftJson.put(MalwareConst.REPORT_APP_COUNT_BEFORE_MAL, getAppCntBeforeMal());
                leftJson.put(MalwareConst.REPORT_HAS_SET_PASSWORD, getSetPswdStatus() ? 1 : 0);
                leftJson.put(MalwareConst.REPORT_DEFAULT_LAUNCHER_TAMPER, getHomeStatus() ? 1 : 0);
                leftJson.put(MalwareConst.REPORT_USER_BE_BEFORE_MAL, ubm != null ? ubm.getBehaviorStatus() : 0);
                if (left < size) {
                    JSONArray jsons = new JSONArray();
                    while (left < size) {
                        jsons.put(((MalAppInfo) this.mMalAppList.get(left)).toJson(true));
                        left++;
                    }
                    leftJson.put(MalwareConst.PACKAGE_LIST, jsons);
                }
                HwLog.i(TAG, "reportAntiMalDataToBD LEFT LENGTH = " + leftJson.toString().length() + "\n LEFT ANTIMAL data : " + leftJson.toString());
                HsmStat.statE(Events.E_ANTIMAL_BASE_INFO, leftJson.toString());
            } catch (JSONException je) {
                HwLog.e(TAG, "reportAntiMalDataToBD JSONException:" + je);
            } catch (Exception e) {
                HwLog.e(TAG, "reportAntiMalDataToBD Exception:" + e);
            }
        }
        return true;
    }

    private void writeMalInfoToFile(String path) {
        BufferedOutputStream bos;
        FileNotFoundException fnfe;
        Object fstr;
        Throwable th;
        IOException ioe;
        Exception e;
        Object bos2;
        if (TextUtils.isEmpty(path)) {
            HwLog.e(TAG, "writeMalInfoToFile path is null");
        } else if (this.mBaseData == null) {
            HwLog.e(TAG, "writeMalInfoToFile mBaseData is null.");
        } else {
            AutoCloseable autoCloseable = null;
            AutoCloseable autoCloseable2 = null;
            try {
                FileOutputStream fstr2 = new FileOutputStream(path, false);
                try {
                    bos = new BufferedOutputStream(fstr2);
                } catch (FileNotFoundException e2) {
                    fnfe = e2;
                    fstr = fstr2;
                    try {
                        HwLog.e(TAG, "writeMalInfoToFile FileNotFoundException:" + fnfe);
                        IoUtils.closeQuietly(autoCloseable);
                        IoUtils.closeQuietly(autoCloseable2);
                    } catch (Throwable th2) {
                        th = th2;
                        IoUtils.closeQuietly(autoCloseable);
                        IoUtils.closeQuietly(autoCloseable2);
                        throw th;
                    }
                } catch (IOException e3) {
                    ioe = e3;
                    fstr = fstr2;
                    HwLog.e(TAG, "writeMalInfoToFile IOException:" + ioe);
                    IoUtils.closeQuietly(autoCloseable);
                    IoUtils.closeQuietly(autoCloseable2);
                } catch (Exception e4) {
                    e = e4;
                    fstr = fstr2;
                    HwLog.e(TAG, "writeMalInfoToFile Exception:" + e);
                    IoUtils.closeQuietly(autoCloseable);
                    IoUtils.closeQuietly(autoCloseable2);
                } catch (Throwable th3) {
                    th = th3;
                    fstr = fstr2;
                    IoUtils.closeQuietly(autoCloseable);
                    IoUtils.closeQuietly(autoCloseable2);
                    throw th;
                }
                try {
                    XmlSerializer serializer = Xml.newSerializer();
                    serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                    serializer.setOutput(bos, "UTF-8");
                    serializer.startDocument("UTF-8", Boolean.valueOf(true));
                    int appListSize = 0;
                    synchronized (mLock) {
                        if (!(this.mAllAppList == null || this.mAllAppList.size() == 0)) {
                            serializer.startTag(null, MalwareConst.PACKAGE_LIST);
                            for (MalAppInfo appInfo : this.mAllAppList) {
                                MalAppInfo.writeMalAppInfo(serializer, appInfo);
                            }
                            serializer.endTag(null, MalwareConst.PACKAGE_LIST);
                            appListSize = this.mAllAppList.size();
                        }
                    }
                    this.mBaseData.mInstalledAppCount = appListSize;
                    BaseData.writeBaseDataInfo(serializer, this.mBaseData);
                    serializer.endDocument();
                    bos.flush();
                    fstr2.flush();
                    IoUtils.closeQuietly(bos);
                    IoUtils.closeQuietly(fstr2);
                } catch (FileNotFoundException e5) {
                    fnfe = e5;
                    autoCloseable2 = fstr2;
                    autoCloseable = bos;
                    HwLog.e(TAG, "writeMalInfoToFile FileNotFoundException:" + fnfe);
                    IoUtils.closeQuietly(autoCloseable);
                    IoUtils.closeQuietly(autoCloseable2);
                } catch (IOException e6) {
                    ioe = e6;
                    fstr = fstr2;
                    bos2 = bos;
                    HwLog.e(TAG, "writeMalInfoToFile IOException:" + ioe);
                    IoUtils.closeQuietly(autoCloseable);
                    IoUtils.closeQuietly(autoCloseable2);
                } catch (Exception e7) {
                    e = e7;
                    fstr = fstr2;
                    bos2 = bos;
                    HwLog.e(TAG, "writeMalInfoToFile Exception:" + e);
                    IoUtils.closeQuietly(autoCloseable);
                    IoUtils.closeQuietly(autoCloseable2);
                } catch (Throwable th4) {
                    th = th4;
                    fstr = fstr2;
                    bos2 = bos;
                    IoUtils.closeQuietly(autoCloseable);
                    IoUtils.closeQuietly(autoCloseable2);
                    throw th;
                }
            } catch (FileNotFoundException e8) {
                fnfe = e8;
                HwLog.e(TAG, "writeMalInfoToFile FileNotFoundException:" + fnfe);
                IoUtils.closeQuietly(autoCloseable);
                IoUtils.closeQuietly(autoCloseable2);
            } catch (IOException e9) {
                ioe = e9;
                HwLog.e(TAG, "writeMalInfoToFile IOException:" + ioe);
                IoUtils.closeQuietly(autoCloseable);
                IoUtils.closeQuietly(autoCloseable2);
            } catch (Exception e10) {
                e = e10;
                HwLog.e(TAG, "writeMalInfoToFile Exception:" + e);
                IoUtils.closeQuietly(autoCloseable);
                IoUtils.closeQuietly(autoCloseable2);
            }
        }
    }

    private void readMalInfoFromFile(String path) {
        FileNotFoundException fnfe;
        XmlPullParserException xppe;
        Exception e;
        Object inStream;
        Throwable th;
        if (TextUtils.isEmpty(path)) {
            HwLog.e(TAG, "readMalInfoFromFile path is null");
            return;
        }
        synchronized (mLock) {
            if (this.mAllAppList != null) {
                this.mAllAppList.clear();
                this.mAllAppList = null;
            }
            this.mAllAppList = new ArrayList();
        }
        if (this.mBaseData == null) {
            this.mBaseData = new BaseData();
        }
        File xmlFile = new File(path);
        if (xmlFile.exists()) {
            AutoCloseable autoCloseable = null;
            try {
                XmlPullParser xmlParser = Xml.newPullParser();
                FileInputStream inStream2 = new FileInputStream(xmlFile);
                try {
                    xmlParser.setInput(inStream2, "UTF-8");
                    MalAppInfo malAppInfo = null;
                    for (int eventType = xmlParser.getEventType(); eventType != 1; eventType = xmlParser.next()) {
                        switch (eventType) {
                            case 2:
                                String name = xmlParser.getName();
                                if (MalwareConst.PACKAGE_LIST.equals(name)) {
                                    HwLog.i(TAG, "readMalInfoFromFile " + name);
                                }
                                if (!"package".equals(name)) {
                                    if (!MalwareConst.MAL_BASE_DATA.equals(name)) {
                                        break;
                                    }
                                    this.mBaseData = BaseData.readBaseDataInfo(xmlParser);
                                    break;
                                }
                                malAppInfo = MalAppInfo.readMalAppInfo(this.mContext, xmlParser);
                                break;
                            case 3:
                                if ("package".equals(xmlParser.getName())) {
                                    if (malAppInfo != null) {
                                        HwLog.i(TAG, "readMalInfoFromFile " + malAppInfo.toString());
                                        synchronized (mLock) {
                                            this.mAllAppList.add(malAppInfo);
                                        }
                                    }
                                    malAppInfo = null;
                                    break;
                                }
                                continue;
                            default:
                                break;
                        }
                    }
                    IoUtils.closeQuietly(inStream2);
                } catch (FileNotFoundException e2) {
                    fnfe = e2;
                    autoCloseable = inStream2;
                } catch (XmlPullParserException e3) {
                    xppe = e3;
                    autoCloseable = inStream2;
                } catch (Exception e4) {
                    e = e4;
                    inStream = inStream2;
                } catch (Throwable th2) {
                    th = th2;
                    inStream = inStream2;
                }
            } catch (FileNotFoundException e5) {
                fnfe = e5;
                try {
                    HwLog.e(TAG, "readMalInfoFromFile FileNotFoundException:" + fnfe);
                    IoUtils.closeQuietly(autoCloseable);
                } catch (Throwable th3) {
                    th = th3;
                    IoUtils.closeQuietly(autoCloseable);
                    throw th;
                }
            } catch (XmlPullParserException e6) {
                xppe = e6;
                HwLog.e(TAG, "readMalInfoFromFile XmlPullParserException:" + xppe);
                IoUtils.closeQuietly(autoCloseable);
            } catch (Exception e7) {
                e = e7;
                HwLog.e(TAG, "readMalInfoFromFile Exception:" + e);
                IoUtils.closeQuietly(autoCloseable);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isInBlackList(int minBlackMatchCount) {
        HashMap<String, String> blackList = readBlackList(this.malBlackListPath);
        if (blackList == null) {
            HwLog.i(TAG, "isInBlackList blackList is null or size is 0.");
            return false;
        }
        int matchBlackCount = 0;
        synchronized (mLock) {
            if (this.mMalAppList == null || this.mMalAppList.size() == 0) {
                HwLog.e(TAG, "isInBlackList input param is null.");
                return false;
            }
            for (MalAppInfo malApp : this.mMalAppList) {
                if (TextUtils.equals(malApp.mSignHash, (CharSequence) blackList.get(malApp.mPackageName))) {
                    matchBlackCount++;
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private HashMap<String, String> readBlackList(String filePath) {
        FileNotFoundException fnfe;
        Object obj;
        XmlPullParserException xppe;
        Exception e;
        Throwable th;
        File xmlFile = new File(filePath);
        if (xmlFile.exists()) {
            HashMap<String, String> hashMap = null;
            AutoCloseable autoCloseable = null;
            try {
                FileInputStream inStream = new FileInputStream(xmlFile);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(inStream, "UTF-8");
                    int eType = parser.getEventType();
                    HashMap<String, String> blackList = null;
                    while (eType != 1) {
                        switch (eType) {
                            case 0:
                                hashMap = blackList;
                                break;
                            case 2:
                                try {
                                    String startTag = parser.getName();
                                    if (!TextUtils.equals(startTag, MalwareConst.BLACK_LIST)) {
                                        if (!TextUtils.equals(startTag, "package")) {
                                            hashMap = blackList;
                                            break;
                                        }
                                        String pkgName = "";
                                        String signHash = "";
                                        if ("pkg_name".equals(parser.getAttributeName(0))) {
                                            pkgName = parser.getAttributeValue(null, "pkg_name");
                                        }
                                        if ("hash".equals(parser.getAttributeName(1))) {
                                            signHash = parser.getAttributeValue(null, "hash");
                                        }
                                        if (!TextUtils.isEmpty(pkgName) && blackList != null) {
                                            blackList.put(pkgName, signHash);
                                            hashMap = blackList;
                                            break;
                                        }
                                        hashMap = blackList;
                                        break;
                                    }
                                    hashMap = new HashMap();
                                    break;
                                } catch (FileNotFoundException e2) {
                                    fnfe = e2;
                                    obj = inStream;
                                    hashMap = blackList;
                                    break;
                                } catch (XmlPullParserException e3) {
                                    xppe = e3;
                                    obj = inStream;
                                    hashMap = blackList;
                                    break;
                                } catch (Exception e4) {
                                    e = e4;
                                    obj = inStream;
                                    hashMap = blackList;
                                    break;
                                } catch (Throwable th2) {
                                    th = th2;
                                    obj = inStream;
                                    break;
                                }
                            case 3:
                                if (TextUtils.equals(parser.getName(), MalwareConst.BLACK_LIST) && blackList != null) {
                                    HwLog.i(TAG, "readBlackList blackList.size():" + blackList.size());
                                }
                                hashMap = blackList;
                                break;
                            default:
                                hashMap = blackList;
                                break;
                        }
                    }
                    IoUtils.closeQuietly(inStream);
                    hashMap = blackList;
                } catch (FileNotFoundException e5) {
                    fnfe = e5;
                    obj = inStream;
                    HwLog.e(TAG, "readBlackList FileNotFoundException:" + fnfe);
                    IoUtils.closeQuietly(autoCloseable);
                    return hashMap;
                } catch (XmlPullParserException e6) {
                    xppe = e6;
                    obj = inStream;
                    HwLog.e(TAG, "readBlackList XmlPullParserException:" + xppe);
                    IoUtils.closeQuietly(autoCloseable);
                    return hashMap;
                } catch (Exception e7) {
                    e = e7;
                    obj = inStream;
                    try {
                        HwLog.e(TAG, "readBlackList Exception:" + e);
                        IoUtils.closeQuietly(autoCloseable);
                        return hashMap;
                    } catch (Throwable th3) {
                        th = th3;
                        IoUtils.closeQuietly(autoCloseable);
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    obj = inStream;
                    IoUtils.closeQuietly(autoCloseable);
                    throw th;
                }
            } catch (FileNotFoundException e8) {
                fnfe = e8;
                HwLog.e(TAG, "readBlackList FileNotFoundException:" + fnfe);
                IoUtils.closeQuietly(autoCloseable);
                return hashMap;
            } catch (XmlPullParserException e9) {
                xppe = e9;
                HwLog.e(TAG, "readBlackList XmlPullParserException:" + xppe);
                IoUtils.closeQuietly(autoCloseable);
                return hashMap;
            } catch (Exception e10) {
                e = e10;
                HwLog.e(TAG, "readBlackList Exception:" + e);
                IoUtils.closeQuietly(autoCloseable);
                return hashMap;
            }
            return hashMap;
        }
        HwLog.i(TAG, "readBlackList xmlFile is not exist, filePath:" + filePath);
        return null;
    }
}

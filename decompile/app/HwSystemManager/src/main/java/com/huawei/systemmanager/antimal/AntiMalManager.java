package com.huawei.systemmanager.antimal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Xml;
import com.huawei.systemmanager.antimal.AntiMalConfig.AntiMalConfigCallback;
import com.huawei.systemmanager.antimal.ui.AntimalwareNotification;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.push.CustomTaskHandler;
import com.huawei.systemmanager.rainbow.CloudSwitchHelper;
import com.huawei.systemmanager.useragreement.UserAgreementHelper;
import com.huawei.systemmanager.util.HwLog;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class AntiMalManager {
    private static final boolean IS_CHINA_AREA = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    public static final int MSG_TYPE_ALERT_HANDLED = 3;
    public static final int MSG_TYPE_REPORT_WHIT_NET = 4;
    public static final int MSG_TYPE_SCREEN_OFF = 2;
    public static final int MSG_TYPE_SCREEN_ON = 1;
    public static final long ONE_MINUTE_FROM_BOOT_COMPLETE = 60000;
    public static final int STATUS_ALERTED = 4;
    public static final int STATUS_ALERTED_HANDLED = 5;
    public static final int STATUS_ALERT_ANALYZED = 3;
    public static final int STATUS_DEFAULT = 0;
    public static final int STATUS_REPORTED = 2;
    public static final int STATUS_REPORT_ANALYZED = 1;
    private static final String TAG = "AntiMalManager";
    private static final boolean isAlertDisable = "false".equalsIgnoreCase(SystemProperties.get("ro.config.antimal_enable", "true"));
    private String antimalPath = null;
    private String antimalPathEmui41 = null;
    private String antimalPathEmui5 = null;
    private boolean isConfigXmlReaded = true;
    private boolean isReceiverRegistered = false;
    private boolean isScreenOn = false;
    private AntiMalConfig mConfig = null;
    private AntiMalConfigCallback mConfigCallback = new AntiMalConfigCallback() {
        public void onAlert() {
            if (AntiMalManager.this.mMalDataMgr == null || AntiMalManager.this.mStatusData == null) {
                HwLog.e(AntiMalManager.TAG, "onGetConfig mMalDataMgr or mStatusData is null.");
                return;
            }
            HwLog.i(AntiMalManager.TAG, "onAlert");
            if (!(!AntiMalManager.this.mStatusData.hasInstalledMal || AntiMalManager.this.mMalDataMgr.getMalAppList().size() == 0 || AntiMalManager.this.mStatusData.mAntiMalStatus == 5 || AntiMalManager.this.mStatusData.mAntiMalStatus == 4)) {
                AntiMalManager.this.alertUser();
                AntiMalManager.this.writeMalStatusFile(AntiMalManager.this.malStatusPath);
            }
        }

        public void onGetConfig() {
            if (AntiMalManager.this.mConfig == null || AntiMalManager.this.mStatusData == null) {
                HwLog.e(AntiMalManager.TAG, "onGetConfig mConfig or mStatusData is null.");
                return;
            }
            AntiMalManager.this.isConfigXmlReaded = AntiMalManager.this.mConfig.readConfigXml();
            HwLog.i(AntiMalManager.TAG, "onGetConfig " + AntiMalManager.this.mConfig.toString());
            if (AntiMalManager.this.mStatusData.mAntiMalStatus == 1) {
                AntiMalManager.this.doDetection();
                if (AntiMalManager.this.mStatusData.mScreenOnTime > AntiMalManager.this.mConfig.mCfgMaxAlertTime) {
                    AntiMalManager.this.doAlert();
                }
            } else if (AntiMalManager.this.mStatusData.mAntiMalStatus == 3) {
                AntiMalManager.this.doAlert();
            }
            if (!AntiMalManager.this.mConfig.mCfgFetureSwitch) {
                AntiMalManager.this.closeFeature();
            }
        }

        public void onGetBlacklist() {
            if (AntiMalManager.this.mConfig == null || AntiMalManager.this.mStatusData == null) {
                HwLog.e(AntiMalManager.TAG, "onGetBlacklist mConfig or mStatusData is null.");
                return;
            }
            HwLog.i(AntiMalManager.TAG, "onGetBlacklist");
            AntiMalManager.this.isConfigXmlReaded = AntiMalManager.this.mConfig.readConfigXml();
            if (AntiMalManager.this.mStatusData.mAntiMalStatus == 3) {
                AntiMalManager.this.doAlert();
            }
        }
    };
    private Context mContext = null;
    private Handler mHandler = null;
    private MalDataManager mMalDataMgr = null;
    private BroadcastReceiver mScreenRecevier = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (!Utility.checkBroadcast(context, intent)) {
                return;
            }
            if (AntiMalManager.this.mStatusData == null) {
                HwLog.e(AntiMalManager.TAG, "mScreenRecevier mStatusData is null");
                return;
            }
            boolean isPowerConnected = AntiMalManager.this.getPowerStatus();
            AntiMalManager antiMalManager;
            if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                AntiMalManager.this.isScreenOn = false;
                if (!isPowerConnected) {
                    antiMalManager = AntiMalManager.this;
                    antiMalManager.onTime = antiMalManager.onTime + (SystemClock.elapsedRealtime() - AntiMalManager.this.screenOnTime);
                }
                if (AntiMalManager.this.onTime > 600000) {
                    StatusData -get6 = AntiMalManager.this.mStatusData;
                    -get6.mScreenOnTime += AntiMalManager.this.onTime;
                    AntiMalManager.this.obtainMessage(2);
                    AntiMalManager.this.onTime = 0;
                }
                if ((AntiMalManager.this.mStatusData.mAntiMalStatus == 4 || AntiMalManager.this.mStatusData.mAntiMalStatus == 5) && AntiMalManager.this.getAlertResult(AntiMalManager.this.mContext) && UserAgreementHelper.getUserAgreementState(AntiMalManager.this.mContext)) {
                    AntiMalManager.this.obtainMessage(3);
                }
                HwLog.i(AntiMalManager.TAG, "receive Broadcast ACTION_SCREEN_OFF mOnTimeSum:" + AntiMalManager.this.mStatusData.mScreenOnTime + " onTime:" + AntiMalManager.this.onTime + " isPowerConnected:" + isPowerConnected);
            } else if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                AntiMalManager.this.isScreenOn = true;
                if (!isPowerConnected) {
                    AntiMalManager.this.screenOnTime = SystemClock.elapsedRealtime();
                }
                AntiMalManager.this.obtainMessage(1);
            } else if ("android.intent.action.ACTION_POWER_CONNECTED".equals(intent.getAction())) {
                if (AntiMalManager.this.isScreenOn) {
                    antiMalManager = AntiMalManager.this;
                    antiMalManager.onTime = antiMalManager.onTime + (SystemClock.elapsedRealtime() - AntiMalManager.this.screenOnTime);
                }
            } else if ("android.intent.action.ACTION_POWER_DISCONNECTED".equals(intent.getAction())) {
                if (AntiMalManager.this.isScreenOn) {
                    AntiMalManager.this.screenOnTime = SystemClock.elapsedRealtime();
                }
            } else if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE") && AntiMalManager.this.mStatusData.hasInstalledMal && AntiMalManager.this.isWifiConnected() && !AntiMalManager.this.mStatusData.isDataReported && SystemClock.elapsedRealtime() > 60000) {
                AntiMalManager.this.obtainMessage(4);
            }
        }
    };
    private StatusData mStatusData = null;
    private HandlerThread mThread = null;
    private UserBehaviorManager mUserBeMgr = null;
    private String malStatusPath = null;
    private boolean needAlertAgain = false;
    private boolean needTransferData = false;
    private String oldPath = null;
    private long onTime = 0;
    private long screenOnTime = 0;
    private String statusOldPath = null;

    public static class StatusData {
        public boolean hasInstalledMal = false;
        public boolean hasOwnerBehavior = false;
        public boolean isAntiMalOpen = true;
        public boolean isDataReported = false;
        public int mAntiMalStatus = 0;
        public long mScreenOnTime = 0;

        public String toString() {
            return "StatusData isAntiMalOpen:" + this.isAntiMalOpen + " hasInstalledMal:" + this.hasInstalledMal + " mAntiMalStatus:" + this.mAntiMalStatus + " mScreenOnTime:" + this.mScreenOnTime + " hasOwnerBehavior:" + this.hasOwnerBehavior + " isDataReported:" + this.isDataReported;
        }

        public static void writeStatusDataInfo(XmlSerializer serializer, StatusData statusData) {
            int i = 1;
            try {
                int i2;
                serializer.startTag(null, "status");
                String str = MalwareConst.IS_ANTIMAL_OPEN;
                if (statusData.isAntiMalOpen) {
                    i2 = 1;
                } else {
                    i2 = 0;
                }
                serializer.attribute(null, str, String.valueOf(i2));
                str = MalwareConst.IS_NEED_REPORT;
                if (statusData.hasInstalledMal) {
                    i2 = 1;
                } else {
                    i2 = 0;
                }
                serializer.attribute(null, str, String.valueOf(i2));
                serializer.attribute(null, MalwareConst.ANTI_MAL_STATUS, String.valueOf(statusData.mAntiMalStatus));
                serializer.attribute(null, MalwareConst.SCREEN_ON_TIME, String.valueOf(statusData.mScreenOnTime));
                str = MalwareConst.REPORT_USER_BE_BEFORE_MAL;
                if (statusData.hasOwnerBehavior) {
                    i2 = 1;
                } else {
                    i2 = 0;
                }
                serializer.attribute(null, str, String.valueOf(i2));
                String str2 = MalwareConst.IS_DATA_REPORTED;
                if (!statusData.isDataReported) {
                    i = 0;
                }
                serializer.attribute(null, str2, String.valueOf(i));
                serializer.endTag(null, "status");
            } catch (IOException e) {
                HwLog.e(AntiMalManager.TAG, "writeStatusDataInfo IOException:" + e);
            }
        }

        public static StatusData readStatusDataInfo(XmlPullParser xmlParser) {
            boolean z;
            boolean z2 = false;
            StatusData statusData = new StatusData();
            if (MalwareConst.IS_ANTIMAL_OPEN.equals(xmlParser.getAttributeName(0))) {
                statusData.isAntiMalOpen = Integer.parseInt(xmlParser.getAttributeValue(null, MalwareConst.IS_ANTIMAL_OPEN)) != 0;
            }
            if (MalwareConst.IS_NEED_REPORT.equals(xmlParser.getAttributeName(1))) {
                if (Integer.parseInt(xmlParser.getAttributeValue(null, MalwareConst.IS_NEED_REPORT)) == 0) {
                    z = false;
                } else {
                    z = true;
                }
                statusData.hasInstalledMal = z;
            }
            if (MalwareConst.ANTI_MAL_STATUS.equals(xmlParser.getAttributeName(2))) {
                statusData.mAntiMalStatus = Integer.parseInt(xmlParser.getAttributeValue(null, MalwareConst.ANTI_MAL_STATUS));
            }
            if (MalwareConst.SCREEN_ON_TIME.equals(xmlParser.getAttributeName(3))) {
                statusData.mScreenOnTime = Long.parseLong(xmlParser.getAttributeValue(null, MalwareConst.SCREEN_ON_TIME));
            }
            if (MalwareConst.REPORT_USER_BE_BEFORE_MAL.equals(xmlParser.getAttributeName(4))) {
                if (Integer.parseInt(xmlParser.getAttributeValue(null, MalwareConst.REPORT_USER_BE_BEFORE_MAL)) == 0) {
                    z = false;
                } else {
                    z = true;
                }
                statusData.hasOwnerBehavior = z;
            }
            if (MalwareConst.IS_DATA_REPORTED.equals(xmlParser.getAttributeName(5))) {
                if (Integer.parseInt(xmlParser.getAttributeValue(null, MalwareConst.IS_DATA_REPORTED)) != 0) {
                    z2 = true;
                }
                statusData.isDataReported = z2;
            }
            return statusData;
        }
    }

    public AntiMalManager(Context context) {
        this.mContext = context;
        this.mThread = new HandlerThread("AntiMalThread");
        this.mThread.start();
        this.mHandler = new Handler(this.mThread.getLooper()) {
            public void handleMessage(Message msg) {
                if (AntiMalManager.this.mStatusData == null || AntiMalManager.this.mConfig == null) {
                    HwLog.e(AntiMalManager.TAG, "handleMessage mStatusData or mConfig is null");
                    return;
                }
                switch (msg.what) {
                    case 1:
                        if (AntiMalManager.this.needAlertAgain) {
                            AntiMalManager.this.alertUser();
                            AntiMalManager.this.writeMalStatusFile(AntiMalManager.this.malStatusPath);
                        } else {
                            if (AntiMalManager.this.mStatusData.mAntiMalStatus == 0 && AntiMalManager.this.mStatusData.mScreenOnTime > AntiMalManager.this.mConfig.mCfgMaxDetectTime && AntiMalManager.this.isUserUnlocked()) {
                                AntiMalManager.this.mStatusData.mAntiMalStatus = 1;
                                if (!AntiMalManager.this.isConfigXmlReaded) {
                                    AntiMalManager.this.isConfigXmlReaded = AntiMalManager.this.mConfig.readConfigXml();
                                }
                                AntiMalManager.this.doDetection();
                            }
                            if (!AntiMalManager.isAlertDisable && AntiMalManager.this.mStatusData.hasInstalledMal && ((AntiMalManager.this.mStatusData.mAntiMalStatus == 1 || AntiMalManager.this.mStatusData.mAntiMalStatus == 2) && AntiMalManager.this.mStatusData.mScreenOnTime > AntiMalManager.this.mConfig.mCfgMaxAlertTime)) {
                                AntiMalManager.this.mStatusData.mAntiMalStatus = 3;
                                if (!AntiMalManager.this.isConfigXmlReaded) {
                                    AntiMalManager.this.isConfigXmlReaded = AntiMalManager.this.mConfig.readConfigXml();
                                }
                                AntiMalManager.this.doAlert();
                            }
                        }
                        if (AntiMalManager.this.mStatusData.mAntiMalStatus != 3 && AntiMalManager.this.mStatusData.mScreenOnTime > AntiMalManager.this.mConfig.mCfgFeatureOpenTime) {
                            AntiMalManager.this.closeFeature();
                            break;
                        }
                    case 2:
                        AntiMalManager.this.writeMalStatusFile(AntiMalManager.this.malStatusPath);
                        break;
                    case 3:
                        if (AntiMalManager.this.mStatusData.isDataReported) {
                            AntiMalManager.this.closeFeature();
                        }
                        AntiMalManager.this.mStatusData.mAntiMalStatus = 5;
                        AntiMalManager.this.writeMalStatusFile(AntiMalManager.this.malStatusPath);
                        HwLog.i(AntiMalManager.TAG, "MSG_TYPE_ALERT_HANDLED alert has been handled");
                        break;
                    case 4:
                        AntiMalManager.this.reportAntiMalDataToBD();
                        AntiMalManager.this.writeMalStatusFile(AntiMalManager.this.malStatusPath);
                        break;
                    default:
                        HwLog.e(AntiMalManager.TAG, "obtain error message");
                        break;
                }
            }
        };
    }

    public boolean isInitialed() {
        return this.mContext != null ? this.mStatusData.isAntiMalOpen : false;
    }

    public boolean needCollectData() {
        return this.mStatusData != null && this.mStatusData.mAntiMalStatus == 0;
    }

    private void initFilePath() {
        this.antimalPathEmui41 = MalwareConst.DATA_HSMMANAGER_PATH_EMUI41 + MalwareConst.ANTI_MAL_MODULE;
        this.antimalPathEmui5 = MalwareConst.DATA_HSMMANAGER_PATH_EMUI5 + MalwareConst.ANTI_MAL_MODULE;
        this.antimalPath = this.antimalPathEmui5;
        File file = new File(this.antimalPath);
        if (!file.exists()) {
            this.oldPath = this.antimalPathEmui41;
            if (new File(this.oldPath).exists()) {
                HwLog.i(TAG, "initFilePath ota upgrade, old filepath:" + this.oldPath);
                this.needTransferData = true;
                this.statusOldPath = this.oldPath + "/" + MalwareConst.ANTI_MAL_STATUS_FILE;
            }
            if (file.mkdirs()) {
                HwLog.i(TAG, "initFilePath created " + this.antimalPath);
            }
        }
        this.malStatusPath = this.antimalPath + "/" + MalwareConst.ANTI_MAL_STATUS_FILE;
    }

    public boolean initAntiMalware() {
        if (IS_CHINA_AREA) {
            this.mStatusData = new StatusData();
            initFilePath();
            if (this.needTransferData) {
                readMalStatusFile(this.statusOldPath);
                writeMalStatusFile(this.malStatusPath);
            } else {
                readMalStatusFile(this.malStatusPath);
            }
            this.mConfig = new AntiMalConfig(this.mContext);
            this.mConfig.init(this.mConfigCallback);
            if (!this.mConfig.readConfigXml()) {
                this.mConfig.mCfgFetureSwitch = this.mStatusData.isAntiMalOpen;
                this.isConfigXmlReaded = false;
            }
            HwLog.i(TAG, "initAntiMalware isAlertDisable:" + isAlertDisable + " " + this.mConfig.toString());
            if (!this.mStatusData.isAntiMalOpen || (this.isConfigXmlReaded && !this.mConfig.mCfgFetureSwitch)) {
                HwLog.i(TAG, "initAntiMalware the feature is closed");
                this.mConfig.unregisterConfigReceiver();
                if (this.mStatusData.isAntiMalOpen) {
                    this.mStatusData.isAntiMalOpen = false;
                    writeMalStatusFile(this.malStatusPath);
                }
                return false;
            }
            this.mUserBeMgr = new UserBehaviorManager(this.mContext);
            this.mUserBeMgr.init(needCollectData(), this.needTransferData, this.antimalPath, this.oldPath);
            this.mMalDataMgr = new MalDataManager(this.mContext);
            this.mMalDataMgr.init(this.mStatusData.hasInstalledMal, this.needTransferData, this.antimalPath, this.oldPath);
            if (this.mStatusData.mAntiMalStatus < 5) {
                registerScreenReceiver();
            }
            if (this.mStatusData.mAntiMalStatus == 4) {
                if (!getAlertResult(this.mContext)) {
                    this.needAlertAgain = true;
                } else if (UserAgreementHelper.getUserAgreementState(this.mContext)) {
                    obtainMessage(3);
                } else {
                    this.needAlertAgain = true;
                }
            }
            registerPushService();
            HwLog.i(TAG, "initAntiMalware success");
            return true;
        }
        HwLog.i(TAG, "initAntiMalware not in cn region");
        return false;
    }

    public void insertAppInfo(MalAppInfo app) {
        if (this.mMalDataMgr != null) {
            this.mMalDataMgr.insertAppInfo(app, this.mConfig, this.mUserBeMgr);
        }
    }

    private synchronized void writeMalStatusFile(String path) {
        FileNotFoundException fnfe;
        Object outStream;
        Object obj;
        IOException ioe;
        Exception e;
        Throwable th;
        if (TextUtils.isEmpty(path)) {
            HwLog.e(TAG, "writeMalStatusFile path is null");
        } else if (this.mStatusData == null) {
            HwLog.e(TAG, "writeMalStatusFile mStatusData is null");
        } else {
            AutoCloseable autoCloseable = null;
            AutoCloseable autoCloseable2 = null;
            try {
                FileOutputStream fstr = new FileOutputStream(path, false);
                try {
                    BufferedOutputStream outStream2 = new BufferedOutputStream(fstr);
                    try {
                        XmlSerializer serializer = Xml.newSerializer();
                        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                        serializer.setOutput(outStream2, "UTF-8");
                        serializer.startDocument("UTF-8", Boolean.valueOf(true));
                        StatusData.writeStatusDataInfo(serializer, this.mStatusData);
                        serializer.endDocument();
                        outStream2.flush();
                        fstr.flush();
                        IoUtils.closeQuietly(outStream2);
                        IoUtils.closeQuietly(fstr);
                        FileOutputStream fileOutputStream = fstr;
                    } catch (FileNotFoundException e2) {
                        fnfe = e2;
                        outStream = outStream2;
                        obj = fstr;
                        HwLog.e(TAG, "writeMalStatusFile FileNotFoundException:" + fnfe);
                        IoUtils.closeQuietly(autoCloseable2);
                        IoUtils.closeQuietly(autoCloseable);
                    } catch (IOException e3) {
                        ioe = e3;
                        outStream = outStream2;
                        obj = fstr;
                        HwLog.e(TAG, "writeMalStatusFile IOException:" + ioe);
                        IoUtils.closeQuietly(autoCloseable2);
                        IoUtils.closeQuietly(autoCloseable);
                    } catch (Exception e4) {
                        e = e4;
                        outStream = outStream2;
                        obj = fstr;
                        try {
                            HwLog.e(TAG, "writeMalStatusFile Exception:" + e);
                            IoUtils.closeQuietly(autoCloseable2);
                            IoUtils.closeQuietly(autoCloseable);
                        } catch (Throwable th2) {
                            th = th2;
                            IoUtils.closeQuietly(autoCloseable2);
                            IoUtils.closeQuietly(autoCloseable);
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        outStream = outStream2;
                        obj = fstr;
                        IoUtils.closeQuietly(autoCloseable2);
                        IoUtils.closeQuietly(autoCloseable);
                        throw th;
                    }
                } catch (FileNotFoundException e5) {
                    fnfe = e5;
                    obj = fstr;
                    HwLog.e(TAG, "writeMalStatusFile FileNotFoundException:" + fnfe);
                    IoUtils.closeQuietly(autoCloseable2);
                    IoUtils.closeQuietly(autoCloseable);
                } catch (IOException e6) {
                    ioe = e6;
                    obj = fstr;
                    HwLog.e(TAG, "writeMalStatusFile IOException:" + ioe);
                    IoUtils.closeQuietly(autoCloseable2);
                    IoUtils.closeQuietly(autoCloseable);
                } catch (Exception e7) {
                    e = e7;
                    obj = fstr;
                    HwLog.e(TAG, "writeMalStatusFile Exception:" + e);
                    IoUtils.closeQuietly(autoCloseable2);
                    IoUtils.closeQuietly(autoCloseable);
                } catch (Throwable th4) {
                    th = th4;
                    obj = fstr;
                    IoUtils.closeQuietly(autoCloseable2);
                    IoUtils.closeQuietly(autoCloseable);
                    throw th;
                }
            } catch (FileNotFoundException e8) {
                fnfe = e8;
                HwLog.e(TAG, "writeMalStatusFile FileNotFoundException:" + fnfe);
                IoUtils.closeQuietly(autoCloseable2);
                IoUtils.closeQuietly(autoCloseable);
            } catch (IOException e9) {
                ioe = e9;
                HwLog.e(TAG, "writeMalStatusFile IOException:" + ioe);
                IoUtils.closeQuietly(autoCloseable2);
                IoUtils.closeQuietly(autoCloseable);
            } catch (Exception e10) {
                e = e10;
                HwLog.e(TAG, "writeMalStatusFile Exception:" + e);
                IoUtils.closeQuietly(autoCloseable2);
                IoUtils.closeQuietly(autoCloseable);
            }
        }
    }

    private void readMalStatusFile(String path) {
        FileNotFoundException fnfe;
        XmlPullParserException xppe;
        IOException ioe;
        Throwable th;
        if (TextUtils.isEmpty(path)) {
            HwLog.e(TAG, "readMalStatusFile path is null");
            return;
        }
        File malStatusXml = new File(path);
        if (malStatusXml.exists()) {
            if (this.mStatusData == null) {
                this.mStatusData = new StatusData();
            }
            AutoCloseable autoCloseable = null;
            try {
                XmlPullParser xmlParser = Xml.newPullParser();
                FileInputStream inStream = new FileInputStream(malStatusXml);
                try {
                    xmlParser.setInput(inStream, "UTF-8");
                    for (int eventType = xmlParser.getEventType(); eventType != 1; eventType = xmlParser.next()) {
                        switch (eventType) {
                            case 0:
                                HwLog.i(TAG, "Start read the xml file");
                                break;
                            case 2:
                                if (!"status".equals(xmlParser.getName())) {
                                    break;
                                }
                                this.mStatusData = StatusData.readStatusDataInfo(xmlParser);
                                break;
                            case 3:
                                HwLog.i(TAG, "Finish reading the xml file");
                                break;
                            default:
                                break;
                        }
                    }
                    IoUtils.closeQuietly(inStream);
                } catch (FileNotFoundException e) {
                    fnfe = e;
                    autoCloseable = inStream;
                } catch (XmlPullParserException e2) {
                    xppe = e2;
                    autoCloseable = inStream;
                } catch (IOException e3) {
                    ioe = e3;
                    autoCloseable = inStream;
                } catch (Throwable th2) {
                    th = th2;
                    Object inStream2 = inStream;
                }
            } catch (FileNotFoundException e4) {
                fnfe = e4;
                try {
                    HwLog.e(TAG, "readMalStatusFile FileNotFoundException:" + fnfe);
                    IoUtils.closeQuietly(autoCloseable);
                    HwLog.i(TAG, this.mStatusData.toString());
                    return;
                } catch (Throwable th3) {
                    th = th3;
                    IoUtils.closeQuietly(autoCloseable);
                    throw th;
                }
            } catch (XmlPullParserException e5) {
                xppe = e5;
                HwLog.e(TAG, "readMalStatusFile XmlPullParserException:" + xppe);
                IoUtils.closeQuietly(autoCloseable);
                HwLog.i(TAG, this.mStatusData.toString());
                return;
            } catch (IOException e6) {
                ioe = e6;
                HwLog.e(TAG, "readMalStatusFile IOException:" + ioe);
                IoUtils.closeQuietly(autoCloseable);
                HwLog.i(TAG, this.mStatusData.toString());
                return;
            }
            HwLog.i(TAG, this.mStatusData.toString());
            return;
        }
        HwLog.i(TAG, "readMalStatusFile file:" + malStatusXml + " is not exist.");
    }

    private void registerScreenReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        filter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.mContext.registerReceiver(this.mScreenRecevier, filter);
        this.isReceiverRegistered = true;
    }

    private void unregisterScreenReceiver() {
        if (this.isReceiverRegistered) {
            this.mContext.unregisterReceiver(this.mScreenRecevier);
            this.isReceiverRegistered = false;
        }
        if (this.mThread != null) {
            this.mThread.quit();
        }
    }

    private void obtainMessage(int type) {
        Message msg = Message.obtain();
        msg.what = type;
        this.mHandler.sendMessage(msg);
    }

    private boolean isWifiConnected() {
        State wifiState = null;
        boolean result = false;
        NetworkInfo info = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getNetworkInfo(1);
        if (info != null) {
            wifiState = info.getState();
        }
        if (wifiState != null && State.CONNECTED == wifiState) {
            result = true;
        }
        HwLog.i(TAG, "isWifiConnected result:" + result);
        return result;
    }

    private boolean getPowerStatus() {
        boolean isCharging = true;
        Intent batteryStatus = this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        if (batteryStatus == null) {
            return false;
        }
        int status = batteryStatus.getIntExtra("status", -1);
        if (!(status == 2 || status == 5)) {
            isCharging = false;
        }
        return isCharging;
    }

    private boolean getAlertResult(Context context) {
        return context.getSharedPreferences(MalwareConst.ANTIMAL_ALERT_RESULT, 4).getBoolean(MalwareConst.ALERT_RESULT, false);
    }

    private void registerPushService() {
        if (CloudSwitchHelper.isCloudEnabled() && !Utility.isTokenRegistered(this.mContext)) {
            HwLog.i(TAG, "registerPushService");
            CustomTaskHandler.getInstance(this.mContext).removeMessages(1);
            CustomTaskHandler.getInstance(this.mContext).sendEmptyMessageDelayed(1, 60000);
        }
    }

    private void doDetection() {
        if (this.mMalDataMgr == null || this.mUserBeMgr == null || this.mStatusData == null) {
            HwLog.e(TAG, "doAlert input param is null.");
            return;
        }
        this.mStatusData.hasInstalledMal = this.mMalDataMgr.analyze(this.mConfig);
        this.mStatusData.hasOwnerBehavior = this.mUserBeMgr.analyze();
        if (this.mStatusData.hasInstalledMal && isWifiConnected()) {
            reportAntiMalDataToBD();
        }
        writeMalStatusFile(this.malStatusPath);
        HwLog.i(TAG, "hasInstalledMal:" + this.mStatusData.hasInstalledMal + "\n" + this.mMalDataMgr.getBaseDataString());
        this.mUserBeMgr.unregisterUserBeReceiver();
    }

    private void doAlert() {
        if (isAlertDisable) {
            HwLog.i(TAG, "doAlert not support alert function.");
        } else if (this.mMalDataMgr == null || this.mConfig == null || this.mStatusData == null) {
            HwLog.e(TAG, "doAlert input param is null.");
        } else {
            if (this.mStatusData.hasInstalledMal && this.mMalDataMgr.getHomeStatus()) {
                alertUser();
            } else if (!this.mStatusData.hasInstalledMal || this.mMalDataMgr.getSetPswdStatus() || this.mStatusData.hasOwnerBehavior || ((this.mMalDataMgr.getDevMgrAppCnt() <= this.mConfig.mCfgMaxDevMgrCount || !this.mMalDataMgr.getLauncherAppStatus()) && !this.mMalDataMgr.isInBlackList(this.mConfig.mCfgBlackMatchCount))) {
                HwLog.i(TAG, "doAlert no need to alert user.");
            } else {
                alertUser();
            }
            writeMalStatusFile(this.malStatusPath);
        }
    }

    private void reportAntiMalDataToBD() {
        if (this.mMalDataMgr == null || this.mStatusData == null) {
            HwLog.e(TAG, "reportAntiMalDataToBD mMalDataMgr or mStatusData is null.");
        } else if (this.mStatusData.isDataReported) {
            HwLog.i(TAG, "reportAntiMalDataToBD already reported.");
        } else {
            if (this.mMalDataMgr.report(this.mUserBeMgr)) {
                if (this.mStatusData.mAntiMalStatus < 2) {
                    this.mStatusData.mAntiMalStatus = 2;
                }
                this.mStatusData.isDataReported = true;
            }
        }
    }

    private void alertUser() {
        if (this.mMalDataMgr == null) {
            HwLog.e(TAG, "alertUser mMalDataMgr is null.");
            return;
        }
        List<String> malAppList = this.mMalDataMgr.getMalAppList();
        if (malAppList.size() == 0) {
            HwLog.e(TAG, "alertUser malAppList size is 0.");
            return;
        }
        String pkg = AntiMalUtils.getUnusedAppName(this.mContext, malAppList);
        Bundle bundle = new Bundle();
        bundle.putSerializable(MalwareConst.PACKAGE_LIST, (Serializable) malAppList);
        new AntimalwareNotification(this.mContext, malAppList.size(), getLableFromPm(pkg)).showNotification(bundle);
        this.mStatusData.mAntiMalStatus = 4;
        this.needAlertAgain = false;
    }

    private void closeFeature() {
        this.mStatusData.isAntiMalOpen = false;
        unregisterScreenReceiver();
        this.mUserBeMgr.unregisterUserBeReceiver();
        this.mConfig.unregisterConfigReceiver();
        writeMalStatusFile(this.malStatusPath);
    }

    private boolean isUserUnlocked() {
        UserManager um = (UserManager) this.mContext.getSystemService(UserManager.class);
        if (um == null) {
            return false;
        }
        return um.isUserUnlocked();
    }

    private String getLableFromPm(String pkgName) {
        PackageManager pm = this.mContext.getPackageManager();
        try {
            return pm.getApplicationInfo(pkgName, 8192).loadLabel(pm).toString().replaceAll("\\s", " ").trim();
        } catch (NameNotFoundException e) {
            return pkgName;
        }
    }

    public AntiMalConfig getAntiMalConfig() {
        return this.mConfig;
    }
}

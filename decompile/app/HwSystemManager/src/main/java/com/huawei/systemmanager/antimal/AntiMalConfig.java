package com.huawei.systemmanager.antimal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Xml;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.push.PushResponse;
import com.huawei.systemmanager.util.HwLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class AntiMalConfig {
    public static final String ACTION_ANTIMAL_ALERT = "com.huawei.systemmanager.ANTIMAL_ALERT_CMD_ACTION";
    public static final String ACTION_ANTIMAL_GET_BLACKLIST = "com.huawei.systemmanager.ANTIMAL_BLACKLIST_UPDATE_ACTION";
    public static final String ACTION_ANTIMAL_GET_CONFIG = "com.huawei.systemmanager.ANTIMAL_CONFIG_UPDATE_ACTION";
    public static final String PUSH_TYPE_BLACKLIST = "antimal_blacklist";
    public static final String PUSH_TYPE_CMD_ALERT = "antimal_alert";
    public static final String PUSH_TYPE_CONFIG = "antimal_config";
    private static final String TAG = "AntiMalConfig";
    private boolean isReceiverRegistered = false;
    public int mCfgBlackMatchCount = 8;
    private AntiMalConfigCallback mCfgCallback = null;
    public long mCfgFeatureOpenTime = MalwareConst.FEATURE_OPEN_TIME;
    public boolean mCfgFetureSwitch = true;
    public long mCfgMaxAlertTime = MalwareConst.MAX_ALERT_TIME;
    public long mCfgMaxAvgSpaceTime = MalwareConst.MAX_AVERAGE_SPACE_TIME;
    public long mCfgMaxDetectTime = MalwareConst.MAX_DETECT_TIME;
    public int mCfgMaxDevMgrCount = 3;
    public long mCfgMaxInsSpaceTime = MalwareConst.MAX_INSTALL_SPACE_TIME;
    public int mCfgMinInsSameCount = 10;
    public int mCfgThermalValue = 40;
    private BroadcastReceiver mConfigRecevier = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (!Utility.checkBroadcast(context, intent)) {
                return;
            }
            if (AntiMalConfig.this.mCfgCallback == null) {
                HwLog.e(AntiMalConfig.TAG, "mConfigRecevier mCfgCallback is null");
                return;
            }
            String type = intent.getStringExtra(PushResponse.PUSH_TYPE_FIELD);
            HwLog.i(AntiMalConfig.TAG, "pushType:" + type);
            if (intent.getAction().equals(AntiMalConfig.ACTION_ANTIMAL_ALERT) && AntiMalConfig.PUSH_TYPE_CMD_ALERT.equals(type)) {
                AntiMalConfig.this.mCfgCallback.onAlert();
            } else if (intent.getAction().equals(AntiMalConfig.ACTION_ANTIMAL_GET_CONFIG) && AntiMalConfig.PUSH_TYPE_CONFIG.equals(type)) {
                AntiMalConfig.this.mCfgCallback.onGetConfig();
            } else if (intent.getAction().equals(AntiMalConfig.ACTION_ANTIMAL_GET_BLACKLIST) && AntiMalConfig.PUSH_TYPE_BLACKLIST.equals(type)) {
                AntiMalConfig.this.mCfgCallback.onGetBlacklist();
            }
        }
    };
    private Context mContext = null;
    private String malConfigPath = null;

    public static abstract class AntiMalConfigCallback {
        public void onAlert() {
        }

        public void onGetConfig() {
        }

        public void onGetBlacklist() {
        }
    }

    public AntiMalConfig(Context context) {
        this.mContext = context;
    }

    public void init(AntiMalConfigCallback callback) {
        this.mCfgCallback = callback;
        this.malConfigPath = this.mContext.getCacheDir() + "/" + MalwareConst.ANTI_MAL_MODULE + "/" + MalwareConst.ANTI_MAL_CONFIG_FILE;
        if (!this.isReceiverRegistered) {
            registerConfigReceiver();
        }
    }

    public String toString() {
        return "AntiMalConfig mCfgFeatureOpenTime:" + this.mCfgFeatureOpenTime + " mCfgMaxDetectTime:" + this.mCfgMaxDetectTime + " mCfgMaxAlertTime:" + this.mCfgMaxAlertTime + " mCfgMaxDevMgrCount:" + this.mCfgMaxDevMgrCount + " mCfgMaxInsSpaceTime:" + this.mCfgMaxInsSpaceTime + " mCfgMaxAvgSpaceTime:" + this.mCfgMaxAvgSpaceTime + " mCfgMinInsSameCount:" + this.mCfgMinInsSameCount + " mCfgFetureSwitch:" + this.mCfgFetureSwitch + " mCfgBlackMatchCount:" + this.mCfgBlackMatchCount;
    }

    private void registerConfigReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_ANTIMAL_ALERT);
        filter.addAction(ACTION_ANTIMAL_GET_CONFIG);
        filter.addAction(ACTION_ANTIMAL_GET_BLACKLIST);
        this.mContext.registerReceiver(this.mConfigRecevier, filter, "com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
        this.isReceiverRegistered = true;
    }

    public void unregisterConfigReceiver() {
        if (this.isReceiverRegistered) {
            this.mContext.unregisterReceiver(this.mConfigRecevier);
            this.isReceiverRegistered = false;
        }
    }

    public boolean readConfigXml() {
        FileNotFoundException fnfe;
        XmlPullParserException xppe;
        IOException e;
        Throwable th;
        boolean result = false;
        File xmlFile = new File(this.malConfigPath);
        if (xmlFile.exists()) {
            HwLog.i(TAG, "readConfigXml filePath:" + this.malConfigPath);
            AutoCloseable autoCloseable = null;
            try {
                FileInputStream inStream = new FileInputStream(xmlFile);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(inStream, "UTF-8");
                    for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                        switch (eventType) {
                            case 2:
                                String tagName = parser.getName();
                                if (!TextUtils.equals(tagName, MalwareConst.CONFIG_FEATURE_SWITCH)) {
                                    if (!TextUtils.equals(tagName, MalwareConst.CONFIG_FEATURE_OPEN_TIME)) {
                                        if (!TextUtils.equals(tagName, MalwareConst.CONFIG_DETECT_TIME)) {
                                            if (!TextUtils.equals(tagName, MalwareConst.CONFIG_ALERT_TIME)) {
                                                if (!TextUtils.equals(tagName, MalwareConst.CONFIG_AVERAGE_SPACE_TIME)) {
                                                    if (!TextUtils.equals(tagName, MalwareConst.CONFIG_SINGLE_SPACE_TIME)) {
                                                        if (!TextUtils.equals(tagName, MalwareConst.CONFIG_DEV_MGR_COUNT)) {
                                                            if (!TextUtils.equals(tagName, MalwareConst.CONFIG_MIN_MAL_COUNT)) {
                                                                if (!TextUtils.equals(tagName, MalwareConst.CONFIG_MIN_BLACK_MATCH_COUNT)) {
                                                                    if (!TextUtils.equals(tagName, MalwareConst.CONFIG_FEATURE_THERMAL_VALUE)) {
                                                                        break;
                                                                    }
                                                                    this.mCfgThermalValue = Integer.parseInt(parser.nextText());
                                                                    break;
                                                                }
                                                                this.mCfgBlackMatchCount = Integer.parseInt(parser.nextText());
                                                                break;
                                                            }
                                                            this.mCfgMinInsSameCount = Integer.parseInt(parser.nextText());
                                                            break;
                                                        }
                                                        this.mCfgMaxDevMgrCount = Integer.parseInt(parser.nextText());
                                                        break;
                                                    }
                                                    this.mCfgMaxInsSpaceTime = Long.parseLong(parser.nextText());
                                                    break;
                                                }
                                                this.mCfgMaxAvgSpaceTime = Long.parseLong(parser.nextText());
                                                break;
                                            }
                                            this.mCfgMaxAlertTime = Long.parseLong(parser.nextText());
                                            break;
                                        }
                                        this.mCfgMaxDetectTime = Long.parseLong(parser.nextText());
                                        break;
                                    }
                                    this.mCfgFeatureOpenTime = Long.parseLong(parser.nextText());
                                    break;
                                }
                                boolean z;
                                if (Integer.parseInt(parser.nextText()) == 0) {
                                    z = false;
                                } else {
                                    z = true;
                                }
                                this.mCfgFetureSwitch = z;
                                break;
                            default:
                                break;
                        }
                    }
                    result = true;
                    IoUtils.closeQuietly(inStream);
                } catch (FileNotFoundException e2) {
                    fnfe = e2;
                    autoCloseable = inStream;
                } catch (XmlPullParserException e3) {
                    xppe = e3;
                    autoCloseable = inStream;
                } catch (IOException e4) {
                    e = e4;
                    autoCloseable = inStream;
                } catch (Throwable th2) {
                    th = th2;
                    autoCloseable = inStream;
                }
            } catch (FileNotFoundException e5) {
                fnfe = e5;
                try {
                    HwLog.e(TAG, "readConfigXml FileNotFoundException:" + fnfe);
                    IoUtils.closeQuietly(autoCloseable);
                    return result;
                } catch (Throwable th3) {
                    th = th3;
                    IoUtils.closeQuietly(autoCloseable);
                    throw th;
                }
            } catch (XmlPullParserException e6) {
                xppe = e6;
                HwLog.e(TAG, "readConfigXml XmlPullParserException:" + xppe);
                IoUtils.closeQuietly(autoCloseable);
                return result;
            } catch (IOException e7) {
                e = e7;
                HwLog.e(TAG, "readConfigXml IOException:" + e);
                IoUtils.closeQuietly(autoCloseable);
                return result;
            }
            return result;
        }
        HwLog.i(TAG, "readConfigXml file:" + this.malConfigPath + " is not exist.");
        return false;
    }
}

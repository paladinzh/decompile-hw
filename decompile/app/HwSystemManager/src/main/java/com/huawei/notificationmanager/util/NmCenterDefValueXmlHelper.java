package com.huawei.notificationmanager.util;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import android.util.Xml;
import com.huawei.notificationmanager.common.ConstValues;
import com.huawei.systemmanager.comm.wrapper.DiskFile;
import com.huawei.systemmanager.customize.AbroadUtils;
import com.huawei.systemmanager.customize.CustomizeManager;
import com.huawei.systemmanager.rainbow.db.CloudDBAdapter;
import com.huawei.systemmanager.rainbow.db.bean.NotificationConfigBean;
import com.huawei.systemmanager.util.HwLog;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class NmCenterDefValueXmlHelper {
    private static final String ATTR_NAME = "name";
    private static final String TAG = "NmCenterDefValueXmlHelper";
    private static final String TAG_PACKAGE = "package";
    private static final String TAG_SYSTEM_APP = "systemapp";
    private static final String TAG_THIRDPARTY_APP = "thirdpartyapp";
    private static final String XML_ASSET = "notification/hsm_notificationcenter_defvalue.xml";
    private static final String XML_DISK_CUST = CustomizeManager.composeCustFileName("xml/hsm/notification/hsm_notificationcenter_defvalue.xml");
    private Map<String, ContentValues> mCachedConfigs = null;

    public ContentValues getCloudPreferedDefaultConfig(Context ctx, String pkgName) {
        ContentValues result;
        NotificationConfigBean cloudBean = CloudDBAdapter.getInstance(ctx).getSingleNotificationConfig(pkgName);
        if (cloudBean != null) {
            result = new ContentValues();
            result.put(ConstValues.NOTIFICATION_CANFORBID, Integer.valueOf(getSubCfgValueFromCloudValue(cloudBean.getCanForbiddenValue())));
            result.put(ConstValues.NOTIFICATION_CFG, Integer.valueOf(Integer.parseInt(cloudBean.getNotificationCfg())));
            result.put(ConstValues.NOTIFICATION_STATUSBAR_CFG, Integer.valueOf(getSubCfgValueFromCloudValue(cloudBean.getStatusbarCfg())));
            result.put(ConstValues.NOTIFICATION_LOCKSCREEN_CFG, Integer.valueOf(getSubCfgValueFromCloudValue(cloudBean.getLockscreenCfg())));
            result.put(ConstValues.NOTIFICATION_HEADSUP_CFG, Integer.valueOf(getSubCfgValueFromCloudValue(cloudBean.getHeadsupCfg())));
        } else {
            result = getDefaultConfig(ctx, pkgName);
        }
        putLockScreenCfg(result);
        return result;
    }

    public void putLockScreenCfg(ContentValues result) {
        if (AbroadUtils.isAbroad() && result != null) {
            result.put(ConstValues.NOTIFICATION_LOCKSCREEN_CFG, Integer.valueOf(1));
            HwLog.d(TAG, "lockscreen=SWITCH_MODE_OPEN");
        }
    }

    private synchronized ContentValues getDefaultConfig(Context ctx, String pkg) {
        if (TextUtils.isEmpty(pkg)) {
            return null;
        }
        if (this.mCachedConfigs == null) {
            HwLog.i(TAG, "mCachedConfigs = null, init.");
            this.mCachedConfigs = getConfigs(ctx);
        }
        return (ContentValues) this.mCachedConfigs.get(pkg);
    }

    private Map<String, ContentValues> getConfigs(Context ctx) {
        HwLog.d(TAG, "getConfigs: Starts");
        Map<String, ContentValues> results = new HashMap();
        getAssertConfigs(ctx, results, XML_ASSET);
        getFileConfigs(results, XML_DISK_CUST);
        HwLog.d(TAG, "getConfigs: Ends. Count = " + results.size());
        return results;
    }

    private static void getAssertConfigs(Context ctx, Map<String, ContentValues> results, String assertPath) {
        InputStream inputStream = null;
        try {
            inputStream = ctx.getAssets().open(assertPath);
            getConfigsInner(results, inputStream);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e32) {
                    e32.printStackTrace();
                }
            }
        }
    }

    private static void getFileConfigs(Map<String, ContentValues> results, String filePath) {
        Exception e;
        Throwable th;
        if (DiskFile.fileExist(filePath)) {
            HwLog.i(TAG, "getFileConfigs: Apply configs: filePath = " + filePath);
            InputStream inputStream = null;
            try {
                InputStream inStream = new FileInputStream(filePath);
                try {
                    getConfigsInner(results, inStream);
                    if (inStream != null) {
                        try {
                            inStream.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    inputStream = inStream;
                } catch (Exception e3) {
                    e = e3;
                    inputStream = inStream;
                    try {
                        e.printStackTrace();
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e22) {
                                e22.printStackTrace();
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e222) {
                                e222.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    inputStream = inStream;
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    throw th;
                }
            } catch (Exception e4) {
                e = e4;
                e.printStackTrace();
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }
    }

    private static void getConfigsInner(Map<String, ContentValues> results, InputStream inStream) {
        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setInput(inStream, null);
            parseXmlInner(results, xmlPullParser);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (NumberFormatException e2) {
            e2.printStackTrace();
        } catch (XmlPullParserException e3) {
            e3.printStackTrace();
        } catch (IOException e4) {
            e4.printStackTrace();
        } catch (IndexOutOfBoundsException e5) {
            e5.printStackTrace();
        } catch (Exception e6) {
            e6.printStackTrace();
        }
    }

    private static void parseXmlInner(Map<String, ContentValues> results, XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        if (xmlPullParser != null) {
            int index = 0;
            boolean isSysApp = false;
            int type;
            do {
                type = xmlPullParser.next();
                switch (type) {
                    case 2:
                        String tag = xmlPullParser.getName();
                        if (!TAG_SYSTEM_APP.equals(tag)) {
                            if (!TAG_THIRDPARTY_APP.equals(tag)) {
                                if ("package".equals(tag)) {
                                    int i;
                                    if (isSysApp) {
                                        i = index;
                                        index++;
                                    } else {
                                        i = 10000;
                                    }
                                    parseAppConfig(results, xmlPullParser, i);
                                    break;
                                }
                            }
                            isSysApp = false;
                            break;
                        }
                        isSysApp = true;
                        break;
                        break;
                }
            } while (type != 1);
        }
    }

    private static void parseAppConfig(Map<String, ContentValues> results, XmlPullParser xmlPullParser, int index) throws XmlPullParserException, IOException {
        int i = 0;
        String pkgName = xmlPullParser.getAttributeValue(null, "name");
        if (TextUtils.isEmpty(pkgName)) {
            HwLog.w(TAG, "parseAppConfig: Find an invalid config, skip");
            return;
        }
        int i2;
        ContentValues value = new ContentValues();
        String cfgStr = xmlPullParser.getAttributeValue(null, ConstValues.NOTIFICATION_CFG);
        int cfg = 2;
        if (!TextUtils.isEmpty(cfgStr)) {
            cfg = Integer.parseInt(cfgStr);
        }
        if (index < 10000) {
            int canForbid;
            if ("false".equals(xmlPullParser.getAttributeValue(null, ConstValues.NOTIFICATION_CANFORBID))) {
                canForbid = 0;
            } else {
                canForbid = 1;
            }
            if (canForbid == 0 && cfg == 0) {
                HwLog.w(TAG, "parseAppConfig: Find a conflict cfg : FALSE == canForbid && FORBID == cfg ,correct cfg to ALLOW , pkgName = " + pkgName);
                cfg = 1;
            }
            value.put(ConstValues.NOTIFICATION_CANFORBID, Integer.valueOf(canForbid));
        } else {
            value.put(ConstValues.NOTIFICATION_CANFORBID, Integer.valueOf(1));
        }
        value.put(ConstValues.NOTIFICATION_CFG, Integer.valueOf(cfg));
        cfgStr = xmlPullParser.getAttributeValue(null, ConstValues.NOTIFICATION_STATUSBAR_CFG);
        String str = ConstValues.NOTIFICATION_STATUSBAR_CFG;
        if ("false".equals(cfgStr)) {
            i2 = 0;
        } else {
            i2 = 1;
        }
        value.put(str, Integer.valueOf(i2));
        cfgStr = xmlPullParser.getAttributeValue(null, ConstValues.NOTIFICATION_LOCKSCREEN_CFG);
        str = ConstValues.NOTIFICATION_LOCKSCREEN_CFG;
        if ("false".equals(cfgStr)) {
            i2 = 0;
        } else {
            i2 = 1;
        }
        value.put(str, Integer.valueOf(i2));
        cfgStr = xmlPullParser.getAttributeValue(null, ConstValues.NOTIFICATION_HEADSUP_CFG);
        String str2 = ConstValues.NOTIFICATION_HEADSUP_CFG;
        if (!"false".equals(cfgStr)) {
            i = 1;
        }
        value.put(str2, Integer.valueOf(i));
        value.put(ConstValues.NOTIFICATION_INDEX, Integer.valueOf(index));
        results.put(pkgName, value);
    }

    private int getSubCfgValueFromCloudValue(boolean allow) {
        return allow ? 1 : 0;
    }
}

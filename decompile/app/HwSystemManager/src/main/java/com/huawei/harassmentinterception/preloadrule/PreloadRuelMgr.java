package com.huawei.harassmentinterception.preloadrule;

import android.content.Context;
import com.huawei.harassmentinterception.common.CommonObject.SmsIntentWrapper;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.util.HwLog;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class PreloadRuelMgr {
    private static final String TAG = "PreloadMgr";
    private static PreloadRuelMgr sInstance = null;
    private Context mContext = null;
    private ArrayList<PreloadRuleObject> mPreloadList = new ArrayList();
    private int mPreloadType = 0;

    private PreloadRuelMgr(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public static synchronized PreloadRuelMgr getInstance(Context context) {
        PreloadRuelMgr preloadRuelMgr;
        synchronized (PreloadRuelMgr.class) {
            if (sInstance == null) {
                sInstance = new PreloadRuelMgr(context);
                sInstance.loadPreloadInfo();
            }
            preloadRuelMgr = sInstance;
        }
        return preloadRuelMgr;
    }

    public boolean shouldCheck(SmsIntentWrapper smsIntentWrapper) {
        if (smsIntentWrapper == null) {
            HwLog.w(TAG, "shouldCheck: Invalid param");
            return false;
        } else if (Utility.isNullOrEmptyList(this.mPreloadList)) {
            return true;
        } else {
            String phoneNumber = smsIntentWrapper.getSmsMsgInfo().getPhone();
            for (PreloadRuleObject obj : this.mPreloadList) {
                if (obj.isMatchWhiteList(phoneNumber)) {
                    return false;
                }
            }
            return true;
        }
    }

    private void loadPreloadInfo() {
        switch (this.mPreloadType) {
            case 0:
                loadLocalPreloadInfo();
                break;
            case 1:
                loadCustPreloadInfo();
                break;
            case 2:
                loadCloudPreloadInfo();
                break;
        }
        if (Utility.isNullOrEmptyList(this.mPreloadList)) {
            HwLog.d(TAG, "loadPreloadInfo: No preload rule");
        }
    }

    private void loadLocalPreloadInfo() {
        InputStream inputStream = null;
        try {
            inputStream = this.mContext.getAssets().open("hsm_harassmentinterception_rule.xml");
            readPreloadInfo(inputStream);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    HwLog.e(TAG, "loadLocalPreloadInfo: IOException", e);
                }
            }
        } catch (IOException e2) {
            HwLog.e(TAG, "loadLocalPreloadInfo: IOException", e2);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e22) {
                    HwLog.e(TAG, "loadLocalPreloadInfo: IOException", e22);
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e222) {
                    HwLog.e(TAG, "loadLocalPreloadInfo: IOException", e222);
                }
            }
        }
    }

    private void loadCustPreloadInfo() {
    }

    private void loadCloudPreloadInfo() {
    }

    private void readPreloadInfo(InputStream inStream) throws Throwable {
        XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
        parser.setInput(inStream, "UTF-8");
        int nCurrentType = 0;
        for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
            switch (eventType) {
                case 2:
                    String tagName = parser.getName();
                    if (!"whitelist".equals(tagName)) {
                        if (!PreloadRuleConst.TAG_PREFIX.equals(tagName)) {
                            if (!"number".equals(tagName)) {
                                break;
                            }
                            this.mPreloadList.add(new PreloadRuleObject(nCurrentType, 1, parser.getAttributeValue(0)));
                            break;
                        }
                        this.mPreloadList.add(new PreloadRuleObject(nCurrentType, 0, parser.getAttributeValue(0)));
                        break;
                    }
                    nCurrentType = 0;
                    break;
                default:
                    break;
            }
        }
    }
}

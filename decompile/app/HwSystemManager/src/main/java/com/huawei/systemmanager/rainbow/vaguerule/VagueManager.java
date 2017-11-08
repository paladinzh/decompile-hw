package com.huawei.systemmanager.rainbow.vaguerule;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import com.huawei.systemmanager.comm.misc.CursorHelper;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.CloudVagueValues;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VagueManager {
    private static final int DEFAULT_ADDVIEW = 1;
    private static final int DEFAULT_BOOTSTARTUP = 1;
    private static final String DEFAULT_CONNECT_DATA = "true";
    private static final String DEFAULT_CONNECT_WIFI = "true";
    private static final int DEFAULT_GETAPPLIST = 2;
    private static final int DEFAULT_NOTIFICATION = 2;
    private static final int DEFAULT_NOTIFICATION_EX = 2;
    private static final int DEFAULT_PERMISSION_CFG = 16777216;
    private static final int DEFAULT_PERMISSION_CODE = 16777232;
    private static final String DEFAULT_TRUST_PERMISSION = "false";
    private static final String INTERNET_ALLOW = "true";
    private static final String LOG_TAG = "VagueManager";
    private static final int PHONE_CODE_PERMISSION = 16;
    private static final int SHORT_CUT_PERMISSION = 16777216;
    private static VagueManager sInstance = null;
    private Context mContext = null;
    private Map<String, Integer> mPkgSdkVersionMap = new HashMap();
    private List<String> mVaguePermissionKeyList = new ArrayList();
    private Map<String, VaguePermissionInfo> mVaguePermissionMap = new HashMap();
    private Object mVagueSync = new Object();

    private class CloudVagueTableChangeObserver extends ContentObserver {
        public CloudVagueTableChangeObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            VagueManager.this.updateVagueInfoInMemory();
        }
    }

    private VagueManager(Context context) {
        this.mContext = context.getApplicationContext();
        updateVagueInfoInMemory();
        HandlerThread handlerThread = new HandlerThread(LOG_TAG);
        handlerThread.start();
        this.mContext.getContentResolver().registerContentObserver(CloudVagueValues.PERMISSION_FEATURE_CONTENT_URI, true, new CloudVagueTableChangeObserver(new Handler(handlerThread.getLooper())));
    }

    public static synchronized VagueManager getInstance(Context context) {
        VagueManager vagueManager;
        synchronized (VagueManager.class) {
            if (sInstance == null && context != null) {
                sInstance = new VagueManager(context);
            }
            vagueManager = sInstance;
        }
        return vagueManager;
    }

    private void updateVagueInfoInMemory() {
        Cursor cursor = this.mContext.getContentResolver().query(CloudVagueValues.PERMISSION_OUTERVIEW_URI, null, null, null, null);
        if (CursorHelper.checkCursorValid(cursor)) {
            List<String> vaguePermissionKeyList = new ArrayList();
            Map<String, VaguePermissionInfo> vaguePermissionMap = new HashMap();
            int vaguePkgNameIndex = cursor.getColumnIndex("packageName");
            while (cursor.moveToNext()) {
                try {
                    String vaguePkgName = cursor.getString(vaguePkgNameIndex);
                    VaguePermissionInfo vaguePermissionInfo = new VaguePermissionInfo();
                    vaguePermissionInfo.parseFrom(cursor);
                    vaguePermissionMap.put(vaguePkgName, vaguePermissionInfo);
                    vaguePermissionKeyList.add(vaguePkgName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
            HwLog.d(LOG_TAG, "Get data from db finish!");
            initVaguePermissionListInOrder(vaguePermissionKeyList, vaguePermissionMap);
        }
    }

    private void initVaguePermissionListInOrder(List<String> keyList, Map<String, VaguePermissionInfo> vaguePermissionMap) {
        VagueOrderUtil comparator = new VagueOrderUtil(keyList);
        synchronized (this.mVagueSync) {
            this.mVaguePermissionMap.clear();
            this.mVaguePermissionMap.putAll(vaguePermissionMap);
            this.mVaguePermissionKeyList.clear();
            this.mVaguePermissionKeyList.addAll(keyList);
            Collections.sort(this.mVaguePermissionKeyList, comparator);
        }
    }

    private VaguePermissionInfo getVagueInfoFromMemory(String pkgName) {
        VaguePermissionInfo vaguePermissionInfo = null;
        synchronized (this.mVagueSync) {
            int lengthList = this.mVaguePermissionKeyList.size();
            for (int i = 0; i < lengthList; i++) {
                if (VaguePackageNameRule.matchRule(pkgName, (String) this.mVaguePermissionKeyList.get(i))) {
                    vaguePermissionInfo = (VaguePermissionInfo) this.mVaguePermissionMap.get(this.mVaguePermissionKeyList.get(i));
                    break;
                }
            }
        }
        return vaguePermissionInfo;
    }

    public int getAddViewPermission(String pkgName) {
        VaguePermissionInfo vaguePermissionInfo = getVagueInfoFromMemory(pkgName);
        if (vaguePermissionInfo == null) {
            return 1;
        }
        return vaguePermissionInfo.mPermissionAddview;
    }

    public int getBootStartupPermission(String pkgName) {
        VaguePermissionInfo vaguePermissionInfo = getVagueInfoFromMemory(pkgName);
        if (vaguePermissionInfo == null) {
            return 1;
        }
        return vaguePermissionInfo.mPermissionBootstartup;
    }

    public int getFetchapplistPermission(String pkgName) {
        VaguePermissionInfo vaguePermissionInfo = getVagueInfoFromMemory(pkgName);
        if (vaguePermissionInfo == null) {
            return 2;
        }
        return vaguePermissionInfo.mPermissionGetApplist;
    }

    public String getNetworkWifiPermission(String pkgName) {
        VaguePermissionInfo vaguePermissionInfo = getVagueInfoFromMemory(pkgName);
        if (vaguePermissionInfo == null) {
            return "true";
        }
        return vaguePermissionInfo.mNetworkWifi;
    }

    public String getNetworkDataPermission(String pkgName) {
        VaguePermissionInfo vaguePermissionInfo = getVagueInfoFromMemory(pkgName);
        if (vaguePermissionInfo == null) {
            return "true";
        }
        return vaguePermissionInfo.mNetworkData;
    }

    public int getNotificationPermission(String pkgName) {
        VaguePermissionInfo vaguePermissionInfo = getVagueInfoFromMemory(pkgName);
        if (vaguePermissionInfo == null) {
            return 2;
        }
        return vaguePermissionInfo.mPermissionSendNotification;
    }

    public int getNotificationExPermission(String pkgName) {
        VaguePermissionInfo vaguePermissionInfo = getVagueInfoFromMemory(pkgName);
        if (vaguePermissionInfo == null) {
            return 2;
        }
        return vaguePermissionInfo.mPermissionNotificationSignal;
    }

    public String getTrustPermission(String pkgName) {
        VaguePermissionInfo vaguePermissionInfo = getVagueInfoFromMemory(pkgName);
        if (vaguePermissionInfo == null) {
            return "false";
        }
        return vaguePermissionInfo.mPermissionTrust;
    }

    private boolean is3rdMApp(String pkgName) {
        boolean z = false;
        ApplicationInfo inf = null;
        try {
            inf = this.mContext.getPackageManager().getApplicationInfo(pkgName, 0);
        } catch (NameNotFoundException e) {
            HwLog.w(LOG_TAG, "get app info fail for:" + pkgName, e);
        } catch (Exception e2) {
            HwLog.w(LOG_TAG, "get app info fail for:" + pkgName, e2);
        }
        if (inf == null) {
            return false;
        }
        if ((inf.flags & 1) == 0 && inf.targetSdkVersion >= 22) {
            z = true;
        }
        return z;
    }

    public int getPermissionCode(String pkgName) {
        VaguePermissionInfo vaguePermissionInfo = getVagueInfoFromMemory(pkgName);
        if (vaguePermissionInfo != null) {
            return vaguePermissionInfo.mPermissionCode;
        }
        if (!is3rdMApp(pkgName)) {
            return 16777232;
        }
        HwLog.i(LOG_TAG, "3rd M app, we don't allow phone id by default");
        return 16777216;
    }

    public int getPermissionCfg(String pkgName) {
        VaguePermissionInfo vaguePermissionInfo = getVagueInfoFromMemory(pkgName);
        if (vaguePermissionInfo == null) {
            return is3rdMApp(pkgName) ? 16777216 : 16777216;
        } else {
            return vaguePermissionInfo.mPermissionCfg;
        }
    }

    public void setPkgSdkVersion(String pkg, int version) {
        synchronized (this.mVagueSync) {
            this.mPkgSdkVersionMap.put(pkg, Integer.valueOf(version));
            HwLog.i(LOG_TAG, "put sdk version of " + pkg + ", version:" + version + ", size:" + this.mPkgSdkVersionMap.size());
        }
    }

    public int getPkgSdkVersion(String pkgName) {
        int res = 0;
        ApplicationInfo inf = null;
        try {
            inf = this.mContext.getPackageManager().getApplicationInfo(pkgName, 0);
        } catch (NameNotFoundException e) {
            HwLog.w(LOG_TAG, "get app info fail for:" + pkgName, e);
        } catch (Exception e2) {
            HwLog.w(LOG_TAG, "get app info fail for:" + pkgName, e2);
        }
        if (inf != null) {
            res = inf.targetSdkVersion;
        }
        synchronized (this.mVagueSync) {
            if (this.mPkgSdkVersionMap.containsKey(pkgName)) {
                if (res == 0) {
                    res = ((Integer) this.mPkgSdkVersionMap.get(pkgName)).intValue();
                }
                this.mPkgSdkVersionMap.remove(pkgName);
            }
        }
        HwLog.i(LOG_TAG, "get sdk version of " + pkgName + ", version:" + res);
        return res;
    }
}

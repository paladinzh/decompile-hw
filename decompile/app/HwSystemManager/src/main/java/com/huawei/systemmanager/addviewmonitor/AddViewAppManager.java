package com.huawei.systemmanager.addviewmonitor;

import android.app.AppOpsManager;
import android.app.AppOpsManager.OpEntry;
import android.app.AppOpsManager.PackageOps;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.util.Log;
import com.huawei.permissionmanager.utils.CommonFunctionUtil;
import com.huawei.systemmanager.comm.grule.GRuleManager;
import com.huawei.systemmanager.comm.grule.scene.monitor.MonitorScenario;
import com.huawei.systemmanager.comm.misc.CursorHelper;
import com.huawei.systemmanager.customize.AbroadUtils;
import com.huawei.systemmanager.customize.CustomizeManager;
import com.huawei.systemmanager.customize.DefValueXmlHelper;
import com.huawei.systemmanager.rainbow.db.CloudDBAdapter;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class AddViewAppManager {
    private static final int ADD_VIEW_APP_LIST = 1;
    private static final int ADD_VIEW_ONE_APP = 0;
    private static String ASSERT_FILE = "dropzone/hsm_dropzone_default_value.xml";
    private static String CONFIG_FILE = CustomizeManager.composeCustFileName("xml/hsm/dropzone/hsm_dropzone_default_value.xml");
    private static final String TAG = "AddViewAppManager";
    private static AddViewAppManager sInstance;
    private boolean mAbroadVersion = true;
    AppOpsManager mAppOps = null;
    private Context mContext = null;
    private DefValueXmlHelper mDefault = new DefValueXmlHelper(CONFIG_FILE, ASSERT_FILE);
    private OpsWriteHandler mHandler;
    private HandlerThread mHandlerThread = new HandlerThread("ops_handler_thread");
    private List<IAppChangeListener> mListeners = new ArrayList();

    private class OpsWriteHandler extends Handler {
        public OpsWriteHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            HwLog.i(AddViewAppManager.TAG, "msg:" + msg.what);
            switch (msg.what) {
                case 0:
                    AddViewAppManager.this.setOpsMode(msg.obj);
                    return;
                case 1:
                    AddViewAppManager.this.setOpsModeList(msg.obj);
                    return;
                default:
                    return;
            }
        }
    }

    public AddViewAppManager(Context context) {
        this.mContext = context.getApplicationContext();
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mHandlerThread.start();
        this.mHandler = new OpsWriteHandler(this.mHandlerThread.getLooper());
        this.mAbroadVersion = getAbroadVersionStatus();
    }

    public static synchronized AddViewAppManager getInstance(Context context) {
        AddViewAppManager addViewAppManager;
        synchronized (AddViewAppManager.class) {
            if (sInstance == null) {
                sInstance = new AddViewAppManager(context.getApplicationContext());
            }
            addViewAppManager = sInstance;
        }
        return addViewAppManager;
    }

    public ArrayList<AddViewAppInfo> initAddViewAppList() {
        if (!getAddViewPermissionFirstFlag()) {
            return getAllMonitorAppsCurrentValue();
        }
        setAddViewPermissionFirstFlag();
        return getAllMonitorAppsDefaultValue();
    }

    private ArrayList<AddViewAppInfo> getAllMonitorAppsDefaultValue() {
        ArrayList<AddViewAppInfo> addViewAppList = new ArrayList();
        List<HsmPkgInfo> appInfoList = HsmPackageManager.getInstance().getInstalledPackages(0);
        List opsAppList = new ArrayList();
        ArrayList<String> pkgNameList = new ArrayList();
        List<String> initedPkgNameList = getInitedPkgInFile();
        HwLog.i(TAG, "getAllMonitorAppsDefaultValue begin");
        for (HsmPkgInfo appInfo : appInfoList) {
            String packageName = appInfo.mPkgName;
            if (initedPkgNameList.contains(packageName)) {
                HwLog.i(TAG, "getAllMonitorAppsDefaultValue, already inited:" + packageName);
            } else if (GRuleManager.getInstance().shouldMonitor(this.mContext, MonitorScenario.SCENARIO_DROPZONE, packageName) && applyDropdownPermission(packageName, null)) {
                AddViewAppInfo addViewAppInfo = new AddViewAppInfo();
                addViewAppInfo.mAddViewAllow = getDefaultValue(this.mContext, packageName, true);
                addViewAppInfo.mPkgName = packageName;
                addViewAppInfo.mLabel = appInfo.label();
                addViewAppInfo.mUid = appInfo.mUid;
                addViewAppList.add(addViewAppInfo);
                opsAppList.add(new OpsAppInfo(addViewAppInfo));
                pkgNameList.add(packageName);
            }
        }
        sendMessage(1, opsAppList);
        saveInitedPkgIntoFile(pkgNameList);
        return addViewAppList;
    }

    private boolean getDefaultValue(Context cxt, String pkg, boolean firstboot) {
        boolean permission = true;
        if (this.mAbroadVersion) {
            HsmPkgInfo pkgInfo = HsmPackageManager.getInstance().getPkgInfo(pkg);
            PackageManager pm = cxt.getPackageManager();
            if (pkgInfo == null) {
                HwLog.w(TAG, "getDefaultValue pkgInfo is null");
                return false;
            } else if (pkgInfo.isLegacy()) {
                return true;
            } else {
                if (pm == null) {
                    HwLog.w(TAG, "pkgManager == null");
                    return false;
                }
                if (pm.checkPermission(AddViewConst.SYSTEM_ALERT_WINDOW, pkg) != 0) {
                    permission = false;
                }
                return permission;
            }
        }
        synchronized (this.mDefault) {
            int defConfig = this.mDefault.getDefaultConfig(pkg);
        }
        if (defConfig != 0) {
            if (defConfig != 1) {
                permission = false;
            }
            return permission;
        } else if (firstboot) {
            return false;
        } else {
            return inCloudWhiteList(pkg);
        }
    }

    private ArrayList<AddViewAppInfo> getAllMonitorAppsCurrentValue() {
        ArrayList<AddViewAppInfo> addViewAppList = new ArrayList();
        List<HsmPkgInfo> appInfoList = HsmPackageManager.getInstance().getInstalledPackages(0);
        Map<String, Boolean> pkgOpsMap = getPackagesForOpsList();
        Set<String> pkgNameSet = pkgOpsMap.keySet();
        List<String> initedPkgNameList = getInitedPkgInFile();
        ArrayList<String> toInitpkgNameList = new ArrayList();
        HwLog.i(TAG, "getAllMonitorAppsCurrentValue begin");
        for (HsmPkgInfo appInfo : appInfoList) {
            String packageName = appInfo.mPkgName;
            if (GRuleManager.getInstance().shouldMonitor(this.mContext, MonitorScenario.SCENARIO_DROPZONE, packageName)) {
                if (applyDropdownPermission(packageName, null)) {
                    AddViewAppInfo addViewAppInfo = new AddViewAppInfo();
                    addViewAppInfo.mPkgName = packageName;
                    addViewAppInfo.mLabel = appInfo.label();
                    addViewAppInfo.mUid = appInfo.mUid;
                    if (pkgNameSet != null && pkgNameSet.contains(packageName)) {
                        addViewAppInfo.mAddViewAllow = ((Boolean) pkgOpsMap.get(packageName)).booleanValue();
                    } else if ((appInfo.mFlag & 1) == 0 || initedPkgNameList.contains(packageName) || this.mAbroadVersion) {
                        if (this.mAbroadVersion) {
                            addViewAppInfo.mAddViewAllow = appInfo.isLegacy();
                        } else {
                            addViewAppInfo.mAddViewAllow = true;
                        }
                        HwLog.i(TAG, "Allow pkgName is: " + packageName);
                    } else {
                        addViewAppInfo.mAddViewAllow = false;
                        sendMessage(0, new OpsAppInfo(addViewAppInfo));
                        toInitpkgNameList.add(packageName);
                        HwLog.i(TAG, "Forbid pkgName is: " + packageName);
                    }
                    addViewAppList.add(addViewAppInfo);
                }
            } else if (pkgOpsMap.containsKey(packageName) && !((Boolean) pkgOpsMap.get(packageName)).booleanValue()) {
                HwLog.i(TAG, "app is not monitored. set it's ops value 'allow':" + packageName);
                setOpsMode(appInfo.mUid, packageName, true);
            }
        }
        saveInitedPkgIntoFile(toInitpkgNameList);
        return addViewAppList;
    }

    private void setOpsModeList(ArrayList<OpsAppInfo> list) {
        for (OpsAppInfo opsInfo : list) {
            setOpsMode(opsInfo);
        }
    }

    private void setOpsMode(OpsAppInfo currentOpsInfo) {
        if (currentOpsInfo == null) {
            HwLog.w(TAG, "setOpsMode return because of currentOpsInfo is null!");
            return;
        }
        this.mAppOps.setMode(24, currentOpsInfo.mUid, currentOpsInfo.mPkgName, currentOpsInfo.mAddViewAllow ? 0 : 1);
        if (currentOpsInfo.mAddViewAllow) {
            Intent enableIntent = new Intent(AddViewConst.ACTION_ADD_VIEW_ENABLED);
            enableIntent.setPackage(this.mContext.getPackageName());
            enableIntent.putExtra("uid", currentOpsInfo.mUid);
            enableIntent.putExtra("package", currentOpsInfo.mPkgName);
            this.mContext.sendBroadcast(enableIntent, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
        }
    }

    public void setOpsMode(int uid, String pkgName, boolean bAllow) {
        OpsAppInfo info = new OpsAppInfo();
        info.mUid = uid;
        info.mPkgName = pkgName;
        info.mAddViewAllow = bAllow;
        setOpsMode(info);
    }

    public static void trustIfNeeded(Context context, int uid, String pkgName, ContentValues contentValues) {
        if (CommonFunctionUtil.getTrustValue(contentValues)) {
            trust(context, uid, pkgName);
            if (Log.HWINFO) {
                HwLog.i(TAG, "trustIfNeeded, is a trust app, set add view trusted.");
            }
        }
    }

    public static void trust(Context context, int uid, String pkgName) {
        AddViewAppManager instance = getInstance(context);
        instance.setOpsMode(uid, pkgName, true);
        ArrayList<String> tmp = new ArrayList();
        tmp.add(pkgName);
        instance.saveInitedPkgIntoFile(tmp);
    }

    private Map<String, Boolean> getPackagesForOpsList() {
        Map<String, Boolean> pkgNameOpsMap = new HashMap();
        int[] array = new int[]{24};
        List<PackageOps> packagesList = this.mAppOps.getPackagesForOps(array);
        if (packagesList == null) {
            HwLog.e(TAG, "The packagesList is null error!");
            return pkgNameOpsMap;
        }
        String pkgName = "";
        for (PackageOps ops : packagesList) {
            pkgName = ops.getPackageName();
            if (UserHandle.myUserId() == UserHandle.getUserId(ops.getUid())) {
                int opsMode;
                boolean z;
                List<OpEntry> appOpsList = ops.getOps();
                if (appOpsList == null || appOpsList.isEmpty()) {
                    opsMode = 1;
                } else {
                    opsMode = ((OpEntry) appOpsList.get(array.length - 1)).getMode();
                }
                HwLog.i(TAG, "ops mode is: " + opsMode + " and ops packageName is: " + pkgName);
                if (opsMode == 0) {
                    z = true;
                } else {
                    z = false;
                }
                pkgNameOpsMap.put(pkgName, Boolean.valueOf(z));
            }
        }
        return pkgNameOpsMap;
    }

    public void registerListener(IAppChangeListener callback) {
        synchronized (this.mListeners) {
            this.mListeners.add(callback);
            HwLog.i(TAG, "register listener:" + callback);
        }
    }

    public void unregisterListener(IAppChangeListener callback) {
        synchronized (this.mListeners) {
            this.mListeners.remove(callback);
            HwLog.i(TAG, "unregister listener:" + callback);
        }
    }

    public void installApp(String pkgName) {
        if (getAddViewPermissionFirstFlag()) {
            setAddViewPermissionFirstFlag();
            getAllMonitorAppsDefaultValue();
        }
        PackageManager pm = this.mContext.getPackageManager();
        try {
            PackageInfo packageInfo = PackageManagerWrapper.getPackageInfo(pm, pkgName, 12288);
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            if (applicationInfo == null || !applyDropdownPermission(pkgName, packageInfo)) {
                HwLog.w(TAG, "Current installed app not apply system_alert_window or applicationInfo null!");
                return;
            }
            AddViewAppInfo appInfo = new AddViewAppInfo();
            appInfo.mPkgName = pkgName;
            appInfo.mAddViewAllow = getDefaultValue(this.mContext, pkgName, false);
            appInfo.mUid = applicationInfo.uid;
            appInfo.mLabel = getAppName(applicationInfo, pm);
            sendMessage(0, new OpsAppInfo(appInfo));
            for (IAppChangeListener listener : this.mListeners) {
                listener.onPackageAdded(pkgName, appInfo);
            }
            addOnePkgNameIntoFile(pkgName);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean inCloudWhiteList(String packageName) {
        boolean z = false;
        Cursor cursor = CloudDBAdapter.getInstance(this.mContext).getAddviewCursor(packageName);
        if (!CursorHelper.checkCursorValid(cursor)) {
            return false;
        }
        cursor.moveToNext();
        int cloud_status = cursor.getInt(cursor.getColumnIndex("permissionCfg"));
        cursor.close();
        if (cloud_status == 0) {
            z = true;
        }
        return z;
    }

    public void uninstallApp(String pkgName) {
        for (IAppChangeListener listener : this.mListeners) {
            listener.onPackageRemoved(pkgName);
        }
        deletePkgNameFromFile(pkgName);
    }

    public void singleOpsChange(AddViewAppInfo appInfo) {
        sendMessage(0, new OpsAppInfo(appInfo));
    }

    public void listOpsChange(List<AddViewAppInfo> appInfoList) {
        List opsAppList = new ArrayList();
        for (AddViewAppInfo appInfo : appInfoList) {
            opsAppList.add(new OpsAppInfo(appInfo));
        }
        sendMessage(1, opsAppList);
    }

    private String getAppName(ApplicationInfo appInfo, PackageManager manager) {
        String appName = "";
        if (appInfo == null || manager == null) {
            return appName;
        }
        return manager.getApplicationLabel(appInfo).toString().replaceAll("\\s", " ").trim();
    }

    private boolean getAddViewPermissionFirstFlag() {
        boolean emendationFlag = false;
        try {
            emendationFlag = this.mContext.getSharedPreferences(AddViewConst.ADD_VIEW_PERMISSION, 4).getBoolean(AddViewConst.ADD_VIEW_PERMISSION_FLAG, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return emendationFlag;
    }

    private void setAddViewPermissionFirstFlag() {
        try {
            Editor editor = this.mContext.getSharedPreferences(AddViewConst.ADD_VIEW_PERMISSION, 4).edit();
            editor.putBoolean(AddViewConst.ADD_VIEW_PERMISSION_FLAG, false);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetAddViewPermissionFirstFlag() {
        try {
            Editor editor = this.mContext.getSharedPreferences(AddViewConst.ADD_VIEW_PERMISSION, 4).edit();
            editor.putBoolean(AddViewConst.ADD_VIEW_PERMISSION_FLAG, true);
            editor.commit();
        } catch (Exception e) {
            HwLog.e(TAG, "resetAddViewPermissionFirstFlag: Exception", e);
        }
    }

    private boolean applyDropdownPermission(String pkgName, PackageInfo info) {
        PackageInfo packageInfo;
        if (info == null) {
            try {
                packageInfo = PackageManagerWrapper.getPackageInfo(this.mContext.getPackageManager(), pkgName, 12288);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
                return true;
            }
        }
        packageInfo = info;
        String[] permissions = packageInfo.requestedPermissions;
        if (permissions != null) {
            for (String permission : permissions) {
                if (AddViewConst.SYSTEM_ALERT_WINDOW.equals(permission)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void sendMessage(int what, List<OpsAppInfo> opsAppList) {
        Message message = new Message();
        message.what = what;
        message.obj = new ArrayList(opsAppList);
        this.mHandler.sendMessage(message);
    }

    private void sendMessage(int what, OpsAppInfo opsApp) {
        Message message = new Message();
        message.what = what;
        message.obj = opsApp;
        this.mHandler.sendMessage(message);
    }

    private boolean getAbroadVersionStatus() {
        return AbroadUtils.isAbroad();
    }

    public void saveInitedPkgIntoFileCheck(ArrayList<String> pkgNameList) {
        if (pkgNameList != null && !pkgNameList.isEmpty()) {
            ArrayList<String> checkedList = new ArrayList();
            for (String packageName : pkgNameList) {
                if (applyDropdownPermission(packageName, null)) {
                    checkedList.add(packageName);
                }
            }
            if (!checkedList.isEmpty()) {
                Intent serviceIntent = new Intent(AddViewConst.ADD_VIEW_RECORD_LIST_ACTION);
                serviceIntent.setClass(this.mContext, AddViewIntentService.class);
                serviceIntent.putStringArrayListExtra(AddViewConst.ADD_VIEW_PKGLIST_KEY, pkgNameList);
                this.mContext.startService(serviceIntent);
            }
        }
    }

    private void saveInitedPkgIntoFile(ArrayList<String> pkgNameList) {
        if (pkgNameList != null && !pkgNameList.isEmpty()) {
            try {
                Editor edit = this.mContext.getSharedPreferences(AddViewConst.ADD_VIEW_SHAREPREFERENCE, 0).edit();
                for (String pkgName : pkgNameList) {
                    edit.putString(pkgName, "");
                }
                edit.commit();
            } catch (Exception e) {
                HwLog.e(TAG, "saveInitedPkgIntoFile Exception msg is: " + e.getMessage());
            }
        }
    }

    public ArrayList<String> getInitedPkgInFile() {
        ArrayList<String> pkgNameList = new ArrayList();
        try {
            for (Entry<String, ?> entry : this.mContext.getSharedPreferences(AddViewConst.ADD_VIEW_SHAREPREFERENCE, 0).getAll().entrySet()) {
                pkgNameList.add((String) entry.getKey());
            }
        } catch (Exception e) {
            HwLog.e(TAG, "getInitedPkgInFile Exception msg is: " + e.getMessage());
        }
        return pkgNameList;
    }

    private void deletePkgNameFromFile(String pkgName) {
        try {
            this.mContext.getSharedPreferences(AddViewConst.ADD_VIEW_SHAREPREFERENCE, 0).edit().remove(pkgName).commit();
        } catch (Exception e) {
            HwLog.e(TAG, "deletePkgNameFromFile Exception msg is: " + e.getMessage());
        }
    }

    private void addOnePkgNameIntoFile(String pkgName) {
        try {
            this.mContext.getSharedPreferences(AddViewConst.ADD_VIEW_SHAREPREFERENCE, 0).edit().putString(pkgName, "").commit();
        } catch (Exception e) {
            HwLog.e(TAG, "addOnePkgNameIntoFile Exception msg is: " + e.getMessage());
        }
    }

    public void deleteRecords(List<String> pkgNameList) {
        if (pkgNameList != null && !pkgNameList.isEmpty()) {
            Map<String, Boolean> pkgOpsMap = getPackagesForOpsList();
            for (HsmPkgInfo appInfo : HsmPackageManager.getInstance().getAllPackages()) {
                String pkgName = appInfo.mPkgName;
                if (pkgNameList.contains(pkgName) && applyDropdownPermission(pkgName, null) && pkgOpsMap.containsKey(pkgName) && !((Boolean) pkgOpsMap.get(pkgName)).booleanValue()) {
                    HwLog.i(TAG, "app is not monitored. set it's ops value 'allow':" + pkgName);
                    setOpsMode(appInfo.mUid, pkgName, true);
                    deletePkgNameFromFile(pkgName);
                }
            }
        }
    }

    public void addRecords(List<String> pkgNameList) {
        if (pkgNameList != null && !pkgNameList.isEmpty()) {
            if (getAddViewPermissionFirstFlag()) {
                setAddViewPermissionFirstFlag();
                getAllMonitorAppsDefaultValue();
            }
            for (HsmPkgInfo appInfo : HsmPackageManager.getInstance().getAllPackages()) {
                String pkgName = appInfo.mPkgName;
                if (pkgNameList.contains(pkgName) && applyDropdownPermission(pkgName, null)) {
                    setOpsMode(appInfo.mUid, pkgName, getDefaultValue(this.mContext, pkgName, false));
                    addOnePkgNameIntoFile(pkgName);
                }
            }
        }
    }

    public boolean isCurrentAppShouldMonitor(String packageName) {
        if (GRuleManager.getInstance().shouldMonitor(this.mContext, MonitorScenario.SCENARIO_DROPZONE, packageName)) {
            return applyDropdownPermission(packageName, null);
        }
        return false;
    }

    public boolean getCurrentAppAddviewValue(String packageName) {
        if (getInitedPkgInFile().contains(packageName)) {
            HwLog.i(TAG, "The current app: " + packageName + " already in the xml file");
        } else {
            List<String> pkgNameList = new ArrayList();
            pkgNameList.add(packageName);
            addRecords(pkgNameList);
        }
        Map<String, Boolean> pkgOpsMap = getPackagesForOpsList();
        Set<String> pkgNameSet = pkgOpsMap.keySet();
        if (pkgNameSet == null || !pkgNameSet.contains(packageName)) {
            return true;
        }
        return ((Boolean) pkgOpsMap.get(packageName)).booleanValue();
    }
}

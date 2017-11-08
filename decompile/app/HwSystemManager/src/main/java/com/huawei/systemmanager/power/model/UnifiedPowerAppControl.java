package com.huawei.systemmanager.power.model;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.IDeviceIdleController;
import android.os.IDeviceIdleController.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.ArrayMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.optimize.process.AbsProtectAppControl;
import com.huawei.systemmanager.optimize.process.IDataChangedListener;
import com.huawei.systemmanager.optimize.smcs.SMCSDatabaseConstant;
import com.huawei.systemmanager.power.comm.ApplicationConstant;
import com.huawei.systemmanager.power.provider.PowerXmlHelper;
import com.huawei.systemmanager.power.provider.SmartProvider;
import com.huawei.systemmanager.power.provider.SmartProviderHelper;
import com.huawei.systemmanager.rainbow.db.CloudDBAdapter;
import com.huawei.systemmanager.rainbow.db.bean.UnifiedPowerAppsConfigBean;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import com.huawei.systemmanager.util.app.IPackageChangeListener.DefListener;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class UnifiedPowerAppControl extends AbsProtectAppControl {
    private static final boolean DEFAULTVALUE_UNIFIEDPOWERAPPS_CHECKED = false;
    private static final boolean DEFAULTVALUE_UNIFIEDPOWERAPPS_SHOW = true;
    private static final String DEVICE_IDLE_SERVICE = "deviceidle";
    public static final String TAG = "UnifiedPowerAppControl";
    private static UnifiedPowerAppControl mAppChangeManager;
    private final ArrayMap<String, UnifiedPowerBean> mApps = HsmCollections.newArrayMap();
    private final IDeviceIdleController mDeviceIdleService;

    class ExternalStorageListener extends DefListener {
        ExternalStorageListener() {
        }

        public void onExternalChanged(String[] packages, boolean available) {
            UnifiedPowerAppControl.this.doExternalChanged(packages, available);
        }
    }

    private UnifiedPowerAppControl(Context context) {
        super(context);
        this.mHandlerThread.start();
        HsmPackageManager.registerListener(new ExternalStorageListener());
        this.mContext.getContentResolver().registerContentObserver(SMCSDatabaseConstant.URI_BACKUP_END, false, this.mContentObserver);
        this.mDeviceIdleService = Stub.asInterface(ServiceManager.getService(DEVICE_IDLE_SERVICE));
    }

    public static synchronized UnifiedPowerAppControl getInstance(Context context) {
        UnifiedPowerAppControl unifiedPowerAppControl;
        synchronized (UnifiedPowerAppControl.class) {
            if (mAppChangeManager == null) {
                mAppChangeManager = new UnifiedPowerAppControl(context);
            }
            unifiedPowerAppControl = mAppChangeManager;
        }
        return unifiedPowerAppControl;
    }

    public Boolean isProtect(String pkg) {
        Boolean valueOf;
        synchronized (this) {
            valueOf = Boolean.valueOf(((UnifiedPowerBean) this.mApps.get(pkg)).is_protected());
        }
        return valueOf;
    }

    public boolean setProtect(List<String> pkgList) {
        List<String> addList = Lists.newArrayListWithCapacity(pkgList.size());
        synchronized (this) {
            for (String app : pkgList) {
                UnifiedPowerBean upb = (UnifiedPowerBean) this.mApps.get(app);
                if (!upb.is_protected()) {
                    upb.setIs_protected(true);
                    this.mApps.put(app, upb);
                    addList.add(app);
                }
            }
        }
        if (addList.isEmpty()) {
            return false;
        }
        HwLog.i(TAG, "set protect list:" + pkgList);
        sendMessage(1, addList);
        return true;
    }

    public boolean setNoProtect(List<String> pkgList) {
        List<String> notProtectList = Lists.newArrayListWithCapacity(pkgList.size());
        synchronized (this) {
            for (String app : pkgList) {
                UnifiedPowerBean upb = (UnifiedPowerBean) this.mApps.get(app);
                if (upb.is_protected()) {
                    upb.setIs_protected(false);
                    this.mApps.put(app, upb);
                    notProtectList.add(app);
                }
            }
        }
        if (notProtectList.isEmpty()) {
            HwLog.i(TAG, "not protect is empty");
            return false;
        }
        HwLog.i(TAG, "set not protect list:" + pkgList);
        sendMessage(2, notProtectList);
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void installAppInner(String apkName) {
        boolean protect;
        boolean show;
        HwLog.i(TAG, "begin install app inner:" + apkName);
        UnifiedPowerAppsConfigBean cloudBean = CloudDBAdapter.getInstance(this.mContext).getSingleUnifiedPowerAppsConfigBean(apkName);
        UnifiedPowerBean unifiedPowerBean = new UnifiedPowerBean();
        if (cloudBean == null) {
            HwLog.i(TAG, " null == unifiedPowerBean");
            HashSet<String> iconApps = getAllAppsWithLauncherIcon();
            HsmPkgInfo info = HsmPackageManager.getInstance().getPkgInfo(apkName);
            if (info == null) {
                HwLog.i(TAG, " info is null!!! ");
                return;
            } else if (info.isRemovable()) {
                UnifiedPowerBean upb = applyControlPolicy(info, iconApps.contains(apkName));
                useCloudDataOverwriteLocalBean(apkName, upb);
                protect = upb.is_protected();
                show = upb.is_show();
            } else {
                return;
            }
        }
        unifiedPowerBean.setPkg_name(cloudBean.getPkgName());
        unifiedPowerBean.setIs_show(cloudBean.isShow());
        unifiedPowerBean.setIs_protected(cloudBean.isProtected());
        useCloudDataOverwriteLocalBean(apkName, unifiedPowerBean);
        protect = unifiedPowerBean.is_protected();
        show = unifiedPowerBean.is_show();
        HwLog.i(TAG, " null != unifiedPowerBean");
        HwLog.i(TAG, "apkName " + apkName + " protect = " + protect + " show =" + show);
        synchronized (this) {
            if (((UnifiedPowerBean) this.mApps.get(apkName)) != null) {
                HwLog.w(TAG, apkName + " already exist!");
            } else if (show) {
                upb = new UnifiedPowerBean();
                upb.setIs_protected(protect);
                upb.setIs_show(show);
                this.mApps.put(apkName, upb);
            }
        }
    }

    protected void uninstallAppInner(String pkgName) {
        synchronized (this) {
            this.mApps.remove(pkgName);
        }
        HwLog.i(TAG, "uninstallAppInner  pkgName:" + pkgName);
        SmartProviderHelper.deleteUnifiedPowerAppListForDB(pkgName, this.mContext);
        getInstance(this.mContext).removeAppToFWKForDOZEAndAppStandby(pkgName);
        for (IDataChangedListener listener : AbsProtectAppControl.getListeners()) {
            listener.onPackageRemoved(pkgName);
        }
    }

    private void doExternalChanged(String[] packages, boolean available) {
        if (available) {
            addSdcardApp(packages);
        } else {
            removeAdcardApp(packages);
        }
    }

    private void addSdcardApp(String[] packages) {
        Map<String, UnifiedPowerBean> protectMap = getProtectMap();
        List<IDataChangedListener> listeners = AbsProtectAppControl.getListeners();
        for (String pkg : packages) {
            HwLog.i(TAG, "addSdcardApp, " + pkg);
            UnifiedPowerBean upb = (UnifiedPowerBean) protectMap.get(pkg);
            if (upb == null) {
                HwLog.i(TAG, "UnifiedPowerBean is null.");
            } else {
                Boolean protectValue = Boolean.valueOf(upb.is_protected());
                if (protectValue != null) {
                    boolean protect = protectValue.booleanValue();
                    for (IDataChangedListener listener : listeners) {
                        listener.onPackageAdded(pkg, protect);
                    }
                }
            }
        }
    }

    protected void protectAppToDB(ArrayList<String> protectList) {
        if (protectList != null) {
            HwLog.i(TAG, "protect db operation begin. protectList " + protectList);
            SmartProviderHelper.updateUnifiedPowerAppListForDB(protectList, 1, this.mContext);
            HwLog.i(TAG, "protect db operation end.");
        }
    }

    protected void notProtectFromDB(ArrayList<String> notProtectList) {
        if (notProtectList != null) {
            HwLog.i(TAG, "not protect db operation begin. " + notProtectList.toString());
            SmartProviderHelper.updateUnifiedPowerAppListForDB(notProtectList, 0, this.mContext);
            HwLog.i(TAG, "not protect db operation end.");
        }
    }

    public Map<String, UnifiedPowerBean> getProtectMap() {
        ArrayMap<String, UnifiedPowerBean> map;
        synchronized (this) {
            map = HsmCollections.newArrayMap(this.mApps);
        }
        return map;
    }

    public void loadData() {
        synchronized (this) {
            this.mApps.clear();
            SmartProviderHelper.getProtectAppFromDb(this.mContext, this.mApps);
        }
    }

    private boolean isAppRemoveAble(HsmPkgInfo info) {
        if (!info.isSystem()) {
            return true;
        }
        if (!info.isRemoveAblePreInstall()) {
            return false;
        }
        HwLog.i(TAG, info.getPackageName() + " is remove able preInstalled!");
        return true;
    }

    public boolean checkExsist(String pkgName) {
        boolean z;
        synchronized (this) {
            z = this.mApps.get(pkgName) != null;
        }
        return z;
    }

    public boolean setProtect(UnifiedPowerAppItem item, boolean protect) {
        if (item == null) {
            return false;
        }
        String pkg = item.getPackageName();
        item.setProtect(protect);
        if (protect) {
            setProtect(pkg);
        } else {
            setNoProtect(pkg);
        }
        return true;
    }

    public void initUnifiedpowerappsTable(SQLiteDatabase db) {
        HwLog.i(TAG, "initUnifiedpowerappsTable....");
        long time1 = System.currentTimeMillis();
        HashSet<String> existPkgs = initialUnifiedPowerAppTableFromXML(db);
        List<HsmPkgInfo> pkgList = HsmPackageManager.getInstance().getAllPackages();
        HashSet<String> iconApps = getAllAppsWithLauncherIcon();
        List<UnifiedPowerBean> initApps = Lists.newArrayList();
        for (HsmPkgInfo info : pkgList) {
            if (info.isRemovable()) {
                String pkgName = info.getPackageName();
                if (!existPkgs.contains(pkgName)) {
                    UnifiedPowerBean upb = applyControlPolicy(info, iconApps.contains(pkgName));
                    useCloudDataOverwriteLocalBean(pkgName, upb);
                    initApps.add(upb);
                }
            }
        }
        SmartProviderHelper.initUnifiedPowerAppTable(initApps, this.mContext, db);
        HwLog.i(TAG, "initUnifiedpowerappsTable cost time:" + (System.currentTimeMillis() - time1));
        updateForDOZEAndAppStandby();
        notifiChanged(SmartProvider.UNIFIED_POWER_APP_RUI);
        notifiChanged(SmartProvider.NIFIED_POWER_APP_DEFAULT_VALUE_RUI);
    }

    private void updateForDOZEAndAppStandby() {
        HwLog.i(TAG, "updateForDOZEAndAppStandby");
        Map<String, ArrayList<String>> res = SmartProviderHelper.getProtectAppFromDbForPowerGenie(GlobalContext.getContext(), "all", null);
        ArrayList<String> protect = (ArrayList) res.get(SmartProvider.CALL_METHOD_PROTECT_KEY);
        ArrayList<String> unprotect = (ArrayList) res.get(SmartProvider.CALL_METHOD_UNPROTECT_KEY);
        for (int i = 0; i < protect.size(); i++) {
            addAppToFWKForDOZEAndAppStandby((String) protect.get(i));
        }
        for (int j = 0; j < unprotect.size(); j++) {
            removeAppToFWKForDOZEAndAppStandby((String) unprotect.get(j));
        }
    }

    private UnifiedPowerBean applyControlPolicy(HsmPkgInfo info, boolean hasIcon) {
        UnifiedPowerBean upb = new UnifiedPowerBean();
        upb.setPkg_name(info.getPackageName());
        upb.setIs_protected(false);
        if (isAppRemoveAble(info)) {
            upb.setIs_protected(false);
        } else {
            upb.setIs_protected(true);
        }
        if (hasIcon || isAppRemoveAble(info)) {
            upb.setIs_show(true);
        } else {
            upb.setIs_show(false);
        }
        return upb;
    }

    protected void notifiChanged(Uri uri) {
        this.mContext.getContentResolver().notifyChange(uri, null);
    }

    private HashSet<String> getAllAppsWithLauncherIcon() {
        HashSet<String> apps = Sets.newHashSet();
        PackageManager pm = this.mContext.getPackageManager();
        Intent mainIntent = new Intent("android.intent.action.MAIN", null);
        mainIntent.addCategory("android.intent.category.LAUNCHER");
        for (ResolveInfo ri : PackageManagerWrapper.queryIntentActivities(pm, mainIntent, 0)) {
            apps.add(ri.activityInfo.packageName);
        }
        return apps;
    }

    private HashSet<String> initialUnifiedPowerAppTableFromXML(SQLiteDatabase db) {
        HashSet<String> pkgs = Sets.newHashSet();
        List<HsmPkgInfo> pkgList = HsmPackageManager.getInstance().getAllPackages();
        List<String> currentPkg = Lists.newArrayList();
        for (HsmPkgInfo info : pkgList) {
            currentPkg.add(info.getPackageName());
        }
        db.beginTransaction();
        try {
            for (UnifiedPowerBean bean : PowerXmlHelper.parseUnifiedPowerAppTableDefaultValue(this.mContext)) {
                ContentValues values = new ContentValues(3);
                values.put("pkg_name", bean.getPkg_name());
                values.put(ApplicationConstant.UNIFIED_POWER_APP_CHECK, Integer.valueOf(changeStringToIntForUnifiedPowerApp(bean.is_protected())));
                values.put(ApplicationConstant.UNIFIED_POWER_APP_SHOW, Integer.valueOf(changeStringToIntForUnifiedPowerApp(bean.is_show())));
                if (currentPkg.contains(bean.getPkg_name())) {
                    db.insert(SmartProvider.UNIFIED_POWER_APP_TABLE, null, values);
                }
                pkgs.add(bean.getPkg_name());
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            HwLog.e(TAG, "initialUnifiedPowerAppTableFromXML faild! " + e.toString());
        } finally {
            db.endTransaction();
        }
        return pkgs;
    }

    private int changeStringToIntForUnifiedPowerApp(boolean value) {
        if (value) {
            return 1;
        }
        return 0;
    }

    public static String createUnifiedPowerAppSQL() {
        StringBuffer sql = new StringBuffer();
        try {
            sql.append("CREATE TABLE IF NOT EXISTS unifiedpowerapps ( ");
            sql.append("pkg_name TEXT NOT NULL PRIMARY KEY , ");
            sql.append("is_protected INTEGER, ");
            sql.append("is_show INTEGER, ");
            sql.append("is_changed INTEGER default 0) ");
            HwLog.v(TAG, "DatabaseHelper.creatTable unifiedpowerapps: " + sql);
            return sql.toString();
        } catch (Exception e) {
            HwLog.e(TAG, "DatabaseHelper.creatTable unifiedpowerapps: catch exception " + e.toString());
            return null;
        }
    }

    public static String createUnifiedPowerAppDefaulValuetSQL() {
        StringBuffer sql = new StringBuffer();
        try {
            sql.append("CREATE TABLE IF NOT EXISTS unifiedpowerappsdefaultvalue ( ");
            sql.append("pkg_name TEXT NOT NULL PRIMARY KEY , ");
            sql.append("is_protected INTEGER, ");
            sql.append("is_show INTEGER ) ");
            HwLog.v(TAG, "DatabaseHelper.creatTable unifiedpowerappdefaultvalue: " + sql);
            return sql.toString();
        } catch (Exception e) {
            HwLog.e(TAG, "DatabaseHelper.creatTable unifiedpowerappdefaultvalue: catch exception " + e.toString());
            return null;
        }
    }

    public static String dropUnifiedPowerAppSQL() {
        return "DROP TABLE IF EXISTS unifiedpowerapps";
    }

    public static String dropUnifiedPowerAppDefaultValueSQL() {
        return "DROP TABLE IF EXISTS unifiedpowerappsdefaultvalue";
    }

    public void addAppToFWKForDOZEAndAppStandby(String pkg) {
        try {
            this.mDeviceIdleService.addPowerSaveWhitelistApp(pkg);
        } catch (RemoteException e) {
            HwLog.e(TAG, "addAppToFWKForDOZEAndAppStandby Unable to reach IDeviceIdleController", e);
        }
    }

    public void removeAppToFWKForDOZEAndAppStandby(String pkg) {
        try {
            this.mDeviceIdleService.removePowerSaveWhitelistApp(pkg);
        } catch (RemoteException e) {
            HwLog.e(TAG, "removeAppToFWKForDOZEAndAppStandby Unable to reach IDeviceIdleController", e);
        }
    }

    private void useCloudDataOverwriteLocalBean(String pkgName, UnifiedPowerBean localBean) {
        UnifiedPowerAppsConfigBean cloudBean = CloudDBAdapter.getInstance(this.mContext).getSingleUnifiedPowerAppsConfigBean(pkgName);
        if (cloudBean != null) {
            HwLog.d(TAG, "useCloudDataOverwriteLocalBean overwrite " + pkgName + " success!");
            localBean.setIs_protected(cloudBean.isProtected());
            localBean.setIs_show(cloudBean.isShow());
        }
    }

    public void updateLocalUnifiedPowerAppTableByHOTA(SQLiteDatabase db) {
        HwLog.i(TAG, "updateLocalUnifiedPowerAppTableByHOTA begin");
        long time1 = System.currentTimeMillis();
        List<UnifiedPowerBean> userUnChangedList = SmartProviderHelper.getUserUnChangedUnifiedPowerList(this.mContext);
        if (userUnChangedList == null) {
            HwLog.w(TAG, "updateLocalUnifiedPowerAppTableByHOTA end userUnChangedList is null");
            return;
        }
        List<UnifiedPowerBean> praseData = PowerXmlHelper.parseUnifiedPowerAppTableDefaultValue(this.mContext);
        HashSet<String> iconApps = getAllAppsWithLauncherIcon();
        for (UnifiedPowerBean bean : userUnChangedList) {
            String pkgName = bean.getPkg_name();
            boolean isExisted = false;
            for (UnifiedPowerBean xmlBean : praseData) {
                if (pkgName.equals(xmlBean.getPkg_name())) {
                    SmartProviderHelper.updateUnifiedPowerAppListForDB(xmlBean.getPkg_name(), xmlBean.is_protected(), xmlBean.is_show(), this.mContext);
                    isExisted = true;
                    HwLog.i(TAG, "updateLocalUnifiedPowerAppTableByHOTA " + xmlBean.getPkg_name() + " is updated");
                    break;
                }
            }
            if (!isExisted) {
                HsmPkgInfo info = HsmPackageManager.getInstance().getPkgInfo(pkgName);
                if (info != null) {
                    UnifiedPowerBean upb = applyControlPolicy(info, iconApps.contains(pkgName));
                    SmartProviderHelper.updateUnifiedPowerAppListForDB(pkgName, upb.is_protected(), upb.is_show(), this.mContext);
                }
            }
        }
        HwLog.i(TAG, "updateLocalUnifiedPowerAppTableByHOTA cost time:" + (System.currentTimeMillis() - time1));
    }

    protected Object getObjectLock() {
        return this;
    }

    public static String getTAG() {
        return TAG;
    }
}

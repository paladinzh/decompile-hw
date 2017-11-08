package com.huawei.systemmanager.optimize.process;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.google.android.collect.Maps;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.huawei.cust.HwCustUtils;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.customize.AbroadUtils;
import com.huawei.systemmanager.filterrule.util.BaseSignatures;
import com.huawei.systemmanager.optimize.smcs.SMCSDatabaseConstant;
import com.huawei.systemmanager.power.comm.ApplicationConstant;
import com.huawei.systemmanager.power.util.SavingSettingUtil;
import com.huawei.systemmanager.rainbow.db.CloudDBAdapter;
import com.huawei.systemmanager.rainbow.db.bean.BackgroundConfigBean;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import com.huawei.systemmanager.util.app.IPackageChangeListener.DefListener;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ProtectAppControl extends AbsProtectAppControl {
    private static final String PERMISSION_NOT_CLEAR = "com.android.permission.system_manager_noclear";
    public static final String TAG = "ProtectAppControl";
    private static ProtectAppControl mAppChangeManager;
    private final ArrayMap<String, Boolean> mApps = HsmCollections.newArrayMap();
    private final HwCustProtectAppControl mCust;

    class ExternalStorageListener extends DefListener {
        ExternalStorageListener() {
        }

        public void onExternalChanged(String[] packages, boolean available) {
            ProtectAppControl.this.doExternalChanged(packages, available);
        }
    }

    private ProtectAppControl(Context context) {
        super(context);
        this.mHandlerThread.start();
        loadData();
        HsmPackageManager.registerListener(new ExternalStorageListener());
        this.mContext.getContentResolver().registerContentObserver(SMCSDatabaseConstant.URI_BACKUP_END, false, this.mContentObserver);
        this.mCust = (HwCustProtectAppControl) HwCustUtils.createObj(HwCustProtectAppControl.class, new Object[0]);
    }

    public static synchronized ProtectAppControl getInstance(Context context) {
        ProtectAppControl protectAppControl;
        synchronized (ProtectAppControl.class) {
            if (mAppChangeManager == null) {
                mAppChangeManager = new ProtectAppControl(context);
            }
            protectAppControl = mAppChangeManager;
        }
        return protectAppControl;
    }

    public Boolean isProtect(String pkg) {
        Boolean bool;
        synchronized (this) {
            bool = (Boolean) this.mApps.get(pkg);
        }
        return bool;
    }

    public boolean setProtect(List<String> pkgList) {
        List<String> addList = Lists.newArrayListWithCapacity(pkgList.size());
        synchronized (this) {
            for (String app : pkgList) {
                Boolean protect = (Boolean) this.mApps.get(app);
                if (protect == null) {
                    HwLog.w(TAG, "pkg:" + app + " is not in mApps map, can not set protect!");
                } else if (!protect.booleanValue()) {
                    this.mApps.put(app, Boolean.valueOf(true));
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
                Boolean protect = (Boolean) this.mApps.get(app);
                if (protect == null) {
                    HwLog.w(TAG, "pkg:" + app + " is not in mApps map, can not set noProtect!");
                } else if (protect.booleanValue()) {
                    this.mApps.put(app, Boolean.valueOf(false));
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
        boolean controlled = true;
        if (isFriendApp(apkName)) {
            controlled = false;
            HwLog.i(TAG, "pkg:" + apkName + " is friend app, did not control");
        } else {
            int state = SmcsDbHelper.getSingleControlledState(this.mContext, apkName);
            if (state == 1) {
                HwLog.i(TAG, "pkg:" + apkName + " cust not controlled, did not control");
                controlled = false;
            } else if (state == 0) {
                controlled = isDefalutControlled(HsmPackageManager.getInstance().getPkgInfo(apkName));
                HwLog.i(TAG, "pkg:" + apkName + " isDefalutControlled is " + controlled);
            }
        }
        if (isHealthApp(apkName)) {
            controlled = false;
            HwLog.i(TAG, "install huawei health, should not be controlled");
        }
        if (isCtsApp(apkName)) {
            controlled = false;
            HwLog.i(TAG, apkName + " is cts app, did not controlled");
        }
        if (controlled) {
            boolean protect = isDefaultProtect(this.mContext, apkName);
            if (AbroadUtils.isAbroad()) {
                protect = true;
            }
            synchronized (this) {
                if (this.mApps.get(apkName) != null) {
                    HwLog.w(TAG, apkName + " already exist!");
                    return;
                }
                this.mApps.put(apkName, Boolean.valueOf(protect));
            }
        } else {
            setNoControlled(apkName);
        }
    }

    protected void uninstallAppInner(String pkgName) {
        synchronized (this) {
            Boolean protect = (Boolean) this.mApps.remove(pkgName);
        }
        if (protect == null) {
            HwLog.i(TAG, "uninstallAppInner no record:" + pkgName);
            return;
        }
        SmcsDbHelper.deleteProtectRecordFromDb(pkgName, this.mContext);
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
        Map<String, Boolean> protectMap = getProtectMap();
        List<IDataChangedListener> listeners = AbsProtectAppControl.getListeners();
        for (String pkg : packages) {
            HwLog.i(TAG, "addSdcardApp, " + pkg);
            Boolean protectValue = (Boolean) protectMap.get(pkg);
            if (protectValue != null) {
                boolean protect = protectValue.booleanValue();
                for (IDataChangedListener listener : listeners) {
                    listener.onPackageAdded(pkg, protect);
                }
            }
        }
    }

    protected void protectAppToDB(ArrayList<String> protectList) {
        if (protectList != null) {
            HwLog.i(TAG, "protect db operation begin. protectList " + protectList);
            if (protectList.size() > 1) {
                SmcsDbHelper.updateProtectAppListForDB(protectList, 1, this.mContext);
            } else if (protectList.size() == 1) {
                SmcsDbHelper.updateProtectAppForDB((String) protectList.get(0), 1, this.mContext);
            }
            HwLog.i(TAG, "protect db operation end.");
        }
    }

    protected void notProtectFromDB(ArrayList<String> deleteList) {
        if (deleteList != null) {
            HwLog.i(TAG, "not protect db operation begin.deleteList " + deleteList.toString());
            if (deleteList.size() > 1) {
                SmcsDbHelper.updateProtectAppListForDB(deleteList, 0, this.mContext);
            } else if (deleteList.size() == 1) {
                SmcsDbHelper.updateProtectAppForDB((String) deleteList.get(0), 0, this.mContext);
            }
            HwLog.i(TAG, "not protect db operation end.");
        }
    }

    private void setNoControlled(String pkgName) {
        synchronized (this) {
            this.mApps.remove(pkgName);
        }
        SmcsDbHelper.deleteProtectRecordFromDb(pkgName, this.mContext);
    }

    public ArrayList<String> getProtectList() {
        ArrayList<String> protectList = Lists.newArrayList();
        synchronized (this) {
            for (Entry<String, Boolean> entry : this.mApps.entrySet()) {
                if (((Boolean) entry.getValue()).booleanValue()) {
                    protectList.add((String) entry.getKey());
                }
            }
        }
        return protectList;
    }

    public ArrayList<String> getAllControlledAppFromDb() {
        return SmcsDbHelper.getAllControlled(this.mContext);
    }

    public ArrayList<String> getProtectedAppListFromDb() {
        ArrayList<String> list = new ArrayList();
        SmcsDbHelper.getListFromDB(list, 1, this.mContext);
        return list;
    }

    public Map<String, Boolean> getProtectMap() {
        ArrayMap<String, Boolean> map;
        synchronized (this) {
            map = HsmCollections.newArrayMap(this.mApps);
        }
        return map;
    }

    public int getProtectNum() {
        int num = 0;
        for (ProtectAppItem item : getProtectAppItems(false)) {
            if (item.isProtect()) {
                num++;
            }
        }
        return num;
    }

    public Map<String, Integer> getProtectListInfo() {
        Map<String, Integer> map = Maps.newHashMap();
        List<ProtectAppItem> list = getProtectAppItems(false);
        int num = 0;
        for (ProtectAppItem item : list) {
            if (item.isProtect()) {
                num++;
            }
        }
        map.put(ApplicationConstant.PROTECT_ALL_KEY, Integer.valueOf(list.size()));
        map.put(ApplicationConstant.PROTECTED_APP_KEY, Integer.valueOf(num));
        return map;
    }

    public List<ProtectAppItem> getProtectAppItems() {
        return getProtectAppItems(true);
    }

    public List<ProtectAppItem> getProtectAppItems(boolean checkPower) {
        ArrayList<ProtectAppItem> result = Lists.newArrayList();
        Map<String, Boolean> protectMap = getProtectMap();
        HashSet<String> powerCostSet = Sets.newHashSet();
        if (checkPower) {
            powerCostSet = SavingSettingUtil.getRogueAppSet(this.mContext);
        }
        for (Entry<String, Boolean> entry : protectMap.entrySet()) {
            String pkg = (String) entry.getKey();
            HsmPkgInfo info = null;
            try {
                info = HsmPackageManager.getInstance().getPkgInfo(pkg, 0);
            } catch (Exception e) {
                HwLog.i(TAG, "getProtectAppItems, pkg:" + pkg + " did not exist or not full installed,");
            }
            if (info != null && (this.mCust == null || !this.mCust.isDisabledPkgForProtected(this.mContext, pkg))) {
                result.add(new ProtectAppItem(info, ((Boolean) entry.getValue()).booleanValue(), powerCostSet.contains(pkg)));
            }
        }
        return result;
    }

    public boolean isFriendApp(String pkgName) {
        try {
            PackageInfo pi = PackageManagerWrapper.getPackageInfo(this.mContext.getPackageManager(), pkgName, 4160);
            if (pi == null || pi.requestedPermissions == null) {
                HwLog.w(TAG, "can't get app's request permissions : " + pkgName);
                return false;
            } else if (findNotClearPermission(pi)) {
                return checkHwSignatures(pi);
            } else {
                return false;
            }
        } catch (NameNotFoundException e) {
            HwLog.w(TAG, "NameNotFoundException:get package info fail. pkg:" + pkgName);
            return false;
        }
    }

    private boolean findNotClearPermission(PackageInfo pi) {
        for (String perm : pi.requestedPermissions) {
            if (PERMISSION_NOT_CLEAR.equals(perm)) {
                HwLog.i(TAG, "this app has no clear permission.");
                return true;
            }
        }
        return false;
    }

    private boolean checkHwSignatures(PackageInfo pi) {
        boolean hwSign = BaseSignatures.getInstance().contains(HsmPkgInfo.getSignaturesCode(pi));
        HwLog.i(TAG, "this new installed app has huawei signatures?" + hwSign);
        return hwSign;
    }

    public void loadData() {
        synchronized (this) {
            this.mApps.clear();
            SmcsDbHelper.getRecordProtectAppFromDb(this.mContext, this.mApps);
        }
    }

    public void checkPackageFullInner() {
        HwLog.i(TAG, "begin to check protect packges");
        long time1 = System.currentTimeMillis();
        SmcsDbHelper.refreshDefaultValueTable(this.mContext);
        long time2 = System.currentTimeMillis();
        HwLog.i(TAG, "checkPackageFullInner refresh table cost time:" + (time2 - time1));
        synchronized (this) {
            HashSet<String> existPkgs = Sets.newHashSet(this.mApps.keySet());
        }
        Map<String, Boolean> controlledMap = SmcsDbHelper.getAllDefaultControledMap(this.mContext);
        List<HsmPkgInfo> pkgList = HsmPackageManager.getInstance().getAllPackages();
        HwLog.i(TAG, "controlledMap size= " + controlledMap.size() + " pkgList size= " + pkgList.size());
        for (HsmPkgInfo info : pkgList) {
            String pkgName = info.getPackageName();
            boolean currentControl = existPkgs.contains(pkgName);
            Boolean custControlled = (Boolean) controlledMap.get(pkgName);
            if (currentControl && isHealthApp(pkgName)) {
                setNoControlled(pkgName);
                HwLog.i(TAG, "huawei health should always be controlled");
            } else if (custControlled == null) {
                boolean defaultControllState = isDefalutControlled(info);
                if (defaultControllState && !currentControl) {
                    installAppInner(pkgName);
                } else if (!defaultControllState && currentControl) {
                    setNoControlled(pkgName);
                }
            } else if ((custControlled.booleanValue() ^ currentControl) != 0) {
                if (custControlled.booleanValue()) {
                    installAppInner(pkgName);
                } else {
                    setNoControlled(pkgName);
                }
            }
        }
        HwLog.i(TAG, "checkPackageFullInner check data cost time:" + (System.currentTimeMillis() - time2));
        notifyListenerDataRefresh();
    }

    public static boolean isDefalutControlled(HsmPkgInfo info) {
        if (info == null) {
            return false;
        }
        String pkgName = info.getPackageName();
        if (!isCtsApp(pkgName)) {
            return !info.isSystem() || info.isRemoveAblePreInstall();
        } else {
            HwLog.i(TAG, pkgName + " is cts app, did not controlled");
            return false;
        }
    }

    private static boolean isCtsApp(String pkg) {
        if (!TextUtils.isEmpty(pkg) && pkg.startsWith("com.android.cts.")) {
            return true;
        }
        return false;
    }

    private static boolean isHealthApp(String pkg) {
        if (!TextUtils.isEmpty(pkg) && pkg.equalsIgnoreCase("com.huawei.health")) {
            return true;
        }
        return false;
    }

    public boolean checkExsist(String pkgName) {
        boolean z;
        synchronized (this) {
            z = this.mApps.get(pkgName) != null;
        }
        return z;
    }

    public boolean setProtect(ProtectAppItem item, boolean protect) {
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

    public ArrayList<String> getAppointList(int isWhite) {
        Context ctx = GlobalContext.getContext();
        ArrayList<String> list = Lists.newArrayList();
        SmcsDbHelper.getListFromDB(list, isWhite, ctx);
        return list;
    }

    public static boolean isDefaultProtect(Context context, String pkg) {
        int protectValue = SmcsDbHelper.getSinlgeProtectState(context, pkg);
        if (protectValue == 2) {
            return true;
        }
        if (protectValue == 1) {
            return false;
        }
        BackgroundConfigBean cloudBean = CloudDBAdapter.getInstance(context).getSingleBackgroundConfig(pkg);
        if (cloudBean == null || !cloudBean.isProtected()) {
            return HsmPackageManager.getInstance().isPreInstalled(pkg) ? false : false;
        } else {
            HwLog.i(TAG, "pkg:" + pkg + " is  default protect in cloud");
            return true;
        }
    }

    public int getDefaultProtectNum(List<ProtectAppItem> mlist) {
        int totalNum = 0;
        for (ProtectAppItem item : mlist) {
            if (isDefaultProtect(this.mContext, item.getPkgInfo().getPackageName())) {
                totalNum++;
            }
        }
        return totalNum;
    }

    public void updateLocalBkgConfigFromCloud(Context context) {
        List<BackgroundConfigBean> cloudBeanList = CloudDBAdapter.getInstance(context).getAllBackgroundConfig();
        if (Utility.isNullOrEmptyList(cloudBeanList)) {
            HwLog.w(TAG, "updateLocalBkgConfigFromCloud: get Empty cloud config");
            return;
        }
        HwLog.i(TAG, "updateLocalBkgConfigFromCloud: cloudBeanList size = " + cloudBeanList.size());
        Map<String, Boolean> mapRecordsNotChangedByUsr = SmcsDbHelper.getRecordProtectAppNotChangeByUsrFromDb(context);
        if (mapRecordsNotChangedByUsr.isEmpty()) {
            HwLog.w(TAG, "updateLocalBkgConfigFromCloud: Local config list is empty");
            return;
        }
        HwLog.i(TAG, "updateLocalBkgConfigFromCloud: Count of Records not changed by user = " + mapRecordsNotChangedByUsr.size());
        Map<String, Boolean> mapChangedConfig = HsmCollections.newArrayMap();
        for (BackgroundConfigBean cloudbean : cloudBeanList) {
            Boolean localConfig = (Boolean) mapRecordsNotChangedByUsr.get(cloudbean.getPkgName());
            if (localConfig != null) {
                if (localConfig.equals(Boolean.valueOf(cloudbean.isProtected()))) {
                    HwLog.i(TAG, "updateLocalBkgConfigFromCloud: Config not changed ,pkg = " + cloudbean.getPkgName() + ", localConfig = " + localConfig.booleanValue() + ", cloudbean.isProtected() = " + cloudbean.isProtected());
                } else {
                    mapChangedConfig.put(cloudbean.getPkgName(), Boolean.valueOf(cloudbean.isProtected()));
                    HwLog.i(TAG, "updateLocalBkgConfigFromCloud: Config change ,package = " + cloudbean.getPkgName() + ", new config = " + cloudbean.isProtected());
                }
            }
        }
        if (mapChangedConfig.isEmpty()) {
            HwLog.i(TAG, "updateLocalBkgConfigFromCloud: No change is found");
            return;
        }
        HwLog.i(TAG, "updateLocalBkgConfigFromCloud: Count of changed config = " + mapChangedConfig.size());
        SmcsDbHelper.updateProtectAppFromCloud(context, mapChangedConfig);
        sendMessage(5, null);
    }

    protected Object getObjectLock() {
        return this;
    }

    public static String getTAG() {
        return TAG;
    }
}

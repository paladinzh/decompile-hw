package com.huawei.systemmanager.netassistant.traffic.datasaver;

import android.content.Context;
import android.util.SparseBooleanArray;
import com.huawei.systemmanager.comm.xml.XmlParserException;
import com.huawei.systemmanager.comm.xml.XmlParsers;
import com.huawei.systemmanager.comm.xml.base.SimpleXmlRow;
import com.huawei.systemmanager.netassistant.netapp.datasource.NetAppManager;
import com.huawei.systemmanager.netassistant.traffic.datasaver.IDataSaver.Data;
import com.huawei.systemmanager.netassistant.traffic.datasaver.IDataSaver.Data.Listener;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DataSaverDataCenter extends DataSaverFWKApi implements Data {
    public static final Comparator<DataSaverEntry> ALPHA_COMPARATOR = new Comparator<DataSaverEntry>() {
        private final Collator sCollator = Collator.getInstance();

        public int compare(DataSaverEntry object1, DataSaverEntry object2) {
            int compareResult = this.sCollator.compare(object1.title, object2.title);
            if (compareResult != 0) {
                return compareResult;
            }
            compareResult = this.sCollator.compare(object1.pkgName, object2.pkgName);
            if (compareResult != 0) {
                return compareResult;
            }
            return object1.uid - object2.uid;
        }
    };
    private static final String PROTECT_APP_LIST_FILE_PATH = "netaccess/hsm_netaccess_protect_apps.xml";
    private static final String TAG = DataSaverDataCenter.class.getSimpleName();
    public static final Comparator<DataSaverEntry> WHITE_BLACK_LIST_COMPARATOR = new Comparator<DataSaverEntry>() {
        public int compare(DataSaverEntry object1, DataSaverEntry object2) {
            if (object1.isWhiteListed && object2.isWhiteListed) {
                if (object1.isBlackListed && !object2.isBlackListed) {
                    return 1;
                }
                if (!object1.isBlackListed && object2.isBlackListed) {
                    return -1;
                }
            }
            if (object1.isWhiteListed && !object2.isWhiteListed) {
                return -1;
            }
            if (object1.isWhiteListed || !object2.isWhiteListed) {
                return DataSaverDataCenter.ALPHA_COMPARATOR.compare(object1, object2);
            }
            return 1;
        }
    };
    SparseBooleanArray mBlacklist;
    private final ArrayList<Listener> mListeners = new ArrayList();
    SparseBooleanArray mWhitelist;

    public DataSaverDataCenter(Context context) {
        super(context);
    }

    protected void onDataSaverStateChanged(boolean enable) {
        notifyDataSaverStateChanged(enable);
    }

    protected void onWhitelistRefreshed(int uid, boolean whitelisted) {
        refreshWhiteList(uid, whitelisted);
        handleWhitelistRefreshed(uid, whitelisted);
    }

    protected void onBlacklistChanged(int uid, boolean blacklisted) {
        refreshBlackList(uid, blacklisted);
        handleBlacklistChanged(uid, blacklisted);
    }

    public List<DataSaverEntry> getList(int type) {
        boolean mIsSystem = true;
        List<DataSaverEntry> cacheList = new ArrayList();
        if (type != 1) {
            mIsSystem = false;
        }
        getDataByHsmPackageManager(cacheList, mIsSystem);
        Comparator<DataSaverEntry> comparator = WHITE_BLACK_LIST_COMPARATOR;
        try {
            Collections.sort(cacheList, comparator);
        } catch (IllegalArgumentException e) {
            HwLog.e(TAG, "Error happens when sorting the app list. Comparator name" + (comparator != null ? comparator.getClass().getName() : ""));
        }
        return cacheList;
    }

    private void getDataByHsmPackageManager(List<DataSaverEntry> cacheList, boolean mIsSystem) {
        for (HsmPkgInfo info : HsmPackageManager.getInstance().getAllPackages()) {
            DataSaverEntry entry = new DataSaverEntry();
            entry.uid = info.getUid();
            entry.pkgName = info.getPackageName();
            entry.title = info.label();
            entry.isWhiteListed = isWhitelisted(info.getUid());
            entry.isBlackListed = isBlacklisted(info.getUid());
            entry.isProtectListed = isProtectListed(info.getPackageName());
            if (NetAppManager.packageCanAccessInternet(this.mPm, info.getPackageName())) {
                if (mIsSystem) {
                    if (!info.isRemovable()) {
                        cacheList.add(entry);
                    }
                } else if (info.isRemovable()) {
                    cacheList.add(entry);
                }
            }
        }
    }

    private boolean isProtectListed(String packageName) {
        return getProtectedList().contains(packageName);
    }

    public List<String> getProtectedList() {
        List<String> list = new ArrayList();
        try {
            for (SimpleXmlRow row : XmlParsers.assetSimpleXmlRows(this.mContext, PROTECT_APP_LIST_FILE_PATH)) {
                list.add(row.getAttrValue("name"));
            }
        } catch (XmlParserException e) {
            HwLog.e(TAG, "initXml failed", e);
        } catch (Exception e2) {
            HwLog.e(TAG, "initXml failed", e2);
        }
        return list;
    }

    public static void initDaSaverProtectedList(Context context) {
        DataSaverDataCenter dataSaverDataCenter = new DataSaverDataCenter(context);
        for (String protectedApp : dataSaverDataCenter.getProtectedList()) {
            if (HsmPackageManager.getInstance().isPreInstalled(protectedApp)) {
                HsmPkgInfo info = HsmPackageManager.getInstance().getPkgInfo(protectedApp);
                if (info != null) {
                    dataSaverDataCenter.setWhiteListed(info.getUid(), true, protectedApp);
                }
            }
        }
        dataSaverDataCenter.release();
    }

    public static void initDaSaverProtectedList(Context context, String pkgName, int uid) {
        DataSaverDataCenter dataSaverDataCenter = new DataSaverDataCenter(context);
        if (dataSaverDataCenter.isProtectListed(pkgName)) {
            dataSaverDataCenter.setWhiteListed(uid, true, pkgName);
        }
        dataSaverDataCenter.release();
    }

    public void registerListner(Listener listener) {
        if (!this.mListeners.contains(listener)) {
            this.mListeners.add(listener);
        }
        if (this.mListeners.size() == 1) {
            super.registerListener();
        }
        listener.onDataSaverStateChange(isDataSaverEnabled());
    }

    public void unregisterListener(Listener listener) {
        if (this.mListeners.contains(listener)) {
            this.mListeners.remove(listener);
        }
        if (this.mListeners.size() == 0) {
            super.unRegisterListener();
        }
    }

    public void setWhiteListed(int uid, boolean whitelisted, String packageName) {
        if (this.mWhitelist == null) {
            loadWhitelist();
        }
        this.mWhitelist.put(uid, whitelisted);
        if (whitelisted) {
            super.addRestrictBackgroundWhitelistedUid(uid, packageName);
        } else {
            super.removeRestrictBackgroundWhitelistedUid(uid, packageName);
        }
    }

    public boolean isWhitelisted(int uid) {
        if (this.mWhitelist == null) {
            loadWhitelist();
        }
        return this.mWhitelist.get(uid);
    }

    private void loadWhitelist() {
        this.mWhitelist = new SparseBooleanArray();
        int[] uids = super.getRestrictBackgroundWhitelistedUids();
        if (uids == null) {
            HwLog.w(TAG, "loadWhitelist uids is null");
            return;
        }
        for (int uid : uids) {
            this.mWhitelist.put(uid, true);
        }
    }

    private void handleWhitelistRefreshed(int uid, boolean whitelisted) {
        for (Listener listener : this.mListeners) {
            listener.onWhiteListStatusChanged(uid, whitelisted);
        }
    }

    private void refreshWhiteList(int uid, boolean whitelisted) {
        if (this.mWhitelist == null) {
            loadWhitelist();
        }
        this.mWhitelist.put(uid, whitelisted);
    }

    public boolean isBlacklisted(int uid) {
        loadBlacklist();
        return this.mBlacklist.get(uid);
    }

    private void loadBlacklist() {
        this.mBlacklist = new SparseBooleanArray();
        int[] uids = super.getUidsWithPolicy();
        if (uids == null) {
            HwLog.w(TAG, "loadBlacklist uids is null");
            return;
        }
        for (int uid : uids) {
            this.mBlacklist.put(uid, true);
        }
    }

    private void handleBlacklistChanged(int uid, boolean blacklisted) {
        for (Listener listener : this.mListeners) {
            listener.onBlackListStatusChanged(uid, blacklisted);
        }
    }

    private void refreshBlackList(int uid, boolean blacklisted) {
        if (this.mBlacklist == null) {
            loadBlacklist();
        }
        this.mBlacklist.put(uid, blacklisted);
    }

    private void notifyDataSaverStateChanged(boolean enable) {
        for (Listener listener : this.mListeners) {
            listener.onDataSaverStateChange(enable);
        }
    }

    public void setDataSaverEnable(boolean enable) {
        super.setDataSaverEnable(enable);
    }

    public boolean isDataSaverEnabled() {
        return super.isDataSaverEnabled();
    }

    public void release() {
        if (this.mBlacklist != null) {
            this.mBlacklist.clear();
        }
        if (this.mWhitelist != null) {
            this.mWhitelist.clear();
        }
        this.mListeners.clear();
    }
}

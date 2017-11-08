package com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.XmlResourceParser;
import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.Storage.PathEntry;
import com.huawei.systemmanager.comm.Storage.PathEntrySet;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.db.SpaceCleannerDBManager;
import com.huawei.systemmanager.util.HwLog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;

public class HwCustAppDataObtain {
    public static final String METADATA_FILE_NAME = "trim_policy";
    public static final String PROTECT_FATH_ID = "protectpath";
    public static final String PROTECT_FILE_TAG = "ProtectPreference";
    public static final String TAG = "HwCustAppDataObtain";
    public static final String TRASH_FATH_ID = "trashpath";
    public static final String TRASH_FILE_TAG = "CleanPreference";
    public static final String TRASH_KEPP_LATEST_ID = "keeplatest";
    public static final String TRASH_KEPP_TIME_ID = "keeptime";
    public static final String TRASH_MATCH_RULE_ID = "matchrule";
    public static final String TRASH_RECOMMEND_ID = "recommended";
    public static final String TRASH_TYPE_ID = "trashtype";
    public static final String TRIM_POLICY_PERMISSION = "com.huawei.systemmanager.permission.APPLY_TRIM_POLICY";
    private static final Object sMutexHwCustAppTrashInfo = new Object();
    private static HwCustAppDataObtain sSingleton;
    private Context mContext = GlobalContext.getContext();

    private HwCustAppDataObtain() {
    }

    public static HwCustAppDataObtain getInstance() {
        HwCustAppDataObtain hwCustAppDataObtain;
        synchronized (sMutexHwCustAppTrashInfo) {
            if (sSingleton == null) {
                sSingleton = new HwCustAppDataObtain();
            }
            hwCustAppDataObtain = sSingleton;
        }
        return hwCustAppDataObtain;
    }

    public static void destroyInstance() {
        synchronized (sMutexHwCustAppTrashInfo) {
            sSingleton = null;
        }
    }

    public void getAllHwCustTrash() {
        new Thread("GetAllHwCustAppTrash") {
            public void run() {
                HwCustAppDataObtain.this.getTrashInfos();
            }
        }.start();
    }

    public void getHwCustTrash(final PackageInfo pi) {
        new Thread("GetHwCustAppTrash") {
            public void run() {
                HwCustAppDataObtain.this.getTrash(pi);
            }
        }.start();
    }

    public void deleteHwCustTrash(final String pkgName) {
        new Thread("deleteHwCustTrash") {
            public void run() {
                HwCustAppDataObtain.this.deleteTrash(pkgName);
            }
        }.start();
    }

    private void getTrashInfos() {
        String[] perm = new String[]{"com.huawei.systemmanager.permission.APPLY_TRIM_POLICY"};
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            HwLog.e(TAG, "pm is null");
            return;
        }
        Iterable pkgs = null;
        try {
            pkgs = pm.getPackagesHoldingPermissions(perm, 128);
        } catch (NullPointerException e) {
            HwLog.e(TAG, "NullPointerException e");
        }
        if (r4 == null) {
            HwLog.e(TAG, "pkgs is null");
        } else if (r4.size() == 0) {
            HwLog.e(TAG, "pkgs size is 0, there is no cust app.");
        } else {
            for (PackageInfo pi : r4) {
                getTrash(pi);
            }
        }
    }

    private void getTrash(PackageInfo pi) {
        if (pi == null || pi.applicationInfo == null) {
            HwLog.e(TAG, "pkgs info is null.");
        } else if (pi.applicationInfo.metaData != null) {
            try {
                int resId = pi.applicationInfo.metaData.getInt(METADATA_FILE_NAME);
                Context pkgCtx = this.mContext.createPackageContext(pi.packageName, 0);
                if (pkgCtx == null) {
                    HwLog.e(TAG, "pkgCtx is null, the packagename is: " + pi.packageName);
                    return;
                }
                storeCustAPPTrash(parseTrimPolicy(pi.packageName, pkgCtx.getResources().getXml(resId)));
            } catch (NameNotFoundException e) {
                HwLog.e(TAG, "Exception msg is: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e2) {
                HwLog.e(TAG, "Exception msg is: " + e2.getMessage());
                e2.printStackTrace();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private HwCustAPPDataInfo parseTrimPolicy(String pkg, XmlResourceParser parser) {
        ArrayList<String> protectPaths = new ArrayList();
        ArrayList<HwCustTrashInfo> trash = new ArrayList();
        try {
            int event = parser.getEventType();
            while (event != 1) {
                switch (event) {
                    case 2:
                        if (PROTECT_FILE_TAG.equals(parser.getName())) {
                            String path = parser.getAttributeValue(null, PROTECT_FATH_ID);
                            if (HwCustAPPDataFilter.isProtectPathValid(pkg, path)) {
                                protectPaths.add(path);
                                break;
                            }
                        } else if (TRASH_FILE_TAG.equals(parser.getName())) {
                            String trashPath = parser.getAttributeValue(null, TRASH_FATH_ID);
                            if (!TextUtils.isEmpty(trashPath)) {
                                String type = parser.getAttributeValue(null, TRASH_TYPE_ID);
                                String recommend = parser.getAttributeValue(null, TRASH_RECOMMEND_ID);
                                String matchRule = parser.getAttributeValue(null, TRASH_MATCH_RULE_ID);
                                String time = parser.getAttributeValue(null, TRASH_KEPP_TIME_ID);
                                String latest = parser.getAttributeValue(null, TRASH_KEPP_LATEST_ID);
                                int trashType = 0;
                                boolean z = true;
                                int keepTime = 0;
                                int keepLatest = 0;
                                if (!TextUtils.isEmpty(type)) {
                                    trashType = Integer.parseInt(type);
                                }
                                if (!TextUtils.isEmpty(recommend)) {
                                    z = Boolean.parseBoolean(recommend);
                                }
                                if (!TextUtils.isEmpty(time)) {
                                    keepTime = Integer.parseInt(time);
                                }
                                if (!TextUtils.isEmpty(latest)) {
                                    keepLatest = Integer.parseInt(latest);
                                }
                                if (!TextUtils.isEmpty(trashPath) && HwCustAPPDataFilter.isTrashPathValid(pkg, trashPath)) {
                                    trash.add(new HwCustTrashInfo(pkg, trashPath, trashType, z, matchRule, keepTime, keepLatest));
                                    break;
                                }
                            }
                            continue;
                        }
                        break;
                }
                event = parser.next();
            }
        } catch (XmlPullParserException e) {
            HwLog.e(TAG, "get trash info failed, message is: " + e.getMessage() + " and pkg is: " + pkg);
            e.printStackTrace();
        } catch (IOException e2) {
            HwLog.e(TAG, "get trash info failed, message is: " + e2.getMessage() + " and pkg is: " + pkg);
            e2.printStackTrace();
        } catch (Exception e3) {
            HwLog.e(TAG, "get trash info failed, message is: " + e3.getMessage() + " and pkg is: " + pkg);
            e3.printStackTrace();
        }
        return new HwCustAPPDataInfo(pkg, protectPaths, trash);
    }

    private synchronized void storeCustAPPTrash(HwCustAPPDataInfo trash) {
        if (trash == null) {
            HwLog.e(TAG, "trash data is null.");
            return;
        }
        SpaceCleannerDBManager dbManager = SpaceCleannerDBManager.getInstance();
        dbManager.setProtectPaths(trash.getPkgName(), trash.getProtectPath());
        dbManager.setTrashInfos(trash.getPkgName(), trash.getTrash());
    }

    private synchronized void deleteTrash(String pkgName) {
        SpaceCleannerDBManager dbManager = SpaceCleannerDBManager.getInstance();
        dbManager.deleteProtectPath(pkgName);
        dbManager.deletePkgTrashInfo(pkgName);
    }

    public synchronized List<HwCustTrashInfo> getHwCustTrashs() {
        List<HwCustTrashInfo> trashInfos;
        trashInfos = SpaceCleannerDBManager.getInstance().getTrashInfo();
        List<String> downloadFilterPaths = Lists.newArrayList();
        for (HwCustTrashInfo trash : trashInfos) {
            String path = trash.getTrashPath();
            if (path.startsWith(HwCustTrashConst.DOWMLOAD_DEFAULT_PATH)) {
                downloadFilterPaths.add(path);
            }
        }
        HwCustTrashInfo systemDownLoadtrash = new HwCustTrashInfo(HwCustTrashConst.DOWMLOAD_DEFAULT_NAME, HwCustTrashConst.DOWMLOAD_DEFAULT_PATH, 11, false, "", 0, 0);
        systemDownLoadtrash.setFilterPaths(downloadFilterPaths);
        trashInfos.add(systemDownLoadtrash);
        return trashInfos;
    }

    public synchronized List<String> getProtectPath() {
        return SpaceCleannerDBManager.getInstance().getProtectPaths();
    }

    public synchronized List<String> getTrashPath() {
        return SpaceCleannerDBManager.getInstance().getTrashPaths();
    }

    public List<String> getHwCustProtectPath(PathEntrySet entrySet) {
        List<String> list = getProtectPath();
        List<String> ingorePathes = Lists.newArrayList();
        List<PathEntry> entries = entrySet.getPathEntry();
        for (String row : list) {
            String row2 = getValidPath(row2);
            for (PathEntry entry : entries) {
                ingorePathes.add(entry.mPath + row2);
            }
        }
        return ingorePathes;
    }

    private String getValidPath(String path) {
        String result = path;
        if (TextUtils.isEmpty(path)) {
            HwLog.e(TAG, "path is empty");
            return result;
        } else if (path.endsWith("/")) {
            return result;
        } else {
            return path + "/";
        }
    }
}

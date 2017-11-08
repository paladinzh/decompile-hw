package com.huawei.permissionmanager.db;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import com.huawei.permission.HoldServiceConst;
import com.huawei.permissionmanager.utils.HwPermissionInfo;
import com.huawei.permissionmanager.utils.ShareLib;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comparator.AlpComparator;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import com.huawei.systemmanager.util.procpolicy.ProcessPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppInfo {
    static final /* synthetic */ boolean -assertionsDisabled;
    public static final int MASK_NOT_INSTALLED_APP = -1;
    public static final AlpComparator<AppInfo> PERMISSIONMANAGER_ALP_COMPARATOR = new AlpComparator<AppInfo>() {
        public String getStringKey(AppInfo t) {
            return t.mAppLabel;
        }
    };
    private static Map<String, Integer> mPermissionToType = getPermissionType();
    public String mAppLabel;
    public int mAppUid;
    public int mPermissionCfg;
    public int mPermissionCode;
    public String mPkgName;
    public List<HwPermissionInfo> mRequestPermissions;
    public int mTrust;

    public int getPermCfgByOper(int r1, int r2) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.permissionmanager.db.AppInfo.getPermCfgByOper(int, int):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.permissionmanager.db.AppInfo.getPermCfgByOper(int, int):int");
    }

    public int getPermCodeByOper(int r1, int r2) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.permissionmanager.db.AppInfo.getPermCodeByOper(int, int):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.permissionmanager.db.AppInfo.getPermCodeByOper(int, int):int");
    }

    static {
        boolean z;
        if (AppInfo.class.desiredAssertionStatus()) {
            z = false;
        } else {
            z = true;
        }
        -assertionsDisabled = z;
    }

    public AppInfo() {
        this.mAppLabel = null;
        this.mPkgName = null;
        this.mAppUid = -1;
        this.mPermissionCode = 0;
        this.mTrust = 0;
        this.mPermissionCfg = 0;
        this.mRequestPermissions = new ArrayList();
    }

    public AppInfo(AppInfo appInfo) {
        this.mAppLabel = appInfo.mAppLabel;
        this.mPkgName = appInfo.mPkgName;
        this.mAppUid = appInfo.mAppUid;
        this.mPermissionCode = appInfo.mPermissionCode;
        this.mTrust = appInfo.mTrust;
        this.mPermissionCfg = appInfo.mPermissionCfg;
        this.mRequestPermissions = appInfo.mRequestPermissions;
        if (ProcessPolicy.shoudlCacheLabel() && this.mAppLabel.equals(this.mPkgName)) {
            PackageManager pm = GlobalContext.getContext().getPackageManager();
            try {
                this.mAppLabel = pm.getApplicationInfo(this.mPkgName, 8192).loadLabel(pm).toString().trim();
            } catch (NameNotFoundException e) {
                HwLog.e("AppInfo", "can't get application info:" + this.mPkgName);
            }
        }
    }

    public AppInfo(Context context, ApplicationInfo app) {
        PackageManager pm = context.getPackageManager();
        this.mPkgName = app.packageName;
        if (ProcessPolicy.shoudlCacheLabel()) {
            this.mAppLabel = app.loadLabel(pm).toString().replaceAll("\\s", " ").trim();
        } else {
            this.mAppLabel = this.mPkgName;
        }
        this.mAppUid = app.uid;
        this.mPermissionCode = 0;
        this.mTrust = 0;
        this.mPermissionCfg = 0;
        this.mRequestPermissions = new ArrayList();
    }

    public AppInfo(HsmPkgInfo app) {
        this.mPkgName = app.mPkgName;
        if (ProcessPolicy.shoudlCacheLabel()) {
            this.mAppLabel = app.label();
        } else {
            this.mAppLabel = this.mPkgName;
        }
        this.mAppUid = app.mUid;
        this.mPermissionCode = 0;
        this.mTrust = 0;
        this.mPermissionCfg = 0;
        this.mRequestPermissions = new ArrayList();
    }

    public boolean isTrust() {
        for (HwPermissionInfo info : this.mRequestPermissions) {
            if ((this.mPermissionCfg & info.mPermissionCode) == 0) {
                if ((this.mPermissionCode & info.mPermissionCode) == 0) {
                }
            }
            return false;
        }
        return true;
    }

    public int getValueByType(int type) {
        return DBAdapter.getValue(type, this.mPermissionCode, this.mPermissionCfg);
    }

    public boolean hasPermission(int permissionType) {
        for (HwPermissionInfo info : this.mRequestPermissions) {
            if ((info.mPermissionCode & permissionType) != 0) {
                return true;
            }
        }
        return false;
    }

    public void updateExtra(Intent intent) {
        intent.putExtra(HoldServiceConst.APP_UID, this.mAppUid);
        intent.putExtra("packageName", this.mPkgName);
    }

    public boolean equals(Object obj) {
        if (obj == null || !getClass().isInstance(obj.getClass())) {
            return false;
        }
        boolean result;
        AppInfo appInfoObj = (AppInfo) obj;
        if (this.mPkgName == null || -1 == this.mAppUid) {
            result = false;
        } else if (this.mPkgName.equals(appInfoObj.mPkgName) && this.mAppUid == appInfoObj.mAppUid) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    public int hashCode() {
        if (-assertionsDisabled) {
            return 42;
        }
        throw new AssertionError("hashCode not designed");
    }

    public static int getCodeMaskByRequestedPermissions(String[] requestedPermissions) {
        int mask = 0;
        for (String permission : requestedPermissions) {
            Integer percode = (Integer) mPermissionToType.get(permission);
            if (percode != null && percode.intValue() >= 0) {
                mask |= percode.intValue();
            }
        }
        return mask;
    }

    private static Map<String, Integer> getPermissionType() {
        Map<String, Integer> permissionToType = new HashMap();
        for (HwPermissionInfo info : ShareLib.getControlPermissions()) {
            String[] infoPers = info.mPermissionStr;
            if (infoPers != null && infoPers.length > 0) {
                for (String per : infoPers) {
                    Integer percode = (Integer) permissionToType.get(per);
                    if (percode == null || percode.intValue() < 0) {
                        permissionToType.put(per, Integer.valueOf(info.mPermissionCode));
                    } else {
                        permissionToType.put(per, Integer.valueOf(percode.intValue() | info.mPermissionCode));
                    }
                }
            }
        }
        return permissionToType;
    }

    public static int getComparePermissionCode(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        int compareCode = 0;
        if (packageName == null) {
            return 0;
        }
        try {
            PackageInfo packageInfo = PackageManagerWrapper.getPackageInfo(pm, packageName, 12288);
            if (!(packageInfo == null || packageInfo.requestedPermissions == null)) {
                String[] permissions = packageInfo.requestedPermissions;
                for (HwPermissionInfo info : ShareLib.getControlPermissions()) {
                    if (getPermission(info, permissions, info.misUnit)) {
                        compareCode += ((Integer) ShareLib.getPermissionTypeMaps().get(info)).intValue();
                    }
                }
            }
            return (compareCode | 67108864) | 33554432;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        } catch (Exception e2) {
            e2.printStackTrace();
            return 0;
        }
    }

    private static boolean getPermission(HwPermissionInfo info, String[] permissions, boolean isUnit) {
        int length = permissions.length;
        for (String perStr : info.mPermissionStr) {
            int i = 0;
            while (i < length) {
                if (perStr.equals(permissions[i])) {
                    if (!isUnit) {
                        return true;
                    }
                } else if (isUnit && i == length - 1) {
                    return false;
                } else {
                    i++;
                }
            }
        }
        return isUnit;
    }
}

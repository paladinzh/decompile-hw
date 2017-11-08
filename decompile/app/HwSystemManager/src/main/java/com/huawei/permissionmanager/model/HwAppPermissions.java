package com.huawei.permissionmanager.model;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import com.google.common.base.Preconditions;
import com.huawei.permission.MPermissionUtil;
import com.huawei.permissionmanager.db.AppInfo;
import com.huawei.permissionmanager.db.DBAdapter;
import com.huawei.permissionmanager.utils.HwPermissionInfo;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.permissionmanager.utils.Utils;
import com.huawei.systemmanager.comm.grule.GRuleManager;
import com.huawei.systemmanager.comm.grule.scene.monitor.MonitorScenario;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HwAppPermissions {
    private static final String TAG = "HwAppPermissions";
    private static final HashMap<String, Integer> grpToType = new HashMap();
    private static final Map<String, String> permissionToGrpName = new HashMap();
    private static final Map<String, Integer> permissionToType = new HashMap();
    private AppInfo mAppInfo;
    private AppPermissions mAppPermissions;
    private List<TypeValuePair> mCalendarPermList = new ArrayList();
    private List<TypeValuePair> mContactsPermList = new ArrayList();
    private Context mContext;
    private boolean mInErrorState = false;
    private boolean mLegacy;
    private PackageInfo mPackageInfo;
    private List<TypeValuePair> mPhonePermList = new ArrayList();
    private boolean mShouldMonitor;
    private HashMap<String, List<TypeValuePair>> mSingles = new HashMap();
    private List<TypeValuePair> mSmsPermList = new ArrayList();
    private HashMap<Integer, TypeValuePair> mTypeToPA = new HashMap();

    public enum SysAllow2Hsm {
        Allow,
        Remind
    }

    public com.huawei.permissionmanager.db.DBPermissionItem getBackupPermissionData(com.huawei.permissionmanager.db.DBPermissionItem r1) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.permissionmanager.model.HwAppPermissions.getBackupPermissionData(com.huawei.permissionmanager.db.DBPermissionItem):com.huawei.permissionmanager.db.DBPermissionItem
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.permissionmanager.model.HwAppPermissions.getBackupPermissionData(com.huawei.permissionmanager.db.DBPermissionItem):com.huawei.permissionmanager.db.DBPermissionItem");
    }

    private void initGroup2SinglesMap() {
        this.mSingles.put(MPermissionUtil.GROUP_CONTACTS, this.mContactsPermList);
        this.mSingles.put(MPermissionUtil.GROUP_SMS, this.mSmsPermList);
        this.mSingles.put(MPermissionUtil.GROUP_PHONE, this.mPhonePermList);
        this.mSingles.put(MPermissionUtil.GROUP_CALENDAR, this.mCalendarPermList);
    }

    public HwAppPermissions(Context context, PackageInfo pkgInfo) {
        boolean z = true;
        if (pkgInfo == null || pkgInfo.applicationInfo == null) {
            HwLog.w(TAG, "get a null packageInfo. enter error state.pkgInfo:" + pkgInfo);
            this.mInErrorState = true;
            return;
        }
        boolean z2;
        if (context != null) {
            z2 = true;
        } else {
            z2 = false;
        }
        try {
            Preconditions.checkArgument(z2, "context can't be null.");
            this.mContext = context;
            String pkgName = pkgInfo.applicationInfo.packageName;
            if (pkgInfo.applicationInfo.targetSdkVersion >= 23) {
                z = false;
            }
            this.mLegacy = z;
            this.mPackageInfo = pkgInfo;
            this.mShouldMonitor = GRuleManager.getInstance().shouldMonitor(context, MonitorScenario.SCENARIO_PERMISSION, pkgName);
            this.mAppPermissions = new AppPermissions(context, pkgInfo, null, false, null);
            DBAdapter.assureAppExistInited(this.mContext, pkgInfo.applicationInfo.uid, pkgName);
            this.mAppInfo = DBAdapter.getInstance(this.mContext).getSingleAppInfo(pkgName);
            initGroupLists();
            initGroup2SinglesMap();
        } catch (NullPointerException e) {
            HwLog.w(TAG, "HwAppPermissions in error state.", e);
        } catch (Exception e2) {
            HwLog.w(TAG, "HwAppPermissions in error state.", e2);
        }
    }

    public static HwAppPermissions create(Context context, String pkg) {
        boolean z;
        boolean z2 = true;
        if (context != null) {
            z = true;
        } else {
            z = false;
        }
        Preconditions.checkArgument(z, "context can't be null.");
        if (pkg == null) {
            z2 = false;
        }
        Preconditions.checkArgument(z2, "context can't be null.");
        PackageInfo info = getPkgInfo(context, pkg);
        if (info == null) {
            HwLog.w(TAG, "getPkgInfo null for " + pkg);
        }
        return new HwAppPermissions(context, info);
    }

    private static PackageInfo getPkgInfo(Context context, String pkg) {
        try {
            return PackageManagerWrapper.getPackageInfo(context.getPackageManager(), pkg, 4096);
        } catch (Exception e) {
            HwLog.w(TAG, "getPkgInfo null for " + pkg);
            return null;
        }
    }

    public static int convertSysValueToHsmValue(boolean legacy, boolean granted, boolean userFixed, SysAllow2Hsm what) {
        if (!legacy) {
            return userFixed ? granted ? 1 : 2 : granted ? 1 : 3;
        } else {
            if (what != SysAllow2Hsm.Allow) {
                return granted ? 3 : 2;
            } else {
                if (granted) {
                    return 1;
                }
                return 2;
            }
        }
    }

    private void setSysGroupValue(boolean forInit, AppPermissionGroup grp, int value, boolean legacy) {
        if (legacy || Utils.shouldShowPermission(grp)) {
            if (1 == value) {
                if (!legacy) {
                    if (Log.HWINFO) {
                        HwLog.i(TAG, "grantRuntimePermissions, to " + grp.getName());
                    }
                    grp.grantRuntimePermissions(false);
                } else if (!forInit) {
                    grp.grantRuntimePermissions(false);
                    HwLog.i(TAG, "grantRuntimePermissions fixed, to " + grp.getName());
                } else if (Log.HWINFO) {
                    HwLog.i(TAG, "ignore grantRuntimePermissions fixed, to " + grp.getName());
                }
            } else if (3 == value) {
                if (legacy) {
                    if (!forInit) {
                        grp.grantRuntimePermissions(false);
                        HwLog.i(TAG, "grantRuntimePermissions fixed(remind), to " + grp.getName());
                    } else if (Log.HWINFO) {
                        HwLog.i(TAG, "ignore grantRuntimePermissions fixed(remind), to " + grp.getName());
                    }
                } else if (!forInit) {
                    grp.revokeRuntimePermissions(false);
                    HwLog.i(TAG, "revokeRuntimePermissions not fixed, to " + grp.getName());
                } else if (Log.HWINFO) {
                    HwLog.i(TAG, "ignore set remind, to " + grp.getName());
                }
            } else if (2 == value) {
                if (legacy) {
                    if (Log.HWINFO) {
                        HwLog.i(TAG, "revokeRuntimePermissions note fixed, to " + grp.getName());
                    }
                    grp.revokeRuntimePermissions(false);
                } else if (!forInit) {
                    if (Log.HWINFO) {
                        HwLog.i(TAG, "revokeRuntimePermissions fixed, to " + grp.getName());
                    }
                    grp.revokeRuntimePermissions(true);
                } else if (Log.HWINFO) {
                    HwLog.i(TAG, "ignore revokeRuntimePermissions fixed, to " + grp.getName());
                }
            }
            return;
        }
        HwLog.w(TAG, "setSysGroupValue trying to set fixed permission:" + grp);
    }

    public void trust() {
    }

    public void setHsmDefaultValues(String reason) {
        if (this.mInErrorState) {
            HwLog.w(TAG, "in error state.");
            return;
        }
        try {
            if (Utility.isTestApp(this.mPackageInfo.packageName)) {
                HwLog.w(TAG, "Don't set sys permission for test.");
                return;
            }
            HashMap<Integer, Integer> usedGroup = getUsedTypesAndValues();
            if (usedGroup != null) {
                for (Entry<Integer, Integer> entry : usedGroup.entrySet()) {
                    setHsmPermValue(true, ((Integer) entry.getKey()).intValue(), ((Integer) entry.getValue()).intValue(), reason);
                }
            }
        } catch (NullPointerException e) {
            HwLog.w(TAG, "setHsmDefaultValues in error state.", e);
        } catch (Exception e2) {
            HwLog.w(TAG, "setHsmDefaultValues in error state.", e2);
        }
    }

    public void setHsmValuesToSys(String reason, int oldVersion) {
        if (this.mInErrorState) {
            HwLog.w(TAG, "in error state.");
            return;
        }
        try {
            if (Utility.isTestApp(this.mPackageInfo.packageName)) {
                HwLog.w(TAG, "Don't set sys permission for test.");
                return;
            }
            for (HwPermissionInfo hwPermInfo : this.mAppInfo.mRequestPermissions) {
                int type = hwPermInfo.mPermissionCode;
                if ((MPermissionUtil.isClassAType(type) || MPermissionUtil.isClassBType(type)) && (oldVersion > 15 || (type & ShareCfg.PERMISSION_MODIFY_CALENDAR) == 0)) {
                    if (oldVersion > 15 || (type & 2048) == 0) {
                        setSystemPermission(type, DBAdapter.getValue(type, this.mAppInfo.mPermissionCode, this.mAppInfo.mPermissionCfg), true, reason);
                    } else {
                        int value = DBAdapter.getValue(type, this.mAppInfo.mPermissionCode, this.mAppInfo.mPermissionCfg);
                        setSystemPermission(2048, value, true, reason);
                        setSystemPermission(ShareCfg.PERMISSION_MODIFY_CALENDAR, value, true, reason);
                    }
                }
            }
        } catch (NullPointerException e) {
            HwLog.w(TAG, "setHsmValuesToSys in error state.", e);
        } catch (Exception e2) {
            HwLog.w(TAG, "setHsmValuesToSys in error state.", e2);
        }
    }

    private HashMap<Integer, Integer> getUsedTypesAndValues() {
        HashMap<Integer, Integer> result = new HashMap();
        if (this.mAppInfo == null) {
            return null;
        }
        for (HwPermissionInfo hwPermInfo : this.mAppInfo.mRequestPermissions) {
            int type = hwPermInfo.mPermissionCode;
            int groupCode = ((Integer) MPermissionUtil.typeToPermCode.get(type, Integer.valueOf(-1))).intValue();
            if (-1 != groupCode) {
                result.put(Integer.valueOf(groupCode), Integer.valueOf(DBAdapter.getValue(type, this.mAppInfo.mPermissionCode, this.mAppInfo.mPermissionCfg)));
            }
        }
        return result;
    }

    public void setSystemPermission(int type, int value, boolean couldFixed, String reason) {
        int i = 0;
        if (this.mInErrorState) {
            HwLog.w(TAG, "in error state.");
            return;
        }
        try {
            HwLog.i(TAG, "setSystemPermission for " + this.mPackageInfo.packageName + ", type:" + type + ", value:" + value + ", reason:" + reason);
            if (value == 3) {
                value = this.mLegacy ? 1 : 3;
            }
            if (!(MPermissionUtil.isClassAType(type) || MPermissionUtil.isClassBType(type))) {
                HwLog.w(TAG, "setSystemPermission for non system permission type:" + type);
            }
            String groupName = (String) MPermissionUtil.typeToPermGroup.get(type);
            if (groupName == null) {
                HwLog.w(TAG, "setSystemPermission for " + this.mPackageInfo.packageName + " has no group corresponding to type " + type + ", reason:" + reason);
                return;
            }
            AppPermissionGroup grp = this.mAppPermissions.getPermissionGroup(groupName);
            if (grp == null) {
                HwLog.w(TAG, "setSystemPermission for " + this.mPackageInfo.packageName + " has no corresponding group. should not happen." + ", reason:" + reason);
            } else if (Utils.shouldShowPermission(grp, this.mPackageInfo.packageName)) {
                String[] filterPermissions = null;
                if (MPermissionUtil.isClassAType(type) && !expandGroup(grp, type, value, this.mLegacy)) {
                    filterPermissions = new String[]{(String) MPermissionUtil.typeToSinglePermission.get(type)};
                }
                String str;
                StringBuilder append;
                if (1 == value) {
                    if (Log.HWINFO) {
                        str = TAG;
                        append = new StringBuilder().append("setSystemPermission grantRuntimePermissions fixed, to ").append(grp.getName()).append(", filter size:");
                        if (filterPermissions != null) {
                            i = filterPermissions.length;
                        }
                        HwLog.i(str, append.append(i).toString());
                    }
                    grp.grantRuntimePermissions(false, filterPermissions);
                } else if (2 == value) {
                    if (Log.HWINFO) {
                        str = TAG;
                        append = new StringBuilder().append("setSystemPermission revokeRuntimePermissions fixed:").append(couldFixed).append(", to ").append(grp.getName()).append(", filter size:");
                        if (filterPermissions != null) {
                            i = filterPermissions.length;
                        }
                        HwLog.i(str, append.append(i).toString());
                    }
                    grp.revokeRuntimePermissions(couldFixed, filterPermissions);
                } else if (3 == value) {
                    if (this.mLegacy) {
                        if (Log.HWINFO) {
                            str = TAG;
                            append = new StringBuilder().append("setSystemPermission grantRuntimePermissions remind, to ").append(grp.getName()).append(", filter size:");
                            if (filterPermissions != null) {
                                i = filterPermissions.length;
                            }
                            HwLog.i(str, append.append(i).toString());
                        }
                        grp.grantRuntimePermissions(false, filterPermissions);
                    } else {
                        if (Log.HWINFO) {
                            HwLog.i(TAG, "setSystemPermission revokeRuntimePermissions remind, to " + grp.getName() + ", filter size:" + (filterPermissions == null ? 0 : filterPermissions.length));
                        }
                        grp.revokeRuntimePermissions(false, filterPermissions);
                    }
                }
            } else {
                HwLog.w(TAG, "setSystemPermission for " + this.mPackageInfo.packageName + "should not show, so don't set it." + ", reason:" + reason);
            }
        } catch (NullPointerException e) {
            HwLog.w(TAG, "setSystemPermission in error state.", e);
        } catch (Exception e2) {
            HwLog.w(TAG, "setSystemPermission in error state.", e2);
        }
    }

    public static boolean expandGroup(AppPermissionGroup grp, int type, int value, boolean legacy) {
        String singleName = (String) MPermissionUtil.typeToSinglePermission.get(type);
        if (singleName == null) {
            HwLog.w(TAG, "expandGroup has no single type " + type);
            return false;
        }
        String groupName = (String) MPermissionUtil.typeToPermGroup.get(type);
        if (groupName == null) {
            HwLog.w(TAG, "expandGroup for has no group corresponding to type " + type);
            return false;
        } else if (grp == null) {
            HwLog.w(TAG, "expandGroup has no corresponding group. should not happen.");
            return false;
        } else {
            List<Permission> children = grp.getPermissions();
            boolean granted = legacy ? value != 2 : value == 1;
            HwLog.i(TAG, "expandGroup for " + groupName + ", single:" + singleName + ", allow:" + granted);
            for (Permission child : children) {
                String name = child.getName();
                if (!(permissionToType.get(name) == null || singleName.equals(name))) {
                    boolean isAppOpAllowed = child.isGranted() ? child.isAppOpAllowed() : false;
                    HwLog.i(TAG, "expandGroup for " + groupName + ", single:" + name + ", allow:" + isAppOpAllowed);
                    if (isAppOpAllowed != granted) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    public boolean getSystemPermission(int type) {
        if (this.mInErrorState) {
            HwLog.w(TAG, "in error state.");
            return true;
        }
        try {
            boolean granted;
            if (MPermissionUtil.isClassAType(type) || MPermissionUtil.isClassBType(type)) {
                String groupName = (String) MPermissionUtil.typeToPermGroup.get(type);
                if (groupName == null) {
                    HwLog.w(TAG, "getSystemPermission for " + this.mPackageInfo.packageName + " has no group corresponding to type " + type);
                    return true;
                }
                AppPermissionGroup grp = this.mAppPermissions.getPermissionGroup(groupName);
                if (grp == null) {
                    HwLog.w(TAG, "getSystemPermission for " + this.mPackageInfo.packageName + " has no corresponding group. should not happen.");
                    return true;
                }
                String[] strArr = null;
                if (MPermissionUtil.isClassAType(type)) {
                    strArr = new String[]{(String) MPermissionUtil.typeToSinglePermission.get(type)};
                }
                granted = grp.areRuntimePermissionsGranted(strArr);
                HwLog.i(TAG, "getSystemPermission for type " + type + ", name:" + ((String) MPermissionUtil.typeToSinglePermission.get(type)) + ", filter size:" + (strArr == null ? 0 : strArr.length) + ", granted:" + granted);
                return granted;
            }
            if (MPermissionUtil.isClassEType(type)) {
                granted = this.mAppInfo.getValueByType(type) != 2;
                HwLog.i(TAG, "getSystemPermission for type " + type + ", name:" + ((String) MPermissionUtil.typeToSinglePermission.get(type)) + ", granted:" + granted);
                return granted;
            }
            HwLog.w(TAG, "getSystemPermission for unexcepted type " + type);
            return true;
        } catch (NullPointerException e) {
            HwLog.w(TAG, "getSystemPermission in error state.", e);
        } catch (Exception e2) {
            HwLog.w(TAG, "getSystemPermission in error state.", e2);
        }
    }

    int getSystemPermissionFlag(int type) {
        if (this.mInErrorState) {
            HwLog.w(TAG, "in error state.");
            return 0;
        }
        try {
            if (MPermissionUtil.isClassAType(type) || MPermissionUtil.isClassBType(type)) {
                String groupName = (String) MPermissionUtil.typeToPermGroup.get(type);
                if (groupName == null) {
                    HwLog.w(TAG, "getSystemPermissionFlag for " + this.mPackageInfo.packageName + " has no group corresponding to type " + type);
                    return 0;
                }
                AppPermissionGroup grp = this.mAppPermissions.getPermissionGroup(groupName);
                if (grp == null) {
                    HwLog.w(TAG, "getSystemPermissionFlag for " + this.mPackageInfo.packageName + " has no corresponding group. should not happen.");
                    return 0;
                }
                String singleName = (String) MPermissionUtil.typeToSinglePermission.get(type);
                if (singleName == null) {
                    HwLog.w(TAG, "getSystemPermissionFlag for " + this.mPackageInfo.packageName + " has no singlename.");
                    return 0;
                }
                for (Permission p : grp.getPermissions()) {
                    if (singleName.equals(p.getName())) {
                        return p.getFlags();
                    }
                }
            }
        } catch (NullPointerException e) {
            HwLog.w(TAG, "getSystemPermissionFlag in error state.", e);
        } catch (Exception e2) {
            HwLog.w(TAG, "getSystemPermissionFlag in error state.", e2);
        }
        return 0;
    }

    public void setHsmPermValue(boolean forInit, int type, int value, String reason) {
        if (this.mInErrorState) {
            HwLog.w(TAG, "in error state.");
            return;
        }
        try {
            TypeValuePair pa = (TypeValuePair) this.mTypeToPA.get(Integer.valueOf(type));
            if (pa != null) {
                pa.setValue(value);
            }
            String groupName = (String) MPermissionUtil.typeToPermGroup.get(type);
            if (groupName == null) {
                HwLog.w(TAG, "setHsmPermValue for " + this.mPackageInfo.packageName + " has no group corresponding to type " + type + ", reason:" + reason);
                return;
            }
            AppPermissionGroup grp = this.mAppPermissions.getPermissionGroup(groupName);
            if (grp == null) {
                HwLog.w(TAG, "setHsmPermValue for " + this.mPackageInfo.packageName + " has no corresponding group. should not happen." + ", reason:" + reason);
            } else if (Utils.shouldShowPermission(grp)) {
                int groupValue = value;
                if (isAClassByGroup(grp.getName())) {
                    groupValue = evaluateGroupValue(groupName);
                    if (groupValue == 0) {
                        groupValue = 1;
                    }
                }
                if (Log.HWINFO) {
                    HwLog.i(TAG, "setHsmPermValue for " + this.mPackageInfo.packageName + " type:" + type + ", value:" + value + ", groupValue:" + groupValue + ", mLegacy:" + this.mLegacy + ", target group:" + grp.getName() + ", reason:" + reason);
                }
                setSysGroupValue(forInit, grp, groupValue, this.mLegacy);
            } else {
                HwLog.w(TAG, "setHsmPermValue for " + this.mPackageInfo.packageName + "should not show, so don't set it." + ", reason:" + reason);
            }
        } catch (NullPointerException e) {
            HwLog.w(TAG, "setHsmPermValue in error state.", e);
        } catch (Exception e2) {
            HwLog.w(TAG, "setHsmPermValue in error state.", e2);
        }
    }

    public int evaluateGroupValue(String grpName) {
        if (this.mInErrorState) {
            HwLog.w(TAG, "in error state.");
            return 0;
        }
        try {
            AppPermissionGroup grp = this.mAppPermissions.getPermissionGroup(grpName);
            if (grp == null) {
                HwLog.w(TAG, "evaluateGroupValue, get group null ");
                return 0;
            }
            List<TypeValuePair> singles = (List) this.mSingles.get(grpName);
            int value;
            if (singles == null || singles.size() == 0) {
                boolean granted = grp.areRuntimePermissionsGranted();
                value = convertSysValueToHsmValue(this.mLegacy, granted, grp.isUserFixed(), SysAllow2Hsm.Allow);
                if (Log.HWINFO) {
                    HwLog.i(TAG, "evaluateGroupValue, package:" + this.mPackageInfo.packageName + ", group:" + grpName + ", granted:" + granted + ", fixed:" + grp.isUserFixed() + ", value:" + value);
                }
                return value;
            }
            value = ((TypeValuePair) singles.get(0)).mValue;
            StringBuffer buf = new StringBuffer();
            for (TypeValuePair pa : singles) {
                buf.append("type:" + pa.mType + ", value:" + pa.mValue + " ");
                if (pa.mValue != value) {
                    value = 0;
                }
            }
            if (Log.HWINFO) {
                HwLog.i(TAG, "evaluateGroupValue, package:" + this.mPackageInfo.packageName + ", group:" + grpName + ", value:" + value + ", sub value:" + buf.toString());
            }
            return value;
        } catch (NullPointerException e) {
            HwLog.w(TAG, "evaluateGroupValue in error state.", e);
            return 0;
        } catch (Exception e2) {
            HwLog.w(TAG, "evaluateGroupValue in error state.", e2);
            return 0;
        }
    }

    private void initGroupLists() {
        if (this.mAppInfo == null) {
            HwLog.w(TAG, "gethwPermissionAppList, this app has not in DB, pkg:" + this.mPackageInfo.applicationInfo.packageName + ", monitor?" + this.mShouldMonitor);
            return;
        }
        for (HwPermissionInfo hwPermInfo : this.mAppInfo.mRequestPermissions) {
            TypeValuePair pa = new TypeValuePair(hwPermInfo.mPermissionCode);
            if (this.mTypeToPA.containsKey(Integer.valueOf(pa.mType))) {
                HwLog.w(TAG, "gethwPermissionAppList, already contains this permission by type:" + pa.mType);
            } else {
                pa.setValue(this.mAppInfo.getValueByType(pa.mType));
                String grpName = (String) MPermissionUtil.typeToPermGroup.get(hwPermInfo.mPermissionCode, null);
                if (isContactsGroup(grpName)) {
                    this.mContactsPermList.add(pa);
                } else if (isPhoneGroup(grpName)) {
                    this.mPhonePermList.add(pa);
                } else if (isSmsGroup(grpName)) {
                    this.mSmsPermList.add(pa);
                } else if (isCalendarGroup(grpName)) {
                    this.mCalendarPermList.add(pa);
                }
                this.mTypeToPA.put(Integer.valueOf(pa.mType), pa);
            }
        }
    }

    public static boolean isAClassByGroup(String grpName) {
        if (MPermissionUtil.GROUP_CONTACTS.equals(grpName) || MPermissionUtil.GROUP_PHONE.equals(grpName) || MPermissionUtil.GROUP_SMS.equals(grpName)) {
            return true;
        }
        return MPermissionUtil.GROUP_CALENDAR.equals(grpName);
    }

    public static boolean isPhoneGroup(String grpName) {
        return MPermissionUtil.GROUP_PHONE.equals(grpName);
    }

    public static boolean isContactsGroup(String grpName) {
        return MPermissionUtil.GROUP_CONTACTS.equals(grpName);
    }

    public static boolean isSmsGroup(String grpName) {
        return MPermissionUtil.GROUP_SMS.equals(grpName);
    }

    public static boolean isCalendarGroup(String grpName) {
        return MPermissionUtil.GROUP_CALENDAR.equals(grpName);
    }

    static {
        grpToType.put("android.permission-group.LOCATION", Integer.valueOf(8));
        grpToType.put(MPermissionUtil.GROUP_MICROPHONE, Integer.valueOf(128));
        grpToType.put(MPermissionUtil.GROUP_CAMERA, Integer.valueOf(1024));
        grpToType.put(MPermissionUtil.GROUP_CALENDAR, Integer.valueOf(2048));
        grpToType.put(MPermissionUtil.GROUP_SENSORS, Integer.valueOf(134217728));
        initPermissionToTypeMap(permissionToType);
        permissionToGrpName.put(ShareCfg.LOCATION_COARSE_PERMISSION, "android.permission-group.LOCATION");
        permissionToGrpName.put(ShareCfg.LOCATION_FINE_PERMISSION, "android.permission-group.LOCATION");
        permissionToGrpName.put(ShareCfg.CALL_AND_CONT_READ_PERMISSION, MPermissionUtil.GROUP_CONTACTS);
        permissionToGrpName.put(ShareCfg.CALL_AND_CONT_WRITE_PERMISSION, MPermissionUtil.GROUP_CONTACTS);
        permissionToGrpName.put("android.permission.GET_ACCOUNTS", MPermissionUtil.GROUP_CONTACTS);
        permissionToGrpName.put(ShareCfg.CALENDAR_PERMISSION, MPermissionUtil.GROUP_CALENDAR);
        permissionToGrpName.put(ShareCfg.CALENDAR_WRITE_PERMISSION, MPermissionUtil.GROUP_CALENDAR);
        permissionToGrpName.put(ShareCfg.CALLLOG_RECORD_READ_PERMISSION, MPermissionUtil.GROUP_PHONE);
        permissionToGrpName.put(ShareCfg.CALLLOG_RECORD_WRITE_PERMISSION, MPermissionUtil.GROUP_PHONE);
        permissionToGrpName.put(ShareCfg.CALL_PHONE_PERMISSION, MPermissionUtil.GROUP_PHONE);
        permissionToGrpName.put(ShareCfg.PHONE_STATE_PERMISSION, MPermissionUtil.GROUP_PHONE);
        permissionToGrpName.put("com.android.voicemail.permission.ADD_VOICEMAIL", MPermissionUtil.GROUP_PHONE);
        permissionToGrpName.put("android.permission.USE_SIP", MPermissionUtil.GROUP_PHONE);
        permissionToGrpName.put("android.permission.PROCESS_OUTGOING_CALLS", MPermissionUtil.GROUP_PHONE);
        permissionToGrpName.put(ShareCfg.MSG_RECORD_READ_PERMISSION, MPermissionUtil.GROUP_SMS);
        permissionToGrpName.put("android.permission.RECEIVE_SMS", MPermissionUtil.GROUP_SMS);
        permissionToGrpName.put("android.permission.RECEIVE_MMS", MPermissionUtil.GROUP_SMS);
        permissionToGrpName.put("android.permission.RECEIVE_WAP_PUSH", MPermissionUtil.GROUP_SMS);
        permissionToGrpName.put(ShareCfg.SEND_SHORT_MESSAGE_PERMISSION, MPermissionUtil.GROUP_SMS);
        permissionToGrpName.put("android.permission.READ_CELL_BROADCASTS", MPermissionUtil.GROUP_SMS);
        permissionToGrpName.put(ShareCfg.CAMERA_PERMISSION, MPermissionUtil.GROUP_CAMERA);
        permissionToGrpName.put(ShareCfg.RECORD_AUDIO_PERMISSION, MPermissionUtil.GROUP_MICROPHONE);
        permissionToGrpName.put(ShareCfg.USE_BODY_SENSORS, MPermissionUtil.GROUP_SENSORS);
        permissionToGrpName.put(ShareCfg.READ_STORAGE_PERMISSION, MPermissionUtil.GROUP_STORAGE);
        permissionToGrpName.put(ShareCfg.WRITE_STORAGE_PERMISSION, MPermissionUtil.GROUP_STORAGE);
    }

    public boolean grantRuntimePermission(String groupName, String[] filterPermissions) {
        if (this.mInErrorState) {
            HwLog.w(TAG, "grantRuntimePermission in error state.");
            return false;
        }
        try {
            AppPermissionGroup grp = this.mAppPermissions.getPermissionGroup(groupName);
            if (grp == null) {
                HwLog.w(TAG, "grantRuntimePermission grp null.");
                return false;
            }
            int length;
            String str = TAG;
            StringBuilder append = new StringBuilder().append("grantRuntimePermission ").append(groupName).append(",filter:");
            if (filterPermissions != null) {
                length = filterPermissions.length;
            } else {
                length = 0;
            }
            HwLog.i(str, append.append(length).toString());
            return grp.grantRuntimePermissions(false, filterPermissions);
        } catch (NullPointerException e) {
            HwLog.w(TAG, "grantRuntimePermission in error state.", e);
            return false;
        } catch (Exception e2) {
            HwLog.w(TAG, "grantRuntimePermission in error state.", e2);
            return false;
        }
    }

    public boolean allPermissionsAllowed() {
        if (this.mInErrorState) {
            HwLog.w(TAG, "allPermissionsAllowed in error state.");
            return false;
        }
        try {
            for (AppPermissionGroup grp : this.mAppPermissions.getPermissionGroups()) {
                for (Permission p : grp.getPermissions()) {
                    String name = p.getName();
                    Integer type = (Integer) permissionToType.get(name);
                    if (!(type == null || (type.intValue() & ShareCfg.TRUST_CODE) == 0)) {
                        if (!p.isAppOpAllowed() || !p.isGranted()) {
                            HwLog.i(TAG, "allPermissionsAllowed, not trust caused by:" + name);
                            return false;
                        }
                    }
                }
            }
            if (this.mAppInfo != null) {
                int eClassCode = this.mAppInfo.mPermissionCode & 1192239104;
                if ((this.mAppInfo.mPermissionCfg & eClassCode) != 0) {
                    HwLog.i(TAG, "allPermissionsAllowed, not trust caused by:" + eClassCode + ", cfg:" + this.mAppInfo.mPermissionCfg);
                    return false;
                }
            }
            return true;
        } catch (NullPointerException e) {
            HwLog.w(TAG, "allPermissionsAllowed in error state.", e);
            return false;
        } catch (Exception e2) {
            HwLog.w(TAG, "allPermissionsAllowed in error state.", e2);
            return false;
        }
    }

    public AppInfo getAppInfo() {
        return this.mAppInfo;
    }

    public static int getGroupCount(Context context, String mPkgName) {
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            HwLog.w(TAG, "getGroupCount, pm null.");
            return 0;
        }
        try {
            PackageInfo info = PackageManagerWrapper.getPackageInfo(pm, mPkgName, 4096);
            HashSet<String> groups = new HashSet();
            String[] permissions = info.requestedPermissions;
            if (permissions != null) {
                for (String p : permissions) {
                    String grp = (String) permissionToGrpName.get(p);
                    if (grp != null) {
                        groups.add(grp);
                    }
                }
            }
            return groups.size();
        } catch (Exception e) {
            HwLog.w(TAG, "get info exception for " + mPkgName, e);
            return 0;
        }
    }

    public static void initPermissionToTypeMap(Map<String, Integer> map) {
        if (map != null) {
            map.put(ShareCfg.LOCATION_COARSE_PERMISSION, Integer.valueOf(8));
            map.put(ShareCfg.LOCATION_FINE_PERMISSION, Integer.valueOf(8));
            map.put(ShareCfg.CALL_AND_CONT_READ_PERMISSION, Integer.valueOf(1));
            map.put(ShareCfg.CALL_AND_CONT_WRITE_PERMISSION, Integer.valueOf(16384));
            map.put("android.permission.GET_ACCOUNTS", null);
            map.put(ShareCfg.CALENDAR_PERMISSION, Integer.valueOf(2048));
            map.put(ShareCfg.CALENDAR_WRITE_PERMISSION, Integer.valueOf(ShareCfg.PERMISSION_MODIFY_CALENDAR));
            map.put(ShareCfg.CALLLOG_RECORD_READ_PERMISSION, Integer.valueOf(2));
            map.put(ShareCfg.CALLLOG_RECORD_WRITE_PERMISSION, Integer.valueOf(32768));
            map.put(ShareCfg.CALL_PHONE_PERMISSION, Integer.valueOf(64));
            map.put(ShareCfg.PHONE_STATE_PERMISSION, Integer.valueOf(16));
            map.put("com.android.voicemail.permission.ADD_VOICEMAIL", null);
            map.put("android.permission.USE_SIP", null);
            map.put("android.permission.PROCESS_OUTGOING_CALLS", null);
            map.put(ShareCfg.MSG_RECORD_READ_PERMISSION, Integer.valueOf(4));
            map.put("android.permission.RECEIVE_SMS", null);
            map.put("android.permission.RECEIVE_MMS", null);
            map.put("android.permission.RECEIVE_WAP_PUSH", null);
            map.put(ShareCfg.SEND_SHORT_MESSAGE_PERMISSION, Integer.valueOf(32));
            map.put("android.permission.READ_CELL_BROADCASTS", null);
            map.put(ShareCfg.CAMERA_PERMISSION, Integer.valueOf(1024));
            map.put(ShareCfg.RECORD_AUDIO_PERMISSION, Integer.valueOf(128));
            map.put(ShareCfg.USE_BODY_SENSORS, Integer.valueOf(134217728));
            map.put(ShareCfg.READ_STORAGE_PERMISSION, Integer.valueOf(256));
            map.put(ShareCfg.WRITE_STORAGE_PERMISSION, Integer.valueOf(256));
        }
    }
}

package com.huawei.permission;

import android.util.SparseArray;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import java.util.HashMap;

public class MPermissionUtil {
    public static final int ALLOW_FIXED = 1;
    public static final int FORBID_FIXED = 3;
    public static final String GROUP_CALENDAR = "android.permission-group.CALENDAR";
    public static final String GROUP_CAMERA = "android.permission-group.CAMERA";
    public static final String GROUP_CONTACTS = "android.permission-group.CONTACTS";
    public static final String GROUP_LOCATION = "android.permission-group.LOCATION";
    public static final String GROUP_MICROPHONE = "android.permission-group.MICROPHONE";
    public static final String GROUP_PHONE = "android.permission-group.PHONE";
    public static final String GROUP_SENSORS = "android.permission-group.SENSORS";
    public static final String GROUP_SMS = "android.permission-group.SMS";
    public static final String GROUP_STORAGE = "android.permission-group.STORAGE";
    public static final int INVALID_VALUE = -1;
    public static final int PERMISSION_TYPE_ALLOWED = 1;
    public static final int PERMISSION_TYPE_BLOCKED = 2;
    public static final int PERMISSION_TYPE_FAIL = 0;
    public static final int PERMISSION_TYPE_REMIND = 3;
    public static final int PERMISSION_TYPE_RESET_0 = 4;
    public static final int PERMISSION_TYPE_RESET_2 = 5;
    private static final String TAG = "MPermissionUtil";
    public static final HashMap<String, int[]> grpToTypeArray = new HashMap();
    public static final SparseArray<Integer> typeToPermCode = new SparseArray();
    public static final SparseArray<String> typeToPermGroup = new SparseArray();
    public static final SparseArray<String> typeToSinglePermission = new SparseArray();

    static {
        grpToTypeArray.put("android.permission-group.LOCATION", new int[]{8});
        grpToTypeArray.put(GROUP_MICROPHONE, new int[]{128});
        grpToTypeArray.put(GROUP_CAMERA, new int[]{1024});
        grpToTypeArray.put(GROUP_CALENDAR, new int[]{2048, ShareCfg.PERMISSION_MODIFY_CALENDAR});
        grpToTypeArray.put(GROUP_SENSORS, new int[]{134217728});
        grpToTypeArray.put(GROUP_STORAGE, new int[]{256});
        grpToTypeArray.put(GROUP_CONTACTS, new int[]{1, 16384});
        grpToTypeArray.put(GROUP_PHONE, new int[]{64, 16, 32768, 2});
        grpToTypeArray.put(GROUP_SMS, new int[]{4, 32});
        typeToPermGroup.put(1, GROUP_CONTACTS);
        typeToPermGroup.put(16384, GROUP_CONTACTS);
        typeToPermGroup.put(2, GROUP_PHONE);
        typeToPermGroup.put(16, GROUP_PHONE);
        typeToPermGroup.put(64, GROUP_PHONE);
        typeToPermGroup.put(32768, GROUP_PHONE);
        typeToPermGroup.put(4, GROUP_SMS);
        typeToPermGroup.put(32, GROUP_SMS);
        typeToPermGroup.put(8, "android.permission-group.LOCATION");
        typeToPermGroup.put(128, GROUP_MICROPHONE);
        typeToPermGroup.put(1024, GROUP_CAMERA);
        typeToPermGroup.put(2048, GROUP_CALENDAR);
        typeToPermGroup.put(134217728, GROUP_SENSORS);
        typeToPermGroup.put(ShareCfg.PERMISSION_MODIFY_CALENDAR, GROUP_CALENDAR);
        typeToPermGroup.put(256, GROUP_STORAGE);
        typeToPermCode.put(1, Integer.valueOf(1));
        typeToPermCode.put(16384, Integer.valueOf(1));
        typeToPermCode.put(2, Integer.valueOf(64));
        typeToPermCode.put(16, Integer.valueOf(64));
        typeToPermCode.put(64, Integer.valueOf(64));
        typeToPermCode.put(32768, Integer.valueOf(64));
        typeToPermCode.put(4, Integer.valueOf(4));
        typeToPermCode.put(32, Integer.valueOf(4));
        typeToPermCode.put(8, Integer.valueOf(8));
        typeToPermCode.put(128, Integer.valueOf(128));
        typeToPermCode.put(1024, Integer.valueOf(1024));
        typeToPermCode.put(2048, Integer.valueOf(2048));
        typeToPermCode.put(ShareCfg.PERMISSION_MODIFY_CALENDAR, Integer.valueOf(2048));
        typeToPermCode.put(134217728, Integer.valueOf(134217728));
        typeToPermCode.put(1048576, Integer.valueOf(1048576));
        typeToPermCode.put(1073741824, Integer.valueOf(1073741824));
        typeToPermCode.put(256, Integer.valueOf(256));
        typeToSinglePermission.put(1, ShareCfg.CALL_AND_CONT_READ_PERMISSION);
        typeToSinglePermission.put(16384, ShareCfg.CALL_AND_CONT_WRITE_PERMISSION);
        typeToSinglePermission.put(2, ShareCfg.CALLLOG_RECORD_READ_PERMISSION);
        typeToSinglePermission.put(16, ShareCfg.PHONE_STATE_PERMISSION);
        typeToSinglePermission.put(64, ShareCfg.CALL_PHONE_PERMISSION);
        typeToSinglePermission.put(32768, ShareCfg.CALLLOG_RECORD_WRITE_PERMISSION);
        typeToSinglePermission.put(4, ShareCfg.MSG_RECORD_READ_PERMISSION);
        typeToSinglePermission.put(32, ShareCfg.SEND_SHORT_MESSAGE_PERMISSION);
        typeToSinglePermission.put(8, ShareCfg.LOCATION_COARSE_PERMISSION);
        typeToSinglePermission.put(128, ShareCfg.RECORD_AUDIO_PERMISSION);
        typeToSinglePermission.put(1024, ShareCfg.CAMERA_PERMISSION);
        typeToSinglePermission.put(2048, ShareCfg.CALENDAR_PERMISSION);
        typeToSinglePermission.put(ShareCfg.PERMISSION_MODIFY_CALENDAR, ShareCfg.CALENDAR_WRITE_PERMISSION);
        typeToSinglePermission.put(134217728, ShareCfg.USE_BODY_SENSORS);
        typeToSinglePermission.put(256, ShareCfg.READ_STORAGE_PERMISSION);
    }

    public static boolean shouldControlByHsm(int permType) {
        if (1000 == permType || 1001 == permType) {
            return true;
        }
        if (isClassBType(permType)) {
            return false;
        }
        if (isClassAType(permType)) {
            return false;
        }
        if (isClassOrigDType(permType)) {
            return true;
        }
        return true;
    }

    public static boolean isClassBType(int permType) {
        return (ShareCfg.PERMISSION_B_CLASS & permType) != 0;
    }

    public static boolean isClassAType(int permType) {
        return (ShareCfg.PERMISSION_A_CLASS & permType) != 0;
    }

    public static boolean isClassOrigDType(int permType) {
        return (ShareCfg.PERMISSION_D_CLASS_ORIG & permType) != 0;
    }

    public static boolean isClassDType(int permType) {
        return (ShareCfg.PERMISSION_D_CLASS & permType) != 0;
    }

    public static boolean isClassEType(int permType) {
        return (1192239104 & permType) != 0;
    }

    private static boolean isSystemDefinedPerm(int permType) {
        return ((ShareCfg.PERMISSION_A_CLASS & permType) == 0 && (ShareCfg.PERMISSION_B_CLASS & permType) == 0) ? false : true;
    }

    public static boolean isLegacySystemPermission(String pkgName, int type) {
        HsmPkgInfo info = HsmPackageManager.getInstance().getPkgInfo(pkgName);
        if (info == null) {
            HwLog.w(TAG, "isLegacySystemPermission, fail to get info of " + pkgName);
            return false;
        }
        boolean isLegacy = info.getTargetSdkVersion() <= 22;
        boolean systemDefined = isSystemDefinedPerm(type);
        if (!isLegacy) {
            systemDefined = false;
        }
        return systemDefined;
    }
}

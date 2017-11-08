package com.huawei.systemmanager.netassistant.traffic.appinfo;

import android.os.UserHandle;
import android.os.UserManager;
import android.util.SparseIntArray;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import java.util.ArrayList;
import java.util.List;

public class SpecialUid {
    private static final String BLUETOOTH_PACKAGE = "com.android.bluetooth";
    private static final String HWPUSH_PACKAGE = "com.huawei.android.pushagent";
    private static final String MMS_PACKAGE = "com.android.mms";
    private static final String NFC_PACKAGE = "com.android.nfc";
    public static final int OTHER_USER_RANGE_START = -2000;
    private static final List<String> PackageList = new ArrayList();
    private static final int REMOVED_UID = -4;
    private static final int ROOT_UID = 0;
    private static final int SYSTEM_UID = 1000;
    public static final String TAG = "SpecialUid";
    private static final int TETHERING_UID = -5;
    private static final SparseIntArray UidNameMap = new SparseIntArray();
    private static final SparseIntArray UidUidMap = new SparseIntArray();

    static {
        int i;
        UidNameMap.put(TETHERING_UID, R.string.data_usage_tethering);
        SparseIntArray sparseIntArray = UidNameMap;
        if (UserManager.supportsMultipleUsers()) {
            i = R.string.data_usage_uninstalled_apps_users;
        } else {
            i = R.string.data_usage_uninstalled_apps;
        }
        sparseIntArray.put(REMOVED_UID, i);
        UidNameMap.put(0, R.string.root_uid_label);
        UidNameMap.put(1000, R.string.process_kernel_label);
        UidUidMap.put(TETHERING_UID, TETHERING_UID);
        UidUidMap.put(REMOVED_UID, REMOVED_UID);
        UidUidMap.put(0, 0);
        PackageList.add(MMS_PACKAGE);
        PackageList.add(HWPUSH_PACKAGE);
        for (String pkgName : PackageList) {
            HsmPkgInfo info = HsmPackageManager.getInstance().getPkgInfo(pkgName);
            if (info != null) {
                UidUidMap.put(info.getUid(), info.getUid());
            }
        }
    }

    public static boolean isSpecialNameUid(int uid) {
        return UidNameMap.indexOfKey(uid) >= 0;
    }

    public static int getSpecialUidName(int uid) {
        return UidNameMap.get(uid);
    }

    public static boolean isWhiteListUid(int uid) {
        boolean value;
        if (UidUidMap.indexOfValue(uid) != -1) {
            value = true;
        } else {
            value = false;
        }
        if (value || !UserHandle.isApp(uid)) {
            return true;
        }
        return false;
    }

    public static boolean isOtherUserUid(int uid) {
        return uid <= -2000;
    }

    public static int getWarningTextId(int uid) {
        HsmPkgInfo mmsInfo = HsmPackageManager.getInstance().getPkgInfo(MMS_PACKAGE);
        HsmPkgInfo pushInfo = HsmPackageManager.getInstance().getPkgInfo(HWPUSH_PACKAGE);
        HsmPkgInfo nfcInfo = HsmPackageManager.getInstance().getPkgInfo(NFC_PACKAGE);
        HsmPkgInfo bluetoothInfo = HsmPackageManager.getInstance().getPkgInfo(BLUETOOTH_PACKAGE);
        if (uid == 1000) {
            return R.string.net_assistant_net_app_network_dialog_msg_androidos;
        }
        if (mmsInfo != null && uid == mmsInfo.getUid()) {
            return R.string.net_assistant_net_app_network_dialog_msg_mms;
        }
        if (uid == 1001) {
            return R.string.net_assistant_net_app_network_dialog_msg_phone;
        }
        if (pushInfo != null && uid == pushInfo.getUid()) {
            return R.string.net_assistant_net_app_network_dialog_msg_push;
        }
        if (nfcInfo != null && uid == nfcInfo.getUid()) {
            return R.string.net_assistant_net_app_network_dialog_msg_nfc;
        }
        if (bluetoothInfo == null || uid != bluetoothInfo.getUid()) {
            return R.string.net_assistant_net_app_network_dialog_msg_androidos;
        }
        return R.string.net_assistant_net_app_network_dialog_msg_bluetooth;
    }

    public static boolean isSystemAccount(int uid) {
        return uid == 0;
    }
}

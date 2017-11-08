package com.huawei.permissionmanager.utils;

import android.util.SparseArray;
import com.huawei.permissionmanager.utils.SingleAppPermissionHelper.PermissionItem;
import com.huawei.systemmanager.R;

public class SingleAppPermissions {
    private SparseArray<PermissionItem> mAppPermissionMap = null;

    public synchronized SparseArray<PermissionItem> getPermissionMaps() {
        if (this.mAppPermissionMap == null) {
            this.mAppPermissionMap = new SparseArray();
            this.mAppPermissionMap.put(ShareCfg.MSG_INDEX, new PermissionItem(R.string.MsgPermissionName_gongxin, 4, PermissionCategory.SMS_GROUP));
            this.mAppPermissionMap.put(ShareCfg.SEND_SMS_INDEX, new PermissionItem(R.string.PayProtectPermission_gongxin, 32, PermissionCategory.SMS_GROUP));
            this.mAppPermissionMap.put(ShareCfg.SEND_MMS_INDEX, new PermissionItem(R.string.SendMMSPermission, 8192, PermissionCategory.SMS_GROUP));
            this.mAppPermissionMap.put(ShareCfg.CALL_PHONE_INDEX, new PermissionItem(R.string.CallPhonePermission, 64, PermissionCategory.PHONE_GROUP));
            this.mAppPermissionMap.put(ShareCfg.CALLLOG_INDEX, new PermissionItem(R.string.CalllogPermissionName, 2, PermissionCategory.PHONE_GROUP));
            this.mAppPermissionMap.put(ShareCfg.WRITE_CALLLOG_INDEX, new PermissionItem(R.string.write_calllog_permission_name, 32768, PermissionCategory.PHONE_GROUP));
            this.mAppPermissionMap.put(ShareCfg.PHONE_CODE_INDEX, new PermissionItem(R.string.ReadPhoneCodePermission, 16, PermissionCategory.PHONE_GROUP));
            this.mAppPermissionMap.put(ShareCfg.CONTACTS_INDEX, new PermissionItem(R.string.ContactsPermissionName, 1, PermissionCategory.CONTACT_GROUP));
            this.mAppPermissionMap.put(ShareCfg.WRITE_CONTACTS_INDEX, new PermissionItem(R.string.write_contacts_permission_name, 16384, PermissionCategory.CONTACT_GROUP));
            this.mAppPermissionMap.put(ShareCfg.CAMERA_INDEX, new PermissionItem(R.string.CameraPermission_gongxin, 1024, PermissionCategory.OTHER));
            this.mAppPermissionMap.put(ShareCfg.CALL_LISTENER_INDEX, new PermissionItem(R.string.PhoneRecorderPermissionAdd, 128, PermissionCategory.OTHER));
            this.mAppPermissionMap.put(ShareCfg.READ_HEALTH_DATA_INDEX, new PermissionItem(R.string.permgrouplab_use_sensors, 134217728, PermissionCategory.OTHER));
            this.mAppPermissionMap.put(ShareCfg.LOCATION_INDEX, new PermissionItem(R.string.LocationPermissionName, 8, PermissionCategory.OTHER));
            this.mAppPermissionMap.put(ShareCfg.READ_MOTION_DATA_INDEX, new PermissionItem(R.string.RmdPermissionTitle, 67108864, PermissionCategory.OTHER));
            this.mAppPermissionMap.put(ShareCfg.GET_PACKAGE_LIST_INDEX, new PermissionItem(R.string.GetAppListPermission, 33554432, PermissionCategory.OTHER));
            this.mAppPermissionMap.put(ShareCfg.EDIT_SHORTCUT_INDEX, new PermissionItem(R.string.EditShortcutPermission, 16777216, PermissionCategory.OTHER));
            this.mAppPermissionMap.put(ShareCfg.READ_CALENDAR_INDEX, new PermissionItem(R.string.permission_access_calendar, 2048, PermissionCategory.CALENDAR_GROUP));
            this.mAppPermissionMap.put(ShareCfg.MODIFY_CALENDAR_INDEX, new PermissionItem(R.string.write_calendar_permission_name, ShareCfg.PERMISSION_MODIFY_CALENDAR, PermissionCategory.CALENDAR_GROUP));
            this.mAppPermissionMap.put(ShareCfg.CALL_FORWARD_INDEX, new PermissionItem(R.string.permission_call_forward, 1048576, PermissionCategory.OTHER));
            this.mAppPermissionMap.put(ShareCfg.ACCESS_BROWSER_RECORDS_INDEX, new PermissionItem(R.string.permission_access_browser_records, 1073741824, PermissionCategory.OTHER));
            this.mAppPermissionMap.put(ShareCfg.ADDVIEW_INDEX, new PermissionItem(R.string.DropzoneAppTitle, 536870912, PermissionCategory.OTHER));
        }
        return this.mAppPermissionMap;
    }
}

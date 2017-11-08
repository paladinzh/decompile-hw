package com.huawei.permissionmanager.ui;

import android.content.Context;
import android.util.SparseArray;
import com.google.common.collect.Lists;
import com.huawei.permissionmanager.ui.permission.BluetoothPermission;
import com.huawei.permissionmanager.ui.permission.CallForwardPermission;
import com.huawei.permissionmanager.ui.permission.CallPermission;
import com.huawei.permissionmanager.ui.permission.CameraPermission;
import com.huawei.permissionmanager.ui.permission.EditShortcutPermission;
import com.huawei.permissionmanager.ui.permission.GetAppListPermission;
import com.huawei.permissionmanager.ui.permission.MobileDataPermission;
import com.huawei.permissionmanager.ui.permission.ReadCalendarPermission;
import com.huawei.permissionmanager.ui.permission.ReadCallLogPermission;
import com.huawei.permissionmanager.ui.permission.ReadContactPermission;
import com.huawei.permissionmanager.ui.permission.ReadHealthDataPermission;
import com.huawei.permissionmanager.ui.permission.ReadHistoryBookmarksPermission;
import com.huawei.permissionmanager.ui.permission.ReadLocationPermission;
import com.huawei.permissionmanager.ui.permission.ReadMotionDataPermission;
import com.huawei.permissionmanager.ui.permission.ReadPhoneCodePermission;
import com.huawei.permissionmanager.ui.permission.ReadSmsPermission;
import com.huawei.permissionmanager.ui.permission.RecorderPermission;
import com.huawei.permissionmanager.ui.permission.SendMmsPermission;
import com.huawei.permissionmanager.ui.permission.SendSmsPermission;
import com.huawei.permissionmanager.ui.permission.WifiPermission;
import com.huawei.permissionmanager.ui.permission.WriteCalendarPermission;
import com.huawei.permissionmanager.ui.permission.WriteCallLogPermission;
import com.huawei.permissionmanager.ui.permission.WriteContactPermission;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.R;
import java.util.ArrayList;
import java.util.List;

public class PermissionTableManager {
    private static PermissionTableManager sInstance = null;
    private final String LOG_TAG = "PermissionTableManager";
    private Context mContext;
    private final SparseArray<Permission> mPermissionMap = new SparseArray();
    private ArrayList<Permission> mPermissionTable = new ArrayList();

    private PermissionTableManager(Context context) {
        this.mContext = context;
        initPermissionTable();
    }

    public static PermissionTableManager getInstance(Context context) {
        PermissionTableManager permissionTableManager;
        synchronized (PermissionTableManager.class) {
            if (sInstance == null) {
                sInstance = new PermissionTableManager(context);
            }
            permissionTableManager = sInstance;
        }
        return permissionTableManager;
    }

    public ArrayList<Permission> getPermissionTable() {
        return this.mPermissionTable;
    }

    public Permission getPermissionObjectByPermissionType(int permissionType) {
        for (Permission permission : this.mPermissionTable) {
            if (permissionType == permission.getPermissionCode()) {
                return permission;
            }
        }
        return null;
    }

    public Permission getPermission(int permissionType) {
        return (Permission) this.mPermissionMap.get(permissionType);
    }

    public List<Permission> getAllPermission() {
        return Lists.newArrayList(this.mPermissionTable);
    }

    private void initPermissionTable() {
        Permission permissionReadContact = new ReadContactPermission(this.mContext, R.string.ContactsPermissionName, R.plurals.ContactsPermissionDescription, 1, R.string.popup_format_bg_contact_emui305, R.string.PrivatePermissionType, R.string.none_app_access_contact_new, new OrCheckPackagePermission());
        permissionReadContact.addAndroidPermission(ShareCfg.CALL_AND_CONT_READ_PERMISSION);
        permissionReadContact.addAndroidPermission(ShareCfg.CALL_AND_CONT_WRITE_PERMISSION);
        this.mPermissionTable.add(permissionReadContact);
        Permission permissionReadShortMessage = new ReadSmsPermission(this.mContext, R.string.MsgPermissionName_gongxin, R.plurals.MsgPermissionDescription, 4, R.string.popup_format_bg_message_gongxin_emui305, R.string.PrivatePermissionType, R.string.none_app_access_message_new_gongxin, new OrCheckPackagePermission());
        permissionReadShortMessage.addAndroidPermission(ShareCfg.MSG_RECORD_READ_PERMISSION);
        permissionReadShortMessage.addAndroidPermission(ShareCfg.MSG_RECORD_WRITE_PERMISSION);
        this.mPermissionTable.add(permissionReadShortMessage);
        Permission permissionReadCallLog = new ReadCallLogPermission(this.mContext, R.string.CalllogPermissionName, R.plurals.CalllogPermissionDescription, 2, R.string.popup_format_bg_call_log_emui305, R.string.PrivatePermissionType, R.string.none_app_access_call_log_new, new OrCheckPackagePermission());
        permissionReadCallLog.addAndroidPermission(ShareCfg.CALL_AND_CONT_READ_PERMISSION);
        permissionReadCallLog.addAndroidPermission(ShareCfg.CALL_AND_CONT_WRITE_PERMISSION);
        this.mPermissionTable.add(permissionReadCallLog);
        Permission permissionReadCalendar = new ReadCalendarPermission(this.mContext, R.string.permission_access_calendar, R.plurals.read_calendar_permissiondescription, 2048, R.string.popup_format_bg_read_calendar_emui305, R.string.BasicPermissionType, R.string.none_app_read_calendar_new, new OrCheckPackagePermission());
        permissionReadCalendar.addAndroidPermission(ShareCfg.CALENDAR_PERMISSION);
        this.mPermissionTable.add(permissionReadCalendar);
        Permission permissionModifyCalendar = new WriteCalendarPermission(this.mContext, R.string.write_calendar_permission_name, R.plurals.modify_calendar_permissiondescription, ShareCfg.PERMISSION_MODIFY_CALENDAR, R.string.popup_format_bg_modify_calendar_emui305, R.string.BasicPermissionType, R.string.none_app_modify_calendar_new, new OrCheckPackagePermission());
        permissionModifyCalendar.addAndroidPermission(ShareCfg.CALENDAR_WRITE_PERMISSION);
        this.mPermissionTable.add(permissionModifyCalendar);
        Permission permissionReadPhoneCode = new ReadPhoneCodePermission(this.mContext, R.string.ReadPhoneCodePermission, R.plurals.ReadPhoneCodePermissionDescription, 16, R.string.popup_format_bg_phone_number_emui305, R.string.PrivatePermissionType, R.string.none_app_access_phone_number_new, new OrCheckPackagePermission());
        permissionReadPhoneCode.addAndroidPermission(ShareCfg.PHONE_STATE_PERMISSION);
        this.mPermissionTable.add(permissionReadPhoneCode);
        Permission permissionWriteContact = new WriteContactPermission(this.mContext, R.string.write_contacts_permission_name, R.plurals.WriteContactsPermissionDescription, 16384, R.string.popup_format_bg_contact_write_emui305, R.string.PrivatePermissionType, R.string.none_app_modify_contact, new OrCheckPackagePermission());
        permissionWriteContact.addAndroidPermission(ShareCfg.CALL_AND_CONT_READ_PERMISSION);
        permissionWriteContact.addAndroidPermission(ShareCfg.CALL_AND_CONT_WRITE_PERMISSION);
        this.mPermissionTable.add(permissionWriteContact);
        Permission permissionWriteCallLog = new WriteCallLogPermission(this.mContext, R.string.write_calllog_permission_name, R.plurals.WriteCalllogPermissionDescription, 32768, R.string.popup_format_bg_call_log_write_emui305, R.string.PrivatePermissionType, R.string.none_app_modify_calllog, new OrCheckPackagePermission());
        permissionWriteCallLog.addAndroidPermission(ShareCfg.CALL_AND_CONT_READ_PERMISSION);
        permissionWriteCallLog.addAndroidPermission(ShareCfg.CALL_AND_CONT_WRITE_PERMISSION);
        this.mPermissionTable.add(permissionWriteCallLog);
        Permission permissionEditShortcut = new EditShortcutPermission(this.mContext, R.string.EditShortcutPermission, R.plurals.EditShortcutPermissionDescription, 16777216, R.string.popup_format_edit_shortcut_emui305, R.string.SettingsPermissionType, R.string.none_app_edit_shortcut, new OrCheckPackagePermission());
        permissionEditShortcut.addAndroidPermission(ShareCfg.INSTALL_SHORTCUT_PERMISSION);
        permissionEditShortcut.addAndroidPermission(ShareCfg.UNINSTALL_SHORTCUT_PERMISSION);
        this.mPermissionTable.add(permissionEditShortcut);
        Permission permissionCall = new CallPermission(this.mContext, R.string.CallPhonePermission, R.plurals.CallPhonePermissionDescription, 64, R.string.popup_format_bg_call_emui305, R.string.SecurityPermissionType, R.string.none_app_make_calls_new, new OrCheckPackagePermission());
        permissionCall.addAndroidPermission(ShareCfg.CALL_PHONE_PERMISSION);
        this.mPermissionTable.add(permissionCall);
        Permission permissionPayment = new SendSmsPermission(this.mContext, R.string.PayProtectPermission_gongxin, R.plurals.PayProtectPermissionDescription_gongxin, 32, R.string.popup_format_bg_sms_gongxin_emui305, R.string.SecurityPermissionType, R.string.none_app_send_msg_gongxin, new OrCheckPackagePermission());
        permissionPayment.addAndroidPermission(ShareCfg.SEND_SHORT_MESSAGE_PERMISSION);
        this.mPermissionTable.add(permissionPayment);
        if (ShareCfg.isControl) {
            Permission permissionSendMMS = new SendMmsPermission(this.mContext, R.string.SendMMSPermission, R.plurals.MMSSendPermissionDescription, 8192, R.string.popup_format_bg_mms_emui305, R.string.SettingsPermissionType, R.string.none_app_send_mms, new OrCheckPackagePermission());
            permissionSendMMS.addAndroidPermission(ShareCfg.SEND_SHORT_MESSAGE_PERMISSION);
            this.mPermissionTable.add(permissionSendMMS);
        }
        Permission permissionCamera = new CameraPermission(this.mContext, R.string.CameraPermission_gongxin, R.plurals.CareraPermissionDescription_gongxin, 1024, R.string.popup_format_use_camera_gongxin_emui305, R.string.BasicPermissionType, R.string.none_app_take_pic_stealthilyAdd_gongxin, new OrCheckPackagePermission());
        permissionCamera.addAndroidPermission(ShareCfg.CAMERA_PERMISSION);
        this.mPermissionTable.add(permissionCamera);
        Permission permissionCallRecorder = new RecorderPermission(this.mContext, R.string.PhoneRecorderPermissionAdd, R.plurals.PhoneRecorderPermissionDescriptionAdd, 128, R.string.popup_format_recoding_callAdd_emui305, R.string.BasicPermissionType, R.string.none_app_monitor_callsAdd, new AndCheckPackagePermission());
        permissionCallRecorder.addAndroidPermission(ShareCfg.RECORD_AUDIO_PERMISSION);
        this.mPermissionTable.add(permissionCallRecorder);
        Permission permissionChangeNetwork = new MobileDataPermission(this.mContext, R.string.Open_Network_Permission, R.plurals.NetWorkPermissionDescriptionAdd, 4194304, R.string.popup_format_open_network_emui305, R.string.SettingsPermissionType, R.string.none_app_open_network, new AndCheckPackagePermission());
        permissionChangeNetwork.addAndroidPermission(ShareCfg.CHANGE_NETWORK_PERMISSION);
        this.mPermissionTable.add(permissionChangeNetwork);
        Permission permissionChangeWifi = new WifiPermission(this.mContext, R.string.Open_Wifi_Permission, R.plurals.OpenWifiPermissionDescription, 2097152, R.string.popup_format_open_wifi_emui305, R.string.SettingsPermissionType, R.string.none_app_open_wifi, new AndCheckPackagePermission());
        permissionChangeWifi.addAndroidPermission(ShareCfg.CHANGE_WIFI_PERMISSION);
        this.mPermissionTable.add(permissionChangeWifi);
        Permission permissionOpenBT = new BluetoothPermission(this.mContext, R.string.Open_BT_Permission, R.plurals.OpenBTPermissionDescription, 8388608, R.string.popup_format_open_bt_emui305, R.string.SettingsPermissionType, R.string.none_app_open_bt, new AndCheckPackagePermission());
        permissionOpenBT.addAndroidPermission(ShareCfg.OPEN_BLUE_TOOTH);
        this.mPermissionTable.add(permissionOpenBT);
        this.mPermissionTable.add(new ReadMotionDataPermission(this.mContext, R.string.RmdPermissionTitle, R.plurals.RmdRestrictedAppDescription_New, 67108864, R.string.RmdPopupMsg_Des, R.string.PrivacyPermissionType, R.string.RmdNoneApp, new AndCheckPackagePermission()));
        Permission permissionReadHealthData = new ReadHealthDataPermission(this.mContext, R.string.permgrouplab_use_sensors, R.plurals.RhdRestrictedAppDescription, 134217728, R.string.permgroupdesc_sensors, R.string.BasicPermissionType, R.string.RhdNoneApp, new AndCheckPackagePermission());
        permissionReadHealthData.addAndroidPermission(ShareCfg.USE_BODY_SENSORS);
        this.mPermissionTable.add(permissionReadHealthData);
        Permission permissionReadLocation = new ReadLocationPermission(this.mContext, R.string.permgrouplab_get_location, R.plurals.LocationPermissionDescription, 8, R.string.permgroupdesc_location, R.string.BasicPermissionType, R.string.none_app_access_location_new, new OrCheckPackagePermission());
        permissionReadLocation.addAndroidPermission(ShareCfg.LOCATION_COARSE_PERMISSION);
        permissionReadLocation.addAndroidPermission(ShareCfg.LOCATION_FINE_PERMISSION);
        this.mPermissionTable.add(permissionReadLocation);
        this.mPermissionTable.add(new GetAppListPermission(this.mContext, R.string.GetAppListPermission, R.plurals.GetAppListPermissionDescription, 33554432, R.string.popup_format_get_applist_emui305, R.string.PrivacyPermissionType, R.string.none_app_get_applist, new AndCheckPackagePermission()));
        Permission permissionAccessBrowserRecords = new ReadHistoryBookmarksPermission(this.mContext, R.string.permission_access_browser_records, R.plurals.permission_access_browser_records_permissiondescription, 1073741824, R.string.popup_format_bg_access_browser_records_emui305, R.string.PrivacyPermissionType, R.string.none_app_access_browser_records_new, new AndCheckPackagePermission());
        permissionAccessBrowserRecords.addAndroidPermission(ShareCfg.READ_HISTORY_BOOKMARKS_PERMISSION);
        this.mPermissionTable.add(permissionAccessBrowserRecords);
        Permission permissionCallForward = new CallForwardPermission(this.mContext, R.string.permission_call_forward, R.plurals.permission_call_forward_permissiondescription, 1048576, R.string.popup_format_bg_call_forward_emui305, R.string.BasicPermissionType, R.string.none_app_call_forward_new, new AndCheckPackagePermission());
        permissionCallForward.addAndroidPermission(ShareCfg.CALL_PHONE_PERMISSION);
        this.mPermissionTable.add(permissionCallForward);
        for (Permission p : this.mPermissionTable) {
            this.mPermissionMap.put(p.getPermissionCode(), p);
        }
    }
}

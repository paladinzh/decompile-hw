package com.huawei.permissionmanager.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.SparseIntArray;
import android.util.Xml;
import com.huawei.permission.HoldServiceConst;
import com.huawei.permissionmanager.db.AppInfo;
import com.huawei.permissionmanager.db.DBHelper;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.grule.GRuleManager;
import com.huawei.systemmanager.comm.grule.scene.monitor.MonitorScenario;
import com.huawei.systemmanager.customize.CustomizeManager;
import com.huawei.systemmanager.util.HwLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;

public class ShareLib {
    private static String FILE_PERMISSION_PARAMETER = CustomizeManager.composeCustFileName("xml/hw_permission_parameter.xml");
    private static final String TAG = "ShareLib";
    private static SparseIntArray mBlockedNotificationStringIdMap = null;
    private static SparseIntArray mBlockedSinglalStringIdMap = null;
    private static Map<HwPermissionInfo, Integer> mPmerssionTypeMap = null;
    private static SparseIntArray mRegisterPerms = null;

    public static ArrayList<HwPermissionInfo> getControlPermissions() {
        ArrayList<HwPermissionInfo> mPermissonList = new ArrayList();
        mPermissonList.add(ShareCfg.CONTACTS_INFO);
        mPermissonList.add(ShareCfg.MSG_INFO);
        mPermissonList.add(ShareCfg.CALLLOG_INFO);
        mPermissonList.add(ShareCfg.LOCATION_INFO);
        mPermissonList.add(ShareCfg.PHONE_CODE_INFO);
        mPermissonList.add(ShareCfg.CALL_LISTENER_INFO);
        mPermissonList.add(ShareCfg.CAMERA_INFO);
        mPermissonList.add(ShareCfg.SEND_SMS_INFO);
        mPermissonList.add(ShareCfg.CALL_PHONE_INFO);
        mPermissonList.add(ShareCfg.SEND_MMS_INFO);
        mPermissonList.add(ShareCfg.WRITE_CONTACTS_INFO);
        mPermissonList.add(ShareCfg.WRITE_CALLLOG_INFO);
        mPermissonList.add(ShareCfg.CHANGE_NETWORK_STATE);
        mPermissonList.add(ShareCfg.CHANGE_WIFI_STATE);
        mPermissonList.add(ShareCfg.OPEN_BT);
        mPermissonList.add(ShareCfg.EDIT_SHORTCUT);
        mPermissonList.add(ShareCfg.GET_PACKAGE_LIST);
        mPermissonList.add(ShareCfg.RMD_HW_PERMISSION_INFO);
        mPermissonList.add(ShareCfg.RHD_HW_PERMISSION_INFO);
        mPermissonList.add(ShareCfg.ACCESS_CALENDAR);
        mPermissonList.add(ShareCfg.MODIFY_CALENDAR);
        mPermissonList.add(ShareCfg.ACCESS_BROWSER_RECORDS);
        mPermissonList.add(ShareCfg.CALL_FORWARD);
        return mPermissonList;
    }

    public static synchronized Map<HwPermissionInfo, Integer> getPermissionTypeMaps() {
        Map<HwPermissionInfo, Integer> map;
        synchronized (ShareLib.class) {
            if (mPmerssionTypeMap == null) {
                mPmerssionTypeMap = new HashMap();
                mPmerssionTypeMap.put(ShareCfg.CONTACTS_INFO, Integer.valueOf(1));
                mPmerssionTypeMap.put(ShareCfg.MSG_INFO, Integer.valueOf(4));
                mPmerssionTypeMap.put(ShareCfg.CALLLOG_INFO, Integer.valueOf(2));
                mPmerssionTypeMap.put(ShareCfg.LOCATION_INFO, Integer.valueOf(8));
                mPmerssionTypeMap.put(ShareCfg.PHONE_CODE_INFO, Integer.valueOf(16));
                mPmerssionTypeMap.put(ShareCfg.CALL_LISTENER_INFO, Integer.valueOf(128));
                mPmerssionTypeMap.put(ShareCfg.CAMERA_INFO, Integer.valueOf(1024));
                mPmerssionTypeMap.put(ShareCfg.SEND_SMS_INFO, Integer.valueOf(32));
                mPmerssionTypeMap.put(ShareCfg.CALL_PHONE_INFO, Integer.valueOf(64));
                mPmerssionTypeMap.put(ShareCfg.SEND_MMS_INFO, Integer.valueOf(8192));
                mPmerssionTypeMap.put(ShareCfg.WRITE_CONTACTS_INFO, Integer.valueOf(16384));
                mPmerssionTypeMap.put(ShareCfg.WRITE_CALLLOG_INFO, Integer.valueOf(32768));
                mPmerssionTypeMap.put(ShareCfg.CHANGE_NETWORK_STATE, Integer.valueOf(4194304));
                mPmerssionTypeMap.put(ShareCfg.CHANGE_WIFI_STATE, Integer.valueOf(2097152));
                mPmerssionTypeMap.put(ShareCfg.OPEN_BT, Integer.valueOf(8388608));
                mPmerssionTypeMap.put(ShareCfg.EDIT_SHORTCUT, Integer.valueOf(16777216));
                mPmerssionTypeMap.put(ShareCfg.GET_PACKAGE_LIST, Integer.valueOf(33554432));
                mPmerssionTypeMap.put(ShareCfg.RMD_HW_PERMISSION_INFO, Integer.valueOf(67108864));
                mPmerssionTypeMap.put(ShareCfg.RHD_HW_PERMISSION_INFO, Integer.valueOf(134217728));
                mPmerssionTypeMap.put(ShareCfg.ACCESS_CALENDAR, Integer.valueOf(2048));
                mPmerssionTypeMap.put(ShareCfg.MODIFY_CALENDAR, Integer.valueOf(ShareCfg.PERMISSION_MODIFY_CALENDAR));
                mPmerssionTypeMap.put(ShareCfg.CALL_FORWARD, Integer.valueOf(1048576));
                mPmerssionTypeMap.put(ShareCfg.ACCESS_BROWSER_RECORDS, Integer.valueOf(1073741824));
            }
            map = mPmerssionTypeMap;
        }
        return map;
    }

    public static ApplicationInfo getApplicationInfo(Context context, int uid) {
        ApplicationInfo resultApp = null;
        PackageManager pm = context.getPackageManager();
        String[] pkaName = pm.getPackagesForUid(uid);
        if (pkaName != null && pkaName.length > 0) {
            try {
                resultApp = pm.getApplicationInfo(pkaName[0], 0);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return resultApp;
    }

    public static AppInfo getAppInfoByUidAndPid(Context context, int uid, int pid) {
        return getAppInfoByUidAndPid(context, uid, pid, MonitorScenario.SCENARIO_PERMISSION);
    }

    public static AppInfo getAppInfoByUidAndPid(Context context, int uid, int pid, String monitorScenario) {
        if (pid == HoldServiceConst.FAKE_PID) {
            ApplicationInfo applicationInfo = getApplicationInfo(context, uid);
            if (applicationInfo == null) {
                return null;
            }
            return new AppInfo(context, applicationInfo);
        }
        List<RunningAppProcessInfo> list = null;
        try {
            list = ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (list == null) {
            return null;
        }
        for (RunningAppProcessInfo runningInfo : list) {
            int runningPid = runningInfo.pid;
            int runningUid = runningInfo.uid;
            if (runningPid == pid && runningUid == uid) {
                return getAppInfo(context, runningInfo.pkgList, monitorScenario);
            }
        }
        return null;
    }

    private static AppInfo getAppInfo(Context context, String[] pkgNameList, String monitorScenario) {
        PackageManager pm = context.getPackageManager();
        for (String pkgName : pkgNameList) {
            if (GRuleManager.getInstance().shouldMonitor(context, monitorScenario, pkgName)) {
                try {
                    return new AppInfo(context, pm.getApplicationInfo(pkgName, 0));
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            HwLog.d(TAG, "getApplication pkgName" + pkgName + " should not be monitor");
        }
        HwLog.d(TAG, "not find the appinfo, this is usually not normal, pkgNameList " + Arrays.toString(pkgNameList));
        return null;
    }

    public static int getNoAppIconId(int permissionType) {
        switch (permissionType) {
            case 1:
            case 16384:
                return R.drawable.ic_no_contact;
            case 2:
            case 64:
            case 128:
            case 32768:
            case 1048576:
                return R.drawable.ic_call_emptypage;
            case 4:
            case 32:
            case 8192:
                return R.drawable.ic_message_emptypage;
            case 8:
                return R.drawable.ic_no_location;
            case 16:
                return R.drawable.ic_no_phoneid;
            case 1024:
                return R.drawable.ic_no_camera;
            case 2048:
            case ShareCfg.PERMISSION_MODIFY_CALENDAR /*268435456*/:
                return R.drawable.ic_no_calender;
            case 2097152:
            case 4194304:
            case 8388608:
                return R.drawable.ic_no_apps;
            default:
                return R.drawable.ic_no_apps;
        }
    }

    public static int setDefaultSpinnerValue(int permissionType) {
        switch (permissionType) {
            case 0:
                return 1;
            case 1:
                return 0;
            case 2:
                return 2;
            default:
                return 3;
        }
    }

    public static Map<String, Integer> findPermissionParameterFromXml(Context context) {
        RuntimeException e1;
        Throwable th;
        Exception e;
        Map<String, Integer> map = new HashMap();
        FileInputStream fileInputStream = null;
        try {
            XmlPullParser xrp;
            File custParameterFile = new File(FILE_PERMISSION_PARAMETER);
            if (custParameterFile.exists()) {
                FileInputStream fin = new FileInputStream(custParameterFile);
                try {
                    xrp = Xml.newPullParser();
                    xrp.setInput(fin, null);
                    fileInputStream = fin;
                } catch (RuntimeException e2) {
                    e1 = e2;
                    fileInputStream = fin;
                    try {
                        e1.printStackTrace();
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e3) {
                                e3.printStackTrace();
                            }
                        }
                        return map;
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e32) {
                                e32.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Exception e4) {
                    e = e4;
                    fileInputStream = fin;
                    e.printStackTrace();
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e322) {
                            e322.printStackTrace();
                        }
                    }
                    return map;
                } catch (Throwable th3) {
                    th = th3;
                    fileInputStream = fin;
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw th;
                }
            }
            xrp = context.getResources().getXml(R.xml.permission_parameter);
            while (xrp.getEventType() != 1) {
                if (xrp.getEventType() == 2) {
                    if ("Parameter".equals(xrp.getName())) {
                        map.put(xrp.getAttributeValue(null, "name"), Integer.valueOf(Integer.parseInt(xrp.getAttributeValue(null, DBHelper.VALUE))));
                    }
                }
                xrp.next();
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e3222) {
                    e3222.printStackTrace();
                }
            }
        } catch (RuntimeException e5) {
            e1 = e5;
        } catch (Exception e6) {
            e = e6;
        }
        return map;
    }

    public static synchronized SparseIntArray getRegisterPerms() {
        SparseIntArray sparseIntArray;
        synchronized (ShareLib.class) {
            if (mRegisterPerms == null) {
                mRegisterPerms = new SparseIntArray();
                mRegisterPerms.put(67108864, 0);
                mRegisterPerms.put(134217728, 0);
            }
            sparseIntArray = mRegisterPerms;
        }
        return sparseIntArray;
    }

    public static synchronized SparseIntArray getBlockedNotificationStringIdMap() {
        SparseIntArray sparseIntArray;
        synchronized (ShareLib.class) {
            if (mBlockedNotificationStringIdMap == null) {
                mBlockedNotificationStringIdMap = new SparseIntArray();
                mBlockedNotificationStringIdMap.put(1, R.string.ContactsPermissionName);
                mBlockedNotificationStringIdMap.put(4, R.string.MsgPermissionName_gongxin);
                mBlockedNotificationStringIdMap.put(2, R.string.CalllogPermissionName);
                mBlockedNotificationStringIdMap.put(2048, R.string.ReadCalendarPermission);
                mBlockedNotificationStringIdMap.put(8, R.string.LocationPermissionName);
                mBlockedNotificationStringIdMap.put(16, R.string.ReadPhoneCodePermission);
                mBlockedNotificationStringIdMap.put(128, R.string.PhoneRecorderPermissionAdd);
                mBlockedNotificationStringIdMap.put(1024, R.string.CameraPermission_gongxin);
                mBlockedNotificationStringIdMap.put(32, R.string.PayProtectPermission_gongxin);
                mBlockedNotificationStringIdMap.put(64, R.string.CallPhonePermission);
                mBlockedNotificationStringIdMap.put(8192, R.string.SendMMSPermission);
                mBlockedNotificationStringIdMap.put(16384, R.string.WriteContactsPermissionName);
                mBlockedNotificationStringIdMap.put(32768, R.string.WriteCalllogPermissionName);
                mBlockedNotificationStringIdMap.put(4194304, R.string.Open_Network_Permission);
                mBlockedNotificationStringIdMap.put(2097152, R.string.Open_Wifi_Permission);
                mBlockedNotificationStringIdMap.put(8388608, R.string.Open_BT_Permission);
                mBlockedNotificationStringIdMap.put(16777216, R.string.Edit_shortcut_Permission);
                mBlockedNotificationStringIdMap.put(33554432, R.string.get_applist_permission);
                mBlockedNotificationStringIdMap.put(67108864, R.string.RmdNotificationPanelTitle);
                mBlockedNotificationStringIdMap.put(134217728, R.string.permgrouplab_use_sensors);
                mBlockedNotificationStringIdMap.put(2048, R.string.permission_access_calendar);
                mBlockedNotificationStringIdMap.put(ShareCfg.PERMISSION_MODIFY_CALENDAR, R.string.permission_modify_calendar);
                mBlockedNotificationStringIdMap.put(1073741824, R.string.permission_access_browser_records);
                mBlockedNotificationStringIdMap.put(1048576, R.string.permission_call_forward);
            }
            sparseIntArray = mBlockedNotificationStringIdMap;
        }
        return sparseIntArray;
    }

    public static synchronized SparseIntArray getBlockedSingalStringIdMap() {
        SparseIntArray sparseIntArray;
        synchronized (ShareLib.class) {
            if (mBlockedSinglalStringIdMap == null) {
                mBlockedSinglalStringIdMap = new SparseIntArray();
                mBlockedSinglalStringIdMap.put(1, R.string.permission_notification_read_contacts);
                mBlockedSinglalStringIdMap.put(4, R.string.permission_notification_read_sms);
                mBlockedSinglalStringIdMap.put(2, R.string.permission_notification_read_calllog);
                mBlockedSinglalStringIdMap.put(2048, R.string.permission_notification_read_calendar);
                mBlockedSinglalStringIdMap.put(8, R.string.permission_notification_read_location);
                mBlockedSinglalStringIdMap.put(16, R.string.permission_notification_read_phonecode);
                mBlockedSinglalStringIdMap.put(128, R.string.permission_notification_record_audio);
                mBlockedSinglalStringIdMap.put(1024, R.string.permission_notification_use_camera);
                mBlockedSinglalStringIdMap.put(32, R.string.permission_notification_send_sms);
                mBlockedSinglalStringIdMap.put(64, R.string.permission_notification_make_call);
                mBlockedSinglalStringIdMap.put(8192, R.string.permission_notification_send_mms);
                mBlockedSinglalStringIdMap.put(16384, R.string.permission_notification_write_contacts);
                mBlockedSinglalStringIdMap.put(32768, R.string.permission_notification_write_calllog);
                mBlockedSinglalStringIdMap.put(4194304, R.string.permission_notification_open_network);
                mBlockedSinglalStringIdMap.put(2097152, R.string.permission_notification_open_wifi);
                mBlockedSinglalStringIdMap.put(8388608, R.string.permission_notification_open_bt);
                mBlockedSinglalStringIdMap.put(16777216, R.string.permission_notification_edit_shortcut);
                mBlockedSinglalStringIdMap.put(33554432, R.string.permission_notification_get_applist);
                mBlockedSinglalStringIdMap.put(67108864, R.string.RmdNotificationTitle);
                mBlockedSinglalStringIdMap.put(134217728, R.string.RhdNotificationTitle);
                mBlockedSinglalStringIdMap.put(2048, R.string.permission_access_calendar);
                mBlockedSinglalStringIdMap.put(ShareCfg.PERMISSION_MODIFY_CALENDAR, R.string.permission_modify_calendar);
                mBlockedSinglalStringIdMap.put(1073741824, R.string.permission_access_browser_records);
                mBlockedSinglalStringIdMap.put(1048576, R.string.permission_call_forward);
            }
            sparseIntArray = mBlockedSinglalStringIdMap;
        }
        return sparseIntArray;
    }
}

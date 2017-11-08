package com.huawei.systemmanager.rainbow.vaguerule;

import android.database.Cursor;

public class VaguePermissionInfo {
    public String mNetworkData = "true";
    public String mNetworkWifi = "true";
    public int mPermissionAddview;
    public int mPermissionBootstartup;
    public int mPermissionCfg = 0;
    public int mPermissionCode = 0;
    public int mPermissionGetApplist;
    public int mPermissionNotificationSignal;
    public int mPermissionSendNotification;
    public String mPermissionTrust = "false";

    public void parseFrom(Cursor cursor) {
        if (cursor != null) {
            this.mPermissionCode = cursor.getInt(cursor.getColumnIndex("permissionCode"));
            this.mPermissionCfg = cursor.getInt(cursor.getColumnIndex("permissionCfg"));
            this.mPermissionTrust = cursor.getString(cursor.getColumnIndex("trust"));
            this.mPermissionBootstartup = Integer.parseInt(cursor.getString(cursor.getColumnIndex("bootstartupDefaultValue")));
            this.mPermissionAddview = Integer.parseInt(cursor.getString(cursor.getColumnIndex("addviewDefaultValue")));
            this.mPermissionSendNotification = Integer.parseInt(cursor.getString(cursor.getColumnIndex("sendNotificationDefaultValue")));
            this.mPermissionNotificationSignal = Integer.parseInt(cursor.getString(cursor.getColumnIndex("sendNotificationDefaultValue")));
            this.mPermissionGetApplist = Integer.parseInt(cursor.getString(cursor.getColumnIndex("getapplistDefaultValue")));
            this.mNetworkData = cursor.getString(cursor.getColumnIndex("netDataPermission"));
            this.mNetworkWifi = cursor.getString(cursor.getColumnIndex("netWifiPermission"));
        }
    }
}

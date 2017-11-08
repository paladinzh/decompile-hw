package com.huawei.systemmanager.netassistant.netapp.control;

public class AppPermissionController {
    public static final int UID_ALL = 0;
    private int permission;
    private boolean success = false;
    private int type;
    private int uid;
    private int uidType;

    public AppPermissionController(int permission, int type, int uid, int uidType) {
        this.permission = permission;
        this.type = type;
        this.uid = uid;
        this.uidType = uidType;
    }

    public int getPermission() {
        return this.permission;
    }

    public void setPermission(int permission) {
        this.permission = permission;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getUid() {
        return this.uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public void setSuccess(boolean status) {
        this.success = status;
    }

    public int getUidType() {
        return this.uidType;
    }

    public void setUidType(int uidType) {
        this.uidType = uidType;
    }
}

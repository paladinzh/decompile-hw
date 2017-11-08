package com.huawei.permission;

import android.content.Context;
import android.net.Uri;
import com.huawei.permissionmanager.db.DBAdapter;
import java.util.HashMap;

public class RuntimePermissionHandler {
    private Context mContext = null;
    private HashMap<String, Long> runtimePerms = new HashMap();

    public RuntimePermissionHandler(Context context) {
        this.mContext = context;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Uri addRuntimePermission(String packageName, int permissionType, int uid) {
        synchronized (this.runtimePerms) {
            Long perms = (Long) this.runtimePerms.get(packageName);
            if (perms == null) {
                this.runtimePerms.put(packageName, Long.valueOf((long) permissionType));
            } else if ((perms.longValue() & ((long) permissionType)) != 0) {
                return null;
            } else {
                this.runtimePerms.put(packageName, Long.valueOf(perms.longValue() | ((long) permissionType)));
            }
        }
    }

    public void removeRuntimePermission(String packageName) {
        synchronized (this.runtimePerms) {
            if (((Long) this.runtimePerms.get(packageName)) != null) {
                this.runtimePerms.remove(packageName);
            }
        }
        DBAdapter.removeFromRuntimeTable(this.mContext, packageName);
    }
}

package com.huawei.permissionmanager.ui.history;

import android.content.Context;
import com.huawei.permissionmanager.ui.Permission;
import com.huawei.systemmanager.comm.component.AppItem;
import com.huawei.systemmanager.util.app.HsmPkgInfo;

public class HistoryItem extends AppItem {
    private long historyCount = 0;
    private long latestTime = 0;
    private final Permission mPermission;

    public HistoryItem(HsmPkgInfo info, Permission permission, long historyCount, long latestTime) {
        super(info);
        this.mPermission = permission;
        this.historyCount = historyCount;
        this.latestTime = latestTime;
    }

    public String getHistoryDescription(Context ctx) {
        String description = "";
        try {
            description = ctx.getString(this.mPermission.getHistoryStringId(), new Object[]{getLabel()});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return description;
    }

    public long getHistoryCount() {
        return this.historyCount;
    }

    public void setHistoryCount(long historyCount) {
        this.historyCount = historyCount;
    }

    public Permission getmPermission() {
        return this.mPermission;
    }

    public long getLatestTime() {
        return this.latestTime;
    }

    public void setLatestTime(long latestTime) {
        this.latestTime = latestTime;
    }
}

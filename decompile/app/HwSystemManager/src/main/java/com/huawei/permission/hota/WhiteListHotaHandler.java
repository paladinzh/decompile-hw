package com.huawei.permission.hota;

import android.content.Context;
import com.huawei.permissionmanager.db.DBAdapter;
import com.huawei.systemmanager.comm.grule.rules.xml.PermissionNotMonitorListRule;
import com.huawei.systemmanager.customize.HotaHandler;

public class WhiteListHotaHandler extends HotaHandler {
    private static final String FILE = PermissionNotMonitorListRule.XML_DISK_CUST;
    private Context mContext = null;

    public WhiteListHotaHandler(String fileName) {
        super(fileName);
    }

    public WhiteListHotaHandler(Context context) {
        super(FILE);
        this.mContext = context;
    }

    protected void onConfigDeleted() {
        super.onConfigDeleted();
        DBAdapter.getInstance(this.mContext).checkConsistency();
    }

    protected void onConfigCreated() {
        super.onConfigCreated();
        DBAdapter.getInstance(this.mContext).checkConsistency();
    }

    protected void onConfigUpdated() {
        super.onConfigUpdated();
        DBAdapter.getInstance(this.mContext).checkConsistency();
    }
}

package com.huawei.permissionmanager.ui;

import android.app.Activity;
import android.app.IHwActivitySplitterImpl;
import android.common.HwFrameworkFactory;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.systemmanager.comm.module.ModulePermissionMgr;
import com.huawei.systemmanager.util.HwLog;

public class JumpActvity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Intent intent = ModulePermissionMgr.getIntent(this);
            IHwActivitySplitterImpl splitter = HwFrameworkFactory.getHwActivitySplitterImpl(this, false);
            if (splitter != null && splitter.isSplitMode()) {
                intent.putExtra("huawei.intent.extra.JUMPED_ACTIVITY", true);
            }
            startActivity(intent);
            finish();
        } catch (Exception e) {
            HwLog.e("JumpActvity", "JumpActvity error", e);
        }
    }
}

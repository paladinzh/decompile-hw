package com.huawei.systemmanager.adblock.ui.view.dlblock;

import android.os.Bundle;
import com.huawei.systemmanager.adblock.ui.view.dlblock.fragment.DlBlockRecordListFragment;
import com.huawei.systemmanager.emui.activities.HsmActivity;

public class DlBlockRecordListActivity extends HsmActivity {
    public boolean isSupprotMultiUser() {
        return false;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null || getIntent() != null) {
            getFragmentManager().beginTransaction().replace(16908290, new DlBlockRecordListFragment()).commit();
        }
    }
}

package com.huawei.harassmentinterception.ui;

import android.app.Fragment;
import com.huawei.systemmanager.comm.component.SingleFragmentActivity;

public class KeywordsActivity extends SingleFragmentActivity {
    protected Fragment buildFragment() {
        return new KeywordsFragment();
    }
}

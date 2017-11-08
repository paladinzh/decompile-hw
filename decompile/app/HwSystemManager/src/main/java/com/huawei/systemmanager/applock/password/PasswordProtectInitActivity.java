package com.huawei.systemmanager.applock.password;

import android.os.Bundle;
import com.huawei.systemmanager.emui.activities.HsmActivity;

public class PasswordProtectInitActivity extends HsmActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflatePinAuth();
    }

    private void inflatePinAuth() {
        getFragmentManager().beginTransaction().replace(16908290, new PasswordProtectInitFragment()).commit();
    }
}

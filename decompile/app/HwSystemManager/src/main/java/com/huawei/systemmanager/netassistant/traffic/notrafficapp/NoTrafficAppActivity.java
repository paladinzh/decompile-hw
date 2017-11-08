package com.huawei.systemmanager.netassistant.traffic.notrafficapp;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.netassistant.util.CommonConstantUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.util.HwLog;

public class NoTrafficAppActivity extends HsmActivity {
    public static final String TAG = "NoTrafficAppActivity";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_fragment_activity);
        Intent intent = getIntent();
        if (intent == null || TextUtils.isEmpty(intent.getStringExtra(CommonConstantUtil.KEY_NETASSISTANT_IMSI))) {
            HwLog.i(TAG, "onCreate , arg is wrong finish");
            finish();
            return;
        }
        Fragment fragment = NoTrafficAppFragment.newInstance(intent.getExtras());
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.contents, fragment);
        fragmentTransaction.commit();
    }
}

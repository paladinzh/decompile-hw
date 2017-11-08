package com.huawei.systemmanager.mainscreen;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;

public class AboutActivity extends HsmActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen_app_about);
        ViewUtil.initEmptyViewMargin(GlobalContext.getContext(), (LinearLayout) findViewById(R.id.ll_app));
        ((TextView) findViewById(R.id.app_version)).setText(getVersionName());
        ((TextView) findViewById(R.id.app_privacypolicy)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    AboutActivity.this.startActivity(new Intent("com.android.settings.HuaweiPrivacyPolicyActivity"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String getVersionName() {
        String versionName = "";
        try {
            PackageInfo pi = PackageManagerWrapper.getPackageInfo(getPackageManager(), getPackageName(), 0);
            if (!TextUtils.isEmpty(pi.versionName)) {
                versionName = pi.versionName;
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return getString(R.string.version, new Object[]{versionName});
    }
}

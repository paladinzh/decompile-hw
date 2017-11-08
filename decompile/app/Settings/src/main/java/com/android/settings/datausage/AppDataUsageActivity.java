package com.android.settings.datausage;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import com.android.settings.SettingsActivity;
import com.android.settingslib.AppItem;

public class AppDataUsageActivity extends SettingsActivity {
    protected void onCreate(Bundle savedInstanceState) {
        String packageName;
        Intent intent = getIntent();
        if (intent.getData() != null) {
            packageName = intent.getData().getSchemeSpecificPart();
        } else {
            packageName = "";
        }
        try {
            int uid = getPackageManager().getPackageUid(packageName, 0);
            Bundle args = new Bundle();
            AppItem appItem = new AppItem(uid);
            appItem.addUid(uid);
            args.putParcelable("app_item", appItem);
            intent.putExtra(":settings:show_fragment_args", args);
            intent.putExtra(":settings:show_fragment", AppDataUsage.class.getName());
            intent.putExtra(":settings:show_fragment_title_resid", 2131626899);
            super.onCreate(savedInstanceState);
        } catch (NameNotFoundException e) {
            Log.w("AppDataUsageActivity", "invalid package: " + packageName);
            try {
                super.onCreate(savedInstanceState);
            } catch (Exception e2) {
            } finally {
                finish();
            }
        }
    }

    protected boolean isValidFragment(String fragmentName) {
        if (super.isValidFragment(fragmentName)) {
            return true;
        }
        return AppDataUsage.class.getName().equals(fragmentName);
    }
}
